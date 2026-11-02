USE QuanLyCuaHangThuCung;
GO

-- Chạy MỘT LẦN khi cần xóa lịch sử giao ca thử nghiệm cũ (GC004, GC005, ...)
-- và tạo lại chuỗi dữ liệu mẫu liên tục GC001 -> GC002 -> GC003.
-- Script này chỉ tác động đến bảng BangGiaoCa.

SET XACT_ABORT ON;
BEGIN TRANSACTION;

DELETE FROM BangGiaoCa;

INSERT INTO BangGiaoCa
    (maGiaoCa, maNhanVien, maCa, tienDauCa, tienCuoiCaHeThong,
     tienCuoiCaThucTe, tienChenhLech, tienThuTienMat, tienChuyenKhoan, tienHoanTra,
     lyDoChenhLech, gioKetThucThucTe, trangThai, maQuanLyDuyet, thoiGianDuyet)
VALUES
    ('GC001', 'NV002', 'CA01', 1000000, 3500000, 3500000, 0, 2500000, 750000, 0,
     N'Khớp két 100%',
     DATEADD(day, -3, DATEADD(hour, 12, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))),
     'DA_DUYET', 'NV001', DATEADD(minute, 10, DATEADD(day, -3, DATEADD(hour, 12, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))))),

    ('GC002', 'NV004', 'CA02', 3500000, 4600000, 4580000, -20000, 1150000, 400000, 50000,
     N'Thối nhầm 20.000 đ',
     DATEADD(day, -2, DATEADD(minute, 30, DATEADD(hour, 17, CAST(CAST(GETDATE() AS DATE) AS DATETIME2)))),
     'DA_DUYET', 'NV001', DATEADD(minute, 8, DATEADD(day, -2, DATEADD(minute, 30, DATEADD(hour, 17, CAST(CAST(GETDATE() AS DATE) AS DATETIME2)))))),

    ('GC003', 'NV001', 'CA03', 4580000, 6500000, 6500000, 0, 1920000, 565000, 0,
     N'Khớp két 100%',
     DATEADD(day, -1, DATEADD(hour, 22, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))),
     'DA_DUYET', 'NV001', DATEADD(minute, 5, DATEADD(day, -1, DATEADD(hour, 22, CAST(CAST(GETDATE() AS DATE) AS DATETIME2)))));

COMMIT TRANSACTION;
GO

SELECT maGiaoCa, maNhanVien, maCa, tienDauCa, tienCuoiCaHeThong,
       tienCuoiCaThucTe, tienChenhLech, tienThuTienMat, tienChuyenKhoan,
       tienHoanTra, trangThai, maQuanLyDuyet, thoiGianDuyet,
       lyDoChenhLech, gioKetThucThucTe
FROM BangGiaoCa
ORDER BY gioKetThucThucTe DESC, maGiaoCa DESC;
GO
