package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.McpToolGroup;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.ToolParamDescriptions;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.*;

import java.time.LocalDate;

/** MCP tool declarations for the Phân công module. */
@Service
@RequiredArgsConstructor
public class PhanCongTools implements McpToolGroup {

    private static final String VUNG = "Mã vùng quản lý (không phải mã đối tượng cụ thể)";
    private static final String GIAI_DOAN = "Giai đoạn phân công cần lọc (khớp đúng, không phân biệt hoa thường)";
    private static final String CAN_BO = "Cán bộ cần lọc: ID, mã hoặc tên";

        private final PhanCongService phanCongService;

    @Tool(name = "phancong_tong_quan", description = """
            Tổng quan phân công nhà thầu theo khu vực, tỉnh, vùng
            Dùng khi: tỷ lệ đối tượng đã giao nhà thầu; có bao nhiêu nhà thầu được giao việc; tổng đối tượng và số khu vực trên Dashboard
            Trả về: số đối tượng, đã giao nhà thầu (tỷ lệ), số nhà thầu, hoàn thành, vướng mắc, tồn; số phân công theo khu vực và tỉnh, soKhuVuc.
            Lưu ý: ô "Tổng đối tượng" và "Khu vực" trên Dashboard = tongDoiTuong và soKhuVuc (không lọc gì).
            Không dùng khi: trạm X giao cho ai -> phancong_danh_sach.
            """)
    public PhanCongTongQuanToolResponse phanCongTongQuan(
            @ToolParam(description = VUNG, required = false) String vung,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = GIAI_DOAN, required = false) String giaiDoan,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate) {
        return phanCongService.tongQuan(new PhanCongFilter(vung, nhaThau, hopDong, khuVuc, tinhThanh, doiTuong, giaiDoan, fromDate, toDate));
    }

    @Tool(name = "phancong_danh_sach", description = """
            Danh sách phân công nhà thầu theo đối tượng (trạm/tuyến), hợp đồng, khu vực, giai đoạn
            Dùng khi: trạm X đang giao cho nhà thầu nào; ai đang làm tuyến X; lịch sử phân công của trạm; nhà thầu A được giao những trạm nào
            Trả về: hợp đồng, nhà thầu, giai đoạn, khu vực, tỉnh, trạng thái, cờ vướng mắc đang mở; có phân trang.
            Lưu ý: lichSu=true để gồm cả phân công đã ngừng.
            Không dùng khi: chỉ cần số tổng hợp -> phancong_tong_quan; việc của 1 cán bộ / tài khoản -> phancong_theo_can_bo.
            """)
    public PhanCongDanhSachResponse phanCongDanhSach(
            @ToolParam(description = VUNG, required = false) String vung,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = GIAI_DOAN, required = false) String giaiDoan,
            @ToolParam(description = "true: gồm cả phân công đã ngừng (lịch sử phân công của đối tượng), mặc định chỉ phân công đang hoạt động", required = false) Boolean lichSu,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return phanCongService.danhSach(new PhanCongFilter(vung, nhaThau, hopDong, khuVuc, tinhThanh, doiTuong, giaiDoan, fromDate, toDate),
                null, lichSu, page, pageSize);
    }

    @Tool(name = "phancong_theo_can_bo", description = """
            Danh sách phân công đang hoạt động theo cán bộ hoặc tài khoản nhà thầu
            Dùng khi: cán bộ X đang được giao việc gì; tài khoản X đang phụ trách đối tượng nào; việc của tôi
            Trả về: các phân công đang hoạt động; có phân trang.
            """)
    public PhanCongTheoCanBoResponse phanCongTheoCanBo(
            @ToolParam(description = CAN_BO) String canBo,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return phanCongService.theoCanBo(canBo, fromDate, toDate, page, pageSize);
    }

    @Tool(name = "phancong_chua_phan_cong", description = """
            Danh sách đối tượng chưa phân công nhà thầu theo hợp đồng, khu vực, tỉnh
            Dùng khi: trạm nào chưa giao nhà thầu; việc nào còn chưa phân công; đối tượng chưa có ai nhận
            Trả về: đối tượng, hợp đồng, khu vực, tỉnh; có phân trang.
            """)
    public PhanCongChuaPhanCongResponse phanCongChuaPhanCong(
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return phanCongService.chuaPhanCong(new PhanCongFilter(null, null, hopDong, khuVuc, tinhThanh, doiTuong, null, null, null),
                page, pageSize);
    }

    @Tool(name = "phancong_ty_le_huy_tram", description = """
            Tổng quan tỷ lệ hủy trạm theo nhà thầu, cán bộ, khu vực
            Dùng khi: tỷ lệ hủy trạm bao nhiêu; cán bộ nào hủy trạm vượt ngưỡng; khu vực nào hủy trạm nhiều
            Trả về: tỷ lệ trạm bị hủy theo nhà thầu/cán bộ (theo=nha_thau) hoặc theo khu vực (theo=khu_vuc), so với ngưỡng cảnh báo (mặc định 15%).
            Lưu ý: khớp cảnh báo "Tỷ lệ hủy trạm vượt ngưỡng — theo cán bộ / theo khu vực" trên Dashboard; hỏi ai vượt ngưỡng thì truyền chiVuotNguong=true.
            """)
    public PhanCongTyLeHuyResponse phanCongTyLeHuyTram(
            @ToolParam(description = "Nhóm theo: nha_thau (nhà thầu/cán bộ được giao, mặc định) hoặc khu_vuc", required = false) String theo,
            @ToolParam(description = "true: chỉ trả các nhóm có tỷ lệ hủy vượt ngưỡng", required = false) Boolean chiVuotNguong) {
        return phanCongService.tyLeHuyTram(theo, chiVuotNguong);
    }

    @Tool(name = "phancong_xep_hang_nha_thau", description = """
            Xếp hạng nhà thầu theo số đối tượng phụ trách
            Dùng khi: nhà thầu nào nhận nhiều việc nhất; nhà thầu nào phụ trách nhiều trạm nhất; top nhà thầu theo khối lượng được giao
            Trả về: số đối tượng, số đang vướng mắc, tổng sản lượng hiệu lực của từng nhà thầu.
            Lưu ý: lọc nhaThau hoặc 1 đối tượng cụ thể (mã trạm/tuyến) thì không có bảng xếp hạng.
            Không dùng khi: nhà thầu làm ra nhiều sản lượng nhất -> sanluong_xep_hang.
            """)
    public PhanCongXepHangNhaThauResponse phanCongXepHangNhaThau(
            @ToolParam(description = VUNG, required = false) String vung,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = GIAI_DOAN, required = false) String giaiDoan,
            @ToolParam(description = "Số dòng xếp hạng, mặc định 5, tối đa 20", required = false) Integer top) {
        return phanCongService.xepHangNhaThau(new PhanCongFilter(vung, nhaThau, hopDong, khuVuc, tinhThanh, doiTuong, giaiDoan, null, null), top);
    }
}
