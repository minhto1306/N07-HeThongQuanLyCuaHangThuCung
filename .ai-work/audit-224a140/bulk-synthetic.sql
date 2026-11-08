SET XACT_ABORT ON;
BEGIN TRANSACTION;
CREATE TABLE #N(n INT PRIMARY KEY);
INSERT INTO #N SELECT TOP(20000) ROW_NUMBER() OVER(ORDER BY (SELECT NULL)) FROM sys.all_objects a CROSS JOIN sys.all_objects b;
INSERT INTO SanPham(maSanPham,maDanhMuc,tenSanPham,maVach,donViCoSo,giaBanCoSo)
SELECT 'SP'+CAST(100000+n AS VARCHAR(10)),'DM01',N'AUDIT Sản phẩm '+CAST(n AS NVARCHAR(10)),'AUDITBAR'+CAST(n AS VARCHAR(10)),N'Gói',10000 FROM #N WHERE n<=5000;
INSERT INTO QuyDoiDonVi(maQuyDoi,maSanPham,maDonVi,tyLeQuyDoi,laDonViCoSo,giaBan)
SELECT 'QD'+CAST(100000+n AS VARCHAR(10)),'SP'+CAST(100000+n AS VARCHAR(10)),'DVT01',1,1,10000 FROM #N WHERE n<=5000;
INSERT INTO QuyDoiDonVi(maQuyDoi,maSanPham,maDonVi,tyLeQuyDoi,laDonViCoSo,giaBan)
SELECT 'QD'+CAST(110000+n AS VARCHAR(10)),'SP'+CAST(100000+n AS VARCHAR(10)),'DVT06',10,0,95000 FROM #N WHERE n<=5000;
INSERT INTO KhachHang(maKhachHang,hoTen,soDienThoai,diemTichLuy,email)
SELECT 'KH'+CAST(100000+n AS VARCHAR(10)),N'AUDIT Khách giả '+CAST(n AS NVARCHAR(10)),'099'+RIGHT('0000000'+CAST(n AS VARCHAR(10)),7),0,'audit'+CAST(n AS VARCHAR(10))+'@example.invalid' FROM #N WHERE n<=5000;
INSERT INTO LoHang(soLo,maSanPham,maNhaCungCap,ngaySanXuat,hanSuDung,ngayNhap,donGiaNhapCoSo,soLuongNhap,soLuongTon)
SELECT 'AUDITLOT'+CAST(n AS VARCHAR(10)),'SP'+CAST(100000+((n-1)%5000)+1 AS VARCHAR(10)),'NCC01',DATEADD(day,-60,GETDATE()),DATEADD(day,30+n%90,GETDATE()),DATEADD(day,-40,GETDATE()),5000,1000,1000 FROM #N WHERE n<=15000;
INSERT INTO HoaDon(maHoaDon,maKhachHang,maNhanVien,ngayLap,thoiGianThanhToan,giamGia,thanhTien,phuongThucThanhToan,trangThaiHoaDon)
SELECT 'AUDITHD'+CAST(n AS VARCHAR(10)),'KH'+CAST(100000+((n-1)%5000)+1 AS VARCHAR(10)),'NV003',DATEADD(second,-n*30,GETDATE()),DATEADD(second,-n*30,GETDATE()),0,10000,N'Tiền mặt',N'Đã thanh toán' FROM #N;
INSERT INTO ChiTietHoaDon(maChiTiet,maHoaDon,loaiChiTiet,maSanPham,soLuong,donGiaLucBan,thanhTien)
SELECT 'AUDITCT'+CAST(n AS VARCHAR(10)),'AUDITHD'+CAST(n AS VARCHAR(10)),'SAN_PHAM','SP'+CAST(100000+((n-1)%5000)+1 AS VARCHAR(10)),1,10000,10000 FROM #N;
INSERT INTO ChiTietXuatLo(maChiTiet,soLo,maQuyDoi,soLuongXuatCoSo)
SELECT 'AUDITCT'+CAST(n AS VARCHAR(10)),'AUDITLOT'+CAST(((n-1)%5000)+1 AS VARCHAR(10)),'QD'+CAST(100000+((n-1)%5000)+1 AS VARCHAR(10)),1 FROM #N;
UPDATE l SET soLuongTon=l.soLuongNhap-x.qty FROM LoHang l JOIN (SELECT soLo,SUM(soLuongXuatCoSo) qty FROM ChiTietXuatLo GROUP BY soLo) x ON x.soLo=l.soLo WHERE l.soLo LIKE 'AUDITLOT%';
UPDATE k SET diemTichLuy=0 FROM KhachHang k WHERE k.maKhachHang LIKE 'KH10%';
DROP TABLE #N;
COMMIT TRANSACTION;
