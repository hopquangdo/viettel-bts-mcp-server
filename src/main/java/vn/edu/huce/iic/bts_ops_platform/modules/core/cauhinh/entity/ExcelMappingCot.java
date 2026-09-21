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
@Table(name = "excel_mapping_cot")
public class ExcelMappingCot extends AuditableEntity {

    @Column(name = "excel_mapping_id", nullable = false)
    private UUID excelMappingId;

    @Column(name = "thuoc_tinh_id")
    private UUID thuocTinhId;

    @Column(name = "cot_excel", length = 50)
    private String cotExcel;

    @Column(name = "bat_buoc", nullable = false)
    private Boolean batBuoc = false;

    @Column(name = "thu_tu", nullable = false)
    private Short thuTu = 0;
}
