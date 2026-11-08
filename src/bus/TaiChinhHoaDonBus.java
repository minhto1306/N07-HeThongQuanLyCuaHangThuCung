package bus;

import java.math.BigDecimal;

/**
 * Nguồn công thức tài chính dùng chung cho POS, đổi trả và đối soát.
 * Lớp này không truy cập GUI/CSDL để có thể kiểm thử độc lập.
 */
public final class TaiChinhHoaDonBus {

    public static final String TIEN_MAT = "Tiền mặt";
    public static final String CHUYEN_KHOAN = "Chuyển khoản";
    public static final String KHONG_HOAN_TIEN = "KHONG_HOAN_TIEN";

    private TaiChinhHoaDonBus() {
    }

    public static final class KetQuaThanhToan {
        private final BigDecimal tongTienHang;
        private final BigDecimal giamGia;
        private final BigDecimal tienDoiDiem;
        private final BigDecimal tienBuTruDoiTra;
        private final BigDecimal tienPhaiThanhToan;
        private final BigDecimal tienHoanDoiTra;

        private KetQuaThanhToan(BigDecimal tongTienHang, BigDecimal giamGia,
                                BigDecimal tienDoiDiem, BigDecimal tienBuTruDoiTra,
                                BigDecimal tienPhaiThanhToan, BigDecimal tienHoanDoiTra) {
            this.tongTienHang = tongTienHang;
            this.giamGia = giamGia;
            this.tienDoiDiem = tienDoiDiem;
            this.tienBuTruDoiTra = tienBuTruDoiTra;
            this.tienPhaiThanhToan = tienPhaiThanhToan;
            this.tienHoanDoiTra = tienHoanDoiTra;
        }

        public BigDecimal getTongTienHang() { return tongTienHang; }
        public BigDecimal getGiamGia() { return giamGia; }
        public BigDecimal getTienDoiDiem() { return tienDoiDiem; }
        public BigDecimal getTienBuTruDoiTra() { return tienBuTruDoiTra; }
        public BigDecimal getTienPhaiThanhToan() { return tienPhaiThanhToan; }
        public BigDecimal getTienHoanDoiTra() { return tienHoanDoiTra; }
    }

    public static KetQuaThanhToan tinhThanhToan(BigDecimal tongTienHang,
                                                 BigDecimal giamGia,
                                                 BigDecimal tienDoiDiem,
                                                 BigDecimal tienBuTruDoiTra) {
        BigDecimal tong = khongAm(tongTienHang, "Tổng tiền hàng");
        BigDecimal giam = khongAm(giamGia, "Giảm giá");
        BigDecimal diem = khongAm(tienDoiDiem, "Tiền đổi điểm");
        BigDecimal buTru = khongAm(tienBuTruDoiTra, "Tiền bù trừ đổi trả");

        BigDecimal sauUuDai = tong.subtract(giam).subtract(diem).max(BigDecimal.ZERO);
        BigDecimal phaiTra = sauUuDai.subtract(buTru).max(BigDecimal.ZERO);
        BigDecimal hoanDoiTra = buTru.subtract(sauUuDai).max(BigDecimal.ZERO);
        return new KetQuaThanhToan(tong, giam, diem, buTru, phaiTra, hoanDoiTra);
    }

    public static BigDecimal tinhTienThuaTienMat(BigDecimal tienKhachDua,
                                                 BigDecimal tienPhaiThanhToan) {
        BigDecimal dua = khongAm(tienKhachDua, "Tiền khách đưa");
        BigDecimal phaiTra = khongAm(tienPhaiThanhToan, "Tiền phải thanh toán");
        if (dua.compareTo(phaiTra) < 0) {
            throw new IllegalArgumentException("Tiền khách đưa chưa đủ; còn thiếu "
                    + phaiTra.subtract(dua).toPlainString());
        }
        return dua.subtract(phaiTra);
    }

    public static BigDecimal tinhTienCuoiCa(BigDecimal tienDauCa,
                                             BigDecimal thuTienMat,
                                             BigDecimal hoanTienMat) {
        BigDecimal dauCa = khongAm(tienDauCa, "Tiền đầu ca");
        BigDecimal thuMat = khongAm(thuTienMat, "Tiền thu tiền mặt");
        BigDecimal hoanMat = khongAm(hoanTienMat, "Tiền hoàn tiền mặt");
        BigDecimal ketQua = dauCa.add(thuMat).subtract(hoanMat);
        if (ketQua.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tiền mặt lý thuyết cuối ca không thể âm");
        }
        return ketQua;
    }

    public static String chuanHoaPhuongThucHoan(String phuongThuc, boolean coTienHoan) {
        if (!coTienHoan) return KHONG_HOAN_TIEN;
        if (phuongThuc == null) {
            throw new IllegalArgumentException("Phải chọn phương thức hoàn tiền");
        }
        String value = phuongThuc.trim();
        if (TIEN_MAT.equalsIgnoreCase(value)) return TIEN_MAT;
        if (CHUYEN_KHOAN.equalsIgnoreCase(value)) return CHUYEN_KHOAN;
        throw new IllegalArgumentException("Phương thức hoàn tiền không hợp lệ: " + value);
    }

    private static BigDecimal khongAm(BigDecimal value, String tenTruong) {
        BigDecimal result = value != null ? value : BigDecimal.ZERO;
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(tenTruong + " không được âm");
        }
        return result;
    }
}
