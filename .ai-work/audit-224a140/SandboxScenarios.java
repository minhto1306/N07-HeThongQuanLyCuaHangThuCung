import java.sql.*;
import java.nio.file.*;
import java.time.*;
import java.math.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import entity.*;
public class SandboxScenarios {
 static final String DB="MioAudit_224a140_20261108";
 static int pass=0,fail=0;
 static Connection connection()throws Exception{Connection c=utils.DBConnection.getConnection();try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT DB_NAME()")){r.next();if(!DB.equals(r.getString(1))){c.close();throw new IllegalStateException("Wrong target DB");}}return c;}
 static void sql(String q)throws Exception{try(Connection c=connection();Statement s=c.createStatement()){s.setQueryTimeout(120);s.execute(q);}}
 static long value(String q)throws Exception{try(Connection c=connection();Statement s=c.createStatement();ResultSet r=s.executeQuery(q)){r.next();return r.getLong(1);}}
 static void result(String n,boolean ok,String evidence){System.out.println((ok?"PASS ":"FAIL ")+n+" | "+evidence);if(ok)pass++;else fail++;}
 static HoaDon sale(String id,String lot,int qty,String qd)throws Exception {
  var lo=new dao.LoHangDAO().getLoHangBySoLo(lot);var q=new dao.SanPhamDAO().getQuyDoiBySanPham(lo.getSanPham().getMaSanPham()).stream().filter(x->x.getMaQuyDoi().equals(qd)).findFirst().orElse(null);
  NhanVien n=new NhanVien();n.setMaNhanVien("NV003");HoaDon h=new HoaDon();h.setMaHoaDon(id);h.setNhanVien(n);h.setPhuongThucThanhToan("Tiền mặt");ChiTietXuatLo x=new ChiTietXuatLo(id+"_1",h,qty,new BigDecimal("10000"),lo,q);x.setSoLuongXuatCoSo(qty);h.addChiTiet(x);return h;
 }
 static PhieuDoiTra returns(String id,HoaDon original,int qty,boolean restock){var p=new PhieuDoiTra();p.setMaPhieuDoiTra(id);p.setHoaDonGoc(original);p.setNhanVien(original.getNhanVien());var d=new ChiTietDoiTra(id+"_1",p,original.getDsChiTiet().get(0).getSanPham(),qty,BigDecimal.valueOf(qty*10000L));if(restock){d.setSoLo(((ChiTietXuatLo)original.getDsChiTiet().get(0)).getLoHang().getSoLo());d.setSoLuongCoSoHoan(qty);}p.addChiTietDoiTra(d);p.setTongTienHoan(BigDecimal.valueOf(qty*10000L));return p;}
 static HoaDon service(String id,int qty,String dvId,long money){HoaDon h=new HoaDon();h.setMaHoaDon(id);NhanVien n=new NhanVien();n.setMaNhanVien("NV003");h.setNhanVien(n);DichVu dv=new DichVu();dv.setMaDichVu(dvId);h.addChiTiet(new ChiTietDichVu(id+"_1",h,qty,BigDecimal.valueOf(money),dv,null,n,BigDecimal.ZERO));h.setPhuongThucThanhToan("Tiền mặt");return h;}
 public static void main(String[] args)throws Exception{
  System.setOut(new java.io.PrintStream("scenario-results.txt","UTF-8"));
  sql("""
   INSERT INTO KhachHang(maKhachHang,hoTen,soDienThoai,diemTichLuy) VALUES('KH900001',N'AUDIT fake customer','0900900001',100);
   INSERT INTO SanPham(maSanPham,maDanhMuc,tenSanPham,maVach,donViCoSo,giaBanCoSo) VALUES
   ('SP900001','DM01',N'AUDIT split lot', 'AUDIT900001',N'Gói',10000),('SP900002','DM01',N'AUDIT atomic stock','AUDIT900002',N'Gói',10000),('SP900003','DM01',N'AUDIT expired','AUDIT900003',N'Gói',10000),('SP900004','DM01',N'AUDIT load','AUDIT900004',N'Gói',10000);
   INSERT INTO QuyDoiDonVi(maQuyDoi,maSanPham,maDonVi,tyLeQuyDoi,laDonViCoSo,giaBan) VALUES
   ('QD900001','SP900001','DVT01',1,1,10000),('QD900002','SP900001','DVT06',10,0,100000),('QD900003','SP900002','DVT01',1,1,10000),('QD900004','SP900003','DVT01',1,1,10000),('QD900005','SP900004','DVT01',1,1,10000);
   INSERT INTO LoHang(soLo,maSanPham,maNhaCungCap,ngaySanXuat,hanSuDung,ngayNhap,donGiaNhapCoSo,soLuongNhap,soLuongTon) VALUES
   ('ATLOT1','SP900001','NCC01',DATEADD(day,-30,GETDATE()),DATEADD(day,10,GETDATE()),DATEADD(day,-20,GETDATE()),5000,5,5),
   ('ATLOT2','SP900001','NCC01',DATEADD(day,-30,GETDATE()),DATEADD(day,20,GETDATE()),DATEADD(day,-20,GETDATE()),5000,15,15),
   ('ATRACE','SP900002','NCC01',DATEADD(day,-30,GETDATE()),DATEADD(day,90,GETDATE()),DATEADD(day,-20,GETDATE()),5000,10,10),
   ('ATEXPIRED','SP900003','NCC01',DATEADD(day,-60,GETDATE()),DATEADD(day,-1,GETDATE()),DATEADD(day,-20,GETDATE()),5000,10,10),
   ('ATLOAD','SP900004','NCC01',DATEADD(day,-30,GETDATE()),DATEADD(day,90,GETDATE()),DATEADD(day,-20,GETDATE()),5000,10000,10000);
   INSERT INTO PhongLuuTru(maPhong,loaiPhong,donGiaNgay,trangThai,tenThuCung,thoiGianCheckIn,phiDichVuPhatSinh) VALUES('P011','VIP',300000,N'ĐANG CÓ KHÁCH',N'AUDIT PET',DATEADD(day,-2,GETDATE()),0),('P012','VIP',300000,N'ĐANG CÓ KHÁCH',N'AUDIT PET',DATEADD(day,-2,GETDATE()),0);
   INSERT INTO TheSpaPass(maThe,sdtKhachHang,tenKhachHang,tenGoi,tongSoBuoi,soBuoiDaDung,donGiaGoi,ngayKichHoat,ngayHetHan,trangThai) VALUES('PASS_AUDIT','0900900001',N'AUDIT fake',N'AUDIT 10',10,0,1000000,GETDATE(),DATEADD(day,90,GETDATE()),N'ĐANG HOẠT ĐỘNG');
   """
  );
  var dao=new dao.HoaDonDAO();
  var regular=sale("ATNORMAL","ATLOAD",1,"QD900005");long before=value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'");dao.thanhToanHoaDon(regular);result("cash_sale_commits",value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'")==before-1,"stock decremented once");
  var bad=sale("ATROLLBACK","ATLOAD",20000,"QD900005");long inv=value("SELECT COUNT(*) FROM HoaDon");before=value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'");boolean rejected=false;try{dao.thanhToanHoaDon(bad);}catch(SQLException e){rejected=true;}result("insufficient_stock_rolls_back",rejected&&value("SELECT COUNT(*) FROM HoaDon")==inv&&value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'")==before,"header/stock preserved");
  var split=sale("ATSPLIT","ATLOT1",5,"MISSING");rejected=false;int error=0;try{dao.thanhToanHoaDon(split);}catch(SQLException e){rejected=true;error=e.getErrorCode();}result("split_nonwhole_package_can_pay",!rejected,"SQL error="+error+" (matches GUI qdXuat=null branch)");result("split_failure_rolls_back",value("SELECT COUNT(*) FROM HoaDon WHERE maHoaDon='ATSPLIT'")==0&&value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOT1'")==5,"no partial header/stock");
  var expired=sale("ATEXPIREDSALE","ATEXPIRED",1,"QD900004");rejected=false;try{dao.thanhToanHoaDon(expired);}catch(Exception e){rejected=true;}result("DAO_blocks_expired_lot",rejected,"expired lot accepted="+!rejected+"; GUI has expiry filter but DAO does not");
  var points=sale("ATPOINTS","ATLOAD",10,"QD900005");points.setKhachHang(new dao.KhachHangDAO().findByMaKhachHang("KH900001"));points.setGiamGia(BigDecimal.valueOf(20000));dao.thanhToanHoaDonDayDu(points,null,null,null,null,20,0);result("redeem_points_transaction",value("SELECT diemTichLuy FROM KhachHang WHERE maKhachHang='KH900001'")==80,"100 - 20 + floor(80000/100000) = 80");
  var low=sale("ATLOWPOINTS","ATLOAD",1,"QD900005");low.setKhachHang(points.getKhachHang());before=value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'");rejected=false;try{dao.thanhToanHoaDonDayDu(low,null,null,null,null,999,0);}catch(SQLException e){rejected=true;}result("insufficient_points_rolls_back",rejected&&value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'")==before&&value("SELECT COUNT(*) FROM HoaDon WHERE maHoaDon='ATLOWPOINTS'")==0,"stock/header rolled back");
  var damaged=returns("ATDAMAGED",regular,1,false);before=value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'");dao.luuPhieuDoiTra(damaged);result("damaged_return_no_restock",value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'")==before,"return accepted without saleable stock increment");
  var r1=returns("ATDOUBLE1",regular,1,true);var r2=returns("ATDOUBLE2",regular,1,true);ExecutorService pool=Executors.newFixedThreadPool(8);CountDownLatch go=new CountDownLatch(1);List<Future<Boolean>> rr=new ArrayList<>();for(var p:List.of(r1,r2))rr.add(pool.submit(()->{go.await();try{return new dao.HoaDonDAO().luuPhieuDoiTra(p);}catch(Exception e){return false;}}));go.countDown();int accepted=0;for(var f:rr)if(f.get())accepted++;result("two_cashiers_cannot_return_same_item_twice",accepted<=1,"parallel returns accepted="+accepted+" for invoice originally containing 1 item; prior damaged return also exists");
  List<Future<Boolean>> races=new ArrayList<>();CountDownLatch raceGo=new CountDownLatch(1);for(int i=0;i<2;i++){final int k=i;HoaDon h=sale("ATRACE"+k,"ATRACE",8,"QD900003");races.add(pool.submit(()->{raceGo.await();try{return new dao.HoaDonDAO().thanhToanHoaDon(h);}catch(Exception e){return false;}}));}raceGo.countDown();accepted=0;for(var f:races)if(f.get())accepted++;result("two_cashiers_cannot_oversell_stock",accepted==1&&value("SELECT soLuongTon FROM LoHang WHERE soLo='ATRACE'")==2,"one successful 8-unit sale from stock 10; accepted="+accepted);
  var hotel=service("ATHOTEL",1,"DV008",600000);dao.thanhToanHoaDonDayDu(hotel,null,null,null,List.of("P011"),0,0);long days=value("SELECT soNgayLuuTru FROM LichSuLuuTru WHERE maHoaDon='ATHOTEL'");result("hotel_two_day_stay_persists_two_days",days==2,"storedDays="+days+" for 2-day room at 300000/day billed 600000");
  sql("UPDATE PhongLuuTru SET trangThai=N'ĐANG CÓ KHÁCH' WHERE maPhong='P011'");var multi=service("ATMULTIHOTEL",1,"DV008",600000);rejected=false;error=0;try{dao.thanhToanHoaDonDayDu(multi,null,null,null,List.of("P011","P012"),0,0);}catch(SQLException e){rejected=true;error=e.getErrorCode();}result("two_rooms_one_invoice_supported",!rejected,"SQL error="+error);result("multiroom_failure_rolls_back",value("SELECT COUNT(*) FROM HoaDon WHERE maHoaDon='ATMULTIHOTEL'")==0&&value("SELECT COUNT(*) FROM PhongLuuTru WHERE maPhong IN ('P011','P012') AND trangThai=N'ĐANG CÓ KHÁCH'")==2,"room states and header preserved");
  var spa=service("ATSPA",2,"DV004",0);dao.thanhToanHoaDonDayDu(spa,null,"PASS_AUDIT",null,null,0,0);long used=value("SELECT soBuoiDaDung FROM TheSpaPass WHERE maThe='PASS_AUDIT'");result("two_spa_units_deduct_two_sessions",used==2,"used="+used+" for service qty=2");
  before=value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'");var neg=sale("ATNEGATIVE","ATLOAD",-2,"QD900005");rejected=false;try{dao.thanhToanHoaDon(neg);}catch(Exception e){rejected=true;}result("DAO_rejects_negative_export",rejected,"accepted="+!rejected+" stockDelta="+(value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'")-before));
  var mismatch=sale("ATMISMATCH","ATLOAD",1,"QD900005");mismatch.setThanhTien(BigDecimal.ONE);rejected=false;try{dao.thanhToanHoaDon(mismatch);}catch(Exception e){rejected=true;}result("DAO_rejects_wrong_invoice_total",rejected,"accepted="+!rejected+" header=1 line=10000");
  long start=System.nanoTime();before=value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'");AtomicInteger completed=new AtomicInteger(),errors=new AtomicInteger();List<Future<?>> jobs=new ArrayList<>();for(int t=0;t<8;t++){final int worker=t;jobs.add(pool.submit(()->{for(int n=0;n<50;n++)try{var h=sale("ATLOAD"+worker+"X"+n,"ATLOAD",1,"QD900005");if(new dao.HoaDonDAO().thanhToanHoaDon(h))completed.incrementAndGet();else errors.incrementAndGet();}catch(Exception e){errors.incrementAndGet();}}));}for(var f:jobs)f.get();pool.shutdown();long delta=before-value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'");double seconds=(System.nanoTime()-start)/1e9;result("400_sales_8_workers",completed.get()==400&&errors.get()==0&&delta==400,"completed="+completed+" errors="+errors+" stockDelta="+delta+" seconds="+String.format(Locale.ROOT,"%.2f",seconds));
  long sessionsBefore=value("SELECT COUNT(*) FROM sys.dm_exec_sessions WHERE database_id=DB_ID()");var staff=new dao.NhanVienDAO().findByMaNhanVien("NV003");for(int i=0;i<20;i++)new dao.NhanVienDAO().updateNhanVien(staff);long leaked=value("SELECT COUNT(*) FROM sys.dm_exec_sessions WHERE database_id=DB_ID()")-sessionsBefore;result("employee_updates_close_connections",leaked<3,"openSessionDelta="+leaked+" after 20 updates");
  System.out.println("TOTAL pass="+pass+" fail="+fail+"; all writes confined to "+DB);
 }
}
