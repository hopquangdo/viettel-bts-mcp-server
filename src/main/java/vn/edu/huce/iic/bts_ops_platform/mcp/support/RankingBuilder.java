package vn.edu.huce.iic.bts_ops_platform.mcp.support;

import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.RankedEntityDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.RankingOverviewDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sort theo value giảm dần + gán rank (1-based) + tính top/bottom by completion — dùng chung cho
 * mọi tool cần xếp hạng 1 nhóm thực thể (nhà thầu, hợp đồng, đối tượng, khu vực...).
 * Input là danh sách {@link RankedEntityDto} CHƯA sort/CHƯA gán rank (rank field bỏ qua, có thể null);
 * việc gom nhóm/aggregate theo entity vẫn do caller tự làm vì phụ thuộc projection riêng từng repository.
 */
public final class RankingBuilder {

    private RankingBuilder() {
    }

    public static RankingOverviewDto build(List<RankedEntityDto> unranked, int topLimit) {
        return build(unranked, topLimit, unranked.size());
    }

    /**
     * Xếp hạng theo chỉ số tuỳ chọn: {@code sortKey} = giaTri (mặc định) | hoanThanh | soDoiTuong | vuongMac; {@code asc} = tăng dần.
     * Đồng hạng theo tên rồi id. {@code rank} gán theo vị trí sau khi sắp.
     */
    public static RankingOverviewDto build(List<RankedEntityDto> unranked, int topLimit, String sortKey, boolean asc) {
        Comparator<RankedEntityDto> primary = switch (sortKey == null ? "giaTri" : sortKey) {
            case "hoanThanh" -> Comparator.comparing(RankedEntityDto::completionRate, Comparator.nullsFirst(Comparator.naturalOrder()));
            case "soDoiTuong" -> Comparator.comparingInt(RankedEntityDto::objectCount);
            case "vuongMac" -> Comparator.comparingInt(RankedEntityDto::issueCount);
            default -> Comparator.comparing(RankedEntityDto::value);
        };
        if (!asc) {
            primary = primary.reversed();
        }
        List<RankedEntityDto> sorted = unranked.stream()
                .sorted(primary
                        .thenComparing(RankedEntityDto::name, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(r -> r.id() == null ? null : r.id().toString(), Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(topLimit)
                .toList();
        return overview(sorted, unranked.size());
    }

    private static RankingOverviewDto overview(List<RankedEntityDto> sorted, long totalEntities) {
        List<RankedEntityDto> ranked = new ArrayList<>(sorted.size());
        for (int i = 0; i < sorted.size(); i++) {
            RankedEntityDto r = sorted.get(i);
            ranked.add(new RankedEntityDto(r.id(), r.name(), r.value(), r.completionRate(), r.objectCount(), r.issueCount(), i + 1));
        }
        RankedEntityDto topByOutput = ranked.stream().max(Comparator.comparing(RankedEntityDto::value)).orElse(null);
        RankedEntityDto topByCompletion = ranked.stream()
                .filter(r -> r.completionRate() != null)
                .max(Comparator.comparing(RankedEntityDto::completionRate)).orElse(null);
        RankedEntityDto bottomByCompletion = ranked.stream()
                .filter(r -> r.completionRate() != null)
                .min(Comparator.comparing(RankedEntityDto::completionRate)).orElse(null);
        return RankingOverviewDto.builder()
                .totalEntities((int) totalEntities)
                .topByOutput(topByOutput)
                .topByCompletion(topByCompletion)
                .bottomByCompletion(bottomByCompletion)
                .rankings(ranked)
                .build();
    }

    /** {@code totalEntities}: tổng số thực thể khi {@code unranked} mới chỉ là phần đầu đã được chọn sẵn (vd top 20 lấy từ DB). */
    public static RankingOverviewDto build(List<RankedEntityDto> unranked, int topLimit, long totalEntities) {
        List<RankedEntityDto> sorted = unranked.stream()
                // đồng hạng: theo tên rồi id để kết quả xác định (trước đây phụ thuộc thứ tự dòng DB)
                .sorted(Comparator.comparing(RankedEntityDto::value).reversed()
                        .thenComparing(RankedEntityDto::name, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(r -> r.id() == null ? null : r.id().toString(), Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(topLimit)
                .toList();

        List<RankedEntityDto> ranked = new ArrayList<>(sorted.size());
        for (int i = 0; i < sorted.size(); i++) {
            RankedEntityDto r = sorted.get(i);
            ranked.add(new RankedEntityDto(r.id(), r.name(), r.value(), r.completionRate(), r.objectCount(), r.issueCount(), i + 1));
        }

        RankedEntityDto topByOutput = ranked.stream().findFirst().orElse(null);
        RankedEntityDto topByCompletion = ranked.stream()
                .filter(r -> r.completionRate() != null)
                .max(Comparator.comparing(RankedEntityDto::completionRate)).orElse(null);
        RankedEntityDto bottomByCompletion = ranked.stream()
                .filter(r -> r.completionRate() != null)
                .min(Comparator.comparing(RankedEntityDto::completionRate)).orElse(null);

        return RankingOverviewDto.builder()
                .totalEntities((int) totalEntities)
                .topByOutput(topByOutput)
                .topByCompletion(topByCompletion)
                .bottomByCompletion(bottomByCompletion)
                .rankings(ranked)
                .build();
    }
}
