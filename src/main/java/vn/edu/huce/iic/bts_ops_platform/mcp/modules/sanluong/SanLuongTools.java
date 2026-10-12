package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.McpToolGroup;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.ToolParamDescriptions;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongDanhSachDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongDoiTuongChuaCoResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongHangMucDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongNghiemThuResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongNhaThauBaoCaoResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongTongQuanToolResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongXepHangResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongXuHuongResponse;

import java.time.LocalDate;

@Service
public final class SanLuongTools implements McpToolGroup {

    private final SanLuongService service;

    private static final String THEO_XEP_HANG = "Chiều xếp hạng (bắt buộc, 1 giá trị): nha_thau (nhà thầu), khu_vuc (khu vực), "
        + "tinh (tỉnh/thành), hop_dong (hợp đồng, tối đa 20 dòng), loai_hop_dong (loại hợp đồng), doi_tuong (top 20 đối tượng)";

    public SanLuongTools(SanLuongService service) {
        this.service = service;
    }

    public vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.response.SanLuongProgressSummaryResponse tienDo(
        vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.request.SanLuongFilter filter,
        Double nguongHoanThanhThap) {
        return service.tienDo(filter, nguongHoanThanhThap);
    }

    @Tool(name = "sanluong_tong_quan", description = """
        Tổng quan sản lượng trong kỳ theo khu vực, tỉnh, nhà thầu, hợp đồng
        Dùng khi: sản lượng tháng này bao nhiêu; làm được bao nhiêu trong kỳ; sản lượng hôm nay; tăng hay giảm so với kỳ trước; bao nhiêu đối tượng có sản lượng
        Trả về: tổng giá trị, giá trị hôm nay, số đối tượng có/chưa có sản lượng, tiến độ hoàn thành hạng mục, so với kỳ trước (soVoiKyTruoc), số vướng mắc đang mở. Chỉ số liệu tổng hợp.
        Lưu ý: không truyền ngày = toàn bộ thời gian. Màn Sản lượng Thi công: "Tổng đối tượng" = tongSoDoiTuong (GỒM CẢ đối tượng chưa có sản lượng), "Có sản lượng" = soDoiTuongCoSanLuong, "Tổng SL kỳ lọc" = giaTriTrongKy, "SL hôm nay" = giaTriHomNay, "Vướng mắc" = soVuongMacDangMo; hỏi "bao nhiêu đối tượng có sản lượng" thì trả soDoiTuongCoSanLuong. Dashboard: "Tổng sản lượng năm" / "Sản lượng tháng" = giaTriHoanThanh (kỳ cả năm / 1 tháng), so với kỳ trước = tangTruongHoanThanh; giaTriTrongKy là số của màn Sản lượng thi công (2 cách tính khác nhau, chọn theo màn người dùng hỏi).
        """)
    public SanLuongTongQuanToolResponse sanLuongTongQuan(
        @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
        @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
        @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
        @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
        @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
        @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
        @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
        @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
        @ToolParam(description = "Ngưỡng % hoàn thành hạng mục: đối tượng đang thi công dưới mức này tính vào soDoiTuongHoanThanhThap, mặc định 50", required = false) Double nguongHoanThanhThap) {
    return service.tongQuan(new SanLuongFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, fromDate, toDate), nguongHoanThanhThap);
    }

    @Tool(name = "sanluong_xep_hang", description = """
        Xếp hạng sản lượng theo nhà thầu, khu vực, tỉnh, hợp đồng, loại hợp đồng hoặc đối tượng
        Dùng khi: nhà thầu nào làm ra sản lượng cao nhất; khu vực nào sản lượng thấp nhất; top hợp đồng theo sản lượng; đội nào làm được nhiều nhất
        Trả về: 1 bảng xếp hạng theo chiều chọn bằng tham số theo.
        Lưu ý: cần nhiều chiều thì gọi nhiều lần với theo khác nhau. Chiều trùng cấp bộ lọc (vd lọc khuVuc và theo=khu_vuc) hoặc lọc 1 đối tượng cụ thể: trả ghiChu kèm gợi ý chiều khác.
        Không dùng khi: nhà thầu nhận nhiều trạm nhất -> phancong_xep_hang_nha_thau.
        """)
    public SanLuongXepHangResponse sanLuongXepHang(
        @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
        @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
        @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
        @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
        @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
        @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
        @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
        @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
        @ToolParam(description = THEO_XEP_HANG) String theo,
        @ToolParam(description = "Tiêu chí xếp hạng: giaTri (mặc định), hoanThanh (% hoàn thành), soDoiTuong, vuongMac", required = false) String xepHangTheo,
        @ToolParam(description = "true: xếp hạng tăng dần (mặc định giảm dần)", required = false) Boolean tangDan) {
    return service.xepHang(new SanLuongFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, fromDate, toDate), theo, xepHangTheo, tangDan);
    }

    @Tool(name = "sanluong_xu_huong", description = """
        Xu hướng sản lượng theo thời gian (ngày, tuần, tháng)
        Dùng khi: sản lượng tăng hay giảm theo thời gian; sản lượng từng tháng năm nay; biểu đồ sản lượng theo tuần; mấy tháng nay làm ăn lên hay xuống
        Trả về: sản lượng từng kỳ con trong khoảng ngày (ngày/tuần/tháng tự chọn theo độ dài khoảng).
        """)
    public SanLuongXuHuongResponse sanLuongXuHuong(
        @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
        @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
        @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
        @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
        @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
        @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
        @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
        @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate) {
    return service.xuHuong(new SanLuongFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, fromDate, toDate));
    }

    @Tool(name = "sanluong_nha_thau_bao_cao", description = """
        Danh sách nhà thầu đã báo hoặc chưa báo sản lượng trong kỳ
        Dùng khi: hôm nay nhà thầu nào đã báo sản lượng; ai chưa báo sản lượng tuần này; bên nào chưa gửi số liệu thi công
        Trả về: danh sách nhà thầu theo 1 trạng thái báo sản lượng; có phân trang.
        Lưu ý: chọn trạng thái bằng trangThai (da_bao hoặc chua_bao); cần cả hai nhóm thì gọi 2 lần.
        """)
    public SanLuongNhaThauBaoCaoResponse sanLuongNhaThauBaoCao(
        @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
        @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
        @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
        @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
        @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
        @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
        @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
        @ToolParam(description = "Trạng thái báo sản lượng (bắt buộc): da_bao = nhà thầu đã báo sản lượng trong kỳ (có bản ghi done/survey/design có ngày thực hiện trong kỳ), chua_bao = nhà thầu đang phụ trách đối tượng nhưng chưa có bản ghi nào như vậy trong kỳ") String trangThai,
        @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
        @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
    return service.nhaThauBaoCao(new SanLuongFilter(null, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, fromDate, toDate), trangThai, page, pageSize);
    }

    @Tool(name = "sanluong_nghiem_thu", description = """
        Tổng quan nghiệm thu sản lượng: đạt, không đạt, chờ nghiệm thu
        Dùng khi: bao nhiêu hạng mục nghiệm thu không đạt và lý do; giá trị nghiệm thu đạt; còn bao nhiêu hạng mục chờ nghiệm thu; khối lượng nào bị trả về
        Trả về: số hạng mục và giá trị đạt, không đạt (kèm lý do, phân trang), chờ nghiệm thu.
        Lưu ý: lọc ngày theo ngày nghiệm thu; không truyền ngày thì tính toàn bộ.
        """)
    public SanLuongNghiemThuResponse sanLuongNghiemThu(
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
    return service.nghiemThu(new SanLuongFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, fromDate, toDate), page, pageSize);
    }

    @Tool(name = "sanluong_doi_tuong_chua_co", description = """
        Danh sách đối tượng (trạm/tuyến) chưa có sản lượng hoàn thành trong kỳ
        Dùng khi: trạm nào chưa có sản lượng tháng này; trạm nào chưa làm được gì; đối tượng nào cả quý không ghi nhận sản lượng
        Trả về: đối tượng, hợp đồng, nhà thầu, khu vực, số hạng mục, có vướng mắc mở hay không; có phân trang.
        """)
    public SanLuongDoiTuongChuaCoResponse sanLuongDoiTuongChuaCo(
        @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
        @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
        @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
        @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
        @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
        @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
        @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
        @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
        @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
    return service.doiTuongChuaCo(new SanLuongFilter(null, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, fromDate, toDate), page, pageSize);
    }

    @Tool(name = "sanluong_danh_sach_doi_tuong", description = """
        Danh sách sản lượng theo từng đối tượng (trạm/tuyến)
        Dùng khi: từng trạm làm được bao nhiêu; bảng sản lượng từng trạm của hợp đồng X; trạm nào có sản lượng hôm nay
        Trả về: mã, tỉnh, khu vực, hợp đồng, nhà thầu, SL tổng, SL hôm nay, thi công gần nhất, hạng mục đã làm/tổng, vướng mắc; có phân trang và sắp xếp.
        """)
    public SanLuongDanhSachDoiTuongResponse sanLuongDanhSachDoiTuong(
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
    return service.danhSachDoiTuong(new SanLuongFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, fromDate, toDate), includeWithoutOutput, sapXep, page, pageSize);
    }

    @Tool(name = "sanluong_hang_muc_doi_tuong", description = """
        Chi tiết hạng mục đã làm và chưa làm của đối tượng (trạm/tuyến)
        Dùng khi: trạm X còn hạng mục nào chưa làm; tuyến X đã làm những hạng mục gì; trạm X còn phải làm những gì
        Trả về: hạng mục đã làm và chưa làm của từng đối tượng.
        Lưu ý: tối đa 5 đối tượng, ngăn cách bằng dấu phẩy.
        """)
    public SanLuongHangMucDoiTuongResponse sanLuongHangMucDoiTuong(
        @ToolParam(description = "Mã hoặc ID đối tượng cụ thể (không phải loại đối tượng); nhiều đối tượng ngăn cách bằng dấu phẩy") String doiTuong) {
    return service.hangMucDoiTuong(doiTuong);
    }
}