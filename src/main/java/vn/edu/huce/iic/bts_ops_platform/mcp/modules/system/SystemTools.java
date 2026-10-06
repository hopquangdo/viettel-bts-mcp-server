package vn.edu.huce.iic.bts_ops_platform.mcp.modules.system;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.McpToolGroup;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.ToolParamDescriptions;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.BienBanFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.SystemOverviewResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.dto.TramTonFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.request.VuongMacFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.BienBanService;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.DoiTuongTonService;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.HopDongService;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.NguonViecService;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.PhanCongService;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.SanLuongService;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.VuongMacService;

import java.time.LocalDate;

@Service
public final class SystemTools implements McpToolGroup {

        private final SanLuongService sanLuongService;
        private final HopDongService hopDongService;
        private final DoiTuongTonService doiTuongTonService;
        private final VuongMacService vuongMacService;
        private final PhanCongService phanCongService;
        private final BienBanService bienBanService;
        private final NguonViecService nguonViecService;

    public SystemTools(
                        SanLuongService sanLuongService,
                        HopDongService hopDongService,
                        DoiTuongTonService doiTuongTonService,
                        VuongMacService vuongMacService,
                        PhanCongService phanCongService,
                        BienBanService bienBanService,
                        NguonViecService nguonViecService) {
                this.sanLuongService = sanLuongService;
                this.hopDongService = hopDongService;
                this.doiTuongTonService = doiTuongTonService;
                this.vuongMacService = vuongMacService;
                this.phanCongService = phanCongService;
                this.bienBanService = bienBanService;
                this.nguonViecService = nguonViecService;
    }

    @Tool(
            name = "system_overview_tool",
            description = """
                    Tổng quan toàn hệ thống gộp hợp đồng, sản lượng, phân công, tồn, vướng mắc, biên bản, nguồn việc
                    Dùng khi: tình hình chung toàn hệ thống; bức tranh tổng thể các mảng; tổng quan chung của khu vực X
                    Trả về: hopDong, sanLuong, phanCong, tramTon, vuongMac, bienBan, nguonViec; mỗi khối giống tool <module>_tong_quan tương ứng.
                    Không dùng khi: cần danh sách hoặc xếp hạng -> tool của module đó.
                    """
    )
    public SystemOverviewResponse systemOverview(
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong) {
        // Gọi tuần tự: mỗi tongQuan tự chạy truy vấn song song qua McpParallel, không lồng async (xem McpParallel).
        // Khoảng ngày chỉ áp cho sản lượng như trước; nguồn việc lấy kỳ mặc định của module (cả năm hiện tại).
        return SystemOverviewResponse.builder()
                .hopDong(hopDongService.tongQuan(new HopDongFilter(null, hopDong, nhaThau, khuVuc, tinhThanh, null, null), null, null, null))
                .sanLuong(sanLuongService.tongQuan(new SanLuongFilter(null, hopDong, nhaThau, khuVuc, tinhThanh, null, fromDate, toDate), null))
                .phanCong(phanCongService.tongQuan(new PhanCongFilter(null, nhaThau, hopDong, khuVuc, tinhThanh, null, null, null, null)))
                .tramTon(doiTuongTonService.tongQuan(new TramTonFilter(null, hopDong, nhaThau, khuVuc, tinhThanh, null), null, null, null))
                .vuongMac(vuongMacService.tongQuan(new VuongMacFilter(null, hopDong, nhaThau, khuVuc, tinhThanh, null, null, null,
                        null, null, null, null, null), null))
                .bienBan(bienBanService.tongQuan(new BienBanFilter(khuVuc, tinhThanh, hopDong, nhaThau, null, null, null)))
                .nguonViec(nguonViecService.tongQuan(new NguonViecFilter(khuVuc, null, null, null, null, null, null, null, null)))
                .build();
    }
}