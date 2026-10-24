package views;

import controllers.NhanVienController;
import models.entity.NhanVien;
import utils.IconHelper;
import utils.ThemeColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Hộp thoại cấu hình & tiếp nhận Dịch vụ Chăm sóc thú cưng chuyên nghiệp.
 * Bao gồm:
 * 1. Chọn Thú cưng & Size cân nặng (tự động điều chỉnh giá dịch vụ theo size: <5kg, 5-10kg, >10kg).
 * 2. Chỉ định Nhân viên Chăm sóc (NVCS / KTV) phụ trách dịch vụ.
 * 3. Tùy chọn dịch vụ cộng thêm (Add-ons).
 * 4. Ghi chú thể trạng / tính cách bé.
 */
public class ChonDichVuDialog extends JDialog {

    private final DecimalFormat df = new DecimalFormat("#,### đ");

    private final String maDichVu;
    private final String tenDichVu;
    private final long giaCoBan;
    private final String thoiLuong;
    private final OnDichVuSelectedListener listener;

    private JTextField txtTenThuCung;
    private JComboBox<String> cboGiongLoai;
    private JComboBox<String> cboSizeCanNang;
    private JComboBox<String> cboKtv;
    private JCheckBox chkCaoVoiRang;
    private JCheckBox chkXitDuongLong;
    private JCheckBox chkDeoNoKhuMui;
    private JTextField txtGhiChu;
    private JLabel lblTongGiaDichVu;

    public interface OnDichVuSelectedListener {
        void onDichVuSelected(String ma, String moTaChiTiet, String dvt, long tongGia, String tenKtv);
    }

    public ChonDichVuDialog(Frame parent, String maDichVu, String tenDichVu, long giaCoBan, String thoiLuong, OnDichVuSelectedListener listener) {
        super(parent, "TIẾP NHẬN DỊCH VỤ - " + tenDichVu.toUpperCase(), true);
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.giaCoBan = giaCoBan;
        this.thoiLuong = thoiLuong;
        this.listener = listener;

        setSize(520, 580);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColors.SURFACE);

        initUI();
        tinhTongTienDichVu();
    }

    private void initUI() {
        // 1. Header Banner
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(ThemeColors.PRIMARY_TINT);
        pnlHeader.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER_LIGHT),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel lblTitle = new JLabel(tenDichVu, IconHelper.getPrimaryIcon(IconHelper.SPARKLES, 20), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Mã DV: " + maDichVu + " · Giá gốc: " + df.format(giaCoBan) + " · Thời lượng: " + thoiLuong);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeColors.PRIMARY_ACTIVE);

        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Form cấu hình dịch vụ
        JPanel pnlBody = new JPanel(new GridBagLayout());
        pnlBody.setBackground(ThemeColors.SURFACE);
        pnlBody.setBorder(new EmptyBorder(14, 18, 14, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);

        // Hàng 0: Tên thú cưng
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        pnlBody.add(new JLabel("Tên bé cưng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtTenThuCung = new JTextField("Miu Miu");
        txtTenThuCung.setBorder(ThemeColors.createInputBorder());
        txtTenThuCung.setPreferredSize(new Dimension(0, 32));
        pnlBody.add(txtTenThuCung, gbc);

        // Hàng 1: Giống loài
        gbc.gridx = 0; gbc.gridy = 1;
        pnlBody.add(new JLabel("Giống loài:"), gbc);
        gbc.gridx = 1;
        cboGiongLoai = new JComboBox<>(new String[]{
                "Mèo Anh Lông Ngắn", "Mèo Ba Tư", "Mèo Ta",
                "Chó Poodle", "Chó Corgi", "Chó Pomeranian", "Chó Phốc Sóc", "Chó Golden/Labrador", "Khác"
        });
        cboGiongLoai.setPreferredSize(new Dimension(0, 32));
        pnlBody.add(cboGiongLoai, gbc);

        // Hàng 2: Cỡ cân nặng (Ảnh hưởng giá dịch vụ)
        gbc.gridx = 0; gbc.gridy = 2;
        pnlBody.add(new JLabel("Cỡ cân nặng (Size):"), gbc);
        gbc.gridx = 1;
        cboSizeCanNang = new JComboBox<>(new String[]{
                "Size S (< 5kg) - Giá chuẩn",
                "Size M (5 - 10kg) - Phụ thu +20%",
                "Size L (> 10kg) - Phụ thu +40%"
        });
        cboSizeCanNang.setPreferredSize(new Dimension(0, 32));
        cboSizeCanNang.addActionListener(e -> tinhTongTienDichVu());
        pnlBody.add(cboSizeCanNang, gbc);

        // Hàng 3: Chỉ định Kỹ thuật viên chăm sóc (NVCS)
        gbc.gridx = 0; gbc.gridy = 3;
        pnlBody.add(new JLabel("KTV phụ trách:"), gbc);
        gbc.gridx = 1;
        cboKtv = new JComboBox<>();
        cboKtv.setPreferredSize(new Dimension(0, 32));
        napDanhSachKtv();
        pnlBody.add(cboKtv, gbc);

        // Hàng 4: Dịch vụ cộng thêm (Add-ons)
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        pnlBody.add(new JLabel("Dịch vụ thêm (Add-ons):"), gbc);
        gbc.gridx = 1;

        JPanel pnlAddons = new JPanel(new GridLayout(3, 1, 0, 4));
        pnlAddons.setOpaque(false);
        chkCaoVoiRang = new JCheckBox("Cạo vôi răng nhẹ thú cưng (+50,000 đ)");
        chkXitDuongLong = new JCheckBox("Xịt dưỡng lông hương đào (+30,000 đ)");
        chkDeoNoKhuMui = new JCheckBox("Đeo nơ tai & Xịt khử mùi VIP (+20,000 đ)");

        chkCaoVoiRang.addActionListener(e -> tinhTongTienDichVu());
        chkXitDuongLong.addActionListener(e -> tinhTongTienDichVu());
        chkDeoNoKhuMui.addActionListener(e -> tinhTongTienDichVu());

        pnlAddons.add(chkCaoVoiRang);
        pnlAddons.add(chkXitDuongLong);
        pnlAddons.add(chkDeoNoKhuMui);
        pnlBody.add(pnlAddons, gbc);

        // Hàng 5: Ghi chú thể trạng
        gbc.gridx = 0; gbc.gridy = 5;
        pnlBody.add(new JLabel("Ghi chú đặc biệt:"), gbc);
        gbc.gridx = 1;
        txtGhiChu = new JTextField("Bé ngoan, sấy khô kỹ vùng tai");
        txtGhiChu.setBorder(ThemeColors.createInputBorder());
        txtGhiChu.setPreferredSize(new Dimension(0, 32));
        pnlBody.add(txtGhiChu, gbc);

        // Hàng 6: Tổng giá dịch vụ sau điều chỉnh
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.insets = new Insets(12, 4, 4, 4);
        JLabel lblGiaTitle = new JLabel("TỔNG GIÁ DỊCH VỤ:");
        lblGiaTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGiaTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        pnlBody.add(lblGiaTitle, gbc);
        gbc.gridx = 1;
        lblTongGiaDichVu = new JLabel("0 đ", SwingConstants.RIGHT);
        lblTongGiaDichVu.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTongGiaDichVu.setForeground(ThemeColors.PRIMARY_ACTIVE);
        pnlBody.add(lblTongGiaDichVu, gbc);

        add(pnlBody, BorderLayout.CENTER);

        // 3. Footer Action Buttons
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        pnlFooter.setBackground(ThemeColors.BACKGROUND);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeColors.BORDER));

        JButton btnHuy = ThemeColors.createSecondaryButton("Hủy bỏ", null);
        JButton btnXacNhan = ThemeColors.createPrimaryButton("Xác nhận thêm vào đơn",
                IconHelper.getIcon(IconHelper.CHECK, 14, Color.WHITE));

        btnHuy.addActionListener(e -> dispose());
        btnXacNhan.addActionListener(e -> xacNhanThemDichVu());

        pnlFooter.add(btnHuy);
        pnlFooter.add(btnXacNhan);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void napDanhSachKtv() {
        NhanVienController nvc = new NhanVienController();
        List<NhanVien> list = nvc.layDanhSachKtvHoatDong();
        for (NhanVien nv : list) {
            cboKtv.addItem(nv.getMaNhanVien() + " - " + nv.getHoTen() + " (" + nv.getChucVu() + ")");
        }
        if (cboKtv.getItemCount() == 0) {
            cboKtv.addItem("NV003 - Tô Thanh Đạt (KTV Grooming)");
            cboKtv.addItem("NV005 - Võ Thị Hồng Ngọc (Chuyên viên Spa)");
        }
    }

    private long tinhTongTienDichVu() {
        double heSoSize = 1.0;
        int sizeIndex = cboSizeCanNang.getSelectedIndex();
        if (sizeIndex == 1) heSoSize = 1.2; // Size M +20%
        else if (sizeIndex == 2) heSoSize = 1.4; // Size L +40%

        long giaTheoSize = Math.round(giaCoBan * heSoSize);

        long giaAddons = 0;
        if (chkCaoVoiRang.isSelected()) giaAddons += 50000;
        if (chkXitDuongLong.isSelected()) giaAddons += 30000;
        if (chkDeoNoKhuMui.isSelected()) giaAddons += 20000;

        long tong = giaTheoSize + giaAddons;
        lblTongGiaDichVu.setText(df.format(tong));
        return tong;
    }

    private void xacNhanThemDichVu() {
        String tenBé = txtTenThuCung.getText().trim();
        if (tenBé.isEmpty()) tenBé = "Bé cưng";

        String ktvFull = (String) cboKtv.getSelectedItem();
        String tenKtv = ktvFull != null ? ktvFull.substring(ktvFull.indexOf("-") + 1, ktvFull.indexOf("(")).trim() : "KTV";

        String sizeText = "Size S";
        if (cboSizeCanNang.getSelectedIndex() == 1) sizeText = "Size M (5-10kg)";
        else if (cboSizeCanNang.getSelectedIndex() == 2) sizeText = "Size L (>10kg)";

        long tongGia = tinhTongTienDichVu();

        // Định dạng mô tả chi tiết chuyên nghiệp trên hóa đơn
        String moTaChiTiet = String.format("%s (%s · %s · %s)", tenDichVu, tenBé, tenKtv, sizeText);

        if (listener != null) {
            listener.onDichVuSelected(maDichVu, moTaChiTiet, thoiLuong, tongGia, tenKtv);
        }
        dispose();
    }
}
