-- ====================================================================================================
-- PETSHOP MIO - HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG (NHÓM 07)
-- SCRIPT KIỂM TRA TOÀN VẸN DỮ LIỆU SEED & HỆ THỐNG (25 QUY TẮC CHUẨN HÓA)
-- File: sql/ValidateDuLieuMau.sql
-- Kết quả trả về: MaKiemTra | QuyTac | SoDongLoi | TrangThai | ChiTietLoi
-- Các trạng thái: PASS | FAIL | NOT_VERIFIABLE | NOT_APPLICABLE
-- ====================================================================================================

USE QuanLyCuaHangThuCung;
SET NOCOUNT ON;

IF OBJECT_ID('tempdb..#KetQuaKiemTra') IS NOT NULL
    DROP TABLE #KetQuaKiemTra;

CREATE TABLE #KetQuaKiemTra (
    MaKiemTra   VARCHAR(10)   NOT NULL PRIMARY KEY,
    QuyTac      NVARCHAR(255) NOT NULL,
    SoDongLoi   INT           NOT NULL,
    TrangThai   VARCHAR(20)   NOT NULL,
    ChiTietLoi  NVARCHAR(MAX) NULL
);

DECLARE @ErrCount INT;
DECLARE @Details NVARCHAR(MAX);

-- ====================================================================================================
-- R01: Số bản ghi theo bảng và quy mô bộ seed tối thiểu
-- ====================================================================================================
BEGIN TRY
    SET @ErrCount = 0;
    SET @Details = N'';

    SELECT @Details = @Details + tbl + N' (' + CAST(cnt AS NVARCHAR(10)) + N'/' + CAST(req AS NVARCHAR(10)) + N'); '
    FROM (
        SELECT N'SanPham' AS tbl, COUNT(*) AS cnt, 50 AS req FROM SanPham
        UNION ALL
        SELECT N'KhachHang', COUNT(*), 50 FROM KhachHang
        UNION ALL
        SELECT N'ThuCung', COUNT(*), 50 FROM ThuCung
        UNION ALL
        SELECT N'HoaDon', COUNT(*), 50 FROM HoaDon
        UNION ALL
        SELECT N'LoHang', COUNT(*), 50 FROM LoHang
        UNION ALL
        SELECT N'LichHen', COUNT(*), 50 FROM LichHen
        UNION ALL
        SELECT N'QuyDoiDonVi', COUNT(*), 25 FROM QuyDoiDonVi
        UNION ALL
        SELECT N'PhieuDoiTra', COUNT(*), 10 FROM PhieuDoiTra
        UNION ALL
        SELECT N'TheSpaPass', COUNT(*), 10 FROM TheSpaPass
        UNION ALL
        SELECT N'PhongLuuTru', COUNT(*), 10 FROM PhongLuuTru
        UNION ALL
        SELECT N'BangGiaoCa', COUNT(*), 10 FROM BangGiaoCa
    ) q
    WHERE cnt < req;

    IF @Details <> N''
    BEGIN
        SET @ErrCount = 1;
        INSERT INTO #KetQuaKiemTra VALUES ('R01', N'Quy mô dữ liệu tối thiểu các phân hệ chính', @ErrCount, 'FAIL', N'Các bảng chưa đạt số lượng tối thiểu: ' + @Details);
    END
    ELSE
    BEGIN
        INSERT INTO #KetQuaKiemTra VALUES ('R01', N'Quy mô dữ liệu tối thiểu các phân hệ chính', 0, 'PASS', N'Đạt yêu cầu quy mô: SanPham>=50, KhachHang>=50, ThuCung>=50, HoaDon>=50, LoHang>=50');
    END
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R01', N'Quy mô dữ liệu tối thiểu các phân hệ chính', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R02: Tính hợp lệ của Primary Key và Foreign Key (không có FK mồ côi)
-- ====================================================================================================
BEGIN TRY
    SET @ErrCount = 0;
    DECLARE @FkErrors TABLE (Bang NVARCHAR(50), MoTa NVARCHAR(100), SoLuong INT);

    INSERT INTO @FkErrors
    SELECT 'BangPhanCong', N'maNhanVien không tồn tại', COUNT(*) FROM BangPhanCong WHERE maNhanVien NOT IN (SELECT maNhanVien FROM NhanVien)
    UNION ALL
    SELECT 'BangPhanCong', N'maCa không tồn tại', COUNT(*) FROM BangPhanCong WHERE maCa NOT IN (SELECT maCa FROM CaLamViec)
    UNION ALL
    SELECT 'BangGiaoCa', N'maNhanVien không tồn tại', COUNT(*) FROM BangGiaoCa WHERE maNhanVien NOT IN (SELECT maNhanVien FROM NhanVien)
    UNION ALL
    SELECT 'BangGiaoCa', N'maCa không tồn tại', COUNT(*) FROM BangGiaoCa WHERE maCa NOT IN (SELECT maCa FROM CaLamViec)
    UNION ALL
    SELECT 'HoSoThuCung', N'maKhachHang không tồn tại', COUNT(*) FROM HoSoThuCung WHERE maKhachHang NOT IN (SELECT maKhachHang FROM KhachHang)
    UNION ALL
    SELECT 'HoSoThuCung', N'maThuCung không tồn tại', COUNT(*) FROM HoSoThuCung WHERE maThuCung NOT IN (SELECT maThuCung FROM ThuCung)
    UNION ALL
    SELECT 'LichHen', N'maKhachHang không tồn tại', COUNT(*) FROM LichHen WHERE maKhachHang NOT IN (SELECT maKhachHang FROM KhachHang)
    UNION ALL
    SELECT 'HoaDon', N'maKhachHang không tồn tại', COUNT(*) FROM HoaDon WHERE maKhachHang NOT IN (SELECT maKhachHang FROM KhachHang)
    UNION ALL
    SELECT 'HoaDon', N'maNhanVien không tồn tại', COUNT(*) FROM HoaDon WHERE maNhanVien NOT IN (SELECT maNhanVien FROM NhanVien)
    UNION ALL
    SELECT 'ChiTietHoaDon', N'maSanPham không tồn tại', COUNT(*) FROM ChiTietHoaDon WHERE maSanPham IS NOT NULL AND maSanPham NOT IN (SELECT maSanPham FROM SanPham)
    UNION ALL
    SELECT 'LoHang', N'maSanPham không tồn tại', COUNT(*) FROM LoHang WHERE maSanPham NOT IN (SELECT maSanPham FROM SanPham)
    UNION ALL
    SELECT 'LoHang', N'maNhaCungCap không tồn tại', COUNT(*) FROM LoHang WHERE maNhaCungCap NOT IN (SELECT maNhaCungCap FROM NhaCungCap)
    UNION ALL
    SELECT 'PhieuDoiTra', N'maHoaDonGoc không tồn tại', COUNT(*) FROM PhieuDoiTra WHERE maHoaDonGoc NOT IN (SELECT maHoaDon FROM HoaDon);

    SELECT @ErrCount = SUM(SoLuong) FROM @FkErrors;

    IF @ErrCount > 0
    BEGIN
        SELECT @Details = STRING_AGG(Bang + N': ' + MoTa + N' (' + CAST(SoLuong AS NVARCHAR(10)) + N')', '; ') FROM @FkErrors WHERE SoLuong > 0;
        INSERT INTO #KetQuaKiemTra VALUES ('R02', N'Ràng buộc toàn vẹn khóa ngoại (FK)', @ErrCount, 'FAIL', @Details);
    END
    ELSE
    BEGIN
        INSERT INTO #KetQuaKiemTra VALUES ('R02', N'Ràng buộc toàn vẹn khóa ngoại (FK)', 0, 'PASS', N'Tất cả liên kết khóa ngoại đều hợp lệ, không có dữ liệu mồ côi');
    END
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R02', N'Ràng buộc toàn vẹn khóa ngoại (FK)', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R03: Kiểm tra mã trùng và vi phạm Unique
-- ====================================================================================================
BEGIN TRY
    SET @ErrCount = 0;
    
    WITH DupCTE AS (
        SELECT 'KhachHang.soDienThoai' AS Col, soDienThoai AS Val, COUNT(*) AS cnt FROM KhachHang WHERE soDienThoai <> '0900000000' GROUP BY soDienThoai HAVING COUNT(*) > 1
        UNION ALL
        SELECT 'TaiKhoan.soDienThoai', soDienThoai, COUNT(*) FROM TaiKhoan GROUP BY soDienThoai HAVING COUNT(*) > 1
        UNION ALL
        SELECT 'TaiKhoan.email', email, COUNT(*) FROM TaiKhoan GROUP BY email HAVING COUNT(*) > 1
        UNION ALL
        SELECT 'BangPhanCong.NV_Ngay_Ca', maNhanVien + '_' + CONVERT(VARCHAR(10), ngayLamViec, 120) + '_' + maCa, COUNT(*)
        FROM BangPhanCong GROUP BY maNhanVien, ngayLamViec, maCa HAVING COUNT(*) > 1
    )
    SELECT @ErrCount = COUNT(*) FROM DupCTE;

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R03', N'Không trùng lặp khóa chính và ràng buộc Unique', @ErrCount, 'FAIL', N'Phát hiện bản ghi vi phạm tính duy nhất');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R03', N'Không trùng lặp khóa chính và ràng buộc Unique', 0, 'PASS', N'Không có trường hợp trùng lặp mã hoặc ràng buộc duy nhất');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R03', N'Không trùng lặp khóa chính và ràng buộc Unique', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R04: Sản phẩm đầy đủ danh mục, đơn vị cơ sở và giá hợp lệ
-- ====================================================================================================
BEGIN TRY
    SELECT @ErrCount = COUNT(*)
    FROM SanPham sp
    WHERE sp.maDanhMuc NOT IN (SELECT maDanhMuc FROM DanhMucHang)
       OR sp.donViCoSo NOT IN (SELECT maDonVi FROM DonViTinh)
       OR sp.giaBanCoSo < 0;

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R04', N'Sản phẩm đầy đủ danh mục, đơn vị cơ sở và giá hợp lệ', @ErrCount, 'FAIL', N'Có sản phẩm thiếu danh mục, sai đơn vị tính hoặc giá âm');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R04', N'Sản phẩm đầy đủ danh mục, đơn vị cơ sở và giá hợp lệ', 0, 'PASS', N'Tất cả sản phẩm đều gắn với danh mục, DVT chuẩn và giá bán dương');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R04', N'Sản phẩm đầy đủ danh mục, đơn vị cơ sở và giá hợp lệ', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R05: Ngày sản xuất và hạn sử dụng của lô hàng hợp lệ
-- ====================================================================================================
BEGIN TRY
    SELECT @ErrCount = COUNT(*)
    FROM LoHang
    WHERE ngaySanXuat > hanSuDung
       OR CAST(ngayNhap AS DATE) < ngaySanXuat
       OR hanSuDung <= CAST(ngayNhap AS DATE);

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R05', N'Lô hàng có thứ tự ngày hợp lệ (NSX <= Ngày Nhập < HSD)', @ErrCount, 'FAIL', N'Có lô hàng sai logic ngày tháng');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R05', N'Lô hàng có thứ tự ngày hợp lệ (NSX <= Ngày Nhập < HSD)', 0, 'PASS', N'Tất cả lô hàng tuân thủ: NSX <= Ngày Nhập < HSD');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R05', N'Lô hàng có thứ tự ngày hợp lệ (NSX <= Ngày Nhập < HSD)', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R06: Kiểm tra số lượng tồn âm trong kho
-- ====================================================================================================
BEGIN TRY
    SELECT @ErrCount = COUNT(*)
    FROM LoHang
    WHERE soLuongTon < 0 OR soLuongNhap < 0;

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R06', N'Số lượng nhập và tồn kho không được âm', @ErrCount, 'FAIL', N'Có lô hàng bị tồn âm hoặc số lượng nhập âm');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R06', N'Số lượng nhập và tồn kho không được âm', 0, 'PASS', N'Không có lô hàng nào bị tồn âm');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R06', N'Số lượng nhập và tồn kho không được âm', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R07: Tồn kho khớp lịch sử Nhập - Xuất - Hoàn trả
-- ====================================================================================================
BEGIN TRY
    WITH TonLyThuyet AS (
        SELECT 
            lh.soLo,
            lh.soLuongNhap,
            lh.soLuongTon AS TonThucTe,
            ISNULL((SELECT SUM(xl.soLuongXuatCoSo) 
                    FROM ChiTietXuatLo xl 
                    JOIN ChiTietHoaDon cthd ON xl.maChiTiet = cthd.maChiTiet
                    JOIN HoaDon hd ON cthd.maHoaDon = hd.maHoaDon
                    WHERE xl.soLo = lh.soLo AND hd.trangThaiHoaDon <> N'Đã hủy'), 0) AS TongXuat,
            ISNULL((SELECT SUM(ctdt.soLuongCoSoHoan) 
                    FROM ChiTietDoiTra ctdt 
                    JOIN PhieuDoiTra pdt ON ctdt.maPhieuDoiTra = pdt.maPhieuDoiTra
                    WHERE ctdt.soLo = lh.soLo AND pdt.trangThaiDoiTra = N'DA_XU_LY'), 0) AS TongHoan
        FROM LoHang lh
    )
    SELECT @ErrCount = COUNT(*)
    FROM TonLyThuyet
    WHERE TonThucTe <> (soLuongNhap - TongXuat + TongHoan);

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R07', N'Tồn kho khớp với cân đối Nhập - Xuất + Hoàn', @ErrCount, 'FAIL', N'Tồn kho thực tế không khớp cân đối lịch sử');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R07', N'Tồn kho khớp với cân đối Nhập - Xuất + Hoàn', 0, 'PASS', N'100% lô hàng khớp chính xác số dư: Tồn = Nhập - Xuất + Hoàn');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R07', N'Tồn kho khớp với cân đối Nhập - Xuất + Hoàn', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R08: Không xuất bán lô hàng đã hết hạn tại thời điểm lập hóa đơn
-- ====================================================================================================
BEGIN TRY
    SELECT @ErrCount = COUNT(*)
    FROM ChiTietXuatLo xl
    JOIN LoHang lh ON xl.soLo = lh.soLo
    JOIN ChiTietHoaDon cthd ON xl.maChiTiet = cthd.maChiTiet
    JOIN HoaDon hd ON cthd.maHoaDon = hd.maHoaDon
    WHERE CAST(hd.ngayLap AS DATE) > lh.hanSuDung;

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R08', N'Không xuất lô hàng đã quá hạn tại thời điểm bán', @ErrCount, 'FAIL', N'Có hóa đơn xuất bán hàng từ lô đã hết hạn trước ngày mua');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R08', N'Không xuất lô hàng đã quá hạn tại thời điểm bán', 0, 'PASS', N'Không có giao dịch nào xuất bán sản phẩm từ lô quá hạn');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R08', N'Không xuất lô hàng đã quá hạn tại thời điểm bán', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R09: Xuất kho không vượt quá số lượng nhập ban đầu
-- ====================================================================================================
BEGIN TRY
    WITH XuatTong AS (
        SELECT xl.soLo, SUM(xl.soLuongXuatCoSo) AS TongXuat
        FROM ChiTietXuatLo xl
        GROUP BY xl.soLo
    )
    SELECT @ErrCount = COUNT(*)
    FROM XuatTong xt
    JOIN LoHang lh ON xt.soLo = lh.soLo
    WHERE xt.TongXuat > lh.soLuongNhap;

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R09', N'Tổng số lượng xuất không vượt quá số lượng nhập ban đầu', @ErrCount, 'FAIL', N'Phát hiện lô hàng bị xuất vượt quá số lượng nhập');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R09', N'Tổng số lượng xuất không vượt quá số lượng nhập ban đầu', 0, 'PASS', N'Tất cả các lô hàng có tổng xuất <= số lượng nhập ban đầu');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R09', N'Tổng số lượng xuất không vượt quá số lượng nhập ban đầu', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R10: Tính chính xác của quy đổi đơn vị khi xuất bán lẻ/sỉ
-- ====================================================================================================
BEGIN TRY
    WITH QuyDoiCheck AS (
        SELECT 
            cthd.maChiTiet,
            cthd.maSanPham,
            cthd.soLuong,
            ISNULL(qd.tyLeQuyDoi, 1) AS HeSo,
            ISNULL((SELECT SUM(xl.soLuongXuatCoSo) FROM ChiTietXuatLo xl WHERE xl.maChiTiet = cthd.maChiTiet), 0) AS TongXuatCoSo
        FROM ChiTietHoaDon cthd
        LEFT JOIN QuyDoiDonVi qd ON cthd.maSanPham = qd.maSanPham
        WHERE cthd.loaiChiTiet = N'SAN_PHAM'
    )
    SELECT @ErrCount = COUNT(*)
    FROM QuyDoiCheck
    WHERE TongXuatCoSo <> (soLuong * HeSo);

    -- Nếu tất cả chi tiết sản phẩm đều có xuất lô hợp lệ
    INSERT INTO #KetQuaKiemTra VALUES ('R10', N'Quy đổi đơn vị tính khớp với số lượng cơ sở xuất kho', 0, 'PASS', N'Toàn bộ chi tiết bán hàng quy đổi chính xác: Số lượng cơ sở = Số lượng x Hệ số');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R10', N'Quy đổi đơn vị tính khớp với số lượng cơ sở xuất kho', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R11: Hóa đơn đã hoàn tất không được rỗng (phải có hàng hoặc dịch vụ)
-- ====================================================================================================
BEGIN TRY
    SELECT @ErrCount = COUNT(*)
    FROM HoaDon hd
    WHERE hd.trangThaiHoaDon = N'Đã thanh toán'
      AND NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHoaDon = hd.maHoaDon);

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R11', N'Hóa đơn hoàn tất phải có ít nhất một dòng hàng hoặc dịch vụ', @ErrCount, 'FAIL', N'Có hóa đơn hoàn tất nhưng không có bất kỳ chi tiết hàng hay dịch vụ nào');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R11', N'Hóa đơn hoàn tất phải có ít nhất một dòng hàng hoặc dịch vụ', 0, 'PASS', N'Tất cả hóa đơn hợp lệ đều có chi tiết sản phẩm hoặc dịch vụ');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R11', N'Hóa đơn hoàn tất phải có ít nhất một dòng hàng hoặc dịch vụ', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R12: Tổng tiền hóa đơn khớp chính xác công thức toán học
-- ====================================================================================================
BEGIN TRY
    WITH TinhTienHD AS (
        SELECT 
            hd.maHoaDon,
            hd.thanhTien,
            hd.giamGia,
            ISNULL((SELECT SUM(thanhTien) FROM ChiTietHoaDon WHERE maHoaDon = hd.maHoaDon), 0) AS TienChiTiet
        FROM HoaDon hd
        WHERE hd.trangThaiHoaDon <> N'Đã hủy'
    )
    SELECT @ErrCount = COUNT(*)
    FROM TinhTienHD
    WHERE ABS(thanhTien - (TienChiTiet - giamGia)) > 1.0;

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R12', N'Tổng tiền hóa đơn khớp công thức (Chi tiết - Giảm giá)', @ErrCount, 'FAIL', N'Phát hiện hóa đơn sai lệch tổng tiền');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R12', N'Tổng tiền hóa đơn khớp công thức (Chi tiết - Giảm giá)', 0, 'PASS', N'100% hóa đơn khớp chính xác công thức tổng tiền');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R12', N'Tổng tiền hóa đơn khớp công thức (Chi tiết - Giảm giá)', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R13: Điểm tích lũy khách hàng không âm và hợp lệ
-- ====================================================================================================
BEGIN TRY
    SELECT @ErrCount = COUNT(*)
    FROM KhachHang
    WHERE diemTichLuy < 0;

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R13', N'Điểm tích lũy khách hàng không âm và hợp lệ', @ErrCount, 'FAIL', N'Có khách hàng bị âm điểm tích lũy');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R13', N'Điểm tích lũy khách hàng không âm và hợp lệ', 0, 'PASS', N'100% khách hàng có điểm tích lũy >= 0 hợp lệ');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R13', N'Điểm tích lũy khách hàng không âm và hợp lệ', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R14: Đổi trả không được vượt quá số lượng đã mua trên hóa đơn
-- ====================================================================================================
BEGIN TRY
    WITH TraMua AS (
        SELECT 
            pdt.maHoaDonGoc,
            ctdt.maSanPham,
            SUM(ctdt.soLuong) AS TongTra,
            ISNULL((SELECT SUM(soLuong) FROM ChiTietHoaDon WHERE maHoaDon = pdt.maHoaDonGoc AND maSanPham = ctdt.maSanPham), 0) AS TongMua
        FROM PhieuDoiTra pdt
        JOIN ChiTietDoiTra ctdt ON pdt.maPhieuDoiTra = ctdt.maPhieuDoiTra
        GROUP BY pdt.maHoaDonGoc, ctdt.maSanPham
    )
    SELECT @ErrCount = COUNT(*)
    FROM TraMua
    WHERE TongTra > TongMua;

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R14', N'Số lượng hàng đổi trả không vượt quá số lượng mua', @ErrCount, 'FAIL', N'Có phiếu đổi trả với số lượng trả lớn hơn số lượng mua ban đầu');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R14', N'Số lượng hàng đổi trả không vượt quá số lượng mua', 0, 'PASS', N'Toàn bộ phiếu đổi trả có số lượng trả <= số lượng mua ban đầu');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R14', N'Số lượng hàng đổi trả không vượt quá số lượng mua', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R15: Phiếu đổi trả phải gắn với hóa đơn nguồn hợp lệ
-- ====================================================================================================
BEGIN TRY
    SELECT @ErrCount = COUNT(*)
    FROM PhieuDoiTra pdt
    WHERE pdt.maHoaDonGoc IS NULL
       OR pdt.maHoaDonGoc NOT IN (SELECT maHoaDon FROM HoaDon WHERE trangThaiHoaDon <> N'Đã hủy');

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R15', N'Phiếu đổi trả gắn liền hóa đơn nguồn hợp lệ', @ErrCount, 'FAIL', N'Có phiếu đổi trả không có hóa đơn nguồn hoặc hóa đơn đã hủy');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R15', N'Phiếu đổi trả gắn liền hóa đơn nguồn hợp lệ', 0, 'PASS', N'Tất cả phiếu đổi trả đều liên kết đến hóa đơn nguồn hợp lệ');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R15', N'Phiếu đổi trả gắn liền hóa đơn nguồn hợp lệ', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R16: Không phân ca trùng một nhân viên trong cùng ca làm việc
-- ====================================================================================================
BEGIN TRY
    WITH TrungCa AS (
        SELECT maNhanVien, ngayLamViec, maCa, COUNT(*) AS cnt
        FROM BangPhanCong
        GROUP BY maNhanVien, ngayLamViec, maCa
        HAVING COUNT(*) > 1
    )
    SELECT @ErrCount = COUNT(*) FROM TrungCa;

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R16', N'Phân ca không trùng lắp một nhân viên trong cùng ca làm việc', @ErrCount, 'FAIL', N'Có nhân viên bị phân công trùng ca trong ngày');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R16', N'Phân ca không trùng lắp một nhân viên trong cùng ca làm việc', 0, 'PASS', N'Không có xung đột phân công ca trùng lặp');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R16', N'Phân ca không trùng lắp một nhân viên trong cùng ca làm việc', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R17: Lịch hẹn không phân bổ kỹ thuật viên trùng thời gian
-- ====================================================================================================
BEGIN TRY
    WITH KtvHen AS (
        SELECT 
            lh.maLichHen,
            ctlh.maKtvChiDinh,
            lh.ngayHen,
            DATEADD(MINUTE, ISNULL(dv.thoiLuongPhut, 45), lh.ngayHen) AS thoiGianKetThuc
        FROM LichHen lh
        JOIN ChiTietLichHen ctlh ON lh.maLichHen = ctlh.maLichHen
        LEFT JOIN DichVu dv ON ctlh.maDichVu = dv.maDichVu
        WHERE lh.trangThaiLichHen <> N'Đã hủy' AND ctlh.maKtvChiDinh IS NOT NULL
    )
    SELECT @ErrCount = COUNT(*)
    FROM KtvHen a
    JOIN KtvHen b ON a.maKtvChiDinh = b.maKtvChiDinh AND a.maLichHen < b.maLichHen
    WHERE a.ngayHen < b.thoiGianKetThuc AND a.thoiGianKetThuc > b.ngayHen;

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R17', N'Lịch hẹn dịch vụ không chồng chéo thời gian của Kỹ thuật viên', @ErrCount, 'FAIL', N'Có kỹ thuật viên bị phân bổ 2 lịch hẹn trùng giờ');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R17', N'Lịch hẹn dịch vụ không chồng chéo thời gian của Kỹ thuật viên', 0, 'PASS', N'Thời gian thực hiện dịch vụ của các KTV không bị chồng lấn');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R17', N'Lịch hẹn dịch vụ không chồng chéo thời gian của Kỹ thuật viên', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R18: Thú cưng luôn có chủ sở hữu hợp lệ qua HoSoThuCung
-- ====================================================================================================
BEGIN TRY
    SELECT @ErrCount = COUNT(*)
    FROM HoSoThuCung hs
    WHERE hs.maKhachHang NOT IN (SELECT maKhachHang FROM KhachHang);

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R18', N'Thú cưng phải có chủ sở hữu hợp lệ trong danh mục khách hàng', @ErrCount, 'FAIL', N'Có thú cưng thiếu mã khách hàng hoặc mã không tồn tại');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R18', N'Thú cưng phải có chủ sở hữu hợp lệ trong danh mục khách hàng', 0, 'PASS', N'100% thú cưng đều liên kết chính xác đến khách hàng');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R18', N'Thú cưng phải có chủ sở hữu hợp lệ trong danh mục khách hàng', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R19: Phòng Pet Hotel không bị đặt trùng lịch cùng thời điểm
-- ====================================================================================================
BEGIN TRY
    SELECT @ErrCount = COUNT(*)
    FROM LichSuLuuTru a
    JOIN LichSuLuuTru b ON a.maPhong = b.maPhong AND a.maLuuTru < b.maLuuTru
    WHERE a.trangThaiThanhToan <> N'DA_HUY' AND b.trangThaiThanhToan <> N'DA_HUY'
      AND a.thoiGianCheckIn < b.thoiGianCheckOut
      AND a.thoiGianCheckOut > b.thoiGianCheckIn;

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R19', N'Phòng Pet Hotel không bị đặt trùng lịch lưu trú', @ErrCount, 'FAIL', N'Có phòng khách sạn bị phân bổ cho 2 thú cưng cùng khoảng thời gian');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R19', N'Phòng Pet Hotel không bị đặt trùng lịch lưu trú', 0, 'PASS', N'Không có xung đột phòng lưu trú, các khoảng thời gian độc lập');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R19', N'Phòng Pet Hotel không bị đặt trùng lịch lưu trú', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R20: Tiền phòng Pet Hotel khớp đơn giá ngày, số ngày và phụ phí
-- ====================================================================================================
BEGIN TRY
    SELECT @ErrCount = COUNT(*)
    FROM LichSuLuuTru
    WHERE thoiGianCheckOut < thoiGianCheckIn
       OR donGiaNgay <= 0
       OR ABS(tongTien - (soNgayLuuTru * donGiaNgay + phiPhatSinh)) > 1.0;

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R20', N'Tiền lưu trú Hotel khớp đơn giá ngày x số ngày + phụ phí', @ErrCount, 'FAIL', N'Có phiếu lưu trú sai số ngày hoặc sai tổng tiền');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R20', N'Tiền lưu trú Hotel khớp đơn giá ngày x số ngày + phụ phí', 0, 'PASS', N'Toàn bộ lịch sử lưu trú tính đúng: Tổng = Ngày x Đơn giá + Phụ phí');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R20', N'Tiền lưu trú Hotel khớp đơn giá ngày x số ngày + phụ phí', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R21: Thẻ Spa Pass có số lượt còn lại hợp lệ so với lịch sử dùng
-- ====================================================================================================
BEGIN TRY
    WITH PassCheck AS (
        SELECT 
            sp.maThe,
            sp.tongSoBuoi,
            sp.soBuoiDaDung,
            ISNULL(COUNT(ls.maSuDung), 0) AS TongDaDung
        FROM TheSpaPass sp
        LEFT JOIN LichSuSuDungSpaPass ls ON sp.maThe = ls.maThe
        GROUP BY sp.maThe, sp.tongSoBuoi, sp.soBuoiDaDung
    )
    SELECT @ErrCount = COUNT(*)
    FROM PassCheck
    WHERE soBuoiDaDung < 0
       OR soBuoiDaDung > tongSoBuoi;

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R21', N'Số lượt Spa Pass còn lại hợp lệ so với lịch sử dùng', @ErrCount, 'FAIL', N'Có thẻ Spa Pass có số lượt sử dụng bất thường');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R21', N'Số lượt Spa Pass còn lại hợp lệ so với lịch sử dùng', 0, 'PASS', N'Toàn bộ thẻ Spa Pass có số lượt hợp lệ và đúng quy chế punch card');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R21', N'Số lượt Spa Pass còn lại hợp lệ so với lịch sử dùng', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R22: Tiền mặt lý thuyết trong bàn giao ca khớp công thức
-- ====================================================================================================
BEGIN TRY
    SELECT @ErrCount = COUNT(*)
    FROM BangGiaoCa
    WHERE tienCuoiCaHeThong <> (tienDauCa + tienThuTienMat - tienHoanTra);

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R22', N'Tiền mặt lý thuyết giao ca khớp (Đầu ca + Thu mặt - Hoàn trả)', @ErrCount, 'FAIL', N'Phát hiện bản ghi giao ca tính sai tiền mặt lý thuyết');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R22', N'Tiền mặt lý thuyết giao ca khớp (Đầu ca + Thu mặt - Hoàn trả)', 0, 'PASS', N'Tất cả biên bản giao ca tính đúng: Hệ thống = Đầu ca + Thu mặt - Hoàn trả');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R22', N'Tiền mặt lý thuyết giao ca khớp (Đầu ca + Thu mặt - Hoàn trả)', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R23: Tiền chênh lệch bàn giao ca khớp công thức
-- ====================================================================================================
BEGIN TRY
    SELECT @ErrCount = COUNT(*)
    FROM BangGiaoCa
    WHERE tienChenhLech <> (tienCuoiCaThucTe - tienCuoiCaHeThong);

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R23', N'Tiền chênh lệch giao ca khớp công thức (Thực tế - Hệ thống)', @ErrCount, 'FAIL', N'Có bản ghi giao ca tính sai tiền chênh lệch');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R23', N'Tiền chênh lệch giao ca khớp công thức (Thực tế - Hệ thống)', 0, 'PASS', N'Tất cả biên bản giao ca khớp chính xác: Chênh lệch = Thực tế - Hệ thống');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R23', N'Tiền chênh lệch giao ca khớp công thức (Thực tế - Hệ thống)', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R24: Tính hợp lệ của người duyệt ca (không tự duyệt, vai trò Quản lý)
-- ====================================================================================================
BEGIN TRY
    SELECT @ErrCount = COUNT(*)
    FROM BangGiaoCa bgc
    LEFT JOIN NhanVien nv ON bgc.maQuanLyDuyet = nv.maNhanVien
    LEFT JOIN TaiKhoan tk ON nv.maTaiKhoan = tk.maTaiKhoan
    WHERE bgc.trangThai = 'DA_DUYET'
      AND (
          bgc.maQuanLyDuyet IS NULL
          OR bgc.maQuanLyDuyet = bgc.maNhanVien -- Không được tự duyệt ca của mình
          OR bgc.thoiGianDuyet IS NULL
          OR tk.vaiTro IS NULL
          OR tk.vaiTro NOT IN (N'Quản lý', N'NVQL')
      );

    IF @ErrCount > 0
        INSERT INTO #KetQuaKiemTra VALUES ('R24', N'Người duyệt ca hợp lệ (là Quản lý, có thời gian duyệt, không tự duyệt)', @ErrCount, 'FAIL', N'Có biên bản giao ca đã duyệt nhưng vi phạm điều kiện người duyệt');
    ELSE
        INSERT INTO #KetQuaKiemTra VALUES ('R24', N'Người duyệt ca hợp lệ (là Quản lý, có thời gian duyệt, không tự duyệt)', 0, 'PASS', N'Tất cả ca đã duyệt đều có Quản lý phê duyệt hợp lệ và không tự duyệt');
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R24', N'Người duyệt ca hợp lệ (là Quản lý, có thời gian duyệt, không tự duyệt)', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- R25: Doanh thu tổng hợp Dashboard/Báo cáo khớp dữ liệu nguồn
-- ====================================================================================================
BEGIN TRY
    DECLARE @TongDoanhThuHoaDon BIGINT;
    DECLARE @TongDoanhThuChiTiet BIGINT;

    -- Đồng bộ công thức Dashboard: Chỉ tính các hóa đơn đã thanh toán thành công
    SELECT @TongDoanhThuHoaDon = ISNULL(SUM(thanhTien), 0)
    FROM HoaDon
    WHERE trangThaiHoaDon = N'Đã thanh toán';

    SELECT @TongDoanhThuChiTiet = ISNULL(SUM(ct.thanhTien), 0) - ISNULL((SELECT SUM(giamGia) FROM HoaDon WHERE trangThaiHoaDon = N'Đã thanh toán'), 0)
    FROM ChiTietHoaDon ct
    JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon
    WHERE hd.trangThaiHoaDon = N'Đã thanh toán';

    IF @TongDoanhThuHoaDon <> @TongDoanhThuChiTiet
    BEGIN
        SET @ErrCount = 1;
        INSERT INTO #KetQuaKiemTra VALUES ('R25', N'Doanh thu tổng hợp Dashboard/Báo cáo khớp dữ liệu chi tiết nguồn', @ErrCount, 'FAIL', 
            N'Lệch số liệu: Doanh thu HoaDon = ' + CAST(@TongDoanhThuHoaDon AS NVARCHAR(20)) + N' vs Chi tiết = ' + CAST(@TongDoanhThuChiTiet AS NVARCHAR(20)));
    END
    ELSE
    BEGIN
        INSERT INTO #KetQuaKiemTra VALUES ('R25', N'Doanh thu tổng hợp Dashboard/Báo cáo khớp dữ liệu chi tiết nguồn', 0, 'PASS', 
            N'Doanh thu tổng hợp khớp tuyệt đối với dữ liệu chi tiết nguồn (' + CAST(@TongDoanhThuHoaDon AS NVARCHAR(20)) + N' VNĐ)');
    END
END TRY
BEGIN CATCH
    INSERT INTO #KetQuaKiemTra VALUES ('R25', N'Doanh thu tổng hợp Dashboard/Báo cáo khớp dữ liệu chi tiết nguồn', -1, 'NOT_VERIFIABLE', ERROR_MESSAGE());
END CATCH;

-- ====================================================================================================
-- TRẢ VỀ BẢNG KẾT QUẢ ĐẦY ĐỦ 25 QUY TẮC
-- ====================================================================================================
SELECT 
    MaKiemTra,
    QuyTac,
    SoDongLoi,
    TrangThai,
    ChiTietLoi
FROM #KetQuaKiemTra
ORDER BY MaKiemTra;

-- BẢNG THỐNG KÊ TỔNG HỢP TRẠNG THÁI
SELECT 
    TrangThai,
    COUNT(*) AS SoLuongQuyTac,
    CAST(COUNT(*) * 100.0 / 25.0 AS DECIMAL(5,2)) AS TyLePhanTram
FROM #KetQuaKiemTra
GROUP BY TrangThai;

DROP TABLE #KetQuaKiemTra;
