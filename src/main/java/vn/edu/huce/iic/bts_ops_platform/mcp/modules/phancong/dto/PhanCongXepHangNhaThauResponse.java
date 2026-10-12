package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** phancong_xep_hang_nha_thau: top nhà thầu phụ trách nhiều đối tượng; không có khi lọc nhaThau hoặc 1 đối tượng cụ thể. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PhanCongXepHangNhaThauResponse(List<PhanCongXepHangNhaThauToolItem> xepHangNhaThau) {
}
