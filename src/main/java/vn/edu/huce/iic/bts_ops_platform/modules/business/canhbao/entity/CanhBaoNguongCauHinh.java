package vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "canh_bao_nguong_cau_hinh")
public class CanhBaoNguongCauHinh extends BaseEntity {

    @Column(name = "loai", nullable = false, unique = true, length = 50)
    private String loai;

    @Column(name = "gia_tri_nguong", nullable = false, precision = 8, scale = 2)
    private BigDecimal giaTriNguong;

    @Column(name = "mo_ta", columnDefinition = "text")
    private String moTa;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private Instant ngayTao;

    @Column(name = "ngay_cap_nhat", nullable = false)
    private Instant ngayCapNhat;
}
