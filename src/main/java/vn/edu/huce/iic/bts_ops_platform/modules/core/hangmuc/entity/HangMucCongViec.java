package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hang_muc_cong_viec")
public class HangMucCongViec extends AuditableEntity {

    @Column(name = "hang_muc_chi_tiet_id", nullable = false)
    private UUID hangMucChiTietId;

    @Column(name = "ma", nullable = false, length = 50)
    private String ma;

    @Column(name = "ten", nullable = false, length = 500)
    private String ten;

    @Column(name = "don_vi", length = 50)
    private String donVi;

    @Column(name = "don_gia", nullable = false, precision = 18, scale = 2)
    private BigDecimal donGia = BigDecimal.ZERO;

    @Column(name = "khoi_luong", precision = 18, scale = 2)
    private BigDecimal khoiLuong;

    @Column(name = "cong_thuc_khoi_luong")
    private String congThucKhoiLuong;

    @Column(name = "cong_thuc_don_gia")
    private String congThucDonGia;

    @Column(name = "cong_thuc_thanh_tien")
    private String congThucThanhTien;

    @Column(name = "vi_tri_thi_cong")
    private String viTriThiCong;

    @Column(name = "thu_tu", nullable = false)
    private Short thuTu = 0;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "trang_thai", length = 50)
    private String trangThai;
}
