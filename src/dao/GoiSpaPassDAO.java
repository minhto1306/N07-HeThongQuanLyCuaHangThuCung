package dao;

import entity.GoiSpaPass;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object quản lý danh mục cấu hình Gói Spa Pass (DV14).
 * Truy vấn thông tin số buổi, đơn giá và thời hạn hiệu lực từ bảng GoiSpaPass.
 */
public class GoiSpaPassDAO {

    public List<GoiSpaPass> getAllGoiSpaPass() {
        String sql = "SELECT maGoi, tenGoi, soBuoi, donGiaGoi, thoiHanNgay, trangThaiHoatDong FROM GoiSpaPass WHERE trangThaiHoatDong = 1";
        List<GoiSpaPass> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new GoiSpaPass(
                        rs.getString("maGoi"),
                        rs.getString("tenGoi"),
                        rs.getInt("soBuoi"),
                        rs.getBigDecimal("donGiaGoi"),
                        rs.getInt("thoiHanNgay"),
                        rs.getBoolean("trangThaiHoatDong")
                ));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải cấu hình GoiSpaPass từ CSDL; dừng bán gói để tránh dùng giá/lượt hard-code", e);
        }
        return list;
    }

    public GoiSpaPass findByMaGoi(String maGoi) {
        if (maGoi == null || maGoi.isBlank()) return null;
        String cleanMa = maGoi.trim().toUpperCase();

        String sql = "SELECT maGoi, tenGoi, soBuoi, donGiaGoi, thoiHanNgay, trangThaiHoatDong FROM GoiSpaPass WHERE UPPER(maGoi) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cleanMa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new GoiSpaPass(
                            rs.getString("maGoi"),
                            rs.getString("tenGoi"),
                            rs.getInt("soBuoi"),
                            rs.getBigDecimal("donGiaGoi"),
                            rs.getInt("thoiHanNgay"),
                            rs.getBoolean("trangThaiHoatDong")
                    );
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tra cứu GoiSpaPass [" + cleanMa + "] từ CSDL", e);
        }
        return null;
    }
}
