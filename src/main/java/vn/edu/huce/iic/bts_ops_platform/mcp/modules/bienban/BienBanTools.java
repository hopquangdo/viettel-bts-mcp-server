package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.McpToolGroup;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.ToolParamDescriptions;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.BienBanFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.BienBanTheoTrangThaiResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.BienBanThieuHoSoResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.BienBanThieuKhaoSatResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.BienBanTongQuanToolResponse;

import java.time.LocalDate;
import java.util.Objects;

@Service
public final class BienBanTools implements McpToolGroup {

    private static final String TRANG_THAI = "Lọc theo trạng thái biên bản: cho_duyet | da_duyet | tu_choi (tu_choi = bị từ chối / không đạt)";

        private final BienBanService bienBanService;

        public BienBanTools(BienBanService bienBanService) {
                this.bienBanService = Objects.requireNonNull(bienBanService);
    }

    @Tool(name = "bienban_tong_quan", description = """
            Tổng quan biên bản theo trạng thái duyệt, loại biên bản, khu vực, tỉnh, hợp đồng, nhà thầu
            Dùng khi: có bao nhiêu biên bản; bao nhiêu biên bản chờ duyệt; số biên bản bị từ chối; thống kê biên bản theo loại
            Trả về: tổng số, số chờ duyệt / đã duyệt / từ chối, số theo loại biên bản.
            Không dùng khi: cần danh sách từng biên bản -> bienban_theo_trang_thai.
            Lưu ý: lọc theo khu vực, tỉnh, hợp đồng, nhà thầu, đối tượng hoặc khoảng ngày.
            """)
    public BienBanTongQuanToolResponse bienBanTongQuan(
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate) {
        return bienBanService.tongQuan(new BienBanFilter(khuVuc, tinhThanh, hopDong, nhaThau, doiTuong, fromDate, toDate));
    }

    @Tool(name = "bienban_thieu_khao_sat", description = """
            Danh sách đối tượng (trạm) thiếu biên bản khảo sát đã duyệt theo khu vực, tỉnh, nhà thầu
            Dùng khi: trạm nào chưa có biên bản khảo sát; trạm nào chưa khảo sát xong; nhà thầu A còn trạm nào thiếu khảo sát
            Trả về: mã trạm, khu vực, mã hợp đồng; có phân trang.
            Không dùng khi: hồ sơ còn thiếu theo hợp đồng -> bienban_thieu_ho_so; trạm chưa giao nhà thầu -> phancong_chua_phan_cong.
            """)
    public BienBanThieuKhaoSatResponse bienBanThieuKhaoSat(
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return bienBanService.thieuKhaoSat(new BienBanFilter(khuVuc, tinhThanh, null, nhaThau, null, null, null), page, pageSize);
    }

    @Tool(name = "bienban_thieu_ho_so", description = """
            Danh sách hợp đồng thiếu hồ sơ biên bản bắt buộc theo checklist, theo hợp đồng, nhà thầu
            Dùng khi: hợp đồng nào còn thiếu hồ sơ; hợp đồng X thiếu biên bản gì; hồ sơ của nhà thầu A đã đủ chưa
            Trả về: mã, tên hợp đồng, danh sách biên bản còn thiếu; có phân trang.
            Không dùng khi: trạm thiếu khảo sát -> bienban_thieu_khao_sat.
            """)
    public BienBanThieuHoSoResponse bienBanThieuHoSo(
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return bienBanService.thieuHoSo(new BienBanFilter(null, null, hopDong, nhaThau, null, null, null), page, pageSize);
    }

    @Tool(name = "bienban_theo_trang_thai", description = """
            Danh sách biên bản theo trạng thái duyệt, hợp đồng, nhà thầu, đối tượng, khoảng ngày
            Dùng khi: biên bản nào đang chờ duyệt; biên bản bị từ chối vì lý do gì; ai lập và ai duyệt biên bản; biên bản của hợp đồng X
            Trả về: mã, loại, hợp đồng, trạng thái, ngày lập, người lập, lý do từ chối, người và ngày phê duyệt; có phân trang.
            Không dùng khi: chỉ cần số đếm theo trạng thái -> bienban_tong_quan.
            Lưu ý: trangThai = cho_duyet | da_duyet | tu_choi (tu_choi = bị từ chối / không đạt).
            """)
    public BienBanTheoTrangThaiResponse bienBanTheoTrangThai(
            @ToolParam(description = TRANG_THAI, required = false) String trangThai,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
                return bienBanService.theoTrangThai(new BienBanFilter(khuVuc, tinhThanh, hopDong, nhaThau, doiTuong, fromDate, toDate),
            trangThai, page, pageSize);
    }
}