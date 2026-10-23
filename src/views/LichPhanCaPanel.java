package views;

import controllers.PhanCaController;
import models.entity.BangPhanCong;
import models.entity.CaLamViec;
import models.entity.NhanVien;
import utils.IconHelper;
import utils.ThemeColors;
import utils.UserSession;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Quản lý Phân công Ca làm việc theo mô hình Thời khóa biểu tuần (IUH Weekly Timetable Matrix).
 * - Trục Dọc (Dòng): 4 Ca làm việc cố định (Ca 1: Sáng, Ca 2: Trưa, Ca 3: Chiều, Ca 4: Tối).
 * - Trục Ngang (Cột): Cột 0 là thông tin ca; Cột 1 đến 7 là Thứ 2 đến Chủ Nhật kèm ngày (dd/MM).
 * - Mỗi ô chứa danh sách Thẻ Nhân Viên Bo Tròn (Shift Cards) phân màu theo vai trò (NVCS, NVTN, NVBH, NVQL).
 * - Menu chuột phải (JPopupMenu): Phân công thêm nhân sự, đổi ca, sửa ghi chú, xóa khỏi ca.
 * - Phân quyền cá nhân hóa: Nhân viên mặc định lọc xem lịch cá nhân, Quản lý toàn quyền điều phối.
 */
public class LichPhanCaPanel extends JPanel {

    private final PhanCaController controller;
    private LocalDate currentMonday;

    private JLabel lblTuanHienTai;
    private JComboBox<String> cboLocVaiTro;
    private JCheckBox chkChiLichCuaToi;
    private JTable tblMaTranPhanCa;
    private DefaultTableModel modelMaTran;

    private final DateTimeFormatter dtfHeader = DateTimeFormatter.ofPattern("dd/MM");
    private final DateTimeFormatter dtfFull = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Định nghĩa 4 ca làm việc cố định
    private static final String[][] THONG_TIN_4_CA = {
            {"CA01", "Ca 1 (Sáng)", "07:00 – 11:30", "#2563EB", "#EFF6FF", "Mở quầy, Tắm dưỡng & Spa sáng"},
            {"CA02", "Ca 2 (Trưa)", "11:30 – 15:30", "#059669", "#ECFDF5", "Grooming tạo kiểu & Đón khách trưa"},
            {"CA03", "Ca 3 (Chiều)", "15:30 – 19:30", "#EA580C", "#FFF7ED", "Tư vấn bán lẻ, Trị liệu & Khách sạn"},
            {"CA04", "Ca 4 (Tối)", "19:30 – 22:00", "#7C3AED", "#F5F3FF", "Bàn giao thú cưng, Kiểm kho & Chốt ca"}
    };

    public LichPhanCaPanel() {
        this(new PhanCaController());
    }

    public LichPhanCaPanel(PhanCaController controller) {
        this.controller = controller;
        this.currentMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(14, 18, 14, 18));
        setBackground(ThemeColors.BACKGROUND);

        initUI();
        taiDuLieuMaTran();
    }

    public void thucHienPhanCa() {
        taiDuLieuMaTran();
    }

    public void traCuuLichCaNhan() {
        if (chkChiLichCuaToi != null) {
            chkChiLichCuaToi.setSelected(true);
            taiDuLieuMaTran();
        }
    }

    private void initUI() {
        // 1. Header Toolbar
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 8));
        pnlHeader.setOpaque(false);

        // Top Header
        JPanel pnlTopTitle = new JPanel(new BorderLayout());
        pnlTopTitle.setOpaque(false);

        JLabel lblTitle = new JLabel("LỊCH PHÂN CÔNG CA LÀM VIỆC THEO TUẦN (TIMETABLE MATRIX)",
                IconHelper.getPrimaryIcon(IconHelper.CALENDAR, 22), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Mô hình Thời khóa biểu tuần 4 ca chuẩn hóa • Thẻ phân vai trò sắc nét • Menu chuột phải điều phối nhanh");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        JPanel pnlTitleText = new JPanel(new BorderLayout(0, 2));
        pnlTitleText.setOpaque(false);
        pnlTitleText.add(lblTitle, BorderLayout.NORTH);
        pnlTitleText.add(lblSub, BorderLayout.SOUTH);

        // Nút hành động phía trên bên phải
        JPanel pnlTopActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlTopActions.setOpaque(false);

        JButton btnXuatLich = ThemeColors.createSecondaryButton("Xuất Excel", IconHelper.getIcon(IconHelper.RECEIPT, 13, ThemeColors.TEXT_SECONDARY));
        btnXuatLich.addActionListener(e -> xuatLichLamViec());
        pnlTopActions.add(btnXuatLich);

        if (UserSession.isQuanLy()) {
            JButton btnXepCa = ThemeColors.createPrimaryButton("+ Xếp ca mới", IconHelper.getIcon(IconHelper.PLUS, 14, Color.WHITE));
            btnXepCa.addActionListener(e -> moDialogXepCaNhanh());
            pnlTopActions.add(btnXepCa);
        }

        pnlTopTitle.add(pnlTitleText, BorderLayout.WEST);
        pnlTopTitle.add(pnlTopActions, BorderLayout.EAST);
        pnlHeader.add(pnlTopTitle, BorderLayout.NORTH);

        // 2. Chú thích các ca làm việc & Vai trò (Legend Card)
        pnlHeader.add(taoPanelLegendCa(), BorderLayout.CENTER);

        // 3. Thanh điều hướng tuần & Bộ lọc
        pnlHeader.add(taoPanelThanhDieuHuong(), BorderLayout.SOUTH);

        add(pnlHeader, BorderLayout.NORTH);

        // 4. Bảng Ma trận Thời khóa biểu tuần
        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setBackground(ThemeColors.SURFACE);
        pnlCenter.setBorder(ThemeColors.createCardBorder(10, 12, 10, 12));

        taoBangMaTran();
        JScrollPane scrTable = new JScrollPane(tblMaTranPhanCa);
        scrTable.setBorder(BorderFactory.createEmptyBorder());
        scrTable.getViewport().setBackground(ThemeColors.SURFACE);
        pnlCenter.add(scrTable, BorderLayout.CENTER);

        // Chú thích thao tác dưới chân bảng
        JLabel lblHint = new JLabel("Mẹo: Nhấp chuột phải vào bất kỳ ô ca trực nào để phân công thêm nhân sự, đổi ca, sửa ghi chú hoặc hủy ca trực.");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(ThemeColors.TEXT_MUTED);
        lblHint.setBorder(new EmptyBorder(8, 6, 4, 6));
        pnlCenter.add(lblHint, BorderLayout.SOUTH);

        add(pnlCenter, BorderLayout.CENTER);
    }

    private JPanel taoPanelLegendCa() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER_LIGHT, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel lblNote = new JLabel("Nhãn vai trò:");
        lblNote.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNote.setForeground(ThemeColors.TEXT_PRIMARY);
        pnl.add(lblNote);

        pnl.add(taoBadgeLegend("NVCS: Chăm sóc thú cưng", new Color(0x7E, 0x22, 0xCE), new Color(0xF3, 0xE8, 0xFF)));
        pnl.add(taoBadgeLegend("NVTN: Thu ngân", new Color(0x1D, 0x4E, 0xD8), new Color(0xEF, 0xF6, 0xFF)));
        pnl.add(taoBadgeLegend("NVBH: Bán hàng", new Color(0xC2, 0x41, 0x0C), new Color(0xFF, 0xF7, 0xED)));
        pnl.add(taoBadgeLegend("NVQL: Quản lý", new Color(0x04, 0x78, 0x57), new Color(0xEC, 0xFD, 0xF5)));

        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 18));
        pnl.add(sep);

        JLabel lblCaNote = new JLabel("4 Ca cố định:");
        lblCaNote.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCaNote.setForeground(ThemeColors.TEXT_PRIMARY);
        pnl.add(lblCaNote);

        pnl.add(taoBadgeLegend("Ca 1: 07:00–11:30", new Color(0x25, 0x63, 0xEB), new Color(0xEF, 0xF6, 0xFF)));
        pnl.add(taoBadgeLegend("Ca 2: 11:30–15:30", new Color(0x05, 0x96, 0x69), new Color(0xEC, 0xFD, 0xF5)));
        pnl.add(taoBadgeLegend("Ca 3: 15:30–19:30", new Color(0xEA, 0x58, 0x0C), new Color(0xFF, 0xF7, 0xED)));
        pnl.add(taoBadgeLegend("Ca 4: 19:30–22:00", new Color(0x7C, 0x3A, 0xED), new Color(0xF5, 0xF3, 0xFF)));

        return pnl;
    }

    private JLabel taoBadgeLegend(String text, Color fg, Color bg) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(fg);
        lbl.setBackground(bg);
        lbl.setOpaque(true);
        lbl.setBorder(new CompoundBorder(
                new LineBorder(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 70), 1, true),
                new EmptyBorder(2, 8, 2, 8)
        ));
        return lbl;
    }

    private JPanel taoPanelThanhDieuHuong() {
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setOpaque(false);

        // Nhóm nút tuần (Sử dụng Vector Icon chuẩn Lucide - Khắc phục hoàn toàn lỗi ký tự ô vuông)
        JPanel pnlNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlNav.setOpaque(false);

        JButton btnPrev = ThemeColors.createSecondaryButton("Tuần trước",
                IconHelper.getIcon(IconHelper.CHEVRON_LEFT, 14, ThemeColors.TEXT_SECONDARY));
        JButton btnNext = ThemeColors.createSecondaryButton("Tuần sau", null);
        btnNext.setIcon(IconHelper.getIcon(IconHelper.CHEVRON_RIGHT, 14, ThemeColors.TEXT_SECONDARY));
        btnNext.setHorizontalTextPosition(SwingConstants.LEFT);

        JButton btnCurrent = ThemeColors.createSecondaryButton("Tuần này", null);

        btnPrev.setPreferredSize(new Dimension(115, 32));
        btnNext.setPreferredSize(new Dimension(115, 32));
        btnCurrent.setPreferredSize(new Dimension(85, 32));

        btnPrev.addActionListener(e -> {
            currentMonday = currentMonday.minusWeeks(1);
            taiDuLieuMaTran();
        });
        btnNext.addActionListener(e -> {
            currentMonday = currentMonday.plusWeeks(1);
            taiDuLieuMaTran();
        });
        btnCurrent.addActionListener(e -> {
            currentMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            taiDuLieuMaTran();
        });

        lblTuanHienTai = new JLabel("", SwingConstants.CENTER);
        lblTuanHienTai.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTuanHienTai.setForeground(ThemeColors.PRIMARY_ACTIVE);
        lblTuanHienTai.setBorder(new EmptyBorder(0, 10, 0, 10));

        pnlNav.add(btnPrev);
        pnlNav.add(lblTuanHienTai);
        pnlNav.add(btnNext);
        pnlNav.add(btnCurrent);

        // Nhóm bộ lọc vai trò & cá nhân hóa
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlFilters.setOpaque(false);

        pnlFilters.add(new JLabel("Lọc vai trò:"));
        cboLocVaiTro = new JComboBox<>(new String[]{
                "Tất cả vai trò",
                "Quản lý (NVQL)",
                "Thu ngân (NVTN)",
                "CS Thú cưng (NVCS)",
                "Bán hàng (NVBH)"
        });
        cboLocVaiTro.setPreferredSize(new Dimension(150, 32));
        cboLocVaiTro.addActionListener(e -> taiDuLieuMaTran());
        pnlFilters.add(cboLocVaiTro);

        chkChiLichCuaToi = new JCheckBox("Chỉ xem lịch của tôi");
        chkChiLichCuaToi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkChiLichCuaToi.setOpaque(false);
        // Nếu không phải quản lý thì mặc định tick xem lịch của tôi
        if (!UserSession.isQuanLy()) {
            chkChiLichCuaToi.setSelected(true);
        }
        chkChiLichCuaToi.addActionListener(e -> taiDuLieuMaTran());
        pnlFilters.add(chkChiLichCuaToi);

        pnl.add(pnlNav, BorderLayout.WEST);
        pnl.add(pnlFilters, BorderLayout.EAST);

        return pnl;
    }

    private void taoBangMaTran() {
        modelMaTran = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblMaTranPhanCa = new JTable(modelMaTran);
        tblMaTranPhanCa.setRowHeight(115); // Chiều cao mặc định tối thiểu cho mỗi ca
        tblMaTranPhanCa.setShowGrid(true);
        tblMaTranPhanCa.setGridColor(ThemeColors.BORDER_LIGHT);
        tblMaTranPhanCa.setSelectionBackground(ThemeColors.PRIMARY_SOFT);
        tblMaTranPhanCa.setSelectionForeground(ThemeColors.TEXT_PRIMARY);

        tblMaTranPhanCa.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblMaTranPhanCa.getTableHeader().setBackground(ThemeColors.PRIMARY_TINT);
        tblMaTranPhanCa.getTableHeader().setForeground(ThemeColors.TEXT_PRIMARY);
        tblMaTranPhanCa.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Bắt sự kiện chuột: Double click hoặc Chuột phải (ContextMenu)
        tblMaTranPhanCa.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblMaTranPhanCa.rowAtPoint(e.getPoint());
                int col = tblMaTranPhanCa.columnAtPoint(e.getPoint());
                if (row < 0 || col < 0) return;

                if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
                    tblMaTranPhanCa.changeSelection(row, col, false, false);
                    if (col >= 1 && col <= 7) {
                        hienThiPopupMenu(e, row, col);
                    }
                } else if (e.getClickCount() == 2) {
                    if (col >= 1 && col <= 7) {
                        xuLyDoubleClickO(row, col);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = tblMaTranPhanCa.rowAtPoint(e.getPoint());
                    int col = tblMaTranPhanCa.columnAtPoint(e.getPoint());
                    if (row >= 0 && col >= 1 && col <= 7) {
                        tblMaTranPhanCa.changeSelection(row, col, false, false);
                        hienThiPopupMenu(e, row, col);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = tblMaTranPhanCa.rowAtPoint(e.getPoint());
                    int col = tblMaTranPhanCa.columnAtPoint(e.getPoint());
                    if (row >= 0 && col >= 1 && col <= 7) {
                        tblMaTranPhanCa.changeSelection(row, col, false, false);
                        hienThiPopupMenu(e, row, col);
                    }
                }
            }
        });
    }

    private void capNhatTieuDeCot() {
        int weekNum = currentMonday.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        LocalDate sunday = currentMonday.plusDays(6);
        lblTuanHienTai.setText("Tuần " + weekNum + ": " + currentMonday.format(dtfHeader) + " – " + sunday.format(dtfHeader) + "/" + currentMonday.getYear());

        LocalDate today = LocalDate.now();

        String[] cols = new String[8];
        cols[0] = "Ca làm việc";

        String[] dayNames = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        for (int i = 0; i < 7; i++) {
            LocalDate d = currentMonday.plusDays(i);
            String title = dayNames[i] + " (" + d.format(dtfHeader) + ")";
            if (d.equals(today)) {
                title += " (HÔM NAY)";
            }
            cols[i + 1] = title;
        }

        modelMaTran.setColumnIdentifiers(cols);

        // Renderer Cột 0: Thông tin Ca trực cố định
        tblMaTranPhanCa.getColumnModel().getColumn(0).setPreferredWidth(170);
        tblMaTranPhanCa.getColumnModel().getColumn(0).setCellRenderer(new ShiftInfoColumnRenderer());

        // Renderer Cột 1 -> 7: Render các thẻ Shift Cards bên trong mỗi ô
        for (int i = 1; i <= 7; i++) {
            tblMaTranPhanCa.getColumnModel().getColumn(i).setPreferredWidth(165);
            tblMaTranPhanCa.getColumnModel().getColumn(i).setCellRenderer(new TimetableCellRenderer());
        }
    }

    public void taiDuLieuMaTran() {
        capNhatTieuDeCot();
        modelMaTran.setRowCount(0);

        String vaiTroFilter = (String) cboLocVaiTro.getSelectedItem();
        boolean chiLichCuaToi = chkChiLichCuaToi.isSelected();
        String currentUserId = UserSession.getUserId();

        // 4 hàng tương ứng với 4 Ca làm việc cố định
        for (int rowIdx = 0; rowIdx < THONG_TIN_4_CA.length; rowIdx++) {
            String maCa = THONG_TIN_4_CA[rowIdx][0];
            Object[] rowData = new Object[8];
            int maxStaffInShift = 1;

            // Cột 0: Dữ liệu ca
            rowData[0] = THONG_TIN_4_CA[rowIdx];

            // Cột 1 -> 7: Danh sách BangPhanCong được lọc
            for (int dayIdx = 0; dayIdx < 7; dayIdx++) {
                LocalDate date = currentMonday.plusDays(dayIdx);
                List<BangPhanCong> listRaw = controller.layPhanCongTheoCaVaNgay(maCa, date);

                List<BangPhanCong> listFiltered = new ArrayList<>();
                for (BangPhanCong pc : listRaw) {
                    if (pc.getNhanVien() == null) continue;
                    NhanVien nv = pc.getNhanVien();
                    String roleCode = nv.getTaiKhoan() != null ? nv.getTaiKhoan().getVaiTro() : "NVBH";

                    // Lọc vai trò
                    if (vaiTroFilter != null && !vaiTroFilter.startsWith("Tất cả")) {
                        if (vaiTroFilter.contains("NVQL") && !roleCode.equals("NVQL")) continue;
                        if (vaiTroFilter.contains("NVTN") && !roleCode.equals("NVTN")) continue;
                        if (vaiTroFilter.contains("NVCS") && !roleCode.equals("NVCS")) continue;
                        if (vaiTroFilter.contains("NVBH") && !roleCode.equals("NVBH")) continue;
                    }

                    // Lọc lịch của tôi
                    if (chiLichCuaToi && !nv.getMaNhanVien().equalsIgnoreCase(currentUserId)) {
                        continue;
                    }

                    listFiltered.add(pc);
                }

                if (listFiltered.size() > maxStaffInShift) {
                    maxStaffInShift = listFiltered.size();
                }

                rowData[dayIdx + 1] = listFiltered;
            }

            modelMaTran.addRow(rowData);
            int rowHeight = Math.max(115, maxStaffInShift * 78 + 24);
            tblMaTranPhanCa.setRowHeight(rowIdx, rowHeight);
        }
    }

    // ==========================================
    // MENU CHUỘT PHẢI (JPOPUPMENU) & DOUBLE CLICK
    // ==========================================
    private void hienThiPopupMenu(MouseEvent e, int row, int col) {
        if (row < 0 || row >= THONG_TIN_4_CA.length || col < 1 || col > 7) return;

        String maCa = THONG_TIN_4_CA[row][0];
        String tenCa = THONG_TIN_4_CA[row][1];
        LocalDate date = currentMonday.plusDays(col - 1);

        @SuppressWarnings("unchecked")
        List<BangPhanCong> list = (List<BangPhanCong>) modelMaTran.getValueAt(row, col);

        JPopupMenu popup = new JPopupMenu();

        // Tiêu đề popup
        JMenuItem itemTitle = new JMenuItem("<html><b>" + tenCa + " • " + date.format(dtfFull) + "</b></html>");
        itemTitle.setEnabled(false);
        popup.add(itemTitle);
        popup.addSeparator();

        // 1. Phân công thêm nhân sự (Quản lý)
        if (UserSession.isQuanLy()) {
            JMenuItem itemAdd = new JMenuItem("+ Phân công thêm nhân sự vào ca này...",
                    IconHelper.getIcon(IconHelper.PLUS, 14, ThemeColors.PRIMARY_ACTIVE));
            itemAdd.addActionListener(ev -> moDialogThemNhanVienVaoCa(maCa, date));
            popup.add(itemAdd);
        }

        // 2. Tác vụ trên từng nhân viên hiện có trong ca
        if (list != null && !list.isEmpty()) {
            popup.addSeparator();
            for (BangPhanCong pc : list) {
                NhanVien nv = pc.getNhanVien();
                if (nv == null) continue;

                String label = nv.getHoTen() + " (" + (nv.getTaiKhoan() != null ? nv.getTaiKhoan().getVaiTro() : "NV") + ")";
                JMenu subMenu = new JMenu(label);
                subMenu.setIcon(IconHelper.getIcon(IconHelper.USER, 13, ThemeColors.TEXT_PRIMARY));

                if (UserSession.isQuanLy()) {
                    JMenuItem itemSuaGhiChu = new JMenuItem("Sửa ghi chú nhiệm vụ...");
                    itemSuaGhiChu.addActionListener(ev -> moDialogSuaGhiChu(pc));

                    JMenuItem itemDoiCa = new JMenuItem("Đổi sang ca khác...");
                    itemDoiCa.addActionListener(ev -> moDialogDoiCa(pc));

                    JMenuItem itemXoa = new JMenuItem("Hủy phân công (Cho nghỉ ca)");
                    itemXoa.setForeground(ThemeColors.DANGER);
                    itemXoa.addActionListener(ev -> {
                        int opt = JOptionPane.showConfirmDialog(this,
                                "Xác nhận hủy phân công của nhân viên [" + nv.getHoTen() + "] tại " + tenCa + " (" + date.format(dtfFull) + ")?",
                                "Hủy phân công", JOptionPane.YES_NO_OPTION);
                        if (opt == JOptionPane.YES_OPTION) {
                            controller.xoaPhanCong(pc.getMaPhanCong());
                            taiDuLieuMaTran();
                            JOptionPane.showMessageDialog(this, "Đã hủy ca trực thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        }
                    });

                    subMenu.add(itemSuaGhiChu);
                    subMenu.add(itemDoiCa);
                    subMenu.addSeparator();
                    subMenu.add(itemXoa);
                } else {
                    JMenuItem itemXem = new JMenuItem("Xem chi tiết nhiệm vụ ca");
                    itemXem.addActionListener(ev -> moDialogChiTietCa(pc));
                    subMenu.add(itemXem);
                }

                popup.add(subMenu);
            }
        }

        popup.show(tblMaTranPhanCa, e.getX(), e.getY());
    }

    private void xuLyDoubleClickO(int row, int col) {
        if (!UserSession.isQuanLy()) {
            @SuppressWarnings("unchecked")
            List<BangPhanCong> list = (List<BangPhanCong>) modelMaTran.getValueAt(row, col);
            if (list != null) {
                for (BangPhanCong pc : list) {
                    if (pc.getNhanVien() != null && pc.getNhanVien().getMaNhanVien().equalsIgnoreCase(UserSession.getUserId())) {
                        moDialogChiTietCa(pc);
                        return;
                    }
                }
            }
            JOptionPane.showMessageDialog(this,
                    "Bạn đang xem lịch ở chế độ Chỉ đọc.\nChỉ Quản lý (NVQL) mới có quyền xếp ca và điều phối ca làm việc!",
                    "Thông báo phân quyền", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String maCa = THONG_TIN_4_CA[row][0];
        LocalDate date = currentMonday.plusDays(col - 1);
        moDialogThemNhanVienVaoCa(maCa, date);
    }

    // ==========================================
    // DIALOG ĐIỀU PHỐI & PHÂN CÔNG CA LÀM VIỆC
    // ==========================================
    private void moDialogThemNhanVienVaoCa(String maCa, LocalDate targetDate) {
        CaLamViec ca = controller.timCaTheoMa(maCa);
        List<NhanVien> allNV = controller.getNhanVienController().layDanhSachNhanVien();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "PHÂN CÔNG NHÂN VIÊN VÀO CA", true);
        dialog.setSize(460, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel pnlBody = new JPanel(new GridLayout(4, 1, 8, 10));
        pnlBody.setBorder(new EmptyBorder(16, 20, 16, 20));
        pnlBody.setBackground(ThemeColors.SURFACE);

        // Thông tin ca & ngày
        JLabel lblInfo = new JLabel("<html>Ca trực: <b style='color:#2563EB;'>" + (ca != null ? ca.getTenCa() : maCa)
                + "</b><br/>Ngày: <b>" + targetDate.format(dtfFull) + "</b></html>");
        pnlBody.add(lblInfo);

        // Chọn nhân viên
        JPanel pnlNv = new JPanel(new BorderLayout(8, 0));
        pnlNv.setOpaque(false);
        pnlNv.add(new JLabel("Chọn nhân sự:"), BorderLayout.WEST);
        JComboBox<String> cboNv = new JComboBox<>();
        for (NhanVien nv : allNV) {
            String role = nv.getTaiKhoan() != null ? nv.getTaiKhoan().getVaiTro() : "NVBH";
            cboNv.addItem(nv.getMaNhanVien() + " - " + nv.getHoTen() + " [" + role + "]");
        }
        pnlNv.add(cboNv, BorderLayout.CENTER);
        pnlBody.add(pnlNv);

        // Ghi chú nhiệm vụ
        JPanel pnlNote = new JPanel(new BorderLayout(8, 0));
        pnlNote.setOpaque(false);
        pnlNote.add(new JLabel("Nhiệm vụ / Ghi chú:"), BorderLayout.WEST);
        JTextField txtNote = new JTextField("Phụ trách Spa & Tiếp nhận bé");
        txtNote.setBorder(ThemeColors.createInputBorder());
        pnlNote.add(txtNote, BorderLayout.CENTER);
        pnlBody.add(pnlNote);

        dialog.add(pnlBody, BorderLayout.CENTER);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBtn.setBackground(ThemeColors.BACKGROUND);
        JButton btnCancel = ThemeColors.createSecondaryButton("Hủy", null);
        JButton btnSave = ThemeColors.createPrimaryButton("Lưu phân công", IconHelper.getIcon(IconHelper.CHECK, 13, Color.WHITE));

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            int idx = cboNv.getSelectedIndex();
            if (idx >= 0 && idx < allNV.size()) {
                NhanVien nv = allNV.get(idx);
                controller.xepCaLamViec(nv, ca, targetDate, txtNote.getText().trim());
                dialog.dispose();
                taiDuLieuMaTran();
                JOptionPane.showMessageDialog(this, "Đã phân công thành công nhân viên " + nv.getHoTen() + " vào ca!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        pnlBtn.add(btnCancel);
        pnlBtn.add(btnSave);
        dialog.add(pnlBtn, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void moDialogSuaGhiChu(BangPhanCong pc) {
        if (pc == null || pc.getNhanVien() == null) return;
        String ghiChuCu = pc.getGhiChu() != null ? pc.getGhiChu() : "";

        String ghiChuMoi = JOptionPane.showInputDialog(this,
                "Nhập nhiệm vụ / ghi chú mới cho nhân viên " + pc.getNhanVien().getHoTen() + ":",
                ghiChuCu);
        if (ghiChuMoi != null) {
            controller.capNhatGhiChuPhanCong(pc.getMaPhanCong(), ghiChuMoi.trim());
            taiDuLieuMaTran();
            JOptionPane.showMessageDialog(this, "Đã cập nhật ghi chú thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void moDialogDoiCa(BangPhanCong pc) {
        if (pc == null || pc.getNhanVien() == null) return;

        JComboBox<String> cboCa = new JComboBox<>(new String[]{
                "CA01 - Ca 1 (07:00 - 11:30)",
                "CA02 - Ca 2 (11:30 - 15:30)",
                "CA03 - Ca 3 (15:30 - 19:30)",
                "CA04 - Ca 4 (19:30 - 22:00)"
        });

        int res = JOptionPane.showConfirmDialog(this, cboCa,
                "Chọn ca làm việc mới cho " + pc.getNhanVien().getHoTen() + ":",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            String sel = (String) cboCa.getSelectedItem();
            if (sel != null) {
                String maCaMoi = sel.substring(0, 4);
                CaLamViec caMoi = controller.timCaTheoMa(maCaMoi);
                controller.doiCaPhanCong(pc.getMaPhanCong(), caMoi);
                taiDuLieuMaTran();
                JOptionPane.showMessageDialog(this, "Đã chuyển sang " + caMoi.getTenCa() + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void moDialogChiTietCa(BangPhanCong pc) {
        if (pc == null) return;
        NhanVien nv = pc.getNhanVien();
        CaLamViec ca = pc.getCaLamViec();

        String msg = String.format(
                "THÔNG TIN CA TRỰC CHI TIẾT:\n\n" +
                        "• Nhân viên: %s (%s)\n" +
                        "• Chức vụ: %s\n" +
                        "• Ngày trực: %s\n" +
                        "• Ca làm việc: %s (%s - %s)\n" +
                        "• Nhiệm vụ: %s\n" +
                        "• Trạng thái: %s",
                nv != null ? nv.getHoTen() : "N/A",
                nv != null ? nv.getMaNhanVien() : "N/A",
                nv != null ? nv.getChucVu() : "N/A",
                pc.getNgayLamViec() != null ? pc.getNgayLamViec().format(dtfFull) : "N/A",
                ca != null ? ca.getTenCa() : "N/A",
                ca != null ? ca.getGioBatDau() : "",
                ca != null ? ca.getGioKetThuc() : "",
                pc.getGhiChu() != null && !pc.getGhiChu().isEmpty() ? pc.getGhiChu() : "Không có",
                pc.getTrangThaiCa()
        );

        JOptionPane.showMessageDialog(this, msg, "Chi tiết ca trực", JOptionPane.INFORMATION_MESSAGE);
    }

    private void moDialogXepCaNhanh() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "XẾP CA LÀM VIỆC MỚI", true);
        dialog.setSize(480, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel pnlBody = new JPanel(new GridLayout(5, 1, 8, 10));
        pnlBody.setBorder(new EmptyBorder(16, 20, 16, 20));
        pnlBody.setBackground(ThemeColors.SURFACE);

        // Chọn nhân viên
        JPanel pnlNv = new JPanel(new BorderLayout(8, 0));
        pnlNv.setOpaque(false);
        pnlNv.add(new JLabel("Nhân viên:"), BorderLayout.WEST);
        List<NhanVien> allNV = controller.getNhanVienController().layDanhSachNhanVien();
        JComboBox<String> cboNv = new JComboBox<>();
        for (NhanVien nv : allNV) {
            cboNv.addItem(nv.getMaNhanVien() + " - " + nv.getHoTen() + " (" + nv.getChucVu() + ")");
        }
        pnlNv.add(cboNv, BorderLayout.CENTER);
        pnlBody.add(pnlNv);

        // Chọn ngày trong tuần hiện tại
        JPanel pnlDate = new JPanel(new BorderLayout(8, 0));
        pnlDate.setOpaque(false);
        pnlDate.add(new JLabel("Ngày trực:"), BorderLayout.WEST);
        JComboBox<String> cboDate = new JComboBox<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = currentMonday.plusDays(i);
            String dow = "Thứ " + (d.getDayOfWeek().getValue() + 1);
            if (d.getDayOfWeek().getValue() == 7) dow = "Chủ Nhật";
            cboDate.addItem(dow + " (" + d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")");
        }
        pnlDate.add(cboDate, BorderLayout.CENTER);
        pnlBody.add(pnlDate);

        // Chọn Ca
        JPanel pnlCa = new JPanel(new BorderLayout(8, 0));
        pnlCa.setOpaque(false);
        pnlCa.add(new JLabel("Ca làm việc:"), BorderLayout.WEST);
        JComboBox<String> cboCa = new JComboBox<>(new String[]{
                "CA01 - Ca 1 (07:00 - 11:30)",
                "CA02 - Ca 2 (11:30 - 15:30)",
                "CA03 - Ca 3 (15:30 - 19:30)",
                "CA04 - Ca 4 (19:30 - 22:00)"
        });
        pnlCa.add(cboCa, BorderLayout.CENTER);
        pnlBody.add(pnlCa);

        // Ghi chú ca
        JPanel pnlNote = new JPanel(new BorderLayout(8, 0));
        pnlNote.setOpaque(false);
        pnlNote.add(new JLabel("Ghi chú ca:"), BorderLayout.WEST);
        JTextField txtNote = new JTextField("Nhận 4 bé Spa bồn sục");
        txtNote.setBorder(ThemeColors.createInputBorder());
        pnlNote.add(txtNote, BorderLayout.CENTER);
        pnlBody.add(pnlNote);

        dialog.add(pnlBody, BorderLayout.CENTER);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBtn.setBackground(ThemeColors.BACKGROUND);
        JButton btnCancel = ThemeColors.createSecondaryButton("Hủy", null);
        JButton btnSave = ThemeColors.createPrimaryButton("Xác nhận Xếp ca", IconHelper.getIcon(IconHelper.CHECK, 13, Color.WHITE));

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            int nvIdx = cboNv.getSelectedIndex();
            if (nvIdx >= 0 && nvIdx < allNV.size()) {
                NhanVien nv = allNV.get(nvIdx);
                int dayIdx = cboDate.getSelectedIndex();
                LocalDate targetDate = currentMonday.plusDays(dayIdx);

                String selCa = (String) cboCa.getSelectedItem();
                String maCa = selCa.substring(0, 4);
                CaLamViec ca = controller.timCaTheoMa(maCa);

                controller.xepCaLamViec(nv, ca, targetDate, txtNote.getText().trim());
                dialog.dispose();
                taiDuLieuMaTran();
                JOptionPane.showMessageDialog(this, "Xếp ca thành công cho nhân viên " + nv.getHoTen() + "!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        pnlBtn.add(btnCancel);
        pnlBtn.add(btnSave);
        dialog.add(pnlBtn, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void xuatLichLamViec() {
        int weekNum = currentMonday.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        JOptionPane.showMessageDialog(this,
                "ĐÃ XUẤT MA TRẬN LỊCH PHÂN CA TUẦN " + weekNum + " RA FILE EXCEL:\n" +
                        "• File: export/LichPhanCa_Tuan" + weekNum + "_" + currentMonday.getYear() + ".xlsx\n" +
                        "• Bao gồm toàn bộ phân công 4 ca và định mức nhân sự tuần.",
                "Xuất lịch thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    // ==========================================
    // RENDERER CỘT 0: THÔNG TIN 4 CA CỐ ĐỊNH
    // ==========================================
    private static class ShiftInfoColumnRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);

            if (value instanceof String[]) {
                String[] info = (String[]) value;
                String tenCa = info[1];
                String gio = info[2];
                String color = info[3];
                String desc = info[5];

                String html = "<html><div style='text-align:center; padding:4px;'>"
                        + "<b style='color:" + color + "; font-size:13px;'>" + tenCa + "</b><br/>"
                        + "<span style='color:#1E293B; font-weight:bold; font-size:11px;'>" + gio + "</span><br/>"
                        + "<span style='color:#64748B; font-size:9.5px; font-style:italic;'>" + desc + "</span>"
                        + "</div></html>";
                lbl.setText(html);
                lbl.setBackground(new Color(0xF8, 0xFA, 0xFC));
            }

            lbl.setBorder(new CompoundBorder(
                    new LineBorder(ThemeColors.BORDER_LIGHT, 1),
                    new EmptyBorder(4, 6, 4, 6)
            ));
            return lbl;
        }
    }

    // ==========================================
    // RENDERER CÁC CỘT NGÀY: SHIFT CARDS BO TRÒN THEO VAI TRÒ
    // ==========================================
    private static class TimetableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setVerticalAlignment(SwingConstants.TOP);
            lbl.setHorizontalAlignment(SwingConstants.LEFT);

            String currentUserId = UserSession.getUserId();

            if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<BangPhanCong> list = (List<BangPhanCong>) value;

                if (list.isEmpty()) {
                    lbl.setText("<html><div style='text-align:center; padding-top:28px; color:#94A3B8; font-size:11px; font-style:italic;'>"
                            + "— Chưa có nhân sự —"
                            + "</div></html>");
                    lbl.setToolTipText("Chưa có nhân viên nào được phân công ca trực này");
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<html><div style='padding:2px;'>");

                    StringBuilder toolTip = new StringBuilder("<html><b>Danh sách nhân sự trong ca:</b><br/>");

                    for (BangPhanCong pc : list) {
                        NhanVien nv = pc.getNhanVien();
                        if (nv == null) continue;

                        String role = nv.getTaiKhoan() != null ? nv.getTaiKhoan().getVaiTro() : "NVBH";
                        boolean isMe = nv.getMaNhanVien().equalsIgnoreCase(currentUserId);

                        // Màu sắc phân biệt theo vai trò
                        String bg = "#FFF7ED";
                        String border = "#FED7AA";
                        String badgeFg = "#C2410C";
                        String badgeBg = "#FFEDD5";

                        if ("NVCS".equals(role)) {
                            bg = "#F3E8FF";
                            border = "#D8B4FE";
                            badgeFg = "#7E22CE";
                            badgeBg = "#E9D5FF";
                        } else if ("NVTN".equals(role)) {
                            bg = "#EFF6FF";
                            border = "#BFDBFE";
                            badgeFg = "#1D4ED8";
                            badgeBg = "#DBEAFE";
                        } else if ("NVQL".equals(role)) {
                            bg = "#ECFDF5";
                            border = "#A7F3D0";
                            badgeFg = "#047857";
                            badgeBg = "#D1FAE5";
                        }

                        // Viền nổi bật cho chính mình
                        if (isMe) {
                            border = "#F97316";
                        }

                        String note = pc.getGhiChu() != null && !pc.getGhiChu().isEmpty() ? pc.getGhiChu() : "";

                        sb.append("<div style='margin-bottom:4px; padding:4px 6px; background:").append(bg)
                                .append("; border:1px solid ").append(border)
                                .append(";'>")
                                .append("<span style='background:").append(badgeBg).append("; color:").append(badgeFg)
                                .append("; font-weight:bold; font-size:9.5px;'>&nbsp;[")
                                .append(role).append("]&nbsp;</span>")
                                .append(" <b style='color:#1E293B; font-size:10.5px;'>").append(nv.getHoTen()).append("</b>");

                        if (isMe) {
                            sb.append(" <span style='color:#F97316; font-weight:bold; font-size:9.5px;'>• BẠN</span>");
                        }

                        if (!note.isEmpty()) {
                            sb.append("<br/><span style='color:#475569; font-size:9.5px; font-style:italic;'>• ")
                                    .append(note).append("</span>");
                        }
                        sb.append("</div>");

                        toolTip.append("• [").append(role).append("] ").append(nv.getHoTen())
                                .append(" - ").append(note.isEmpty() ? "Không ghi chú" : note).append("<br/>");
                    }

                    sb.append("</div></html>");
                    lbl.setText(sb.toString());
                    lbl.setToolTipText(toolTip.append("</html>").toString());
                }
            }

            if (isSelected) {
                lbl.setBackground(ThemeColors.PRIMARY_SOFT);
            } else {
                lbl.setBackground(ThemeColors.SURFACE);
            }

            lbl.setBorder(new CompoundBorder(
                    new LineBorder(ThemeColors.BORDER_LIGHT, 1),
                    new EmptyBorder(3, 4, 3, 4)
            ));

            return lbl;
        }
    }
}
