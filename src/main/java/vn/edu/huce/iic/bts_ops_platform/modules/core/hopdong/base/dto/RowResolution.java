package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto;

import java.util.List;
import java.util.Map;

public record RowResolution(Map<String, Object> row, List<String> errors) {
}
