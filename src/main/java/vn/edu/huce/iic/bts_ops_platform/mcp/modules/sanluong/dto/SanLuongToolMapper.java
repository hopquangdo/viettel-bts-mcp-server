package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PeriodInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankedEntityDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankingOverviewDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpFormat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** DTO nội bộ module Sản lượng -> DTO trả cho LLM (tên trường, nhãn tiếng Việt, tiền/%/ngày đã định dạng). */
public final class SanLuongToolMapper {

    private static final String KHONG_ID = new UUID(0L, 0L).toString();

    private SanLuongToolMapper() {
    }

    /** Tiêu chí sắp xếp đọc được: "Giá trị sản lượng, cao xuống thấp". */
    public static String tieuChi(String khoaXepHang, boolean tangDan) {
        String ten = switch (khoaXepHang == null ? "giaTri" : khoaXepHang) {
            case "hoanThanh" -> "Tỷ lệ hoàn thành hạng mục";
            case "soDoiTuong" -> "Số đối tượng";
            case "vuongMac" -> "Số đối tượng có vướng mắc đang mở";
            default -> "Giá trị sản lượng";
        };
        return ten + (tangDan ? ", thấp lên cao" : ", cao xuống thấp");
    }

    public static SanLuongXepHangResponse xepHang(SanLuongXepHangChieu chieu, PeriodInfo period, String khoaXepHang,
                                                  boolean tangDan, RankingOverviewDto ranking, String ghiChu) {
        String ky = period != null ? McpFormat.ky(period.fromDate(), period.toDate()) : null;
        if (ranking == null) {
            return new SanLuongXepHangResponse(ky, chieu.label(), tieuChi(khoaXepHang, tangDan), null, null, null, null, null, ghiChu);
        }
        boolean coSoDoiTuong = chieu != SanLuongXepHangChieu.DOI_TUONG;
        List<RankedEntityDto> rows = ranking.getRankings() != null ? ranking.getRankings() : List.of();
        return new SanLuongXepHangResponse(
                ky,
                chieu.label(),
                tieuChi(khoaXepHang, tangDan),
                ranking.getTotalEntities(),
                item(ranking.getTopByOutput(), coSoDoiTuong),
                item(ranking.getTopByCompletion(), coSoDoiTuong),
                item(ranking.getBottomByCompletion(), coSoDoiTuong),
                rows.stream().map(r -> item(r, coSoDoiTuong)).toList(),
                ghiChu);
    }

    static SanLuongXepHangItem item(RankedEntityDto r, boolean coSoDoiTuong) {
        if (r == null) {
            return null;
        }
        boolean coId = r.id() != null && !KHONG_ID.equals(r.id());
        String ten = r.name() != null && !r.name().isBlank() ? r.name() : "Chưa xác định";
        return new SanLuongXepHangItem(
                r.rank(),
                coId ? r.id() : null,
                ten,
                McpFormat.tien(r.value()),
                McpFormat.phanTram(r.completionRate()),
                coSoDoiTuong ? r.objectCount() : null,
                r.issueCount());
    }

    /** % tăng trưởng kiểu Dashboard (DashboardTinhToanHelper.growthPercent): kỳ trước 0 -> 0% hoặc +100%; null khi không có số. */
    public static SanLuongTongQuanToolResponse tongQuan(SanLuongTongQuanResponse tq, double nguongHoanThanhThap) {
        var sum = tq.summary();
        var pr = tq.progress();
        var tr = tq.trend();
        SanLuongTongQuanToolResponse.SoVoiKyTruoc soVoi = tr == null ? null : new SanLuongTongQuanToolResponse.SoVoiKyTruoc(
                tr.comparisonLabel(),
                McpFormat.tien(tr.previousValue()),
                tr.growthPercent() == null ? null : (tr.growthPercent().signum() > 0 ? "+" : "") + McpFormat.phanTram(tr.growthPercent()),
                xuHuong(tr.direction()));
        return new SanLuongTongQuanToolResponse(
                tq.period() != null ? McpFormat.ky(tq.period().fromDate(), tq.period().toDate()) : null,
                sum.getTotalObjects(),
                sum.getObjectsWithOutput(),
                sum.getObjectsWithoutOutput(),
                McpFormat.tien(sum.getPeriodValue()),
                McpFormat.tien(sum.getTodayValue()),
                McpFormat.tien(sum.getAverageValuePerObject()),
                sum.getOpenIssueCount(),
                McpFormat.phanTram(pr.getCompletionRate()),
                pr.getCompletedCount(),
                pr.getInProgressCount(),
                pr.getNotStartedCount(),
                pr.getLowCompletionCount(),
                McpFormat.phanTram(BigDecimal.valueOf(nguongHoanThanhThap)),
                soVoi);
    }

    static String xuHuong(String direction) {
        if (direction == null) {
            return null;
        }
        return switch (direction) {
            case "UP" -> "Tăng";
            case "DOWN" -> "Giảm";
            case "STABLE" -> "Ổn định";
            default -> "Chưa có dữ liệu kỳ trước";
        };
    }

    public static SanLuongNhaThauBaoCaoResponse nhaThauBaoCao(SanLuongTrangThaiBaoCao trangThai, PeriodInfo period,
                                                              PagedResult<SanLuongNhaThauItem> trang) {
        List<SanLuongNhaThauBaoCaoItem> items = trang.items().stream()
                .map(i -> new SanLuongNhaThauBaoCaoItem(
                        i.getNhaThau() != null && i.getNhaThau().id() != null ? i.getNhaThau().id().toString() : null,
                        i.getNhaThau() != null ? i.getNhaThau().ten() : null,
                        i.getSoDoiTuongPhuTrach(),
                        trangThai == SanLuongTrangThaiBaoCao.DA_BAO ? McpFormat.tien(i.getGiaTriDaBao()) : null))
                .toList();
        String ghiChu = trang.totalItems() == 0
                ? "Không có nhà thầu nào ở trạng thái \"" + trangThai.label().toLowerCase() + "\" trong kỳ và phạm vi đã lọc."
                : null;
        return new SanLuongNhaThauBaoCaoResponse(
                period != null ? McpFormat.ky(period.fromDate(), period.toDate()) : null,
                trangThai.label(),
                trang.totalItems(),
                trang.page(),
                trang.pageSize(),
                trang.totalPages(),
                items,
                ghiChu);
    }
}
