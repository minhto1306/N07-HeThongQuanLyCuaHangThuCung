package gui;

import bus.BanHangBus;
import bus.SpaPassBus;
import bus.ThuCungBus;
import entity.HoSoSucKhoe;
import entity.HoSoThuCung;
import entity.KhachHang;
import entity.TheSpaPass;
import entity.ThuCung;
import utils.IconHelper;
import utils.ThemeColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel Quản lý Thú cưng, Dịch vụ & Khách hàng Thành viên.
 * Bao gồm quản lý Hồ sơ thú cưng (Microchip), Hồ sơ sức khỏe thể trạng, Lịch hẹn chăm sóc và Khách hàng Thành viên (CRUD).
 * Thiết kế chuẩn phong cách Warm Peach / Soft Apricot, tích hợp Lucide Icons đồng bộ.
 */
public class ThuCungDichVuPanel extends JPanel {

    private final ThuCungBus controller = new ThuCungBus();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SpaPassBus SpaPassBus = new SpaPassBus();
    private DefaultTableModel modelHoSo;
    private DefaultTableModel modelSucKhoe;
    private DefaultTableModel modelKhachHang;
    private JTable tblKhachHang;
    private JTextField txtTimKhachHang;
    private JComboBox<String> cboLocHangKhach;
    private DefaultTableModel modelTheHoiVien;
    private JTable tblTheHoiVien;
    private JTextField txtTimTheHoiVien;
    private JComboBox<String> cboLocTrangThaiThe;
    private JTabbedPane tabbedPane;

    public ThuCungDichVuPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(14, 16, 14, 16));
        setBackground(ThemeColors.BACKGROUND);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Tích hợp Icon Lucide vào từng Tab
        tabbedPane.addTab("  Hồ sơ Thú cưng & Microchip",
                IconHelper.getPrimaryIcon(IconHelper.PAW, 16), taoTabHoSoThuCung());
        tabbedPane.addTab("  Hồ sơ Sức khỏe & Thể trạng",
                IconHelper.getPrimaryIcon(IconHelper.STETHOSCOPE, 16), taoTabHoSoSucKhoe());
        tabbedPane.addTab("  Lịch hẹn & Phân công KTV",
                IconHelper.getPrimaryIcon(IconHelper.CALENDAR, 16), new QuanLyLichHenPanel(controller));
        tabbedPane.addTab("  Khách hàng Thành viên & Điểm VIP",
                IconHelper.getPrimaryIcon(IconHelper.USERS, 16), taoTabQuanLyKhachHang());
        tabbedPane.addTab("  Thẻ Hội Viên & Gói Spa Pass",
                IconHelper.getPrimaryIcon(IconHelper.CREDIT_CARD, 16), taoTabQuanLyTheHoiVien());

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void chonTabHoSoThuCung() {
        if (tabbedPane != null) tabbedPane.setSelectedIndex(0);
    }

    public void chonTabHoSoSucKhoe() {
        if (tabbedPane != null) tabbedPane.setSelectedIndex(1);
    }

    public void chonTabLichHen() {
        if (tabbedPane != null) tabbedPane.setSelectedIndex(2);
    }

    public void chonTabKhachHang() {
        if (tabbedPane != null) {
            tabbedPane.setSelectedIndex(3);
            napDuLieuKhachHang();
        }
    }

    public void chonTabTheHoiVien() {
        if (tabbedPane != null) {
            tabbedPane.setSelectedIndex(4);
            napDuLieuTheHoiVien();
        }
    }

    public void dangKyThuCungMoi() {
        chonTabHoSoThuCung();
        moDialogDangKyThuCung();
    }

    private JPanel taoTabHoSoThuCung() {
        JPanel pnl = new JPanel(new BorderLayout(10, 12));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        // Thanh công cụ trên
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlToolbar.setOpaque(false);

        JTextField txtSearch = new JTextField(18);
        txtSearch.setPreferredSize(new Dimension(220, 36));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên bé, chủ, SĐT, microchip...");
        txtSearch.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.SEARCH, 15, ThemeColors.TEXT_MUTED));
        txtSearch.setBorder(ThemeColors.createInputBorder());

        JButton btnThem = ThemeColors.createPrimaryButton("Đăng ký Thú cưng mới", IconHelper.getIcon(IconHelper.PLUS, 14, Color.WHITE));
        JButton btnLamMoi = ThemeColors.createSecondaryButton("Làm mới", IconHelper.getIcon(IconHelper.REFRESH, 14, ThemeColors.TEXT_SECONDARY));

        btnThem.addActionListener(e -> moDialogDangKyThuCung());
        btnLamMoi.addActionListener(e -> {
            txtSearch.setText("");
            napDuLieuHoSoThuCung("");
        });
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { napDuLieuHoSoThuCung(txtSearch.getText()); }
            public void removeUpdate(DocumentEvent e) { napDuLieuHoSoThuCung(txtSearch.getText()); }
            public void changedUpdate(DocumentEvent e) { napDuLieuHoSoThuCung(txtSearch.getText()); }
        });

        pnlToolbar.add(new JLabel("Từ khóa:"));
        pnlToolbar.add(txtSearch);
        pnlToolbar.add(btnThem);
        pnlToolbar.add(btnLamMoi);

        // Bảng danh sách
        String[] cols = {"Mã TC", "Tên thú cưng", "Giống loài", "Ngày sinh", "Giới tính", "Chủ nuôi", "Số điện thoại", "Mã Microchip", "Trạng thái"};
        modelHoSo = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable tbl = taoBangChuan(modelHoSo);
        napDuLieuHoSoThuCung("");

        pnl.add(pnlToolbar, BorderLayout.NORTH);
        pnl.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return pnl;
    }

    private void napDuLieuHoSoThuCung() {
        napDuLieuHoSoThuCung("");
    }

    private void napDuLieuHoSoThuCung(String tuKhoa) {
        modelHoSo.setRowCount(0);
        String kw = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase();
        List<HoSoThuCung> list = controller.layDanhSachHoSoThuCung();
        for (HoSoThuCung hs : list) {
            ThuCung tc = hs.getThuCung();
            String chu = hs.getKhachHang() != null ? hs.getKhachHang().getHoTen() : "Khách vãng lai";
            String sdt = hs.getKhachHang() != null ? hs.getKhachHang().getSoDienThoai() : "";
            String chip = hs.getSoMicrochip() != null ? hs.getSoMicrochip() : "";
            if (!kw.isEmpty()) {
                boolean match = tc.getTenThuCung().toLowerCase().contains(kw)
                        || tc.getMaThuCung().toLowerCase().contains(kw)
                        || chu.toLowerCase().contains(kw)
                        || sdt.toLowerCase().contains(kw)
                        || chip.toLowerCase().contains(kw);
                if (!match) continue;
            }
            modelHoSo.addRow(new Object[]{
                    tc.getMaThuCung(),
                    tc.getTenThuCung(),
                    tc.getGiongLoai(),
                    tc.getNgaySinh() != null ? tc.getNgaySinh().format(df) : "Chưa rõ",
                    tc.getGioiTinh(),
                    chu,
                    sdt,
                    chip,
                    hs.getTrangThaiNuoi()
            });
        }
    }

    private void moDialogDangKyThuCung() {
        String maTC = utils.MaTuSinhHelper.sinhMaThuCung();
        JTextField txtMaTC = new JTextField(maTC);
        txtMaTC.setEditable(false);
        txtMaTC.setBackground(new Color(0xF1, 0xF5, 0xF9));

        JTextField txtTen = new JTextField();
        JTextField txtGiong = new JTextField("Mèo Anh Lông Ngắn");
        JComboBox<String> cboSex = new JComboBox<>(new String[]{"Đực", "Cái"});
        JTextField txtChu = new JTextField("Nguyễn Văn An");
        JTextField txtSdt = new JTextField("0988111222");
        JTextField txtChip = new JTextField("MC-982" + maTC.replace("TC", "") + "-VN");

        Object[] msg = {
                "Mã thú cưng (Hệ thống tự sinh):", txtMaTC,
                "Tên thú cưng:", txtTen,
                "Giống loài:", txtGiong,
                "Giới tính:", cboSex,
                "Chủ sở hữu:", txtChu,
                "Số điện thoại chủ:", txtSdt,
                "Số Microchip gắn kèm:", txtChip
        };

        int opt = JOptionPane.showConfirmDialog(this, msg, "ĐĂNG KÝ HỒ SƠ THÚ CƯNG MỚI", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION && !txtTen.getText().trim().isEmpty()) {
            ThuCung tc = new ThuCung(maTC, txtTen.getText().trim(),
                    txtGiong.getText().trim(), LocalDate.now().minusMonths(6), (String) cboSex.getSelectedItem());
            boolean ok = controller.themThuCungTheoSdtChu(tc, txtChu.getText().trim(), txtSdt.getText().trim(), txtChip.getText().trim());
            if (ok) {
                napDuLieuHoSoThuCung();
                napDuLieuKhachHang();
                JOptionPane.showMessageDialog(this, "Đã tạo hồ sơ thú cưng [" + maTC + "] và cấp mã Microchip thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Không thể lưu hồ sơ thú cưng vào CSDL! Vui lòng kiểm tra lại thông tin.", "Lỗi lưu trữ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel taoTabHoSoSucKhoe() {
        JPanel pnl = new JPanel(new BorderLayout(10, 12));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlToolbar.setOpaque(false);

        JButton btnKiemTraMoi = ThemeColors.createPrimaryButton("Khám / Kiểm tra thể trạng trước dịch vụ",
                IconHelper.getIcon(IconHelper.PLUS, 14, Color.WHITE));
        JButton btnLamMoiSK = ThemeColors.createSecondaryButton("Làm mới",
                IconHelper.getIcon(IconHelper.REFRESH, 14, ThemeColors.TEXT_SECONDARY));

        btnKiemTraMoi.addActionListener(e -> moDialogKhamSucKhoe());
        btnLamMoiSK.addActionListener(e -> napDuLieuSucKhoe());

        pnlToolbar.add(btnKiemTraMoi);
        JButton btnGuiMailNhacTiem = ThemeColors.createSecondaryButton("Gửi Email Nhắc Tiêm Phòng & Voucher 10%", IconHelper.getIcon(IconHelper.STETHOSCOPE, 14, ThemeColors.PRIMARY_ACTIVE));
        btnGuiMailNhacTiem.addActionListener(e -> guiEmailNhacTiemPhongThuCung());
        pnlToolbar.add(btnGuiMailNhacTiem);
        pnlToolbar.add(btnLamMoiSK);

        String[] cols = {"Mã SK", "Thú cưng", "Ngày kiểm tra", "Cân nặng (kg)", "Nhiệt độ (°C)", "Tình trạng ve rận", "Tiêm phòng dại", "Người kiểm tra", "Ghi chú thể trạng"};
        modelSucKhoe = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable tbl = taoBangChuan(modelSucKhoe);
        napDuLieuSucKhoe();

        pnl.add(pnlToolbar, BorderLayout.NORTH);
        pnl.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return pnl;
    }

    private void napDuLieuSucKhoe() {
        modelSucKhoe.setRowCount(0);
        List<HoSoSucKhoe> list = controller.layDanhSachHoSoSucKhoe();
        for (HoSoSucKhoe sk : list) {
            modelSucKhoe.addRow(new Object[]{
                    sk.getMaSucKhoe(),
                    sk.getThuCung() != null ? sk.getThuCung().getTenThuCung() : "Thú cưng",
                    sk.getNgayKiemTra() != null ? sk.getNgayKiemTra().format(dtf) : "",
                    sk.getCanNang() + " kg",
                    sk.getNhietDo() + " °C",
                    sk.getTinhTrangVeRan(),
                    sk.isTiemPhongDai() ? "Đã tiêm" : "Chưa rõ",
                    sk.getNhanVienKiemTra() != null ? sk.getNhanVienKiemTra().getHoTen() : "KTV",
                    sk.getGhiChuTheTrang()
            });
        }
    }

    private void moDialogKhamSucKhoe() {
        String maSK = utils.MaTuSinhHelper.sinhMaSucKhoe();
        JTextField txtMaSK = new JTextField(maSK);
        txtMaSK.setEditable(false);
        txtMaSK.setBackground(new Color(0xF1, 0xF5, 0xF9));

        // 1. Tạo ComboBox lấy danh sách thú cưng thực tế từ Controller thay vì gõ tay
        JComboBox<ThuCung> cboThuCung = new JComboBox<>();
        List<HoSoThuCung> dsHoSo = controller.layDanhSachHoSoThuCung();
        for (HoSoThuCung hs : dsHoSo) {
            if (hs.getThuCung() != null) {
                cboThuCung.addItem(hs.getThuCung());
            }
        }
        // Custom hiển thị tên trên ComboBox
        cboThuCung.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ThuCung) {
                    ThuCung t = (ThuCung) value;
                    setText(t.getMaThuCung() + " - " + t.getTenThuCung() + " (" + t.getGiongLoai() + ")");
                }
                return this;
            }
        });

        JTextField txtCanNang = new JTextField("4.5");
        JTextField txtNhietDo = new JTextField("38.6");
        JTextField txtVeRan = new JTextField("Không có");
        JCheckBox chkTiemPhong = new JCheckBox("Đã tiêm phòng dại đủ mũi", true);
        JTextField txtGhiChu = new JTextField("Thể trạng bình thường, sẵn sàng làm dịch vụ");

        Object[] msg = {
                "Mã phiếu khám (Hệ thống tự sinh):", txtMaSK,
                "Chọn thú cưng khám:", cboThuCung, // Thay JTextField bằng JComboBox
                "Cân nặng (kg):", txtCanNang,
                "Nhiệt độ (°C):", txtNhietDo,
                "Tình trạng ve rận / da lông:", txtVeRan,
                "Tiêm phòng:", chkTiemPhong,
                "Ghi chú sức khỏe:", txtGhiChu
        };

        int opt = JOptionPane.showConfirmDialog(this, msg, "KIỂM TRA THỂ TRẠNG TRƯỚC DỊCH VỤ", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            // 2. Lấy đúng đối tượng thú cưng được chọn từ ComboBox
            ThuCung selectedTC = (ThuCung) cboThuCung.getSelectedItem();
            if (selectedTC == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn thú cưng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            entity.NhanVien cur = utils.UserSession.getCurrentUser();
            entity.NhanVien nvKiemTra = (cur != null) ? cur : new entity.NhanVien("NV001", "Lê Khãi Minh", "NVQL", null, null);

            HoSoSucKhoe sk = new HoSoSucKhoe(
                    maSK,
                    selectedTC, // Truyền đúng thú cưng đã chọn (mang mã và tên thật)
                    nvKiemTra,
                    new BigDecimal(txtCanNang.getText().trim()),
                    new BigDecimal(txtNhietDo.getText().trim()),
                    txtVeRan.getText().trim(),
                    chkTiemPhong.isSelected(),
                    txtGhiChu.getText().trim(),
                    LocalDateTime.now()
            );
            
            boolean okSK = controller.themHoSoSucKhoe(sk);
            if (okSK) {
                napDuLieuSucKhoe();
                JOptionPane.showMessageDialog(this, "Đã lưu hồ sơ sức khỏe [" + maSK + "] và thể trạng thú cưng!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Không thể lưu hồ sơ sức khỏe vào CSDL!", "Lỗi lưu trữ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JTable taoBangChuan(DefaultTableModel model) {
        JTable tbl = new JTable(model);
        tbl.setRowHeight(34);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.setShowHorizontalLines(true);
        tbl.setShowVerticalLines(false);
        tbl.setGridColor(ThemeColors.BORDER_LIGHT);
        tbl.setSelectionBackground(ThemeColors.PRIMARY_SOFT);
        tbl.setSelectionForeground(ThemeColors.TEXT_PRIMARY);

        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tbl.getTableHeader().setBackground(ThemeColors.PRIMARY_TINT);
        tbl.getTableHeader().setForeground(ThemeColors.TEXT_PRIMARY);
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 36));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(65);
        tbl.getColumnModel().getColumn(0).setCellRenderer(center);

        return tbl;
    }

    // =========================================================================
    // TAB 4: QUẢN LÝ KHÁCH HÀNG THÀNH VIÊN & ĐIỂM VIP (TÌM KIẾM + THÊM / SỬA / XÓA)
    // =========================================================================
    private JPanel taoTabQuanLyKhachHang() {
        JPanel pnl = new JPanel(new BorderLayout(10, 12));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        pnlToolbar.setOpaque(false);

        txtTimKhachHang = new JTextField(18);
        txtTimKhachHang.setPreferredSize(new Dimension(230, 36));
        txtTimKhachHang.putClientProperty("JTextField.placeholderText", "Tìm theo SĐT, họ tên, mã KH...");
        txtTimKhachHang.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.SEARCH, 15, ThemeColors.TEXT_MUTED));
        txtTimKhachHang.setBorder(ThemeColors.createInputBorder());

        cboLocHangKhach = new JComboBox<>(new String[]{
                "Tất cả khách hàng",
                "Khách cũ - VIP Kim Cương (>= 100 điểm)",
                "Khách cũ - Hạng Vàng (50 - 99 điểm)",
                "Khách cũ - Hạng Bạc (20 - 49 điểm)",
                "Khách hàng Mới (< 20 điểm)"
        });
        cboLocHangKhach.setPreferredSize(new Dimension(245, 36));
        cboLocHangKhach.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton btnThemKH = ThemeColors.createPrimaryButton("+ Thêm Khách mới",
                IconHelper.getIcon(IconHelper.PLUS, 14, Color.WHITE));
        JButton btnSuaKH = ThemeColors.createSecondaryButton("Sửa thông tin KH",
                IconHelper.getIcon(IconHelper.EDIT, 14, ThemeColors.PRIMARY_ACTIVE));
        JButton btnXoaKH = ThemeColors.createSecondaryButton("Xóa KH",
                IconHelper.getIcon(IconHelper.TRASH, 14, ThemeColors.DANGER));
        btnXoaKH.setForeground(ThemeColors.DANGER);
        JButton btnLamMoiKH = ThemeColors.createSecondaryButton("Làm mới",
                IconHelper.getIcon(IconHelper.REFRESH, 14, ThemeColors.TEXT_SECONDARY));

        txtTimKhachHang.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { napDuLieuKhachHang(); }
            public void removeUpdate(DocumentEvent e) { napDuLieuKhachHang(); }
            public void changedUpdate(DocumentEvent e) { napDuLieuKhachHang(); }
        });
        cboLocHangKhach.addActionListener(e -> napDuLieuKhachHang());

        btnThemKH.addActionListener(e -> moDialogThemKhachHang());
        btnSuaKH.addActionListener(e -> moDialogSuaKhachHang());
        btnXoaKH.addActionListener(e -> xoaKhachHangDangChon());
        btnLamMoiKH.addActionListener(e -> {
            txtTimKhachHang.setText("");
            cboLocHangKhach.setSelectedIndex(0);
            napDuLieuKhachHang();
        });

        pnlToolbar.add(new JLabel("Tìm KH:"));
        pnlToolbar.add(txtTimKhachHang);
        pnlToolbar.add(new JLabel("Phân loại:"));
        pnlToolbar.add(cboLocHangKhach);
        pnlToolbar.add(btnThemKH);
        pnlToolbar.add(btnSuaKH);
        pnlToolbar.add(btnXoaKH);
        pnlToolbar.add(btnLamMoiKH);

        String[] cols = {"Mã KH", "Họ và tên khách hàng", "Số điện thoại", "Email nhận tin", "Địa chỉ liên hệ", "Điểm tích lũy", "Phân loại", "Hạng thẻ", "Ưu đãi quy đổi"};
        modelKhachHang = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblKhachHang = taoBangChuan(modelKhachHang);
        napDuLieuKhachHang();

        pnl.add(pnlToolbar, BorderLayout.NORTH);
        pnl.add(new JScrollPane(tblKhachHang), BorderLayout.CENTER);
        return pnl;
    }

    public void napDuLieuKhachHang() {
        if (modelKhachHang == null) return;
        modelKhachHang.setRowCount(0);
        String kw = txtTimKhachHang != null ? txtTimKhachHang.getText().trim() : "";
        int filterIdx = cboLocHangKhach != null ? cboLocHangKhach.getSelectedIndex() : 0;

        List<KhachHang> ds = BanHangBus.timKiemGoiYKhachHang(kw);
        for (KhachHang kh : ds) {
            int diem = kh.getDiemTichLuy();
            if (filterIdx == 1 && diem < 100) continue;
            if (filterIdx == 2 && (diem < 50 || diem >= 100)) continue;
            if (filterIdx == 3 && (diem < 20 || diem >= 50)) continue;
            if (filterIdx == 4 && diem >= 20) continue;

            String phanLoai = diem >= 20 ? "Khách quen (Khách cũ)" : "Khách hàng Mới";
            String hangThe = BanHangBus.xepHangThanhVien(diem);
            String uuDai = String.format("Quy đổi tối đa %,d đ (%d điểm)", diem * 1000L, diem);

            modelKhachHang.addRow(new Object[]{
                    kh.getMaKhachHang(),
                    kh.getHoTen(),
                    kh.getSoDienThoai(),
                    (kh.getEmail() != null && !kh.getEmail().isEmpty()) ? kh.getEmail() : "(chưa có)",
                    (kh.getDiaChi() != null && !kh.getDiaChi().isEmpty()) ? kh.getDiaChi() : "(chưa có)",
                    diem + " điểm",
                    phanLoai,
                    hangThe,
                    uuDai
            });
        }
    }

    private void moDialogThemKhachHang() {
        JTextField txtMa = new JTextField(BanHangBus.sinhMaKhachHangMoi());
        txtMa.setEditable(false);
        txtMa.setFocusable(false);
        txtMa.setBackground(new Color(0xF1, 0xF5, 0xF9));
        JTextField txtHoTen = new JTextField();
        JTextField txtSdt = new JTextField();
        JTextField txtDiem = new JTextField("0");
        JTextField txtEmail = new JTextField(); // thêm ô nhập email để gửi thông báo lịch hẹn
        JTextField txtDiaChi = new JTextField();

        Object[] msg = {
                "Mã khách hàng (Tự động):", txtMa,
                "Họ và tên khách hàng (*):", txtHoTen,
                "Số điện thoại (10 số) (*):", txtSdt,
                "Email nhận thông báo lịch & tiêm phòng:", txtEmail,
                "Địa chỉ liên hệ nhà khách:", txtDiaChi,
                "Điểm tích lũy khởi tạo:", txtDiem
        };

        int opt = JOptionPane.showConfirmDialog(this, msg, "THÊM KHÁCH HÀNG THÀNH VIÊN MỚI", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String ten = txtHoTen.getText().trim();
            String sdt = txtSdt.getText().trim();
            if (ten.isEmpty() || !sdt.matches("^0\\d{9}$")) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Họ tên và Số điện thoại hợp lệ (10 chữ số bắt đầu bằng 0)!",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int diem = 0;
            try {
                diem = Math.max(0, Integer.parseInt(txtDiem.getText().trim()));
            } catch (NumberFormatException ignored) {
            }
            String email = txtEmail.getText().trim();
            String diaChi = txtDiaChi.getText().trim();
            BanHangBus.luuHoacCapNhatKhachHang(sdt, ten, diem, email, diaChi);
            napDuLieuKhachHang();
            JOptionPane.showMessageDialog(this, "Đã lưu hồ sơ khách hàng [" + ten + " - " + sdt + "] thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void moDialogSuaKhachHang() {
        int row = tblKhachHang.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 khách hàng trên bảng để chỉnh sửa thông tin!",
                    "Chưa chọn khách hàng", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maKH = modelKhachHang.getValueAt(row, 0).toString();
        String tenCu = modelKhachHang.getValueAt(row, 1).toString();
        String sdtCu = modelKhachHang.getValueAt(row, 2).toString();
        String emailCu = modelKhachHang.getValueAt(row, 3).toString().replace("(chưa có)", "");
        String diaChiCu = modelKhachHang.getValueAt(row, 4).toString().replace("(chưa có)", "");
        String diemCuStr = modelKhachHang.getValueAt(row, 5).toString().replaceAll("\\D+", "");

        JTextField txtHoTen = new JTextField(tenCu);
        JTextField txtSdt = new JTextField(sdtCu);
        JTextField txtEmail = new JTextField(emailCu);
        JTextField txtDiaChi = new JTextField(diaChiCu);
        JTextField txtDiem = new JTextField(diemCuStr.isEmpty() ? "0" : diemCuStr);

        Object[] msg = {
                "Mã khách hàng: " + maKH,
                "Họ và tên khách hàng (*):", txtHoTen,
                "Số điện thoại (10 số) (*):", txtSdt,
                "Email nhận thông báo (*):", txtEmail,
                "Địa chỉ nhà khách:", txtDiaChi,
                "Điểm tích lũy hiện có:", txtDiem
        };

        int opt = JOptionPane.showConfirmDialog(this, msg, "CẬP NHẬT THÔNG TIN KHÁCH HÀNG " + maKH, JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String tenMoi = txtHoTen.getText().trim();
            String sdtMoi = txtSdt.getText().trim();
            if (tenMoi.isEmpty() || !sdtMoi.matches("^0\\d{9}$")) {
                JOptionPane.showMessageDialog(this, "Họ tên không được để trống và SĐT phải gồm 10 chữ số bắt đầu bằng 0!",
                        "Lỗi định dạng", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int diemMoi = 0;
            try {
                diemMoi = Math.max(0, Integer.parseInt(txtDiem.getText().trim()));
            } catch (NumberFormatException ignored) {
            }
            String emailMoi = txtEmail.getText().trim();
            String diaChiMoi = txtDiaChi.getText().trim();
            if (BanHangBus.capNhatThongTinKhachHang(maKH, tenMoi, sdtMoi, diemMoi, emailMoi, diaChiMoi)) {
                napDuLieuKhachHang();
                JOptionPane.showMessageDialog(this, "Đã cập nhật thông tin khách hàng " + maKH + " thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void xoaKhachHangDangChon() {
        int row = tblKhachHang.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 khách hàng trên bảng để xóa!",
                    "Chưa chọn khách hàng", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maKH = modelKhachHang.getValueAt(row, 0).toString();
        String tenKH = modelKhachHang.getValueAt(row, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa khách hàng [" + maKH + " - " + tenKH + "] khỏi danh sách thành viên?",
                "Xác nhận xóa khách hàng", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (BanHangBus.xoaKhachHang(maKH)) {
                napDuLieuKhachHang();
                JOptionPane.showMessageDialog(this, "Đã xóa khách hàng [" + maKH + "] khỏi hệ thống.",
                        "Đã xóa", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // =========================================================================
    // TAB 5: QUẢN LÝ THẺ HỘI VIÊN & GÓI DỊCH VỤ SPA PASS (PUNCH CARD TRẢ TRƯỚC)
    // =========================================================================
    private JPanel taoTabQuanLyTheHoiVien() {
        JPanel pnl = new JPanel(new BorderLayout(10, 12));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlToolbar.setOpaque(false);

        txtTimTheHoiVien = new JTextField(18);
        txtTimTheHoiVien.setPreferredSize(new Dimension(240, 36));
        txtTimTheHoiVien.putClientProperty("JTextField.placeholderText", "Tìm theo SĐT, tên khách, mã thẻ...");
        txtTimTheHoiVien.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.SEARCH, 15, ThemeColors.TEXT_MUTED));
        txtTimTheHoiVien.setBorder(ThemeColors.createInputBorder());

        cboLocTrangThaiThe = new JComboBox<>(new String[]{"Tất cả trạng thái", "Đang hoạt động", "Đã hết buổi", "Hết hạn sử dụng"});
        cboLocTrangThaiThe.setPreferredSize(new Dimension(170, 36));
        cboLocTrangThaiThe.setBackground(ThemeColors.SURFACE);

        JButton btnPhatHanhThe = ThemeColors.createPrimaryButton("Phát hành Thẻ mới", IconHelper.getIcon(IconHelper.PLUS, 14, Color.WHITE));
        JButton btnNapBuoi = ThemeColors.createSecondaryButton("Nạp thêm / Gia hạn buổi", IconHelper.getIcon(IconHelper.EDIT, 14, ThemeColors.TEXT_SECONDARY));
        JButton btnLamMoiThe = ThemeColors.createSecondaryButton("Làm mới", IconHelper.getIcon(IconHelper.REFRESH, 14, ThemeColors.TEXT_SECONDARY));

        txtTimTheHoiVien.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { napDuLieuTheHoiVien(); }
            public void removeUpdate(DocumentEvent e) { napDuLieuTheHoiVien(); }
            public void changedUpdate(DocumentEvent e) { napDuLieuTheHoiVien(); }
        });
        cboLocTrangThaiThe.addActionListener(e -> napDuLieuTheHoiVien());

        btnPhatHanhThe.addActionListener(e -> moDialogPhatHanhTheMoi());
        btnNapBuoi.addActionListener(e -> moDialogGiaHanHoacNapBuoi());
        btnLamMoiThe.addActionListener(e -> {
            txtTimTheHoiVien.setText("");
            cboLocTrangThaiThe.setSelectedIndex(0);
            napDuLieuTheHoiVien();
        });

        pnlToolbar.add(new JLabel("Tìm kiếm:"));
        pnlToolbar.add(txtTimTheHoiVien);
        pnlToolbar.add(new JLabel("Trạng thái:"));
        pnlToolbar.add(cboLocTrangThaiThe);
        pnlToolbar.add(btnPhatHanhThe);
        pnlToolbar.add(btnNapBuoi);
        pnlToolbar.add(btnLamMoiThe);

        String[] cols = {"Mã Thẻ", "Tên khách hàng", "Số điện thoại", "Tên gói dịch vụ", "Tổng buổi", "Đã dùng", "Còn lại", "Đơn giá gói", "Ngày kích hoạt", "Hạn sử dụng", "Trạng thái"};
        modelTheHoiVien = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblTheHoiVien = taoBangChuan(modelTheHoiVien);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        tblTheHoiVien.getColumnModel().getColumn(4).setCellRenderer(center);
        tblTheHoiVien.getColumnModel().getColumn(5).setCellRenderer(center);
        tblTheHoiVien.getColumnModel().getColumn(6).setCellRenderer(center);
        tblTheHoiVien.getColumnModel().getColumn(8).setCellRenderer(center);
        tblTheHoiVien.getColumnModel().getColumn(9).setCellRenderer(center);

        tblTheHoiVien.getColumnModel().getColumn(10).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String st = value != null ? value.toString() : "";
                if ("ĐANG HOẠT ĐỘNG".equalsIgnoreCase(st)) {
                    lbl.setForeground(new Color(5, 150, 105)); // Emerald
                } else if ("ĐÃ HẾT BUỔI".equalsIgnoreCase(st)) {
                    lbl.setForeground(new Color(220, 38, 38)); // Red
                } else {
                    lbl.setForeground(new Color(217, 119, 6)); // Amber
                }
                return lbl;
            }
        });

        napDuLieuTheHoiVien();

        pnl.add(pnlToolbar, BorderLayout.NORTH);
        pnl.add(new JScrollPane(tblTheHoiVien), BorderLayout.CENTER);
        return pnl;
    }

    public void napDuLieuTheHoiVien() {
        if (modelTheHoiVien == null) return;
        modelTheHoiVien.setRowCount(0);
        String kw = txtTimTheHoiVien != null ? txtTimTheHoiVien.getText().trim().toLowerCase() : "";
        int filterIdx = cboLocTrangThaiThe != null ? cboLocTrangThaiThe.getSelectedIndex() : 0;

        List<TheSpaPass> dsThe = SpaPassBus.layTatCaThe();
        for (TheSpaPass the : dsThe) {
            the.capNhatTrangThai();
            String st = the.getTrangThai();

            if (filterIdx == 1 && !"ĐANG HOẠT ĐỘNG".equalsIgnoreCase(st)) continue;
            if (filterIdx == 2 && !"ĐÃ HẾT BUỔI".equalsIgnoreCase(st)) continue;
            if (filterIdx == 3 && !"HẾT HẠN SỬ DỤNG".equalsIgnoreCase(st)) continue;

            if (!kw.isEmpty()) {
                boolean match = (the.getMaThe() != null && the.getMaThe().toLowerCase().contains(kw))
                        || (the.getTenKhachHang() != null && the.getTenKhachHang().toLowerCase().contains(kw))
                        || (the.getSdtKhachHang() != null && the.getSdtKhachHang().contains(kw))
                        || (the.getTenGoi() != null && the.getTenGoi().toLowerCase().contains(kw));
                if (!match) continue;
            }

            modelTheHoiVien.addRow(new Object[]{
                    the.getMaThe(),
                    the.getTenKhachHang(),
                    the.getSdtKhachHang(),
                    the.getTenGoi(),
                    the.getTongSoBuoi() + " buổi",
                    the.getSoBuoiDaDung() + " buổi",
                    the.getSoBuoiConLai() + " buổi",
                    String.format("%,d đ", the.getDonGiaGoi()),
                    the.getNgayKichHoat() != null ? the.getNgayKichHoat().format(df) : "",
                    the.getNgayHetHan() != null ? the.getNgayHetHan().format(df) : "",
                    st
            });
        }
    }

    private void moDialogPhatHanhTheMoi() {
        JTextField txtSdt = new JTextField();
        JTextField txtHoTen = new JTextField();
        JComboBox<String> cboGoi = new JComboBox<>(new String[]{
                "Gói Spa Pass 5 Buổi (900.000 đ - Tiết kiệm 20%)",
                "Gói Spa Pass 10 Buổi (1.600.000 đ - Tặng 2 buổi tắm)",
                "Gói Chăm Sóc Định Kỳ 3 Buổi (580.000 đ)"
        });

        txtSdt.getDocument().addDocumentListener(new DocumentListener() {
            private void lookup() {
                String s = txtSdt.getText().trim();
                if (s.matches("^0\\d{9}$")) {
                    List<KhachHang> ds = BanHangBus.timKiemGoiYKhachHang(s);
                    for (KhachHang kh : ds) {
                        if (s.equals(kh.getSoDienThoai())) {
                            txtHoTen.setText(kh.getHoTen());
                            break;
                        }
                    }
                }
            }
            public void insertUpdate(DocumentEvent e) { lookup(); }
            public void removeUpdate(DocumentEvent e) { lookup(); }
            public void changedUpdate(DocumentEvent e) { lookup(); }
        });

        Object[] msg = {
                "Số điện thoại khách hàng (10 số) (*):", txtSdt,
                "Họ và tên khách hàng (*):", txtHoTen,
                "Loại gói Spa Pass phát hành (*):", cboGoi
        };

        int opt = JOptionPane.showConfirmDialog(this, msg, "PHÁT HÀNH THẺ HỘI VIÊN / SPA PASS MỚI", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String sdt = txtSdt.getText().trim();
            String ten = txtHoTen.getText().trim();
            if (ten.isEmpty() || !sdt.matches("^0\\d{9}$")) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Họ tên và Số điện thoại 10 chữ số hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int goiIdx = cboGoi.getSelectedIndex();
            int soBuoi = goiIdx == 1 ? 10 : (goiIdx == 2 ? 3 : 5);
            long donGia = goiIdx == 1 ? 1600000L : (goiIdx == 2 ? 580000L : 900000L);
            String tenGoi = goiIdx == 1 ? "Gói Spa Pass 10 Buổi (Gia Đình)" : (goiIdx == 2 ? "Gói Chăm Sóc Định Kỳ 3 Buổi" : "Gói Spa Pass 5 Buổi (Punch Card)");

            TheSpaPass theMoi = SpaPassBus.kichHoatTheMoi(sdt, ten, tenGoi, soBuoi, donGia);
            napDuLieuTheHoiVien();
            JOptionPane.showMessageDialog(this,
                    "Phát hành Thẻ Spa Pass thành công!\n"
                            + "- Mã thẻ: " + theMoi.getMaThe() + "\n"
                            + "- Khách hàng: " + theMoi.getTenKhachHang() + " (" + theMoi.getSdtKhachHang() + ")\n"
                            + "- Gói dịch vụ: " + theMoi.getTenGoi() + " (" + theMoi.getTongSoBuoi() + " buổi)\n"
                            + "- Hạn sử dụng: Đến ngày " + theMoi.getNgayHetHanStr(),
                    "Phát hành thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void moDialogGiaHanHoacNapBuoi() {
        int row = tblTheHoiVien.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 thẻ trên bảng để nạp thêm hoặc gia hạn!", "Chưa chọn thẻ", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maThe = modelTheHoiVien.getValueAt(row, 0).toString();
        String tenKhach = modelTheHoiVien.getValueAt(row, 1).toString();
        TheSpaPass the = SpaPassBus.timTheTheoMa(maThe);
        if (the == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin thẻ: " + maThe, "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField txtSoBuoiNap = new JTextField("5");
        JTextField txtSoNgayGiaHan = new JTextField("60");

        Object[] msg = {
                "Mã thẻ: " + the.getMaThe() + " - Khách hàng: " + tenKhach,
                "Số buổi hiện tại: " + the.getSoBuoiConLai() + "/" + the.getTongSoBuoi() + " buổi (Hạn: " + the.getNgayHetHanStr() + ")",
                "Số buổi nạp thêm:", txtSoBuoiNap,
                "Số ngày gia hạn thêm (ngày):", txtSoNgayGiaHan
        };

        int opt = JOptionPane.showConfirmDialog(this, msg, "NẠP THÊM BUỔI & GIA HẠN THẺ SPA PASS", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            try {
                int buoi = Integer.parseInt(txtSoBuoiNap.getText().trim());
                int ngay = Integer.parseInt(txtSoNgayGiaHan.getText().trim());
                the.napThemBuoi(buoi, ngay);
                napDuLieuTheHoiVien();
                JOptionPane.showMessageDialog(this,
                        "Đã nạp thêm " + buoi + " buổi và gia hạn " + ngay + " ngày cho thẻ [" + maThe + "] thành công!\n"
                                + "- Số buổi khả dụng mới: " + the.getSoBuoiConLai() + "/" + the.getTongSoBuoi() + " buổi\n"
                                + "- Hạn sử dụng mới: " + the.getNgayHetHanStr(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Số buổi và số ngày phải là số nguyên dương hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // hàm gửi email nhắc lịch tiêm phòng dại hoặc tái chủng 7 bệnh cho chủ thú cưng
    private void guiEmailNhacTiemPhongThuCung() {
        List<HoSoThuCung> ds = controller.layDanhSachHoSoThuCung();
        if (ds == null || ds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có hồ sơ thú cưng nào trong hệ thống!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        HoSoThuCung hsChon = ds.get(0);
        String tenBe = hsChon.getThuCung() != null ? hsChon.getThuCung().getTenThuCung() : "Bé cưng";
        String tenChu = hsChon.getKhachHang() != null ? hsChon.getKhachHang().getHoTen() : "Quý khách";
        String emailChu = (hsChon.getKhachHang() != null && hsChon.getKhachHang().getEmail() != null && !hsChon.getKhachHang().getEmail().isEmpty())
                ? hsChon.getKhachHang().getEmail() : "khachhang.an@gmail.com";

        String hanTaiChung = LocalDate.now().plusDays(7).format(df);
        String tieuDe = "[Mio Pet Clinic] Nhắc Lịch Tái Chủng Vaccine Định Kỳ Cho Bé " + tenBe;
        String html = utils.EmailThongBaoHelper.taoEmailNhacTiemPhong(
                tenChu, tenBe, "Vaccine Phòng Dại & Mũi 7 Bệnh Tổng Hợp", hanTaiChung, "MIOHEALTH10");

        utils.EmailThongBaoHelper.moHopThoaiGuiEmail(SwingUtilities.getWindowAncestor(this), emailChu, tieuDe, html);
    }

}
