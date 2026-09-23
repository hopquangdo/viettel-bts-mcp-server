package vn.edu.huce.iic.bts_ops_platform.mcp.dto.common;

import lombok.Builder;
import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.bienban.BienBanQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong.HopDongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec.NguonViecQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.phancong.PhanCongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.SanLuongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.tramton.TramTonQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.vuongmac.VuongMacQueryResponse;

/** Tổng quan gộp TOÀN BỘ 7 module trong 1 lần gọi — trả về cho tool system_overview_tool. */
@Data
@Builder
public class SystemOverviewResponse {
    private HopDongQueryResponse hopDong;
    private SanLuongQueryResponse sanLuong;
    private PhanCongQueryResponse phanCong;
    private TramTonQueryResponse tramTon;
    private VuongMacQueryResponse vuongMac;
    private BienBanQueryResponse bienBan;
    private NguonViecQueryResponse nguonViec;
}
