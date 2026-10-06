package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankedEntityDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankingOverviewDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.ProgressGroupProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.NumberUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.RankingBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Khung chung cho các chiều gom theo khoá của nhóm đối tượng (nhà thầu, khu vực, tỉnh, hợp đồng, loại hợp đồng). */
public abstract class SanLuongNhomXepHangStrategy implements SanLuongXepHangStrategy {

    /** Khoá gom; null = bỏ nhóm đó. */
    protected abstract UUID khoa(ProgressGroupProjection g);

    protected abstract String ten(ProgressGroupProjection g);

    /** Số dòng tối đa của bảng. */
    protected int topLimit() {
        return Integer.MAX_VALUE;
    }

    /** Xếp hạng theo khoá từ các nhóm đối tượng; mỗi nhóm tính {@code n} lần. */
    @Override
    public RankingOverviewDto rank(SanLuongXepHangContext ctx) {
        record Acc(String name, BigDecimal[] output, long[] objectCount, long[] issueCount, BigDecimal[] rateSum,
                   long[] rateCounted) {
        }
        Map<UUID, Acc> byKey = new LinkedHashMap<>();
        for (var g : ctx.groups()) {
            UUID key = khoa(g);
            if (key == null) {
                continue;
            }
            long n = g.getN();
            BigDecimal weight = BigDecimal.valueOf(n);
            Acc acc = byKey.computeIfAbsent(key, id -> new Acc(
                    ten(g), new BigDecimal[]{BigDecimal.ZERO}, new long[]{0}, new long[]{0}, new BigDecimal[]{BigDecimal.ZERO}, new long[]{0}));
            acc.output()[0] = acc.output()[0].add(NumberUtil.nz(g.getPeriodValue()).multiply(weight));
            acc.objectCount()[0] += n;
            if (NumberUtil.isTrue(g.getHasOpenVuongMac())) {
                acc.issueCount()[0] += n;
            }
            int total = NumberUtil.nz(g.getTotalHangMuc());
            if (total > 0) {
                int done = NumberUtil.nz(g.getDoneHangMuc());
                acc.rateSum()[0] = acc.rateSum()[0].add(NumberUtil.percent(done, total, 4).multiply(weight));
                acc.rateCounted()[0] += n;
            }
        }

        List<RankedEntityDto> unranked = byKey.entrySet().stream()
                .map(entry -> {
                    Acc acc = entry.getValue();
                    BigDecimal completionRate = acc.rateCounted()[0] > 0
                            ? acc.rateSum()[0].divide(BigDecimal.valueOf(acc.rateCounted()[0]), 1, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return new RankedEntityDto(entry.getKey().toString(), acc.name(), acc.output()[0], completionRate,
                            (int) acc.objectCount()[0], (int) acc.issueCount()[0], null);
                })
                .toList();

        return RankingBuilder.build(unranked, topLimit(), ctx.khoaXepHang(), ctx.tangDan());
    }
}
