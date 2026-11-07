package dao;

import entity.LoHang;
import entity.QuyDoiDonVi;
import entity.SanPham;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/** Ghi sản phẩm, quy đổi cơ sở và lô nhập trong một transaction duy nhất. */
public class NhapKhoDAO {
    public boolean nhapSanPhamVaLoHang(SanPham sp, QuyDoiDonVi qd, LoHang lo) throws SQLException {
        String exists = "SELECT 1 FROM SanPham WHERE maSanPham=?";
        String insertSp = "INSERT INTO SanPham(maSanPham,maDanhMuc,tenSanPham,maVach,donViCoSo,giaBanCoSo,moTa) VALUES(?,?,?,?,?,?,?)";
        String insertQd = "INSERT INTO QuyDoiDonVi(maQuyDoi,maSanPham,maDonVi,tyLeQuyDoi,laDonViCoSo,giaBan) VALUES(?,?,?,?,?,?)";
        String insertLo = "INSERT INTO LoHang(soLo,maSanPham,maNhaCungCap,ngaySanXuat,hanSuDung,donGiaNhapCoSo,soLuongNhap,soLuongTon) VALUES(?,?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            boolean daCo;
            try (PreparedStatement ps = conn.prepareStatement(exists)) {
                ps.setString(1, sp.getMaSanPham());
                try (ResultSet rs = ps.executeQuery()) { daCo = rs.next(); }
            }
            if (!daCo) {
                try (PreparedStatement ps = conn.prepareStatement(insertSp)) {
                    ps.setString(1, sp.getMaSanPham());
                    ps.setString(2, sp.getDanhMucHang().getMaDanhMuc());
                    ps.setString(3, sp.getTenSanPham());
                    if (sp.getMaVach().isBlank()) ps.setNull(4, Types.VARCHAR); else ps.setString(4, sp.getMaVach());
                    ps.setString(5, sp.getDonViCoSo());
                    ps.setBigDecimal(6, sp.getGiaBanCoSo());
                    ps.setString(7, sp.getMoTa());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(insertQd)) {
                    ps.setString(1, qd.getMaQuyDoi());
                    ps.setString(2, sp.getMaSanPham());
                    ps.setString(3, qd.getDonViTinh().getMaDonVi());
                    ps.setInt(4, qd.getTyLeQuyDoi());
                    ps.setBoolean(5, qd.isLaDonViCoSo());
                    ps.setBigDecimal(6, qd.getGiaBan());
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(insertLo)) {
                ps.setString(1, lo.getSoLo());
                ps.setString(2, sp.getMaSanPham());
                ps.setString(3, lo.getNhaCungCap().getMaNhaCungCap());
                ps.setDate(4, lo.getNgaySanXuat() != null ? Date.valueOf(lo.getNgaySanXuat()) : null);
                ps.setDate(5, Date.valueOf(lo.getHanSuDung()));
                ps.setBigDecimal(6, lo.getDonGiaNhapCoSo());
                ps.setInt(7, lo.getSoLuongNhap());
                ps.setInt(8, lo.getSoLuongTon());
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }
}
