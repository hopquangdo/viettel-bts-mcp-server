package vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class NguonViecBangDuLieu {
    private List<NguonViecCustomColumnDto> customColumns = new ArrayList<>();
    private Map<String, Map<String, String>> customValues = new HashMap<>();
    private List<NguonViecManualRowDto> manualRows = new ArrayList<>();
    private Map<String, String> ghiChuKv = new HashMap<>();
    private NguonViecColumnLayoutDto columnLayout;
}
