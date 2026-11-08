package entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể Sản phẩm (SanPham).
 * Quản lý định danh hàng hóa, mã vạch Barcode, đơn vị cơ sở và giá bán niêm yết do Quản lý quy định.
 * Một Sản phẩm có thể có nhiều Lô hàng (LoHang) với hạn sử dụng và giá nhập khác nhau.
 * Một Sản phẩm có thể có nhiều quy cách quy đổi đơn vị (QuyDoiDonVi) với tỷ lệ và giá bán tương ứng.
 */
public class SanPham {
    private String maSanPham;
    private DanhMucHang danhMucHang;
    private String tenSanPham;
    private String maVach;
    private String donViCoSo;
    private BigDecimal giaBanCoSo;
    private String moTa;

    private List<LoHang> dsLoHang = new ArrayList<>();
    private List<QuyDoiDonVi> dsQuyDoi = new ArrayList<>();

    public SanPham() {
        this.donViCoSo = "Đơn vị";
        this.giaBanCoSo = BigDecimal.ZERO;
    }

    public SanPham(String maSanPham, DanhMucHang danhMucHang, String tenSanPham, String moTa) {
        this(maSanPham, danhMucHang, tenSanPham, "", "Đơn vị", BigDecimal.ZERO, moTa);
    }

    public SanPham(String maSanPham, DanhMucHang danhMucHang, String tenSanPham, String maVach,
                   String donViCoSo, BigDecimal giaBanCoSo, String moTa) {
        this.maSanPham = maSanPham;
        this.danhMucHang = danhMucHang;
        this.tenSanPham = tenSanPham;
        this.maVach = maVach;
        this.donViCoSo = (donViCoSo != null && !donViCoSo.isEmpty()) ? donViCoSo : "Đơn vị";
        this.giaBanCoSo = giaBanCoSo != null ? giaBanCoSo : BigDecimal.ZERO;
        this.moTa = moTa;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public DanhMucHang getDanhMucHang() {
        return danhMucHang;
    }

    public void setDanhMucHang(DanhMucHang danhMucHang) {
        this.danhMucHang = danhMucHang;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public String getMaVach() {
        return maVach != null ? maVach : "";
    }

    public void setMaVach(String maVach) {
        this.maVach = maVach;
    }

    public String getDonViCoSo() {
        return donViCoSo != null ? donViCoSo : "Đơn vị";
    }

    public void setDonViCoSo(String donViCoSo) {
        this.donViCoSo = donViCoSo;
    }

    public BigDecimal getGiaBanCoSo() {
        return giaBanCoSo != null ? giaBanCoSo : BigDecimal.ZERO;
    }

    public void setGiaBanCoSo(BigDecimal giaBanCoSo) {
        this.giaBanCoSo = giaBanCoSo;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public List<LoHang> getDsLoHang() {
        return dsLoHang;
    }

    public void setDsLoHang(List<LoHang> dsLoHang) {
        this.dsLoHang = dsLoHang != null ? dsLoHang : new ArrayList<>();
    }

    public List<QuyDoiDonVi> getDsQuyDoi() {
        return dsQuyDoi;
    }

    public void setDsQuyDoi(List<QuyDoiDonVi> dsQuyDoi) {
        this.dsQuyDoi = dsQuyDoi != null ? dsQuyDoi : new ArrayList<>();
    }

    // note lai: ham tinh tong ton kho da duoc chuyen sang KhoLoHangBus.tinhTongTonKhaDung(sp, dsLo)
    // entity SanPham chi thuan chua thuoc tinh maSP, tenSP, donVi, giaBan... dung chuan JavaBean

    @Override
    public String toString() {
        return "SanPham{" +
                "maSanPham='" + maSanPham + '\'' +
                ", tenSanPham='" + tenSanPham + '\'' +
                ", maVach='" + maVach + '\'' +
                ", donViCoSo='" + donViCoSo + '\'' +
                ", giaBanCoSo=" + giaBanCoSo +
                '}';
    }
}
