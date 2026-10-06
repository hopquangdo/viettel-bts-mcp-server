package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 1 nhà thầu trong sanluong_nha_thau_bao_cao. {@code id} giữ lại vì tham số nhaThau của các tool nhận ID.
 *
 * @param giaTriDaBao "820 triệu đồng"; chỉ có khi trạng thái là đã báo
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SanLuongNhaThauBaoCaoItem(
        String id,
        String ten,
        long soDoiTuongPhuTrach,
        String giaTriDaBao) {
}
