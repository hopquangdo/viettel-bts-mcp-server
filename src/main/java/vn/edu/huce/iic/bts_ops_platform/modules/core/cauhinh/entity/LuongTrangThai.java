package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

@Getter
@Setter
@Entity
@Table(name = "luong_trang_thai")
public class LuongTrangThai extends AuditableEntity {

    @Column(name = "ten", nullable = false, length = 100)
    private String ten;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
