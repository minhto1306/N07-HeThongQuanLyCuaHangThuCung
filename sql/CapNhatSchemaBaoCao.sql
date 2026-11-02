USE QuanLyCuaHangThuCung;
GO

-- Bổ sung mốc nhập kho để báo cáo Xuất - Nhập - Tồn có thể lọc đúng theo kỳ.
-- Dữ liệu cũ được ước định mốc nhập theo ngày sản xuất vì schema cũ không lưu ngày nhập.
IF COL_LENGTH('dbo.LoHang', 'ngayNhap') IS NULL
BEGIN
    ALTER TABLE dbo.LoHang ADD ngayNhap DATETIME2 NULL;

    EXEC sys.sp_executesql N'
        UPDATE dbo.LoHang
           SET ngayNhap = COALESCE(CAST(ngaySanXuat AS DATETIME2), SYSDATETIME())
         WHERE ngayNhap IS NULL;';

    EXEC sys.sp_executesql N'ALTER TABLE dbo.LoHang ALTER COLUMN ngayNhap DATETIME2 NOT NULL;';
    EXEC sys.sp_executesql N'ALTER TABLE dbo.LoHang ADD CONSTRAINT DF_LoHang_NgayNhap DEFAULT SYSDATETIME() FOR ngayNhap;';
END;
GO

SELECT soLo, maSanPham, ngayNhap, soLuongNhap, soLuongTon
FROM dbo.LoHang
ORDER BY ngayNhap, soLo;
GO
