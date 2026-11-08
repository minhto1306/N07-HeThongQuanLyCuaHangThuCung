import java.sql.*;
public class AuthCheck {
 public static void main(String[] args)throws Exception {
  try(Connection c=utils.DBConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT DB_NAME()")){r.next();if(!"MioRepair_20261108".equals(r.getString(1)))throw new IllegalStateException("Wrong test DB");}
  var b=new bus.DangNhapBus();int fail=0;
  boolean[] checks={b.xacThucQuenMatKhau("0912345678","audit-wrong@example.invalid")==null,b.xacThucQuenMatKhau("0912345678","")==null,b.xacThucQuenMatKhau("0912345678","minh.thungan@miopet.vn")!=null,!b.datLaiMatKhau("0912345678","12345"),!b.doiMatKhau("NV003","123456","12345")};
  String[] names={"wrong_email_rejected","blank_email_rejected","registered_pair_accepted","short_reset_rejected","short_change_rejected"};
  for(int i=0;i<checks.length;i++){System.out.println((checks[i]?"PASS ":"FAIL ")+names[i]);if(!checks[i])fail++;}if(fail>0)throw new AssertionError("Auth failures="+fail);
 }
}
