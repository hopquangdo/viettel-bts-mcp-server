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
@Table(name = "trang_thai_hop_dong")
public class TrangThaiHopDong extends AuditableEntity {

    @Column(name = "ten", length = 50)
    private String ten;

    @Column(name = "ma", length = 20)
    private String ma;

    @Column(name = "mau_sac", length = 20)
    private String mauSac;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
