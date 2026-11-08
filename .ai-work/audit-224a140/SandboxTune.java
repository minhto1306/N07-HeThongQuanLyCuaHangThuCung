import java.sql.*;
public class SandboxTune {
 public static void main(String[] args)throws Exception{
  try(Connection c=SandboxScenarios.connection();Statement s=c.createStatement()){
   s.execute("ALTER DATABASE SCOPED CONFIGURATION SET MAXDOP = 1");
   System.out.println("Sandbox DB only: MAXDOP=1; global SQL settings unchanged");
  }
 }
}
