package models.dao;

import models.entity.LoHang;
import models.entity.NhaCungCap;
import models.entity.SanPham;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Lô Hàng & Nhà Cung Cấp.
 * Cung cấp đầy đủ các thao tác CRUD và thuật toán FEFO (First Expired, First Out).
 */
public class LoHangDAO {

    /**
     * Lấy toàn bộ danh sách lô hàng trong kho
     */
    public List<LoHang> getAllLoHang() throws SQLException {
        List<LoHang> list = new ArrayList<>();
        String sql = "SELECT l.*, s.tenSanPham, n.tenNhaCungCap " +
                "FROM LoHang l " +
                "JOIN SanPham s ON l.maSanPham = s.maSanPham " +
                "JOIN NhaCungCap n ON l.maNhaCungCap = n.maNhaCungCap " +
                "ORDER BY l.hanSuDung ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToLoHang(rs));
            }
        }
        return list;
    }

    /**
     * Tìm lô hàng theo số lô
     */
    public LoHang getLoHangBySoLo(String soLo) throws SQLException {
        String sql = "SELECT l.*, s.tenSanPham, n.tenNhaCungCap " +
                "FROM LoHang l " +
                "JOIN SanPham s ON l.maSanPham = s.maSanPham " +
                "JOIN NhaCungCap n ON l.maNhaCungCap = n.maNhaCungCap " +
                "WHERE l.soLo = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soLo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLoHang(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lấy lô hàng khả dụng theo quy tắc FEFO (Hạn sử dụng gần nhất, còn tồn kho)
     */
    public LoHang getLoHangFEFOBySanPham(String maSanPham) throws SQLException {
        String sql = "SELECT TOP 1 l.*, s.tenSanPham, n.tenNhaCungCap " +
                "FROM LoHang l " +
                "JOIN SanPham s ON l.maSanPham = s.maSanPham " +
                "JOIN NhaCungCap n ON l.maNhaCungCap = n.maNhaCungCap " +
                "WHERE l.maSanPham = ? AND l.soLuongTon > 0 " +
                "ORDER BY l.hanSuDung ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSanPham);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLoHang(rs);
                }
            }
        }

        // Nếu hết hàng hoặc chưa có lô, thử lấy lô bất kỳ của sản phẩm
        String sqlFallback = "SELECT TOP 1 l.*, s.tenSanPham, n.tenNhaCungCap " +
                "FROM LoHang l " +
                "JOIN SanPham s ON l.maSanPham = s.maSanPham " +
                "JOIN NhaCungCap n ON l.maNhaCungCap = n.maNhaCungCap " +
                "WHERE l.maSanPham = ? " +
                "ORDER BY l.hanSuDung DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlFallback)) {
            ps.setString(1, maSanPham);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLoHang(rs);
                }
            }
        }
        return null;
    }

    /**
     * Thêm lô hàng mới nhập kho
     */
    public boolean insertLoHang(LoHang lh) throws SQLException {
        String sql = "INSERT INTO LoHang (soLo, maSanPham, maNhaCungCap, ngaySanXuat, hanSuDung, donGiaNhapCoSo, soLuongNhap, soLuongTon) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lh.getSoLo());
            ps.setString(2, lh.getSanPham() != null ? lh.getSanPham().getMaSanPham() : null);
            ps.setString(3, lh.getNhaCungCap() != null ? lh.getNhaCungCap().getMaNhaCungCap() : null);
            ps.setDate(4, lh.getNgaySanXuat() != null ? Date.valueOf(lh.getNgaySanXuat()) : null);
            ps.setDate(5, Date.valueOf(lh.getHanSuDung()));
            ps.setBigDecimal(6, lh.getDonGiaNhapCoSo());
            ps.setInt(7, lh.getSoLuongNhap());
            ps.setInt(8, lh.getSoLuongTon());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật thông tin lô hàng
     */
    public boolean updateLoHang(LoHang lh) throws SQLException {
        String sql = "UPDATE LoHang SET maSanPham = ?, maNhaCungCap = ?, ngaySanXuat = ?, hanSuDung = ?, " +
                "donGiaNhapCoSo = ?, soLuongNhap = ?, soLuongTon = ? WHERE soLo = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lh.getSanPham() != null ? lh.getSanPham().getMaSanPham() : null);
            ps.setString(2, lh.getNhaCungCap() != null ? lh.getNhaCungCap().getMaNhaCungCap() : null);
            ps.setDate(3, lh.getNgaySanXuat() != null ? Date.valueOf(lh.getNgaySanXuat()) : null);
            ps.setDate(4, Date.valueOf(lh.getHanSuDung()));
            ps.setBigDecimal(5, lh.getDonGiaNhapCoSo());
            ps.setInt(6, lh.getSoLuongNhap());
            ps.setInt(7, lh.getSoLuongTon());
            ps.setString(8, lh.getSoLo());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa lô hàng theo số lô
     */
    public boolean deleteLoHang(String soLo) throws SQLException {
        String sql = "DELETE FROM LoHang WHERE soLo = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soLo);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Trừ tồn kho của một lô hàng cụ thể
     */
    public boolean truTonKho(String soLo, int soLuongXuat) throws SQLException {
        String sql = "UPDATE LoHang SET soLuongTon = soLuongTon - ? WHERE soLo = ? AND soLuongTon >= ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLuongXuat);
            ps.setString(2, soLo);
            ps.setInt(3, soLuongXuat);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cộng hoàn trả tồn kho cho một lô hàng cụ thể (khi đổi trả hàng nguyên tem)
     */
    public boolean congTonKho(String soLo, int soLuongCong) throws SQLException {
        String sql = "UPDATE LoHang SET soLuongTon = soLuongTon + ? WHERE soLo = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLuongCong);
            ps.setString(2, soLo);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lấy danh sách các lô hàng cận hạn sử dụng (trong vòng N ngày tới)
     */
    public List<LoHang> layDanhSachCanDate(int soNgayCanhBao) throws SQLException {
        List<LoHang> list = new ArrayList<>();
        String sql = "SELECT l.*, s.tenSanPham, n.tenNhaCungCap " +
                "FROM LoHang l " +
                "JOIN SanPham s ON l.maSanPham = s.maSanPham " +
                "JOIN NhaCungCap n ON l.maNhaCungCap = n.maNhaCungCap " +
                "WHERE DATEDIFF(day, CAST(GETDATE() AS DATE), l.hanSuDung) <= ? AND l.soLuongTon > 0 " +
                "ORDER BY l.hanSuDung ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soNgayCanhBao);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLoHang(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy danh sách các lô hàng còn tồn kho khả dụng của một sản phẩm,
     * sắp xếp ưu tiên theo hạn sử dụng tăng dần (FEFO).
     */
    public List<LoHang> getAvailableLotsFEFO(String maSanPham) throws SQLException {
        List<LoHang> list = new ArrayList<>();
        String sql = "SELECT l.*, s.tenSanPham, n.tenNhaCungCap " +
                "FROM LoHang l " +
                "JOIN SanPham s ON l.maSanPham = s.maSanPham " +
                "JOIN NhaCungCap n ON l.maNhaCungCap = n.maNhaCungCap " +
                "WHERE l.maSanPham = ? AND l.soLuongTon > 0 AND l.hanSuDung >= CAST(GETDATE() AS DATE) " +
                "ORDER BY l.hanSuDung ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSanPham);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLoHang(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy tổng tồn kho khả dụng của một sản phẩm từ tất cả các lô còn hạn.
     */
    public int getTongTonKhaDung(String maSanPham) throws SQLException {
        String sql = "SELECT SUM(soLuongTon) AS tongTon " +
                "FROM LoHang " +
                "WHERE maSanPham = ? AND soLuongTon > 0 AND hanSuDung >= CAST(GETDATE() AS DATE)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSanPham);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("tongTon");
                }
            }
        }
        return 0;
    }

    /**
     * Lấy toàn bộ danh sách nhà cung cấp
     */
    public List<NhaCungCap> getAllNhaCungCap() throws SQLException {
        List<NhaCungCap> list = new ArrayList<>();
        String sql = "SELECT * FROM NhaCungCap ORDER BY tenNhaCungCap ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new NhaCungCap(
                        rs.getString("maNhaCungCap"),
                        rs.getString("tenNhaCungCap"),
                        rs.getString("diaChi"),
                        rs.getString("soDienThoai")
                ));
            }
        }
        return list;
    }

    /**
     * Thêm nhà cung cấp mới
     */
    public boolean insertNhaCungCap(NhaCungCap ncc) throws SQLException {
        String sql = "INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, diaChi, soDienThoai) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ncc.getMaNhaCungCap());
            ps.setString(2, ncc.getTenNhaCungCap());
            ps.setString(3, ncc.getDiaChi());
            ps.setString(4, ncc.getSoDienThoai());
            return ps.executeUpdate() > 0;
        }
    }

    private LoHang mapResultSetToLoHang(ResultSet rs) throws SQLException {
        LoHang lo = new LoHang();
        lo.setSoLo(rs.getString("soLo"));

        SanPham sp = new SanPham();
        sp.setMaSanPham(rs.getString("maSanPham"));
        sp.setTenSanPham(rs.getString("tenSanPham"));
        lo.setSanPham(sp);

        NhaCungCap ncc = new NhaCungCap();
        ncc.setMaNhaCungCap(rs.getString("maNhaCungCap"));
        ncc.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
        lo.setNhaCungCap(ncc);

        Date nsx = rs.getDate("ngaySanXuat");
        if (nsx != null) lo.setNgaySanXuat(nsx.toLocalDate());
        lo.setHanSuDung(rs.getDate("hanSuDung").toLocalDate());
        lo.setDonGiaNhapCoSo(rs.getBigDecimal("donGiaNhapCoSo"));
        lo.setSoLuongNhap(rs.getInt("soLuongNhap"));
        lo.setSoLuongTon(rs.getInt("soLuongTon"));

        return lo;
    }
}
