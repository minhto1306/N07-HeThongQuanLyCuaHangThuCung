package models.dao;

import models.entity.BangPhanCong;
import models.entity.CaLamViec;
import models.entity.NhanVien;
import models.entity.TaiKhoan;
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
        String sql = "SELECT pc.*, nv.hoTen, nv.chucVu, nv.soDienThoai, nv.tenDangNhap, " +
                     "tk.vaiTro, tk.trangThaiHoatDong, c.tenCa, c.gioBatDau, c.gioKetThuc " +
                     "FROM BangPhanCong pc " +
                     "JOIN NhanVien nv ON pc.maNhanVien = nv.maNhanVien " +
                     "LEFT JOIN TaiKhoan tk ON nv.tenDangNhap = tk.tenDangNhap " +
                     "JOIN CaLamViec c ON pc.maCa = c.maCa " +
                     "ORDER BY pc.ngayLamViec ASC, pc.maCa ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TaiKhoan tk = rs.getString("tenDangNhap") != null
                        ? new TaiKhoan(rs.getString("tenDangNhap"), "", rs.getString("vaiTro"), rs.getBoolean("trangThaiHoatDong"))
                        : null;
                NhanVien nv = new NhanVien(
                        rs.getString("maNhanVien"),
                        rs.getString("hoTen"),
                        rs.getString("chucVu"),
                        rs.getString("soDienThoai"),
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
        String sql = "INSERT INTO BangPhanCong (maPhanCong, maNhanVien, maCa, ngayLamViec, trangThaiCa, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pc.getMaPhanCong());
            ps.setString(2, pc.getNhanVien().getMaNhanVien());
            ps.setString(3, pc.getCaLamViec().getMaCa());
            ps.setDate(4, Date.valueOf(pc.getNgayLamViec()));
            ps.setString(5, pc.getTrangThaiCa() != null ? pc.getTrangThaiCa() : "Đã phân công");
            ps.setString(6, pc.getGhiChu() != null ? pc.getGhiChu() : "");
            return ps.executeUpdate() > 0;
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
