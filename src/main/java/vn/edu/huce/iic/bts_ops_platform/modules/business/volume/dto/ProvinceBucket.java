package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Bucketing nội bộ khi aggregate volume theo tỉnh/vùng. */
public final class ProvinceBucket {
    public final String region;
    public final String province;
    public final String provinceKey;
    public final String oldProvince;
    public final String contractor;
    public long stations;
    public long stationsWithOutput;
    public long shortageStations;
    public long surplusStations;
    public BigDecimal contractValue = BigDecimal.ZERO;
    public BigDecimal constructionValue = BigDecimal.ZERO;
    /** Tổng thiếu chỉ từ trạm đã có SL (plan − SL). */
    public BigDecimal shortageValue = BigDecimal.ZERO;
    /** Tổng thừa chỉ từ trạm đã có SL vượt định mức. */
    public BigDecimal surplusValue = BigDecimal.ZERO;
    /** Mã đối tượng thiếu SL (để truy vết trạm nào ở tỉnh nào). */
    public final List<String> shortageCodes = new ArrayList<>();
    /** Mã đối tượng vượt ngưỡng / thừa SL. */
    public final List<String> surplusCodes = new ArrayList<>();

    public ProvinceBucket(
            String region, String province, String provinceKey, String oldProvince, String contractor) {
        this.region = region;
        this.province = province;
        this.provinceKey = provinceKey;
        this.oldProvince = oldProvince;
        this.contractor = contractor;
    }
}
