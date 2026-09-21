package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity;

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
@Table(name = "hop_dong_thuoc_tinh")
public class HopDongThuocTinh extends AuditableEntity {

    @Column(name = "hop_dong_id", nullable = false)
    private UUID hopDongId;

    @Column(name = "thuoc_tinh_hop_dong_id", nullable = false)
    private UUID thuocTinhHopDongId;

    @Column(name = "gia_tri")
    private String giaTri;

    @Column(name = "thu_tu", nullable = false)
    private Short thuTu = 0;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
