package views;

import utils.IconHelper;
import utils.ThemeColors;
import utils.UserSession;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Phân hệ Quản lý Khách sạn Thú cưng (Pet Hotel & Boarding Management).
 * - Sơ đồ Phòng / Chuồng Lưu trú dạng Lưới (Room Grid Matrix).
 * - Phân chia 3 hạng phòng: Tiêu chuẩn, VIP Suite, Condo cho Mèo.
 * - Trực quan trạng thái Happy Hues 15: Phòng Trống (#c3f0ca), Đang có khách (#ffc6c7), Đang dọn dẹp (#fef3c7).
 * - Quy trình nghiệp vụ: Check-in, Nhật ký dinh dưỡng & chăm sóc trong ngày, Check-out tự động đẩy sang POS.
 */
public class PetHotelPanel extends JPanel {

    private final DecimalFormat dfMoney = new DecimalFormat("#,### đ");
    private final DateTimeFormatter dtfDisplay = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter dtfShort = DateTimeFormatter.ofPattern("HH:mm - dd/MM");

    // ==========================================
    // DATA MODEL CHO PHÒNG LƯU TRÚ PET HOTEL
    // ==========================================
    public static class PhongLuuTru {
        public String maPhong;
        public String loaiPhong;       // "Phòng Tiêu chuẩn", "Phòng VIP Suite", "Condo cho Mèo"
        public long donGiaNgay;
        public String trangThai;       // "TRỐNG", "ĐANG CÓ KHÁCH", "ĐANG DỌN DẸP"
        public String tenThuCung;
        public String giongLoai;
        public String tenChu;
        public String sdtChu;
        public LocalDateTime thoiGianCheckIn;
        public LocalDateTime duKienCheckOut;
        public String ghiChuChamSoc;
        public boolean daChoAnSang;
        public boolean daChoAnToi;
        public boolean daDiDaoVeSinh;
        public long phiDichVuPhatSinh;

        public PhongLuuTru(String maPhong, String loaiPhong, long donGiaNgay, String trangThai) {
            this.maPhong = maPhong;
            this.loaiPhong = loaiPhong;
            this.donGiaNgay = donGiaNgay;
            this.trangThai = trangThai;
            this.tenThuCung = "";
            this.giongLoai = "";
            this.tenChu = "";
            this.sdtChu = "";
            this.ghiChuChamSoc = "";
            this.daChoAnSang = false;
            this.daChoAnToi = false;
            this.daDiDaoVeSinh = false;
            this.phiDichVuPhatSinh = 0L;
        }
    }

    private static final List<PhongLuuTru> DANH_SACH_PHONG = new ArrayList<>();

    static {
        dongBoPhongTuDatabase();
    }

    public static void dongBoPhongTuDatabase() {
        try {
            models.dao.PhongLuuTruDAO dao = new models.dao.PhongLuuTruDAO();
            List<PhongLuuTru> dbList = dao.getAllPhong();
            if (dbList != null && !dbList.isEmpty()) {
                DANH_SACH_PHONG.clear();
                DANH_SACH_PHONG.addAll(dbList);
                return;
            }
        } catch (Exception e) {
            System.err.println("[PetHotelPanel] Lỗi tải phòng lưu trú CSDL: " + e.getMessage());
        }
        khoiTaoDuLieuMauPetHotel();
    }

    public static void luuPhongXuongDB(PhongLuuTru p) {
        if (p == null) return;
        try {
            new models.dao.PhongLuuTruDAO().updatePhong(p);
        } catch (Exception e) {
            System.err.println("[PetHotelPanel] Lỗi cập nhật phòng CSDL: " + e.getMessage());
        }
    }

    private static void khoiTaoDuLieuMauPetHotel() {
        if (!DANH_SACH_PHONG.isEmpty()) return;

        // 1. Dãy Phòng Tiêu Chuẩn (Standard Room - 150k/ngày)
        PhongLuuTru p101 = new PhongLuuTru("P101", "Phòng Tiêu chuẩn", 150000, "ĐANG CÓ KHÁCH");
        p101.tenThuCung = "Bim Bim";
        p101.giongLoai = "Corgi (8.5kg)";
        p101.tenChu = "Nguyễn Văn An";
        p101.sdtChu = "0988111222";
        p101.thoiGianCheckIn = LocalDateTime.now().minusDays(2).withHour(9).withMinute(0);
        p101.duKienCheckOut = LocalDateTime.now().plusDays(1).withHour(17).withMinute(0);
        p101.ghiChuChamSoc = "Ăn hạt Royal Canin ngâm mềm, dắt đi dạo 2 lần/ngày, sợ sấm sét";
        p101.daChoAnSang = true;
        p101.daChoAnToi = true;
        p101.daDiDaoVeSinh = true;
        DANH_SACH_PHONG.add(p101);

        PhongLuuTru p102 = new PhongLuuTru("P102", "Phòng Tiêu chuẩn", 150000, "TRỐNG");
        DANH_SACH_PHONG.add(p102);

        PhongLuuTru p103 = new PhongLuuTru("P103", "Phòng Tiêu chuẩn", 150000, "ĐANG CÓ KHÁCH");
        p103.tenThuCung = "Bơ";
        p103.giongLoai = "Poodle Tiny (3.2kg)";
        p103.tenChu = "Trần Thị Mai";
        p103.sdtChu = "0977333444";
        p103.thoiGianCheckIn = LocalDateTime.now().minusDays(1).withHour(10).withMinute(30);
        p103.duKienCheckOut = LocalDateTime.now().plusDays(2).withHour(12).withMinute(0);
        p103.ghiChuChamSoc = "Ăn kèm pate gà, chải lông mỗi chiều, uống men tiêu hóa buổi sáng";
        p103.daChoAnSang = true;
        p103.daChoAnToi = false;
        p103.daDiDaoVeSinh = true;
        DANH_SACH_PHONG.add(p103);

        PhongLuuTru p104 = new PhongLuuTru("P104", "Phòng Tiêu chuẩn", 150000, "ĐANG DỌN DẸP");
        DANH_SACH_PHONG.add(p104);

        // 2. Dãy Phòng VIP Suite (Máy lạnh, camera 24/7 - 250k/ngày)
        PhongLuuTru v201 = new PhongLuuTru("V201", "Phòng VIP Suite", 250000, "ĐANG CÓ KHÁCH");
        v201.tenThuCung = "Lu Lu";
        v201.giongLoai = "Golden Retriever (24kg)";
        v201.tenChu = "Phạm Hoàng Long";
        v201.sdtChu = "0918776655";
        v201.thoiGianCheckIn = LocalDateTime.now().minusDays(3).withHour(14).withMinute(0);
        v201.duKienCheckOut = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        v201.ghiChuChamSoc = "Camera VIP bật 24/7 cho chủ xem, ăn 3 bữa, uống canxi kèm sữa chua";
        v201.daChoAnSang = true;
        v201.daChoAnToi = true;
        v201.daDiDaoVeSinh = true;
        DANH_SACH_PHONG.add(v201);

        PhongLuuTru v202 = new PhongLuuTru("V202", "Phòng VIP Suite", 250000, "TRỐNG");
        DANH_SACH_PHONG.add(v202);

        PhongLuuTru v203 = new PhongLuuTru("V203", "Phòng VIP Suite", 250000, "TRỐNG");
        DANH_SACH_PHONG.add(v203);

        PhongLuuTru v204 = new PhongLuuTru("V204", "Phòng VIP Suite", 250000, "ĐANG CÓ KHÁCH");
        v204.tenThuCung = "Sammy";
        v204.giongLoai = "Samoyed (19kg)";
        v204.tenChu = "Đỗ Quỳnh Chi";
        v204.sdtChu = "0905123456";
        v204.thoiGianCheckIn = LocalDateTime.now().minusHours(8);
        v204.duKienCheckOut = LocalDateTime.now().plusDays(3).withHour(16).withMinute(0);
        v204.ghiChuChamSoc = "Bật điều hòa 22 độ C liên tục, chải lông kĩ tránh vón búi";
        v204.daChoAnSang = true;
        v204.daChoAnToi = false;
        v204.daDiDaoVeSinh = true;
        DANH_SACH_PHONG.add(v204);

        // 3. Dãy Condo Cho Mèo (Cat Condo nhiều tầng - 200k/ngày)
        PhongLuuTru c301 = new PhongLuuTru("C301", "Condo cho Mèo", 200000, "ĐANG CÓ KHÁCH");
        c301.tenThuCung = "Miu Miu";
        c301.giongLoai = "Mèo Anh Lông Ngắn (4.5kg)";
        c301.tenChu = "Lê Hoàng Yến";
        c301.sdtChu = "0912345678";
        c301.thoiGianCheckIn = LocalDateTime.now().minusDays(1).withHour(8).withMinute(0);
        c301.duKienCheckOut = LocalDateTime.now().plusDays(2).withHour(18).withMinute(0);
        c301.ghiChuChamSoc = "Ăn hạt Orijen + súp thưởng Ciao lúc 15h, dọn khay cát 2 lần/ngày";
        c301.daChoAnSang = true;
        c301.daChoAnToi = false;
        c301.daDiDaoVeSinh = true;
        DANH_SACH_PHONG.add(c301);

        PhongLuuTru c302 = new PhongLuuTru("C302", "Condo cho Mèo", 200000, "TRỐNG");
        DANH_SACH_PHONG.add(c302);

        PhongLuuTru c303 = new PhongLuuTru("C303", "Condo cho Mèo", 200000, "TRỐNG");
        DANH_SACH_PHONG.add(c303);

        PhongLuuTru c304 = new PhongLuuTru("C304", "Condo cho Mèo", 200000, "TRỐNG");
        DANH_SACH_PHONG.add(c304);
    }

    // UI Components
    private JPanel pnlGridMatrix;
    private JPanel pnlDetailsContainer;
    private JLabel lblKpiTongPhong;
    private JLabel lblKpiDangLuuTru;
    private JLabel lblKpiPhongTrong;
    private JLabel lblKpiDangDonDep;

    private String boLocLoaiPhong = "TẤT CẢ";
    private String boLocTrangThai = "TẤT CẢ";
    private PhongLuuTru phongDangChon = null;

    public PetHotelPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(ThemeColors.BACKGROUND);
        setBorder(new EmptyBorder(12, 16, 14, 16));

        khoiTaoGiaoDien();
        if (!DANH_SACH_PHONG.isEmpty()) {
            phongDangChon = DANH_SACH_PHONG.get(0);
            capNhatKhungChiTiet(phongDangChon);
        }
        capNhatSoLieuKPI();
        locVaHienThiGridPhong();
    }

    private void khoiTaoGiaoDien() {
        // 1. Vùng Header & KPI trên đỉnh
        JPanel pnlNorth = new JPanel(new BorderLayout(0, 10));
        pnlNorth.setOpaque(false);

        // Title Header
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ LƯU TRÚ THÚ CƯNG (PET HOTEL & BOARDING)",
                IconHelper.getPrimaryIcon(IconHelper.HOME, 22), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        lblTitle.setIconTextGap(10);

        JLabel lblSub = new JLabel("Sơ đồ phòng/chuồng thời gian thực, nhật ký dinh dưỡng và quy trình check-in / check-out 1 chạm", SwingConstants.LEFT);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeColors.TEXT_SECONDARY);

        pnlTitle.add(lblTitle, BorderLayout.NORTH);
        pnlTitle.add(lblSub, BorderLayout.SOUTH);

        // 4 Thẻ KPI trực quan
        JPanel pnlKPIs = new JPanel(new GridLayout(1, 4, 12, 0));
        pnlKPIs.setOpaque(false);

        lblKpiTongPhong = new JLabel("12", SwingConstants.LEFT);
        lblKpiDangLuuTru = new JLabel("5", SwingConstants.LEFT);
        lblKpiPhongTrong = new JLabel("6", SwingConstants.LEFT);
        lblKpiDangDonDep = new JLabel("1", SwingConstants.LEFT);

        pnlKPIs.add(taoTheKpi("TỔNG SỐ PHÒNG", lblKpiTongPhong, "Quy mô 12 chuồng tiêu chuẩn/VIP", ThemeColors.SURFACE, ThemeColors.TEXT_PRIMARY, IconHelper.BOXES));
        pnlKPIs.add(taoTheKpi("ĐANG CÓ KHÁCH", lblKpiDangLuuTru, "Công suất 42% đang lưu trú", new Color(0xFF, 0xEB, 0xEE), new Color(0x9E, 0x22, 0x38), IconHelper.PAW));
        pnlKPIs.add(taoTheKpi("PHÒNG TRỐNG", lblKpiPhongTrong, "Sẵn sàng tiếp nhận ngay", new Color(0xED, 0xFA, 0xEE), new Color(0x23, 0x4E, 0x27), IconHelper.CHECK));
        pnlKPIs.add(taoTheKpi("ĐANG DỌN DẸP", lblKpiDangDonDep, "Khử trùng tia cực tím & đệm", new Color(0xFE, 0xF3, 0xC7), new Color(0x92, 0x40, 0x0E), IconHelper.REFRESH));

        // Thanh công cụ bộ lọc và nút tác vụ
        JPanel pnlToolbar = new JPanel(new BorderLayout(10, 0));
        pnlToolbar.setOpaque(false);

        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlFilters.setOpaque(false);

        String[] loaiPhongs = {"TẤT CẢ", "Phòng Tiêu chuẩn", "Phòng VIP Suite", "Condo cho Mèo"};
        for (String lp : loaiPhongs) {
            JButton btnFilter = ThemeColors.createSecondaryButton(lp, null);
            btnFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnFilter.addActionListener(e -> {
                boLocLoaiPhong = lp;
                locVaHienThiGridPhong();
            });
            pnlFilters.add(btnFilter);
        }

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        pnlActions.setOpaque(false);

        JButton btnCheckInMoi = ThemeColors.createPrimaryButton("+ Tiếp nhận gửi thú cưng", IconHelper.getIcon(IconHelper.PLUS, 15, ThemeColors.TEXT_PRIMARY));
        btnCheckInMoi.addActionListener(e -> moDialogCheckIn(null));

        JButton btnLamMoi = ThemeColors.createSecondaryButton("Làm mới", IconHelper.getIcon(IconHelper.REFRESH, 14, ThemeColors.TEXT_SECONDARY));
        btnLamMoi.addActionListener(e -> {
            capNhatSoLieuKPI();
            locVaHienThiGridPhong();
            if (phongDangChon != null) capNhatKhungChiTiet(phongDangChon);
        });

        pnlActions.add(btnCheckInMoi);
        pnlActions.add(btnLamMoi);

        pnlToolbar.add(pnlFilters, BorderLayout.WEST);
        pnlToolbar.add(pnlActions, BorderLayout.EAST);

        pnlNorth.add(pnlTitle, BorderLayout.NORTH);
        pnlNorth.add(pnlKPIs, BorderLayout.CENTER);
        pnlNorth.add(pnlToolbar, BorderLayout.SOUTH);

        add(pnlNorth, BorderLayout.NORTH);

        // 2. Vùng Nội dung: Cột Trái (Sơ đồ Lưới thu gọn) + Cột Phải (Chi tiết & Form nhập thông tin mở rộng)
        JPanel pnlCenter = new JPanel(new BorderLayout(12, 0));
        pnlCenter.setOpaque(false);

        // Sơ đồ Lưới phòng (Room Grid Matrix thu gọn)
        pnlGridMatrix = new JPanel(new GridLayout(0, 3, 8, 8));
        pnlGridMatrix.setOpaque(false);

        JPanel pnlGridWrapper = new JPanel(new BorderLayout());
        pnlGridWrapper.setOpaque(false);
        pnlGridWrapper.add(pnlGridMatrix, BorderLayout.NORTH);

        JScrollPane scrollGrid = new JScrollPane(pnlGridWrapper);
        scrollGrid.setBorder(null);
        scrollGrid.setOpaque(false);
        scrollGrid.getViewport().setOpaque(false);
        scrollGrid.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollGrid.getVerticalScrollBar().setUnitIncrement(16);

        // Bảng Chi tiết & Nhập thông tin bên phải mở rộng không gian
        pnlDetailsContainer = new JPanel(new BorderLayout());
        pnlDetailsContainer.setPreferredSize(new Dimension(475, 0));
        pnlDetailsContainer.setBackground(ThemeColors.SURFACE);
        pnlDetailsContainer.setBorder(ThemeColors.createCardBorder(12, 16, 12, 16));

        pnlCenter.add(scrollGrid, BorderLayout.CENTER);
        pnlCenter.add(pnlDetailsContainer, BorderLayout.EAST);

        add(pnlCenter, BorderLayout.CENTER);
    }

    private JPanel taoTheKpi(String tieuDe, JLabel lblGiaTri, String moTa, Color bgColor, Color textColor, String iconName) {
        JPanel card = new JPanel(new BorderLayout(6, 4));
        card.setBackground(bgColor);
        card.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblTitle = new JLabel(tieuDe);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(ThemeColors.TEXT_MUTED);

        JLabel lblIcon = new JLabel(IconHelper.getIcon(iconName, 16, textColor));

        pnlTop.add(lblTitle, BorderLayout.WEST);
        pnlTop.add(lblIcon, BorderLayout.EAST);

        lblGiaTri.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblGiaTri.setForeground(textColor);

        JLabel lblSub = new JLabel(moTa);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(ThemeColors.TEXT_SECONDARY);

        card.add(pnlTop, BorderLayout.NORTH);
        card.add(lblGiaTri, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);
        return card;
    }

    public void capNhatSoLieuKPI() {
        int tong = DANH_SACH_PHONG.size();
        int dangCoKhach = 0;
        int trong = 0;
        int donDep = 0;

        for (PhongLuuTru p : DANH_SACH_PHONG) {
            if ("ĐANG CÓ KHÁCH".equalsIgnoreCase(p.trangThai)) dangCoKhach++;
            else if ("TRỐNG".equalsIgnoreCase(p.trangThai)) trong++;
            else if ("ĐANG DỌN DẸP".equalsIgnoreCase(p.trangThai)) donDep++;
        }

        lblKpiTongPhong.setText(String.valueOf(tong));
        lblKpiDangLuuTru.setText(String.valueOf(dangCoKhach));
        lblKpiPhongTrong.setText(String.valueOf(trong));
        lblKpiDangDonDep.setText(String.valueOf(donDep));
    }

    private void locVaHienThiGridPhong() {
        pnlGridMatrix.removeAll();

        for (PhongLuuTru p : DANH_SACH_PHONG) {
            boolean matchLoai = "TẤT CẢ".equalsIgnoreCase(boLocLoaiPhong) || p.loaiPhong.equalsIgnoreCase(boLocLoaiPhong);
            boolean matchTrangThai = "TẤT CẢ".equalsIgnoreCase(boLocTrangThai) || p.trangThai.equalsIgnoreCase(boLocTrangThai);

            if (matchLoai && matchTrangThai) {
                pnlGridMatrix.add(taoThePhongCard(p));
            }
        }

        pnlGridMatrix.revalidate();
        pnlGridMatrix.repaint();
    }

    private JPanel taoThePhongCard(PhongLuuTru p) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setPreferredSize(new Dimension(170, 112));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        boolean isOccupied = "ĐANG CÓ KHÁCH".equalsIgnoreCase(p.trangThai);
        boolean isCleaning = "ĐANG DỌN DẸP".equalsIgnoreCase(p.trangThai);
        boolean isSelected = (phongDangChon != null && phongDangChon.maPhong.equals(p.maPhong));

        Color cardBg = isOccupied ? new Color(0xFF, 0xF5, 0xF5) : (isCleaning ? new Color(0xFF, 0xFB, 0xEB) : ThemeColors.SURFACE);
        card.setBackground(cardBg);

        Border baseBorder = isSelected
                ? new CompoundBorder(new LineBorder(ThemeColors.PRIMARY, 2, true), new EmptyBorder(6, 8, 6, 8))
                : new CompoundBorder(new LineBorder(ThemeColors.BORDER, 1, true), new EmptyBorder(7, 9, 7, 9));
        card.setBorder(baseBorder);

        // Header thẻ phòng: Mã phòng + Badge trạng thái gọn
        JPanel pnlHeader = new JPanel(new BorderLayout(4, 0));
        pnlHeader.setOpaque(false);

        JLabel lblMaPhong = new JLabel(p.maPhong);
        lblMaPhong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMaPhong.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel lblBadge;
        if (isOccupied) {
            lblBadge = ThemeColors.createBadge("CÓ KHÁCH", new Color(0xFF, 0xC6, 0xC7), ThemeColors.TEXT_PRIMARY);
        } else if (isCleaning) {
            lblBadge = ThemeColors.createBadge("ĐANG DỌN", new Color(0xFE, 0xF3, 0xC7), new Color(0x92, 0x40, 0x0E));
        } else {
            lblBadge = ThemeColors.createBadge("TRỐNG", new Color(0xC3, 0xF0, 0xCA), new Color(0x23, 0x4E, 0x27));
        }

        pnlHeader.add(lblMaPhong, BorderLayout.WEST);
        pnlHeader.add(lblBadge, BorderLayout.EAST);

        // Nội dung giữa thu gọn
        JPanel pnlCenter = new JPanel(new GridLayout(isOccupied ? 3 : 2, 1, 0, 1));
        pnlCenter.setOpaque(false);

        JLabel lblLoaiGia = new JLabel(p.loaiPhong + " · " + dfMoney.format(p.donGiaNgay) + "/ng");
        lblLoaiGia.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblLoaiGia.setForeground(ThemeColors.TEXT_MUTED);
        pnlCenter.add(lblLoaiGia);

        if (isOccupied) {
            JLabel lblBe = new JLabel("Bé: " + p.tenThuCung);
            lblBe.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblBe.setForeground(ThemeColors.TEXT_PRIMARY);

            String timeStr = "Vào: " + (p.thoiGianCheckIn != null ? p.thoiGianCheckIn.format(dtfShort) : "—");
            JLabel lblTime = new JLabel(timeStr);
            lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblTime.setForeground(ThemeColors.TEXT_SECONDARY);

            pnlCenter.add(lblBe);
            pnlCenter.add(lblTime);
        } else if (isCleaning) {
            JLabel lblClean = new JLabel("Khử trùng UV & thay đệm");
            lblClean.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            lblClean.setForeground(new Color(0x92, 0x40, 0x0E));
            pnlCenter.add(lblClean);
        } else {
            JLabel lblSanSang = new JLabel("Sẵn sàng nhận thú cưng");
            lblSanSang.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblSanSang.setForeground(new Color(0x23, 0x4E, 0x27));
            pnlCenter.add(lblSanSang);
        }

        // Footer thẻ: Gợi ý gọn
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setOpaque(false);

        JLabel lblActionHint = new JLabel(isOccupied ? "Chi tiết & Nhật ký >" : (isCleaning ? "Hoàn tất dọn >" : "+ Nhập thông tin >"));
        lblActionHint.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblActionHint.setForeground(isOccupied ? ThemeColors.PRIMARY_ACTIVE : (isCleaning ? new Color(0x92, 0x40, 0x0E) : ThemeColors.PRIMARY));
        pnlFooter.add(lblActionHint, BorderLayout.EAST);

        card.add(pnlHeader, BorderLayout.NORTH);
        card.add(pnlCenter, BorderLayout.CENTER);
        card.add(pnlFooter, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                phongDangChon = p;
                locVaHienThiGridPhong();
                capNhatKhungChiTiet(p);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!card.getBackground().equals(ThemeColors.PRIMARY_TINT)) {
                    card.setBorder(new CompoundBorder(
                            new LineBorder(ThemeColors.PRIMARY_HOVER, 1, true),
                            new EmptyBorder(7, 9, 7, 9)
                    ));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (phongDangChon != null && phongDangChon.maPhong.equals(p.maPhong)) {
                    card.setBorder(new CompoundBorder(
                            new LineBorder(ThemeColors.PRIMARY, 2, true),
                            new EmptyBorder(6, 8, 6, 8)
                    ));
                } else {
                    card.setBorder(new CompoundBorder(
                            new LineBorder(ThemeColors.BORDER, 1, true),
                            new EmptyBorder(7, 9, 7, 9)
                    ));
                }
            }
        });

        return card;
    }

    private void capNhatKhungChiTiet(PhongLuuTru p) {
        pnlDetailsContainer.removeAll();

        if (p == null) {
            pnlDetailsContainer.revalidate();
            pnlDetailsContainer.repaint();
            return;
        }

        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(6, 4, 8, 4));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(2, 0, 4, 0);

        // 1. Header chi tiết phòng (có khoảng đệm trên rõ ràng, không bị cắt chữ)
        JPanel pnlHead = new JPanel(new BorderLayout(8, 0));
        pnlHead.setOpaque(false);

        JLabel lblMa = new JLabel("PHÒNG " + p.maPhong + " · " + p.loaiPhong);
        lblMa.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblMa.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel lblBadgeStatus;
        if ("ĐANG CÓ KHÁCH".equalsIgnoreCase(p.trangThai)) {
            lblBadgeStatus = ThemeColors.createBadge("ĐANG LƯU TRÚ", new Color(0xFF, 0xC6, 0xC7), ThemeColors.TEXT_PRIMARY);
        } else if ("ĐANG DỌN DẸP".equalsIgnoreCase(p.trangThai)) {
            lblBadgeStatus = ThemeColors.createBadge("ĐANG DỌN", new Color(0xFE, 0xF3, 0xC7), new Color(0x92, 0x40, 0x0E));
        } else {
            lblBadgeStatus = ThemeColors.createBadge("SẴN SÀNG", new Color(0xC3, 0xF0, 0xCA), new Color(0x23, 0x4E, 0x27));
        }

        pnlHead.add(lblMa, BorderLayout.WEST);
        pnlHead.add(lblBadgeStatus, BorderLayout.EAST);
        pnl.add(pnlHead, gbc);

        // Giá phòng
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel lblGia = new JLabel("Đơn giá lưu trú: " + dfMoney.format(p.donGiaNgay) + " / ngày (24 giờ)");
        lblGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGia.setForeground(ThemeColors.TEXT_SECONDARY);
        pnl.add(lblGia, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        JSeparator sep1 = new JSeparator();
        sep1.setForeground(ThemeColors.BORDER);
        pnl.add(sep1, gbc);

        if ("ĐANG CÓ KHÁCH".equalsIgnoreCase(p.trangThai)) {
            // Thông tin thú cưng & chủ nuôi (dùng taoRowThongTin căn lề trái x=0 chuẩn xác)
            gbc.gridy++;
            gbc.insets = new Insets(0, 0, 4, 0);
            JLabel lblSecThuCung = new JLabel("THÔNG TIN BÉ CƯNG & KHÁCH HÀNG");
            lblSecThuCung.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblSecThuCung.setForeground(ThemeColors.PRIMARY_ACTIVE);
            pnl.add(lblSecThuCung, gbc);

            gbc.insets = new Insets(1, 0, 1, 0);
            gbc.gridy++;
            pnl.add(taoRowThongTin("Tên bé cưng:", p.tenThuCung + " (" + p.giongLoai + ")"), gbc);
            gbc.gridy++;
            pnl.add(taoRowThongTin("Chủ sở hữu:", p.tenChu + " - " + p.sdtChu), gbc);
            gbc.gridy++;
            pnl.add(taoRowThongTin("Thời gian gửi:", p.thoiGianCheckIn != null ? p.thoiGianCheckIn.format(dtfDisplay) : "—"), gbc);
            gbc.gridy++;
            pnl.add(taoRowThongTin("Dự kiến đón:", p.duKienCheckOut != null ? p.duKienCheckOut.format(dtfDisplay) : "—"), gbc);
            gbc.gridy++;
            gbc.insets = new Insets(1, 0, 8, 0);
            pnl.add(taoRowThongTin("Dặn dò đặc biệt:", p.ghiChuChamSoc != null && !p.ghiChuChamSoc.isEmpty() ? p.ghiChuChamSoc : "Không có"), gbc);

            // Form chỉnh sửa trực tiếp ngay trên khung bên phải
            gbc.gridy++;
            gbc.insets = new Insets(0, 0, 6, 0);
            JPanel pnlQuickEdit = new JPanel(new GridBagLayout());
            pnlQuickEdit.setBackground(ThemeColors.BACKGROUND);
            pnlQuickEdit.setBorder(new CompoundBorder(
                    new LineBorder(ThemeColors.BORDER, 1, true),
                    new EmptyBorder(8, 10, 8, 10)
            ));
            GridBagConstraints ge = new GridBagConstraints();
            ge.fill = GridBagConstraints.HORIZONTAL;
            ge.insets = new Insets(2, 2, 2, 2);

            JTextField txtEditBe = new JTextField(p.tenThuCung);
            JTextField txtEditChu = new JTextField(p.tenChu);
            JTextField txtEditSdt = new JTextField(p.sdtChu);
            JTextField txtEditNote = new JTextField(p.ghiChuChamSoc != null ? p.ghiChuChamSoc : "");

            ge.gridx = 0; ge.gridy = 0; ge.weightx = 0;
            pnlQuickEdit.add(new JLabel("Bé cưng:"), ge);
            ge.gridx = 1; ge.weightx = 0.5;
            pnlQuickEdit.add(txtEditBe, ge);
            ge.gridx = 2; ge.weightx = 0;
            pnlQuickEdit.add(new JLabel("SĐT:"), ge);
            ge.gridx = 3; ge.weightx = 0.5;
            pnlQuickEdit.add(txtEditSdt, ge);

            ge.gridx = 0; ge.gridy = 1; ge.weightx = 0;
            pnlQuickEdit.add(new JLabel("Chủ nuôi:"), ge);
            ge.gridx = 1; ge.gridwidth = 3; ge.weightx = 1.0;
            pnlQuickEdit.add(txtEditChu, ge);

            ge.gridx = 0; ge.gridy = 2; ge.gridwidth = 1; ge.weightx = 0;
            pnlQuickEdit.add(new JLabel("Dặn dò:"), ge);
            ge.gridx = 1; ge.gridwidth = 3; ge.weightx = 1.0;
            pnlQuickEdit.add(txtEditNote, ge);

            pnl.add(pnlQuickEdit, gbc);

            // Nhật ký chăm sóc trong ngày (Daily Care Checklist)
            gbc.gridy++;
            gbc.insets = new Insets(2, 0, 4, 0);
            JLabel lblSecCare = new JLabel("NHẬT KÝ CHĂM SÓC TRONG NGÀY");
            lblSecCare.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblSecCare.setForeground(ThemeColors.PRIMARY_ACTIVE);
            pnl.add(lblSecCare, gbc);

            JCheckBox chkAnSang = new JCheckBox("Đã cho ăn sáng & nước (07:30)", p.daChoAnSang);
            chkAnSang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            chkAnSang.setOpaque(false);
            chkAnSang.setFocusPainted(false);
            chkAnSang.addActionListener(e -> { p.daChoAnSang = chkAnSang.isSelected(); luuPhongXuongDB(p); });

            JCheckBox chkDiDao = new JCheckBox("Đã dắt đi dạo sân chơi (09:00)", p.daDiDaoVeSinh);
            chkDiDao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            chkDiDao.setOpaque(false);
            chkDiDao.setFocusPainted(false);
            chkDiDao.addActionListener(e -> { p.daDiDaoVeSinh = chkDiDao.isSelected(); luuPhongXuongDB(p); });

            JCheckBox chkAnToi = new JCheckBox("Đã cho ăn chiều & đệm ấm (17:30)", p.daChoAnToi);
            chkAnToi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            chkAnToi.setOpaque(false);
            chkAnToi.setFocusPainted(false);
            chkAnToi.addActionListener(e -> { p.daChoAnToi = chkAnToi.isSelected(); luuPhongXuongDB(p); });

            gbc.insets = new Insets(1, 0, 1, 0);
            gbc.gridy++;
            pnl.add(chkAnSang, gbc);
            gbc.gridy++;
            pnl.add(chkDiDao, gbc);
            gbc.gridy++;
            gbc.insets = new Insets(1, 0, 8, 0);
            pnl.add(chkAnToi, gbc);

            // Nút Lưu cập nhật nhanh & Check-out tính tiền
            gbc.gridy++;
            gbc.insets = new Insets(0, 0, 6, 0);
            JButton btnLuuCapNhat = ThemeColors.createSecondaryButton("Lưu cập nhật thông tin & Dặn dò", IconHelper.getIcon(IconHelper.EDIT, 14, ThemeColors.PRIMARY_ACTIVE));
            btnLuuCapNhat.setPreferredSize(new Dimension(0, 32));
            btnLuuCapNhat.addActionListener(e -> {
                if (!txtEditBe.getText().trim().isEmpty()) p.tenThuCung = txtEditBe.getText().trim();
                if (!txtEditChu.getText().trim().isEmpty()) p.tenChu = txtEditChu.getText().trim();
                if (!txtEditSdt.getText().trim().isEmpty()) p.sdtChu = txtEditSdt.getText().trim();
                p.ghiChuChamSoc = txtEditNote.getText().trim();
                luuPhongXuongDB(p);
                locVaHienThiGridPhong();
                capNhatKhungChiTiet(p);
                JOptionPane.showMessageDialog(this, "Đã lưu cập nhật thông tin lưu trú phòng " + p.maPhong + "!", "Đã lưu", JOptionPane.INFORMATION_MESSAGE);
            });
            pnl.add(btnLuuCapNhat, gbc);

            gbc.gridy++;
            gbc.insets = new Insets(0, 0, 4, 0);
            JButton btnCheckOut = ThemeColors.createPrimaryButton("Check-out & Đẩy sang POS thanh toán", IconHelper.getIcon(IconHelper.RECEIPT, 16, ThemeColors.TEXT_PRIMARY));
            btnCheckOut.setPreferredSize(new Dimension(0, 36));
            btnCheckOut.addActionListener(e -> thucHienCheckOut(p));
            pnl.add(btnCheckOut, gbc);

        } else if ("ĐANG DỌN DẸP".equalsIgnoreCase(p.trangThai)) {
            gbc.gridy++;
            gbc.insets = new Insets(0, 0, 14, 0);
            JLabel lblDonDep = new JLabel("Phòng đang trong quy trình khử trùng tiêu chuẩn");
            lblDonDep.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblDonDep.setForeground(ThemeColors.TEXT_SECONDARY);
            pnl.add(lblDonDep, gbc);

            gbc.gridy++;
            JButton btnHoanTatDon = ThemeColors.createPrimaryButton("Đánh dấu Đã Dọn Xong & Sẵn Sàng", IconHelper.getIcon(IconHelper.CHECK, 15, ThemeColors.TEXT_PRIMARY));
            btnHoanTatDon.setPreferredSize(new Dimension(0, 38));
            btnHoanTatDon.addActionListener(e -> {
                p.trangThai = "TRỐNG";
                luuPhongXuongDB(p);
                capNhatSoLieuKPI();
                locVaHienThiGridPhong();
                capNhatKhungChiTiet(p);
                JOptionPane.showMessageDialog(this, "Phòng " + p.maPhong + " đã sẵn sàng tiếp nhận bé cưng mới!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            });
            pnl.add(btnHoanTatDon, gbc);

        } else {
            // Phòng TRỐNG -> Hiển thị luôn Form Nhập Thông Tin Check-in trực tiếp bên phải
            gbc.gridy++;
            gbc.insets = new Insets(0, 0, 6, 0);
            JLabel lblTrong = new JLabel("NHẬP THÔNG TIN TIẾP NHẬN BÉ CƯNG VÀO PHÒNG " + p.maPhong);
            lblTrong.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblTrong.setForeground(new Color(0x23, 0x4E, 0x27));
            pnl.add(lblTrong, gbc);

            JPanel pnlFormIn = new JPanel(new GridBagLayout());
            pnlFormIn.setBackground(ThemeColors.BACKGROUND);
            pnlFormIn.setBorder(new CompoundBorder(
                    new LineBorder(ThemeColors.BORDER, 1, true),
                    new EmptyBorder(10, 12, 10, 12)
            ));
            GridBagConstraints gf = new GridBagConstraints();
            gf.fill = GridBagConstraints.HORIZONTAL;
            gf.insets = new Insets(3, 2, 3, 2);

            JTextField txtBeIn = new JTextField();
            JTextField txtGiongIn = new JTextField("Poodle / Mèo Anh");
            JTextField txtChuIn = new JTextField();
            JTextField txtSdtIn = new JTextField();
            JSpinner spnSoNgay = new JSpinner(new SpinnerNumberModel(2, 1, 30, 1));
            JTextField txtDanDoIn = new JTextField("Ăn hạt 2 bữa/ngày, dắt đi dạo sáng");

            gf.gridx = 0; gf.gridy = 0; gf.weightx = 0.3;
            pnlFormIn.add(new JLabel("Tên thú cưng (*):"), gf);
            gf.gridx = 1; gf.weightx = 0.7;
            pnlFormIn.add(txtBeIn, gf);

            gf.gridx = 0; gf.gridy = 1; gf.weightx = 0.3;
            pnlFormIn.add(new JLabel("Giống & Cân nặng:"), gf);
            gf.gridx = 1; gf.weightx = 0.7;
            pnlFormIn.add(txtGiongIn, gf);

            gf.gridx = 0; gf.gridy = 2; gf.weightx = 0.3;
            pnlFormIn.add(new JLabel("Họ tên chủ (*):"), gf);
            gf.gridx = 1; gf.weightx = 0.7;
            pnlFormIn.add(txtChuIn, gf);

            gf.gridx = 0; gf.gridy = 3; gf.weightx = 0.3;
            pnlFormIn.add(new JLabel("SĐT liên hệ (*):"), gf);
            gf.gridx = 1; gf.weightx = 0.7;
            pnlFormIn.add(txtSdtIn, gf);

            gf.gridx = 0; gf.gridy = 4; gf.weightx = 0.3;
            pnlFormIn.add(new JLabel("Số ngày gửi:"), gf);
            gf.gridx = 1; gf.weightx = 0.7;
            pnlFormIn.add(spnSoNgay, gf);

            gf.gridx = 0; gf.gridy = 5; gf.weightx = 0.3;
            pnlFormIn.add(new JLabel("Dặn dò chăm sóc:"), gf);
            gf.gridx = 1; gf.weightx = 0.7;
            pnlFormIn.add(txtDanDoIn, gf);

            gbc.gridy++;
            gbc.insets = new Insets(0, 0, 10, 0);
            pnl.add(pnlFormIn, gbc);

            gbc.gridy++;
            gbc.insets = new Insets(0, 0, 6, 0);
            JButton btnCheckInPhong = ThemeColors.createPrimaryButton("+ Xác nhận Check-in ngay vào phòng " + p.maPhong, IconHelper.getIcon(IconHelper.CHECK, 15, ThemeColors.TEXT_PRIMARY));
            btnCheckInPhong.setPreferredSize(new Dimension(0, 36));
            btnCheckInPhong.addActionListener(e -> {
                String tenBe = txtBeIn.getText().trim();
                String tenChu = txtChuIn.getText().trim();
                String sdt = txtSdtIn.getText().trim();
                if (tenBe.isEmpty() || tenChu.isEmpty() || sdt.isEmpty()) {
                    moDialogCheckIn(p);
                    return;
                }
                int soNgay = (Integer) spnSoNgay.getValue();
                p.trangThai = "ĐANG CÓ KHÁCH";
                p.tenThuCung = tenBe;
                p.giongLoai = txtGiongIn.getText().trim();
                p.tenChu = tenChu;
                p.sdtChu = sdt;
                p.thoiGianCheckIn = LocalDateTime.now();
                p.duKienCheckOut = LocalDateTime.now().plusDays(soNgay);
                p.ghiChuChamSoc = txtDanDoIn.getText().trim();
                p.daChoAnSang = false;
                p.daChoAnToi = false;
                p.daDiDaoVeSinh = false;
                luuPhongXuongDB(p);
                capNhatSoLieuKPI();
                locVaHienThiGridPhong();
                capNhatKhungChiTiet(p);
                JOptionPane.showMessageDialog(this, "Đã Check-in bé [" + tenBe + "] vào phòng " + p.maPhong + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            });
            pnl.add(btnCheckInPhong, gbc);

            gbc.gridy++;
            JButton btnChuyenDonDep = ThemeColors.createSecondaryButton("Đổi sang trạng thái Dọn dẹp", IconHelper.getIcon(IconHelper.REFRESH, 14, ThemeColors.TEXT_SECONDARY));
            btnChuyenDonDep.setPreferredSize(new Dimension(0, 32));
            btnChuyenDonDep.addActionListener(e -> {
                p.trangThai = "ĐANG DỌN DẸP";
                luuPhongXuongDB(p);
                capNhatSoLieuKPI();
                locVaHienThiGridPhong();
                capNhatKhungChiTiet(p);
            });
            pnl.add(btnChuyenDonDep, gbc);
        }

        JPanel pnlNorthWrapper = new JPanel(new BorderLayout());
        pnlNorthWrapper.setOpaque(false);
        pnlNorthWrapper.add(pnl, BorderLayout.NORTH);

        JScrollPane scrollDetails = new JScrollPane(pnlNorthWrapper);
        scrollDetails.setBorder(null);
        scrollDetails.setOpaque(false);
        scrollDetails.getViewport().setOpaque(false);
        scrollDetails.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pnlDetailsContainer.add(scrollDetails, BorderLayout.CENTER);
        pnlDetailsContainer.revalidate();
        pnlDetailsContainer.repaint();
    }

    private JPanel taoRowThongTin(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(2, 0, 2, 0));

        JLabel lblL = new JLabel(label);
        lblL.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblL.setForeground(ThemeColors.TEXT_SECONDARY);
        lblL.setPreferredSize(new Dimension(105, 20));
        lblL.setVerticalAlignment(SwingConstants.TOP);

        JTextArea txtV = new JTextArea(value);
        txtV.setEditable(false);
        txtV.setLineWrap(true);
        txtV.setWrapStyleWord(true);
        txtV.setOpaque(false);
        txtV.setBorder(null);
        txtV.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtV.setForeground(ThemeColors.TEXT_PRIMARY);
        txtV.setFocusable(false);

        row.add(lblL, BorderLayout.WEST);
        row.add(txtV, BorderLayout.CENTER);
        return row;
    }

    private void moDialogCheckIn(PhongLuuTru phongChiDinh) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "CHECK-IN LƯU TRÚ PET HOTEL", true);
        dlg.setSize(520, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(ThemeColors.SURFACE);
        pnlBody.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Form Fields
        JTextField txtSdt = new JTextField();
        JTextField txtTenChu = new JTextField();
        JTextField txtTenBe = new JTextField();
        JTextField txtGiongLoai = new JTextField();
        JComboBox<String> cboPhong = new JComboBox<>();
        JSpinner spnSoNgay = new JSpinner(new SpinnerNumberModel(2, 1, 30, 1));
        JTextArea txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);

        // Nạp danh sách phòng trống vào combobox
        List<PhongLuuTru> dsTrong = new ArrayList<>();
        for (PhongLuuTru p : DANH_SACH_PHONG) {
            if ("TRỐNG".equalsIgnoreCase(p.trangThai) || (phongChiDinh != null && phongChiDinh.maPhong.equals(p.maPhong))) {
                dsTrong.add(p);
                cboPhong.addItem(p.maPhong + " - " + p.loaiPhong + " (" + dfMoney.format(p.donGiaNgay) + "/ngày)");
            }
        }

        if (phongChiDinh != null) {
            for (int i = 0; i < dsTrong.size(); i++) {
                if (dsTrong.get(i).maPhong.equals(phongChiDinh.maPhong)) {
                    cboPhong.setSelectedIndex(i);
                    break;
                }
            }
        }

        txtSdt.putClientProperty("JTextField.placeholderText", "Ví dụ: 0988111222 (tự động tra khách VIP)...");
        txtSdt.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { checkTraCuu(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { checkTraCuu(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { checkTraCuu(); }
            private void checkTraCuu() {
                String s = txtSdt.getText().trim();
                models.entity.KhachHang kh = new controllers.BanHangController().timKhachHang(s);
                if (kh != null) {
                    txtTenChu.setText(kh.getHoTen());
                }
            }
        });

        pnlBody.add(taoFieldGroup("Số điện thoại chủ nuôi:", txtSdt));
        pnlBody.add(Box.createVerticalStrut(8));
        pnlBody.add(taoFieldGroup("Họ và tên chủ nuôi:", txtTenChu));
        pnlBody.add(Box.createVerticalStrut(8));
        pnlBody.add(taoFieldGroup("Tên bé cưng lưu trú:", txtTenBe));
        pnlBody.add(Box.createVerticalStrut(8));
        pnlBody.add(taoFieldGroup("Giống loài / Cân nặng (vd: Poodle 4kg):", txtGiongLoai));
        pnlBody.add(Box.createVerticalStrut(8));
        pnlBody.add(taoFieldGroup("Chọn phòng lưu trú trống:", cboPhong));
        pnlBody.add(Box.createVerticalStrut(8));
        pnlBody.add(taoFieldGroup("Số ngày dự kiến lưu trú:", spnSoNgay));
        pnlBody.add(Box.createVerticalStrut(8));
        pnlBody.add(taoFieldGroup("Ghi chú dinh dưỡng / dặn dò thuốc men:", new JScrollPane(txtGhiChu)));

        // Buttons
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        pnlFooter.setBackground(ThemeColors.BACKGROUND);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeColors.BORDER));

        JButton btnHuy = ThemeColors.createSecondaryButton("Hủy bỏ", null);
        btnHuy.addActionListener(e -> dlg.dispose());

        JButton btnXacNhan = ThemeColors.createPrimaryButton("Xác nhận Check-in", IconHelper.getIcon(IconHelper.CHECK, 15, ThemeColors.TEXT_PRIMARY));
        btnXacNhan.addActionListener(e -> {
            String tenBe = txtTenBe.getText().trim();
            String tenChu = txtTenChu.getText().trim();
            String sdt = txtSdt.getText().trim();

            if (tenBe.isEmpty() || tenChu.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập đầy đủ tên bé cưng và tên chủ nuôi!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int selIdx = cboPhong.getSelectedIndex();
            if (selIdx < 0 || selIdx >= dsTrong.size()) {
                JOptionPane.showMessageDialog(dlg, "Hiện không còn phòng trống phù hợp!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            PhongLuuTru pChon = dsTrong.get(selIdx);
            pChon.trangThai = "ĐANG CÓ KHÁCH";
            pChon.tenThuCung = tenBe;
            pChon.giongLoai = txtGiongLoai.getText().trim().isEmpty() ? "Thú cưng" : txtGiongLoai.getText().trim();
            pChon.tenChu = tenChu;
            pChon.sdtChu = sdt;
            pChon.thoiGianCheckIn = LocalDateTime.now();
            int soNgay = (Integer) spnSoNgay.getValue();
            pChon.duKienCheckOut = LocalDateTime.now().plusDays(soNgay);
            pChon.ghiChuChamSoc = txtGhiChu.getText().trim();
            pChon.daChoAnSang = false;
            pChon.daChoAnToi = false;
            pChon.daDiDaoVeSinh = false;
            luuPhongXuongDB(pChon);

            capNhatSoLieuKPI();
            locVaHienThiGridPhong();
            capNhatKhungChiTiet(pChon);
            dlg.dispose();

            JOptionPane.showMessageDialog(this,
                    "CHECK-IN THÀNH CÔNG CHO BÉ " + tenBe.toUpperCase() + "!\n" +
                            "• Phòng: " + pChon.maPhong + " (" + pChon.loaiPhong + ")\n" +
                            "• Dự kiến đón: " + pChon.duKienCheckOut.format(dtfDisplay) + " (" + soNgay + " ngày)\n" +
                            "• Đơn giá: " + dfMoney.format(pChon.donGiaNgay) + "/ngày",
                    "Check-in thành công", JOptionPane.INFORMATION_MESSAGE);
        });

        pnlFooter.add(btnHuy);
        pnlFooter.add(btnXacNhan);

        dlg.add(pnlBody, BorderLayout.CENTER);
        dlg.add(pnlFooter, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JPanel taoFieldGroup(String label, JComponent comp) {
        JPanel pnl = new JPanel(new BorderLayout(0, 4));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, comp instanceof JScrollPane ? 85 : 55));
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(ThemeColors.TEXT_PRIMARY);

        if (comp instanceof JTextField) {
            comp.setPreferredSize(new Dimension(0, 32));
            comp.setBorder(ThemeColors.createInputBorder());
        }

        pnl.add(lbl, BorderLayout.NORTH);
        pnl.add(comp, BorderLayout.CENTER);
        return pnl;
    }

    private void thucHienCheckOut(PhongLuuTru p) {
        if (p == null || !"ĐANG CÓ KHÁCH".equalsIgnoreCase(p.trangThai)) return;

        // Tính số ngày lưu trú thực tế (tối thiểu 1 ngày)
        long days = 1;
        if (p.thoiGianCheckIn != null) {
            days = ChronoUnit.DAYS.between(p.thoiGianCheckIn.toLocalDate(), LocalDateTime.now().toLocalDate());
            if (days <= 0) days = 1;
        }

        long tienPhong = days * p.donGiaNgay;
        long tongTien = tienPhong + p.phiDichVuPhatSinh;

        String msg = String.format("XÁC NHẬN CHECK-OUT PET HOTEL:\n" +
                        "• Phòng: %s (%s)\n" +
                        "• Bé cưng: %s (Chủ: %s - %s)\n" +
                        "• Thời gian lưu trú: %d ngày (từ %s đến nay)\n" +
                        "• Tiền phòng: %s\n" +
                        "• Phụ phí dịch vụ: %s\n" +
                        "------------------------------------\n" +
                        "TỔNG THANH TOÁN: %s\n\n" +
                        "Bạn có muốn đẩy dữ liệu này sang Hóa đơn POS để thanh toán ngay?",
                p.maPhong, p.loaiPhong, p.tenThuCung, p.tenChu, p.sdtChu,
                days, (p.thoiGianCheckIn != null ? p.thoiGianCheckIn.format(dtfShort) : "—"),
                dfMoney.format(tienPhong), dfMoney.format(p.phiDichVuPhatSinh),
                dfMoney.format(tongTien));

        int opt = JOptionPane.showConfirmDialog(this, msg, "Xác nhận Check-out", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            // Đổi trạng thái sang dọn dẹp
            p.trangThai = "ĐANG DỌN DẸP";
            luuPhongXuongDB(p);
            String tenBe = p.tenThuCung;
            String tenChu = p.tenChu;
            String sdt = p.sdtChu;
            String maPhong = p.maPhong;
            String loaiPhong = p.loaiPhong;

            capNhatSoLieuKPI();
            locVaHienThiGridPhong();
            capNhatKhungChiTiet(p);

            // Đẩy sang Hóa đơn POS qua MainDashboard
            Container top = getTopLevelAncestor();
            if (top instanceof MainDashboard) {
                MainDashboard md = (MainDashboard) top;
                md.dieuHuongSangChucNang(MainDashboard.CARD_POS);
                BanHangPOSPanel pos = md.getBanHangPOSPanel();
                if (pos != null) {
                    pos.tiepNhanDichVuPetHotel(maPhong, loaiPhong, tenBe, tenChu, sdt, (int) days, p.donGiaNgay, tongTien);
                }
            }
        }
    }

    public static List<PhongLuuTru> layDanhSachPhong() {
        return DANH_SACH_PHONG;
    }
}
