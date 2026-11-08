-- ==============================================================================
-- MIGRATION SCRIPT: BẢNG CẤU HÌNH GÓI DỊCH VỤ SPA PASS (GoiSpaPass)
-- Dự án: PetShop Mio - Hệ thống Quản lý Cửa hàng Thú cưng
-- Mã vấn đề: DV14 (Chuẩn hóa cấu hình số buổi và thời hạn Spa Pass từ CSDL)
-- ==============================================================================
-- HƯỚNG DẪN:
-- 1. Script này an toàn và có tính lũy kế (Idempotent), có thể chạy lại nhiều lần.
-- 2. Quản lý số buổi và hạn dùng chuẩn xác, không đoán mò hoặc dùng regex từ tên gói.
-- 3. File này để nhóm kiểm duyệt; KHÔNG tự động thực thi trên Production.
-- ==============================================================================

USE [QuanLyCuaHangThuCung];
GO

PRINT N'>>> BẮT ĐẦU KIỂM TRA VÀ KHỞI TẠO BẢNG CẤU HÌNH GÓI SPA PASS...';

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'GoiSpaPass')
BEGIN
    CREATE TABLE GoiSpaPass (
        maGoi NVARCHAR(50) PRIMARY KEY,
        tenGoi NVARCHAR(255) NOT NULL,
        soBuoi INT NOT NULL,
        donGiaGoi DECIMAL(18,2) NOT NULL,
        thoiHanNgay INT NOT NULL,
        trangThaiHoatDong BIT NOT NULL DEFAULT 1
    );
    PRINT N'Đã tạo bảng GoiSpaPass thành công.';
END
GO

IF NOT EXISTS (SELECT 1 FROM GoiSpaPass WHERE maGoi = 'GOI_SPA10')
BEGIN
    INSERT INTO GoiSpaPass (maGoi, tenGoi, soBuoi, donGiaGoi, thoiHanNgay, trangThaiHoatDong)
    VALUES ('GOI_SPA10', N'Gói Chăm Sóc Spa 10 Buổi', 10, 1200000, 180, 1);
END

IF NOT EXISTS (SELECT 1 FROM GoiSpaPass WHERE maGoi = 'GOI_SPA05')
BEGIN
    INSERT INTO GoiSpaPass (maGoi, tenGoi, soBuoi, donGiaGoi, thoiHanNgay, trangThaiHoatDong)
    VALUES ('GOI_SPA05', N'Gói Chăm Sóc Spa 05 Buổi', 5, 650000, 90, 1);
END

IF NOT EXISTS (SELECT 1 FROM GoiSpaPass WHERE maGoi = 'GOI_SPA_VIP')
BEGIN
    INSERT INTO GoiSpaPass (maGoi, tenGoi, soBuoi, donGiaGoi, thoiHanNgay, trangThaiHoatDong)
    VALUES ('GOI_SPA_VIP', N'Gói Spa Chuyên Sâu VIP 12 Buổi', 12, 1800000, 240, 1);
END
GO

PRINT N'>>> HOÀN TẤT MIGRATION DV14.';
GO
