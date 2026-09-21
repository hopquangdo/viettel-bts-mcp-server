package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "san_luong")
public class SanLuong extends AuditableEntity {

    @Column(name = "hop_dong_id")
    private UUID hopDongId;

    @Column(name = "hop_dong_doi_tuong_id")
    private UUID hopDongDoiTuongId;

    @Column(name = "hang_muc_cong_viec_id")
    private UUID hangMucCongViecId;

    @Column(name = "hang_muc_chi_tiet_id")
    private UUID hangMucChiTietId;

    @Column(name = "ngay_thuc_hien")
    private LocalDate ngayThucHien;

    @Column(name = "lan_cap_nhat_id")
    private UUID lanCapNhatId;

    @Column(name = "don_gia", precision = 18, scale = 2)
    private BigDecimal donGia;

    @Column(name = "khoi_luong_hoan_thanh", precision = 18, scale = 2)
    private BigDecimal khoiLuongHoanThanh;

    @Column(name = "trang_thai", length = 50)
    private String trangThai;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;

    /** dat | khong_dat — null nghĩa là chưa nghiệm thu. Khác với `trangThai` (workflow tiến độ
     * pending/done/issue) — đây là kết quả nghiệm thu chất lượng/khối lượng của đúng lần ghi nhận này. */
    @Column(name = "ket_qua_nghiem_thu", length = 20)
    private String ketQuaNghiemThu;

    @Column(name = "ly_do_khong_dat", columnDefinition = "text")
    private String lyDoKhongDat;

    @Column(name = "nguoi_nghiem_thu_id")
    private UUID nguoiNghiemThuId;

    @Column(name = "nguoi_nghiem_thu_ten")
    private String nguoiNghiemThuTen;

    @Column(name = "ngay_nghiem_thu")
    private Instant ngayNghiemThu;
}
