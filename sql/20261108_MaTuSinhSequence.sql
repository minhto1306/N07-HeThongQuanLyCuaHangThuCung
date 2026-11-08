-- Migration: Bảng quản lý cấp phát số thứ tự mã tự sinh tập trung (Idempotent)
IF OBJECT_ID(N'dbo.MaTuSinhSequence', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.MaTuSinhSequence (
        prefix NVARCHAR(80) NOT NULL,
        giaTri BIGINT NOT NULL,
        CONSTRAINT PK_MaTuSinhSequence PRIMARY KEY (prefix),
        CONSTRAINT CK_MaTuSinhSequence_GiaTri CHECK (giaTri >= 0)
    );
END;
