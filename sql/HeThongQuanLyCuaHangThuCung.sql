-- ======================================================================================
-- DỰ ÁN MÔN HỌC: PHÁT TRIỂN ỨNG DỤNG (APPLICATION DEVELOPMENT - AD)
-- HỌC KỲ: HK1 / 2026-2027 | NHÓM: 07 (N07)
-- ĐỀ TÀI: XÂY DỰNG HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG (Pet Shop Management System)
-- DBMS: MICROSOFT SQL SERVER
-- TẬP LỆNH: 01_schema.sql (DDL Tạo bảng và ràng buộc toàn vẹn)
-- ======================================================================================

USE master;
GO

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'QuanLyCuaHangThuCung')
BEGIN
    CREATE DATABASE QuanLyCuaHangThuCung;
END
GO

USE QuanLyCuaHangThuCung;
GO

-- XÓA BẢNG NẾU ĐÃ TỒN TẠI THEO THỨ TỰ RÀNG BUỘC KHÓA NGOẠI
IF OBJECT_ID('PhongLuuTru', 'U') IS NOT NULL DROP TABLE PhongLuuTru;
IF OBJECT_ID('ChiTietDoiTra', 'U') IS NOT NULL DROP TABLE ChiTietDoiTra;
IF OBJECT_ID('PhieuDoiTra', 'U') IS NOT NULL DROP TABLE PhieuDoiTra;
IF OBJECT_ID('ChiTietDichVu', 'U') IS NOT NULL DROP TABLE ChiTietDichVu;
IF OBJECT_ID('ChiTietXuatLo', 'U') IS NOT NULL DROP TABLE ChiTietXuatLo;
IF OBJECT_ID('ChiTietHoaDon', 'U') IS NOT NULL DROP TABLE ChiTietHoaDon;
IF OBJECT_ID('HoaDon', 'U') IS NOT NULL DROP TABLE HoaDon;
IF OBJECT_ID('ChiTietLichHen', 'U') IS NOT NULL DROP TABLE ChiTietLichHen;
IF OBJECT_ID('LichHen', 'U') IS NOT NULL DROP TABLE LichHen;
IF OBJECT_ID('HoSoSucKhoe', 'U') IS NOT NULL DROP TABLE HoSoSucKhoe;
IF OBJECT_ID('HoSoThuCung', 'U') IS NOT NULL DROP TABLE HoSoThuCung;
IF OBJECT_ID('ThuCung', 'U') IS NOT NULL DROP TABLE ThuCung;
IF OBJECT_ID('KhachHang', 'U') IS NOT NULL DROP TABLE KhachHang;
IF OBJECT_ID('DichVu', 'U') IS NOT NULL DROP TABLE DichVu;
IF OBJECT_ID('LoHang', 'U') IS NOT NULL DROP TABLE LoHang;
IF OBJECT_ID('NhaCungCap', 'U') IS NOT NULL DROP TABLE NhaCungCap;
IF OBJECT_ID('QuyDoiDonVi', 'U') IS NOT NULL DROP TABLE QuyDoiDonVi;
IF OBJECT_ID('DonViTinh', 'U') IS NOT NULL DROP TABLE DonViTinh;
IF OBJECT_ID('SanPham', 'U') IS NOT NULL DROP TABLE SanPham;
IF OBJECT_ID('DanhMucHang', 'U') IS NOT NULL DROP TABLE DanhMucHang;
IF OBJECT_ID('BangGiaoCa', 'U') IS NOT NULL DROP TABLE BangGiaoCa;
IF OBJECT_ID('BangPhanCong', 'U') IS NOT NULL DROP TABLE BangPhanCong;
IF OBJECT_ID('CaLamViec', 'U') IS NOT NULL DROP TABLE CaLamViec;
IF OBJECT_ID('NhanVien', 'U') IS NOT NULL DROP TABLE NhanVien;
IF OBJECT_ID('TaiKhoan', 'U') IS NOT NULL DROP TABLE TaiKhoan;
GO

-- ======================================================================================
-- 1. PHÂN HỆ NHÂN SỰ & CA LÀM VIỆC
-- ======================================================================================

CREATE TABLE TaiKhoan (
    tenDangNhap NVARCHAR(50) PRIMARY KEY,
    matKhau NVARCHAR(255) NOT NULL,
    vaiTro NVARCHAR(50) NOT NULL, -- Admin, NhanVienThuNgan, KyThuatVien
    trangThaiHoatDong BIT NOT NULL DEFAULT 1
);
GO

CREATE TABLE NhanVien (
    maNhanVien NVARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    chucVu NVARCHAR(50) NOT NULL,
    soDienThoai NVARCHAR(15),
    tenDangNhap NVARCHAR(50) UNIQUE,
    CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (tenDangNhap) REFERENCES TaiKhoan(tenDangNhap)
);
GO

CREATE TABLE CaLamViec (
    maCa NVARCHAR(20) PRIMARY KEY,
    tenCa NVARCHAR(50) NOT NULL,
    gioBatDau TIME NOT NULL,
    gioKetThuc TIME NOT NULL
);
GO

CREATE TABLE BangPhanCong (
    maPhanCong NVARCHAR(20) PRIMARY KEY,
    maNhanVien NVARCHAR(20) NOT NULL,
    maCa NVARCHAR(20) NOT NULL,
    ngayLamViec DATE NOT NULL,
    trangThaiCa NVARCHAR(50) DEFAULT N'Chưa bắt đầu',
    ghiChu NVARCHAR(255) NULL,
    CONSTRAINT FK_PhanCong_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_PhanCong_CaLamViec FOREIGN KEY (maCa) REFERENCES CaLamViec(maCa)
);
GO

CREATE TABLE BangGiaoCa (
    maGiaoCa NVARCHAR(20) PRIMARY KEY,
    maNhanVien NVARCHAR(20) NOT NULL,
    maCa NVARCHAR(20) NOT NULL,
    tienDauCa DECIMAL(18,2) NOT NULL DEFAULT 0,
    tienCuoiCaHeThong DECIMAL(18,2) NOT NULL DEFAULT 0,
    tienCuoiCaThucTe DECIMAL(18,2) NOT NULL DEFAULT 0,
    tienChenhLech DECIMAL(18,2) NOT NULL DEFAULT 0,
    lyDoChenhLech NVARCHAR(255),
    gioKetThucThucTe DATETIME2,
    CONSTRAINT FK_GiaoCa_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_GiaoCa_CaLamViec FOREIGN KEY (maCa) REFERENCES CaLamViec(maCa)
);
GO

-- ======================================================================================
-- 2. PHÂN HỆ KHÁCH HÀNG & THÚ CƯNG
-- ======================================================================================

CREATE TABLE KhachHang (
    maKhachHang NVARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDienThoai NVARCHAR(15) NOT NULL UNIQUE,
    diemTichLuy INT NOT NULL DEFAULT 0,
    email NVARCHAR(100),
    diaChi NVARCHAR(255)
);
GO

CREATE TABLE ThuCung (
    maThuCung NVARCHAR(20) PRIMARY KEY,
    tenThuCung NVARCHAR(100) NOT NULL,
    giongLoai NVARCHAR(50),
    ngaySinh DATE,
    gioiTinh NVARCHAR(10) -- Đực, Cái
);
GO

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
GO

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
GO

-- ======================================================================================
-- 3. PHÂN HỆ DỊCH VỤ & LỊCH HẸN
-- ======================================================================================

CREATE TABLE DichVu (
    maDichVu NVARCHAR(20) PRIMARY KEY,
    tenDichVu NVARCHAR(100) NOT NULL,
    giaDichVu DECIMAL(18,2) NOT NULL,
    thoiLuongPhut INT NOT NULL
);
GO

CREATE TABLE LichHen (
    maLichHen NVARCHAR(20) PRIMARY KEY,
    maKhachHang NVARCHAR(20) NOT NULL,
    maNhanVienTiepNhan NVARCHAR(20),
    ngayHen DATETIME2 NOT NULL,
    trangThaiLichHen NVARCHAR(50) DEFAULT N'Đã đặt', -- Đã đặt, Đang phục vụ, Hoàn thành, Hủy
    ghiChu NVARCHAR(255),
    CONSTRAINT FK_LichHen_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    CONSTRAINT FK_LichHen_NhanVien FOREIGN KEY (maNhanVienTiepNhan) REFERENCES NhanVien(maNhanVien)
);
GO

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
GO

-- ======================================================================================
-- 4. PHÂN HỆ SẢN PHẨM & KHO & ĐƠN VỊ TÍNH
-- ======================================================================================

CREATE TABLE DanhMucHang (
    maDanhMuc NVARCHAR(20) PRIMARY KEY,
    tenDanhMuc NVARCHAR(100) NOT NULL,
    moTa NVARCHAR(255)
);
GO

CREATE TABLE SanPham (
    maSanPham NVARCHAR(20) PRIMARY KEY,
    maDanhMuc NVARCHAR(20) NOT NULL,
    tenSanPham NVARCHAR(150) NOT NULL,
    moTa NVARCHAR(255),
    CONSTRAINT FK_SanPham_DanhMuc FOREIGN KEY (maDanhMuc) REFERENCES DanhMucHang(maDanhMuc)
);
GO

CREATE TABLE DonViTinh (
    maDonVi NVARCHAR(20) PRIMARY KEY,
    tenDonVi NVARCHAR(50) NOT NULL
);
GO

CREATE TABLE QuyDoiDonVi (
    maQuyDoi NVARCHAR(20) PRIMARY KEY,
    maSanPham NVARCHAR(20) NOT NULL,
    maDonVi NVARCHAR(20) NOT NULL,
    tyLeQuyDoi INT NOT NULL DEFAULT 1,
    laDonViCoSo BIT NOT NULL DEFAULT 0,
    giaBan DECIMAL(18,2) NOT NULL,
    CONSTRAINT FK_QuyDoi_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham),
    CONSTRAINT FK_QuyDoi_DonVi FOREIGN KEY (maDonVi) REFERENCES DonViTinh(maDonVi)
);
GO

CREATE TABLE NhaCungCap (
    maNhaCungCap NVARCHAR(20) PRIMARY KEY,
    tenNhaCungCap NVARCHAR(150) NOT NULL,
    diaChi NVARCHAR(255),
    soDienThoai NVARCHAR(15)
);
GO

CREATE TABLE LoHang (
    soLo NVARCHAR(50) PRIMARY KEY,
    maSanPham NVARCHAR(20) NOT NULL,
    maNhaCungCap NVARCHAR(20) NOT NULL,
    ngayNhap DATETIME2 NOT NULL CONSTRAINT DF_LoHang_NgayNhap DEFAULT SYSDATETIME(),
    ngaySanXuat DATE,
    hanSuDung DATE NOT NULL,
    donGiaNhapCoSo DECIMAL(18,2) NOT NULL,
    soLuongNhap INT NOT NULL,
    soLuongTon INT NOT NULL,
    CONSTRAINT FK_LoHang_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham),
    CONSTRAINT FK_LoHang_NhaCungCap FOREIGN KEY (maNhaCungCap) REFERENCES NhaCungCap(maNhaCungCap),
    CONSTRAINT CK_LoHang_SoLuongTon CHECK (soLuongTon >= 0)
);
GO

-- ======================================================================================
-- 5. PHÂN HỆ BÁN HÀNG, HÓA ĐƠN (ĐA HÌNH) & ĐỔI TRẢ
-- ======================================================================================

CREATE TABLE HoaDon (
    maHoaDon NVARCHAR(20) PRIMARY KEY,
    maKhachHang NVARCHAR(20),
    maNhanVien NVARCHAR(20) NOT NULL,
    ngayLap DATETIME2 NOT NULL,
    thoiGianThanhToan DATETIME2,
    giamGia DECIMAL(18,2) DEFAULT 0,
    thanhTien DECIMAL(18,2) NOT NULL DEFAULT 0,
    phuongThucThanhToan NVARCHAR(50), -- Tiền mặt, Chuyển khoản, Thẻ
    trangThaiHoaDon NVARCHAR(50) NOT NULL, -- Chờ thanh toán, Đã thanh toán, Đã hủy
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
GO

-- Bảng cha Đa hình: ChiTietHoaDon
CREATE TABLE ChiTietHoaDon (
    maChiTiet NVARCHAR(20) PRIMARY KEY,
    maHoaDon NVARCHAR(20) NOT NULL,
    loaiChiTiet NVARCHAR(20) NOT NULL, -- XUAT_LO / DICH_VU
    soLuong INT NOT NULL,
    donGiaLucBan DECIMAL(18,2) NOT NULL,
    thanhTien DECIMAL(18,2) NOT NULL,
    CONSTRAINT FK_ChiTiet_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE
);
GO

-- Lớp con 1: ChiTietXuatLo (Bán hàng theo Lô)
CREATE TABLE ChiTietXuatLo (
    maChiTiet NVARCHAR(20) PRIMARY KEY,
    soLo NVARCHAR(50) NOT NULL,
    maQuyDoi NVARCHAR(20) NULL,
    soLuongXuatCoSo INT NOT NULL,
    CONSTRAINT FK_XuatLo_ChiTietHoaDon FOREIGN KEY (maChiTiet) REFERENCES ChiTietHoaDon(maChiTiet) ON DELETE CASCADE,
    CONSTRAINT FK_XuatLo_LoHang FOREIGN KEY (soLo) REFERENCES LoHang(soLo),
    CONSTRAINT FK_XuatLo_QuyDoi FOREIGN KEY (maQuyDoi) REFERENCES QuyDoiDonVi(maQuyDoi)
);
GO

-- Lớp con 2: ChiTietDichVu (Làm dịch vụ thú cưng)
CREATE TABLE ChiTietDichVu (
    maChiTiet NVARCHAR(20) PRIMARY KEY,
    maDichVu NVARCHAR(20) NOT NULL,
    maThuCung NVARCHAR(20) NULL,
    maKtvThucHien NVARCHAR(20),
    tienHoaHong DECIMAL(18,2) DEFAULT 0,
    CONSTRAINT FK_CTDichVu_ChiTietHoaDon FOREIGN KEY (maChiTiet) REFERENCES ChiTietHoaDon(maChiTiet) ON DELETE CASCADE,
    CONSTRAINT FK_CTDichVu_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu),
    CONSTRAINT FK_CTDichVu_ThuCung FOREIGN KEY (maThuCung) REFERENCES ThuCung(maThuCung),
    CONSTRAINT FK_CTDichVu_NhanVien FOREIGN KEY (maKtvThucHien) REFERENCES NhanVien(maNhanVien)
);
GO

-- Phân hệ Đổi - Trả hàng
CREATE TABLE PhieuDoiTra (
    maPhieuDoiTra NVARCHAR(20) PRIMARY KEY,
    maHoaDonGoc NVARCHAR(20) NOT NULL,
    maNhanVien NVARCHAR(20) NOT NULL,
    ngayDoiTra DATETIME2 NOT NULL,
    lyDoDoiTra NVARCHAR(255),
    tongTienHoan DECIMAL(18,2) NOT NULL DEFAULT 0,
    trangThaiDoiTra NVARCHAR(50) DEFAULT N'Hoàn thành',
    CONSTRAINT FK_DoiTra_HoaDon FOREIGN KEY (maHoaDonGoc) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT FK_DoiTra_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
GO

CREATE TABLE ChiTietDoiTra (
    maChiTietDoiTra NVARCHAR(20) PRIMARY KEY,
    maPhieuDoiTra NVARCHAR(20) NOT NULL,
    maSanPham NVARCHAR(20) NOT NULL,
    soLuongDoiTra INT NOT NULL,
    tienHoanLai DECIMAL(18,2) NOT NULL,
    CONSTRAINT FK_CTDoiTra_Phieu FOREIGN KEY (maPhieuDoiTra) REFERENCES PhieuDoiTra(maPhieuDoiTra) ON DELETE CASCADE,
    CONSTRAINT FK_CTDoiTra_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham)
);
GO

-- Phân hệ Khách sạn thú cưng (Pet Hotel)
CREATE TABLE PhongLuuTru (
    maPhong NVARCHAR(20) PRIMARY KEY,
    loaiPhong NVARCHAR(50) NOT NULL,
    donGiaNgay BIGINT NOT NULL,
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
    phiDichVuPhatSinh BIGINT NOT NULL DEFAULT 0
);
GO

PRINT N'Hoàn thành khởi tạo Schema cơ sở dữ liệu QuanLyCuaHangThuCung thành công!';
GO
-- ======================================================================================
-- TẬP LỆNH: 02_sample_data.sql (Dữ liệu mẫu kiểm thử hệ thống đầy đủ cho toàn bộ phân hệ)
-- ======================================================================================

USE QuanLyCuaHangThuCung;
GO

-- Đảm bảo cột ghiChu tồn tại trong BangPhanCong và bảng PhongLuuTru tồn tại
IF COL_LENGTH('BangPhanCong', 'ghiChu') IS NULL
    ALTER TABLE BangPhanCong ADD ghiChu NVARCHAR(255) NULL;
GO

IF OBJECT_ID('PhongLuuTru', 'U') IS NULL
CREATE TABLE PhongLuuTru (
    maPhong NVARCHAR(20) PRIMARY KEY,
    loaiPhong NVARCHAR(50) NOT NULL,
    donGiaNgay BIGINT NOT NULL,
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
    phiDichVuPhatSinh BIGINT NOT NULL DEFAULT 0
);
GO

-- 1. Tài khoản & Nhân viên (4 Vai trò: NVQL, NVBH, NVCS, NVTN)
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE tenDangNhap = 'admin')
    INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThaiHoatDong) VALUES ('admin', '123456', 'NVQL', 1);
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE tenDangNhap = 'thungan01')
    INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThaiHoatDong) VALUES ('thungan01', '123456', 'NVTN', 1);
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE tenDangNhap = 'ktv01')
    INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThaiHoatDong) VALUES ('ktv01', '123456', 'NVCS', 1);
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE tenDangNhap = 'banhang01')
    INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThaiHoatDong) VALUES ('banhang01', '123456', 'NVBH', 1);
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE tenDangNhap = 'ktv02')
    INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThaiHoatDong) VALUES ('ktv02', '123456', 'NVCS', 1);
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE tenDangNhap = 'banhang02')
    INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThaiHoatDong) VALUES ('banhang02', '123456', 'NVBH', 0);

IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNhanVien = 'NV001')
    INSERT INTO NhanVien (maNhanVien, hoTen, chucVu, soDienThoai, tenDangNhap) VALUES ('NV001', N'Lê Khãi Minh', N'Quản lý hệ thống', '0901234567', 'admin');
IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNhanVien = 'NV002')
    INSERT INTO NhanVien (maNhanVien, hoTen, chucVu, soDienThoai, tenDangNhap) VALUES ('NV002', N'Lê Anh Minh', N'Thu ngân ca sáng', '0912345678', 'thungan01');
IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNhanVien = 'NV003')
    INSERT INTO NhanVien (maNhanVien, hoTen, chucVu, soDienThoai, tenDangNhap) VALUES ('NV003', N'Tô Thanh Đạt', N'Kỹ thuật viên chăm sóc', '0923456789', 'ktv01');
IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNhanVien = 'NV004')
    INSERT INTO NhanVien (maNhanVien, hoTen, chucVu, soDienThoai, tenDangNhap) VALUES ('NV004', N'Trần Thị Thu Hà', N'Nhân viên bán hàng', '0934567890', 'banhang01');
IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNhanVien = 'NV005')
    INSERT INTO NhanVien (maNhanVien, hoTen, chucVu, soDienThoai, tenDangNhap) VALUES ('NV005', N'Võ Thị Hồng Ngọc', N'Chuyên viên Spa thú cưng', '0945678901', 'ktv02');
IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNhanVien = 'NV006')
    INSERT INTO NhanVien (maNhanVien, hoTen, chucVu, soDienThoai, tenDangNhap) VALUES ('NV006', N'Lê Anh Tuấn', N'Nhân viên bán hàng', '0956789012', 'banhang02');

-- 2. Ca làm việc (4 ca cố định)
IF NOT EXISTS (SELECT 1 FROM CaLamViec WHERE maCa = 'CA01')
    INSERT INTO CaLamViec (maCa, tenCa, gioBatDau, gioKetThuc) VALUES ('CA01', N'Ca 1 (07:00 - 11:30)', '07:00:00', '11:30:00');
IF NOT EXISTS (SELECT 1 FROM CaLamViec WHERE maCa = 'CA02')
    INSERT INTO CaLamViec (maCa, tenCa, gioBatDau, gioKetThuc) VALUES ('CA02', N'Ca 2 (11:30 - 15:30)', '11:30:00', '15:30:00');
IF NOT EXISTS (SELECT 1 FROM CaLamViec WHERE maCa = 'CA03')
    INSERT INTO CaLamViec (maCa, tenCa, gioBatDau, gioKetThuc) VALUES ('CA03', N'Ca 3 (15:30 - 19:30)', '15:30:00', '19:30:00');
IF NOT EXISTS (SELECT 1 FROM CaLamViec WHERE maCa = 'CA04')
    INSERT INTO CaLamViec (maCa, tenCa, gioBatDau, gioKetThuc) VALUES ('CA04', N'Ca 4 (19:30 - 22:00)', '19:30:00', '22:00:00');

-- 3. Khách hàng & Thú cưng
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKhachHang = 'KH001')
    INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, diemTichLuy) VALUES ('KH001', N'Nguyễn Văn An', '0988111222', 120);
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKhachHang = 'KH002')
    INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, diemTichLuy) VALUES ('KH002', N'Trần Thị Mai', '0977333444', 50);
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKhachHang = 'KH003')
    INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, diemTichLuy) VALUES ('KH003', N'Lê Hoàng Yến', '0912345678', 15);
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKhachHang = 'KH004')
    INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, diemTichLuy) VALUES ('KH004', N'Phạm Hoàng Long', '0918776655', 210);
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKhachHang = 'KH005')
    INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, diemTichLuy) VALUES ('KH005', N'Đỗ Quỳnh Chi', '0905123456', 35);

IF NOT EXISTS (SELECT 1 FROM ThuCung WHERE maThuCung = 'TC001')
    INSERT INTO ThuCung (maThuCung, tenThuCung, giongLoai, ngaySinh, gioiTinh) VALUES ('TC001', N'Miu Miu', N'Mèo Anh Lông Ngắn', '2023-05-10', N'Cái');
IF NOT EXISTS (SELECT 1 FROM ThuCung WHERE maThuCung = 'TC002')
    INSERT INTO ThuCung (maThuCung, tenThuCung, giongLoai, ngaySinh, gioiTinh) VALUES ('TC002', N'Lu Lu', N'Chó Poodle', '2022-11-20', N'Đực');
IF NOT EXISTS (SELECT 1 FROM ThuCung WHERE maThuCung = 'TC003')
    INSERT INTO ThuCung (maThuCung, tenThuCung, giongLoai, ngaySinh, gioiTinh) VALUES ('TC003', N'Bông', N'Chó Pomeranian (Phốc Sóc)', '2024-02-14', N'Cái');
IF NOT EXISTS (SELECT 1 FROM ThuCung WHERE maThuCung = 'TC004')
    INSERT INTO ThuCung (maThuCung, tenThuCung, giongLoai, ngaySinh, gioiTinh) VALUES ('TC004', N'Bim Bim', N'Chó Corgi', '2023-08-05', N'Đực');

IF NOT EXISTS (SELECT 1 FROM HoSoThuCung WHERE maHoSo = 'HS001')
    INSERT INTO HoSoThuCung (maHoSo, maKhachHang, maThuCung, soMicrochip, ngayDangKy, trangThaiNuoi) VALUES ('HS001', 'KH001', 'TC001', 'MC982001', '2023-06-01', N'Đang nuôi');
IF NOT EXISTS (SELECT 1 FROM HoSoThuCung WHERE maHoSo = 'HS002')
    INSERT INTO HoSoThuCung (maHoSo, maKhachHang, maThuCung, soMicrochip, ngayDangKy, trangThaiNuoi) VALUES ('HS002', 'KH002', 'TC002', 'MC982002', '2023-01-15', N'Đang nuôi');
IF NOT EXISTS (SELECT 1 FROM HoSoThuCung WHERE maHoSo = 'HS003')
    INSERT INTO HoSoThuCung (maHoSo, maKhachHang, maThuCung, soMicrochip, ngayDangKy, trangThaiNuoi) VALUES ('HS003', 'KH003', 'TC003', 'MC982003', '2024-03-01', N'Đang nuôi');
IF NOT EXISTS (SELECT 1 FROM HoSoThuCung WHERE maHoSo = 'HS004')
    INSERT INTO HoSoThuCung (maHoSo, maKhachHang, maThuCung, soMicrochip, ngayDangKy, trangThaiNuoi) VALUES ('HS004', 'KH001', 'TC004', 'MC982004', '2023-09-10', N'Đang nuôi');

IF NOT EXISTS (SELECT 1 FROM HoSoSucKhoe WHERE maSucKhoe = 'SK001')
    INSERT INTO HoSoSucKhoe (maSucKhoe, maThuCung, maNhanVienKiemTra, canNang, nhietDo, tinhTrangVeRan, tiemPhongDai, ghiChuTheTrang, ngayKiemTra)
    VALUES ('SK001', 'TC001', 'NV003', 4.20, 38.50, N'Không có ve rận', 1, N'Thể trạng tốt, lông mượt, đủ điều kiện tắm sấy', DATEADD(day, -2, GETDATE()));
IF NOT EXISTS (SELECT 1 FROM HoSoSucKhoe WHERE maSucKhoe = 'SK002')
    INSERT INTO HoSoSucKhoe (maSucKhoe, maThuCung, maNhanVienKiemTra, canNang, nhietDo, tinhTrangVeRan, tiemPhongDai, ghiChuTheTrang, ngayKiemTra)
    VALUES ('SK002', 'TC002', 'NV003', 5.80, 38.70, N'Có ít ve ở tai trái', 1, N'Khuyến nghị gói tắm thảo dược trị ve rận', DATEADD(day, -1, GETDATE()));
IF NOT EXISTS (SELECT 1 FROM HoSoSucKhoe WHERE maSucKhoe = 'SK003')
    INSERT INTO HoSoSucKhoe (maSucKhoe, maThuCung, maNhanVienKiemTra, canNang, nhietDo, tinhTrangVeRan, tiemPhongDai, ghiChuTheTrang, ngayKiemTra)
    VALUES ('SK003', 'TC003', 'NV003', 2.40, 38.40, N'Sạch sẽ', 1, N'Chó nhỏ hiếu động, cần sấy nhiệt độ ấm vừa phải', DATEADD(hour, -4, GETDATE()));

-- 4. Dịch vụ chăm sóc
IF NOT EXISTS (SELECT 1 FROM DichVu WHERE maDichVu = 'DV001')
    INSERT INTO DichVu (maDichVu, tenDichVu, giaDichVu, thoiLuongPhut) VALUES ('DV001', N'Cắt Tỉa Lông Tạo Kiểu Thẩm Mỹ', 300000, 90);
IF NOT EXISTS (SELECT 1 FROM DichVu WHERE maDichVu = 'DV002')
    INSERT INTO DichVu (maDichVu, tenDichVu, giaDichVu, thoiLuongPhut) VALUES ('DV002', N'Cắt Mài Móng & Vệ Sinh Tai', 100000, 30);
IF NOT EXISTS (SELECT 1 FROM DichVu WHERE maDichVu = 'DV003')
    INSERT INTO DichVu (maDichVu, tenDichVu, giaDichVu, thoiLuongPhut) VALUES ('DV003', N'Vắt Tuyến Hôi & Cạo Bàn Chân', 90000, 30);
IF NOT EXISTS (SELECT 1 FROM DichVu WHERE maDichVu = 'DV004')
    INSERT INTO DichVu (maDichVu, tenDichVu, giaDichVu, thoiLuongPhut) VALUES ('DV004', N'Tắm Sấy Khử Mùi & Dưỡng Lông', 150000, 45);
IF NOT EXISTS (SELECT 1 FROM DichVu WHERE maDichVu = 'DV005')
    INSERT INTO DichVu (maDichVu, tenDichVu, giaDichVu, thoiLuongPhut) VALUES ('DV005', N'Tắm Thảo Dược & Trị Ve Rận', 220000, 60);
IF NOT EXISTS (SELECT 1 FROM DichVu WHERE maDichVu = 'DV006')
    INSERT INTO DichVu (maDichVu, tenDichVu, giaDichVu, thoiLuongPhut) VALUES ('DV006', N'Ngâm Bồn Sục Ozone Thư Giãn', 250000, 60);
IF NOT EXISTS (SELECT 1 FROM DichVu WHERE maDichVu = 'DV007')
    INSERT INTO DichVu (maDichVu, tenDichVu, giaDichVu, thoiLuongPhut) VALUES ('DV007', N'Nhuộm Lông Nghệ Thuật (Tai & Đuôi)', 180000, 60);
IF NOT EXISTS (SELECT 1 FROM DichVu WHERE maDichVu = 'DV008')
    INSERT INTO DichVu (maDichVu, tenDichVu, giaDichVu, thoiLuongPhut) VALUES ('DV008', N'Lưu Chuồng Pet Hotel (VIP)', 200000, 1440);

-- 5. Lịch hẹn chăm sóc mẫu
IF NOT EXISTS (SELECT 1 FROM LichHen WHERE maLichHen = 'LH001')
BEGIN
    INSERT INTO LichHen (maLichHen, maKhachHang, maNhanVienTiepNhan, ngayHen, trangThaiLichHen, ghiChu)
    VALUES ('LH001', 'KH001', 'NV003', DATEADD(hour, 1, GETDATE()), N'Đã đặt', N'Khách dặn cắt tỉa gọn vùng mắt');
    INSERT INTO ChiTietLichHen (maChiTietLichHen, maLichHen, maThuCung, maDichVu, maKtvChiDinh)
    VALUES ('CTLH01', 'LH001', 'TC001', 'DV001', 'NV003');
END

IF NOT EXISTS (SELECT 1 FROM LichHen WHERE maLichHen = 'LH002')
BEGIN
    INSERT INTO LichHen (maLichHen, maKhachHang, maNhanVienTiepNhan, ngayHen, trangThaiLichHen, ghiChu)
    VALUES ('LH002', 'KH002', 'NV003', DATEADD(minute, -30, GETDATE()), N'Đang phục vụ', N'Đang sấy bông và chải lông');
    INSERT INTO ChiTietLichHen (maChiTietLichHen, maLichHen, maThuCung, maDichVu, maKtvChiDinh)
    VALUES ('CTLH02', 'LH002', 'TC002', 'DV002', 'NV003');
END

IF NOT EXISTS (SELECT 1 FROM LichHen WHERE maLichHen = 'LH003')
BEGIN
    INSERT INTO LichHen (maLichHen, maKhachHang, maNhanVienTiepNhan, ngayHen, trangThaiLichHen, ghiChu)
    VALUES ('LH003', 'KH003', 'NV003', DATEADD(hour, -3, GETDATE()), N'Hoàn thành', N'Đã bàn giao thú cưng cho chủ');
    INSERT INTO ChiTietLichHen (maChiTietLichHen, maLichHen, maThuCung, maDichVu, maKtvChiDinh)
    VALUES ('CTLH03', 'LH003', 'TC003', 'DV003', 'NV003');
END

IF NOT EXISTS (SELECT 1 FROM LichHen WHERE maLichHen = 'LH004')
BEGIN
    INSERT INTO LichHen (maLichHen, maKhachHang, maNhanVienTiepNhan, ngayHen, trangThaiLichHen, ghiChu)
    VALUES ('LH004', 'KH001', 'NV003', DATEADD(hour, 3, GETDATE()), N'Đã đặt', N'Tắm khử mùi cho bé Corgi');
    INSERT INTO ChiTietLichHen (maChiTietLichHen, maLichHen, maThuCung, maDichVu, maKtvChiDinh)
    VALUES ('CTLH04', 'LH004', 'TC004', 'DV001', 'NV003');
END

-- 6. Danh mục, Sản phẩm, Đơn vị tính & Quy đổi
IF NOT EXISTS (SELECT 1 FROM DanhMucHang WHERE maDanhMuc = 'DM01')
    INSERT INTO DanhMucHang (maDanhMuc, tenDanhMuc, moTa) VALUES ('DM01', N'Thức ăn thú cưng', N'Hạt khô, pate dinh dưỡng');
IF NOT EXISTS (SELECT 1 FROM DanhMucHang WHERE maDanhMuc = 'DM02')
    INSERT INTO DanhMucHang (maDanhMuc, tenDanhMuc, moTa) VALUES ('DM02', N'Phụ kiện & Đồ chơi', N'Vòng cổ, đồ cào móng, dây dắt');
IF NOT EXISTS (SELECT 1 FROM DanhMucHang WHERE maDanhMuc = 'DM03')
    INSERT INTO DanhMucHang (maDanhMuc, tenDanhMuc, moTa) VALUES ('DM03', N'Vệ sinh & Chăm sóc', N'Cát vệ sinh, sữa tắm, xịt khử mùi');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE maSanPham = 'SP001')
    INSERT INTO SanPham (maSanPham, maDanhMuc, tenSanPham, moTa) VALUES ('SP001', 'DM01', N'Hạt Royal Canin Kitten 2kg', N'Dành cho mèo con dưới 12 tháng');
IF NOT EXISTS (SELECT 1 FROM SanPham WHERE maSanPham = 'SP002')
    INSERT INTO SanPham (maSanPham, maDanhMuc, tenSanPham, moTa) VALUES ('SP002', 'DM01', N'Pate Whiskas Vị Cá Thu 85g', N'Bổ sung dinh dưỡng cho mèo trưởng thành');
IF NOT EXISTS (SELECT 1 FROM SanPham WHERE maSanPham = 'SP003')
    INSERT INTO SanPham (maSanPham, maDanhMuc, tenSanPham, moTa) VALUES ('SP003', 'DM01', N'Pate Whiskas (Lốc 6 lon)', N'Combo 6 lon tiết kiệm');
IF NOT EXISTS (SELECT 1 FROM SanPham WHERE maSanPham = 'SP004')
    INSERT INTO SanPham (maSanPham, maDanhMuc, tenSanPham, moTa) VALUES ('SP004', 'DM02', N'Vòng Cổ Chuông Nơ Thú Cưng', N'Vòng cổ vải lụa mềm chuông đồng');
IF NOT EXISTS (SELECT 1 FROM SanPham WHERE maSanPham = 'SP005')
    INSERT INTO SanPham (maSanPham, maDanhMuc, tenSanPham, moTa) VALUES ('SP005', 'DM03', N'Sữa Tắm Dưỡng Lông Bio-Care', N'Sữa tắm thảo mộc ngừa rụng lông');
IF NOT EXISTS (SELECT 1 FROM SanPham WHERE maSanPham = 'SP006')
    INSERT INTO SanPham (maSanPham, maDanhMuc, tenSanPham, moTa) VALUES ('SP006', 'DM03', N'Cát Vệ Sinh Mèo Hương Táo 5kg', N'Cát đất sét vón cục khử mùi');

IF NOT EXISTS (SELECT 1 FROM DonViTinh WHERE maDonVi = 'DVT01') INSERT INTO DonViTinh (maDonVi, tenDonVi) VALUES ('DVT01', N'Gói');
IF NOT EXISTS (SELECT 1 FROM DonViTinh WHERE maDonVi = 'DVT02') INSERT INTO DonViTinh (maDonVi, tenDonVi) VALUES ('DVT02', N'Lon');
IF NOT EXISTS (SELECT 1 FROM DonViTinh WHERE maDonVi = 'DVT03') INSERT INTO DonViTinh (maDonVi, tenDonVi) VALUES ('DVT03', N'Lốc 6 lon');
IF NOT EXISTS (SELECT 1 FROM DonViTinh WHERE maDonVi = 'DVT04') INSERT INTO DonViTinh (maDonVi, tenDonVi) VALUES ('DVT04', N'Thùng 24 lon');
IF NOT EXISTS (SELECT 1 FROM DonViTinh WHERE maDonVi = 'DVT05') INSERT INTO DonViTinh (maDonVi, tenDonVi) VALUES ('DVT05', N'Chiếc');
IF NOT EXISTS (SELECT 1 FROM DonViTinh WHERE maDonVi = 'DVT06') INSERT INTO DonViTinh (maDonVi, tenDonVi) VALUES ('DVT06', N'Chai');
IF NOT EXISTS (SELECT 1 FROM DonViTinh WHERE maDonVi = 'DVT07') INSERT INTO DonViTinh (maDonVi, tenDonVi) VALUES ('DVT07', N'Túi');

IF NOT EXISTS (SELECT 1 FROM QuyDoiDonVi WHERE maQuyDoi = 'QD001') INSERT INTO QuyDoiDonVi (maQuyDoi, maSanPham, maDonVi, tyLeQuyDoi, laDonViCoSo, giaBan) VALUES ('QD001', 'SP001', 'DVT01', 1, 1, 380000);
IF NOT EXISTS (SELECT 1 FROM QuyDoiDonVi WHERE maQuyDoi = 'QD002') INSERT INTO QuyDoiDonVi (maQuyDoi, maSanPham, maDonVi, tyLeQuyDoi, laDonViCoSo, giaBan) VALUES ('QD002', 'SP002', 'DVT02', 1, 1, 20000);
IF NOT EXISTS (SELECT 1 FROM QuyDoiDonVi WHERE maQuyDoi = 'QD003') INSERT INTO QuyDoiDonVi (maQuyDoi, maSanPham, maDonVi, tyLeQuyDoi, laDonViCoSo, giaBan) VALUES ('QD003', 'SP002', 'DVT03', 6, 0, 115000);
IF NOT EXISTS (SELECT 1 FROM QuyDoiDonVi WHERE maQuyDoi = 'QD004') INSERT INTO QuyDoiDonVi (maQuyDoi, maSanPham, maDonVi, tyLeQuyDoi, laDonViCoSo, giaBan) VALUES ('QD004', 'SP002', 'DVT04', 24, 0, 450000);
IF NOT EXISTS (SELECT 1 FROM QuyDoiDonVi WHERE maQuyDoi = 'QD005') INSERT INTO QuyDoiDonVi (maQuyDoi, maSanPham, maDonVi, tyLeQuyDoi, laDonViCoSo, giaBan) VALUES ('QD005', 'SP003', 'DVT03', 1, 1, 115000);
IF NOT EXISTS (SELECT 1 FROM QuyDoiDonVi WHERE maQuyDoi = 'QD006') INSERT INTO QuyDoiDonVi (maQuyDoi, maSanPham, maDonVi, tyLeQuyDoi, laDonViCoSo, giaBan) VALUES ('QD006', 'SP004', 'DVT05', 1, 1, 45000);
IF NOT EXISTS (SELECT 1 FROM QuyDoiDonVi WHERE maQuyDoi = 'QD007') INSERT INTO QuyDoiDonVi (maQuyDoi, maSanPham, maDonVi, tyLeQuyDoi, laDonViCoSo, giaBan) VALUES ('QD007', 'SP005', 'DVT06', 1, 1, 85000);
IF NOT EXISTS (SELECT 1 FROM QuyDoiDonVi WHERE maQuyDoi = 'QD008') INSERT INTO QuyDoiDonVi (maQuyDoi, maSanPham, maDonVi, tyLeQuyDoi, laDonViCoSo, giaBan) VALUES ('QD008', 'SP006', 'DVT07', 1, 1, 65000);

-- 7. Nhà cung cấp & Lô hàng (FEFO)
IF NOT EXISTS (SELECT 1 FROM NhaCungCap WHERE maNhaCungCap = 'NCC01')
    INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, diaChi, soDienThoai) VALUES ('NCC01', N'Công ty Cổ phần Thức Ăn Thú Cưng Việt Nam', N'123 CMT8, Q.10, TP.HCM', '0283999888');
IF NOT EXISTS (SELECT 1 FROM NhaCungCap WHERE maNhaCungCap = 'NCC02')
    INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, diaChi, soDienThoai) VALUES ('NCC02', N'Pet Accessories Sài Gòn', N'456 Lê Văn Sỹ, Q.3, TP.HCM', '0909888777');

IF NOT EXISTS (SELECT 1 FROM LoHang WHERE soLo = 'LO_RC_2026_01')
    INSERT INTO LoHang (soLo, maSanPham, maNhaCungCap, ngaySanXuat, hanSuDung, donGiaNhapCoSo, soLuongNhap, soLuongTon) VALUES ('LO_RC_2026_01', 'SP001', 'NCC01', '2026-01-10', '2027-01-10', 280000, 50, 45);
IF NOT EXISTS (SELECT 1 FROM LoHang WHERE soLo = 'LO_WS_2026_01')
    INSERT INTO LoHang (soLo, maSanPham, maNhaCungCap, ngaySanXuat, hanSuDung, donGiaNhapCoSo, soLuongNhap, soLuongTon) VALUES ('LO_WS_2026_01', 'SP002', 'NCC01', '2026-02-01', '2027-08-01', 14000, 200, 180);
IF NOT EXISTS (SELECT 1 FROM LoHang WHERE soLo = 'LO_WS_LOC_01')
    INSERT INTO LoHang (soLo, maSanPham, maNhaCungCap, ngaySanXuat, hanSuDung, donGiaNhapCoSo, soLuongNhap, soLuongTon) VALUES ('LO_WS_LOC_01', 'SP003', 'NCC01', '2026-02-01', '2027-08-01', 80000, 50, 30);
IF NOT EXISTS (SELECT 1 FROM LoHang WHERE soLo = 'LO_PK_2026_01')
    INSERT INTO LoHang (soLo, maSanPham, maNhaCungCap, ngaySanXuat, hanSuDung, donGiaNhapCoSo, soLuongNhap, soLuongTon) VALUES ('LO_PK_2026_01', 'SP004', 'NCC02', '2026-01-01', '2028-01-01', 25000, 100, 60);
IF NOT EXISTS (SELECT 1 FROM LoHang WHERE soLo = 'LO_BC_2026_02')
    INSERT INTO LoHang (soLo, maSanPham, maNhaCungCap, ngaySanXuat, hanSuDung, donGiaNhapCoSo, soLuongNhap, soLuongTon) VALUES ('LO_BC_2026_02', 'SP005', 'NCC01', '2026-02-15', '2028-02-15', 50000, 50, 35);
IF NOT EXISTS (SELECT 1 FROM LoHang WHERE soLo = 'LO_CAT_2026_01')
    INSERT INTO LoHang (soLo, maSanPham, maNhaCungCap, ngaySanXuat, hanSuDung, donGiaNhapCoSo, soLuongNhap, soLuongTon) VALUES ('LO_CAT_2026_01', 'SP006', 'NCC01', '2026-03-01', '2028-03-01', 40000, 100, 50);

-- 8. Hóa đơn mẫu (HD982104 & HD981502)
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHoaDon = 'HD982104')
BEGIN
    INSERT INTO HoaDon (maHoaDon, maKhachHang, maNhanVien, ngayLap, thoiGianThanhToan, giamGia, thanhTien, phuongThucThanhToan, trangThaiHoaDon)
    VALUES ('HD982104', 'KH001', 'NV002', DATEADD(hour, -3, GETDATE()), DATEADD(hour, -3, GETDATE()), 20000, 380000, N'Chuyển khoản QR', N'Đã thanh toán');
    INSERT INTO ChiTietHoaDon (maChiTiet, maHoaDon, loaiChiTiet, soLuong, donGiaLucBan, thanhTien)
    VALUES ('HD982104_CT1', 'HD982104', 'SAN_PHAM', 1, 380000, 380000),
           ('HD982104_CT2', 'HD982104', 'SAN_PHAM', 1, 20000, 20000);
    INSERT INTO ChiTietXuatLo (maChiTiet, soLo, maQuyDoi, soLuongXuatCoSo)
    VALUES ('HD982104_CT1', 'LO_RC_2026_01', 'QD001', 1),
           ('HD982104_CT2', 'LO_WS_2026_01', 'QD002', 1);
END

IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHoaDon = 'HD981502')
BEGIN
    INSERT INTO HoaDon (maHoaDon, maKhachHang, maNhanVien, ngayLap, thoiGianThanhToan, giamGia, thanhTien, phuongThucThanhToan, trangThaiHoaDon)
    VALUES ('HD981502', 'KH002', 'NV002', DATEADD(hour, -5, GETDATE()), DATEADD(hour, -5, GETDATE()), 0, 300000, N'Tiền mặt', N'Đã thanh toán');
    INSERT INTO ChiTietHoaDon (maChiTiet, maHoaDon, loaiChiTiet, soLuong, donGiaLucBan, thanhTien)
    VALUES ('HD981502_CT1', 'HD981502', 'DICH_VU', 1, 300000, 300000);
    INSERT INTO ChiTietDichVu (maChiTiet, maDichVu, maThuCung, maKtvThucHien, tienHoaHong)
    VALUES ('HD981502_CT1', 'DV001', 'TC002', 'NV003', 30000);
END

-- 9. Biên bản Bàn giao ca mẫu
IF NOT EXISTS (SELECT 1 FROM BangGiaoCa WHERE maGiaoCa = 'GC001')
    INSERT INTO BangGiaoCa (maGiaoCa, maNhanVien, maCa, tienDauCa, tienCuoiCaHeThong, tienCuoiCaThucTe, tienChenhLech, lyDoChenhLech, gioKetThucThucTe)
    VALUES ('GC001', 'NV002', 'CA01', 1000000, 3500000, 3500000, 0, N'✓ Đã duyệt (QL: Trần Quản Lý) | Khớp két 100%', DATEADD(day, -3, DATEADD(hour, 12, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))));
IF NOT EXISTS (SELECT 1 FROM BangGiaoCa WHERE maGiaoCa = 'GC002')
    INSERT INTO BangGiaoCa (maGiaoCa, maNhanVien, maCa, tienDauCa, tienCuoiCaHeThong, tienCuoiCaThucTe, tienChenhLech, lyDoChenhLech, gioKetThucThucTe)
    VALUES ('GC002', 'NV004', 'CA02', 3500000, 4600000, 4580000, -20000, N'✓ Đã duyệt (QL: Trần Quản Lý) | Thối nhầm 20.000 đ', DATEADD(day, -2, DATEADD(minute, 30, DATEADD(hour, 17, CAST(CAST(GETDATE() AS DATE) AS DATETIME2)))));
IF NOT EXISTS (SELECT 1 FROM BangGiaoCa WHERE maGiaoCa = 'GC003')
    INSERT INTO BangGiaoCa (maGiaoCa, maNhanVien, maCa, tienDauCa, tienCuoiCaHeThong, tienCuoiCaThucTe, tienChenhLech, lyDoChenhLech, gioKetThucThucTe)
    VALUES ('GC003', 'NV001', 'CA03', 4580000, 6500000, 6500000, 0, N'✓ Đã duyệt (QL: Trần Quản Lý) | Khớp két 100%', DATEADD(day, -1, DATEADD(hour, 22, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))));

-- 10. Phòng Lưu Trú Pet Hotel mẫu (12 phòng)
IF NOT EXISTS (SELECT 1 FROM PhongLuuTru WHERE maPhong = 'P101')
    INSERT INTO PhongLuuTru (maPhong, loaiPhong, donGiaNgay, trangThai, tenThuCung, giongLoai, tenChu, sdtChu, thoiGianCheckIn, duKienCheckOut, ghiChuChamSoc, daChoAnSang, daChoAnToi, daDiDaoVeSinh, phiDichVuPhatSinh)
    VALUES ('P101', N'Phòng Tiêu chuẩn', 150000, N'ĐANG CÓ KHÁCH', N'Bim Bim', N'Corgi (8.5kg)', N'Nguyễn Văn An', '0988111222', DATEADD(day, -2, GETDATE()), DATEADD(day, 1, GETDATE()), N'Ăn hạt Royal Canin ngâm mềm, dắt đi dạo 2 lần/ngày', 1, 1, 1, 0);
IF NOT EXISTS (SELECT 1 FROM PhongLuuTru WHERE maPhong = 'P102')
    INSERT INTO PhongLuuTru (maPhong, loaiPhong, donGiaNgay, trangThai, tenThuCung, giongLoai, tenChu, sdtChu, thoiGianCheckIn, duKienCheckOut, ghiChuChamSoc, daChoAnSang, daChoAnToi, daDiDaoVeSinh, phiDichVuPhatSinh)
    VALUES ('P102', N'Phòng Tiêu chuẩn', 150000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0);
IF NOT EXISTS (SELECT 1 FROM PhongLuuTru WHERE maPhong = 'P103')
    INSERT INTO PhongLuuTru (maPhong, loaiPhong, donGiaNgay, trangThai, tenThuCung, giongLoai, tenChu, sdtChu, thoiGianCheckIn, duKienCheckOut, ghiChuChamSoc, daChoAnSang, daChoAnToi, daDiDaoVeSinh, phiDichVuPhatSinh)
    VALUES ('P103', N'Phòng Tiêu chuẩn', 150000, N'ĐANG CÓ KHÁCH', N'Bơ', N'Poodle Tiny (3.2kg)', N'Trần Thị Mai', '0977333444', DATEADD(day, -1, GETDATE()), DATEADD(day, 2, GETDATE()), N'Ăn kèm pate gà, chải lông mỗi chiều', 1, 0, 1, 0);
IF NOT EXISTS (SELECT 1 FROM PhongLuuTru WHERE maPhong = 'P104')
    INSERT INTO PhongLuuTru (maPhong, loaiPhong, donGiaNgay, trangThai, tenThuCung, giongLoai, tenChu, sdtChu, thoiGianCheckIn, duKienCheckOut, ghiChuChamSoc, daChoAnSang, daChoAnToi, daDiDaoVeSinh, phiDichVuPhatSinh)
    VALUES ('P104', N'Phòng Tiêu chuẩn', 150000, N'ĐANG DỌN DẸP', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0);
IF NOT EXISTS (SELECT 1 FROM PhongLuuTru WHERE maPhong = 'V201')
    INSERT INTO PhongLuuTru (maPhong, loaiPhong, donGiaNgay, trangThai, tenThuCung, giongLoai, tenChu, sdtChu, thoiGianCheckIn, duKienCheckOut, ghiChuChamSoc, daChoAnSang, daChoAnToi, daDiDaoVeSinh, phiDichVuPhatSinh)
    VALUES ('V201', N'Phòng VIP Suite', 250000, N'ĐANG CÓ KHÁCH', N'Lu Lu', N'Golden Retriever (24kg)', N'Phạm Hoàng Long', '0918776655', DATEADD(day, -3, GETDATE()), DATEADD(day, 1, GETDATE()), N'Camera VIP bật 24/7 cho chủ xem, ăn 3 bữa', 1, 1, 1, 0);
IF NOT EXISTS (SELECT 1 FROM PhongLuuTru WHERE maPhong = 'V202')
    INSERT INTO PhongLuuTru (maPhong, loaiPhong, donGiaNgay, trangThai, tenThuCung, giongLoai, tenChu, sdtChu, thoiGianCheckIn, duKienCheckOut, ghiChuChamSoc, daChoAnSang, daChoAnToi, daDiDaoVeSinh, phiDichVuPhatSinh)
    VALUES ('V202', N'Phòng VIP Suite', 250000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0);
IF NOT EXISTS (SELECT 1 FROM PhongLuuTru WHERE maPhong = 'V203')
    INSERT INTO PhongLuuTru (maPhong, loaiPhong, donGiaNgay, trangThai, tenThuCung, giongLoai, tenChu, sdtChu, thoiGianCheckIn, duKienCheckOut, ghiChuChamSoc, daChoAnSang, daChoAnToi, daDiDaoVeSinh, phiDichVuPhatSinh)
    VALUES ('V203', N'Phòng VIP Suite', 250000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0);
IF NOT EXISTS (SELECT 1 FROM PhongLuuTru WHERE maPhong = 'V204')
    INSERT INTO PhongLuuTru (maPhong, loaiPhong, donGiaNgay, trangThai, tenThuCung, giongLoai, tenChu, sdtChu, thoiGianCheckIn, duKienCheckOut, ghiChuChamSoc, daChoAnSang, daChoAnToi, daDiDaoVeSinh, phiDichVuPhatSinh)
    VALUES ('V204', N'Phòng VIP Suite', 250000, N'ĐANG CÓ KHÁCH', N'Sammy', N'Samoyed (19kg)', N'Đỗ Quỳnh Chi', '0905123456', DATEADD(hour, -8, GETDATE()), DATEADD(day, 3, GETDATE()), N'Bật điều hòa 22 độ C liên tục, chải lông kĩ', 1, 0, 1, 0);
IF NOT EXISTS (SELECT 1 FROM PhongLuuTru WHERE maPhong = 'C301')
    INSERT INTO PhongLuuTru (maPhong, loaiPhong, donGiaNgay, trangThai, tenThuCung, giongLoai, tenChu, sdtChu, thoiGianCheckIn, duKienCheckOut, ghiChuChamSoc, daChoAnSang, daChoAnToi, daDiDaoVeSinh, phiDichVuPhatSinh)
    VALUES ('C301', N'Condo cho Mèo', 200000, N'ĐANG CÓ KHÁCH', N'Miu Miu', N'Mèo Anh Lông Ngắn (4.5kg)', N'Lê Hoàng Yến', '0912345678', DATEADD(day, -1, GETDATE()), DATEADD(day, 2, GETDATE()), N'Ăn hạt Orijen + súp thưởng Ciao lúc 15h', 1, 0, 1, 0);
IF NOT EXISTS (SELECT 1 FROM PhongLuuTru WHERE maPhong = 'C302')
    INSERT INTO PhongLuuTru (maPhong, loaiPhong, donGiaNgay, trangThai, tenThuCung, giongLoai, tenChu, sdtChu, thoiGianCheckIn, duKienCheckOut, ghiChuChamSoc, daChoAnSang, daChoAnToi, daDiDaoVeSinh, phiDichVuPhatSinh)
    VALUES ('C302', N'Condo cho Mèo', 200000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0);
IF NOT EXISTS (SELECT 1 FROM PhongLuuTru WHERE maPhong = 'C303')
    INSERT INTO PhongLuuTru (maPhong, loaiPhong, donGiaNgay, trangThai, tenThuCung, giongLoai, tenChu, sdtChu, thoiGianCheckIn, duKienCheckOut, ghiChuChamSoc, daChoAnSang, daChoAnToi, daDiDaoVeSinh, phiDichVuPhatSinh)
    VALUES ('C303', N'Condo cho Mèo', 200000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0);
IF NOT EXISTS (SELECT 1 FROM PhongLuuTru WHERE maPhong = 'C304')
    INSERT INTO PhongLuuTru (maPhong, loaiPhong, donGiaNgay, trangThai, tenThuCung, giongLoai, tenChu, sdtChu, thoiGianCheckIn, duKienCheckOut, ghiChuChamSoc, daChoAnSang, daChoAnToi, daDiDaoVeSinh, phiDichVuPhatSinh)
    VALUES ('C304', N'Condo cho Mèo', 200000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0);

PRINT N'Hoàn thành nạp dữ liệu mẫu kiểm thử!';
GO
