package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity;

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
@Table(name = "hop_dong_doi_tuong_gia_tri")
public class HopDongDoiTuongGiaTri extends AuditableEntity {

    @Column(name = "hop_dong_doi_tuong_id", nullable = false)
    private UUID hopDongDoiTuongId;

    @Column(name = "thuoc_tinh_id", nullable = false)
    private UUID thuocTinhId;

    @Column(name = "gia_tri")
    private String giaTri;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
