import dao.*;
import java.time.*;
import java.util.*;
import java.sql.*;
public class DaoReadProbe {
 interface Read { Object get() throws Exception; }
 static int pass=0,fail=0;
 static void check(String name,Read read){try{Object o=read.get();System.out.println("PASS "+name+(o instanceof Collection?" rows="+((Collection<?>)o).size():""));pass++;}catch(Exception e){System.out.println("FAIL "+name+" type="+e.getClass().getSimpleName()+(e instanceof SQLException?" code="+((SQLException)e).getErrorCode():""));fail++;}}
 public static void main(String[] args)throws Exception{
  System.setOut(new java.io.PrintStream(".ai-work/audit-224a140/dao-evidence.txt","UTF-8"));
  var b=new BaoCaoThongKeDAO();var start=LocalDate.of(2026,1,1).atStartOfDay();var end=LocalDate.of(2026,11,9).atStartOfDay();
  check("products",()->new SanPhamDAO().getAllSanPham());
  check("lots",()->new LoHangDAO().getAllLoHang());
  check("customers",()->new KhachHangDAO().getAllKhachHang());
  check("pet_profiles",()->new ThuCungDAO().getAllHoSoThuCung());
  check("employees",()->new NhanVienDAO().getAllNhanVien());
  check("appointments",()->new LichHenDAO().getAllLichHen());
  check("services",()->new DichVuDAO().getAllDichVu());
  check("invoices",()->new HoaDonDAO().getAllHoaDon());
  check("shifts",()->new PhanCaDAO().getAllPhanCong());
  check("handover",()->new BangGiaoCaDAO().getAllBangGiaoCa());
  check("hotel_rooms",()->new PhongLuuTruDAO().getAllPhong());
  check("spa_cards",()->new TheSpaPassDAO().getAllTheSpaPass());
  check("report_revenue",()->b.getDoanhThu(start,end));
  check("report_top_selling",()->b.getTopBanChay(start,end));
  check("report_staff",()->b.getHieuSuatNhanSu(start,end));
  check("report_customers",()->b.getKhachHang());
  check("report_hotel",()->b.getHotelSummary());
  check("report_returns",()->b.getDoiTraSummary(start,end));
  check("report_fefo",()->b.getLoHangFefo());
  check("report_inventory",()->b.getXuatNhapTon(start,end));
  System.out.println("TOTAL pass="+pass+" fail="+fail);
 }
}

