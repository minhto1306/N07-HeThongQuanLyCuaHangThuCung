import java.sql.*;
import java.nio.file.*;
import java.time.*;
public class BulkAndBenchmark {
 interface Query{Object call()throws Exception;}
 static void timed(String name,Query q)throws Exception{long st=System.nanoTime();Object r=q.call();System.out.println(name+" ms="+((System.nanoTime()-st)/1_000_000)+(r instanceof java.util.Collection?" rows="+((java.util.Collection<?>)r).size():""));}
 public static void main(String[] args)throws Exception{
  System.setOut(new java.io.PrintStream("bulk-benchmark.txt","UTF-8"));
  try(Connection c=utils.DBConnection.getConnection();Statement s=c.createStatement()){
   try(ResultSet r=s.executeQuery("SELECT DB_NAME()")){r.next();if(!"MioAudit_224a140_20261108".equals(r.getString(1)))throw new IllegalStateException("Wrong DB");}
   if(args.length>0&&args[0].equals("seed")){s.setQueryTimeout(120);long st=System.nanoTime();s.execute(Files.readString(Path.of("../bulk-synthetic.sql")));System.out.println("BULK_INSERT ms="+((System.nanoTime()-st)/1_000_000));}
   try(ResultSet r=s.executeQuery("SELECT (SELECT COUNT(*) FROM SanPham),(SELECT COUNT(*) FROM LoHang),(SELECT COUNT(*) FROM KhachHang),(SELECT COUNT(*) FROM HoaDon)")){r.next();System.out.println("COUNTS products="+r.getInt(1)+" lots="+r.getInt(2)+" customers="+r.getInt(3)+" invoices="+r.getInt(4));}
  }
  timed("products",()->new dao.SanPhamDAO().getAllSanPham());
  timed("lots",()->new dao.LoHangDAO().getAllLoHang());
  timed("customers",()->new dao.KhachHangDAO().getAllKhachHang());
  timed("invoice_history",()->new dao.HoaDonDAO().getAllHoaDon());
  var b=new dao.BaoCaoThongKeDAO();var tu=LocalDate.now().minusDays(60).atStartOfDay();var den=LocalDate.now().plusDays(1).atStartOfDay();
  timed("report_revenue",()->b.getDoanhThu(tu,den));
  timed("report_top",()->b.getTopBanChay(tu,den));
  timed("report_inventory",()->b.getXuatNhapTon(tu,den));
  new bus.DangNhapBus().xuLyDangNhap("0912345678","123456");
  timed("POS_static_history_authenticated",()->{Class.forName("gui.BanHangPOSPanel");return null;});
 }
}
