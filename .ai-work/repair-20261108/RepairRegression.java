import java.sql.*;
import java.math.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import entity.*;

/** Mutating integration tests: only a dedicated synthetic database is allowed. */
public class RepairRegression {
 static final String DB="MioRepair_20261108";
 static final String RUN="R"+Long.toString(System.currentTimeMillis(),36).toUpperCase();
 static int pass,fail;
 static String id(String suffix){return RUN+suffix;}
 static Connection connection()throws Exception {
  Connection c=utils.DBConnection.getConnection();
  try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT DB_NAME()")){
   r.next();if(!DB.equals(r.getString(1))){c.close();throw new IllegalStateException("Wrong test DB");}
  }return c;
 }
 static void sql(String q)throws Exception {try(Connection c=connection();Statement s=c.createStatement()){s.setQueryTimeout(90);s.execute(q);}}
 static long value(String q)throws Exception {try(Connection c=connection();Statement s=c.createStatement();ResultSet r=s.executeQuery(q)){r.next();return r.getLong(1);}}
 static void check(String name,boolean ok,String evidence){System.out.println((ok?"PASS ":"FAIL ")+name+" | "+evidence);if(ok)pass++;else fail++;}
 interface Action{void run()throws Exception;}
 static boolean rejected(Action action){try{action.run();return false;}catch(Exception expected){return true;}}
 static HoaDon sale(String suffix,String lot,int qty,String qd)throws Exception {
  var lo=new dao.LoHangDAO().getLoHangBySoLo(lot);
  var q=new dao.SanPhamDAO().getQuyDoiBySanPham(lo.getSanPham().getMaSanPham()).stream().filter(x->x.getMaQuyDoi().equals(qd)).findFirst().orElse(null);
  var h=new HoaDon();h.setMaHoaDon(id(suffix));var n=new NhanVien();n.setMaNhanVien("NV003");h.setNhanVien(n);h.setPhuongThucThanhToan("Tiền mặt");
  var x=new ChiTietXuatLo(h.getMaHoaDon()+"_1",h,qty,BigDecimal.valueOf(10000),lo,q);x.setSoLuongXuatCoSo(qty);h.addChiTiet(x);return h;
 }
 static PhieuDoiTra returns(String suffix,HoaDon original,int qty,boolean restock){
  var p=new PhieuDoiTra();p.setMaPhieuDoiTra(id(suffix));p.setHoaDonGoc(original);p.setNhanVien(original.getNhanVien());p.setTongTienHoan(BigDecimal.valueOf(qty*10000L));
  var d=new ChiTietDoiTra(p.getMaPhieuDoiTra()+"_1",p,original.getDsChiTiet().get(0).getSanPham(),qty,BigDecimal.valueOf(qty*10000L));d.setSoLuongCoSoTra(qty);
  if(restock){d.setSoLo(((ChiTietXuatLo)original.getDsChiTiet().get(0)).getLoHang().getSoLo());d.setSoLuongCoSoHoan(qty);}p.addChiTietDoiTra(d);return p;
 }
 static HoaDon service(String suffix,int qty,String dvId,long price){
  var h=new HoaDon();h.setMaHoaDon(id(suffix));var n=new NhanVien();n.setMaNhanVien("NV003");h.setNhanVien(n);h.setPhuongThucThanhToan("Tiền mặt");
  var d=new DichVu();d.setMaDichVu(dvId);h.addChiTiet(new ChiTietDichVu(id(suffix)+"_1",h,qty,BigDecimal.valueOf(price),d,null,n,BigDecimal.ZERO));return h;
 }
 static void fixture()throws Exception {
  sql("INSERT INTO KhachHang(maKhachHang,hoTen,soDienThoai,diemTichLuy) VALUES('"+id("KH")+"',N'SYNTHETIC REPAIR','"+id("TEL")+"',100);");
  for(int k=1;k<=4;k++){
   sql("INSERT INTO SanPham(maSanPham,maDanhMuc,tenSanPham,maVach,donViCoSo,giaBanCoSo) VALUES('"+id("SP"+k)+"','DM01',N'SYNTHETIC REPAIR "+k+"','"+id("BAR"+k)+"',N'Gói',10000);"+
    "INSERT INTO QuyDoiDonVi(maQuyDoi,maSanPham,maDonVi,tyLeQuyDoi,laDonViCoSo,giaBan) VALUES('"+id("QD"+k)+"','"+id("SP"+k)+"','DVT01',1,1,10000);"+
    "INSERT INTO LoHang(soLo,maSanPham,maNhaCungCap,ngaySanXuat,hanSuDung,ngayNhap,donGiaNhapCoSo,soLuongNhap,soLuongTon) VALUES('"+id("LOT"+k)+"','"+id("SP"+k)+"','NCC01',DATEADD(day,-30,GETDATE()),DATEADD(day,"+(k==3?-1:90)+",GETDATE()),DATEADD(day,-20,GETDATE()),5000,"+(k==2?10:10000)+","+(k==2?10:10000)+");");
  }
  sql("INSERT INTO QuyDoiDonVi(maQuyDoi,maSanPham,maDonVi,tyLeQuyDoi,laDonViCoSo,giaBan) VALUES('"+id("PACK")+"','"+id("SP1")+"','DVT06',10,0,100000);");
 }
 public static void main(String[] args)throws Exception {
  connection().close();fixture();var dao=new dao.HoaDonDAO();
  var normal=sale("NORMAL",id("LOT4"),1,id("QD4"));long before=value("SELECT soLuongTon FROM LoHang WHERE soLo='"+id("LOT4")+"'");dao.thanhToanHoaDon(normal);
  check("cash_sale",value("SELECT soLuongTon FROM LoHang WHERE soLo='"+id("LOT4")+"'")==before-1,"stock decremented once");
  var insufficient=sale("INSUFF",id("LOT4"),20000,id("QD4"));before=value("SELECT soLuongTon FROM LoHang WHERE soLo='"+id("LOT4")+"'");
  check("insufficient_stock_rollback",rejected(()->dao.thanhToanHoaDon(insufficient))&&value("SELECT COUNT(*) FROM HoaDon WHERE maHoaDon='"+insufficient.getMaHoaDon()+"'")==0&&value("SELECT soLuongTon FROM LoHang WHERE soLo='"+id("LOT4")+"'")==before,"header and stock preserved");
  var split=sale("SPLIT",id("LOT1"),5,"ABSENT");dao.thanhToanHoaDon(split);
  check("split_pack_real_base_unit",value("SELECT COUNT(*) FROM ChiTietXuatLo WHERE maChiTiet='"+split.getMaHoaDon()+"_CT1' AND maQuyDoi='"+id("QD1")+"' AND soLuongXuatCoSo=5")==1,"null QD resolved to real base conversion");
  var expired=sale("EXPIRED",id("LOT3"),1,id("QD3"));check("expired_lot_rejected",rejected(()->dao.thanhToanHoaDon(expired)),"DAO guard");
  var negative=sale("NEGATIVE",id("LOT4"),-2,id("QD4"));check("negative_export_rejected",rejected(()->dao.thanhToanHoaDon(negative)),"DAO guard");
  var mismatch=sale("MISMATCH",id("LOT4"),1,id("QD4"));mismatch.setThanhTien(BigDecimal.ONE);check("wrong_total_rejected",rejected(()->dao.thanhToanHoaDon(mismatch)),"header1 vs line10000");
  var wrongUnit=sale("WRONGQD",id("LOT4"),1,id("QD4"));((ChiTietXuatLo)wrongUnit.getDsChiTiet().get(0)).setQuyDoiDonVi(new dao.SanPhamDAO().getQuyDoiBySanPham(id("SP1")).get(0));
  check("foreign_product_unit_rejected",rejected(()->dao.thanhToanHoaDon(wrongUnit)),"QD must match lot product");
  var points=sale("POINTS",id("LOT4"),10,id("QD4"));points.setKhachHang(new dao.KhachHangDAO().findByMaKhachHang(id("KH")));points.setGiamGia(BigDecimal.valueOf(20000));dao.thanhToanHoaDonDayDu(points,null,null,null,null,20,0);
  check("redeem_points",value("SELECT diemTichLuy FROM KhachHang WHERE maKhachHang='"+id("KH")+"'")==80,"100-20+floor(80000/100000)");
  var low=sale("LOWPOINTS",id("LOT4"),1,id("QD4"));low.setKhachHang(points.getKhachHang());check("insufficient_points_rollback",rejected(()->dao.thanhToanHoaDonDayDu(low,null,null,null,null,999,0))&&value("SELECT COUNT(*) FROM HoaDon WHERE maHoaDon='"+low.getMaHoaDon()+"'")==0,"atomic point/stock/header rollback");
  var damaged=returns("DAMAGED",normal,1,false);before=value("SELECT soLuongTon FROM LoHang WHERE soLo='"+id("LOT4")+"'");dao.luuPhieuDoiTra(damaged);
  check("damaged_return",value("SELECT soLuongTon FROM LoHang WHERE soLo='"+id("LOT4")+"'")==before&&value("SELECT soLuongCoSoTra FROM ChiTietDoiTra WHERE maPhieuDoiTra='"+damaged.getMaPhieuDoiTra()+"'")==1,"records return without stock");
  check("damaged_already_returned_blocks_duplicate",rejected(()->dao.luuPhieuDoiTra(returns("DUP",normal,1,true))),"damaged also counts toward sold limit");
  var raceOriginal=sale("RETSALE",id("LOT4"),1,id("QD4"));dao.thanhToanHoaDon(raceOriginal);
  ExecutorService pool=Executors.newFixedThreadPool(8);
  try{
   CountDownLatch go=new CountDownLatch(1);List<Future<Boolean>> jobs=new ArrayList<>();
   for(int k=0;k<2;k++){var p=returns("RACE"+k,raceOriginal,1,true);jobs.add(pool.submit(()->{go.await();try{return new dao.HoaDonDAO().luuPhieuDoiTra(p);}catch(Exception e){return false;}}));}
   go.countDown();int accepted=0;for(var f:jobs)if(f.get(90,TimeUnit.SECONDS))accepted++;
   check("concurrent_return_exactly_one",accepted==1,"accepted="+accepted+" of two attempts for one sold unit");
   CountDownLatch saleGo=new CountDownLatch(1);jobs.clear();
   for(int k=0;k<2;k++){var h=sale("STOCK"+k,id("LOT2"),8,id("QD2"));jobs.add(pool.submit(()->{saleGo.await();try{return new dao.HoaDonDAO().thanhToanHoaDon(h);}catch(Exception e){return false;}}));}
   saleGo.countDown();accepted=0;for(var f:jobs)if(f.get(90,TimeUnit.SECONDS))accepted++;
   check("concurrent_sale_no_oversell",accepted==1&&value("SELECT soLuongTon FROM LoHang WHERE soLo='"+id("LOT2")+"'")==2,"8-unit orders from stock10; accepted="+accepted);
   if(args.length>0&&args[0].equals("stress")){
    var completed=new AtomicInteger();var errors=new AtomicInteger();List<Future<?>> load=new ArrayList<>();long start=System.nanoTime();before=value("SELECT soLuongTon FROM LoHang WHERE soLo='"+id("LOT4")+"'");
    for(int k=0;k<8;k++){final int worker=k;load.add(pool.submit(()->{for(int n=0;n<50;n++)try{if(new dao.HoaDonDAO().thanhToanHoaDon(sale("L"+worker+"X"+n,id("LOT4"),1,id("QD4"))))completed.incrementAndGet();else errors.incrementAndGet();}catch(Exception e){errors.incrementAndGet();}}));}
    for(var f:load)f.get(180,TimeUnit.SECONDS);
    check("400_sales_8_workers",completed.get()==400&&errors.get()==0&&before-value("SELECT soLuongTon FROM LoHang WHERE soLo='"+id("LOT4")+"'")==400,"completed="+completed+" errors="+errors+" seconds="+(System.nanoTime()-start)/1e9);
   }
  }finally{pool.shutdownNow();}
  hotelSpa(dao);profileAndReports(dao);
  var stock=new dao.LoHangDAO();before=value("SELECT soLuongTon FROM LoHang WHERE soLo='"+id("LOT4")+"'");
  check("negative_stock_adjustment_rejected",!stock.congTonKho(id("LOT4"),-2)&&value("SELECT soLuongTon FROM LoHang WHERE soLo='"+id("LOT4")+"'")==before,"no quantity change");
  check("FEFO_excludes_expired",stock.getLoHangFEFOBySanPham(id("SP3"))==null,"no expired fallback");
  System.out.println("TOTAL pass="+pass+" fail="+fail+" run="+RUN+" database="+DB);if(fail>0)throw new AssertionError("Regression failures="+fail);
 }
 static void hotelSpa(dao.HoaDonDAO dao)throws Exception {
  for(int k=1;k<=3;k++)sql("INSERT INTO PhongLuuTru(maPhong,loaiPhong,donGiaNgay,trangThai,tenThuCung,thoiGianCheckIn,phiDichVuPhatSinh) VALUES('"+id("ROOM"+k)+"','VIP',300000,N'ĐANG CÓ KHÁCH',N'SYNTHETIC PET',DATEADD(day,-2,GETDATE()),0);");
  var h=service("HOTEL",1,"DV008",600000);((ChiTietDichVu)h.getDsChiTiet().get(0)).setMaPhongHotel(id("ROOM1"));dao.thanhToanHoaDonDayDu(h,null,null,null,List.of(id("ROOM1")),0,0);
  check("hotel_two_days",value("SELECT soNgayLuuTru FROM LichSuLuuTru WHERE maHoaDon='"+h.getMaHoaDon()+"'")==2,"qty1 hotel line persists2 calendar days");
  var multi=service("HOTELS",1,"DV008",600000);((ChiTietDichVu)multi.getDsChiTiet().get(0)).setMaPhongHotel(id("ROOM2"));var second=service("SECOND",1,"DV008",600000).getDsChiTiet().get(0);((ChiTietDichVu)second).setMaPhongHotel(id("ROOM3"));multi.addChiTiet(second);dao.thanhToanHoaDonDayDu(multi,null,null,null,List.of(id("ROOM2"),id("ROOM3")),0,0);
  check("two_rooms_one_invoice",value("SELECT COUNT(*) FROM LichSuLuuTru WHERE maHoaDon='"+multi.getMaHoaDon()+"' AND soNgayLuuTru=2 AND tongTien=600000")==2,"two distinct room histories");
  sql("INSERT INTO TheSpaPass(maThe,sdtKhachHang,tenKhachHang,tenGoi,tongSoBuoi,soBuoiDaDung,donGiaGoi,ngayKichHoat,ngayHetHan,trangThai) VALUES('"+id("PASS")+"','"+id("TEL")+"',N'SYNTHETIC',N'TEST10',10,0,1000000,GETDATE(),DATEADD(day,90,GETDATE()),N'ĐANG HOẠT ĐỘNG');");
  var spa=service("SPA",2,"DV004",0);spa.setKhachHang(new dao.KhachHangDAO().findByMaKhachHang(id("KH")));((ChiTietDichVu)spa.getDsChiTiet().get(0)).setSuDungSpaPass(true);dao.thanhToanHoaDonDayDu(spa,null,id("PASS"),null,null,0,0);
  check("two_spa_sessions",value("SELECT soBuoiDaDung FROM TheSpaPass WHERE maThe='"+id("PASS")+"'")==2&&value("SELECT soLuot FROM LichSuSuDungSpaPass WHERE maHoaDon='"+spa.getMaHoaDon()+"'")==2,"counter and history both2");
  var wrong=service("WRONGSPA",1,"DV004",0);((ChiTietDichVu)wrong.getDsChiTiet().get(0)).setSuDungSpaPass(true);
  check("spa_customer_required",rejected(()->dao.thanhToanHoaDonDayDu(wrong,null,id("PASS"),null,null,0,0))&&value("SELECT soBuoiDaDung FROM TheSpaPass WHERE maThe='"+id("PASS")+"'")==2,"guest cannot consume linked card");
 }
 static void profileAndReports(dao.HoaDonDAO dao)throws Exception {
  var cashier=new NhanVien();cashier.setMaNhanVien("NV003");cashier.setChucVu("NVTN");utils.UserSession.initSession(cashier);
  long before=value("SELECT diemTichLuy FROM KhachHang WHERE maKhachHang='"+id("KH")+"'");bus.BanHangBus.luuHoacCapNhatKhachHang(id("TEL"),"SYNTHETIC renamed",999);
  check("cashier_profile_preserves_points",value("SELECT diemTichLuy FROM KhachHang WHERE maKhachHang='"+id("KH")+"'")==before,"stale client points999 ignored");
  var stale=new dao.KhachHangDAO().findByMaKhachHang(id("KH"));stale.setDiemTichLuy(999);new dao.KhachHangDAO().update(stale);
  check("DAO_profile_preserves_points",value("SELECT diemTichLuy FROM KhachHang WHERE maKhachHang='"+id("KH")+"'")==before,"legacy DAO cannot bypass cashier role");utils.UserSession.clearSession();
  var pack=sale("PACKSALE",id("LOT1"),1,id("PACK"));pack.getDsChiTiet().get(0).setDonGiaLucBan(BigDecimal.valueOf(100000));((ChiTietXuatLo)pack.getDsChiTiet().get(0)).setSoLuongXuatCoSo(10);pack.capNhatTongTien();dao.thanhToanHoaDon(pack);
  var report=new dao.BaoCaoThongKeDAO();var tu=LocalDate.now().minusDays(60).atStartOfDay();var den=LocalDate.now().plusDays(1).atStartOfDay();long real=value("SELECT SUM(x.soLuongXuatCoSo) FROM ChiTietXuatLo x JOIN ChiTietHoaDon c ON c.maChiTiet=x.maChiTiet WHERE c.maSanPham='"+id("SP1")+"'");
  long shown=report.getTopBanChay(tu,den).stream().filter(x->id("SP1").equals(x.ma)).mapToLong(x->x.soLuong).sum();check("top_base_units",shown==real,"shown="+shown+" exported="+real);
  long restocked=value("SELECT COALESCE(SUM(soLuongCoSoHoan),0) FROM ChiTietDoiTra WHERE maSanPham='"+id("SP4")+"'");long xnt=report.getXuatNhapTon(tu,den).stream().filter(x->id("SP4").equals(x.maSanPham)).mapToLong(x->x.hoanTraTrongKy).sum();check("XNT_actual_restock",xnt==restocked,"report="+xnt+" physical="+restocked);
  sql("UPDATE HoaDon SET thanhTien=123 WHERE maHoaDon='"+pack.getMaHoaDon()+"'; UPDATE ChiTietHoaDon SET thanhTien=99 WHERE maHoaDon='"+pack.getMaHoaDon()+"';");
  var stored=dao.findById(pack.getMaHoaDon());check("preserve_stored_invoice_values",stored.getThanhTien().compareTo(BigDecimal.valueOf(123))==0&&stored.getDsChiTiet().get(0).getThanhTien().compareTo(BigDecimal.valueOf(99))==0,"deliberate SQL fixture header123 line99");
 }
}
