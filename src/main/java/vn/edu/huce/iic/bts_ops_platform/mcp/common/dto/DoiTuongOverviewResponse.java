package vn.edu.huce.iic.bts_ops_platform.mcp.common.dto;

import lombok.Builder;
import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.BienBanQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hosodoituong.dto.HoSoDoiTuongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.dto.TramTonQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacQueryResponse;

/**
 * Kết quả tool doituong_tool. Có doiTuong: toàn cảnh MỘT đối tượng (trạm/tuyến), gộp 7 module đã lọc
 * sẵn theo đối tượng nên các bảng xếp hạng/tổng hợp toàn hệ thống thường rỗng hoặc null. Không có
 * doiTuong: chỉ có hoSoDoiTuong (hồ sơ/mốc triển khai nhiều đối tượng theo bộ lọc), các khối khác null.
 */
@Data
@Builder
public class DoiTuongOverviewResponse {
    private HopDongQueryResponse hopDong;
    private SanLuongQueryResponse sanLuong;
    private PhanCongQueryResponse phanCong;
    private TramTonQueryResponse tramTon;
    private VuongMacQueryResponse vuongMac;
    private BienBanQueryResponse bienBan;
    private HoSoDoiTuongQueryResponse hoSoDoiTuong;
}
