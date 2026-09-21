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
@Table(name = "doi_tuong_hop_dong_lien_ket")
public class DoiTuongHopDongLienKet extends AuditableEntity {

    @Column(name = "doi_tuong_quan_ly_id", nullable = false)
    private UUID doiTuongQuanLyId;

    @Column(name = "loai_hop_dong_id")
    private UUID loaiHopDongId;

    @Column(name = "kieu_hop_dong_id")
    private UUID kieuHopDongId;

    @Column(name = "thu_tu", nullable = false)
    private Short thuTu = 0;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
