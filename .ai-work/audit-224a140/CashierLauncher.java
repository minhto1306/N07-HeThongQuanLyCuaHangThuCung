import java.sql.*;
public class CashierLauncher {
 public static void main(String[] args)throws Exception{
  try(Connection c=utils.DBConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT DB_NAME()")){r.next();if(!"MioAudit_224a140_20261108".equals(r.getString(1)))throw new IllegalStateException("Refusing UI outside sandbox DB");}
  var login=new bus.DangNhapBus().xuLyDangNhap("0912345678","123456");
  if(!login.isThanhCong())throw new IllegalStateException("Synthetic cashier authentication failed");
  com.formdev.flatlaf.themes.FlatMacLightLaf.setup();
  long started=System.nanoTime();
  javax.swing.SwingUtilities.invokeLater(()->{gui.MainDashboard app=new gui.MainDashboard();app.setTitle("MIO AUDIT ONLY - SYNTHETIC CASHIER - 224a140");app.setVisible(true);System.out.println("UI_READY ms="+((System.nanoTime()-started)/1000000));long[] last={System.nanoTime()};new javax.swing.Timer(100,e->{long now=System.nanoTime();long delay=(now-last[0])/1000000;if(delay>500)System.out.println("EDT_STALL ms="+delay);last[0]=now;}).start();});
 }
}
