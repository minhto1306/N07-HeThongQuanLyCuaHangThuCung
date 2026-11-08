package dao;

import entity.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Hotel and Spa writes share the invoice transaction and its rollback. */
final class HoaDonDichVu {
    private HoaDonDichVu() {}

    static void completeHotel(Connection c,HoaDon h,List<String> rooms)throws SQLException {
        if(rooms==null || rooms.isEmpty())return;
        SortedSet<String> sorted=new TreeSet<>();
        for(String room:rooms){if(room==null || room.isBlank())throw new SQLException("Chưa xác định phòng Hotel");sorted.add(room);}
        List<ChiTietDichVu> hotelLines=new ArrayList<>();
        for(ChiTietHoaDon ct:h.getDsChiTiet())if(ct instanceof ChiTietDichVu d && d.getDichVu()!=null && "DV008".equals(d.getDichVu().getMaDichVu()))hotelLines.add(d);
        for(String room:sorted){
            int days;BigDecimal charge;
            try(PreparedStatement ps=c.prepareStatement("SELECT CASE WHEN DATEDIFF(DAY,CAST(thoiGianCheckIn AS DATE),CAST(GETDATE() AS DATE))<1 THEN 1 ELSE DATEDIFF(DAY,CAST(thoiGianCheckIn AS DATE),CAST(GETDATE() AS DATE)) END days,donGiaNgay,COALESCE(phiDichVuPhatSinh,0) extra FROM PhongLuuTru WITH(UPDLOCK,HOLDLOCK) WHERE maPhong=? AND trangThai=N'ĐANG CÓ KHÁCH' AND thoiGianCheckIn IS NOT NULL AND thoiGianCheckIn<=SYSDATETIME()")){
                ps.setString(1,room);try(ResultSet r=ps.executeQuery()){
                    if(!r.next())throw new SQLException("Phòng "+room+" không còn khách hoặc ngày nhận phòng không hợp lệ");
                    days=r.getInt("days");charge=r.getBigDecimal("donGiaNgay").multiply(BigDecimal.valueOf(days)).add(r.getBigDecimal("extra"));
                }
            }
            List<ChiTietDichVu> matches=new ArrayList<>();
            for(var line:hotelLines)if(room.equals(line.getMaPhongHotel()))matches.add(line);
            if(matches.isEmpty() && sorted.size()==1 && hotelLines.size()==1 && hotelLines.get(0).getMaPhongHotel()==null)matches.add(hotelLines.get(0));
            if(matches.size()!=1 || matches.get(0).getThanhTien().subtract(charge).abs().compareTo(BigDecimal.ONE)>0)throw new SQLException("Dòng tính tiền phòng "+room+" không khớp số ngày và phụ phí; vui lòng tải lại");
            String insert="INSERT INTO LichSuLuuTru(maPhong,tenThuCung,tenChu,sdtChu,thoiGianCheckIn,thoiGianCheckOut,soNgayLuuTru,donGiaNgay,phiPhatSinh,tongTien,ghiChu,maHoaDon,trangThaiThanhToan) SELECT maPhong,tenThuCung,tenChu,sdtChu,thoiGianCheckIn,SYSDATETIME(),?,donGiaNgay,COALESCE(phiDichVuPhatSinh,0),?,ghiChuChamSoc,?,N'DA_THANH_TOAN' FROM PhongLuuTru WHERE maPhong=? AND trangThai=N'ĐANG CÓ KHÁCH'";
            try(PreparedStatement ps=c.prepareStatement(insert)){
                ps.setInt(1,days);ps.setBigDecimal(2,charge);ps.setString(3,h.getMaHoaDon());ps.setString(4,room);
                if(ps.executeUpdate()!=1)throw new SQLException("Không lưu được lịch sử phòng "+room);
            }
            try(PreparedStatement ps=c.prepareStatement("UPDATE PhongLuuTru SET trangThai=N'ĐANG DỌN DẸP' WHERE maPhong=? AND trangThai=N'ĐANG CÓ KHÁCH'")){
                ps.setString(1,room);if(ps.executeUpdate()!=1)throw new SQLException("Không cập nhật được trạng thái phòng "+room);
            }
        }
        for(var line:hotelLines)if(line.getMaPhongHotel()!=null && !sorted.contains(line.getMaPhongHotel()))throw new SQLException("Phòng trên dòng bán chưa được chọn checkout");
    }

    static void processSpa(Connection c,HoaDon h,String card,TheSpaPass newCard)throws SQLException {
        if(card!=null && !card.isBlank()){
            if(newCard!=null)throw new SQLException("Tách riêng giao dịch mua thẻ và sử dụng thẻ");
            long sessions=0;boolean marked=false;
            for(ChiTietHoaDon ct:h.getDsChiTiet())if(ct instanceof ChiTietDichVu d && d.isSuDungSpaPass()){marked=true;sessions+=d.getSoLuong();}
            if(!marked)for(ChiTietHoaDon ct:h.getDsChiTiet())if(ct instanceof ChiTietDichVu d && d.getDichVu()!=null && "DV004".equals(d.getDichVu().getMaDichVu()))sessions+=d.getSoLuong();
            if(sessions<=0 || sessions>Integer.MAX_VALUE)throw new SQLException("Phải có số lượt dịch vụ Spa hợp lệ");
            if(h.getKhachHang()==null || h.getKhachHang().getMaKhachHang()==null)throw new SQLException("Sử dụng Spa Pass phải liên kết đúng khách hàng");
            String update="UPDATE TheSpaPass SET soBuoiDaDung=soBuoiDaDung+? WHERE maThe=? AND trangThai=N'ĐANG HOẠT ĐỘNG' AND ngayKichHoat<=CAST(GETDATE() AS DATE) AND ngayHetHan>=CAST(GETDATE() AS DATE) AND soBuoiDaDung>=0 AND tongSoBuoi-soBuoiDaDung>=? AND EXISTS(SELECT 1 FROM KhachHang k WHERE k.maKhachHang=? AND k.soDienThoai=TheSpaPass.sdtKhachHang)";
            try(PreparedStatement ps=c.prepareStatement(update)){
                ps.setInt(1,(int)sessions);ps.setString(2,card);ps.setInt(3,(int)sessions);ps.setString(4,h.getKhachHang().getMaKhachHang());
                if(ps.executeUpdate()!=1)throw new SQLException("Thẻ không đúng chủ, hết hạn hoặc không đủ lượt; vui lòng tải lại");
            }
            try(PreparedStatement ps=c.prepareStatement("INSERT INTO LichSuSuDungSpaPass(maThe,maHoaDon,maNhanVien,thoiGianSuDung,ghiChu,soLuot) VALUES(?,?,?,SYSDATETIME(),?,?)")){
                ps.setString(1,card);ps.setString(2,h.getMaHoaDon());ps.setString(3,h.getNhanVien().getMaNhanVien());ps.setString(4,"Thanh toán tại POS");ps.setInt(5,(int)sessions);ps.executeUpdate();
            }
        }
        if(newCard!=null){
            if(h.getKhachHang()==null || newCard.getTongSoBuoi()<=0 || newCard.getSoBuoiDaDung()!=0
                    || newCard.getDonGiaGoi()<0 || newCard.getNgayKichHoat()==null || newCard.getNgayHetHan()==null
                    || newCard.getNgayHetHan().isBefore(newCard.getNgayKichHoat()))throw new SQLException("Dữ liệu mua Spa Pass không hợp lệ");
            try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM KhachHang WHERE maKhachHang=? AND soDienThoai=?")){
                ps.setString(1,h.getKhachHang().getMaKhachHang());ps.setString(2,newCard.getSdtKhachHang());try(ResultSet r=ps.executeQuery()){if(!r.next())throw new SQLException("Chủ thẻ không khớp khách hàng trên hóa đơn");}
            }
            try(PreparedStatement ps=c.prepareStatement("INSERT INTO TheSpaPass(maThe,sdtKhachHang,tenKhachHang,tenGoi,tongSoBuoi,soBuoiDaDung,donGiaGoi,ngayKichHoat,ngayHetHan,trangThai,maHoaDonKichHoat) VALUES(?,?,?,?,?,0,?,?,?,?,?)")){
                ps.setString(1,newCard.getMaThe());ps.setString(2,newCard.getSdtKhachHang());ps.setString(3,newCard.getTenKhachHang());ps.setString(4,newCard.getTenGoi());ps.setInt(5,newCard.getTongSoBuoi());ps.setBigDecimal(6,BigDecimal.valueOf(newCard.getDonGiaGoi()));ps.setDate(7,java.sql.Date.valueOf(newCard.getNgayKichHoat()));ps.setDate(8,java.sql.Date.valueOf(newCard.getNgayHetHan()));ps.setString(9,"ĐANG HOẠT ĐỘNG");ps.setString(10,h.getMaHoaDon());ps.executeUpdate();
            }
        }
    }
}
