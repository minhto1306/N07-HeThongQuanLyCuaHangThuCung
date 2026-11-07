package dao;

import entity.KhachHang;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// DAO thao tác với bảng KhachHang trong SQL Server
// đã bổ sung đọc thêm 2 cột email và diaChi để gửi thông báo lịch hẹn với pet hotel
public class KhachHangDAO {

    // hàm phụ trợ để lấy chuỗi an toàn, lỡ database máy nào chưa chạy alter table thêm cột email
    // thì nó không bị văng exception sập ứng dụng
    private String layGiaTriAnToan(ResultSet rs, String colName) {
        try {
            String val = rs.getString(colName);
            return val != null ? val : "";
        } catch (Exception e) {
            return "";
        }
    }

    // Lấy toàn bộ danh sách khách hàng sắp xếp theo tên
    public List<KhachHang> getAllKhachHang() throws SQLException {
        List<KhachHang> list = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang ORDER BY hoTen ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new KhachHang(
                        rs.getString("maKhachHang"),
                        rs.getString("hoTen"),
                        rs.getString("soDienThoai"),
                        rs.getInt("diemTichLuy"),
                        layGiaTriAnToan(rs, "email"),
                        layGiaTriAnToan(rs, "diaChi")
                ));
            }
        }
        return list;
    }

    // Tìm khách hàng bằng số điện thoại - cái này quầy POS hay gọi nhất để tích điểm
    public KhachHang findBySoDienThoai(String soDienThoai) throws SQLException {
        String sql = "SELECT * FROM KhachHang WHERE soDienThoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soDienThoai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new KhachHang(
                            rs.getString("maKhachHang"),
                            rs.getString("hoTen"),
                            rs.getString("soDienThoai"),
                            rs.getInt("diemTichLuy"),
                            layGiaTriAnToan(rs, "email"),
                            layGiaTriAnToan(rs, "diaChi")
                    );
                }
            }
        }
        return null;
    }

    // Tìm khách hàng theo mã định danh KH001, KH002...
    public KhachHang findByMaKhachHang(String maKhachHang) throws SQLException {
        String sql = "SELECT * FROM KhachHang WHERE maKhachHang = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new KhachHang(
                            rs.getString("maKhachHang"),
                            rs.getString("hoTen"),
                            rs.getString("soDienThoai"),
                            rs.getInt("diemTichLuy"),
                            layGiaTriAnToan(rs, "email"),
                            layGiaTriAnToan(rs, "diaChi")
                    );
                }
            }
        }
        return null;
    }

    // Thêm khách hàng mới: ưu tiên insert kèm cả email và địa chỉ nếu db đã có cột
    public boolean insert(KhachHang kh) throws SQLException {
        String sqlCoEmail = "INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, diemTichLuy, email, diaChi) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlCoEmail)) {
            ps.setString(1, kh.getMaKhachHang());
            ps.setString(2, kh.getHoTen());
            ps.setString(3, kh.getSoDienThoai());
            ps.setInt(4, kh.getDiemTichLuy());
            ps.setString(5, kh.getEmail());
            ps.setString(6, kh.getDiaChi());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // nếu lỗi do chưa có cột email hoặc diaChi thì fallback về câu lệnh insert cũ
            String sqlCu = "INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, diemTichLuy) VALUES (?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement psCu = conn.prepareStatement(sqlCu)) {
                psCu.setString(1, kh.getMaKhachHang());
                psCu.setString(2, kh.getHoTen());
                psCu.setString(3, kh.getSoDienThoai());
                psCu.setInt(4, kh.getDiemTichLuy());
                return psCu.executeUpdate() > 0;
            }
        }
    }

    // Cập nhật thông tin khách hàng, có cập nhật cả email và địa chỉ mới
    public boolean update(KhachHang kh) throws SQLException {
        String sqlCoEmail = "UPDATE KhachHang SET hoTen = ?, soDienThoai = ?, diemTichLuy = ?, email = ?, diaChi = ? WHERE maKhachHang = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlCoEmail)) {
            ps.setString(1, kh.getHoTen());
            ps.setString(2, kh.getSoDienThoai());
            ps.setInt(3, kh.getDiemTichLuy());
            ps.setString(4, kh.getEmail());
            ps.setString(5, kh.getDiaChi());
            ps.setString(6, kh.getMaKhachHang());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // fallback nếu csdl máy bạn khác chưa kịp alter table
            String sqlCu = "UPDATE KhachHang SET hoTen = ?, soDienThoai = ?, diemTichLuy = ? WHERE maKhachHang = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement psCu = conn.prepareStatement(sqlCu)) {
                psCu.setString(1, kh.getHoTen());
                psCu.setString(2, kh.getSoDienThoai());
                psCu.setInt(3, kh.getDiemTichLuy());
                psCu.setString(4, kh.getMaKhachHang());
                return psCu.executeUpdate() > 0;
            }
        }
    }

    // Xóa khách hàng theo mã
    public boolean delete(String maKhachHang) throws SQLException {
        String sql = "DELETE FROM KhachHang WHERE maKhachHang = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            return ps.executeUpdate() > 0;
        }
    }

    // Cộng hoặc trừ điểm tích lũy của khách hàng khi thanh toán hóa đơn
    public boolean capNhatDiemTichLuy(String maKhachHang, int diemThayDoi) throws SQLException {
        String sql = "UPDATE KhachHang SET diemTichLuy = diemTichLuy + ? WHERE maKhachHang = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, diemThayDoi);
            ps.setString(2, maKhachHang);
            return ps.executeUpdate() > 0;
        }
    }
}
