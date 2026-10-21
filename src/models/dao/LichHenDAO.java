package models.dao;

import models.entity.*;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Lịch hẹn và Chi tiết Lịch hẹn dịch vụ.
 */
public class LichHenDAO {

    /**
     * Lấy toàn bộ danh sách lịch hẹn kèm khách hàng và chi tiết dịch vụ
     */
    public List<LichHen> getAllLichHen() throws SQLException {
        List<LichHen> list = new ArrayList<>();
        String sql = "SELECT lh.maLichHen, lh.ngayHen, lh.trangThaiLichHen, lh.ghiChu, " +
                "kh.maKhachHang, kh.hoTen AS tenKhachHang, kh.soDienThoai, kh.diemTichLuy, " +
                "nv.maNhanVien AS maNvTiepNhan, nv.hoTen AS tenNvTiepNhan " +
                "FROM LichHen lh " +
                "JOIN KhachHang kh ON lh.maKhachHang = kh.maKhachHang " +
                "LEFT JOIN NhanVien nv ON lh.maNhanVienTiepNhan = nv.maNhanVien " +
                "ORDER BY lh.ngayHen DESC";

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

                NhanVien nv = null;
                if (rs.getString("maNvTiepNhan") != null) {
                    nv = new NhanVien();
                    nv.setMaNhanVien(rs.getString("maNvTiepNhan"));
                    nv.setHoTen(rs.getString("tenNvTiepNhan"));
                }

                Timestamp ts = rs.getTimestamp("ngayHen");
                LocalDateTime ngayHen = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

                LichHen lh = new LichHen(
                        rs.getString("maLichHen"),
                        kh,
                        nv,
                        ngayHen,
                        rs.getString("trangThaiLichHen"),
                        rs.getString("ghiChu")
                );

                // Nạp danh sách chi tiết
                lh.setDsChiTiet(getChiTietByLichHen(conn, lh.getMaLichHen(), lh));
                list.add(lh);
            }
        }
        return list;
    }

    /**
     * Lấy danh sách chi tiết lịch hẹn của 1 mã lịch hẹn
     */
    private List<ChiTietLichHen> getChiTietByLichHen(Connection conn, String maLichHen, LichHen lh) throws SQLException {
        List<ChiTietLichHen> ds = new ArrayList<>();
        String sql = "SELECT ctlh.maChiTietLichHen, " +
                "tc.maThuCung, tc.tenThuCung, tc.giongLoai, " +
                "dv.maDichVu, dv.tenDichVu, dv.giaDichVu, dv.thoiLuongPhut, " +
                "ktv.maNhanVien AS maKtv, ktv.hoTen AS tenKtv " +
                "FROM ChiTietLichHen ctlh " +
                "JOIN ThuCung tc ON ctlh.maThuCung = tc.maThuCung " +
                "JOIN DichVu dv ON ctlh.maDichVu = dv.maDichVu " +
                "LEFT JOIN NhanVien ktv ON ctlh.maKtvChiDinh = ktv.maNhanVien " +
                "WHERE ctlh.maLichHen = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLichHen);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThuCung tc = new ThuCung();
                    tc.setMaThuCung(rs.getString("maThuCung"));
                    tc.setTenThuCung(rs.getString("tenThuCung"));
                    tc.setGiongLoai(rs.getString("giongLoai"));

                    DichVu dv = new DichVu(
                            rs.getString("maDichVu"),
                            rs.getString("tenDichVu"),
                            rs.getBigDecimal("giaDichVu"),
                            rs.getInt("thoiLuongPhut")
                    );

                    NhanVien ktv = null;
                    if (rs.getString("maKtv") != null) {
                        ktv = new NhanVien();
                        ktv.setMaNhanVien(rs.getString("maKtv"));
                        ktv.setHoTen(rs.getString("tenKtv"));
                    }

                    ChiTietLichHen ct = new ChiTietLichHen(
                            rs.getString("maChiTietLichHen"),
                            lh,
                            tc,
                            dv,
                            ktv
                    );
                    ds.add(ct);
                }
            }
        }
        return ds;
    }

    /**
     * Thêm lịch hẹn mới kèm chi tiết (Transaction JDBC an toàn)
     */
    public boolean insertLichHen(LichHen lh) throws SQLException {
        String sqlLH = "INSERT INTO LichHen (maLichHen, maKhachHang, maNhanVienTiepNhan, ngayHen, trangThaiLichHen, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlCT = "INSERT INTO ChiTietLichHen (maChiTietLichHen, maLichHen, maThuCung, maDichVu, maKtvChiDinh) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psLH = conn.prepareStatement(sqlLH)) {
                psLH.setString(1, lh.getMaLichHen());
                psLH.setString(2, lh.getKhachHang().getMaKhachHang());
                psLH.setString(3, lh.getNhanVienTiepNhan() != null ? lh.getNhanVienTiepNhan().getMaNhanVien() : null);
                psLH.setTimestamp(4, Timestamp.valueOf(lh.getNgayHen() != null ? lh.getNgayHen() : LocalDateTime.now()));
                psLH.setString(5, lh.getTrangThaiLichHen() != null ? lh.getTrangThaiLichHen() : "Đã đặt");
                psLH.setString(6, lh.getGhiChu());
                psLH.executeUpdate();
            }

            for (ChiTietLichHen ct : lh.getDsChiTiet()) {
                try (PreparedStatement psCT = conn.prepareStatement(sqlCT)) {
                    psCT.setString(1, ct.getMaChiTietLichHen());
                    psCT.setString(2, lh.getMaLichHen());
                    psCT.setString(3, ct.getThuCung().getMaThuCung());
                    psCT.setString(4, ct.getDichVu().getMaDichVu());
                    psCT.setString(5, ct.getKtvChiDinh() != null ? ct.getKtvChiDinh().getMaNhanVien() : null);
                    psCT.executeUpdate();
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
     * Cập nhật trạng thái lịch hẹn (Đã đặt, Đang phục vụ, Hoàn thành, Hủy)
     */
    public boolean capNhatTrangThai(String maLichHen, String trangThaiMoi) throws SQLException {
        String sql = "UPDATE LichHen SET trangThaiLichHen = ? WHERE maLichHen = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThaiMoi);
            ps.setString(2, maLichHen);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Phân công KTV chỉ định cho chi tiết lịch hẹn
     */
    public boolean capNhatKtvChiDinh(String maChiTietLichHen, String maKtv) throws SQLException {
        String sql = "UPDATE ChiTietLichHen SET maKtvChiDinh = ? WHERE maChiTietLichHen = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKtv);
            ps.setString(2, maChiTietLichHen);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa lịch hẹn kèm toàn bộ chi tiết (Transaction)
     */
    public boolean deleteLichHen(String maLichHen) throws SQLException {
        String sqlCT = "DELETE FROM ChiTietLichHen WHERE maLichHen = ?";
        String sqlLH = "DELETE FROM LichHen WHERE maLichHen = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlCT)) {
                ps1.setString(1, maLichHen);
                ps1.executeUpdate();
            }

            int rows;
            try (PreparedStatement ps2 = conn.prepareStatement(sqlLH)) {
                ps2.setString(1, maLichHen);
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
}
