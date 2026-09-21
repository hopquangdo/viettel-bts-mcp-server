package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ExcelMappingImportService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.HangMucImportMappingFields;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Đọc giá trị từng dòng Excel đã parse cho import Hạng mục thi công.
 * Ưu tiên tên cột từ cấu hình mapping; fallback theo {@link HangMucImportFields}.
 */
@Component
@RequiredArgsConstructor
public class HangMucImportFieldResolver {

    private final ExcelMappingImportService excelMappingImportService;

    public HangMucImportMappingFields resolveMappingFields(UUID excelMappingId) {
        return new HangMucImportMappingFields(
                resolveAttributeField(excelMappingId, HangMucImportAttributeRole.NHOM_HANG_MUC),
                resolveAttributeField(excelMappingId, HangMucImportAttributeRole.HANG_MUC),
                resolveAttributeField(excelMappingId, HangMucImportAttributeRole.CONG_TAC),
                resolveAttributeField(excelMappingId, HangMucImportAttributeRole.DON_GIA),
                resolveAttributeField(excelMappingId, HangMucImportAttributeRole.KHOI_LUONG),
                resolveAttributeField(excelMappingId, HangMucImportAttributeRole.DON_VI),
                resolveAttributeField(excelMappingId, HangMucImportAttributeRole.VI_TRI_THI_CONG),
                resolveAttributeField(excelMappingId, HangMucImportAttributeRole.MA_CONG_TAC),
                resolveAttributeField(excelMappingId, HangMucImportAttributeRole.MA_IMPORT),
                resolveAttributeField(excelMappingId, HangMucImportAttributeRole.STT));
    }

    private String resolveAttributeField(UUID excelMappingId, HangMucImportAttributeRole role) {
        return excelMappingImportService
                .resolveMappedAttributeFieldName(excelMappingId, HangMucImportFields.nameFor(role))
                .orElse(null);
    }

    public String readString(Map<String, Object> row, String configuredField, HangMucImportAttributeRole role) {
        String fromConfig = readString(row, configuredField);
        if (fromConfig != null) {
            return fromConfig;
        }
        return readString(row, HangMucImportFields.nameFor(role));
    }

    public String readString(Map<String, Object> row, String... keys) {
        if (keys == null) {
            return null;
        }
        for (String key : keys) {
            if (key == null || key.isBlank()) {
                continue;
            }
            Object value = row.get(key);
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value).trim();
            if (!text.isBlank()) {
                return text;
            }
        }
        return null;
    }

    public Short readShort(Map<String, Object> row, String configuredField, HangMucImportAttributeRole role) {
        String text = readString(row, configuredField, role);
        if (text == null) {
            return null;
        }
        try {
            return Short.parseShort(text.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public BigDecimal readDecimal(Map<String, Object> row, String configuredField, HangMucImportAttributeRole role) {
        String configured = configuredField;
        if (configured != null && !configured.isBlank()) {
            BigDecimal value = parseDecimal(row.get(configured));
            if (value != null) {
                return value;
            }
        }
        BigDecimal value = parseDecimal(row.get(HangMucImportFields.nameFor(role)));
        return value;
    }

    private BigDecimal parseDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof BigDecimal decimal) {
                return decimal;
            }
            if (value instanceof Number number) {
                return BigDecimal.valueOf(number.doubleValue());
            }
            String text = String.valueOf(value).trim().replace(",", "").replace(" ", "");
            if (!text.isBlank()) {
                return new BigDecimal(text);
            }
        } catch (NumberFormatException ignored) {
            return null;
        }
        return null;
    }
}
