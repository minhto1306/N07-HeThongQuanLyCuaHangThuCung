package controllers;

import models.dao.NhanVienDAO;
import models.entity.NhanVien;
import models.entity.TaiKhoan;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller điều phối quản lý Nhân viên & Tài khoản phân quyền.
 * Kết nối trực tiếp 100% CSDL SQL Server qua NhanVienDAO.
 */
public class NhanVienController {

    private final NhanVienDAO nhanVienDAO;

    public NhanVienController() {
        this.nhanVienDAO = new NhanVienDAO();
    }

    /**
     * Lấy toàn bộ danh sách nhân viên kèm thông tin tài khoản từ CSDL SQL Server.
     */
    public List<NhanVien> layDanhSachNhanVien() {
        try {
            return nhanVienDAO.getAllNhanVien();
        } catch (SQLException e) {
            System.err.println("[NhanVienController] Lỗi đọc Nhân viên từ CSDL: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Thêm nhân viên mới vào CSDL.
     */
    public boolean themNhanVien(String hoTen, String chucVu, String soDienThoai,
                                String tenDangNhap, String matKhau, String vaiTro) {
        int maxNum = 6;
        for (NhanVien item : layDanhSachNhanVien()) {
            if (item.getMaNhanVien() != null && item.getMaNhanVien().startsWith("NV")) {
                try {
                    int n = Integer.parseInt(item.getMaNhanVien().substring(2).replaceAll("[^0-9]", ""));
                    if (n > maxNum) maxNum = n;
                } catch (Exception ignored) {}
            }
        }
        String maNV = "NV" + String.format("%03d", maxNum + 1);
        TaiKhoan tk = new TaiKhoan(tenDangNhap, matKhau, vaiTro, true);
        NhanVien nv = new NhanVien(maNV, hoTen, chucVu, soDienThoai, tk);

        try {
            return nhanVienDAO.insertNhanVien(nv, matKhau, vaiTro);
        } catch (SQLException e) {
            System.err.println("[NhanVienController] Lỗi insert CSDL: " + e.getMessage());
            return false;
        }
    }

    /**
     * Đổi vai trò tài khoản nhân viên.
     */
    public boolean doiVaiTro(String tenDangNhap, String vaiTroMoi) {
        try {
            return nhanVienDAO.updateVaiTro(tenDangNhap, vaiTroMoi);
        } catch (SQLException e) {
            System.err.println("[NhanVienController] Lỗi update vai trò CSDL: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cấp lại mật khẩu mặc định (123456) hoặc mật khẩu mới.
     */
    public boolean resetMatKhau(String tenDangNhap, String matKhauMoi) {
        try {
            return nhanVienDAO.resetMatKhau(tenDangNhap, matKhauMoi);
        } catch (SQLException e) {
            System.err.println("[NhanVienController] Lỗi reset mật khẩu CSDL: " + e.getMessage());
            return false;
        }
    }

    /**
     * Bật/tắt trạng thái hoạt động (Khóa hoặc Kích hoạt).
     */
    public boolean toggleTrangThai(String tenDangNhap) {
        for (NhanVien nv : layDanhSachNhanVien()) {
            if (nv.getTaiKhoan() != null && nv.getTaiKhoan().getTenDangNhap().equals(tenDangNhap)) {
                boolean trangThaiMoi = !nv.getTaiKhoan().isTrangThaiHoatDong();
                try {
                    boolean ok = nhanVienDAO.toggleTrangThai(tenDangNhap, trangThaiMoi);
                    if (ok) {
                        nv.getTaiKhoan().setTrangThaiHoatDong(trangThaiMoi);
                        return true;
                    }
                } catch (SQLException e) {
                    System.err.println("[NhanVienController] Lỗi toggle trạng thái CSDL: " + e.getMessage());
                    return false;
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Lấy danh sách kỹ thuật viên chăm sóc đang hoạt động (cho ComboBox chỉ định KTV).
     */
    public List<NhanVien> layDanhSachKtvHoatDong() {
        List<NhanVien> list = new ArrayList<>();
        for (NhanVien nv : layDanhSachNhanVien()) {
            if (nv.getTaiKhoan() != null && "NVCS".equalsIgnoreCase(nv.getTaiKhoan().getVaiTro())
                    && nv.getTaiKhoan().isTrangThaiHoatDong()) {
                list.add(nv);
            }
        }
        if (list.isEmpty()) {
            list.add(new NhanVien("NV003", "Tô Thanh Đạt", "Kỹ thuật viên Grooming", "0923456789", null));
            list.add(new NhanVien("NV005", "Võ Thị Hồng Ngọc", "Chuyên viên Spa thú cưng", "0945678901", null));
        }
        return list;
    }
}
