package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.McpToolGroup;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.ToolParamDescriptions;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.request.VuongMacFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacQuaHanResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacTimKiemResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacXepHangResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.VuongMacService;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VuongMacTools implements McpToolGroup {

    private static final String TRANG_THAI = "Trạng thái vướng mắc: pending (chờ xử lý), in_progress (đang xử lý), resolved (đã xử lý), rejected (từ chối)";
    private static final String KIEU = "Loại vướng mắc: giai_phong_mat_bang, thiet_ke, thu_tuc_phap_ly, nguon_vat_lieu, thi_cong";
    private static final String GIAI_DOAN = "Giai đoạn phát sinh vướng mắc (khớp đúng, không phân biệt hoa thường)";
    private static final String DANG_MO = "true: chỉ vướng mắc đang mở (chờ xử lý hoặc đang xử lý)";
    private static final String CAN_BO = "Cán bộ xử lý vướng mắc cần lọc: ID, tên hoặc tên đăng nhập, ví dụ \"Vinh\"";
    private static final String TU_NGAY_TAO = "Ngày tạo vướng mắc từ ngày (giờ VN)";
    private static final String DEN_NGAY_TAO = "Ngày tạo vướng mắc đến ngày (gồm cả ngày này)";
    private static final String THIEU_NHAN_LUC = "Vướng mắc đang mở thiếu nhân lực: nha_thau (trạm chưa phân công nhà thầu --"
        + " dùng khi hỏi chung \"thiếu nhân lực xử lý\"), nguoi_xu_ly (chỉ khi hỏi rõ vướng mắc chưa giao người xử lý),"
        + " bat_ky (thiếu 1 trong 2)";

    private final VuongMacService vuongMacService;

    @Tool(name = "vuongmac_tong_quan", description = """
        Tổng quan vướng mắc theo trạng thái xử lý, loại vướng mắc, khu vực
        Dùng khi: có bao nhiêu vướng mắc đang mở; bao nhiêu đối tượng đang vướng; đã xử lý xong bao nhiêu vướng mắc trong kỳ; số vướng mắc quá hạn
        Trả về: tổng số, theo trạng thái (chờ xử lý, đang xử lý, đã xử lý, từ chối), quá hạn 30 ngày, số theo loại, daXuLyTrongKy (khi truyền fromDate hoặc sinceDate), soHopDongCoVuongMacMo, soDoiTuongCoVuongMacMo.
        Lưu ý: tongSo = mọi vướng mắc; "đang vướng" / "chưa xử lý" / "đang mở" = dangVuong (= choXuLy + dangXuLy, ô "Đang vướng" trên Dashboard); "đã giải quyết" = daXuLy; "bao nhiêu đối tượng đang vướng" cũng dùng dangVuong.
        Không dùng khi: ô "Có vướng mắc" trên màn Tiến độ hợp đồng -> hopdong_tong_quan.soHopDongVuongMac.
        """)
    public VuongMacTongQuanResponse vuongMacTongQuan(
        @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
        @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
        @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
        @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
        @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
        @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
        @ToolParam(description = TRANG_THAI, required = false) String trangThai,
        @ToolParam(description = KIEU, required = false) String kieuVuongMac,
        @ToolParam(description = GIAI_DOAN, required = false) String giaiDoan,
        @ToolParam(description = DANG_MO, required = false) Boolean dangMoOnly,
        @ToolParam(description = CAN_BO, required = false) String canBo,
        @ToolParam(description = TU_NGAY_TAO, required = false) LocalDate fromDate,
        @ToolParam(description = DEN_NGAY_TAO, required = false) LocalDate toDate,
        @ToolParam(description = "Đầu kỳ tính daXuLyTrongKy khi không truyền fromDate", required = false) LocalDate sinceDate) {
        var result = vuongMacService.tongQuan(new VuongMacFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, trangThai,
            kieuVuongMac, giaiDoan, dangMoOnly, canBo, fromDate, toDate), sinceDate);
        return new VuongMacTongQuanResponse(
            result.tongSo(), result.choXuLy(), result.dangXuLy(), result.daXuLy(), result.tuChoi(),
            result.dangVuong(), result.quaHan30Ngay(), result.daXuLyTrongKy(), result.theoLoai(),
            result.soHopDongCoVuongMacMo(), result.soDoiTuongCoVuongMacMo());
    }

    @Tool(name = "vuongmac_tim_kiem", description = """
        Danh sách vướng mắc theo hợp đồng, đối tượng, nhà thầu, khu vực, giai đoạn, loại, cán bộ xử lý
        Dùng khi: liệt kê vướng mắc đang mở của hợp đồng X; trạm X đang vướng gì; vướng mắc do cán bộ X xử lý; trạm nào thiếu nhân lực xử lý vướng mắc
        Trả về: mã, giai đoạn, loại, mô tả, trạng thái, hợp đồng, khu vực, tỉnh, nhà thầu, người xử lý; có phân trang.
        Lưu ý: hỏi vướng mắc / trạm thiếu nhân lực xử lý thì truyền thieuNhanLuc=nha_thau.
        """)
    public VuongMacTimKiemResponse vuongMacTimKiem(
        @ToolParam(description = "Chữ cần tìm trong mã hoặc nội dung mô tả vướng mắc (không dùng cho tên trạm, hợp đồng, nhà thầu, địa bàn -- đã có tham số riêng)", required = false) String moTaChua,
        @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
        @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
        @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
        @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
        @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
        @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
        @ToolParam(description = TRANG_THAI, required = false) String trangThai,
        @ToolParam(description = KIEU, required = false) String kieuVuongMac,
        @ToolParam(description = GIAI_DOAN, required = false) String giaiDoan,
        @ToolParam(description = DANG_MO, required = false) Boolean dangMoOnly,
        @ToolParam(description = CAN_BO, required = false) String canBo,
        @ToolParam(description = TU_NGAY_TAO, required = false) LocalDate fromDate,
        @ToolParam(description = DEN_NGAY_TAO, required = false) LocalDate toDate,
        @ToolParam(description = THIEU_NHAN_LUC, required = false) String thieuNhanLuc,
        @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
        @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return new VuongMacTimKiemResponse(vuongMacService.timKiem(
            new VuongMacFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, trangThai,
                kieuVuongMac, giaiDoan, dangMoOnly, canBo, fromDate, toDate, thieuNhanLuc), moTaChua, page, pageSize).danhSach());
    }

    @Tool(name = "vuongmac_xep_hang", description = """
        Xếp hạng vướng mắc đang mở theo hợp đồng hoặc khu vực
        Dùng khi: hợp đồng nào nhiều vướng mắc nhất; khu vực nào nhiều vướng mắc nhất; top hợp đồng vướng mắc đang mở
        Trả về: 1 bảng theo chiều chọn bằng tham số theo (hop_dong hoặc khu_vuc).
        Lưu ý: lọc 1 đối tượng cụ thể thì trả ghiChu thay cho bảng.
        """)
    public VuongMacXepHangResponse vuongMacXepHang(
        @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
        @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
        @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
        @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
        @ToolParam(description = KIEU, required = false) String kieuVuongMac,
        @ToolParam(description = GIAI_DOAN, required = false) String giaiDoan,
        @ToolParam(description = CAN_BO, required = false) String canBo,
        @ToolParam(description = TU_NGAY_TAO, required = false) LocalDate fromDate,
        @ToolParam(description = DEN_NGAY_TAO, required = false) LocalDate toDate,
        @ToolParam(description = "Chiều xếp hạng (bắt buộc, 1 giá trị): hop_dong (hợp đồng), khu_vuc (khu vực)") String theo,
        @ToolParam(description = "Số dòng của bảng, mặc định 5, tối đa 10", required = false) Integer top) {
        return vuongMacService.xepHang(new VuongMacFilter(doiTuong, null, nhaThau, null, tinhThanh, loaiHopDong, null,
            kieuVuongMac, giaiDoan, null, canBo, fromDate, toDate), theo, top);
    }

    @Tool(name = "vuongmac_qua_han", description = """
        Danh sách vướng mắc quá hạn tồn quá N ngày trên toàn hệ thống
        Dùng khi: vướng mắc nào tồn quá 30 ngày; vướng mắc nào để lâu chưa xử lý; danh sách vướng mắc quá hạn
        Trả về: vướng mắc tồn quá N ngày kể từ khi tạo (mặc định 30); có phân trang.
        Không dùng khi: cần lọc theo hợp đồng, khu vực, nhà thầu -> vuongmac_tim_kiem với dangMoOnly=true và fromDate/toDate.
        """)
    public VuongMacQuaHanResponse vuongMacQuaHan(
        @ToolParam(description = "Số ngày để coi vướng mắc là quá hạn, mặc định 30", required = false) Integer quaHanNgay,
        @ToolParam(description = "Thay cho quaHanNgay: quá hạn = tạo trước ngày này", required = false) LocalDate sinceDate,
        @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
        @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        var result = vuongMacService.quaHan(quaHanNgay, sinceDate, page, pageSize);
        return new VuongMacQuaHanResponse(result.soNgayQuaHan(), result.quaHan());
    }
}