import java.sql.*;
import java.nio.file.*;
public class CashierEdgeCases extends SandboxScenarios {
 public static void main(String[] args)throws Exception {
  System.setOut(new java.io.PrintStream("cashier-edge-results.txt","UTF-8"));
  try(var c=connection()){}
  utils.UserSession.initSession(new dao.NhanVienDAO().findByMaNhanVien("NV003"));
  long pointsBefore=value("SELECT diemTichLuy FROM KhachHang WHERE maKhachHang='KH900001'");
  bus.BanHangBus.luuHoacCapNhatKhachHang("0900900001","AUDIT fake customer",100);
  long pointsAfter=value("SELECT diemTichLuy FROM KhachHang WHERE maKhachHang='KH900001'");
  result("cashier_profile_save_cannot_overwrite_points",pointsAfter==pointsBefore,"role="+utils.UserSession.getRoleCode()+" before="+pointsBefore+" after="+pointsAfter);
  var sp=new dao.SanPhamDAO().findByMaVachHoacMaSP("AUDITBAR4999");result("barcode_lookup_large_catalog",sp!=null&&"SP104999".equals(sp.getMaSanPham()),"barcode resolved expected product");
  String javaExe=Path.of(System.getProperty("java.home"),"bin","java.exe").toString(),cp=System.getProperty("java.class.path");
  var p1=new ProcessBuilder(javaExe,"-cp",cp,"CodeProbe").start();var p2=new ProcessBuilder(javaExe,"-cp",cp,"CodeProbe").start();
  String id1=new String(p1.getInputStream().readAllBytes()).trim(),id2=new String(p2.getInputStream().readAllBytes()).trim();p1.waitFor();p2.waitFor();result("separate_cashier_processes_get_unique_invoice_codes",p1.exitValue()==0&&p2.exitValue()==0&&!id1.equals(id2),"both generated same ID="+id1.equals(id2));
  var rows=new dao.BaoCaoThongKeDAO().getTopBanChay(java.time.LocalDate.now().minusDays(60).atStartOfDay(),java.time.LocalDate.now().plusDays(1).atStartOfDay());
  // Test live quantities: baseline seed products have both base and pack unit rows.
  long real=value("SELECT SUM(x.soLuongXuatCoSo) FROM ChiTietXuatLo x JOIN ChiTietHoaDon c ON c.maChiTiet=x.maChiTiet JOIN HoaDon h ON h.maHoaDon=c.maHoaDon WHERE c.maSanPham='SP001' AND h.trangThaiHoaDon=N'Đã thanh toán'");
  long shown=rows.stream().filter(x->"SP001".equals(x.ma)).mapToLong(x->x.soLuong).sum();result("top_sales_base_quantity_matches_exports",shown==real,"topQty="+shown+" exportedBaseQty="+real);
  long sqlTotal=value("SELECT thanhTien FROM HoaDon WHERE maHoaDon='ATMISMATCH'");long entityTotal=new dao.HoaDonDAO().findById("ATMISMATCH").getThanhTien().longValue();result("invoice_read_preserves_stored_total",sqlTotal==entityTotal,"sql="+sqlTotal+" entity="+entityTotal+"; deliberately mismatched test invoice");
  System.out.println("TOTAL pass="+pass+" fail="+fail);
 }
}
