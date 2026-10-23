package controllers;

import models.dao.NhanVienDAO;
import models.entity.NhanVien;
import models.entity.TaiKhoan;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller điều phối quản lý Nhân viên & Tài khoản phân quyền.
 * Kết nối trực tiếp CSDL qua NhanVienDAO kèm cơ chế Fallback mượt mà.
 */
public class NhanVienController {

    private final NhanVienDAO nhanVienDAO;
    private final List<NhanVien> mockNhanVienList = new ArrayList<>();

    public NhanVienController() {
        this.nhanVienDAO = new NhanVienDAO();
        khoiTaoDuLieuMau();
    }

    private void khoiTaoDuLieuMau() {
        TaiKhoan tk1 = new TaiKhoan("admin", "123456", "NVQL", true);
        NhanVien nv1 = new NhanVien("NV001", "Lê Khãi Minh", "Quản lý hệ thống", "0901234567", tk1);

        TaiKhoan tk2 = new TaiKhoan("thungan01", "123456", "NVTN", true);
        NhanVien nv2 = new NhanVien("NV002", "Lê Anh Minh", "Nhân viên thu ngân", "0912345678", tk2);

        TaiKhoan tk3 = new TaiKhoan("chamsoc01", "123456", "NVCS", true);
        NhanVien nv3 = new NhanVien("NV003", "Tô Thanh Đạt", "Nhân viên chăm sóc thú cưng", "0923456789", tk3);

        TaiKhoan tk4 = new TaiKhoan("banhang01", "123456", "NVBH", true);
        NhanVien nv4 = new NhanVien("NV004", "Trần Thị Thu Hà", "Nhân viên bán hàng", "0934567890", tk4);

        TaiKhoan tk5 = new TaiKhoan("ktv02", "123456", "NVCS", true);
        NhanVien nv5 = new NhanVien("NV005", "Võ Thị Hồng Ngọc", "Chuyên viên Spa thú cưng", "0945678901", tk5);

        TaiKhoan tk6 = new TaiKhoan("banhang02", "123456", "NVBH", false);
        NhanVien nv6 = new NhanVien("NV006", "Lê Anh Tuấn", "Nhân viên bán hàng", "0956789012", tk6);

        mockNhanVienList.add(nv1);
        mockNhanVienList.add(nv2);
        mockNhanVienList.add(nv3);
        mockNhanVienList.add(nv4);
        mockNhanVienList.add(nv5);
        mockNhanVienList.add(nv6);
    }

    /**
     * Lấy toàn bộ danh sách nhân viên kèm thông tin tài khoản từ CSDL.
     */
    public List<NhanVien> layDanhSachNhanVien() {
        try {
            List<NhanVien> dbList = nhanVienDAO.getAllNhanVien();
            if (dbList != null && !dbList.isEmpty()) {
                return dbList;
            }
        } catch (SQLException e) {
            System.err.println("[NhanVienController] CSDL offline, sử dụng danh sách bộ nhớ tạm: " + e.getMessage());
        }
        return mockNhanVienList;
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
            boolean ok = nhanVienDAO.insertNhanVien(nv, matKhau, vaiTro);
            if (ok) {
                mockNhanVienList.add(nv);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[NhanVienController] Lỗi insert CSDL: " + e.getMessage());
            return false;
        }

        return false;
    }

    /**
     * Đổi vai trò tài khoản nhân viên.
     */
    public boolean doiVaiTro(String tenDangNhap, String vaiTroMoi) {
        try {
            boolean ok = nhanVienDAO.updateVaiTro(tenDangNhap, vaiTroMoi);
            if (ok) {
                capNhatVaiTroMock(tenDangNhap, vaiTroMoi);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[NhanVienController] Lỗi update vai trò CSDL: " + e.getMessage());
            return false;
        }

        return false;
    }

    private boolean capNhatVaiTroMock(String tenDangNhap, String vaiTroMoi) {
        for (NhanVien nv : mockNhanVienList) {
            if (nv.getTaiKhoan() != null && nv.getTaiKhoan().getTenDangNhap().equals(tenDangNhap)) {
                nv.getTaiKhoan().setVaiTro(vaiTroMoi);
                return true;
            }
        }
        return false;
    }

    /**
     * Cấp lại mật khẩu mặc định (123456) hoặc mật khẩu mới.
     */
    public boolean resetMatKhau(String tenDangNhap, String matKhauMoi) {
        try {
            boolean ok = nhanVienDAO.resetMatKhau(tenDangNhap, matKhauMoi);
            if (ok) {
                capNhatMatKhauMock(tenDangNhap, matKhauMoi);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[NhanVienController] Lỗi reset mật khẩu CSDL: " + e.getMessage());
            return false;
        }

        return false;
    }

    private boolean capNhatMatKhauMock(String tenDangNhap, String matKhauMoi) {
        for (NhanVien nv : mockNhanVienList) {
            if (nv.getTaiKhoan() != null && nv.getTaiKhoan().getTenDangNhap().equals(tenDangNhap)) {
                nv.getTaiKhoan().setMatKhau(matKhauMoi);
                return true;
            }
        }
        return false;
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
