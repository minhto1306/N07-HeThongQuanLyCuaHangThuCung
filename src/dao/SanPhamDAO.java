package dao;

import entity.*;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Sản Phẩm, Danh Mục Hàng & Quy Đổi Đơn Vị Tính.
 * Cung cấp đầy đủ các thao tác CRUD (Thêm, Xóa, Sửa, Lấy danh sách, Tìm kiếm theo ID).
 */
public class SanPhamDAO {

    public String sinhMaSanPhamMoi() {
        String sql = "SELECT dbo.fn_SinhMaSanPham()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString(1);
        } catch (SQLException ignored) {}
        return "SP001";
    }

    public String sinhMaQuyDoiMoi() {
        String sql = "SELECT dbo.fn_SinhMaQuyDoi()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString(1);
        } catch (SQLException ignored) {}
        return "QD001";
    }



    private SanPham mapResultSetToSanPham(ResultSet rs) throws SQLException {
        DanhMucHang dm = new DanhMucHang(
                rs.getString("maDanhMuc"),
                rs.getString("tenDanhMuc"),
                rs.getString("moTaDM")
        );
        String maVach = "";
        String donViCoSo = "Đơn vị";
        java.math.BigDecimal giaBanCoSo = java.math.BigDecimal.ZERO;
        try {
            String mv = rs.getString("maVach");
            if (mv != null) maVach = mv;
        } catch (SQLException ignored) {}
        try {
            String dv = rs.getString("donViCoSo");
            if (dv != null && !dv.isEmpty()) donViCoSo = dv;
        } catch (SQLException ignored) {}
        try {
            java.math.BigDecimal gb = rs.getBigDecimal("giaBanCoSo");
            if (gb != null) giaBanCoSo = gb;
        } catch (SQLException ignored) {}

        return new SanPham(
                rs.getString("maSanPham"),
                dm,
                rs.getString("tenSanPham"),
                maVach,
                donViCoSo,
                giaBanCoSo,
                rs.getString("moTa")
        );
    }

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
                list.add(mapResultSetToSanPham(rs));
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
                    return mapResultSetToSanPham(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tìm sản phẩm nhanh theo Mã vạch (Barcode) hoặc Mã sản phẩm (hỗ trợ máy quét POS)
     */
    public SanPham findByMaVachHoacMaSP(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) return null;
        String kw = keyword.trim();
        String sql = "SELECT s.*, d.tenDanhMuc, d.moTa AS moTaDM " +
                     "FROM SanPham s JOIN DanhMucHang d ON s.maDanhMuc = d.maDanhMuc " +
                     "WHERE s.maVach = ? OR s.maSanPham = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kw);
            ps.setString(2, kw);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSanPham(rs);
                }
            }
        } catch (SQLException e) {
            return getSanPhamById(kw);
        }
        return null;
    }

    /**
     * Thêm sản phẩm mới vào CSDL
     */
    public boolean insertSanPham(SanPham sp) throws SQLException {
        String sql = "INSERT INTO SanPham (maSanPham, maDanhMuc, tenSanPham, maVach, donViCoSo, giaBanCoSo, moTa) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sp.getMaSanPham());
            ps.setString(2, sp.getDanhMucHang() != null ? sp.getDanhMucHang().getMaDanhMuc() : null);
            ps.setString(3, sp.getTenSanPham());
            if (sp.getMaVach() != null && !sp.getMaVach().trim().isEmpty()) {
                ps.setString(4, sp.getMaVach().trim());
            } else {
                ps.setNull(4, Types.VARCHAR);
            }
            ps.setString(5, sp.getDonViCoSo());
            ps.setBigDecimal(6, sp.getGiaBanCoSo());
            ps.setString(7, sp.getMoTa());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật thông tin sản phẩm
     */
    public boolean updateSanPham(SanPham sp) throws SQLException {
        String sql = "UPDATE SanPham SET maDanhMuc = ?, tenSanPham = ?, maVach = ?, donViCoSo = ?, giaBanCoSo = ?, moTa = ? WHERE maSanPham = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sp.getDanhMucHang() != null ? sp.getDanhMucHang().getMaDanhMuc() : null);
            ps.setString(2, sp.getTenSanPham());
            if (sp.getMaVach() != null && !sp.getMaVach().trim().isEmpty()) {
                ps.setString(3, sp.getMaVach().trim());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            ps.setString(4, sp.getDonViCoSo());
            ps.setBigDecimal(5, sp.getGiaBanCoSo());
            ps.setString(6, sp.getMoTa());
            ps.setString(7, sp.getMaSanPham());
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
    /**
     * Tự động sinh mã sản phẩm 
     */
    public String phatSinhMaSanPham() {
        int maxId = 0;
        String sql = "SELECT maSanPham FROM SanPham";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maSanPham");
                if (ma != null && ma.trim().toUpperCase().startsWith("SP")) {
                    try {
                        // Trích xuất phần số đằng sau chữ SP
                        int num = Integer.parseInt(ma.trim().substring(2).replaceAll("[^0-9]", ""));
                        if (num > maxId) {
                            maxId = num;
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            System.err.println("[SanPhamDAO] Lỗi phát sinh mã sản phẩm: " + e.getMessage());
        }
        // Trả về mã kế tiếp 
        return String.format("SP%03d", maxId + 1);
    }

    /**
     * Lấy danh sách sản phẩm kèm quy đổi đơn vị tính và tồn kho cơ sở phục vụ Catalog bán hàng POS.
     * Mỗi phần tử Object[] gồm: [0] maSP, [1] tenSP, [2] moTa, [3] maVach, [4] maQuyDoi, [5] giaBan(long), [6] tenDonVi, [7] tyLe(int), [8] tonKhoCoSo(int)
     */
    public List<Object[]> layDanhSachSanPhamCatalogPOS() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sqlSP = "SELECT sp.maSanPham, sp.tenSanPham, sp.moTa, ISNULL(sp.maVach, '') AS maVach, qd.maQuyDoi, qd.giaBan, dvt.tenDonVi, qd.tyLeQuyDoi AS tyLe, " +
                "       ISNULL((SELECT SUM(soLuongTon) FROM LoHang WHERE maSanPham = sp.maSanPham AND soLuongTon > 0), 0) AS tonKhoCoSo " +
                "FROM SanPham sp " +
                "INNER JOIN QuyDoiDonVi qd ON sp.maSanPham = qd.maSanPham " +
                "INNER JOIN DonViTinh dvt ON qd.maDonVi = dvt.maDonVi " +
                "ORDER BY sp.maSanPham ASC, qd.tyLeQuyDoi ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlSP);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("maSanPham"),
                        rs.getString("tenSanPham"),
                        rs.getString("moTa"),
                        rs.getString("maVach"),
                        rs.getString("maQuyDoi"),
                        (long) rs.getDouble("giaBan"),
                        rs.getString("tenDonVi"),
                        rs.getInt("tyLe"),
                        rs.getInt("tonKhoCoSo")
                });
            }
        }
        return list;
    }
}

