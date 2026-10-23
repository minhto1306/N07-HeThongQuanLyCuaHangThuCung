package models.entity;

/**
 * Thực thể Sản phẩm
 */
public class SanPham {
    private String maSanPham;
    private DanhMucHang danhMucHang;
    private String tenSanPham;
    private String moTa;

    public SanPham() {}

    public SanPham(String maSanPham, DanhMucHang danhMucHang, String tenSanPham, String moTa) {
        this.maSanPham = maSanPham;
        this.danhMucHang = danhMucHang;
        this.tenSanPham = tenSanPham;
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

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public String toString() {
        return "SanPham{" +
                "maSanPham='" + maSanPham + '\'' +
                ", tenSanPham='" + tenSanPham + '\'' +
                ", danhMuc=" + (danhMucHang != null ? danhMucHang.getTenDanhMuc() : "null") +
                '}';
    }
}
