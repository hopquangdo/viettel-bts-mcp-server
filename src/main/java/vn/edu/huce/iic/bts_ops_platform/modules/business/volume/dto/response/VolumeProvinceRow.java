package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeProvinceRow {
    private String id;
    private int order;
    private String province;
    private String provinceKey;
    private String legacyCode;
    private String nhaThau;
    private long contractStations;
    private long constructionStations;
    private long shortageStations;
    private long surplusStations;
    private BigDecimal contractValue;
    private BigDecimal constructionValue;
    private BigDecimal remainingValue;
    private long quotaStations;
    private BigDecimal quotaValue;
    private BigDecimal shortage;
    private BigDecimal surplus;
    private BigDecimal avgExceeded;
    private String variant;
}
