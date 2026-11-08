package dao;

import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Nguồn dữ liệu tập trung cho Trung tâm Báo biểu.
 * Mọi báo cáo đều đọc trực tiếp từ SQL Server và dùng cùng một khoảng thời gian [từ, đến).
 */
public class BaoCaoThongKeDAO {

    public static final class DoanhThuRow {
        public String maHoaDon;
        public LocalDateTime thoiGian;
        public String khachHang;
        public String thuNgan;
        public long tienHang;
        public long giamGia;
        public long doanhThuThuan;
        public long doanhThuHangHoa;
        public long doanhThuDichVu;
        public long giaVon;
        public String phuongThuc;
    }

    public static final class TopBanChayRow {
        public String ma;
        public String ten;
        public String phanNhom;
        public String donViTinh;
        public int soLuong;
        public long doanhThu;
    }

    public static final class HieuSuatNhanSuRow {
        public String maNhanVien;
        public String hoTen;
        public String chucVu;
        public boolean dangHoatDong;
        public int soCa;
        public int soHoaDon;
        public int soDichVu;
        public long doanhThuPhuTrach;
        public long hoaHong;
    }

    public static final class KhachHangRow {
        public String maKhachHang;
        public String hoTen;
        public String soDienThoai;
        public int diemTichLuy;
    }

    public static final class HotelSummaryRow {
        public String loaiPhong;
        public int tongPhong;
        public int dangCoKhach;
        public int dangDon;
        public int phongTrong;
        public long doanhThuNgayTamTinh;
    }

    public static final class DoiTraSummary {
        public int soPhieu;
        public int soLuongSanPham;
        public long tongTienHoan;
    }

    public static final class LoHangFefoRow {
        public String soLo;
        public String maSanPham;
        public String tenSanPham;
        public LocalDate ngaySanXuat;
        public LocalDate hanSuDung;
        public long soNgayConLai;
        public int soLuongTon;
        public String donViTinh;
    }

    public static final class XuatNhapTonRow {
        public String maSanPham;
        public String tenSanPham;
        public String donViTinh;
        public int tonDauKy;
        public int nhapTrongKy;
        public int xuatBanTrongKy;
        public int hoanTraTrongKy;
        public int tonCuoiKy;
        public long donGiaBan;
        public long giaTriTonTheoGiaNhap;
    }

    public List<DoanhThuRow> getDoanhThu(LocalDateTime tu, LocalDateTime den) throws SQLException {
        String sql =
                "WITH CT_Rev AS (" +
                " SELECT ct.maHoaDon," +
                " SUM(CASE WHEN ct.loaiChiTiet IN ('SAN_PHAM','XUAT_LO','ChiTietXuatLo') THEN CAST(ct.thanhTien AS BIGINT) ELSE 0 END) doanhThuHang," +
                " SUM(CASE WHEN ct.loaiChiTiet IN ('DICH_VU','ChiTietDichVu') THEN CAST(ct.thanhTien AS BIGINT) ELSE 0 END) doanhThuDV" +
                " FROM ChiTietHoaDon ct" +
                " GROUP BY ct.maHoaDon" +
                ")," +
                "CT_Cost AS (" +
                " SELECT ct.maHoaDon," +
                " SUM(CASE WHEN ct.loaiChiTiet IN ('SAN_PHAM','XUAT_LO','ChiTietXuatLo') THEN CAST(COALESCE(xl.soLuongXuatCoSo, ct.soLuong) AS BIGINT) * CAST(COALESCE(l.donGiaNhapCoSo,0) AS BIGINT) ELSE 0 END) giaVonHang," +
                " SUM(CASE WHEN ct.loaiChiTiet IN ('DICH_VU','ChiTietDichVu') THEN CAST(COALESCE(dv.tienHoaHong,0) AS BIGINT) ELSE 0 END) giaVonDV" +
                " FROM ChiTietHoaDon ct" +
                " LEFT JOIN ChiTietXuatLo xl ON xl.maChiTiet=ct.maChiTiet" +
                " LEFT JOIN LoHang l ON l.soLo=xl.soLo" +
                " LEFT JOIN ChiTietDichVu dv ON dv.maChiTiet=ct.maChiTiet" +
                " GROUP BY ct.maHoaDon" +
                ")" +
                " SELECT h.maHoaDon, COALESCE(h.thoiGianThanhToan,h.ngayLap) thoiGian," +
                " COALESCE(kh.hoTen,N'Khách vãng lai') khachHang, COALESCE(nv.hoTen,N'Không xác định') thuNgan," +
                " CAST(COALESCE(rev.doanhThuHang,0)+COALESCE(rev.doanhThuDV,0) AS BIGINT) tienHang," +
                " CAST(COALESCE(h.giamGia,0)+COALESCE(h.tienDoiDiem,0) AS BIGINT) giamGia," +
                " CAST(COALESCE(h.thanhTien,0) AS BIGINT) doanhThuThuan," +
                " CAST(COALESCE(rev.doanhThuHang,0) AS BIGINT) doanhThuHang," +
                " CAST(COALESCE(rev.doanhThuDV,0) AS BIGINT) doanhThuDV," +
                " CAST(COALESCE(cost.giaVonHang,0) + COALESCE(cost.giaVonDV,0) AS BIGINT) giaVon," +
                " COALESCE(h.phuongThucThanhToan,N'Không xác định') phuongThuc" +
                " FROM HoaDon h" +
                " LEFT JOIN KhachHang kh ON kh.maKhachHang=h.maKhachHang" +
                " LEFT JOIN NhanVien nv ON nv.maNhanVien=h.maNhanVien" +
                " LEFT JOIN CT_Rev rev ON rev.maHoaDon=h.maHoaDon" +
                " LEFT JOIN CT_Cost cost ON cost.maHoaDon=h.maHoaDon" +
                " WHERE h.trangThaiHoaDon=N'Đã thanh toán'" +
                " AND COALESCE(h.thoiGianThanhToan,h.ngayLap)>=? AND COALESCE(h.thoiGianThanhToan,h.ngayLap)<?" +
                " ORDER BY thoiGian DESC";
        List<DoanhThuRow> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setRange(ps, tu, den);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DoanhThuRow r = new DoanhThuRow();
                    r.maHoaDon = rs.getString("maHoaDon");
                    r.thoiGian = rs.getTimestamp("thoiGian").toLocalDateTime();
                    r.khachHang = rs.getString("khachHang");
                    r.thuNgan = rs.getString("thuNgan");
                    r.tienHang = rs.getLong("tienHang");
                    r.giamGia = rs.getLong("giamGia");
                    r.doanhThuThuan = rs.getLong("doanhThuThuan");
                    long hangGross = rs.getLong("doanhThuHang");
                    long dvGross = rs.getLong("doanhThuDV");
                    long gross = hangGross + dvGross;
                    r.doanhThuHangHoa = gross > 0 ? Math.round(r.doanhThuThuan * (hangGross * 1.0 / gross)) : r.doanhThuThuan;
                    r.doanhThuDichVu = r.doanhThuThuan - r.doanhThuHangHoa;
                    r.giaVon = rs.getLong("giaVon");
                    r.phuongThuc = rs.getString("phuongThuc");
                    rows.add(r);
                }
            }
        }
        return rows;
    }

    public List<TopBanChayRow> getTopBanChay(LocalDateTime tu, LocalDateTime den) throws SQLException {
        String sql =
                "SELECT COALESCE(ct.maDichVu, ct.maSanPham) ma," +
                " COALESCE(d.tenDichVu, sp.tenSanPham, N'Sản phẩm') ten," +
                " CASE WHEN ct.loaiChiTiet IN ('DICH_VU','ChiTietDichVu') THEN N'Dịch vụ' ELSE N'Hàng hóa' END phanNhom," +
                " CASE WHEN ct.loaiChiTiet IN ('DICH_VU','ChiTietDichVu') THEN N'Lượt' ELSE COALESCE(sp.donViCoSo, N'Đơn vị') END donViTinh," +
                " SUM(ct.soLuong) soLuong, SUM(CAST(ct.thanhTien AS BIGINT)) doanhThu" +
                " FROM ChiTietHoaDon ct" +
                " JOIN HoaDon h ON h.maHoaDon=ct.maHoaDon" +
                " LEFT JOIN SanPham sp ON sp.maSanPham=ct.maSanPham" +
                " LEFT JOIN DichVu d ON d.maDichVu=ct.maDichVu" +
                " WHERE h.trangThaiHoaDon=N'Đã thanh toán'" +
                " AND COALESCE(h.thoiGianThanhToan,h.ngayLap)>=? AND COALESCE(h.thoiGianThanhToan,h.ngayLap)<?" +
                " GROUP BY COALESCE(ct.maDichVu, ct.maSanPham)," +
                " COALESCE(d.tenDichVu, sp.tenSanPham, N'Sản phẩm')," +
                " CASE WHEN ct.loaiChiTiet IN ('DICH_VU','ChiTietDichVu') THEN N'Dịch vụ' ELSE N'Hàng hóa' END," +
                " CASE WHEN ct.loaiChiTiet IN ('DICH_VU','ChiTietDichVu') THEN N'Lượt' ELSE COALESCE(sp.donViCoSo, N'Đơn vị') END" +
                " ORDER BY doanhThu DESC, soLuong DESC";
        List<TopBanChayRow> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setRange(ps, tu, den);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TopBanChayRow r = new TopBanChayRow();
                    r.ma = rs.getString("ma");
                    r.ten = rs.getString("ten");
                    r.phanNhom = rs.getString("phanNhom");
                    r.donViTinh = rs.getString("donViTinh");
                    r.soLuong = rs.getInt("soLuong");
                    r.doanhThu = rs.getLong("doanhThu");
                    rows.add(r);
                }
            }
        }
        return rows;
    }

    public List<HieuSuatNhanSuRow> getHieuSuatNhanSu(LocalDateTime tu, LocalDateTime den) throws SQLException {
        String sql =
                "WITH PC AS (SELECT maNhanVien,COUNT(*) soCa FROM BangPhanCong WHERE ngayLamViec>=CAST(? AS date) AND ngayLamViec<CAST(? AS date) GROUP BY maNhanVien)," +
                " HD AS (SELECT maNhanVien,COUNT(*) soHD,SUM(CAST(thanhTien AS BIGINT)) doanhThu FROM HoaDon" +
                " WHERE trangThaiHoaDon=N'Đã thanh toán' AND COALESCE(thoiGianThanhToan,ngayLap)>=? AND COALESCE(thoiGianThanhToan,ngayLap)<? GROUP BY maNhanVien)," +
                " DV AS (SELECT dv.maKtvThucHien,COUNT(*) soDV,SUM(CAST(ct.thanhTien AS BIGINT)) doanhThuDV,SUM(CAST(COALESCE(dv.tienHoaHong,0) AS BIGINT)) hoaHong" +
                " FROM ChiTietDichVu dv JOIN ChiTietHoaDon ct ON ct.maChiTiet=dv.maChiTiet JOIN HoaDon h ON h.maHoaDon=ct.maHoaDon" +
                " WHERE h.trangThaiHoaDon=N'Đã thanh toán' AND COALESCE(h.thoiGianThanhToan,h.ngayLap)>=? AND COALESCE(h.thoiGianThanhToan,h.ngayLap)<? GROUP BY dv.maKtvThucHien)" +
                " SELECT nv.maNhanVien,nv.hoTen,nv.chucVu,COALESCE(tk.trangThaiHoatDong,1) hoatDong," +
                " COALESCE(pc.soCa,0) soCa,COALESCE(hd.soHD,0) soHD,COALESCE(dv.soDV,0) soDV," +
                " COALESCE(hd.doanhThu,0)+COALESCE(dv.doanhThuDV,0) doanhThu,COALESCE(dv.hoaHong,0) hoaHong" +
                " FROM NhanVien nv LEFT JOIN TaiKhoan tk ON tk.maTaiKhoan=nv.maTaiKhoan" +
                " LEFT JOIN PC pc ON pc.maNhanVien=nv.maNhanVien LEFT JOIN HD hd ON hd.maNhanVien=nv.maNhanVien LEFT JOIN DV dv ON dv.maKtvThucHien=nv.maNhanVien" +
                " ORDER BY doanhThu DESC,nv.maNhanVien";
        List<HieuSuatNhanSuRow> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(tu));
            ps.setTimestamp(2, Timestamp.valueOf(den));
            ps.setTimestamp(3, Timestamp.valueOf(tu));
            ps.setTimestamp(4, Timestamp.valueOf(den));
            ps.setTimestamp(5, Timestamp.valueOf(tu));
            ps.setTimestamp(6, Timestamp.valueOf(den));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HieuSuatNhanSuRow r = new HieuSuatNhanSuRow();
                    r.maNhanVien = rs.getString("maNhanVien");
                    r.hoTen = rs.getString("hoTen");
                    r.chucVu = rs.getString("chucVu");
                    r.dangHoatDong = rs.getBoolean("hoatDong");
                    r.soCa = rs.getInt("soCa");
                    r.soHoaDon = rs.getInt("soHD");
                    r.soDichVu = rs.getInt("soDV");
                    r.doanhThuPhuTrach = rs.getLong("doanhThu");
                    r.hoaHong = rs.getLong("hoaHong");
                    rows.add(r);
                }
            }
        }
        return rows;
    }

    public List<KhachHangRow> getKhachHang() throws SQLException {
        List<KhachHangRow> rows = new ArrayList<>();
        String sql = "SELECT maKhachHang,hoTen,soDienThoai,diemTichLuy FROM KhachHang ORDER BY diemTichLuy DESC,maKhachHang";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                KhachHangRow r = new KhachHangRow();
                r.maKhachHang = rs.getString("maKhachHang");
                r.hoTen = rs.getString("hoTen");
                r.soDienThoai = rs.getString("soDienThoai");
                r.diemTichLuy = rs.getInt("diemTichLuy");
                rows.add(r);
            }
        }
        return rows;
    }

    public List<HotelSummaryRow> getHotelSummary() throws SQLException {
        String sql = "SELECT loaiPhong,COUNT(*) tongPhong," +
                " SUM(CASE WHEN trangThai=N'ĐANG CÓ KHÁCH' THEN 1 ELSE 0 END) dangCoKhach," +
                " SUM(CASE WHEN trangThai=N'ĐANG DỌN DẸP' THEN 1 ELSE 0 END) dangDon," +
                " SUM(CASE WHEN trangThai=N'TRỐNG' THEN 1 ELSE 0 END) phongTrong," +
                " SUM(CASE WHEN trangThai=N'ĐANG CÓ KHÁCH' THEN donGiaNgay+COALESCE(phiDichVuPhatSinh,0) ELSE 0 END) doanhThuNgay" +
                " FROM PhongLuuTru GROUP BY loaiPhong ORDER BY loaiPhong";
        List<HotelSummaryRow> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HotelSummaryRow r = new HotelSummaryRow();
                r.loaiPhong = rs.getString("loaiPhong");
                r.tongPhong = rs.getInt("tongPhong");
                r.dangCoKhach = rs.getInt("dangCoKhach");
                r.dangDon = rs.getInt("dangDon");
                r.phongTrong = rs.getInt("phongTrong");
                r.doanhThuNgayTamTinh = rs.getLong("doanhThuNgay");
                rows.add(r);
            }
        }
        return rows;
    }

    public DoiTraSummary getDoiTraSummary(LocalDateTime tu, LocalDateTime den) throws SQLException {
        String sql =
                "WITH P AS (" +
                " SELECT p.maPhieuDoiTra, p.tienHoanThucTe" +
                " FROM PhieuDoiTra p" +
                " WHERE p.ngayDoiTra>=? AND p.ngayDoiTra<?" +
                ")," +
                "CT AS (" +
                " SELECT ct.maPhieuDoiTra, SUM(ct.soLuongDoiTra) soLuong" +
                " FROM ChiTietDoiTra ct" +
                " WHERE ct.maPhieuDoiTra IN (SELECT maPhieuDoiTra FROM P)" +
                " GROUP BY ct.maPhieuDoiTra" +
                ")" +
                " SELECT COUNT(p.maPhieuDoiTra) soPhieu," +
                " COALESCE(SUM(ct.soLuong),0) soLuong," +
                " COALESCE(SUM(CAST(p.tienHoanThucTe AS BIGINT)),0) tongHoan" +
                " FROM P p" +
                " LEFT JOIN CT ct ON ct.maPhieuDoiTra=p.maPhieuDoiTra";
        DoiTraSummary r = new DoiTraSummary();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setRange(ps, tu, den);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r.soPhieu = rs.getInt("soPhieu");
                    r.soLuongSanPham = rs.getInt("soLuong");
                    r.tongTienHoan = rs.getLong("tongHoan");
                }
            }
        }
        return r;
    }

    public List<LoHangFefoRow> getLoHangFefo() throws SQLException {
        String sql = "SELECT l.soLo,l.maSanPham,sp.tenSanPham,l.ngaySanXuat,l.hanSuDung," +
                " DATEDIFF(day,CAST(GETDATE() AS date),l.hanSuDung) soNgayConLai,l.soLuongTon,COALESCE(dvt.tenDonVi,N'Đơn vị') donViTinh" +
                " FROM LoHang l JOIN SanPham sp ON sp.maSanPham=l.maSanPham" +
                " OUTER APPLY (SELECT TOP 1 dv.tenDonVi FROM QuyDoiDonVi q JOIN DonViTinh dv ON dv.maDonVi=q.maDonVi" +
                " WHERE q.maSanPham=l.maSanPham AND q.tyLeQuyDoi=1 ORDER BY q.maQuyDoi) dvt" +
                " WHERE l.soLuongTon>0 ORDER BY l.hanSuDung,l.soLo";
        List<LoHangFefoRow> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LoHangFefoRow r = new LoHangFefoRow();
                r.soLo = rs.getString("soLo");
                r.maSanPham = rs.getString("maSanPham");
                r.tenSanPham = rs.getString("tenSanPham");
                r.ngaySanXuat = rs.getDate("ngaySanXuat").toLocalDate();
                r.hanSuDung = rs.getDate("hanSuDung").toLocalDate();
                r.soNgayConLai = rs.getLong("soNgayConLai");
                r.soLuongTon = rs.getInt("soLuongTon");
                r.donViTinh = rs.getString("donViTinh");
                rows.add(r);
            }
        }
        return rows;
    }

    public List<XuatNhapTonRow> getXuatNhapTon(LocalDateTime tu, LocalDateTime den) throws SQLException {
        String sql =
                "WITH L AS (SELECT maSanPham,SUM(CAST(soLuongTon AS BIGINT)) tonHienTai," +
                " SUM(CAST(soLuongTon AS BIGINT)*CAST(donGiaNhapCoSo AS BIGINT)) giaTriHienTai FROM LoHang GROUP BY maSanPham)," +
                " N AS (SELECT maSanPham," +
                " SUM(CASE WHEN ngayNhap>=? AND ngayNhap<? THEN soLuongNhap ELSE 0 END) nhapKy," +
                " SUM(CASE WHEN ngayNhap>=? THEN soLuongNhap ELSE 0 END) nhapSau," +
                " SUM(CASE WHEN ngayNhap>=? THEN CAST(soLuongNhap AS BIGINT)*CAST(donGiaNhapCoSo AS BIGINT) ELSE 0 END) giaTriNhapSau" +
                " FROM LoHang GROUP BY maSanPham)," +
                " X AS (SELECT l.maSanPham," +
                " SUM(CASE WHEN COALESCE(h.thoiGianThanhToan,h.ngayLap)>=? AND COALESCE(h.thoiGianThanhToan,h.ngayLap)<? THEN x.soLuongXuatCoSo ELSE 0 END) xuatKy," +
                " SUM(CASE WHEN COALESCE(h.thoiGianThanhToan,h.ngayLap)>=? THEN x.soLuongXuatCoSo ELSE 0 END) xuatSau," +
                " SUM(CASE WHEN COALESCE(h.thoiGianThanhToan,h.ngayLap)>=? THEN CAST(x.soLuongXuatCoSo AS BIGINT)*CAST(l.donGiaNhapCoSo AS BIGINT) ELSE 0 END) giaTriXuatSau" +
                " FROM ChiTietXuatLo x JOIN LoHang l ON l.soLo=x.soLo JOIN ChiTietHoaDon cthd ON cthd.maChiTiet=x.maChiTiet" +
                " JOIN HoaDon h ON h.maHoaDon=cthd.maHoaDon WHERE h.trangThaiHoaDon=N'Đã thanh toán' GROUP BY l.maSanPham)," +
                " R AS (SELECT ct.maSanPham," +
                " SUM(CASE WHEN p.ngayDoiTra>=? AND p.ngayDoiTra<? THEN COALESCE(NULLIF(ct.soLuongCoSoHoan,0),ct.soLuongDoiTra) ELSE 0 END) traKy," +
                " SUM(CASE WHEN p.ngayDoiTra>=? THEN COALESCE(NULLIF(ct.soLuongCoSoHoan,0),ct.soLuongDoiTra) ELSE 0 END) traSau," +
                " SUM(CASE WHEN p.ngayDoiTra>=? THEN CAST(COALESCE(NULLIF(ct.soLuongCoSoHoan,0),ct.soLuongDoiTra) AS BIGINT)*CAST(COALESCE(l.donGiaNhapCoSo,0) AS BIGINT) ELSE 0 END) giaTriTraSau" +
                " FROM ChiTietDoiTra ct JOIN PhieuDoiTra p ON p.maPhieuDoiTra=ct.maPhieuDoiTra LEFT JOIN LoHang l ON l.soLo=ct.soLo GROUP BY ct.maSanPham)" +
                " SELECT sp.maSanPham,sp.tenSanPham,COALESCE(dvt.tenDonVi,N'Đơn vị') donViTinh," +
                " (COALESCE(l.tonHienTai,0)-COALESCE(n.nhapSau,0)+COALESCE(x.xuatSau,0)-COALESCE(r.traSau,0)) tonCuoi," +
                " COALESCE(n.nhapKy,0) nhapKy,COALESCE(x.xuatKy,0) xuatKy,COALESCE(r.traKy,0) traKy," +
                " COALESCE(qd.giaBan,0) giaBan," +
                " (COALESCE(l.giaTriHienTai,0)-COALESCE(n.giaTriNhapSau,0)+COALESCE(x.giaTriXuatSau,0)-COALESCE(r.giaTriTraSau,0)) giaTriTon" +
                " FROM SanPham sp LEFT JOIN L l ON l.maSanPham=sp.maSanPham LEFT JOIN N n ON n.maSanPham=sp.maSanPham" +
                " LEFT JOIN X x ON x.maSanPham=sp.maSanPham LEFT JOIN R r ON r.maSanPham=sp.maSanPham" +
                " OUTER APPLY (SELECT TOP 1 q.giaBan,q.maDonVi FROM QuyDoiDonVi q WHERE q.maSanPham=sp.maSanPham AND q.tyLeQuyDoi=1 ORDER BY q.maQuyDoi) qd" +
                " LEFT JOIN DonViTinh dvt ON dvt.maDonVi=qd.maDonVi ORDER BY sp.maSanPham";
        List<XuatNhapTonRow> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setTimestamp(i++, Timestamp.valueOf(tu)); ps.setTimestamp(i++, Timestamp.valueOf(den));
            ps.setTimestamp(i++, Timestamp.valueOf(den)); ps.setTimestamp(i++, Timestamp.valueOf(den));
            ps.setTimestamp(i++, Timestamp.valueOf(tu)); ps.setTimestamp(i++, Timestamp.valueOf(den));
            ps.setTimestamp(i++, Timestamp.valueOf(den)); ps.setTimestamp(i++, Timestamp.valueOf(den));
            ps.setTimestamp(i++, Timestamp.valueOf(tu)); ps.setTimestamp(i++, Timestamp.valueOf(den));
            ps.setTimestamp(i++, Timestamp.valueOf(den)); ps.setTimestamp(i++, Timestamp.valueOf(den));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    XuatNhapTonRow r = new XuatNhapTonRow();
                    r.maSanPham = rs.getString("maSanPham");
                    r.tenSanPham = rs.getString("tenSanPham");
                    r.donViTinh = rs.getString("donViTinh");
                    r.tonCuoiKy = Math.max(0, rs.getInt("tonCuoi"));
                    r.nhapTrongKy = rs.getInt("nhapKy");
                    r.xuatBanTrongKy = rs.getInt("xuatKy");
                    r.hoanTraTrongKy = rs.getInt("traKy");
                    r.tonDauKy = Math.max(0, r.tonCuoiKy - r.nhapTrongKy + r.xuatBanTrongKy - r.hoanTraTrongKy);
                    r.donGiaBan = rs.getLong("giaBan");
                    r.giaTriTonTheoGiaNhap = rs.getLong("giaTriTon");
                    rows.add(r);
                }
            }
        }
        return rows;
    }

    private static void setRange(PreparedStatement ps, LocalDateTime tu, LocalDateTime den) throws SQLException {
        ps.setTimestamp(1, Timestamp.valueOf(tu));
        ps.setTimestamp(2, Timestamp.valueOf(den));
    }
}
