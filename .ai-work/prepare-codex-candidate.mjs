import fs from 'node:fs';
import crypto from 'node:crypto';
const out='.ai-work/candidate/', manifest=[];
const sha=b=>crypto.createHash('sha256').update(b).digest('hex');
function edit(path,fn){const raw=fs.readFileSync(path);let s=raw.toString('utf8').replaceAll('\r\n','\n');const replace=(a,b)=>{if(s.split(a).length!==2)throw Error('Nonunique '+path+' '+a.slice(0,70));s=s.replace(a,()=>b);};fn(replace,()=>s,v=>s=v);fs.mkdirSync(out+path.slice(0,path.lastIndexOf('/')),{recursive:true});fs.writeFileSync(out+path,s);manifest.push({path,sha256:sha(raw)});}
edit('src/dao/HoaDonDAO.java',(rep,get,set)=>{
 let s=get();let a=s.indexOf('    private void hoanTatHotelTrongTransaction('),b=s.indexOf('    private void kiemTraHoaDonHopLe(',a);
 s=s.slice(0,a)+`    private void hoanTatHotelTrongTransaction(Connection conn, HoaDon hoaDon, List<String> rooms) throws SQLException {
        HoaDonDichVu.completeHotel(conn, hoaDon, rooms);
    }

    private void xuLySpaPassTrongTransaction(Connection conn, HoaDon hoaDon, String card, TheSpaPass newCard) throws SQLException {
        HoaDonDichVu.processSpa(conn, hoaDon, card, newCard);
    }

`+s.slice(b);set(s);
 s=get();a=s.indexOf('    private void kiemTraHoaDonHopLe(');b=s.indexOf('    private void luuHoaDonTrongTransaction(',a);
 set(s.slice(0,a)+`    private void kiemTraHoaDonHopLe(HoaDon hoaDon) {
        HoaDonIntegrity.validateSale(hoaDon);
    }

`+s.slice(b));
 rep('        kiemTraHoaDonHopLe(hoaDon);','        kiemTraHoaDonHopLe(hoaDon);\n        if (diemSuDung < 0 || diemThuHoiDoTraHang < 0) throw new IllegalArgumentException("Số điểm không được âm");');
 rep('                truDiemTrongTransaction(conn, hoaDon, diemSuDung, diemThuHoiDoTraHang);','                // Return-point recovery is derived from the original customer inside the return transaction.\n                truDiemTrongTransaction(conn, hoaDon, diemSuDung, 0);');
 rep('WHERE soLo = ? AND soLuongTon >= ?";','WHERE soLo = ? AND soLuongTon >= ? AND hanSuDung >= CAST(GETDATE() AS DATE)";');
 rep('                    psXuat.setString(3, xuatLo.getQuyDoiDonVi() != null ? xuatLo.getQuyDoiDonVi().getMaQuyDoi() : null);','                    psXuat.setString(3, HoaDonIntegrity.resolveUnit(conn, xuatLo));');
 rep('                        hd.addChiTiet(ct);','                        hd.getDsChiTiet().add(ct); // Preserve the persisted invoice total.');
 rep('                    hd.setDsChiTiet(getChiTietByHoaDon(hd.getMaHoaDon()));','                    hd.getDsChiTiet().addAll(getChiTietByHoaDon(hd.getMaHoaDon()));');
 rep('            c.setSoLuongXuatCoSo(rs.getInt("soLuongXuatCoSo"));\n            return c;','            c.setSoLuongXuatCoSo(rs.getInt("soLuongXuatCoSo"));\n            c.setThanhTien(rs.getBigDecimal("thanhTien"));\n            return c;');
 rep('            return new ChiTietDichVu(','            ChiTietDichVu c = new ChiTietDichVu(');
 rep('                    rs.getBigDecimal("tienHoaHong")\n            );','                    rs.getBigDecimal("tienHoaHong")\n            );\n            c.setThanhTien(rs.getBigDecimal("thanhTien"));\n            return c;');
 rep('SUM(ct.soLuongDoiTra) AS tongDaTra','SUM(ct.soLuongCoSoTra) AS tongDaTra');
 rep('            System.err.println("[HoaDonDAO] Lỗi tra cứu số lượng đã trả: " + e.getMessage());','            throw new IllegalStateException("Không tải được lịch sử trả hàng; hãy kiểm tra migration và thử lại", e);');
 rep('        String sqlPhieu = "INSERT INTO PhieuDoiTra','        HoaDonIntegrity.ReturnPoints returnPoints = HoaDonIntegrity.validateReturn(conn, pdt);\n        String sqlPhieu = "INSERT INTO PhieuDoiTra');
 rep('soLuongCoSoHoan) VALUES (?, ?, ?, ?, ?, ?, ?)";','soLuongCoSoHoan, soLuongCoSoTra) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";');
 rep('                psCT.executeUpdate();\n\n                if (canHoanKho)', '                psCT.setInt(8, ct.getSoLuongCoSoTra());\n                psCT.executeUpdate();\n\n                if (canHoanKho)');
 s=get();a=s.indexOf('    private void luuPhieuDoiTraTrongTransaction(');b=s.indexOf('    /**\n     * Hủy hóa đơn',a);const method=s.slice(a,b);const end=method.lastIndexOf('\n    }');set(s.slice(0,a)+method.slice(0,end)+'\n        returnPoints.apply(conn);'+method.slice(end)+s.slice(b));
});
for(const path of ['src/entity/HoaDon.java','src/entity/ChiTietHoaDon.java'])edit(path,rep=>rep('if (this.thanhTien == null || this.thanhTien.compareTo(BigDecimal.ZERO) == 0)', 'if (this.thanhTien == null)'));
edit('src/entity/ChiTietDoiTra.java',rep=>{
 rep('    private int soLuongCoSoHoan;','    private int soLuongCoSoHoan;\n    private int soLuongCoSoTra;');
 rep('    public int getSoLuongCoSoHoan()', '    public int getSoLuongCoSoTra() { return soLuongCoSoTra; }\n    public void setSoLuongCoSoTra(int value) { soLuongCoSoTra = value; }\n    public int getSoLuongCoSoHoan()');
});
edit('src/entity/ChiTietDichVu.java',rep=>{
 rep('    private BigDecimal tienHoaHong;','    private BigDecimal tienHoaHong;\n    private String maPhongHotel;\n    private boolean suDungSpaPass;');
 rep('    public ThuCung getThuCung()', '    public String getMaPhongHotel() { return maPhongHotel; }\n    public void setMaPhongHotel(String value) { maPhongHotel = value; }\n    public boolean isSuDungSpaPass() { return suDungSpaPass; }\n    public void setSuDungSpaPass(boolean value) { suDungSpaPass = value; }\n\n    public ThuCung getThuCung()');
});
edit('src/entity/DoiTraTransferData.java',rep=>{
 rep('        public int soLuongCoSoHoan;', '        public int soLuongCoSoHoan;\n        public int soLuongCoSoTra;');
 rep('            this.soLo = soLo;\n            this.soLuongCoSoHoan = soLuongCoSoHoan;', '            this.soLuongCoSoTra = soLuongCoSoHoan;\n            this.soLo = nguyenTem ? soLo : null;\n            this.soLuongCoSoHoan = nguyenTem ? soLuongCoSoHoan : 0;');
 rep('        public int getSoLuongCoSoHoan()', '        public int getSoLuongCoSoTra() { return soLuongCoSoTra; }\n        public void setSoLuongCoSoTra(int value) { soLuongCoSoTra = value; }\n        public int getSoLuongCoSoHoan()');
});
edit('src/gui/BanHangPOSPanel.java',rep=>{
 rep('        btnThanhToan.addActionListener(e -> thucHienThanhToan());','        btnThanhToan.addActionListener(e -> thucHienThanhToan());\n        registerKeyboardAction(e -> {\n            if (isShowing() && btnThanhToan.isEnabled()) btnThanhToan.doClick();\n        }, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);');
 rep('                    hdReal.addChiTiet(ct);\n                }\n            }\n\n            // 2.', '                    if (ma.startsWith("DV-HOTEL-")) ct.setMaPhongHotel(ma.substring("DV-HOTEL-".length()));\n                    ct.setSuDungSpaPass("DV_SPAPASS".equals(ma));\n                    hdReal.addChiTiet(ct);\n                }\n            }\n\n            // 2.');
 rep('                        ctDoiTra.setSoLuongCoSoHoan(mon.soLuongCoSoHoan);','                        ctDoiTra.setSoLuongCoSoHoan(mon.soLuongCoSoHoan);\n                        ctDoiTra.setSoLuongCoSoTra(mon.soLuongCoSoTra);');
 rep('            String maTheSuDung = null;', '            if (pdt != null) pdt.setTongTienHoan(java.math.BigDecimal.valueOf(tienHoanThua));\n\n            String maTheSuDung = null;');
 rep('int diemThuongMoi = (int) (canTra / 100000L);','int diemThuongMoi = (sdt.isEmpty() && tenKhach.equalsIgnoreCase("Khách vãng lai")) ? 0 : (int) (canTra / 100000L);');
});
edit('src/gui/DoiTraHangPanel.java',rep=>{
 rep('        java.util.Map<String, Integer> mapDaTra = doiTraBus.layMapSoLuongDaTra(hd.maHoaDon);','        java.util.Map<String, Integer> mapDaTra;\n        try { mapDaTra = doiTraBus.layMapSoLuongDaTra(hd.maHoaDon); }\n        catch (RuntimeException ex) {\n            modelSanPham.setRowCount(0);\n            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không tải được lịch sử trả", JOptionPane.ERROR_MESSAGE);\n            return;\n        }');
 rep('                int slDaTra = mapDaTra.getOrDefault(ma.trim().toUpperCase(), 0);\n                int slConDuocTra = Math.max(0, slMua - slDaTra);', '                String productKey = ma.trim().toUpperCase();\n                int daTraCoSo = mapDaTra.getOrDefault(productKey, 0);\n                int consumed = Math.min(tongSoLuongCoSo, daTraCoSo);\n                mapDaTra.put(productKey, Math.max(0, daTraCoSo - consumed));\n                int slConDuocTra = Math.max(0, (tongSoLuongCoSo - consumed) / soLuongCoSoMoiDonVi);\n                int slDaTra = slMua - slConDuocTra;');
 rep('        if (currentHoaDon.sdt != null && !currentHoaDon.sdt.trim().isEmpty()) {\n            BanHangBus.luuHoacCapNhatKhachHang(currentHoaDon.sdt, currentHoaDon.tenKhach, diemMoi);\n        }', '        // Points are recovered by the DAO in the same transaction as the return.');
 rep('                if ((Boolean) mon[7]) {\n                    chiTiet.setSoLo', '                chiTiet.setSoLuongCoSoTra((Integer) mon[9]);\n                if ((Boolean) mon[7]) {\n                    chiTiet.setSoLo');
});
for(const path of ['src/dao/HoaDonIntegrity.java','src/dao/HoaDonDichVu.java'])manifest.push({path,sha256:null});
fs.writeFileSync('.ai-work/candidate/manifest.json',JSON.stringify(manifest,null,2));
console.log('Prepared '+manifest.length+' source candidates under .ai-work; project sources unchanged.');
