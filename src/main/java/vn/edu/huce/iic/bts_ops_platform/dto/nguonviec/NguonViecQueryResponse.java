package vn.edu.huce.iic.bts_ops_platform.dto.nguonviec;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.common.dto.RankedItemResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.common.PagedResult;

import java.util.List;

/** Kết quả tool hợp nhất nguonviec_tool. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NguonViecQueryResponse {
    private NguonViecSummaryToolDto summary;
    private NguonViecTabCountsToolDto tabCounts;
    private PagedResult<NguonViecRowToolItem> danhSach;
    private List<RankedItemResponse> xepHangRuiRo;
    /** Xếp hạng trung tâm theo sản lượng thực hiện (sxTong, tỷ đồng) — luôn có, không lọc theo trungTam. */
    private List<RankedItemResponse> xepHangTrungTam;
    /** Nguồn việc sắp hết giá trị được giao (slConHD/giaTriHD thấp, giaTriHD > 0) — luôn có, tối đa 10 dòng. */
    private List<RankedItemResponse> sapHetGiaTri;
}
