package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.query.SanLuongProgressGroupQuery;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.response.SanLuongProgressSummaryResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Pure progress calculations for grouped production objects. */
public final class SanLuongCalc {

    public SanLuongProgressSummaryResponse tienDo(List<SanLuongProgressGroupQuery> groups, double nguongHoanThanhThap) {
        long completed = 0;
        long inProgress = 0;
        long notStarted = 0;
        long lowCompletion = 0;
        BigDecimal weightedRateSum = BigDecimal.ZERO;
        long counted = 0;
        BigDecimal threshold = BigDecimal.valueOf(nguongHoanThanhThap);

        if (groups != null) {
            for (SanLuongProgressGroupQuery group : groups) {
                if (group == null) {
                    continue;
                }
                int total = nz(group.tongHangMuc());
                long groupSize = nz(group.soDoiTuong());
                if (total <= 0 || groupSize <= 0) {
                    continue;
                }
                int done = nz(group.hangMucHoanThanh());
                BigDecimal rate = percent(done, total, 4);
                weightedRateSum = weightedRateSum.add(rate.multiply(BigDecimal.valueOf(groupSize)));
                counted += groupSize;

                if (done >= total) {
                    completed += groupSize;
                } else if (done == 0) {
                    notStarted += groupSize;
                } else {
                    inProgress += groupSize;
                    if (rate.compareTo(threshold) < 0) {
                        lowCompletion += groupSize;
                    }
                }
            }
        }

        BigDecimal completionRate = counted > 0
                ? weightedRateSum.divide(BigDecimal.valueOf(counted), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new SanLuongProgressSummaryResponse(completionRate, completed, inProgress, notStarted, lowCompletion);
    }

    private static BigDecimal percent(int done, int total, int scale) {
        return BigDecimal.valueOf(done)
                .divide(BigDecimal.valueOf(total), scale + 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(scale, RoundingMode.HALF_UP);
    }

    private static int nz(Integer value) {
        return value != null ? value : 0;
    }

    private static long nz(Long value) {
        return value != null ? value : 0L;
    }
}