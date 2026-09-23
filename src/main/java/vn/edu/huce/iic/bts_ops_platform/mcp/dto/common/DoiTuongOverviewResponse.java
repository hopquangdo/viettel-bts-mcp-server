package vn.edu.huce.iic.bts_ops_platform.mcp.dto.common;

import lombok.Builder;
import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.bienban.BienBanQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong.HopDongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong.HoSoDoiTuongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.phancong.PhanCongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.SanLuongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.tramton.TramTonQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.vuongmac.VuongMacQueryResponse;

/**
 * Hồ sơ tổng hợp của MỘT đối tượng (trạm/tuyến) cụ thể, gộp 6 module theo bộ lọc doiTuong — trả về
 * cho tool doituong_tool. Mỗi khối giữ nguyên cấu trúc response gốc của tool tương ứng, chỉ khác là
 * đã lọc sẵn theo đối tượng nên các bảng xếp hạng/tổng hợp toàn hệ thống thường rỗng hoặc null.
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
