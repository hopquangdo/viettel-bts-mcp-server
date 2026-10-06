package vn.edu.huce.iic.bts_ops_platform.mcp.common.dto;

import lombok.Builder;
import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.BienBanTongQuanToolResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTongQuanToolResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongTongQuanToolResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongTongQuanToolResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.dto.TramTonTongQuanToolResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacTongQuanResponse;

/**
 * Tổng quan gộp 7 module trong 1 lần gọi — trả về cho tool system_overview_tool. Mỗi khối đúng bằng kết quả tool
 * {@code <module>_tong_quan} tương ứng: chỉ số liệu tổng hợp, không có danh sách hay bảng xếp hạng.
 */
@Data
@Builder
public class SystemOverviewResponse {
    private HopDongTongQuanToolResponse hopDong;
    private SanLuongTongQuanToolResponse sanLuong;
    private PhanCongTongQuanToolResponse phanCong;
    private TramTonTongQuanToolResponse tramTon;
    private VuongMacTongQuanResponse vuongMac;
    private BienBanTongQuanToolResponse bienBan;
    private NguonViecTongQuanResponse nguonViec;
}
