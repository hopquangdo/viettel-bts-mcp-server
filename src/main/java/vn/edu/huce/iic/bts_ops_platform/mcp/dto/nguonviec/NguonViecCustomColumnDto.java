package vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class NguonViecCustomColumnDto {
    private String id;
    private String name;
    private String type;
    private String formula;
    private String format;
    private String dataType;
    private List<String> dropdownOptions = new ArrayList<>();
}
