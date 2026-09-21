package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class SanLuongNhatKyEntryResponse {
    private String id;
    private Instant updatedAt;
    private LocalDate date;
    private String ghiChu;
    private boolean hasIssue;
    private Map<String, BigDecimal> volumes = new LinkedHashMap<>();
}
