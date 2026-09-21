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
@Table(name = "kieu_du_lieu")
public class KieuDuLieu extends AuditableEntity {

    @Column(name = "ten", length = 20)
    private String ten;

    @Column(name = "lien_ket_bang", length = 100)
    private String lienKetBang;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
