package views;

import utils.IconHelper;
import utils.ThemeColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
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
        void onNhapHang(String maSP, String tenSP, String danhMuc, String dvtCoSo,
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
        capNhatDanhGiaFefo();
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
        txtMaSP = new JTextField("SP" + (System.currentTimeMillis() % 1000));
        txtMaSP.setPreferredSize(new Dimension(0, 32));
        txtMaSP.setBorder(ThemeColors.createInputBorder());
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

        // Số lô
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        card.add(new JLabel("Số lô hàng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtSoLo = new JTextField("LO_2026_" + (System.currentTimeMillis() % 1000));
        txtSoLo.setPreferredSize(new Dimension(0, 32));
        txtSoLo.setBorder(ThemeColors.createInputBorder());
        card.add(txtSoLo, gbc);

        // Ngày sản xuất
        gbc.gridx = 0; gbc.gridy = 2;
        card.add(new JLabel("Ngày sản xuất (NSX):"), gbc);
        gbc.gridx = 1;
        txtNgaySX = new JTextField(LocalDate.now().minusMonths(1).format(dtf));
        txtNgaySX.setPreferredSize(new Dimension(0, 32));
        txtNgaySX.setBorder(ThemeColors.createInputBorder());
        card.add(txtNgaySX, gbc);

        // Hạn sử dụng
        gbc.gridx = 0; gbc.gridy = 3;
        card.add(new JLabel("Hạn sử dụng (HSD):"), gbc);
        gbc.gridx = 1;
        txtHanSD = new JTextField(LocalDate.now().plusMonths(8).format(dtf));
        txtHanSD.setPreferredSize(new Dimension(0, 32));
        txtHanSD.setBorder(ThemeColors.createInputBorder());
        txtHanSD.addActionListener(e -> capNhatDanhGiaFefo());
        txtHanSD.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                capNhatDanhGiaFefo();
            }
        });
        card.add(txtHanSD, gbc);

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
            LocalDate hsd = LocalDate.parse(txtHanSD.getText().trim(), dtf);
            LocalDate now = LocalDate.now();
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

            String maSP = txtMaSP.getText().trim();
            String danhMuc = (String) cboDanhMuc.getSelectedItem();
            String ncc = (String) cboNhaCungCap.getSelectedItem();
            String dvt = (String) cboDvtCoSo.getSelectedItem();
            String soLo = txtSoLo.getText().trim();
            String nsx = txtNgaySX.getText().trim();
            String hsd = txtHanSD.getText().trim();
            String fefo = lblFefoBadge.getText();

            if (listener != null) {
                listener.onNhapHang(maSP, ten, danhMuc, dvt, soLo, ncc, nsx, hsd, giaNhap, giaBan, sl, fefo);
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
        }
    }
}
