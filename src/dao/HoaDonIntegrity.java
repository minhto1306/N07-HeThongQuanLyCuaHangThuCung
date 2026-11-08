package dao;

import entity.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.*;

/** Validation on the same connection as HoaDonDAO's transaction. */
final class HoaDonIntegrity {
    private HoaDonIntegrity() {}
    private static final BigDecimal POINT_UNIT = BigDecimal.valueOf(100000);

    static void validateSale(HoaDon h) {
        if (h == null || h.getMaHoaDon() == null || h.getMaHoaDon().isBlank()
                || h.getDsChiTiet() == null || h.getDsChiTiet().isEmpty()
                || h.getNhanVien() == null || h.getNhanVien().getMaNhanVien() == null
                || h.getNhanVien().getMaNhanVien().isBlank()) {
            throw new IllegalArgumentException("Hóa đơn phải có mã, nhân viên và chi tiết");
        }
        nonnegative(h.getGiamGia(), "Giảm giá");
        nonnegative(h.getThanhTien(), "Tổng thanh toán");
        BigDecimal sum = BigDecimal.ZERO;
        for (ChiTietHoaDon ct : h.getDsChiTiet()) {
            if (ct == null || ct.getSoLuong() <= 0) throw new IllegalArgumentException("Số lượng bán phải dương");
            nonnegative(ct.getDonGiaLucBan(), "Đơn giá");
            nonnegative(ct.getThanhTien(), "Thành tiền dòng");
            BigDecimal roundingTolerance = BigDecimal.valueOf(ct.getSoLuong())
                    .multiply(new BigDecimal("0.005")).max(BigDecimal.ONE);
            if (ct.getDonGiaLucBan().multiply(BigDecimal.valueOf(ct.getSoLuong()))
                    .subtract(ct.getThanhTien()).abs().compareTo(roundingTolerance) > 0) {
                throw new IllegalArgumentException("Thành tiền dòng không khớp số lượng và đơn giá");
            }
            if (ct instanceof ChiTietXuatLo x && x.getSoLuongXuatCoSo() <= 0) {
                throw new IllegalArgumentException("Số lượng xuất cơ sở phải dương");
            }
            sum = sum.add(ct.getThanhTien());
        }
        if (sum.subtract(h.getGiamGia()).max(BigDecimal.ZERO).subtract(h.getThanhTien())
                .abs().compareTo(new BigDecimal("0.01")) > 0) {
            throw new IllegalArgumentException("Tổng thanh toán không khớp chi tiết và giảm giá");
        }
    }

    static String resolveUnit(Connection c, ChiTietXuatLo x) throws SQLException {
        if (x.getLoHang() == null || x.getLoHang().getSoLo() == null) throw new SQLException("Chưa chọn lô xuất");
        String qd = x.getQuyDoiDonVi() == null ? null : x.getQuyDoiDonVi().getMaQuyDoi();
        if (qd != null && qd.isBlank()) throw new SQLException("Mã quy đổi không hợp lệ");
        String sql = "SELECT TOP 1 q.maQuyDoi,q.tyLeQuyDoi,l.maSanPham FROM LoHang l "
                + "JOIN QuyDoiDonVi q ON q.maSanPham=l.maSanPham WHERE l.soLo=? "
                + (qd == null ? "AND q.tyLeQuyDoi=1 " : "AND q.maQuyDoi=? ")
                + "ORDER BY q.laDonViCoSo DESC,q.maQuyDoi";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, x.getLoHang().getSoLo());
            if (qd != null) ps.setString(2, qd);
            try (ResultSet r = ps.executeQuery()) {
                if (!r.next()) throw new SQLException("Không có quy đổi hợp lệ của sản phẩm trong lô; bổ sung đơn vị cơ sở trước khi bán");
                String product = r.getString("maSanPham");
                int ratio = r.getInt("tyLeQuyDoi");
                if (x.getSanPham() == null || !product.equals(x.getSanPham().getMaSanPham()) || ratio <= 0
                        || (long)x.getSoLuong() * ratio != x.getSoLuongXuatCoSo()) {
                    throw new SQLException("Sản phẩm, đơn vị quy đổi và số lượng xuất cơ sở không khớp");
                }
                return r.getString("maQuyDoi");
            }
        }
    }

    record ReturnPoints(String customer, int reclaim) {
        void apply(Connection c) throws SQLException {
            if (customer == null || reclaim <= 0) return;
            try (PreparedStatement ps = c.prepareStatement("UPDATE KhachHang SET diemTichLuy=CASE WHEN diemTichLuy>=? THEN diemTichLuy-? ELSE 0 END WHERE maKhachHang=?")) {
                ps.setInt(1, reclaim);ps.setInt(2, reclaim);ps.setString(3, customer);
                if (ps.executeUpdate() != 1) throw new SQLException("Không tìm thấy khách hàng gốc để thu hồi điểm");
            }
        }
    }
    private static final class Sold {
        long base;
        int ratio;
        BigDecimal gross;
        Sold(long base, int ratio, BigDecimal gross) { this.base=base;this.ratio=ratio;this.gross=gross; }
    }
    private static final class Returned {
        long base;
        BigDecimal money=BigDecimal.ZERO;
        boolean unresolved;
    }

    static ReturnPoints validateReturn(Connection c, PhieuDoiTra p) throws SQLException {
        if (p == null || p.getHoaDonGoc() == null || p.getHoaDonGoc().getMaHoaDon() == null
                || p.getDsChiTietDoiTra() == null || p.getDsChiTietDoiTra().isEmpty()) {
            throw new IllegalArgumentException("Phiếu trả phải có hóa đơn gốc và chi tiết");
        }
        String invoice = p.getHoaDonGoc().getMaHoaDon(), customer;
        BigDecimal discount, sourceTotal;
        // Every return path takes this lock before reading earlier returns.
        try (PreparedStatement ps=c.prepareStatement("SELECT maKhachHang,giamGia,thanhTien,trangThaiHoaDon FROM HoaDon WITH(UPDLOCK,HOLDLOCK) WHERE maHoaDon=?")) {
            ps.setString(1, invoice);
            try(ResultSet r=ps.executeQuery()) {
                if(!r.next() || !"Đã thanh toán".equals(r.getString("trangThaiHoaDon"))) throw new SQLException("Chỉ được trả hàng của hóa đơn đã thanh toán");
                customer=r.getString("maKhachHang");discount=r.getBigDecimal("giamGia");sourceTotal=r.getBigDecimal("thanhTien");
            }
        }
        Map<String,Sold> sold=new HashMap<>();BigDecimal gross=BigDecimal.ZERO;
        try(PreparedStatement ps=c.prepareStatement("SELECT ct.maSanPham,SUM(CAST(x.soLuongXuatCoSo AS BIGINT)) base,SUM(ct.thanhTien) gross,MIN(CASE WHEN ct.soLuong>0 AND x.soLuongXuatCoSo%ct.soLuong=0 THEN x.soLuongXuatCoSo/ct.soLuong ELSE 0 END) minRatio,MAX(CASE WHEN ct.soLuong>0 AND x.soLuongXuatCoSo%ct.soLuong=0 THEN x.soLuongXuatCoSo/ct.soLuong ELSE 0 END) maxRatio FROM ChiTietHoaDon ct JOIN ChiTietXuatLo x ON x.maChiTiet=ct.maChiTiet WHERE ct.maHoaDon=? GROUP BY ct.maSanPham")) {
            ps.setString(1,invoice);try(ResultSet r=ps.executeQuery()){while(r.next()){
                int ratio=r.getInt("minRatio")==r.getInt("maxRatio")?r.getInt("minRatio"):0;
                sold.put(r.getString("maSanPham"),new Sold(r.getLong("base"),ratio,r.getBigDecimal("gross")));
            }}
        }
        try(PreparedStatement ps=c.prepareStatement("SELECT COALESCE(SUM(thanhTien),0) FROM ChiTietHoaDon WHERE maHoaDon=?")){
            ps.setString(1,invoice);try(ResultSet r=ps.executeQuery()){r.next();gross=r.getBigDecimal(1);}
        }
        Map<String,Returned> previous=new HashMap<>();BigDecimal oldMoney=BigDecimal.ZERO;
        try(PreparedStatement ps=c.prepareStatement("SELECT ct.maSanPham,ct.soLuongCoSoTra,ct.tienHoanLai FROM ChiTietDoiTra ct JOIN PhieuDoiTra p ON p.maPhieuDoiTra=ct.maPhieuDoiTra WHERE p.maHoaDonGoc=?")){
            ps.setString(1,invoice);try(ResultSet r=ps.executeQuery()){while(r.next()){
                Returned a=previous.computeIfAbsent(r.getString(1),k->new Returned());
                long base=r.getLong(2);a.unresolved|=r.wasNull()||base<=0;a.base+=base;
                BigDecimal money=r.getBigDecimal(3);a.money=a.money.add(money);oldMoney=oldMoney.add(money);
            }}
        }
        Map<String,Returned> proposed=new HashMap<>();Map<String,Long> lotRestock=new HashMap<>();BigDecimal newMoney=BigDecimal.ZERO;
        for(ChiTietDoiTra ct:p.getDsChiTietDoiTra()){
            if(ct==null || ct.getSanPham()==null || ct.getSoLuongDoiTra()<=0) throw new IllegalArgumentException("Chi tiết trả phải có sản phẩm và số lượng dương");
            nonnegative(ct.getTienHoanLai(),"Tiền trả dòng");String product=ct.getSanPham().getMaSanPham();Sold original=sold.get(product);
            if(original==null || original.base<=0) throw new SQLException("Sản phẩm trả không có trên hóa đơn gốc");
            long base=ct.getSoLuongCoSoTra();
            if(base<=0){
                // Conversion ambiguity cannot be guessed from a product's current pack settings.
                if(original.ratio<=0) throw new SQLException("Hóa đơn bán nhiều quy cách; cần chỉ rõ số lượng trả cơ sở");
                base=(long)ct.getSoLuongDoiTra()*original.ratio;
                if(base>Integer.MAX_VALUE)throw new SQLException("Số lượng trả quá lớn");ct.setSoLuongCoSoTra((int)base);
            }
            if(ct.getSoLuongCoSoHoan()<0 || ct.getSoLuongCoSoHoan()>base) throw new SQLException("Lượng nhập lại kho vượt lượng hàng trả");
            if(ct.getSoLuongCoSoHoan()>0){
                if(ct.getSoLo()==null || ct.getSoLo().isBlank())throw new SQLException("Nhập lại kho phải chỉ rõ lô gốc");
                lotRestock.merge(product+"\u0001"+ct.getSoLo(),(long)ct.getSoLuongCoSoHoan(),Long::sum);
            }
            Returned a=proposed.computeIfAbsent(product,k->new Returned());a.base+=base;a.money=a.money.add(ct.getTienHoanLai());newMoney=newMoney.add(ct.getTienHoanLai());
        }
        for(var entry:proposed.entrySet()){
            Sold original=sold.get(entry.getKey());Returned prior=previous.getOrDefault(entry.getKey(),new Returned()), now=entry.getValue();
            if(prior.unresolved)throw new SQLException("Lịch sử trả cũ chưa đối soát được đơn vị cơ sở; cần quản lý kiểm tra");
            if(prior.base+now.base>original.base)throw new SQLException("Tổng hàng trả vượt số lượng đã mua hoặc vừa được trả tại quầy khác");
            BigDecimal netFactor=gross.signum()>0?gross.subtract(discount).max(BigDecimal.ZERO).divide(gross,16,RoundingMode.DOWN):BigDecimal.ZERO;
            BigDecimal cap=original.gross.multiply(netFactor).multiply(BigDecimal.valueOf(prior.base+now.base)).divide(BigDecimal.valueOf(original.base),2,RoundingMode.HALF_UP);
            if(prior.money.add(now.money).subtract(cap).compareTo(BigDecimal.ONE)>0)throw new SQLException("Tiền hoàn vượt giá trị hàng gốc sau giảm giá");
        }
        for(var entry:lotRestock.entrySet()){
            String[] key=entry.getKey().split("\u0001",2);long soldInLot=0, returnedToLot=0;
            try(PreparedStatement ps=c.prepareStatement("SELECT COALESCE(SUM(CAST(x.soLuongXuatCoSo AS BIGINT)),0) FROM ChiTietXuatLo x JOIN ChiTietHoaDon ct ON ct.maChiTiet=x.maChiTiet WHERE ct.maHoaDon=? AND ct.maSanPham=? AND x.soLo=?")){
                ps.setString(1,invoice);ps.setString(2,key[0]);ps.setString(3,key[1]);try(ResultSet r=ps.executeQuery()){r.next();soldInLot=r.getLong(1);}
            }
            try(PreparedStatement ps=c.prepareStatement("SELECT COALESCE(SUM(CAST(ct.soLuongCoSoHoan AS BIGINT)),0) FROM ChiTietDoiTra ct JOIN PhieuDoiTra p ON p.maPhieuDoiTra=ct.maPhieuDoiTra WHERE p.maHoaDonGoc=? AND ct.maSanPham=? AND ct.soLo=?")){
                ps.setString(1,invoice);ps.setString(2,key[0]);ps.setString(3,key[1]);try(ResultSet r=ps.executeQuery()){r.next();returnedToLot=r.getLong(1);}
            }
            if(soldInLot<=0 || returnedToLot+entry.getValue()>soldInLot)throw new SQLException("Lô nhập lại không thuộc hóa đơn hoặc vượt lượng xuất gốc");
        }
        nonnegative(p.getTongTienHoan(),"Tiền thực hoàn");
        if(p.getTongTienHoan().compareTo(newMoney)>0)throw new SQLException("Tiền thực hoàn vượt giá trị hàng trả");
        int earned=sourceTotal.divide(POINT_UNIT,0,RoundingMode.DOWN).intValue();
        int before=Math.min(earned,oldMoney.divide(POINT_UNIT,0,RoundingMode.DOWN).intValue());
        int after=Math.min(earned,oldMoney.add(newMoney).divide(POINT_UNIT,0,RoundingMode.DOWN).intValue());
        return new ReturnPoints(customer,Math.max(0,after-before));
    }
    private static void nonnegative(BigDecimal value,String name){
        if(value==null || value.signum()<0)throw new IllegalArgumentException(name+" phải không âm");
    }
}
