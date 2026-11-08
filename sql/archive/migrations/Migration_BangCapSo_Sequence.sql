-- ==============================================================================
-- MIGRATION SCRIPT: BẢNG CẤP SỐ THỨ TỰ TẬP TRUNG CHỐNG TRÙNG MÃ ĐA MÁY / ĐA QUẦY
-- Dự án: PetShop Mio - Hệ thống Quản lý Cửa hàng Thú cưng
-- Mã vấn đề: BH03 (Sinh mã an toàn concurrency)
-- ==============================================================================
-- HƯỚNG DẪN:
-- 1. Script này an toàn và có tính lũy kế (Idempotent), có thể chạy lại nhiều lần.
-- 2. Tạo bảng BangCapSo để quản lý giá trị số thứ tự tiếp theo của từng loại mã
--    bằng kỹ thuật UPDATE ... WITH (UPDLOCK, ROWLOCK) OUTPUT INSERTED.soHienTai.
-- 3. Giữ nguyên 100% định dạng mã hiện tại (HDyyMMdd-xx-xxx, NVxxx, SPxxx, DTxxx...).
-- 4. KHÔNG sử dụng UUID làm phá vỡ nghiệp vụ quản lý và giao diện người dùng.
-- 5. File này để nhóm kiểm duyệt; KHÔNG tự động thực thi trên Production.
-- ==============================================================================

USE [QuanLyCuaHangThuCung];
GO

PRINT N'>>> BẮT ĐẦU KHỞI TẠO BẢNG CẤP SỐ THỨ TỰ ĐỒNG BỘ...';

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BangCapSo')
BEGIN
    CREATE TABLE BangCapSo (
        loaiMa NVARCHAR(50) PRIMARY KEY,     -- Ví dụ: 'NV', 'SP', 'DV', 'HD_261108', 'DT_261108'
        soHienTai INT NOT NULL DEFAULT 0,    -- Số thứ tự hiện tại đã cấp
        moTa NVARCHAR(255)
    );
    PRINT N'Đã tạo bảng BangCapSo.';
END
GO

-- Khởi tạo giá trị ban đầu từ dữ liệu thực tế hiện có trong CSDL nếu chưa có bản ghi
IF NOT EXISTS (SELECT 1 FROM BangCapSo WHERE loaiMa = 'NV')
BEGIN
    DECLARE @maxNV INT = 0;
    SELECT @maxNV = ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maNhanVien, 3, 20))), 0) FROM NhanVien WHERE maNhanVien LIKE 'NV%';
    INSERT INTO BangCapSo (loaiMa, soHienTai, moTa) VALUES ('NV', @maxNV, N'Mã nhân viên / tài khoản');
END

IF NOT EXISTS (SELECT 1 FROM BangCapSo WHERE loaiMa = 'SP')
BEGIN
    DECLARE @maxSP INT = 0;
    SELECT @maxSP = ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maSanPham, 3, 20))), 0) FROM SanPham WHERE maSanPham LIKE 'SP%';
    INSERT INTO BangCapSo (loaiMa, soHienTai, moTa) VALUES ('SP', @maxSP, N'Mã sản phẩm');
END

IF NOT EXISTS (SELECT 1 FROM BangCapSo WHERE loaiMa = 'DV')
BEGIN
    DECLARE @maxDV INT = 0;
    SELECT @maxDV = ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maDichVu, 3, 20))), 0) FROM DichVu WHERE maDichVu LIKE 'DV%';
    INSERT INTO BangCapSo (loaiMa, soHienTai, moTa) VALUES ('DV', @maxDV, N'Mã dịch vụ');
END

IF NOT EXISTS (SELECT 1 FROM BangCapSo WHERE loaiMa = 'KH')
BEGIN
    DECLARE @maxKH INT = 0;
    SELECT @maxKH = ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maKhachHang, 3, 20))), 0) FROM KhachHang WHERE maKhachHang LIKE 'KH%';
    INSERT INTO BangCapSo (loaiMa, soHienTai, moTa) VALUES ('KH', @maxKH, N'Mã khách hàng thành viên');
END

IF NOT EXISTS (SELECT 1 FROM BangCapSo WHERE loaiMa = 'TC')
BEGIN
    DECLARE @maxTC INT = 0;
    SELECT @maxTC = ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maThuCung, 3, 20))), 0) FROM ThuCung WHERE maThuCung LIKE 'TC%';
    INSERT INTO BangCapSo (loaiMa, soHienTai, moTa) VALUES ('TC', @maxTC, N'Mã thú cưng');
END

IF NOT EXISTS (SELECT 1 FROM BangCapSo WHERE loaiMa = 'HS')
BEGIN
    DECLARE @maxHS INT = 0;
    SELECT @maxHS = ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maHoSo, 3, 20))), 0) FROM HoSoThuCung WHERE maHoSo LIKE 'HS%';
    INSERT INTO BangCapSo (loaiMa, soHienTai, moTa) VALUES ('HS', @maxHS, N'Mã hồ sơ thú cưng');
END

IF NOT EXISTS (SELECT 1 FROM BangCapSo WHERE loaiMa = 'SK')
BEGIN
    DECLARE @maxSK INT = 0;
    SELECT @maxSK = ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maHoSo, 3, 20))), 0) FROM HoSoSucKhoe WHERE maHoSo LIKE 'SK%';
    INSERT INTO BangCapSo (loaiMa, soHienTai, moTa) VALUES ('SK', @maxSK, N'Mã sổ sức khỏe');
END

IF NOT EXISTS (SELECT 1 FROM BangCapSo WHERE loaiMa = 'DVT')
BEGIN
    DECLARE @maxDVT INT = 0;
    SELECT @maxDVT = ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maDonVi, 4, 20))), 0) FROM DonViTinh WHERE maDonVi LIKE 'DVT%';
    INSERT INTO BangCapSo (loaiMa, soHienTai, moTa) VALUES ('DVT', @maxDVT, N'Mã đơn vị tính');
END

IF NOT EXISTS (SELECT 1 FROM BangCapSo WHERE loaiMa = 'QD')
BEGIN
    DECLARE @maxQD INT = 0;
    SELECT @maxQD = ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maQuyDoi, 3, 20))), 0) FROM QuyDoiDonVi WHERE maQuyDoi LIKE 'QD%';
    INSERT INTO BangCapSo (loaiMa, soHienTai, moTa) VALUES ('QD', @maxQD, N'Mã quy đổi đơn vị');
END

IF NOT EXISTS (SELECT 1 FROM BangCapSo WHERE loaiMa = 'GC')
BEGIN
    DECLARE @maxGC INT = 0;
    SELECT @maxGC = ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maGiaoCa, 3, 20))), 0) FROM BangGiaoCa WHERE maGiaoCa LIKE 'GC%';
    INSERT INTO BangCapSo (loaiMa, soHienTai, moTa) VALUES ('GC', @maxGC, N'Mã biên bản giao ca');
END

IF NOT EXISTS (SELECT 1 FROM BangCapSo WHERE loaiMa = 'PC')
BEGIN
    DECLARE @maxPC INT = 0;
    SELECT @maxPC = ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maPhanCong, 3, 20))), 0) FROM BangPhanCong WHERE maPhanCong LIKE 'PC%';
    INSERT INTO BangCapSo (loaiMa, soHienTai, moTa) VALUES ('PC', @maxPC, N'Mã bảng phân công');
END
GO

PRINT N'>>> HOÀN TẤT MIGRATION BH03.';
GO
