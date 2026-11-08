import java.sql.*;
import java.util.*;
import java.nio.file.*;
public class SchemaProbe {
 public static void main(String[] args) throws Exception {
  System.setOut(new java.io.PrintStream(Files.newOutputStream(Path.of(".ai-work/audit-224a140/sql-evidence.txt")),true,"UTF-8"));
  Properties p=new Properties();
  try(var in=Files.newInputStream(Path.of("config/database.properties"))){p.load(in);}
  String host=p.getProperty("db.host","localhost").trim();
  String port=p.getProperty("db.port","1433").trim();
  String instance=p.getProperty("db.instance","SQLEXPRESS").trim();
  String db=p.getProperty("db.name","QuanLyCuaHangThuCung").trim();
  Properties auth=new Properties();
  auth.setProperty("user",p.getProperty("db.user","").trim());
  auth.setProperty("password",p.getProperty("db.password",""));
  DriverManager.setLoginTimeout(5);
  for(int mode=0;mode<2;mode++) {
   String url="jdbc:sqlserver://"+host+(mode==0?":"+port:";instanceName="+instance)+";databaseName="+db+";encrypt=true;trustServerCertificate=true;loginTimeout=5;socketTimeout=6000";
   try(Connection c=DriverManager.getConnection(url,auth)) {
    System.out.println("CONNECTED route="+mode);
    String[] queries={
     "SELECT TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME,DATA_TYPE,CHARACTER_MAXIMUM_LENGTH,IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS ORDER BY TABLE_SCHEMA,TABLE_NAME,ORDINAL_POSITION",
     "SELECT o.name,o.type_desc FROM sys.objects o WHERE o.is_ms_shipped=0 AND o.type IN ('FN','P','TR','UQ','C','F') ORDER BY o.type,o.name",
     "SELECT OBJECT_NAME(object_id),name,filter_definition FROM sys.indexes WHERE is_unique=1 AND object_id IN (SELECT object_id FROM sys.tables) ORDER BY object_id,name"
    };
    for(int i=0;i<queries.length;i++)try(Statement s=c.createStatement()) {
     s.setQueryTimeout(10);
     try(ResultSet r=s.executeQuery(queries[i])){System.out.println("SECTION="+i);while(r.next()){StringJoiner row=new StringJoiner("|");for(int col=1;col<=r.getMetaData().getColumnCount();col++)row.add(String.valueOf(r.getObject(col)));System.out.println(row);}}
    }
    Map<String,String> checks=new LinkedHashMap<>();
    checks.put("mapping_shift","SELECT TOP(0) gioBatDauCa,ngayGiaoCa FROM dbo.BangGiaoCa");
    checks.put("mapping_returns","SELECT TOP(0) soLo,soLuongCoSoHoan FROM dbo.ChiTietDoiTra");
    checks.put("mapping_hotel","SELECT TOP(0) maHoaDon,trangThaiThanhToan FROM dbo.LichSuLuuTru");
    checks.put("mapping_spa_activation","SELECT TOP(0) maHoaDonKichHoat FROM dbo.TheSpaPass");
    checks.put("mapping_spa_usage","SELECT TOP(0) maThe,maHoaDon FROM dbo.LichSuSuDungSpaPass");
    checks.put("expired_lots_with_stock","SELECT COUNT(*) FROM dbo.LoHang WHERE hanSuDung<CAST(GETDATE() AS date) AND soLuongTon>0");
    checks.put("negative_stock","SELECT COUNT(*) FROM dbo.LoHang WHERE soLuongTon<0");
    checks.put("negative_points","SELECT COUNT(*) FROM dbo.KhachHang WHERE diemTichLuy<0");
    checks.put("spa_overused","SELECT COUNT(*) FROM dbo.TheSpaPass WHERE soBuoiDaDung>tongSoBuoi");
    checks.put("duplicate_shift_groups","SELECT COUNT(*) FROM (SELECT maNhanVien,maCa,CAST(gioKetThucThucTe AS date) d FROM dbo.BangGiaoCa GROUP BY maNhanVien,maCa,CAST(gioKetThucThucTe AS date) HAVING COUNT(*)>1) x");
    checks.put("split_unit_inconsistency","SELECT COUNT(*) FROM dbo.ChiTietHoaDon c JOIN dbo.ChiTietXuatLo x ON x.maChiTiet=c.maChiTiet JOIN dbo.QuyDoiDonVi q ON q.maQuyDoi=x.maQuyDoi WHERE c.soLuong*q.tyLeQuyDoi<>x.soLuongXuatCoSo");
    for(var check:checks.entrySet())try(Statement s=c.createStatement()) {
     s.setQueryTimeout(10);
     try(ResultSet r=s.executeQuery(check.getValue())){System.out.print("CHECK "+check.getKey()+" OK");if(r.next())System.out.print(" count="+r.getLong(1));System.out.println();}
    }catch(SQLException e){System.out.println("CHECK "+check.getKey()+" FAIL state="+e.getSQLState()+" code="+e.getErrorCode());}
    try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT o.name,m.definition FROM sys.sql_modules m JOIN sys.objects o ON o.object_id=m.object_id WHERE o.type='FN' OR o.name='sp_DangNhap'")) {
     while(r.next())System.out.println("MODULE "+r.getString(1)+"\n"+r.getString(2));
    }
    String script=Files.readString(Path.of("sql/HeThongQuanLyCuaHangThuCung.sql"));
    var matcher=java.util.regex.Pattern.compile("(?ms)^CREATE TABLE (\\w+) \\((.*?)^\\);").matcher(script);
    DatabaseMetaData meta=c.getMetaData();int tables=0,columns=0,missing=0;
    while(matcher.find()) {
     tables++;String table=matcher.group(1);Set<String> live=new HashSet<>();
     try(ResultSet r=meta.getColumns(null,"dbo",table,null)){while(r.next())live.add(r.getString("COLUMN_NAME").toLowerCase());}
     var cm=java.util.regex.Pattern.compile("(?m)^    (\\w+) (NVARCHAR|INT|BIGINT|BIT|DATE|DATETIME2|DECIMAL|TIME|AS)\\b").matcher(matcher.group(2));
     if(live.isEmpty())System.out.println("MISSING_TABLE "+table);
     while(cm.find()){columns++;if(!live.contains(cm.group(1).toLowerCase())){missing++;System.out.println("MISSING_COLUMN "+table+"."+cm.group(1));}}
    }
    System.out.println("SCHEMA_COMPARE tables="+tables+" columns="+columns+" missingColumns="+missing);
    return;
   }catch(SQLException e){System.out.println("CONNECT_OR_QUERY_FAILED route="+mode+" state="+e.getSQLState()+" code="+e.getErrorCode());}
  }
 }
}

