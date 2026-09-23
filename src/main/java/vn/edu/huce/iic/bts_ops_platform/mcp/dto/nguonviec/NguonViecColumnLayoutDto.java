package vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class NguonViecColumnLayoutDto {
    private List<String> columnOrder = new ArrayList<>();
    private List<String> hiddenColumns = new ArrayList<>();
}
