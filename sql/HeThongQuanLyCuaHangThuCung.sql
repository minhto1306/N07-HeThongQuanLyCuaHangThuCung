-- ======================================================================================
-- ĐỒ ÁN: HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG MIO (PET SHOP MANAGEMENT SYSTEM)
-- MÔN HỌC: PHÁT TRIỂN PHẦN MỀM / CÔNG NGHỆ JAVA
-- ĐƠN VỊ: KHOA CÔNG NGHỆ THÔNG TIN - ĐẠI HỌC CÔNG NGHIỆP TP.HCM (IUH) - NHÓM 07
-- GVHD: ThS. PHẠM THANH HÙNG
-- TẬP TIN DUY NHẤT: KHỞI TẠO CSDL TOÀN DIỆN VÀ NẠP DỮ LIỆU MẪU MỌI TRƯỜNG HỢP KIỂM THỬ
-- ======================================================================================
USE master;
GO

-- 1. XÓA SẠCH CƠ SỞ DỮ LIỆU CŨ NẾU ĐÃ TỒN TẠI ĐỂ TÁI LẬP MỚI 100%
IF EXISTS (SELECT 1 FROM sys.databases WHERE name = 'QuanLyCuaHangThuCung')
BEGIN
    ALTER DATABASE QuanLyCuaHangThuCung SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLyCuaHangThuCung;
    PRINT N'-> Đã xóa cơ sở dữ liệu cũ QuanLyCuaHangThuCung thành công!';
END
GO

CREATE DATABASE QuanLyCuaHangThuCung;
GO

USE QuanLyCuaHangThuCung;
GO

PRINT N'-> Đã khởi tạo cơ sở dữ liệu mới QuanLyCuaHangThuCung thành công!';
GO

-- ======================================================================================
-- PHẦN I: KHỞI TẠO CẤU TRÚC BẢNG (SCHEMA DEFINITION TOÀN BỘ CÁC PHÂN HỆ)
-- ======================================================================================
-- 1. Bảng Tài khoản người dùng (Phân quyền RBAC 4 vai trò chuẩn: NVQL, NVTN, NVBH, NVCS)
CREATE TABLE TaiKhoan (
    maTaiKhoan NVARCHAR(20) PRIMARY KEY,       -- Tự động tăng: NV001, NV002, ... (khớp maNhanVien)
    soDienThoai NVARCHAR(15) NOT NULL UNIQUE,  -- Dùng để nhân viên đăng nhập
    email NVARCHAR(100) NOT NULL UNIQUE,       -- Dùng để xác thực quên mật khẩu
    matKhau NVARCHAR(64) NOT NULL,             -- Chuỗi băm SHA-256 (64 ký tự hex)
    vaiTro NVARCHAR(20) NOT NULL CHECK (vaiTro IN ('NVQL', 'NVTN', 'NVBH', 'NVCS')), -- 4 vai trò cốt lõi
    trangThaiHoatDong BIT NOT NULL DEFAULT 1   -- 1: Đang làm việc, 0: Khóa/Đã nghỉ
);
GO

-- 2. Bảng Nhân viên
CREATE TABLE NhanVien (
    maNhanVien NVARCHAR(20) PRIMARY KEY,       -- Tự động tăng: NV001, NV002, ...
    hoTen NVARCHAR(100) NOT NULL,
    chucVu NVARCHAR(50) NOT NULL,
    soDienThoai NVARCHAR(15) NOT NULL UNIQUE,
    email NVARCHAR(100) NOT NULL,
    maTaiKhoan NVARCHAR(20) NOT NULL UNIQUE,
    CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (maTaiKhoan) REFERENCES TaiKhoan(maTaiKhoan) ON DELETE CASCADE
);
GO

-- 3. Bảng Ca làm việc
CREATE TABLE CaLamViec (
    maCa NVARCHAR(20) PRIMARY KEY,
    tenCa NVARCHAR(50) NOT NULL,
    gioBatDau TIME NOT NULL,
    gioKetThuc TIME NOT NULL
);
GO

-- 4. Bảng Phân công ca trực
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
GO

-- 5. Bảng Bàn giao ca thu ngân & Két tiền (Có kiểm soát tiền mặt, chuyển khoản, hoàn trả và duyệt ca)
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
    trangThai NVARCHAR(20) NOT NULL DEFAULT 'CHO_DUYET', -- DANG_MO, CHO_DUYET, DA_DUYET
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
GO

CREATE UNIQUE INDEX UQ_BangGiaoCa_NhanVien_Ngay_Ca
ON BangGiaoCa(maNhanVien, ngayGiaoCa, maCa)
WHERE ngayGiaoCa IS NOT NULL;
GO

-- 6. Bảng Khách hàng thành viên (Đã tích hợp email nhận thông báo và địa chỉ)
CREATE TABLE KhachHang (
    maKhachHang NVARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDienThoai NVARCHAR(15) NOT NULL UNIQUE,
    diemTichLuy INT NOT NULL DEFAULT 0 CHECK (diemTichLuy >= 0),
    email NVARCHAR(100) NULL,
    diaChi NVARCHAR(255) NULL
);
GO

-- 7. Bảng Thú cưng
CREATE TABLE ThuCung (
    maThuCung NVARCHAR(20) PRIMARY KEY,
    tenThuCung NVARCHAR(100) NOT NULL,
    giongLoai NVARCHAR(50),
    ngaySinh DATE,
    gioiTinh NVARCHAR(10) -- Đực, Cái
);
GO

-- 8. Bảng Hồ sơ thú cưng & Định danh Microchip
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

-- 9. Bảng Sổ theo dõi sức khỏe & Thể trạng y tế thú cưng
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

-- 10. Bảng Danh mục Dịch vụ Spa & Grooming
CREATE TABLE DichVu (
    maDichVu NVARCHAR(20) PRIMARY KEY,
    tenDichVu NVARCHAR(100) NOT NULL,
    giaDichVu DECIMAL(18,2) NOT NULL,
    thoiLuongPhut INT NOT NULL
);
GO

-- 11. Bảng Lịch hẹn dịch vụ
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

-- 12. Bảng Chi tiết lịch hẹn (Phân công Kỹ thuật viên)
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

-- 13. Bảng Danh mục hàng hóa
CREATE TABLE DanhMucHang (
    maDanhMuc NVARCHAR(20) PRIMARY KEY,
    tenDanhMuc NVARCHAR(100) NOT NULL,
    moTa NVARCHAR(255)
);
GO

-- 14. Bảng Sản phẩm
CREATE TABLE SanPham (
    maSanPham NVARCHAR(20) PRIMARY KEY,
    maDanhMuc NVARCHAR(20) NOT NULL,
    tenSanPham NVARCHAR(100) NOT NULL,
    maVach NVARCHAR(30) NULL UNIQUE,
    donViCoSo NVARCHAR(30) NOT NULL DEFAULT N'Đơn vị',
    giaBanCoSo DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (giaBanCoSo >= 0),
    moTa NVARCHAR(255),
    CONSTRAINT FK_SanPham_DanhMuc FOREIGN KEY (maDanhMuc) REFERENCES DanhMucHang(maDanhMuc)
);
GO

-- 15. Bảng Đơn vị tính
CREATE TABLE DonViTinh (
    maDonVi NVARCHAR(20) PRIMARY KEY,
    tenDonVi NVARCHAR(50) NOT NULL
);
GO

-- 16. Bảng Quy đổi đơn vị tính đa cấp
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
GO

-- 17. Bảng Nhà cung cấp
CREATE TABLE NhaCungCap (
    maNhaCungCap NVARCHAR(20) PRIMARY KEY,
    tenNhaCungCap NVARCHAR(100) NOT NULL,
    diaChi NVARCHAR(255),
    soDienThoai NVARCHAR(20)
);
GO

-- 18. Bảng Lô hàng (Quản lý hạn sử dụng theo thuật toán FEFO)
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
GO

-- 19. Bảng Hóa đơn bán hàng POS
CREATE TABLE HoaDon (
    maHoaDon NVARCHAR(20) PRIMARY KEY,
    maKhachHang NVARCHAR(20) NULL,
    maNhanVien NVARCHAR(20) NOT NULL,
    ngayLap DATETIME2 NOT NULL,
    thoiGianThanhToan DATETIME2,
    giamGia DECIMAL(18,2) DEFAULT 0,
    thanhTien DECIMAL(18,2) NOT NULL,
    phuongThucThanhToan NVARCHAR(50), -- Tiền mặt, Chuyển khoản QR, Thẻ
    trangThaiHoaDon NVARCHAR(50) DEFAULT N'Đã thanh toán',
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
GO

-- 20. Bảng Chi tiết hóa đơn
CREATE TABLE ChiTietHoaDon (
    maChiTiet NVARCHAR(50) PRIMARY KEY,
    maHoaDon NVARCHAR(20) NOT NULL,
    loaiChiTiet NVARCHAR(20) NOT NULL, -- SAN_PHAM, DICH_VU
    maSanPham NVARCHAR(20) NULL,
    maDichVu NVARCHAR(20) NULL,
    soLuong INT NOT NULL,
    donGiaLucBan DECIMAL(18,2) NOT NULL,
    thanhTien DECIMAL(18,2) NOT NULL,
    CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE,
    CONSTRAINT FK_CTHD_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham),
    CONSTRAINT FK_CTHD_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu)
);
GO

-- 21. Bảng Chi tiết xuất kho theo lô hàng (FEFO Tracking)
CREATE TABLE ChiTietXuatLo (
    maChiTietXuatLo INT IDENTITY(1,1) PRIMARY KEY,
    maChiTiet NVARCHAR(50) NOT NULL,
    soLo NVARCHAR(50) NOT NULL,
    maQuyDoi NVARCHAR(20) NOT NULL,
    soLuongXuatCoSo INT NOT NULL,
    CONSTRAINT FK_CTXuatLo_CTHD FOREIGN KEY (maChiTiet) REFERENCES ChiTietHoaDon(maChiTiet) ON DELETE CASCADE,
    CONSTRAINT FK_CTXuatLo_LoHang FOREIGN KEY (soLo) REFERENCES LoHang(soLo),
    CONSTRAINT FK_CTXuatLo_QuyDoi FOREIGN KEY (maQuyDoi) REFERENCES QuyDoiDonVi(maQuyDoi)
);
GO

-- 22. Bảng Chi tiết dịch vụ thực hiện & Hoa hồng kỹ thuật viên (10%)
CREATE TABLE ChiTietDichVu (
    maChiTietDichVu INT IDENTITY(1,1) PRIMARY KEY,
    maChiTiet NVARCHAR(50) NOT NULL,
    maDichVu NVARCHAR(20) NOT NULL,
    maThuCung NVARCHAR(20),
    maKtvThucHien NVARCHAR(20),
    tienHoaHong DECIMAL(18,2) DEFAULT 0,
    CONSTRAINT FK_CTDV_CTHD FOREIGN KEY (maChiTiet) REFERENCES ChiTietHoaDon(maChiTiet) ON DELETE CASCADE,
    CONSTRAINT FK_CTDV_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu),
    CONSTRAINT FK_CTDV_ThuCung FOREIGN KEY (maThuCung) REFERENCES ThuCung(maThuCung),
    CONSTRAINT FK_CTDV_Ktv FOREIGN KEY (maKtvThucHien) REFERENCES NhanVien(maNhanVien)
);
GO

-- 23. Bảng Phiếu đổi trả hàng hóa
CREATE TABLE PhieuDoiTra (
    maPhieuDoiTra NVARCHAR(20) PRIMARY KEY,
    maHoaDonGoc NVARCHAR(20) NOT NULL,
    maNhanVienTiepNhan NVARCHAR(20) NOT NULL,
    ngayDoiTra DATETIME2 NOT NULL,
    tongTienHoan DECIMAL(18,2) NOT NULL,
    lyDoDoiTra NVARCHAR(255),
    trangThaiDoiTra NVARCHAR(50) DEFAULT N'Hoàn thành',
    CONSTRAINT FK_DoiTra_HoaDon FOREIGN KEY (maHoaDonGoc) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT FK_DoiTra_NhanVien FOREIGN KEY (maNhanVienTiepNhan) REFERENCES NhanVien(maNhanVien)
);
GO

-- 24. Bảng Chi tiết đổi trả
CREATE TABLE ChiTietDoiTra (
    maChiTietDoiTra NVARCHAR(50) PRIMARY KEY,
    maPhieuDoiTra NVARCHAR(20) NOT NULL,
    maSanPham NVARCHAR(20) NOT NULL,
    soLuongDoiTra INT NOT NULL DEFAULT 1,
    tienHoanLai DECIMAL(18,2) NOT NULL DEFAULT 0,
    soLo NVARCHAR(50) NULL,
    soLuongCoSoHoan INT NOT NULL DEFAULT 0,
    soLuong INT NULL,
    donGiaHoan DECIMAL(18,2) NULL,
    lyDoChiTiet NVARCHAR(255),
    CONSTRAINT FK_CTDoiTra_Phieu FOREIGN KEY (maPhieuDoiTra) REFERENCES PhieuDoiTra(maPhieuDoiTra) ON DELETE CASCADE,
    CONSTRAINT FK_CTDoiTra_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham),
    CONSTRAINT FK_CTDoiTra_LoHang FOREIGN KEY (soLo) REFERENCES LoHang(soLo),
    CONSTRAINT CK_CTDoiTra_SoLuongCoSoHoan CHECK (soLuongCoSoHoan >= 0)
);
GO

-- 25. Bảng Quản lý Khách sạn lưu trú thú cưng (Pet Hotel & Boarding Matrix)
CREATE TABLE PhongLuuTru (
    maPhong NVARCHAR(20) PRIMARY KEY,
    loaiPhong NVARCHAR(50) NOT NULL,       -- Phòng Tiêu chuẩn, Phòng VIP Suite, Condo cho Mèo
    donGiaNgay BIGINT NOT NULL,
    trangThai NVARCHAR(50) NOT NULL,       -- TRỐNG, ĐANG CÓ KHÁCH, ĐANG DỌN DẸP
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

-- 26. Bảng Thẻ Gói Spa Pass (Punch Card định kỳ tích hợp POS)
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
GO

CREATE UNIQUE INDEX UQ_TheSpaPass_HoaDonKichHoat
ON TheSpaPass(maHoaDonKichHoat) WHERE maHoaDonKichHoat IS NOT NULL;
GO

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
GO

-- 27. Bảng Lịch sử lưu trú Khách sạn thú cưng (Pet Hotel Checkout History)
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
GO

CREATE UNIQUE INDEX UQ_LichSuLuuTru_HoaDon
ON LichSuLuuTru(maHoaDon) WHERE maHoaDon IS NOT NULL;
GO

PRINT N'-> Hoàn thành tạo cấu trúc bảng (Schema) thành công!';
GO

-- ======================================================================================
-- PHẦN II: NẠP DỮ LIỆU MẪU KIỂM THỬ MỌI KỊCH BẢN VÀ TRƯỜNG HỢP NGHIỆP VỤ (SEED DATA)
-- ======================================================================================
-- 1. Tài khoản & Nhân viên (4 thành viên phụ trách 4 phân hệ cốt lõi + KTV + NV nghỉ việc)
INSERT INTO TaiKhoan (maTaiKhoan, soDienThoai, email, matKhau, vaiTro, trangThaiHoatDong) VALUES
('NV001', '0901234567', 'minh.quanly@miopet.vn', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'NVQL',  1),
('NV002', '0923456789', 'dat.kho@miopet.vn',      '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'NVQL',  1),
('NV003', '0912345678', 'minh.thungan@miopet.vn',  '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'NVTN',  1),
('NV004', '0945678901', 'ngoc.spa@miopet.vn',      '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'NVCS',  1),
('NV005', '0934567890', 'ha.ktv@miopet.vn',        '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'NVCS',  1),
('NV006', '0956789012', 'hung.banhang@miopet.vn',  '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'NVBH',  1);
INSERT INTO NhanVien (maNhanVien, hoTen, chucVu, soDienThoai, email, maTaiKhoan) VALUES
('NV001', N'Lê Khãi Minh',    N'Quản lý hệ thống',        '0901234567', 'minh.quanly@miopet.vn', 'NV001'),
('NV002', N'Tô Thành Đạt',     N'Quản lý kho & Lô hàng',   '0923456789', 'dat.kho@miopet.vn',      'NV002'),
('NV003', N'Lê Anh Minh',      N'Thu ngân trưởng ca',      '0912345678', 'minh.thungan@miopet.vn',  'NV003'),
('NV004', N'Võ Thị Hồng Ngọc', N'Chuyên viên Spa & Hotel', '0945678901', 'ngoc.spa@miopet.vn',      'NV004'),
('NV005', N'Trần Thị Thu Hà',  N'Kỹ thuật viên chăm sóc',  '0934567890', 'ha.ktv@miopet.vn',        'NV005'),
('NV006', N'Nguyễn Văn Hùng',  N'Nhân viên bán hàng (Cũ)', '0956789012', 'hung.banhang@miopet.vn',  'NV006');
GO

-- 2. Ca làm việc cố định
INSERT INTO CaLamViec (maCa, tenCa, gioBatDau, gioKetThuc) VALUES
('CA01', N'Ca Sáng (07:00 - 11:30)', '07:00:00', '11:30:00'),
('CA02', N'Ca Trưa (11:30 - 15:30)', '11:30:00', '15:30:00'),
('CA03', N'Ca Chiều (15:30 - 19:30)', '15:30:00', '19:30:00'),
('CA04', N'Ca Tối (19:30 - 22:00)',  '19:30:00', '22:00:00');
GO

-- 3. Phân công ca trực (Phủ kín các ngày trong tuần hiện tại để hiển thị ma trận và xuất Excel)
INSERT INTO BangPhanCong (maNhanVien, maCa, ngayLamViec, ghiChu) VALUES
-- Ngày hôm qua (-1)
('NV002', 'CA01', DATEADD(day, -1, CAST(GETDATE() AS DATE)), N'Kiểm kê lô hàng nhập sáng'),
('NV003', 'CA02', DATEADD(day, -1, CAST(GETDATE() AS DATE)), N'Trực quầy POS thu ngân'),
('NV004', 'CA03', DATEADD(day, -1, CAST(GETDATE() AS DATE)), N'Nhận tắm sấy chó mèo'),
-- Ngày hôm nay (0)
('NV001', 'CA01', CAST(GETDATE() AS DATE), N'Mở cửa cửa hàng & Họp giao ban đầu ngày'),
('NV003', 'CA01', CAST(GETDATE() AS DATE), N'Trực ca thu ngân sáng'),
('NV002', 'CA02', CAST(GETDATE() AS DATE), N'Soát đơn vị tính quy đổi và xếp kho'),
('NV004', 'CA02', CAST(GETDATE() AS DATE), N'Theo dõi dinh dưỡng chuồng Pet Hotel'),
('NV005', 'CA03', CAST(GETDATE() AS DATE), N'Cắt tỉa tạo kiểu thú cưng'),
('NV003', 'CA04', CAST(GETDATE() AS DATE), N'Chốt ca thu ngân tối & Khóa két tiền'),
-- Ngày mai (+1)
('NV002', 'CA01', DATEADD(day, 1, CAST(GETDATE() AS DATE)), N'Nhập hàng nhà cung cấp NCC01'),
('NV004', 'CA01', DATEADD(day, 1, CAST(GETDATE() AS DATE)), N'Đón thú cưng khách sạn check-in'),
('NV003', 'CA02', DATEADD(day, 1, CAST(GETDATE() AS DATE)), N'Trực quầy bán lẻ'),
('NV005', 'CA03', DATEADD(day, 1, CAST(GETDATE() AS DATE)), N'Ca chăm sóc Spa chiều');
GO

-- 4. Bàn giao ca thu ngân & Đối soát tiền két (Đủ các kịch bản: Khớp két, Thiếu tiền chờ duyệt, Ca đang mở)
INSERT INTO BangGiaoCa (maGiaoCa, maNhanVien, maCa, tienDauCa, tienCuoiCaHeThong, tienCuoiCaThucTe, tienChenhLech, tienThuTienMat, tienChuyenKhoan, tienHoanTra, lyDoChenhLech, gioKetThucThucTe, trangThai, maQuanLyDuyet, thoiGianDuyet) VALUES
-- Ca 1: Khớp két 100%, Quản lý đã duyệt ký nhận K80
('GC001', 'NV003', 'CA01', 1000000, 3500000, 3500000, 0, 2500000, 850000, 0, 
 N'Khớp két 100%, không phát sinh sai lệch', 
 DATEADD(day, -2, DATEADD(hour, 11, DATEADD(minute, 30, CAST(CAST(GETDATE() AS DATE) AS DATETIME2)))), 
 'DA_DUYET', 'NV001', DATEADD(day, -2, DATEADD(hour, 11, DATEADD(minute, 45, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))))),
-- Ca 2: ĐANG CHỜ DUYỆT (CHO_DUYET), bị THIẾU 50.000đ do thu ngân thối nhầm tiền -> Dùng để demo chức năng Duyệt ca & in K80
('GC002', 'NV005', 'CA02', 3500000, 5200000, 5150000, -50000, 1700000, 450000, 20000, 
 N'Thối nhầm tiền thừa 50.000đ cho khách mua hạt Royal Canin', 
 DATEADD(day, -1, DATEADD(hour, 15, DATEADD(minute, 35, CAST(CAST(GETDATE() AS DATE) AS DATETIME2)))), 
 'CHO_DUYET', NULL, NULL),
-- Ca 3: ĐÃ DUYỆT, có phát sinh giao dịch hoàn tiền trả hàng
('GC003', 'NV003', 'CA03', 2000000, 4800000, 4800000, 0, 2800000, 1200000, 65000, 
 N'Đã hoàn tiền đổi trả 1 túi cát vệ sinh cho khách Mai', 
 DATEADD(day, -1, DATEADD(hour, 19, DATEADD(minute, 30, CAST(CAST(GETDATE() AS DATE) AS DATETIME2)))), 
 'DA_DUYET', 'NV001', DATEADD(day, -1, DATEADD(hour, 19, DATEADD(minute, 40, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))))),
-- Ca 4: ĐANG MỞ (DANG_MO) của ca trực hiện tại -> Để thu ngân thao tác Bàn giao ca trực tiếp trên giao diện
('GC004', 'NV003', 'CA01', 1500000, 1500000, 1500000, 0, 0, 0, 0, 
 N'Ca trực hiện tại đang phục vụ khách hàng', 
 NULL, 'DANG_MO', NULL, NULL);
GO

-- 5. Khách hàng thành viên (Đầy đủ các hạng thẻ: Kim Cương, Vàng, Bạc, Mới, có Email & Địa chỉ)
INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, diemTichLuy, email, diaChi) VALUES
('KH001', N'Nguyễn Văn An',  '0988111222', 125, 'khachhang.an@gmail.com',  N'12 Nguyễn Văn Bảo, P.4, Q. Gò Vấp, TP.HCM'), -- VIP Kim Cương (>= 100 điểm)
('KH002', N'Trần Thị Mai',   '0977333444', 68,  'mai.tran@gmail.com',       N'45 Lê Lợi, P.3, Q. Gò Vấp, TP.HCM'),        -- Hạng Vàng (50 - 99 điểm)
('KH003', N'Lê Hoàng Yến',   '0912345678', 35,  'hoangyen.pet@gmail.com',   N'78 Quang Trung, P.10, Q. Gò Vấp, TP.HCM'),  -- Hạng Bạc (20 - 49 điểm)
('KH004', N'Đặng Quốc Bảo',  '0933555777', 10,  'quocbao.dang@gmail.com',   N'15 Phan Văn Trị, P.7, Q. Gò Vấp, TP.HCM'),  -- Khách hàng Mới (< 20 điểm)
('KH005', N'Khách vãng lai', '0900000000', 0,   'hotro.miopet@gmail.com',   N'Khách mua trực tiếp tại quầy');             -- Khách vãng lai
GO

-- 6. Danh mục Thú cưng & Hồ sơ Microchip
INSERT INTO ThuCung (maThuCung, tenThuCung, giongLoai, ngaySinh, gioiTinh) VALUES
('TC001', N'Miu Miu',    N'Mèo Anh Lông Ngắn',            '2023-05-10', N'Cái'),
('TC002', N'Lu Lu',      N'Chó Poodle Tiny',              '2022-11-20', N'Đực'),
('TC003', N'Bông',       N'Chó Pomeranian (Phốc Sóc)',    '2024-02-14', N'Cái'),
('TC004', N'Bim Bim',    N'Chó Corgi Chân Ngắn',          '2023-08-05', N'Đực'),
('TC005', N'Golden Sam', N'Chó Golden Retriever',         '2022-04-12', N'Đực');
GO

INSERT INTO HoSoThuCung (maHoSo, maKhachHang, maThuCung, soMicrochip, ngayDangKy, trangThaiNuoi) VALUES
('HS001', 'KH001', 'TC001', 'MC-982001-VN', '2023-06-01', N'Đang nuôi'),
('HS002', 'KH002', 'TC002', 'MC-982002-VN', '2023-01-15', N'Đang nuôi'),
('HS003', 'KH003', 'TC003', 'MC-982003-VN', '2024-03-01', N'Đang nuôi'),
('HS004', 'KH001', 'TC004', 'MC-982004-VN', '2023-09-10', N'Đang nuôi'),
('HS005', 'KH004', 'TC005', 'MC-982005-VN', '2022-05-01', N'Đang nuôi');
GO

-- 7. Sổ theo dõi thể trạng y tế & Tiêm chủng (Có trường hợp tốt, có trường hợp QUÁ HẠN TIÊM PHÒNG để demo nút gửi email)
INSERT INTO HoSoSucKhoe (maSucKhoe, maThuCung, maNhanVienKiemTra, canNang, nhietDo, tinhTrangVeRan, tiemPhongDai, ghiChuTheTrang, ngayKiemTra) VALUES
-- Trường hợp 1: Thể trạng hoàn hảo, đủ điều kiện làm spa
('SK001', 'TC001', 'NV004', 4.20, 38.50, N'Không có ve rận, da sạch', 1, 
 N'Thể trạng tốt, lông mượt, phản xạ lanh lợi', DATEADD(day, -5, GETDATE())),
-- Trường hợp 2: BÉ CHƯA TIÊM PHÒNG DẠI + CÓ VE RẬN -> Để kiểm thử cảnh báo y tế và gửi email nhắc tiêm kèm voucher
('SK002', 'TC002', 'NV004', 5.80, 38.70, N'Có ve rận nhẹ ở kẽ tai', 0, 
 N'QUÁ HẠN TIÊM DẠI - Đã tư vấn chủ tiêm bổ sung và tắm thảo dược trị ve', DATEADD(day, -2, GETDATE())),
-- Trường hợp 3: Thể trạng chó con nhỏ cần nhiệt độ ấm
('SK003', 'TC003', 'NV004', 2.40, 38.40, N'Sạch sẽ, da hồng hào', 1, 
 N'Bé nhỏ hiếu động, sấy ấm mức vừa phải', DATEADD(day, -1, GETDATE())),
-- Trường hợp 4: Cân nặng lớn (28kg)
('SK004', 'TC005', 'NV004', 28.00, 38.60, N'Sạch sẽ', 1, 
 N'Chó lớn ngoan ngoãn, lông dày cần chải kỹ lớp lót', DATEADD(day, -10, GETDATE()));
GO

-- 8. Danh mục Dịch vụ chăm sóc & Spa làm đẹp
INSERT INTO DichVu (maDichVu, tenDichVu, giaDichVu, thoiLuongPhut) VALUES
('DV001', N'Cắt Tỉa Lông Tạo Kiểu Thẩm Mỹ',    300000, 90),
('DV002', N'Cắt Mài Móng & Vệ Sinh Tai',       100000, 30),
('DV003', N'Vắt Tuyến Hôi & Cạo Bàn Chân',      90000, 30),
('DV004', N'Tắm Sấy Khử Mùi & Dưỡng Lông Lụa', 150000, 45),
('DV005', N'Tắm Thảo Dược & Trị Ve Rận Bọ Chét',220000, 60),
('DV006', N'Ngâm Bồn Sục Ozone Thư Giãn',     250000, 60),
('DV007', N'Nhuộm Lông Nghệ Thuật (Tai & Đuôi)', 180000, 60),
('DV008', N'Lưu Trú Khách Sạn Pet Hotel VIP',  250000, 1440);
GO

-- 9. Lịch hẹn dịch vụ Spa (Đầy đủ các trạng thái: Đã đặt, Đang phục vụ, Quá giờ, Hoàn thành, Đã hủy)
INSERT INTO LichHen (maLichHen, maKhachHang, maNhanVienTiepNhan, ngayHen, trangThaiLichHen, ghiChu) VALUES
-- Trạng thái: Đã đặt (Chờ tiếp nhận) -> Có nút gửi email nhắc lịch
('LH001', 'KH001', 'NV004', DATEADD(hour, 16, CAST(CAST(GETDATE() AS DATE) AS DATETIME)), N'Đã đặt', N'Cắt tỉa gọn vùng mặt và chân cho bé Miu Miu'),
-- Trạng thái: Đang phục vụ đúng tiến độ (Kỹ thuật viên đang làm, mới 40p / 90p)
('LH002', 'KH002', 'NV004', DATEADD(minute, -40, GETDATE()), N'Đang phục vụ', N'Đang sấy tạo kiểu lông xù cho bé Lu Lu'),
-- Trạng thái: Hoàn thành (Sẵn sàng bấm Chuyển sang POS thanh toán)
('LH003', 'KH003', 'NV004', DATEADD(hour, 9, CAST(CAST(GETDATE() AS DATE) AS DATETIME)), N'Hoàn thành', N'Đã hoàn thành tắm thảo dược khử mùi'),
-- Trạng thái: Đã hủy (Khách bận việc gia đình)
('LH004', 'KH004', 'NV004', DATEADD(day, -1, GETDATE()), N'Hủy', N'Khách dời lịch sang tuần sau'),
-- Trạng thái: Đang phục vụ nhưng QUÁ GIỜ (đã 95p so với thời lượng chuẩn 60p của DV005) -> Kiểm thử bộ lọc ⚠️ Quá giờ
('LH005', 'KH001', 'NV004', DATEADD(minute, -95, GETDATE()), N'Đang phục vụ', N'Gỡ rối lông dày và tắm thảo dược trị ve cho bé Bim Bim');
GO

INSERT INTO ChiTietLichHen (maChiTietLichHen, maLichHen, maThuCung, maDichVu, maKtvChiDinh) VALUES
('CTLH01', 'LH001', 'TC001', 'DV001', 'NV004'),
('CTLH02', 'LH002', 'TC002', 'DV001', 'NV005'),
('CTLH03', 'LH003', 'TC003', 'DV005', 'NV004'),
('CTLH04', 'LH004', 'TC005', 'DV004', 'NV005'),
('CTLH05', 'LH005', 'TC004', 'DV005', 'NV004');
GO

-- 10. Danh mục hàng hóa & Sản phẩm bán lẻ
INSERT INTO DanhMucHang (maDanhMuc, tenDanhMuc, moTa) VALUES
('DM01', N'Thức ăn dinh dưỡng', N'Hạt khô, pate thịt tươi, súp thưởng thú cưng'),
('DM02', N'Phụ kiện & Đồ chơi',  N'Vòng cổ, dây dắt, chuồng lồng, đồ cào móng'),
('DM03', N'Vệ sinh & Chăm sóc', N'Cát vệ sinh, sữa tắm trị ve, xịt khử mùi hôi');
GO

INSERT INTO SanPham (maSanPham, maDanhMuc, tenSanPham, maVach, donViCoSo, giaBanCoSo, moTa) VALUES
('SP001', 'DM01', N'Hạt Royal Canin Kitten 2kg',       '893600000001', N'Gói',    380000, N'Thức ăn hạt dinh dưỡng cho mèo con dưới 12 tháng'),
('SP002', 'DM01', N'Pate Whiskas Vị Cá Thu 85g',       '893600000002', N'Lon',     20000, N'Pate túi thơm ngon bổ sung taurine và omega'),
('SP003', 'DM01', N'Thuốc Nhỏ Gáy Trị Ve Frontline',   '893600000003', N'Tuýp',    75000, N'Tuýp nhỏ gáy đặc trị ve rận bọ chét cho chó mèo'),
('SP004', 'DM02', N'Vòng Cổ Chuông Nơ Thú Cưng',       '893600000004', N'Chiếc',   45000, N'Vòng cổ thời trang vải nhung chuông đồng chống lạc'),
('SP005', 'DM03', N'Sữa Tắm Dưỡng Lông Bio-Care 500ml', '893600000005', N'Chai',    85000, N'Sữa tắm thảo mộc dưỡng lông mượt không cay mắt'),
('SP006', 'DM03', N'Cát Vệ Sinh Mèo Hương Táo 5kg',    '893600000006', N'Túi 5kg', 65000, N'Cát đất sét bentonite vón cục nhanh khử mùi tốt');
GO

-- 11. Đơn vị tính & Bảng Quy đổi ĐVT đa cấp
INSERT INTO DonViTinh (maDonVi, tenDonVi) VALUES
('DVT01', N'Gói'),
('DVT02', N'Lon'),
('DVT03', N'Lốc 6 lon'),
('DVT04', N'Thùng 24 lon'),
('DVT05', N'Chiếc'),
('DVT06', N'Chai'),
('DVT07', N'Túi 5kg'),
('DVT08', N'Tuýp'),
('DVT09', N'Hộp 3 tuýp');
GO

INSERT INTO QuyDoiDonVi (maQuyDoi, maSanPham, maDonVi, tyLeQuyDoi, laDonViCoSo, giaBan) VALUES
-- SP001: Hạt Royal Canin Kitten 2kg
('QD001', 'SP001', 'DVT01', 1,  1, 380000), -- Đơn vị cơ sở: Gói
-- SP002: Pate Whiskas (Đa cấp: Lon lẻ -> Lốc 6 lon -> Thùng 24 lon)
('QD002', 'SP002', 'DVT02', 1,  1, 20000),  -- Đơn vị cơ sở: Lon lẻ (20k)
('QD003', 'SP002', 'DVT03', 6,  0, 115000), -- Lốc 6 lon (tiết kiệm 5k)
('QD004', 'SP002', 'DVT04', 24, 0, 450000), -- Thùng 24 lon (tiết kiệm 30k)
-- SP003: Thuốc nhỏ gáy Frontline (Tuýp lẻ -> Hộp 3 tuýp)
('QD005', 'SP003', 'DVT08', 1,  1, 75000),  -- Đơn vị cơ sở: Tuýp (75k)
('QD006', 'SP003', 'DVT09', 3,  0, 210000), -- Hộp 3 tuýp (tiết kiệm 15k)
-- SP004: Vòng cổ
('QD007', 'SP004', 'DVT05', 1,  1, 45000),
-- SP005: Sữa tắm Bio-Care
('QD008', 'SP005', 'DVT06', 1,  1, 85000),
-- SP006: Cát vệ sinh mèo
('QD009', 'SP006', 'DVT07', 1,  1, 65000);
GO

-- 12. Nhà cung cấp & Lô hàng (Kiểm thử FEFO: Lô còn hạn dài, CẬN HẠN <= 30 ngày, và HẾT HẠN)
INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, diaChi, soDienThoai) VALUES
('NCC01', N'Công ty Cổ phần Thức Ăn Thú Cưng Việt Nam', N'123 CMT8, Q.10, TP.HCM',   '0283999888'),
('NCC02', N'Pet Accessories & Pharmacy Sài Gòn',        N'456 Lê Văn Sỹ, Q.3, TP.HCM', '0909888777');
GO

INSERT INTO LoHang (soLo, maSanPham, maNhaCungCap, ngaySanXuat, hanSuDung, ngayNhap, donGiaNhapCoSo, soLuongNhap, soLuongTon) VALUES
-- Lô 1: Còn hạn dài, tồn kho dồi dào
('LO_RC_2026_01', 'SP001', 'NCC01', '2026-01-10', '2027-01-10', DATEADD(day, -45, GETDATE()), 280000, 50, 42),
-- Lô 2: CẬN HẠN (Hết hạn trong vòng 20 ngày) -> Báo động đỏ trên bảng Kho và xuất trước theo FEFO!
('LO_WS_CANHAN',  'SP002', 'NCC01', '2025-06-01', DATEADD(day, 20, GETDATE()), DATEADD(day, -60, GETDATE()), 14000, 100, 15),
-- Lô 3: Còn hạn dài (xuất sau lô cận hạn theo thuật toán FEFO)
('LO_WS_DAIHAN',  'SP002', 'NCC01', '2026-02-01', '2027-08-01', DATEADD(day, -10, GETDATE()), 14000, 200, 190),
-- Lô 4: DƯỚI ĐỊNH MỨC AN TOÀN (ROP - Tồn chỉ còn 4 tuýp trong khi ROP là 10) -> Để test Báo cáo Đề xuất nhập hàng tự động
('LO_FL_SAP_HET', 'SP003', 'NCC02', '2026-01-01', '2027-12-31', DATEADD(day, -30, GETDATE()), 50000, 30, 4),
-- Lô 5: Phụ kiện vòng cổ (Hạn 3 năm)
('LO_PK_2026_01', 'SP004', 'NCC02', '2026-01-01', '2029-01-01', DATEADD(day, -25, GETDATE()), 25000, 100, 75),
-- Lô 6: Sữa tắm Bio-Care
('LO_BC_2026_01', 'SP005', 'NCC01', '2026-02-15', '2028-02-15', DATEADD(day, -15, GETDATE()), 55000, 50, 38),
-- Lô 7: Cát vệ sinh
('LO_CAT_2026_01','SP006', 'NCC01', '2026-03-01', '2028-03-01', DATEADD(day, -5, GETDATE()),  42000, 100, 65);
GO

-- 13. Hóa đơn bán hàng POS & 7 Báo biểu thống kê (Có Tiền mặt, VietQR, Đổi điểm thưởng VIP, Dịch vụ Spa)
-- Hóa đơn 1: Hôm nay, thanh toán VietQR Napas, có giảm giá điểm VIP 20k
INSERT INTO HoaDon (maHoaDon, maKhachHang, maNhanVien, ngayLap, thoiGianThanhToan, giamGia, thanhTien, phuongThucThanhToan, trangThaiHoaDon)
VALUES ('HD261103-01', 'KH001', 'NV003', DATEADD(hour, 9, CAST(CAST(GETDATE() AS DATE) AS DATETIME)), DATEADD(hour, 9, CAST(CAST(GETDATE() AS DATE) AS DATETIME)), 20000, 475000, N'Chuyển khoản QR', N'Đã thanh toán');
INSERT INTO ChiTietHoaDon (maChiTiet, maHoaDon, loaiChiTiet, maSanPham, maDichVu, soLuong, donGiaLucBan, thanhTien) VALUES
('HD261103-01_CT1', 'HD261103-01', 'SAN_PHAM', 'SP001', NULL, 1, 380000, 380000), -- 1 gói hạt Royal Canin
('HD261103-01_CT2', 'HD261103-01', 'SAN_PHAM', 'SP002', NULL, 1, 115000, 115000); -- 1 lốc 6 lon pate Whiskas
INSERT INTO ChiTietXuatLo (maChiTiet, soLo, maQuyDoi, soLuongXuatCoSo) VALUES
('HD261103-01_CT1', 'LO_RC_2026_01', 'QD001', 1),
('HD261103-01_CT2', 'LO_WS_CANHAN',  'QD003', 6); -- Xuất lô cận hạn trước theo đúng FEFO
-- Hóa đơn 2: Hôm nay, thanh toán Tiền mặt dịch vụ Spa Cắt tỉa (KTV Ngọc nhận 10% hoa hồng = 30k)
INSERT INTO HoaDon (maHoaDon, maKhachHang, maNhanVien, ngayLap, thoiGianThanhToan, giamGia, thanhTien, phuongThucThanhToan, trangThaiHoaDon)
VALUES ('HD261103-02', 'KH002', 'NV003', DATEADD(hour, 10, CAST(CAST(GETDATE() AS DATE) AS DATETIME)), DATEADD(hour, 10, CAST(CAST(GETDATE() AS DATE) AS DATETIME)), 0, 300000, N'Tiền mặt', N'Đã thanh toán');
INSERT INTO ChiTietHoaDon (maChiTiet, maHoaDon, loaiChiTiet, maSanPham, maDichVu, soLuong, donGiaLucBan, thanhTien) VALUES
('HD261103-02_CT1', 'HD261103-02', 'DICH_VU', NULL, 'DV001', 1, 300000, 300000);
INSERT INTO ChiTietDichVu (maChiTiet, maDichVu, maThuCung, maKtvThucHien, tienHoaHong) VALUES
('HD261103-02_CT1', 'DV001', 'TC002', 'NV004', 30000);
-- Hóa đơn 3: Hôm qua, kết hợp vừa mua sữa tắm vừa làm dịch vụ ngâm sục Ozone
INSERT INTO HoaDon (maHoaDon, maKhachHang, maNhanVien, ngayLap, thoiGianThanhToan, giamGia, thanhTien, phuongThucThanhToan, trangThaiHoaDon)
VALUES ('HD261102-01', 'KH003', 'NV003', DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()), 0, 335000, N'Chuyển khoản QR', N'Đã thanh toán');
INSERT INTO ChiTietHoaDon (maChiTiet, maHoaDon, loaiChiTiet, maSanPham, maDichVu, soLuong, donGiaLucBan, thanhTien) VALUES
('HD261102-01_CT1', 'HD261102-01', 'SAN_PHAM', 'SP005', NULL,    1, 85000, 85000),
('HD261102-01_CT2', 'HD261102-01', 'DICH_VU',  NULL,    'DV006', 1, 250000, 250000);
INSERT INTO ChiTietXuatLo (maChiTiet, soLo, maQuyDoi, soLuongXuatCoSo) VALUES
('HD261102-01_CT1', 'LO_BC_2026_01', 'QD008', 1);
INSERT INTO ChiTietDichVu (maChiTiet, maDichVu, maThuCung, maKtvThucHien, tienHoaHong) VALUES
('HD261102-01_CT2', 'DV006', 'TC003', 'NV005', 25000);
-- Hóa đơn 4: Cách đây 3 ngày, hóa đơn mua cát và đồ chơi phụ kiện
INSERT INTO HoaDon (maHoaDon, maKhachHang, maNhanVien, ngayLap, thoiGianThanhToan, giamGia, thanhTien, phuongThucThanhToan, trangThaiHoaDon)
VALUES ('HD261031-01', 'KH004', 'NV003', DATEADD(day, -3, GETDATE()), DATEADD(day, -3, GETDATE()), 0, 110000, N'Tiền mặt', N'Đã thanh toán');
INSERT INTO ChiTietHoaDon (maChiTiet, maHoaDon, loaiChiTiet, maSanPham, maDichVu, soLuong, donGiaLucBan, thanhTien) VALUES
('HD261031-01_CT1', 'HD261031-01', 'SAN_PHAM', 'SP006', NULL, 1, 65000, 65000),
('HD261031-01_CT2', 'HD261031-01', 'SAN_PHAM', 'SP004', NULL, 1, 45000, 45000);
INSERT INTO ChiTietXuatLo (maChiTiet, soLo, maQuyDoi, soLuongXuatCoSo) VALUES
('HD261031-01_CT1', 'LO_CAT_2026_01', 'QD009', 1),
('HD261031-01_CT2', 'LO_PK_2026_01',  'QD007', 1);
GO

-- 14. Phiếu đổi trả hàng mẫu (Khách đổi trả 1 lon pate bị móp vỏ, hoàn kho lại nguyên vẹn)
INSERT INTO PhieuDoiTra (maPhieuDoiTra, maHoaDonGoc, maNhanVienTiepNhan, ngayDoiTra, tongTienHoan, lyDoDoiTra)
VALUES ('DT001', 'HD261103-01', 'NV003', DATEADD(minute, -30, GETDATE()), 20000, N'Khách mua nhầm vị cá thu, đổi sang pate cá hồi');
INSERT INTO ChiTietDoiTra (maChiTietDoiTra, maPhieuDoiTra, maSanPham, soLuongDoiTra, tienHoanLai, soLuong, donGiaHoan, lyDoChiTiet)
VALUES ('CTDT001', 'DT001', 'SP002', 1, 20000, 1, 20000, N'Hàng nguyên vẹn, còn nguyên tem hạn sử dụng');
GO

-- 15. Khách sạn lưu trú thú cưng (Pet Hotel - 12 Phòng đủ 3 phân hạng và 3 trạng thái kiểm thử)
INSERT INTO PhongLuuTru (maPhong, loaiPhong, donGiaNgay, trangThai, tenThuCung, giongLoai, tenChu, sdtChu, thoiGianCheckIn, duKienCheckOut, ghiChuChamSoc, daChoAnSang, daChoAnToi, daDiDaoVeSinh, phiDichVuPhatSinh) VALUES
-- Phân hạng 1: Phòng Tiêu chuẩn (150.000đ/ngày)
('P101', N'Phòng Tiêu chuẩn', 150000, N'ĐANG CÓ KHÁCH', N'Bim Bim', N'Corgi Chân Ngắn (9.5kg)', N'Nguyễn Văn An', '0988111222', 
 DATEADD(day, -2, GETDATE()), DATEADD(day, 1, GETDATE()), N'Ăn hạt Royal Canin ngâm mềm, dắt đi dạo 2 lần/ngày', 1, 1, 1, 0),
('P102', N'Phòng Tiêu chuẩn', 150000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0),
('P103', N'Phòng Tiêu chuẩn', 150000, N'ĐANG CÓ KHÁCH', N'Bơ', N'Poodle Nâu Đỏ (3.5kg)', N'Trần Thị Mai', '0977333444', 
 DATEADD(day, -1, GETDATE()), DATEADD(day, 2, GETDATE()), N'Ăn kèm pate gà, chải lông mỗi chiều, có phụ thu nhỏ gáy ve', 1, 0, 1, 75000),
('P104', N'Phòng Tiêu chuẩn', 150000, N'ĐANG DỌN DẸP', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0),
-- Phân hạng 2: Phòng VIP Suite (250.000đ/ngày)
('V201', N'Phòng VIP Suite', 250000, N'ĐANG CÓ KHÁCH', N'Golden Sam', N'Golden Retriever (28kg)', N'Đặng Quốc Bảo', '0933555777', 
 DATEADD(day, -3, GETDATE()), DATEADD(day, 1, GETDATE()), N'Camera VIP bật 24/7 cho chủ xem, ăn 3 bữa thịt bò tươi', 1, 1, 1, 100000),
('V202', N'Phòng VIP Suite', 250000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0),
('V203', N'Phòng VIP Suite', 250000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0),
('V204', N'Phòng VIP Suite', 250000, N'ĐANG CÓ KHÁCH', N'Lu Lu', N'Poodle Tiny (5.8kg)', N'Trần Thị Mai', '0977333444', 
 DATEADD(hour, -10, GETDATE()), DATEADD(day, 3, GETDATE()), N'Bật điều hòa 24 độ C liên tục, sấy ấm đệm ngủ', 1, 0, 1, 0),
-- Phân hạng 3: Condo cho Mèo (200.000đ/ngày)
('C301', N'Condo cho Mèo', 200000, N'ĐANG CÓ KHÁCH', N'Miu Miu', N'Mèo Anh Lông Ngắn (4.2kg)', N'Lê Hoàng Yến', '0912345678', 
 DATEADD(day, -1, GETDATE()), DATEADD(day, 2, GETDATE()), N'Ăn hạt Orijen + súp thưởng Ciao lúc 15h chiều', 1, 0, 1, 30000),
('C302', N'Condo cho Mèo', 200000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0),
('C303', N'Condo cho Mèo', 200000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0),
('C304', N'Condo cho Mèo', 200000, N'TRỐNG', '', '', '', '', NULL, NULL, '', 0, 0, 0, 0);
GO

-- 26. Thẻ Spa Pass mẫu kiểm thử quầy POS
INSERT INTO TheSpaPass (maThe, sdtKhachHang, tenKhachHang, tenGoi, tongSoBuoi, soBuoiDaDung, donGiaGoi, ngayKichHoat, ngayHetHan, trangThai) VALUES
('PASS_0988111222_01', '0988111222', N'Nguyễn Văn An', N'Gói Spa Chăm Sóc Thư Giãn (5 Buổi)', 5, 2, 900000, DATEADD(day, -20, CAST(GETDATE() AS DATE)), DATEADD(day, 70, CAST(GETDATE() AS DATE)), N'ĐANG HOẠT ĐỘNG'),
('PASS_0977333444_01', '0977333444', N'Trần Thị Mai', N'Gói Spa Toàn Diện & Cắt Tỉa (10 Buổi)', 10, 8, 1600000, DATEADD(day, -45, CAST(GETDATE() AS DATE)), DATEADD(day, 45, CAST(GETDATE() AS DATE)), N'ĐANG HOẠT ĐỘNG');
GO

-- ======================================================================================
-- KIỂM TRA TÍNH TOÀN VẸN VÀ THỐNG KÊ DỮ LIỆU ĐÃ NẠP
-- ======================================================================================
PRINT N'==================================================================';
PRINT N'    HỆ THỐNG CƠ SỞ DỮ LIỆU QUẢN LÝ CỬA HÀNG THÚ CƯNG MIO (IUH)    ';
PRINT N'       KHỞI TẠO VÀ NẠP DỮ LIỆU MẪU MỌI KỊCH BẢN THÀNH CÔNG!       ';
PRINT N'==================================================================';
SELECT 'TaiKhoan' AS TenBang, COUNT(*) AS SoLuongBanGhi FROM TaiKhoan
UNION ALL
SELECT 'NhanVien', COUNT(*) FROM NhanVien
UNION ALL
SELECT 'CaLamViec', COUNT(*) FROM CaLamViec
UNION ALL
SELECT 'BangPhanCong', COUNT(*) FROM BangPhanCong
UNION ALL
SELECT 'BangGiaoCa', COUNT(*) FROM BangGiaoCa
UNION ALL
SELECT 'KhachHang', COUNT(*) FROM KhachHang
UNION ALL
SELECT 'ThuCung', COUNT(*) FROM ThuCung
UNION ALL
SELECT 'HoSoThuCung', COUNT(*) FROM HoSoThuCung
UNION ALL
SELECT 'HoSoSucKhoe', COUNT(*) FROM HoSoSucKhoe
UNION ALL
SELECT 'DichVu', COUNT(*) FROM DichVu
UNION ALL
SELECT 'LichHen', COUNT(*) FROM LichHen
UNION ALL
SELECT 'ChiTietLichHen', COUNT(*) FROM ChiTietLichHen
UNION ALL
SELECT 'DanhMucHang', COUNT(*) FROM DanhMucHang
UNION ALL
SELECT 'SanPham', COUNT(*) FROM SanPham
UNION ALL
SELECT 'DonViTinh', COUNT(*) FROM DonViTinh
UNION ALL
SELECT 'QuyDoiDonVi', COUNT(*) FROM QuyDoiDonVi
UNION ALL
SELECT 'NhaCungCap', COUNT(*) FROM NhaCungCap
UNION ALL
SELECT 'LoHang', COUNT(*) FROM LoHang
UNION ALL
SELECT 'HoaDon', COUNT(*) FROM HoaDon
UNION ALL
SELECT 'ChiTietHoaDon', COUNT(*) FROM ChiTietHoaDon
UNION ALL
SELECT 'ChiTietXuatLo', COUNT(*) FROM ChiTietXuatLo
UNION ALL
SELECT 'ChiTietDichVu', COUNT(*) FROM ChiTietDichVu
UNION ALL
SELECT 'PhieuDoiTra', COUNT(*) FROM PhieuDoiTra
UNION ALL
SELECT 'ChiTietDoiTra', COUNT(*) FROM ChiTietDoiTra
UNION ALL
SELECT 'PhongLuuTru', COUNT(*) FROM PhongLuuTru
UNION ALL
SELECT 'TheSpaPass', COUNT(*) FROM TheSpaPass
UNION ALL
SELECT 'LichSuLuuTru', COUNT(*) FROM LichSuLuuTru;
GO

