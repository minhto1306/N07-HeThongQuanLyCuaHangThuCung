/*
  Chạy một lần trên CSDL đã tồn tại.
  Script không tự xóa dữ liệu trùng: nếu phát hiện trùng, script dừng để Quản lý
  kiểm tra và chọn đúng bản ghi cần giữ, tránh mất lịch sử nghiệp vụ.
*/
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF EXISTS (
    SELECT 1
    FROM BangPhanCong
    GROUP BY maNhanVien, ngayLamViec, maCa
    HAVING COUNT(*) > 1
)
    THROW 51001, N'BangPhanCong đang có dữ liệu trùng nhân viên/ngày/ca. Hãy xử lý bản ghi trùng trước khi tạo ràng buộc.', 1;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.BangPhanCong')
      AND name = N'UQ_BangPhanCong_NhanVien_Ngay_Ca'
)
    CREATE UNIQUE INDEX UQ_BangPhanCong_NhanVien_Ngay_Ca
        ON dbo.BangPhanCong(maNhanVien, ngayLamViec, maCa);

IF COL_LENGTH(N'dbo.BangGiaoCa', N'ngayGiaoCa') IS NULL
    EXEC(N'ALTER TABLE dbo.BangGiaoCa ADD ngayGiaoCa AS CONVERT(DATE, gioKetThucThucTe) PERSISTED');

IF COL_LENGTH(N'dbo.BangGiaoCa', N'gioBatDauCa') IS NULL
    ALTER TABLE dbo.BangGiaoCa ADD gioBatDauCa DATETIME2 NULL;

IF EXISTS (
    SELECT 1
    FROM dbo.BangGiaoCa
    WHERE gioKetThucThucTe IS NOT NULL
    GROUP BY maNhanVien, CONVERT(DATE, gioKetThucThucTe), maCa
    HAVING COUNT(*) > 1
)
    THROW 51002, N'BangGiaoCa đang có nhiều biên bản cho cùng nhân viên/ngày/ca. Hãy đối soát trước khi tạo ràng buộc.', 1;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.BangGiaoCa')
      AND name = N'UQ_BangGiaoCa_NhanVien_Ngay_Ca'
)
    EXEC(N'CREATE UNIQUE INDEX UQ_BangGiaoCa_NhanVien_Ngay_Ca
        ON dbo.BangGiaoCa(maNhanVien, ngayGiaoCa, maCa)
        WHERE gioKetThucThucTe IS NOT NULL;');

IF COL_LENGTH(N'dbo.ChiTietDoiTra', N'soLo') IS NULL
    EXEC(N'ALTER TABLE dbo.ChiTietDoiTra ADD soLo NVARCHAR(50) NULL;');

IF COL_LENGTH(N'dbo.ChiTietDoiTra', N'soLuongCoSoHoan') IS NULL
    EXEC(N'ALTER TABLE dbo.ChiTietDoiTra ADD soLuongCoSoHoan INT NOT NULL CONSTRAINT DF_CTDoiTra_SoLuongCoSoHoan DEFAULT 0;');

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_CTDoiTra_LoHang')
    EXEC(N'ALTER TABLE dbo.ChiTietDoiTra ADD CONSTRAINT FK_CTDoiTra_LoHang FOREIGN KEY (soLo) REFERENCES dbo.LoHang(soLo);');

IF COL_LENGTH(N'dbo.LichSuLuuTru', N'maHoaDon') IS NULL
    EXEC(N'ALTER TABLE dbo.LichSuLuuTru ADD maHoaDon NVARCHAR(20) NULL;');

IF COL_LENGTH(N'dbo.LichSuLuuTru', N'trangThaiThanhToan') IS NULL
    EXEC(N'ALTER TABLE dbo.LichSuLuuTru ADD trangThaiThanhToan NVARCHAR(30) NOT NULL CONSTRAINT DF_LichSuLuuTru_ThanhToan DEFAULT ''CHO_THANH_TOAN'';');

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_LichSuLuuTru_HoaDon')
    EXEC(N'ALTER TABLE dbo.LichSuLuuTru ADD CONSTRAINT FK_LichSuLuuTru_HoaDon FOREIGN KEY (maHoaDon) REFERENCES dbo.HoaDon(maHoaDon);');

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.LichSuLuuTru') AND name=N'UQ_LichSuLuuTru_HoaDon')
    EXEC(N'CREATE UNIQUE INDEX UQ_LichSuLuuTru_HoaDon ON dbo.LichSuLuuTru(maHoaDon) WHERE maHoaDon IS NOT NULL;');

IF COL_LENGTH(N'dbo.TheSpaPass', N'maHoaDonKichHoat') IS NULL
    EXEC(N'ALTER TABLE dbo.TheSpaPass ADD maHoaDonKichHoat NVARCHAR(20) NULL;');

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_TheSpaPass_HoaDonKichHoat')
    EXEC(N'ALTER TABLE dbo.TheSpaPass ADD CONSTRAINT FK_TheSpaPass_HoaDonKichHoat FOREIGN KEY (maHoaDonKichHoat) REFERENCES dbo.HoaDon(maHoaDon);');

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.TheSpaPass') AND name=N'UQ_TheSpaPass_HoaDonKichHoat')
    EXEC(N'CREATE UNIQUE INDEX UQ_TheSpaPass_HoaDonKichHoat
        ON dbo.TheSpaPass(maHoaDonKichHoat) WHERE maHoaDonKichHoat IS NOT NULL;');

IF OBJECT_ID(N'dbo.LichSuSuDungSpaPass', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.LichSuSuDungSpaPass (
        maSuDung BIGINT IDENTITY(1,1) PRIMARY KEY,
        maThe NVARCHAR(50) NOT NULL,
        maHoaDon NVARCHAR(20) NOT NULL,
        maNhanVien NVARCHAR(20) NOT NULL,
        thoiGianSuDung DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        ghiChu NVARCHAR(255),
        CONSTRAINT FK_LSSpaPass_The FOREIGN KEY (maThe) REFERENCES dbo.TheSpaPass(maThe),
        CONSTRAINT FK_LSSpaPass_HoaDon FOREIGN KEY (maHoaDon) REFERENCES dbo.HoaDon(maHoaDon),
        CONSTRAINT FK_LSSpaPass_NhanVien FOREIGN KEY (maNhanVien) REFERENCES dbo.NhanVien(maNhanVien),
        CONSTRAINT UQ_LSSpaPass_HoaDon UNIQUE (maHoaDon)
    );
END;

COMMIT TRANSACTION;
GO
