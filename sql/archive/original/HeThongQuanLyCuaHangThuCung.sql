-- ======================================================================================
-- HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG MIO (PET SHOP MANAGEMENT SYSTEM) - NHÓM 07
-- TẬP TIN HỢP NHẤT TOÀN DIỆN CƠ SỞ DỮ LIỆU & BỘ DỮ LIỆU MẪU ĐỒNG BỘ
-- File: HeThongQuanLyThuCung.sql
-- 
-- CƠ CHẾ THỰC THI 2 GIAI ĐOẠN QUA SESSION_CONTEXT:
-- 1. CHẾ ĐỘ 'SCHEMA_ONLY' (Mặc định - An toàn cho Production / Cài đặt mới):
--    Chỉ tạo 30 bảng vật lý, khóa chính, khóa ngoại, ràng buộc CHECK và cấu hình chuẩn
--    (GoiSpaPass, BangCapSo). Không nạp dữ liệu giao dịch hay tài khoản thử nghiệm.
-- 
-- 2. CHẾ ĐỘ 'DEMO_DATA' (Dành riêng cho môi trường DEV / TEST):
--    Nạp đầy đủ bộ dữ liệu mẫu thực tế, đồng bộ toàn diện giữa Kho, FEFO, Bán hàng,
--    Đổi trả, Lịch hẹn, Spa Pass, Pet Hotel, Phân công và Bàn giao ca.
-- 
-- HƯỚNG DẪN CHỌN CHẾ ĐỘ KHI CHẠY TRONG SSMS / SQLCMD:
--   * Người vận hành phải tự tạo/chọn database đích và chạy trong CÙNG MỘT SESSION:
--       EXEC sp_set_session_context N'XacNhanDatabase', N'<đúng DB_NAME hiện tại>';
--       EXEC sp_set_session_context N'CheDo', N'SCHEMA_ONLY'; -- hoặc DEMO_DATA
--   * DEMO_DATA còn yêu cầu XacNhanDemo = PETSHOP_MIO_DEMO_FRESH_ONLY, database Dev/Test
--     đúng tên cho phép, NV001 đã bootstrap bằng PBKDF2 và database nghiệp vụ còn trống.
-- 
-- LỚP BẢO VỆ AN TOÀN (SAFETY GUARDS):
--   1. Script không CREATE/USE/DROP database và từ chối các database hệ thống.
--   2. XacNhanDatabase phải khớp tuyệt đối DB_NAME() trong cùng session.
--   3. DEMO_DATA chỉ chạy trên QuanLyCuaHangThuCung_Test hoặc QuanLyCuaHangThuCung_Dev,
--      có token xác nhận và từ chối database đã có dữ liệu nghiệp vụ.
-- ======================================================================================

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

-- DÒNG CẤU HÌNH CHẾ ĐỘ (Mặc định: SCHEMA_ONLY. Đổi thành DEMO_DATA khi cần nạp dữ liệu thử nghiệm):
-- EXEC sp_set_session_context N'CheDo', N'DEMO_DATA';
IF SESSION_CONTEXT(N'CheDo') IS NULL
BEGIN
    EXEC sp_set_session_context N'CheDo', N'SCHEMA_ONLY';
END
GO

-- Không tự CREATE/USE database: người vận hành phải chọn và xác nhận đúng đích.
IF DB_NAME() IN ('master', 'tempdb', 'model', 'msdb')
    THROW 50000, N'Phải tự tạo/chọn database đích trước khi chạy file.', 1;
IF CAST(SESSION_CONTEXT(N'XacNhanDatabase') AS NVARCHAR(128)) <> DB_NAME()
    THROW 50001, N'Chưa xác nhận đúng database đích bằng SESSION_CONTEXT XacNhanDatabase.', 1;
IF CAST(SESSION_CONTEXT(N'CheDo') AS NVARCHAR(50)) NOT IN (N'SCHEMA_ONLY', N'DEMO_DATA')
    THROW 50002, N'CheDo chỉ được là SCHEMA_ONLY hoặc DEMO_DATA.', 1;

-- Preflight phải hoàn tất trước toàn bộ DDL/DML phía dưới.
IF CAST(SESSION_CONTEXT(N'CheDo') AS NVARCHAR(50)) = N'SCHEMA_ONLY'
   AND EXISTS (SELECT 1 FROM sys.tables WHERE is_ms_shipped = 0)
    THROW 50003, N'SCHEMA_ONLY chỉ dành cho database fresh-install chưa có bảng người dùng.', 1;

IF CAST(SESSION_CONTEXT(N'CheDo') AS NVARCHAR(50)) = N'DEMO_DATA'
BEGIN
    IF DB_NAME() NOT IN (N'QuanLyCuaHangThuCung_Test', N'QuanLyCuaHangThuCung_Dev')
        THROW 50004, N'DEMO_DATA chỉ được chạy trên database Test/Dev đúng tên cho phép.', 1;
    IF CAST(SESSION_CONTEXT(N'XacNhanDemo') AS NVARCHAR(100)) <> N'PETSHOP_MIO_DEMO_FRESH_ONLY'
        THROW 50005, N'Thiếu token XacNhanDemo cho bộ dữ liệu fresh-only.', 1;
    IF (SELECT COUNT(*) FROM sys.tables
        WHERE is_ms_shipped = 0 AND name IN (
            N'TaiKhoan', N'NhanVien', N'CaLamViec', N'BangPhanCong', N'BangGiaoCa',
            N'KhachHang', N'ThuCung', N'HoSoThuCung', N'HoSoSucKhoe', N'DichVu',
            N'LichHen', N'ChiTietLichHen', N'DanhMucHang', N'SanPham', N'DonViTinh',
            N'QuyDoiDonVi', N'NhaCungCap', N'LoHang', N'HoaDon', N'ChiTietHoaDon',
            N'ChiTietXuatLo', N'ChiTietDichVu', N'PhieuDoiTra', N'ChiTietDoiTra',
            N'PhongLuuTru', N'TheSpaPass', N'LichSuSuDungSpaPass', N'LichSuLuuTru',
            N'GoiSpaPass', N'BangCapSo'
        )) <> 30
        THROW 50006, N'DEMO_DATA yêu cầu schema chuẩn đủ 30 bảng; hãy chạy SCHEMA_ONLY trên database mới trước.', 1;
END
GO

PRINT N'======================================================================================';
PRINT N'HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG MIO - THỰC THI SCRIPT CSDL';
PRINT N'DATABASE ĐANG KẾT NỐI: [' + DB_NAME() + N']';
PRINT N'CHẾ ĐỘ THỰC THI HIỆN TẠI: [' + CAST(ISNULL(SESSION_CONTEXT(N'CheDo'), N'SCHEMA_ONLY') AS NVARCHAR(50)) + N']';
PRINT N'======================================================================================';
GO

-- ======================================================================================
-- PHẦN I: KHỞI TẠO CẤU TRÚC 30 BẢNG VÀ RÀNG BUỘC (SCHEMA DEFINITION)
-- ======================================================================================

-- 1. Bảng Tài khoản người dùng (TC02: Mở rộng matKhau lên 256 ký tự cho chuẩn hash PBKDF2-SHA256)
IF OBJECT_ID(N'dbo.TaiKhoan', N'U') IS NULL
BEGIN
    CREATE TABLE TaiKhoan (
        maTaiKhoan NVARCHAR(20) PRIMARY KEY,
        soDienThoai NVARCHAR(15) NOT NULL UNIQUE,
        email NVARCHAR(100) NOT NULL UNIQUE,
        matKhau NVARCHAR(256) NOT NULL,
        vaiTro NVARCHAR(20) NOT NULL CHECK (vaiTro IN ('NVQL', 'NVTN', 'NVBH', 'NVCS')),
        trangThaiHoatDong BIT NOT NULL DEFAULT 1
    );
END
GO

-- 2. Bảng Nhân viên
IF OBJECT_ID(N'dbo.NhanVien', N'U') IS NULL
BEGIN
    CREATE TABLE NhanVien (
        maNhanVien NVARCHAR(20) PRIMARY KEY,
        hoTen NVARCHAR(100) NOT NULL,
        chucVu NVARCHAR(50) NOT NULL,
        soDienThoai NVARCHAR(15) NOT NULL UNIQUE,
        email NVARCHAR(100) NOT NULL,
        maTaiKhoan NVARCHAR(20) NOT NULL UNIQUE,
        CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (maTaiKhoan) REFERENCES TaiKhoan(maTaiKhoan) ON DELETE CASCADE
    );
END
GO

-- 3. Bảng Ca làm việc
IF OBJECT_ID(N'dbo.CaLamViec', N'U') IS NULL
BEGIN
    CREATE TABLE CaLamViec (
        maCa NVARCHAR(20) PRIMARY KEY,
        tenCa NVARCHAR(50) NOT NULL,
        gioBatDau TIME NOT NULL,
        gioKetThuc TIME NOT NULL
    );
END
GO

-- 4. Bảng Phân công ca trực
IF OBJECT_ID(N'dbo.BangPhanCong', N'U') IS NULL
BEGIN
    CREATE TABLE BangPhanCong (
        maPhanCong INT IDENTITY(1,1) PRIMARY KEY,
        maNhanVien NVARCHAR(20) NOT NULL,
        maCa NVARCHAR(20) NOT NULL,
        ngayLamViec DATE NOT NULL,
        trangThaiCa NVARCHAR(50) DEFAULT N'Đã phân công',
        ghiChu NVARCHAR(255) NULL,
        CONSTRAINT FK_PhanCong_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
        CONSTRAINT FK_PhanCong_CaLamViec FOREIGN KEY (maCa) REFERENCES CaLamViec(maCa),
        CONSTRAINT UQ_BangPhanCong_NhanVien_Ngay_Ca UNIQUE (maNhanVien, ngayLamViec, maCa)
    );
END
GO

-- 5. Bảng Bàn giao ca thu ngân & Két tiền (TC10: Phân tách tiền mặt, chuyển khoản, duyệt ca)
IF OBJECT_ID(N'dbo.BangGiaoCa', N'U') IS NULL
BEGIN
    CREATE TABLE BangGiaoCa (
        maGiaoCa NVARCHAR(20) PRIMARY KEY,
        maNhanVien NVARCHAR(20) NOT NULL,
        maCa NVARCHAR(20) NOT NULL,
        tienDauCa DECIMAL(18,2) NOT NULL,
        tienCuoiCaHeThong DECIMAL(18,2) NOT NULL,
        tienCuoiCaThucTe DECIMAL(18,2) NOT NULL,
        tienChenhLech DECIMAL(18,2) NOT NULL,
        tienThuTienMat DECIMAL(18,2) NOT NULL DEFAULT 0,
        tienChuyenKhoan DECIMAL(18,2) NOT NULL DEFAULT 0,
        tienHoanTra DECIMAL(18,2) NOT NULL DEFAULT 0,
        lyDoChenhLech NVARCHAR(255),
        gioBatDauCa DATETIME2 NULL,
        gioKetThucThucTe DATETIME2,
        ngayGiaoCa AS CONVERT(DATE, gioKetThucThucTe) PERSISTED,
        trangThai NVARCHAR(20) NOT NULL DEFAULT 'CHO_DUYET',
        maQuanLyDuyet NVARCHAR(20) NULL,
        thoiGianDuyet DATETIME2 NULL,
        CONSTRAINT FK_GiaoCa_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
        CONSTRAINT FK_GiaoCa_CaLamViec FOREIGN KEY (maCa) REFERENCES CaLamViec(maCa),
        CONSTRAINT FK_GiaoCa_QuanLyDuyet FOREIGN KEY (maQuanLyDuyet) REFERENCES NhanVien(maNhanVien),
        CONSTRAINT CK_BangGiaoCa_TrangThai CHECK (trangThai IN ('DANG_MO', 'CHO_DUYET', 'DA_DUYET')),
        CONSTRAINT CK_BangGiaoCa_SoTienKhongAm CHECK (
            tienDauCa >= 0 AND tienCuoiCaHeThong >= 0 AND tienCuoiCaThucTe >= 0
            AND tienThuTienMat >= 0 AND tienChuyenKhoan >= 0 AND tienHoanTra >= 0
        ),
        CONSTRAINT CK_BangGiaoCa_ThongTinDuyet CHECK (
            (trangThai = 'DA_DUYET' AND maQuanLyDuyet IS NOT NULL AND thoiGianDuyet IS NOT NULL)
            OR (trangThai <> 'DA_DUYET' AND maQuanLyDuyet IS NULL AND thoiGianDuyet IS NULL)
        )
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.BangGiaoCa') AND name = N'UQ_BangGiaoCa_NhanVien_Ngay_Ca')
BEGIN
    CREATE UNIQUE INDEX UQ_BangGiaoCa_NhanVien_Ngay_Ca
    ON BangGiaoCa(maNhanVien, ngayGiaoCa, maCa)
    WHERE gioKetThucThucTe IS NOT NULL;
END
GO

-- 6. Bảng Khách hàng thành viên
IF OBJECT_ID(N'dbo.KhachHang', N'U') IS NULL
BEGIN
    CREATE TABLE KhachHang (
        maKhachHang NVARCHAR(20) PRIMARY KEY,
        hoTen NVARCHAR(100) NOT NULL,
        soDienThoai NVARCHAR(15) NOT NULL UNIQUE,
        diemTichLuy INT NOT NULL DEFAULT 0 CHECK (diemTichLuy >= 0),
        email NVARCHAR(100) NULL,
        diaChi NVARCHAR(255) NULL
    );
END
GO

-- 7. Bảng Thú cưng
IF OBJECT_ID(N'dbo.ThuCung', N'U') IS NULL
BEGIN
    CREATE TABLE ThuCung (
        maThuCung NVARCHAR(20) PRIMARY KEY,
        tenThuCung NVARCHAR(100) NOT NULL,
        giongLoai NVARCHAR(50),
        ngaySinh DATE,
        gioiTinh NVARCHAR(10)
    );
END
GO

-- 8. Bảng Hồ sơ thú cưng & Định danh Microchip
IF OBJECT_ID(N'dbo.HoSoThuCung', N'U') IS NULL
BEGIN
    CREATE TABLE HoSoThuCung (
        maHoSo NVARCHAR(20) PRIMARY KEY,
        maKhachHang NVARCHAR(20) NOT NULL,
        maThuCung NVARCHAR(20) NOT NULL,
        soMicrochip NVARCHAR(50),
        ngayDangKy DATE NOT NULL,
        trangThaiNuoi NVARCHAR(50) DEFAULT N'Đang nuôi',
        CONSTRAINT FK_HoSo_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
        CONSTRAINT FK_HoSo_ThuCung FOREIGN KEY (maThuCung) REFERENCES ThuCung(maThuCung)
    );
END
GO

-- 9. Bảng Sổ theo dõi sức khỏe & Thể trạng y tế thú cưng
IF OBJECT_ID(N'dbo.HoSoSucKhoe', N'U') IS NULL
BEGIN
    CREATE TABLE HoSoSucKhoe (
        maSucKhoe NVARCHAR(20) PRIMARY KEY,
        maThuCung NVARCHAR(20) NOT NULL,
        maNhanVienKiemTra NVARCHAR(20) NOT NULL,
        canNang DECIMAL(6,2),
        nhietDo DECIMAL(4,2),
        tinhTrangVeRan NVARCHAR(100),
        tiemPhongDai BIT NOT NULL DEFAULT 0,
        ghiChuTheTrang NVARCHAR(255),
        ngayKiemTra DATETIME2 NOT NULL,
        CONSTRAINT FK_SucKhoe_ThuCung FOREIGN KEY (maThuCung) REFERENCES ThuCung(maThuCung),
        CONSTRAINT FK_SucKhoe_NhanVien FOREIGN KEY (maNhanVienKiemTra) REFERENCES NhanVien(maNhanVien)
    );
END
GO

-- 10. Bảng Danh mục Dịch vụ Spa & Grooming (DV08: Thời lượng phút chuẩn cho thuật toán kiểm tra trùng lịch)
IF OBJECT_ID(N'dbo.DichVu', N'U') IS NULL
BEGIN
    CREATE TABLE DichVu (
        maDichVu NVARCHAR(20) PRIMARY KEY,
        tenDichVu NVARCHAR(100) NOT NULL,
        giaDichVu DECIMAL(18,2) NOT NULL CHECK (giaDichVu >= 0),
        thoiLuongPhut INT NOT NULL CHECK (thoiLuongPhut > 0)
    );
END
GO

-- 11. Bảng Lịch hẹn dịch vụ
IF OBJECT_ID(N'dbo.LichHen', N'U') IS NULL
BEGIN
    CREATE TABLE LichHen (
        maLichHen NVARCHAR(20) PRIMARY KEY,
        maKhachHang NVARCHAR(20) NOT NULL,
        maNhanVienTiepNhan NVARCHAR(20),
        ngayHen DATETIME2 NOT NULL,
        trangThaiLichHen NVARCHAR(50) DEFAULT N'Đã đặt',
        ghiChu NVARCHAR(255),
        CONSTRAINT FK_LichHen_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
        CONSTRAINT FK_LichHen_NhanVien FOREIGN KEY (maNhanVienTiepNhan) REFERENCES NhanVien(maNhanVien)
    );
END
GO

-- 12. Bảng Chi tiết lịch hẹn (Phân công Kỹ thuật viên & Thú cưng)
IF OBJECT_ID(N'dbo.ChiTietLichHen', N'U') IS NULL
BEGIN
    CREATE TABLE ChiTietLichHen (
        maChiTietLichHen NVARCHAR(20) PRIMARY KEY,
        maLichHen NVARCHAR(20) NOT NULL,
        maThuCung NVARCHAR(20) NOT NULL,
        maDichVu NVARCHAR(20) NOT NULL,
        maKtvChiDinh NVARCHAR(20),
        CONSTRAINT FK_CTLichHen_LichHen FOREIGN KEY (maLichHen) REFERENCES LichHen(maLichHen),
        CONSTRAINT FK_CTLichHen_ThuCung FOREIGN KEY (maThuCung) REFERENCES ThuCung(maThuCung),
        CONSTRAINT FK_CTLichHen_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu),
        CONSTRAINT FK_CTLichHen_Ktv FOREIGN KEY (maKtvChiDinh) REFERENCES NhanVien(maNhanVien)
    );
END
GO

-- 13. Bảng Danh mục hàng hóa
IF OBJECT_ID(N'dbo.DanhMucHang', N'U') IS NULL
BEGIN
    CREATE TABLE DanhMucHang (
        maDanhMuc NVARCHAR(20) PRIMARY KEY,
        tenDanhMuc NVARCHAR(100) NOT NULL,
        moTa NVARCHAR(255)
    );
END
GO

-- 14. Bảng Sản phẩm
IF OBJECT_ID(N'dbo.SanPham', N'U') IS NULL
BEGIN
    CREATE TABLE SanPham (
        maSanPham NVARCHAR(20) PRIMARY KEY,
        maDanhMuc NVARCHAR(20) NOT NULL,
        tenSanPham NVARCHAR(100) NOT NULL,
        maVach NVARCHAR(30) NULL,
        donViCoSo NVARCHAR(30) NOT NULL DEFAULT N'Đơn vị',
        giaBanCoSo DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (giaBanCoSo >= 0),
        moTa NVARCHAR(255),
        CONSTRAINT FK_SanPham_DanhMuc FOREIGN KEY (maDanhMuc) REFERENCES DanhMucHang(maDanhMuc)
    );
    CREATE UNIQUE NONCLUSTERED INDEX UQ_SanPham_MaVach
        ON SanPham(maVach) WHERE maVach IS NOT NULL;
END
ELSE
BEGIN
    -- SQL Server coi hai NULL là trùng nhau trong UNIQUE constraint.
    -- Nâng cấp DB cũ trước khi tạo filtered index, không xóa dữ liệu sản phẩm.
    DECLARE @uqMaVachName sysname;
    SELECT TOP (1) @uqMaVachName = kc.name
    FROM sys.key_constraints kc
    JOIN sys.index_columns ic ON ic.object_id = kc.parent_object_id
        AND ic.index_id = kc.unique_index_id
    JOIN sys.columns col ON col.object_id = ic.object_id
        AND col.column_id = ic.column_id
    WHERE kc.parent_object_id = OBJECT_ID(N'dbo.SanPham')
        AND kc.type = 'UQ' AND col.name = N'maVach';
    IF @uqMaVachName IS NOT NULL
        EXEC(N'ALTER TABLE dbo.SanPham DROP CONSTRAINT ' + QUOTENAME(@uqMaVachName));
    IF NOT EXISTS (SELECT 1 FROM sys.indexes
                   WHERE object_id = OBJECT_ID(N'dbo.SanPham')
                     AND name = N'UQ_SanPham_MaVach')
        CREATE UNIQUE NONCLUSTERED INDEX UQ_SanPham_MaVach
            ON dbo.SanPham(maVach) WHERE maVach IS NOT NULL;
END
GO

-- 15. Bảng Đơn vị tính
IF OBJECT_ID(N'dbo.DonViTinh', N'U') IS NULL
BEGIN
    CREATE TABLE DonViTinh (
        maDonVi NVARCHAR(20) PRIMARY KEY,
        tenDonVi NVARCHAR(50) NOT NULL
    );
END
GO

-- 16. Bảng Quy đổi đơn vị tính đa cấp (BH01: Tỷ lệ quy đổi chuẩn không làm tròn sai số)
IF OBJECT_ID(N'dbo.QuyDoiDonVi', N'U') IS NULL
BEGIN
    CREATE TABLE QuyDoiDonVi (
        maQuyDoi NVARCHAR(20) PRIMARY KEY,
        maSanPham NVARCHAR(20) NOT NULL,
        maDonVi NVARCHAR(20) NOT NULL,
        tyLeQuyDoi INT NOT NULL CHECK (tyLeQuyDoi > 0),
        laDonViCoSo BIT NOT NULL DEFAULT 0,
        giaBan DECIMAL(18,2) NOT NULL CHECK (giaBan >= 0),
        CONSTRAINT FK_QuyDoi_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham),
        CONSTRAINT FK_QuyDoi_DonVi FOREIGN KEY (maDonVi) REFERENCES DonViTinh(maDonVi)
    );
END
GO

-- 17. Bảng Nhà cung cấp
IF OBJECT_ID(N'dbo.NhaCungCap', N'U') IS NULL
BEGIN
    CREATE TABLE NhaCungCap (
        maNhaCungCap NVARCHAR(20) PRIMARY KEY,
        tenNhaCungCap NVARCHAR(100) NOT NULL,
        diaChi NVARCHAR(255),
        soDienThoai NVARCHAR(20)
    );
END
GO

-- 18. Bảng Lô hàng (BH02: Quản lý hạn sử dụng theo thuật toán FEFO)
IF OBJECT_ID(N'dbo.LoHang', N'U') IS NULL
BEGIN
    CREATE TABLE LoHang (
        soLo NVARCHAR(50) PRIMARY KEY,
        maSanPham NVARCHAR(20) NOT NULL,
        maNhaCungCap NVARCHAR(20) NOT NULL,
        ngaySanXuat DATE NOT NULL,
        hanSuDung DATE NOT NULL,
        ngayNhap DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        donGiaNhapCoSo DECIMAL(18,2) NOT NULL CHECK (donGiaNhapCoSo >= 0),
        soLuongNhap INT NOT NULL CHECK (soLuongNhap >= 0),
        soLuongTon INT NOT NULL CHECK (soLuongTon >= 0),
        CONSTRAINT FK_LoHang_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham),
        CONSTRAINT FK_LoHang_NhaCungCap FOREIGN KEY (maNhaCungCap) REFERENCES NhaCungCap(maNhaCungCap)
    );
END
GO

-- 19. Bảng Hóa đơn bán hàng POS (Phase 2: Tách biệt giảm giá VIP, tiền đổi điểm và tiền bù trừ đổi trả)
IF OBJECT_ID(N'dbo.HoaDon', N'U') IS NULL
BEGIN
    CREATE TABLE HoaDon (
        maHoaDon NVARCHAR(20) PRIMARY KEY,
        maKhachHang NVARCHAR(20) NULL,
        maNhanVien NVARCHAR(20) NOT NULL,
        ngayLap DATETIME2 NOT NULL,
        thoiGianThanhToan DATETIME2,
        giamGia DECIMAL(18,2) NOT NULL DEFAULT 0,
        tienDoiDiem DECIMAL(18,2) NOT NULL DEFAULT 0,
        tienBuTruDoiTra DECIMAL(18,2) NOT NULL DEFAULT 0,
        thanhTien DECIMAL(18,2) NOT NULL,
        phuongThucThanhToan NVARCHAR(50),
        trangThaiHoaDon NVARCHAR(50) DEFAULT N'Đã thanh toán',
        CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
        CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
        CONSTRAINT CK_HoaDon_SoTienKhongAm CHECK (
            giamGia >= 0 AND tienDoiDiem >= 0 AND tienBuTruDoiTra >= 0 AND thanhTien >= 0
        )
    );
END
GO

-- 20. Bảng Chi tiết hóa đơn
IF OBJECT_ID(N'dbo.ChiTietHoaDon', N'U') IS NULL
BEGIN
    CREATE TABLE ChiTietHoaDon (
        maChiTiet NVARCHAR(50) PRIMARY KEY,
        maHoaDon NVARCHAR(20) NOT NULL,
        loaiChiTiet NVARCHAR(20) NOT NULL,
        maSanPham NVARCHAR(20) NULL,
        maDichVu NVARCHAR(20) NULL,
        soLuong INT NOT NULL CHECK (soLuong > 0),
        donGiaLucBan DECIMAL(18,2) NOT NULL CHECK (donGiaLucBan >= 0),
        thanhTien DECIMAL(18,2) NOT NULL CHECK (thanhTien >= 0),
        CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE,
        CONSTRAINT FK_CTHD_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham),
        CONSTRAINT FK_CTHD_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu)
    );
END
GO

-- 21. Bảng Chi tiết xuất kho theo lô hàng (FEFO Tracking)
IF OBJECT_ID(N'dbo.ChiTietXuatLo', N'U') IS NULL
BEGIN
    CREATE TABLE ChiTietXuatLo (
        maChiTietXuatLo INT IDENTITY(1,1) PRIMARY KEY,
        maChiTiet NVARCHAR(50) NOT NULL,
        soLo NVARCHAR(50) NOT NULL,
        maQuyDoi NVARCHAR(20) NULL,
        soLuongXuatCoSo INT NOT NULL CHECK (soLuongXuatCoSo > 0),
        CONSTRAINT FK_CTXuatLo_CTHD FOREIGN KEY (maChiTiet) REFERENCES ChiTietHoaDon(maChiTiet) ON DELETE CASCADE,
        CONSTRAINT FK_CTXuatLo_LoHang FOREIGN KEY (soLo) REFERENCES LoHang(soLo),
        CONSTRAINT FK_CTXuatLo_QuyDoi FOREIGN KEY (maQuyDoi) REFERENCES QuyDoiDonVi(maQuyDoi)
    );
END
GO

-- 22. Bảng Chi tiết dịch vụ thực hiện & Hoa hồng kỹ thuật viên (10%)
IF OBJECT_ID(N'dbo.ChiTietDichVu', N'U') IS NULL
BEGIN
    CREATE TABLE ChiTietDichVu (
        maChiTietDichVu INT IDENTITY(1,1) PRIMARY KEY,
        maChiTiet NVARCHAR(50) NOT NULL,
        maDichVu NVARCHAR(20) NOT NULL,
        maThuCung NVARCHAR(20),
        maKtvThucHien NVARCHAR(20),
        tienHoaHong DECIMAL(18,2) DEFAULT 0 CHECK (tienHoaHong >= 0),
        CONSTRAINT FK_CTDV_CTHD FOREIGN KEY (maChiTiet) REFERENCES ChiTietHoaDon(maChiTiet) ON DELETE CASCADE,
        CONSTRAINT FK_CTDV_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu),
        CONSTRAINT FK_CTDV_ThuCung FOREIGN KEY (maThuCung) REFERENCES ThuCung(maThuCung),
        CONSTRAINT FK_CTDV_Ktv FOREIGN KEY (maKtvThucHien) REFERENCES NhanVien(maNhanVien)
    );
END
GO

-- 23. Bảng Phiếu đổi trả hàng hóa (TC09, TC10: Bổ sung 5 trường hạch toán tài chính độc lập)
IF OBJECT_ID(N'dbo.PhieuDoiTra', N'U') IS NULL
BEGIN
    CREATE TABLE PhieuDoiTra (
        maPhieuDoiTra NVARCHAR(20) PRIMARY KEY,
        maHoaDonGoc NVARCHAR(20) NOT NULL,
        maNhanVienTiepNhan NVARCHAR(20) NOT NULL,
        ngayDoiTra DATETIME2 NOT NULL,
        tongTienHoan DECIMAL(18,2) NOT NULL DEFAULT 0,
        lyDoDoiTra NVARCHAR(255),
        trangThaiDoiTra NVARCHAR(50) DEFAULT N'Hoàn thành',
        giaTriHangTra DECIMAL(18,2) NOT NULL DEFAULT 0,
        giaTriHangDoi DECIMAL(18,2) NOT NULL DEFAULT 0,
        tienHoanThucTe DECIMAL(18,2) NOT NULL DEFAULT 0,
        tienKhachTraThem DECIMAL(18,2) NOT NULL DEFAULT 0,
        phuongThucHoanTien NVARCHAR(50) NOT NULL DEFAULT N'CHUA_XAC_DINH',
        CONSTRAINT FK_DoiTra_HoaDon FOREIGN KEY (maHoaDonGoc) REFERENCES HoaDon(maHoaDon),
        CONSTRAINT FK_DoiTra_NhanVien FOREIGN KEY (maNhanVienTiepNhan) REFERENCES NhanVien(maNhanVien),
        CONSTRAINT CK_PhieuDoiTra_SoTien CHECK (
            tongTienHoan >= 0 AND giaTriHangTra >= 0 AND giaTriHangDoi >= 0 
            AND tienHoanThucTe >= 0 AND tienKhachTraThem >= 0
        )
    );
END
GO

-- 24. Bảng Chi tiết đổi trả (TC05, TC06: Kiểm soát số lượng hoàn kho cơ sở và lô hàng hoàn)
IF OBJECT_ID(N'dbo.ChiTietDoiTra', N'U') IS NULL
BEGIN
    CREATE TABLE ChiTietDoiTra (
        maChiTietDoiTra NVARCHAR(50) PRIMARY KEY,
        maPhieuDoiTra NVARCHAR(20) NOT NULL,
        maSanPham NVARCHAR(20) NOT NULL,
        soLuongDoiTra INT NOT NULL DEFAULT 1 CHECK (soLuongDoiTra > 0),
        tienHoanLai DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tienHoanLai >= 0),
        soLo NVARCHAR(50) NULL,
        soLuongCoSoHoan INT NOT NULL DEFAULT 0 CHECK (soLuongCoSoHoan >= 0),
        soLuong INT NULL,
        donGiaHoan DECIMAL(18,2) NULL,
        lyDoChiTiet NVARCHAR(255),
        CONSTRAINT FK_CTDoiTra_Phieu FOREIGN KEY (maPhieuDoiTra) REFERENCES PhieuDoiTra(maPhieuDoiTra) ON DELETE CASCADE,
        CONSTRAINT FK_CTDoiTra_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham),
        CONSTRAINT FK_CTDoiTra_LoHang FOREIGN KEY (soLo) REFERENCES LoHang(soLo)
    );
END
GO

-- 25. Bảng Quản lý Khách sạn lưu trú thú cưng (TC13: Pet Hotel Boarding)
IF OBJECT_ID(N'dbo.PhongLuuTru', N'U') IS NULL
BEGIN
    CREATE TABLE PhongLuuTru (
        maPhong NVARCHAR(20) PRIMARY KEY,
        loaiPhong NVARCHAR(50) NOT NULL,
        donGiaNgay BIGINT NOT NULL CHECK (donGiaNgay >= 0),
        trangThai NVARCHAR(50) NOT NULL,
        tenThuCung NVARCHAR(100),
        giongLoai NVARCHAR(100),
        tenChu NVARCHAR(100),
        sdtChu NVARCHAR(20),
        thoiGianCheckIn DATETIME2,
        duKienCheckOut DATETIME2,
        ghiChuChamSoc NVARCHAR(255),
        daChoAnSang BIT NOT NULL DEFAULT 0,
        daChoAnToi BIT NOT NULL DEFAULT 0,
        daDiDaoVeSinh BIT NOT NULL DEFAULT 0,
        phiDichVuPhatSinh BIGINT NOT NULL DEFAULT 0 CHECK (phiDichVuPhatSinh >= 0)
    );
END
GO

-- 26. Bảng Thẻ Gói Spa Pass (DV13: Thẻ Punch Card định kỳ)
IF OBJECT_ID(N'dbo.TheSpaPass', N'U') IS NULL
BEGIN
    CREATE TABLE TheSpaPass (
        maThe NVARCHAR(50) PRIMARY KEY,
        sdtKhachHang NVARCHAR(15) NOT NULL,
        tenKhachHang NVARCHAR(100) NOT NULL,
        tenGoi NVARCHAR(100) NOT NULL,
        tongSoBuoi INT NOT NULL CHECK (tongSoBuoi > 0),
        soBuoiDaDung INT NOT NULL DEFAULT 0 CHECK (soBuoiDaDung >= 0),
        donGiaGoi DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (donGiaGoi >= 0),
        ngayKichHoat DATE NOT NULL,
        ngayHetHan DATE NOT NULL,
        trangThai NVARCHAR(50) NOT NULL DEFAULT N'ĐANG HOẠT ĐỘNG',
        maHoaDonKichHoat NVARCHAR(20) NULL,
        CONSTRAINT FK_TheSpaPass_HoaDonKichHoat FOREIGN KEY (maHoaDonKichHoat) REFERENCES HoaDon(maHoaDon)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.TheSpaPass') AND name = N'UQ_TheSpaPass_HoaDonKichHoat')
BEGIN
    CREATE UNIQUE INDEX UQ_TheSpaPass_HoaDonKichHoat
    ON TheSpaPass(maHoaDonKichHoat) WHERE maHoaDonKichHoat IS NOT NULL;
END
GO

-- 27. Bảng Lịch sử sử dụng Spa Pass
IF OBJECT_ID(N'dbo.LichSuSuDungSpaPass', N'U') IS NULL
BEGIN
    CREATE TABLE LichSuSuDungSpaPass (
        maSuDung BIGINT IDENTITY(1,1) PRIMARY KEY,
        maThe NVARCHAR(50) NOT NULL,
        maHoaDon NVARCHAR(20) NOT NULL,
        maNhanVien NVARCHAR(20) NOT NULL,
        thoiGianSuDung DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        ghiChu NVARCHAR(255),
        CONSTRAINT FK_LSSpaPass_The FOREIGN KEY (maThe) REFERENCES TheSpaPass(maThe),
        CONSTRAINT FK_LSSpaPass_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
        CONSTRAINT FK_LSSpaPass_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
        CONSTRAINT UQ_LSSpaPass_HoaDon UNIQUE (maHoaDon)
    );
END
GO

-- 28. Bảng Lịch sử lưu trú Khách sạn thú cưng (TC13: Pet Hotel Checkout History)
IF OBJECT_ID(N'dbo.LichSuLuuTru', N'U') IS NULL
BEGIN
    CREATE TABLE LichSuLuuTru (
        maLuuTru INT IDENTITY(1,1) PRIMARY KEY,
        maPhong NVARCHAR(20) NOT NULL,
        tenThuCung NVARCHAR(100),
        tenChu NVARCHAR(100),
        sdtChu NVARCHAR(20),
        thoiGianCheckIn DATETIME2,
        thoiGianCheckOut DATETIME2,
        soNgayLuuTru INT CHECK (soNgayLuuTru >= 0),
        donGiaNgay DECIMAL(18,2) CHECK (donGiaNgay >= 0),
        phiPhatSinh DECIMAL(18,2) CHECK (phiPhatSinh >= 0),
        tongTien DECIMAL(18,2) CHECK (tongTien >= 0),
        ghiChu NVARCHAR(500),
        maHoaDon NVARCHAR(20) NULL,
        trangThaiThanhToan NVARCHAR(30) NOT NULL DEFAULT 'CHO_THANH_TOAN',
        CONSTRAINT FK_LichSuLuuTru_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
        CONSTRAINT CK_LichSuLuuTru_ThanhToan CHECK (trangThaiThanhToan IN ('CHO_THANH_TOAN','DA_THANH_TOAN','DA_HUY'))
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.LichSuLuuTru') AND name = N'UQ_LichSuLuuTru_HoaDon')
BEGIN
    CREATE UNIQUE INDEX UQ_LichSuLuuTru_HoaDon
    ON LichSuLuuTru(maHoaDon) WHERE maHoaDon IS NOT NULL;
END
GO

-- 29. Bảng Cấu hình danh mục Gói Spa Pass (DV14: Loại bỏ hoàn toàn Regex cắt chuỗi tại quầy POS)
IF OBJECT_ID(N'dbo.GoiSpaPass', N'U') IS NULL
BEGIN
    CREATE TABLE GoiSpaPass (
        maGoi NVARCHAR(50) PRIMARY KEY,
        tenGoi NVARCHAR(255) NOT NULL,
        soBuoi INT NOT NULL CHECK (soBuoi > 0),
        donGiaGoi DECIMAL(18,2) NOT NULL CHECK (donGiaGoi >= 0),
        thoiHanNgay INT NOT NULL CHECK (thoiHanNgay > 0),
        trangThaiHoatDong BIT NOT NULL DEFAULT 1
    );
END
GO

-- 30. Bảng Cấp số thứ tự tập trung chống trùng mã (BH03: Atomic Concurrency)
IF OBJECT_ID(N'dbo.BangCapSo', N'U') IS NULL
BEGIN
    CREATE TABLE BangCapSo (
        loaiMa NVARCHAR(50) PRIMARY KEY,
        soHienTai INT NOT NULL DEFAULT 0,
        moTa NVARCHAR(255)
    );
END
GO

-- ======================================================================================
-- PHẦN II: DỮ LIỆU CẤU HÌNH CƠ BẢN CẦN THIẾT (SYSTEM CONFIGURATION DATA)
-- (Chỉ nạp danh mục gói dịch vụ và bảng cấp số, không chứa thông tin cá nhân hay giao dịch)
-- ======================================================================================

-- Cấu hình mặc định cho GoiSpaPass
IF NOT EXISTS (SELECT 1 FROM GoiSpaPass WHERE maGoi = 'GOI_SPA10')
    INSERT INTO GoiSpaPass (maGoi, tenGoi, soBuoi, donGiaGoi, thoiHanNgay, trangThaiHoatDong)
    VALUES ('GOI_SPA10', N'Gói Chăm Sóc Spa 10 Buổi', 10, 1200000, 180, 1);

IF NOT EXISTS (SELECT 1 FROM GoiSpaPass WHERE maGoi = 'GOI_SPA05')
    INSERT INTO GoiSpaPass (maGoi, tenGoi, soBuoi, donGiaGoi, thoiHanNgay, trangThaiHoatDong)
    VALUES ('GOI_SPA05', N'Gói Chăm Sóc Spa 05 Buổi', 5, 650000, 90, 1);

IF NOT EXISTS (SELECT 1 FROM GoiSpaPass WHERE maGoi = 'GOI_SPA_VIP')
    INSERT INTO GoiSpaPass (maGoi, tenGoi, soBuoi, donGiaGoi, thoiHanNgay, trangThaiHoatDong)
    VALUES ('GOI_SPA_VIP', N'Gói Spa Chuyên Sâu VIP 12 Buổi', 12, 1800000, 240, 1);
GO

-- Khởi tạo danh mục tiền tố trong BangCapSo
MERGE BangCapSo AS Target
USING (VALUES 
    ('NV', 0, N'Mã nhân viên / tài khoản'),
    ('SP', 0, N'Mã sản phẩm'),
    ('DV', 0, N'Mã dịch vụ'),
    ('KH', 0, N'Mã khách hàng thành viên'),
    ('TC', 0, N'Mã thú cưng'),
    ('HS', 0, N'Mã hồ sơ thú cưng'),
    ('SK', 0, N'Mã sổ sức khỏe'),
    ('DVT', 0, N'Mã đơn vị tính'),
    ('QD', 0, N'Mã quy đổi đơn vị'),
    ('GC', 0, N'Mã biên bản giao ca'),
    ('PC', 0, N'Mã phân công ca trực'),
    ('HD', 0, N'Mã hóa đơn bán hàng'),
    ('DT', 0, N'Mã phiếu đổi trả')
) AS Source (loaiMa, soHienTai, moTa)
ON Target.loaiMa = Source.loaiMa
WHEN NOT MATCHED THEN
    INSERT (loaiMa, soHienTai, moTa) VALUES (Source.loaiMa, Source.soHienTai, Source.moTa);
GO

PRINT N'>>> ĐÃ HOÀN TẤT GIAI ĐOẠN 1: CẤU TRÚC 30 BẢNG VÀ CẤU HÌNH CƠ BẢN.';
GO


-- ======================================================================================
-- PHẦN III: BỘ DỮ LIỆU MẪU ĐỒNG BỘ THỰC TẾ (SYNCHRONIZED REALISTIC DEMO DATA)
-- ======================================================================================
IF CAST(SESSION_CONTEXT(N'CheDo') AS NVARCHAR(50)) = N'DEMO_DATA'
BEGIN
    IF DB_NAME() NOT IN (N'QuanLyCuaHangThuCung_Test', N'QuanLyCuaHangThuCung_Dev')
        THROW 50010, N'DEMO_DATA chỉ được chạy trên đúng database Test/Dev đã quy định.', 1;
    IF CAST(SESSION_CONTEXT(N'XacNhanDemo') AS NVARCHAR(100)) <> N'PETSHOP_MIO_DEMO_FRESH_ONLY'
        THROW 50011, N'Thiếu xác nhận DEMO fresh-only trong session.', 1;
    IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE maTaiKhoan=N'NV001' AND vaiTro=N'NVQL' AND trangThaiHoatDong=1 AND matKhau LIKE N'PBKDF2$%')
        THROW 50012, N'Phải tạo NV001 an toàn bằng BootstrapAdminAccount trước khi nạp demo.', 1;
    IF EXISTS (SELECT 1 FROM HoaDon) OR EXISTS (SELECT 1 FROM SanPham) OR EXISTS (SELECT 1 FROM KhachHang)
       OR EXISTS (SELECT 1 FROM BangGiaoCa) OR EXISTS (SELECT 1 FROM TaiKhoan WHERE maTaiKhoan<>N'NV001')
        THROW 50013, N'Database không còn trống; từ chối xóa hoặc ghi đè dữ liệu.', 1;

    PRINT N'>>> BẮT ĐẦU NẠP BỘ DỮ LIỆU MẪU ĐỒNG BỘ PETSHOP MIO VÀO DATABASE [' + DB_NAME() + N']...';

    SET XACT_ABORT ON;
    BEGIN TRY
    BEGIN TRANSACTION;

    -- 1. CA LÀM VIỆC
    INSERT INTO CaLamViec (maCa, tenCa, gioBatDau, gioKetThuc) VALUES
    ('CA01', N'Ca Sáng (07:00 - 11:30)', '07:00:00', '11:30:00'),
    ('CA02', N'Ca Trưa (11:30 - 15:30)', '11:30:00', '15:30:00'),
    ('CA03', N'Ca Chiều (15:30 - 19:30)', '15:30:00', '19:30:00'),
    ('CA04', N'Ca Tối (19:30 - 22:00)', '19:30:00', '22:00:00');

    -- 2. TÀI KHOẢN VÀ NHÂN VIÊN (10 nhân sự đủ 4 vai trò: NVQL, NVTN, NVBH, NVCS)
    -- NV001 đã được người vận hành bootstrap trước. Các tài khoản demo còn lại bị khóa;
    -- Quản lý phải cấp mật khẩu riêng bằng PBKDF2 rồi mới chủ động kích hoạt từng tài khoản.
    INSERT INTO TaiKhoan (maTaiKhoan, soDienThoai, email, matKhau, vaiTro, trangThaiHoatDong) VALUES
    ('NV002', '0923456789', 'dat.kho@miopet.vn',     'PBKDF2$65536$d3425e19c5cdad1a1dfdb26c249b5181$f2d998fa87a82de3d39cd4ee861d7cd67ed24f9865cd8be08f662db36fc31ffd', 'NVQL', 0),
    ('NV003', '0912345678', 'minh.thungan@miopet.vn', 'PBKDF2$65536$5abb5d0499dc55cb376b42a198909e40$8985a9bfbd9701e7f90762fa4116ff28a3b93e391f562f3f827d3f696cb846a1', 'NVTN', 0),
    ('NV004', '0913456789', 'mai.thungan@miopet.vn',  'PBKDF2$65536$c26b68444f04cbccace801f4d73d6041$5b207f4b40adb514be67194ab14b9171e4c1b311319508247e6114f3d6bc9d8a', 'NVTN', 0),
    ('NV005', '0956789012', 'hung.banhang@miopet.vn', 'PBKDF2$65536$4145402ac9625ba2057646f6b2192f13$db3aedaf76f1d4537f8b1d9a25ba1e661affeaf375f290ae96e1797cdbcc3521', 'NVBH', 0),
    ('NV006', '0967890123', 'lan.banhang@miopet.vn',  'PBKDF2$65536$56d3bc873caf763ec748e64b9f20d160$d057040d12576300f93f51379a01bd2a42e72324ed2b3d6a694b363a78797719', 'NVBH', 0),
    ('NV007', '0945678901', 'ngoc.spa@miopet.vn',     'PBKDF2$65536$ab1dacf2e00668a04541bb7d08e1f07e$97338fbe070d553810cb5e9aaa455236195cf1b3b4e2d86c4f5f6ac6e7b4b76d', 'NVCS', 0),
    ('NV008', '0934567890', 'ha.ktv@miopet.vn',       'PBKDF2$65536$30e7254b60bad45bafc628b0e5965196$c697b9408e9bda909e61843612a2be2dd6714c85e721542e2d8ce61440f46f56', 'NVCS', 0),
    ('NV009', '0978901234', 'trong.ktv@miopet.vn',    'PBKDF2$65536$1c4c305ab29d8a248ac9c89d4263d3fc$8b78f27568f07f560d0b16918131e7e2774363c9dff78f818beaf5b13d881139', 'NVCS', 0),
    ('NV010', '0989012345', 'ngan.hotel@miopet.vn',   'PBKDF2$65536$b3cd23a5ef4a94782d93c71c5ed6c63c$717f518153a1cf93960a133350b92d9ff845263ade55d934512f3e2fbb8cd49c', 'NVCS', 0);

    INSERT INTO NhanVien (maNhanVien, hoTen, chucVu, soDienThoai, email, maTaiKhoan) VALUES
    ('NV002', N'Tô Thành Đạt',     N'Quản lý kho & vận hành',  '0923456789', 'dat.kho@miopet.vn',     'NV002'),
    ('NV003', N'Lê Anh Minh',      N'Thu ngân trưởng ca',      '0912345678', 'minh.thungan@miopet.vn', 'NV003'),
    ('NV004', N'Trần Thị Mai',     N'Thu ngân ca chiều',       '0913456789', 'mai.thungan@miopet.vn',  'NV004'),
    ('NV005', N'Nguyễn Văn Hùng',  N'Nhân viên bán hàng & tư vấn', '0956789012', 'hung.banhang@miopet.vn', 'NV005'),
    ('NV006', N'Phạm Thị Lan',     N'Nhân viên bán hàng',      '0967890123', 'lan.banhang@miopet.vn',  'NV006'),
    ('NV007', N'Võ Thị Hồng Ngọc', N'Chuyên viên Spa & Grooming', '0945678901', 'ngoc.spa@miopet.vn', 'NV007'),
    ('NV008', N'Trần Thị Thu Hà',  N'Kỹ thuật viên chăm sóc',  '0934567890', 'ha.ktv@miopet.vn',       'NV008'),
    ('NV009', N'Hoàng Đức Trọng',  N'Kỹ thuật viên Spa thú cưng', '0978901234', 'trong.ktv@miopet.vn', 'NV009'),
    ('NV010', N'Đỗ Kim Ngân',      N'Nhân viên khách sạn thú cưng', '0989012345', 'ngan.hotel@miopet.vn', 'NV010');

    -- 3. DANH MỤC HÀNG HÓA
    INSERT INTO DanhMucHang (maDanhMuc, tenDanhMuc, moTa) VALUES
    ('DM01', N'Thức ăn & Dinh dưỡng', N'Thức ăn khô hạt, thức ăn ướt pate, sữa bột và bánh thưởng'),
    ('DM02', N'Phụ kiện & Đồ dùng', N'Vòng cổ, dây dắt, chuồng, đệm ngủ, túi vận chuyển'),
    ('DM03', N'Vệ sinh & Chăm sóc', N'Cát vệ sinh, sữa tắm khử mùi, tã lót chuồng, lược chải lông'),
    ('DM04', N'Thuốc & Y tế thú cưng', N'Thuốc nhỏ gáy ve rận, men tiêu hóa, gel dinh dưỡng, tẩy giun'),
    ('DM05', N'Đồ chơi & Vận động', N'Cây cào móng mèo, bóng cao su gặm, cần câu mèo, đồ chơi tương tác');

    -- 4. ĐƠN VỊ TÍNH
    INSERT INTO DonViTinh (maDonVi, tenDonVi) VALUES
    ('DVT01', N'Gói'), ('DVT02', N'Lon'), ('DVT03', N'Chai'), ('DVT04', N'Chiếc'),
    ('DVT05', N'Túi'), ('DVT06', N'Hộp'), ('DVT07', N'Thùng'), ('DVT08', N'Bao');

    -- 5. NHÀ CUNG CẤP (10 đối tác phân phối chính thức)
    INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, diaChi, soDienThoai) VALUES
    ('NCC01', N'Công ty CP Phân phối Royal Canin VN', N'KCN Tân Bình, Tân Phú, TP.HCM', '02838112233'),
    ('NCC02', N'Công ty TNHH Dược Thú Y Bio-Pharmachemie', N'Quận 9, TP. Thủ Đức, TP.HCM', '02838994455'),
    ('NCC03', N'Nhà phân phối Phụ kiện thú cưng PetZone', N'Quận 10, TP.HCM', '02839223344'),
    ('NCC04', N'Công ty TNHH Thức ăn Thú cưng Mars Petcare VN', N'KCN Biên Hòa 2, Đồng Nai', '02838556677'),
    ('NCC05', N'Tổng kho Cát Vệ sinh & Phụ kiện Minh Phát', N'Bình Tân, TP.HCM', '02837667788'),
    ('NCC06', N'Công ty CP CTC Thú Cưng Việt Nam', N'Hoàng Mai, Hà Nội', '02436778899'),
    ('NCC07', N'Dược Thú Y Ceva Sante Animale VN', N'Phú Nhuận, TP.HCM', '02839955667'),
    ('NCC08', N'Công ty TNHH SmartHeart & Me-O VN', N'KCN VSIP 1, Bình Dương', '02743788990'),
    ('NCC09', N'Thương hiệu Bioline Pet Care Đức', N'Tân Bình, TP.HCM', '02838445566'),
    ('NCC10', N'Công ty TNHH Ferplast Phụ kiện Ý', N'Bình Thạnh, TP.HCM', '02835112233');

    -- 6. DỊCH VỤ NIÊM YẾT
    INSERT INTO DichVu (maDichVu, tenDichVu, giaDichVu, thoiLuongPhut) VALUES
    ('DV001', N'Tắm sấy vệ sinh toàn diện', 150000, 45),
    ('DV002', N'Cắt tỉa tạo kiểu lông nghệ thuật', 250000, 60),
    ('DV003', N'Nhuộm lông thẩm mỹ 4 chân/tai', 350000, 90),
    ('DV004', N'Combo Spa VIP (Tắm dưỡng + Cắt tỉa + Đắp mặt nạ)', 400000, 100),
    ('DV005', N'Khám sức khỏe tổng quát thú cưng', 120000, 30),
    ('DV006', N'Tiêm ngừa Vaccine 7 bệnh phòng dại', 280000, 20),
    ('DV007', N'Khách sạn thú cưng lưu trú 24h', 200000, 1440),
    ('DV008', N'Spa Thư Giãn Hương Thảo Mộc', 180000, 50);

    -- 7. PHÒNG LƯU TRÚ PET HOTEL (8 phòng: 6 phòng trống, 2 phòng đang có khách lưu trú)
    INSERT INTO PhongLuuTru (maPhong, loaiPhong, donGiaNgay, trangThai, tenThuCung, giongLoai, tenChu, sdtChu, thoiGianCheckIn, duKienCheckOut, ghiChuChamSoc, daChoAnSang, daChoAnToi, daDiDaoVeSinh, phiDichVuPhatSinh) VALUES
    ('P001', N'Deluxe Cat Suite', 250000, N'Trống', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 0),
    ('P002', N'Deluxe Dog Suite', 300000, N'Đang sử dụng', N'Bông', N'Poodle Trắng', N'Lê Thị Ngọc', '0912345605', '2026-11-06 09:00:00', '2026-11-10 17:00:00', N'Bé thích ăn pate trộn hạt lúc 8h sáng, dắt đi dạo 2 lần/ngày', 1, 1, 1, 50000),
    ('P003', N'Standard Cat Room', 180000, N'Trống', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 0),
    ('P004', N'Standard Dog Room', 200000, N'Đang sử dụng', N'Mimi', N'Mèo Anh lông ngắn', N'Ngô Văn Tài', '0912345609', '2026-11-07 10:00:00', '2026-11-09 18:00:00', N'Mèo nhát người lạ, cần vuốt ve nhẹ nhàng trước khi cho ăn', 1, 0, 1, 0),
    ('P005', N'Cozy Small Room', 150000, N'Trống', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 0),
    ('P006', N'Standard Cat Villa', 220000, N'Trống', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 0),
    ('P007', N'Twin Suite', 350000, N'Trống', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 0),
    ('P008', N'Phòng Cách ly Y tế', 200000, N'Trống', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 0);


    -- 8. SẢN PHẨM HỆ THỐNG (50 sản phẩm chuẩn trải đều 5 danh mục)
    INSERT INTO SanPham (maSanPham, maDanhMuc, tenSanPham, maVach, donViCoSo, giaBanCoSo, moTa) VALUES
    ('SP001', 'DM01', N'Hạt Royal Canin Mini Adult 2kg', '8936000000001', 'DVT01', 350000, N'Thức ăn hạt cao cấp cho chó nhỏ trưởng thành'),
    ('SP002', 'DM01', N'Pate Whiskas Cá Biển Lon 400g', '8936000000002', 'DVT02', 38000, N'Pate ướt dinh dưỡng hương vị cá biển cho mèo'),
    ('SP003', 'DM01', N'Hạt Cho Mèo Con Royal Canin Kitten 2kg', '8936000000003', 'DVT01', 365000, N'Dinh dưỡng phát triển toàn diện cho mèo con'),
    ('SP004', 'DM01', N'Hạt Chó Con SmartHeart Puppy Bò 1.5kg', '8936000000004', 'DVT01', 125000, N'Hạt dinh dưỡng vị thịt bò và sữa cho chó con'),
    ('SP005', 'DM01', N'Pate Chó Con Pedigree Bò Rau Củ 130g', '8936000000005', 'DVT01', 22000, N'Pate sốt đậm đà bổ sung kẽm và omega'),
    ('SP006', 'DM01', N'Súp Thưởng Ciao Churu Gà Gói 4 Tuýp', '8936000000006', 'DVT01', 45000, N'Súp thưởng dạng sốt mềm bổ sung nước cho mèo'),
    ('SP007', 'DM01', N'Bánh Xương Sạch Răng Pedigree DentaStix', '8936000000007', 'DVT01', 48000, N'Giảm 80% vôi răng và cao răng cho chó'),
    ('SP008', 'DM01', N'Sữa Bột Bio-Milk Dinh Dưỡng 100g', '8936000000008', 'DVT01', 35000, N'Sữa thay thế mẹ cho chó mèo sơ sinh'),
    ('SP009', 'DM01', N'Hạt Catsrang Mọi Lứa Tuổi 2kg', '8936000000009', 'DVT01', 210000, N'Hạt nhập khẩu Hàn Quốc tiêu hóa búi lông'),
    ('SP010', 'DM01', N'Pate Mèo Monge Vị Cá Hồi Lon 80g', '8936000000010', 'DVT02', 32000, N'Pate không ngũ cốc không chất bảo quản'),
    ('SP011', 'DM01', N'Hạt Ganador Cừu & Gạo Chó Trưởng Thành 3kg', '8936000000011', 'DVT01', 195000, N'Bổ sung Vitamin E và Selen tăng đề kháng'),
    ('SP012', 'DM01', N'Cỏ Mèo Tươi Catnip Giảm Stress Hộp 50g', '8936000000012', 'DVT06', 40000, N'Bạc hà mèo sấy khô kích thích tiêu hóa'),
    ('SP013', 'DM02', N'Vòng Cổ Chuông Đeo Cổ Phản Quang', '8936000000013', 'DVT04', 35000, N'Vòng cổ dù cao cấp có chuông reo chống lạc'),
    ('SP014', 'DM02', N'Dây Dắt Kèm Yếm Ngực Chống Siết', '8936000000014', 'DVT04', 110000, N'Yếm chữ H trợ lực không gây đau thở'),
    ('SP015', 'DM02', N'Đệm Ngủ Tròn Nhung Mềm Size M', '8936000000015', 'DVT04', 185000, N'Đệm giữ ấm mùa đông lông cừu nhân tạo'),
    ('SP016', 'DM02', N'Chuồng Sơn Tĩnh Điện Gấp Gọn Size 60cm', '8936000000016', 'DVT04', 380000, N'Khung sắt nan dày kèm khay nhựa hứng vệ sinh'),
    ('SP017', 'DM02', N'Túi Vận Chuyển Phi Hành Gia Trong Suốt', '8936000000017', 'DVT04', 290000, N'Balo thông gió đi phượt dạo phố cho thú nhỏ'),
    ('SP018', 'DM02', N'Bát Ăn Đôi Inox Đế Cao Su Chống Trượt', '8936000000018', 'DVT04', 75000, N'Bát inox 304 không gỉ dễ tháo rời vệ sinh'),
    ('SP019', 'DM02', N'Bình Nước Gắn Chuồng Tự Động Bi Lăn 400ml', '8936000000019', 'DVT03', 55000, N'Đầu vòi bi lăn chống rò rỉ ướt lông'),
    ('SP020', 'DM02', N'Lược Chải Lông Rụng Nút Bấm Tự Làm Sạch', '8936000000020', 'DVT04', 95000, N'Đầu kim bọc cao su massage da kích thích lông mọc'),
    ('SP021', 'DM02', N'Kìm Cắt Móng Thú Cưng Kèm Dũa Chuyên Dụng', '8936000000021', 'DVT04', 65000, N'Lưỡi thép góc nghiêng an toàn tránh cắt vào tủy'),
    ('SP022', 'DM02', N'Rọ Mõm Silicon Thoáng Khí Chống Cắn Bậy', '8936000000022', 'DVT04', 80000, N'Chất liệu dẻo êm ái vẫn uống được nước'),
    ('SP023', 'DM03', N'Cát Vệ Sinh Đất Sét Hương Chanh Cature 10L', '8936000000023', 'DVT05', 110000, N'Độ vón cục siêu cứng khóa mùi nhanh chóng'),
    ('SP024', 'DM03', N'Cát Đậu Nành Tofu Tự Nhiên Xả Bồn Cầu 6L', '8936000000024', 'DVT05', 135000, N'Cát hữu cơ thân thiện môi trường không bụi 99%'),
    ('SP025', 'DM03', N'Sữa Tắm SOS Trắng Lông Khử Mùi 530ml', '8936000000025', 'DVT03', 120000, N'Làm sáng màu lông chống ố vàng lưu hương 7 ngày'),
    ('SP026', 'DM03', N'Sữa Tắm Joyce & Dolls Dưỡng Mượt Lông 400ml', '8936000000026', 'DVT03', 210000, N'Chiết xuất tinh dầu hoa hồng phục hồi lông hư tổn'),
    ('SP027', 'DM03', N'Tã Bỉm Lót Chuồng Dono Gói 50 Miếng', '8936000000027', 'DVT01', 160000, N'Khóa ẩm 6 lớp kháng khuẩn hút nhanh'),
    ('SP028', 'DM03', N'Dung Dịch Xịt Khử Mùi Diệt Khuẩn Bioline 500ml', '8936000000028', 'DVT03', 95000, N'Khử trùng chuồng trại mùi nước tiểu phân thú'),
    ('SP029', 'DM03', N'Nước Nhỏ Mắt Vệ Sinh Ghèn Kháng Viêm 60ml', '8936000000029', 'DVT03', 45000, N'Rửa trôi bụi bẩn giảm ố cay khóe mắt'),
    ('SP030', 'DM03', N'Bột Nhổ Lông Tai Giảm Đau Khử Mùi 30g', '8936000000030', 'DVT06', 60000, N'Làm mềm chân lông tai giảm ngứa ráy'),
    ('SP031', 'DM03', N'Khăn Ướt Lau Mắt Mũi Tai Gói 80 Miếng', '8936000000031', 'DVT01', 40000, N'Không cồn không mùi nhẹ dịu vùng da nhạy cảm'),
    ('SP032', 'DM03', N'Chai Xịt Hướng Dẫn Đi Vệ Sinh Đúng Chỗ 120ml', '8936000000032', 'DVT03', 70000, N'Mùi định hướng hình thành thói quen cho cún con'),
    ('SP033', 'DM04', N'Thuốc Nhỏ Gáy Trị Ve Fronil Spot Hộp 5 Tuýp', '8936000000033', 'DVT06', 185000, N'Tiêu diệt ve rận bọ chét tác dụng 30 ngày'),
    ('SP034', 'DM04', N'Viên Nhai Tẩy Giun Chó Drontal Plus Vỉ 2 Viên', '8936000000034', 'DVT06', 80000, N'Trị giun đũa giun móc sán dây hiệu quả cao'),
    ('SP035', 'DM04', N'Gel Dinh Dưỡng Tăng Đề Kháng Nourse 120g', '8936000000035', 'DVT06', 160000, N'Phục hồi sức khỏe thú ốm sau phẫu thuật'),
    ('SP036', 'DM04', N'Men Tiêu Hóa Vi Sinh Sống Probio Hộp 20 Gói', '8936000000036', 'DVT06', 75000, N'Ngừa tiêu chảy phân sống do đổi thức ăn'),
    ('SP037', 'DM04', N'Canxi Nano Bio-Calvit Chắc Răng Xương 100 Viên', '8936000000037', 'DVT06', 130000, N'Hỗ trợ chó con còi xương chân yếu hạ bàn'),
    ('SP038', 'DM04', N'Thuốc Xịt Trị Nấm Da Alkin Fungikur 50ml', '8936000000038', 'DVT03', 155000, N'Trị nấm vảy gàu ngứa rụng lông quanh mắt miệng'),
    ('SP039', 'DM04', N'Siro Bổ Máu Kích Thích Thèm Ăn Pet-Tinic 100ml', '8936000000039', 'DVT03', 140000, N'Bổ sung sắt vitamin nhóm B cho thú mang thai'),
    ('SP040', 'DM04', N'Xịt Sát Trùng Vết Thương Hở Betadine Pet 100ml', '8936000000040', 'DVT03', 65000, N'Sát khuẩn vết trầy xước không xót da'),
    ('SP041', 'DM04', N'Dầu Cá Hồi Tươi Alaska Sáng Mắt Mượt Lông 250ml', '8936000000041', 'DVT03', 220000, N'Dồi dào EPA DHA giảm rụng lông viêm da'),
    ('SP042', 'DM04', N'Vitamin Tổng Hợp GimCat Paste Cho Mèo 100g', '8936000000042', 'DVT06', 175000, N'Bổ sung 12 loại vitamin thiết yếu cho tim và mắt'),
    ('SP043', 'DM05', N'Cây Cào Móng Trụ Dây Đay Sisal Kèm Chuột', '8936000000043', 'DVT04', 120000, N'Bảo vệ sofa thỏa mãn bản năng mài vuốt'),
    ('SP044', 'DM05', N'Cần Câu Mèo Lông Vũ Chuông Reo Dài 90cm', '8936000000044', 'DVT04', 30000, N'Đồ chơi kích thích phản xạ săn mồi của mèo'),
    ('SP045', 'DM05', N'Bóng Cao Su Gai Kêu Chíp Chíp Siêu Bền', '8936000000045', 'DVT04', 40000, N'Chất liệu cao su thiên nhiên massage nướu'),
    ('SP046', 'DM05', N'Xương Gặm Sạch Răng Vị Thịt Bò Orgo Gói 90g', '8936000000046', 'DVT01', 35000, N'Thơm ngon dai bền giải tỏa ngứa răng cún nhỏ'),
    ('SP047', 'DM05', N'Đĩa Bay Silicon Dẻo Huấn Luyện Ném Bắt', '8936000000047', 'DVT04', 60000, N'Bay êm không gây mẻ răng tổn thương vòm họng'),
    ('SP048', 'DM05', N'Chuột Nhồi Bông Bạc Hà Catnip Tương Tác', '8936000000048', 'DVT04', 25000, N'Đồ chơi nhồi cỏ catnip tự nhiên giúp mèo vui vẻ'),
    ('SP049', 'DM05', N'Tháp Bóng 3 Tầng Vui Nhộn Cho Mèo Vờn', '8936000000049', 'DVT04', 85000, N'Chất liệu nhựa ABS an toàn không cạnh sắc'),
    ('SP050', 'DM05', N'Đồ Chơi Dây Thừng Kéo Co Vải Cotton 3 Mối', '8936000000050', 'DVT04', 45000, N'Sợi cotton dệt chặt tăng lực cắn răng');

    -- 9. QUY ĐỔI ĐƠN VỊ TÍNH (50 đơn vị cơ sở + 25 quy cách đóng gói thứ cấp)
    INSERT INTO QuyDoiDonVi (maQuyDoi, maSanPham, maDonVi, tyLeQuyDoi, laDonViCoSo, giaBan) VALUES
    ('QD001', 'SP001', 'DVT01', 1, 1, 350000),
    ('QD002', 'SP002', 'DVT02', 1, 1, 38000),
    ('QD003', 'SP003', 'DVT01', 1, 1, 365000),
    ('QD004', 'SP004', 'DVT01', 1, 1, 125000),
    ('QD005', 'SP005', 'DVT01', 1, 1, 22000),
    ('QD006', 'SP006', 'DVT01', 1, 1, 45000),
    ('QD007', 'SP007', 'DVT01', 1, 1, 48000),
    ('QD008', 'SP008', 'DVT01', 1, 1, 35000),
    ('QD009', 'SP009', 'DVT01', 1, 1, 210000),
    ('QD010', 'SP010', 'DVT02', 1, 1, 32000),
    ('QD011', 'SP011', 'DVT01', 1, 1, 195000),
    ('QD012', 'SP012', 'DVT06', 1, 1, 40000),
    ('QD013', 'SP013', 'DVT04', 1, 1, 35000),
    ('QD014', 'SP014', 'DVT04', 1, 1, 110000),
    ('QD015', 'SP015', 'DVT04', 1, 1, 185000),
    ('QD016', 'SP016', 'DVT04', 1, 1, 380000),
    ('QD017', 'SP017', 'DVT04', 1, 1, 290000),
    ('QD018', 'SP018', 'DVT04', 1, 1, 75000),
    ('QD019', 'SP019', 'DVT03', 1, 1, 55000),
    ('QD020', 'SP020', 'DVT04', 1, 1, 95000),
    ('QD021', 'SP021', 'DVT04', 1, 1, 65000),
    ('QD022', 'SP022', 'DVT04', 1, 1, 80000),
    ('QD023', 'SP023', 'DVT05', 1, 1, 110000),
    ('QD024', 'SP024', 'DVT05', 1, 1, 135000),
    ('QD025', 'SP025', 'DVT03', 1, 1, 120000),
    ('QD026', 'SP026', 'DVT03', 1, 1, 210000),
    ('QD027', 'SP027', 'DVT01', 1, 1, 160000),
    ('QD028', 'SP028', 'DVT03', 1, 1, 95000),
    ('QD029', 'SP029', 'DVT03', 1, 1, 45000),
    ('QD030', 'SP030', 'DVT06', 1, 1, 60000),
    ('QD031', 'SP031', 'DVT01', 1, 1, 40000),
    ('QD032', 'SP032', 'DVT03', 1, 1, 70000),
    ('QD033', 'SP033', 'DVT06', 1, 1, 185000),
    ('QD034', 'SP034', 'DVT06', 1, 1, 80000),
    ('QD035', 'SP035', 'DVT06', 1, 1, 160000),
    ('QD036', 'SP036', 'DVT06', 1, 1, 75000),
    ('QD037', 'SP037', 'DVT06', 1, 1, 130000),
    ('QD038', 'SP038', 'DVT03', 1, 1, 155000),
    ('QD039', 'SP039', 'DVT03', 1, 1, 140000),
    ('QD040', 'SP040', 'DVT03', 1, 1, 65000),
    ('QD041', 'SP041', 'DVT03', 1, 1, 220000),
    ('QD042', 'SP042', 'DVT06', 1, 1, 175000),
    ('QD043', 'SP043', 'DVT04', 1, 1, 120000),
    ('QD044', 'SP044', 'DVT04', 1, 1, 30000),
    ('QD045', 'SP045', 'DVT04', 1, 1, 40000),
    ('QD046', 'SP046', 'DVT01', 1, 1, 35000),
    ('QD047', 'SP047', 'DVT04', 1, 1, 60000),
    ('QD048', 'SP048', 'DVT04', 1, 1, 25000),
    ('QD049', 'SP049', 'DVT04', 1, 1, 85000),
    ('QD050', 'SP050', 'DVT04', 1, 1, 45000),
    ('QD051', 'SP001', 'DVT07', 6, 0, 1980000),
    ('QD052', 'SP002', 'DVT07', 24, 0, 850000),
    ('QD053', 'SP003', 'DVT07', 6, 0, 2050000),
    ('QD054', 'SP004', 'DVT07', 8, 0, 920000),
    ('QD055', 'SP005', 'DVT06', 12, 0, 245000),
    ('QD056', 'SP006', 'DVT06', 10, 0, 420000),
    ('QD057', 'SP007', 'DVT06', 12, 0, 530000),
    ('QD058', 'SP008', 'DVT06', 10, 0, 320000),
    ('QD059', 'SP009', 'DVT07', 6, 0, 1180000),
    ('QD060', 'SP010', 'DVT07', 24, 0, 720000),
    ('QD061', 'SP011', 'DVT08', 4, 0, 730000),
    ('QD062', 'SP012', 'DVT07', 12, 0, 440000),
    ('QD063', 'SP023', 'DVT08', 3, 0, 310000),
    ('QD064', 'SP024', 'DVT07', 6, 0, 750000),
    ('QD065', 'SP025', 'DVT07', 12, 0, 1350000),
    ('QD066', 'SP026', 'DVT07', 12, 0, 2350000),
    ('QD067', 'SP027', 'DVT07', 6, 0, 900000),
    ('QD068', 'SP033', 'DVT07', 10, 0, 1750000),
    ('QD069', 'SP034', 'DVT07', 20, 0, 1500000),
    ('QD070', 'SP035', 'DVT07', 12, 0, 1800000),
    ('QD071', 'SP036', 'DVT07', 10, 0, 690000),
    ('QD072', 'SP037', 'DVT07', 12, 0, 1450000),
    ('QD073', 'SP041', 'DVT07', 10, 0, 2050000),
    ('QD074', 'SP042', 'DVT07', 12, 0, 1950000),
    ('QD075', 'SP046', 'DVT06', 12, 0, 390000);

    -- 10. LÔ HÀNG TỒN KHO FEFO (72 lô hàng: có lô hết hạn, cận hạn, hết tồn, và lô bình thường)
    INSERT INTO LoHang (soLo, maSanPham, maNhaCungCap, ngaySanXuat, hanSuDung, ngayNhap, donGiaNhapCoSo, soLuongNhap, soLuongTon) VALUES
    ('LH2026-001', 'SP001', 'NCC01', '2025-08-01', '2026-08-15', '2025-08-10 08:30:00', 240000, 30, 30),
    ('LH2026-002', 'SP001', 'NCC01', '2025-11-15', '2026-11-20', '2025-11-25 09:00:00', 250000, 40, 36),
    ('LH2026-003', 'SP001', 'NCC01', '2026-03-01', '2027-06-30', '2026-03-10 10:00:00', 255000, 100, 100),
    ('LH2026-004', 'SP002', 'NCC04', '2025-09-01', '2026-09-01', '2025-09-10 09:00:00', 25000, 60, 60),
    ('LH2026-005', 'SP002', 'NCC04', '2025-12-01', '2026-12-01', '2025-12-10 09:00:00', 26000, 80, 51),
    ('LH2026-006', 'SP002', 'NCC04', '2026-04-01', '2027-08-01', '2026-04-10 10:00:00', 27000, 150, 150),
    ('LH2026-007', 'SP003', 'NCC01', '2025-11-01', '2026-11-25', '2025-11-10 08:00:00', 255000, 35, 27),
    ('LH2026-008', 'SP003', 'NCC01', '2026-03-15', '2027-07-15', '2026-03-25 09:30:00', 260000, 80, 80),
    ('LH2026-009', 'SP004', 'NCC08', '2025-10-01', '2026-11-15', '2025-10-10 09:00:00', 85000, 50, 39),
    ('LH2026-010', 'SP004', 'NCC08', '2026-02-10', '2027-05-10', '2026-02-20 08:30:00', 88000, 120, 120),
    ('LH2026-011', 'SP005', 'NCC04', '2025-12-10', '2026-12-10', '2025-12-18 10:00:00', 15000, 100, 81),
    ('LH2026-012', 'SP005', 'NCC04', '2026-04-15', '2027-09-15', '2026-04-20 09:00:00', 16000, 200, 200),
    ('LH2026-013', 'SP006', 'NCC06', '2025-11-01', '2026-11-30', '2025-11-15 08:30:00', 30000, 80, 66),
    ('LH2026-014', 'SP006', 'NCC06', '2026-03-01', '2027-06-01', '2026-03-15 08:30:00', 32000, 150, 150),
    ('LH2026-015', 'SP007', 'NCC04', '2025-10-15', '2026-11-20', '2025-10-25 09:30:00', 32000, 60, 46),
    ('LH2026-016', 'SP007', 'NCC04', '2026-02-20', '2027-05-20', '2026-03-05 09:30:00', 34000, 100, 100),
    ('LH2026-017', 'SP008', 'NCC02', '2025-08-20', '2026-08-20', '2025-09-01 08:00:00', 22000, 50, 50),
    ('LH2026-018', 'SP008', 'NCC02', '2026-03-10', '2027-09-10', '2026-03-20 09:00:00', 24000, 120, 116),
    ('LH2026-019', 'SP009', 'NCC06', '2025-11-10', '2026-11-30', '2025-11-20 10:00:00', 145000, 40, 33),
    ('LH2026-020', 'SP009', 'NCC06', '2026-04-01', '2027-08-01', '2026-04-10 10:00:00', 150000, 80, 80),
    ('LH2026-021', 'SP010', 'NCC06', '2025-12-01', '2026-12-15', '2025-12-10 08:30:00', 21000, 80, 56),
    ('LH2026-022', 'SP010', 'NCC06', '2026-03-25', '2027-07-25', '2026-04-05 09:00:00', 22500, 150, 150),
    ('LH2026-023', 'SP011', 'NCC02', '2025-12-01', '2026-12-30', '2025-12-15 09:00:00', 45000, 40, 35),
    ('LH2026-024', 'SP011', 'NCC02', '2026-03-01', '2028-12-31', '2026-03-15 09:00:00', 50000, 100, 100),
    ('LH2026-025', 'SP012', 'NCC03', '2025-12-01', '2026-12-30', '2025-12-15 09:00:00', 45000, 40, 38),
    ('LH2026-026', 'SP012', 'NCC03', '2026-03-01', '2028-12-31', '2026-03-15 09:00:00', 50000, 100, 100),
    ('LH2026-027', 'SP013', 'NCC04', '2025-12-01', '2026-12-30', '2025-12-15 09:00:00', 45000, 40, 39),
    ('LH2026-028', 'SP013', 'NCC04', '2026-03-01', '2028-12-31', '2026-03-15 09:00:00', 50000, 100, 100),
    ('LH2026-029', 'SP014', 'NCC05', '2025-12-01', '2026-12-30', '2025-12-15 09:00:00', 45000, 40, 39),
    ('LH2026-030', 'SP014', 'NCC05', '2026-03-01', '2028-12-31', '2026-03-15 09:00:00', 50000, 100, 100),
    ('LH2026-031', 'SP015', 'NCC06', '2025-12-01', '2026-12-30', '2025-12-15 09:00:00', 45000, 40, 39),
    ('LH2026-032', 'SP015', 'NCC06', '2026-03-01', '2028-12-31', '2026-03-15 09:00:00', 50000, 100, 100),
    ('LH2026-033', 'SP016', 'NCC07', '2025-12-01', '2026-12-30', '2025-12-15 09:00:00', 45000, 40, 39),
    ('LH2026-034', 'SP016', 'NCC07', '2026-03-01', '2028-12-31', '2026-03-15 09:00:00', 50000, 100, 100),
    ('LH2026-035', 'SP017', 'NCC08', '2025-12-01', '2026-12-30', '2025-12-15 09:00:00', 45000, 40, 39),
    ('LH2026-036', 'SP017', 'NCC08', '2026-03-01', '2028-12-31', '2026-03-15 09:00:00', 50000, 100, 100),
    ('LH2026-037', 'SP018', 'NCC09', '2025-12-01', '2026-12-30', '2025-12-15 09:00:00', 45000, 40, 37),
    ('LH2026-038', 'SP018', 'NCC09', '2026-03-01', '2028-12-31', '2026-03-15 09:00:00', 50000, 100, 100),
    ('LH2026-039', 'SP019', 'NCC10', '2025-12-01', '2026-12-30', '2025-12-15 09:00:00', 45000, 40, 39),
    ('LH2026-040', 'SP019', 'NCC10', '2026-03-01', '2028-12-31', '2026-03-15 09:00:00', 50000, 100, 100),
    ('LH2026-041', 'SP020', 'NCC01', '2025-12-01', '2026-12-30', '2025-12-15 09:00:00', 45000, 40, 38),
    ('LH2026-042', 'SP020', 'NCC01', '2026-03-01', '2028-12-31', '2026-03-15 09:00:00', 50000, 100, 100),
    ('LH2026-043', 'SP021', 'NCC02', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-044', 'SP022', 'NCC03', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-045', 'SP023', 'NCC04', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-046', 'SP024', 'NCC05', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 93),
    ('LH2026-047', 'SP025', 'NCC06', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 87),
    ('LH2026-048', 'SP026', 'NCC07', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-049', 'SP027', 'NCC08', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 93),
    ('LH2026-050', 'SP028', 'NCC09', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 97),
    ('LH2026-051', 'SP029', 'NCC10', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-052', 'SP030', 'NCC01', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-053', 'SP031', 'NCC02', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 96),
    ('LH2026-054', 'SP032', 'NCC03', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-055', 'SP033', 'NCC04', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-056', 'SP034', 'NCC05', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-057', 'SP035', 'NCC06', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-058', 'SP036', 'NCC07', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-059', 'SP037', 'NCC08', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-060', 'SP038', 'NCC09', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-061', 'SP039', 'NCC10', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-062', 'SP040', 'NCC01', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-063', 'SP041', 'NCC02', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-064', 'SP042', 'NCC03', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-065', 'SP043', 'NCC04', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-066', 'SP044', 'NCC05', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 97),
    ('LH2026-067', 'SP045', 'NCC06', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 97),
    ('LH2026-068', 'SP046', 'NCC07', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 86),
    ('LH2026-069', 'SP047', 'NCC08', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-070', 'SP048', 'NCC09', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 98),
    ('LH2026-071', 'SP049', 'NCC10', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 99),
    ('LH2026-072', 'SP050', 'NCC01', '2026-02-01', '2028-12-31', '2026-02-15 09:00:00', 50000, 100, 98);


    -- 11. KHÁCH HÀNG THÀNH VIÊN (50 khách hàng với phân khúc đa dạng: vãng lai, nuôi 1 pet, nuôi nhiều pet, dùng Spa/Hotel)
    INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, diemTichLuy, email, diaChi) VALUES
    ('KH001', N'Nguyễn Văn An', '0903112201', 120, 'an.nguyen@gmail.com', N'128 Lê Lợi, P. Bến Nghé, Quận 1, TP.HCM'),
    ('KH002', N'Trần Thị Bích', '0903112202', 45, 'bich.tran@gmail.com', N'45 Hai Bà Trưng, Quận 3, TP.HCM'),
    ('KH003', N'Lê Hoàng Cường', '0903112203', 250, 'cuong.le@yahoo.com', N'89 Nguyễn Trãi, Phường 2, Quận 5, TP.HCM'),
    ('KH004', N'Phạm Thu Dung', '0903112204', 80, 'dung.pham@gmail.com', N'12 Đinh Tiên Hoàng, Bình Thạnh, TP.HCM'),
    ('KH005', N'Hoàng Văn Em', '0903112205', 15, 'em.hoang@outlook.com', N'234 Phan Đăng Lưu, Phú Nhuận, TP.HCM'),
    ('KH006', N'Vũ Minh Giang', '0903112206', 310, 'giang.vu@gmail.com', N'67 Trường Chinh, Tân Bình, TP.HCM'),
    ('KH007', N'Đặng Thùy Hương', '0903112207', 95, 'huong.dang@gmail.com', N'18 Cộng Hòa, P. 13, Tân Bình, TP.HCM'),
    ('KH008', N'Bùi Quốc Hùng', '0903112208', 0, 'hung.bui@hotmail.com', N'56 Quang Trung, Gò Vấp, TP.HCM'),
    ('KH009', N'Đỗ Mai Khôi', '0903112209', 180, 'khoi.do@gmail.com', N'92 Hoàng Văn Thụ, Phú Nhuận, TP.HCM'),
    ('KH010', N'Hồ Bích Liên', '0903112210', 60, 'lien.ho@gmail.com', N'14 Võ Văn Tần, Quận 3, TP.HCM'),
    ('KH011', N'Ngô Quốc Minh', '0912345601', 350, 'minh.ngo@gmail.com', N'78 Nam Kỳ Khởi Nghĩa, Quận 1, TP.HCM'),
    ('KH012', N'Dương Yến Nga', '0912345602', 420, 'nga.duong@gmail.com', N'105 Trần Hưng Đạo, Quận 5, TP.HCM'),
    ('KH013', N'Lý Thanh Oanh', '0912345603', 190, 'oanh.ly@gmail.com', N'33 Nguyễn Văn Cừ, Quận 5, TP.HCM'),
    ('KH014', N'Mai Xuân Phúc', '0912345604', 510, 'phuc.mai@gmail.com', N'88 Lý Thường Kiệt, Quận 10, TP.HCM'),
    ('KH015', N'Lê Thị Ngọc', '0912345605', 680, 'ngoc.le@gmail.com', N'214 Sư Vạn Hạnh, Quận 10, TP.HCM'),
    ('KH016', N'Phan Gia Quân', '0912345606', 290, 'quan.phan@gmail.com', N'15 Cách Mạng Tháng 8, Tân Bình, TP.HCM'),
    ('KH017', N'Trịnh Bảo Sơn', '0912345607', 750, 'son.trinh@gmail.com', N'40 Bạch Đằng, Bình Thạnh, TP.HCM'),
    ('KH018', N'Tạ Cẩm Tú', '0912345608', 830, 'tu.ta@gmail.com', N'55 Nơ Trang Long, Bình Thạnh, TP.HCM'),
    ('KH019', N'Ngô Văn Tài', '0912345609', 400, 'tai.ngo@gmail.com', N'62 Xô Viết Nghệ Tĩnh, Bình Thạnh, TP.HCM'),
    ('KH020', N'Đoàn Phương Uyên', '0912345610', 610, 'uyen.doan@gmail.com', N'19 Pasteur, P. Bến Nghé, Quận 1, TP.HCM'),
    ('KH021', N'Lâm Quốc Việt', '0912345611', 170, 'viet.lam@gmail.com', N'101 Đồng Khởi, Quận 1, TP.HCM'),
    ('KH022', N'Trương Triệu Vy', '0912345612', 230, 'vy.truong@gmail.com', N'38 Tôn Đức Thắng, Quận 1, TP.HCM'),
    ('KH023', N'Võ Thành Ý', '0912345613', 520, 'y.vo@gmail.com', N'84 Nguyễn Thị Minh Khai, Quận 3, TP.HCM'),
    ('KH024', N'Bạch Hồng Ánh', '0912345614', 310, 'anh.bach@gmail.com', N'12 Điện Biên Phủ, Bình Thạnh, TP.HCM'),
    ('KH025', N'Thái Kim Bảo', '0912345615', 480, 'bao.thai@gmail.com', N'76 Lê Duẩn, Quận 1, TP.HCM'),
    ('KH026', N'Cao Tấn Dũng', '0912345616', 195, 'dung.cao@gmail.com', N'90 Nguyễn Huệ, Quận 1, TP.HCM'),
    ('KH027', N'Đào Thị Gấm', '0912345617', 340, 'gam.dao@gmail.com', N'112 Hùng Vương, Quận 5, TP.HCM'),
    ('KH028', N'Hà Vĩnh Hưng', '0912345618', 620, 'hung.ha@gmail.com', N'49 An Dương Vương, Quận 5, TP.HCM'),
    ('KH029', N'Khưu Mỹ Kim', '0912345619', 270, 'kim.khuu@gmail.com', N'15 Nguyễn Tri Phương, Quận 10, TP.HCM'),
    ('KH030', N'Lưu Quang Long', '0912345620', 540, 'long.luu@gmail.com', N'300 Ba Tháng Hai, Quận 10, TP.HCM'),
    ('KH031', N'Nghiêm Thu Ngân', '0912345621', 380, 'ngan.nghiem@gmail.com', N'25 Tô Hiến Thành, Quận 10, TP.HCM'),
    ('KH032', N'Ông Thế Phong', '0912345622', 450, 'phong.ong@gmail.com', N'180 Lê Đại Hành, Quận 11, TP.HCM'),
    ('KH033', N'Quách Như Quỳnh', '0912345623', 290, 'quynh.quach@gmail.com', N'44 Ông Ích Khiêm, Quận 11, TP.HCM'),
    ('KH034', N'Sầm Hoài Sang', '0912345624', 710, 'sang.sam@gmail.com', N'95 Lạc Long Quân, Quận 11, TP.HCM'),
    ('KH035', N'Tăng Vĩ Thành', '0912345625', 580, 'thanh.tang@gmail.com', N'20 Bình Thới, Quận 11, TP.HCM'),
    ('KH036', N'Uông Diễm Thúy', '0912345626', 890, 'thuy.uong@gmail.com', N'16 Âu Cơ, P. 14, Tân Bình, TP.HCM'),
    ('KH037', N'Vương Đình Trọng', '0912345627', 1250, 'trong.vuong@gmail.com', N'77 Hoàng Hoa Thám, Tân Bình, TP.HCM'),
    ('KH038', N'Xung Thảo Vân', '0912345628', 920, 'van.xung@gmail.com', N'33 Phổ Quang, Tân Bình, TP.HCM'),
    ('KH039', N'Yên Hải Đăng', '0912345629', 640, 'dang.yen@gmail.com', N'82 Phan Xích Long, Phú Nhuận, TP.HCM'),
    ('KH040', N'An Gia Hân', '0912345630', 780, 'han.an@gmail.com', N'150 Phan Đăng Lưu, Phú Nhuận, TP.HCM'),
    ('KH041', N'Biện Khánh Linh', '0912345631', 850, 'linh.bien@gmail.com', N'27 Huỳnh Văn Bánh, Phú Nhuận, TP.HCM'),
    ('KH042', N'Chu Ngọc Minh', '0912345632', 670, 'minh.chu@gmail.com', N'104 Lê Văn Sỹ, Phú Nhuận, TP.HCM'),
    ('KH043', N'Diệp Trọng Nghĩa', '0912345633', 940, 'nghia.diep@gmail.com', N'59 Nguyễn Kiệm, Gò Vấp, TP.HCM'),
    ('KH044', N'Giang Tuấn Phát', '0912345634', 430, 'phat.giang@gmail.com', N'118 Lê Đức Thọ, Gò Vấp, TP.HCM'),
    ('KH045', N'Hứa Minh Quân', '0912345635', 720, 'quan.hua@gmail.com', N'45 Nguyễn Oanh, Gò Vấp, TP.HCM'),
    ('KH046', N'Kha Vĩnh Thụy', '0912345636', 1400, 'thuy.kha@gmail.com', N'22 Thảo Điền, TP. Thủ Đức, TP.HCM'),
    ('KH047', N'Lục Thúy Vy', '0912345637', 1150, 'vy.luc@gmail.com', N'68 Mai Chí Thọ, TP. Thủ Đức, TP.HCM'),
    ('KH048', N'Mạch Đức Trí', '0912345638', 980, 'tri.mach@gmail.com', N'105 Trần Não, TP. Thủ Đức, TP.HCM'),
    ('KH049', N'Nhan Ái Vân', '0912345639', 1320, 'van.nhan@gmail.com', N'50 Nguyễn Cơ Thạch, TP. Thủ Đức, TP.HCM'),
    ('KH050', N'Phó Gia Khiêm', '0912345640', 1600, 'khiem.pho@gmail.com', N'18 Song Hành, TP. Thủ Đức, TP.HCM');

    -- 12. THÚ CƯNG KHÁCH HÀNG (Đúng 50 thú cưng đa dạng giống loài)
    INSERT INTO ThuCung (maThuCung, tenThuCung, giongLoai, ngaySinh, gioiTinh) VALUES
    ('TC001', N'Lucky', N'Poodle Nâu Đỏ', '2023-04-10', N'Đực'),
    ('TC002', N'Miu', N'Mèo Anh Lông Ngắn', '2022-08-15', N'Cái'),
    ('TC003', N'Bé Bơ', N'Corgi Pembroke', '2023-01-20', N'Đực'),
    ('TC004', N'Kem', N'Mèo Ba Tư Trắng', '2021-11-05', N'Cái'),
    ('TC005', N'Bông', N'Poodle Trắng', '2022-05-18', N'Cái'),
    ('TC006', N'Cúp Vàng', N'Golden Retriever', '2021-03-25', N'Đực'),
    ('TC007', N'Mun', N'Mèo Mướp Ta', '2023-06-12', N'Đực'),
    ('TC008', N'Lu', N'Chó Phốc Sóc Pomeranian', '2022-09-30', N'Cái'),
    ('TC009', N'Misa', N'Mèo Xiêm Mắt Xanh', '2023-02-14', N'Cái'),
    ('TC010', N'Sữa', N'Chó Samoyed Tuyết', '2021-07-08', N'Đực'),
    ('TC011', N'Simba', N'Mèo Vàng Mắt Ngọc', '2022-10-10', N'Đực'),
    ('TC012', N'Mimi', N'Mèo Anh Lông Ngắn Bicolor', '2023-03-01', N'Cái'),
    ('TC013', N'Gấu', N'Chó Husky Sibir', '2021-12-04', N'Đực'),
    ('TC014', N'Moka', N'Chó Pug Mặt Xệ', '2022-04-19', N'Đực'),
    ('TC015', N'Nala', N'Mèo Mèo Ragdoll', '2022-07-22', N'Cái'),
    ('TC016', N'Đốm', N'Chó Dalmatian Đốm', '2021-09-17', N'Đực'),
    ('TC017', N'Xíu', N'Chó Chihuahua Mini', '2023-05-05', N'Cái'),
    ('TC018', N'Bánh Bao', N'Mèo Scottish Fold Tai Cụp', '2022-11-11', N'Đực'),
    ('TC019', N'Kiki', N'Chó Lạp Xưởng Dachshund', '2022-02-28', N'Đực'),
    ('TC020', N'Luna', N'Mèo Nga Mắt Xanh', '2023-01-15', N'Cái'),
    ('TC021', N'Milo', N'Chó Beagle Săn Thỏ', '2021-08-09', N'Đực'),
    ('TC022', N'Bim Bim', N'Mèo Chân Ngắn Munchkin', '2023-04-20', N'Cái'),
    ('TC023', N'Bơ Lạc', N'Chó Shiba Inu Nhật', '2022-06-06', N'Đực'),
    ('TC024', N'Mây', N'Mèo Angora Thổ Nhĩ Kỳ', '2021-10-30', N'Cái'),
    ('TC025', N'Choco', N'Chó Cocker Spaniel', '2022-01-12', N'Đực'),
    ('TC026', N'Ruby', N'Poodle Tiny Đỏ', '2023-07-01', N'Cái'),
    ('TC027', N'Sapphire', N'Mèo Anh Lông Dài', '2022-12-15', N'Đực'),
    ('TC028', N'Bắp', N'Chó Corgi Fluffy', '2023-03-10', N'Đực'),
    ('TC029', N'Khoai', N'Chó Corgi Cardigan', '2023-03-10', N'Cái'),
    ('TC030', N'Sắn', N'Mèo Sphynx Không Lông', '2022-05-20', N'Đực'),
    ('TC031', N'Tôm', N'Mèo Bengal Vằn Báo', '2022-09-09', N'Đực'),
    ('TC032', N'Tép', N'Mèo Ba Tư Exotic', '2023-02-02', N'Cái'),
    ('TC033', N'Rocky', N'Chó Bully Mỹ Cơ Bắp', '2021-06-18', N'Đực'),
    ('TC034', N'Rambo', N'Chó Boxer Vện', '2022-08-08', N'Đực'),
    ('TC035', N'Mận', N'Mèo Mướp Hoa', '2023-05-15', N'Cái'),
    ('TC036', N'Đào', N'Mèo Tam Thể Phúc Lộc', '2023-05-15', N'Cái'),
    ('TC037', N'Max', N'Chó Border Collie Thông Minh', '2021-11-20', N'Đực'),
    ('TC038', N'Bella', N'Chó Phốc Hươu Mini', '2022-10-05', N'Cái'),
    ('TC039', N'Pudding', N'Mèo Anh Lông Ngắn Lilac', '2023-01-18', N'Cái'),
    ('TC040', N'Jelly', N'Mèo Anh Lông Ngắn Golden', '2023-01-18', N'Đực'),
    ('TC041', N'Thor', N'Chó Doberman Pinscher', '2021-04-14', N'Đực'),
    ('TC042', N'Loki', N'Chó Malinois Bỉ', '2022-03-30', N'Đực'),
    ('TC043', N'Matcha', N'Mèo Chinchilla Bạc', '2023-06-25', N'Cái'),
    ('TC044', N'Gạo', N'Poodle Teacup Trắng', '2023-08-10', N'Cái'),
    ('TC045', N'Nếp', N'Poodle Teacup Nâu', '2023-08-10', N'Đực'),
    ('TC046', N'Diamond', N'Mèo Caracal Lai Quý', '2022-01-01', N'Đực'),
    ('TC047', N'Crown', N'Chó Akita Inu Hoàng Gia', '2021-05-15', N'Đực'),
    ('TC048', N'Princess', N'Mèo Ragdoll Blue Point', '2022-09-18', N'Cái'),
    ('TC049', N'King', N'Chó Tibetan Mastiff Ngao Tạng', '2020-12-12', N'Đực'),
    ('TC050', N'Queen', N'Mèo Maine Coon Khổng Lồ', '2021-08-28', N'Cái');

    -- 13. HỒ SƠ THÚ CƯNG & ĐỊNH DANH MICROCHIP (50 hồ sơ liên kết đúng chủ nuôi)
    INSERT INTO HoSoThuCung (maHoSo, maKhachHang, maThuCung, soMicrochip, ngayDangKy, trangThaiNuoi) VALUES
    ('HS001', 'KH011', 'TC001', '9851410001', '2026-01-15', N'Đang nuôi'),
    ('HS002', 'KH012', 'TC002', '9851410002', '2026-01-15', N'Đang nuôi'),
    ('HS003', 'KH013', 'TC003', '9851410003', '2026-01-15', N'Đang nuôi'),
    ('HS004', 'KH014', 'TC004', '9851410004', '2026-01-15', N'Đang nuôi'),
    ('HS005', 'KH015', 'TC005', '9851410005', '2026-01-15', N'Đang nuôi'),
    ('HS006', 'KH016', 'TC006', '9851410006', '2026-01-15', N'Đang nuôi'),
    ('HS007', 'KH017', 'TC007', '9851410007', '2026-01-15', N'Đang nuôi'),
    ('HS008', 'KH018', 'TC008', '9851410008', '2026-01-15', N'Đang nuôi'),
    ('HS009', 'KH019', 'TC009', '9851410009', '2026-01-15', N'Đang nuôi'),
    ('HS010', 'KH020', 'TC010', '9851410010', '2026-01-15', N'Đang nuôi'),
    ('HS011', 'KH021', 'TC011', '9851410011', '2026-01-15', N'Đang nuôi'),
    ('HS012', 'KH022', 'TC012', '9851410012', '2026-01-15', N'Đang nuôi'),
    ('HS013', 'KH023', 'TC013', '9851410013', '2026-01-15', N'Đang nuôi'),
    ('HS014', 'KH024', 'TC014', '9851410014', '2026-01-15', N'Đang nuôi'),
    ('HS015', 'KH025', 'TC015', '9851410015', '2026-01-15', N'Đang nuôi'),
    ('HS016', 'KH026', 'TC016', '9851410016', '2026-01-15', N'Đang nuôi'),
    ('HS017', 'KH027', 'TC017', '9851410017', '2026-01-15', N'Đang nuôi'),
    ('HS018', 'KH028', 'TC018', '9851410018', '2026-01-15', N'Đang nuôi'),
    ('HS019', 'KH029', 'TC019', '9851410019', '2026-01-15', N'Đang nuôi'),
    ('HS020', 'KH030', 'TC020', '9851410020', '2026-01-15', N'Đang nuôi'),
    ('HS021', 'KH031', 'TC021', '9851410021', '2026-01-15', N'Đang nuôi'),
    ('HS022', 'KH032', 'TC022', '9851410022', '2026-01-15', N'Đang nuôi'),
    ('HS023', 'KH033', 'TC023', '9851410023', '2026-01-15', N'Đang nuôi'),
    ('HS024', 'KH034', 'TC024', '9851410024', '2026-01-15', N'Đang nuôi'),
    ('HS025', 'KH035', 'TC025', '9851410025', '2026-01-15', N'Đang nuôi'),
    ('HS026', 'KH036', 'TC026', '9851410026', '2026-03-20', N'Đang nuôi'),
    ('HS027', 'KH036', 'TC027', '9851410027', '2026-03-20', N'Đang nuôi'),
    ('HS028', 'KH037', 'TC028', '9851410028', '2026-03-20', N'Đang nuôi'),
    ('HS029', 'KH037', 'TC029', '9851410029', '2026-03-20', N'Đang nuôi'),
    ('HS030', 'KH037', 'TC030', '9851410030', '2026-03-20', N'Đang nuôi'),
    ('HS031', 'KH038', 'TC031', '9851410031', '2026-03-20', N'Đang nuôi'),
    ('HS032', 'KH038', 'TC032', '9851410032', '2026-03-20', N'Đang nuôi'),
    ('HS033', 'KH039', 'TC033', '9851410033', '2026-03-20', N'Đang nuôi'),
    ('HS034', 'KH039', 'TC034', '9851410034', '2026-03-20', N'Đang nuôi'),
    ('HS035', 'KH040', 'TC035', '9851410035', '2026-03-20', N'Đang nuôi'),
    ('HS036', 'KH040', 'TC036', '9851410036', '2026-03-20', N'Đang nuôi'),
    ('HS037', 'KH041', 'TC037', '9851410037', '2026-03-20', N'Đang nuôi'),
    ('HS038', 'KH041', 'TC038', '9851410038', '2026-03-20', N'Đang nuôi'),
    ('HS039', 'KH042', 'TC039', '9851410039', '2026-03-20', N'Đang nuôi'),
    ('HS040', 'KH042', 'TC040', '9851410040', '2026-03-20', N'Đang nuôi'),
    ('HS041', 'KH043', 'TC041', '9851410041', '2026-03-20', N'Đang nuôi'),
    ('HS042', 'KH043', 'TC042', '9851410042', '2026-03-20', N'Đang nuôi'),
    ('HS043', 'KH044', 'TC043', '9851410043', '2026-03-20', N'Đang nuôi'),
    ('HS044', 'KH045', 'TC044', '9851410044', '2026-03-20', N'Đang nuôi'),
    ('HS045', 'KH045', 'TC045', '9851410045', '2026-03-20', N'Đang nuôi'),
    ('HS046', 'KH046', 'TC046', '9851410046', '2026-03-20', N'Đang nuôi'),
    ('HS047', 'KH047', 'TC047', '9851410047', '2026-03-20', N'Đang nuôi'),
    ('HS048', 'KH048', 'TC048', '9851410048', '2026-03-20', N'Đang nuôi'),
    ('HS049', 'KH049', 'TC049', '9851410049', '2026-03-20', N'Đang nuôi'),
    ('HS050', 'KH050', 'TC050', '9851410050', '2026-03-20', N'Đang nuôi');

    -- 14. SỔ THEO DÕI SỨC KHỎE VÀ THỂ TRẠNG (50 sổ sức khỏe do KTV chuyên trách kiểm tra)
    INSERT INTO HoSoSucKhoe (maSucKhoe, maThuCung, maNhanVienKiemTra, canNang, nhietDo, tinhTrangVeRan, tiemPhongDai, ghiChuTheTrang, ngayKiemTra) VALUES
    ('SK001', 'TC001', 'NV007', 4.70, 38.3, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-02 10:00:00'),
    ('SK002', 'TC002', 'NV008', 5.90, 38.4, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-03 10:00:00'),
    ('SK003', 'TC003', 'NV007', 7.10, 38.5, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-04 10:00:00'),
    ('SK004', 'TC004', 'NV008', 8.30, 38.6, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-05 10:00:00'),
    ('SK005', 'TC005', 'NV007', 9.50, 38.2, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-06 10:00:00'),
    ('SK006', 'TC006', 'NV008', 10.70, 38.3, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-07 10:00:00'),
    ('SK007', 'TC007', 'NV007', 11.90, 38.4, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-08 10:00:00'),
    ('SK008', 'TC008', 'NV008', 13.10, 38.5, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-09 10:00:00'),
    ('SK009', 'TC009', 'NV007', 14.30, 38.6, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-10 10:00:00'),
    ('SK010', 'TC010', 'NV008', 15.50, 38.2, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-11 10:00:00'),
    ('SK011', 'TC011', 'NV007', 16.70, 38.3, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-12 10:00:00'),
    ('SK012', 'TC012', 'NV008', 17.90, 38.4, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-13 10:00:00'),
    ('SK013', 'TC013', 'NV007', 19.10, 38.5, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-14 10:00:00'),
    ('SK014', 'TC014', 'NV008', 20.30, 38.6, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-15 10:00:00'),
    ('SK015', 'TC015', 'NV007', 3.50, 38.2, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-16 10:00:00'),
    ('SK016', 'TC016', 'NV008', 4.70, 38.3, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-17 10:00:00'),
    ('SK017', 'TC017', 'NV007', 5.90, 38.4, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-18 10:00:00'),
    ('SK018', 'TC018', 'NV008', 7.10, 38.5, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-19 10:00:00'),
    ('SK019', 'TC019', 'NV007', 8.30, 38.6, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-20 10:00:00'),
    ('SK020', 'TC020', 'NV008', 9.50, 38.2, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-21 10:00:00'),
    ('SK021', 'TC021', 'NV007', 10.70, 38.3, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-22 10:00:00'),
    ('SK022', 'TC022', 'NV008', 11.90, 38.4, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-23 10:00:00'),
    ('SK023', 'TC023', 'NV007', 13.10, 38.5, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-24 10:00:00'),
    ('SK024', 'TC024', 'NV008', 14.30, 38.6, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-25 10:00:00'),
    ('SK025', 'TC025', 'NV007', 15.50, 38.2, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-26 10:00:00'),
    ('SK026', 'TC026', 'NV008', 16.70, 38.3, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-27 10:00:00'),
    ('SK027', 'TC027', 'NV007', 17.90, 38.4, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-28 10:00:00'),
    ('SK028', 'TC028', 'NV008', 19.10, 38.5, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-01 10:00:00'),
    ('SK029', 'TC029', 'NV007', 20.30, 38.6, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-02 10:00:00'),
    ('SK030', 'TC030', 'NV008', 3.50, 38.2, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-03 10:00:00'),
    ('SK031', 'TC031', 'NV007', 4.70, 38.3, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-04 10:00:00'),
    ('SK032', 'TC032', 'NV008', 5.90, 38.4, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-05 10:00:00'),
    ('SK033', 'TC033', 'NV007', 7.10, 38.5, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-06 10:00:00'),
    ('SK034', 'TC034', 'NV008', 8.30, 38.6, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-07 10:00:00'),
    ('SK035', 'TC035', 'NV007', 9.50, 38.2, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-08 10:00:00'),
    ('SK036', 'TC036', 'NV008', 10.70, 38.3, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-09 10:00:00'),
    ('SK037', 'TC037', 'NV007', 11.90, 38.4, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-10 10:00:00'),
    ('SK038', 'TC038', 'NV008', 13.10, 38.5, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-11 10:00:00'),
    ('SK039', 'TC039', 'NV007', 14.30, 38.6, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-12 10:00:00'),
    ('SK040', 'TC040', 'NV008', 15.50, 38.2, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-13 10:00:00'),
    ('SK041', 'TC041', 'NV007', 16.70, 38.3, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-14 10:00:00'),
    ('SK042', 'TC042', 'NV008', 17.90, 38.4, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-15 10:00:00'),
    ('SK043', 'TC043', 'NV007', 19.10, 38.5, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-16 10:00:00'),
    ('SK044', 'TC044', 'NV008', 20.30, 38.6, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-17 10:00:00'),
    ('SK045', 'TC045', 'NV007', 3.50, 38.2, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-18 10:00:00'),
    ('SK046', 'TC046', 'NV008', 4.70, 38.3, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-19 10:00:00'),
    ('SK047', 'TC047', 'NV007', 5.90, 38.4, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-20 10:00:00'),
    ('SK048', 'TC048', 'NV008', 7.10, 38.5, N'Âm tính', 0, N'Cần nhắc chủ tiêm phòng dại mũi nhắc lại', '2026-10-21 10:00:00'),
    ('SK049', 'TC049', 'NV007', 8.30, 38.6, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-22 10:00:00'),
    ('SK050', 'TC050', 'NV008', 9.50, 38.2, N'Âm tính', 1, N'Thể trạng khỏe mạnh, lông bóng mượt', '2026-10-23 10:00:00');


    -- 15. PHÂN CÔNG CA LÀM VIỆC (48 ca trực trong 12 ngày gần nhất)
    INSERT INTO BangPhanCong (maNhanVien, maCa, ngayLamViec, ghiChu) VALUES
    ('NV003', 'CA01', '2026-10-27', N'Thu ngân ca sáng chính'),
    ('NV005', 'CA02', '2026-10-27', N'Bán hàng ca trưa'),
    ('NV004', 'CA03', '2026-10-27', N'Thu ngân ca chiều chính'),
    ('NV007', 'CA04', '2026-10-27', N'Trực kỹ thuật Spa & Lưu trú tối'),
    ('NV003', 'CA01', '2026-10-28', N'Thu ngân ca sáng chính'),
    ('NV005', 'CA02', '2026-10-28', N'Bán hàng ca trưa'),
    ('NV004', 'CA03', '2026-10-28', N'Thu ngân ca chiều chính'),
    ('NV007', 'CA04', '2026-10-28', N'Trực kỹ thuật Spa & Lưu trú tối'),
    ('NV003', 'CA01', '2026-10-29', N'Thu ngân ca sáng chính'),
    ('NV005', 'CA02', '2026-10-29', N'Bán hàng ca trưa'),
    ('NV004', 'CA03', '2026-10-29', N'Thu ngân ca chiều chính'),
    ('NV007', 'CA04', '2026-10-29', N'Trực kỹ thuật Spa & Lưu trú tối'),
    ('NV003', 'CA01', '2026-10-30', N'Thu ngân ca sáng chính'),
    ('NV005', 'CA02', '2026-10-30', N'Bán hàng ca trưa'),
    ('NV004', 'CA03', '2026-10-30', N'Thu ngân ca chiều chính'),
    ('NV007', 'CA04', '2026-10-30', N'Trực kỹ thuật Spa & Lưu trú tối'),
    ('NV003', 'CA01', '2026-10-31', N'Thu ngân ca sáng chính'),
    ('NV005', 'CA02', '2026-10-31', N'Bán hàng ca trưa'),
    ('NV004', 'CA03', '2026-10-31', N'Thu ngân ca chiều chính'),
    ('NV007', 'CA04', '2026-10-31', N'Trực kỹ thuật Spa & Lưu trú tối'),
    ('NV003', 'CA01', '2026-11-01', N'Thu ngân ca sáng chính'),
    ('NV005', 'CA02', '2026-11-01', N'Bán hàng ca trưa'),
    ('NV004', 'CA03', '2026-11-01', N'Thu ngân ca chiều chính'),
    ('NV007', 'CA04', '2026-11-01', N'Trực kỹ thuật Spa & Lưu trú tối'),
    ('NV003', 'CA01', '2026-11-02', N'Thu ngân ca sáng chính'),
    ('NV005', 'CA02', '2026-11-02', N'Bán hàng ca trưa'),
    ('NV004', 'CA03', '2026-11-02', N'Thu ngân ca chiều chính'),
    ('NV007', 'CA04', '2026-11-02', N'Trực kỹ thuật Spa & Lưu trú tối'),
    ('NV003', 'CA01', '2026-11-03', N'Thu ngân ca sáng chính'),
    ('NV005', 'CA02', '2026-11-03', N'Bán hàng ca trưa'),
    ('NV004', 'CA03', '2026-11-03', N'Thu ngân ca chiều chính'),
    ('NV007', 'CA04', '2026-11-03', N'Trực kỹ thuật Spa & Lưu trú tối'),
    ('NV003', 'CA01', '2026-11-04', N'Thu ngân ca sáng chính'),
    ('NV005', 'CA02', '2026-11-04', N'Bán hàng ca trưa'),
    ('NV004', 'CA03', '2026-11-04', N'Thu ngân ca chiều chính'),
    ('NV007', 'CA04', '2026-11-04', N'Trực kỹ thuật Spa & Lưu trú tối'),
    ('NV003', 'CA01', '2026-11-05', N'Thu ngân ca sáng chính'),
    ('NV005', 'CA02', '2026-11-05', N'Bán hàng ca trưa'),
    ('NV004', 'CA03', '2026-11-05', N'Thu ngân ca chiều chính'),
    ('NV007', 'CA04', '2026-11-05', N'Trực kỹ thuật Spa & Lưu trú tối'),
    ('NV003', 'CA01', '2026-11-06', N'Thu ngân ca sáng chính'),
    ('NV005', 'CA02', '2026-11-06', N'Bán hàng ca trưa'),
    ('NV004', 'CA03', '2026-11-06', N'Thu ngân ca chiều chính'),
    ('NV007', 'CA04', '2026-11-06', N'Trực kỹ thuật Spa & Lưu trú tối'),
    ('NV003', 'CA01', '2026-11-07', N'Thu ngân ca sáng chính'),
    ('NV005', 'CA02', '2026-11-07', N'Bán hàng ca trưa'),
    ('NV004', 'CA03', '2026-11-07', N'Thu ngân ca chiều chính'),
    ('NV007', 'CA04', '2026-11-07', N'Trực kỹ thuật Spa & Lưu trú tối');

    -- 16. HÓA ĐƠN BÁN HÀNG POS (50 hóa đơn: 44 đã thanh toán, 2 đã hủy, 4 sử dụng Spa Pass)
    INSERT INTO HoaDon (maHoaDon, maKhachHang, maNhanVien, ngayLap, thoiGianThanhToan, giamGia, tienDoiDiem, tienBuTruDoiTra, thanhTien, phuongThucThanhToan, trangThaiHoaDon) VALUES
    ('HD001', 'KH001', 'NV003', '2026-10-27 08:45:00', '2026-10-27 08:47:00', 0, 0, 0, 434000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD002', 'KH002', 'NV004', '2026-10-27 16:15:00', '2026-10-27 16:18:00', 0, 0, 0, 1020000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD003', 'KH003', 'NV003', '2026-10-28 09:20:00', '2026-10-28 09:23:00', 0, 0, 0, 530000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD004', 'KH004', 'NV004', '2026-10-28 17:30:00', '2026-10-28 17:34:00', 0, 0, 0, 438000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD005', 'KH005', 'NV003', '2026-10-29 08:30:00', '2026-10-29 08:33:00', 0, 0, 0, 380000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD006', 'KH006', 'NV004', '2026-10-29 16:40:00', '2026-10-29 16:43:00', 0, 0, 0, 425000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD007', 'KH007', 'NV003', '2026-10-30 09:15:00', '2026-10-30 09:18:00', 0, 0, 0, 720000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD008', 'KH008', 'NV004', '2026-10-30 18:20:00', '2026-10-30 18:24:00', 0, 0, 0, 890000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD009', 'KH009', 'NV003', '2026-10-31 08:50:00', '2026-10-31 08:53:00', 0, 0, 0, 450000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD010', 'KH010', 'NV004', '2026-10-31 16:50:00', '2026-10-31 16:54:00', 0, 0, 0, 750000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD011', 'KH011', 'NV003', '2026-11-01 09:40:00', '2026-11-01 09:43:00', 0, 0, 0, 579000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD012', 'KH012', 'NV004', '2026-11-01 17:15:00', '2026-11-01 17:19:00', 0, 0, 0, 1080000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD013', 'KH013', 'NV003', '2026-11-02 08:40:00', '2026-11-02 08:44:00', 0, 0, 0, 630000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD014', 'KH014', 'NV004', '2026-11-02 16:30:00', '2026-11-02 16:34:00', 0, 0, 0, 435000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD015', 'KH015', 'NV003', '2026-11-03 09:10:00', '2026-11-03 09:14:00', 0, 0, 0, 1530000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD016', 'KH016', 'NV004', '2026-11-03 17:40:00', '2026-11-03 17:44:00', 0, 0, 0, 620000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD017', 'KH017', 'NV003', '2026-11-04 08:30:00', '2026-11-04 08:33:00', 0, 0, 0, 401000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD018', 'KH018', 'NV004', '2026-11-04 16:15:00', '2026-11-04 16:19:00', 0, 0, 0, 546000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD019', 'KH019', 'NV003', '2026-11-05 09:20:00', '2026-11-05 09:24:00', 0, 0, 0, 1050000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD020', 'KH020', 'NV004', '2026-11-05 17:10:00', '2026-11-05 17:14:00', 0, 0, 0, 875000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD021', 'KH021', 'NV003', '2026-11-06 08:45:00', '2026-11-06 08:49:00', 0, 0, 0, 880000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD022', 'KH022', 'NV004', '2026-11-06 16:45:00', '2026-11-06 16:49:00', 0, 0, 0, 1475000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD023', 'KH023', 'NV003', '2026-11-07 09:30:00', '2026-11-07 09:34:00', 0, 0, 0, 495000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD024', 'KH024', 'NV003', '2026-11-04 10:15:00', '2026-11-04 10:19:00', 0, 0, 0, 442000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD025', 'KH025', 'NV004', '2026-11-05 16:30:00', '2026-11-05 16:34:00', 0, 0, 0, 160000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD026', 'KH026', 'NV004', '2026-11-06 18:10:00', '2026-11-06 18:14:00', 0, 0, 0, 2630000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD027', 'KH027', 'NV003', '2026-10-27 10:30:00', '2026-10-27 10:33:00', 0, 0, 0, 150000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD028', 'KH028', 'NV004', '2026-10-28 16:30:00', '2026-10-28 16:33:00', 0, 0, 0, 250000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD029', 'KH029', 'NV003', '2026-10-29 10:15:00', '2026-10-29 10:18:00', 0, 0, 0, 350000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD030', 'KH030', 'NV004', '2026-10-30 17:00:00', '2026-10-30 17:03:00', 0, 0, 0, 400000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD031', 'KH031', 'NV003', '2026-10-31 10:00:00', '2026-10-31 10:03:00', 0, 0, 0, 120000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD032', 'KH032', 'NV004', '2026-11-01 16:00:00', '2026-11-01 16:03:00', 0, 0, 0, 280000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD033', 'KH033', 'NV003', '2026-11-02 10:15:00', '2026-11-02 10:18:00', 0, 0, 0, 180000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD034', 'KH034', 'NV004', '2026-11-03 16:45:00', '2026-11-03 16:48:00', 0, 0, 0, 150000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD035', 'KH035', 'NV003', '2026-10-27 09:30:00', '2026-10-27 09:34:00', 0, 0, 0, 500000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD036', 'KH036', 'NV004', '2026-10-28 18:00:00', '2026-10-28 18:04:00', 0, 0, 0, 900000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD037', 'KH037', 'NV003', '2026-10-29 11:00:00', '2026-10-29 11:04:00', 0, 0, 0, 360000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD038', 'KH038', 'NV004', '2026-10-31 16:00:00', '2026-10-31 16:04:00', 0, 0, 0, 600000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD039', 'KH039', 'NV003', '2026-11-01 10:30:00', '2026-11-01 10:34:00', 0, 0, 0, 450000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD040', 'KH040', 'NV004', '2026-11-02 18:30:00', '2026-11-02 18:34:00', 0, 0, 0, 440000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD041', 'KH041', 'NV003', '2026-10-27 10:00:00', '2026-10-27 10:03:00', 0, 0, 0, 1200000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD042', 'KH042', 'NV004', '2026-10-28 17:00:00', '2026-10-28 17:03:00', 0, 0, 0, 650000, N'Tiền mặt', N'Đã thanh toán'),
    ('HD043', 'KH043', 'NV003', '2026-10-29 09:50:00', '2026-10-29 09:53:00', 0, 0, 0, 1800000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD044', 'KH044', 'NV004', '2026-10-30 16:30:00', '2026-10-30 16:33:00', 0, 0, 0, 1200000, N'Chuyển khoản', N'Đã thanh toán'),
    ('HD045', 'KH045', 'NV003', '2026-11-03 10:45:00', NULL, 0, 0, 0, 535000, NULL, N'Đã hủy'),
    ('HD046', 'KH046', 'NV004', '2026-11-04 18:00:00', NULL, 0, 0, 0, 370000, NULL, N'Đã hủy'),
    ('HD047', 'KH041', 'NV003', '2026-10-28 10:00:00', '2026-10-28 10:02:00', 150000, 0, 0, 0, N'Spa Pass', N'Đã thanh toán'),
    ('HD048', 'KH041', 'NV004', '2026-10-31 16:00:00', '2026-10-31 16:02:00', 150000, 0, 0, 0, N'Spa Pass', N'Đã thanh toán'),
    ('HD049', 'KH042', 'NV003', '2026-10-30 10:15:00', '2026-10-30 10:17:00', 150000, 0, 0, 0, N'Spa Pass', N'Đã thanh toán'),
    ('HD050', 'KH043', 'NV003', '2026-11-01 10:45:00', '2026-11-01 10:47:00', 250000, 0, 0, 0, N'Spa Pass', N'Đã thanh toán');

    -- 17. CHI TIẾT HÓA ĐƠN (109 chi tiết: rổ hàng bán lẻ đa mặt hàng, quy đổi đơn vị, dịch vụ, spa pass, hotel)
    INSERT INTO ChiTietHoaDon (maChiTiet, maHoaDon, loaiChiTiet, maSanPham, maDichVu, soLuong, donGiaLucBan, thanhTien) VALUES
    ('CTHD001', 'HD001', 'SAN_PHAM', 'SP001', NULL, 1, 350000, 350000),
    ('CTHD002', 'HD001', 'SAN_PHAM', 'SP005', NULL, 2, 22000, 44000),
    ('CTHD003', 'HD001', 'SAN_PHAM', 'SP045', NULL, 1, 40000, 40000),
    ('CTHD004', 'HD002', 'SAN_PHAM', 'SP002', NULL, 1, 850000, 850000),
    ('CTHD005', 'HD002', 'SAN_PHAM', 'SP023', NULL, 1, 110000, 110000),
    ('CTHD006', 'HD002', 'SAN_PHAM', 'SP044', NULL, 2, 30000, 60000),
    ('CTHD007', 'HD003', 'SAN_PHAM', 'SP003', NULL, 1, 365000, 365000),
    ('CTHD008', 'HD003', 'SAN_PHAM', 'SP006', NULL, 2, 45000, 90000),
    ('CTHD009', 'HD003', 'SAN_PHAM', 'SP018', NULL, 1, 75000, 75000),
    ('CTHD010', 'HD004', 'SAN_PHAM', 'SP004', NULL, 2, 125000, 250000),
    ('CTHD011', 'HD004', 'SAN_PHAM', 'SP007', NULL, 1, 48000, 48000),
    ('CTHD012', 'HD004', 'SAN_PHAM', 'SP020', NULL, 1, 95000, 95000),
    ('CTHD013', 'HD004', 'SAN_PHAM', 'SP050', NULL, 1, 45000, 45000),
    ('CTHD014', 'HD005', 'SAN_PHAM', 'SP005', NULL, 1, 245000, 245000),
    ('CTHD015', 'HD005', 'SAN_PHAM', 'SP008', NULL, 2, 35000, 70000),
    ('CTHD016', 'HD005', 'SAN_PHAM', 'SP021', NULL, 1, 65000, 65000),
    ('CTHD017', 'HD006', 'SAN_PHAM', 'SP009', NULL, 1, 210000, 210000),
    ('CTHD018', 'HD006', 'SAN_PHAM', 'SP012', NULL, 2, 40000, 80000),
    ('CTHD019', 'HD006', 'SAN_PHAM', 'SP024', NULL, 1, 135000, 135000),
    ('CTHD020', 'HD007', 'SAN_PHAM', 'SP007', NULL, 1, 530000, 530000),
    ('CTHD021', 'HD007', 'SAN_PHAM', 'SP014', NULL, 1, 110000, 110000),
    ('CTHD022', 'HD007', 'SAN_PHAM', 'SP031', NULL, 2, 40000, 80000),
    ('CTHD023', 'HD008', 'SAN_PHAM', 'SP010', NULL, 1, 720000, 720000),
    ('CTHD024', 'HD008', 'SAN_PHAM', 'SP025', NULL, 1, 120000, 120000),
    ('CTHD025', 'HD008', 'SAN_PHAM', 'SP048', NULL, 2, 25000, 50000),
    ('CTHD026', 'HD009', 'SAN_PHAM', 'SP011', NULL, 1, 195000, 195000),
    ('CTHD027', 'HD009', 'SAN_PHAM', 'SP015', NULL, 1, 185000, 185000),
    ('CTHD028', 'HD009', 'SAN_PHAM', 'SP046', NULL, 2, 35000, 70000),
    ('CTHD029', 'HD010', 'SAN_PHAM', 'SP006', NULL, 1, 420000, 420000),
    ('CTHD030', 'HD010', 'SAN_PHAM', 'SP026', NULL, 1, 210000, 210000),
    ('CTHD031', 'HD010', 'SAN_PHAM', 'SP043', NULL, 1, 120000, 120000),
    ('CTHD032', 'HD011', 'SAN_PHAM', 'SP001', NULL, 1, 350000, 350000),
    ('CTHD033', 'HD011', 'SAN_PHAM', 'SP002', NULL, 3, 38000, 114000),
    ('CTHD034', 'HD011', 'SAN_PHAM', 'SP019', NULL, 1, 55000, 55000),
    ('CTHD035', 'HD011', 'SAN_PHAM', 'SP047', NULL, 1, 60000, 60000),
    ('CTHD036', 'HD012', 'SAN_PHAM', 'SP027', NULL, 1, 900000, 900000),
    ('CTHD037', 'HD012', 'SAN_PHAM', 'SP028', NULL, 1, 95000, 95000),
    ('CTHD038', 'HD012', 'SAN_PHAM', 'SP049', NULL, 1, 85000, 85000),
    ('CTHD039', 'HD013', 'SAN_PHAM', 'SP003', NULL, 1, 365000, 365000),
    ('CTHD040', 'HD013', 'SAN_PHAM', 'SP033', NULL, 1, 185000, 185000),
    ('CTHD041', 'HD013', 'SAN_PHAM', 'SP034', NULL, 1, 80000, 80000),
    ('CTHD042', 'HD014', 'SAN_PHAM', 'SP004', NULL, 1, 125000, 125000),
    ('CTHD043', 'HD014', 'SAN_PHAM', 'SP035', NULL, 1, 160000, 160000),
    ('CTHD044', 'HD014', 'SAN_PHAM', 'SP036', NULL, 1, 75000, 75000),
    ('CTHD045', 'HD014', 'SAN_PHAM', 'SP018', NULL, 1, 75000, 75000),
    ('CTHD046', 'HD015', 'SAN_PHAM', 'SP009', NULL, 1, 1180000, 1180000),
    ('CTHD047', 'HD015', 'SAN_PHAM', 'SP037', NULL, 1, 130000, 130000),
    ('CTHD048', 'HD015', 'SAN_PHAM', 'SP041', NULL, 1, 220000, 220000),
    ('CTHD049', 'HD016', 'SAN_PHAM', 'SP016', NULL, 1, 380000, 380000),
    ('CTHD050', 'HD016', 'SAN_PHAM', 'SP027', NULL, 1, 160000, 160000),
    ('CTHD051', 'HD016', 'SAN_PHAM', 'SP022', NULL, 1, 80000, 80000),
    ('CTHD052', 'HD017', 'SAN_PHAM', 'SP017', NULL, 1, 290000, 290000),
    ('CTHD053', 'HD017', 'SAN_PHAM', 'SP005', NULL, 3, 22000, 66000),
    ('CTHD054', 'HD017', 'SAN_PHAM', 'SP029', NULL, 1, 45000, 45000),
    ('CTHD055', 'HD018', 'SAN_PHAM', 'SP038', NULL, 1, 155000, 155000),
    ('CTHD056', 'HD018', 'SAN_PHAM', 'SP039', NULL, 1, 140000, 140000),
    ('CTHD057', 'HD018', 'SAN_PHAM', 'SP042', NULL, 1, 175000, 175000),
    ('CTHD058', 'HD018', 'SAN_PHAM', 'SP002', NULL, 2, 38000, 76000),
    ('CTHD059', 'HD019', 'SAN_PHAM', 'SP004', NULL, 1, 920000, 920000),
    ('CTHD060', 'HD019', 'SAN_PHAM', 'SP030', NULL, 1, 60000, 60000),
    ('CTHD061', 'HD019', 'SAN_PHAM', 'SP032', NULL, 1, 70000, 70000),
    ('CTHD062', 'HD020', 'SAN_PHAM', 'SP011', NULL, 1, 730000, 730000),
    ('CTHD063', 'HD020', 'SAN_PHAM', 'SP040', NULL, 1, 65000, 65000),
    ('CTHD064', 'HD020', 'SAN_PHAM', 'SP045', NULL, 2, 40000, 80000),
    ('CTHD065', 'HD021', 'SAN_PHAM', 'SP024', NULL, 1, 750000, 750000),
    ('CTHD066', 'HD021', 'SAN_PHAM', 'SP006', NULL, 2, 45000, 90000),
    ('CTHD067', 'HD021', 'SAN_PHAM', 'SP012', NULL, 1, 40000, 40000),
    ('CTHD068', 'HD022', 'SAN_PHAM', 'SP025', NULL, 1, 1350000, 1350000),
    ('CTHD069', 'HD022', 'SAN_PHAM', 'SP031', NULL, 2, 40000, 80000),
    ('CTHD070', 'HD022', 'SAN_PHAM', 'SP050', NULL, 1, 45000, 45000),
    ('CTHD071', 'HD023', 'SAN_PHAM', 'SP001', NULL, 1, 350000, 350000),
    ('CTHD072', 'HD023', 'SAN_PHAM', 'SP008', NULL, 2, 35000, 70000),
    ('CTHD073', 'HD023', 'SAN_PHAM', 'SP018', NULL, 1, 75000, 75000),
    ('CTHD074', 'HD024', 'SAN_PHAM', 'SP001', NULL, 1, 350000, 350000),
    ('CTHD075', 'HD024', 'SAN_PHAM', 'SP005', NULL, 2, 22000, 44000),
    ('CTHD076', 'HD024', 'SAN_PHAM', 'SP007', NULL, 1, 48000, 48000),
    ('CTHD077', 'HD025', 'SAN_PHAM', 'SP013', NULL, 1, 35000, 35000),
    ('CTHD078', 'HD025', 'SAN_PHAM', 'SP020', NULL, 1, 95000, 95000),
    ('CTHD079', 'HD025', 'SAN_PHAM', 'SP044', NULL, 1, 30000, 30000),
    ('CTHD080', 'HD026', 'SAN_PHAM', 'SP003', NULL, 1, 2050000, 2050000),
    ('CTHD081', 'HD026', 'SAN_PHAM', 'SP046', NULL, 1, 390000, 390000),
    ('CTHD082', 'HD026', 'SAN_PHAM', 'SP028', NULL, 2, 95000, 190000),
    ('CTHD083', 'HD027', 'DICH_VU', NULL, 'DV001', 1, 150000, 150000),
    ('CTHD084', 'HD028', 'DICH_VU', NULL, 'DV002', 1, 250000, 250000),
    ('CTHD085', 'HD029', 'DICH_VU', NULL, 'DV003', 1, 350000, 350000),
    ('CTHD086', 'HD030', 'DICH_VU', NULL, 'DV004', 1, 400000, 400000),
    ('CTHD087', 'HD031', 'DICH_VU', NULL, 'DV005', 1, 120000, 120000),
    ('CTHD088', 'HD032', 'DICH_VU', NULL, 'DV006', 1, 280000, 280000),
    ('CTHD089', 'HD033', 'DICH_VU', NULL, 'DV008', 1, 180000, 180000),
    ('CTHD090', 'HD034', 'DICH_VU', NULL, 'DV001', 1, 150000, 150000),
    ('CTHD091', 'HD035', 'DICH_VU', NULL, 'DV007', 2, 250000, 500000),
    ('CTHD092', 'HD036', 'DICH_VU', NULL, 'DV007', 3, 300000, 900000),
    ('CTHD093', 'HD037', 'DICH_VU', NULL, 'DV007', 2, 180000, 360000),
    ('CTHD094', 'HD038', 'DICH_VU', NULL, 'DV007', 3, 200000, 600000),
    ('CTHD095', 'HD039', 'DICH_VU', NULL, 'DV007', 3, 150000, 450000),
    ('CTHD096', 'HD040', 'DICH_VU', NULL, 'DV007', 2, 220000, 440000),
    ('CTHD097', 'HD041', 'DICH_VU', NULL, 'DV001', 10, 120000, 1200000),
    ('CTHD098', 'HD042', 'DICH_VU', NULL, 'DV001', 5, 130000, 650000),
    ('CTHD099', 'HD043', 'DICH_VU', NULL, 'DV004', 12, 150000, 1800000),
    ('CTHD100', 'HD044', 'DICH_VU', NULL, 'DV001', 10, 120000, 1200000),
    ('CTHD101', 'HD045', 'SAN_PHAM', 'SP001', NULL, 1, 350000, 350000),
    ('CTHD102', 'HD045', 'SAN_PHAM', 'SP015', NULL, 1, 185000, 185000),
    ('CTHD103', 'HD046', 'SAN_PHAM', 'SP041', NULL, 1, 220000, 220000),
    ('CTHD104', 'HD046', 'SAN_PHAM', 'SP014', NULL, 1, 110000, 110000),
    ('CTHD105', 'HD046', 'SAN_PHAM', 'SP045', NULL, 1, 40000, 40000),
    ('CTHD106', 'HD047', 'DICH_VU', NULL, 'DV001', 1, 150000, 150000),
    ('CTHD107', 'HD048', 'DICH_VU', NULL, 'DV001', 1, 150000, 150000),
    ('CTHD108', 'HD049', 'DICH_VU', NULL, 'DV001', 1, 150000, 150000),
    ('CTHD109', 'HD050', 'DICH_VU', NULL, 'DV002', 1, 250000, 250000);

    -- 18. CHI TIẾT XUẤT KHO THEO LÔ HÀNG (FEFO Tracking: xuất đúng lô cận hạn trước, quy đổi chính xác)
    INSERT INTO ChiTietXuatLo (maChiTiet, soLo, maQuyDoi, soLuongXuatCoSo) VALUES
    ('CTHD001', 'LH2026-002', 'QD001', 1),
    ('CTHD002', 'LH2026-011', 'QD005', 2),
    ('CTHD003', 'LH2026-067', 'QD045', 1),
    ('CTHD004', 'LH2026-005', 'QD052', 24),
    ('CTHD005', 'LH2026-045', 'QD023', 1),
    ('CTHD006', 'LH2026-066', 'QD044', 2),
    ('CTHD007', 'LH2026-007', 'QD003', 1),
    ('CTHD008', 'LH2026-013', 'QD006', 2),
    ('CTHD009', 'LH2026-037', 'QD018', 1),
    ('CTHD010', 'LH2026-009', 'QD004', 2),
    ('CTHD011', 'LH2026-015', 'QD007', 1),
    ('CTHD012', 'LH2026-041', 'QD020', 1),
    ('CTHD013', 'LH2026-072', 'QD050', 1),
    ('CTHD014', 'LH2026-011', 'QD055', 12),
    ('CTHD015', 'LH2026-018', 'QD008', 2),
    ('CTHD016', 'LH2026-043', 'QD021', 1),
    ('CTHD017', 'LH2026-019', 'QD009', 1),
    ('CTHD018', 'LH2026-025', 'QD012', 2),
    ('CTHD019', 'LH2026-046', 'QD024', 1),
    ('CTHD020', 'LH2026-015', 'QD057', 12),
    ('CTHD021', 'LH2026-029', 'QD014', 1),
    ('CTHD022', 'LH2026-053', 'QD031', 2),
    ('CTHD023', 'LH2026-021', 'QD060', 24),
    ('CTHD024', 'LH2026-047', 'QD025', 1),
    ('CTHD025', 'LH2026-070', 'QD048', 2),
    ('CTHD026', 'LH2026-023', 'QD011', 1),
    ('CTHD027', 'LH2026-031', 'QD015', 1),
    ('CTHD028', 'LH2026-068', 'QD046', 2),
    ('CTHD029', 'LH2026-013', 'QD056', 10),
    ('CTHD030', 'LH2026-048', 'QD026', 1),
    ('CTHD031', 'LH2026-065', 'QD043', 1),
    ('CTHD032', 'LH2026-002', 'QD001', 1),
    ('CTHD033', 'LH2026-005', 'QD002', 3),
    ('CTHD034', 'LH2026-039', 'QD019', 1),
    ('CTHD035', 'LH2026-069', 'QD047', 1),
    ('CTHD036', 'LH2026-049', 'QD067', 6),
    ('CTHD037', 'LH2026-050', 'QD028', 1),
    ('CTHD038', 'LH2026-071', 'QD049', 1),
    ('CTHD039', 'LH2026-007', 'QD003', 1),
    ('CTHD040', 'LH2026-055', 'QD033', 1),
    ('CTHD041', 'LH2026-056', 'QD034', 1),
    ('CTHD042', 'LH2026-009', 'QD004', 1),
    ('CTHD043', 'LH2026-057', 'QD035', 1),
    ('CTHD044', 'LH2026-058', 'QD036', 1),
    ('CTHD045', 'LH2026-037', 'QD018', 1),
    ('CTHD046', 'LH2026-019', 'QD059', 6),
    ('CTHD047', 'LH2026-059', 'QD037', 1),
    ('CTHD048', 'LH2026-063', 'QD041', 1),
    ('CTHD049', 'LH2026-033', 'QD016', 1),
    ('CTHD050', 'LH2026-049', 'QD027', 1),
    ('CTHD051', 'LH2026-044', 'QD022', 1),
    ('CTHD052', 'LH2026-035', 'QD017', 1),
    ('CTHD053', 'LH2026-011', 'QD005', 3),
    ('CTHD054', 'LH2026-051', 'QD029', 1),
    ('CTHD055', 'LH2026-060', 'QD038', 1),
    ('CTHD056', 'LH2026-061', 'QD039', 1),
    ('CTHD057', 'LH2026-064', 'QD042', 1),
    ('CTHD058', 'LH2026-005', 'QD002', 2),
    ('CTHD059', 'LH2026-009', 'QD054', 8),
    ('CTHD060', 'LH2026-052', 'QD030', 1),
    ('CTHD061', 'LH2026-054', 'QD032', 1),
    ('CTHD062', 'LH2026-023', 'QD061', 4),
    ('CTHD063', 'LH2026-062', 'QD040', 1),
    ('CTHD064', 'LH2026-067', 'QD045', 2),
    ('CTHD065', 'LH2026-046', 'QD064', 6),
    ('CTHD066', 'LH2026-013', 'QD006', 2),
    ('CTHD067', 'LH2026-025', 'QD012', 1),
    ('CTHD068', 'LH2026-047', 'QD065', 12),
    ('CTHD069', 'LH2026-053', 'QD031', 2),
    ('CTHD070', 'LH2026-072', 'QD050', 1),
    ('CTHD071', 'LH2026-002', 'QD001', 1),
    ('CTHD072', 'LH2026-018', 'QD008', 2),
    ('CTHD073', 'LH2026-037', 'QD018', 1),
    ('CTHD074', 'LH2026-002', 'QD001', 1),
    ('CTHD075', 'LH2026-011', 'QD005', 2),
    ('CTHD076', 'LH2026-015', 'QD007', 1),
    ('CTHD077', 'LH2026-027', 'QD013', 1),
    ('CTHD078', 'LH2026-041', 'QD020', 1),
    ('CTHD079', 'LH2026-066', 'QD044', 1),
    ('CTHD080', 'LH2026-007', 'QD053', 6),
    ('CTHD081', 'LH2026-068', 'QD075', 12),
    ('CTHD082', 'LH2026-050', 'QD028', 2);

    -- 19. CHI TIẾT DỊCH VỤ THỰC HIỆN & HOA HỒNG KỸ THUẬT VIÊN (10%)
    INSERT INTO ChiTietDichVu (maChiTiet, maDichVu, maThuCung, maKtvThucHien, tienHoaHong) VALUES
    ('CTHD083', 'DV001', 'TC003', 'NV007', 15000),
    ('CTHD084', 'DV002', 'TC004', 'NV008', 25000),
    ('CTHD085', 'DV003', 'TC005', 'NV009', 35000),
    ('CTHD086', 'DV004', 'TC006', 'NV007', 40000),
    ('CTHD087', 'DV005', 'TC007', 'NV008', 12000),
    ('CTHD088', 'DV006', 'TC008', 'NV009', 28000),
    ('CTHD089', 'DV008', 'TC009', 'NV007', 18000),
    ('CTHD090', 'DV001', 'TC010', 'NV008', 15000),
    ('CTHD091', 'DV007', 'TC011', 'NV010', 50000),
    ('CTHD092', 'DV007', 'TC012', 'NV010', 90000),
    ('CTHD093', 'DV007', 'TC013', 'NV010', 36000),
    ('CTHD094', 'DV007', 'TC014', 'NV010', 60000),
    ('CTHD095', 'DV007', 'TC015', 'NV010', 45000),
    ('CTHD096', 'DV007', 'TC016', 'NV010', 44000),
    ('CTHD106', 'DV001', 'TC008', 'NV007', 15000),
    ('CTHD107', 'DV001', 'TC008', 'NV008', 15000),
    ('CTHD108', 'DV001', 'TC009', 'NV007', 15000),
    ('CTHD109', 'DV002', 'TC010', 'NV008', 25000);

    -- 20. PHIẾU ĐỔI TRẢ HÀNG & CHI TIẾT (2 phiếu trong thời hạn 7 ngày = 7.69% tỷ lệ hoàn trả sản phẩm, chuẩn bán lẻ thực tế)
    INSERT INTO PhieuDoiTra (maPhieuDoiTra, maHoaDonGoc, maNhanVienTiepNhan, ngayDoiTra, tongTienHoan, lyDoDoiTra, trangThaiDoiTra, giaTriHangTra, giaTriHangDoi, tienHoanThucTe, tienKhachTraThem, phuongThucHoanTien) VALUES
    ('DT001', 'HD024', 'NV003', '2026-11-06 10:00:00', 350000, N'Bao bì hạt bị rách khi mở kiểm tra, hoàn tiền mặt', N'Hoàn thành', 350000, 0, 350000, 0, N'Tiền mặt'),
    ('DT002', 'HD025', 'NV004', '2026-11-07 16:30:00', 0, N'Khách đổi vòng cổ lấy kìm cắt móng, bù thêm tiền', N'Hoàn thành', 35000, 65000, 0, 30000, N'KHONG_HOAN_TIEN');

    INSERT INTO ChiTietDoiTra (maChiTietDoiTra, maPhieuDoiTra, maSanPham, soLuongDoiTra, tienHoanLai, soLo, soLuongCoSoHoan, soLuong, donGiaHoan, lyDoChiTiet) VALUES
    ('CTDT001', 'DT001', 'SP001', 1, 350000, 'LH2026-003', 0, 1, 350000, N'Rách bao bì không nhập lại kho'),
    ('CTDT002', 'DT002', 'SP013', 1, 35000, 'LH2026-025', 1, 1, 35000, N'Hàng nguyên tem mác nhập lại kho');

    -- 21. THẺ GÓI SPA PASS & LỊCH SỬ SỬ DỤNG (4 thẻ bán ra, 4 lượt sử dụng liên kết đúng hóa đơn)
    INSERT INTO TheSpaPass (maThe, sdtKhachHang, tenKhachHang, tenGoi, tongSoBuoi, soBuoiDaDung, donGiaGoi, ngayKichHoat, ngayHetHan, trangThai, maHoaDonKichHoat) VALUES
    ('CARD-SPA-001', '0912345641', N'Võ Hoàng Quân',     N'Gói Chăm Sóc Spa 10 Buổi',       10, 2, 1200000, '2026-10-27', '2027-04-27', N'ĐANG HOẠT ĐỘNG', 'HD041'),
    ('CARD-SPA-002', '0912345642', N'Bùi Phương Thảo',   N'Gói Chăm Sóc Spa 05 Buổi',       5,  1, 650000,  '2026-10-28', '2027-01-28', N'ĐANG HOẠT ĐỘNG', 'HD042'),
    ('CARD-SPA-003', '0912345643', N'Đặng Quốc Bảo',     N'Gói Spa Chuyên Sâu VIP 12 Buổi', 12, 1, 1800000, '2026-10-29', '2027-06-29', N'ĐANG HOẠT ĐỘNG', 'HD043'),
    ('CARD-SPA-004', '0912345644', N'Lương Thị Minh',    N'Gói Chăm Sóc Spa 10 Buổi',       10, 0, 1200000, '2026-10-30', '2027-04-30', N'ĐANG HOẠT ĐỘNG', 'HD044');

    INSERT INTO LichSuSuDungSpaPass (maThe, maHoaDon, maNhanVien, thoiGianSuDung, ghiChu) VALUES
    ('CARD-SPA-001', 'HD047', 'NV007', '2026-10-28 10:00:00', N'Tắm sấy cún cưng buổi 1'),
    ('CARD-SPA-001', 'HD048', 'NV008', '2026-10-31 16:00:00', N'Tắm sấy cún cưng buổi 2'),
    ('CARD-SPA-002', 'HD049', 'NV007', '2026-10-30 10:15:00', N'Tắm vệ sinh tai móng buổi 1'),
    ('CARD-SPA-003', 'HD050', 'NV008', '2026-11-01 10:45:00', N'Combo Spa VIP buổi 1');

    -- 22. LỊCH SỬ LƯU TRÚ PET HOTEL (12 lượt: 6 đã thanh toán trả phòng, 2 đang lưu trú, 2 đặt trước, 2 hủy)
    INSERT INTO LichSuLuuTru (maPhong, tenThuCung, tenChu, sdtChu, thoiGianCheckIn, thoiGianCheckOut, soNgayLuuTru, donGiaNgay, phiPhatSinh, tongTien, ghiChu, maHoaDon, trangThaiThanhToan) VALUES
    ('P001', N'Miu',    N'Lâm Quốc Việt',    '0912345611', '2026-10-25 09:00:00', '2026-10-27 09:00:00', 2, 250000, 0, 500000, N'Bé ngoan, ăn tốt', 'HD035', 'DA_THANH_TOAN'),
    ('P002', N'Bé Bơ',  N'Trương Triệu Vy',  '0912345612', '2026-10-25 10:00:00', '2026-10-28 17:00:00', 3, 300000, 0, 900000, N'Dắt đi dạo hàng ngày', 'HD036', 'DA_THANH_TOAN'),
    ('P003', N'Kem',    N'Võ Thành Ý',       '0912345613', '2026-10-27 08:30:00', '2026-10-29 10:30:00', 2, 180000, 0, 360000, N'Mèo ăn pate riêng mang theo', 'HD037', 'DA_THANH_TOAN'),
    ('P004', N'Cúp Vàng', N'Bạch Hồng Ánh', '0912345614', '2026-10-28 09:00:00', '2026-10-31 15:30:00', 3, 200000, 0, 600000, N'Chăm sóc đúng quy trình', 'HD038', 'DA_THANH_TOAN'),
    ('P005', N'Mun',    N'Thái Kim Bảo',     '0912345615', '2026-10-29 10:00:00', '2026-11-01 10:00:00', 3, 150000, 0, 450000, N'Thú nhỏ ngoan', 'HD039', 'DA_THANH_TOAN'),
    ('P006', N'Lu',     N'Cao Tấn Dũng',     '0912345616', '2026-10-31 09:00:00', '2026-11-02 18:00:00', 2, 220000, 0, 440000, N'Villa mèo rộng thoáng', 'HD040', 'DA_THANH_TOAN'),
    ('P002', N'Bông',   N'Lê Thị Ngọc',      '0912345605', '2026-11-06 09:00:00', '2026-11-10 17:00:00', 4, 300000, 50000, 1250000, N'Đang lưu trú, ăn pate trộn hạt', NULL, 'CHO_THANH_TOAN'),
    ('P004', N'Mimi',   N'Ngô Văn Tài',      '0912345609', '2026-11-07 10:00:00', '2026-11-09 18:00:00', 2, 200000, 0, 400000, N'Đang lưu trú, vuốt ve trước khi ăn', NULL, 'CHO_THANH_TOAN'),
    ('P001', N'Lucky',  N'Ngô Quốc Minh',    '0912345601', '2026-11-10 09:00:00', '2026-11-12 17:00:00', 2, 250000, 0, 500000, N'Khách đặt trước chuyến công tác', NULL, 'CHO_THANH_TOAN'),
    ('P003', N'Simba',  N'Dương Yến Nga',    '0912345602', '2026-11-11 10:00:00', '2026-11-13 18:00:00', 2, 180000, 0, 360000, N'Khách đặt trước', NULL, 'CHO_THANH_TOAN'),
    ('P005', N'Gấu',    N'Lý Thanh Oanh',    '0912345603', '2026-10-26 09:00:00', '2026-10-28 17:00:00', 2, 150000, 0, 300000, N'Khách hủy do hoãn chuyến bay', NULL, 'DA_HUY'),
    ('P007', N'Moka',   N'Mai Xuân Phúc',    '0912345604', '2026-11-03 10:00:00', '2026-11-05 18:00:00', 2, 350000, 0, 700000, N'Khách đổi lịch có người thân trông', NULL, 'DA_HUY');

    -- 23. LỊCH HẸN VÀ CHI TIẾT (50 lịch hẹn: 40 hoàn thành, 6 sắp tới, 4 đã hủy; không trùng giờ KTV/thú cưng)
    INSERT INTO LichHen (maLichHen, maKhachHang, maNhanVienTiepNhan, ngayHen, trangThaiLichHen, ghiChu) VALUES
    ('LH001', 'KH012', 'NV003', '2026-10-02 09:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH002', 'KH013', 'NV004', '2026-10-03 10:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH003', 'KH014', 'NV003', '2026-10-04 11:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH004', 'KH015', 'NV004', '2026-10-05 12:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH005', 'KH016', 'NV003', '2026-10-06 13:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH006', 'KH017', 'NV004', '2026-10-07 14:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH007', 'KH018', 'NV003', '2026-10-08 15:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH008', 'KH019', 'NV004', '2026-10-09 16:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH009', 'KH020', 'NV003', '2026-10-10 08:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH010', 'KH021', 'NV004', '2026-10-11 09:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH011', 'KH022', 'NV003', '2026-10-12 10:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH012', 'KH023', 'NV004', '2026-10-13 11:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH013', 'KH024', 'NV003', '2026-10-14 12:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH014', 'KH025', 'NV004', '2026-10-15 13:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH015', 'KH026', 'NV003', '2026-10-16 14:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH016', 'KH027', 'NV004', '2026-10-17 15:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH017', 'KH028', 'NV003', '2026-10-18 16:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH018', 'KH029', 'NV004', '2026-10-19 08:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH019', 'KH030', 'NV003', '2026-10-20 09:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH020', 'KH031', 'NV004', '2026-10-21 10:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH021', 'KH032', 'NV003', '2026-10-22 11:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH022', 'KH033', 'NV004', '2026-10-23 12:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH023', 'KH034', 'NV003', '2026-10-24 13:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH024', 'KH035', 'NV004', '2026-10-25 14:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH025', 'KH036', 'NV003', '2026-10-01 15:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH026', 'KH037', 'NV004', '2026-11-02 16:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH027', 'KH038', 'NV003', '2026-11-03 08:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH028', 'KH039', 'NV004', '2026-11-04 09:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH029', 'KH040', 'NV003', '2026-11-05 10:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH030', 'KH041', 'NV004', '2026-11-06 11:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH031', 'KH042', 'NV003', '2026-11-07 12:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH032', 'KH043', 'NV004', '2026-11-08 13:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH033', 'KH044', 'NV003', '2026-11-09 14:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH034', 'KH045', 'NV004', '2026-11-10 15:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH035', 'KH046', 'NV003', '2026-11-11 16:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH036', 'KH047', 'NV004', '2026-11-12 08:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH037', 'KH048', 'NV003', '2026-11-13 09:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH038', 'KH049', 'NV004', '2026-11-14 10:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH039', 'KH050', 'NV003', '2026-11-15 11:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH040', 'KH011', 'NV004', '2026-11-16 12:00:00', N'Đã hoàn thành', N'Khách đến đúng hẹn, dịch vụ tốt'),
    ('LH041', 'KH012', 'NV003', '2026-11-09 14:00:00', N'Chờ xác nhận', N'Khách hẹn trước qua hotline'),
    ('LH042', 'KH013', 'NV004', '2026-11-10 09:00:00', N'Đã xác nhận', N'Khách hẹn trước qua hotline'),
    ('LH043', 'KH014', 'NV003', '2026-11-11 10:00:00', N'Chờ xác nhận', N'Khách hẹn trước qua hotline'),
    ('LH044', 'KH015', 'NV004', '2026-11-12 11:00:00', N'Đã xác nhận', N'Khách hẹn trước qua hotline'),
    ('LH045', 'KH016', 'NV003', '2026-11-13 12:00:00', N'Chờ xác nhận', N'Khách hẹn trước qua hotline'),
    ('LH046', 'KH017', 'NV004', '2026-11-14 13:00:00', N'Đã xác nhận', N'Khách hẹn trước qua hotline'),
    ('LH047', 'KH018', 'NV003', '2026-11-02 17:00:00', N'Đã hủy', N'Khách bận việc đột xuất báo hủy trước 2 tiếng'),
    ('LH048', 'KH019', 'NV004', '2026-11-03 14:00:00', N'Đã hủy', N'Khách bận việc đột xuất báo hủy trước 2 tiếng'),
    ('LH049', 'KH020', 'NV003', '2026-11-04 15:00:00', N'Đã hủy', N'Khách bận việc đột xuất báo hủy trước 2 tiếng'),
    ('LH050', 'KH021', 'NV004', '2026-11-05 16:00:00', N'Đã hủy', N'Khách bận việc đột xuất báo hủy trước 2 tiếng');

    INSERT INTO ChiTietLichHen (maChiTietLichHen, maLichHen, maThuCung, maDichVu, maKtvChiDinh) VALUES
    ('CTLH001', 'LH001', 'TC001', 'DV001', 'NV008'),
    ('CTLH002', 'LH002', 'TC002', 'DV002', 'NV009'),
    ('CTLH003', 'LH003', 'TC003', 'DV003', 'NV007'),
    ('CTLH004', 'LH004', 'TC004', 'DV004', 'NV008'),
    ('CTLH005', 'LH005', 'TC005', 'DV005', 'NV009'),
    ('CTLH006', 'LH006', 'TC006', 'DV006', 'NV007'),
    ('CTLH007', 'LH007', 'TC007', 'DV001', 'NV008'),
    ('CTLH008', 'LH008', 'TC008', 'DV002', 'NV009'),
    ('CTLH009', 'LH009', 'TC009', 'DV003', 'NV007'),
    ('CTLH010', 'LH010', 'TC010', 'DV004', 'NV008'),
    ('CTLH011', 'LH011', 'TC011', 'DV005', 'NV009'),
    ('CTLH012', 'LH012', 'TC012', 'DV006', 'NV007'),
    ('CTLH013', 'LH013', 'TC013', 'DV001', 'NV008'),
    ('CTLH014', 'LH014', 'TC014', 'DV002', 'NV009'),
    ('CTLH015', 'LH015', 'TC015', 'DV003', 'NV007'),
    ('CTLH016', 'LH016', 'TC016', 'DV004', 'NV008'),
    ('CTLH017', 'LH017', 'TC017', 'DV005', 'NV009'),
    ('CTLH018', 'LH018', 'TC018', 'DV006', 'NV007'),
    ('CTLH019', 'LH019', 'TC019', 'DV001', 'NV008'),
    ('CTLH020', 'LH020', 'TC020', 'DV002', 'NV009'),
    ('CTLH021', 'LH021', 'TC021', 'DV003', 'NV007'),
    ('CTLH022', 'LH022', 'TC022', 'DV004', 'NV008'),
    ('CTLH023', 'LH023', 'TC023', 'DV005', 'NV009'),
    ('CTLH024', 'LH024', 'TC024', 'DV006', 'NV007'),
    ('CTLH025', 'LH025', 'TC025', 'DV001', 'NV008'),
    ('CTLH026', 'LH026', 'TC026', 'DV002', 'NV009'),
    ('CTLH027', 'LH027', 'TC027', 'DV003', 'NV007'),
    ('CTLH028', 'LH028', 'TC028', 'DV004', 'NV008'),
    ('CTLH029', 'LH029', 'TC029', 'DV005', 'NV009'),
    ('CTLH030', 'LH030', 'TC030', 'DV006', 'NV007'),
    ('CTLH031', 'LH031', 'TC031', 'DV001', 'NV008'),
    ('CTLH032', 'LH032', 'TC032', 'DV002', 'NV009'),
    ('CTLH033', 'LH033', 'TC033', 'DV003', 'NV007'),
    ('CTLH034', 'LH034', 'TC034', 'DV004', 'NV008'),
    ('CTLH035', 'LH035', 'TC035', 'DV005', 'NV009'),
    ('CTLH036', 'LH036', 'TC036', 'DV006', 'NV007'),
    ('CTLH037', 'LH037', 'TC037', 'DV001', 'NV008'),
    ('CTLH038', 'LH038', 'TC038', 'DV002', 'NV009'),
    ('CTLH039', 'LH039', 'TC039', 'DV003', 'NV007'),
    ('CTLH040', 'LH040', 'TC040', 'DV004', 'NV008'),
    ('CTLH041', 'LH041', 'TC041', 'DV005', 'NV009'),
    ('CTLH042', 'LH042', 'TC042', 'DV006', 'NV007'),
    ('CTLH043', 'LH043', 'TC043', 'DV001', 'NV008'),
    ('CTLH044', 'LH044', 'TC044', 'DV002', 'NV009'),
    ('CTLH045', 'LH045', 'TC045', 'DV003', 'NV007'),
    ('CTLH046', 'LH046', 'TC046', 'DV004', 'NV008'),
    ('CTLH047', 'LH047', 'TC047', 'DV005', 'NV009'),
    ('CTLH048', 'LH048', 'TC048', 'DV006', 'NV007'),
    ('CTLH049', 'LH049', 'TC049', 'DV001', 'NV008'),
    ('CTLH050', 'LH050', 'TC050', 'DV002', 'NV009');

    -- 24. BIÊN BẢN BÀN GIAO CA THU NGÂN (24 ca: đối soát khớp két 100%, 2 ca có ghi chú lý do chênh lệch)
    INSERT INTO BangGiaoCa (maGiaoCa, maNhanVien, maCa, tienDauCa, tienCuoiCaHeThong, tienCuoiCaThucTe, tienChenhLech, tienThuTienMat, tienChuyenKhoan, tienHoanTra, lyDoChenhLech, gioBatDauCa, gioKetThucThucTe, trangThai, maQuanLyDuyet, thoiGianDuyet) VALUES
    ('GC001', 'NV003', 'CA01', 2000000, 3084000, 3084000, 0, 1084000, 1200000, 0, NULL, '2026-10-27 07:00:00', '2026-10-27 11:30:00', 'DA_DUYET', 'NV001', '2026-10-27 11:45:00'),
    ('GC002', 'NV004', 'CA03', 2000000, 2000000, 2000000, 0, 0, 1020000, 0, NULL, '2026-10-27 15:30:00', '2026-10-27 19:30:00', 'DA_DUYET', 'NV002', '2026-10-27 19:45:00'),
    ('GC003', 'NV003', 'CA01', 2000000, 2530000, 2530000, 0, 530000, 0, 0, NULL, '2026-10-28 07:00:00', '2026-10-28 11:30:00', 'DA_DUYET', 'NV001', '2026-10-28 11:45:00'),
    ('GC004', 'NV004', 'CA03', 2000000, 3088000, 3088000, 0, 1088000, 1150000, 0, NULL, '2026-10-28 15:30:00', '2026-10-28 19:30:00', 'DA_DUYET', 'NV002', '2026-10-28 19:45:00'),
    ('GC005', 'NV003', 'CA01', 2000000, 3090000, 3090000, 0, 1090000, 1800000, 0, NULL, '2026-10-29 07:00:00', '2026-10-29 11:30:00', 'DA_DUYET', 'NV001', '2026-10-29 11:45:00'),
    ('GC006', 'NV004', 'CA03', 2000000, 2000000, 2000000, 0, 0, 425000, 0, NULL, '2026-10-29 15:30:00', '2026-10-29 19:30:00', 'DA_DUYET', 'NV002', '2026-10-29 19:45:00'),
    ('GC007', 'NV003', 'CA01', 2000000, 2720000, 2720000, 0, 720000, 0, 0, NULL, '2026-10-30 07:00:00', '2026-10-30 11:30:00', 'DA_DUYET', 'NV001', '2026-10-30 11:45:00'),
    ('GC008', 'NV004', 'CA03', 2000000, 2000000, 2000000, 0, 0, 2490000, 0, NULL, '2026-10-30 15:30:00', '2026-10-30 19:30:00', 'DA_DUYET', 'NV002', '2026-10-30 19:45:00'),
    ('GC009', 'NV003', 'CA01', 2000000, 2570000, 2570000, 0, 570000, 0, 0, NULL, '2026-10-31 07:00:00', '2026-10-31 11:30:00', 'DA_DUYET', 'NV001', '2026-10-31 11:45:00'),
    ('GC010', 'NV004', 'CA03', 2000000, 2000000, 2000000, 0, 0, 1350000, 0, NULL, '2026-10-31 15:30:00', '2026-10-31 19:30:00', 'DA_DUYET', 'NV002', '2026-10-31 19:45:00'),
    ('GC011', 'NV003', 'CA01', 2000000, 3029000, 3029000, 0, 1029000, 0, 0, NULL, '2026-11-01 07:00:00', '2026-11-01 11:30:00', 'DA_DUYET', 'NV001', '2026-11-01 11:45:00'),
    ('GC012', 'NV004', 'CA03', 2000000, 2000000, 2000000, 0, 0, 1360000, 0, NULL, '2026-11-01 15:30:00', '2026-11-01 19:30:00', 'DA_DUYET', 'NV002', '2026-11-01 19:45:00'),
    ('GC013', 'NV003', 'CA01', 2000000, 2810000, 2810000, 0, 810000, 0, 0, NULL, '2026-11-02 07:00:00', '2026-11-02 11:30:00', 'DA_DUYET', 'NV001', '2026-11-02 11:45:00'),
    ('GC014', 'NV004', 'CA03', 2000000, 2875000, 2875000, 0, 875000, 0, 0, NULL, '2026-11-02 15:30:00', '2026-11-02 19:30:00', 'DA_DUYET', 'NV002', '2026-11-02 19:45:00'),
    ('GC015', 'NV003', 'CA01', 2000000, 2000000, 2000000, 0, 0, 1530000, 0, NULL, '2026-11-03 07:00:00', '2026-11-03 11:30:00', 'DA_DUYET', 'NV001', '2026-11-03 11:45:00'),
    ('GC016', 'NV004', 'CA03', 2000000, 2770000, 2770000, 0, 770000, 0, 0, NULL, '2026-11-03 15:30:00', '2026-11-03 19:30:00', 'DA_DUYET', 'NV002', '2026-11-03 19:45:00'),
    ('GC017', 'NV003', 'CA01', 2000000, 2843000, 2843000, 0, 843000, 0, 0, NULL, '2026-11-04 07:00:00', '2026-11-04 11:30:00', 'DA_DUYET', 'NV001', '2026-11-04 11:45:00'),
    ('GC018', 'NV004', 'CA03', 2000000, 2000000, 2000000, 0, 0, 546000, 0, NULL, '2026-11-04 15:30:00', '2026-11-04 19:30:00', 'DA_DUYET', 'NV002', '2026-11-04 19:45:00'),
    ('GC019', 'NV003', 'CA01', 2000000, 3050000, 3050000, 0, 1050000, 0, 0, NULL, '2026-11-05 07:00:00', '2026-11-05 11:30:00', 'DA_DUYET', 'NV001', '2026-11-05 11:45:00'),
    ('GC020', 'NV004', 'CA03', 2000000, 3035000, 3035000, 0, 1035000, 0, 0, NULL, '2026-11-05 15:30:00', '2026-11-05 19:30:00', 'DA_DUYET', 'NV002', '2026-11-05 19:45:00'),
    ('GC021', 'NV003', 'CA01', 2000000, 1650000, 1650000, 0, 0, 880000, 350000, NULL, '2026-11-06 07:00:00', '2026-11-06 11:30:00', 'DA_DUYET', 'NV001', '2026-11-06 11:45:00'),
    ('GC022', 'NV004', 'CA03', 2000000, 3475000, 3475000, 0, 1475000, 2630000, 0, NULL, '2026-11-06 15:30:00', '2026-11-06 19:30:00', 'DA_DUYET', 'NV002', '2026-11-06 19:45:00'),
    ('GC023', 'NV003', 'CA01', 2000000, 2495000, 2485000, -10000, 495000, 0, 0, N'Thối nhầm tiền thừa cho khách hàng mua hàng', '2026-11-07 07:00:00', '2026-11-07 11:30:00', 'DA_DUYET', 'NV001', '2026-11-07 11:45:00'),
    ('GC024', 'NV004', 'CA03', 2000000, 2030000, 2050000, 20000, 30000, 0, 0, N'Khách boa tiền lẻ vào két thu ngân', '2026-11-07 15:30:00', '2026-11-07 19:30:00', 'DA_DUYET', 'NV002', '2026-11-07 19:45:00');

    -- 25. CẬP NHẬT TIỀN TỐ TRONG BẢNG CẤP SỐ (Đảm bảo Java Swing tự sinh mã tiếp theo không trùng khóa)
    UPDATE BangCapSo SET soHienTai = 10 WHERE loaiMa = 'NV';
    UPDATE BangCapSo SET soHienTai = 50 WHERE loaiMa = 'SP';
    UPDATE BangCapSo SET soHienTai = 8  WHERE loaiMa = 'DV';
    UPDATE BangCapSo SET soHienTai = 50 WHERE loaiMa = 'KH';
    UPDATE BangCapSo SET soHienTai = 50 WHERE loaiMa = 'TC';
    UPDATE BangCapSo SET soHienTai = 50 WHERE loaiMa = 'HS';
    UPDATE BangCapSo SET soHienTai = 50 WHERE loaiMa = 'SK';
    UPDATE BangCapSo SET soHienTai = 8  WHERE loaiMa = 'DVT';
    UPDATE BangCapSo SET soHienTai = 75 WHERE loaiMa = 'QD';
    UPDATE BangCapSo SET soHienTai = 24 WHERE loaiMa = 'GC';
    UPDATE BangCapSo SET soHienTai = 48 WHERE loaiMa = 'PC';
    UPDATE BangCapSo SET soHienTai = 50 WHERE loaiMa = 'HD';
    UPDATE BangCapSo SET soHienTai = 2  WHERE loaiMa = 'DT';

    COMMIT TRANSACTION;
    PRINT N'>>> ĐÃ NẠP THÀNH CÔNG BỘ DỮ LIỆU MẪU ĐỒNG BỘ THỰC TẾ PETSHOP MIO!';
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
ELSE
BEGIN
    PRINT N'>>> CHẾ ĐỘ THỰC THI HIỆN TẠI LÀ SCHEMA_ONLY. BỎ QUA NẠP DEMO DATA AN TOÀN.';
END
GO

PRINT N'======================================================================================';
PRINT N'ĐÃ HOÀN TẤT THỰC THI SCRIPT HETHONGQUANLYTHUCUNG.SQL THÀNH CÔNG!';
PRINT N'======================================================================================';
GO

