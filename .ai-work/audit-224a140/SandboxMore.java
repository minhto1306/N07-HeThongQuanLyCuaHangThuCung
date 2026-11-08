import java.sql.*;
import java.math.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import entity.*;
public class SandboxMore extends SandboxScenarios {
 public static void main(String[] args)throws Exception {
  System.setOut(new java.io.PrintStream("scenario-more.txt","UTF-8"));
  sql("INSERT INTO PhongLuuTru(maPhong,loaiPhong,donGiaNgay,trangThai,tenThuCung,thoiGianCheckIn,phiDichVuPhatSinh) VALUES('ATROOM1','VIP',300000,N'ĐANG CÓ KHÁCH',N'AUDIT PET',DATEADD(day,-2,GETDATE()),0),('ATROOM2','VIP',300000,N'ĐANG CÓ KHÁCH',N'AUDIT PET',DATEADD(day,-2,GETDATE()),0)");
  var dao=new dao.HoaDonDAO();var hotel=service("ATHOTEL",1,"DV008",600000);dao.thanhToanHoaDonDayDu(hotel,null,null,null,List.of("ATROOM1"),0,0);long days=value("SELECT soNgayLuuTru FROM LichSuLuuTru WHERE maHoaDon='ATHOTEL'");result("hotel_two_day_stay_persists_two_days",days==2,"storedDays="+days+" for 2-day room at 300000/day billed 600000");
  sql("UPDATE PhongLuuTru SET trangThai=N'ĐANG CÓ KHÁCH' WHERE maPhong='ATROOM1'");var multi=service("ATMULTIHOTEL",1,"DV008",600000);boolean rejected=false;int error=0;try{dao.thanhToanHoaDonDayDu(multi,null,null,null,List.of("ATROOM1","ATROOM2"),0,0);}catch(SQLException e){rejected=true;error=e.getErrorCode();}result("two_rooms_one_invoice_supported",!rejected,"SQL error="+error);result("multiroom_failure_rolls_back",value("SELECT COUNT(*) FROM HoaDon WHERE maHoaDon='ATMULTIHOTEL'")==0&&value("SELECT COUNT(*) FROM PhongLuuTru WHERE maPhong IN ('ATROOM1','ATROOM2') AND trangThai=N'ĐANG CÓ KHÁCH'")==2,"room states and header preserved");
  var spa=service("ATSPA",2,"DV004",0);dao.thanhToanHoaDonDayDu(spa,null,"PASS_AUDIT",null,null,0,0);long used=value("SELECT soBuoiDaDung FROM TheSpaPass WHERE maThe='PASS_AUDIT'");result("two_spa_units_deduct_two_sessions",used==2,"used="+used+" for service qty=2");
  long before=value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'");var neg=sale("ATNEGATIVE","ATLOAD",-2,"QD900005");rejected=false;try{dao.thanhToanHoaDon(neg);}catch(Exception e){rejected=true;}result("DAO_rejects_negative_export",rejected,"accepted="+!rejected+" stockDelta="+(value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'")-before));
  var mismatch=sale("ATMISMATCH","ATLOAD",1,"QD900005");mismatch.setThanhTien(BigDecimal.ONE);rejected=false;try{dao.thanhToanHoaDon(mismatch);}catch(Exception e){rejected=true;}result("DAO_rejects_wrong_invoice_total",rejected,"accepted="+!rejected+" header=1 line=10000");
  long start=System.nanoTime();before=value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'");AtomicInteger completed=new AtomicInteger(),errors=new AtomicInteger();ExecutorService pool=Executors.newFixedThreadPool(8);try{List<Future<?>> jobs=new ArrayList<>();for(int t=0;t<8;t++){final int worker=t;jobs.add(pool.submit(()->{for(int n=0;n<50;n++)try{var h=sale("ATLOAD"+worker+"X"+n,"ATLOAD",1,"QD900005");if(new dao.HoaDonDAO().thanhToanHoaDon(h))completed.incrementAndGet();else errors.incrementAndGet();}catch(Exception e){errors.incrementAndGet();}}));}for(var f:jobs)f.get();}finally{pool.shutdownNow();}long delta=before-value("SELECT soLuongTon FROM LoHang WHERE soLo='ATLOAD'");double seconds=(System.nanoTime()-start)/1e9;result("400_sales_8_workers",completed.get()==400&&errors.get()==0&&delta==400,"completed="+completed+" errors="+errors+" stockDelta="+delta+" seconds="+String.format(Locale.ROOT,"%.2f",seconds));
  long sessionsBefore=value("SELECT COUNT(*) FROM sys.dm_exec_sessions WHERE database_id=DB_ID()");var staff=new dao.NhanVienDAO().findByMaNhanVien("NV003");for(int i=0;i<20;i++)new dao.NhanVienDAO().updateNhanVien(staff);long leaked=value("SELECT COUNT(*) FROM sys.dm_exec_sessions WHERE database_id=DB_ID()")-sessionsBefore;result("employee_updates_close_connections",leaked<3,"openSessionDelta="+leaked+" after 20 updates");
  System.out.println("TOTAL pass="+pass+" fail="+fail+"; all writes confined to "+DB);
 }
}
