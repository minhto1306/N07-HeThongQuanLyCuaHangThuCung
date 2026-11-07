package dao;



import entity.BangGiaoCa;

import entity.CaLamViec;

import entity.NhanVien;

import utils.DBConnection;



import java.sql.*;

import java.time.LocalDateTime;

import java.math.BigDecimal;

import java.util.ArrayList;

import java.util.List;



/**

 * Data Access Object cho Quản lý Ca làm việc & Biên bản Bàn giao ca (BangGiaoCa).

 * Cung cấp đầy đủ các thao tác CRUD và kiểm soát đối soát quỹ tiền mặt.

 */

public class BangGiaoCaDAO {

    public String sinhMaGiaoCaMoi() {
        String sql = "SELECT dbo.fn_SinhMaGiaoCa()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString(1);
        } catch (SQLException ignored) {}
        return "GC001";
    }



    /**

     * Mở ca làm việc mới khi nhân viên bắt đầu ca trực

     */

    public boolean moCaLamViec(BangGiaoCa giaoCa) throws SQLException {

        return insertBangGiaoCa(giaoCa);

    }



    public boolean insertBangGiaoCa(BangGiaoCa giaoCa) throws SQLException {

        String sql = "INSERT INTO BangGiaoCa (maGiaoCa, maNhanVien, maCa, tienDauCa, tienCuoiCaHeThong, tienCuoiCaThucTe, tienChenhLech, " +

                     "tienThuTienMat, tienChuyenKhoan, tienHoanTra, lyDoChenhLech, gioKetThucThucTe, trangThai, maQuanLyDuyet, thoiGianDuyet) " +

                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();

             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, giaoCa.getMaGiaoCa());

            ps.setString(2, giaoCa.getNhanVien() != null ? giaoCa.getNhanVien().getMaNhanVien() : "NV002");

            ps.setString(3, giaoCa.getCaLamViec() != null ? giaoCa.getCaLamViec().getMaCa() : "CA01");

            ps.setBigDecimal(4, giaoCa.getTienDauCa());

            ps.setBigDecimal(5, giaoCa.getTienCuoiCaHeThong());

            ps.setBigDecimal(6, giaoCa.getTienCuoiCaThucTe());

            ps.setBigDecimal(7, giaoCa.getTienChenhLech());

            ps.setBigDecimal(8, giaoCa.getTienThuTienMat());

            ps.setBigDecimal(9, giaoCa.getTienChuyenKhoan());

            ps.setBigDecimal(10, giaoCa.getTienHoanTra());

            ps.setString(11, giaoCa.getLyDoChenhLech());

            if (giaoCa.getGioKetThucThucTe() != null) {

                ps.setTimestamp(12, Timestamp.valueOf(giaoCa.getGioKetThucThucTe()));

            } else {

                ps.setNull(12, Types.TIMESTAMP);

            }

            ps.setString(13, giaoCa.getTrangThai() != null ? giaoCa.getTrangThai() : "CHO_DUYET");

            if (giaoCa.getQuanLyDuyet() != null) ps.setString(14, giaoCa.getQuanLyDuyet().getMaNhanVien());

            else ps.setNull(14, Types.NVARCHAR);

            if (giaoCa.getThoiGianDuyet() != null) ps.setTimestamp(15, Timestamp.valueOf(giaoCa.getThoiGianDuyet()));

            else ps.setNull(15, Types.TIMESTAMP);

            return ps.executeUpdate() > 0;

        }

    }



    /**

     * Chốt kết ca và ghi nhận đối soát chênh lệch tiền mặt

     */

    public boolean chotKetCa(BangGiaoCa giaoCa) throws SQLException {

        String sql = "UPDATE BangGiaoCa SET tienCuoiCaHeThong=?, tienCuoiCaThucTe=?, tienChenhLech=?, tienThuTienMat=?, " +

                     "tienChuyenKhoan=?, tienHoanTra=?, lyDoChenhLech=?, gioKetThucThucTe=?, trangThai=? WHERE maGiaoCa=?";

        try (Connection conn = DBConnection.getConnection();

             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, giaoCa.getTienCuoiCaHeThong());

            ps.setBigDecimal(2, giaoCa.getTienCuoiCaThucTe());

            ps.setBigDecimal(3, giaoCa.getTienChenhLech());

            ps.setBigDecimal(4, giaoCa.getTienThuTienMat());

            ps.setBigDecimal(5, giaoCa.getTienChuyenKhoan());

            ps.setBigDecimal(6, giaoCa.getTienHoanTra());

            ps.setString(7, giaoCa.getLyDoChenhLech());

            ps.setTimestamp(8, Timestamp.valueOf(giaoCa.getGioKetThucThucTe() != null ? giaoCa.getGioKetThucThucTe() : LocalDateTime.now()));

            ps.setString(9, giaoCa.getTrangThai() != null ? giaoCa.getTrangThai() : "CHO_DUYET");

            ps.setString(10, giaoCa.getMaGiaoCa());

            return ps.executeUpdate() > 0;

        }

    }



    /**

     * Lấy toàn bộ danh sách biên bản bàn giao ca

     */

    public List<BangGiaoCa> getAllBangGiaoCa() throws SQLException {

        List<BangGiaoCa> list = new ArrayList<>();

        String sql = "SELECT bgc.*, nv.hoTen AS tenNhanVien, c.tenCa, ql.hoTen AS tenQuanLyDuyet " +

                     "FROM BangGiaoCa bgc " +

                     "LEFT JOIN NhanVien nv ON bgc.maNhanVien = nv.maNhanVien " +

                     "LEFT JOIN CaLamViec c ON bgc.maCa = c.maCa " +

                      "LEFT JOIN NhanVien ql ON bgc.maQuanLyDuyet = ql.maNhanVien " +

                     "ORDER BY bgc.gioKetThucThucTe DESC, bgc.maGiaoCa DESC";



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

                boSungThongTinK80(bgc, rs);

                list.add(bgc);

            }

        }

        return list;

    }



    /**

     * Lấy số tiền mặt thực tế của ca vừa bàn giao gần nhất để làm tiền đầu ca

     * kế tiếp. Chỉ các ca đã chốt (có giờ kết thúc) mới được sử dụng.

     *

     * @return số tiền bàn giao gần nhất; null nếu chưa từng có ca nào được chốt

     */

    public Long getTienThucTeCaBanGiaoGanNhat() throws SQLException {

        BangGiaoCa caGanNhat = getCaBanGiaoGanNhat();

        return caGanNhat != null && caGanNhat.getTienCuoiCaThucTe() != null

                ? caGanNhat.getTienCuoiCaThucTe().longValue()

                : null;

    }



    /**

     * Lấy ca đã chốt gần nhất để xác định cả số tiền bàn giao và mốc bắt đầu

     * tính doanh thu cho ca kế tiếp.

     */

    public BangGiaoCa getCaBanGiaoGanNhat() throws SQLException {

        String sql = "SELECT TOP 1 bgc.*, nv.hoTen AS tenNhanVien, c.tenCa, ql.hoTen AS tenQuanLyDuyet " +

                     "FROM BangGiaoCa " +

                     "bgc LEFT JOIN NhanVien nv ON bgc.maNhanVien = nv.maNhanVien " +

                     "LEFT JOIN CaLamViec c ON bgc.maCa = c.maCa " +

                      "LEFT JOIN NhanVien ql ON bgc.maQuanLyDuyet = ql.maNhanVien " +

                     "WHERE bgc.gioKetThucThucTe IS NOT NULL " +

                     "ORDER BY bgc.gioKetThucThucTe DESC, bgc.maGiaoCa DESC";



        try (Connection conn = DBConnection.getConnection();

             PreparedStatement ps = conn.prepareStatement(sql);

             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                NhanVien nv = new NhanVien();

                nv.setMaNhanVien(rs.getString("maNhanVien"));

                nv.setHoTen(rs.getString("tenNhanVien"));



                CaLamViec ca = new CaLamViec();

                ca.setMaCa(rs.getString("maCa"));

                ca.setTenCa(rs.getString("tenCa"));



                Timestamp kt = rs.getTimestamp("gioKetThucThucTe");

                BangGiaoCa bgc = new BangGiaoCa(

                        rs.getString("maGiaoCa"), nv, ca,

                        rs.getBigDecimal("tienDauCa"),

                        rs.getBigDecimal("tienCuoiCaHeThong"),

                        rs.getBigDecimal("tienCuoiCaThucTe"),

                        rs.getBigDecimal("tienChenhLech"),

                        rs.getString("lyDoChenhLech"),

                        kt != null ? kt.toLocalDateTime() : null

                );

                boSungThongTinK80(bgc, rs);

                return bgc;

            }

        }

        return null;

    }



    /** Tổng hóa đơn tiền mặt phát sinh sau lần bàn giao gần nhất. */

    public long tinhDoanhThuTienMatTuThoiDiem(LocalDateTime tuThoiDiem) {

        String sql = "SELECT ISNULL(SUM(thanhTien), 0) AS tongTienMat FROM HoaDon " +

                     "WHERE thoiGianThanhToan > ? " +

                     "AND (trangThaiHoaDon = N'Đã thanh toán' OR trangThaiHoaDon IS NULL) " +

                     "AND (phuongThucThanhToan LIKE N'%Tiền mặt%' OR phuongThucThanhToan IS NULL)";

        return tinhTongTheoThoiDiem(sql, "tongTienMat", tuThoiDiem, "doanh thu tiền mặt theo ca");

    }



    /** Tổng hóa đơn chuyển khoản phát sinh sau lần bàn giao gần nhất (chỉ tham khảo). */

    public long tinhDoanhThuChuyenKhoanTuThoiDiem(LocalDateTime tuThoiDiem) {

        String sql = "SELECT ISNULL(SUM(thanhTien), 0) AS tongCK FROM HoaDon " +

                     "WHERE thoiGianThanhToan > ? " +

                     "AND (trangThaiHoaDon = N'Đã thanh toán' OR trangThaiHoaDon IS NULL) " +

                     "AND phuongThucThanhToan IS NOT NULL " +

                     "AND phuongThucThanhToan NOT LIKE N'%Tiền mặt%'";

        return tinhTongTheoThoiDiem(sql, "tongCK", tuThoiDiem, "doanh thu chuyển khoản theo ca");

    }



    /** Tổng tiền đổi/trả phát sinh sau lần bàn giao gần nhất. */

    public long tinhTongTienHoanTraTuThoiDiem(LocalDateTime tuThoiDiem) {

        String sql = "SELECT ISNULL(SUM(tongTienHoan), 0) AS tongHoan FROM PhieuDoiTra " +

                     "WHERE ngayDoiTra > ?";

        return tinhTongTheoThoiDiem(sql, "tongHoan", tuThoiDiem, "tiền hoàn trả theo ca");

    }



    private long tinhTongTheoThoiDiem(String sql, String columnName, LocalDateTime tuThoiDiem, String logName) {

        LocalDateTime mocTinh = tuThoiDiem != null

                ? tuThoiDiem

                : java.time.LocalDate.now().atStartOfDay();

        try (Connection conn = DBConnection.getConnection();

             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(mocTinh));

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next() ? rs.getLong(columnName) : 0L;

            }

        } catch (Exception e) {

            System.err.println("[BangGiaoCaDAO] Lỗi tính " + logName + ": " + e.getMessage());

            return 0L;

        }

    }



    /**

     * Lấy ca làm việc đang mở (chưa kết ca) của một nhân viên

     */

    public BangGiaoCa getCaHienTai(String maNhanVien) throws SQLException {

        String sql = "SELECT TOP 1 bgc.*, nv.hoTen AS tenNhanVien, c.tenCa, ql.hoTen AS tenQuanLyDuyet " +

                     "FROM BangGiaoCa bgc " +

                     "LEFT JOIN NhanVien nv ON bgc.maNhanVien = nv.maNhanVien " +

                     "LEFT JOIN CaLamViec c ON bgc.maCa = c.maCa " +

                      "LEFT JOIN NhanVien ql ON bgc.maQuanLyDuyet = ql.maNhanVien " +

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



                    BangGiaoCa bgc = new BangGiaoCa(

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

                    boSungThongTinK80(bgc, rs);

                    return bgc;

                }

            }

        }

        return null;

    }



    /**

     * Tính tổng doanh thu Tiền mặt thực thu trong ngày hôm nay từ bảng HoaDon

     */

    public long tinhDoanhThuTienMatTrongNgay() {

        String sql = "SELECT ISNULL(SUM(thanhTien), 0) AS tongTienMat " +

                     "FROM HoaDon " +

                     "WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE) " +

                     "  AND (trangThaiHoaDon = N'Đã thanh toán' OR trangThaiHoaDon IS NULL) " +

                     "  AND (phuongThucThanhToan LIKE N'%Tiền mặt%' OR phuongThucThanhToan IS NULL)";

        try (Connection conn = DBConnection.getConnection();

             PreparedStatement ps = conn.prepareStatement(sql);

             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                return rs.getLong("tongTienMat");

            }

        } catch (Exception e) {

            System.err.println("[BangGiaoCaDAO] Lỗi tính doanh thu tiền mặt: " + e.getMessage());

        }

        return 0L;

    }



    /**

     * Tính tổng doanh thu Chuyển khoản / QR / VNPay / Thẻ trong ngày hôm nay từ bảng HoaDon

     */

    public long tinhDoanhThuChuyenKhoanTrongNgay() {

        String sql = "SELECT ISNULL(SUM(thanhTien), 0) AS tongCK " +

                     "FROM HoaDon " +

                     "WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE) " +

                     "  AND (trangThaiHoaDon = N'Đã thanh toán' OR trangThaiHoaDon IS NULL) " +

                     "  AND phuongThucThanhToan IS NOT NULL " +

                     "  AND phuongThucThanhToan NOT LIKE N'%Tiền mặt%'";

        try (Connection conn = DBConnection.getConnection();

             PreparedStatement ps = conn.prepareStatement(sql);

             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                return rs.getLong("tongCK");

            }

        } catch (Exception e) {

            System.err.println("[BangGiaoCaDAO] Lỗi tính doanh thu chuyển khoản: " + e.getMessage());

        }

        return 0L;

    }



    /**

     * Tính tổng tiền hoàn trả khách (Đổi / Trả hàng) trong ngày hôm nay từ bảng PhieuDoiTra

     */

    public long tinhTongTienHoanTraTrongNgay() {

        String sql = "SELECT ISNULL(SUM(tongTienHoan), 0) AS tongHoan " +

                     "FROM PhieuDoiTra " +

                     "WHERE CAST(ngayDoiTra AS DATE) = CAST(GETDATE() AS DATE)";

        try (Connection conn = DBConnection.getConnection();

             PreparedStatement ps = conn.prepareStatement(sql);

             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                return rs.getLong("tongHoan");

            }

        } catch (Exception e) {

            System.err.println("[BangGiaoCaDAO] Lỗi tính tiền hoàn trả: " + e.getMessage());

        }

        return 0L;

    }



    /**

     * Cập nhật trạng thái Quản lý duyệt đối soát ca vào cột lyDoChenhLech của BangGiaoCa

     */

    public boolean duyetGiaoCa(String maGiaoCa, String maQuanLy, LocalDateTime thoiGianDuyet) {

        String sql = "UPDATE BangGiaoCa SET trangThai='DA_DUYET', maQuanLyDuyet=?, thoiGianDuyet=? " +

                     "WHERE maGiaoCa=? AND trangThai='CHO_DUYET'";

        try (Connection conn = DBConnection.getConnection();

             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maQuanLy);

            ps.setTimestamp(2, Timestamp.valueOf(thoiGianDuyet != null ? thoiGianDuyet : LocalDateTime.now()));

            ps.setString(3, maGiaoCa);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {

            System.err.println("[BangGiaoCaDAO] Lỗi cập nhật duyệt ca: " + e.getMessage());

            return false;

        }

    }



    // tương thích ngược cho các unit test kiểm thử duyệt ca cũ

    public boolean capNhatGhiChuDuyetCa(String maGiaoCa, String ghiChuMoi) throws SQLException {

        duyetGiaoCa(maGiaoCa, "NV001", LocalDateTime.now());

        String sql = "UPDATE BangGiaoCa SET lyDoChenhLech = ? WHERE maGiaoCa = ?";

        try (Connection conn = DBConnection.getConnection();

             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ghiChuMoi);

            ps.setString(2, maGiaoCa);

            return ps.executeUpdate() > 0;

        }

    }



    /**

     * Lấy danh sách các biên bản giao ca đang ở trạng thái chờ Quản lý duyệt

     */

    public List<BangGiaoCa> getDanhSachCaChoDuyet() {

        List<BangGiaoCa> list = new ArrayList<>();

        String sql = "SELECT bgc.*, nv.hoTen AS tenNhanVien, c.tenCa, ql.hoTen AS tenQuanLyDuyet " +

                     "FROM BangGiaoCa bgc " +

                     "LEFT JOIN NhanVien nv ON bgc.maNhanVien = nv.maNhanVien " +

                     "LEFT JOIN CaLamViec c ON bgc.maCa = c.maCa " +

                      "LEFT JOIN NhanVien ql ON bgc.maQuanLyDuyet = ql.maNhanVien " +

                     "WHERE bgc.trangThai = 'CHO_DUYET' " +

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

                boSungThongTinK80(bgc, rs);

                list.add(bgc);

            }

        } catch (Exception e) {

            System.err.println("[BangGiaoCaDAO] Lỗi lấy danh sách ca chờ duyệt: " + e.getMessage());

        }

        return list;

    }



    /** Lấy đầy đủ dữ liệu lịch sử của một biên bản để xem/in lại K80. */

    public BangGiaoCa getBangGiaoCaTheoMa(String maGiaoCa) throws SQLException {

        String sql = "SELECT bgc.*, nv.hoTen AS tenNhanVien, c.tenCa, ql.hoTen AS tenQuanLyDuyet " +

                     "FROM BangGiaoCa bgc " +

                     "LEFT JOIN NhanVien nv ON bgc.maNhanVien = nv.maNhanVien " +

                     "LEFT JOIN CaLamViec c ON bgc.maCa = c.maCa " +

                     "LEFT JOIN NhanVien ql ON bgc.maQuanLyDuyet = ql.maNhanVien " +

                     "WHERE bgc.maGiaoCa = ?";

        try (Connection conn = DBConnection.getConnection();

             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maGiaoCa);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) return null;



                NhanVien nv = new NhanVien();

                nv.setMaNhanVien(rs.getString("maNhanVien"));

                nv.setHoTen(rs.getString("tenNhanVien"));

                CaLamViec ca = new CaLamViec();

                ca.setMaCa(rs.getString("maCa"));

                ca.setTenCa(rs.getString("tenCa"));

                Timestamp kt = rs.getTimestamp("gioKetThucThucTe");

                BangGiaoCa bgc = new BangGiaoCa(

                        rs.getString("maGiaoCa"), nv, ca,

                        rs.getBigDecimal("tienDauCa"), rs.getBigDecimal("tienCuoiCaHeThong"),

                        rs.getBigDecimal("tienCuoiCaThucTe"), rs.getBigDecimal("tienChenhLech"),

                        rs.getString("lyDoChenhLech"), kt != null ? kt.toLocalDateTime() : null);

                boSungThongTinK80(bgc, rs);

                return bgc;

            }

        }

    }



    private void boSungThongTinK80(BangGiaoCa bgc, ResultSet rs) throws SQLException {

        BigDecimal tienMat = rs.getBigDecimal("tienThuTienMat");

        BigDecimal chuyenKhoan = rs.getBigDecimal("tienChuyenKhoan");

        BigDecimal hoanTra = rs.getBigDecimal("tienHoanTra");

        bgc.setTienThuTienMat(tienMat != null ? tienMat : BigDecimal.ZERO);

        bgc.setTienChuyenKhoan(chuyenKhoan != null ? chuyenKhoan : BigDecimal.ZERO);

        bgc.setTienHoanTra(hoanTra != null ? hoanTra : BigDecimal.ZERO);

        bgc.setTrangThai(rs.getString("trangThai"));



        String maQuanLy = rs.getString("maQuanLyDuyet");

        if (maQuanLy != null && !maQuanLy.trim().isEmpty()) {

            NhanVien quanLy = new NhanVien();

            quanLy.setMaNhanVien(maQuanLy);

            quanLy.setHoTen(rs.getString("tenQuanLyDuyet"));

            bgc.setQuanLyDuyet(quanLy);

        }

        Timestamp thoiGianDuyet = rs.getTimestamp("thoiGianDuyet");

        bgc.setThoiGianDuyet(thoiGianDuyet != null ? thoiGianDuyet.toLocalDateTime() : null);

    }



    /**

     * Tự động phát sinh mã giao ca tiếp theo dạng GCxxx

     */

    public String phatSinhMaGiaoCa() {

        String sql = "SELECT TOP 1 maGiaoCa FROM BangGiaoCa WHERE maGiaoCa LIKE 'GC%' AND maGiaoCa NOT LIKE 'GC_TEST_%' ORDER BY LEN(maGiaoCa) DESC, maGiaoCa DESC";

        try (Connection conn = DBConnection.getConnection();

             PreparedStatement ps = conn.prepareStatement(sql);

             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                String ma = rs.getString("maGiaoCa");

                String numStr = ma.replaceAll("[^0-9]", "");

                if (!numStr.isEmpty()) {

                    int num = Integer.parseInt(numStr) + 1;

                    return String.format("GC%03d", num);

                }

            }

        } catch (Exception ignored) {}

        return "GC001";

    }

}

