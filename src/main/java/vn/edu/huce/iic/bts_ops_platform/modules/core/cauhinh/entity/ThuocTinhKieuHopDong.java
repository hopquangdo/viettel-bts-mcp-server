package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity;

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
@Table(name = "thuoc_tinh_kieu_hop_dong")
public class ThuocTinhKieuHopDong extends AuditableEntity {

    @Column(name = "kieu_hop_dong_id", nullable = false)
    private UUID kieuHopDongId;

    @Column(name = "thuoc_tinh_hop_dong_id", nullable = false)
    private UUID thuocTinhHopDongId;

    @Column(name = "thu_tu", nullable = false)
    private Short thuTu = 0;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
