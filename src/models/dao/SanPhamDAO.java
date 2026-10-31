package models.dao;

import models.entity.*;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Sản Phẩm, Danh Mục Hàng & Quy Đổi Đơn Vị Tính.
 * Cung cấp đầy đủ các thao tác CRUD (Thêm, Xóa, Sửa, Lấy danh sách, Tìm kiếm theo ID).
 */
public class SanPhamDAO {

    /**
     * Lấy toàn bộ danh sách sản phẩm kèm danh mục
     */
    public List<SanPham> getAllSanPham() throws SQLException {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT s.*, d.tenDanhMuc, d.moTa AS moTaDM " +
                     "FROM SanPham s JOIN DanhMucHang d ON s.maDanhMuc = d.maDanhMuc ORDER BY s.maSanPham ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DanhMucHang dm = new DanhMucHang(
                        rs.getString("maDanhMuc"),
                        rs.getString("tenDanhMuc"),
                        rs.getString("moTaDM")
                );
                SanPham sp = new SanPham(
                        rs.getString("maSanPham"),
                        dm,
                        rs.getString("tenSanPham"),
                        rs.getString("moTa")
                );
                list.add(sp);
            }
        }
        return list;
    }

    /**
     * Tìm sản phẩm theo mã ID
     */
    public SanPham getSanPhamById(String maSanPham) throws SQLException {
        String sql = "SELECT s.*, d.tenDanhMuc, d.moTa AS moTaDM " +
                     "FROM SanPham s JOIN DanhMucHang d ON s.maDanhMuc = d.maDanhMuc WHERE s.maSanPham = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSanPham);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DanhMucHang dm = new DanhMucHang(
                            rs.getString("maDanhMuc"),
                            rs.getString("tenDanhMuc"),
                            rs.getString("moTaDM")
                    );
                    return new SanPham(
                            rs.getString("maSanPham"),
                            dm,
                            rs.getString("tenSanPham"),
                            rs.getString("moTa")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Thêm sản phẩm mới vào CSDL
     */
    public boolean insertSanPham(SanPham sp) throws SQLException {
        String sql = "INSERT INTO SanPham (maSanPham, maDanhMuc, tenSanPham, moTa) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sp.getMaSanPham());
            ps.setString(2, sp.getDanhMucHang() != null ? sp.getDanhMucHang().getMaDanhMuc() : null);
            ps.setString(3, sp.getTenSanPham());
            ps.setString(4, sp.getMoTa());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật thông tin sản phẩm
     */
    public boolean updateSanPham(SanPham sp) throws SQLException {
        String sql = "UPDATE SanPham SET maDanhMuc = ?, tenSanPham = ?, moTa = ? WHERE maSanPham = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sp.getDanhMucHang() != null ? sp.getDanhMucHang().getMaDanhMuc() : null);
            ps.setString(2, sp.getTenSanPham());
            ps.setString(3, sp.getMoTa());
            ps.setString(4, sp.getMaSanPham());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa sản phẩm theo mã
     */
    public boolean deleteSanPham(String maSanPham) throws SQLException {
        // Xóa các bảng phụ thuộc trước (QuyDoiDonVi)
        String sqlDelQuyDoi = "DELETE FROM QuyDoiDonVi WHERE maSanPham = ?";
        String sqlDelSP = "DELETE FROM SanPham WHERE maSanPham = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlDelQuyDoi)) {
                ps1.setString(1, maSanPham);
                ps1.executeUpdate();
            }

            int rows;
            try (PreparedStatement ps2 = conn.prepareStatement(sqlDelSP)) {
                ps2.setString(1, maSanPham);
                rows = ps2.executeUpdate();
            }

            conn.commit();
            return rows > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Lấy danh sách quy đổi đơn vị tính & giá bán của một sản phẩm
     */
    public List<QuyDoiDonVi> getQuyDoiBySanPham(String maSanPham) throws SQLException {
        List<QuyDoiDonVi> list = new ArrayList<>();
        String sql = "SELECT q.*, dv.tenDonVi " +
                     "FROM QuyDoiDonVi q JOIN DonViTinh dv ON q.maDonVi = dv.maDonVi " +
                     "WHERE q.maSanPham = ? ORDER BY q.tyLeQuyDoi ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSanPham);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DonViTinh dvt = new DonViTinh(rs.getString("maDonVi"), rs.getString("tenDonVi"));
                    QuyDoiDonVi qd = new QuyDoiDonVi();
                    qd.setMaQuyDoi(rs.getString("maQuyDoi"));
                    qd.setDonViTinh(dvt);
                    qd.setTyLeQuyDoi(rs.getInt("tyLeQuyDoi"));
                    qd.setLaDonViCoSo(rs.getBoolean("laDonViCoSo"));
                    qd.setGiaBan(rs.getBigDecimal("giaBan"));
                    list.add(qd);
                }
            }
        }
        return list;
    }

    /**
     * Thêm mới một quy đổi đơn vị tính
     */
    public boolean insertQuyDoi(QuyDoiDonVi qd) throws SQLException {
        String sql = "INSERT INTO QuyDoiDonVi (maQuyDoi, maSanPham, maDonVi, tyLeQuyDoi, laDonViCoSo, giaBan) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, qd.getMaQuyDoi());
            ps.setString(2, qd.getSanPham() != null ? qd.getSanPham().getMaSanPham() : null);
            ps.setString(3, qd.getDonViTinh() != null ? qd.getDonViTinh().getMaDonVi() : null);
            ps.setInt(4, qd.getTyLeQuyDoi());
            ps.setBoolean(5, qd.isLaDonViCoSo());
            ps.setBigDecimal(6, qd.getGiaBan());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật thông tin quy đổi đơn vị tính
     */
    public boolean updateQuyDoi(QuyDoiDonVi qd) throws SQLException {
        String sql = "UPDATE QuyDoiDonVi SET tyLeQuyDoi = ?, laDonViCoSo = ?, giaBan = ? WHERE maQuyDoi = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qd.getTyLeQuyDoi());
            ps.setBoolean(2, qd.isLaDonViCoSo());
            ps.setBigDecimal(3, qd.getGiaBan());
            ps.setString(4, qd.getMaQuyDoi());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa một quy đổi đơn vị tính
     */
    public boolean deleteQuyDoi(String maQuyDoi) throws SQLException {
        String sql = "DELETE FROM QuyDoiDonVi WHERE maQuyDoi = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maQuyDoi);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lấy toàn bộ danh mục hàng hóa
     */
    public List<DanhMucHang> getAllDanhMuc() throws SQLException {
        List<DanhMucHang> list = new ArrayList<>();
        String sql = "SELECT * FROM DanhMucHang ORDER BY tenDanhMuc ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new DanhMucHang(
                        rs.getString("maDanhMuc"),
                        rs.getString("tenDanhMuc"),
                        rs.getString("moTa")
                ));
            }
        }
        return list;
    }

    /**
     * Lấy toàn bộ đơn vị tính
     */
    public List<DonViTinh> getAllDonViTinh() throws SQLException {
        List<DonViTinh> list = new ArrayList<>();
        String sql = "SELECT * FROM DonViTinh ORDER BY tenDonVi ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new DonViTinh(
                        rs.getString("maDonVi"),
                        rs.getString("tenDonVi")
                ));
            }
        }
        return list;
    }

    /**
     * Thêm danh mục hàng mới
     */
    public boolean insertDanhMuc(DanhMucHang dm) throws SQLException {
        String sql = "INSERT INTO DanhMucHang (maDanhMuc, tenDanhMuc, moTa) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dm.getMaDanhMuc());
            ps.setString(2, dm.getTenDanhMuc());
            ps.setString(3, dm.getMoTa());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Thêm đơn vị tính mới
     */
    public boolean insertDonViTinh(DonViTinh dvt) throws SQLException {
        String sql = "INSERT INTO DonViTinh (maDonVi, tenDonVi) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dvt.getMaDonVi());
            ps.setString(2, dvt.getTenDonVi());
            return ps.executeUpdate() > 0;
        }
    }
}
