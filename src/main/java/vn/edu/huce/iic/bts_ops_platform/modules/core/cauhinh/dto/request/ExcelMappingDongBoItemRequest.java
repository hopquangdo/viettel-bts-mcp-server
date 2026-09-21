package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class ExcelMappingDongBoItemRequest {
    private UUID thuocTinhId;
    private String cotExcel;
    private Boolean batBuoc;
    private Short thuTu;
}
