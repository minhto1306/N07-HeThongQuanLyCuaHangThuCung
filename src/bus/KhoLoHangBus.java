package bus;

import dao.LoHangDAO;
import dao.SanPhamDAO;
import entity.LoHang;
import entity.QuyDoiDonVi;
import entity.SanPham;
import entity.DanhMucHang;
import entity.DonViTinh;
import entity.NhaCungCap;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller điều phối toàn bộ nghiệp vụ Quản lý Kho, Lô hàng FEFO, Điểm đặt hàng ROP và Quy đổi đơn vị.
 * Tách biệt hoàn toàn tầng GUI khỏi SanPhamDAO và LoHangDAO.
 */
public class KhoLoHangBus {

    private final LoHangDAO loHangDAO;
    private final SanPhamDAO sanPhamDAO;

    public KhoLoHangBus() {
        this.loHangDAO = new LoHangDAO();
        this.sanPhamDAO = new SanPhamDAO();
    }

    /**
     * Lấy toàn bộ danh sách sản phẩm trong hệ thống.
     */
    public List<SanPham> layDanhSachSanPham() {
        try {
            return sanPhamDAO.getAllSanPham();
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Lỗi layDanhSachSanPham: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<DanhMucHang> layDanhSachDanhMuc() {
        try {
            return sanPhamDAO.getAllDanhMuc();
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Lỗi lấy danh mục: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<DonViTinh> layDanhSachDonViTinh() {
        try {
            return sanPhamDAO.getAllDonViTinh();
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Lỗi lấy đơn vị tính: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<NhaCungCap> layDanhSachNhaCungCap() {
        try {
            return loHangDAO.getAllNhaCungCap();
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Lỗi lấy nhà cung cấp: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Lấy danh sách quy đổi đơn vị theo mã sản phẩm.
     */
    public List<QuyDoiDonVi> layQuyDoiBySanPham(String maSP) {
        try {
            return sanPhamDAO.getQuyDoiBySanPham(maSP);
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Lỗi layQuyDoiBySanPham: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Lấy sản phẩm theo mã.
     */
    public SanPham laySanPhamById(String maSP) {
        try {
            return sanPhamDAO.getSanPhamById(maSP);
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Lỗi laySanPhamById: " + e.getMessage());
            return null;
        }
    }

    /**
     * Thêm sản phẩm mới kèm đơn vị quy đổi cơ sở.
     */
    public boolean themSanPham(SanPham sp) {
        if (!utils.UserSession.isQuanLy()) {
            throw new SecurityException("Chỉ Quản lý (NVQL) mới có quyền thêm sản phẩm mới!");
        }
        try {
            return sanPhamDAO.insertSanPham(sp);
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Lỗi themSanPham: " + e.getMessage());
            return false;
        }
    }

    /**
     * Thêm đơn vị tính mới vào CSDL.
     */
    public boolean themDonViTinh(entity.DonViTinh dvt) {
        if (!utils.UserSession.isQuanLy()) {
            throw new SecurityException("Chỉ Quản lý (NVQL) mới có quyền thêm đơn vị tính!");
        }
        try {
            return sanPhamDAO.insertDonViTinh(dvt);
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Lỗi themDonViTinh: " + e.getMessage());
            return false;
        }
    }

    /**
     * Thêm đơn vị quy đổi mới cho sản phẩm.
     */
    public boolean themQuyDoi(QuyDoiDonVi qd) {
        if (!utils.UserSession.isQuanLy()) {
            throw new SecurityException("Chỉ Quản lý (NVQL) mới có quyền thêm quy đổi đơn vị!");
        }
        try {
            return sanPhamDAO.insertQuyDoi(qd);
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Lỗi themQuyDoi: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa đơn vị quy đổi theo mã.
     */
    public boolean xoaQuyDoi(String maQuyDoi) {
        if (!utils.UserSession.isQuanLy()) {
            throw new SecurityException("Chỉ Quản lý (NVQL) mới có quyền xóa quy đổi đơn vị!");
        }
        try {
            return sanPhamDAO.deleteQuyDoi(maQuyDoi);
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Lỗi xoaQuyDoi: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy toàn bộ danh sách lô hàng trong kho.
     */
    public List<LoHang> layDanhSachLoHang() {
        try {
            return loHangDAO.getAllLoHang();
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Lỗi layDanhSachLoHang: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Trừ tồn kho lô hàng theo mã số lô (xuất kho bán lẻ FEFO).
     */
    public boolean truTonKho(String soLo, int soLuong) {
        try {
            return loHangDAO.truTonKho(soLo, soLuong);
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Lỗi truTonKho: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cộng trả tồn kho lô hàng theo mã số lô (đổi trả hoàn hàng vào kho).
     */
    public boolean congTonKho(String soLo, int soLuong) {
        try {
            return loHangDAO.congTonKho(soLo, soLuong);
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Lỗi congTonKho: " + e.getMessage());
            return false;
        }
    }

    /**
     * Nhập lô hàng mới vào kho.
     */
    public boolean nhapLoHang(LoHang lh) {
        if (!utils.UserSession.isQuanLy()) {
            throw new SecurityException("Chỉ Quản lý (NVQL) mới có quyền nhập lô hàng mới!");
        }
        try {
            return loHangDAO.insertLoHang(lh);
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Lỗi nhapLoHang: " + e.getMessage());
            return false;
        }
    }

    public boolean nhapSanPhamVaLoHang(SanPham sp, QuyDoiDonVi qd, LoHang lo) {
        if (!utils.UserSession.isQuanLy()) {
            throw new SecurityException("Chỉ Quản lý (NVQL) mới có quyền thực hiện nghiệp vụ nhập kho!");
        }
        try {
            return new dao.NhapKhoDAO().nhapSanPhamVaLoHang(sp, qd, lo);
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Transaction nhập kho thất bại: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật số lượng tồn kho thực tế khi kiểm kê.
     */
    public boolean capNhatSoLuongTon(String soLo, int soLuongMoi) {
        if (!utils.UserSession.isQuanLy()) {
            throw new SecurityException("Chỉ Quản lý (NVQL) mới có quyền điều chỉnh số lượng tồn kiểm kê!");
        }
        try {
            return loHangDAO.capNhatSoLuongTon(soLo, soLuongMoi);
        } catch (SQLException e) {
            System.err.println("[KhoLoHangBus] Lỗi capNhatSoLuongTon: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy danh sách các lô hàng cảnh báo hạn dùng (hết hạn hoặc còn <= 60 ngày).
     */
    public List<LoHang> layDanhSachLoCanhBaoHanDung() {
        List<LoHang> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (LoHang lh : layDanhSachLoHang()) {
            if (lh.getHanSuDung() != null && lh.getSoLuongTon() > 0) {
                long days = ChronoUnit.DAYS.between(today, lh.getHanSuDung());
                if (days <= 60) {
                    result.add(lh);
                }
            }
        }
        return result;
    }

    /**
     * Đếm tổng số lượng lô hàng cảnh báo hạn sử dụng (hạn dùng <= 60 ngày).
     */
    public int demSoLoCanhBaoHanDung() {
        return layDanhSachLoCanhBaoHanDung().size();
    }
}
