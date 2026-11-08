import java.sql.*;
import java.nio.file.*;
import java.util.*;
public class ApplyRepairMigrations {
 public static void main(String[] args)throws Exception {
  if(args.length<1)throw new IllegalArgumentException("Explicit script paths required");
  Set<String> allowed=Set.of("20261108_DoiTraCoSo.sql","20261108_HotelSpa.sql","20261108_MaTuSinhSequence.sql");
  try(Connection c=utils.DBConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT DB_NAME()")) {
   r.next();if(!"MioRepair_20261108".equals(r.getString(1)))throw new IllegalStateException("Wrong synthetic DB");
   for(String arg:args){Path p=Path.of(arg);if(!allowed.contains(p.getFileName().toString()))throw new IllegalArgumentException("Unexpected script");
    String script=Files.readString(p);if(script.matches("(?is).*\\bUSE\\s+.*"))throw new IllegalStateException("Script switches target DB");
    for(int repeat=0;repeat<2;repeat++)for(String batch:script.split("(?im)^\\s*GO\\s*$"))if(!batch.isBlank())try(Statement q=c.createStatement()){q.setQueryTimeout(60);q.execute(batch);}
    System.out.println("PASS idempotent migration "+p.getFileName());
   }
  }
 }
}
