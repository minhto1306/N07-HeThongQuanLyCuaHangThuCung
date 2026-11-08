import java.sql.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
public class LiveAudit {
 static void run(Connection c,String id,String sql) {
  try(Statement s=c.createStatement()){s.setQueryTimeout(15);try(ResultSet r=s.executeQuery(sql)){
   while(r.next()){StringJoiner j=new StringJoiner("|");j.add(id);for(int i=1;i<=r.getMetaData().getColumnCount();i++)j.add(String.valueOf(r.getObject(i)));System.out.println(j);}
  }}catch(SQLException e){System.out.println(id+"|SQL_ERROR|"+e.getErrorCode()+"|"+e.getSQLState());}
 }
 public static void main(String[] args)throws Exception {
  System.setOut(new java.io.PrintStream(".ai-work/audit-224a140/live-checks.txt","UTF-8"));
  Properties p=new Properties();try(var in=Files.newInputStream(Path.of("config/database.properties"))){p.load(in);}
  Properties auth=new Properties();auth.setProperty("user",p.getProperty("db.user","").trim());auth.setProperty("password",p.getProperty("db.password",""));
  String url="jdbc:sqlserver://"+p.getProperty("db.host","localhost").trim()+":"+p.getProperty("db.port","1433").trim()+";databaseName="+p.getProperty("db.name","QuanLyCuaHangThuCung").trim()+";encrypt=true;trustServerCertificate=true;loginTimeout=5;socketTimeout=20000";
  try(Connection c=DriverManager.getConnection(url,auth)) {
   System.out.println("CONNECTED");
   String text=Files.readString(Path.of("sql/ValidateDuLieuMau.sql"));
   // Only the reviewed per-rule BEGIN TRY blocks run. Session temp table only; no migration/seed/cleanup.
   try(Statement s=c.createStatement()){s.execute("CREATE TABLE #KetQuaKiemTra (MaKiemTra VARCHAR(10) PRIMARY KEY,QuyTac NVARCHAR(255),SoDongLoi INT,TrangThai VARCHAR(20),ChiTietLoi NVARCHAR(MAX))");}
   Matcher m=Pattern.compile("(?s)-- (R\\d{2}):.*?(BEGIN TRY.*?END CATCH;)").matcher(text);
   while(m.find())try(Statement s=c.createStatement()){
    s.setQueryTimeout(15);s.execute("SET NOCOUNT ON; DECLARE @ErrCount INT=0; DECLARE @Details NVARCHAR(MAX)=N''; "+m.group(2));
   }catch(SQLException e){System.out.println(m.group(1)+"|NOT_VERIFIABLE|SQL_ERROR="+e.getErrorCode());}
   run(c,"RULE","SELECT MaKiemTra,TrangThai,SoDongLoi FROM #KetQuaKiemTra ORDER BY MaKiemTra");
   Map<String,String> q=new LinkedHashMap<>();
   q.put("DB_TABLES","SELECT COUNT(*) FROM sys.tables WHERE is_ms_shipped=0");
   q.put("LIVE_COUNTS","SELECT (SELECT COUNT(*) FROM SanPham) products,(SELECT COUNT(*) FROM HoaDon) invoices,(SELECT COUNT(*) FROM LoHang) lots,(SELECT COUNT(*) FROM KhachHang) customers");
   q.put("INVOICE_AMOUNT_MISMATCH","SELECT COUNT(*) FROM HoaDon h OUTER APPLY(SELECT SUM(thanhTien) amount FROM ChiTietHoaDon x WHERE x.maHoaDon=h.maHoaDon) x WHERE h.trangThaiHoaDon=N'Đã thanh toán' AND ABS(h.thanhTien-(COALESCE(x.amount,0)-COALESCE(h.giamGia,0)))>1");
   q.put("HEADER_LINE_RETURNS_MISMATCH","SELECT COUNT(*) FROM PhieuDoiTra p OUTER APPLY(SELECT SUM(tienHoanLai) amount FROM ChiTietDoiTra x WHERE x.maPhieuDoiTra=p.maPhieuDoiTra) x WHERE ABS(p.tongTienHoan-COALESCE(x.amount,0))>1");
   q.put("HANDOVER_CASH_FORMULA","SELECT COUNT(*) FROM BangGiaoCa WHERE tienCuoiCaHeThong<>tienDauCa+tienThuTienMat-tienHoanTra");
   q.put("HANDOVER_APPROVER_ROLE","SELECT COUNT(*) FROM BangGiaoCa g LEFT JOIN NhanVien n ON n.maNhanVien=g.maQuanLyDuyet LEFT JOIN TaiKhoan t ON t.maTaiKhoan=n.maTaiKhoan WHERE g.trangThai='DA_DUYET' AND (t.vaiTro<>'NVQL' OR t.maTaiKhoan IS NULL OR g.maQuanLyDuyet=g.maNhanVien)");
   q.put("SERVICE_STAFF_WRONG_ROLE","SELECT COUNT(*) FROM ChiTietDichVu d JOIN NhanVien n ON n.maNhanVien=d.maKtvThucHien JOIN TaiKhoan t ON t.maTaiKhoan=n.maTaiKhoan WHERE t.vaiTro NOT IN ('NVCS','NVQL')");
   q.put("PETS_WITHOUT_OWNER","SELECT COUNT(*) FROM ThuCung t WHERE NOT EXISTS(SELECT 1 FROM HoSoThuCung h WHERE h.maThuCung=t.maThuCung)");
   q.put("BASE_UNIT_NAME_VS_ID","SELECT COUNT(*) FROM SanPham p WHERE EXISTS(SELECT 1 FROM DonViTinh d WHERE d.tenDonVi=p.donViCoSo) AND NOT EXISTS(SELECT 1 FROM DonViTinh d WHERE d.maDonVi=p.donViCoSo)");
   q.put("SPA_HISTORY_COUNTER_MISMATCH","SELECT COUNT(*) FROM TheSpaPass t OUTER APPLY(SELECT COUNT(*) uses FROM LichSuSuDungSpaPass h WHERE h.maThe=t.maThe) x WHERE t.soBuoiDaDung<>x.uses");
   q.put("RETURNS_EXCEED_SALES","SELECT COUNT(*) FROM (SELECT p.maHoaDonGoc,d.maSanPham,SUM(d.soLuongDoiTra) returned FROM PhieuDoiTra p JOIN ChiTietDoiTra d ON d.maPhieuDoiTra=p.maPhieuDoiTra GROUP BY p.maHoaDonGoc,d.maSanPham) r OUTER APPLY(SELECT SUM(c.soLuong) sold FROM ChiTietHoaDon c WHERE c.maHoaDon=r.maHoaDonGoc AND c.maSanPham=r.maSanPham) s WHERE r.returned>COALESCE(s.sold,0)");
   q.put("REAL_QUYDOI_MISMATCH","SELECT COUNT(*) FROM ChiTietHoaDon ct OUTER APPLY(SELECT SUM(x.soLuongXuatCoSo) amount,COUNT(DISTINCT x.maQuyDoi) kinds,MIN(q.tyLeQuyDoi) ratio FROM ChiTietXuatLo x JOIN QuyDoiDonVi q ON q.maQuyDoi=x.maQuyDoi WHERE x.maChiTiet=ct.maChiTiet) x WHERE x.kinds=1 AND x.amount<>ct.soLuong*x.ratio");
   q.put("RETURN_NOT_RESTOCKED","SELECT COUNT(*) FROM ChiTietDoiTra WHERE soLuongCoSoHoan=0");
   for(String f:new String[]{"fn_SinhMaLichHen()","fn_SinhMaPhieuDoiTra()","fn_SinhMaHoaDon(N'NV001')"})
    q.put("UDF_"+f,"SELECT CASE WHEN dbo."+f+" IS NULL THEN 0 ELSE 1 END");
   for(var e:q.entrySet())run(c,e.getKey(),e.getValue());
   String phone=null;try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT TOP(1) t.soDienThoai FROM TaiKhoan t JOIN NhanVien n ON n.maTaiKhoan=t.maTaiKhoan WHERE t.trangThaiHoatDong=1")){if(r.next())phone=r.getString(1);}
   if(phone!=null){boolean wronglyAccepted=new bus.DangNhapBus().xacThucQuenMatKhau(phone,"audit-unregistered-address@example.invalid")!=null;System.out.println("RESET_WRONG_EMAIL_ACCEPTED|"+wronglyAccepted);}
  }
 }
}
