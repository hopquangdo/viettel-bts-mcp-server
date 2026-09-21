package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeProvinceAlert {
    private String id;
    private String province;
    private String region;
    private long stationCount;
    private BigDecimal volumeValue;
    private BigDecimal avgValue;
    private String type;
    /** Mã đối tượng cảnh báo trong tỉnh — để truy vết trạm nào. */
    @Builder.Default
    private List<String> stationCodes = new ArrayList<>();
}
