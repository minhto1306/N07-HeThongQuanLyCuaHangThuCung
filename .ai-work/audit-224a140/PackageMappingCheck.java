import java.math.*;
import java.time.*;
public class PackageMappingCheck extends SandboxScenarios {
 public static void main(String[] args)throws Exception{
  System.setOut(new java.io.PrintStream("package-mapping-results.txt","UTF-8"));
  var h=sale("ATPACK","ATLOT2",1,"QD900002");h.getDsChiTiet().get(0).setDonGiaLucBan(new BigDecimal("100000"));((entity.ChiTietXuatLo)h.getDsChiTiet().get(0)).setSoLuongXuatCoSo(10);h.setThanhTien(new BigDecimal("100000"));new dao.HoaDonDAO().thanhToanHoaDon(h);
  var dao=new dao.BaoCaoThongKeDAO();var tu=LocalDate.now().minusDays(60).atStartOfDay();var den=LocalDate.now().plusDays(1).atStartOfDay();
  long real=value("SELECT SUM(x.soLuongXuatCoSo) FROM ChiTietXuatLo x JOIN ChiTietHoaDon c ON c.maChiTiet=x.maChiTiet WHERE c.maSanPham='SP900001'");
  long shown=dao.getTopBanChay(tu,den).stream().filter(x->"SP900001".equals(x.ma)).mapToLong(x->x.soLuong).sum();result("top_base_units_for_one_10_unit_pack",shown==real,"shownBaseQty="+shown+" exportedBaseQty="+real);
  long restocked=value("SELECT SUM(d.soLuongCoSoHoan) FROM ChiTietDoiTra d WHERE maSanPham='SP900004'");
  long xnt=dao.getXuatNhapTon(tu,den).stream().filter(x->"SP900004".equals(x.maSanPham)).mapToLong(x->x.hoanTraTrongKy).sum();result("XNT_counts_only_saleable_restock",xnt==restocked,"reportReturn="+xnt+" realRestock="+restocked+" (damaged return must not add stock)");
  System.out.println("TOTAL pass="+pass+" fail="+fail);
 }
}
