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
@Table(name = "hop_dong_tep_dinh_kem")
public class HopDongTepDinhKem extends AuditableEntity {

    @Column(name = "hop_dong_id", nullable = false)
    private UUID hopDongId;

    @Column(name = "tep_dinh_kem_id", nullable = false)
    private UUID tepDinhKemId;

    @Column(name = "loai_tai_lieu", length = 50)
    private String loaiTaiLieu;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
