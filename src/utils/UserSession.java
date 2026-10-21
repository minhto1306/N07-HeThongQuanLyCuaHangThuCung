package utils;

import models.entity.NhanVien;
import models.entity.TaiKhoan;

/**
 * Quản lý phiên làm việc tập trung (User Session) cho toàn bộ ứng dụng.
 * Hỗ trợ phân quyền truy cập nghiêm ngặt theo 4 vai trò nghiệp vụ:
 * 1. NVQL (Nhân viên Quản lý / Quản trị hệ thống)
 * 2. NVBH (Nhân viên Bán hàng tại quầy)
 * 3. NVCS (Nhân viên Chăm sóc thú cưng / Kỹ thuật viên)
 * 4. NVTN (Nhân viên Thu ngân)
 */
public final class UserSession {

    public enum Role {
        NVQL("NVQL", "Quản lý hệ thống"),
        NVBH("NVBH", "Nhân viên Bán hàng"),
        NVCS("NVCS", "Nhân viên Chăm sóc Thú cưng"),
        NVTN("NVTN", "Nhân viên Thu ngân");

        private final String code;
        private final String description;

        Role(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        /**
         * Chuyển đổi linh hoạt từ chuỗi vai trò lưu trong DB / tài khoản sang Enum Role.
         */
        public static Role fromString(String roleStr) {
            if (roleStr == null) return NVTN;
            String normalized = roleStr.trim().toUpperCase();

            if (normalized.contains("QL") || normalized.contains("ADMIN") || normalized.contains("QUANLY")) {
                return NVQL;
            } else if (normalized.contains("BH") || normalized.contains("BANHANG")) {
                return NVBH;
            } else if (normalized.contains("CS") || normalized.contains("KTV") || normalized.contains("KYTHUAT") || normalized.contains("CHAMSOC")) {
                return NVCS;
            } else if (normalized.contains("TN") || normalized.contains("THUNGAN")) {
                return NVTN;
            }
            return NVTN;
        }
    }

    private static NhanVien currentUser = null;
    private static Role currentRole = Role.NVQL;

    private UserSession() {}

    /**
     * Khởi tạo phiên làm việc khi đăng nhập thành công.
     */
    public static void initSession(NhanVien nhanVien) {
        currentUser = nhanVien;
        if (nhanVien != null && nhanVien.getTaiKhoan() != null) {
            currentRole = Role.fromString(nhanVien.getTaiKhoan().getVaiTro());
        } else if (nhanVien != null) {
            currentRole = Role.fromString(nhanVien.getChucVu());
        } else {
            currentRole = Role.NVQL;
        }
    }

    public static NhanVien getCurrentUser() {
        return currentUser;
    }

    public static Role getCurrentRole() {
        return currentRole;
    }

    public static String getUserId() {
        return currentUser != null ? currentUser.getMaNhanVien() : "NV_ANONYMOUS";
    }

    public static String getFullName() {
        return currentUser != null ? currentUser.getHoTen() : "Chưa đăng nhập";
    }

    public static String getRoleCode() {
        return currentRole != null ? currentRole.getCode() : "GUEST";
    }

    public static String getRoleDescription() {
        return currentRole != null ? currentRole.getDescription() : "Khách";
    }

    public static boolean isQuanLy() {
        return currentRole == Role.NVQL;
    }

    public static boolean isBanHang() {
        return currentRole == Role.NVBH;
    }

    public static boolean isChamSoc() {
        return currentRole == Role.NVCS;
    }

    public static boolean isThuNgan() {
        return currentRole == Role.NVTN;
    }

    /**
     * Kiểm tra thẩm quyền truy cập thẻ chức năng (Card Name) trong MainDashboard theo ma trận RBAC.
     */
    public static boolean canAccess(String cardName) {
        if (isQuanLy()) {
            return true; // Quản lý (NVQL) có toàn quyền truy cập tất cả phân hệ
        }

        switch (cardName) {
            case "CARD_POS":
                // Bán hàng (NVBH) và Thu ngân (NVTN) được phép vào POS
                return isBanHang() || isThuNgan();

            case "CARD_THU_CUNG":
                // Chăm sóc (NVCS), Bán hàng (NVBH) và Thu ngân (NVTN) đều cần tra cứu thông tin thú cưng/khách hàng
                return isChamSoc() || isBanHang() || isThuNgan();

            case "CARD_LICH_HEN":
                // Chăm sóc (NVCS), Thu ngân (NVTN) và Bán hàng (NVBH) được vào lịch hẹn
                return isChamSoc() || isThuNgan() || isBanHang();

            case "CARD_PHAN_CA":
                // Màn hình phân ca làm việc chỉ dành riêng cho Quản lý hệ thống (NVQL)
                return false;

            case "CARD_KHO":
                // Bán hàng và Thu ngân được xem tồn kho tra cứu giá bán
                return isBanHang() || isThuNgan();

            case "CARD_GIAO_CA":
                // Thu ngân thực hiện kết két bàn giao ca
                return isThuNgan();

            case "CARD_TONG_QUAN":
                // Dashboard điều hành chỉ dành riêng cho NVQL
                return false;

            case "CARD_PET_HOTEL":
                // Quản lý (NVQL), Thu ngân (NVTN) và Chăm sóc (NVCS) đều có quyền vào Pet Hotel
                return isQuanLy() || isThuNgan() || isChamSoc();

            case "CARD_DOI_TRA":
                // Quản lý (NVQL), Bán hàng (NVBH) và Thu ngân (NVTN) đều có quyền xử lý đổi trả hàng
                return isQuanLy() || isBanHang() || isThuNgan();

            case "CARD_NHAN_VIEN":
                // Phân quyền nhân sự và tài khoản chỉ dành riêng cho NVQL
                return false;

            default:
                return false;
        }
    }

    public static boolean canAccessPetHotel() {
        return canAccess("CARD_PET_HOTEL");
    }

    public static boolean canAccessDoiTraHang() {
        return canAccess("CARD_DOI_TRA");
    }

    // ==========================================
    // KIỂM TRA QUYỀN THEO 6 NHÓM CHỨC NĂNG SLIDE 17
    // ==========================================
    public static boolean canAccessNhom1HeThongPhanQuyen() {
        return isQuanLy();
    }

    public static boolean canAccessNhom1GiaoCa() {
        return isQuanLy() || isThuNgan();
    }

    public static boolean canAccessNhom2DanhMucHangHoa() {
        return isQuanLy() || isBanHang() || isThuNgan();
    }

    public static boolean canAccessNhom2DanhMucNhaCungCap() {
        return isQuanLy();
    }

    public static boolean canAccessNhom3CapNhatNhapKho() {
        return isQuanLy();
    }

    public static boolean canAccessNhom3CapNhatTiepNhanLich() {
        return isQuanLy() || isBanHang() || isThuNgan();
    }

    public static boolean canAccessNhom3CapNhatSucKhoeBe() {
        return isQuanLy() || isChamSoc();
    }

    public static boolean canAccessNhom3CapNhatNhanSu() {
        return isQuanLy();
    }

    public static boolean canAccessNhom4XuLyBanHangPOS() {
        return isQuanLy() || isBanHang() || isThuNgan();
    }

    public static boolean canAccessNhom4XuLyPhanCaToanQuyen() {
        return isQuanLy();
    }

    public static boolean canAccessNhom4XuLyTienDoDichVu() {
        return isQuanLy() || isChamSoc();
    }

    public static boolean canAccessNhom4XuLyDoiTraHang() {
        return isQuanLy() || isBanHang() || isThuNgan();
    }

    public static boolean canAccessNhom5TimKiemKhachVIP() {
        return isQuanLy() || isBanHang() || isThuNgan();
    }

    public static boolean canAccessNhom5TimKiemLichHen() {
        return isQuanLy() || isBanHang() || isThuNgan() || isChamSoc();
    }

    public static boolean canAccessNhom5TimKiemTonKhoFEFO() {
        return isQuanLy() || isBanHang() || isThuNgan();
    }

    public static boolean canAccessNhom5TimKiemHoaDonCu() {
        return isQuanLy() || isBanHang() || isThuNgan();
    }

    public static boolean canAccessNhom6BaoBieuDoanhThu() {
        return isQuanLy() || isThuNgan();
    }

    public static boolean canAccessNhom6BaoBieuThongKeDichVu() {
        return isQuanLy() || isChamSoc();
    }

    public static boolean canAccessNhom6BaoBieuCanhBaoFEFO() {
        return isQuanLy();
    }

    public static boolean canAccessNhom6BaoBieuXuatNhapTon() {
        return isQuanLy();
    }

    /**
     * Hủy phiên làm việc khi đăng xuất.
     */
    public static void clearSession() {
        currentUser = null;
        currentRole = Role.NVQL;
    }
}
