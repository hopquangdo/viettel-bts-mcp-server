package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ChecklistMucResponse {
    private UUID id;
    private UUID hopDongId;
    private String ten;
    private Integer thuTu;
    private String ghiChu;
}
