package gui;

import utils.IconHelper;
import utils.ThemeColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Hộp thoại Thêm Sản phẩm & Nhập Lô hàng Mới (FEFO Strategy).
 * Thiết kế chuẩn FlatLaf: Đánh giá hạn dùng tự động theo 3 cấp độ màu (Xanh lá, Vàng, Đỏ).
 */
public class NhapSanPhamVaLoHangDialog extends JDialog {

    private final DecimalFormat df = new DecimalFormat("#,### đ");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public interface OnNhapHangThanhCongListener {
        boolean onNhapHang(String maSP, String tenSP, String danhMuc, String dvtCoSo,
                        String soLo, String ncc, String nsx, String hsd,
                        long giaNhap, long giaBan, int soLuongNhap, String trangThaiFefo);
    }

    private final OnNhapHangThanhCongListener listener;

    private JTextField txtMaSP;
    private JTextField txtTenSP;
    private JComboBox<String> cboDanhMuc;
    private JComboBox<String> cboNhaCungCap;
    private JComboBox<String> cboDvtCoSo;
    private JTextField txtGiaNhap;
    private JTextField txtGiaBan;

    private JTextField txtSoLo;
    private JTextField txtNgaySX;
    private JTextField txtHanSD;
    private JSpinner spnSoLuongNhap;
    private JLabel lblFefoBadge;

    public NhapSanPhamVaLoHangDialog(Frame parent, OnNhapHangThanhCongListener listener) {
        super(parent, "NHẬP SẢN PHẨM & LÔ HÀNG MỚI (FEFO)", true);
        this.listener = listener;

        setSize(780, 580);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColors.SURFACE);

        initUI();
        napDanhMucTuCoSoDuLieu();
        capNhatDanhGiaFefo();
    }

    private void napDanhMucTuCoSoDuLieu() {
        bus.KhoLoHangBus khoBus = new bus.KhoLoHangBus();
        java.util.List<entity.DanhMucHang> danhMuc = khoBus.layDanhSachDanhMuc();
        java.util.List<entity.NhaCungCap> nhaCungCap = khoBus.layDanhSachNhaCungCap();
        java.util.List<entity.DonViTinh> donViTinh = khoBus.layDanhSachDonViTinh();
        if (!danhMuc.isEmpty()) {
            cboDanhMuc.setModel(new DefaultComboBoxModel<>(danhMuc.stream()
                    .map(entity.DanhMucHang::getTenDanhMuc).toArray(String[]::new)));
        }
        if (!nhaCungCap.isEmpty()) {
            cboNhaCungCap.setModel(new DefaultComboBoxModel<>(nhaCungCap.stream()
                    .map(entity.NhaCungCap::getTenNhaCungCap).toArray(String[]::new)));
        }
        if (!donViTinh.isEmpty()) {
            cboDvtCoSo.setModel(new DefaultComboBoxModel<>(donViTinh.stream()
                    .map(entity.DonViTinh::getTenDonVi).toArray(String[]::new)));
        }
    }

    private void initUI() {
        // 1. Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(ThemeColors.PRIMARY_TINT);
        pnlHeader.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER_LIGHT),
                new EmptyBorder(14, 20, 14, 20)
        ));

        JLabel lblTitle = new JLabel("THÊM SẢN PHẨM & NHẬP LÔ HÀNG MỚI",
                IconHelper.getPrimaryIcon(IconHelper.BOXES, 20), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Quản lý thông tin nguồn gốc, hạn dùng và phân loại xuất kho theo chuẩn FEFO");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Body - 2 Khối Card song song (Trái: Sản phẩm, Phải: Lô hàng & FEFO)
        JPanel pnlBody = new JPanel(new GridLayout(1, 2, 14, 0));
        pnlBody.setBackground(ThemeColors.BACKGROUND);
        pnlBody.setBorder(new EmptyBorder(14, 16, 14, 16));

        pnlBody.add(taoCardSanPham());
        pnlBody.add(taoCardLoHang());

        add(pnlBody, BorderLayout.CENTER);

        // 3. Footer Buttons
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        pnlFooter.setBackground(ThemeColors.SURFACE);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeColors.BORDER_LIGHT));

        JButton btnHuy = ThemeColors.createSecondaryButton("Hủy bỏ", null);
        JButton btnLuu = ThemeColors.createPrimaryButton("Lưu & Nhập kho", IconHelper.getIcon(IconHelper.CHECK, 14, Color.WHITE));

        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> xuLyLuuKho());

        pnlFooter.add(btnHuy);
        pnlFooter.add(btnLuu);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private JPanel taoCardSanPham() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(ThemeColors.SURFACE);
        card.setBorder(ThemeColors.createCardBorder(12, 14, 12, 14));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);

        // Tiêu đề card
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblSecTitle = new JLabel("1. THÔNG TIN SẢN PHẨM & ĐƠN VỊ CƠ SỞ",
                IconHelper.getIcon(IconHelper.TAG, 16, ThemeColors.PRIMARY), SwingConstants.LEFT);
        lblSecTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSecTitle.setForeground(ThemeColors.PRIMARY_ACTIVE);
        card.add(lblSecTitle, gbc);

        gbc.gridwidth = 1;

        // Mã SP & Tên SP
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        card.add(new JLabel("Mã sản phẩm:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
     //  Tự động lấy mã tiếp theo từ Database
        txtMaSP = new JTextField(utils.MaTuSinhHelper.sinhMaSanPham());
        txtMaSP.setPreferredSize(new Dimension(0, 32));
        txtMaSP.setBorder(ThemeColors.createInputBorder());

        //  KHÓA CỨNG không cho người dùng tự gõ tay sửa mã (tránh trùng khóa chính)
        txtMaSP.setEditable(false);
        txtMaSP.setFocusable(false);
        txtMaSP.setBackground(new Color(241, 245, 249)); // Màu xám nhạt báo hiệu 
        card.add(txtMaSP, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        card.add(new JLabel("Tên sản phẩm:"), gbc);
        gbc.gridx = 1;
        txtTenSP = new JTextField();
        txtTenSP.setPreferredSize(new Dimension(0, 32));
        txtTenSP.setBorder(ThemeColors.createInputBorder());
        card.add(txtTenSP, gbc);

        // Danh mục
        gbc.gridx = 0; gbc.gridy = 3;
        card.add(new JLabel("Danh mục hàng:"), gbc);
        gbc.gridx = 1;
        cboDanhMuc = new JComboBox<>(new String[]{
                "Thức ăn thú cưng (Hạt, Pate)",
                "Cát vệ sinh & Khử mùi",
                "Thuốc thú y & Dinh dưỡng",
                "Phụ kiện & Đồ chơi thú cưng"
        });
        cboDanhMuc.setPreferredSize(new Dimension(0, 32));
        card.add(cboDanhMuc, gbc);

        // Nhà cung cấp
        gbc.gridx = 0; gbc.gridy = 4;
        card.add(new JLabel("Nhà cung cấp:"), gbc);
        gbc.gridx = 1;
        cboNhaCungCap = new JComboBox<>(new String[]{
                "Công ty TNHH Pet Food VN",
                "Nhà phân phối Royal Canin VN",
                "Công ty TNHH SmartHeart VN",
                "Xưởng Phụ kiện Thú cưng Sài Gòn"
        });
        cboNhaCungCap.setPreferredSize(new Dimension(0, 32));
        card.add(cboNhaCungCap, gbc);

        // Đơn vị tính cơ sở
        gbc.gridx = 0; gbc.gridy = 5;
        card.add(new JLabel("ĐVT cơ sở:"), gbc);
        gbc.gridx = 1;
        cboDvtCoSo = new JComboBox<>(new String[]{"Lon", "Gói", "Cái", "Chai", "Hộp", "Bao", "Kg"});
        cboDvtCoSo.setPreferredSize(new Dimension(0, 32));
        card.add(cboDvtCoSo, gbc);

        // Giá nhập & Giá bán niêm yết
        gbc.gridx = 0; gbc.gridy = 6;
        card.add(new JLabel("Giá nhập cơ sở:"), gbc);
        gbc.gridx = 1;
        txtGiaNhap = new JTextField("25000");
        txtGiaNhap.setPreferredSize(new Dimension(0, 32));
        txtGiaNhap.setBorder(ThemeColors.createInputBorder());
        card.add(txtGiaNhap, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        card.add(new JLabel("Giá bán lẻ cơ sở:"), gbc);
        gbc.gridx = 1;
        txtGiaBan = new JTextField("38000");
        txtGiaBan.setPreferredSize(new Dimension(0, 32));
        txtGiaBan.setBorder(ThemeColors.createInputBorder());
        card.add(txtGiaBan, gbc);

        return card;
    }

    private JPanel taoCardLoHang() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(ThemeColors.SURFACE);
        card.setBorder(ThemeColors.createCardBorder(12, 14, 12, 14));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);

        // Tiêu đề card
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblSecTitle = new JLabel("2. THÔNG TIN LÔ HÀNG & HẠN DÙNG (FEFO)",
                IconHelper.getIcon(IconHelper.CLOCK, 16, ThemeColors.PRIMARY), SwingConstants.LEFT);
        lblSecTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSecTitle.setForeground(ThemeColors.PRIMARY_ACTIVE);
        card.add(lblSecTitle, gbc);

        gbc.gridwidth = 1;

        // Số lô (Tự động sinh & khóa chỉnh sửa theo quy chuẩn mã tự tăng)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        card.add(new JLabel("Số lô hàng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtSoLo = new JTextField(utils.MaTuSinhHelper.sinhSoLo());
        txtSoLo.setPreferredSize(new Dimension(0, 32));
        txtSoLo.setBorder(ThemeColors.createInputBorder());
        txtSoLo.setEditable(false);
        txtSoLo.setFocusable(false);
        txtSoLo.setBackground(new Color(241, 245, 249));
        txtSoLo.setToolTipText("Số lô tự động sinh và tăng dần theo ngày nhập kho (LO-yyMMdd-xxx).");
        card.add(txtSoLo, gbc);

        // Ngày sản xuất (Kèm nút chọn lịch)
        gbc.gridx = 0; gbc.gridy = 2;
        card.add(new JLabel("Ngày sản xuất (NSX):"), gbc);
        gbc.gridx = 1;
        txtNgaySX = new JTextField(LocalDate.now().minusMonths(1).format(dtf));
        txtNgaySX.setEditable(false);
        txtNgaySX.setBackground(Color.WHITE);
        txtNgaySX.setBorder(ThemeColors.createInputBorder());
        txtNgaySX.setPreferredSize(new Dimension(0, 32));

        JButton btnChonNSX = new JButton(IconHelper.getIcon(IconHelper.CALENDAR, 14, ThemeColors.PRIMARY_ACTIVE));
        btnChonNSX.setPreferredSize(new Dimension(34, 32));
        btnChonNSX.setFocusPainted(false);
        btnChonNSX.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChonNSX.addActionListener(e -> hienThiCalendarPopup(btnChonNSX, txtNgaySX, this::capNhatDanhGiaFefo));
        JPanel pnlNgaySX = new JPanel(new BorderLayout(4, 0));
        pnlNgaySX.setOpaque(false);
        pnlNgaySX.add(txtNgaySX, BorderLayout.CENTER);
        pnlNgaySX.add(btnChonNSX, BorderLayout.EAST);
        card.add(pnlNgaySX, gbc);

        // Hạn sử dụng (Kèm nút chọn lịch)
        gbc.gridx = 0; gbc.gridy = 3;
        card.add(new JLabel("Hạn sử dụng (HSD):"), gbc);
        gbc.gridx = 1;
        txtHanSD = new JTextField(LocalDate.now().plusMonths(8).format(dtf));
        txtHanSD.setEditable(false);
        txtHanSD.setBackground(Color.WHITE);
        txtHanSD.setBorder(ThemeColors.createInputBorder());
        txtHanSD.setPreferredSize(new Dimension(0, 32));

        JButton btnChonHSD = new JButton(IconHelper.getIcon(IconHelper.CALENDAR, 14, ThemeColors.PRIMARY_ACTIVE));
        btnChonHSD.setPreferredSize(new Dimension(34, 32));
        btnChonHSD.setFocusPainted(false);
        btnChonHSD.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChonHSD.addActionListener(e -> hienThiCalendarPopup(btnChonHSD, txtHanSD, this::capNhatDanhGiaFefo));

        JPanel pnlHanSD = new JPanel(new BorderLayout(4, 0));
        pnlHanSD.setOpaque(false);
        pnlHanSD.add(txtHanSD, BorderLayout.CENTER);
        pnlHanSD.add(btnChonHSD, BorderLayout.EAST);
        card.add(pnlHanSD, gbc);
        // Đánh giá FEFO Live
        gbc.gridx = 0; gbc.gridy = 4;
        card.add(new JLabel("Đánh giá FEFO:"), gbc);
        gbc.gridx = 1;
        lblFefoBadge = ThemeColors.createBadge("Đang tính toán...", ThemeColors.SUCCESS_LIGHT, ThemeColors.SUCCESS_TEXT);
        lblFefoBadge.setPreferredSize(new Dimension(0, 30));
        card.add(lblFefoBadge, gbc);

        // Số lượng nhập
        gbc.gridx = 0; gbc.gridy = 5;
        card.add(new JLabel("Số lượng nhập:"), gbc);
        gbc.gridx = 1;
        spnSoLuongNhap = new JSpinner(new SpinnerNumberModel(50, 1, 10000, 5));
        spnSoLuongNhap.setPreferredSize(new Dimension(0, 32));
        card.add(spnSoLuongNhap, gbc);

        // Hướng dẫn FEFO
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        String guide = "<html><div style='color:#64748B; font-size:11px; padding:6px; background:#F8FAFC; border-radius:6px;'>"
                + "<b>Quy tắc xuất kho FEFO (First Expired, First Out):</b><br/>"
                + "• <font color='#10B981'><b>Xanh lá</b></font>: HSD an toàn (> 6 tháng)<br/>"
                + "• <font color='#F59E0B'><b>Vàng cam</b></font>: Cận date (1 - 3 tháng) - Xuất ưu tiên<br/>"
                + "• <font color='#F43F5E'><b>Đỏ báo động</b></font>: Cận date nguy cấp (&lt; 30 ngày) hoặc hết hạn"
                + "</div></html>";
        JLabel lblGuide = new JLabel(guide);
        card.add(lblGuide, gbc);

        return card;
    }

    private void capNhatDanhGiaFefo() {
        try {
            LocalDate nsx = LocalDate.parse(txtNgaySX.getText().trim(), dtf);
            LocalDate hsd = LocalDate.parse(txtHanSD.getText().trim(), dtf);
            LocalDate now = LocalDate.now();

            // Nếu HSD trước hoặc trùng NSX -> Báo lỗi logic ngay trên Badge
            if (!hsd.isAfter(nsx)) {
                lblFefoBadge.setText("LỖI: HSD PHẢI SAU NGÀY SX");
                lblFefoBadge.setBackground(ThemeColors.DANGER_BG);
                lblFefoBadge.setForeground(ThemeColors.DANGER_TEXT);
                return;
            }

            long days = ChronoUnit.DAYS.between(now, hsd);

            if (days <= 0) {
                lblFefoBadge.setText("ĐÃ HẾT HẠN SỬ DỤNG");
                lblFefoBadge.setBackground(ThemeColors.DANGER_BG);
                lblFefoBadge.setForeground(ThemeColors.DANGER_TEXT);
            } else if (days <= 30) {
                lblFefoBadge.setText("CẬN DATE NGUY CẤP (" + days + " ngày) - CẦN THANH LÝ");
                lblFefoBadge.setBackground(ThemeColors.DANGER_BG);
                lblFefoBadge.setForeground(ThemeColors.DANGER_TEXT);
            } else if (days <= 90) {
                lblFefoBadge.setText("CẬN DATE (" + (days / 30) + " tháng) - ƯU TIÊN XUẤT SỚM");
                lblFefoBadge.setBackground(ThemeColors.WARNING_BG);
                lblFefoBadge.setForeground(ThemeColors.WARNING_TEXT);
            } else {
                lblFefoBadge.setText("HẠN DÙNG AN TOÀN (" + (days / 30) + " tháng)");
                lblFefoBadge.setBackground(ThemeColors.SUCCESS_BG);
                lblFefoBadge.setForeground(ThemeColors.SUCCESS_TEXT);
            }
        } catch (Exception e) {
            lblFefoBadge.setText("Định dạng ngày: dd/MM/yyyy");
            lblFefoBadge.setBackground(ThemeColors.PRIMARY_TINT);
            lblFefoBadge.setForeground(ThemeColors.TEXT_MUTED);
        }
    }

    private void xuLyLuuKho() {
        String ten = txtTenSP.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên sản phẩm!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            txtTenSP.requestFocus();
            return;
        }

        try {
            long giaNhap = Long.parseLong(txtGiaNhap.getText().replace(",", "").replace(".", "").trim());
            long giaBan = Long.parseLong(txtGiaBan.getText().replace(",", "").replace(".", "").trim());
            int sl = (int) spnSoLuongNhap.getValue();

            if (giaNhap < 0 || giaBan < 0 || sl <= 0) {
                throw new IllegalArgumentException("Giá nhập, giá bán không được âm và số lượng phải lớn hơn 0");
            }

            String maSP = txtMaSP.getText().trim();
            String danhMuc = (String) cboDanhMuc.getSelectedItem();
            String ncc = (String) cboNhaCungCap.getSelectedItem();
            String dvt = (String) cboDvtCoSo.getSelectedItem();
            String soLo = txtSoLo.getText().trim();
            String nsx = txtNgaySX.getText().trim();
            String hsd = txtHanSD.getText().trim();

            //  Kiểm tra logic HSD phải sau NSX
            LocalDate dNgaySX = LocalDate.parse(nsx, dtf);
            LocalDate dHanSD = LocalDate.parse(hsd, dtf);
            if (!dHanSD.isAfter(dNgaySX)) {
                JOptionPane.showMessageDialog(this, 
                        "Lỗi logic thời gian:\nHạn sử dụng (" + hsd + ") phải diễn ra SAU Ngày sản xuất (" + nsx + ")!", 
                        "Lỗi ngày tháng", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Kiểm tra giá bán không được thấp hơn giá nhập
            if (giaBan < giaNhap) {
                JOptionPane.showMessageDialog(this, 
                        "Giá bán lẻ cơ sở không được nhỏ hơn giá nhập cơ sở!", 
                        "Lỗi nghiệp vụ giá", JOptionPane.WARNING_MESSAGE);
                txtGiaBan.requestFocus();
                return;
            }

            String fefo = lblFefoBadge.getText();

            boolean daLuu = listener != null
                    && listener.onNhapHang(maSP, ten, danhMuc, dvt, soLo, ncc, nsx, hsd,
                            giaNhap, giaBan, sl, fefo);
            if (!daLuu) {
                JOptionPane.showMessageDialog(this,
                        "Không thể lưu sản phẩm/lô hàng vào CSDL. Không có thông báo thành công giả.\n"
                        + "Vui lòng kiểm tra kết nối và dữ liệu danh mục rồi thử lại.",
                        "Nhập kho thất bại", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this,
                    "Nhập kho sản phẩm & lô hàng thành công!\n" +
                            "• Sản phẩm: " + ten + " [" + maSP + "]\n" +
                            "• Lô: " + soLo + " (" + sl + " " + dvt + ")\n" +
                            "• Trạng thái FEFO: " + fefo,
                    "Nhập hàng thành công", JOptionPane.INFORMATION_MESSAGE);

            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Đơn giá nhập và giá bán phải là số hợp lệ!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi định dạng ngày tháng (dd/MM/yyyy)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Bật Popup bảng lịch chọn ngày tháng trực quan chống gõ sai

    private void hienThiCalendarPopup(JComponent invoker, JTextField targetField, Runnable onDatePicked) {
        LocalDate initialDate;
        try {
            initialDate = LocalDate.parse(targetField.getText().trim(), dtf);
        } catch (Exception ex) {
            initialDate = LocalDate.now();
        }

        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER_ACCENT, 1, true),
                new EmptyBorder(6, 6, 6, 6)
        ));
        popup.setBackground(Color.WHITE);

        final YearMonth[] currentYM = {YearMonth.from(initialDate)};
        final LocalDate currentDate = initialDate;

        JPanel pnlCalendar = new JPanel(new BorderLayout(0, 6));
        pnlCalendar.setBackground(Color.WHITE);
        pnlCalendar.setPreferredSize(new Dimension(270, 240));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JButton btnPrev = new JButton("<");
        JButton btnNext = new JButton(">");
        JLabel lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMonthYear.setForeground(ThemeColors.PRIMARY_ACTIVE);

        btnPrev.setPreferredSize(new Dimension(36, 26));
        btnNext.setPreferredSize(new Dimension(36, 26));
        btnPrev.setFocusPainted(false);
        btnNext.setFocusPainted(false);

        pnlHeader.add(btnPrev, BorderLayout.WEST);
        pnlHeader.add(lblMonthYear, BorderLayout.CENTER);
        pnlHeader.add(btnNext, BorderLayout.EAST);
        pnlCalendar.add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlDaysGrid = new JPanel(new GridLayout(7, 7, 2, 2));
        pnlDaysGrid.setOpaque(false);
        pnlCalendar.add(pnlDaysGrid, BorderLayout.CENTER);

        Runnable renderGrid = () -> {
            pnlDaysGrid.removeAll();
            lblMonthYear.setText("Tháng " + currentYM[0].getMonthValue() + " / " + currentYM[0].getYear());

            String[] dowNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
            for (String dow : dowNames) {
                JLabel l = new JLabel(dow, SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                l.setForeground(ThemeColors.TEXT_MUTED);
                pnlDaysGrid.add(l);
            }

            LocalDate firstDay = currentYM[0].atDay(1);
            int startCol = firstDay.getDayOfWeek().getValue() - 1;
            int daysInMonth = currentYM[0].lengthOfMonth();

            for (int i = 0; i < startCol; i++) {
                pnlDaysGrid.add(new JLabel(""));
            }

            for (int d = 1; d <= daysInMonth; d++) {
                int dayNum = d;
                LocalDate thisDate = currentYM[0].atDay(dayNum);
                JButton btnDay = new JButton(String.valueOf(dayNum));
                btnDay.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                btnDay.setMargin(new Insets(1, 1, 1, 1));
                btnDay.setFocusPainted(false);
                btnDay.setCursor(new Cursor(Cursor.HAND_CURSOR));

                if (thisDate.equals(currentDate)) {
                    btnDay.setBackground(ThemeColors.PRIMARY);
                    btnDay.setForeground(Color.WHITE);
                    btnDay.setFont(new Font("Segoe UI", Font.BOLD, 11));
                } else if (thisDate.equals(LocalDate.now())) {
                    btnDay.setBackground(ThemeColors.PRIMARY_TINT);
                    btnDay.setForeground(ThemeColors.PRIMARY_ACTIVE);
                } else {
                    btnDay.setBackground(Color.WHITE);
                    btnDay.setForeground(ThemeColors.TEXT_PRIMARY);
                }

                btnDay.addActionListener(ev -> {
                    targetField.setText(thisDate.format(dtf));
                    popup.setVisible(false);
                    if (onDatePicked != null) {
                        onDatePicked.run();
                    }
                });
                pnlDaysGrid.add(btnDay);
            }

            int filled = startCol + daysInMonth;
            while (filled < 42) {
                pnlDaysGrid.add(new JLabel(""));
                filled++;
            }

            pnlDaysGrid.revalidate();
            pnlDaysGrid.repaint();
        };

        btnPrev.addActionListener(ev -> {
            currentYM[0] = currentYM[0].minusMonths(1);
            renderGrid.run();
        });

        btnNext.addActionListener(ev -> {
            currentYM[0] = currentYM[0].plusMonths(1);
            renderGrid.run();
        });

        renderGrid.run();
        popup.add(pnlCalendar);
        popup.show(invoker, 0, invoker.getHeight() + 2);
    }
}
