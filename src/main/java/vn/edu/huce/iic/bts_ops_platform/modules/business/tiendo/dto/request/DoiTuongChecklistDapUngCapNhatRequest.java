package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class DoiTuongChecklistDapUngCapNhatRequest {

    @NotNull
    private UUID checklistMucId;

    @NotNull
    private Boolean daDuyet;

    private String ghiChu;
}
