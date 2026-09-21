package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ExcelMappingDongBoRequest {
    @Valid
    @NotNull
    private List<ExcelMappingDongBoItemRequest> cotDanhSach;
}
