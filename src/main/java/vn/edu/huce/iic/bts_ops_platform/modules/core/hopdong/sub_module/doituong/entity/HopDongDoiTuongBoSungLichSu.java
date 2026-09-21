package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.util.UUID;

/** Nhật ký điều chỉnh bổ sung sản lượng Volume HD (add/remove trên boSungSanLuong). */
@Getter
@Setter
@Entity
@Table(name = "hop_dong_doi_tuong_bo_sung_lich_su")
public class HopDongDoiTuongBoSungLichSu extends AuditableEntity {

    @Column(name = "hop_dong_id", nullable = false)
    private UUID hopDongId;

    @Column(name = "hop_dong_doi_tuong_id", nullable = false)
    private UUID hopDongDoiTuongId;

    @Column(name = "hanh_dong", nullable = false, length = 20)
    private String hanhDong;

    @Column(name = "delta_vnd", nullable = false, precision = 18, scale = 2)
    private BigDecimal deltaVnd;

    @Column(name = "bo_sung_truoc", nullable = false, precision = 18, scale = 2)
    private BigDecimal boSungTruoc;

    @Column(name = "bo_sung_sau", nullable = false, precision = 18, scale = 2)
    private BigDecimal boSungSau;

    @Column(name = "ly_do", nullable = false, length = 64)
    private String lyDo;

    @Column(name = "ghi_chu", columnDefinition = "text")
    private String ghiChu;
}
