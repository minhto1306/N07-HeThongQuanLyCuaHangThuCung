import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
public class IdReservationCheck {
 static Connection connection()throws Exception{Connection c=utils.DBConnection.getConnection();try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT DB_NAME()")){r.next();if(!"MioRepair_20261108".equals(r.getString(1))){c.close();throw new IllegalStateException("Wrong DB");}}return c;}
 public static void main(String[] args)throws Exception{
  connection().close();
  if(args.length>0){for(int i=0;i<10;i++)System.out.println(utils.MaTuSinhHelper.sinhMaHoaDon("NV003"));return;}
  String java=System.getProperty("java.home")+"/bin/java.exe",cp=System.getProperty("java.class.path");List<Process> children=new ArrayList<>();Set<String> ids=new HashSet<>();int count=0;
  try{
   for(int k=0;k<8;k++)children.add(new ProcessBuilder(java,"-cp",cp,"IdReservationCheck","child").redirectError(ProcessBuilder.Redirect.INHERIT).start());
   for(Process p:children){if(!p.waitFor(90,TimeUnit.SECONDS))throw new AssertionError("ID child timeout");if(p.exitValue()!=0)throw new AssertionError("ID child failed");for(String line:p.inputReader().lines().toList()){if(!line.startsWith("HD"))throw new AssertionError("Unexpected child output");count++;ids.add(line);}}
  }finally{for(Process p:children)if(p.isAlive())p.destroyForcibly();}
  System.out.println((count==80&&ids.size()==80?"PASS ":"FAIL ")+"cross_process_IDs generated="+count+" unique="+ids.size()+" JVMs=8");
  if(count!=80||ids.size()!=80)throw new AssertionError("Duplicate reservation");
  String prefix="PASS-0900999999-";
  try(Connection c=connection();PreparedStatement s=c.prepareStatement("IF NOT EXISTS(SELECT 1 FROM MaTuSinhSequence WHERE prefix=?) INSERT INTO MaTuSinhSequence(prefix,giaTri) VALUES(?,2147483647)")){s.setString(1,prefix);s.setString(2,prefix);s.executeUpdate();}
  boolean rejected=false;try{utils.MaTuSinhHelper.sinhMaTheSpaPass("0900999999");}catch(IllegalStateException expected){rejected=true;}
  long stored;try(Connection c=connection();PreparedStatement s=c.prepareStatement("SELECT giaTri FROM MaTuSinhSequence WHERE prefix=?")){s.setString(1,prefix);try(ResultSet r=s.executeQuery()){r.next();stored=r.getLong(1);}}
  System.out.println((rejected&&stored==2147483647L?"PASS ":"FAIL ")+"ID_overflow_rejected_without_mutation");if(!rejected||stored!=2147483647L)throw new AssertionError("Overflow reservation changed");
 }
}
