package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity;

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
@Table(name = "ke_hoach_trien_khai_chi_tiet")
public class KeHoachTrienKhaiChiTiet extends AuditableEntity {

    @Column(name = "ke_hoach_id", nullable = false)
    private UUID keHoachId;

    @Column(name = "hop_dong_doi_tuong_id", nullable = false)
    private UUID hopDongDoiTuongId;

    @Column(name = "khu_vuc_id")
    private UUID khuVucId;

    @Column(name = "nha_thau_id")
    private UUID nhaThauId;
}
