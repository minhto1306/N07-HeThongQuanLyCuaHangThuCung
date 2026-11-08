import java.lang.reflect.*;
import java.sql.*;
import java.math.*;
import java.time.*;
import java.util.*;
import entity.*;
public class ContractProbe {
 static int pass=0,fail=0;
 static void check(String n,boolean value){System.out.println((value?"PASS ":"FAIL ")+n);if(value)pass++;else fail++;}
 static class Recorder {
  Map<String,List<Map<Integer,Object>>> writes=new LinkedHashMap<>();
  Object value(Class<?> t){if(t==boolean.class)return false;if(t==int.class)return 0;if(t==long.class)return 0L;return null;}
  Connection conn(){return (Connection)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Connection.class},(p,m,a)->{
   if(m.getName().equals("prepareStatement")){String sql=(String)a[0];Map<Integer,Object> params=new HashMap<>();return Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{PreparedStatement.class},(pp,mm,aa)->{
    if(mm.getName().startsWith("set")&&aa!=null&&aa[0] instanceof Integer){params.put((Integer)aa[0],mm.getName().equals("setNull")?null:aa[1]);return null;}
    if(mm.getName().equals("executeUpdate")){writes.computeIfAbsent(sql,k->new ArrayList<>()).add(new HashMap<>(params));return 1;}
    return value(mm.getReturnType());
   });}return value(m.getReturnType());
  });}
  Object inserted(String sqlPart,int param){return writes.entrySet().stream().filter(e->e.getKey().contains(sqlPart)).findFirst().orElseThrow().getValue().get(0).get(param);}
 }
 static Recorder write(HoaDon h)throws Exception{Recorder r=new Recorder();Method m=dao.HoaDonDAO.class.getDeclaredMethod("luuHoaDonTrongTransaction",Connection.class,HoaDon.class);m.setAccessible(true);m.invoke(new dao.HoaDonDAO(),r.conn(),h);return r;}
 static HoaDon invoice(int qty,QuyDoiDonVi qd,BigDecimal header){
  HoaDon h=new HoaDon();h.setMaHoaDon("AUDIT_ONLY");h.setNgayLap(LocalDateTime.of(2026,11,8,12,0));h.setThanhTien(header);h.setGiamGia(BigDecimal.ZERO);NhanVien n=new NhanVien();n.setMaNhanVien("AUDIT_STAFF");h.setNhanVien(n);
  SanPham sp=new SanPham();sp.setMaSanPham("AUDIT_PRODUCT");LoHang l=new LoHang();l.setSoLo("AUDIT_LOT");l.setSanPham(sp);l.setHanSuDung(LocalDate.of(2020,1,1));
  ChiTietXuatLo x=new ChiTietXuatLo("AUDIT_CT",h,qty,new BigDecimal("10000"),l,qd);x.setSoLuongXuatCoSo(qty);h.addChiTiet(x);return h;
 }
 public static void main(String[] args)throws Exception{
  System.setOut(new java.io.PrintStream(".ai-work/audit-224a140/contract-evidence.txt","UTF-8"));
  utils.UserSession.clearSession();
  for(String card:new String[]{"CARD_POS","CARD_NHAN_VIEN","CARD_KHO","CARD_PHAN_CA","CARD_GIAO_CA","CARD_TONG_QUAN","CARD_DOI_TRA","CARD_PET_HOTEL"})check("guest_denied_"+card,!utils.UserSession.canAccess(card));
  check("csv_quote",utils.CsvHelper.cell("a\"b").equals("\"a\"\"b\""));
  check("csv_formula",utils.CsvHelper.cell("=1+1").equals("\"'=1+1\""));
  Recorder split=write(invoice(5,null,new BigDecimal("50000")));
  check("split_lot_payload_satisfies_required_maQuyDoi",split.inserted("INSERT INTO ChiTietXuatLo",3)!=null);
  QuyDoiDonVi q=new QuyDoiDonVi();q.setMaQuyDoi("AUDIT_QD");q.setTyLeQuyDoi(1);
  Recorder neg=write(invoice(-2,q,new BigDecimal("-20000")));
  check("DAO_rejects_negative_export_payload",((Integer)neg.inserted("INSERT INTO ChiTietXuatLo",4))>0);
  HoaDon mismatched=invoice(1,q,new BigDecimal("10000"));mismatched.setThanhTien(new BigDecimal("1"));Recorder mismatch=write(mismatched);
  BigDecimal head=(BigDecimal)mismatch.inserted("INSERT INTO HoaDon",7);BigDecimal line=(BigDecimal)mismatch.inserted("INSERT INTO ChiTietHoaDon",8);
  check("DAO_rejects_inconsistent_invoice_total",head.compareTo(line)==0);
  check("DAO_update_guards_expiry",mismatch.writes.keySet().stream().filter(s->s.startsWith("UPDATE LoHang SET soLuongTon = soLuongTon -")).allMatch(s->s.contains("hanSuDung")));
  System.out.println("TOTAL pass="+pass+" fail="+fail+"; JDBC recorder only, no real writes");
 }
}
