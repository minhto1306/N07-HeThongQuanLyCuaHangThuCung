-- ======================================================================================
-- DỰ ÁN MÔN HỌC: PHÁT TRIỂN ỨNG DỤNG (APPLICATION DEVELOPMENT - AD)
-- HỌC KỲ: HK1 / 2026-2027 | NHÓM: 07 (N07)
-- ĐỀ TÀI: XÂY DỰNG HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG (PET SHOP MANAGEMENT SYSTEM)
-- DBMS: MICROSOFT SQL SERVER 2019 / 2022 / 2025
-- TỆP LỆNH DUY NHẤT: DB_HeThongQuanLyThuCung.sql (Khởi tạo Schema & Nạp sẵn Dữ liệu mẫu)
-- ======================================================================================

USE [master]
GO

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'QuanLyCuaHangThuCung')
BEGIN
    CREATE DATABASE [QuanLyCuaHangThuCung]
END
GO

USE [QuanLyCuaHangThuCung]
GO

-- XÓA BẢNG NẾU ĐÃ TỒN TẠI (THEO THỨ TỰ RÀNG BUỘC KHÓA NGOẠI)
IF OBJECT_ID(N'[dbo].[PhongLuuTru]', 'U') IS NOT NULL DROP TABLE [dbo].[PhongLuuTru];
IF OBJECT_ID(N'[dbo].[ChiTietDoiTra]', 'U') IS NOT NULL DROP TABLE [dbo].[ChiTietDoiTra];
IF OBJECT_ID(N'[dbo].[PhieuDoiTra]', 'U') IS NOT NULL DROP TABLE [dbo].[PhieuDoiTra];
IF OBJECT_ID(N'[dbo].[ChiTietDichVu]', 'U') IS NOT NULL DROP TABLE [dbo].[ChiTietDichVu];
IF OBJECT_ID(N'[dbo].[ChiTietXuatLo]', 'U') IS NOT NULL DROP TABLE [dbo].[ChiTietXuatLo];
IF OBJECT_ID(N'[dbo].[ChiTietHoaDon]', 'U') IS NOT NULL DROP TABLE [dbo].[ChiTietHoaDon];
IF OBJECT_ID(N'[dbo].[HoaDon]', 'U') IS NOT NULL DROP TABLE [dbo].[HoaDon];
IF OBJECT_ID(N'[dbo].[ChiTietLichHen]', 'U') IS NOT NULL DROP TABLE [dbo].[ChiTietLichHen];
IF OBJECT_ID(N'[dbo].[LichHen]', 'U') IS NOT NULL DROP TABLE [dbo].[LichHen];
IF OBJECT_ID(N'[dbo].[HoSoSucKhoe]', 'U') IS NOT NULL DROP TABLE [dbo].[HoSoSucKhoe];
IF OBJECT_ID(N'[dbo].[HoSoThuCung]', 'U') IS NOT NULL DROP TABLE [dbo].[HoSoThuCung];
IF OBJECT_ID(N'[dbo].[ThuCung]', 'U') IS NOT NULL DROP TABLE [dbo].[ThuCung];
IF OBJECT_ID(N'[dbo].[KhachHang]', 'U') IS NOT NULL DROP TABLE [dbo].[KhachHang];
IF OBJECT_ID(N'[dbo].[DichVu]', 'U') IS NOT NULL DROP TABLE [dbo].[DichVu];
IF OBJECT_ID(N'[dbo].[LoHang]', 'U') IS NOT NULL DROP TABLE [dbo].[LoHang];
IF OBJECT_ID(N'[dbo].[NhaCungCap]', 'U') IS NOT NULL DROP TABLE [dbo].[NhaCungCap];
IF OBJECT_ID(N'[dbo].[QuyDoiDonVi]', 'U') IS NOT NULL DROP TABLE [dbo].[QuyDoiDonVi];
IF OBJECT_ID(N'[dbo].[DonViTinh]', 'U') IS NOT NULL DROP TABLE [dbo].[DonViTinh];
IF OBJECT_ID(N'[dbo].[SanPham]', 'U') IS NOT NULL DROP TABLE [dbo].[SanPham];
IF OBJECT_ID(N'[dbo].[DanhMucHang]', 'U') IS NOT NULL DROP TABLE [dbo].[DanhMucHang];
IF OBJECT_ID(N'[dbo].[BangGiaoCa]', 'U') IS NOT NULL DROP TABLE [dbo].[BangGiaoCa];
IF OBJECT_ID(N'[dbo].[BangPhanCong]', 'U') IS NOT NULL DROP TABLE [dbo].[BangPhanCong];
IF OBJECT_ID(N'[dbo].[CaLamViec]', 'U') IS NOT NULL DROP TABLE [dbo].[CaLamViec];
IF OBJECT_ID(N'[dbo].[NhanVien]', 'U') IS NOT NULL DROP TABLE [dbo].[NhanVien];
IF OBJECT_ID(N'[dbo].[TaiKhoan]', 'U') IS NOT NULL DROP TABLE [dbo].[TaiKhoan];
GO
/****** Object:  Table [dbo].[BangGiaoCa]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[BangGiaoCa](
	[maGiaoCa] [nvarchar](20) NOT NULL,
	[maNhanVien] [nvarchar](20) NOT NULL,
	[maCa] [nvarchar](20) NOT NULL,
	[tienDauCa] [decimal](18, 2) NOT NULL,
	[tienCuoiCaHeThong] [decimal](18, 2) NOT NULL,
	[tienCuoiCaThucTe] [decimal](18, 2) NOT NULL,
	[tienChenhLech] [decimal](18, 2) NOT NULL,
	[lyDoChenhLech] [nvarchar](255) NULL,
	[gioKetThucThucTe] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[maGiaoCa] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[BangPhanCong]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[BangPhanCong](
	[maPhanCong] [nvarchar](20) NOT NULL,
	[maNhanVien] [nvarchar](20) NOT NULL,
	[maCa] [nvarchar](20) NOT NULL,
	[ngayLamViec] [date] NOT NULL,
	[trangThaiCa] [nvarchar](50) NULL,
	[ghiChu] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[maPhanCong] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[CaLamViec]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[CaLamViec](
	[maCa] [nvarchar](20) NOT NULL,
	[tenCa] [nvarchar](50) NOT NULL,
	[gioBatDau] [time](7) NOT NULL,
	[gioKetThuc] [time](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maCa] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ChiTietDichVu]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ChiTietDichVu](
	[maChiTiet] [nvarchar](20) NOT NULL,
	[maDichVu] [nvarchar](20) NOT NULL,
	[maThuCung] [nvarchar](20) NULL,
	[maKtvThucHien] [nvarchar](20) NULL,
	[tienHoaHong] [decimal](18, 2) NULL,
PRIMARY KEY CLUSTERED 
(
	[maChiTiet] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ChiTietDoiTra]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ChiTietDoiTra](
	[maChiTietDoiTra] [nvarchar](20) NOT NULL,
	[maPhieuDoiTra] [nvarchar](20) NOT NULL,
	[maSanPham] [nvarchar](20) NOT NULL,
	[soLuongDoiTra] [int] NOT NULL,
	[tienHoanLai] [decimal](18, 2) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maChiTietDoiTra] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ChiTietHoaDon]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ChiTietHoaDon](
	[maChiTiet] [nvarchar](20) NOT NULL,
	[maHoaDon] [nvarchar](20) NOT NULL,
	[loaiChiTiet] [nvarchar](20) NOT NULL,
	[soLuong] [int] NOT NULL,
	[donGiaLucBan] [decimal](18, 2) NOT NULL,
	[thanhTien] [decimal](18, 2) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maChiTiet] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ChiTietLichHen]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ChiTietLichHen](
	[maChiTietLichHen] [nvarchar](20) NOT NULL,
	[maLichHen] [nvarchar](20) NOT NULL,
	[maThuCung] [nvarchar](20) NOT NULL,
	[maDichVu] [nvarchar](20) NOT NULL,
	[maKtvChiDinh] [nvarchar](20) NULL,
PRIMARY KEY CLUSTERED 
(
	[maChiTietLichHen] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ChiTietXuatLo]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ChiTietXuatLo](
	[maChiTiet] [nvarchar](20) NOT NULL,
	[soLo] [nvarchar](50) NOT NULL,
	[maQuyDoi] [nvarchar](20) NULL,
	[soLuongXuatCoSo] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maChiTiet] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[DanhMucHang]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DanhMucHang](
	[maDanhMuc] [nvarchar](20) NOT NULL,
	[tenDanhMuc] [nvarchar](100) NOT NULL,
	[moTa] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[maDanhMuc] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[DichVu]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DichVu](
	[maDichVu] [nvarchar](20) NOT NULL,
	[tenDichVu] [nvarchar](100) NOT NULL,
	[giaDichVu] [decimal](18, 2) NOT NULL,
	[thoiLuongPhut] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maDichVu] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[DonViTinh]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DonViTinh](
	[maDonVi] [nvarchar](20) NOT NULL,
	[tenDonVi] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maDonVi] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[HoaDon]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[HoaDon](
	[maHoaDon] [nvarchar](20) NOT NULL,
	[maKhachHang] [nvarchar](20) NULL,
	[maNhanVien] [nvarchar](20) NOT NULL,
	[ngayLap] [datetime2](7) NOT NULL,
	[thoiGianThanhToan] [datetime2](7) NULL,
	[giamGia] [decimal](18, 2) NULL,
	[thanhTien] [decimal](18, 2) NOT NULL,
	[phuongThucThanhToan] [nvarchar](50) NULL,
	[trangThaiHoaDon] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maHoaDon] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[HoSoSucKhoe]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[HoSoSucKhoe](
	[maSucKhoe] [nvarchar](20) NOT NULL,
	[maThuCung] [nvarchar](20) NOT NULL,
	[maNhanVienKiemTra] [nvarchar](20) NOT NULL,
	[canNang] [decimal](6, 2) NULL,
	[nhietDo] [decimal](4, 2) NULL,
	[tinhTrangVeRan] [nvarchar](100) NULL,
	[tiemPhongDai] [bit] NOT NULL,
	[ghiChuTheTrang] [nvarchar](255) NULL,
	[ngayKiemTra] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maSucKhoe] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[HoSoThuCung]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[HoSoThuCung](
	[maHoSo] [nvarchar](20) NOT NULL,
	[maKhachHang] [nvarchar](20) NOT NULL,
	[maThuCung] [nvarchar](20) NOT NULL,
	[soMicrochip] [nvarchar](50) NULL,
	[ngayDangKy] [date] NOT NULL,
	[trangThaiNuoi] [nvarchar](50) NULL,
PRIMARY KEY CLUSTERED 
(
	[maHoSo] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[KhachHang]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[KhachHang](
	[maKhachHang] [nvarchar](20) NOT NULL,
	[hoTen] [nvarchar](100) NOT NULL,
	[soDienThoai] [nvarchar](15) NOT NULL,
	[diemTichLuy] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maKhachHang] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[soDienThoai] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[LichHen]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[LichHen](
	[maLichHen] [nvarchar](20) NOT NULL,
	[maKhachHang] [nvarchar](20) NOT NULL,
	[maNhanVienTiepNhan] [nvarchar](20) NULL,
	[ngayHen] [datetime2](7) NOT NULL,
	[trangThaiLichHen] [nvarchar](50) NULL,
	[ghiChu] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[maLichHen] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[LoHang]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[LoHang](
	[soLo] [nvarchar](50) NOT NULL,
	[maSanPham] [nvarchar](20) NOT NULL,
	[maNhaCungCap] [nvarchar](20) NOT NULL,
	[ngaySanXuat] [date] NULL,
	[hanSuDung] [date] NOT NULL,
	[donGiaNhapCoSo] [decimal](18, 2) NOT NULL,
	[soLuongNhap] [int] NOT NULL,
	[soLuongTon] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[soLo] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[NhaCungCap]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[NhaCungCap](
	[maNhaCungCap] [nvarchar](20) NOT NULL,
	[tenNhaCungCap] [nvarchar](150) NOT NULL,
	[diaChi] [nvarchar](255) NULL,
	[soDienThoai] [nvarchar](15) NULL,
PRIMARY KEY CLUSTERED 
(
	[maNhaCungCap] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[NhanVien]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[NhanVien](
	[maNhanVien] [nvarchar](20) NOT NULL,
	[hoTen] [nvarchar](100) NOT NULL,
	[chucVu] [nvarchar](50) NOT NULL,
	[soDienThoai] [nvarchar](15) NULL,
	[tenDangNhap] [nvarchar](50) NULL,
PRIMARY KEY CLUSTERED 
(
	[maNhanVien] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[tenDangNhap] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[PhieuDoiTra]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PhieuDoiTra](
	[maPhieuDoiTra] [nvarchar](20) NOT NULL,
	[maHoaDonGoc] [nvarchar](20) NOT NULL,
	[maNhanVien] [nvarchar](20) NOT NULL,
	[ngayDoiTra] [datetime2](7) NOT NULL,
	[lyDoDoiTra] [nvarchar](255) NULL,
	[tongTienHoan] [decimal](18, 2) NOT NULL,
	[trangThaiDoiTra] [nvarchar](50) NULL,
PRIMARY KEY CLUSTERED 
(
	[maPhieuDoiTra] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[PhongLuuTru]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PhongLuuTru](
	[maPhong] [nvarchar](20) NOT NULL,
	[loaiPhong] [nvarchar](50) NOT NULL,
	[donGiaNgay] [bigint] NOT NULL,
	[trangThai] [nvarchar](50) NOT NULL,
	[tenThuCung] [nvarchar](100) NULL,
	[giongLoai] [nvarchar](100) NULL,
	[tenChu] [nvarchar](100) NULL,
	[sdtChu] [nvarchar](20) NULL,
	[thoiGianCheckIn] [datetime2](7) NULL,
	[duKienCheckOut] [datetime2](7) NULL,
	[ghiChuChamSoc] [nvarchar](255) NULL,
	[daChoAnSang] [bit] NOT NULL,
	[daChoAnToi] [bit] NOT NULL,
	[daDiDaoVeSinh] [bit] NOT NULL,
	[phiDichVuPhatSinh] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maPhong] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[QuyDoiDonVi]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[QuyDoiDonVi](
	[maQuyDoi] [nvarchar](20) NOT NULL,
	[maSanPham] [nvarchar](20) NOT NULL,
	[maDonVi] [nvarchar](20) NOT NULL,
	[tyLeQuyDoi] [int] NOT NULL,
	[laDonViCoSo] [bit] NOT NULL,
	[giaBan] [decimal](18, 2) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maQuyDoi] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[SanPham]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[SanPham](
	[maSanPham] [nvarchar](20) NOT NULL,
	[maDanhMuc] [nvarchar](20) NOT NULL,
	[tenSanPham] [nvarchar](150) NOT NULL,
	[moTa] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[maSanPham] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[TaiKhoan]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TaiKhoan](
	[tenDangNhap] [nvarchar](50) NOT NULL,
	[matKhau] [nvarchar](255) NOT NULL,
	[vaiTro] [nvarchar](50) NOT NULL,
	[trangThaiHoatDong] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[tenDangNhap] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ThuCung]    Script Date: 21/10/2026 6:04:18 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ThuCung](
	[maThuCung] [nvarchar](20) NOT NULL,
	[tenThuCung] [nvarchar](100) NOT NULL,
	[giongLoai] [nvarchar](50) NULL,
	[ngaySinh] [date] NULL,
	[gioiTinh] [nvarchar](10) NULL,
PRIMARY KEY CLUSTERED 
(
	[maThuCung] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[BangGiaoCa] ADD  DEFAULT ((0)) FOR [tienDauCa]
GO
ALTER TABLE [dbo].[BangGiaoCa] ADD  DEFAULT ((0)) FOR [tienCuoiCaHeThong]
GO
ALTER TABLE [dbo].[BangGiaoCa] ADD  DEFAULT ((0)) FOR [tienCuoiCaThucTe]
GO
ALTER TABLE [dbo].[BangGiaoCa] ADD  DEFAULT ((0)) FOR [tienChenhLech]
GO
ALTER TABLE [dbo].[BangPhanCong] ADD  DEFAULT (N'Chưa bắt đầu') FOR [trangThaiCa]
GO
ALTER TABLE [dbo].[ChiTietDichVu] ADD  DEFAULT ((0)) FOR [tienHoaHong]
GO
ALTER TABLE [dbo].[HoaDon] ADD  DEFAULT ((0)) FOR [giamGia]
GO
ALTER TABLE [dbo].[HoaDon] ADD  DEFAULT ((0)) FOR [thanhTien]
GO
ALTER TABLE [dbo].[HoSoSucKhoe] ADD  DEFAULT ((0)) FOR [tiemPhongDai]
GO
ALTER TABLE [dbo].[HoSoThuCung] ADD  DEFAULT (N'Đang nuôi') FOR [trangThaiNuoi]
GO
ALTER TABLE [dbo].[KhachHang] ADD  DEFAULT ((0)) FOR [diemTichLuy]
GO
ALTER TABLE [dbo].[LichHen] ADD  DEFAULT (N'Đã đặt') FOR [trangThaiLichHen]
GO
ALTER TABLE [dbo].[PhieuDoiTra] ADD  DEFAULT ((0)) FOR [tongTienHoan]
GO
ALTER TABLE [dbo].[PhieuDoiTra] ADD  DEFAULT (N'Hoàn thành') FOR [trangThaiDoiTra]
GO
ALTER TABLE [dbo].[PhongLuuTru] ADD  DEFAULT ((0)) FOR [daChoAnSang]
GO
ALTER TABLE [dbo].[PhongLuuTru] ADD  DEFAULT ((0)) FOR [daChoAnToi]
GO
ALTER TABLE [dbo].[PhongLuuTru] ADD  DEFAULT ((0)) FOR [daDiDaoVeSinh]
GO
ALTER TABLE [dbo].[PhongLuuTru] ADD  DEFAULT ((0)) FOR [phiDichVuPhatSinh]
GO
ALTER TABLE [dbo].[QuyDoiDonVi] ADD  DEFAULT ((1)) FOR [tyLeQuyDoi]
GO
ALTER TABLE [dbo].[QuyDoiDonVi] ADD  DEFAULT ((0)) FOR [laDonViCoSo]
GO
ALTER TABLE [dbo].[TaiKhoan] ADD  DEFAULT ((1)) FOR [trangThaiHoatDong]
GO
ALTER TABLE [dbo].[BangGiaoCa]  WITH CHECK ADD  CONSTRAINT [FK_GiaoCa_CaLamViec] FOREIGN KEY([maCa])
REFERENCES [dbo].[CaLamViec] ([maCa])
GO
ALTER TABLE [dbo].[BangGiaoCa] CHECK CONSTRAINT [FK_GiaoCa_CaLamViec]
GO
ALTER TABLE [dbo].[BangGiaoCa]  WITH CHECK ADD  CONSTRAINT [FK_GiaoCa_NhanVien] FOREIGN KEY([maNhanVien])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[BangGiaoCa] CHECK CONSTRAINT [FK_GiaoCa_NhanVien]
GO
ALTER TABLE [dbo].[BangPhanCong]  WITH CHECK ADD  CONSTRAINT [FK_PhanCong_CaLamViec] FOREIGN KEY([maCa])
REFERENCES [dbo].[CaLamViec] ([maCa])
GO
ALTER TABLE [dbo].[BangPhanCong] CHECK CONSTRAINT [FK_PhanCong_CaLamViec]
GO
ALTER TABLE [dbo].[BangPhanCong]  WITH CHECK ADD  CONSTRAINT [FK_PhanCong_NhanVien] FOREIGN KEY([maNhanVien])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[BangPhanCong] CHECK CONSTRAINT [FK_PhanCong_NhanVien]
GO
ALTER TABLE [dbo].[ChiTietDichVu]  WITH CHECK ADD  CONSTRAINT [FK_CTDichVu_ChiTietHoaDon] FOREIGN KEY([maChiTiet])
REFERENCES [dbo].[ChiTietHoaDon] ([maChiTiet])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[ChiTietDichVu] CHECK CONSTRAINT [FK_CTDichVu_ChiTietHoaDon]
GO
ALTER TABLE [dbo].[ChiTietDichVu]  WITH CHECK ADD  CONSTRAINT [FK_CTDichVu_DichVu] FOREIGN KEY([maDichVu])
REFERENCES [dbo].[DichVu] ([maDichVu])
GO
ALTER TABLE [dbo].[ChiTietDichVu] CHECK CONSTRAINT [FK_CTDichVu_DichVu]
GO
ALTER TABLE [dbo].[ChiTietDichVu]  WITH CHECK ADD  CONSTRAINT [FK_CTDichVu_NhanVien] FOREIGN KEY([maKtvThucHien])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[ChiTietDichVu] CHECK CONSTRAINT [FK_CTDichVu_NhanVien]
GO
ALTER TABLE [dbo].[ChiTietDichVu]  WITH CHECK ADD  CONSTRAINT [FK_CTDichVu_ThuCung] FOREIGN KEY([maThuCung])
REFERENCES [dbo].[ThuCung] ([maThuCung])
GO
ALTER TABLE [dbo].[ChiTietDichVu] CHECK CONSTRAINT [FK_CTDichVu_ThuCung]
GO
ALTER TABLE [dbo].[ChiTietDoiTra]  WITH CHECK ADD  CONSTRAINT [FK_CTDoiTra_Phieu] FOREIGN KEY([maPhieuDoiTra])
REFERENCES [dbo].[PhieuDoiTra] ([maPhieuDoiTra])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[ChiTietDoiTra] CHECK CONSTRAINT [FK_CTDoiTra_Phieu]
GO
ALTER TABLE [dbo].[ChiTietDoiTra]  WITH CHECK ADD  CONSTRAINT [FK_CTDoiTra_SanPham] FOREIGN KEY([maSanPham])
REFERENCES [dbo].[SanPham] ([maSanPham])
GO
ALTER TABLE [dbo].[ChiTietDoiTra] CHECK CONSTRAINT [FK_CTDoiTra_SanPham]
GO
ALTER TABLE [dbo].[ChiTietHoaDon]  WITH CHECK ADD  CONSTRAINT [FK_ChiTiet_HoaDon] FOREIGN KEY([maHoaDon])
REFERENCES [dbo].[HoaDon] ([maHoaDon])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[ChiTietHoaDon] CHECK CONSTRAINT [FK_ChiTiet_HoaDon]
GO
ALTER TABLE [dbo].[ChiTietLichHen]  WITH CHECK ADD  CONSTRAINT [FK_CTLichHen_DichVu] FOREIGN KEY([maDichVu])
REFERENCES [dbo].[DichVu] ([maDichVu])
GO
ALTER TABLE [dbo].[ChiTietLichHen] CHECK CONSTRAINT [FK_CTLichHen_DichVu]
GO
ALTER TABLE [dbo].[ChiTietLichHen]  WITH CHECK ADD  CONSTRAINT [FK_CTLichHen_Ktv] FOREIGN KEY([maKtvChiDinh])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[ChiTietLichHen] CHECK CONSTRAINT [FK_CTLichHen_Ktv]
GO
ALTER TABLE [dbo].[ChiTietLichHen]  WITH CHECK ADD  CONSTRAINT [FK_CTLichHen_LichHen] FOREIGN KEY([maLichHen])
REFERENCES [dbo].[LichHen] ([maLichHen])
GO
ALTER TABLE [dbo].[ChiTietLichHen] CHECK CONSTRAINT [FK_CTLichHen_LichHen]
GO
ALTER TABLE [dbo].[ChiTietLichHen]  WITH CHECK ADD  CONSTRAINT [FK_CTLichHen_ThuCung] FOREIGN KEY([maThuCung])
REFERENCES [dbo].[ThuCung] ([maThuCung])
GO
ALTER TABLE [dbo].[ChiTietLichHen] CHECK CONSTRAINT [FK_CTLichHen_ThuCung]
GO
ALTER TABLE [dbo].[ChiTietXuatLo]  WITH CHECK ADD  CONSTRAINT [FK_XuatLo_ChiTietHoaDon] FOREIGN KEY([maChiTiet])
REFERENCES [dbo].[ChiTietHoaDon] ([maChiTiet])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[ChiTietXuatLo] CHECK CONSTRAINT [FK_XuatLo_ChiTietHoaDon]
GO
ALTER TABLE [dbo].[ChiTietXuatLo]  WITH CHECK ADD  CONSTRAINT [FK_XuatLo_LoHang] FOREIGN KEY([soLo])
REFERENCES [dbo].[LoHang] ([soLo])
GO
ALTER TABLE [dbo].[ChiTietXuatLo] CHECK CONSTRAINT [FK_XuatLo_LoHang]
GO
ALTER TABLE [dbo].[ChiTietXuatLo]  WITH CHECK ADD  CONSTRAINT [FK_XuatLo_QuyDoi] FOREIGN KEY([maQuyDoi])
REFERENCES [dbo].[QuyDoiDonVi] ([maQuyDoi])
GO
ALTER TABLE [dbo].[ChiTietXuatLo] CHECK CONSTRAINT [FK_XuatLo_QuyDoi]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_KhachHang] FOREIGN KEY([maKhachHang])
REFERENCES [dbo].[KhachHang] ([maKhachHang])
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_KhachHang]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_NhanVien] FOREIGN KEY([maNhanVien])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_NhanVien]
GO
ALTER TABLE [dbo].[HoSoSucKhoe]  WITH CHECK ADD  CONSTRAINT [FK_SucKhoe_NhanVien] FOREIGN KEY([maNhanVienKiemTra])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[HoSoSucKhoe] CHECK CONSTRAINT [FK_SucKhoe_NhanVien]
GO
ALTER TABLE [dbo].[HoSoSucKhoe]  WITH CHECK ADD  CONSTRAINT [FK_SucKhoe_ThuCung] FOREIGN KEY([maThuCung])
REFERENCES [dbo].[ThuCung] ([maThuCung])
GO
ALTER TABLE [dbo].[HoSoSucKhoe] CHECK CONSTRAINT [FK_SucKhoe_ThuCung]
GO
ALTER TABLE [dbo].[HoSoThuCung]  WITH CHECK ADD  CONSTRAINT [FK_HoSo_KhachHang] FOREIGN KEY([maKhachHang])
REFERENCES [dbo].[KhachHang] ([maKhachHang])
GO
ALTER TABLE [dbo].[HoSoThuCung] CHECK CONSTRAINT [FK_HoSo_KhachHang]
GO
ALTER TABLE [dbo].[HoSoThuCung]  WITH CHECK ADD  CONSTRAINT [FK_HoSo_ThuCung] FOREIGN KEY([maThuCung])
REFERENCES [dbo].[ThuCung] ([maThuCung])
GO
ALTER TABLE [dbo].[HoSoThuCung] CHECK CONSTRAINT [FK_HoSo_ThuCung]
GO
ALTER TABLE [dbo].[LichHen]  WITH CHECK ADD  CONSTRAINT [FK_LichHen_KhachHang] FOREIGN KEY([maKhachHang])
REFERENCES [dbo].[KhachHang] ([maKhachHang])
GO
ALTER TABLE [dbo].[LichHen] CHECK CONSTRAINT [FK_LichHen_KhachHang]
GO
ALTER TABLE [dbo].[LichHen]  WITH CHECK ADD  CONSTRAINT [FK_LichHen_NhanVien] FOREIGN KEY([maNhanVienTiepNhan])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[LichHen] CHECK CONSTRAINT [FK_LichHen_NhanVien]
GO
ALTER TABLE [dbo].[LoHang]  WITH CHECK ADD  CONSTRAINT [FK_LoHang_NhaCungCap] FOREIGN KEY([maNhaCungCap])
REFERENCES [dbo].[NhaCungCap] ([maNhaCungCap])
GO
ALTER TABLE [dbo].[LoHang] CHECK CONSTRAINT [FK_LoHang_NhaCungCap]
GO
ALTER TABLE [dbo].[LoHang]  WITH CHECK ADD  CONSTRAINT [FK_LoHang_SanPham] FOREIGN KEY([maSanPham])
REFERENCES [dbo].[SanPham] ([maSanPham])
GO
ALTER TABLE [dbo].[LoHang] CHECK CONSTRAINT [FK_LoHang_SanPham]
GO
ALTER TABLE [dbo].[NhanVien]  WITH CHECK ADD  CONSTRAINT [FK_NhanVien_TaiKhoan] FOREIGN KEY([tenDangNhap])
REFERENCES [dbo].[TaiKhoan] ([tenDangNhap])
GO
ALTER TABLE [dbo].[NhanVien] CHECK CONSTRAINT [FK_NhanVien_TaiKhoan]
GO
ALTER TABLE [dbo].[PhieuDoiTra]  WITH CHECK ADD  CONSTRAINT [FK_DoiTra_HoaDon] FOREIGN KEY([maHoaDonGoc])
REFERENCES [dbo].[HoaDon] ([maHoaDon])
GO
ALTER TABLE [dbo].[PhieuDoiTra] CHECK CONSTRAINT [FK_DoiTra_HoaDon]
GO
ALTER TABLE [dbo].[PhieuDoiTra]  WITH CHECK ADD  CONSTRAINT [FK_DoiTra_NhanVien] FOREIGN KEY([maNhanVien])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[PhieuDoiTra] CHECK CONSTRAINT [FK_DoiTra_NhanVien]
GO
ALTER TABLE [dbo].[QuyDoiDonVi]  WITH CHECK ADD  CONSTRAINT [FK_QuyDoi_DonVi] FOREIGN KEY([maDonVi])
REFERENCES [dbo].[DonViTinh] ([maDonVi])
GO
ALTER TABLE [dbo].[QuyDoiDonVi] CHECK CONSTRAINT [FK_QuyDoi_DonVi]
GO
ALTER TABLE [dbo].[QuyDoiDonVi]  WITH CHECK ADD  CONSTRAINT [FK_QuyDoi_SanPham] FOREIGN KEY([maSanPham])
REFERENCES [dbo].[SanPham] ([maSanPham])
GO
ALTER TABLE [dbo].[QuyDoiDonVi] CHECK CONSTRAINT [FK_QuyDoi_SanPham]
GO
ALTER TABLE [dbo].[SanPham]  WITH CHECK ADD  CONSTRAINT [FK_SanPham_DanhMuc] FOREIGN KEY([maDanhMuc])
REFERENCES [dbo].[DanhMucHang] ([maDanhMuc])
GO
ALTER TABLE [dbo].[SanPham] CHECK CONSTRAINT [FK_SanPham_DanhMuc]
GO
ALTER TABLE [dbo].[LoHang]  WITH CHECK ADD  CONSTRAINT [CK_LoHang_SoLuongTon] CHECK  (([soLuongTon]>=(0)))
GO
ALTER TABLE [dbo].[LoHang] CHECK CONSTRAINT [CK_LoHang_SoLuongTon]
GO

-- ======================================================================================
-- NẠP DỮ LIỆU MẪU KIỂM THỬ NGHIỆP VỤ (SEEDS DATA)
-- ======================================================================================
USE [QuanLyCuaHangThuCung]
GO

-- 1. Tài khoản & Nhân viên (4 vai trò: NVQL, NVTN, NVBH, NVCS)
INSERT INTO [dbo].[TaiKhoan] ([tenDangNhap], [matKhau], [vaiTro], [trangThaiHoatDong]) VALUES 
('admin', '123456', 'NVQL', 1),
('thungan01', '123456', 'NVTN', 1),
('ktv01', '123456', 'NVCS', 1),
('banhang01', '123456', 'NVBH', 1),
('ktv02', '123456', 'NVCS', 1),
('banhang02', '123456', 'NVBH', 0);
GO

INSERT INTO [dbo].[NhanVien] ([maNhanVien], [hoTen], [chucVu], [soDienThoai], [tenDangNhap]) VALUES 
('NV001', N'Lê Khãi Minh', N'Quản lý hệ thống', '0901234567', 'admin'),
('NV002', N'Lê Anh Minh', N'Thu ngân ca sáng', '0912345678', 'thungan01'),
('NV003', N'Tô Thanh Đạt', N'Kỹ thuật viên chăm sóc', '0923456789', 'ktv01'),
('NV004', N'Trần Thị Thu Hà', N'Nhân viên bán hàng', '0934567890', 'banhang01'),
('NV005', N'Võ Thị Hồng Ngọc', N'Chuyên viên Spa thú cưng', '0945678901', 'ktv02'),
('NV006', N'Lê Anh Tuấn', N'Nhân viên bán hàng', '0956789012', 'banhang02');
GO

-- 2. Ca làm việc (4 ca chuẩn)
INSERT INTO [dbo].[CaLamViec] ([maCa], [tenCa], [gioBatDau], [gioKetThuc]) VALUES 
('CA01', N'Ca 1 (07:00 - 11:30)', '07:00:00', '11:30:00'),
('CA02', N'Ca 2 (11:30 - 15:30)', '11:30:00', '15:30:00'),
('CA03', N'Ca 3 (15:30 - 19:30)', '15:30:00', '19:30:00'),
('CA04', N'Ca 4 (19:30 - 22:00)', '19:30:00', '22:00:00');
GO

-- 3. Khách hàng & Thú cưng
INSERT INTO [dbo].[KhachHang] ([maKhachHang], [hoTen], [soDienThoai], [diemTichLuy]) VALUES 
('KH001', N'Nguyễn Văn An', '0988111222', 120),
('KH002', N'Trần Thị Mai', '0977333444', 50),
('KH003', N'Lê Hoàng Yến', '0912345678', 15),
('KH004', N'Phạm Hoàng Long', '0918776655', 210),
('KH005', N'Đỗ Quỳnh Chi', '0905123456', 35);
GO

INSERT INTO [dbo].[ThuCung] ([maThuCung], [tenThuCung], [giongLoai], [ngaySinh], [gioiTinh]) VALUES 
('TC001', N'Miu Miu', N'Mèo Anh Lông Ngắn', '2023-05-10', N'Cái'),
('TC002', N'Lu Lu', N'Chó Poodle', '2022-11-20', N'Đực'),
('TC003', N'Bông', N'Chó Pomeranian (Phốc Sóc)', '2024-02-14', N'Cái'),
('TC004', N'Bim Bim', N'Chó Corgi', '2023-08-05', N'Đực');
GO

INSERT INTO [dbo].[HoSoThuCung] ([maHoSo], [maKhachHang], [maThuCung], [soMicrochip], [ngayDangKy], [trangThaiNuoi]) VALUES 
('HS001', 'KH001', 'TC001', 'MC982001', '2023-06-01', N'Đang nuôi'),
('HS002', 'KH002', 'TC002', 'MC982002', '2023-01-15', N'Đang nuôi'),
('HS003', 'KH003', 'TC003', 'MC982003', '2024-03-01', N'Đang nuôi'),
('HS004', 'KH001', 'TC004', 'MC982004', '2023-09-10', N'Đang nuôi');
GO

INSERT INTO [dbo].[HoSoSucKhoe] ([maSucKhoe], [maThuCung], [maNhanVienKiemTra], [canNang], [nhietDo], [tinhTrangVeRan], [tiemPhongDai], [ghiChuTheTrang], [ngayKiemTra]) VALUES 
('SK001', 'TC001', 'NV003', 4.20, 38.50, N'Không có ve rận', 1, N'Thể trạng tốt, lông mượt, đủ điều kiện tắm sấy', DATEADD(day, -2, GETDATE())),
('SK002', 'TC002', 'NV003', 5.80, 38.70, N'Có ít ve ở tai trái', 1, N'Khuyến nghị gói tắm thảo dược trị ve rận', DATEADD(day, -1, GETDATE())),
('SK003', 'TC003', 'NV003', 2.40, 38.40, N'Sạch sẽ', 1, N'Chó nhỏ hiếu động, cần sấy nhiệt độ ấm vừa phải', DATEADD(hour, -4, GETDATE()));
GO

-- 4. Dịch vụ chăm sóc & Spa
INSERT INTO [dbo].[DichVu] ([maDichVu], [tenDichVu], [giaDichVu], [thoiLuongPhut]) VALUES 
('DV001', N'Cắt Tỉa Lông Tạo Kiểu Thẩm Mỹ', 300000, 90),
('DV002', N'Cắt Mài Móng & Vệ Sinh Tai', 100000, 30),
('DV003', N'Vắt Tuyến Hôi & Cạo Bàn Chân', 90000, 30),
('DV004', N'Tắm Sấy Khử Mùi & Dưỡng Lông', 150000, 45),
('DV005', N'Tắm Thảo Dược & Trị Ve Rận', 220000, 60),
('DV006', N'Ngâm Bồn Sục Ozone Thư Giãn', 250000, 60),
('DV007', N'Nhuộm Lông Nghệ Thuật (Tai & Đuôi)', 180000, 60),
('DV008', N'Lưu Chuồng Pet Hotel (VIP)', 200000, 1440);
GO

-- 5. Lịch hẹn chăm sóc mẫu
INSERT INTO [dbo].[LichHen] ([maLichHen], [maKhachHang], [maNhanVienTiepNhan], [ngayHen], [trangThaiLichHen], [ghiChu]) VALUES 
('LH001', 'KH001', 'NV003', DATEADD(hour, 1, GETDATE()), N'Đã đặt', N'Khách dặn cắt tỉa gọn vùng mắt'),
('LH002', 'KH002', 'NV003', DATEADD(minute, -30, GETDATE()), N'Đang phục vụ', N'Đang sấy bông và chải lông'),
('LH003', 'KH003', 'NV003', DATEADD(hour, -3, GETDATE()), N'Hoàn thành', N'Đã bàn giao thú cưng cho chủ'),
('LH004', 'KH001', 'NV003', DATEADD(hour, 3, GETDATE()), N'Đã đặt', N'Tắm khử mùi cho bé Corgi');
GO

INSERT INTO [dbo].[ChiTietLichHen] ([maChiTietLichHen], [maLichHen], [maThuCung], [maDichVu], [maKtvChiDinh]) VALUES 
('CTLH01', 'LH001', 'TC001', 'DV001', 'NV003'),
('CTLH02', 'LH002', 'TC002', 'DV002', 'NV003'),
('CTLH03', 'LH003', 'TC003', 'DV003', 'NV003'),
('CTLH04', 'LH004', 'TC004', 'DV001', 'NV003');
GO

-- 6. Danh mục, Sản phẩm, Đơn vị tính & Quy đổi
INSERT INTO [dbo].[DanhMucHang] ([maDanhMuc], [tenDanhMuc], [moTa]) VALUES 
('DM01', N'Thức ăn thú cưng', N'Hạt khô, pate dinh dưỡng'),
('DM02', N'Phụ kiện & Đồ chơi', N'Vòng cổ, đồ cào móng, dây dắt'),
('DM03', N'Vệ sinh & Chăm sóc', N'Cát vệ sinh, sữa tắm, xịt khử mùi');
GO

INSERT INTO [dbo].[SanPham] ([maSanPham], [maDanhMuc], [tenSanPham], [moTa]) VALUES 
('SP001', 'DM01', N'Hạt Royal Canin Kitten 2kg', N'Dành cho mèo con dưới 12 tháng'),
('SP002', 'DM01', N'Pate Whiskas Vị Cá Thu 85g', N'Bổ sung dinh dưỡng cho mèo trưởng thành'),
('SP003', 'DM01', N'Pate Whiskas (Lốc 6 lon)', N'Combo 6 lon tiết kiệm'),
('SP004', 'DM02', N'Vòng Cổ Chuông Nơ Thú Cưng', N'Vòng cổ vải lụa mềm chuông đồng'),
('SP005', 'DM03', N'Sữa Tắm Dưỡng Lông Bio-Care', N'Sữa tắm thảo mộc ngừa rụng lông'),
('SP006', 'DM03', N'Cát Vệ Sinh Mèo Hương Táo 5kg', N'Cát đất sét vón cục khử mùi');
GO

INSERT INTO [dbo].[DonViTinh] ([maDonVi], [tenDonVi]) VALUES 
('DVT01', N'Gói'),
('DVT02', N'Lon'),
('DVT03', N'Lốc 6 lon'),
('DVT04', N'Thùng 24 lon'),
('DVT05', N'Chiếc'),
('DVT06', N'Chai'),
('DVT07', N'Túi');
GO

INSERT INTO [dbo].[QuyDoiDonVi] ([maQuyDoi], [maSanPham], [maDonVi], [tyLeQuyDoi], [laDonViCoSo], [giaBan]) VALUES 
('QD001', 'SP001', 'DVT01', 1, 1, 380000),
('QD002', 'SP002', 'DVT02', 1, 1, 20000),
('QD003', 'SP002', 'DVT03', 6, 0, 115000),
('QD004', 'SP002', 'DVT04', 24, 0, 450000),
('QD005', 'SP003', 'DVT03', 1, 1, 115000),
('QD006', 'SP004', 'DVT05', 1, 1, 45000),
('QD007', 'SP005', 'DVT06', 1, 1, 85000),
('QD008', 'SP006', 'DVT07', 1, 1, 65000);
GO

-- 7. Nhà cung cấp & Lô hàng FEFO
INSERT INTO [dbo].[NhaCungCap] ([maNhaCungCap], [tenNhaCungCap], [diaChi], [soDienThoai]) VALUES 
('NCC01', N'Công ty Cổ phần Thức Ăn Thú Cưng Việt Nam', N'123 CMT8, Q.10, TP.HCM', '0283999888'),
('NCC02', N'Pet Accessories Sài Gòn', N'456 Lê Văn Sỹ, Q.3, TP.HCM', '0909888777');
GO

INSERT INTO [dbo].[LoHang] ([soLo], [maSanPham], [maNhaCungCap], [ngaySanXuat], [hanSuDung], [donGiaNhapCoSo], [soLuongNhap], [soLuongTon]) VALUES 
('LO_RC_2026_01', 'SP001', 'NCC01', '2026-01-10', '2027-01-10', 280000, 50, 45),
('LO_WS_2026_01', 'SP002', 'NCC01', '2026-02-01', '2027-08-01', 14000, 200, 180),
('LO_WS_LOC_01', 'SP003', 'NCC01', '2026-02-01', '2027-08-01', 80000, 50, 30),
('LO_PK_2026_01', 'SP004', 'NCC02', '2026-01-01', '2028-01-01', 25000, 100, 60),
('LO_BC_2026_02', 'SP005', 'NCC01', '2026-02-15', '2028-02-15', 50000, 50, 35),
('LO_CAT_2026_01', 'SP006', 'NCC01', '2026-03-01', '2028-03-01', 40000, 100, 50);
GO

-- 8. Hóa đơn mẫu & Chi tiết xuất lô / Dịch vụ
INSERT INTO [dbo].[HoaDon] ([maHoaDon], [maKhachHang], [maNhanVien], [ngayLap], [thoiGianThanhToan], [giamGia], [thanhTien], [phuongThucThanhToan], [trangThaiHoaDon]) VALUES 
('HD982104', 'KH001', 'NV002', DATEADD(hour, -3, GETDATE()), DATEADD(hour, -3, GETDATE()), 20000, 380000, N'Chuyển khoản QR', N'Đã thanh toán'),
('HD981502', 'KH002', 'NV002', DATEADD(hour, -5, GETDATE()), DATEADD(hour, -5, GETDATE()), 0, 300000, N'Tiền mặt', N'Đã thanh toán');
GO

INSERT INTO [dbo].[ChiTietHoaDon] ([maChiTiet], [maHoaDon], [loaiChiTiet], [soLuong], [donGiaLucBan], [thanhTien]) VALUES 
('HD982104_CT1', 'HD982104', 'SAN_PHAM', 1, 380000, 380000),
('HD982104_CT2', 'HD982104', 'SAN_PHAM', 1, 20000, 20000),
('HD981502_CT1', 'HD981502', 'DICH_VU', 1, 300000, 300000);
GO

INSERT INTO [dbo].[ChiTietXuatLo] ([maChiTiet], [soLo], [maQuyDoi], [soLuongXuatCoSo]) VALUES 
('HD982104_CT1', 'LO_RC_2026_01', 'QD001', 1),
('HD982104_CT2', 'LO_WS_2026_01', 'QD002', 1);
GO

INSERT INTO [dbo].[ChiTietDichVu] ([maChiTiet], [maDichVu], [maThuCung], [maKtvThucHien], [tienHoaHong]) VALUES 
('HD981502_CT1', 'DV001', 'TC002', 'NV003', 30000);
GO

-- 9. Biên bản Bàn giao ca mẫu
INSERT INTO [dbo].[BangGiaoCa] ([maGiaoCa], [maNhanVien], [maCa], [tienDauCa], [tienCuoiCaHeThong], [tienCuoiCaThucTe], [tienChenhLech], [lyDoChenhLech], [gioKetThucThucTe]) VALUES 
('GC001', 'NV002', 'CA01', 2000000, 6850000, 6850000, 0, N'Khớp 100% — Đã nộp quỹ cho Quản lý', DATEADD(day, -2, GETDATE())),
('GC002', 'NV004', 'CA02', 2000000, 5420000, 5400000, -20000, N'Lệch -20,000 đ (Thối nhầm tiền lẻ khách vãng lai)', DATEADD(day, -1, GETDATE())),
('GC003', 'NV001', 'CA03', 2000000, 4190000, 4190000, 0, N'Khớp 100% — Chốt két cuối ngày an toàn', DATEADD(hour, -12, GETDATE()));
GO

-- 10. Phòng Lưu Trú Pet Hotel mẫu (12 phòng đầy đủ 3 loại)
INSERT INTO [dbo].[PhongLuuTru] ([maPhong], [loaiPhong], [donGiaNgay], [trangThai], [tenThuCung], [giongLoai], [tenChu], [sdtChu], [thoiGianCheckIn], [duKienCheckOut], [ghiChuChamSoc], [daChoAnSang], [daChoAnToi], [daDiDaoVeSinh], [phiDichVuPhatSinh]) VALUES 
('P101', N'Phòng Tiêu chuẩn', 150000, N'ĐANG CÓ KHÁCH', N'Bim Bim', N'Corgi (8.5kg)', N'Nguyễn Văn An', '0988111222', DATEADD(day, -2, GETDATE()), DATEADD(day, 1, GETDATE()), N'Ăn hạt Royal Canin ngâm mềm, dắt đi dạo 2 lần/ngày', 1, 1, 1, 0),
('P102', N'Phòng Tiêu chuẩn', 150000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0),
('P103', N'Phòng Tiêu chuẩn', 150000, N'ĐANG CÓ KHÁCH', N'Bơ', N'Poodle Tiny (3.2kg)', N'Trần Thị Mai', '0977333444', DATEADD(day, -1, GETDATE()), DATEADD(day, 2, GETDATE()), N'Ăn kèm pate gà, chải lông mỗi chiều', 1, 0, 1, 0),
('P104', N'Phòng Tiêu chuẩn', 150000, N'ĐANG DỌN DẸP', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0),
('V201', N'Phòng VIP Suite', 250000, N'ĐANG CÓ KHÁCH', N'Lu Lu', N'Golden Retriever (24kg)', N'Phạm Hoàng Long', '0918776655', DATEADD(day, -3, GETDATE()), DATEADD(day, 1, GETDATE()), N'Camera VIP bật 24/7 cho chủ xem, ăn 3 bữa', 1, 1, 1, 0),
('V202', N'Phòng VIP Suite', 250000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0),
('V203', N'Phòng VIP Suite', 250000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0),
('V204', N'Phòng VIP Suite', 250000, N'ĐANG CÓ KHÁCH', N'Sammy', N'Samoyed (19kg)', N'Đỗ Quỳnh Chi', '0905123456', DATEADD(hour, -8, GETDATE()), DATEADD(day, 3, GETDATE()), N'Bật điều hòa 22 độ C liên tục, chải lông kĩ', 1, 0, 1, 0),
('C301', N'Condo cho Mèo', 200000, N'ĐANG CÓ KHÁCH', N'Miu Miu', N'Mèo Anh Lông Ngắn (4.5kg)', N'Lê Hoàng Yến', '0912345678', DATEADD(day, -1, GETDATE()), DATEADD(day, 2, GETDATE()), N'Ăn hạt Orijen + súp thưởng Ciao lúc 15h', 1, 0, 1, 0),
('C302', N'Condo cho Mèo', 200000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0),
('C303', N'Condo cho Mèo', 200000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0),
('C304', N'Condo cho Mèo', 200000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0);
GO

PRINT N'======================================================================================';
PRINT N'ĐÃ HOÀN TẤT KHỞI TẠO CSDL VÀ NẠP DỮ LIỆU MẪU QUANLYCUAHANGTHUCUNG THÀNH CÔNG 100%!';
PRINT N'======================================================================================';
GO
