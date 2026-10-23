package views;

import utils.IconHelper;
import utils.ThemeColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Hộp thoại Thiết lập Đơn vị tính Quy đổi & Bảng giá đa quy cách đóng gói.
 * Hỗ trợ bán hàng đa đơn vị tính: Lon -> Khay/Thùng, Gói -> Thùng, Kg -> Bao...
 */
public class ThietLapQuyDoiDialog extends JDialog {

    private final DecimalFormat df = new DecimalFormat("#,### đ");

    public interface OnQuyDoiUpdatedListener {
        void onQuyDoiAdded(String maSP, String tenSP, String donViMoi, int tyLe, long giaBan);
    }

    private final OnQuyDoiUpdatedListener listener;

    private JComboBox<String> cboSanPham;
    private JLabel lblDvtGoc;
    private JLabel lblGiaGoc;
    private DefaultTableModel modelQuyDoi;
    private JTable tblQuyDoi;

    private JTextField txtDonViMoi;
    private JSpinner spnTyLe;
    private JTextField txtGiaBanMoi;

    public static class QuyDoiItem {
        String maSP;
        String tenSP;
        String donVi;
        int tyLe;
        boolean laCoSo;
        long giaBan;

        public QuyDoiItem(String maSP, String tenSP, String donVi, int tyLe, boolean laCoSo, long giaBan) {
            this.maSP = maSP;
            this.tenSP = tenSP;
            this.donVi = donVi;
            this.tyLe = tyLe;
            this.laCoSo = laCoSo;
            this.giaBan = giaBan;
        }
    }

    private final List<QuyDoiItem> dsQuyDoi = new ArrayList<>();

    public ThietLapQuyDoiDialog(Frame parent, OnQuyDoiUpdatedListener listener) {
        super(parent, "THIẾT LẬP ĐƠN VỊ TÍNH QUY ĐỔI (UNIT CONVERSION)", true);
        this.listener = listener;

        setSize(780, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColors.SURFACE);

        khoiTaoDuLieuMau();
        initUI();
        capNhatBangTheoSanPham();
    }

    private void khoiTaoDuLieuMau() {
        try {
            models.dao.SanPhamDAO spDao = new models.dao.SanPhamDAO();
            List<models.entity.SanPham> dsSP = spDao.getAllSanPham();
            if (dsSP != null && !dsSP.isEmpty()) {
                for (models.entity.SanPham sp : dsSP) {
                    List<models.entity.QuyDoiDonVi> listQd = spDao.getQuyDoiBySanPham(sp.getMaSanPham());
                    for (models.entity.QuyDoiDonVi qd : listQd) {
                        dsQuyDoi.add(new QuyDoiItem(
                                sp.getMaSanPham(),
                                sp.getTenSanPham(),
                                qd.getDonViTinh() != null ? qd.getDonViTinh().getTenDonVi() : "Đơn vị",
                                qd.getTyLeQuyDoi(),
                                qd.isLaDonViCoSo(),
                                qd.getGiaBan() != null ? qd.getGiaBan().longValue() : 0L
                        ));
                    }
                }
                if (!dsQuyDoi.isEmpty()) return;
            }
        } catch (Exception ignored) {}

        dsQuyDoi.add(new QuyDoiItem("SP002", "Pate Whiskas Vị Cá Thu 85g", "Lon", 1, true, 20000));
        dsQuyDoi.add(new QuyDoiItem("SP002", "Pate Whiskas Vị Cá Thu 85g", "Lốc 6 lon", 6, false, 115000));
        dsQuyDoi.add(new QuyDoiItem("SP002", "Pate Whiskas Vị Cá Thu 85g", "Khay 12 lon", 12, false, 225000));
        dsQuyDoi.add(new QuyDoiItem("SP002", "Pate Whiskas Vị Cá Thu 85g", "Thùng 24 lon", 24, false, 440000));

        dsQuyDoi.add(new QuyDoiItem("SP001", "Hạt Royal Canin Kitten 2kg", "Gói", 1, true, 380000));
        dsQuyDoi.add(new QuyDoiItem("SP001", "Hạt Royal Canin Kitten 2kg", "Thùng (6 gói)", 6, false, 2200000));

        dsQuyDoi.add(new QuyDoiItem("SP005", "Cát vệ sinh thú cưng 5kg", "Túi", 1, true, 65000));
        dsQuyDoi.add(new QuyDoiItem("SP005", "Cát vệ sinh thú cưng 5kg", "Bao lớn (20kg)", 4, false, 240000));
    }

    private void initUI() {
        // 1. Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(ThemeColors.PRIMARY_TINT);
        pnlHeader.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER_LIGHT),
                new EmptyBorder(14, 20, 14, 20)
        ));

        JLabel lblTitle = new JLabel("THIẾT LẬP ĐƠN VỊ QUY ĐỔI & BẢNG GIÁ ĐA QUY CÁCH",
                IconHelper.getPrimaryIcon(IconHelper.SETTINGS, 20), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Hỗ trợ bán hàng đa quy cách đóng gói (Khay, Thùng, Lốc, Bao) và tự động trừ kho theo ĐVT cơ sở");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Body - Trái: Chọn SP & Thêm quy đổi mới, Phải: Bảng danh sách quy đổi
        JPanel pnlBody = new JPanel(new BorderLayout(14, 0));
        pnlBody.setBackground(ThemeColors.BACKGROUND);
        pnlBody.setBorder(new EmptyBorder(14, 16, 14, 16));

        pnlBody.add(taoPanelTrai(), BorderLayout.WEST);
        pnlBody.add(taoPanelPhai(), BorderLayout.CENTER);

        add(pnlBody, BorderLayout.CENTER);

        // 3. Footer
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        pnlFooter.setBackground(ThemeColors.SURFACE);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeColors.BORDER_LIGHT));

        JButton btnDong = ThemeColors.createSecondaryButton("Đóng lại", null);
        btnDong.addActionListener(e -> dispose());
        pnlFooter.add(btnDong);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private JPanel taoPanelTrai() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setPreferredSize(new Dimension(340, 0));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(12, 14, 12, 14));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);

        // Tiêu đề
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblSec = new JLabel("THÊM QUY CÁCH QUY ĐỔI MỚI", IconHelper.getIcon(IconHelper.PLUS, 15, ThemeColors.PRIMARY), SwingConstants.LEFT);
        lblSec.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSec.setForeground(ThemeColors.PRIMARY_ACTIVE);
        pnl.add(lblSec, gbc);

        gbc.gridwidth = 1;

        // Chọn sản phẩm
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.4;
        pnl.add(new JLabel("Chọn sản phẩm:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        cboSanPham = new JComboBox<>(new String[]{
                "SP002 - Pate Whiskas Vị Cá Thu 85g",
                "SP001 - Hạt Royal Canin Kitten 2kg",
                "SP005 - Cát vệ sinh thú cưng 5kg"
        });
        cboSanPham.setPreferredSize(new Dimension(0, 32));
        cboSanPham.addActionListener(e -> capNhatBangTheoSanPham());
        pnl.add(cboSanPham, gbc);

        // ĐVT cơ sở
        gbc.gridx = 0; gbc.gridy = 2;
        pnl.add(new JLabel("ĐVT cơ sở:"), gbc);
        gbc.gridx = 1;
        lblDvtGoc = new JLabel("Lon (1 cơ sở)", SwingConstants.LEFT);
        lblDvtGoc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDvtGoc.setForeground(ThemeColors.PRIMARY_ACTIVE);
        pnl.add(lblDvtGoc, gbc);

        // Giá cơ sở
        gbc.gridx = 0; gbc.gridy = 3;
        pnl.add(new JLabel("Giá niêm yết gốc:"), gbc);
        gbc.gridx = 1;
        lblGiaGoc = new JLabel("20,000 đ", SwingConstants.LEFT);
        lblGiaGoc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGiaGoc.setForeground(ThemeColors.TEXT_PRIMARY);
        pnl.add(lblGiaGoc, gbc);

        // Đơn vị quy đổi mới
        gbc.gridx = 0; gbc.gridy = 4;
        pnl.add(new JLabel("Tên quy cách mới:"), gbc);
        gbc.gridx = 1;
        txtDonViMoi = new JTextField();
        txtDonViMoi.setPreferredSize(new Dimension(0, 32));
        txtDonViMoi.setBorder(ThemeColors.createInputBorder());
        txtDonViMoi.putClientProperty("JTextField.placeholderText", "Ví dụ: Khay 12 lon, Thùng...");
        pnl.add(txtDonViMoi, gbc);

        // Tỷ lệ quy đổi
        gbc.gridx = 0; gbc.gridy = 5;
        pnl.add(new JLabel("Tỷ lệ quy đổi:"), gbc);
        gbc.gridx = 1;
        spnTyLe = new JSpinner(new SpinnerNumberModel(12, 2, 500, 1));
        spnTyLe.setPreferredSize(new Dimension(0, 32));
        pnl.add(spnTyLe, gbc);

        // Giá bán niêm yết
        gbc.gridx = 0; gbc.gridy = 6;
        pnl.add(new JLabel("Giá bán quy cách:"), gbc);
        gbc.gridx = 1;
        txtGiaBanMoi = new JTextField("225000");
        txtGiaBanMoi.setPreferredSize(new Dimension(0, 32));
        txtGiaBanMoi.setBorder(ThemeColors.createInputBorder());
        pnl.add(txtGiaBanMoi, gbc);

        // Nút thêm
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        JButton btnThemQuyDoi = ThemeColors.createPrimaryButton("Thêm Quy đổi này", IconHelper.getIcon(IconHelper.PLUS, 14, Color.WHITE));
        btnThemQuyDoi.addActionListener(e -> xuLyThemQuyDoi());
        pnl.add(btnThemQuyDoi, gbc);

        // Hướng dẫn
        gbc.gridx = 0; gbc.gridy = 8;
        String guide = "<html><div style='color:#64748B; font-size:11px; padding:8px; background:#F8FAFC; border-radius:6px;'>"
                + "<b>Công thức trừ kho tự động:</b><br/>"
                + "Khi khách mua <b>2 Khay</b> (tỷ lệ 12), hệ thống POS tự động trừ tồn kho <b>24 Lon</b> cơ sở từ lô hàng theo FEFO."
                + "</div></html>";
        JLabel lblGuide = new JLabel(guide);
        pnl.add(lblGuide, gbc);

        return pnl;
    }

    private JPanel taoPanelPhai() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(12, 14, 12, 14));

        JLabel lblSec = new JLabel("BẢNG CÁC QUY CÁCH ĐANG ÁP DỤNG",
                IconHelper.getIcon(IconHelper.BOXES, 15, ThemeColors.PRIMARY), SwingConstants.LEFT);
        lblSec.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSec.setForeground(ThemeColors.PRIMARY_ACTIVE);

        String[] cols = {"Đơn vị quy đổi", "Tỷ lệ ĐVT gốc", "Là ĐVT cơ sở", "Giá bán niêm yết", "Tiết kiệm ước tính"};
        modelQuyDoi = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblQuyDoi = new JTable(modelQuyDoi);
        tblQuyDoi.setRowHeight(32);
        tblQuyDoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblQuyDoi.setSelectionBackground(ThemeColors.PRIMARY_SOFT);
        tblQuyDoi.setSelectionForeground(ThemeColors.TEXT_PRIMARY);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        tblQuyDoi.getColumnModel().getColumn(1).setCellRenderer(center);
        tblQuyDoi.getColumnModel().getColumn(2).setCellRenderer(center);

        pnl.add(lblSec, BorderLayout.NORTH);
        pnl.add(new JScrollPane(tblQuyDoi), BorderLayout.CENTER);

        return pnl;
    }

    private void capNhatBangTheoSanPham() {
        modelQuyDoi.setRowCount(0);
        String sel = (String) cboSanPham.getSelectedItem();
        if (sel == null) return;
        String maSP = sel.substring(0, sel.indexOf(" - ")).trim();

        QuyDoiItem coSo = null;
        for (QuyDoiItem item : dsQuyDoi) {
            if (item.maSP.equals(maSP) && item.laCoSo) {
                coSo = item;
                break;
            }
        }

        if (coSo != null) {
            lblDvtGoc.setText(coSo.donVi + " (Tỷ lệ: 1)");
            lblGiaGoc.setText(df.format(coSo.giaBan));
        }

        for (QuyDoiItem item : dsQuyDoi) {
            if (item.maSP.equals(maSP)) {
                String tietKiem = "---";
                if (!item.laCoSo && coSo != null) {
                    long giaGocNhanTyLe = coSo.giaBan * item.tyLe;
                    long diff = giaGocNhanTyLe - item.giaBan;
                    if (diff > 0) tietKiem = "Tiết kiệm " + df.format(diff);
                }
                modelQuyDoi.addRow(new Object[]{
                        item.donVi,
                        item.tyLe,
                        item.laCoSo ? "ĐƠN VỊ CƠ SỞ" : "Quy đổi",
                        df.format(item.giaBan),
                        tietKiem
                });
            }
        }
    }

    private void xuLyThemQuyDoi() {
        String tenDvt = txtDonViMoi.getText().trim();
        if (tenDvt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên quy cách quy đổi (ví dụ: Khay 12 lon, Thùng 24 lon)!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            txtDonViMoi.requestFocus();
            return;
        }

        try {
            int tyLe = (int) spnTyLe.getValue();
            long giaBan = Long.parseLong(txtGiaBanMoi.getText().replace(",", "").replace(".", "").trim());

            String sel = (String) cboSanPham.getSelectedItem();
            String maSP = sel.substring(0, sel.indexOf(" - ")).trim();
            String tenSP = sel.substring(sel.indexOf(" - ") + 3).trim();

            try {
                models.dao.SanPhamDAO spDao = new models.dao.SanPhamDAO();
                String maDvt = "DV" + (System.currentTimeMillis() % 100000);
                models.entity.DonViTinh dvt = new models.entity.DonViTinh(maDvt, tenDvt);
                spDao.insertDonViTinh(dvt);
                models.entity.SanPham sp = new models.entity.SanPham(maSP, null, tenSP, "");
                models.entity.QuyDoiDonVi qd = new models.entity.QuyDoiDonVi(
                        "QD" + (System.currentTimeMillis() % 100000),
                        sp, dvt, tyLe, false, java.math.BigDecimal.valueOf(giaBan)
                );
                spDao.insertQuyDoi(qd);
            } catch (Exception ex) {
                System.err.println("[ThietLapQuyDoiDialog] Lỗi lưu quy đổi CSDL: " + ex.getMessage());
            }

            QuyDoiItem newItem = new QuyDoiItem(maSP, tenSP, tenDvt, tyLe, false, giaBan);
            dsQuyDoi.add(newItem);

            capNhatBangTheoSanPham();

            if (listener != null) {
                listener.onQuyDoiAdded(maSP, tenSP, tenDvt, tyLe, giaBan);
            }

            JOptionPane.showMessageDialog(this,
                    "Đã thêm quy cách đóng gói [" + tenDvt + "] thành công!\n" +
                            "• Tỷ lệ quy đổi: 1 " + tenDvt + " = " + tyLe + " " + lblDvtGoc.getText() + "\n" +
                            "• Giá bán niêm yết: " + df.format(giaBan),
                    "Thiết lập thành công", JOptionPane.INFORMATION_MESSAGE);

            txtDonViMoi.setText("");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá bán quy cách phải là số nguyên dương hợp lệ!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        }
    }
}
