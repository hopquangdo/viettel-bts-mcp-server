package vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

@Getter
@Setter
@Entity
@Table(name = "chu_dau_tu")
public class ChuDauTu extends AuditableEntity {

    @Column(name = "ma", nullable = false, unique = true, length = 50)
    private String ma;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}