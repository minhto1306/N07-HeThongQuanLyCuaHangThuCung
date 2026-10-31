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
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private static void khoiTaoLoHangMacDinhNeuCan() {
        if (DANH_SACH_LO_HANG.isEmpty()) {
            try {
                models.dao.LoHangDAO loDao = new models.dao.LoHangDAO();
                List<models.entity.LoHang> dbList = loDao.getAllLoHang();
                if (dbList != null && !dbList.isEmpty()) {
                    for (models.entity.LoHang lh : dbList) {
                        String nsxStr = lh.getNgaySanXuat() != null ? lh.getNgaySanXuat().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                        String hsdStr = lh.getHanSuDung() != null ? lh.getHanSuDung().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                        long days = lh.getHanSuDung() != null ? java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), lh.getHanSuDung()) : 999;
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
                }
            } catch (Exception e) {
                System.err.println("[KhoLoHangPanel] Lỗi nạp lô hàng CSDL: " + e.getMessage());
            }
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
            inst.lamMoiBangROP();
        }
    }

    public static void truTonKhoTheoFEFO(String maSP, int soLuongMua) {
        if (maSP == null || soLuongMua <= 0) return;
        khoiTaoLoHangMacDinhNeuCan();
        int canTru = soLuongMua;
        models.dao.LoHangDAO loDao = new models.dao.LoHangDAO();
        for (LoHangRecord lo : DANH_SACH_LO_HANG) {
            boolean match = lo.maSP.equalsIgnoreCase(maSP) || lo.tenSP.equalsIgnoreCase(maSP);
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
            inst.lamMoiBangROP();
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
            inst.lamMoiBangROP();
        }
    }

    private final DecimalFormat df = new DecimalFormat("#,### đ");

    private DefaultTableModel modelSanPham;
    private JTable tblSanPham;
    private TableRowSorter<DefaultTableModel> sorterSanPham;

    private DefaultTableModel modelLoHang;
    private JTable tblLoHang;
    private TableRowSorter<DefaultTableModel> sorterLoHang;

    private DefaultTableModel modelROP;
    private JTable tblROP;
    private TableRowSorter<DefaultTableModel> sorterROP;
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
        tabbedPane.addTab("  Điểm Tái Đặt Hàng (ROP) & Đề Xuất Nhập Kho",
                IconHelper.getPrimaryIcon(IconHelper.ALERT_TRIANGLE, 16), taoTabDiemDatHangROP());

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void chonTabSanPham() {
        if (tabbedPane != null) tabbedPane.setSelectedIndex(0);
    }

    public void chonTabLoHangFEFO() {
        if (tabbedPane != null) tabbedPane.setSelectedIndex(1);
    }

    public void chonTabDiemDatHangROP() {
        if (tabbedPane != null) tabbedPane.setSelectedIndex(2);
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
                sorterLoHang.setRowFilter(RowFilter.regexFilter("(?i)Nguy cấp|ngày"));
            } else if (idx == 3) {
                sorterLoHang.setRowFilter(RowFilter.regexFilter("(?i)Hết hạn|Quá hạn|Đã hết|Hết",9));
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
                    inst.lamMoiBangROP();
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

    // =========================================================================
    // MÔ HÌNH 5: ĐIỂM TÁI ĐẶT HÀNG (ROP) & ĐỀ XUẤT NHẬP KHO DỰ PHÒNG
    // Công thức: ROP = (d * L) + SS
    // =========================================================================

    public static class ROPItem {
        public String maSP;
        public String tenSP;
        public int tonThucTe;
        public int tieuThuNgay; // d
        public int leadTime;    // L
        public int tonAnToan;   // SS
        public int rop;         // (d * L) + SS
        public String trangThai;
        public int deXuatNhap;  // Q
        public String nhaCungCap;
        public long donGiaNhap;

        public ROPItem(String maSP, String tenSP, int tonThucTe, int tieuThuNgay, int leadTime, int tonAnToan,
                       String trangThai, int deXuatNhap, String nhaCungCap, long donGiaNhap) {
            this.maSP = maSP;
            this.tenSP = tenSP;
            this.tonThucTe = tonThucTe;
            this.tieuThuNgay = tieuThuNgay;
            this.leadTime = leadTime;
            this.tonAnToan = tonAnToan;
            this.rop = (tieuThuNgay * leadTime) + tonAnToan;
            this.trangThai = trangThai;
            this.deXuatNhap = deXuatNhap;
            this.nhaCungCap = nhaCungCap;
            this.donGiaNhap = donGiaNhap;
        }
    }

    public static List<ROPItem> tinhToanDanhSachROP() {
        khoiTaoLoHangMacDinhNeuCan();
        List<ROPItem> list = new ArrayList<>();

        class SpConfig {
            String ma, ten, ncc;
            int d, L, ss;
            long giaNhap;
            SpConfig(String ma, String ten, String ncc, int d, int L, int ss, long giaNhap) {
                this.ma = ma; this.ten = ten; this.ncc = ncc; this.d = d; this.L = L; this.ss = ss; this.giaNhap = giaNhap;
            }
        }

        List<SpConfig> configs = new ArrayList<>();
        try {
            List<models.entity.SanPham> dsSP = new models.dao.SanPhamDAO().getAllSanPham();
            for (models.entity.SanPham sp : dsSP) {
                String ncc = "Nhà phân phối chính hãng";
                long giaNhap = 50000L;
                for (LoHangRecord lo : DANH_SACH_LO_HANG) {
                    if (sp.getMaSanPham().equalsIgnoreCase(lo.maSP)) {
                        ncc = lo.nhaCungCap;
                        try {
                            giaNhap = Long.parseLong(lo.giaNhap.replaceAll("[^0-9]", ""));
                        } catch (Exception ignored) {}
                        break;
                    }
                }
                configs.add(new SpConfig(sp.getMaSanPham(), sp.getTenSanPham(), ncc, 5, 2, 5, giaNhap));
            }
        } catch (Exception e) {
            System.err.println("[KhoLoHangPanel] Lỗi nạp ROP từ CSDL: " + e.getMessage());
        }

        for (SpConfig cfg : configs) {
            int tonTong = 0;
            boolean coLoCanDate = false;

            for (LoHangRecord lo : DANH_SACH_LO_HANG) {
                if (cfg.ma.equalsIgnoreCase(lo.maSP) || cfg.ten.equalsIgnoreCase(lo.tenSP)) {
                    tonTong += Math.max(0, lo.soLuongTon);
                    String st = lo.trangThaiFEFO != null ? lo.trangThaiFEFO.toUpperCase() : "";
                    if (st.contains("CẬN DATE") || st.contains("NGUY CẤP") || st.contains("HẾT HẠN") || st.contains("ƯU TIÊN")) {
                        if (lo.soLuongTon > 0) {
                            coLoCanDate = true;
                        }
                    }
                }
            }

            int ropVal = (cfg.d * cfg.L) + cfg.ss;
            String trangThai;
            int qDeXuat;

            if (coLoCanDate) {
                trangThai = "FEFO: CẬN DATE (Ưu tiên xả hàng)";
                qDeXuat = 0;
            } else if (tonTong <= ropVal) {
                trangThai = "CẦN NHẬP HÀNG GẤP (Tồn <= ROP)";
                qDeXuat = Math.max(10, (cfg.d * 14) + cfg.ss - tonTong);
            } else {
                trangThai = "AN TOÀN (Tồn đủ bán)";
                qDeXuat = 0;
            }

            list.add(new ROPItem(cfg.ma, cfg.ten, tonTong, cfg.d, cfg.L, cfg.ss, trangThai, qDeXuat, cfg.ncc, cfg.giaNhap));
        }

        return list;
    }

    private JPanel taoTabDiemDatHangROP() {
        JPanel pnl = new JPanel(new BorderLayout(10, 12));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlToolbar.setOpaque(false);

        JTextField txtSearch = new JTextField(15);
        txtSearch.setPreferredSize(new Dimension(190, 36));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm mã, tên sản phẩm...");
        txtSearch.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.SEARCH, 15, ThemeColors.TEXT_MUTED));
        txtSearch.setBorder(ThemeColors.createInputBorder());

        JComboBox<String> cboLoc = new JComboBox<>(new String[]{"Tất cả mặt hàng", "Cần nhập hàng gấp (Tồn <= ROP)", "FEFO Cận date (Ưu tiên xả)", "An toàn"});
        cboLoc.setPreferredSize(new Dimension(215, 36));

        JButton btnTaoPO = ThemeColors.createPrimaryButton("Tạo Đơn Đề Xuất Nhập Hàng (Draft PO)", IconHelper.getIcon(IconHelper.FILE_TEXT, 14, Color.WHITE));
        JButton btnXuatCsv = ThemeColors.createSecondaryButton("Xuất Báo Cáo ROP (.csv)", IconHelper.getIcon(IconHelper.FILE_TEXT, 14, ThemeColors.TEXT_SECONDARY));
        JButton btnLamMoi = ThemeColors.createSecondaryButton("Làm mới (F5)", IconHelper.getIcon(IconHelper.REFRESH, 14, ThemeColors.TEXT_SECONDARY));

        btnTaoPO.addActionListener(e -> moDialogDeXuatNhapHang());
        btnXuatCsv.addActionListener(e -> xuatBaoCaoROPCsv());
        btnLamMoi.addActionListener(e -> lamMoiBangROP());

        cboLoc.addActionListener(e -> {
            int idx = cboLoc.getSelectedIndex();
            if (sorterROP == null) return;
            if (idx == 1) {
                sorterROP.setRowFilter(RowFilter.regexFilter("(?i)CẦN NHẬP", 8));
            } else if (idx == 2) {
                sorterROP.setRowFilter(RowFilter.regexFilter("(?i)CẬN DATE", 8));
            } else if (idx == 3) {
                sorterROP.setRowFilter(RowFilter.regexFilter("(?i)AN TOÀN", 8));
            } else {
                sorterROP.setRowFilter(null);
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { locROP(txtSearch.getText().trim()); }
            @Override
            public void removeUpdate(DocumentEvent e) { locROP(txtSearch.getText().trim()); }
            @Override
            public void changedUpdate(DocumentEvent e) { locROP(txtSearch.getText().trim()); }
        });

        pnlToolbar.add(new JLabel("Tìm:"));
        pnlToolbar.add(txtSearch);
        pnlToolbar.add(new JLabel("Lọc trạng thái:"));
        pnlToolbar.add(cboLoc);
        pnlToolbar.add(btnTaoPO);
        pnlToolbar.add(btnXuatCsv);
        pnlToolbar.add(btnLamMoi);

        String[] cols = {"STT", "Mã SP", "Tên sản phẩm", "Tồn thực tế", "Tiêu thụ/ngày (d)", "Lead Time (L)", "Tồn an toàn (SS)", "Điểm ROP", "Trạng thái khuyến nghị", "Đề xuất nhập (Q)", "Nhà cung cấp"};
        modelROP = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        lamMoiBangROP();

        tblROP = taoBangChuan(modelROP);
        sorterROP = new TableRowSorter<>(modelROP);
        tblROP.setRowSorter(sorterROP);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        tblROP.getColumnModel().getColumn(0).setPreferredWidth(35);
        tblROP.getColumnModel().getColumn(0).setCellRenderer(center);
        tblROP.getColumnModel().getColumn(1).setPreferredWidth(65);
        tblROP.getColumnModel().getColumn(1).setCellRenderer(center);
        tblROP.getColumnModel().getColumn(2).setPreferredWidth(190);
        tblROP.getColumnModel().getColumn(3).setPreferredWidth(75);
        tblROP.getColumnModel().getColumn(3).setCellRenderer(center);
        tblROP.getColumnModel().getColumn(4).setPreferredWidth(100);
        tblROP.getColumnModel().getColumn(4).setCellRenderer(center);
        tblROP.getColumnModel().getColumn(5).setPreferredWidth(85);
        tblROP.getColumnModel().getColumn(5).setCellRenderer(center);
        tblROP.getColumnModel().getColumn(6).setPreferredWidth(95);
        tblROP.getColumnModel().getColumn(6).setCellRenderer(center);
        tblROP.getColumnModel().getColumn(7).setPreferredWidth(75);
        tblROP.getColumnModel().getColumn(7).setCellRenderer(center);
        tblROP.getColumnModel().getColumn(8).setPreferredWidth(195);
        tblROP.getColumnModel().getColumn(9).setPreferredWidth(100);
        tblROP.getColumnModel().getColumn(9).setCellRenderer(center);
        tblROP.getColumnModel().getColumn(10).setPreferredWidth(180);

        tblROP.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                if (value != null) {
                    String str = value.toString();
                    if (str.contains("CẦN NHẬP")) {
                        lbl.setForeground(new Color(220, 38, 38));
                    } else if (str.contains("CẬN DATE")) {
                        lbl.setForeground(new Color(217, 119, 6));
                    } else {
                        lbl.setForeground(new Color(22, 163, 74));
                    }
                }
                return lbl;
            }
        });

        pnl.add(pnlToolbar, BorderLayout.NORTH);
        pnl.add(new JScrollPane(tblROP), BorderLayout.CENTER);
        return pnl;
    }

    public void lamMoiBangROP() {
        if (modelROP == null) return;
        modelROP.setRowCount(0);
        List<ROPItem> ds = tinhToanDanhSachROP();
        int stt = 1;
        for (ROPItem it : ds) {
            modelROP.addRow(new Object[]{
                    stt++,
                    it.maSP,
                    it.tenSP,
                    it.tonThucTe,
                    it.tieuThuNgay + " đv/ngày",
                    it.leadTime + " ngày",
                    it.tonAnToan + " đv",
                    it.rop,
                    it.trangThai,
                    (it.deXuatNhap > 0 ? ("+ " + it.deXuatNhap + " đv") : "—"),
                    it.nhaCungCap
            });
        }
    }

    private void locROP(String text) {
        if (sorterROP == null) return;
        if (text.isEmpty()) {
            sorterROP.setRowFilter(null);
        } else {
            sorterROP.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1, 2, 8, 10));
        }
    }

    private void moDialogDeXuatNhapHang() {
        List<ROPItem> dsAll = tinhToanDanhSachROP();
        List<ROPItem> dsCanNhap = new ArrayList<>();
        long tongTienUocTinh = 0;
        for (ROPItem it : dsAll) {
            if (it.deXuatNhap > 0) {
                dsCanNhap.add(it);
                tongTienUocTinh += (it.deXuatNhap * it.donGiaNhap);
            }
        }

        if (dsCanNhap.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "TẤT CẢ MẶT HÀNG ĐANG Ở MỨC TỒN AN TOÀN!\n" +
                            "Không có sản phẩm nào chạm ngưỡng ROP (hoặc đang ưu tiên xả hàng cận date FEFO).\n" +
                            "Chưa cần tạo đơn đề xuất nhập hàng mới.",
                    "Kho hàng an toàn", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Đề Xuất Đơn Hàng Nhập Kho (Draft PO)", true);
        dialog.setSize(750, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBorder(new EmptyBorder(12, 16, 8, 16));
        pnlHeader.setBackground(new Color(254, 243, 199));

        JLabel lblTitle = new JLabel("ĐỀ XUẤT NHẬP HÀNG TỰ ĐỘNG THEO CÔNG THỨC ROP = (d × L) + SS", IconHelper.getIcon(IconHelper.ALERT_TRIANGLE, 18, new Color(217, 119, 6)), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(146, 64, 14));
        JLabel lblSubtitle = new JLabel("Hệ thống tự động tính toán nhu cầu tồn bù 14 ngày & loại trừ các mặt hàng FEFO đang cận date.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtitle.setForeground(new Color(180, 83, 9));

        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblSubtitle, BorderLayout.SOUTH);

        String[] cols = {"Mã SP", "Tên sản phẩm", "Tồn kho", "Ngưỡng ROP", "Số lượng đặt (Q)", "Đơn giá nhập", "Thành tiền ước tính", "Nhà cung cấp"};
        DefaultTableModel modelPO = new DefaultTableModel(cols, 0);
        for (ROPItem it : dsCanNhap) {
            long tt = it.deXuatNhap * it.donGiaNhap;
            modelPO.addRow(new Object[]{
                    it.maSP, it.tenSP, it.tonThucTe, it.rop, it.deXuatNhap, df.format(it.donGiaNhap), df.format(tt), it.nhaCungCap
            });
        }
        JTable tblPO = new JTable(modelPO);
        tblPO.setRowHeight(30);
        tblPO.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel pnlFooter = new JPanel(new BorderLayout(10, 0));
        pnlFooter.setBorder(new EmptyBorder(10, 16, 12, 16));

        JLabel lblTongTien = new JLabel("TỔNG TIỀN DỰ KIẾN: " + df.format(tongTienUocTinh));
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTongTien.setForeground(ThemeColors.PRIMARY_ACTIVE);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnLuuTxt = ThemeColors.createPrimaryButton("Lưu Phiếu Đề Xuất (.txt)", IconHelper.getIcon(IconHelper.FILE_TEXT, 13, Color.WHITE));
        JButton btnDong = ThemeColors.createSecondaryButton("Đóng", null);

        final long finalTongTien = tongTienUocTinh;
        btnLuuTxt.addActionListener(e -> {
            try {
                File dir = new File("export");
                if (!dir.exists()) dir.mkdirs();
                String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                File outFile = new File(dir, "DeXuatNhapHang_ROP_" + timestamp + ".txt");
                try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8))) {
                    pw.println("==========================================================================");
                    pw.println("             PHIẾU ĐỀ XUẤT NHẬP HÀNG TỰ ĐỘNG THEO ĐIỂM ROP                ");
                    pw.println("                   CỬA HÀNG THÚ CƯNG MIO PET SHOP                        ");
                    pw.println("==========================================================================");
                    pw.println("Thời gian tạo: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                    pw.println("Công thức ROP: ROP = (Tiêu thụ d * LeadTime L) + Tồn an toàn SS");
                    pw.println("Quy tắc FEFO : Đã loại bỏ các mặt hàng có lô cận date khỏi đề xuất nhập mới");
                    pw.println("--------------------------------------------------------------------------");
                    pw.printf("%-8s | %-30s | %-6s | %-6s | %-12s | %-15s%n", "Mã SP", "Tên sản phẩm", "Tồn", "Đặt Q", "Đơn giá", "Thành tiền");
                    pw.println("--------------------------------------------------------------------------");
                    for (ROPItem it : dsCanNhap) {
                        pw.printf("%-8s | %-30s | %-6d | %-6d | %-12s | %-15s%n",
                                it.maSP, it.tenSP, it.tonThucTe, it.deXuatNhap, df.format(it.donGiaNhap), df.format(it.deXuatNhap * it.donGiaNhap));
                    }
                    pw.println("--------------------------------------------------------------------------");
                    pw.println("TỔNG KINH PHÍ NHẬP HÀNG DỰ KIẾN: " + df.format(finalTongTien));
                    pw.println("==========================================================================");
                }
                JOptionPane.showMessageDialog(dialog,
                        "ĐÃ XUẤT PHIẾU ĐỀ XUẤT NHẬP HÀNG THÀNH CÔNG!\n\nĐường dẫn tệp:\n" + outFile.getAbsolutePath(),
                        "Xuất thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi khi lưu tệp: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnDong.addActionListener(e -> dialog.dispose());

        pnlButtons.add(btnLuuTxt);
        pnlButtons.add(btnDong);

        pnlFooter.add(lblTongTien, BorderLayout.WEST);
        pnlFooter.add(pnlButtons, BorderLayout.EAST);

        dialog.add(pnlHeader, BorderLayout.NORTH);
        dialog.add(new JScrollPane(tblPO), BorderLayout.CENTER);
        dialog.add(pnlFooter, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void xuatBaoCaoROPCsv() {
        try {
            File dir = new File("export");
            if (!dir.exists()) dir.mkdirs();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File outFile = new File(dir, "BaoCao_ROP_" + timestamp + ".csv");

            try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8)) {
                osw.write('\uFEFF');
                osw.write("STT,Mã SP,Tên sản phẩm,Tồn thực tế,Tiêu thụ ngày (d),Lead Time (L),Tồn an toàn (SS),Điểm ROP,Trạng thái khuyến nghị,Đề xuất nhập (Q),Nhà cung cấp\n");
                List<ROPItem> ds = tinhToanDanhSachROP();
                int stt = 1;
                for (ROPItem it : ds) {
                    osw.write(stt++ + ",\"" + it.maSP + "\",\"" + it.tenSP + "\"," + it.tonThucTe + ","
                            + it.tieuThuNgay + "," + it.leadTime + "," + it.tonAnToan + "," + it.rop + ",\""
                            + it.trangThai + "\"," + (it.deXuatNhap > 0 ? it.deXuatNhap : 0) + ",\"" + it.nhaCungCap + "\"\n");
                }
            }

            JOptionPane.showMessageDialog(this,
                    "ĐÃ XUẤT BÁO CÁO ROP (.CSV) CHUẨN UTF-8 BOM!\n\nĐường dẫn: " + outFile.getAbsolutePath(),
                    "Xuất báo cáo thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi xuất báo cáo CSV: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
