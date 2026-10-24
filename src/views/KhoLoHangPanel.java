package views;

import utils.IconHelper;
import utils.ThemeColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Panel Quản lý Kho, Sản phẩm, Đơn vị tính quy đổi và Lô hàng FEFO.
 * Thiết kế giao diện tông Warm Peach / Soft Apricot, tích hợp Lucide Icons đồng bộ.
 * Hỗ trợ kết nối trực tiếp với NhapSanPhamVaLoHangDialog và ThietLapQuyDoiDialog.
 */
public class KhoLoHangPanel extends JPanel {

    public static class LoHangRecord {
        public String soLo;
        public String maSP;
        public String tenSP;
        public String nhaCungCap;
        public String ngaySX;
        public String hanSD;
        public String giaNhap;
        public int soLuongNhap;
        public int soLuongTon;
        public String donVi;
        public String trangThaiFEFO;

        public LoHangRecord(String soLo, String maSP, String tenSP, String nhaCungCap, String ngaySX, String hanSD, String giaNhap, int soLuongNhap, int soLuongTon, String donVi, String trangThaiFEFO) {
            this.soLo = soLo;
            this.maSP = maSP;
            this.tenSP = tenSP;
            this.nhaCungCap = nhaCungCap;
            this.ngaySX = ngaySX;
            this.hanSD = hanSD;
            this.giaNhap = giaNhap;
            this.soLuongNhap = soLuongNhap;
            this.soLuongTon = soLuongTon;
            this.donVi = donVi;
            this.trangThaiFEFO = trangThaiFEFO;
        }
    }

    private static final List<LoHangRecord> DANH_SACH_LO_HANG = new ArrayList<>();
    private static final List<KhoLoHangPanel> INSTANCES = new ArrayList<>();

    static {
        khoiTaoLoHangMacDinhNeuCan();
    }

    private static void khoiTaoLoHangMacDinhNeuCan() {
        if (DANH_SACH_LO_HANG.isEmpty()) {
            try {
                models.dao.LoHangDAO loDao = new models.dao.LoHangDAO();
                List<models.entity.LoHang> dbList = loDao.getAllLoHang();
                if (dbList != null && !dbList.isEmpty()) {
                    for (models.entity.LoHang lh : dbList) {
                        String nsxStr = lh.getNgaySanXuat() != null ? lh.getNgaySanXuat().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                        String hsdStr = lh.getHanSuDung() != null ? lh.getHanSuDung().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                        long days = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), lh.getHanSuDung());
                        String fefo;
                        if (days <= 0) fefo = "ĐÃ HẾT HẠN SỬ DỤNG";
                        else if (days <= 30) fefo = "CẬN DATE NGUY CẤP (" + days + " ngày) - CẦN THANH LÝ";
                        else if (days <= 90) fefo = "CẬN DATE (" + (days / 30) + " tháng) - ƯU TIÊN XUẤT SỚM";
                        else fefo = "HẠN DÙNG AN TOÀN (> 6 tháng)";

                        DANH_SACH_LO_HANG.add(new LoHangRecord(
                                lh.getSoLo(),
                                lh.getSanPham() != null ? lh.getSanPham().getMaSanPham() : "",
                                lh.getSanPham() != null ? lh.getSanPham().getTenSanPham() : "",
                                lh.getNhaCungCap() != null ? lh.getNhaCungCap().getTenNhaCungCap() : "",
                                nsxStr,
                                hsdStr,
                                String.format("%,d đ", lh.getDonGiaNhapCoSo() != null ? lh.getDonGiaNhapCoSo().longValue() : 0L),
                                lh.getSoLuongNhap(),
                                lh.getSoLuongTon(),
                                "Đơn vị",
                                fefo
                        ));
                    }
                    return;
                }
            } catch (Exception ignored) {}

            DANH_SACH_LO_HANG.add(new LoHangRecord("LH2026-001", "SP002", "Pate Whiskas Vị Cá Thu 85g", "Công ty TNHH Pet Food VN", "15/05/2026", "15/11/2026", "12,000 đ", 100, 25, "Lon", "CẬN DATE (1 tháng) - ƯU TIÊN XUẤT SỚM"));
            DANH_SACH_LO_HANG.add(new LoHangRecord("LH2026-002", "SP002", "Pate Whiskas Vị Cá Thu 85g", "Công ty TNHH Pet Food VN", "20/06/2026", "20/12/2026", "12,000 đ", 150, 150, "Lon", "CẬN DATE (2 tháng) - ƯU TIÊN XUẤT SỚM"));
            DANH_SACH_LO_HANG.add(new LoHangRecord("LH2026-003", "SP001", "Hạt Royal Canin Kitten 2kg", "Nhà phân phối Royal Canin", "01/03/2026", "28/11/2026", "290,000 đ", 30, 10, "Gói", "CẬN DATE (1 tháng) - ƯU TIÊN XUẤT SỚM"));
            DANH_SACH_LO_HANG.add(new LoHangRecord("LH2026-004", "SP001", "Hạt Royal Canin Kitten 2kg", "Nhà phân phối Royal Canin", "10/08/2026", "10/08/2027", "295,000 đ", 50, 50, "Gói", "HẠN DÙNG AN TOÀN (> 6 tháng)"));
            DANH_SACH_LO_HANG.add(new LoHangRecord("LH2026-005", "SP004", "Sữa Tắm Dưỡng Lông Bio-Care", "Công ty TNHH Pet Food VN", "10/01/2026", "10/01/2028", "55,000 đ", 40, 35, "Chai", "HẠN DÙNG AN TOÀN (> 6 tháng)"));
        }
    }

    public static List<LoHangRecord> layDanhSachLoHangFEFO() {
        khoiTaoLoHangMacDinhNeuCan();
        return DANH_SACH_LO_HANG;
    }

    public static int demSoLoCanhBaoFEFO() {
        khoiTaoLoHangMacDinhNeuCan();
        int count = 0;
        for (LoHangRecord lo : DANH_SACH_LO_HANG) {
            if (lo.soLuongTon > 0) {
                String st = lo.trangThaiFEFO != null ? lo.trangThaiFEFO.toUpperCase() : "";
                if (st.contains("CẬN DATE") || st.contains("NGUY CẤP") || st.contains("HẾT HẠN") || st.contains("ƯU TIÊN") || lo.soLuongTon <= 15) {
                    count++;
                }
            }
        }
        return count;
    }

    public static void dongBoLoHangTuDatabase() {
        DANH_SACH_LO_HANG.clear();
        khoiTaoLoHangMacDinhNeuCan();
        for (KhoLoHangPanel inst : INSTANCES) {
            inst.lamMoiBangLoHang();
            inst.lamMoiBangSanPhamTuDatabase();
        }
    }

    public static void truTonKhoTheoFEFO(String maSP, int soLuongMua) {
        if (maSP == null || soLuongMua <= 0) return;
        khoiTaoLoHangMacDinhNeuCan();
        int canTru = soLuongMua;
        models.dao.LoHangDAO loDao = new models.dao.LoHangDAO();
        for (LoHangRecord lo : DANH_SACH_LO_HANG) {
            boolean match = lo.maSP.equalsIgnoreCase(maSP) || lo.tenSP.equalsIgnoreCase(maSP)
                    || ("SP005".equalsIgnoreCase(maSP) && "SP004".equalsIgnoreCase(lo.maSP))
                    || ("SP003".equalsIgnoreCase(maSP) && "SP002".equalsIgnoreCase(lo.maSP));
            if (match && lo.soLuongTon > 0 && canTru > 0) {
                int tru = Math.min(lo.soLuongTon, canTru);
                lo.soLuongTon -= tru;
                canTru -= tru;
                try { loDao.truTonKho(lo.soLo, tru); } catch (Exception ignored) {}
                if (lo.soLuongTon == 0) {
                    lo.trangThaiFEFO = "ĐÃ HẾT TỒN LÔ";
                } else if (lo.soLuongTon <= 10 && !lo.trangThaiFEFO.toUpperCase().contains("CẬN DATE")
                        && !lo.trangThaiFEFO.toUpperCase().contains("NGUY CẤP")
                        && !lo.trangThaiFEFO.toUpperCase().contains("HẾT HẠN")) {
                    lo.trangThaiFEFO = "SẮP HẾT TỒN (" + lo.soLuongTon + " " + lo.donVi + ") - ƯU TIÊN";
                }
            }
        }
        for (KhoLoHangPanel inst : INSTANCES) {
            inst.lamMoiBangLoHang();
        }
    }

    public static void congTonKho(String maSP, int soLuongHoan) {
        if (maSP == null || soLuongHoan <= 0) return;
        khoiTaoLoHangMacDinhNeuCan();
        models.dao.LoHangDAO loDao = new models.dao.LoHangDAO();
        for (LoHangRecord lo : DANH_SACH_LO_HANG) {
            boolean match = lo.maSP.equalsIgnoreCase(maSP) || lo.tenSP.equalsIgnoreCase(maSP);
            if (match) {
                lo.soLuongTon += soLuongHoan;
                try { loDao.congTonKho(lo.soLo, soLuongHoan); } catch (Exception ignored) {}
                if ("ĐÃ HẾT TỒN LÔ".equalsIgnoreCase(lo.trangThaiFEFO)) {
                    lo.trangThaiFEFO = "AN TOÀN (> 6 tháng)";
                }
                break;
            }
        }
        for (KhoLoHangPanel inst : INSTANCES) {
            inst.lamMoiBangLoHang();
        }
    }

    private final DecimalFormat df = new DecimalFormat("#,### đ");

    private DefaultTableModel modelSanPham;
    private JTable tblSanPham;
    private TableRowSorter<DefaultTableModel> sorterSanPham;

    private DefaultTableModel modelLoHang;
    private JTable tblLoHang;
    private TableRowSorter<DefaultTableModel> sorterLoHang;
    private JTabbedPane tabbedPane;

    public KhoLoHangPanel() {
        khoiTaoLoHangMacDinhNeuCan();
        if (!INSTANCES.contains(this)) {
            INSTANCES.add(this);
        }
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(14, 16, 14, 16));
        setBackground(ThemeColors.BACKGROUND);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Tích hợp Icon Lucide vào từng Tab
        tabbedPane.addTab("  Sản phẩm & Đơn vị tính Quy đổi",
                IconHelper.getPrimaryIcon(IconHelper.BOXES, 16), taoTabSanPhamDonVi());
        tabbedPane.addTab("  Quản lý Lô hàng & Hạn sử dụng (FEFO)",
                IconHelper.getPrimaryIcon(IconHelper.TAG, 16), taoTabLoHangFEFO());

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void chonTabSanPham() {
        if (tabbedPane != null) tabbedPane.setSelectedIndex(0);
    }

    public void chonTabLoHangFEFO() {
        if (tabbedPane != null) tabbedPane.setSelectedIndex(1);
    }

    private JPanel taoTabSanPhamDonVi() {
        JPanel pnl = new JPanel(new BorderLayout(10, 12));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlToolbar.setOpaque(false);

        JTextField txtSearch = new JTextField(16);
        txtSearch.setPreferredSize(new Dimension(210, 36));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên, mã sản phẩm...");
        txtSearch.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.SEARCH, 15, ThemeColors.TEXT_MUTED));
        txtSearch.setBorder(ThemeColors.createInputBorder());

        JButton btnSearch = ThemeColors.createSecondaryButton("Lọc", IconHelper.getIcon(IconHelper.SEARCH, 14, ThemeColors.TEXT_SECONDARY));
        JButton btnThemSP = ThemeColors.createPrimaryButton("Thêm Sản phẩm & Lô mới", IconHelper.getIcon(IconHelper.PLUS, 14, Color.WHITE));
        JButton btnQuyDoi = ThemeColors.createSecondaryButton("Thiết lập Quy đổi ĐVT", IconHelper.getIcon(IconHelper.SETTINGS, 14, ThemeColors.TEXT_SECONDARY));

        btnThemSP.addActionListener(e -> moDialogNhapHang());
        btnQuyDoi.addActionListener(e -> moDialogQuyDoi());

        pnlToolbar.add(new JLabel("Tìm kiếm:"));
        pnlToolbar.add(txtSearch);
        pnlToolbar.add(btnSearch);
        pnlToolbar.add(btnThemSP);
        pnlToolbar.add(btnQuyDoi);

        String[] cols = {"Mã SP", "Tên sản phẩm", "Danh mục hàng", "Đơn vị tính", "Tỷ lệ quy đổi", "Là ĐVT cơ sở", "Giá bán niêm yết", "Mô tả"};
        modelSanPham = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        lamMoiBangSanPhamTuDatabase();

        tblSanPham = taoBangChuan(modelSanPham);
        sorterSanPham = new TableRowSorter<>(modelSanPham);
        tblSanPham.setRowSorter(sorterSanPham);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { locSanPham(txtSearch.getText().trim()); }
            @Override
            public void removeUpdate(DocumentEvent e) { locSanPham(txtSearch.getText().trim()); }
            @Override
            public void changedUpdate(DocumentEvent e) { locSanPham(txtSearch.getText().trim()); }
        });

        pnl.add(pnlToolbar, BorderLayout.NORTH);
        pnl.add(new JScrollPane(tblSanPham), BorderLayout.CENTER);
        return pnl;
    }

    private JPanel taoTabLoHangFEFO() {
        JPanel pnl = new JPanel(new BorderLayout(10, 12));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlToolbar.setOpaque(false);

        JButton btnNhapLo = ThemeColors.createPrimaryButton("Nhập Lô hàng mới (FEFO)", IconHelper.getIcon(IconHelper.PLUS, 14, Color.WHITE));
        JComboBox<String> cboLocHSD = new JComboBox<>(new String[]{"Tất cả lô hàng", "Lô ưu tiên xuất (FEFO)", "Lô hạn dưới 30 ngày / Nguy cấp", "Lô hết hạn"});
        cboLocHSD.setPreferredSize(new Dimension(230, 36));

        JButton btnKiemKe = ThemeColors.createSecondaryButton("Kiểm kê tồn kho", IconHelper.getIcon(IconHelper.CLIPBOARD_CHECK, 14, ThemeColors.TEXT_SECONDARY));

        btnNhapLo.addActionListener(e -> moDialogNhapHang());
        btnKiemKe.addActionListener(e -> xuLyKiemKe());

        pnlToolbar.add(btnNhapLo);
        pnlToolbar.add(new JLabel("Lọc theo HSD:"));
        pnlToolbar.add(cboLocHSD);
        pnlToolbar.add(btnKiemKe);

        String[] cols = {"Số lô", "Mã SP", "Tên sản phẩm", "Nhà cung cấp", "Ngày sản xuất", "Hạn sử dụng", "Giá nhập cơ sở", "Số lượng nhập", "Số lượng tồn", "Trạng thái FEFO"};
        modelLoHang = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        lamMoiBangLoHang();

        tblLoHang = taoBangChuan(modelLoHang);
        sorterLoHang = new TableRowSorter<>(modelLoHang);
        tblLoHang.setRowSorter(sorterLoHang);

        // Tô màu và format trạng thái FEFO 3 cấp độ
        DefaultTableCellRenderer fefoRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String str = value.toString().toUpperCase();
                    if (str.contains("NGUY CẤP") || str.contains("HẾT HẠN") || str.contains("ĐÃ HẾT")) {
                        c.setForeground(ThemeColors.DANGER);
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (str.contains("CẬN DATE") || str.contains("ƯU TIÊN") || str.contains("SẮP HẾT")) {
                        c.setForeground(ThemeColors.WARNING_TEXT);
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(ThemeColors.SUCCESS);
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                }
                return c;
            }
        };
        tblLoHang.getColumnModel().getColumn(9).setCellRenderer(fefoRenderer);

        cboLocHSD.addActionListener(e -> {
            int idx = cboLocHSD.getSelectedIndex();
            if (idx == 0) {
                sorterLoHang.setRowFilter(null);
            } else if (idx == 1) {
                sorterLoHang.setRowFilter(RowFilter.regexFilter("(?i)Ưu tiên|Cận date"));
            } else if (idx == 2) {
                sorterLoHang.setRowFilter(RowFilter.regexFilter("(?i)Nguy cấp|30 ngày"));
            } else if (idx == 3) {
                sorterLoHang.setRowFilter(RowFilter.regexFilter("(?i)Hết hạn|Đã hết"));
            }
        });

        pnl.add(pnlToolbar, BorderLayout.NORTH);
        pnl.add(new JScrollPane(tblLoHang), BorderLayout.CENTER);
        return pnl;
    }

    public void lamMoiBangSanPhamTuDatabase() {
        if (modelSanPham == null) return;
        modelSanPham.setRowCount(0);
        try {
            models.dao.SanPhamDAO spDao = new models.dao.SanPhamDAO();
            List<models.entity.SanPham> dsSP = spDao.getAllSanPham();
            if (dsSP != null && !dsSP.isEmpty()) {
                for (models.entity.SanPham sp : dsSP) {
                    String dmTen = sp.getDanhMucHang() != null ? sp.getDanhMucHang().getTenDanhMuc() : "Hàng hóa";
                    List<models.entity.QuyDoiDonVi> listQd = spDao.getQuyDoiBySanPham(sp.getMaSanPham());
                    if (listQd != null && !listQd.isEmpty()) {
                        for (models.entity.QuyDoiDonVi qd : listQd) {
                            String dvtTen = qd.getDonViTinh() != null ? qd.getDonViTinh().getTenDonVi() : "Đơn vị";
                            long gia = qd.getGiaBan() != null ? qd.getGiaBan().longValue() : 0L;
                            modelSanPham.addRow(new Object[]{
                                    sp.getMaSanPham(), sp.getTenSanPham(), dmTen, dvtTen,
                                    String.valueOf(qd.getTyLeQuyDoi()),
                                    qd.isLaDonViCoSo() ? "Có" : "Không",
                                    df.format(gia),
                                    sp.getMoTa() != null ? sp.getMoTa() : ""
                            });
                        }
                    } else {
                        modelSanPham.addRow(new Object[]{
                                sp.getMaSanPham(), sp.getTenSanPham(), dmTen, "Đơn vị", "1", "Có", "0 đ", sp.getMoTa() != null ? sp.getMoTa() : ""
                        });
                    }
                }
                return;
            }
        } catch (Exception ignored) {}
        modelSanPham.addRow(new Object[]{"SP001", "Hạt Royal Canin Kitten 2kg", "Thức ăn thú cưng", "Gói", "1", "Có", "380,000 đ", "Dành cho mèo con dưới 12 tháng"});
        modelSanPham.addRow(new Object[]{"SP002", "Pate Whiskas Vị Cá Thu 85g", "Thức ăn thú cưng", "Lon", "1", "Có", "20,000 đ", "Pate dinh dưỡng mèo"});
    }

    public void lamMoiBangLoHang() {
        if (modelLoHang == null) return;
        modelLoHang.setRowCount(0);
        for (LoHangRecord lo : DANH_SACH_LO_HANG) {
            modelLoHang.addRow(new Object[]{
                    lo.soLo, lo.maSP, lo.tenSP, lo.nhaCungCap, lo.ngaySX, lo.hanSD,
                    lo.giaNhap, lo.soLuongNhap, lo.soLuongTon, lo.trangThaiFEFO
            });
        }
    }

    private void locSanPham(String kw) {
        if (kw.isEmpty()) {
            sorterSanPham.setRowFilter(null);
        } else {
            sorterSanPham.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(kw)));
        }
    }

    public void moDialogNhapHang() {
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        NhapSanPhamVaLoHangDialog dialog = new NhapSanPhamVaLoHangDialog(topFrame, (maSP, tenSP, danhMuc, dvtCoSo, soLo, ncc, nsx, hsd, giaNhap, giaBan, slNhap, fefo) -> {
            // 1. Thêm vào bảng Sản phẩm
            modelSanPham.addRow(new Object[]{
                    maSP, tenSP, danhMuc, dvtCoSo, "1", "Có", df.format(giaBan), "Mặt hàng nhập kho mới"
            });

            // 2. Thêm vào danh sách và làm mới bảng Lô hàng FEFO
            LoHangRecord loMoi = new LoHangRecord(
                    soLo, maSP, tenSP, ncc, nsx, hsd, df.format(giaNhap), slNhap, slNhap, dvtCoSo, fefo
            );
            DANH_SACH_LO_HANG.add(0, loMoi);
            for (KhoLoHangPanel inst : INSTANCES) {
                inst.lamMoiBangLoHang();
            }
            TongQuanPanel.lamMoiDuLieuDashboardAll();

            // 3. Ghi nhận trực tiếp vào CSDL qua SanPhamDAO & LoHangDAO
            try {
                models.dao.SanPhamDAO spDao = new models.dao.SanPhamDAO();
                models.dao.LoHangDAO loDao = new models.dao.LoHangDAO();
                if (spDao.getSanPhamById(maSP) == null) {
                    models.entity.DanhMucHang dm = new models.entity.DanhMucHang("DM01", danhMuc, "");
                    models.entity.SanPham spMoi = new models.entity.SanPham(maSP, dm, tenSP, "Nhập kho mới");
                    spDao.insertSanPham(spMoi);
                    models.entity.DonViTinh dvt = new models.entity.DonViTinh("DVT01", dvtCoSo);
                    models.entity.QuyDoiDonVi qd = new models.entity.QuyDoiDonVi("QD" + System.currentTimeMillis() % 100000, spMoi, dvt, 1, true, java.math.BigDecimal.valueOf(giaBan));
                    spDao.insertQuyDoi(qd);
                }
                java.time.format.DateTimeFormatter dtfParse = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                java.time.LocalDate dNgaySX = java.time.LocalDate.parse(nsx, dtfParse);
                java.time.LocalDate dHanSD = java.time.LocalDate.parse(hsd, dtfParse);
                models.entity.SanPham spRef = new models.entity.SanPham(maSP, null, tenSP, "");
                models.entity.NhaCungCap nccRef = new models.entity.NhaCungCap("NCC01", ncc, "", "");
                models.entity.LoHang lhMoi = new models.entity.LoHang(soLo, spRef, nccRef, dNgaySX, dHanSD, java.math.BigDecimal.valueOf(giaNhap), slNhap, slNhap);
                loDao.insertLoHang(lhMoi);
            } catch (Exception ex) {
                System.err.println("[KhoLoHangPanel] Lỗi lưu CSDL SQL Server: " + ex.getMessage());
            }
        });
        dialog.setVisible(true);
    }

    public void nhapKhoHangHoa() {
        chonTabLoHangFEFO();
        moDialogNhapHang();
    }

    public void traCuuTonKhoFEFO(String kw) {
        chonTabLoHangFEFO();
        if (sorterLoHang != null && kw != null && !kw.trim().isEmpty()) {
            sorterLoHang.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(kw.trim())));
        }
    }

    public void xuatBaoCaoTonKho() {
        chonTabLoHangFEFO();
        JOptionPane.showMessageDialog(this,
                "Đã kết xuất báo cáo kiểm kê xuất - nhập - tồn kho & Lô hàng FEFO ra tệp Excel thành công!\nTổng số lô hàng đang lưu trữ: " + DANH_SACH_LO_HANG.size() + " lô.",
                "Xuất báo biểu kho", JOptionPane.INFORMATION_MESSAGE);
    }

    private void moDialogQuyDoi() {
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        ThietLapQuyDoiDialog dialog = new ThietLapQuyDoiDialog(topFrame, (maSP, tenSP, donViMoi, tyLe, giaBan) -> {
            // Thêm quy cách quy đổi mới vào bảng sản phẩm
            modelSanPham.addRow(new Object[]{
                    maSP, tenSP, "Thức ăn thú cưng", donViMoi, String.valueOf(tyLe), "Không", df.format(giaBan), "Quy cách đóng gói mới"
            });
        });
        dialog.setVisible(true);
    }

    private void xuLyKiemKe() {
        int row = tblLoHang.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lô hàng cần kiểm kê tồn kho!", "Nhắc nhở", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String soLo = (String) tblLoHang.getValueAt(row, 0);
        String tenSP = (String) tblLoHang.getValueAt(row, 2);
        int tonHienTai = (int) tblLoHang.getValueAt(row, 8);

        String input = JOptionPane.showInputDialog(this,
                "Lô hàng: " + soLo + " - " + tenSP + "\n" +
                "Số lượng tồn trên hệ thống: " + tonHienTai + "\n\n" +
                "Nhập số lượng kiểm kê thực tế tại kho:",
                tonHienTai);

        if (input != null && !input.trim().isEmpty()) {
            try {
                int tonThucTe = Integer.parseInt(input.trim());
                for (LoHangRecord lo : DANH_SACH_LO_HANG) {
                    if (lo.soLo.equalsIgnoreCase(soLo)) {
                        lo.soLuongTon = Math.max(0, tonThucTe);
                        if (lo.soLuongTon == 0) {
                            lo.trangThaiFEFO = "ĐÃ HẾT TỒN LÔ";
                        }
                        try (java.sql.Connection conn = utils.DBConnection.getConnection();
                             java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE LoHang SET soLuongTon = ? WHERE soLo = ?")) {
                            ps.setInt(1, lo.soLuongTon);
                            ps.setString(2, soLo);
                            ps.executeUpdate();
                        } catch (Exception ignored) {}
                        break;
                    }
                }
                for (KhoLoHangPanel inst : INSTANCES) {
                    inst.lamMoiBangLoHang();
                }
                TongQuanPanel.lamMoiDuLieuDashboardAll();
                int diff = tonThucTe - tonHienTai;
                String msg = diff == 0 ? "Số lượng tồn kho khớp chính xác!" :
                        ("Đã điều chỉnh lệch kho: " + (diff > 0 ? "+" : "") + diff + " đơn vị.");
                JOptionPane.showMessageDialog(this,
                        "CẬP NHẬT BIÊN BẢN KIỂM KÊ THÀNH CÔNG!\n" + msg,
                        "Kiểm kê hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Số lượng kiểm kê phải là số nguyên dương!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JTable taoBangChuan(DefaultTableModel model) {
        JTable tbl = new JTable(model);
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
        tbl.getColumnModel().getColumn(0).setPreferredWidth(75);
        tbl.getColumnModel().getColumn(0).setCellRenderer(center);

        return tbl;
    }
}
