package vn.edu.huce.iic.bts_ops_platform.mcp.definitions;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.SanLuongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.handler.SanLuongToolHandler;

import java.time.LocalDate;
import java.util.EnumSet;

/** MCP tool module Sản lượng: sanluong_tool (đủ mọi khối) và các tool nhỏ sanluong_* (mỗi tool 1 khối). */
@Service
@RequiredArgsConstructor
public class SanLuongToolDefinitions implements McpToolGroup {

    private final SanLuongToolHandler sanLuongToolHandler;

    // ------------------------------------------------------------------ sanluong_tool

    @Tool(
            name = "sanluong_tool",
            description = """
                    Mục đích: Báo cáo sản lượng theo khoảng thời gian đã chọn, lọc theo đối tượng, hợp đồng,
                    nhà thầu hoặc khu vực.
                    Dùng khi: cần tổng hợp sản lượng, xếp hạng, xu hướng hoặc tìm nhà thầu đã/chưa báo trong kỳ.
                    Trả về: summary, progress, trend, ranking theo nhaThau/khuVuc/tinh/hopDong/loaiHopDong/doiTuong (bảng cùng cấp với bộ lọc được bỏ; sắp xếp bằng xepHangTheo),
                    periodTrend (giá trị sản lượng theo từng kỳ con), nhaThauChuaBaoTrongKy (nhà thầu CHƯA có sản lượng 'done' trong kỳ)
                    và nhaThauDaBaoTrongKy (nhà thầu ĐÃ có sản lượng 'done' trong kỳ, kèm giaTriDaBao). Muốn biết "nhà thầu nào đã báo sản lượng
                    hôm nay": gọi với fromDate=toDate=hôm nay rồi đọc nhaThauDaBaoTrongKy.
                    Ngoài ra: doiTuongChuaCoSanLuong (đối tượng/trạm nào chưa ghi nhận sản lượng trong khoảng ngày, phân trang) và nghiemThu
                    (số hạng mục và giá trị đã duyệt = nghiệm thu đạt, không đạt kèm lý do, chờ nghiệm thu). summary.periodValue là tiền đã báo.
                    Khi doiTuong là mã/id của đối tượng cụ thể (trạm, tuyến): có hangMucDoiTuong (hạng mục đã làm và chưa làm, tối đa 5 đối tượng).
                    Luôn có danhSachDoiTuong: bảng từng đối tượng (SL tổng, SL hôm nay, thi công gần nhất, tỉnh, hạng mục đã làm/tổng).
                    Quy tắc xếp hạng: bảng xếp hạng bị bỏ bằng null khi bộ lọc đứng cùng cấp với bảng đó (vd hopDong -> xepHangHopDong null, khuVuc -> xepHangKhuVuc null).
                    Nếu bộ lọc không cùng cấp thì bảng vẫn trả về danh sách, có thể rỗng [] thay vì null. Với lọc 1 đối tượng cụ thể, bỏ mọi bảng xếp hạng.
                    """
    )
        public SanLuongQueryResponse sanLuong(
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = "Ngưỡng % hoàn thành hạng mục: đối tượng đang thi công dưới mức này tính vào lowCompletionCount, mặc định 50", required = false) Double nguongHoanThanhThap,
            @ToolParam(description = "false: chỉ liệt kê đối tượng đã có sản lượng trong kỳ ở topDoiTuong; bỏ trống hoặc true: gồm cả đối tượng chưa có sản lượng", required = false) Boolean includeWithoutOutput,
            @ToolParam(description = "Sắp xếp danhSachDoiTuong (bảng từng đối tượng: mã, tỉnh, khu vực, hợp đồng, nhà thầu, SL tổng, SL hôm nay, thi công gần nhất, hạng mục, vướng mắc; chỉ gồm đối tượng có bản ghi trong kỳ, trừ khi includeWithoutOutput=true; phân trang theo page/pageSize) giảm dần theo: latest (thi công gần nhất, mặc định), periodValue (giá trị trong kỳ), totalValue (SL tổng), todayValue (SL hôm nay)", required = false) String sapXep,
            @ToolParam(description = "Loại hợp đồng (lĩnh vực) cần lọc: ID, mã hoặc tên", required = false) String loaiHopDong,
            @ToolParam(description = "Sắp xếp các bảng xếp hạng (nhaThau, khuVuc, tinh, hopDong, loaiHopDong) theo: giaTri (mặc định), hoanThanh (% hoàn thành), soDoiTuong, vuongMac. Lọc theo cấp nào thì bảng cấp đó được bỏ (vd lọc khuVuc thì không có khuVuc, chỉ có tinh, hopDong, nhaThau… trong khu vực đó); lọc 1 đối tượng cụ thể thì không có bảng xếp hạng nào, chỉ có hangMucDoiTuong", required = false) String xepHangTheo,
            @ToolParam(description = "true: xếp hạng tăng dần (mặc định giảm dần)", required = false) Boolean tangDan,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return sanLuongToolHandler.query(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, fromDate, toDate, page, pageSize, nguongHoanThanhThap, includeWithoutOutput, sapXep, loaiHopDong, xepHangTheo, tangDan);
    }

    // ------------------------------------------------------------------ sanluong_* (tool nhỏ, mỗi tool 1 khối của sanluong_tool)

    @Tool(name = "sanluong_tong_quan", description = """
            Tổng quan sản lượng trong kỳ: tổng giá trị (VNĐ), giá trị hôm nay, số đối tượng có/chưa có sản lượng,
            tiến độ hoàn thành hạng mục, so sánh với kỳ trước (trend), số vướng mắc đang mở.
            Không truyền ngày: lấy toàn bộ thời gian.
            """)
    public SanLuongQueryResponse sanLuongTongQuan(
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = "Ngưỡng % hoàn thành hạng mục: đối tượng đang thi công dưới mức này tính vào lowCompletionCount, mặc định 50", required = false) Double nguongHoanThanhThap) {
        return sanLuongToolHandler.query(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, fromDate, toDate, null, null,
                nguongHoanThanhThap, null, null, loaiHopDong, null, null, EnumSet.of(SanLuongToolHandler.Block.TONG_QUAN));
    }

    @Tool(name = "sanluong_xep_hang", description = """
            Xếp hạng sản lượng theo nhà thầu, khu vực, tỉnh, hợp đồng, loại hợp đồng và top đối tượng.
            Bảng cùng cấp với bộ lọc bị bỏ (vd lọc khuVuc thì không có bảng khuVuc, chỉ có tỉnh/hợp đồng/nhà thầu trong khu vực đó);
            lọc 1 đối tượng cụ thể thì không có bảng nào.
            """)
    public SanLuongQueryResponse sanLuongXepHang(
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = "Tiêu chí xếp hạng: giaTri (mặc định), hoanThanh (% hoàn thành), soDoiTuong, vuongMac", required = false) String xepHangTheo,
            @ToolParam(description = "true: xếp hạng tăng dần (mặc định giảm dần)", required = false) Boolean tangDan) {
        return sanLuongToolHandler.query(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, fromDate, toDate, null, null,
                null, null, null, loaiHopDong, xepHangTheo, tangDan, EnumSet.of(SanLuongToolHandler.Block.XEP_HANG));
    }

    @Tool(name = "sanluong_xu_huong", description = """
            Diễn biến sản lượng theo từng kỳ con trong khoảng ngày (ngày/tuần/tháng tự chọn theo độ dài khoảng).
            Dùng khi hỏi xu hướng, tăng/giảm theo thời gian, biểu đồ sản lượng.
            """)
    public SanLuongQueryResponse sanLuongXuHuong(
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate) {
        return sanLuongToolHandler.query(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, fromDate, toDate, null, null,
                null, null, null, loaiHopDong, null, null, EnumSet.of(SanLuongToolHandler.Block.XU_HUONG));
    }

    @Tool(name = "sanluong_nha_thau_bao_cao", description = """
            Nhà thầu ĐÃ và CHƯA báo sản lượng (trạng thái done) trong kỳ, có phân trang.
            "Hôm nay ai chưa báo sản lượng": fromDate=toDate=hôm nay, đọc nhaThauChuaBaoTrongKy.
            """)
    public SanLuongQueryResponse sanLuongNhaThauBaoCao(
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return sanLuongToolHandler.query(null, hopDong, nhaThau, khuVuc, tinhThanh, fromDate, toDate, page, pageSize,
                null, null, null, loaiHopDong, null, null, EnumSet.of(SanLuongToolHandler.Block.NHA_THAU_BAO));
    }

    @Tool(name = "sanluong_nghiem_thu", description = """
            Nghiệm thu sản lượng: số hạng mục và giá trị đạt, không đạt (kèm lý do, phân trang), chờ nghiệm thu.
            Lọc ngày theo ngày nghiệm thu; không truyền ngày thì tính toàn bộ.
            """)
    public SanLuongQueryResponse sanLuongNghiemThu(
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return sanLuongToolHandler.query(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, fromDate, toDate, page, pageSize,
                null, null, null, loaiHopDong, null, null, EnumSet.of(SanLuongToolHandler.Block.NGHIEM_THU));
    }

    @Tool(name = "sanluong_doi_tuong_chua_co", description = """
            Đối tượng (trạm, tuyến…) chưa ghi nhận sản lượng hoàn thành nào trong khoảng ngày, có phân trang,
            kèm hợp đồng, nhà thầu, khu vực, số hạng mục, có vướng mắc mở hay không.
            """)
    public SanLuongQueryResponse sanLuongDoiTuongChuaCo(
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return sanLuongToolHandler.query(null, hopDong, nhaThau, khuVuc, tinhThanh, fromDate, toDate, page, pageSize,
                null, null, null, loaiHopDong, null, null, EnumSet.of(SanLuongToolHandler.Block.DOI_TUONG_CHUA_CO));
    }

    @Tool(name = "sanluong_danh_sach_doi_tuong", description = """
            Bảng từng đối tượng: mã, tỉnh, khu vực, hợp đồng, nhà thầu, SL tổng, SL hôm nay, thi công gần nhất,
            hạng mục đã làm/tổng, vướng mắc; có phân trang và sắp xếp.
            """)
    public SanLuongQueryResponse sanLuongDanhSachDoiTuong(
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = "true: gồm cả đối tượng chưa có sản lượng trong kỳ (mặc định chỉ đối tượng có bản ghi)", required = false) Boolean includeWithoutOutput,
            @ToolParam(description = "Sắp xếp giảm dần theo: latest (thi công gần nhất, mặc định), periodValue (giá trị trong kỳ), totalValue (SL tổng), todayValue (SL hôm nay)", required = false) String sapXep,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return sanLuongToolHandler.query(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, fromDate, toDate, page, pageSize,
                null, includeWithoutOutput, sapXep, loaiHopDong, null, null, EnumSet.of(SanLuongToolHandler.Block.DANH_SACH_DOI_TUONG));
    }

    @Tool(name = "sanluong_hang_muc_doi_tuong", description = """
            Hạng mục đã làm và chưa làm của 1 hoặc vài đối tượng cụ thể (trạm, tuyến; tối đa 5, ngăn cách bằng dấu phẩy).
            """)
    public SanLuongQueryResponse sanLuongHangMucDoiTuong(
            @ToolParam(description = "Mã hoặc ID đối tượng cụ thể (không phải loại đối tượng); nhiều đối tượng ngăn cách bằng dấu phẩy") String doiTuong) {
        return sanLuongToolHandler.query(doiTuong, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, EnumSet.of(SanLuongToolHandler.Block.HANG_MUC));
    }
}
