package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto;

import java.util.Map;

public record PreparedRow(Map<String, Object> row, int excelRowNumber) {
}
