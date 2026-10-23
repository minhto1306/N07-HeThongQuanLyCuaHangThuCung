package models.dao;

import models.entity.*;
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
    public boolean thanhToanHoaDon(HoaDon hoaDon) throws SQLException {
        if (hoaDon == null || hoaDon.getDsChiTiet() == null || hoaDon.getDsChiTiet().isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn phải có ít nhất một khoản mục chi tiết để thanh toán!");
        }

        Connection conn = null;
        PreparedStatement psHoaDon = null;
        PreparedStatement psCTHoaDon = null;
        PreparedStatement psCTXuatLo = null;
        PreparedStatement psTruKho = null;
        PreparedStatement psCTDichVu = null;
        PreparedStatement psTichDiem = null;

        String sqlHoaDon = "INSERT INTO HoaDon (maHoaDon, maKhachHang, maNhanVien, ngayLap, thoiGianThanhToan, giamGia, thanhTien, phuongThucThanhToan, trangThaiHoaDon) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlCTHoaDon = "INSERT INTO ChiTietHoaDon (maChiTiet, maHoaDon, loaiChiTiet, soLuong, donGiaLucBan, thanhTien) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        String sqlCTXuatLo = "INSERT INTO ChiTietXuatLo (maChiTiet, soLo, maQuyDoi, soLuongXuatCoSo) VALUES (?, ?, ?, ?)";

        String sqlTruKho = "UPDATE LoHang SET soLuongTon = soLuongTon - ? WHERE soLo = ? AND soLuongTon >= ?";

        String sqlCTDichVu = "INSERT INTO ChiTietDichVu (maChiTiet, maDichVu, maThuCung, maKtvThucHien, tienHoaHong) VALUES (?, ?, ?, ?, ?)";

        String sqlTichDiem = "UPDATE KhachHang SET diemTichLuy = diemTichLuy + ? WHERE maKhachHang = ?";

        try {
            conn = DBConnection.getNewConnection();
            // BẮT ĐẦU GIAO DỊCH JDBC
            conn.setAutoCommit(false);

            // 1. Thêm Hóa đơn
            psHoaDon = conn.prepareStatement(sqlHoaDon);
            psHoaDon.setString(1, hoaDon.getMaHoaDon());
            if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getMaKhachHang() != null) {
                psHoaDon.setString(2, hoaDon.getKhachHang().getMaKhachHang());
            } else {
                psHoaDon.setNull(2, Types.NVARCHAR);
            }
            psHoaDon.setString(3, hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getMaNhanVien() : "NV001");
            psHoaDon.setTimestamp(4, Timestamp.valueOf(hoaDon.getNgayLap() != null ? hoaDon.getNgayLap() : LocalDateTime.now()));
            psHoaDon.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            psHoaDon.setBigDecimal(6, hoaDon.getGiamGia() != null ? hoaDon.getGiamGia() : BigDecimal.ZERO);
            psHoaDon.setBigDecimal(7, hoaDon.getThanhTien());
            psHoaDon.setString(8, hoaDon.getPhuongThucThanhToan() != null ? hoaDon.getPhuongThucThanhToan() : "Tiền mặt");
            psHoaDon.setString(9, "Đã thanh toán");
            psHoaDon.executeUpdate();

            // 2. Chuẩn bị các PreparedStatement cho chi tiết
            psCTHoaDon = conn.prepareStatement(sqlCTHoaDon);
            psCTXuatLo = conn.prepareStatement(sqlCTXuatLo);
            psTruKho = conn.prepareStatement(sqlTruKho);
            psCTDichVu = conn.prepareStatement(sqlCTDichVu);

            int index = 1;
            for (ChiTietHoaDon ct : hoaDon.getDsChiTiet()) {
                String maChiTiet = hoaDon.getMaHoaDon() + "_CT" + (index++);
                ct.setMaChiTiet(maChiTiet);

                // Thêm bảng cha ChiTietHoaDon
                psCTHoaDon.setString(1, maChiTiet);
                psCTHoaDon.setString(2, hoaDon.getMaHoaDon());
                psCTHoaDon.setString(3, ct.getLoaiChiTiet());
                psCTHoaDon.setInt(4, ct.getSoLuong());
                psCTHoaDon.setBigDecimal(5, ct.getDonGiaLucBan());
                psCTHoaDon.setBigDecimal(6, ct.getThanhTien());
                psCTHoaDon.executeUpdate();

                // Xử lý đa hình
                if (ct instanceof ChiTietXuatLo) {
                    ChiTietXuatLo xuatLo = (ChiTietXuatLo) ct;

                    psCTXuatLo.setString(1, maChiTiet);
                    psCTXuatLo.setString(2, xuatLo.getLoHang().getSoLo());
                    psCTXuatLo.setString(3, xuatLo.getQuyDoiDonVi() != null ? xuatLo.getQuyDoiDonVi().getMaQuyDoi() : null);
                    psCTXuatLo.setInt(4, xuatLo.getSoLuongXuatCoSo());
                    psCTXuatLo.executeUpdate();

                    // TRỪ TỒN KHO LÔ HÀNG VỚI KHÓA RÀNG BUỘC
                    psTruKho.setInt(1, xuatLo.getSoLuongXuatCoSo());
                    psTruKho.setString(2, xuatLo.getLoHang().getSoLo());
                    psTruKho.setInt(3, xuatLo.getSoLuongXuatCoSo());
                    int rowsAffected = psTruKho.executeUpdate();

                    if (rowsAffected == 0) {
                        throw new SQLException("Lô hàng " + xuatLo.getLoHang().getSoLo() + " không đủ số lượng tồn khả dụng để xuất kho!");
                    }
                } else if (ct instanceof ChiTietDichVu) {
                    ChiTietDichVu dv = (ChiTietDichVu) ct;

                    psCTDichVu.setString(1, maChiTiet);
                    psCTDichVu.setString(2, dv.getDichVu().getMaDichVu());
                    psCTDichVu.setString(3, dv.getThuCung() != null ? dv.getThuCung().getMaThuCung() : null);
                    psCTDichVu.setString(4, dv.getKtvThucHien() != null ? dv.getKtvThucHien().getMaNhanVien() : null);
                    psCTDichVu.setBigDecimal(5, dv.getTienHoaHong() != null ? dv.getTienHoaHong() : BigDecimal.ZERO);
                    psCTDichVu.executeUpdate();
                }
            }

            // 3. Tích điểm cho khách hàng (100.000 đ = 1 điểm)
            if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getMaKhachHang() != null) {
                int diemCong = hoaDon.getThanhTien().divide(new BigDecimal("100000"), 0, RoundingMode.DOWN).intValue();
                if (diemCong > 0) {
                    psTichDiem = conn.prepareStatement(sqlTichDiem);
                    psTichDiem.setInt(1, diemCong);
                    psTichDiem.setString(2, hoaDon.getKhachHang().getMaKhachHang());
                    psTichDiem.executeUpdate();
                }
            }

            // COMMIT GIAO DỊCH THÀNH CÔNG
            conn.commit();
            hoaDon.setTrangThaiHoaDon("Đã thanh toán");
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Giao dịch thanh toán bị rollback do lỗi: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    System.err.println("Lỗi khi rollback giao dịch: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (psHoaDon != null) psHoaDon.close();
            if (psCTHoaDon != null) psCTHoaDon.close();
            if (psCTXuatLo != null) psCTXuatLo.close();
            if (psTruKho != null) psTruKho.close();
            if (psCTDichVu != null) psCTDichVu.close();
            if (psTichDiem != null) psTichDiem.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
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

        String sqlAllCT = "SELECT ct.*, " +
                "xl.soLo, xl.maQuyDoi, xl.soLuongXuatCoSo, l.maSanPham, s.tenSanPham, dvt.tenDonVi, " +
                "dv.maDichVu, d.tenDichVu, d.thoiLuongPhut, dv.maThuCung, tc.tenThuCung, dv.maKtvThucHien, dv.tienHoaHong " +
                "FROM ChiTietHoaDon ct " +
                "LEFT JOIN ChiTietXuatLo xl ON ct.maChiTiet = xl.maChiTiet " +
                "LEFT JOIN LoHang l ON xl.soLo = l.soLo " +
                "LEFT JOIN SanPham s ON l.maSanPham = s.maSanPham " +
                "LEFT JOIN QuyDoiDonVi qd ON xl.maQuyDoi = qd.maQuyDoi " +
                "LEFT JOIN DonViTinh dvt ON qd.maDonVi = dvt.maDonVi " +
                "LEFT JOIN ChiTietDichVu dv ON ct.maChiTiet = dv.maChiTiet " +
                "LEFT JOIN DichVu d ON dv.maDichVu = d.maDichVu " +
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
        String sql = "SELECT ct.*, " +
                "xl.soLo, xl.maQuyDoi, xl.soLuongXuatCoSo, l.maSanPham, s.tenSanPham, dvt.tenDonVi, " +
                "dv.maDichVu, d.tenDichVu, d.thoiLuongPhut, dv.maThuCung, tc.tenThuCung, dv.maKtvThucHien, dv.tienHoaHong " +
                "FROM ChiTietHoaDon ct " +
                "LEFT JOIN ChiTietXuatLo xl ON ct.maChiTiet = xl.maChiTiet " +
                "LEFT JOIN LoHang l ON xl.soLo = l.soLo " +
                "LEFT JOIN SanPham s ON l.maSanPham = s.maSanPham " +
                "LEFT JOIN QuyDoiDonVi qd ON xl.maQuyDoi = qd.maQuyDoi " +
                "LEFT JOIN DonViTinh dvt ON qd.maDonVi = dvt.maDonVi " +
                "LEFT JOIN ChiTietDichVu dv ON ct.maChiTiet = dv.maChiTiet " +
                "LEFT JOIN DichVu d ON dv.maDichVu = d.maDichVu " +
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
     * Lưu phiếu đổi trả hàng hóa vào CSDL (Transaction JDBC an toàn)
     */
    public boolean luuPhieuDoiTra(PhieuDoiTra pdt) throws SQLException {
        String sqlPhieu = "INSERT INTO PhieuDoiTra (maPhieuDoiTra, maHoaDonGoc, maNhanVien, ngayDoiTra, lyDoDoiTra, tongTienHoan, trangThaiDoiTra) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlCT = "INSERT INTO ChiTietDoiTra (maChiTietDoiTra, maPhieuDoiTra, maSanPham, soLuongDoiTra, tienHoanLai) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getNewConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlPhieu)) {
                ps1.setString(1, pdt.getMaPhieuDoiTra());
                ps1.setString(2, pdt.getHoaDonGoc().getMaHoaDon());
                ps1.setString(3, pdt.getNhanVien() != null ? pdt.getNhanVien().getMaNhanVien() : "NV001");
                ps1.setTimestamp(4, Timestamp.valueOf(pdt.getNgayDoiTra() != null ? pdt.getNgayDoiTra() : LocalDateTime.now()));
                ps1.setString(5, pdt.getLyDoDoiTra());
                ps1.setBigDecimal(6, pdt.getTongTienHoan());
                ps1.setString(7, pdt.getTrangThaiDoiTra() != null ? pdt.getTrangThaiDoiTra() : "Hoàn thành");
                ps1.executeUpdate();
            }

            if (pdt.getDsChiTietDoiTra() != null) {
                try (PreparedStatement ps2 = conn.prepareStatement(sqlCT)) {
                    for (ChiTietDoiTra ct : pdt.getDsChiTietDoiTra()) {
                        ps2.setString(1, ct.getMaChiTietDoiTra());
                        ps2.setString(2, pdt.getMaPhieuDoiTra());
                        ps2.setString(3, ct.getSanPham().getMaSanPham());
                        ps2.setInt(4, ct.getSoLuongDoiTra());
                        ps2.setBigDecimal(5, ct.getTienHoanLai());
                        ps2.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
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
