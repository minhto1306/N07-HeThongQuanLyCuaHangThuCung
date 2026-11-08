-- Run after CapNhatRangBuocNghiepVu.sql, in the explicitly selected database.
-- Ambiguous legacy returns remain NULL and must be reconciled before another return.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF COL_LENGTH(N'dbo.ChiTietDoiTra',N'soLuongCoSoTra') IS NULL
    EXEC(N'ALTER TABLE dbo.ChiTietDoiTra ADD soLuongCoSoTra INT NULL;');

EXEC(N';WITH OriginalRatio AS (
 SELECT ct.maHoaDon,ct.maSanPham,
 MIN(CASE WHEN ct.soLuong>0 AND x.soLuongXuatCoSo%ct.soLuong=0
     THEN x.soLuongXuatCoSo/ct.soLuong ELSE 0 END) minRatio,
 MAX(CASE WHEN ct.soLuong>0 AND x.soLuongXuatCoSo%ct.soLuong=0
     THEN x.soLuongXuatCoSo/ct.soLuong ELSE 0 END) maxRatio
 FROM dbo.ChiTietHoaDon ct JOIN dbo.ChiTietXuatLo x ON x.maChiTiet=ct.maChiTiet
 GROUP BY ct.maHoaDon,ct.maSanPham
)
UPDATE d SET soLuongCoSoTra=CONVERT(INT,CAST(d.soLuongDoiTra AS BIGINT)*o.minRatio)
FROM dbo.ChiTietDoiTra d JOIN dbo.PhieuDoiTra p ON p.maPhieuDoiTra=d.maPhieuDoiTra
JOIN OriginalRatio o ON o.maHoaDon=p.maHoaDonGoc AND o.maSanPham=d.maSanPham
WHERE d.soLuongCoSoTra IS NULL AND o.minRatio=o.maxRatio AND o.minRatio>0
 AND CAST(d.soLuongDoiTra AS BIGINT)*o.minRatio BETWEEN 1 AND 2147483647
 AND COALESCE(d.soLuongCoSoHoan,0)<=CAST(d.soLuongDoiTra AS BIGINT)*o.minRatio;');

IF NOT EXISTS(SELECT 1 FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID(N'dbo.ChiTietDoiTra') AND name=N'CK_DoiTra_SoLuongCoSoTra')
    EXEC(N'ALTER TABLE dbo.ChiTietDoiTra WITH CHECK ADD CONSTRAINT CK_DoiTra_SoLuongCoSoTra CHECK(soLuongCoSoTra IS NULL OR soLuongCoSoTra>0);');
COMMIT TRANSACTION;
GO
