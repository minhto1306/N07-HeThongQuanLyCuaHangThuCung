package controllers;

import models.dao.BangGiaoCaDAO;
import models.entity.BangGiaoCa;
import models.entity.CaLamViec;
import models.entity.NhanVien;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Controller điều phối nghiệp vụ Mở ca, Chốt ca và Bàn giao két tiền
 */
public class GiaoCaController {

    private final BangGiaoCaDAO bangGiaoCaDAO;
    private BangGiaoCa caHienTai;

    public GiaoCaController() {
        this.bangGiaoCaDAO = new BangGiaoCaDAO();
    }

    /**
     * Bắt đầu một ca làm việc mới: Nhập số tiền mặt đầu ca
     */
    public boolean moCa(String maGiaoCa, NhanVien nv, CaLamViec ca, BigDecimal tienDauCa) throws SQLException {
        if (tienDauCa == null || tienDauCa.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số tiền đầu ca không được âm!");
        }

        this.caHienTai = new BangGiaoCa(
                maGiaoCa,
                nv,
                ca,
                tienDauCa,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "",
                null
        );

        return bangGiaoCaDAO.moCaLamViec(this.caHienTai);
    }

    /**
     * Chốt ca làm việc cuối ngày: Đối soát tiền mặt lý thuyết vs thực tế
     */
    public boolean chotCa(BigDecimal tienHeThong, BigDecimal tienThucTe, String lyDoChenhLech) throws SQLException {
        if (caHienTai == null) {
            throw new IllegalStateException("Chưa có ca làm việc nào đang mở!");
        }

        caHienTai.setTienCuoiCaHeThong(tienHeThong);
        caHienTai.setTienCuoiCaThucTe(tienThucTe);
        caHienTai.capNhatTienChenhLech();
        caHienTai.setGioKetThucThucTe(LocalDateTime.now());

        // Bắt buộc nhập lý do nếu phát sinh chênh lệch tiền mặt
        if (caHienTai.getTienChenhLech().compareTo(BigDecimal.ZERO) != 0) {
            if (lyDoChenhLech == null || lyDoChenhLech.trim().isEmpty()) {
                throw new IllegalArgumentException("Phát hiện chênh lệch két tiền mặt! Bắt buộc phải nhập lý do giải trình.");
            }
            caHienTai.setLyDoChenhLech(lyDoChenhLech);
        }

        return bangGiaoCaDAO.chotKetCa(this.caHienTai);
    }

    public BangGiaoCa getCaHienTai() {
        return caHienTai;
    }
}
