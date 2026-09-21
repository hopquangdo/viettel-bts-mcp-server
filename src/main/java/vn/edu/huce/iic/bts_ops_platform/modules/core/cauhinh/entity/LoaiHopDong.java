package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "loai_hop_dong")
public class LoaiHopDong extends AuditableEntity {

    @Column(name = "ma", nullable = false, unique = true, length = 50)
    private String ma;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "heso_nguong")
    private BigDecimal hesoNguong;

    /** thi_cong | tu_van_thiet_ke — quyết định danh mục biên bản nào áp dụng cho hợp đồng thuộc
     * loại này (xem KieuHopDongDanhMucBienBan) và hiển thị thẻ phân biệt 2 luồng trên giao diện. */
    @Column(name = "he_nghiep_vu", nullable = true, length = 30)
    private String heNghiepVu = "thi_cong";

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
