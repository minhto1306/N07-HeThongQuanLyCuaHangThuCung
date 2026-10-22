package models.dao;

import models.entity.BangGiaoCa;
import models.entity.CaLamViec;
import models.entity.NhanVien;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Quản lý Ca làm việc & Biên bản Bàn giao ca (BangGiaoCa).
 * Cung cấp đầy đủ các thao tác CRUD và kiểm soát đối soát quỹ tiền mặt.
 */
public class BangGiaoCaDAO {

    /**
     * Mở ca làm việc mới khi nhân viên bắt đầu ca trực
     */
    public boolean moCaLamViec(BangGiaoCa giaoCa) throws SQLException {
        return insertBangGiaoCa(giaoCa);
    }

    public boolean insertBangGiaoCa(BangGiaoCa giaoCa) throws SQLException {
        String sql = "INSERT INTO BangGiaoCa (maGiaoCa, maNhanVien, maCa, tienDauCa, tienCuoiCaHeThong, tienCuoiCaThucTe, tienChenhLech, lyDoChenhLech, gioKetThucThucTe) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, giaoCa.getMaGiaoCa());
            ps.setString(2, giaoCa.getNhanVien() != null ? giaoCa.getNhanVien().getMaNhanVien() : "NV002");
            ps.setString(3, giaoCa.getCaLamViec() != null ? giaoCa.getCaLamViec().getMaCa() : "CA01");
            ps.setBigDecimal(4, giaoCa.getTienDauCa());
            ps.setBigDecimal(5, giaoCa.getTienCuoiCaHeThong());
            ps.setBigDecimal(6, giaoCa.getTienCuoiCaThucTe());
            ps.setBigDecimal(7, giaoCa.getTienChenhLech());
            ps.setString(8, giaoCa.getLyDoChenhLech());
            if (giaoCa.getGioKetThucThucTe() != null) {
                ps.setTimestamp(9, Timestamp.valueOf(giaoCa.getGioKetThucThucTe()));
            } else {
                ps.setNull(9, Types.TIMESTAMP);
            }
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Chốt kết ca và ghi nhận đối soát chênh lệch tiền mặt
     */
    public boolean chotKetCa(BangGiaoCa giaoCa) throws SQLException {
        String sql = "UPDATE BangGiaoCa SET tienCuoiCaHeThong = ?, tienCuoiCaThucTe = ?, tienChenhLech = ?, lyDoChenhLech = ?, gioKetThucThucTe = ? " +
                     "WHERE maGiaoCa = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, giaoCa.getTienCuoiCaHeThong());
            ps.setBigDecimal(2, giaoCa.getTienCuoiCaThucTe());
            ps.setBigDecimal(3, giaoCa.getTienChenhLech());
            ps.setString(4, giaoCa.getLyDoChenhLech());
            ps.setTimestamp(5, Timestamp.valueOf(giaoCa.getGioKetThucThucTe() != null ? giaoCa.getGioKetThucThucTe() : LocalDateTime.now()));
            ps.setString(6, giaoCa.getMaGiaoCa());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lấy toàn bộ danh sách biên bản bàn giao ca
     */
    public List<BangGiaoCa> getAllBangGiaoCa() throws SQLException {
        List<BangGiaoCa> list = new ArrayList<>();
        String sql = "SELECT bgc.*, nv.hoTen AS tenNhanVien, c.tenCa " +
                     "FROM BangGiaoCa bgc " +
                     "LEFT JOIN NhanVien nv ON bgc.maNhanVien = nv.maNhanVien " +
                     "LEFT JOIN CaLamViec c ON bgc.maCa = c.maCa " +
                     "ORDER BY bgc.maGiaoCa DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNhanVien(rs.getString("maNhanVien"));
                nv.setHoTen(rs.getString("tenNhanVien"));

                CaLamViec ca = new CaLamViec();
                ca.setMaCa(rs.getString("maCa"));
                ca.setTenCa(rs.getString("tenCa"));

                Timestamp kt = rs.getTimestamp("gioKetThucThucTe");
                LocalDateTime gioKT = (kt != null) ? kt.toLocalDateTime() : null;

                BangGiaoCa bgc = new BangGiaoCa(
                        rs.getString("maGiaoCa"),
                        nv,
                        ca,
                        rs.getBigDecimal("tienDauCa"),
                        rs.getBigDecimal("tienCuoiCaHeThong"),
                        rs.getBigDecimal("tienCuoiCaThucTe"),
                        rs.getBigDecimal("tienChenhLech"),
                        rs.getString("lyDoChenhLech"),
                        gioKT
                );
                list.add(bgc);
            }
        }
        return list;
    }

    /**
     * Lấy ca làm việc đang mở (chưa kết ca) của một nhân viên
     */
    public BangGiaoCa getCaHienTai(String maNhanVien) throws SQLException {
        String sql = "SELECT TOP 1 bgc.*, nv.hoTen AS tenNhanVien, c.tenCa " +
                     "FROM BangGiaoCa bgc " +
                     "LEFT JOIN NhanVien nv ON bgc.maNhanVien = nv.maNhanVien " +
                     "LEFT JOIN CaLamViec c ON bgc.maCa = c.maCa " +
                     "WHERE bgc.maNhanVien = ? AND bgc.gioKetThucThucTe IS NULL " +
                     "ORDER BY bgc.maGiaoCa DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNhanVien(rs.getString("maNhanVien"));
                    nv.setHoTen(rs.getString("tenNhanVien"));

                    CaLamViec ca = new CaLamViec();
                    ca.setMaCa(rs.getString("maCa"));
                    ca.setTenCa(rs.getString("tenCa"));

                    return new BangGiaoCa(
                            rs.getString("maGiaoCa"),
                            nv,
                            ca,
                            rs.getBigDecimal("tienDauCa"),
                            rs.getBigDecimal("tienCuoiCaHeThong"),
                            rs.getBigDecimal("tienCuoiCaThucTe"),
                            rs.getBigDecimal("tienChenhLech"),
                            rs.getString("lyDoChenhLech"),
                            null
                    );
                }
            }
        }
        return null;
    }
}
