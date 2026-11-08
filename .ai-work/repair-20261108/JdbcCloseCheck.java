import java.sql.*;
public class JdbcCloseCheck {
 static long sessions() throws Exception {
  try(Connection c=utils.DBConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT DB_NAME(),(SELECT COUNT(*) FROM sys.dm_exec_sessions WHERE database_id=DB_ID())")) {
   r.next();if(!"MioRepair_20261108".equals(r.getString(1)))throw new IllegalStateException("Wrong test DB");return r.getLong(2);
  }
 }
 public static void main(String[] args)throws Exception {
  long before=sessions();var d=new dao.NhanVienDAO();var n=d.findByMaNhanVien("NV003");
  for(int i=0;i<20;i++)if(!d.updateNhanVien(n))throw new AssertionError("Update failed");
  long delta=sessions()-before;
  System.out.println((delta<=1?"PASS ":"FAIL ")+"20_employee_updates_session_delta="+delta);
  if(delta>1)throw new AssertionError("Leaked connections");
 }
}
