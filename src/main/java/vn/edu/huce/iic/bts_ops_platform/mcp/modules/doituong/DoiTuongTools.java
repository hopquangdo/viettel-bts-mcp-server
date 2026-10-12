package vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituong;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.McpToolGroup;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.ToolParamDescriptions;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.DoiTuongOverviewResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.BienBanService;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.DoiTuongTonService;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hosodoituong.HoSoDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.HopDongService;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.PhanCongService;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.SanLuongService;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.VuongMacService;

import java.time.LocalDate;

@Service
public final class DoiTuongTools implements McpToolGroup {

    private final HopDongService hopDongService;
    private final SanLuongService sanLuongService;
    private final PhanCongService phanCongService;
    private final DoiTuongTonService doiTuongTonService;
    private final VuongMacService vuongMacService;
    private final BienBanService bienBanService;
    private final HoSoDoiTuongService hoSoDoiTuongService;

    public DoiTuongTools(
            HopDongService hopDongService,
            SanLuongService sanLuongService,
            PhanCongService phanCongService,
            DoiTuongTonService doiTuongTonService,
            VuongMacService vuongMacService,
            BienBanService bienBanService,
            HoSoDoiTuongService hoSoDoiTuongService) {
        this.hopDongService = hopDongService;
        this.sanLuongService = sanLuongService;
        this.phanCongService = phanCongService;
        this.doiTuongTonService = doiTuongTonService;
        this.vuongMacService = vuongMacService;
        this.bienBanService = bienBanService;
        this.hoSoDoiTuongService = hoSoDoiTuongService;
    }

    @Tool(
            name = "doituong_tool",
            description = """
                    Chi tiết đối tượng (trạm/tuyến) toàn cảnh, hoặc tổng quan hồ sơ triển khai nhiều đối tượng
                    Dùng khi: trạm X đang thế nào; tuyến X còn vướng gì; trạm X đã bàn giao mặt bằng chưa; trạm X khởi công ngày nào; tháng này bao nhiêu đối tượng khởi công; hợp đồng X còn bao nhiêu trạm chưa khảo sát
                    Trả về (truyền doiTuong, 1 trạm/tuyến): hopDong (tiến độ, theoBuoc), sanLuong (hangMucDoiTuong, danhSachDoiTuong), tramTon (đang tồn hay không, lý do), vuongMac (danhSach), phanCong (đang giao cho ai), bienBan (của hợp đồng chứa đối tượng), hoSoDoiTuong.thuocTinh (bàn giao mặt bằng, vật tư A/B, ngayKhoiCong -- null là chưa khởi công).
                    Trả về (không truyền doiTuong, lọc hopDong/nhaThau/khuVuc/tinhThanh/trangThaiHopDong/coNhomUuTien): chỉ hoSoDoiTuong gồm tongQuan (số đối tượng thiếu bàn giao mặt bằng/vật tư, cần có bộ lọc), chuaKhaoSat (chưa bàn giao mặt bằng, cần có bộ lọc), khaoSatXongChuaCoSanLuong (đã bàn giao mặt bằng nhưng chưa có sản lượng; fromDate/toDate lọc theo ngày bàn giao), khoiCongTrongKy (khởi công trong kỳ; fromDate/toDate lọc theo ngày khởi công; tổng số = khoiCongTrongKy.totalItems).
                    Lưu ý: ngày khởi công = ngày ghi nhận sản lượng hoàn thành (done) đầu tiên của đối tượng trong hợp đồng.
                    Không dùng khi: sản lượng, tồn đọng, vướng mắc, phân công của NHIỀU đối tượng hoặc xếp hạng -> tool của mảng tương ứng.
                    """
    )
    public DoiTuongOverviewResponse doiTuong(
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG + ". Bỏ trống để xem hồ sơ nhiều đối tượng (chế độ 2)", required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU + ". Chỉ áp dụng khi không truyền doiTuong", required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC + ". Chỉ áp dụng khi không truyền doiTuong", required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH + ". Chỉ áp dụng khi không truyền doiTuong", required = false) String tinhThanh,
            @ToolParam(description = "Bước trạng thái của đối tượng trong hợp đồng cần lọc: ID, mã hoặc tên (ví dụ Khảo sát, Thi công). Chỉ áp dụng khi không truyền doiTuong", required = false) String trangThaiHopDong,
            @ToolParam(description = "true: chỉ đối tượng thuộc nhóm ưu tiên; false: chỉ đối tượng không thuộc nhóm ưu tiên; bỏ trống: tất cả. Chỉ áp dụng khi không truyền doiTuong", required = false) Boolean coNhomUuTien,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = ToolParamDescriptions.PAGE + ". Chỉ áp dụng khi không truyền doiTuong", required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE + ". Chỉ áp dụng khi không truyền doiTuong", required = false) Integer pageSize) {
        if (doiTuong == null || doiTuong.isBlank()) {
            return DoiTuongOverviewResponse.builder()
                    .hoSoDoiTuong(hoSoDoiTuongService.query(null, hopDong, nhaThau, khuVuc, tinhThanh, trangThaiHopDong, coNhomUuTien, fromDate, toDate, page, pageSize))
                    .build();
        }
        return DoiTuongOverviewResponse.builder()
                .hopDong(hopDongService.query(doiTuong, null, null, null, null, null, null, null, null, null, null, null, null, null, fromDate, toDate, null))
                .sanLuong(sanLuongService.query(doiTuong, null, null, null, null, fromDate, toDate, null, null, null, null, null, null, null, null))
                .phanCong(phanCongService.query(null, null, null, null, null, null, null, doiTuong, null, null, null, null, null, fromDate, toDate))
                .tramTon(doiTuongTonService.query(doiTuong, null, null, null, null, null, null, null, null, null, null, null, null, fromDate, toDate))
                .vuongMac(vuongMacService.query(doiTuong, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, fromDate, toDate))
                .bienBan(bienBanService.query(null, null, null, null, null, doiTuong, fromDate, toDate, null, null))
                .hoSoDoiTuong(hoSoDoiTuongService.query(doiTuong, hopDong, null, null, null, null, null, fromDate, toDate, null, null))
                .build();
    }
}