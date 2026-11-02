package views;

import models.dao.BaoCaoThongKeDAO;
import utils.IconHelper;
import utils.ThemeColors;
import utils.UserSession;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;

/** Trung tâm 7 báo biểu, đọc trực tiếp và đồng nhất từ SQL Server. */
public class BaoCaoThongKeDialog extends JDialog {
    private final DecimalFormat dfMoney = new DecimalFormat("#,### đ");
    private final DateTimeFormatter dfDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dfDateTime = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
    private final BaoCaoThongKeDAO baoCaoDAO = new BaoCaoThongKeDAO();
    private JTabbedPane tabbedPane;
    private JComboBox<String> cboKyBaoCao;
    private LocalDateTime tuNgay;
    private LocalDateTime denNgay;
    private final int tabMacDinh;

    public BaoCaoThongKeDialog(Frame parent, int defaultTabIndex) {
        super(parent, "TRUNG TÂM BÁO BIỂU & THỐNG KÊ QUẢN TRỊ - PET SHOP N07", true);
        tabMacDinh = defaultTabIndex;
        khoiTaoGiaoDien();
    }

    public static void moHopThoaiBaoCao(Component parentComp, int tabIndex) {
        Window w = SwingUtilities.getWindowAncestor(parentComp);
        new BaoCaoThongKeDialog(w instanceof Frame ? (Frame) w : null, tabIndex).setVisible(true);
    }

    private void khoiTaoGiaoDien() {
        setSize(1180, 740);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColors.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(ThemeColors.SURFACE);
        header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, ThemeColors.BORDER), new EmptyBorder(12, 18, 12, 18)));
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        JLabel title = new JLabel("HỆ THỐNG BÁO BIỂU & PHÂN TÍCH DỮ LIỆU ĐA CHIỀU", IconHelper.getPrimaryIcon(IconHelper.BAR_CHART, 20), SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel sub = new JLabel("Số liệu đọc trực tiếp từ SQL Server theo kỳ báo cáo đang chọn");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(ThemeColors.TEXT_SECONDARY);
        titles.add(title);
        titles.add(sub);

        cboKyBaoCao = new JComboBox<>(new String[]{
                "Hôm nay (" + LocalDate.now().format(dfDate) + ")", "7 ngày gần nhất",
                "Tháng " + LocalDate.now().getMonthValue() + "/" + LocalDate.now().getYear(), "Quý hiện tại"
        });
        cboKyBaoCao.setPreferredSize(new Dimension(230, 32));
        cboKyBaoCao.addActionListener(e -> taiLaiTatCaBaoCao(-1));
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        filter.setOpaque(false);
        filter.add(new JLabel("Kỳ báo cáo:"));
        filter.add(cboKyBaoCao);
        header.add(titles, BorderLayout.WEST);
        header.add(filter, BorderLayout.EAST);

        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(ThemeColors.SURFACE);
        bottom.setBorder(new MatteBorder(1, 0, 0, 0, ThemeColors.BORDER));
        JButton btnCsv = ThemeColors.createPrimaryButton("Xuất CSV mở bằng Excel", IconHelper.getIcon(IconHelper.CHECK, 14, Color.WHITE));
        JButton btnPrint = ThemeColors.createSecondaryButton("In bảng đang xem", IconHelper.getIcon(IconHelper.CLIPBOARD_CHECK, 14, ThemeColors.TEXT_SECONDARY));
        JButton btnClose = ThemeColors.createSecondaryButton("Đóng", null);
        btnCsv.addActionListener(e -> xuatBaoCaoRaCsv());
        btnPrint.addActionListener(e -> inBaoCaoDangXem());
        btnClose.addActionListener(e -> dispose());
        bottom.add(btnPrint);
        bottom.add(btnCsv);
        bottom.add(btnClose);

        add(header, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        taiLaiTatCaBaoCao(tabMacDinh);
    }

    private void taiLaiTatCaBaoCao(int tabUuTien) {
        if (tabbedPane == null || cboKyBaoCao == null) return;
        int tabDangChon = tabbedPane.getSelectedIndex();
        capNhatKhoangThoiGian();
        tabbedPane.removeAll();
        themTab(0, "Doanh thu & Lợi nhuận", IconHelper.BAR_CHART, UserSession.canAccessNhom6BaoBieuDoanhThu(), this::taoTabDoanhThuLoiNhuan, tabUuTien);
        themTab(1, "Top Bán chạy", IconHelper.TRENDING_UP, UserSession.canAccessNhom6BaoBieuThongKeDichVu(), this::taoTabTopBanChay, tabUuTien);
        themTab(2, "Hiệu suất KTV & Nhân sự", IconHelper.USERS, UserSession.canAccessNhom6BaoBieuThongKeDichVu(), this::taoTabHieuSuatNhanSu, tabUuTien);
        themTab(3, "Khách VIP & Điểm", IconHelper.USER_CHECK, UserSession.canAccessNhom5TimKiemKhachVIP(), this::taoTabKhachHangVIP, tabUuTien);
        themTab(4, "Pet Hotel & Đổi trả", IconHelper.HOME, UserSession.canAccess(MainDashboard.CARD_PET_HOTEL), this::taoTabPetHotelVaDoiTra, tabUuTien);
        themTab(5, "Cảnh báo FEFO", IconHelper.ALERT_TRIANGLE, UserSession.canAccessNhom6BaoBieuCanhBaoFEFO(), this::taoTabCanhBaoFEFO, tabUuTien);
        themTab(6, "Xuất - Nhập - Tồn", IconHelper.BOXES, UserSession.canAccessNhom6BaoBieuXuatNhapTon(), this::taoTabXuatNhapTon, tabUuTien);
        if (tabbedPane.getTabCount() == 0) {
            tabbedPane.addTab("Không có quyền", taoPanelThongBao("Tài khoản hiện tại không được cấp quyền xem báo biểu."));
        } else if (tabUuTien < 0 && tabDangChon >= 0 && tabDangChon < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(tabDangChon);
        }
    }

    private void themTab(int maTab, String title, String icon, boolean allowed, Supplier<JPanel> panelFactory, int preferred) {
        if (!allowed) return;
        tabbedPane.addTab(title, IconHelper.getIcon(icon, 14, ThemeColors.PRIMARY), panelFactory.get());
        tabbedPane.setToolTipTextAt(tabbedPane.getTabCount() - 1, "Dữ liệu " + tuNgay.toLocalDate().format(dfDate) + " - " + denNgay.minusNanos(1).toLocalDate().format(dfDate));
        if (maTab == preferred) tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

    private void capNhatKhoangThoiGian() {
        LocalDate now = LocalDate.now();
        switch (cboKyBaoCao.getSelectedIndex()) {
            case 1: tuNgay = now.minusDays(6).atStartOfDay(); denNgay = now.plusDays(1).atStartOfDay(); break;
            case 2: tuNgay = now.withDayOfMonth(1).atStartOfDay(); denNgay = tuNgay.plusMonths(1); break;
            case 3:
                int month = ((now.getMonthValue() - 1) / 3) * 3 + 1;
                tuNgay = LocalDate.of(now.getYear(), month, 1).atStartOfDay(); denNgay = tuNgay.plusMonths(3); break;
            default: tuNgay = now.atStartOfDay(); denNgay = now.plusDays(1).atStartOfDay();
        }
    }

    private JPanel taoTabDoanhThuLoiNhuan() {
        try {
            List<BaoCaoThongKeDAO.DoanhThuRow> rows = baoCaoDAO.getDoanhThu(tuNgay, denNgay);
            long tong = 0, hang = 0, dichVu = 0, giaVon = 0;
            DefaultTableModel model = taoModelKhongSua(new String[]{"Mã HĐ", "Thời gian", "Khách hàng", "Thu ngân", "Tiền hàng", "Giảm giá", "Doanh thu thuần", "Giá vốn", "Lãi gộp", "Phương thức"});
            for (BaoCaoThongKeDAO.DoanhThuRow r : rows) {
                tong += r.doanhThuThuan; hang += r.doanhThuHangHoa; dichVu += r.doanhThuDichVu; giaVon += r.giaVon;
                model.addRow(new Object[]{r.maHoaDon, r.thoiGian.format(dfDateTime), r.khachHang, r.thuNgan, dfMoney.format(r.tienHang), dfMoney.format(r.giamGia), dfMoney.format(r.doanhThuThuan), dfMoney.format(r.giaVon), dfMoney.format(r.doanhThuThuan - r.giaVon), r.phuongThuc});
            }
            long lai = tong - giaVon;
            JPanel kpis = taoHangKpi(4);
            kpis.add(taoTheKpiMini("TỔNG DOANH THU THUẦN", dfMoney.format(tong), rows.size() + " hóa đơn đã thanh toán", ThemeColors.PRIMARY_TINT, ThemeColors.PRIMARY_ACTIVE));
            kpis.add(taoTheKpiMini("DOANH THU HÀNG HÓA", dfMoney.format(hang), "Đã phân bổ giảm giá theo tỷ trọng", new Color(0xE0F2FE), new Color(0x0369A1)));
            kpis.add(taoTheKpiMini("DOANH THU DỊCH VỤ", dfMoney.format(dichVu), "Spa, Grooming và lưu trú đã xuất HĐ", new Color(0xF3E8FF), new Color(0x7E22CE)));
            kpis.add(taoTheKpiMini("LỢI NHUẬN GỘP", dfMoney.format(lai), tong > 0 ? "Biên " + (lai * 100 / tong) + "%" : "Chưa phát sinh doanh thu", ThemeColors.SUCCESS_LIGHT, ThemeColors.SUCCESS_TEXT));
            return taoPanelBaoCao(kpis, model);
        } catch (SQLException e) { return taoPanelLoi(e); }
    }

    private JPanel taoTabTopBanChay() {
        try {
            List<BaoCaoThongKeDAO.TopBanChayRow> rows = baoCaoDAO.getTopBanChay(tuNgay, denNgay);
            long tongDT = rows.stream().mapToLong(r -> r.doanhThu).sum();
            int tongSL = rows.stream().mapToInt(r -> r.soLuong).sum();
            String topSP = rows.stream().filter(r -> "Hàng hóa".equals(r.phanNhom)).findFirst().map(r -> r.ten).orElse("Chưa phát sinh");
            String topDV = rows.stream().filter(r -> "Dịch vụ".equals(r.phanNhom)).findFirst().map(r -> r.ten).orElse("Chưa phát sinh");
            DefaultTableModel model = taoModelKhongSua(new String[]{"Hạng", "Mã SP/DV", "Tên Sản phẩm / Dịch vụ", "Phân nhóm", "ĐVT", "Số lượng", "Doanh thu", "Tỷ trọng (%)"});
            int rank = 1;
            for (BaoCaoThongKeDAO.TopBanChayRow r : rows) {
                model.addRow(new Object[]{"#" + rank++, r.ma, r.ten, r.phanNhom, r.donViTinh, r.soLuong, dfMoney.format(r.doanhThu), String.format("%.1f%%", tongDT > 0 ? r.doanhThu * 100.0 / tongDT : 0)});
            }
            JPanel kpis = taoHangKpi(3);
            kpis.add(taoTheKpiMini("SẢN PHẨM BÁN CHẠY", topSP, "Xếp theo doanh thu chi tiết HĐ", ThemeColors.PRIMARY_TINT, ThemeColors.PRIMARY_ACTIVE));
            kpis.add(taoTheKpiMini("DỊCH VỤ ĐƯỢC DÙNG NHIỀU", topDV, "Dữ liệu dịch vụ đã thanh toán", new Color(0xF3E8FF), new Color(0x7E22CE)));
            kpis.add(taoTheKpiMini("TỔNG SỐ LƯỢNG TIÊU THỤ", tongSL + " Đơn vị / Lượt", rows.size() + " mặt hàng có phát sinh", ThemeColors.SUCCESS_LIGHT, ThemeColors.SUCCESS_TEXT));
            return taoPanelBaoCao(kpis, model);
        } catch (SQLException e) { return taoPanelLoi(e); }
    }

    private JPanel taoTabHieuSuatNhanSu() {
        try {
            List<BaoCaoThongKeDAO.HieuSuatNhanSuRow> rows = baoCaoDAO.getHieuSuatNhanSu(tuNgay, denNgay);
            int active = (int) rows.stream().filter(r -> r.dangHoatDong).count();
            long total = rows.stream().mapToLong(r -> r.doanhThuPhuTrach).sum();
            BaoCaoThongKeDAO.HieuSuatNhanSuRow best = rows.stream().filter(r -> r.soDichVu > 0).max((a, b) -> Long.compare(a.doanhThuPhuTrach, b.doanhThuPhuTrach)).orElse(null);
            DefaultTableModel model = taoModelKhongSua(new String[]{"Mã NV", "Họ và Tên", "Vai trò", "Số ca", "Số HĐ", "Dịch vụ hoàn tất", "Doanh thu phụ trách", "Hoa hồng", "Đánh giá"});
            for (BaoCaoThongKeDAO.HieuSuatNhanSuRow r : rows) {
                String note = !r.dangHoatDong ? "Tài khoản ngừng hoạt động" : r.soDichVu > 0 ? "Có phát sinh dịch vụ" : r.soCa > 0 ? "Có lịch trực" : "Chưa phát sinh trong kỳ";
                model.addRow(new Object[]{r.maNhanVien, r.hoTen, r.chucVu, r.soCa, r.soHoaDon, r.soDichVu, dfMoney.format(r.doanhThuPhuTrach), dfMoney.format(r.hoaHong), note});
            }
            JPanel kpis = taoHangKpi(3);
            kpis.add(taoTheKpiMini("NHÂN SỰ ĐANG HOẠT ĐỘNG", active + " / " + rows.size() + " Nhân viên", "Theo trạng thái tài khoản", ThemeColors.PRIMARY_TINT, ThemeColors.PRIMARY_ACTIVE));
            kpis.add(taoTheKpiMini("KTV DỊCH VỤ NỔI BẬT", best != null ? best.hoTen : "Chưa phát sinh", best != null ? best.soDichVu + " dịch vụ hoàn tất" : "Không có dữ liệu trong kỳ", ThemeColors.SUCCESS_LIGHT, ThemeColors.SUCCESS_TEXT));
            kpis.add(taoTheKpiMini("DOANH THU PHỤ TRÁCH", dfMoney.format(total), "Hóa đơn thu ngân + dịch vụ KTV", new Color(0xE0F2FE), new Color(0x0369A1)));
            return taoPanelBaoCao(kpis, model);
        } catch (SQLException e) { return taoPanelLoi(e); }
    }

    private JPanel taoTabKhachHangVIP() {
        try {
            List<BaoCaoThongKeDAO.KhachHangRow> rows = baoCaoDAO.getKhachHang();
            int vip = (int) rows.stream().filter(r -> r.diemTichLuy >= 100).count();
            int points = rows.stream().mapToInt(r -> r.diemTichLuy).sum();
            DefaultTableModel model = taoModelKhongSua(new String[]{"Mã KH", "Họ và Tên", "Số điện thoại", "Hạng thành viên", "Điểm hiện có", "Giá trị quy đổi", "Ưu đãi"});
            for (BaoCaoThongKeDAO.KhachHangRow r : rows) {
                model.addRow(new Object[]{r.maKhachHang, r.hoTen, r.soDienThoai, xepHang(r.diemTichLuy), r.diemTichLuy + " điểm", dfMoney.format(r.diemTichLuy * 1000L), r.diemTichLuy >= 100 ? "Giảm 5% + đổi điểm" : "100.000đ = 1 điểm"});
            }
            JPanel kpis = taoHangKpi(3);
            kpis.add(taoTheKpiMini("TỔNG KHÁCH HÀNG THÀNH VIÊN", rows.size() + " Khách hàng", "Đọc trực tiếp từ KhachHang", ThemeColors.PRIMARY_TINT, ThemeColors.PRIMARY_ACTIVE));
            kpis.add(taoTheKpiMini("KHÁCH VIP KIM CƯƠNG", vip + " Khách VIP", "Từ 100 điểm, giảm tự động 5%", new Color(0xFEF3C7), new Color(0xB45309)));
            kpis.add(taoTheKpiMini("QUỸ ĐIỂM ĐANG LƯU HÀNH", points + " điểm", "Tương đương " + dfMoney.format(points * 1000L), ThemeColors.SUCCESS_LIGHT, ThemeColors.SUCCESS_TEXT));
            return taoPanelBaoCao(kpis, model);
        } catch (SQLException e) { return taoPanelLoi(e); }
    }

    private JPanel taoTabPetHotelVaDoiTra() {
        try {
            List<BaoCaoThongKeDAO.HotelSummaryRow> rooms = baoCaoDAO.getHotelSummary();
            BaoCaoThongKeDAO.DoiTraSummary ret = baoCaoDAO.getDoiTraSummary(tuNgay, denNgay);
            int total = rooms.stream().mapToInt(r -> r.tongPhong).sum();
            int occupied = rooms.stream().mapToInt(r -> r.dangCoKhach).sum();
            int cleaning = rooms.stream().mapToInt(r -> r.dangDon).sum();
            long revenue = rooms.stream().mapToLong(r -> r.doanhThuNgayTamTinh).sum();
            DefaultTableModel model = taoModelKhongSua(new String[]{"Phân khu / Nghiệp vụ", "Tổng quy mô", "Đang sử dụng", "Đang dọn", "Còn trống", "Doanh thu / Giá trị"});
            for (BaoCaoThongKeDAO.HotelSummaryRow r : rooms) model.addRow(new Object[]{r.loaiPhong, r.tongPhong + " phòng", r.dangCoKhach + " phòng", r.dangDon + " phòng", r.phongTrong + " phòng", dfMoney.format(r.doanhThuNgayTamTinh) + " / ngày"});
            model.addRow(new Object[]{"Đổi trả trong kỳ", ret.soPhieu + " phiếu", ret.soLuongSanPham + " sản phẩm", "—", "—", dfMoney.format(ret.tongTienHoan)});
            JPanel kpis = taoHangKpi(3);
            kpis.add(taoTheKpiMini("CÔNG SUẤT PET HOTEL", occupied + " / " + total + " Phòng", total > 0 ? String.format("Tỷ lệ %.1f%% · %d phòng đang dọn", occupied * 100.0 / total, cleaning) : "Chưa có phòng", ThemeColors.PRIMARY_TINT, ThemeColors.PRIMARY_ACTIVE));
            kpis.add(taoTheKpiMini("DOANH THU LƯU TRÚ / NGÀY", dfMoney.format(revenue), "Phòng đang có khách + phụ phí", ThemeColors.SUCCESS_LIGHT, ThemeColors.SUCCESS_TEXT));
            kpis.add(taoTheKpiMini("HOÀN TIỀN TRONG KỲ", dfMoney.format(ret.tongTienHoan), ret.soPhieu + " phiếu đổi trả", new Color(0xE0F2FE), new Color(0x0369A1)));
            return taoPanelBaoCao(kpis, model);
        } catch (SQLException e) { return taoPanelLoi(e); }
    }

    private JPanel taoTabCanhBaoFEFO() {
        try {
            List<BaoCaoThongKeDAO.LoHangFefoRow> rows = baoCaoDAO.getLoHangFefo();
            int urgent = (int) rows.stream().filter(r -> r.soNgayConLai <= 30).count();
            int near = (int) rows.stream().filter(r -> r.soNgayConLai > 30 && r.soNgayConLai <= 180).count();
            DefaultTableModel model = taoModelKhongSua(new String[]{"Số Lô", "Mã SP", "Tên Hàng hóa", "Ngày sản xuất", "Hạn sử dụng", "Số ngày còn lại", "Tồn lô", "Cảnh báo FEFO"});
            for (BaoCaoThongKeDAO.LoHangFefoRow r : rows) model.addRow(new Object[]{r.soLo, r.maSanPham, r.tenSanPham, r.ngaySanXuat.format(dfDate), r.hanSuDung.format(dfDate), r.soNgayConLai, r.soLuongTon + " " + r.donViTinh, trangThaiFefo(r.soNgayConLai)});
            JPanel kpis = taoHangKpi(3);
            kpis.add(taoTheKpiMini("LÔ NGUY CẤP / HẾT HẠN", urgent + " Lô hàng", "Còn tối đa 30 ngày", ThemeColors.DANGER_LIGHT, ThemeColors.DANGER_TEXT));
            kpis.add(taoTheKpiMini("LÔ CẬN DATE 1 - 6 THÁNG", near + " Lô hàng", "Ưu tiên xuất trước theo FEFO", ThemeColors.WARNING_LIGHT, ThemeColors.WARNING_TEXT));
            kpis.add(taoTheKpiMini("TỔNG LÔ CÒN TỒN", rows.size() + " Lô hàng", "Chỉ hiển thị lô còn số lượng", ThemeColors.SUCCESS_LIGHT, ThemeColors.SUCCESS_TEXT));
            return taoPanelBaoCao(kpis, model);
        } catch (SQLException e) { return taoPanelLoi(e); }
    }

    private JPanel taoTabXuatNhapTon() {
        try {
            List<BaoCaoThongKeDAO.XuatNhapTonRow> rows = baoCaoDAO.getXuatNhapTon(tuNgay, denNgay);
            int ton = rows.stream().mapToInt(r -> r.tonCuoiKy).sum();
            int nhap = rows.stream().mapToInt(r -> r.nhapTrongKy).sum();
            int xuat = rows.stream().mapToInt(r -> r.xuatBanTrongKy).sum();
            long value = rows.stream().mapToLong(r -> r.giaTriTonTheoGiaNhap).sum();
            DefaultTableModel model = taoModelKhongSua(new String[]{"Mã SP", "Tên Hàng hóa", "ĐVT cơ sở", "Tồn đầu kỳ", "Nhập trong kỳ", "Xuất bán", "Hoàn trả", "Tồn hiện tại", "Giá bán", "Giá trị tồn theo giá nhập"});
            for (BaoCaoThongKeDAO.XuatNhapTonRow r : rows) model.addRow(new Object[]{r.maSanPham, r.tenSanPham, r.donViTinh, r.tonDauKy, r.nhapTrongKy, r.xuatBanTrongKy, r.hoanTraTrongKy, r.tonCuoiKy, dfMoney.format(r.donGiaBan), dfMoney.format(r.giaTriTonTheoGiaNhap)});
            JPanel kpis = taoHangKpi(4);
            kpis.add(taoTheKpiMini("TỔNG MÃ HÀNG", rows.size() + " Mặt hàng", "Đọc từ SanPham", ThemeColors.PRIMARY_TINT, ThemeColors.PRIMARY_ACTIVE));
            kpis.add(taoTheKpiMini("NHẬP / XUẤT TRONG KỲ", nhap + " / " + xuat, "Đơn vị cơ sở", new Color(0xE0F2FE), new Color(0x0369A1)));
            kpis.add(taoTheKpiMini("TỒN HIỆN TẠI", ton + " Đơn vị", "Tổng tồn các lô còn lại", ThemeColors.WARNING_LIGHT, ThemeColors.WARNING_TEXT));
            kpis.add(taoTheKpiMini("GIÁ TRỊ TỒN THEO GIÁ NHẬP", dfMoney.format(value), "Không dùng giá bán niêm yết", ThemeColors.SUCCESS_LIGHT, ThemeColors.SUCCESS_TEXT));
            return taoPanelBaoCao(kpis, model);
        } catch (SQLException e) { return taoPanelLoi(e); }
    }

    private String xepHang(int p) { return p >= 100 ? "VIP Kim Cương" : p >= 50 ? "Hạng Vàng" : p >= 20 ? "Hạng Bạc" : "Thành viên Mới"; }
    private String trangThaiFefo(long d) { return d < 0 ? "ĐÃ HẾT HẠN" : d <= 30 ? "NGUY CẤP - CẦN XỬ LÝ" : d <= 180 ? "CẬN DATE - ƯU TIÊN XUẤT" : "HẠN DÙNG AN TOÀN"; }

    private void xuatBaoCaoRaCsv() {
        try {
            JTable table = timJTableTrongComponent(tabbedPane.getSelectedComponent());
            if (table == null) throw new IllegalStateException("Tab hiện tại không có bảng dữ liệu để xuất.");
            String name = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).replaceAll("[^a-zA-Z0-9_\\p{L}]", "_");
            File dir = new File("export");
            if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Không tạo được thư mục export.");
            File file = new File(dir, "BaoCao_" + name + "_" + System.currentTimeMillis() + ".csv");
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                pw.write('\ufeff');
                pw.println(csv("BÁO CÁO THỐNG KÊ QUẢN TRỊ - PET SHOP N07"));
                pw.println(csv("Phân hệ: " + tabbedPane.getTitleAt(tabbedPane.getSelectedIndex())));
                pw.println(csv("Kỳ dữ liệu: " + tuNgay.toLocalDate().format(dfDate) + " - " + denNgay.minusNanos(1).toLocalDate().format(dfDate)));
                pw.println();
                for (int c = 0; c < table.getColumnCount(); c++) pw.print(csv(table.getColumnName(c)) + (c + 1 < table.getColumnCount() ? "," : ""));
                pw.println();
                for (int r = 0; r < table.getRowCount(); r++) {
                    for (int c = 0; c < table.getColumnCount(); c++) pw.print(csv(String.valueOf(table.getValueAt(r, c))) + (c + 1 < table.getColumnCount() ? "," : ""));
                    pw.println();
                }
            }
            int option = JOptionPane.showConfirmDialog(this, "Đã xuất CSV thành công:\n" + file.getAbsolutePath() + "\n\nMở file ngay?", "Xuất báo cáo", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (option == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Không thể xuất báo cáo: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
    }

    private void inBaoCaoDangXem() {
        try {
            JTable table = timJTableTrongComponent(tabbedPane.getSelectedComponent());
            if (table == null) throw new IllegalStateException("Tab hiện tại không có bảng để in.");
            boolean done = table.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex())), new MessageFormat("Kỳ " + tuNgay.toLocalDate().format(dfDate) + " - " + denNgay.minusNanos(1).toLocalDate().format(dfDate) + " · Trang {0}"));
            if (done) JOptionPane.showMessageDialog(this, "Đã gửi bảng tới máy in.", "In báo biểu", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Không thể in báo biểu: " + e.getMessage(), "Lỗi in", JOptionPane.ERROR_MESSAGE); }
    }

    private String csv(String s) { return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\""; }
    private JPanel taoPanelBaoCao(JPanel kpis, DefaultTableModel model) {
        JPanel p = new JPanel(new BorderLayout(0, 10)); p.setOpaque(false); p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(kpis, BorderLayout.NORTH); p.add(new JScrollPane(taoBangBaoCao(model)), BorderLayout.CENTER); return p;
    }
    private JPanel taoPanelLoi(Exception e) { return taoPanelThongBao("Không thể đọc dữ liệu báo cáo từ SQL Server: " + e.getMessage()); }
    private JPanel taoPanelThongBao(String msg) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel l = new JLabel("<html><div style='text-align:center'>" + msg + "</div></html>", SwingConstants.CENTER); l.setForeground(ThemeColors.DANGER_TEXT); l.setFont(new Font("Segoe UI", Font.BOLD, 13)); p.add(l); return p;
    }
    private JPanel taoHangKpi(int columns) { JPanel p = new JPanel(new GridLayout(1, columns, 12, 0)); p.setOpaque(false); return p; }
    private JPanel taoTheKpiMini(String title, String value, String sub, Color bg, Color fg) {
        JPanel card = new JPanel(new GridLayout(3, 1, 0, 2)); card.setBackground(bg); card.setBorder(new CompoundBorder(new LineBorder(ThemeColors.BORDER, 1, true), new EmptyBorder(10, 14, 10, 14)));
        JLabel a = new JLabel(title); a.setFont(new Font("Segoe UI", Font.BOLD, 11)); a.setForeground(ThemeColors.TEXT_SECONDARY);
        JLabel b = new JLabel(value); b.setFont(new Font("Segoe UI", Font.BOLD, 16)); b.setForeground(fg);
        JLabel c = new JLabel(sub); c.setFont(new Font("Segoe UI", Font.PLAIN, 11)); c.setForeground(ThemeColors.TEXT_SECONDARY);
        card.add(a); card.add(b); card.add(c); return card;
    }
    private DefaultTableModel taoModelKhongSua(String[] cols) { return new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } }; }
    private JTable taoBangBaoCao(DefaultTableModel model) {
        JTable t = new JTable(model); t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); t.setRowHeight(32); t.setFont(new Font("Segoe UI", Font.PLAIN, 12)); t.setShowHorizontalLines(true); t.setShowVerticalLines(false); t.setGridColor(ThemeColors.BORDER_LIGHT); t.setSelectionBackground(ThemeColors.PRIMARY_SOFT); t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12)); t.getTableHeader().setBackground(ThemeColors.PRIMARY_TINT); t.getTableHeader().setPreferredSize(new Dimension(0, 34));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer(); center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < t.getColumnCount(); i++) { t.getColumnModel().getColumn(i).setPreferredWidth(i == 1 || i == 2 ? 180 : 125); if (i == 0) t.getColumnModel().getColumn(i).setCellRenderer(center); }
        return t;
    }
    private JTable timJTableTrongComponent(Component c) {
        if (c instanceof JTable) return (JTable) c;
        if (c instanceof Container) for (Component child : ((Container) c).getComponents()) { JTable t = timJTableTrongComponent(child); if (t != null) return t; }
        return null;
    }
}
