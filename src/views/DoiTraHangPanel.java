package views;

import controllers.BanHangController;
import models.entity.KhachHang;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Phân hệ Quản lý Đổi Trả Hàng Hóa & Hoàn Tiền (Return & Exchange Management)
 * Chuẩn Slide 17 IUH & Bảng màu Happy Hues Palette 15 (#faeee7, #ff8ba7, #33272a).
 * Hỗ trợ 2 Kịch bản Nghiệp vụ thực tế:
 * 1. Kịch bản A: Trả hàng - Hoàn tiền (Khấu trừ điểm VIP, Hoàn kho nếu nguyên tem).
 * 2. Kịch bản B: Đổi sang món khác (Bù/hoàn chênh lệch, Cập nhật kho cả món cũ và mới).
 */
public class DoiTraHangPanel extends JPanel {

    private final DecimalFormat df = new DecimalFormat("#,### đ");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

    // Khu vực 1: Tra cứu Hóa đơn
    private JTextField txtTraCuu;
    private JLabel lblInfoMaHD;
    private JLabel lblInfoKhach;
    private JLabel lblInfoThuNgan;
    private JLabel lblInfoThoiGian;
    private JLabel lblInfoTongTien;
    private JPanel pnlInfoCard;

    // Khu vực 2: Bảng sản phẩm
    private DefaultTableModel modelSanPham;
    private JTable tblSanPham;

    // Khu vực 3: Kịch bản đổi trả
    private JRadioButton radKichBanA;
    private JRadioButton radKichBanB;
    private JComboBox<BanHangPOSPanel.SanPhamDoiTra> cboSanPhamMoi;
    private JSpinner spnSoLuongMoi;
    private JLabel lblKetQuaHoanTien;
    private JLabel lblKetQuaDiemVIP;
    private JLabel lblKetQuaKho;
    private JLabel lblKetQuaChenhLech;
    private JPanel pnlKichBanBDetails;

    // Data hóa đơn đang chọn
    private BanHangPOSPanel.HoaDonDaThanhToan currentHoaDon = null;
    private KhachHang currentKhachHang = null;
    private boolean dangCapNhatBang = false;

    public DoiTraHangPanel() {
        setLayout(new BorderLayout(0, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));
        setBackground(ThemeColors.BACKGROUND);

        // Header trên cùng
        add(taoHeader(), BorderLayout.NORTH);

        // Nội dung chính: Chia 2 nửa hoặc bố cục trên/dưới dạng thẻ
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 14));
        pnlCenter.setOpaque(false);

        // Nửa trên: Khu vực 1 (Tra cứu) & Khu vực 2 (Bảng sản phẩm)
        JPanel pnlTopSection = new JPanel(new BorderLayout(0, 10));
        pnlTopSection.setOpaque(false);
        pnlTopSection.add(taoKhuVucTraCuu(), BorderLayout.NORTH);
        pnlTopSection.add(taoKhuVucBangSanPham(), BorderLayout.CENTER);

        // Nửa dưới: Khu vực 3 (2 Kịch bản) & Khu vực 4 (Nút In phiếu)
        JPanel pnlBottomSection = new JPanel(new BorderLayout(0, 10));
        pnlBottomSection.setOpaque(false);
        pnlBottomSection.add(taoKhuVucKichBanNghiepVu(), BorderLayout.CENTER);
        pnlBottomSection.add(taoKhuVucThaoTac(), BorderLayout.SOUTH);

        pnlCenter.add(pnlTopSection, BorderLayout.CENTER);
        pnlCenter.add(pnlBottomSection, BorderLayout.SOUTH);

        add(pnlCenter, BorderLayout.CENTER);

        // Tải sẵn hóa đơn mẫu đầu tiên nếu có để nhân viên thấy ngay dữ liệu
        napHoaDonMacDinh();
    }

    private JPanel taoHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel pnlTitles = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlTitles.setOpaque(false);

        JLabel lblTitle = new JLabel("XỬ LÝ ĐỔI TRẢ HÀNG HÓA & HOÀN TIỀN", IconHelper.getPrimaryIcon(IconHelper.REFRESH, 20), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Hỗ trợ tra cứu hóa đơn cũ, hoàn tiền, đổi sang sản phẩm khác, bù trừ chênh lệch và khấu trừ điểm VIP.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(ThemeColors.TEXT_SECONDARY);

        pnlTitles.add(lblTitle);
        pnlTitles.add(lblSub);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlActions.setOpaque(false);

        JButton btnLamMoi = ThemeColors.createSecondaryButton("Làm mới / Đơn khác", IconHelper.getIcon(IconHelper.REFRESH, 14, ThemeColors.TEXT_SECONDARY));
        btnLamMoi.setPreferredSize(new Dimension(160, 36));
        btnLamMoi.addActionListener(e -> lamMoiGiaoDien());

        pnlActions.add(btnLamMoi);

        header.add(pnlTitles, BorderLayout.WEST);
        header.add(pnlActions, BorderLayout.EAST);
        return header;
    }

    // ==========================================
    // KHU VỰC 1: TRA CỨU HÓA ĐƠN GỐC
    // ==========================================
    private JPanel taoKhuVucTraCuu() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(12, 14, 12, 14));

        JPanel pnlSearchBar = new JPanel(new BorderLayout(8, 0));
        pnlSearchBar.setOpaque(false);

        JLabel lblSearchTitle = new JLabel("Tra cứu Hóa đơn:", IconHelper.getIcon(IconHelper.SEARCH, 15, ThemeColors.PRIMARY_ACTIVE), SwingConstants.LEFT);
        lblSearchTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearchTitle.setPreferredSize(new Dimension(140, 34));

        txtTraCuu = new JTextField();
        txtTraCuu.setPreferredSize(new Dimension(0, 34));
        txtTraCuu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTraCuu.putClientProperty("JTextField.placeholderText", "Nhập Mã HĐ (vd: HD982104) hoặc Số điện thoại khách hàng (vd: 0988111222)...");
        txtTraCuu.setBorder(ThemeColors.createInputBorder());
        txtTraCuu.addActionListener(e -> thucHienTraCuuHoaDon());

        JButton btnTim = ThemeColors.createSecondaryButton("Tra cứu HĐ", IconHelper.getIcon(IconHelper.SEARCH, 14, ThemeColors.TEXT_PRIMARY));
        btnTim.setPreferredSize(new Dimension(110, 34));
        btnTim.addActionListener(e -> thucHienTraCuuHoaDon());

        JButton btnChonLichSu = ThemeColors.createSecondaryButton("Chọn từ Lịch sử", IconHelper.getIcon(IconHelper.RECEIPT, 14, ThemeColors.TEXT_SECONDARY));
        btnChonLichSu.setPreferredSize(new Dimension(140, 34));
        btnChonLichSu.addActionListener(e -> hienThiDialogChonHoaDonLichSu());

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnTim);
        pnlButtons.add(btnChonLichSu);

        pnlSearchBar.add(lblSearchTitle, BorderLayout.WEST);
        pnlSearchBar.add(txtTraCuu, BorderLayout.CENTER);
        pnlSearchBar.add(pnlButtons, BorderLayout.EAST);

        // Thông tin Hóa đơn hiển thị bên dưới
        pnlInfoCard = new JPanel(new GridLayout(2, 3, 12, 6));
        pnlInfoCard.setOpaque(false);
        pnlInfoCard.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER_LIGHT, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        lblInfoMaHD = new JLabel("• Mã HĐ: —");
        lblInfoMaHD.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblInfoMaHD.setForeground(ThemeColors.PRIMARY_ACTIVE);

        lblInfoKhach = new JLabel("• Khách hàng: —");
        lblInfoKhach.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblInfoThuNgan = new JLabel("• Thu ngân: —");
        lblInfoThuNgan.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblInfoThoiGian = new JLabel("• Thời gian mua: —");
        lblInfoThoiGian.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblInfoTongTien = new JLabel("• Tổng thanh toán: —");
        lblInfoTongTien.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblInfoTongTien.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel lblChinhSach = new JLabel("• Chính sách: Đổi trả trong 07 ngày kể từ khi mua.");
        lblChinhSach.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblChinhSach.setForeground(ThemeColors.TEXT_MUTED);

        pnlInfoCard.add(lblInfoMaHD);
        pnlInfoCard.add(lblInfoKhach);
        pnlInfoCard.add(lblInfoThuNgan);
        pnlInfoCard.add(lblInfoThoiGian);
        pnlInfoCard.add(lblInfoTongTien);
        pnlInfoCard.add(lblChinhSach);

        pnl.add(pnlSearchBar, BorderLayout.NORTH);
        pnl.add(pnlInfoCard, BorderLayout.CENTER);
        return pnl;
    }

    // ==========================================
    // KHU VỰC 2: BẢNG SẢN PHẨM CẦN ĐỔI / TRẢ
    // ==========================================
    private JPanel taoKhuVucBangSanPham() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(12, 14, 12, 14));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("DANH SÁCH SẢN PHẨM TRONG HÓA ĐƠN CẦN ĐỔI / TRẢ", IconHelper.getPrimaryIcon(IconHelper.BOXES, 16), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel lblGuide = new JLabel("Tích chọn sản phẩm muốn đổi/trả -> Nhập số lượng và chọn lý do / tình trạng hàng.");
        lblGuide.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblGuide.setForeground(ThemeColors.TEXT_MUTED);

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(lblGuide, BorderLayout.EAST);

        String[] cols = {
                "Chọn", "STT", "Mã SP", "Tên sản phẩm", "ĐVT", "Đơn giá", "Đã mua", "SL Trả", "Lý do đổi trả", "Tình trạng hàng"
        };

        modelSanPham = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                if (columnIndex == 1 || columnIndex == 6 || columnIndex == 7) return Integer.class;
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // Cho phép sửa cột Chọn (0), SL Trả (7), Lý do (8), Tình trạng (9)
                return column == 0 || column == 7 || column == 8 || column == 9;
            }
        };

        tblSanPham = new JTable(modelSanPham);
        tblSanPham.setRowHeight(36);
        tblSanPham.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblSanPham.setShowHorizontalLines(true);
        tblSanPham.setShowVerticalLines(false);
        tblSanPham.setGridColor(ThemeColors.BORDER_LIGHT);
        tblSanPham.setSelectionBackground(ThemeColors.PRIMARY_SOFT);
        tblSanPham.setSelectionForeground(ThemeColors.TEXT_PRIMARY);
        tblSanPham.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblSanPham.getTableHeader().setBackground(ThemeColors.PRIMARY_TINT);
        tblSanPham.getTableHeader().setPreferredSize(new Dimension(0, 34));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);

        tblSanPham.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblSanPham.getColumnModel().getColumn(1).setPreferredWidth(45);
        tblSanPham.getColumnModel().getColumn(1).setCellRenderer(center);
        tblSanPham.getColumnModel().getColumn(2).setPreferredWidth(65);
        tblSanPham.getColumnModel().getColumn(2).setCellRenderer(center);
        tblSanPham.getColumnModel().getColumn(3).setPreferredWidth(180);
        tblSanPham.getColumnModel().getColumn(4).setPreferredWidth(50);
        tblSanPham.getColumnModel().getColumn(4).setCellRenderer(center);
        tblSanPham.getColumnModel().getColumn(5).setPreferredWidth(85);
        tblSanPham.getColumnModel().getColumn(6).setPreferredWidth(60);
        tblSanPham.getColumnModel().getColumn(6).setCellRenderer(center);
        tblSanPham.getColumnModel().getColumn(7).setPreferredWidth(65);
        tblSanPham.getColumnModel().getColumn(7).setCellRenderer(center);
        tblSanPham.getColumnModel().getColumn(8).setPreferredWidth(160);
        tblSanPham.getColumnModel().getColumn(9).setPreferredWidth(180);

        // Combobox cho Lý do đổi trả
        JComboBox<String> cboLyDo = new JComboBox<>(new String[]{
                "Sản phẩm lỗi/hỏng",
                "Thú cưng không hợp/dị ứng",
                "Đổi size vòng cổ/chuồng",
                "Khách đổi ý"
        });
        tblSanPham.getColumnModel().getColumn(8).setCellEditor(new DefaultCellEditor(cboLyDo));

        // Combobox cho Tình trạng hàng
        JComboBox<String> cboTinhTrang = new JComboBox<>(new String[]{
                "Còn nguyên tem (nhập lại kho)",
                "Hàng lỗi/rách (chờ hủy)"
        });
        tblSanPham.getColumnModel().getColumn(9).setCellEditor(new DefaultCellEditor(cboTinhTrang));

        // Lắng nghe sự kiện cập nhật bảng để tính lại số tiền
        modelSanPham.addTableModelListener(e -> capNhatTinhToanKichBan());

        JScrollPane scrollTable = new JScrollPane(tblSanPham);
        scrollTable.setPreferredSize(new Dimension(0, 160));
        scrollTable.setBorder(new LineBorder(ThemeColors.BORDER_LIGHT, 1, true));

        pnl.add(pnlHeader, BorderLayout.NORTH);
        pnl.add(scrollTable, BorderLayout.CENTER);
        return pnl;
    }

    // ==========================================
    // KHU VỰC 3: XỬ LÝ 2 KỊCH BẢN NGHIỆP VỤ
    // ==========================================
    private JPanel taoKhuVucKichBanNghiepVu() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(12, 14, 12, 14));

        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("LỰA CHỌN KỊCH BẢN XỬ LÝ:", IconHelper.getPrimaryIcon(IconHelper.CLIPBOARD_CHECK, 16), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        radKichBanA = new JRadioButton("Kịch bản A: Trả hàng - Hoàn tiền", true);
        radKichBanA.setFont(new Font("Segoe UI", Font.BOLD, 13));
        radKichBanA.setForeground(ThemeColors.TEXT_PRIMARY);
        radKichBanA.setOpaque(false);
        radKichBanA.setFocusPainted(false);
        radKichBanA.addActionListener(e -> chuyenDoiKichBan(true));

        radKichBanB = new JRadioButton("Kịch bản B: Đổi sang sản phẩm khác", false);
        radKichBanB.setFont(new Font("Segoe UI", Font.BOLD, 13));
        radKichBanB.setForeground(ThemeColors.TEXT_PRIMARY);
        radKichBanB.setOpaque(false);
        radKichBanB.setFocusPainted(false);
        radKichBanB.addActionListener(e -> chuyenDoiKichBan(false));

        ButtonGroup group = new ButtonGroup();
        group.add(radKichBanA);
        group.add(radKichBanB);

        pnlHeader.add(lblTitle);
        pnlHeader.add(radKichBanA);
        pnlHeader.add(radKichBanB);

        // Panel chi tiết kịch bản
        JPanel pnlDetails = new JPanel(new GridLayout(1, 2, 14, 0));
        pnlDetails.setOpaque(false);

        // Khối trái: Kết quả hoàn tiền & Khấu trừ điểm VIP (Dùng cho cả A và B)
        JPanel pnlSummary = new JPanel(new GridLayout(3, 1, 0, 6));
        pnlSummary.setOpaque(false);
        pnlSummary.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER_LIGHT, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        lblKetQuaHoanTien = new JLabel("Tiền hàng đổi/trả: 0 đ");
        lblKetQuaHoanTien.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKetQuaHoanTien.setForeground(ThemeColors.PRIMARY_ACTIVE);

        lblKetQuaDiemVIP = new JLabel("Khấu trừ điểm VIP: -0 điểm");
        lblKetQuaDiemVIP.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblKetQuaDiemVIP.setForeground(ThemeColors.DANGER);

        lblKetQuaKho = new JLabel("Tồn kho hoàn lại: 0 món (Nếu nguyên tem)");
        lblKetQuaKho.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblKetQuaKho.setForeground(ThemeColors.SUCCESS);

        pnlSummary.add(lblKetQuaHoanTien);
        pnlSummary.add(lblKetQuaDiemVIP);
        pnlSummary.add(lblKetQuaKho);

        // Khối phải: Cấu hình sản phẩm đổi mới (Dành cho Kịch bản B)
        pnlKichBanBDetails = new JPanel(new GridBagLayout());
        pnlKichBanBDetails.setOpaque(false);
        pnlKichBanBDetails.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER_LIGHT, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblChonMonMoi = new JLabel("Chọn món mới:");
        lblChonMonMoi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlKichBanBDetails.add(lblChonMonMoi, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        List<BanHangPOSPanel.SanPhamDoiTra> dsSanPham = BanHangPOSPanel.layDanhSachSanPhamDoi();
        cboSanPhamMoi = new JComboBox<>(dsSanPham.toArray(new BanHangPOSPanel.SanPhamDoiTra[0]));
        cboSanPhamMoi.setPreferredSize(new Dimension(220, 30));
        cboSanPhamMoi.addActionListener(e -> capNhatTinhToanKichBan());
        pnlKichBanBDetails.add(cboSanPhamMoi, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblSoLuongMoi = new JLabel("Số lượng mới:");
        lblSoLuongMoi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlKichBanBDetails.add(lblSoLuongMoi, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        spnSoLuongMoi = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnSoLuongMoi.setPreferredSize(new Dimension(80, 30));
        spnSoLuongMoi.addChangeListener(e -> capNhatTinhToanKichBan());
        pnlKichBanBDetails.add(spnSoLuongMoi, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        lblKetQuaChenhLech = new JLabel("Tiền chênh lệch bù/hoàn: 0 đ");
        lblKetQuaChenhLech.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKetQuaChenhLech.setForeground(ThemeColors.TEXT_PRIMARY);
        pnlKichBanBDetails.add(lblKetQuaChenhLech, gbc);

        pnlDetails.add(pnlSummary);
        pnlDetails.add(pnlKichBanBDetails);

        pnl.add(pnlHeader, BorderLayout.NORTH);
        pnl.add(pnlDetails, BorderLayout.CENTER);

        chuyenDoiKichBan(true);
        return pnl;
    }

    private void chuyenDoiKichBan(boolean laKichBanA) {
        cboSanPhamMoi.setEnabled(!laKichBanA);
        spnSoLuongMoi.setEnabled(!laKichBanA);
        if (laKichBanA) {
            pnlKichBanBDetails.setBackground(ThemeColors.BACKGROUND);
            lblKetQuaChenhLech.setText("Kịch bản A: Hoàn 100% tiền hàng đã chọn.");
            lblKetQuaChenhLech.setForeground(ThemeColors.TEXT_MUTED);
        } else {
            pnlKichBanBDetails.setBackground(ThemeColors.SURFACE);
        }
        capNhatTinhToanKichBan();
    }

    // ==========================================
    // KHU VỰC 4: THAO TÁC & IN PHIẾU ĐỔI TRẢ
    // ==========================================
    private JPanel taoKhuVucThaoTac() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        pnl.setOpaque(false);

        JButton btnXacNhanInPhieu = new JButton("XÁC NHẬN & IN PHIẾU ĐỔI TRẢ (80MM)", IconHelper.getIcon(IconHelper.RECEIPT, 16, Color.WHITE));
        btnXacNhanInPhieu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnXacNhanInPhieu.setBackground(ThemeColors.PRIMARY);
        btnXacNhanInPhieu.setForeground(Color.WHITE);
        btnXacNhanInPhieu.setPreferredSize(new Dimension(320, 44));
        btnXacNhanInPhieu.setFocusPainted(false);
        btnXacNhanInPhieu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXacNhanInPhieu.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.PRIMARY_HOVER, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btnXacNhanInPhieu.addActionListener(e -> thucHienXacNhanDoiTra());

        pnl.add(btnXacNhanInPhieu);
        return pnl;
    }

    // ==========================================
    // LOGIC NGHIỆP VỤ & TÍNH TOÁN
    // ==========================================
    private void napHoaDonMacDinh() {
        List<BanHangPOSPanel.HoaDonDaThanhToan> ds = BanHangPOSPanel.layDanhSachHoaDonDaThanhToan();
        if (!ds.isEmpty()) {
            hienThiHoaDon(ds.get(0));
        }
    }

    public void lamMoiGiaoDien() {
        txtTraCuu.setText("");
        napHoaDonMacDinh();
    }

    private void hienThiDialogChonHoaDonLichSu() {
        List<BanHangPOSPanel.HoaDonDaThanhToan> ds = BanHangPOSPanel.layDanhSachHoaDonDaThanhToan();
        if (ds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có hóa đơn nào đã thanh toán trong hệ thống!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] cols = {"Mã HĐ", "Thời gian", "Khách hàng", "Số điện thoại", "Tổng tiền", "Thu ngân"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (BanHangPOSPanel.HoaDonDaThanhToan hd : ds) {
            model.addRow(new Object[]{
                    hd.maHoaDon, hd.thoiGian, hd.tenKhach, hd.sdt, df.format(hd.tongThanhToan), hd.thuNgan
            });
        }

        JTable tbl = new JTable(model);
        tbl.setRowHeight(32);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (tbl.getRowCount() > 0) tbl.setRowSelectionInterval(0, 0);

        JScrollPane sp = new JScrollPane(tbl);
        sp.setPreferredSize(new Dimension(600, 260));

        int opt = JOptionPane.showConfirmDialog(this, sp, "CHỌN HÓA ĐƠN ĐÃ THANH TOÁN ĐỂ ĐỔI TRẢ", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
            int selectedRow = tbl.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < ds.size()) {
                hienThiHoaDon(ds.get(selectedRow));
            }
        }
    }

    private void thucHienTraCuuHoaDon() {
        String keyword = txtTraCuu.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã Hóa Đơn hoặc Số điện thoại cần tìm!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<BanHangPOSPanel.HoaDonDaThanhToan> ds = BanHangPOSPanel.layDanhSachHoaDonDaThanhToan();
        BanHangPOSPanel.HoaDonDaThanhToan timThay = null;
        for (BanHangPOSPanel.HoaDonDaThanhToan hd : ds) {
            if (hd.maHoaDon.equalsIgnoreCase(keyword) || (hd.sdt != null && hd.sdt.equalsIgnoreCase(keyword))) {
                timThay = hd;
                break;
            }
        }

        if (timThay != null) {
            hienThiHoaDon(timThay);
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn nào khớp với từ khóa: " + keyword, "Không tìm thấy", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void hienThiHoaDon(BanHangPOSPanel.HoaDonDaThanhToan hd) {
        this.currentHoaDon = hd;
        this.currentKhachHang = new BanHangController().timKhachHang(hd.sdt);

        int diem = currentKhachHang != null ? currentKhachHang.getDiemTichLuy() : 0;
        String vipBadge = diem >= 100 ? " (VIP)" : "";

        lblInfoMaHD.setText("• Mã HĐ: " + hd.maHoaDon);
        lblInfoKhach.setText("• Khách hàng: " + hd.tenKhach + " (" + (hd.sdt != null ? hd.sdt : "Vãng lai") + " - " + diem + " điểm" + vipBadge + ")");
        lblInfoThuNgan.setText("• Thu ngân: " + hd.thuNgan);
        lblInfoThoiGian.setText("• Thời gian mua: " + hd.thoiGian);
        lblInfoTongTien.setText("• Tổng thanh toán: " + df.format(hd.tongThanhToan) + " (" + hd.phuongThuc + ")");

        // Đổ sản phẩm vào bảng
        modelSanPham.setRowCount(0);
        int stt = 1;
        for (Object[] mon : hd.dsMon) {
            // [stt, maSP, tenSP, dvt, sl, donGia, thanhTien]
            String ma = mon.length > 1 && mon[1] != null ? mon[1].toString() : "SP000";
            String ten = mon.length > 2 && mon[2] != null ? mon[2].toString() : "Sản phẩm";
            String dvt = mon.length > 3 && mon[3] != null ? mon[3].toString() : "Gói";
            int slMua = mon.length > 4 && mon[4] instanceof Integer ? (Integer) mon[4] : 1;
            String giaStr = mon.length > 5 && mon[5] != null ? mon[5].toString() : "0 đ";

            // Chỉ cho phép đổi trả hàng hóa vật lý (bắt đầu bằng SP), không đổi dịch vụ đã thực hiện
            if (ma.startsWith("SP")) {
                modelSanPham.addRow(new Object[]{
                        false, stt++, ma, ten, dvt, giaStr, slMua, 1, "Sản phẩm lỗi/hỏng", "Còn nguyên tem (nhập lại kho)"
                });
            }
        }

        if (modelSanPham.getRowCount() > 0) {
            // Mặc định tích chọn món đầu tiên để tiện thao tác
            modelSanPham.setValueAt(true, 0, 0);
        }

        capNhatTinhToanKichBan();
    }

    private void capNhatTinhToanKichBan() {
        if (dangCapNhatBang) return;
        dangCapNhatBang = true;
        try {
            long tongTienTra = 0;
            int tongSlTra = 0;
            int tongSlNguyenTem = 0;

            for (int i = 0; i < modelSanPham.getRowCount(); i++) {
                Boolean chon = (Boolean) modelSanPham.getValueAt(i, 0);
                if (Boolean.TRUE.equals(chon)) {
                    int slMua = (int) modelSanPham.getValueAt(i, 6);
                    int slTra = 1;
                    Object valSlTra = modelSanPham.getValueAt(i, 7);
                    if (valSlTra instanceof Integer) {
                        slTra = (Integer) valSlTra;
                    } else if (valSlTra != null) {
                        try { slTra = Integer.parseInt(valSlTra.toString().trim()); } catch (Exception ignored) {}
                    }

                    // Giới hạn slTra không vượt quá slMua và >= 1
                    if (slTra > slMua) slTra = slMua;
                    if (slTra < 1) slTra = 1;
                    if (!Integer.valueOf(slTra).equals(valSlTra)) {
                        modelSanPham.setValueAt(slTra, i, 7);
                    }

                    long donGia = parseGia(modelSanPham.getValueAt(i, 5));
                    tongTienTra += (donGia * slTra);
                    tongSlTra += slTra;

                    String tinhTrang = (String) modelSanPham.getValueAt(i, 9);
                    if (tinhTrang != null && tinhTrang.contains("nguyên tem")) {
                        tongSlNguyenTem += slTra;
                    }
                }
            }

            int diemKhauTru = (int) (tongTienTra / 10000L);

            lblKetQuaHoanTien.setText("Tiền hàng đổi/trả: " + df.format(tongTienTra));
            lblKetQuaDiemVIP.setText("Khấu trừ điểm VIP: -" + diemKhauTru + " điểm");
            lblKetQuaKho.setText("Tồn kho hoàn lại: +" + tongSlNguyenTem + " món nguyên tem");

            if (radKichBanB.isSelected()) {
                BanHangPOSPanel.SanPhamDoiTra spMoi = (BanHangPOSPanel.SanPhamDoiTra) cboSanPhamMoi.getSelectedItem();
                int slMoi = (Integer) spnSoLuongMoi.getValue();
                long giaTriMonMoi = spMoi != null ? spMoi.donGia * slMoi : 0;
                long chenhLech = giaTriMonMoi - tongTienTra;

                if (chenhLech > 0) {
                    lblKetQuaChenhLech.setText("<html>Khách cần thanh toán thêm: <b style='color:#33272A; font-size:14px;'>+" + df.format(chenhLech) + "</b></html>");
                } else if (chenhLech < 0) {
                    lblKetQuaChenhLech.setText("<html>Cửa hàng hoàn lại cho khách: <b style='color:#E53E3E; font-size:14px;'>-" + df.format(Math.abs(chenhLech)) + "</b></html>");
                } else {
                    lblKetQuaChenhLech.setText("Đổi ngang giá (Chênh lệch: 0 đ)");
                }
            }
        } finally {
            dangCapNhatBang = false;
        }
    }

    private long parseGia(Object obj) {
        if (obj == null) return 0;
        String s = obj.toString().replaceAll("[^0-9]", "");
        if (s.isEmpty()) return 0;
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return 0;
        }
    }

    // ==========================================
    // XÁC NHẬN & IN PHIẾU ĐỔI TRẢ
    // ==========================================
    public void thucHienXacNhanDoiTra() {
        if (currentHoaDon == null) {
            JOptionPane.showMessageDialog(this, "Chưa có hóa đơn nào được chọn để xử lý đổi trả!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Object[]> dsMonTra = new ArrayList<>();
        long tongTienTra = 0;
        int tongSlNguyenTem = 0;

        for (int i = 0; i < modelSanPham.getRowCount(); i++) {
            Boolean chon = (Boolean) modelSanPham.getValueAt(i, 0);
            if (Boolean.TRUE.equals(chon)) {
                String maSP = (String) modelSanPham.getValueAt(i, 2);
                String tenSP = (String) modelSanPham.getValueAt(i, 3);
                String dvt = (String) modelSanPham.getValueAt(i, 4);
                long donGia = parseGia(modelSanPham.getValueAt(i, 5));
                int slTra = (Integer) modelSanPham.getValueAt(i, 7);
                String lyDo = (String) modelSanPham.getValueAt(i, 8);
                String tinhTrang = (String) modelSanPham.getValueAt(i, 9);
                boolean nguyenTem = tinhTrang != null && tinhTrang.contains("nguyên tem");

                long thanhTien = donGia * slTra;
                tongTienTra += thanhTien;
                if (nguyenTem) tongSlNguyenTem += slTra;

                dsMonTra.add(new Object[]{maSP, tenSP, dvt, slTra, donGia, thanhTien, lyDo, nguyenTem});
            }
        }

        if (dsMonTra.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng tích chọn ít nhất 01 sản phẩm cần đổi/trả!", "Chưa chọn món", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean laKichBanA = radKichBanA.isSelected();
        BanHangPOSPanel.SanPhamDoiTra spMoi = (BanHangPOSPanel.SanPhamDoiTra) cboSanPhamMoi.getSelectedItem();
        int slMoi = (Integer) spnSoLuongMoi.getValue();
        long giaTriMonMoi = (!laKichBanA && spMoi != null) ? spMoi.donGia * slMoi : 0;
        long chenhLech = giaTriMonMoi - tongTienTra;

        // 1. Cập nhật Hoàn kho nếu nguyên tem
        for (Object[] mon : dsMonTra) {
            String ma = (String) mon[0];
            int sl = (Integer) mon[3];
            boolean nt = (Boolean) mon[7];
            if (nt) {
                KhoLoHangPanel.congTonKho(ma, sl);
                BanHangPOSPanel.congTonKhoCatalog(ma, sl);
            }
        }

        // 2. Nếu Kịch bản B: trừ kho món mới
        if (!laKichBanA && spMoi != null) {
            KhoLoHangPanel.truTonKhoTheoFEFO(spMoi.maSP, slMoi);
            BanHangPOSPanel.truTonKhoCatalog(spMoi.maSP, slMoi);
        }

        // 3. Khấu trừ / Cập nhật điểm VIP
        int diemCu = currentKhachHang != null ? currentKhachHang.getDiemTichLuy() : 0;
        int diemKhauTru = (int) (tongTienTra / 10000L);
        int diemMoi = Math.max(0, diemCu - diemKhauTru);

        if (!laKichBanA && chenhLech > 0) {
            int diemCongThem = (int) (chenhLech / 10000L);
            diemMoi += diemCongThem;
        }

        if (currentHoaDon.sdt != null && !currentHoaDon.sdt.trim().isEmpty()) {
            BanHangController.luuHoacCapNhatKhachHang(currentHoaDon.sdt, currentHoaDon.tenKhach, diemMoi);
        }

        // Lưu Phiếu Đổi Trả vào SQL Server
        try {
            models.dao.HoaDonDAO hdDao = new models.dao.HoaDonDAO();
            models.entity.PhieuDoiTra pdt = new models.entity.PhieuDoiTra();
            pdt.setMaPhieuDoiTra("DT" + (System.currentTimeMillis() % 1000000));
            models.entity.HoaDon hdGoc = new models.entity.HoaDon();
            hdGoc.setMaHoaDon(currentHoaDon.maHoaDon);
            pdt.setHoaDonGoc(hdGoc);
            models.entity.NhanVien nv = new models.entity.NhanVien();
            nv.setMaNhanVien(utils.UserSession.getUserId() != null ? utils.UserSession.getUserId() : "NV001");
            pdt.setNhanVien(nv);
            pdt.setNgayDoiTra(LocalDateTime.now());
            pdt.setLyDoDoiTra(laKichBanA ? "Trả hàng - Hoàn tiền" : "Đổi sang sản phẩm khác");
            pdt.setTongTienHoan(java.math.BigDecimal.valueOf(tongTienTra));

            int idx = 1;
            for (Object[] mon : dsMonTra) {
                String ma = (String) mon[0];
                int sl = (Integer) mon[3];
                long thanhTien = (Long) mon[5];
                pdt.addChiTietDoiTra(new models.entity.ChiTietDoiTra(
                        pdt.getMaPhieuDoiTra() + "_" + (idx++),
                        pdt, new models.entity.SanPham(ma, null, "", ""), sl,
                        java.math.BigDecimal.valueOf(thanhTien)
                ));
            }
            hdDao.luuPhieuDoiTra(pdt);
        } catch (Exception ex) {
            System.err.println("[DoiTraHangPanel] Lỗi lưu phiếu đổi trả CSDL: " + ex.getMessage());
        }

        // 4. Đồng bộ Dashboard
        TongQuanPanel.lamMoiDuLieuDashboardAll();

        // 5. In Phiếu Đổi Trả nhiệt 80mm
        hienThiPhieuDoiTraDialog(dsMonTra, laKichBanA, spMoi, slMoi, giaTriMonMoi, chenhLech, tongTienTra, diemKhauTru, diemMoi);
    }

    private void hienThiPhieuDoiTraDialog(List<Object[]> dsMonTra, boolean laKichBanA,
                                          BanHangPOSPanel.SanPhamDoiTra spMoi, int slMoi, long giaTriMonMoi,
                                          long chenhLech, long tongTienTra, int diemKhauTru, int diemMoi) {
        String maPhieu = "DT" + System.currentTimeMillis() % 1000000;
        String thoiGian = LocalDateTime.now().format(dtf);

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("        PET SHOP MANAGEMENT - HỆ THỐNG CỬA HÀNG      \n");
        sb.append("             PHIẾU XÁC NHẬN ĐỔI TRẢ HÀNG HÓA        \n");
        sb.append("====================================================\n");
        sb.append("Mã phiếu:       ").append(maPhieu).append("\n");
        sb.append("Hóa đơn gốc:    ").append(currentHoaDon.maHoaDon).append(" (Mua ngày: ").append(currentHoaDon.thoiGian).append(")\n");
        sb.append("Thời gian lập:  ").append(thoiGian).append("\n");
        sb.append("Khách hàng:     ").append(currentHoaDon.tenKhach).append(" (").append(currentHoaDon.sdt != null ? currentHoaDon.sdt : "Vãng lai").append(")\n");
        sb.append("Thu ngân:       ").append(currentHoaDon.thuNgan).append("\n");
        sb.append("Hình thức:      ").append(laKichBanA ? "Trả hàng - Hoàn tiền" : "Đổi sang sản phẩm khác").append("\n");
        sb.append("----------------------------------------------------\n");
        sb.append("CÁC MÓN TRẢ LẠI:\n");
        for (Object[] mon : dsMonTra) {
            sb.append(String.format(" + [%s] %s x%d %s\n", mon[0], mon[1], mon[3], mon[2]));
            sb.append(String.format("   Đơn giá: %s | Thành tiền: %s\n", df.format(mon[4]), df.format(mon[5])));
            sb.append(String.format("   Lý do: %s | Tình trạng: %s\n", mon[6], (Boolean) mon[7] ? "Nguyên tem (Nhập kho)" : "Hàng lỗi (Hủy)"));
        }
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("TỔNG TIỀN HÀNG TRẢ:           %s\n", df.format(tongTienTra)));

        if (!laKichBanA && spMoi != null) {
            sb.append("----------------------------------------------------\n");
            sb.append("MÓN ĐỔI MỚI:\n");
            sb.append(String.format(" + [%s] %s x%d %s\n", spMoi.maSP, spMoi.tenSP, slMoi, spMoi.dvt));
            sb.append(String.format("   Giá trị món mới:           %s\n", df.format(giaTriMonMoi)));
            sb.append("----------------------------------------------------\n");
            if (chenhLech > 0) {
                sb.append(String.format("KHÁCH ĐÓNG THÊM CHÊNH LỆCH:   +%s\n", df.format(chenhLech)));
            } else if (chenhLech < 0) {
                sb.append(String.format("CỬA HÀNG HOÀN LẠI TIỀN THỪA:  -%s\n", df.format(Math.abs(chenhLech))));
            } else {
                sb.append("ĐỔI NGANG GIÁ (CHÊNH LỆCH:    0 đ)\n");
            }
        } else {
            sb.append(String.format("TIỀN HOÀN LẠI CHO KHÁCH:      %s\n", df.format(tongTienTra)));
        }

        sb.append("----------------------------------------------------\n");
        sb.append("CẬP NHẬT ĐIỂM VIP:\n");
        sb.append(String.format(" - Khấu trừ điểm thưởng đơn cũ: -%d điểm\n", diemKhauTru));
        sb.append(String.format(" - Điểm tích lũy còn lại:       %d điểm\n", diemMoi));
        sb.append("====================================================\n");
        sb.append("   CẢM ƠN QUÝ KHÁCH ĐÃ TIN TƯỞNG DỊCH VỤ CỬA HÀNG!   \n");
        sb.append("====================================================\n");

        JTextArea txtReceipt = new JTextArea(sb.toString());
        txtReceipt.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtReceipt.setEditable(false);
        txtReceipt.setBackground(new Color(254, 252, 248));
        txtReceipt.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane scroll = new JScrollPane(txtReceipt);
        scroll.setPreferredSize(new Dimension(480, 440));
        scroll.setBorder(new LineBorder(ThemeColors.BORDER_LIGHT, 1, true));

        JOptionPane.showMessageDialog(this, scroll, "IN PHIẾU ĐỔI TRẢ NHIỆT 80MM THÀNH CÔNG", JOptionPane.INFORMATION_MESSAGE,
                IconHelper.getIcon(IconHelper.RECEIPT, 24, ThemeColors.SUCCESS));

        // Sau khi hoàn tất thì làm mới lại bảng
        lamMoiGiaoDien();
    }

    /**
     * Phương thức nghiệp vụ tự động phục vụ Unit Test & Integration Test (VerifyModulesTest).
     */
    public boolean xuLyDoiTraTuDong(String maHD, String maSP, int soLuongTra, boolean nguyenTem,
                                     boolean laDoiHang, String maSPMoi, int soLuongMoi) {
        List<BanHangPOSPanel.HoaDonDaThanhToan> ds = BanHangPOSPanel.layDanhSachHoaDonDaThanhToan();
        BanHangPOSPanel.HoaDonDaThanhToan target = null;
        for (BanHangPOSPanel.HoaDonDaThanhToan hd : ds) {
            if (hd.maHoaDon.equalsIgnoreCase(maHD) || (hd.sdt != null && hd.sdt.equalsIgnoreCase(maHD))) {
                target = hd;
                break;
            }
        }
        if (target == null) return false;

        // Tìm món trong HĐ
        long donGia = 0;
        for (Object[] mon : target.dsMon) {
            String ma = mon.length > 1 && mon[1] != null ? mon[1].toString() : "";
            if (ma.equalsIgnoreCase(maSP)) {
                donGia = parseGia(mon.length > 5 ? mon[5] : "0");
                break;
            }
        }
        if (donGia <= 0) return false;

        long tienHoan = donGia * soLuongTra;
        if (nguyenTem) {
            KhoLoHangPanel.congTonKho(maSP, soLuongTra);
            BanHangPOSPanel.congTonKhoCatalog(maSP, soLuongTra);
        }

        if (laDoiHang && maSPMoi != null) {
            KhoLoHangPanel.truTonKhoTheoFEFO(maSPMoi, soLuongMoi);
            BanHangPOSPanel.truTonKhoCatalog(maSPMoi, soLuongMoi);
        }

        int diemKhauTru = (int) (tienHoan / 10000L);
        KhachHang kh = new BanHangController().timKhachHang(target.sdt);
        int diemCu = kh != null ? kh.getDiemTichLuy() : 0;
        int diemMoi = Math.max(0, diemCu - diemKhauTru);
        if (target.sdt != null) {
            BanHangController.luuHoacCapNhatKhachHang(target.sdt, target.tenKhach, diemMoi);
        }

        return true;
    }
}
