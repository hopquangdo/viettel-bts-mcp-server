package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HangMucNhomComputed {

    private UUID id;
    private String ma;
    private String ten;

    @Builder.Default
    private BigDecimal thanhTien = BigDecimal.ZERO;

    @Builder.Default
    private List<HangMucItemComputed> hangMuc = new ArrayList<>();
}
