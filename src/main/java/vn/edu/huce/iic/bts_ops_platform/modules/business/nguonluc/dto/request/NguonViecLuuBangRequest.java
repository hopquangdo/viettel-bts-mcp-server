package vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.NguonViecColumnLayoutDto;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.NguonViecCustomColumnDto;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.NguonViecManualRowDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class NguonViecLuuBangRequest {
    @NotBlank
    private String trungTam;
    private List<NguonViecCustomColumnDto> customColumns = new ArrayList<>();
    private Map<String, Map<String, String>> customValues = new HashMap<>();
    private List<NguonViecManualRowDto> manualRows = new ArrayList<>();
    private Map<String, String> ghiChuKv = new HashMap<>();
    private NguonViecColumnLayoutDto columnLayout;
}
