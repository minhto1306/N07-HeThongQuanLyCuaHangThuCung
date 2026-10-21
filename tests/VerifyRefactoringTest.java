
import controllers.DangNhapController;
import controllers.ThuCungController;
import models.entity.LichHen;
import models.entity.NhanVien;
import utils.UserSession;
import views.MainDashboard;

import java.util.List;

public class VerifyRefactoringTest {

    public static void main(String[] args) {
        System.out.println("========== BẮT ĐẦU KIỂM THỬ TÁI CẤU TRÚC HỆ THỐNG ==========");

        DangNhapController dnController = new DangNhapController();

        // 1. Kiểm thử phân quyền 4 vai trò
        System.out.println("\n[1] KIỂM THỬ 4 VAI TRÒ ĐĂNG NHẬP:");

        // Test NVQL
        DangNhapController.KetQuaDangNhap kq1 = dnController.xuLyDangNhap("admin", "123456");
        assertCheck("Đăng nhập NVQL (admin)", kq1.isThanhCong() && kq1.getNhanVien() != null);
        assertCheck("Vai trò NVQL", UserSession.getCurrentRole() == UserSession.Role.NVQL);
        assertCheck("NVQL vào được Tổng quan", UserSession.canAccess(MainDashboard.CARD_TONG_QUAN));
        assertCheck("NVQL vào được Kho", UserSession.canAccess(MainDashboard.CARD_KHO));
        assertCheck("NVQL vào được POS", UserSession.canAccess(MainDashboard.CARD_POS));
        assertCheck("NVQL vào được Lịch hẹn", UserSession.canAccess(MainDashboard.CARD_LICH_HEN));

        // Test NVQL phân hệ Nhân sự
        assertCheck("NVQL vào được Quản lý Nhân sự & TK", UserSession.canAccess(MainDashboard.CARD_NHAN_VIEN));

        // Test NVBH
        DangNhapController.KetQuaDangNhap kq2 = dnController.xuLyDangNhap("banhang01", "123456");
        assertCheck("Đăng nhập NVBH (banhang01)", kq2.isThanhCong() && kq2.getNhanVien() != null);
        assertCheck("Vai trò NVBH", UserSession.getCurrentRole() == UserSession.Role.NVBH);
        assertCheck("NVBH vào được POS", UserSession.canAccess(MainDashboard.CARD_POS));
        assertCheck("NVBH BỊ CHẶN vào Kho", !UserSession.canAccess(MainDashboard.CARD_KHO));
        assertCheck("NVBH BỊ CHẶN vào Quản lý Nhân sự", !UserSession.canAccess(MainDashboard.CARD_NHAN_VIEN));
        assertCheck("NVBH BỊ CHẶN vào Tổng quan KPI", !UserSession.canAccess(MainDashboard.CARD_TONG_QUAN));

        // Test NVCS (chamsoc01)
        DangNhapController.KetQuaDangNhap kq3 = dnController.xuLyDangNhap("chamsoc01", "123456");
        assertCheck("Đăng nhập NVCS (chamsoc01)", kq3.isThanhCong() && kq3.getNhanVien() != null);
        assertCheck("Vai trò NVCS", UserSession.getCurrentRole() == UserSession.Role.NVCS);
        assertCheck("NVCS vào được Lịch hẹn", UserSession.canAccess(MainDashboard.CARD_LICH_HEN));
        assertCheck("NVCS vào được Thú cưng", UserSession.canAccess(MainDashboard.CARD_THU_CUNG));
        assertCheck("NVCS BỊ CHẶN vào POS", !UserSession.canAccess(MainDashboard.CARD_POS));
        assertCheck("NVCS BỊ CHẶN vào Kho", !UserSession.canAccess(MainDashboard.CARD_KHO));
        assertCheck("NVCS BỊ CHẶN vào Quản lý Nhân sự", !UserSession.canAccess(MainDashboard.CARD_NHAN_VIEN));
        assertCheck("NVCS BỊ CHẶN vào Tổng quan", !UserSession.canAccess(MainDashboard.CARD_TONG_QUAN));

        // Test NVTN
        DangNhapController.KetQuaDangNhap kq4 = dnController.xuLyDangNhap("thungan01", "123456");
        assertCheck("Đăng nhập NVTN (thungan01)", kq4.isThanhCong() && kq4.getNhanVien() != null);
        assertCheck("Vai trò NVTN", UserSession.getCurrentRole() == UserSession.Role.NVTN);
        assertCheck("NVTN vào được Giao ca", UserSession.canAccess(MainDashboard.CARD_GIAO_CA));
        assertCheck("NVTN vào được POS", UserSession.canAccess(MainDashboard.CARD_POS));
        assertCheck("NVTN BỊ CHẶN vào Lịch hẹn", !UserSession.canAccess(MainDashboard.CARD_LICH_HEN));
        assertCheck("NVTN BỊ CHẶN vào Kho", !UserSession.canAccess(MainDashboard.CARD_KHO));
        assertCheck("NVTN BỊ CHẶN vào Quản lý Nhân sự", !UserSession.canAccess(MainDashboard.CARD_NHAN_VIEN));
        assertCheck("NVTN BỊ CHẶN vào Tổng quan", !UserSession.canAccess(MainDashboard.CARD_TONG_QUAN));

        // 2. Kiểm thử nghiệp vụ Lịch hẹn & Dịch vụ (Pet Care thuần túy, không có Bác sĩ thú y)
        System.out.println("\n[2] KIỂM THỬ QUẢN LÝ LỊCH HẸN & DỊCH VỤ (PET CARE & SPA):");
        ThuCungController tcController = new ThuCungController();
        List<LichHen> dsLichHen = tcController.layDanhSachLichHen();
        assertCheck("Lấy danh sách lịch hẹn thành công (" + dsLichHen.size() + " lịch)", !dsLichHen.isEmpty());

        for (LichHen lh : dsLichHen) {
            for (models.entity.ChiTietLichHen ct : lh.getDsChiTiet()) {
                String tenDV = ct.getDichVu() != null ? ct.getDichVu().getTenDichVu() : "";
                boolean hasBacSi = tenDV.contains("Bác sĩ") || tenDV.contains("Khám bệnh");
                assertCheck("Dịch vụ [" + tenDV + "] không phải bác sĩ/khám bệnh", !hasBacSi);
            }
        }

        String testMaLH = dsLichHen.get(0).getMaLichHen();
        boolean capNhatOk = tcController.capNhatTrangThaiLichHen(testMaLH, "Đang phục vụ");
        assertCheck("Cập nhật trạng thái lịch hẹn [" + testMaLH + "] sang 'Đang phục vụ'", capNhatOk);

        boolean hoanThanhOk = tcController.capNhatTrangThaiLichHen(testMaLH, "Hoàn thành");
        assertCheck("Cập nhật trạng thái lịch hẹn [" + testMaLH + "] sang 'Hoàn thành'", hoanThanhOk);

        // 3. Kiểm thử phân hệ Quản lý Nhân viên & KTV điều phối
        System.out.println("\n[3] KIỂM THỬ PHÂN HỆ QUẢN LÝ NHÂN VIÊN (NhanVienController):");
        controllers.NhanVienController nvController = new controllers.NhanVienController();
        List<NhanVien> dsNV = nvController.layDanhSachNhanVien();
        assertCheck("Lấy danh sách nhân viên thành công (" + dsNV.size() + " nhân viên)", !dsNV.isEmpty());

        List<NhanVien> dsKtv = nvController.layDanhSachKtvHoatDong();
        assertCheck("Lấy danh sách KTV/NVCS đang hoạt động (" + dsKtv.size() + " KTV)", !dsKtv.isEmpty());
        for (NhanVien ktv : dsKtv) {
            assertCheck("KTV có vai trò NVCS/Chăm sóc: " + ktv.getHoTen(),
                    UserSession.Role.fromString(ktv.getTaiKhoan() != null ? ktv.getTaiKhoan().getVaiTro() : ktv.getChucVu()) == UserSession.Role.NVCS);
        }

        String testUser = dsNV.get(0).getTaiKhoan() != null ? dsNV.get(0).getTaiKhoan().getTenDangNhap() : "admin";
        boolean doiVaiTroOk = nvController.doiVaiTro(testUser, "NVQL");
        assertCheck("Đổi vai trò nhân viên thành công", doiVaiTroOk);

        boolean resetPassOk = nvController.resetMatKhau(testUser, "123456");
        assertCheck("Cấp lại mật khẩu nhân viên thành công", resetPassOk);

        // 4. Kiểm thử Vector Icon Paw & Graceful Offline Mode
        System.out.println("\n[4] KIỂM THỬ VECTOR ICON & CHẾ ĐỘ OFFLINE:");
        javax.swing.Icon pawIcon = utils.IconHelper.getVectorIcon(utils.IconHelper.PAW, 68, java.awt.Color.WHITE);
        assertCheck("Vector Paw Icon tạo thành công (size 68)", pawIcon != null && pawIcon.getIconWidth() == 68);

        boolean offlineChecked = utils.DBConnection.isOfflineMode();
        System.out.println("  [INFO] Trạng thái CSDL hiện tại: " + (offlineChecked ? "Offline (Mock Data)" : "Online (SQL Server)"));
        assertCheck("Kiểm tra DBConnection an toàn không crash", true);

        System.out.println("\n========== TẤT CẢ KIỂM THỬ ĐÃ VƯỢT QUA 100% THÀNH CÔNG! ==========");
    }

    private static void assertCheck(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName);
            System.exit(1);
        }
    }
}
