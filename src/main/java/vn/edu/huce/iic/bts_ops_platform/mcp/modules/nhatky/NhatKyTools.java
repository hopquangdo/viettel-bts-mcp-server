package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhatky;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.McpToolGroup;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.ToolParamDescriptions;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhatky.dto.NhatKyQueryResponse;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class NhatKyTools implements McpToolGroup {

    private final NhatKyService nhatKyService;

    @Tool(
            name = "auditlog_tool",
            description = """
                    Danh sách nhật ký thao tác trên hợp đồng và đối tượng theo hành động, người thực hiện
                    Dùng khi: ai nhập sản lượng lần đầu cho trạm X; ai sửa sản lượng, sửa những gì; ai đổi trạng thái đối tượng; hôm qua ai thao tác trên hợp đồng X
                    Trả về: tongSo, lanDau (thao tác sớm nhất), ganNhat (thao tác mới nhất), danhSach (thời gian, người thực hiện, hành động, mô tả "trường cũ -> mới", hợp đồng, mã đối tượng), luuY khi dữ liệu cũ không có người thực hiện.
                    Lưu ý: hành động gồm nhập, sửa, xóa, nghiệm thu sản lượng, cập nhật/chuyển trạng thái đối tượng, quyết toán, tạo/sửa hợp đồng. Người nhập lần đầu: truyền doiTuong, hanhDong=NHAP_SAN_LUONG, đọc lanDau; người sửa: hanhDong=SUA_SAN_LUONG.
                    """
    )
    public NhatKyQueryResponse nhatKy(
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = "Hành động, nhiều giá trị ngăn cách bằng dấu phẩy: NHAP_SAN_LUONG, SUA_SAN_LUONG, XOA_SAN_LUONG, NGHIEM_THU_SAN_LUONG, CAP_NHAT_DOI_TUONG, DOI_TRANG_THAI_DOI_TUONG, THEM_DOI_TUONG, XOA_DOI_TUONG, IMPORT_DOI_TUONG, THEM_THUOC_TINH, SUA_THUOC_TINH, CAP_NHAT_QUYET_TOAN, TAO_HOP_DONG, CAP_NHAT_HOP_DONG, XOA_HOP_DONG; san_luong là cả 4 thao tác sản lượng. Bỏ trống: tất cả", required = false) String hanhDong,
            @ToolParam(description = "Tên người thực hiện (tìm chứa, không phân biệt hoa thường)", required = false) String nguoiThucHien,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = "Thứ tự danhSach: moi_nhat (mặc định) hoặc cu_nhat", required = false) String thuTu,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return nhatKyService.query(doiTuong, hopDong, hanhDong, nguoiThucHien, fromDate, toDate, thuTu, page, pageSize);
    }
}