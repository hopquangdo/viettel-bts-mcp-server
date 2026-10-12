package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang;

import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankedEntityDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankingOverviewDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.DoiTuongChuaCoProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.NumberUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.RankingBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Xếp hạng đối tượng: 20 đối tượng đầu (đã sắp theo giá trị ở DB); {@code totalEntities} là tổng số đối tượng. */
@Component
public class SanLuongDoiTuongXepHangStrategy implements SanLuongXepHangStrategy {

    @Override
    public SanLuongXepHangChieu chieu() {
        return SanLuongXepHangChieu.DOI_TUONG;
    }

    @Override
    public RankingOverviewDto rank(SanLuongXepHangContext ctx) {
        List<RankedEntityDto> unranked = ctx.topDoiTuong().stream()
                .map(row -> {
                    int total = NumberUtil.nz(row.getTotalHangMuc());
                    BigDecimal completionRate = BigDecimal.ZERO;
                    if (total > 0) {
                        completionRate = NumberUtil.percent(NumberUtil.nz(row.getDoneHangMuc()), total, 4)
                                .divide(BigDecimal.ONE, 1, RoundingMode.HALF_UP);
                    }
                    // doiTuongTen là tên loại đối tượng ("Trạm") → ưu tiên mã khoá chính (mã trạm/tuyến) để phân biệt từng dòng.
                    String ma = row instanceof DoiTuongChuaCoProjection c ? c.getMaDoiTuong() : null;
                    String ten = ma != null && !ma.isBlank() ? ma : row.getDoiTuongTen();
                    return new RankedEntityDto(row.getDoiTuongId().toString(), ten,
                            NumberUtil.nz(row.getPeriodValue()), completionRate, 1,
                            NumberUtil.isTrue(row.getHasOpenVuongMac()) ? 1 : 0, null);
                })
                .toList();
        return RankingBuilder.build(unranked, 20, ctx.soDoiTuong());
    }
}
