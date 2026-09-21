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
@Table(name = "hop_dong_checklist_muc")
public class HopDongChecklistMuc extends AuditableEntity {

    @Column(name = "hop_dong_id", nullable = false)
    private UUID hopDongId;

    @Column(name = "ten", nullable = false, length = 255)
    private String ten;

    @Column(name = "thu_tu", nullable = false)
    private Integer thuTu = 0;

    @Column(name = "ghi_chu", columnDefinition = "text")
    private String ghiChu;
}
