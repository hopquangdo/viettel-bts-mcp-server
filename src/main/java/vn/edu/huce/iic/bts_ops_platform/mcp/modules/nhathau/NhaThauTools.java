package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.McpToolGroup;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.ToolParamDescriptions;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauQueryResponse;

@Service
@RequiredArgsConstructor
public class NhaThauTools implements McpToolGroup {

    private final NhaThauService nhaThauService;

    @Tool(
            name = "nhathau_tool",
            description = """
                    Danh sách nhà thầu theo khu vực, tỉnh, hợp đồng
                    Dùng khi: có những nhà thầu nào; bao nhiêu nhà thầu; tìm nhà thầu theo tên; nhà thầu nào làm ở tỉnh X
                    Trả về: tongSo, danhSach (id, tên, tên đăng nhập, số hợp đồng, số đối tượng đang phụ trách).
                    Không dùng khi: sản lượng, vướng mắc, tồn, phân công của 1 nhà thầu -> tool của mảng đó kèm tham số nhaThau.
                    """
    )
    public NhaThauQueryResponse nhaThau(
            @ToolParam(description = "Tên hoặc tên đăng nhập (một phần) nhà thầu cần tìm; khớp nhiều thì trả tất cả", required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return nhaThauService.query(nhaThau, hopDong, khuVuc, tinhThanh, page, pageSize);
    }
}