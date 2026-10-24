package views;

import controllers.ThuCungController;
import models.entity.*;
import utils.IconHelper;
import utils.ThemeColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hộp thoại tiếp nhận đặt lịch hẹn chăm sóc thú cưng mới (SaaS Balanced 2-Column Layout).
 * Thiết kế giao diện hiện đại với Time Slot Badges, Mini Calendar date picker,
 * tự động tính size thú cưng và loại trừ tuyệt đối các dịch vụ y tế/bác sĩ.
 */
public class DatLichHenDialog extends JDialog {

    private final ThuCungController controller;
    private final Runnable onSuccessCallback;

    // Cột Trái: Thông tin khách & thú cưng
    private JTextField txtKhachHang;
    private JTextField txtSdt;
    private JTextField txtTenThuCung;
    private JComboBox<String> cboGiongLoai;
    private JSpinner spnCanNang;
    private JLabel lblSizeBadge;
    private JRadioButton radDuc;
    private JRadioButton radCai;
    private JTextArea txtGhiChuThuCung;

    // Cột Phải: Dịch vụ, KTV & Thời gian
    private JComboBox<String> cboDichVu;
    private JComboBox<String> cboKtv;
    private LocalDate selectedDate = LocalDate.now();
    private JLabel lblNgayHienTai;
    private String selectedTimeSlot = "09:30";

    // Time Slot Badges Map
    private final Map<String, JButton> slotButtons = new HashMap<>();
    private final String[] morningSlots = {"08:30", "09:30", "10:30", "11:00"};
    private final String[] afternoonSlots = {"13:30", "14:30", "15:30", "16:30", "17:30", "18:30"};

    private JCheckBox chkCaoVoi;
    private JCheckBox chkXitDuong;

    private boolean isConfirmed = false;

    public DatLichHenDialog(Frame parent, ThuCungController controller, Runnable onSuccessCallback) {
        super(parent, "TIẾP NHẬN LỊCH HẸN DỊCH VỤ SPA & GROOMING", true);
        this.controller = controller;
        this.onSuccessCallback = onSuccessCallback;

        setSize(840, 650);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColors.SURFACE);

        initUI();
        capNhatSlotAvailability();
    }

    private void initUI() {
        // 1. Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(ThemeColors.PRIMARY_TINT);
        pnlHeader.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER_LIGHT),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel lblTitle = new JLabel("TIẾP NHẬN ĐẶT LỊCH HẸN CHĂM SÓC THÚ CƯNG", IconHelper.getPrimaryIcon(IconHelper.CALENDAR, 22), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Hệ thống đặt hẹn Spa & Grooming thuần túy • Phân công Kỹ thuật viên & Chọn khung giờ trực quan");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Body: Balanced 2 Columns
        JPanel pnlBody = new JPanel(new GridLayout(1, 2, 16, 0));
        pnlBody.setBackground(ThemeColors.BACKGROUND);
        pnlBody.setBorder(new EmptyBorder(14, 16, 14, 16));

        pnlBody.add(taoCotTraiKhachHangVaThuCung());
        pnlBody.add(taoCotPhaiDichVuVaKhungGio());

        add(pnlBody, BorderLayout.CENTER);

        // 3. Footer
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(ThemeColors.SURFACE);
        pnlFooter.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeColors.BORDER),
                new EmptyBorder(10, 20, 10, 20)
        ));

        JLabel lblSummary = new JLabel("<html><span style='color:#64748B;'>Trạng thái mặc định:</span> <b style='color:#F59E0B;'>Chờ tiếp nhận</b> &nbsp;|&nbsp; <span style='color:#64748B;'>Thời lượng:</span> <b>45 - 90 phút</b></html>");
        lblSummary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlFooter.add(lblSummary, BorderLayout.WEST);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);

        JButton btnHuy = ThemeColors.createSecondaryButton("Hủy bỏ", null);
        JButton btnXacNhan = ThemeColors.createPrimaryButton("Xác nhận Đặt lịch", IconHelper.getIcon(IconHelper.CHECK, 14, Color.WHITE));

        btnHuy.addActionListener(e -> dispose());
        btnXacNhan.addActionListener(e -> xuLyLuuLichHen());

        pnlButtons.add(btnHuy);
        pnlButtons.add(btnXacNhan);
        pnlFooter.add(pnlButtons, BorderLayout.EAST);

        add(pnlFooter, BorderLayout.SOUTH);
    }

    private JPanel taoCotTraiKhachHangVaThuCung() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(ThemeColors.SURFACE);
        card.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        // Title
        JLabel lblSec = new JLabel("THÔNG TIN KHÁCH & BÉ CƯNG", IconHelper.getPrimaryIcon(IconHelper.USER, 16), SwingConstants.LEFT);
        lblSec.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSec.setForeground(ThemeColors.TEXT_PRIMARY);
        card.add(lblSec, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Họ tên khách
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        form.add(new JLabel("Họ tên khách:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtKhachHang = new JTextField("Nguyễn Văn An");
        txtKhachHang.setBorder(ThemeColors.createInputBorder());
        txtKhachHang.setPreferredSize(new Dimension(0, 30));
        form.add(txtKhachHang, gbc);

        // SĐT & Nút Tra cứu nhanh
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1;
        JPanel pnlSdt = new JPanel(new BorderLayout(4, 0));
        pnlSdt.setOpaque(false);
        txtSdt = new JTextField("0988111222");
        txtSdt.setBorder(ThemeColors.createInputBorder());
        txtSdt.setPreferredSize(new Dimension(0, 30));
        JButton btnTraCuu = ThemeColors.createSecondaryButton("Gợi ý", IconHelper.getIcon(IconHelper.SEARCH, 12, ThemeColors.TEXT_SECONDARY));
        btnTraCuu.setPreferredSize(new Dimension(75, 30));
        btnTraCuu.addActionListener(e -> traCuuKhachHangNhanh());
        pnlSdt.add(txtSdt, BorderLayout.CENTER);
        pnlSdt.add(btnTraCuu, BorderLayout.EAST);
        form.add(pnlSdt, gbc);

        // Tên thú cưng
        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Tên bé cưng:"), gbc);
        gbc.gridx = 1;
        txtTenThuCung = new JTextField("Miu Miu");
        txtTenThuCung.setBorder(ThemeColors.createInputBorder());
        txtTenThuCung.setPreferredSize(new Dimension(0, 30));
        form.add(txtTenThuCung, gbc);

        // Giống loài
        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("Giống loài:"), gbc);
        gbc.gridx = 1;
        cboGiongLoai = new JComboBox<>(new String[]{
                "Mèo Anh Lông Ngắn", "Mèo Ba Tư", "Mèo Xiêm",
                "Chó Poodle", "Chó Corgi", "Chó Pomeranian", "Chó Golden Retriever", "Khác"
        });
        cboGiongLoai.setPreferredSize(new Dimension(0, 30));
        form.add(cboGiongLoai, gbc);

        // Cân nặng & Badge Size
        gbc.gridx = 0; gbc.gridy = 4;
        form.add(new JLabel("Cân nặng (kg):"), gbc);
        gbc.gridx = 1;
        JPanel pnlWeight = new JPanel(new BorderLayout(8, 0));
        pnlWeight.setOpaque(false);
        spnCanNang = new JSpinner(new SpinnerNumberModel(4.2, 0.5, 60.0, 0.5));
        spnCanNang.setPreferredSize(new Dimension(90, 30));
        spnCanNang.addChangeListener(e -> capNhatSizeBadge());

        lblSizeBadge = new JLabel("Size Nhỏ (< 5kg)", SwingConstants.CENTER);
        lblSizeBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSizeBadge.setOpaque(true);
        lblSizeBadge.setBackground(new Color(0xE0, 0xF2, 0xFE));
        lblSizeBadge.setForeground(new Color(0x02, 0x84, 0xC7));
        lblSizeBadge.setBorder(new EmptyBorder(4, 8, 4, 8));

        pnlWeight.add(spnCanNang, BorderLayout.WEST);
        pnlWeight.add(lblSizeBadge, BorderLayout.CENTER);
        form.add(pnlWeight, gbc);

        // Giới tính
        gbc.gridx = 0; gbc.gridy = 5;
        form.add(new JLabel("Giới tính:"), gbc);
        gbc.gridx = 1;
        JPanel pnlGender = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlGender.setOpaque(false);
        radDuc = new JRadioButton("Đực", true);
        radCai = new JRadioButton("Cái");
        radDuc.setOpaque(false);
        radCai.setOpaque(false);
        ButtonGroup bgGender = new ButtonGroup();
        bgGender.add(radDuc);
        bgGender.add(radCai);
        pnlGender.add(radDuc);
        pnlGender.add(radCai);
        form.add(pnlGender, gbc);

        // Ghi chú thú cưng
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Ghi chú bé:"), gbc);
        gbc.gridx = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtGhiChuThuCung = new JTextArea("Bé ngoan, sấy khô kỹ vùng tai", 4, 15);
        txtGhiChuThuCung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChuThuCung.setLineWrap(true);
        txtGhiChuThuCung.setWrapStyleWord(true);
        txtGhiChuThuCung.setBorder(ThemeColors.createInputBorder());
        JScrollPane scrGhiChu = new JScrollPane(txtGhiChuThuCung);
        scrGhiChu.setPreferredSize(new Dimension(0, 80));
        form.add(scrGhiChu, gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel taoCotPhaiDichVuVaKhungGio() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(ThemeColors.SURFACE);
        card.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        // Title
        JLabel lblSec = new JLabel("DỊCH VỤ, KTV & KHUNG GIỜ HẸN", IconHelper.getPrimaryIcon(IconHelper.SPARKLES, 16), SwingConstants.LEFT);
        lblSec.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSec.setForeground(ThemeColors.TEXT_PRIMARY);
        card.add(lblSec, BorderLayout.NORTH);

        JPanel pnlContent = new JPanel();
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));
        pnlContent.setOpaque(false);

        // 1. Dịch vụ Spa & Grooming thuần túy
        pnlContent.add(new JLabel("Gói Dịch vụ Chăm sóc:"));
        pnlContent.add(Box.createVerticalStrut(3));
        cboDichVu = new JComboBox<>(new String[]{
                "DV001 - Tắm sấy khử mùi & Vệ sinh tai móng (150,000 đ - 45p)",
                "DV002 - Cắt tỉa lông tạo kiểu trọn gói (300,000 đ - 60p)",
                "DV003 - Spa ngâm bồn sục Ozone thư giãn (250,000 đ - 60p)",
                "DV004 - Tắm dưỡng thảo dược & Trị ve rận (200,000 đ - 45p)",
                "DV005 - Nhuộm lông nghệ thuật 2 tai & đuôi (180,000 đ - 45p)",
                "DV006 - Khách sạn thú cưng lưu trú 24h (180,000 đ)"
        });
        cboDichVu.setPreferredSize(new Dimension(360, 30));
        cboDichVu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cboDichVu.addActionListener(e -> {
            if (cboDichVu.getSelectedItem() != null && cboDichVu.getSelectedItem().toString().contains("Khách sạn")) {
                JOptionPane.showMessageDialog(this,
                        "Chức năng Khách sạn thú cưng đang được hoàn thiện trong giai đoạn tiếp theo!",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                cboDichVu.setSelectedIndex(0);
            }
        });
        pnlContent.add(cboDichVu);

        // 2. Kỹ thuật viên (NVCS)
        pnlContent.add(Box.createVerticalStrut(8));
        pnlContent.add(new JLabel("Kỹ thuật viên chăm sóc (NVCS):"));
        pnlContent.add(Box.createVerticalStrut(3));
        cboKtv = new JComboBox<>(new String[]{
                "NV003 - Tô Thanh Đạt (Chuyên Cắt tỉa & Grooming)",
                "NV005 - Võ Thị Hồng Ngọc (Chuyên Spa & Tắm)",
                "Chờ phân công ca trực sau"
        });
        cboKtv.setPreferredSize(new Dimension(360, 30));
        cboKtv.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cboKtv.addActionListener(e -> capNhatSlotAvailability());
        pnlContent.add(cboKtv);

        // 3. Mini Calendar Picker
        pnlContent.add(Box.createVerticalStrut(10));
        JPanel pnlDateHeader = new JPanel(new BorderLayout());
        pnlDateHeader.setOpaque(false);
        pnlDateHeader.add(new JLabel("Ngày hẹn:"), BorderLayout.WEST);

        lblNgayHienTai = new JLabel(formatDateDisplay(selectedDate));
        lblNgayHienTai.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNgayHienTai.setForeground(ThemeColors.PRIMARY);
        pnlDateHeader.add(lblNgayHienTai, BorderLayout.EAST);
        pnlContent.add(pnlDateHeader);

        pnlContent.add(Box.createVerticalStrut(4));
        JPanel pnlQuickDates = new JPanel(new GridLayout(1, 3, 6, 0));
        pnlQuickDates.setOpaque(false);
        pnlQuickDates.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JButton btnHomNay = taoDateChipButton("Hôm nay", 0, true);
        JButton btnNgayMai = taoDateChipButton("Ngày mai", 1, false);
        JButton btnNgayKia = taoDateChipButton("Ngày kia", 2, false);

        pnlQuickDates.add(btnHomNay);
        pnlQuickDates.add(btnNgayMai);
        pnlQuickDates.add(btnNgayKia);
        pnlContent.add(pnlQuickDates);

        // 4. Interactive Time Slot Badges
        pnlContent.add(Box.createVerticalStrut(10));
        pnlContent.add(new JLabel("Khung giờ Sáng (08:30 - 11:30):"));
        pnlContent.add(Box.createVerticalStrut(3));
        JPanel pnlMorningSlots = new JPanel(new GridLayout(1, 4, 6, 0));
        pnlMorningSlots.setOpaque(false);
        pnlMorningSlots.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        for (String slot : morningSlots) {
            JButton btn = taoTimeSlotButton(slot);
            slotButtons.put(slot, btn);
            pnlMorningSlots.add(btn);
        }
        pnlContent.add(pnlMorningSlots);

        pnlContent.add(Box.createVerticalStrut(8));
        pnlContent.add(new JLabel("Khung giờ Chiều & Tối (13:30 - 19:00):"));
        pnlContent.add(Box.createVerticalStrut(3));
        JPanel pnlAfternoonSlots = new JPanel(new GridLayout(2, 3, 6, 6));
        pnlAfternoonSlots.setOpaque(false);
        pnlAfternoonSlots.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        for (String slot : afternoonSlots) {
            JButton btn = taoTimeSlotButton(slot);
            slotButtons.put(slot, btn);
            pnlAfternoonSlots.add(btn);
        }
        pnlContent.add(pnlAfternoonSlots);

        // 5. Tùy chọn cộng thêm (Add-ons)
        pnlContent.add(Box.createVerticalStrut(10));
        JPanel pnlAddons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlAddons.setOpaque(false);
        chkCaoVoi = new JCheckBox("+ Cạo vôi răng nhẹ (+50k)");
        chkCaoVoi.setOpaque(false);
        chkXitDuong = new JCheckBox("+ Xịt dưỡng hương đào (+30k)");
        chkXitDuong.setOpaque(false);
        pnlAddons.add(chkCaoVoi);
        pnlAddons.add(chkXitDuong);
        pnlContent.add(pnlAddons);

        card.add(pnlContent, BorderLayout.CENTER);
        return card;
    }

    private JButton taoDateChipButton(String text, int dayOffset, boolean defaultActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (defaultActive) {
            btn.setBackground(ThemeColors.PRIMARY_SOFT);
            btn.setForeground(ThemeColors.PRIMARY_ACTIVE);
            btn.setBorder(new LineBorder(ThemeColors.PRIMARY, 1, true));
        } else {
            btn.setBackground(ThemeColors.SURFACE);
            btn.setForeground(ThemeColors.TEXT_SECONDARY);
            btn.setBorder(new LineBorder(ThemeColors.BORDER, 1, true));
        }

        btn.addActionListener(e -> {
            selectedDate = LocalDate.now().plusDays(dayOffset);
            lblNgayHienTai.setText(formatDateDisplay(selectedDate));

            // Reset visual of date chips
            Container parent = btn.getParent();
            if (parent != null) {
                for (Component comp : parent.getComponents()) {
                    if (comp instanceof JButton) {
                        comp.setBackground(ThemeColors.SURFACE);
                        comp.setForeground(ThemeColors.TEXT_SECONDARY);
                        ((JButton) comp).setBorder(new LineBorder(ThemeColors.BORDER, 1, true));
                    }
                }
            }
            btn.setBackground(ThemeColors.PRIMARY_SOFT);
            btn.setForeground(ThemeColors.PRIMARY_ACTIVE);
            btn.setBorder(new LineBorder(ThemeColors.PRIMARY, 1, true));

            capNhatSlotAvailability();
        });
        return btn;
    }

    private JButton taoTimeSlotButton(String slot) {
        JButton btn = new JButton(slot);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(65, 28));

        if (slot.equals(selectedTimeSlot)) {
            btn.setBackground(ThemeColors.PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new LineBorder(ThemeColors.PRIMARY_HOVER, 1, true));
        } else {
            btn.setBackground(ThemeColors.BACKGROUND);
            btn.setForeground(ThemeColors.TEXT_PRIMARY);
            btn.setBorder(new LineBorder(ThemeColors.BORDER, 1, true));
        }

        btn.addActionListener(e -> {
            selectedTimeSlot = slot;
            capNhatTimeSlotStyle();
        });
        return btn;
    }

    private void capNhatTimeSlotStyle() {
        for (Map.Entry<String, JButton> entry : slotButtons.entrySet()) {
            String slot = entry.getKey();
            JButton btn = entry.getValue();
            if (!btn.isEnabled()) continue;

            if (slot.equals(selectedTimeSlot)) {
                btn.setBackground(ThemeColors.PRIMARY);
                btn.setForeground(Color.WHITE);
                btn.setBorder(new LineBorder(ThemeColors.PRIMARY_HOVER, 1, true));
            } else {
                btn.setBackground(ThemeColors.SURFACE);
                btn.setForeground(ThemeColors.TEXT_PRIMARY);
                btn.setBorder(new LineBorder(ThemeColors.BORDER, 1, true));
            }
        }
    }

    private void capNhatSlotAvailability() {
        String ktvSelected = (String) cboKtv.getSelectedItem();
        String maKtv = "";
        if (ktvSelected != null && ktvSelected.startsWith("NV")) {
            maKtv = ktvSelected.substring(0, 5);
        }

        List<LichHen> allHen = controller.layDanhSachLichHen();
        List<String> bookedSlots = new ArrayList<>();

        if (allHen != null && !maKtv.isEmpty()) {
            for (LichHen lh : allHen) {
                if (lh.getNgayHen() != null &&
                        lh.getNgayHen().toLocalDate().equals(selectedDate) &&
                        !lh.getTrangThaiLichHen().equalsIgnoreCase("Hủy") &&
                        !lh.getTrangThaiLichHen().equalsIgnoreCase("Đã hủy")) {

                    boolean matchKtv = (lh.getNhanVienTiepNhan() != null && lh.getNhanVienTiepNhan().getMaNhanVien().equals(maKtv));
                    if (!matchKtv && lh.getDsChiTiet() != null) {
                        for (ChiTietLichHen ct : lh.getDsChiTiet()) {
                            if (ct.getKtvChiDinh() != null && ct.getKtvChiDinh().getMaNhanVien().equals(maKtv)) {
                                matchKtv = true;
                                break;
                            }
                        }
                    }

                    if (matchKtv) {
                        String timeStr = lh.getNgayHen().format(DateTimeFormatter.ofPattern("HH:mm"));
                        bookedSlots.add(timeStr);
                    }
                }
            }
        }

        for (Map.Entry<String, JButton> entry : slotButtons.entrySet()) {
            String slot = entry.getKey();
            JButton btn = entry.getValue();

            if (bookedSlots.contains(slot)) {
                btn.setEnabled(false);
                btn.setText(slot + " (Kín)");
                btn.setBackground(new Color(0xF1, 0xF5, 0xF9));
                btn.setForeground(ThemeColors.TEXT_MUTED);
                btn.setBorder(new LineBorder(ThemeColors.BORDER_LIGHT, 1, true));
                btn.setToolTipText("Kỹ thuật viên đã kín lịch khung giờ này");
                if (slot.equals(selectedTimeSlot)) {
                    // switch to an available slot
                    for (String otherSlot : morningSlots) {
                        if (!bookedSlots.contains(otherSlot)) {
                            selectedTimeSlot = otherSlot;
                            break;
                        }
                    }
                }
            } else {
                btn.setEnabled(true);
                btn.setText(slot);
                btn.setToolTipText("Khung giờ còn trống");
            }
        }
        capNhatTimeSlotStyle();
    }

    private void capNhatSizeBadge() {
        double w = ((Number) spnCanNang.getValue()).doubleValue();
        if (w < 5.0) {
            lblSizeBadge.setText("Size Nhỏ (< 5kg)");
            lblSizeBadge.setBackground(new Color(0xE0, 0xF2, 0xFE));
            lblSizeBadge.setForeground(new Color(0x02, 0x84, 0xC7));
        } else if (w <= 10.0) {
            lblSizeBadge.setText("Size Vừa (5 - 10kg)");
            lblSizeBadge.setBackground(ThemeColors.PRIMARY_TINT);
            lblSizeBadge.setForeground(ThemeColors.PRIMARY_ACTIVE);
        } else {
            lblSizeBadge.setText("Size Lớn (> 10kg)");
            lblSizeBadge.setBackground(new Color(0xFA, 0xE8, 0xFF));
            lblSizeBadge.setForeground(new Color(0x93, 0x33, 0xEA));
        }
    }

    private void traCuuKhachHangNhanh() {
        String sdt = txtSdt.getText().trim();
        if (sdt.equals("0988111222")) {
            txtKhachHang.setText("Nguyễn Văn An");
            txtTenThuCung.setText("Miu Miu");
            cboGiongLoai.setSelectedItem("Mèo Anh Lông Ngắn");
            spnCanNang.setValue(4.2);
            capNhatSizeBadge();
            JOptionPane.showMessageDialog(this, "Đã tìm thấy thông tin khách hàng cũ!", "Tra cứu", JOptionPane.INFORMATION_MESSAGE);
        } else if (sdt.equals("0977333444")) {
            txtKhachHang.setText("Trần Thị Mai");
            txtTenThuCung.setText("Lu Lu");
            cboGiongLoai.setSelectedItem("Chó Poodle");
            spnCanNang.setValue(5.8);
            capNhatSizeBadge();
            JOptionPane.showMessageDialog(this, "Đã tìm thấy thông tin khách hàng cũ!", "Tra cứu", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Chưa có dữ liệu cho SĐT này. Tiếp tục nhập thông tin mới.", "Thông tin", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String formatDateDisplay(LocalDate date) {
        String dow = "Thứ " + (date.getDayOfWeek().getValue() + 1);
        if (date.getDayOfWeek().getValue() == 7) dow = "Chủ Nhật";
        return dow + ", " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private void xuLyLuuLichHen() {
        String hoTen = txtKhachHang.getText().trim();
        String sdt = txtSdt.getText().trim();
        String tenTc = txtTenThuCung.getText().trim();

        if (hoTen.isEmpty() || sdt.isEmpty() || tenTc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin Khách hàng, SĐT và Tên thú cưng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maLH = "LH" + System.currentTimeMillis() % 1000000;
        KhachHang kh = new KhachHang("KH" + System.currentTimeMillis() % 10000, hoTen, sdt, 0);

        String ktvSelected = (String) cboKtv.getSelectedItem();
        String maKtv = "NV003";
        String tenKtv = "Tô Thanh Đạt";
        if (ktvSelected != null && ktvSelected.startsWith("NV005")) {
            maKtv = "NV005";
            tenKtv = "Võ Thị Hồng Ngọc";
        }
        NhanVien ktv = new NhanVien(maKtv, tenKtv, "Kỹ thuật viên", "", null);

        String gioiTinh = radDuc.isSelected() ? "Đực" : "Cái";
        ThuCung tc = new ThuCung("TC" + System.currentTimeMillis() % 10000, tenTc, (String) cboGiongLoai.getSelectedItem(), null, gioiTinh);

        String dvStr = (String) cboDichVu.getSelectedItem();
        String maDv = dvStr.substring(0, 5);
        String tenDv = dvStr.substring(8, dvStr.indexOf("(")).trim();
        DichVu dv = new DichVu(maDv, tenDv, new BigDecimal("200000"), 45);

        // Giờ hẹn kết hợp
        String[] timeParts = selectedTimeSlot.split(":");
        LocalTime time = LocalTime.of(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
        LocalDateTime henDateTime = LocalDateTime.of(selectedDate, time);

        StringBuilder sbNote = new StringBuilder();
        if (!txtGhiChuThuCung.getText().trim().isEmpty()) {
            sbNote.append(txtGhiChuThuCung.getText().trim());
        }
        if (chkCaoVoi.isSelected()) {
            if (sbNote.length() > 0) sbNote.append("; ");
            sbNote.append("+ Cạo vôi răng (+50k)");
        }
        if (chkXitDuong.isSelected()) {
            if (sbNote.length() > 0) sbNote.append("; ");
            sbNote.append("+ Xịt dưỡng đào (+30k)");
        }

        LichHen lh = new LichHen(maLH, kh, ktv, henDateTime, "Đã đặt", sbNote.toString());
        lh.addChiTiet(new ChiTietLichHen("CT" + System.currentTimeMillis() % 10000, lh, tc, dv, ktv));

        boolean ok = controller.datLichHenMoi(lh);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "TIẾP NHẬN ĐẶT LỊCH HẸN THÀNH CÔNG!\n\n" +
                            "• Mã lịch hẹn: " + maLH + "\n" +
                            "• Thời gian: " + henDateTime.format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")) + "\n" +
                            "• Khách hàng: " + hoTen + " (" + sdt + ")\n" +
                            "• Thú cưng: " + tenTc + " (" + lblSizeBadge.getText() + ")\n" +
                            "• Dịch vụ: " + tenDv + "\n" +
                            "• KTV phụ trách: " + tenKtv + "\n" +
                            "• Trạng thái: Đã đặt (Chờ tiếp nhận)",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            isConfirmed = true;
            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu lịch hẹn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }
}
