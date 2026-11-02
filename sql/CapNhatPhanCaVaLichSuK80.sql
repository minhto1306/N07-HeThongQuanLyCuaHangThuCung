USE QuanLyCuaHangThuCung;
GO

SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- Không tự ý xóa phân công. Nếu dữ liệu cũ bị trùng, liệt kê để người quản trị xử lý trước.
IF EXISTS (
    SELECT 1 FROM BangPhanCong
    GROUP BY maNhanVien, ngayLamViec, maCa
    HAVING COUNT(*) > 1
)
BEGIN
    SELECT maNhanVien, ngayLamViec, maCa, COUNT(*) AS soBanGhi
    FROM BangPhanCong
    GROUP BY maNhanVien, ngayLamViec, maCa
    HAVING COUNT(*) > 1;
    THROW 51000, N'BangPhanCong đang có dữ liệu trùng. Hãy gộp nhiệm vụ vào một bản ghi rồi chạy lại migration.', 1;
END;

IF NOT EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_BangPhanCong_NhanVien_Ngay_Ca')
    ALTER TABLE BangPhanCong
    ADD CONSTRAINT UQ_BangPhanCong_NhanVien_Ngay_Ca
        UNIQUE (maNhanVien, ngayLamViec, maCa);

IF COL_LENGTH('dbo.BangGiaoCa', 'tienThuTienMat') IS NULL
    EXEC(N'ALTER TABLE dbo.BangGiaoCa ADD tienThuTienMat DECIMAL(18,2) NOT NULL CONSTRAINT DF_BangGiaoCa_TienThuTienMat DEFAULT 0');
IF COL_LENGTH('dbo.BangGiaoCa', 'tienChuyenKhoan') IS NULL
    EXEC(N'ALTER TABLE dbo.BangGiaoCa ADD tienChuyenKhoan DECIMAL(18,2) NOT NULL CONSTRAINT DF_BangGiaoCa_TienChuyenKhoan DEFAULT 0');
IF COL_LENGTH('dbo.BangGiaoCa', 'tienHoanTra') IS NULL
    EXEC(N'ALTER TABLE dbo.BangGiaoCa ADD tienHoanTra DECIMAL(18,2) NOT NULL CONSTRAINT DF_BangGiaoCa_TienHoanTra DEFAULT 0');
IF COL_LENGTH('dbo.BangGiaoCa', 'trangThai') IS NULL
    EXEC(N'ALTER TABLE dbo.BangGiaoCa ADD trangThai NVARCHAR(20) NOT NULL CONSTRAINT DF_BangGiaoCa_TrangThai DEFAULT ''CHO_DUYET''');
IF COL_LENGTH('dbo.BangGiaoCa', 'maQuanLyDuyet') IS NULL
    EXEC(N'ALTER TABLE dbo.BangGiaoCa ADD maQuanLyDuyet NVARCHAR(20) NULL');
IF COL_LENGTH('dbo.BangGiaoCa', 'thoiGianDuyet') IS NULL
    EXEC(N'ALTER TABLE dbo.BangGiaoCa ADD thoiGianDuyet DATETIME2 NULL');

-- Dữ liệu cũ chưa lưu chi tiết nên tiền mặt chỉ có thể khôi phục theo công thức lịch sử.
-- Các bản ghi mới luôn lưu trực tiếp ba khoản tiền, không còn suy ngược khi in K80.
EXEC(N'
UPDATE dbo.BangGiaoCa
SET tienThuTienMat = CASE WHEN tienCuoiCaHeThong >= tienDauCa
                          THEN tienCuoiCaHeThong - tienDauCa ELSE 0 END
WHERE tienThuTienMat = 0
  AND (lyDoChenhLech LIKE N''%Đã duyệt%'' OR lyDoChenhLech LIKE N''%Chờ Quản lý duyệt%'');

UPDATE dbo.BangGiaoCa
SET trangThai = CASE
    WHEN gioKetThucThucTe IS NULL THEN ''DANG_MO''
    WHEN lyDoChenhLech LIKE N''%Đã duyệt%'' OR lyDoChenhLech LIKE N''✓%'' THEN ''DA_DUYET''
    ELSE ''CHO_DUYET'' END
WHERE lyDoChenhLech LIKE N''%Đã duyệt%''
   OR lyDoChenhLech LIKE N''✓%''
   OR lyDoChenhLech LIKE N''%Chờ Quản lý duyệt%''
   OR trangThai NOT IN (''DANG_MO'', ''CHO_DUYET'', ''DA_DUYET'');

DECLARE @maQuanLy NVARCHAR(20) = (
    SELECT TOP 1 nv.maNhanVien
    FROM dbo.NhanVien nv
    INNER JOIN dbo.TaiKhoan tk ON tk.tenDangNhap = nv.tenDangNhap
    WHERE tk.vaiTro = ''NVQL''
    ORDER BY nv.maNhanVien
);
UPDATE dbo.BangGiaoCa
SET maQuanLyDuyet = COALESCE(maQuanLyDuyet, @maQuanLy),
    thoiGianDuyet = COALESCE(thoiGianDuyet, gioKetThucThucTe)
WHERE trangThai = ''DA_DUYET''
  AND (maQuanLyDuyet IS NULL OR thoiGianDuyet IS NULL);

UPDATE dbo.BangGiaoCa
SET lyDoChenhLech = LTRIM(RTRIM(
    CASE WHEN CHARINDEX(N''|'', lyDoChenhLech) > 0
         THEN SUBSTRING(lyDoChenhLech, CHARINDEX(N''|'', lyDoChenhLech) + 1, 255)
         WHEN lyDoChenhLech LIKE N''%Chờ Quản lý duyệt%'' THEN N''''
         ELSE lyDoChenhLech END))
WHERE lyDoChenhLech LIKE N''%Đã duyệt%''
   OR lyDoChenhLech LIKE N''✓%''
   OR lyDoChenhLech LIKE N''%Chờ Quản lý duyệt%'';
');

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_GiaoCa_QuanLyDuyet')
    EXEC(N'ALTER TABLE dbo.BangGiaoCa ADD CONSTRAINT FK_GiaoCa_QuanLyDuyet
        FOREIGN KEY (maQuanLyDuyet) REFERENCES dbo.NhanVien(maNhanVien)');

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_BangGiaoCa_TrangThai')
    EXEC(N'ALTER TABLE dbo.BangGiaoCa ADD CONSTRAINT CK_BangGiaoCa_TrangThai
        CHECK (trangThai IN (''DANG_MO'', ''CHO_DUYET'', ''DA_DUYET''))');

COMMIT TRANSACTION;
GO

EXEC(N'SELECT maGiaoCa, tienThuTienMat, tienChuyenKhoan, tienHoanTra,
       trangThai, maQuanLyDuyet, thoiGianDuyet, lyDoChenhLech
FROM dbo.BangGiaoCa
ORDER BY gioKetThucThucTe DESC, maGiaoCa DESC');
GO
