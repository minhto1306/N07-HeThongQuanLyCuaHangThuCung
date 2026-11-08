-- Run after CapNhatRangBuocNghiepVu.sql in the explicitly selected database.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF EXISTS(SELECT 1 FROM dbo.LichSuLuuTru WHERE maHoaDon IS NOT NULL GROUP BY maHoaDon,maPhong HAVING COUNT(*)>1)
    THROW 51101,N'Lịch sử Hotel trùng hóa đơn/phòng; cần đối soát trước khi migration.',1;

IF EXISTS(SELECT 1 FROM sys.indexes i WHERE i.object_id=OBJECT_ID(N'dbo.LichSuLuuTru') AND i.name=N'UQ_LichSuLuuTru_HoaDon'
 AND (SELECT COUNT(*) FROM sys.index_columns ic WHERE ic.object_id=i.object_id AND ic.index_id=i.index_id AND ic.key_ordinal>0)=1)
    DROP INDEX UQ_LichSuLuuTru_HoaDon ON dbo.LichSuLuuTru;

IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.LichSuLuuTru') AND name=N'UQ_LichSuLuuTru_HoaDon')
    CREATE UNIQUE INDEX UQ_LichSuLuuTru_HoaDon ON dbo.LichSuLuuTru(maHoaDon,maPhong) WHERE maHoaDon IS NOT NULL;

IF COL_LENGTH(N'dbo.LichSuSuDungSpaPass',N'soLuot') IS NULL
    EXEC(N'ALTER TABLE dbo.LichSuSuDungSpaPass ADD soLuot INT NOT NULL CONSTRAINT DF_LSSpaPass_SoLuot DEFAULT 1 WITH VALUES;');
IF NOT EXISTS(SELECT 1 FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID(N'dbo.LichSuSuDungSpaPass') AND name=N'CK_LSSpaPass_SoLuot')
    EXEC(N'ALTER TABLE dbo.LichSuSuDungSpaPass WITH CHECK ADD CONSTRAINT CK_LSSpaPass_SoLuot CHECK(soLuot>0);');
COMMIT TRANSACTION;
GO
