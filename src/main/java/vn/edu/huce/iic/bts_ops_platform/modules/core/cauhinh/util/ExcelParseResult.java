package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.util;

import java.util.List;
import java.util.Map;

public record ExcelParseResult(List<String> sheetNames, List<Map<String, Object>> rows) {
}
