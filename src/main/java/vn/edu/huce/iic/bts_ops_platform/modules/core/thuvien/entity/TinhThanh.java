package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tinh_thanh")
public class TinhThanh extends AuditableEntity {

    @Column(name = "tinh_thanh_id", nullable = false)
    private UUID tinhThanhId;

    @Column(name = "ma", nullable = false, length = 20)
    private String ma;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "la_tinh_cu", nullable = false)
    private Boolean laTinhCu = false;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
