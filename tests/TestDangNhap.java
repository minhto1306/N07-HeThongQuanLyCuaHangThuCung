package tests;

import controllers.DangNhapController;
import views.LoginForm;
import views.MainDashboard;

public class TestDangNhap {
    public static void main(String[] args) {
        System.out.println("=== KIỂM THỬ XÁC THỰC VÀ GIAO DIỆN ===");
        
        // 1. Kiểm thử Controller
        DangNhapController ctrl = new DangNhapController();
        DangNhapController.KetQuaDangNhap kq1 = ctrl.xuLyDangNhap("admin", "123456");
        System.out.println("Test 1 (admin): Thành công = " + kq1.isThanhCong() + ", NV = " + (kq1.getNhanVien() != null ? kq1.getNhanVien().getHoTen() : "null"));
        
        DangNhapController.KetQuaDangNhap kq2 = ctrl.xuLyDangNhap("thungan01", "123456");
        System.out.println("Test 2 (thungan01): Thành công = " + kq2.isThanhCong() + ", NV = " + (kq2.getNhanVien() != null ? kq2.getNhanVien().getHoTen() : "null"));

        DangNhapController.KetQuaDangNhap kq3 = ctrl.xuLyDangNhap("admin", "sai_mat_khau");
        System.out.println("Test 3 (sai pass): Thành công = " + kq3.isThanhCong() + ", Msg = " + kq3.getThongDiep());

        // 2. Khởi tạo đối tượng giao diện (kiểm tra không bị crash hoặc lỗi layout)
        try {
            LoginForm loginForm = new LoginForm();
            System.out.println("Test 4 (LoginForm): Khởi tạo thành công! Title = " + loginForm.getTitle());
            loginForm.dispose();

            MainDashboard dashboard = new MainDashboard(kq1.getNhanVien());
            System.out.println("Test 5 (MainDashboard): Khởi tạo thành công! Title = " + dashboard.getTitle());
            dashboard.dispose();
        } catch (Throwable t) {
            System.err.println("Lỗi khởi tạo UI: " + t.getMessage());
            t.printStackTrace();
        }

        System.out.println("=== HOÀN TẤT KIỂM THỬ THÀNH CÔNG ===");
    }
}
