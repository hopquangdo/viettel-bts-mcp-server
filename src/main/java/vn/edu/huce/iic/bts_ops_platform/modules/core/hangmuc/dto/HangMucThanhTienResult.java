package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HangMucThanhTienResult {

    @Builder.Default
    private BigDecimal tongThanhTien = BigDecimal.ZERO;

    @Builder.Default
    private Map<UUID, HangMucRowComputed> byId = new HashMap<>();

    @Builder.Default
    private List<HangMucNhomComputed> nhom = new ArrayList<>();
}
