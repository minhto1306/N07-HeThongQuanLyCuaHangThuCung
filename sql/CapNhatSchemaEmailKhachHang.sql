-- Script bổ sung cột email và diaChi cho bảng KhachHang
-- Dùng cho tính năng gửi email xác nhận đặt lịch hẹn, nhắc tiêm phòng và báo cáo chăm sóc thú cưng lưu trú Pet Hotel
USE QuanLyCuaHangThuCung;
GO

IF COL_LENGTH('KhachHang', 'email') IS NULL
BEGIN
    ALTER TABLE KhachHang ADD email NVARCHAR(100) NULL;
    PRINT N'Đã thêm cột email vào bảng KhachHang thành công!';
END
GO

IF COL_LENGTH('KhachHang', 'diaChi') IS NULL
BEGIN
    ALTER TABLE KhachHang ADD diaChi NVARCHAR(255) NULL;
    PRINT N'Đã thêm cột diaChi vào bảng KhachHang thành công!';
END
GO

-- Cập nhật dữ liệu mẫu email cho các khách hàng để demo gửi thư
UPDATE KhachHang SET email = 'khachhang.an@gmail.com', diaChi = N'12 Nguyễn Văn Bảo, Gò Vấp, TP.HCM' WHERE maKhachHang = 'KH001';
UPDATE KhachHang SET email = 'mai.tran@gmail.com', diaChi = N'45 Lê Lợi, P.4, Gò Vấp, TP.HCM' WHERE maKhachHang = 'KH002';
UPDATE KhachHang SET email = 'hoangnam.pet@gmail.com', diaChi = N'78 Quang Trung, P.10, Gò Vấp, TP.HCM' WHERE maKhachHang = 'KH003';
GO

PRINT N'Hoàn tất cập nhật schema email khách hàng!';
GO
