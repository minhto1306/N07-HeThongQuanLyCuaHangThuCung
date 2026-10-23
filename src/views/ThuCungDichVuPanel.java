package views;

import controllers.BanHangController;
import controllers.ThuCungController;
import models.entity.HoSoSucKhoe;
import models.entity.HoSoThuCung;
import models.entity.KhachHang;
import models.entity.ThuCung;
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

    private final ThuCungController controller = new ThuCungController();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DefaultTableModel modelHoSo;
    private DefaultTableModel modelSucKhoe;
    private DefaultTableModel modelKhachHang;
    private JTable tblKhachHang;
    private JTextField txtTimKhachHang;
    private JComboBox<String> cboLocHangKhach;
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
        JTextField txtTen = new JTextField();
        JTextField txtGiong = new JTextField("Mèo Anh Lông Ngắn");
        JComboBox<String> cboSex = new JComboBox<>(new String[]{"Đực", "Cái"});
        JTextField txtChu = new JTextField("Nguyễn Văn An");
        JTextField txtSdt = new JTextField("0988111222");
        JTextField txtChip = new JTextField("MC" + System.currentTimeMillis() % 100000);

        Object[] msg = {
                "Tên thú cưng:", txtTen,
                "Giống loài:", txtGiong,
                "Giới tính:", cboSex,
                "Chủ sở hữu:", txtChu,
                "Số điện thoại chủ:", txtSdt,
                "Số Microchip gắn kèm:", txtChip
        };

        int opt = JOptionPane.showConfirmDialog(this, msg, "ĐĂNG KÝ HỒ SƠ THÚ CƯNG MỚI", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION && !txtTen.getText().trim().isEmpty()) {
            ThuCung tc = new ThuCung("TC" + System.currentTimeMillis() % 10000, txtTen.getText().trim(),
                    txtGiong.getText().trim(), LocalDate.now().minusMonths(6), (String) cboSex.getSelectedItem());
            controller.themThuCung(tc, "KH001", txtChip.getText().trim());
            napDuLieuHoSoThuCung();
            JOptionPane.showMessageDialog(this, "Đã tạo hồ sơ thú cưng và cấp mã Microchip thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
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
        JTextField txtTenTc = new JTextField("Miu Miu");
        JTextField txtCanNang = new JTextField("4.5");
        JTextField txtNhietDo = new JTextField("38.6");
        JTextField txtVeRan = new JTextField("Không có");
        JCheckBox chkTiemPhong = new JCheckBox("Đã tiêm phòng dại đủ mũi", true);
        JTextField txtGhiChu = new JTextField("Thể trạng bình thường, sẵn sàng làm dịch vụ");

        Object[] msg = {
                "Thú cưng khám:", txtTenTc,
                "Cân nặng (kg):", txtCanNang,
                "Nhiệt độ (°C):", txtNhietDo,
                "Tình trạng ve rận / da lông:", txtVeRan,
                "Tiêm phòng:", chkTiemPhong,
                "Ghi chú sức khỏe:", txtGhiChu
        };

        int opt = JOptionPane.showConfirmDialog(this, msg, "KIỂM TRA THỂ TRẠNG TRƯỚC DỊCH VỤ", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            ThuCung tc = new ThuCung("TC001", txtTenTc.getText().trim(), "Mèo", null, "Cái");
            HoSoSucKhoe sk = new HoSoSucKhoe(
                    "SK" + System.currentTimeMillis() % 10000,
                    tc,
                    null,
                    new BigDecimal(txtCanNang.getText().trim()),
                    new BigDecimal(txtNhietDo.getText().trim()),
                    txtVeRan.getText().trim(),
                    chkTiemPhong.isSelected(),
                    txtGhiChu.getText().trim(),
                    LocalDateTime.now()
            );
            controller.themHoSoSucKhoe(sk);
            napDuLieuSucKhoe();
            JOptionPane.showMessageDialog(this, "Đã lưu hồ sơ sức khỏe và thể trạng thú cưng!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
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

        String[] cols = {"Mã KH", "Họ và tên khách hàng", "Số điện thoại", "Điểm tích lũy", "Phân loại (Khách cũ / Mới)", "Hạng thẻ thành viên", "Ưu đãi quy đổi"};
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

        List<KhachHang> ds = BanHangController.timKiemGoiYKhachHang(kw);
        for (KhachHang kh : ds) {
            int diem = kh.getDiemTichLuy();
            if (filterIdx == 1 && diem < 100) continue;
            if (filterIdx == 2 && (diem < 50 || diem >= 100)) continue;
            if (filterIdx == 3 && (diem < 20 || diem >= 50)) continue;
            if (filterIdx == 4 && diem >= 20) continue;

            String phanLoai = diem >= 20 ? "Khách quen (Khách cũ)" : "Khách hàng Mới";
            String hangThe = BanHangController.xepHangThanhVien(diem);
            String uuDai = String.format("Quy đổi tối đa %,d đ (%d điểm)", diem * 1000L, diem);

            modelKhachHang.addRow(new Object[]{
                    kh.getMaKhachHang(),
                    kh.getHoTen(),
                    kh.getSoDienThoai(),
                    diem + " điểm",
                    phanLoai,
                    hangThe,
                    uuDai
            });
        }
    }

    private void moDialogThemKhachHang() {
        JTextField txtMa = new JTextField(BanHangController.sinhMaKhachHangMoi());
        txtMa.setEditable(false);
        JTextField txtHoTen = new JTextField();
        JTextField txtSdt = new JTextField();
        JTextField txtDiem = new JTextField("0");

        Object[] msg = {
                "Mã khách hàng (Tự động):", txtMa,
                "Họ và tên khách hàng (*):", txtHoTen,
                "Số điện thoại (10 số) (*):", txtSdt,
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
            BanHangController.luuHoacCapNhatKhachHang(sdt, ten, diem);
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
        String diemCuStr = modelKhachHang.getValueAt(row, 3).toString().replaceAll("\\D+", "");

        JTextField txtHoTen = new JTextField(tenCu);
        JTextField txtSdt = new JTextField(sdtCu);
        JTextField txtDiem = new JTextField(diemCuStr.isEmpty() ? "0" : diemCuStr);

        Object[] msg = {
                "Mã khách hàng: " + maKH,
                "Họ và tên khách hàng (*):", txtHoTen,
                "Số điện thoại (10 số) (*):", txtSdt,
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
            if (BanHangController.capNhatThongTinKhachHang(maKH, tenMoi, sdtMoi, diemMoi)) {
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
            if (BanHangController.xoaKhachHang(maKH)) {
                napDuLieuKhachHang();
                JOptionPane.showMessageDialog(this, "Đã xóa khách hàng [" + maKH + "] khỏi hệ thống.",
                        "Đã xóa", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
