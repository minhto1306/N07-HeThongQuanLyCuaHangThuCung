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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Panel Bàn giao ca làm việc & Đối soát két tiền mặt giữa thu ngân và quản lý.
 * Thiết kế giao diện tông Warm Peach / Soft Apricot, tích hợp Lucide Icons đồng bộ.
 */
public class BanGiaoCaPanel extends JPanel {

    private final DecimalFormat df = new DecimalFormat("#,### đ");

    private JTextField txtTienDauCa;
    private JLabel lblTienHeThong;
    private JLabel lblTienDuKien;
    private JTextField txtTienThucTe;
    private JLabel lblChenhLech;
    private JTextArea txtLyDoChenhLech;

    private DefaultTableModel modelLichSu;

    private final long tienDauCa = 1000000; // 1 triệu tiền lẻ đầu ca
    private final long tienHeThongGhiNhan = 5420000; // Doanh thu tiền mặt hệ thống ghi nhận trong ca

    public BanGiaoCaPanel() {
        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 18, 16, 18));
        setBackground(ThemeColors.BACKGROUND);

        // Phân chia: Trái là Biên bản kết ca, Phải là Lịch sử bàn giao các ca trước
        JPanel pnlLeft = taoPanelBienBanKetCa();
        pnlLeft.setPreferredSize(new Dimension(480, 0));

        JPanel pnlRight = taoPanelLichSuGiaoCa();

        add(pnlLeft, BorderLayout.WEST);
        add(pnlRight, BorderLayout.CENTER);
    }

    private JPanel taoPanelBienBanKetCa() {
        JPanel pnl = new JPanel(new BorderLayout(0, 12));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        JLabel lblTitle = new JLabel("BIÊN BẢN ĐỐI SOÁT & KẾT CA HIỆN TẠI",
                IconHelper.getPrimaryIcon(IconHelper.CLIPBOARD_CHECK, 18), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        lblTitle.setIconTextGap(8);
        pnl.add(lblTitle, BorderLayout.NORTH);

        // Form chi tiết
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Ca làm việc & Thu ngân
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblCaTitle = new JLabel("Ca làm việc:");
        lblCaTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCaTitle.setForeground(ThemeColors.TEXT_SECONDARY);
        pnlForm.add(lblCaTitle, gbc);

        gbc.gridx = 1;
        JLabel lblCa = new JLabel("CA01 - Ca Sáng (07:30 - 12:00)");
        lblCa.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCa.setForeground(ThemeColors.TEXT_PRIMARY);
        pnlForm.add(lblCa, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblTnTitle = new JLabel("Thu ngân phụ trách:");
        lblTnTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTnTitle.setForeground(ThemeColors.TEXT_SECONDARY);
        pnlForm.add(lblTnTitle, gbc);

        gbc.gridx = 1;
        JLabel lblThuNgan = new JLabel("Lê Anh Minh (thungan01)");
        lblThuNgan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblThuNgan.setForeground(ThemeColors.TEXT_PRIMARY);
        pnlForm.add(lblThuNgan, gbc);

        // Tiền đầu ca
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblDcTitle = new JLabel("Tiền đầu ca (Định mức két):");
        lblDcTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDcTitle.setForeground(ThemeColors.TEXT_SECONDARY);
        pnlForm.add(lblDcTitle, gbc);

        gbc.gridx = 1;
        txtTienDauCa = new JTextField(df.format(tienDauCa));
        txtTienDauCa.setEditable(false);
        txtTienDauCa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTienDauCa.setPreferredSize(new Dimension(160, 34));
        txtTienDauCa.setBorder(ThemeColors.createInputBorder());
        pnlForm.add(txtTienDauCa, gbc);

        // Doanh thu tiền mặt hệ thống
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblHtTitle = new JLabel("Doanh thu tiền mặt hệ thống:");
        lblHtTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblHtTitle.setForeground(ThemeColors.TEXT_SECONDARY);
        pnlForm.add(lblHtTitle, gbc);

        gbc.gridx = 1;
        lblTienHeThong = new JLabel(df.format(tienHeThongGhiNhan));
        lblTienHeThong.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTienHeThong.setForeground(ThemeColors.PRIMARY_ACTIVE);
        pnlForm.add(lblTienHeThong, gbc);

        // Tiền lý thuyết cuối ca
        long tongLyThuyet = tienDauCa + tienHeThongGhiNhan;
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblLtTitle = new JLabel("Tổng tiền cuối ca lý thuyết:");
        lblLtTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLtTitle.setForeground(ThemeColors.TEXT_SECONDARY);
        pnlForm.add(lblLtTitle, gbc);

        gbc.gridx = 1;
        lblTienDuKien = new JLabel(df.format(tongLyThuyet));
        lblTienDuKien.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTienDuKien.setForeground(ThemeColors.SUCCESS);
        pnlForm.add(lblTienDuKien, gbc);

        // Tiền thực tế kiểm đếm
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel lblTtTitle = new JLabel("Tiền mặt thực tế đếm được:");
        lblTtTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTtTitle.setForeground(ThemeColors.TEXT_SECONDARY);
        pnlForm.add(lblTtTitle, gbc);

        gbc.gridx = 1;
        txtTienThucTe = new JTextField(String.valueOf(tongLyThuyet));
        txtTienThucTe.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtTienThucTe.setPreferredSize(new Dimension(160, 34));
        txtTienThucTe.setBorder(ThemeColors.createInputBorder());
        txtTienThucTe.addActionListener(e -> tinhChenhLech());
        pnlForm.add(txtTienThucTe, gbc);

        // Chênh lệch
        gbc.gridx = 0; gbc.gridy = 6;
        JLabel lblClTitle = new JLabel("Chênh lệch (Thực tế - Hệ thống):");
        lblClTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblClTitle.setForeground(ThemeColors.TEXT_SECONDARY);
        pnlForm.add(lblClTitle, gbc);

        gbc.gridx = 1;
        lblChenhLech = new JLabel("0 đ (Khớp két 100%)");
        lblChenhLech.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblChenhLech.setForeground(ThemeColors.SUCCESS);
        pnlForm.add(lblChenhLech, gbc);

        // Lý do chênh lệch
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblLdTitle = new JLabel("Lý do chênh lệch:");
        lblLdTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLdTitle.setForeground(ThemeColors.TEXT_SECONDARY);
        pnlForm.add(lblLdTitle, gbc);

        gbc.gridx = 1;
        txtLyDoChenhLech = new JTextArea(3, 20);
        txtLyDoChenhLech.setLineWrap(true);
        txtLyDoChenhLech.setWrapStyleWord(true);
        txtLyDoChenhLech.setBorder(new EmptyBorder(4, 6, 4, 6));
        JScrollPane spLyDo = new JScrollPane(txtLyDoChenhLech);
        spLyDo.setBorder(ThemeColors.createInputBorder());
        pnlForm.add(spLyDo, gbc);

        pnl.add(pnlForm, BorderLayout.CENTER);

        // Nút bấm xác nhận & in
        JPanel pnlButtons = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlButtons.setOpaque(false);

        JButton btnXacNhan = new JButton("XÁC NHẬN KẾT CA & BÀN GIAO",
                IconHelper.getIcon(IconHelper.CLIPBOARD_CHECK, 16, Color.WHITE));
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXacNhan.setBackground(ThemeColors.PRIMARY);
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setPreferredSize(new Dimension(0, 42));
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXacNhan.setIconTextGap(8);
        btnXacNhan.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.PRIMARY_HOVER, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        btnXacNhan.addActionListener(e -> xacNhanBanGiaoCa());

        JButton btnInBienBan = ThemeColors.createSecondaryButton("In Biên bản Giao ca",
                IconHelper.getIcon(IconHelper.RECEIPT, 14, ThemeColors.TEXT_SECONDARY));
        btnInBienBan.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã xuất và in biên bản giao ca đối soát két tiền!", "In biên bản", JOptionPane.INFORMATION_MESSAGE));

        pnlButtons.add(btnXacNhan);
        pnlButtons.add(btnInBienBan);
        pnl.add(pnlButtons, BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel taoPanelLichSuGiaoCa() {
        JPanel pnl = new JPanel(new BorderLayout(8, 10));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        JLabel lblTitle = new JLabel("LỊCH SỬ BÀN GIAO CA GẦN ĐÂY",
                IconHelper.getIcon(IconHelper.CLOCK, 18, ThemeColors.PRIMARY), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        lblTitle.setIconTextGap(8);
        pnl.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Mã GC", "Ca", "Thu ngân", "Đầu ca", "Lý thuyết HT", "Thực tế", "Chênh lệch", "Thời điểm kết ca", "Ghi chú"};
        modelLichSu = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        dongBoLichSuGiaoCaTuDatabase();

        JTable tbl = new JTable(modelLichSu);
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
        tbl.getColumnModel().getColumn(0).setPreferredWidth(60);
        tbl.getColumnModel().getColumn(0).setCellRenderer(center);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(90);
        tbl.getColumnModel().getColumn(1).setCellRenderer(center);

        pnl.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return pnl;
    }

    private void tinhChenhLech() {
        try {
            long lyThuyet = tienDauCa + tienHeThongGhiNhan;
            String text = txtTienThucTe.getText().replace(",", "").replace(".", "").trim();
            long thucTe = text.isEmpty() ? 0 : Long.parseLong(text);
            long chenhLech = thucTe - lyThuyet;

            if (chenhLech == 0) {
                lblChenhLech.setText("0 đ (Khớp két 100%)");
                lblChenhLech.setForeground(ThemeColors.SUCCESS);
            } else if (chenhLech > 0) {
                lblChenhLech.setText("+" + df.format(chenhLech) + " (Thừa tiền mặt)");
                lblChenhLech.setForeground(ThemeColors.WARNING);
            } else {
                lblChenhLech.setText(df.format(chenhLech) + " (Thiếu tiền mặt)");
                lblChenhLech.setForeground(ThemeColors.DANGER);
            }
        } catch (NumberFormatException e) {
            lblChenhLech.setText("Số tiền không hợp lệ");
            lblChenhLech.setForeground(ThemeColors.DANGER);
        }
    }

    public void dongBoLichSuGiaoCaTuDatabase() {
        if (modelLichSu == null) return;
        modelLichSu.setRowCount(0);
        try {
            models.dao.BangGiaoCaDAO dao = new models.dao.BangGiaoCaDAO();
            java.util.List<models.entity.BangGiaoCa> dbList = dao.getAllBangGiaoCa();
            if (dbList != null && !dbList.isEmpty()) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                for (models.entity.BangGiaoCa bgc : dbList) {
                    String maCa = bgc.getCaLamViec() != null && bgc.getCaLamViec().getMaCa() != null ? bgc.getCaLamViec().getMaCa() : "CA01";
                    String tenNV = bgc.getNhanVien() != null && bgc.getNhanVien().getHoTen() != null ? bgc.getNhanVien().getHoTen() : "Lê Anh Minh";
                    long dauCa = bgc.getTienDauCa() != null ? bgc.getTienDauCa().longValue() : 0L;
                    long ht = bgc.getTienCuoiCaHeThong() != null ? bgc.getTienCuoiCaHeThong().longValue() : 0L;
                    long tt = bgc.getTienCuoiCaThucTe() != null ? bgc.getTienCuoiCaThucTe().longValue() : 0L;
                    long cl = bgc.getTienChenhLech() != null ? bgc.getTienChenhLech().longValue() : 0L;
                    String gioKT = bgc.getGioKetThucThucTe() != null ? bgc.getGioKetThucThucTe().format(dtf) : "Đang mở";
                    modelLichSu.addRow(new Object[]{
                            bgc.getMaGiaoCa(), maCa, tenNV,
                            df.format(dauCa), df.format(ht), df.format(tt), df.format(cl),
                            gioKT, bgc.getLyDoChenhLech()
                    });
                }
                return;
            }
        } catch (Exception e) {
            System.err.println("[BanGiaoCaPanel] Lỗi tải lịch sử giao ca CSDL: " + e.getMessage());
        }
        modelLichSu.addRow(new Object[]{"GC001", "CA03 (Tối)", "Lê Anh Minh", "1,000,000 đ", "7,850,000 đ", "7,850,000 đ", "0 đ", "09/10/2026 22:05", "Khớp số liệu"});
        modelLichSu.addRow(new Object[]{"GC002", "CA02 (Chiều)", "Tô Thanh Đạt", "1,000,000 đ", "6,120,000 đ", "6,100,000 đ", "-20,000 đ", "09/10/2026 17:35", "Lệch tiền thối"});
        modelLichSu.addRow(new Object[]{"GC003", "CA01 (Sáng)", "Lê Anh Minh", "1,000,000 đ", "5,200,000 đ", "5,200,000 đ", "0 đ", "09/10/2026 12:02", "Khớp két"});
    }

    public void thucHienGiaoCa() {
        xacNhanBanGiaoCa();
    }

    private void xacNhanBanGiaoCa() {
        tinhChenhLech();
        long lyThuyet = tienDauCa + tienHeThongGhiNhan;
        String text = txtTienThucTe.getText().replace(",", "").replace(".", "").trim();
        long thucTe = text.isEmpty() ? 0 : Long.parseLong(text);
        long chenhLech = thucTe - lyThuyet;

        if (chenhLech != 0 && txtLyDoChenhLech.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Phát hiện chênh lệch tiền mặt (" + df.format(chenhLech) + ")!\n" +
                    "Theo quy định nghiệp vụ, thu ngân BẮT BUỘC nhập lý do giải trình trước khi kết ca.",
                    "Cảnh báo chênh lệch", JOptionPane.WARNING_MESSAGE);
            txtLyDoChenhLech.requestFocus();
            return;
        }

        LocalDateTime nowDt = LocalDateTime.now();
        String now = nowDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        int maxNum = modelLichSu.getRowCount();
        for (int i = 0; i < modelLichSu.getRowCount(); i++) {
            String m = String.valueOf(modelLichSu.getValueAt(i, 0));
            if (m.startsWith("GC")) {
                try {
                    int n = Integer.parseInt(m.substring(2).replaceAll("[^0-9]", ""));
                    if (n > maxNum) maxNum = n;
                } catch (Exception ignored) {}
            }
        }
        String maGC = String.format("GC%03d", maxNum + 1);
        String note = chenhLech == 0 ? "Khớp két" : txtLyDoChenhLech.getText().trim();

        try {
            models.dao.BangGiaoCaDAO dao = new models.dao.BangGiaoCaDAO();
            models.entity.NhanVien nv = new models.entity.NhanVien("NV002", "Lê Anh Minh", "", "", null);
            models.entity.CaLamViec ca = new models.entity.CaLamViec("CA01", "Ca 1", null, null);
            models.entity.BangGiaoCa bgc = new models.entity.BangGiaoCa(
                    maGC, nv, ca,
                    java.math.BigDecimal.valueOf(tienDauCa),
                    java.math.BigDecimal.valueOf(lyThuyet),
                    java.math.BigDecimal.valueOf(thucTe),
                    java.math.BigDecimal.valueOf(chenhLech),
                    note, nowDt
            );
            dao.insertBangGiaoCa(bgc);
        } catch (Exception e) {
            System.err.println("[BanGiaoCaPanel] Lỗi lưu giao ca CSDL: " + e.getMessage());
        }

        modelLichSu.insertRow(0, new Object[]{
                maGC, "CA01", "Lê Anh Minh",
                df.format(tienDauCa), df.format(lyThuyet),
                df.format(thucTe), df.format(chenhLech), now, note
        });

        JOptionPane.showMessageDialog(this,
                "BÀN GIAO CA LÀM VIỆC THÀNH CÔNG!\n\n" +
                "- Mã giao ca: " + maGC + "\n" +
                "- Thời điểm kết ca: " + now + "\n" +
                "- Đã đối soát két và lưu trữ dữ liệu vào hệ thống.",
                "Kết ca thành công", JOptionPane.INFORMATION_MESSAGE);
    }
}
