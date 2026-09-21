package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class KeHoachImportChiTietRequest {

    @NotEmpty
    private List<UUID> doiTuongIds;
}
