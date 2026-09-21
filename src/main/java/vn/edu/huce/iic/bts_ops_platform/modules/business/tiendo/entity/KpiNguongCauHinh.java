package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.BaseEntity;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "kpi_nguong_cau_hinh")
public class KpiNguongCauHinh extends BaseEntity {

    @Column(name = "ma", nullable = false, unique = true, length = 50)
    private String ma;

    @Column(name = "ten", nullable = false, length = 255)
    private String ten;

    @Column(name = "so_ngay_nguong", nullable = false)
    private Integer soNgayNguong;

    @Column(name = "uu_tien", nullable = false)
    private Integer uuTien = 2;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private Instant ngayTao;

    @Column(name = "ngay_cap_nhat", nullable = false)
    private Instant ngayCapNhat;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        ngayTao = now;
        ngayCapNhat = now;
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = Instant.now();
    }
}
