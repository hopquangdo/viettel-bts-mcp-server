package vn.edu.huce.iic.bts_ops_platform.mcp.handler;

import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhatky.NhatKyQueryResponse;

import java.time.LocalDate;

/** Contract logic cho tool nhật ký thao tác (ai làm gì, lúc nào) — 1 method duy nhất. */
public interface NhatKyToolHandler {

    NhatKyQueryResponse query(String doiTuong, String hopDong, String hanhDong, String nguoiThucHien,
                              LocalDate fromDate, LocalDate toDate, String thuTu, Integer page, Integer pageSize);
}
