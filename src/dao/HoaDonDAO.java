package dao;

import entity.*;
import utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
        return thucThiThanhToan(hoaDon, null, null, null, null, Collections.emptyList(), 0, 0);
    }

    /** Lưu hóa đơn đổi hàng và phiếu trả trong cùng một transaction. */
    public boolean thanhToanHoaDonKemDoiTra(HoaDon hoaDon, PhieuDoiTra phieuDoiTra) throws SQLException {
        if (phieuDoiTra == null) {
            throw new IllegalArgumentException("Phiếu đổi trả không được để trống");
        }
        return thucThiThanhToan(hoaDon, phieuDoiTra, null, null, null, Collections.emptyList(), 0, 0);
    }

    /** Giao dịch POS đầy đủ, bao gồm đổi trả và/hoặc biến động Spa Pass, Pet Hotel snapshot. */
    public boolean thanhToanHoaDonDayDu(HoaDon hoaDon, PhieuDoiTra phieuDoiTra,
                                        String maTheSuDung, TheSpaPass theMoi,
                                        List<String> dsMaPhongHotel,
                                        List<entity.HotelCheckoutSnapshot> dsSnapshotsHotel,
                                        int diemSuDung, int diemThuHoiDoTraHang) throws SQLException {
        return thucThiThanhToan(hoaDon, phieuDoiTra, maTheSuDung, theMoi,
                dsMaPhongHotel, dsSnapshotsHotel, diemSuDung, diemThuHoiDoTraHang);
    }

    public boolean thanhToanHoaDonDayDu(HoaDon hoaDon, PhieuDoiTra phieuDoiTra,
                                        String maTheSuDung, TheSpaPass theMoi,
                                        List<String> dsMaPhongHotel, int diemSuDung,
                                        int diemThuHoiDoTraHang) throws SQLException {
        return thanhToanHoaDonDayDu(hoaDon, phieuDoiTra, maTheSuDung, theMoi,
                dsMaPhongHotel, Collections.emptyList(), diemSuDung, diemThuHoiDoTraHang);
    }

    private boolean thucThiThanhToan(HoaDon hoaDon, PhieuDoiTra phieuDoiTra,
                                     String maTheSuDung, TheSpaPass theMoi,
                                     List<String> dsMaPhongHotel,
                                     List<entity.HotelCheckoutSnapshot> dsSnapshotsHotel,
                                     int diemSuDung, int diemThuHoiDoTraHang) throws SQLException {
        kiemTraHoaDonHopLe(hoaDon);
        try (Connection conn = DBConnection.getNewConnection()) {
            conn.setAutoCommit(false);
            try {
                yeuCauSchemaTaiChinhPhase2(conn);
                if (phieuDoiTra != null) {
                    luuPhieuDoiTraTrongTransaction(conn, phieuDoiTra);
                }
                luuHoaDonTrongTransaction(conn, hoaDon);
                truDiemTrongTransaction(conn, hoaDon, diemSuDung, diemThuHoiDoTraHang);
                xuLySpaPassTrongTransaction(conn, hoaDon, maTheSuDung, theMoi);
                hoanTatHotelTrongTransaction(conn, hoaDon, dsMaPhongHotel, dsSnapshotsHotel);
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

    private void hoanTatHotelTrongTransaction(Connection conn, HoaDon hoaDon,
                                               List<String> dsMaPhongHotel,
                                               List<entity.HotelCheckoutSnapshot> dsSnapshotsHotel) throws SQLException {
        if ((dsMaPhongHotel == null || dsMaPhongHotel.isEmpty()) && (dsSnapshotsHotel == null || dsSnapshotsHotel.isEmpty())) {
            return;
        }

        java.util.Map<String, entity.HotelCheckoutSnapshot> mapSnapshots = new java.util.HashMap<>();
        if (dsSnapshotsHotel != null) {
            for (entity.HotelCheckoutSnapshot snap : dsSnapshotsHotel) {
                if (snap != null && snap.getMaPhong() != null) {
                    mapSnapshots.put(snap.getMaPhong().trim().toUpperCase(), snap);
                }
            }
        }

        java.util.Set<String> allRooms = new java.util.LinkedHashSet<>();
        if (dsMaPhongHotel != null) {
            for (String mp : dsMaPhongHotel) {
                if (mp != null && !mp.isBlank()) allRooms.add(mp.trim());
            }
        }
        for (String mp : mapSnapshots.keySet()) {
            allRooms.add(mp);
        }

        String sqlInsertSnapshot = "INSERT INTO LichSuLuuTru (maPhong, tenThuCung, tenChu, sdtChu, thoiGianCheckIn, "
                + "thoiGianCheckOut, soNgayLuuTru, donGiaNgay, phiPhatSinh, tongTien, ghiChu, maHoaDon, trangThaiThanhToan) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, N'DA_THANH_TOAN')";

        String sqlPhong = "UPDATE PhongLuuTru SET trangThai=N'ĐANG DỌN DẸP' WHERE maPhong=? AND trangThai=N'ĐANG CÓ KHÁCH'";

        try (PreparedStatement psSnap = conn.prepareStatement(sqlInsertSnapshot);
             PreparedStatement psPhong = conn.prepareStatement(sqlPhong)) {

            for (String maPhong : allRooms) {
                entity.HotelCheckoutSnapshot snap = mapSnapshots.get(maPhong.toUpperCase());
                if (snap != null) {
                    // Dùng đúng snapshot thanh toán chốt lúc check-out (Lỗi B03: Không tính lại ngày hay GETDATE)
                    psSnap.setString(1, snap.getMaPhong());
                    psSnap.setString(2, snap.getTenThuCung());
                    psSnap.setString(3, snap.getTenChu());
                    psSnap.setString(4, snap.getSdtChu());
                    psSnap.setTimestamp(5, snap.getThoiGianCheckIn() != null ? Timestamp.valueOf(snap.getThoiGianCheckIn()) : null);
                    psSnap.setTimestamp(6, Timestamp.valueOf(snap.getThoiGianCheckOut() != null ? snap.getThoiGianCheckOut() : LocalDateTime.now()));
                    psSnap.setInt(7, snap.getSoNgayLuuTru());
                    psSnap.setLong(8, snap.getDonGiaNgay());
                    psSnap.setLong(9, snap.getPhiPhatSinh());
                    psSnap.setLong(10, snap.getTongTien());
                    psSnap.setString(11, snap.getGhiChu());
                    psSnap.setString(12, hoaDon != null ? hoaDon.getMaHoaDon() : null);
                    if (psSnap.executeUpdate() != 1) {
                        throw new SQLException("Không thể ghi nhận lịch sử lưu trú cho phòng [" + maPhong + "]");
                    }
                } else {
                    // Bắt buộc phải có snapshot chốt check-out từ Pet Hotel để đảm bảo minh bạch (TC13)
                    throw new SQLException("Thiếu thông tin snapshot thanh toán của phòng Pet Hotel [" + maPhong
                            + "]. Vui lòng check-out phòng lại từ giao diện Quản lý Khách sạn Thú cưng để chốt thời gian và bảng kê chi phí chính xác.");
                }

                psPhong.setString(1, maPhong);
                if (psPhong.executeUpdate() != 1) {
                    throw new SQLException("Phòng [" + maPhong + "] không còn ở trạng thái 'ĐANG CÓ KHÁCH' hoặc đã được checkout/thanh toán trước đó!");
                }
            }
        }
    }

    private void xuLySpaPassTrongTransaction(Connection conn, HoaDon hoaDon,
                                              String maTheSuDung, TheSpaPass theMoi) throws SQLException {
        if (maTheSuDung != null && !maTheSuDung.isBlank()) {
            String sqlUpdate = "UPDATE TheSpaPass SET soBuoiDaDung = soBuoiDaDung + 1, "
                    + "trangThai = CASE WHEN soBuoiDaDung + 1 >= tongSoBuoi THEN N'ĐÃ HẾT LƯỢT' ELSE trangThai END "
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
        if (hoaDon.getGiamGia() != null && hoaDon.getGiamGia().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giảm giá không được âm");
        }
        if (hoaDon.getTienDoiDiem() != null && hoaDon.getTienDoiDiem().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tiền đổi điểm không được âm");
        }
        if (hoaDon.getTienBuTruDoiTra() != null && hoaDon.getTienBuTruDoiTra().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tiền bù trừ đổi trả không được âm");
        }
        for (ChiTietHoaDon ct : hoaDon.getDsChiTiet()) {
            if (ct == null || ct.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng chi tiết hóa đơn phải lớn hơn 0");
            }
            if (ct.getDonGiaLucBan() == null || ct.getDonGiaLucBan().compareTo(BigDecimal.ZERO) < 0
                    || ct.getThanhTien() == null || ct.getThanhTien().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Đơn giá và thành tiền chi tiết không được âm");
            }
        }
    }

    private void yeuCauSchemaTaiChinhPhase2(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_NAME='HoaDon' AND COLUMN_NAME IN ('tienDoiDiem','tienBuTruDoiTra')";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (!rs.next() || rs.getInt(1) != 2) {
                throw new SQLException("CSDL chưa có schema tài chính Phase 2 của bảng HoaDon. "
                        + "Hãy tạo lại CSDL bằng sql/HeThongQuanLyThuCung.sql; hệ thống dừng để tránh ghi sai giảm giá/đổi điểm/đổi trả.");
            }
        }
    }

    private void luuHoaDonTrongTransaction(Connection conn, HoaDon hoaDon) throws SQLException {
        String sqlHoaDon = "INSERT INTO HoaDon (maHoaDon, maKhachHang, maNhanVien, ngayLap, thoiGianThanhToan, giamGia, tienDoiDiem, tienBuTruDoiTra, thanhTien, phuongThucThanhToan, trangThaiHoaDon) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlCTHoaDon = "INSERT INTO ChiTietHoaDon (maChiTiet, maHoaDon, loaiChiTiet, maSanPham, maDichVu, soLuong, donGiaLucBan, thanhTien) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlCTXuatLo = "INSERT INTO ChiTietXuatLo (maChiTiet, soLo, maQuyDoi, soLuongXuatCoSo) VALUES (?, ?, ?, ?)";
        String sqlTruKho = "UPDATE LoHang SET soLuongTon = soLuongTon - ? WHERE soLo = ? AND soLuongTon >= ?";
        String sqlCTDichVu = "INSERT INTO ChiTietDichVu (maChiTiet, maDichVu, maThuCung, maKtvThucHien, tienHoaHong) VALUES (?, ?, ?, ?, ?)";
        String sqlTichDiem = "UPDATE KhachHang SET diemTichLuy = diemTichLuy + ? WHERE maKhachHang = ?";

        hoaDon.capNhatTongTien();
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
            ps.setBigDecimal(7, hoaDon.getTienDoiDiem() != null ? hoaDon.getTienDoiDiem() : BigDecimal.ZERO);
            ps.setBigDecimal(8, hoaDon.getTienBuTruDoiTra() != null ? hoaDon.getTienBuTruDoiTra() : BigDecimal.ZERO);
            ps.setBigDecimal(9, hoaDon.getThanhTien());
            ps.setString(10, hoaDon.getPhuongThucThanhToan() != null ? hoaDon.getPhuongThucThanhToan() : "Tiền mặt");
            ps.setString(11, "Đã thanh toán");
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Không thể ghi hóa đơn " + hoaDon.getMaHoaDon());
            }
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
     * Lưu phiếu đổi trả hàng hóa vào CSDL (Transaction JDBC an toàn kèm khấu trừ điểm tích lũy nguyên tử)
     */
    public boolean luuPhieuDoiTra(PhieuDoiTra pdt, String sdtKhachHang, int diemKhauTru) throws SQLException {
        try (Connection conn = DBConnection.getNewConnection()) {
            conn.setAutoCommit(false);
            try {
                luuPhieuDoiTraTrongTransaction(conn, pdt);
                if (diemKhauTru > 0) {
                    // Ưu tiên lấy đúng maKhachHang từ hóa đơn gốc để tránh cập nhật nhầm hoặc trùng SĐT
                    String maKhachHang = null;
                    if (pdt.getHoaDonGoc() != null && pdt.getHoaDonGoc().getKhachHang() != null) {
                        maKhachHang = pdt.getHoaDonGoc().getKhachHang().getMaKhachHang();
                    }
                    if (maKhachHang == null || maKhachHang.isBlank()) {
                        String sqlFindKh = "SELECT maKhachHang FROM HoaDon WHERE maHoaDon = ?";
                        try (PreparedStatement psKh = conn.prepareStatement(sqlFindKh)) {
                            psKh.setString(1, pdt.getHoaDonGoc().getMaHoaDon());
                            try (ResultSet rsKh = psKh.executeQuery()) {
                                if (rsKh.next()) maKhachHang = rsKh.getString("maKhachHang");
                            }
                        }
                    }

                    if (maKhachHang != null && !maKhachHang.isBlank()) {
                        String sqlDiem = "UPDATE KhachHang SET diemTichLuy = CASE WHEN diemTichLuy - ? < 0 THEN 0 ELSE diemTichLuy - ? END "
                                + "WHERE maKhachHang = ?";
                        try (PreparedStatement psDiem = conn.prepareStatement(sqlDiem)) {
                            psDiem.setInt(1, diemKhauTru);
                            psDiem.setInt(2, diemKhauTru);
                            psDiem.setString(3, maKhachHang.trim());
                            int rows = psDiem.executeUpdate();
                            if (rows != 1) {
                                throw new SQLException("Không thể khấu trừ điểm đúng khách hàng [" + maKhachHang + "]; giao dịch đã rollback");
                            }
                        }
                    } else {
                        throw new SQLException("Hóa đơn gốc không liên kết khách hàng thành viên nên không thể khấu trừ điểm đổi trả");
                    }
                }
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

    public boolean luuPhieuDoiTra(PhieuDoiTra pdt) throws SQLException {
        return luuPhieuDoiTra(pdt, null, 0);
    }

    private static volatile Boolean hasPhieuDoiTraExtendedColumns = null;

    private static synchronized boolean coCotMoRongPhieuDoiTra(Connection conn) {
        if (hasPhieuDoiTraExtendedColumns != null) {
            return hasPhieuDoiTraExtendedColumns;
        }
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'PhieuDoiTra' AND COLUMN_NAME = 'tienHoanThucTe'")) {
            hasPhieuDoiTraExtendedColumns = rs.next();
            return hasPhieuDoiTraExtendedColumns;
        } catch (Exception e) {
            hasPhieuDoiTraExtendedColumns = false;
            return false;
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
        if (pdt.getDsChiTietDoiTra() == null || pdt.getDsChiTietDoiTra().isEmpty()) {
            throw new IllegalArgumentException("Phiếu đổi trả phải có ít nhất một mặt hàng");
        }

        // Khóa hàng hóa đơn gốc trong transaction để chống concurrency race (TC05)
        String sqlLockHoaDon = "SELECT trangThaiHoaDon FROM HoaDon WITH (UPDLOCK, ROWLOCK) WHERE maHoaDon = ?";
        try (PreparedStatement psLock = conn.prepareStatement(sqlLockHoaDon)) {
            psLock.setString(1, pdt.getHoaDonGoc().getMaHoaDon());
            try (ResultSet rsLock = psLock.executeQuery()) {
                if (!rsLock.next()) {
                    throw new IllegalArgumentException("Hóa đơn gốc [" + pdt.getHoaDonGoc().getMaHoaDon() + "] không tồn tại trong hệ thống");
                }
                String trangThai = rsLock.getString("trangThaiHoaDon");
                if (trangThai == null || !trangThai.trim().equalsIgnoreCase("Đã thanh toán")) {
                    throw new IllegalArgumentException("Chỉ được đổi trả hóa đơn đã thanh toán; trạng thái hiện tại: " + trangThai);
                }
            }
        }

        // Gom nhóm tổng số lượng đổi trả theo sản phẩm trước khi kiểm tra (TC06)
        java.util.Map<String, Integer> tongTraTheoSP = new java.util.HashMap<>();
        BigDecimal tongGiaTriChiTiet = BigDecimal.ZERO;
        for (ChiTietDoiTra ct : pdt.getDsChiTietDoiTra()) {
            if (ct.getSanPham() == null || ct.getSanPham().getMaSanPham() == null) {
                throw new IllegalArgumentException("Chi tiết đổi trả chưa xác định sản phẩm");
            }
            String maSP = ct.getSanPham().getMaSanPham().trim();
            if (ct.getSoLuongDoiTra() <= 0) {
                throw new IllegalArgumentException("Số lượng đổi trả phải lớn hơn 0 cho sản phẩm " + maSP);
            }
            if (ct.getSoLuongCoSoHoan() < 0) {
                throw new IllegalArgumentException("Số lượng cơ sở hoàn kho không được âm cho sản phẩm " + maSP);
            }
            if (ct.getTienHoanLai() == null || ct.getTienHoanLai().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Tiền hoàn chi tiết không được âm cho sản phẩm " + maSP);
            }
            int tongCu = tongTraTheoSP.getOrDefault(maSP, 0);
            try {
                tongTraTheoSP.put(maSP, Math.addExact(tongCu, ct.getSoLuongDoiTra()));
            } catch (ArithmeticException overflow) {
                throw new IllegalArgumentException("Tổng số lượng đổi trả vượt giới hạn cho sản phẩm " + maSP, overflow);
            }
            tongGiaTriChiTiet = tongGiaTriChiTiet.add(ct.getTienHoanLai());
        }

        BigDecimal giaTriHangTra = pdt.getGiaTriHangTra() != null ? pdt.getGiaTriHangTra() : BigDecimal.ZERO;
        BigDecimal giaTriHangDoi = pdt.getGiaTriHangDoi() != null ? pdt.getGiaTriHangDoi() : BigDecimal.ZERO;
        if (giaTriHangTra.compareTo(tongGiaTriChiTiet) != 0) {
            throw new IllegalArgumentException("Giá trị hàng trả không khớp tổng tiền các chi tiết đổi trả");
        }
        if (giaTriHangDoi.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá trị hàng đổi không được âm");
        }
        BigDecimal chenhLech = giaTriHangTra.subtract(giaTriHangDoi);
        BigDecimal tienHoanDung = chenhLech.max(BigDecimal.ZERO);
        BigDecimal tienTraThemDung = chenhLech.negate().max(BigDecimal.ZERO);
        if (pdt.getTienHoanThucTe() == null || pdt.getTienHoanThucTe().compareTo(tienHoanDung) != 0
                || pdt.getTienKhachTraThem() == null || pdt.getTienKhachTraThem().compareTo(tienTraThemDung) != 0) {
            throw new IllegalArgumentException("Tiền hoàn/khách trả thêm không khớp chênh lệch hàng trả và hàng đổi");
        }
        if (pdt.getTongTienHoan() == null || pdt.getTongTienHoan().compareTo(tienHoanDung) != 0) {
            throw new IllegalArgumentException("Tổng tiền hoàn lưu tương thích phải khớp tiền hoàn thực tế");
        }
        pdt.setPhuongThucHoanTien(bus.TaiChinhHoaDonBus.chuanHoaPhuongThucHoan(
                pdt.getPhuongThucHoanTien(), tienHoanDung.compareTo(BigDecimal.ZERO) > 0));

        // Kiểm tra tổng số lượng đổi trả không vượt quá số lượng đã mua trừ đi đã trả trước đó
        String sqlCheckDaMua = "SELECT COALESCE(SUM(soLuong), 0) FROM ChiTietHoaDon WHERE maHoaDon = ? AND maSanPham = ?";
        String sqlCheckDaTra = "SELECT COALESCE(SUM(ct.soLuongDoiTra), 0) FROM ChiTietDoiTra ct "
                + "JOIN PhieuDoiTra p ON ct.maPhieuDoiTra = p.maPhieuDoiTra "
                + "WHERE p.maHoaDonGoc = ? AND ct.maSanPham = ?";
        try (PreparedStatement psMua = conn.prepareStatement(sqlCheckDaMua);
             PreparedStatement psTra = conn.prepareStatement(sqlCheckDaTra)) {
            for (java.util.Map.Entry<String, Integer> entry : tongTraTheoSP.entrySet()) {
                String maSP = entry.getKey();
                int soLuongTraHienTai = entry.getValue();

                psMua.setString(1, pdt.getHoaDonGoc().getMaHoaDon());
                psMua.setString(2, maSP);
                int daMua = 0;
                try (ResultSet rs = psMua.executeQuery()) {
                    if (rs.next()) daMua = rs.getInt(1);
                }

                psTra.setString(1, pdt.getHoaDonGoc().getMaHoaDon());
                psTra.setString(2, maSP);
                int daTra = 0;
                try (ResultSet rs = psTra.executeQuery()) {
                    if (rs.next()) daTra = rs.getInt(1);
                }

                if (daTra + soLuongTraHienTai > daMua) {
                    throw new IllegalArgumentException("Tổng số lượng đổi trả sản phẩm [" + maSP + "] là "
                            + soLuongTraHienTai + " vượt quá số lượng còn được phép trả ("
                            + Math.max(0, daMua - daTra) + ") của hóa đơn " + pdt.getHoaDonGoc().getMaHoaDon());
                }
            }
        }

        boolean coCotMoRong = coCotMoRongPhieuDoiTra(conn);
        if (!coCotMoRong) {
            throw new SQLException("CSDL chưa có schema đổi trả Phase 1/2. Hãy tạo lại CSDL bằng sql/HeThongQuanLyThuCung.sql; không thể ghi thiếu phương thức và tiền hoàn thực tế.");
        }
        String sqlPhieu = "INSERT INTO PhieuDoiTra (maPhieuDoiTra, maHoaDonGoc, maNhanVienTiepNhan, ngayDoiTra, lyDoDoiTra, tongTienHoan, trangThaiDoiTra, giaTriHangTra, giaTriHangDoi, tienHoanThucTe, tienKhachTraThem, phuongThucHoanTien) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlCT = "INSERT INTO ChiTietDoiTra (maChiTietDoiTra, maPhieuDoiTra, maSanPham, soLuongDoiTra, tienHoanLai, soLo, soLuongCoSoHoan) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlHoanLo = "UPDATE LoHang SET soLuongTon = soLuongTon + ? WHERE soLo = ? AND maSanPham = ?";

        try (PreparedStatement ps = conn.prepareStatement(sqlPhieu)) {
            ps.setString(1, pdt.getMaPhieuDoiTra());
            ps.setString(2, pdt.getHoaDonGoc().getMaHoaDon());
            ps.setString(3, pdt.getNhanVien().getMaNhanVien());
            ps.setTimestamp(4, Timestamp.valueOf(pdt.getNgayDoiTra() != null ? pdt.getNgayDoiTra() : LocalDateTime.now()));
            ps.setString(5, pdt.getLyDoDoiTra());
            BigDecimal tongHoan = pdt.getTongTienHoan() != null ? pdt.getTongTienHoan() : BigDecimal.ZERO;
            ps.setBigDecimal(6, tongHoan);
            ps.setString(7, pdt.getTrangThaiDoiTra() != null ? pdt.getTrangThaiDoiTra() : "Hoàn thành");
            ps.setBigDecimal(8, pdt.getGiaTriHangTra());
            ps.setBigDecimal(9, pdt.getGiaTriHangDoi());
            ps.setBigDecimal(10, pdt.getTienHoanThucTe());
            ps.setBigDecimal(11, pdt.getTienKhachTraThem());
            ps.setString(12, pdt.getPhuongThucHoanTien());
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Không thể ghi phiếu đổi trả " + pdt.getMaPhieuDoiTra());
            }
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
                boolean canHoanKho = (ct.getSoLo() != null && !ct.getSoLo().isBlank() && ct.getSoLuongCoSoHoan() > 0);
                psCT.setString(1, ct.getMaChiTietDoiTra());
                psCT.setString(2, pdt.getMaPhieuDoiTra());
                psCT.setString(3, ct.getSanPham().getMaSanPham());
                psCT.setInt(4, ct.getSoLuongDoiTra());
                psCT.setBigDecimal(5, ct.getTienHoanLai());
                if (canHoanKho) {
                    psCT.setString(6, ct.getSoLo());
                    psCT.setInt(7, ct.getSoLuongCoSoHoan());
                } else {
                    psCT.setNull(6, Types.NVARCHAR);
                    psCT.setInt(7, 0);
                }
                if (psCT.executeUpdate() != 1) {
                    throw new SQLException("Không thể ghi chi tiết đổi trả " + ct.getMaChiTietDoiTra());
                }

                if (canHoanKho) {
                    psKho.setInt(1, ct.getSoLuongCoSoHoan());
                    psKho.setString(2, ct.getSoLo());
                    psKho.setString(3, ct.getSanPham().getMaSanPham());
                    if (psKho.executeUpdate() != 1) {
                        throw new SQLException("Không tìm thấy đúng lô gốc " + ct.getSoLo() + " để hoàn kho");
                    }
                }
            }
        }
    }

    /**
     * Hủy hóa đơn
     */
    public boolean huyHoaDon(String maHoaDon) throws SQLException {
        if (!utils.UserSession.isQuanLy() && !utils.UserSession.isThuNgan()) {
            throw new SecurityException("Người dùng hiện tại không có quyền hủy hóa đơn!");
        }
        if (maHoaDon == null || maHoaDon.isBlank()) {
            return false;
        }

        try (Connection conn = DBConnection.getNewConnection()) {
            conn.setAutoCommit(false);
            try {
            String sqlCheck = "SELECT trangThaiHoaDon FROM HoaDon WITH (UPDLOCK, ROWLOCK) WHERE maHoaDon = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setString(1, maHoaDon);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) return false;
                    String trangThai = rs.getString("trangThaiHoaDon");
                    if (trangThai != null && trangThai.equalsIgnoreCase("Đã hủy")) {
                        return false;
                    }
                    if (trangThai != null && (trangThai.equalsIgnoreCase("Đã thanh toán")
                            || trangThai.equalsIgnoreCase("HOÀN THÀNH")
                            || trangThai.equalsIgnoreCase("ĐÃ THANH TOÁN"))) {
                        throw new UnsupportedOperationException(
                                "Hóa đơn [" + maHoaDon + "] đã thanh toán hoàn tất, không được hủy trực tiếp! "
                                + "Vui lòng sử dụng quy trình Đổi trả / Hoàn tiền để đảm bảo tính toàn vẹn tồn kho và tài chính.");
                    }
                }
            }

            String sql = "UPDATE HoaDon SET trangThaiHoaDon = N'Đã hủy' "
                    + "WHERE maHoaDon = ? AND trangThaiHoaDon <> N'Đã hủy'";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maHoaDon);
                boolean updated = ps.executeUpdate() == 1;
                if (updated) conn.commit(); else conn.rollback();
                return updated;
            }
            } catch (SQLException | RuntimeException ex) {
                try { conn.rollback(); } catch (SQLException rollbackEx) { ex.addSuppressed(rollbackEx); }
                throw ex;
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
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
        hd.setTienDoiDiem(getBigDecimalNeuCo(rs, "tienDoiDiem"));
        hd.setTienBuTruDoiTra(getBigDecimalNeuCo(rs, "tienBuTruDoiTra"));
        hd.setThanhTien(rs.getBigDecimal("thanhTien"));
        hd.setPhuongThucThanhToan(rs.getString("phuongThucThanhToan"));
        hd.setTrangThaiHoaDon(rs.getString("trangThaiHoaDon"));

        return hd;
    }

    private BigDecimal getBigDecimalNeuCo(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(meta.getColumnLabel(i))) {
                BigDecimal value = rs.getBigDecimal(columnName);
                return value != null ? value : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
}
