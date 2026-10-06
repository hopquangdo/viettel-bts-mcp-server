package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.McpToolGroup;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.ToolParamDescriptions;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecDanhSachResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecSapHetGiaTriResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecXepHangResponse;

import java.time.LocalDate;

/**
 * MCP tool module Nguồn việc: mỗi tool nguonviec_* có method handler, tham số và DTO kết quả riêng.
 * Thay cho nguonviec_tool cũ (trả mọi khối trong 1 lần gọi).
 */
@Service
@RequiredArgsConstructor
public class NguonViecTools implements McpToolGroup {

    private static final String TRANG_THAI = "Trạng thái pháp lý cần lọc, ví dụ vuong_phap_ly";
    private static final String LOAI_CV = "Loại công việc: kstk (khảo sát thiết kế), tc (thi công), tk (thiết kế)";
    private static final String PHAP_LY = "Pháp lý hợp đồng: da_co (đã có), chua_co (chưa có), nhap (nháp)";
    private static final String LINH_VUC = "Lĩnh vực hợp đồng, ví dụ tu-van, kiem-dinh, tong (khớp đúng, không phân biệt hoa thường)";
    private static final String HOP_DONG_TIM = "Mã hợp đồng hoặc tên chương trình (một phần) cần tìm";

        private final NguonViecService nguonViecService;

    @Tool(name = "nguonviec_tong_quan", description = """
            Tổng quan nguồn việc theo khu vực, lĩnh vực, trạng thái, pháp lý
            Dùng khi: tổng giá trị hợp đồng nguồn việc; doanh thu đã hoàn thành; sản lượng còn lại theo hợp đồng; giá trị chưa kết thúc lên doanh thu
            Trả về: giá trị hợp đồng, sản lượng, doanh thu, giá trị còn lại, chưa khả thi, SL hủy, SL vướng; số hợp đồng theo tab (tất cả, đang triển khai/vướng pháp lý, gần hoàn thành).
            Lưu ý: nhãn màn hình -> trường: "Tổng giá trị HĐ" = giaTriHopDong; "SL đã thực hiện" = sanLuongThucHien; "DT đã hoàn thành" = doanhThu; "DT còn từ SL đã báo" = sanLuongChuaCoDoanhThu; "Chưa KT lên DT" / "chưa kết thúc lên doanh thu" = chuaKhaThi; "SL còn theo HĐ" / "sản lượng còn lại" = giaTriConLai; "SL hủy" = soLuongHuy; "SL vướng" = soVuongMacMo. Số liệu theo kỳ fromDate–toDate (mặc định cả năm hiện tại): người dùng nêu năm/kỳ thì truyền đúng kỳ đó. Lọc nhaThau không áp lên số tổng hợp (chỉ áp lên nguonviec_danh_sach).
            """)
    public NguonViecTongQuanResponse nguonViecTongQuan(
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = TRANG_THAI, required = false) String trangThai,
            @ToolParam(description = LOAI_CV, required = false) String loaiCv,
            @ToolParam(description = PHAP_LY, required = false) String phapLy,
            @ToolParam(description = LINH_VUC, required = false) String linhVuc,
            @ToolParam(description = HOP_DONG_TIM, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate) {
        return nguonViecService.tongQuan(
                new NguonViecFilter(khuVuc, null, trangThai, loaiCv, phapLy, linhVuc, hopDong, fromDate, toDate));
    }

    @Tool(name = "nguonviec_danh_sach", description = """
            Danh sách hợp đồng nguồn việc theo khu vực, nhà thầu, trạng thái, loại công việc, pháp lý, lĩnh vực
            Dùng khi: liệt kê hợp đồng nguồn việc; hợp đồng nguồn việc vướng pháp lý; nguồn việc của trung tâm X gồm những hợp đồng nào
            Trả về: mã HĐ, chương trình, lĩnh vực, loại CV, pháp lý, giá trị, sản lượng, doanh thu, trạng thái, nhà thầu, trung tâm, ngày ký/kết thúc; có phân trang.
            """)
    public NguonViecDanhSachResponse nguonViecDanhSach(
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = TRANG_THAI, required = false) String trangThai,
            @ToolParam(description = LOAI_CV, required = false) String loaiCv,
            @ToolParam(description = PHAP_LY, required = false) String phapLy,
            @ToolParam(description = LINH_VUC, required = false) String linhVuc,
            @ToolParam(description = "Tab: all (tất cả), active (đang triển khai hoặc vướng pháp lý), nearly (gần hoàn thành)", required = false) String tab,
            @ToolParam(description = HOP_DONG_TIM, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return nguonViecService.danhSach(
                new NguonViecFilter(khuVuc, nhaThau, trangThai, loaiCv, phapLy, linhVuc, hopDong, fromDate, toDate),
                tab, page, pageSize);
    }

    @Tool(name = "nguonviec_xep_hang", description = """
            Xếp hạng nguồn việc theo rủi ro hợp đồng hoặc trung tâm
            Dùng khi: hợp đồng nguồn việc nào rủi ro nhất; trung tâm nào sản lượng thực hiện cao nhất; top hợp đồng rủi ro
            Trả về: theo=rui_ro: top hợp đồng rủi ro cao nhất (điểm rủi ro); theo=trung_tam: mọi trung tâm theo sản lượng thực hiện.
            Lưu ý: mỗi lần gọi trả 1 bảng; tính trên toàn bộ hợp đồng nguồn việc, chỉ lọc theo khoảng thời gian.
            """)
    public NguonViecXepHangResponse nguonViecXepHang(
            @ToolParam(description = "Chiều xếp hạng (bắt buộc, 1 giá trị): rui_ro (hợp đồng rủi ro cao), trung_tam (trung tâm theo sản lượng)") String theo,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = "Số hợp đồng top khi theo=rui_ro, mặc định 5, tối đa 10", required = false) Integer top) {
        return nguonViecService.xepHang(theo, fromDate, toDate, top);
    }

    @Tool(name = "nguonviec_sap_het_gia_tri", description = """
            Danh sách hợp đồng nguồn việc sắp hết giá trị được giao
            Dùng khi: hợp đồng nào sắp hết giá trị; hợp đồng còn dưới 10% giá trị; hợp đồng nào còn ít giá trị nhất
            Trả về: tối đa 10 hợp đồng, ít giá trị còn lại nhất lên đầu.
            Lưu ý: giá trị còn lại từ ngưỡng % trở xuống so với giá trị hợp đồng (mặc định 10%); tính trên toàn hệ thống trong kỳ.
            """)
    public NguonViecSapHetGiaTriResponse nguonViecSapHetGiaTri(
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = "Ngưỡng % giá trị hợp đồng còn lại để tính là sắp hết giá trị, mặc định 10", required = false) Double nguongSapHet) {
        return nguonViecService.sapHetGiaTri(fromDate, toDate, nguongSapHet);
    }
}