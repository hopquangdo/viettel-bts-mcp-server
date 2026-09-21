package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hop_dong_doi_tuong_checklist_dap_ung")
public class HopDongDoiTuongChecklistDapUng extends AuditableEntity {

    @Column(name = "hop_dong_doi_tuong_id", nullable = false)
    private UUID hopDongDoiTuongId;

    @Column(name = "checklist_muc_id", nullable = false)
    private UUID checklistMucId;

    @Column(name = "da_duyet", nullable = false)
    private Boolean daDuyet = false;

    @Column(name = "ngay_duyet")
    private Instant ngayDuyet;

    @Column(name = "nguoi_duyet_id")
    private UUID nguoiDuyetId;

    @Column(name = "ghi_chu", columnDefinition = "text")
    private String ghiChu;
}
