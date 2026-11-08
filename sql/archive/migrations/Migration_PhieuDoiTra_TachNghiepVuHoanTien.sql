-- ==============================================================================
-- MIGRATION SCRIPT: TÁCH RIÊNG CÁC TRƯỜNG GIÁ TRỊ VÀ PHƯƠNG THỨC HOÀN TIỀN TRONG PHIEUDOITRA
-- Dự án: PetShop Mio - Hệ thống Quản lý Cửa hàng Thú cưng
-- Mã vấn đề: TC09, TC10 (Minh bạch dòng tiền đổi trả / két tiền ca)
-- ==============================================================================
-- HƯỚNG DẪN:
-- 1. Script này an toàn và có tính lũy kế (Idempotent), có thể chạy lại nhiều lần.
-- 2. Chỉ tạo thêm 5 cột phục vụ hạch toán độc lập:
--    - giaTriHangTra: Tổng giá trị sản phẩm khách trả lại
--    - giaTriHangDoi: Tổng giá trị sản phẩm mới khách đổi sang
--    - tienHoanThucTe: Số tiền thực tế cửa hàng chi trả lại khách
--    - tienKhachTraThem: Số tiền khách phải đóng thêm nếu đổi hàng giá trị cao hơn
--    - phuongThucHoanTien: Phương thức hoàn tiền thực tế ('Tiền mặt', 'Chuyển khoản', 'CHUA_XAC_DINH')
-- 3. Dữ liệu lịch sử cũ không thể xác định phương thức hoàn sẽ mang 'CHUA_XAC_DINH', không tự suy đoán.
-- 4. KHÔNG tự thực thi trên Production/Database đang sử dụng khi chưa được phê duyệt.
-- ==============================================================================

USE [QuanLyCuaHangThuCung];
GO

PRINT N'>>> BẮT ĐẦU KIỂM TRA VÀ BỔ SUNG CỘT CHO BẢNG PhieuDoiTra...';

-- 1. Thêm cột giaTriHangTra
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'PhieuDoiTra' AND COLUMN_NAME = 'giaTriHangTra'
)
BEGIN
    ALTER TABLE PhieuDoiTra ADD giaTriHangTra DECIMAL(18,2) NULL;
    PRINT N'Đã thêm cột giaTriHangTra vào PhieuDoiTra.';
END

-- 2. Thêm cột giaTriHangDoi
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'PhieuDoiTra' AND COLUMN_NAME = 'giaTriHangDoi'
)
BEGIN
    ALTER TABLE PhieuDoiTra ADD giaTriHangDoi DECIMAL(18,2) NULL;
    PRINT N'Đã thêm cột giaTriHangDoi vào PhieuDoiTra.';
END

-- 3. Thêm cột tienHoanThucTe
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'PhieuDoiTra' AND COLUMN_NAME = 'tienHoanThucTe'
)
BEGIN
    ALTER TABLE PhieuDoiTra ADD tienHoanThucTe DECIMAL(18,2) NULL;
    PRINT N'Đã thêm cột tienHoanThucTe vào PhieuDoiTra.';
END

-- 4. Thêm cột tienKhachTraThem
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'PhieuDoiTra' AND COLUMN_NAME = 'tienKhachTraThem'
)
BEGIN
    ALTER TABLE PhieuDoiTra ADD tienKhachTraThem DECIMAL(18,2) NULL;
    PRINT N'Đã thêm cột tienKhachTraThem vào PhieuDoiTra.';
END

-- 5. Thêm cột phuongThucHoanTien
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'PhieuDoiTra' AND COLUMN_NAME = 'phuongThucHoanTien'
)
BEGIN
    ALTER TABLE PhieuDoiTra ADD phuongThucHoanTien NVARCHAR(50) NULL CONSTRAINT DF_PDT_phuongThucHoan DEFAULT N'CHUA_XAC_DINH';
    PRINT N'Đã thêm cột phuongThucHoanTien vào PhieuDoiTra.';
END
GO

-- 6. Không suy diễn dữ liệu lịch sử cũ (TC09, TC10):
-- Dữ liệu cũ giữ nguyên NULL và phuongThucHoanTien = N'CHUA_XAC_DINH' để đảm bảo tính trung thực số liệu hạch toán.
UPDATE PhieuDoiTra
SET phuongThucHoanTien = N'CHUA_XAC_DINH'
WHERE phuongThucHoanTien IS NULL;
GO

PRINT N'>>> HOÀN TẤT MIGRATION TC09 / TC10.';
GO
