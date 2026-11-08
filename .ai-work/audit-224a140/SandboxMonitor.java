import java.sql.*;
public class SandboxMonitor {
 public static void main(String[] args)throws Exception{
  try(Connection c=SandboxScenarios.connection();Statement s=c.createStatement()){
   s.setQueryTimeout(8);
   try(ResultSet r=s.executeQuery("SELECT session_id,status,wait_type,blocking_session_id,total_elapsed_time,LEFT(t.text,180) FROM sys.dm_exec_requests r OUTER APPLY sys.dm_exec_sql_text(r.sql_handle) t WHERE database_id=DB_ID()")){while(r.next())System.out.println(r.getInt(1)+"|"+r.getString(2)+"|"+r.getString(3)+"|block="+r.getInt(4)+"|ms="+r.getLong(5)+"|"+r.getString(6));}
   try(ResultSet r=s.executeQuery("SELECT g.session_id,g.requested_memory_kb,g.required_memory_kb,g.granted_memory_kb,g.wait_time_ms FROM sys.dm_exec_query_memory_grants g JOIN sys.dm_exec_requests r ON r.session_id=g.session_id WHERE r.database_id=DB_ID()")){while(r.next())System.out.println("GRANT session="+r.getInt(1)+" requestedKB="+r.getLong(2)+" requiredKB="+r.getLong(3)+" grantedKB="+r.getLong(4)+" waitMS="+r.getLong(5));}
   try(ResultSet r=s.executeQuery("SELECT name,value_in_use FROM sys.configurations WHERE name IN ('max server memory (MB)','max degree of parallelism')")){while(r.next())System.out.println("SERVER_READ_ONLY "+r.getString(1)+"="+r.getString(2));}
  }
 }
}
