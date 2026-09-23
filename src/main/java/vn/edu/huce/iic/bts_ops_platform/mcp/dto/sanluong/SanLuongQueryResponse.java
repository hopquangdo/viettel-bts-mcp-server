package vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.OutputSummaryDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PeriodInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PeriodTrendDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.ProgressSummaryDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.RankingOverviewDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.TrendInfo;

/**
 * Overview tổng quan module Sản lượng cho chatbot/AI.
 * DTO chỉ chứa dữ liệu nghiệp vụ đã tổng hợp — LLM chịu trách nhiệm biến dữ liệu thành câu trả lời tự nhiên.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanLuongQueryResponse {

    private PeriodInfo period;
    private OutputSummaryDto summary;
    private ProgressSummaryDto progress;
    private TrendInfo trend;
    private PeriodTrendDto<SanLuongPeriodTrendDto> periodTrend;
    private RankingOverviewDto nhaThau;
    private RankingOverviewDto khuVuc;
    private RankingOverviewDto doiTuong;
    private RankingOverviewDto hopDong;
    /** Xếp hạng theo tỉnh (khoá ngoại tinh_thanh_id của đối tượng); bỏ khi đã lọc theo tỉnh hoặc theo 1 đối tượng cụ thể. */
    private RankingOverviewDto tinh;
    /** Xếp hạng theo loại hợp đồng (lĩnh vực); bỏ khi đã lọc theo loại hợp đồng hoặc theo 1 đối tượng cụ thể. */
    private RankingOverviewDto loaiHopDong;
    /** Nhà thầu đang phụ trách đối tượng hoạt động nhưng chưa có sản lượng 'done' trong [fromDate, toDate] — luôn có, phân trang theo page/pageSize. */
    private PagedResult<SanLuongNhaThauItem> nhaThauChuaBaoTrongKy;
    /** Nhà thầu đang phụ trách đối tượng hoạt động và đã có sản lượng 'done' trong [fromDate, toDate] — luôn có, phân trang theo page/pageSize. */
    private PagedResult<SanLuongNhaThauItem> nhaThauDaBaoTrongKy;
    /** Nghiệm thu sản lượng: số hạng mục đạt/không đạt và danh sách không đạt kèm lý do (lọc ngày theo ngày nghiệm thu) — luôn có. */
    private SanLuongNghiemThuDto nghiemThu;
    /** Đối tượng (trạm, tuyến…) chưa ghi nhận sản lượng hoàn thành nào trong khoảng ngày, theo bộ lọc; phân trang theo page/pageSize — luôn có. */
    private PagedResult<SanLuongDoiTuongChuaCoItem> doiTuongChuaCoSanLuong;
    /** Danh sách từng đối tượng (SL tổng, SL hôm nay, thi công gần nhất, tỉnh, tiến độ…) — luôn có; phân trang theo page/pageSize. */
    private PagedResult<SanLuongDoiTuongItem> danhSachDoiTuong;
    /** Hạng mục đã làm/chưa làm của từng đối tượng cụ thể (tối đa 5) — chỉ có khi tham số doiTuong là mã/id đối tượng cụ thể (trạm, tuyến…), không phải loại đối tượng. */
    private java.util.List<SanLuongHangMucDoiTuongDto> hangMucDoiTuong;
}
