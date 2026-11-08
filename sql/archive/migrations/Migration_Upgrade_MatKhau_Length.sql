-- ==============================================================================
-- MIGRATION SCRIPT: MỞ RỘNG ĐỘ DÀI CỘT TAIKHOAN.MATKHAU ĐỂ LƯU HASH PBKDF2
-- Dự án: PetShop Mio - Hệ thống Quản lý Cửa hàng Thú cưng
-- Mã vấn đề: TC02 (Bảo mật mật khẩu)
-- ==============================================================================
-- HƯỚNG DẪN:
-- 1. Script này an toàn và có tính lũy kế (Idempotent), có thể chạy lại nhiều lần.
-- 2. Tự động kiểm tra độ dài hiện tại của cột matKhau trước khi ALTER.
-- 3. KHÔNG làm mất dữ liệu mật khẩu cũ (các hash SHA-256 64 ký tự vẫn được giữ nguyên).
-- 4. Không tự động thực thi; chờ Quản trị viên/DBA kiểm duyệt trước khi chạy trên Production.
-- ==============================================================================

USE [QuanLyCuaHangThuCung];
GO

PRINT N'>>> BẮT ĐẦU KIỂM TRA VÀ NÂNG CẤP ĐỘ DÀI CỘT TaiKhoan.matKhau...';

IF EXISTS (
    SELECT 1 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'TaiKhoan' 
      AND COLUMN_NAME = 'matKhau' 
      AND (CHARACTER_MAXIMUM_LENGTH < 256 OR CHARACTER_MAXIMUM_LENGTH IS NULL)
)
BEGIN
    PRINT N'Cột TaiKhoan.matKhau hiện tại có độ dài < 256 ký tự. Tiến hành mở rộng lên NVARCHAR(256)...';
    
    ALTER TABLE TaiKhoan 
    ALTER COLUMN matKhau NVARCHAR(256) NOT NULL;
    
    PRINT N'MỞ RỘNG CỘT TaiKhoan.matKhau LÊN NVARCHAR(256) THÀNH CÔNG!';
END
ELSE
BEGIN
    PRINT N'Cột TaiKhoan.matKhau đã có độ dài >= 256 ký tự (hoặc không cần thay đổi). Bỏ qua.';
END
GO

PRINT N'>>> HOÀN TẤT MIGRATION TC02.';
GO
