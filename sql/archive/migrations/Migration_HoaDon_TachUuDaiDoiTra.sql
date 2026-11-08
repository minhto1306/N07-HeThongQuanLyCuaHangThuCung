-- ==============================================================================
-- MIGRATION SCRIPT: BỔ SUNG CỘT TIEN_DOI_DIEM VÀ TIEN_BU_TRU_DOI_TRA CHO BẢNG HOADON
-- Dự án: PetShop Mio - Hệ thống Quản lý Cửa hàng Thú cưng
-- Mã vấn đề: Phase 2 (Minh bạch hạch toán giảm giá VIP, tiền đổi điểm và bù trừ đổi trả)
-- ==============================================================================
-- HƯỚNG DẪN:
-- 1. Script này an toàn và có tính lũy kế (Idempotent), có thể chạy lại nhiều lần.
-- 2. Dành cho các CSDL ĐÃ CÓ DỮ LIỆU CŨ để nâng cấp lên chuẩn Phase 2 mà không mất dữ liệu.
-- 3. File này để nhóm kiểm duyệt; KHÔNG tự động thực thi trên Production.
-- ==============================================================================

USE [QuanLyCuaHangThuCung];
GO

PRINT N'>>> BẮT ĐẦU KIỂM TRA VÀ BỔ SUNG CỘT TÀI CHÍNH PHASE 2 CHO BẢNG HoaDon...';

-- 1. Thêm cột tienDoiDiem
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'HoaDon' AND COLUMN_NAME = 'tienDoiDiem'
)
BEGIN
    ALTER TABLE HoaDon ADD tienDoiDiem DECIMAL(18,2) NOT NULL CONSTRAINT DF_HoaDon_TienDoiDiem DEFAULT 0;
    PRINT N'Đã thêm cột tienDoiDiem vào bảng HoaDon.';
END
ELSE
BEGIN
    PRINT N'Cột tienDoiDiem đã tồn tại trong bảng HoaDon.';
END

-- 2. Thêm cột tienBuTruDoiTra
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'HoaDon' AND COLUMN_NAME = 'tienBuTruDoiTra'
)
BEGIN
    ALTER TABLE HoaDon ADD tienBuTruDoiTra DECIMAL(18,2) NOT NULL CONSTRAINT DF_HoaDon_TienBuTruDoiTra DEFAULT 0;
    PRINT N'Đã thêm cột tienBuTruDoiTra vào bảng HoaDon.';
END
ELSE
BEGIN
    PRINT N'Cột tienBuTruDoiTra đã tồn tại trong bảng HoaDon.';
END
GO

PRINT N'>>> HOÀN TẤT MIGRATION HOA DON PHASE 2.';
GO
