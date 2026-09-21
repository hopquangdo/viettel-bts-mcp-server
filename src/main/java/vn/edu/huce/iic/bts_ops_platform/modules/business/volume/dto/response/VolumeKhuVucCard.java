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
public class VolumeKhuVucCard {
    private String id;
    private String name;
    private String color;
    private BigDecimal usedPercent;
    private String barColor;
    private BigDecimal contractValue;
    private BigDecimal constructionValue;
    private BigDecimal remainingValue;
    /** Chênh lệch có dấu = constructionValue - contractValue (âm = thiếu, dương = thừa). */
    private BigDecimal chenhLech;
    private BigDecimal avgPerStation;
    private long quotaStations;
    private long shortageStations;
    private long surplusStations;
}
