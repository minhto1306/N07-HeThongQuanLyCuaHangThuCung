import java.nio.file.*;
import java.sql.*;
import java.util.*;
public class RepairSandbox {
 static final String DB="MioRepair_20261108";
 static int errors=0;
 static void execute(Connection c,String sql,String name)throws Exception {
  int i=0;for(String batch:sql.split("(?im)^\\s*GO\\s*(?:--[^\\r\\n]*)?$")){
   if(batch.isBlank())continue;i++;try(Statement s=c.createStatement()){s.setQueryTimeout(90);s.execute(batch);}
   catch(SQLException e){errors++;System.out.println(name+" batch="+i+" ERROR="+e.getErrorCode());if(!name.equals("base-schema"))throw e;}
  }
 }
 public static void main(String[] args)throws Exception {
  Path root=Path.of("").toAbsolutePath();Path env=root.resolve(".ai-work/repair-20261108/app");Files.createDirectories(env.resolve("config"));
  System.setOut(new java.io.PrintStream(root.resolve(".ai-work/repair-20261108/setup.txt").toFile(),"UTF-8"));
  Properties p=new Properties();try(var in=Files.newInputStream(root.resolve("config/database.properties"))){p.load(in);}
  Properties auth=new Properties();auth.setProperty("user",p.getProperty("db.user","").trim());auth.setProperty("password",p.getProperty("db.password",""));
  String url="jdbc:sqlserver://"+p.getProperty("db.host","localhost").trim()+":"+p.getProperty("db.port","1433").trim()+";encrypt=true;trustServerCertificate=true;loginTimeout=5;socketTimeout=120000;";
  try(Connection c=DriverManager.getConnection(url+"databaseName=master",auth)) {
   try(PreparedStatement s=c.prepareStatement("SELECT COUNT(*) FROM sys.databases WHERE name=?")){s.setString(1,DB);try(ResultSet r=s.executeQuery()){r.next();if(r.getInt(1)!=0)throw new IllegalStateException("Sandbox already exists; refusing overwrite");}}
   try(Statement s=c.createStatement()){s.execute("CREATE DATABASE ["+DB+"]");}c.setCatalog(DB);
   try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT DB_NAME()")){r.next();if(!DB.equals(r.getString(1)))throw new IllegalStateException("Wrong database");}
   String base=Files.readString(root.resolve("sql/HeThongQuanLyCuaHangThuCung.sql"));
   execute(c,base.substring(base.indexOf("CREATE TABLE TaiKhoan"),base.indexOf("-- PHẦN II:")),"base-schema");
   execute(c,Files.readString(root.resolve("sql/CapNhatRangBuocNghiepVu.sql")),"migration-sandbox-only");
   for(String b:base.split("(?im)^\\s*GO\\s*$"))if(b.matches("(?s).*CREATE (?:OR ALTER )?(?:FUNCTION|PROCEDURE).*"))execute(c,b,"module");
   String seed=Files.readString(root.resolve("sql/SeedDuLieuMauDongBo.sql"));
   seed=seed.replace("USE QuanLyCuaHangThuCung;","USE ["+DB+"];");
   if(seed.contains("USE QuanLyCuaHangThuCung"))throw new IllegalStateException("Unsafe seed target");
   execute(c,seed,"seed-sandbox-only");
   try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM sys.tables WHERE is_ms_shipped=0")){r.next();System.out.println("SANDBOX_READY name="+DB+" tables="+r.getInt(1)+" baseSchemaErrors="+errors);}
  }
  p.setProperty("db.name",DB);try(var out=Files.newOutputStream(env.resolve("config/database.properties"))){p.store(out,"Isolated synthetic audit DB; do not copy into production");}
  Files.writeString(env.resolve("AUDIT_ENVIRONMENT.txt"),"SYNTHETIC TEST ONLY\nDatabase="+DB+"\nEmail/payment configuration absent.\n");
 }
}

