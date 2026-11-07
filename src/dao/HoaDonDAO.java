package dao;

import entity.*;
import utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Hóa Đơn và các Chi Tiết Đa Hình.
 * Áp dụng nghiêm ngặt JDBC Transaction (ACID) đảm bảo an toàn tuyệt đối khi thanh toán và trừ kho.
 */
public class HoaDonDAO {

    /**
     * Lưu và thanh toán hóa đơn với giao dịch phân tán trên nhiều bảng:
     * 1. Thêm bản ghi HoaDon
     * 2. Duyệt từng dòng đa hình:
     *    - Nếu là ChiTietXuatLo: thêm ChiTietHoaDon, ChiTietXuatLo, và TRỪ TỒN KHO LÔ HÀNG (FEFO).
     *    - Nếu là ChiTietDichVu: thêm ChiTietHoaDon, ChiTietDichVu, ghi nhận hoa hồng KTV.
     * 3. Tích điểm cho khách hàng (100.000đ = 1 điểm).
     * Bắt buộc rollback nếu có bất kỳ lỗi nào hoặc kho không đủ tồn.
     */
    // Thực thi giao dịch thanh toán: lưu thông tin hóa đơn, chi tiết xuất lô, trừ tồn kho và tích lũy điểm thưởng
    public boolean thanhToanHoaDon(HoaDon hoaDon) throws SQLException {
        return thucThiThanhToan(hoaDon, null, null, null, null, 0, 0);
    }

    /** Lưu hóa đơn đổi hàng và phiếu trả trong cùng một transaction. */
    public boolean thanhToanHoaDonKemDoiTra(HoaDon hoaDon, PhieuDoiTra phieuDoiTra) throws SQLException {
        if (phieuDoiTra == null) {
            throw new IllegalArgumentException("Phiếu đổi trả không được để trống");
        }
        return thucThiThanhToan(hoaDon, phieuDoiTra, null, null, null, 0, 0);
    }

    /** Giao dịch POS đầy đủ, bao gồm đổi trả và/hoặc biến động Spa Pass. */
    public boolean thanhToanHoaDonDayDu(HoaDon hoaDon, PhieuDoiTra phieuDoiTra,
                                        String maTheSuDung, TheSpaPass theMoi,
                                        List<String> dsMaPhongHotel, int diemSuDung,
                                        int diemThuHoiDoTraHang) throws SQLException {
        return thucThiThanhToan(hoaDon, phieuDoiTra, maTheSuDung, theMoi,
                dsMaPhongHotel, diemSuDung, diemThuHoiDoTraHang);
    }

    private boolean thucThiThanhToan(HoaDon hoaDon, PhieuDoiTra phieuDoiTra,
                                     String maTheSuDung, TheSpaPass theMoi,
                                     List<String> dsMaPhongHotel, int diemSuDung,
                                     int diemThuHoiDoTraHang) throws SQLException {
        kiemTraHoaDonHopLe(hoaDon);
        try (Connection conn = DBConnection.getNewConnection()) {
            conn.setAutoCommit(false);
            try {
                if (phieuDoiTra != null) {
                    luuPhieuDoiTraTrongTransaction(conn, phieuDoiTra);
                }
                luuHoaDonTrongTransaction(conn, hoaDon);
                truDiemTrongTransaction(conn, hoaDon, diemSuDung, diemThuHoiDoTraHang);
                xuLySpaPassTrongTransaction(conn, hoaDon, maTheSuDung, theMoi);
                hoanTatHotelTrongTransaction(conn, hoaDon.getMaHoaDon(), dsMaPhongHotel);
                conn.commit();
                hoaDon.setTrangThaiHoaDon("Đã thanh toán");
                return true;
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private void truDiemTrongTransaction(Connection conn, HoaDon hoaDon,
                                         int diemSuDung, int diemThuHoiDoTraHang) throws SQLException {
        if (diemSuDung <= 0 && diemThuHoiDoTraHang <= 0) return;
        if (hoaDon.getKhachHang() == null || hoaDon.getKhachHang().getMaKhachHang() == null) {
            throw new SQLException("Không thể trừ điểm khi hóa đơn chưa liên kết khách hàng");
        }
        int diemVuaCong = hoaDon.getThanhTien()
                .divide(new BigDecimal("100000"), 0, RoundingMode.DOWN).intValue();
        String sql = "UPDATE KhachHang SET diemTichLuy="
                + "CASE WHEN diemTichLuy-?-?-?<0 THEN ? ELSE diemTichLuy-?-? END "
                + "WHERE maKhachHang=? AND diemTichLuy>=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, diemVuaCong);
            ps.setInt(2, Math.max(0, diemSuDung));
            ps.setInt(3, Math.max(0, diemThuHoiDoTraHang));
            ps.setInt(4, diemVuaCong);
            ps.setInt(5, Math.max(0, diemSuDung));
            ps.setInt(6, Math.max(0, diemThuHoiDoTraHang));
            ps.setString(7, hoaDon.getKhachHang().getMaKhachHang());
            ps.setInt(8, diemVuaCong + Math.max(0, diemSuDung));
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Điểm dùng thanh toán không đủ hoặc vừa thay đổi ở giao dịch khác; vui lòng tải lại khách hàng");
            }
        }
    }

    private void hoanTatHotelTrongTransaction(Connection conn, String maHoaDon,
                                               List<String> dsMaPhongHotel) throws SQLException {
        if (dsMaPhongHotel == null || dsMaPhongHotel.isEmpty()) return;
        String sqlLichSu = "INSERT INTO LichSuLuuTru (maPhong,tenThuCung,tenChu,sdtChu,thoiGianCheckIn,thoiGianCheckOut,soNgayLuuTru,donGiaNgay,phiPhatSinh,tongTien,ghiChu,maHoaDon,trangThaiThanhToan) "
                + "SELECT maPhong,tenThuCung,tenChu,sdtChu,thoiGianCheckIn,SYSDATETIME(),"
                + "CASE WHEN DATEDIFF(DAY,CAST(thoiGianCheckIn AS DATE),CAST(GETDATE() AS DATE))<1 THEN 1 ELSE DATEDIFF(DAY,CAST(thoiGianCheckIn AS DATE),CAST(GETDATE() AS DATE)) END,"
                + "donGiaNgay,phiDichVuPhatSinh,"
                + "(CASE WHEN DATEDIFF(DAY,CAST(thoiGianCheckIn AS DATE),CAST(GETDATE() AS DATE))<1 THEN 1 ELSE DATEDIFF(DAY,CAST(thoiGianCheckIn AS DATE),CAST(GETDATE() AS DATE)) END)*donGiaNgay+phiDichVuPhatSinh,"
                + "ghiChuChamSoc,?,N'DA_THANH_TOAN' FROM PhongLuuTru WHERE maPhong=? AND trangThai=N'ĐANG CÓ KHÁCH'";
        String sqlPhong = "UPDATE PhongLuuTru SET trangThai=N'ĐANG DỌN DẸP' WHERE maPhong=? AND trangThai=N'ĐANG CÓ KHÁCH'";
        try (PreparedStatement psLichSu = conn.prepareStatement(sqlLichSu);
             PreparedStatement psPhong = conn.prepareStatement(sqlPhong)) {
            for (String maPhong : dsMaPhongHotel) {
                psLichSu.setString(1, maHoaDon);
                psLichSu.setString(2, maPhong);
                if (psLichSu.executeUpdate() != 1) {
                    throw new SQLException("Phòng " + maPhong + " không còn khách hoặc dữ liệu checkout không hợp lệ");
                }
                psPhong.setString(1, maPhong);
                if (psPhong.executeUpdate() != 1) {
                    throw new SQLException("Không thể chuyển phòng " + maPhong + " sang trạng thái dọn dẹp");
                }
            }
        }
    }

    private void xuLySpaPassTrongTransaction(Connection conn, HoaDon hoaDon,
                                              String maTheSuDung, TheSpaPass theMoi) throws SQLException {
        if (maTheSuDung != null && !maTheSuDung.isBlank()) {
            String sqlUpdate = "UPDATE TheSpaPass SET soBuoiDaDung=soBuoiDaDung+1 "
                    + "WHERE maThe=? AND trangThai=N'ĐANG HOẠT ĐỘNG' "
                    + "AND ngayHetHan>=CAST(GETDATE() AS DATE) AND soBuoiDaDung<tongSoBuoi";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, maTheSuDung);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Thẻ Spa Pass không còn lượt, đã hết hạn hoặc vừa được sử dụng ở giao dịch khác");
                }
            }
            String sqlHistory = "INSERT INTO LichSuSuDungSpaPass(maThe,maHoaDon,maNhanVien,thoiGianSuDung,ghiChu) VALUES(?,?,?,SYSDATETIME(),?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlHistory)) {
                ps.setString(1, maTheSuDung);
                ps.setString(2, hoaDon.getMaHoaDon());
                ps.setString(3, hoaDon.getNhanVien().getMaNhanVien());
                ps.setString(4, "Thanh toán tại POS đơn " + hoaDon.getMaHoaDon());
                ps.executeUpdate();
            }
        }

        if (theMoi != null) {
            if (theMoi.getSdtKhachHang() == null || theMoi.getSdtKhachHang().isBlank()) {
                throw new IllegalArgumentException("Mua Spa Pass bắt buộc phải chọn khách hàng có số điện thoại");
            }
            String sqlInsert = "INSERT INTO TheSpaPass (maThe,sdtKhachHang,tenKhachHang,tenGoi,tongSoBuoi,soBuoiDaDung,donGiaGoi,ngayKichHoat,ngayHetHan,trangThai,maHoaDonKichHoat) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setString(1, theMoi.getMaThe());
                ps.setString(2, theMoi.getSdtKhachHang());
                ps.setString(3, theMoi.getTenKhachHang());
                ps.setString(4, theMoi.getTenGoi());
                ps.setInt(5, theMoi.getTongSoBuoi());
                ps.setInt(6, theMoi.getSoBuoiDaDung());
                ps.setBigDecimal(7, BigDecimal.valueOf(theMoi.getDonGiaGoi()));
                ps.setDate(8, Date.valueOf(theMoi.getNgayKichHoat()));
                ps.setDate(9, Date.valueOf(theMoi.getNgayHetHan()));
                ps.setString(10, theMoi.getTrangThai());
                ps.setString(11, hoaDon.getMaHoaDon());
                ps.executeUpdate();
            }
        }
    }

    private void kiemTraHoaDonHopLe(HoaDon hoaDon) {
        if (hoaDon == null || hoaDon.getDsChiTiet() == null || hoaDon.getDsChiTiet().isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn phải có ít nhất một khoản mục chi tiết để thanh toán!");
        }
        if (hoaDon.getNhanVien() == null || hoaDon.getNhanVien().getMaNhanVien() == null
                || hoaDon.getNhanVien().getMaNhanVien().isBlank()) {
            throw new IllegalArgumentException("Hóa đơn phải xác định nhân viên đang đăng nhập");
        }
    }

    private void luuHoaDonTrongTransaction(Connection conn, HoaDon hoaDon) throws SQLException {
        String sqlHoaDon = "INSERT INTO HoaDon (maHoaDon, maKhachHang, maNhanVien, ngayLap, thoiGianThanhToan, giamGia, thanhTien, phuongThucThanhToan, trangThaiHoaDon) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlCTHoaDon = "INSERT INTO ChiTietHoaDon (maChiTiet, maHoaDon, loaiChiTiet, maSanPham, maDichVu, soLuong, donGiaLucBan, thanhTien) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlCTXuatLo = "INSERT INTO ChiTietXuatLo (maChiTiet, soLo, maQuyDoi, soLuongXuatCoSo) VALUES (?, ?, ?, ?)";
        String sqlTruKho = "UPDATE LoHang SET soLuongTon = soLuongTon - ? WHERE soLo = ? AND soLuongTon >= ?";
        String sqlCTDichVu = "INSERT INTO ChiTietDichVu (maChiTiet, maDichVu, maThuCung, maKtvThucHien, tienHoaHong) VALUES (?, ?, ?, ?, ?)";
        String sqlTichDiem = "UPDATE KhachHang SET diemTichLuy = diemTichLuy + ? WHERE maKhachHang = ?";

        try (PreparedStatement ps = conn.prepareStatement(sqlHoaDon)) {
            ps.setString(1, hoaDon.getMaHoaDon());
            if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getMaKhachHang() != null) {
                ps.setString(2, hoaDon.getKhachHang().getMaKhachHang());
            } else {
                ps.setNull(2, Types.NVARCHAR);
            }
            ps.setString(3, hoaDon.getNhanVien().getMaNhanVien());
            ps.setTimestamp(4, Timestamp.valueOf(hoaDon.getNgayLap() != null ? hoaDon.getNgayLap() : LocalDateTime.now()));
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setBigDecimal(6, hoaDon.getGiamGia() != null ? hoaDon.getGiamGia() : BigDecimal.ZERO);
            ps.setBigDecimal(7, hoaDon.getThanhTien());
            ps.setString(8, hoaDon.getPhuongThucThanhToan() != null ? hoaDon.getPhuongThucThanhToan() : "Tiền mặt");
            ps.setString(9, "Đã thanh toán");
            ps.executeUpdate();
        }

        try (PreparedStatement psCT = conn.prepareStatement(sqlCTHoaDon);
             PreparedStatement psXuat = conn.prepareStatement(sqlCTXuatLo);
             PreparedStatement psTruKho = conn.prepareStatement(sqlTruKho);
             PreparedStatement psDV = conn.prepareStatement(sqlCTDichVu)) {
            int index = 1;
            for (ChiTietHoaDon ct : hoaDon.getDsChiTiet()) {
                String maChiTiet = hoaDon.getMaHoaDon() + "_CT" + (index++);
                ct.setMaChiTiet(maChiTiet);
                String maSP = ct.getSanPham() != null ? ct.getSanPham().getMaSanPham() : null;
                if (maSP == null && ct instanceof ChiTietXuatLo) {
                    ChiTietXuatLo xl = (ChiTietXuatLo) ct;
                    if (xl.getLoHang() != null && xl.getLoHang().getSanPham() != null) {
                        maSP = xl.getLoHang().getSanPham().getMaSanPham();
                    }
                }
                String maDV = ct.getDichVu() != null ? ct.getDichVu().getMaDichVu() : null;

                psCT.setString(1, maChiTiet);
                psCT.setString(2, hoaDon.getMaHoaDon());
                psCT.setString(3, ct.getLoaiChiTiet());
                if (maSP != null) psCT.setString(4, maSP); else psCT.setNull(4, Types.NVARCHAR);
                if (maDV != null) psCT.setString(5, maDV); else psCT.setNull(5, Types.NVARCHAR);
                psCT.setInt(6, ct.getSoLuong());
                psCT.setBigDecimal(7, ct.getDonGiaLucBan());
                psCT.setBigDecimal(8, ct.getThanhTien());
                psCT.executeUpdate();

                if (ct instanceof ChiTietXuatLo) {
                    ChiTietXuatLo xuatLo = (ChiTietXuatLo) ct;
                    if (xuatLo.getLoHang() == null || xuatLo.getLoHang().getSoLo() == null) {
                        throw new SQLException("Chi tiết xuất kho chưa xác định số lô");
                    }
                    psXuat.setString(1, maChiTiet);
                    psXuat.setString(2, xuatLo.getLoHang().getSoLo());
                    psXuat.setString(3, xuatLo.getQuyDoiDonVi() != null ? xuatLo.getQuyDoiDonVi().getMaQuyDoi() : null);
                    psXuat.setInt(4, xuatLo.getSoLuongXuatCoSo());
                    psXuat.executeUpdate();

                    psTruKho.setInt(1, xuatLo.getSoLuongXuatCoSo());
                    psTruKho.setString(2, xuatLo.getLoHang().getSoLo());
                    psTruKho.setInt(3, xuatLo.getSoLuongXuatCoSo());
                    if (psTruKho.executeUpdate() != 1) {
                        throw new SQLException("Lô hàng " + xuatLo.getLoHang().getSoLo() + " không đủ số lượng tồn khả dụng để xuất kho!");
                    }
                } else if (ct instanceof ChiTietDichVu) {
                    ChiTietDichVu dv = (ChiTietDichVu) ct;
                    if (dv.getDichVu() == null || dv.getDichVu().getMaDichVu() == null) {
                        throw new SQLException("Chi tiết dịch vụ chưa xác định mã dịch vụ");
                    }
                    psDV.setString(1, maChiTiet);
                    psDV.setString(2, dv.getDichVu().getMaDichVu());
                    psDV.setString(3, dv.getThuCung() != null ? dv.getThuCung().getMaThuCung() : null);
                    psDV.setString(4, dv.getKtvThucHien() != null ? dv.getKtvThucHien().getMaNhanVien() : null);
                    psDV.setBigDecimal(5, dv.getTienHoaHong() != null ? dv.getTienHoaHong() : BigDecimal.ZERO);
                    psDV.executeUpdate();
                }
            }
        }

        if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getMaKhachHang() != null) {
            int diemCong = hoaDon.getThanhTien().divide(new BigDecimal("100000"), 0, RoundingMode.DOWN).intValue();
            if (diemCong > 0) {
                try (PreparedStatement ps = conn.prepareStatement(sqlTichDiem)) {
                    ps.setInt(1, diemCong);
                    ps.setString(2, hoaDon.getKhachHang().getMaKhachHang());
                    ps.executeUpdate();
                }
            }
        }
    }

    /**
     * Lấy toàn bộ danh sách hóa đơn trong hệ thống kèm chi tiết (Tối ưu hóa: Chỉ 2 câu lệnh SQL, xóa bỏ hoàn toàn N+1 queries)
     */
    public List<HoaDon> getAllHoaDon() throws SQLException {
        List<HoaDon> list = new ArrayList<>();
        java.util.Map<String, HoaDon> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT h.*, kh.hoTen AS tenKhachHang, kh.soDienThoai, nv.hoTen AS tenNhanVien " +
                "FROM HoaDon h " +
                "LEFT JOIN KhachHang kh ON h.maKhachHang = kh.maKhachHang " +
                "LEFT JOIN NhanVien nv ON h.maNhanVien = nv.maNhanVien " +
                "ORDER BY h.ngayLap DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HoaDon hd = mapResultSetToHoaDon(rs);
                map.put(hd.getMaHoaDon(), hd);
                list.add(hd);
            }
        }

        if (map.isEmpty()) return list;

        String sqlAllCT = "SELECT ct.maChiTiet, ct.maHoaDon, ct.loaiChiTiet, ct.soLuong, ct.donGiaLucBan, ct.thanhTien, " +
                "xl.soLo, xl.maQuyDoi, xl.soLuongXuatCoSo, COALESCE(ct.maSanPham, l.maSanPham) AS maSanPham, s.tenSanPham, dvt.tenDonVi, " +
                "COALESCE(ct.maDichVu, dv.maDichVu) AS maDichVu, d.tenDichVu, d.thoiLuongPhut, dv.maThuCung, tc.tenThuCung, dv.maKtvThucHien, dv.tienHoaHong " +
                "FROM ChiTietHoaDon ct " +
                "LEFT JOIN ChiTietXuatLo xl ON ct.maChiTiet = xl.maChiTiet " +
                "LEFT JOIN LoHang l ON xl.soLo = l.soLo " +
                "LEFT JOIN SanPham s ON COALESCE(ct.maSanPham, l.maSanPham) = s.maSanPham " +
                "LEFT JOIN QuyDoiDonVi qd ON xl.maQuyDoi = qd.maQuyDoi " +
                "LEFT JOIN DonViTinh dvt ON qd.maDonVi = dvt.maDonVi " +
                "LEFT JOIN ChiTietDichVu dv ON ct.maChiTiet = dv.maChiTiet " +
                "LEFT JOIN DichVu d ON COALESCE(ct.maDichVu, dv.maDichVu) = d.maDichVu " +
                "LEFT JOIN ThuCung tc ON dv.maThuCung = tc.maThuCung " +
                "ORDER BY ct.maChiTiet ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlAllCT);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maHD = rs.getString("maHoaDon");
                HoaDon hd = map.get(maHD);
                if (hd != null) {
                    ChiTietHoaDon ct = mapResultSetToChiTiet(rs);
                    if (ct != null) {
                        ct.setHoaDon(hd);
                        hd.addChiTiet(ct);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Tìm kiếm hóa đơn theo mã và nạp đầy đủ danh sách chi tiết đa hình
     */
    public HoaDon findById(String maHoaDon) throws SQLException {
        String sql = "SELECT h.*, kh.hoTen AS tenKhachHang, kh.soDienThoai, nv.hoTen AS tenNhanVien " +
                "FROM HoaDon h " +
                "LEFT JOIN KhachHang kh ON h.maKhachHang = kh.maKhachHang " +
                "LEFT JOIN NhanVien nv ON h.maNhanVien = nv.maNhanVien " +
                "WHERE h.maHoaDon = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HoaDon hd = mapResultSetToHoaDon(rs);
                    hd.setDsChiTiet(getChiTietByHoaDon(hd.getMaHoaDon()));
                    return hd;
                }
            }
        }
        return null;
    }

    /**
     * Lấy danh sách chi tiết đa hình của một hóa đơn
     */
    public List<ChiTietHoaDon> getChiTietByHoaDon(String maHoaDon) throws SQLException {
        List<ChiTietHoaDon> list = new ArrayList<>();
        String sql = "SELECT ct.maChiTiet, ct.maHoaDon, ct.loaiChiTiet, ct.soLuong, ct.donGiaLucBan, ct.thanhTien, " +
                "xl.soLo, xl.maQuyDoi, xl.soLuongXuatCoSo, COALESCE(ct.maSanPham, l.maSanPham) AS maSanPham, s.tenSanPham, dvt.tenDonVi, " +
                "COALESCE(ct.maDichVu, dv.maDichVu) AS maDichVu, d.tenDichVu, d.thoiLuongPhut, dv.maThuCung, tc.tenThuCung, dv.maKtvThucHien, dv.tienHoaHong " +
                "FROM ChiTietHoaDon ct " +
                "LEFT JOIN ChiTietXuatLo xl ON ct.maChiTiet = xl.maChiTiet " +
                "LEFT JOIN LoHang l ON xl.soLo = l.soLo " +
                "LEFT JOIN SanPham s ON COALESCE(ct.maSanPham, l.maSanPham) = s.maSanPham " +
                "LEFT JOIN QuyDoiDonVi qd ON xl.maQuyDoi = qd.maQuyDoi " +
                "LEFT JOIN DonViTinh dvt ON qd.maDonVi = dvt.maDonVi " +
                "LEFT JOIN ChiTietDichVu dv ON ct.maChiTiet = dv.maChiTiet " +
                "LEFT JOIN DichVu d ON COALESCE(ct.maDichVu, dv.maDichVu) = d.maDichVu " +
                "LEFT JOIN ThuCung tc ON dv.maThuCung = tc.maThuCung " +
                "WHERE ct.maHoaDon = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietHoaDon ct = mapResultSetToChiTiet(rs);
                    if (ct != null) {
                        list.add(ct);
                    }
                }
            }
        }
        return list;
    }

    private ChiTietHoaDon mapResultSetToChiTiet(ResultSet rs) throws SQLException {
        String loai = rs.getString("loaiChiTiet");
        if ("SAN_PHAM".equalsIgnoreCase(loai) || "ChiTietXuatLo".equalsIgnoreCase(loai) || "XUAT_LO".equalsIgnoreCase(loai)) {
            SanPham sp = new SanPham();
            sp.setMaSanPham(rs.getString("maSanPham") != null ? rs.getString("maSanPham") : "SP001");
            sp.setTenSanPham(rs.getString("tenSanPham") != null ? rs.getString("tenSanPham") : "Sản phẩm");

            LoHang lo = new LoHang();
            lo.setSoLo(rs.getString("soLo"));
            lo.setSanPham(sp);

            QuyDoiDonVi qd = null;
            if (rs.getString("tenDonVi") != null) {
                DonViTinh dvt = new DonViTinh("", rs.getString("tenDonVi"));
                qd = new QuyDoiDonVi();
                qd.setMaQuyDoi(rs.getString("maQuyDoi"));
                qd.setDonViTinh(dvt);
            }

            ChiTietXuatLo c = new ChiTietXuatLo(
                    rs.getString("maChiTiet"),
                    null,
                    rs.getInt("soLuong"),
                    rs.getBigDecimal("donGiaLucBan"),
                    lo,
                    qd
            );
            c.setSanPham(sp);
            c.setSoLuongXuatCoSo(rs.getInt("soLuongXuatCoSo"));
            return c;
        } else if ("DICH_VU".equalsIgnoreCase(loai) || "ChiTietDichVu".equalsIgnoreCase(loai)) {
            DichVu d = new DichVu();
            d.setMaDichVu(rs.getString("maDichVu") != null ? rs.getString("maDichVu") : "DV001");
            d.setTenDichVu(rs.getString("tenDichVu") != null ? rs.getString("tenDichVu") : "Dịch vụ");
            d.setThoiLuongPhut(rs.getInt("thoiLuongPhut") > 0 ? rs.getInt("thoiLuongPhut") : 60);

            ThuCung tc = null;
            if (rs.getString("maThuCung") != null) {
                tc = new ThuCung();
                tc.setMaThuCung(rs.getString("maThuCung"));
                tc.setTenThuCung(rs.getString("tenThuCung"));
            }

            NhanVien ktv = new NhanVien();
            ktv.setMaNhanVien(rs.getString("maKtvThucHien"));

            return new ChiTietDichVu(
                    rs.getString("maChiTiet"),
                    null,
                    rs.getInt("soLuong"),
                    rs.getBigDecimal("donGiaLucBan"),
                    d,
                    tc,
                    ktv,
                    rs.getBigDecimal("tienHoaHong")
            );
        }
        return null;
    }

    /**
     * Lấy tổng số lượng đã đổi/trả trước đó của từng mã sản phẩm thuộc một Hóa đơn gốc
     * giúp chặn triệt để việc trả lố hoặc trả trùng nhiều lần trên cùng 1 hóa đơn.
     */
    public java.util.Map<String, Integer> layMapSoLuongDaTra(String maHoaDonGoc) {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        if (maHoaDonGoc == null || maHoaDonGoc.trim().isEmpty()) return map;
        String sql = "SELECT ct.maSanPham, SUM(ct.soLuongDoiTra) AS tongDaTra " +
                     "FROM ChiTietDoiTra ct " +
                     "JOIN PhieuDoiTra p ON ct.maPhieuDoiTra = p.maPhieuDoiTra " +
                     "WHERE p.maHoaDonGoc = ? " +
                     "GROUP BY ct.maSanPham";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHoaDonGoc.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maSP = rs.getString("maSanPham");
                    int daTra = rs.getInt("tongDaTra");
                    if (maSP != null) {
                        map.put(maSP.trim().toUpperCase(), daTra);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[HoaDonDAO] Lỗi tra cứu số lượng đã trả: " + e.getMessage());
        }
        return map;
    }

    /**
     * Lưu phiếu đổi trả hàng hóa vào CSDL (Transaction JDBC an toàn)
     */
    public boolean luuPhieuDoiTra(PhieuDoiTra pdt) throws SQLException {
        try (Connection conn = DBConnection.getNewConnection()) {
            conn.setAutoCommit(false);
            try {
                luuPhieuDoiTraTrongTransaction(conn, pdt);
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                try { conn.rollback(); } catch (SQLException rollbackEx) { e.addSuppressed(rollbackEx); }
                throw e;
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    private void luuPhieuDoiTraTrongTransaction(Connection conn, PhieuDoiTra pdt) throws SQLException {
        if (pdt == null || pdt.getHoaDonGoc() == null || pdt.getHoaDonGoc().getMaHoaDon() == null) {
            throw new IllegalArgumentException("Phiếu đổi trả phải liên kết hóa đơn gốc");
        }
        if (pdt.getNhanVien() == null || pdt.getNhanVien().getMaNhanVien() == null
                || pdt.getNhanVien().getMaNhanVien().isBlank()) {
            throw new IllegalArgumentException("Phiếu đổi trả phải xác định nhân viên tiếp nhận");
        }
        String sqlPhieu = "INSERT INTO PhieuDoiTra (maPhieuDoiTra, maHoaDonGoc, maNhanVienTiepNhan, ngayDoiTra, lyDoDoiTra, tongTienHoan, trangThaiDoiTra) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlCT = "INSERT INTO ChiTietDoiTra (maChiTietDoiTra, maPhieuDoiTra, maSanPham, soLuongDoiTra, tienHoanLai, soLo, soLuongCoSoHoan) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlHoanLo = "UPDATE LoHang SET soLuongTon = soLuongTon + ? WHERE soLo = ? AND maSanPham = ?";

        try (PreparedStatement ps = conn.prepareStatement(sqlPhieu)) {
            ps.setString(1, pdt.getMaPhieuDoiTra());
            ps.setString(2, pdt.getHoaDonGoc().getMaHoaDon());
            ps.setString(3, pdt.getNhanVien().getMaNhanVien());
            ps.setTimestamp(4, Timestamp.valueOf(pdt.getNgayDoiTra() != null ? pdt.getNgayDoiTra() : LocalDateTime.now()));
            ps.setString(5, pdt.getLyDoDoiTra());
            ps.setBigDecimal(6, pdt.getTongTienHoan());
            ps.setString(7, pdt.getTrangThaiDoiTra() != null ? pdt.getTrangThaiDoiTra() : "Hoàn thành");
            ps.executeUpdate();
        }

        if (pdt.getDsChiTietDoiTra() == null || pdt.getDsChiTietDoiTra().isEmpty()) {
            throw new IllegalArgumentException("Phiếu đổi trả phải có ít nhất một mặt hàng");
        }
        try (PreparedStatement psCT = conn.prepareStatement(sqlCT);
             PreparedStatement psKho = conn.prepareStatement(sqlHoanLo)) {
            for (ChiTietDoiTra ct : pdt.getDsChiTietDoiTra()) {
                if (ct.getSanPham() == null || ct.getSanPham().getMaSanPham() == null) {
                    throw new IllegalArgumentException("Chi tiết đổi trả chưa xác định sản phẩm");
                }
                if (ct.getSoLo() == null || ct.getSoLo().isBlank() || ct.getSoLuongCoSoHoan() <= 0) {
                    throw new IllegalArgumentException("Hàng trả phải xác định đúng lô gốc và số lượng cơ sở");
                }
                psCT.setString(1, ct.getMaChiTietDoiTra());
                psCT.setString(2, pdt.getMaPhieuDoiTra());
                psCT.setString(3, ct.getSanPham().getMaSanPham());
                psCT.setInt(4, ct.getSoLuongDoiTra());
                psCT.setBigDecimal(5, ct.getTienHoanLai());
                psCT.setString(6, ct.getSoLo());
                psCT.setInt(7, ct.getSoLuongCoSoHoan());
                psCT.executeUpdate();

                psKho.setInt(1, ct.getSoLuongCoSoHoan());
                psKho.setString(2, ct.getSoLo());
                psKho.setString(3, ct.getSanPham().getMaSanPham());
                if (psKho.executeUpdate() != 1) {
                    throw new SQLException("Không tìm thấy đúng lô gốc " + ct.getSoLo() + " để hoàn kho");
                }
            }
        }
    }

    /**
     * Hủy hóa đơn
     */
    public boolean huyHoaDon(String maHoaDon) throws SQLException {
        String sql = "UPDATE HoaDon SET trangThaiHoaDon = N'Đã hủy' WHERE maHoaDon = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            return ps.executeUpdate() > 0;
        }
    }

    private HoaDon mapResultSetToHoaDon(ResultSet rs) throws SQLException {
        KhachHang kh = null;
        if (rs.getString("maKhachHang") != null) {
            kh = new KhachHang(
                    rs.getString("maKhachHang"),
                    rs.getString("tenKhachHang"),
                    rs.getString("soDienThoai"),
                    0
            );
        }

        NhanVien nv = null;
        if (rs.getString("maNhanVien") != null) {
            nv = new NhanVien();
            nv.setMaNhanVien(rs.getString("maNhanVien"));
            nv.setHoTen(rs.getString("tenNhanVien"));
        }

        Timestamp nl = rs.getTimestamp("ngayLap");
        LocalDateTime ngayLap = (nl != null) ? nl.toLocalDateTime() : LocalDateTime.now();

        Timestamp tt = rs.getTimestamp("thoiGianThanhToan");
        LocalDateTime thoiGianTT = (tt != null) ? tt.toLocalDateTime() : null;

        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(rs.getString("maHoaDon"));
        hd.setKhachHang(kh);
        hd.setNhanVien(nv);
        hd.setNgayLap(ngayLap);
        hd.setThoiGianThanhToan(thoiGianTT);
        hd.setGiamGia(rs.getBigDecimal("giamGia"));
        hd.setThanhTien(rs.getBigDecimal("thanhTien"));
        hd.setPhuongThucThanhToan(rs.getString("phuongThucThanhToan"));
        hd.setTrangThaiHoaDon(rs.getString("trangThaiHoaDon"));

        return hd;
    }
}
