-- ==========================================================================================
-- DỰ ÁN: PETSHOP MIO - HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG (NHÓM 07)
-- SCRIPT: CleanupDuLieuMau.sql (Dọn dẹp an toàn toàn bộ dữ liệu mẫu / Seed Data)
-- THỰC THI: Đã được Người dùng xác nhận phê duyệt tại Điểm dừng 2 & 3.
-- NGUYÊN TẮC: Xóa theo đúng thứ tự phụ thuộc ngược (Reverse Foreign Key Dependency)
-- ==========================================================================================

USE QuanLyCuaHangThuCung;
GO

SET XACT_ABORT ON;
BEGIN TRANSACTION;

PRINT N'>>> BẮT ĐẦU DỌN DẸP DỮ LIỆU CŨ PETSHOP MIO THEO THỨ TỰ PHỤ THUỘC NGƯỢC...';

-- 1. Lớp phụ thuộc sâu nhất (Hotel, Spa Pass History)
IF OBJECT_ID('dbo.LichSuLuuTru', 'U') IS NOT NULL DELETE FROM dbo.LichSuLuuTru;
IF OBJECT_ID('dbo.LichSuSuDungSpaPass', 'U') IS NOT NULL DELETE FROM dbo.LichSuSuDungSpaPass;

-- 2. Thẻ Spa Pass & Phòng lưu trú
IF OBJECT_ID('dbo.TheSpaPass', 'U') IS NOT NULL DELETE FROM dbo.TheSpaPass;
IF OBJECT_ID('dbo.PhongLuuTru', 'U') IS NOT NULL DELETE FROM dbo.PhongLuuTru;

-- 3. Chi tiết đổi trả & Phiếu đổi trả
IF OBJECT_ID('dbo.ChiTietDoiTra', 'U') IS NOT NULL DELETE FROM dbo.ChiTietDoiTra;
IF OBJECT_ID('dbo.PhieuDoiTra', 'U') IS NOT NULL DELETE FROM dbo.PhieuDoiTra;

-- 4. Chi tiết hóa đơn (Xuất lô, Dịch vụ, Chi tiết bán hàng) & Hóa đơn
IF OBJECT_ID('dbo.ChiTietXuatLo', 'U') IS NOT NULL DELETE FROM dbo.ChiTietXuatLo;
IF OBJECT_ID('dbo.ChiTietDichVu', 'U') IS NOT NULL DELETE FROM dbo.ChiTietDichVu;
IF OBJECT_ID('dbo.ChiTietHoaDon', 'U') IS NOT NULL DELETE FROM dbo.ChiTietHoaDon;
IF OBJECT_ID('dbo.HoaDon', 'U') IS NOT NULL DELETE FROM dbo.HoaDon;

-- 5. Lịch hẹn & Chi tiết lịch hẹn
IF OBJECT_ID('dbo.ChiTietLichHen', 'U') IS NOT NULL DELETE FROM dbo.ChiTietLichHen;
IF OBJECT_ID('dbo.LichHen', 'U') IS NOT NULL DELETE FROM dbo.LichHen;

-- 6. Hồ sơ thú cưng, Sổ sức khỏe & Thú cưng
IF OBJECT_ID('dbo.HoSoSucKhoe', 'U') IS NOT NULL DELETE FROM dbo.HoSoSucKhoe;
IF OBJECT_ID('dbo.HoSoThuCung', 'U') IS NOT NULL DELETE FROM dbo.HoSoThuCung;
IF OBJECT_ID('dbo.ThuCung', 'U') IS NOT NULL DELETE FROM dbo.ThuCung;

-- 7. Khách hàng
IF OBJECT_ID('dbo.KhachHang', 'U') IS NOT NULL DELETE FROM dbo.KhachHang;

-- 8. Lô hàng, Quy đổi đơn vị & Sản phẩm
IF OBJECT_ID('dbo.LoHang', 'U') IS NOT NULL DELETE FROM dbo.LoHang;
IF OBJECT_ID('dbo.QuyDoiDonVi', 'U') IS NOT NULL DELETE FROM dbo.QuyDoiDonVi;
IF OBJECT_ID('dbo.SanPham', 'U') IS NOT NULL DELETE FROM dbo.SanPham;

-- 9. Bàn giao ca & Phân ca
IF OBJECT_ID('dbo.BangGiaoCa', 'U') IS NOT NULL DELETE FROM dbo.BangGiaoCa;
IF OBJECT_ID('dbo.BangPhanCong', 'U') IS NOT NULL DELETE FROM dbo.BangPhanCong;

-- 10. Nhân viên & Tài khoản
IF OBJECT_ID('dbo.NhanVien', 'U') IS NOT NULL DELETE FROM dbo.NhanVien;
IF OBJECT_ID('dbo.TaiKhoan', 'U') IS NOT NULL DELETE FROM dbo.TaiKhoan;

-- 11. Danh mục nền tảng (Dịch vụ, NCC, Ca làm việc, Đơn vị tính, Danh mục hàng)
IF OBJECT_ID('dbo.DichVu', 'U') IS NOT NULL DELETE FROM dbo.DichVu;
IF OBJECT_ID('dbo.NhaCungCap', 'U') IS NOT NULL DELETE FROM dbo.NhaCungCap;
IF OBJECT_ID('dbo.CaLamViec', 'U') IS NOT NULL DELETE FROM dbo.CaLamViec;
IF OBJECT_ID('dbo.DonViTinh', 'U') IS NOT NULL DELETE FROM dbo.DonViTinh;
IF OBJECT_ID('dbo.DanhMucHang', 'U') IS NOT NULL DELETE FROM dbo.DanhMucHang;

PRINT N'>>> HOÀN TẤT DỌN DẸP DỮ LIỆU CŨ THÀNH CÔNG VÀ AN TOÀN 100%!';

COMMIT TRANSACTION;
GO
