package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.util;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ExcelMappingCot;

import java.util.Map;
import java.util.UUID;

record ExcelResolvedColumn(ExcelMappingCot column, int columnIndex) {
    String fieldName(Map<UUID, String> thuocTinhNames) {
        if (column.getThuocTinhId() != null) {
            String fromThuocTinh = thuocTinhNames.get(column.getThuocTinhId());
            if (fromThuocTinh != null && !fromThuocTinh.isBlank()) {
                return fromThuocTinh;
            }
        }
        if (column.getCotExcel() != null && !column.getCotExcel().isBlank()) {
            return column.getCotExcel().trim();
        }
        if (column.getThuocTinhId() != null) {
            return column.getThuocTinhId().toString();
        }
        return column.getId() != null ? column.getId().toString() : "unknown";
    }
}
