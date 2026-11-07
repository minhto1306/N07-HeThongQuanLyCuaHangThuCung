package dao;

import entity.BangPhanCong;
import entity.CaLamViec;
import entity.NhanVien;
import entity.TaiKhoan;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý Ca làm việc (CaLamViec) và Bảng phân công (BangPhanCong) trên SQL Server.
 */
public class PhanCaDAO {

    public List<CaLamViec> getAllCaLamViec() throws SQLException {
        List<CaLamViec> list = new ArrayList<>();
        String sql = "SELECT * FROM CaLamViec ORDER BY maCa ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Time bd = rs.getTime("gioBatDau");
                Time kt = rs.getTime("gioKetThuc");
                list.add(new CaLamViec(
                        rs.getString("maCa"),
                        rs.getString("tenCa"),
                        bd != null ? bd.toLocalTime() : LocalTime.of(7, 0),
                        kt != null ? kt.toLocalTime() : LocalTime.of(11, 30)
                ));
            }
        }
        return list;
    }

    public List<BangPhanCong> getAllPhanCong() throws SQLException {
        List<BangPhanCong> list = new ArrayList<>();
        String sql = "SELECT pc.*, nv.hoTen, nv.chucVu, nv.soDienThoai, nv.email, nv.maTaiKhoan, " +
                     "tk.vaiTro, tk.trangThaiHoatDong, c.tenCa, c.gioBatDau, c.gioKetThuc " +
                     "FROM BangPhanCong pc " +
                     "JOIN NhanVien nv ON pc.maNhanVien = nv.maNhanVien " +
                     "LEFT JOIN TaiKhoan tk ON nv.maTaiKhoan = tk.maTaiKhoan " +
                     "JOIN CaLamViec c ON pc.maCa = c.maCa " +
                     "ORDER BY pc.ngayLamViec ASC, pc.maCa ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maTK = null;
                try { maTK = rs.getString("maTaiKhoan"); } catch (SQLException ignored) {}
                String email = "";
                try { email = rs.getString("email"); } catch (SQLException ignored) {}
                TaiKhoan tk = maTK != null
                        ? new TaiKhoan(maTK, rs.getString("soDienThoai"), email != null ? email : "", "", rs.getString("vaiTro"), rs.getBoolean("trangThaiHoatDong"))
                        : null;
                NhanVien nv = new NhanVien(
                        rs.getString("maNhanVien"),
                        rs.getString("hoTen"),
                        rs.getString("chucVu"),
                        rs.getString("soDienThoai"),
                        email != null ? email : "",
                        tk
                );
                Time bd = rs.getTime("gioBatDau");
                Time kt = rs.getTime("gioKetThuc");
                CaLamViec ca = new CaLamViec(
                        rs.getString("maCa"),
                        rs.getString("tenCa"),
                        bd != null ? bd.toLocalTime() : LocalTime.of(7, 0),
                        kt != null ? kt.toLocalTime() : LocalTime.of(11, 30)
                );
                Date d = rs.getDate("ngayLamViec");
                String ghiChu = "";
                try { ghiChu = rs.getString("ghiChu"); } catch (SQLException ignored) {}
                list.add(new BangPhanCong(
                        rs.getString("maPhanCong"),
                        nv,
                        ca,
                        d != null ? d.toLocalDate() : LocalDate.now(),
                        rs.getString("trangThaiCa"),
                        ghiChu != null ? ghiChu : ""
                ));
            }
        }
        return list;
    }

    public boolean insertPhanCong(BangPhanCong pc) throws SQLException {
        String sql = "INSERT INTO BangPhanCong (maNhanVien, maCa, ngayLamViec, trangThaiCa, ghiChu) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pc.getNhanVien().getMaNhanVien());
            ps.setString(2, pc.getCaLamViec().getMaCa());
            ps.setDate(3, Date.valueOf(pc.getNgayLamViec()));
            ps.setString(4, pc.getTrangThaiCa() != null ? pc.getTrangThaiCa() : "Đã phân công");
            ps.setString(5, pc.getGhiChu() != null ? pc.getGhiChu() : "");
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        pc.setMaPhanCong(String.valueOf(rs.getInt(1)));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean updateCaPhanCong(String maPhanCong, String maCaMoi) throws SQLException {
        String sql = "UPDATE BangPhanCong SET maCa = ? WHERE maPhanCong = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maCaMoi);
            ps.setString(2, maPhanCong);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateGhiChu(String maPhanCong, String ghiChuMoi) throws SQLException {
        String sql = "UPDATE BangPhanCong SET ghiChu = ? WHERE maPhanCong = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ghiChuMoi);
            ps.setString(2, maPhanCong);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletePhanCong(String maPhanCong) throws SQLException {
        String sql = "DELETE FROM BangPhanCong WHERE maPhanCong = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhanCong);
            return ps.executeUpdate() > 0;
        }
    }
}
