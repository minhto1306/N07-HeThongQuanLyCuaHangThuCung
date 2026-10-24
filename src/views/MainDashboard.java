package views;

import controllers.DangNhapController;
import models.entity.NhanVien;
import models.entity.TaiKhoan;
import utils.IconHelper;
import utils.ThemeColors;
import utils.UserSession;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Giao diện làm việc chính (Main Dashboard) của Hệ thống Quản lý Cửa hàng Thú cưng.
 * Áp dụng kiến trúc điều hướng kép (Dual Navigation) bám sát 100% Slide 17 IUH:
 * - Hệ thống Menu 6 nhóm chức năng chuẩn Slide 17: Hệ thống, Danh mục, Cập nhật, Xử lý, Tìm kiếm, Báo biểu.
 * - Thanh thực đơn JMenuBar trên đỉnh với phím tắt Alt+1 đến Alt+6 và phân quyền RBAC chặt chẽ.
 * - Thanh bên Sectional Sidebar phân nhóm 6 cụm chức năng có Section Header rõ ràng, hỗ trợ cuộn mượt mà.
 * - Tông màu Warm Peach / Soft Apricot / Nude sang trọng, FlatLaf hiện đại, khử răng cưa toàn diện.
 */
public class MainDashboard extends JFrame {

    private final NhanVien currentUser;
    private JPanel pnlContentCards;
    private CardLayout cardLayout;

    private final List<JButton> menuButtons = new ArrayList<>();
    private final List<String> menuIconNames = new ArrayList<>();
    private final List<String> menuCardNames = new ArrayList<>();

    // Các màn hình con nghiệp vụ
    private TongQuanPanel tongQuanPanel;
    private BanHangPOSPanel banHangPOSPanel;
    private QuanLyLichHenPanel quanLyLichHenPanel;
    private ThuCungDichVuPanel thuCungDichVuPanel;
    private KhoLoHangPanel khoLoHangPanel;
    private QuanLyNhanVienPanel quanLyNhanVienPanel;
    private BanGiaoCaPanel banGiaoCaPanel;
    private LichPhanCaPanel lichPhanCaPanel;
    private PetHotelPanel petHotelPanel;
    private DoiTraHangPanel doiTraHangPanel;

    // Tên các thẻ Card
    public static final String CARD_TONG_QUAN = "CARD_TONG_QUAN";
    public static final String CARD_POS = "CARD_POS";
    public static final String CARD_LICH_HEN = "CARD_LICH_HEN";
    public static final String CARD_PET_HOTEL = "CARD_PET_HOTEL";
    public static final String CARD_THU_CUNG = "CARD_THU_CUNG";
    public static final String CARD_KHO = "CARD_KHO";
    public static final String CARD_NHAN_VIEN = "CARD_NHAN_VIEN";
    public static final String CARD_GIAO_CA = "CARD_GIAO_CA";
    public static final String CARD_PHAN_CA = "CARD_PHAN_CA";
    public static final String CARD_DOI_TRA = "CARD_DOI_TRA";

    /**
     * Constructor mặc định (dùng cho chạy thử nghiệm hoặc standalone preview).
     */
    public MainDashboard() {
        this(taoNhanVienMacDinh());
    }

    /**
     * Constructor chính với nhân viên đã xác thực từ LoginForm.
     */
    public MainDashboard(NhanVien nhanVien) {
        this.currentUser = nhanVien != null ? nhanVien : taoNhanVienMacDinh();
        UserSession.initSession(this.currentUser);
        khoiTaoGiaoDien();
    }

    private static NhanVien taoNhanVienMacDinh() {
        TaiKhoan tk = new TaiKhoan("admin", "123456", "NVQL", true);
        return new NhanVien("NV001", "Lê Khãi Minh", "Quản lý hệ thống", "0901234567", tk);
    }

    private void khoiTaoGiaoDien() {
        setTitle("HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG - " + UserSession.getFullName() + " [" + UserSession.getRoleCode() + "]");
        setSize(1360, 880);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColors.BACKGROUND);

        UserSession.Role role = UserSession.getCurrentRole();

        // 1. Thanh thực đơn đỉnh màn hình 6 nhóm chức năng (JMenuBar chuẩn Slide 17 IUH)
        setJMenuBar(taoJMenuBarHuongChucNang(role));

        // 2. Vùng đỉnh chỉ giữ Header thương hiệu gọn gàng (đã xóa dòng tab Tổng quan, Bán hàng POS...)
        add(taoTopHeader(), BorderLayout.NORTH);

        // 3. Đã giải phóng hoàn toàn Sidebar bên trái (Không add vào BorderLayout.WEST)
        // Dành trọn vẹn 100% diện tích chiều ngang cho POS và các Bảng dữ liệu!

        // 4. Vùng nội dung chính (Center CardLayout Panels)
        cardLayout = new CardLayout();
        pnlContentCards = new JPanel(cardLayout);
        pnlContentCards.setBackground(ThemeColors.BACKGROUND);

        // Khởi tạo các màn hình con nghiệp vụ
        tongQuanPanel = new TongQuanPanel();
        banHangPOSPanel = new BanHangPOSPanel();
        quanLyLichHenPanel = new QuanLyLichHenPanel();
        thuCungDichVuPanel = new ThuCungDichVuPanel();
        khoLoHangPanel = new KhoLoHangPanel();
        quanLyNhanVienPanel = new QuanLyNhanVienPanel();
        banGiaoCaPanel = new BanGiaoCaPanel();
        lichPhanCaPanel = new LichPhanCaPanel();
        petHotelPanel = new PetHotelPanel();
        doiTraHangPanel = new DoiTraHangPanel();

        pnlContentCards.add(tongQuanPanel, CARD_TONG_QUAN);
        pnlContentCards.add(banHangPOSPanel, CARD_POS);
        pnlContentCards.add(quanLyLichHenPanel, CARD_LICH_HEN);
        pnlContentCards.add(petHotelPanel, CARD_PET_HOTEL);
        pnlContentCards.add(thuCungDichVuPanel, CARD_THU_CUNG);
        pnlContentCards.add(khoLoHangPanel, CARD_KHO);
        pnlContentCards.add(quanLyNhanVienPanel, CARD_NHAN_VIEN);
        pnlContentCards.add(banGiaoCaPanel, CARD_GIAO_CA);
        pnlContentCards.add(lichPhanCaPanel, CARD_PHAN_CA);
        pnlContentCards.add(doiTraHangPanel, CARD_DOI_TRA);

        add(pnlContentCards, BorderLayout.CENTER);

        // 5. Thanh trạng thái dưới cùng (South Status Bar)
        add(taoBottomStatusBar(), BorderLayout.SOUTH);

        // Mặc định chọn màn hình phù hợp theo vai trò vừa đăng nhập
        chonManHinhMacDinhTheoVaiTro();
    }

    // =========================================================================
    // 1. THANH THỰC ĐƠN ĐỈNH MÀN HÌNH (JMENUBAR 6 NHÓM CHỨC NĂNG CHUẨN SLIDE 17)
    // =========================================================================
    private JMenuBar taoJMenuBarHuongChucNang(UserSession.Role role) {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(ThemeColors.SURFACE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER));

        // Nhóm 1: Hệ thống (Alt + 1)
        JMenu mnuHeThong = taoMenuChinh("1. Hệ thống", KeyEvent.VK_1, IconHelper.SETTINGS);
        mnuHeThong.add(taoMenuItem("Bảng điều khiển Tổng quan (Dashboard)", IconHelper.BAR_CHART, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), UserSession.canAccess(CARD_TONG_QUAN), e -> dieuHuongSangChucNang(CARD_TONG_QUAN)));
        mnuHeThong.addSeparator();
        mnuHeThong.add(taoMenuItem("Đổi mật khẩu tài khoản", IconHelper.KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), true, e -> thucHienDoiMatKhau()));
        mnuHeThong.add(taoMenuItem("Phân quyền vai trò", IconHelper.SHIELD, KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), UserSession.canAccessNhom1HeThongPhanQuyen(), e -> dieuHuongSangChucNang(CARD_NHAN_VIEN)));
        mnuHeThong.add(taoMenuItem("Bàn giao ca / Chốt két", IconHelper.CLIPBOARD_CHECK, KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), UserSession.canAccessNhom1GiaoCa(), e -> dieuHuongSangChucNang(CARD_GIAO_CA)));
        mnuHeThong.addSeparator();
        mnuHeThong.add(taoMenuItem("Đăng xuất tài khoản", IconHelper.LOG_OUT, KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), true, e -> xuLyDangXuat()));
        mnuHeThong.add(taoMenuItem("Thoát ứng dụng", IconHelper.TRASH, KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK), true, e -> thucHienThoatHeThong()));
        menuBar.add(mnuHeThong);

        // Nhóm 2: Danh mục (Alt + 2)
        JMenu mnuDanhMuc = taoMenuChinh("2. Danh mục", KeyEvent.VK_2, IconHelper.BOXES);
        mnuDanhMuc.add(taoMenuItem("Hàng hóa & ĐVT quy đổi", IconHelper.BOXES, KeyStroke.getKeyStroke(KeyEvent.VK_H, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), UserSession.canAccessNhom2DanhMucHangHoa(), e -> {
            dieuHuongSangChucNang(CARD_KHO);
            if (khoLoHangPanel != null) khoLoHangPanel.chonTabSanPham();
        }));
        mnuDanhMuc.add(taoMenuItem("Dịch vụ Grooming / Spa", IconHelper.SCISSORS, KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), true, e -> {
            dieuHuongSangChucNang(CARD_THU_CUNG);
            if (thuCungDichVuPanel != null) thuCungDichVuPanel.chonTabLichHen();
        }));
        mnuDanhMuc.add(taoMenuItem("Khách hàng thành viên & Điểm VIP", IconHelper.USER, KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), true, e -> {
            dieuHuongSangChucNang(CARD_THU_CUNG);
            if (thuCungDichVuPanel != null) thuCungDichVuPanel.chonTabKhachHang();
        }));
        mnuDanhMuc.add(taoMenuItem("Hồ sơ Thú cưng & Microchip", IconHelper.PAW, KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), true, e -> {
            dieuHuongSangChucNang(CARD_THU_CUNG);
            if (thuCungDichVuPanel != null) thuCungDichVuPanel.chonTabHoSoThuCung();
        }));
        mnuDanhMuc.add(taoMenuItem("Nhà cung cấp hàng hóa", IconHelper.BOXES, KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), UserSession.canAccessNhom2DanhMucNhaCungCap(), e -> {
            dieuHuongSangChucNang(CARD_KHO);
            if (khoLoHangPanel != null) khoLoHangPanel.chonTabLoHangFEFO();
        }));
        menuBar.add(mnuDanhMuc);

        // Nhóm 3: Cập nhật (Alt + 3)
        JMenu mnuCapNhat = taoMenuChinh("3. Cập nhật", KeyEvent.VK_3, IconHelper.EDIT);
        mnuCapNhat.add(taoMenuItem("Nhập kho hàng hóa (FEFO)", IconHelper.PLUS, KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK), UserSession.canAccessNhom3CapNhatNhapKho(), e -> {
            dieuHuongSangChucNang(CARD_KHO);
            if (khoLoHangPanel != null) khoLoHangPanel.nhapKhoHangHoa();
        }));
        mnuCapNhat.add(taoMenuItem("Tiếp nhận lịch hẹn mới", IconHelper.CALENDAR, KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), UserSession.canAccessNhom3CapNhatTiepNhanLich(), e -> {
            dieuHuongSangChucNang(CARD_LICH_HEN);
            if (quanLyLichHenPanel != null) quanLyLichHenPanel.tiepNhanLichHen();
        }));
        mnuCapNhat.add(taoMenuItem("Hồ sơ thể trạng & Sức khỏe bé", IconHelper.STETHOSCOPE, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), UserSession.canAccessNhom3CapNhatSucKhoeBe(), e -> {
            dieuHuongSangChucNang(CARD_THU_CUNG);
            if (thuCungDichVuPanel != null) thuCungDichVuPanel.chonTabHoSoSucKhoe();
        }));
        mnuCapNhat.add(taoMenuItem("Cập nhật nhân sự & Tài khoản", IconHelper.USERS, KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), UserSession.canAccessNhom3CapNhatNhanSu(), e -> {
            dieuHuongSangChucNang(CARD_NHAN_VIEN);
            if (quanLyNhanVienPanel != null) quanLyNhanVienPanel.capNhatNhanSu();
        }));
        menuBar.add(mnuCapNhat);

        // Nhóm 4: Xử lý (Alt + 4)
        JMenu mnuXuLy = taoMenuChinh("4. Xử lý", KeyEvent.VK_4, IconHelper.CART);
        mnuXuLy.add(taoMenuItem("Bán hàng POS (Lập HĐ)", IconHelper.CART, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), UserSession.canAccessNhom4XuLyBanHangPOS(), e -> {
            dieuHuongSangChucNang(CARD_POS);
            if (banHangPOSPanel != null) banHangPOSPanel.xuLyLapHoaDon();
        }));
        mnuXuLy.add(taoMenuItem("Phân ca làm việc (4 ca tuần)", IconHelper.CALENDAR, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), UserSession.canAccessNhom4XuLyPhanCaToanQuyen(), e -> {
            dieuHuongSangChucNang(CARD_PHAN_CA);
            if (lichPhanCaPanel != null) lichPhanCaPanel.thucHienPhanCa();
        }));
        mnuXuLy.add(taoMenuItem("Cập nhật tiến độ dịch vụ", IconHelper.SPARKLES, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), UserSession.canAccessNhom4XuLyTienDoDichVu(), e -> {
            dieuHuongSangChucNang(CARD_LICH_HEN);
        }));
        mnuXuLy.add(taoMenuItem("Xử lý đổi trả hàng & Tra soát HĐ", IconHelper.RECEIPT, KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), UserSession.canAccessNhom4XuLyDoiTraHang(), e -> {
            dieuHuongSangChucNang(CARD_DOI_TRA);
        }));
        mnuXuLy.add(taoMenuItem("Lưu trú Khách sạn Thú cưng (Pet Hotel)", IconHelper.HOME, KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), UserSession.canAccess(CARD_PET_HOTEL), e -> {
            dieuHuongSangChucNang(CARD_PET_HOTEL);
        }));
        menuBar.add(mnuXuLy);

        // Nhóm 5: Tìm kiếm (Alt + 5)
        JMenu mnuTimKiem = taoMenuChinh("5. Tìm kiếm", KeyEvent.VK_5, IconHelper.SEARCH);
        mnuTimKiem.add(taoMenuItem("Tra cứu Khách hàng & Điểm VIP", IconHelper.SEARCH, KeyStroke.getKeyStroke(KeyEvent.VK_F1, KeyEvent.CTRL_DOWN_MASK), UserSession.canAccessNhom5TimKiemKhachVIP(), e -> {
            dieuHuongSangChucNang(CARD_POS);
            if (banHangPOSPanel != null) banHangPOSPanel.traCuuKhachHangVaVIP("");
        }));
        mnuTimKiem.add(taoMenuItem("Tra cứu Lịch hẹn dịch vụ", IconHelper.CALENDAR, KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.CTRL_DOWN_MASK), UserSession.canAccessNhom5TimKiemLichHen(), e -> {
            dieuHuongSangChucNang(CARD_LICH_HEN);
        }));
        mnuTimKiem.add(taoMenuItem("Tra cứu Tồn kho & Lô FEFO", IconHelper.BOXES, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.CTRL_DOWN_MASK), UserSession.canAccessNhom5TimKiemTonKhoFEFO(), e -> {
            dieuHuongSangChucNang(CARD_KHO);
            if (khoLoHangPanel != null) khoLoHangPanel.chonTabLoHangFEFO();
        }));
        mnuTimKiem.add(taoMenuItem("Tra cứu Hóa đơn cũ", IconHelper.RECEIPT, KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.CTRL_DOWN_MASK), UserSession.canAccessNhom5TimKiemHoaDonCu(), e -> {
            dieuHuongSangChucNang(CARD_POS);
            if (banHangPOSPanel != null) banHangPOSPanel.chonTabLichSu();
        }));
        menuBar.add(mnuTimKiem);

        // Nhóm 6: Báo biểu (Alt + 6) - Mở rộng đầy đủ 7 loại thống kê quản trị
        JMenu mnuBaoBieu = taoMenuChinh("6. Báo biểu", KeyEvent.VK_6, IconHelper.BAR_CHART);
        mnuBaoBieu.add(taoMenuItem("1. Báo cáo Doanh thu & Lợi nhuận gộp", IconHelper.BAR_CHART, KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), UserSession.canAccessNhom6BaoBieuDoanhThu(), e -> {
            BaoCaoThongKeDialog.moHopThoaiBaoCao(this, 0);
        }));
        mnuBaoBieu.add(taoMenuItem("2. Top Sản phẩm & Dịch vụ bán chạy", IconHelper.TRENDING_UP, KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), UserSession.canAccessNhom6BaoBieuThongKeDichVu(), e -> {
            BaoCaoThongKeDialog.moHopThoaiBaoCao(this, 1);
        }));
        mnuBaoBieu.add(taoMenuItem("3. Hiệu suất Nhân viên & Kỹ thuật viên Spa", IconHelper.USERS, null, UserSession.canAccessNhom6BaoBieuThongKeDichVu(), e -> {
            BaoCaoThongKeDialog.moHopThoaiBaoCao(this, 2);
        }));
        mnuBaoBieu.add(taoMenuItem("4. Phân tích Khách hàng VIP & Tích điểm", IconHelper.SPARKLES, null, UserSession.canAccessNhom5TimKiemKhachVIP(), e -> {
            BaoCaoThongKeDialog.moHopThoaiBaoCao(this, 3);
        }));
        mnuBaoBieu.add(taoMenuItem("5. Thống kê Lưu trú Pet Hotel & Đổi trả hàng", IconHelper.HOME, null, UserSession.canAccess(CARD_PET_HOTEL), e -> {
            BaoCaoThongKeDialog.moHopThoaiBaoCao(this, 4);
        }));
        mnuBaoBieu.addSeparator();
        mnuBaoBieu.add(taoMenuItem("6. Cảnh báo Hạn dùng Lô hàng FEFO", IconHelper.ALERT_TRIANGLE, KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), UserSession.canAccessNhom6BaoBieuCanhBaoFEFO(), e -> {
            BaoCaoThongKeDialog.moHopThoaiBaoCao(this, 5);
        }));
        mnuBaoBieu.add(taoMenuItem("7. Báo cáo Xuất - Nhập - Tồn kho tổng hợp", IconHelper.BOXES, KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), UserSession.canAccessNhom6BaoBieuXuatNhapTon(), e -> {
            BaoCaoThongKeDialog.moHopThoaiBaoCao(this, 6);
        }));
        menuBar.add(mnuBaoBieu);

        return menuBar;
    }

    private JMenu taoMenuChinh(String title, int mnemonic, String iconName) {
        JMenu menu = new JMenu(title);
        menu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        menu.setForeground(ThemeColors.TEXT_PRIMARY);
        menu.setMnemonic(mnemonic);
        menu.setIcon(IconHelper.getIcon(iconName, 16, ThemeColors.PRIMARY));
        menu.setIconTextGap(8);
        menu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return menu;
    }

    private JMenuItem taoMenuItem(String text, String iconName, KeyStroke accelerator, boolean enabled, ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        item.setIcon(IconHelper.getIcon(iconName, 15, enabled ? ThemeColors.TEXT_PRIMARY : ThemeColors.TEXT_MUTED));
        item.setIconTextGap(8);
        if (accelerator != null) {
            item.setAccelerator(accelerator);
        }
        item.setEnabled(enabled);
        if (!enabled) {
            item.setToolTipText("Chức năng chỉ dành cho vai trò được cấp phép");
        }
        item.addActionListener(action);
        return item;
    }

    // =========================================================================
    // 2. THANH ĐIỀU HƯỚNG NGANG (TOP NAVIGATION BAR 6 NHÓM CHỨC NĂNG SLIDE 17 IUH)
    // =========================================================================
    private JPanel taoTopNavigationBar(UserSession.Role role) {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(ThemeColors.SURFACE);
        navBar.setPreferredSize(new Dimension(0, 48));
        navBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER),
                new EmptyBorder(4, 16, 4, 16)
        ));

        menuButtons.clear();
        menuIconNames.clear();
        menuCardNames.clear();

        JPanel pnlTabsContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        pnlTabsContainer.setOpaque(false);

        // TẦNG 2: DÃY NÚT DẠNG VIÊN THUỐC (PILL TABS) PHÂN QUYỀN THEO VAI TRÒ
        switch (role) {
            case NVQL:
                pnlTabsContainer.add(taoNutQuickTab("Tổng quan", IconHelper.BAR_CHART, CARD_TONG_QUAN));
                pnlTabsContainer.add(taoNutQuickTab("Bán hàng POS", IconHelper.CART, CARD_POS));
                pnlTabsContainer.add(taoNutQuickTab("Đổi trả hàng", IconHelper.REFRESH, CARD_DOI_TRA));
                pnlTabsContainer.add(taoNutQuickTab("Lịch hẹn & Spa", IconHelper.CALENDAR, CARD_LICH_HEN));
                pnlTabsContainer.add(taoNutQuickTab("Pet Hotel", IconHelper.HOME, CARD_PET_HOTEL));
                pnlTabsContainer.add(taoNutQuickTab("Khách & Thú cưng", IconHelper.PAW, CARD_THU_CUNG));
                pnlTabsContainer.add(taoNutQuickTab("Kho & Lô FEFO", IconHelper.BOXES, CARD_KHO));
                pnlTabsContainer.add(taoNutQuickTab("Phân ca tuần", IconHelper.CALENDAR, CARD_PHAN_CA));
                break;
            case NVBH:
                pnlTabsContainer.add(taoNutQuickTab("Bán hàng POS", IconHelper.CART, CARD_POS));
                pnlTabsContainer.add(taoNutQuickTab("Đổi trả hàng", IconHelper.REFRESH, CARD_DOI_TRA));
                pnlTabsContainer.add(taoNutQuickTab("Khách & Thú cưng", IconHelper.PAW, CARD_THU_CUNG));
                pnlTabsContainer.add(taoNutQuickTab("Tra cứu kho", IconHelper.BOXES, "ACTION_TRA_CUU_KHO"));
                break;
            case NVTN:
                pnlTabsContainer.add(taoNutQuickTab("Bán hàng POS", IconHelper.CART, CARD_POS));
                pnlTabsContainer.add(taoNutQuickTab("Đổi trả hàng", IconHelper.REFRESH, CARD_DOI_TRA));
                pnlTabsContainer.add(taoNutQuickTab("Hóa đơn chờ & Tiến độ", IconHelper.CLOCK, "ACTION_DON_CHO"));
                pnlTabsContainer.add(taoNutQuickTab("Lịch hẹn & Spa", IconHelper.CALENDAR, CARD_LICH_HEN));
                pnlTabsContainer.add(taoNutQuickTab("Pet Hotel", IconHelper.HOME, CARD_PET_HOTEL));
                break;
            case NVCS:
                pnlTabsContainer.add(taoNutQuickTab("Lịch hẹn & Spa", IconHelper.CALENDAR, CARD_LICH_HEN));
                pnlTabsContainer.add(taoNutQuickTab("Pet Hotel", IconHelper.HOME, CARD_PET_HOTEL));
                pnlTabsContainer.add(taoNutQuickTab("Hồ sơ thú cưng", IconHelper.PAW, "ACTION_HO_SO_THU_CUNG"));
                break;
        }

        JScrollPane scrollNav = new JScrollPane(pnlTabsContainer);
        scrollNav.setBorder(null);
        scrollNav.setOpaque(false);
        scrollNav.getViewport().setOpaque(false);
        scrollNav.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollNav.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollNav.getHorizontalScrollBar().setUnitIncrement(16);

        navBar.add(scrollNav, BorderLayout.CENTER);
        return navBar;
    }

    private JButton taoNutQuickTab(String title, String iconName, String cardName) {
        JButton btn = new JButton(title, IconHelper.getIcon(iconName, 15, ThemeColors.TEXT_SECONDARY));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(ThemeColors.TEXT_PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setIconTextGap(6);
        btn.putClientProperty("FlatLaf.style", "arc: 20");
        btn.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!btn.getBackground().equals(ThemeColors.PRIMARY)) {
                    btn.setBackground(ThemeColors.PRIMARY_TINT);
                    btn.setBorder(new CompoundBorder(
                            new LineBorder(ThemeColors.PRIMARY_HOVER, 1, true),
                            new EmptyBorder(6, 14, 6, 14)
                    ));
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!btn.getBackground().equals(ThemeColors.PRIMARY)) {
                    btn.setBackground(Color.WHITE);
                    btn.setBorder(new CompoundBorder(
                            new LineBorder(ThemeColors.BORDER, 1, true),
                            new EmptyBorder(6, 14, 6, 14)
                    ));
                }
            }
        });
        btn.addActionListener(e -> dieuHuongSangChucNang(cardName));

        menuButtons.add(btn);
        menuIconNames.add(iconName);
        menuCardNames.add(cardName);
        return btn;
    }

    private void chonManHinhMacDinhTheoVaiTro() {
        UserSession.Role role = UserSession.getCurrentRole();
        switch (role) {
            case NVQL:
                dieuHuongSangChucNang(CARD_TONG_QUAN);
                break;
            case NVBH:
                dieuHuongSangChucNang(CARD_POS);
                break;
            case NVCS:
                dieuHuongSangChucNang(CARD_LICH_HEN);
                break;
            case NVTN:
                dieuHuongSangChucNang(CARD_POS);
                break;
        }
    }

    /**
     * Điều hướng sang thẻ chức năng với kiểm tra thẩm quyền RBAC và đồng bộ dữ liệu.
     */
    public void dieuHuongSangChucNang(String cardName) {
        String targetCard = cardName;
        boolean openDonCho = false;
        boolean openTraCuuKho = false;
        boolean openHoSoThuCung = false;

        if ("ACTION_DON_CHO".equals(cardName)) {
            targetCard = CARD_POS;
            openDonCho = true;
        } else if ("ACTION_TRA_CUU_KHO".equals(cardName)) {
            targetCard = CARD_KHO;
            openTraCuuKho = true;
        } else if ("ACTION_HO_SO_THU_CUNG".equals(cardName)) {
            targetCard = CARD_THU_CUNG;
            openHoSoThuCung = true;
        }

        if (!UserSession.canAccess(targetCard)) {
            JOptionPane.showMessageDialog(this,
                    "Bạn không có quyền truy cập vào phân hệ này!\nVui lòng liên hệ Quản lý hệ thống để được cấp quyền.",
                    "Truy cập bị từ chối",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (CARD_TONG_QUAN.equals(targetCard) && tongQuanPanel != null) {
            tongQuanPanel.lamMoiDuLieuDashboard();
        } else if (CARD_PHAN_CA.equals(targetCard) && lichPhanCaPanel != null) {
            lichPhanCaPanel.taiDuLieuMaTran();
        } else if (CARD_PET_HOTEL.equals(targetCard) && petHotelPanel != null) {
            petHotelPanel.capNhatSoLieuKPI();
        } else if (CARD_DOI_TRA.equals(targetCard) && doiTraHangPanel != null) {
            doiTraHangPanel.lamMoiGiaoDien();
        }

        cardLayout.show(pnlContentCards, targetCard);

        if (openDonCho && banHangPOSPanel != null) {
            banHangPOSPanel.chonTabDonCho();
        } else if (openTraCuuKho && khoLoHangPanel != null) {
            khoLoHangPanel.chonTabSanPham();
        } else if (openHoSoThuCung && thuCungDichVuPanel != null) {
            thuCungDichVuPanel.chonTabHoSoThuCung();
        }

        // Cập nhật trạng thái Active / Inactive của nút Pill Tabs
        for (int i = 0; i < menuButtons.size(); i++) {
            JButton b = menuButtons.get(i);
            String iconName = menuIconNames.get(i);
            String cName = menuCardNames.get(i);

            boolean isActive = cName.equals(cardName) || (targetCard.equals(cName) && !cName.startsWith("ACTION_"));
            if (isActive) {
                b.setBackground(ThemeColors.PRIMARY); // Hồng san hô #FF8BA7
                b.setForeground(ThemeColors.TEXT_PRIMARY); // Nâu sẫm #33272A
                b.setIcon(IconHelper.getIcon(iconName, 15, ThemeColors.TEXT_PRIMARY));
                b.setBorder(new CompoundBorder(
                        new LineBorder(ThemeColors.PRIMARY_HOVER, 1, true),
                        new EmptyBorder(6, 14, 6, 14)
                ));
            } else {
                b.setBackground(Color.WHITE);
                b.setForeground(ThemeColors.TEXT_PRIMARY);
                b.setIcon(IconHelper.getIcon(iconName, 15, ThemeColors.TEXT_SECONDARY));
                b.setBorder(new CompoundBorder(
                        new LineBorder(ThemeColors.BORDER, 1, true),
                        new EmptyBorder(6, 14, 6, 14)
                ));
            }
        }
    }

    // =========================================================================
    // 3. THANH TIÊU ĐỀ & THANH TRẠNG THÁI
    // =========================================================================
    private JPanel taoTopHeader() {
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(ThemeColors.HEADER_BG);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER),
                new EmptyBorder(4, 16, 4, 16)
        ));

        // Logo & Tên thương hiệu
        JPanel pnlLogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        pnlLogo.setOpaque(false);

        JLabel lblLogo = new JLabel(IconHelper.getIcon(IconHelper.PAW, 22, ThemeColors.PRIMARY));
        JLabel lblBrand = new JLabel("PET SHOP MANAGEMENT SYSTEM");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBrand.setForeground(ThemeColors.TEXT_PRIMARY);

        pnlLogo.add(lblLogo);
        pnlLogo.add(lblBrand);

        // Thông tin người dùng & Badge vai trò
        UserSession.Role role = UserSession.getCurrentRole();
        JPanel pnlUser = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        pnlUser.setOpaque(false);

        String roleBadgeText = role.getCode() + " - " + role.getDescription().toUpperCase();
        JLabel lblRoleBadge = ThemeColors.createBadge(roleBadgeText, ThemeColors.PRIMARY_SOFT, ThemeColors.TEXT_PRIMARY);

        String roleIconName = getRoleIcon(role);
        JLabel lblRoleIcon = new JLabel(IconHelper.getIcon(roleIconName, 18, ThemeColors.PRIMARY));

        JLabel lblUserInfo = new JLabel(currentUser.getHoTen() + " (" + currentUser.getMaNhanVien() + ")");
        lblUserInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUserInfo.setForeground(ThemeColors.TEXT_PRIMARY);

        JButton btnDangXuat = new JButton("Đăng xuất", IconHelper.getIcon(IconHelper.LOG_OUT, 13, Color.WHITE));
        btnDangXuat.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDangXuat.setBackground(ThemeColors.DANGER);
        btnDangXuat.setForeground(Color.WHITE);
        btnDangXuat.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.DANGER, 1, true),
                new EmptyBorder(3, 10, 3, 10)
        ));
        btnDangXuat.setFocusPainted(false);
        btnDangXuat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDangXuat.setIconTextGap(5);
        btnDangXuat.addActionListener(e -> xuLyDangXuat());

        pnlUser.add(lblRoleIcon);
        pnlUser.add(lblUserInfo);
        pnlUser.add(lblRoleBadge);
        pnlUser.add(btnDangXuat);

        header.add(pnlLogo, BorderLayout.WEST);
        header.add(pnlUser, BorderLayout.EAST);
        return header;
    }

    private String getRoleIcon(UserSession.Role role) {
        switch (role) {
            case NVQL: return IconHelper.ROLE_NVQL;
            case NVBH: return IconHelper.ROLE_NVBH;
            case NVCS: return IconHelper.ROLE_NVCS;
            case NVTN: return IconHelper.ROLE_NVTN;
            default: return IconHelper.USER_CHECK;
        }
    }

    private JPanel taoBottomStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(ThemeColors.BACKGROUND);
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeColors.BORDER),
                new EmptyBorder(6, 16, 6, 16)
        ));

        JLabel lblDbStatus = new JLabel("  Trạng thái: Sẵn sàng làm việc (Slide 17 Functional Architecture)",
                IconHelper.getIcon(IconHelper.CHECK, 14, ThemeColors.SUCCESS), SwingConstants.LEFT);
        lblDbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDbStatus.setForeground(ThemeColors.TEXT_PRIMARY);

        String timeNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"));
        JLabel lblTime = new JLabel("Thời gian máy chủ: " + timeNow + "  ",
                IconHelper.getIcon(IconHelper.CLOCK, 14, ThemeColors.TEXT_MUTED), SwingConstants.RIGHT);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTime.setForeground(ThemeColors.TEXT_SECONDARY);
        lblTime.setHorizontalTextPosition(SwingConstants.LEFT);

        status.add(lblDbStatus, BorderLayout.WEST);
        status.add(lblTime, BorderLayout.EAST);
        return status;
    }

    // =========================================================================
    // 4. HÀNH ĐỘNG HỆ THỐNG
    // =========================================================================
    public void thucHienDoiMatKhau() {
        JPasswordField txtOldPass = new JPasswordField();
        JPasswordField txtNewPass = new JPasswordField();
        JPasswordField txtConfirmPass = new JPasswordField();

        Object[] msg = {
                "Tài khoản hiện tại: " + UserSession.getFullName() + " (" + UserSession.getRoleCode() + ")",
                "Mật khẩu hiện tại:", txtOldPass,
                "Mật khẩu mới (tối thiểu 4 ký tự):", txtNewPass,
                "Xác nhận mật khẩu mới:", txtConfirmPass
        };

        int opt = JOptionPane.showConfirmDialog(this, msg, "ĐỔI MẬT KHẨU TÀI KHOẢN", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, IconHelper.getIcon(IconHelper.KEY, 24, ThemeColors.PRIMARY));
        if (opt == JOptionPane.OK_OPTION) {
            String oldP = new String(txtOldPass.getPassword()).trim();
            String newP = new String(txtNewPass.getPassword()).trim();
            String confP = new String(txtConfirmPass.getPassword()).trim();

            if (oldP.isEmpty() || newP.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ mật khẩu!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newP.equals(confP)) {
                JOptionPane.showMessageDialog(this, "Xác nhận mật khẩu mới không khớp!", "Lỗi xác nhận", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (newP.length() < 4) {
                JOptionPane.showMessageDialog(this, "Mật khẩu mới phải có ít nhất 4 ký tự!", "Cảnh báo bảo mật", JOptionPane.WARNING_MESSAGE);
                return;
            }

            DangNhapController c = new DangNhapController();
            boolean ok = c.doiMatKhau(currentUser.getMaNhanVien(), oldP, newP);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu tài khoản thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thất bại. Vui lòng kiểm tra lại mật khẩu hiện tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void thucHienThoatHeThong() {
        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn thoát hoàn toàn khỏi ứng dụng?",
                "Xác nhận thoát",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void xuLyDangXuat() {
        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?",
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            DangNhapController.dangXuat();
            this.dispose();
            new LoginForm().setVisible(true);
        }
    }

    // Getters cho các Panel con phục vụ kiểm thử
    public TongQuanPanel getTongQuanPanel() { return tongQuanPanel; }
    public BanHangPOSPanel getBanHangPOSPanel() { return banHangPOSPanel; }
    public QuanLyLichHenPanel getQuanLyLichHenPanel() { return quanLyLichHenPanel; }
    public ThuCungDichVuPanel getThuCungDichVuPanel() { return thuCungDichVuPanel; }
    public KhoLoHangPanel getKhoLoHangPanel() { return khoLoHangPanel; }
    public QuanLyNhanVienPanel getQuanLyNhanVienPanel() { return quanLyNhanVienPanel; }
    public BanGiaoCaPanel getBanGiaoCaPanel() { return banGiaoCaPanel; }
    public LichPhanCaPanel getLichPhanCaPanel() { return lichPhanCaPanel; }
    public PetHotelPanel getPetHotelPanel() { return petHotelPanel; }
    public DoiTraHangPanel getDoiTraHangPanel() { return doiTraHangPanel; }

    public static void main(String[] args) {
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }
}
