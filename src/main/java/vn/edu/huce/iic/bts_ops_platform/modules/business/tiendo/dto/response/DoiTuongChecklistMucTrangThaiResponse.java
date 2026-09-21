package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class DoiTuongChecklistMucTrangThaiResponse {
    private UUID checklistMucId;
    private String ten;
    private Integer thuTu;
    private Boolean daDuyet;
    private Instant ngayDuyet;
}
