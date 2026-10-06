package vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume;

import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.McpToolGroup;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.ToolParamDescriptions;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoTramVuotNguongResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoTongQuanToolResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoChiTietHopDongToolDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoDanhSachHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoKhuVucToolDto;

/**
 * MCP tools for volume budget queries. Each tool returns only its own result block.
 */
@Service
@RequiredArgsConstructor
public class VolumeTools implements McpToolGroup {

    private static final String HOP_DONG_TIM = "Mã hoặc tên (một phần) hợp đồng cần tìm; khớp nhiều hợp đồng thì trả tất cả";
    private static final String HE_SO = "Hệ số ngưỡng GCCC dùng để tính ngân sách, ghi đè hệ số mặc định trong cấu hình (thường 2.0)";
    private static final String NGUONG_CANH_BAO = "Ngưỡng % sử dụng ngân sách từ mức này trở lên là cảnh báo (canh_bao/warning), mặc định 90";
    private static final String STATUS_FILTER = "Trạng thái hợp đồng cần liệt kê: thieu, thua, can_bang (theo sản lượng so với ngân sách), alert (cảnh báo nghiêm trọng), da_qt, dang_qt, chua_qt (quyết toán), all (tất cả, mặc định)";

        private final VolumeService volumeService;

    @Tool(name = "volume_tong_quan", description = """
            Tổng quan ngân sách hợp đồng so với thành tiền thi công theo loại hợp đồng, khu vực, tỉnh
            Dùng khi: tỷ lệ sử dụng ngân sách; bao nhiêu hợp đồng vượt ngưỡng ngân sách; tổng KPI trên Dashboard; kiểm soát volume
            Trả về: số HĐ, giá trị HĐ, tổng thành tiền, chênh lệch, tỷ lệ sử dụng, số HĐ vượt ngưỡng/cảnh báo/thiếu/thừa/cân bằng, quyết toán, số trạm, số HĐ theo loại, tongKpi.
            Lưu ý: "Tổng KPI" trên Dashboard = tongKpi (không lọc khu vực/tỉnh/hợp đồng). soTramBatThuong là số của màn Kiểm soát Volume.
            Không dùng khi: danh sách trạm bất thường kiểu Dashboard -> volume_tram_vuot_nguong.
            """)
    public NganHoTongQuanToolResponse volumeTongQuan(
            @ToolParam(description = HOP_DONG_TIM, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = HE_SO, required = false) Double heSo,
            @ToolParam(description = NGUONG_CANH_BAO, required = false) Double nguongCanhBao) {
        return volumeService.tongQuan(new NganHoFilter(null, hopDong, heSo, nguongCanhBao, loaiHopDong, khuVuc, tinhThanh));
    }

    @Tool(name = "volume_chi_tiet_hop_dong", description = """
            Chi tiết ngân sách một hợp đồng theo nhóm và hạng mục
            Dùng khi: hợp đồng X đã dùng bao nhiêu phần trăm ngân sách; ngân sách hợp đồng X phân rã theo hạng mục; hợp đồng X vượt ngân sách chưa
            Trả về: giá trị HĐ, thành tiền thi công, chênh lệch, tỷ lệ sử dụng, trạng thái, phân rã theo nhóm và hạng mục.
            """)
    public NganHoChiTietHopDongToolDto volumeChiTietHopDong(
            @ToolParam(description = ToolParamDescriptions.HOP_DONG) String hopDong,
            @ToolParam(description = NGUONG_CANH_BAO, required = false) Double nguongCanhBao) {
        return volumeService.chiTietHopDong(hopDong, nguongCanhBao);
    }

    @Tool(name = "volume_theo_khu_vuc", description = """
            Chi tiết ngân sách một hợp đồng theo khu vực, tỉnh
            Dùng khi: hợp đồng X lệch ngân sách ở khu vực nào; tỉnh nào thiếu hay thừa ngân sách trong hợp đồng X
            Trả về: thẻ khu vực (giá trị, thành tiền, chênh lệch, số trạm thiếu/thừa) và cảnh báo tỉnh thiếu/thừa kèm mã trạm.
            """)
    public NganHoKhuVucToolDto volumeTheoKhuVuc(
            @ToolParam(description = ToolParamDescriptions.HOP_DONG) String hopDong,
            @ToolParam(description = NGUONG_CANH_BAO, required = false) Double nguongCanhBao) {
        return volumeService.theoKhuVuc(hopDong, nguongCanhBao);
    }

    @Tool(name = "volume_tram_vuot_nguong", description = """
            Danh sách trạm sản lượng vượt ngưỡng bình quân hợp đồng
            Dùng khi: trạm nào sản lượng bất thường; trạm nào vượt ngưỡng hợp đồng; bao nhiêu trạm vượt ngưỡng
            Trả về: mã trạm, hợp đồng, khu vực, sản lượng, giá trị bình quân (giá trị HĐ / số trạm), hệ số, ngưỡng, tỷ lệ so với bình quân, đã quyết toán, đã kiểm tra; có phân trang.
            Lưu ý: khớp ticker "Trạm sản lượng bất thường (vượt ngưỡng HĐ)" trên Dashboard. Xếp theo tỷ lệ so với bình quân giảm dần; totalItems là tổng số trạm vượt ngưỡng.
            """)
    public NganHoTramVuotNguongResponse volumeTramVuotNguong(
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return volumeService.tramVuotNguong(new NganHoFilter(hopDong, null, null, null, loaiHopDong, khuVuc, tinhThanh), page, pageSize);
    }

    @Tool(name = "volume_danh_sach_hop_dong", description = """
            Danh sách hợp đồng theo trạng thái ngân sách thiếu, thừa, cân bằng, cảnh báo hoặc quyết toán
            Dùng khi: hợp đồng nào thiếu ngân sách; hợp đồng nào thừa ngân sách; hợp đồng nào đang cảnh báo ngân sách
            Trả về: giá trị HĐ, sản lượng thực tế, số trạm, chênh lệch %, trạng thái; có phân trang.
            """)
    public NganHoDanhSachHopDongResponse volumeDanhSachHopDong(
            @ToolParam(description = STATUS_FILTER, required = false) String statusFilter,
            @ToolParam(description = HOP_DONG_TIM, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = HE_SO, required = false) Double heSo,
            @ToolParam(description = NGUONG_CANH_BAO, required = false) Double nguongCanhBao,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return volumeService.danhSachHopDong(new NganHoFilter(null, hopDong, heSo, nguongCanhBao, loaiHopDong, khuVuc, tinhThanh),
                statusFilter, page, pageSize);
    }
}