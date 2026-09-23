package vn.edu.huce.iic.bts_ops_platform.mcp.entity.thuvien;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.entity.AuditableEntity;

@Getter
@Setter
@Entity
@Table(name = "khu_vuc")
public class KhuVuc extends AuditableEntity {

    @Column(name = "ma", nullable = false, unique = true, length = 20)
    private String ma;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
