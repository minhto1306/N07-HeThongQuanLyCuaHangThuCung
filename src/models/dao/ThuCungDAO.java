package models.dao;

import models.entity.HoSoSucKhoe;
import models.entity.HoSoThuCung;
import models.entity.KhachHang;
import models.entity.NhanVien;
import models.entity.ThuCung;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Thú cưng, Hồ sơ nuôi & Hồ sơ sức khỏe.
 * Cung cấp đầy đủ các thao tác CRUD và Transaction an toàn.
 */
public class ThuCungDAO {

    /**
     * Lấy toàn bộ danh sách thú cưng kèm thông tin chủ nuôi và mã Microchip
     */
    public List<HoSoThuCung> getAllHoSoThuCung() throws SQLException {
        List<HoSoThuCung> list = new ArrayList<>();
        String sql = "SELECT hs.maHoSo, hs.soMicrochip, hs.ngayDangKy, hs.trangThaiNuoi, " +
                "tc.maThuCung, tc.tenThuCung, tc.giongLoai, tc.ngaySinh, tc.gioiTinh, " +
                "kh.maKhachHang, kh.hoTen AS tenKhachHang, kh.soDienThoai, kh.diemTichLuy " +
                "FROM HoSoThuCung hs " +
                "JOIN ThuCung tc ON hs.maThuCung = tc.maThuCung " +
                "JOIN KhachHang kh ON hs.maKhachHang = kh.maKhachHang " +
                "ORDER BY hs.ngayDangKy DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                KhachHang kh = new KhachHang(
                        rs.getString("maKhachHang"),
                        rs.getString("tenKhachHang"),
                        rs.getString("soDienThoai"),
                        rs.getInt("diemTichLuy")
                );

                Date ns = rs.getDate("ngaySinh");
                LocalDate ngaySinh = (ns != null) ? ns.toLocalDate() : null;
                ThuCung tc = new ThuCung(
                        rs.getString("maThuCung"),
                        rs.getString("tenThuCung"),
                        rs.getString("giongLoai"),
                        ngaySinh,
                        rs.getString("gioiTinh")
                );

                Date ndk = rs.getDate("ngayDangKy");
                LocalDate ngayDangKy = (ndk != null) ? ndk.toLocalDate() : LocalDate.now();

                HoSoThuCung hs = new HoSoThuCung(
                        rs.getString("maHoSo"),
                        kh,
                        tc,
                        rs.getString("soMicrochip"),
                        ngayDangKy,
                        rs.getString("trangThaiNuoi")
                );
                list.add(hs);
            }
        }
        return list;
    }

    /**
     * Lấy danh sách thú cưng của một khách hàng cụ thể
     */
    public List<ThuCung> getThuCungByKhachHang(String maKhachHang) throws SQLException {
        List<ThuCung> list = new ArrayList<>();
        String sql = "SELECT tc.* FROM ThuCung tc " +
                "JOIN HoSoThuCung hs ON tc.maThuCung = hs.maThuCung " +
                "WHERE hs.maKhachHang = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date ns = rs.getDate("ngaySinh");
                    LocalDate ngaySinh = (ns != null) ? ns.toLocalDate() : null;
                    list.add(new ThuCung(
                            rs.getString("maThuCung"),
                            rs.getString("tenThuCung"),
                            rs.getString("giongLoai"),
                            ngaySinh,
                            rs.getString("gioiTinh")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Tìm thú cưng theo mã ID
     */
    public ThuCung getThuCungById(String maThuCung) throws SQLException {
        String sql = "SELECT * FROM ThuCung WHERE maThuCung = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maThuCung);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date ns = rs.getDate("ngaySinh");
                    LocalDate ngaySinh = (ns != null) ? ns.toLocalDate() : null;
                    return new ThuCung(
                            rs.getString("maThuCung"),
                            rs.getString("tenThuCung"),
                            rs.getString("giongLoai"),
                            ngaySinh,
                            rs.getString("gioiTinh")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Thêm thú cưng mới kèm tạo Hồ sơ Microchip (Transaction JDBC an toàn)
     */
    public boolean insertThuCung(ThuCung tc, String maKhachHang, String soMicrochip) throws SQLException {
        String sqlTC = "INSERT INTO ThuCung (maThuCung, tenThuCung, giongLoai, ngaySinh, gioiTinh) VALUES (?, ?, ?, ?, ?)";
        String sqlHS = "INSERT INTO HoSoThuCung (maHoSo, maKhachHang, maThuCung, soMicrochip, ngayDangKy, trangThaiNuoi) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psTC = conn.prepareStatement(sqlTC)) {
                psTC.setString(1, tc.getMaThuCung());
                psTC.setString(2, tc.getTenThuCung());
                psTC.setString(3, tc.getGiongLoai());
                psTC.setDate(4, tc.getNgaySinh() != null ? Date.valueOf(tc.getNgaySinh()) : null);
                psTC.setString(5, tc.getGioiTinh());
                psTC.executeUpdate();
            }

            String maHoSo = "HS" + System.currentTimeMillis() % 1000000;
            try (PreparedStatement psHS = conn.prepareStatement(sqlHS)) {
                psHS.setString(1, maHoSo);
                psHS.setString(2, maKhachHang);
                psHS.setString(3, tc.getMaThuCung());
                psHS.setString(4, soMicrochip != null ? soMicrochip : "MC" + System.currentTimeMillis() % 100000);
                psHS.setDate(5, Date.valueOf(LocalDate.now()));
                psHS.setString(6, "Đang nuôi");
                psHS.executeUpdate();
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
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Cập nhật thông tin thú cưng
     */
    public boolean updateThuCung(ThuCung tc) throws SQLException {
        String sql = "UPDATE ThuCung SET tenThuCung = ?, giongLoai = ?, ngaySinh = ?, gioiTinh = ? WHERE maThuCung = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tc.getTenThuCung());
            ps.setString(2, tc.getGiongLoai());
            ps.setDate(3, tc.getNgaySinh() != null ? Date.valueOf(tc.getNgaySinh()) : null);
            ps.setString(4, tc.getGioiTinh());
            ps.setString(5, tc.getMaThuCung());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa thú cưng kèm hồ sơ sức khỏe và hồ sơ nuôi (Transaction)
     */
    public boolean deleteThuCung(String maThuCung) throws SQLException {
        String sqlSK = "DELETE FROM HoSoSucKhoe WHERE maThuCung = ?";
        String sqlHS = "DELETE FROM HoSoThuCung WHERE maThuCung = ?";
        String sqlTC = "DELETE FROM ThuCung WHERE maThuCung = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlSK)) {
                ps1.setString(1, maThuCung);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(sqlHS)) {
                ps2.setString(1, maThuCung);
                ps2.executeUpdate();
            }

            int rows;
            try (PreparedStatement ps3 = conn.prepareStatement(sqlTC)) {
                ps3.setString(1, maThuCung);
                rows = ps3.executeUpdate();
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
     * Lấy danh sách lịch sử kiểm tra sức khỏe thể trạng
     */
    public List<HoSoSucKhoe> getAllHoSoSucKhoe() throws SQLException {
        List<HoSoSucKhoe> list = new ArrayList<>();
        String sql = "SELECT sk.*, tc.tenThuCung, tc.giongLoai, nv.hoTen AS tenNhanVien " +
                "FROM HoSoSucKhoe sk " +
                "JOIN ThuCung tc ON sk.maThuCung = tc.maThuCung " +
                "LEFT JOIN NhanVien nv ON sk.maNhanVienKiemTra = nv.maNhanVien " +
                "ORDER BY sk.ngayKiemTra DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ThuCung tc = new ThuCung();
                tc.setMaThuCung(rs.getString("maThuCung"));
                tc.setTenThuCung(rs.getString("tenThuCung"));
                tc.setGiongLoai(rs.getString("giongLoai"));

                NhanVien nv = null;
                if (rs.getString("maNhanVienKiemTra") != null) {
                    nv = new NhanVien();
                    nv.setMaNhanVien(rs.getString("maNhanVienKiemTra"));
                    nv.setHoTen(rs.getString("tenNhanVien"));
                }

                Timestamp ts = rs.getTimestamp("ngayKiemTra");
                LocalDateTime ngayKT = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

                HoSoSucKhoe sk = new HoSoSucKhoe(
                        rs.getString("maSucKhoe"),
                        tc,
                        nv,
                        rs.getBigDecimal("canNang"),
                        rs.getBigDecimal("nhietDo"),
                        rs.getString("tinhTrangVeRan"),
                        rs.getBoolean("tiemPhongDai"),
                        rs.getString("ghiChuTheTrang"),
                        ngayKT
                );
                list.add(sk);
            }
        }
        return list;
    }

    /**
     * Thêm mới một hồ sơ kiểm tra sức khỏe thể trạng trước dịch vụ
     */
    public boolean insertHoSoSucKhoe(HoSoSucKhoe sk) throws SQLException {
        String sql = "INSERT INTO HoSoSucKhoe (maSucKhoe, maThuCung, maNhanVienKiemTra, canNang, nhietDo, tinhTrangVeRan, tiemPhongDai, ghiChuTheTrang, ngayKiemTra) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sk.getMaSucKhoe());
            ps.setString(2, sk.getThuCung().getMaThuCung());
            ps.setString(3, sk.getNhanVienKiemTra() != null ? sk.getNhanVienKiemTra().getMaNhanVien() : null);
            ps.setBigDecimal(4, sk.getCanNang());
            ps.setBigDecimal(5, sk.getNhietDo());
            ps.setString(6, sk.getTinhTrangVeRan());
            ps.setBoolean(7, sk.isTiemPhongDai());
            ps.setString(8, sk.getGhiChuTheTrang());
            ps.setTimestamp(9, Timestamp.valueOf(sk.getNgayKiemTra() != null ? sk.getNgayKiemTra() : LocalDateTime.now()));
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật hồ sơ kiểm tra sức khỏe
     */
    public boolean updateHoSoSucKhoe(HoSoSucKhoe sk) throws SQLException {
        String sql = "UPDATE HoSoSucKhoe SET canNang = ?, nhietDo = ?, tinhTrangVeRan = ?, tiemPhongDai = ?, ghiChuTheTrang = ? WHERE maSucKhoe = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, sk.getCanNang());
            ps.setBigDecimal(2, sk.getNhietDo());
            ps.setString(3, sk.getTinhTrangVeRan());
            ps.setBoolean(4, sk.isTiemPhongDai());
            ps.setString(5, sk.getGhiChuTheTrang());
            ps.setString(6, sk.getMaSucKhoe());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa hồ sơ sức khỏe
     */
    public boolean deleteHoSoSucKhoe(String maSucKhoe) throws SQLException {
        String sql = "DELETE FROM HoSoSucKhoe WHERE maSucKhoe = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSucKhoe);
            return ps.executeUpdate() > 0;
        }
    }
}
