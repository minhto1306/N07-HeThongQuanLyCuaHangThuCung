package views;

import utils.IconHelper;
import utils.ThemeColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

/**
 * Biểu mẫu Hóa đơn bán hàng chuẩn siêu thị / Pet Shop hiện đại (Thermal Receipt / A5 Invoice).
 * Hiển thị ngay sau khi thanh toán tại quầy POS hoặc khi Xem lại / In lại hóa đơn trong Lịch sử.
 */
public class HoaDonPreviewDialog extends JDialog {

    private final DecimalFormat df = new DecimalFormat("#,### đ");
    private final BanHangPOSPanel.HoaDonDaThanhToan hoaDon;
    private final long tienKhachDua;
    private final long tienThoiLai;
    private final int diemCongThem;
    private final int tongDiemSauTT;

    public HoaDonPreviewDialog(Frame parent, BanHangPOSPanel.HoaDonDaThanhToan hd,
                               long tienKhachDua, long tienThoiLai, int diemCongThem, int tongDiemSauTT) {
        super(parent, "BIỂU MẪU HÓA ĐƠN BÁN HÀNG - " + (hd != null ? hd.maHoaDon : ""), true);
        this.hoaDon = hd;
        this.tienKhachDua = tienKhachDua > 0 ? tienKhachDua : (hd != null ? hd.tongThanhToan : 0);
        this.tienThoiLai = Math.max(0, this.tienKhachDua - (hd != null ? hd.tongThanhToan : 0));
        this.diemCongThem = diemCongThem >= 0 ? diemCongThem : (int) ((hd != null ? hd.tongThanhToan : 0) / 10000L);
        this.tongDiemSauTT = tongDiemSauTT;

        khoiTaoGiaoDien();
    }

    public static void hienThiHoaDon(Component parentComp, BanHangPOSPanel.HoaDonDaThanhToan hd,
                                     long tienDua, long tienThoi, int diemCong, int tongDiem) {
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(parentComp);
        HoaDonPreviewDialog dlg = new HoaDonPreviewDialog(topFrame, hd, tienDua, tienThoi, diemCong, tongDiem);
        dlg.setVisible(true);
    }

    private void khoiTaoGiaoDien() {
        setSize(520, 720);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0xF1, 0xF5, 0xF9));

        // Cho phép bấm ESC để đóng
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Tờ giấy hóa đơn màu trắng ở giữa có viền đổ bóng nhẹ
        JPanel pnlPaperWrapper = new JPanel(new BorderLayout());
        pnlPaperWrapper.setOpaque(false);
        pnlPaperWrapper.setBorder(new EmptyBorder(14, 18, 10, 18));

        JPanel pnlPaper = new JPanel();
        pnlPaper.setLayout(new BoxLayout(pnlPaper, BoxLayout.Y_AXIS));
        pnlPaper.setBackground(Color.WHITE);
        pnlPaper.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xCB, 0xD5, 0xE1), 1, true),
                new EmptyBorder(18, 22, 18, 22)
        ));

        // 1. HEADER CỬA HÀNG
        JPanel pnlStoreHeader = new JPanel(new BorderLayout());
        pnlStoreHeader.setOpaque(false);
        pnlStoreHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnlStoreText = new JPanel();
        pnlStoreText.setLayout(new BoxLayout(pnlStoreText, BoxLayout.Y_AXIS));
        pnlStoreText.setOpaque(false);

        JLabel lblShopName = new JLabel("PET SHOP MANAGEMENT - NHÓM 07", IconHelper.getPrimaryIcon(IconHelper.PAW, 20), SwingConstants.CENTER);
        lblShopName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblShopName.setForeground(ThemeColors.TEXT_PRIMARY);
        lblShopName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblAddress = new JLabel("Đ/c: 12 Nguyễn Văn Bảo, P.4, Q.Gò Vấp, TP.HCM");
        lblAddress.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblAddress.setForeground(ThemeColors.TEXT_SECONDARY);
        lblAddress.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblHotline = new JLabel("Hotline: 1900 6868 · Website: www.petshop-n07.vn");
        lblHotline.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHotline.setForeground(ThemeColors.TEXT_SECONDARY);
        lblHotline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblInvoiceTitle = new JLabel("HÓA ĐƠN BÁN HÀNG & DỊCH VỤ");
        lblInvoiceTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblInvoiceTitle.setForeground(ThemeColors.PRIMARY_ACTIVE);
        lblInvoiceTitle.setBorder(new EmptyBorder(8, 0, 2, 0));
        lblInvoiceTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlStoreText.add(lblShopName);
        pnlStoreText.add(Box.createVerticalStrut(2));
        pnlStoreText.add(lblAddress);
        pnlStoreText.add(lblHotline);
        pnlStoreText.add(lblInvoiceTitle);
        pnlStoreHeader.add(pnlStoreText, BorderLayout.CENTER);
        pnlPaper.add(pnlStoreHeader);
        pnlPaper.add(Box.createVerticalStrut(8));

        pnlPaper.add(taoDuongKeNetDut());
        pnlPaper.add(Box.createVerticalStrut(8));

        // 2. THÔNG TIN CHUNG HÓA ĐƠN & KHÁCH HÀNG (2 CỘT)
        JPanel pnlMeta = new JPanel(new GridLayout(0, 2, 8, 4));
        pnlMeta.setOpaque(false);
        pnlMeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlMeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        String sdtDisplay = (hoaDon.sdt == null || hoaDon.sdt.isEmpty()) ? "—" : hoaDon.sdt;
        pnlMeta.add(taoCapMeta("Số Hóa Đơn:", hoaDon.maHoaDon, true));
        pnlMeta.add(taoCapMeta("Ngày giờ:", hoaDon.thoiGian, false));
        pnlMeta.add(taoCapMeta("Khách hàng:", hoaDon.tenKhach, true));
        pnlMeta.add(taoCapMeta("Số ĐT:", sdtDisplay, false));
        pnlMeta.add(taoCapMeta("Thu ngân:", hoaDon.thuNgan, false));
        pnlMeta.add(taoCapMeta("Hình thức TT:", hoaDon.phuongThuc, false));

        pnlPaper.add(pnlMeta);
        pnlPaper.add(Box.createVerticalStrut(8));
        pnlPaper.add(taoDuongKeNetDut());
        pnlPaper.add(Box.createVerticalStrut(6));

        // 3. BẢNG CHI TIẾT MẶT HÀNG / DỊCH VỤ
        String[] cols = {"STT", "Tên mặt hàng / Dịch vụ", "ĐVT", "SL", "Đơn giá", "Thành tiền"};
        DefaultTableModel modelItems = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        int stt = 1;
        for (Object[] mon : hoaDon.dsMon) {
            Object ten = mon.length > 2 ? mon[2] : "";
            Object dvt = mon.length > 3 ? mon[3] : "";
            Object sl = mon.length > 4 ? mon[4] : 1;
            Object donGia = mon.length > 5 ? mon[5] : "0 đ";
            Object thanhTien = mon.length > 6 ? mon[6] : "0 đ";
            modelItems.addRow(new Object[]{stt++, ten, dvt, sl, donGia, thanhTien});
        }

        JTable tblItems = new JTable(modelItems);
        tblItems.setRowHeight(28);
        tblItems.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tblItems.setShowHorizontalLines(true);
        tblItems.setShowVerticalLines(false);
        tblItems.setGridColor(new Color(0xE2, 0xE8, 0xF0));
        tblItems.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tblItems.getTableHeader().setBackground(new Color(0xF8, 0xFA, 0xFC));
        tblItems.getTableHeader().setPreferredSize(new Dimension(0, 26));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);

        tblItems.getColumnModel().getColumn(0).setPreferredWidth(28);
        tblItems.getColumnModel().getColumn(0).setCellRenderer(center);
        tblItems.getColumnModel().getColumn(1).setPreferredWidth(165);
        tblItems.getColumnModel().getColumn(2).setPreferredWidth(42);
        tblItems.getColumnModel().getColumn(2).setCellRenderer(center);
        tblItems.getColumnModel().getColumn(3).setPreferredWidth(28);
        tblItems.getColumnModel().getColumn(3).setCellRenderer(center);
        tblItems.getColumnModel().getColumn(4).setPreferredWidth(68);
        tblItems.getColumnModel().getColumn(4).setCellRenderer(right);
        tblItems.getColumnModel().getColumn(5).setPreferredWidth(76);
        tblItems.getColumnModel().getColumn(5).setCellRenderer(right);

        int tableHeight = Math.min(200, Math.max(95, hoaDon.dsMon.size() * 28 + 30));
        JScrollPane scrollItems = new JScrollPane(tblItems);
        scrollItems.setPreferredSize(new Dimension(0, tableHeight));
        scrollItems.setMaximumSize(new Dimension(Integer.MAX_VALUE, tableHeight));
        scrollItems.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollItems.setBorder(new LineBorder(new Color(0xE2, 0xE8, 0xF0), 1));
        scrollItems.getViewport().setBackground(Color.WHITE);

        pnlPaper.add(scrollItems);
        pnlPaper.add(Box.createVerticalStrut(8));

        // 4. KHUNG TỔNG KẾT THANH TOÁN
        JPanel pnlTotals = new JPanel(new GridLayout(0, 2, 8, 4));
        pnlTotals.setOpaque(false);
        pnlTotals.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlTotals.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        pnlTotals.add(taoLabelTrai("Tổng tiền hàng (" + hoaDon.dsMon.size() + " món):", Font.PLAIN, 12, ThemeColors.TEXT_SECONDARY));
        pnlTotals.add(taoLabelPhai(df.format(hoaDon.tongTienHang), Font.BOLD, 12, ThemeColors.TEXT_PRIMARY));

        pnlTotals.add(taoLabelTrai("Chiết khấu VIP / Đổi điểm:", Font.PLAIN, 12, ThemeColors.TEXT_SECONDARY));
        pnlTotals.add(taoLabelPhai((hoaDon.giamGia > 0 ? "-" : "") + df.format(hoaDon.giamGia), Font.BOLD, 12, ThemeColors.DANGER));

        pnlTotals.add(taoLabelTrai("TỔNG THANH TOÁN:", Font.BOLD, 14, ThemeColors.TEXT_PRIMARY));
        pnlTotals.add(taoLabelPhai(df.format(hoaDon.tongThanhToan), Font.BOLD, 15, ThemeColors.PRIMARY_ACTIVE));

        pnlTotals.add(taoLabelTrai("Tiền khách đưa:", Font.PLAIN, 12, ThemeColors.TEXT_SECONDARY));
        pnlTotals.add(taoLabelPhai(df.format(tienKhachDua), Font.BOLD, 12, ThemeColors.TEXT_PRIMARY));

        pnlTotals.add(taoLabelTrai("Tiền thối lại khách:", Font.PLAIN, 12, ThemeColors.TEXT_SECONDARY));
        pnlTotals.add(taoLabelPhai(df.format(tienThoiLai), Font.BOLD, 12, ThemeColors.SUCCESS_TEXT));

        pnlPaper.add(pnlTotals);
        pnlPaper.add(Box.createVerticalStrut(6));

        // Dòng điểm thưởng thành viên
        if (diemCongThem > 0 || tongDiemSauTT > 0) {
            JPanel pnlLoyalty = new JPanel(new BorderLayout());
            pnlLoyalty.setBackground(ThemeColors.PRIMARY_TINT);
            pnlLoyalty.setBorder(new CompoundBorder(
                    new LineBorder(ThemeColors.BORDER_ACCENT, 1, true),
                    new EmptyBorder(4, 10, 4, 10)
            ));
            pnlLoyalty.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlLoyalty.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

            String loyaltyStr = "★ Điểm tích lũy cộng thêm: +" + diemCongThem + " điểm"
                    + (tongDiemSauTT > 0 ? "  |  Tổng điểm hiện có: " + tongDiemSauTT + " điểm" : "");
            JLabel lblLoyalty = new JLabel(loyaltyStr, SwingConstants.CENTER);
            lblLoyalty.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblLoyalty.setForeground(ThemeColors.PRIMARY_ACTIVE);
            pnlLoyalty.add(lblLoyalty, BorderLayout.CENTER);
            pnlPaper.add(pnlLoyalty);
            pnlPaper.add(Box.createVerticalStrut(8));
        }

        pnlPaper.add(taoDuongKeNetDut());
        pnlPaper.add(Box.createVerticalStrut(8));

        // 5. FOOTER MÃ VẠCH & LỜI CẢM ƠN
        JPanel pnlFooter = new JPanel();
        pnlFooter.setLayout(new BoxLayout(pnlFooter, BoxLayout.Y_AXIS));
        pnlFooter.setOpaque(false);
        pnlFooter.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnlBarcode = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0x1E, 0x29, 0x3B));
                int w = getWidth();
                int startX = (w - 180) / 2;
                int hash = Math.abs(hoaDon.maHoaDon.hashCode());
                int x = startX;
                for (int i = 0; i < 42; i++) {
                    int barW = ((hash >> (i % 16)) & 1) == 1 ? 3 : 1;
                    g2.fillRect(x, 2, barW, 24);
                    x += barW + (((i % 3) == 0) ? 2 : 1);
                }
                g2.dispose();
            }
        };
        pnlBarcode.setOpaque(false);
        pnlBarcode.setPreferredSize(new Dimension(220, 28));
        pnlBarcode.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel lblCodeText = new JLabel("* " + hoaDon.maHoaDon + " *");
        lblCodeText.setFont(new Font("Consolas", Font.BOLD, 11));
        lblCodeText.setForeground(ThemeColors.TEXT_MUTED);
        lblCodeText.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblThanks = new JLabel("Cảm ơn Quý khách & Hẹn gặp lại bé cưng!");
        lblThanks.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblThanks.setForeground(ThemeColors.TEXT_SECONDARY);
        lblThanks.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPolicy = new JLabel("(Quý khách vui lòng giữ phiếu để đổi trả hàng trong vòng 7 ngày)");
        lblPolicy.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblPolicy.setForeground(ThemeColors.TEXT_MUTED);
        lblPolicy.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlFooter.add(pnlBarcode);
        pnlFooter.add(lblCodeText);
        pnlFooter.add(Box.createVerticalStrut(4));
        pnlFooter.add(lblThanks);
        pnlFooter.add(lblPolicy);

        JPanel pnlFooterWrap = new JPanel(new BorderLayout());
        pnlFooterWrap.setOpaque(false);
        pnlFooterWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlFooterWrap.add(pnlFooter, BorderLayout.CENTER);
        pnlPaper.add(pnlFooterWrap);

        JScrollPane scrollPaper = new JScrollPane(pnlPaper);
        scrollPaper.setBorder(null);
        scrollPaper.getVerticalScrollBar().setUnitIncrement(16);
        pnlPaperWrapper.add(scrollPaper, BorderLayout.CENTER);

        // 6. THANH NÚT TÁC VỤ DƯỚI CÙNG
        JPanel pnlBottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBottomActions.setBackground(Color.WHITE);
        pnlBottomActions.setBorder(new MatteBorder(1, 0, 0, 0, ThemeColors.BORDER));

        JButton btnInMayIn = ThemeColors.createPrimaryButton("In Hóa Đơn (Máy in 80mm)", IconHelper.getIcon(IconHelper.CLIPBOARD_CHECK, 15, ThemeColors.TEXT_PRIMARY));
        JButton btnXuatFile = ThemeColors.createSecondaryButton("Lưu phiếu (.txt)", IconHelper.getIcon(IconHelper.FILE_TEXT, 14, ThemeColors.TEXT_SECONDARY));
        JButton btnDong = ThemeColors.createSecondaryButton("Đóng (ESC)", null);

        btnInMayIn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "ĐÃ GỬI LỆNH IN TỚI MÁY IN HÓA ĐƠN NHIỆT 80MM!\n" +
                            "• Mã hóa đơn: " + hoaDon.maHoaDon + "\n" +
                            "• Khách hàng: " + hoaDon.tenKhach + "\n" +
                            "• Tổng thanh toán: " + df.format(hoaDon.tongThanhToan) + "\n" +
                            "• Trạng thái: Đã in phiếu thành công.",
                    "In hóa đơn thành công", JOptionPane.INFORMATION_MESSAGE);
        });

        btnXuatFile.addActionListener(e -> luuPhieuRaFileTxt());
        btnDong.addActionListener(e -> dispose());

        pnlBottomActions.add(btnXuatFile);
        pnlBottomActions.add(btnInMayIn);
        pnlBottomActions.add(btnDong);

        add(pnlPaperWrapper, BorderLayout.CENTER);
        add(pnlBottomActions, BorderLayout.SOUTH);
    }

    private JPanel taoDuongKeNetDut() {
        JPanel line = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0xCB, 0xD5, 0xE1));
                Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0);
                g2.setStroke(dashed);
                g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
                g2.dispose();
            }
        };
        line.setOpaque(false);
        line.setPreferredSize(new Dimension(0, 6));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }

    private JPanel taoCapMeta(String label, String value, boolean boldVal) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(ThemeColors.TEXT_MUTED);

        JLabel v = new JLabel(value != null ? value : "—");
        v.setFont(new Font("Segoe UI", boldVal ? Font.BOLD : Font.PLAIN, 11));
        v.setForeground(ThemeColors.TEXT_PRIMARY);

        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private JLabel taoLabelTrai(String text, int style, int size, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.LEFT);
        lbl.setFont(new Font("Segoe UI", style, size));
        lbl.setForeground(color);
        return lbl;
    }

    private JLabel taoLabelPhai(String text, int style, int size, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.RIGHT);
        lbl.setFont(new Font("Segoe UI", style, size));
        lbl.setForeground(color);
        return lbl;
    }

    private void luuPhieuRaFileTxt() {
        try {
            File dir = new File("export");
            if (!dir.exists()) dir.mkdirs();
            File f = new File(dir, "HoaDon_" + hoaDon.maHoaDon + ".txt");
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
                pw.println("======================================================");
                pw.println("          PET SHOP MANAGEMENT - NHÓM 07               ");
                pw.println("     Đ/c: 12 Nguyễn Văn Bảo, P.4, Gò Vấp, TP.HCM      ");
                pw.println("======================================================");
                pw.println("              HÓA ĐƠN BÁN HÀNG & DỊCH VỤ              ");
                pw.println("Mã HĐ: " + hoaDon.maHoaDon + "  |  Ngày: " + hoaDon.thoiGian);
                pw.println("Khách hàng: " + hoaDon.tenKhach + "  |  SĐT: " + hoaDon.sdt);
                pw.println("Thu ngân: " + hoaDon.thuNgan + "  |  HTTT: " + hoaDon.phuongThuc);
                pw.println("------------------------------------------------------");
                for (Object[] mon : hoaDon.dsMon) {
                    pw.printf("- %-26s | SL: %-2s | %s%n", mon[2], mon[4], mon[6]);
                }
                pw.println("------------------------------------------------------");
                pw.println("Tổng tiền hàng:   " + df.format(hoaDon.tongTienHang));
                pw.println("Giảm giá VIP/Điểm:" + df.format(hoaDon.giamGia));
                pw.println("TỔNG THANH TOÁN:  " + df.format(hoaDon.tongThanhToan));
                pw.println("Tiền khách đưa:   " + df.format(tienKhachDua));
                pw.println("Tiền thối lại:    " + df.format(tienThoiLai));
                pw.println("======================================================");
                pw.println("     Cảm ơn Quý khách & Hẹn gặp lại bé cưng!          ");
            }
            JOptionPane.showMessageDialog(this,
                    "Đã lưu bản in hóa đơn thành công tại:\n" + f.getAbsolutePath(),
                    "Lưu phiếu thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
