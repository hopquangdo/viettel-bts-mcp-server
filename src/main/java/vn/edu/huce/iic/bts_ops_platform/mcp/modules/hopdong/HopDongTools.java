package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.McpToolGroup;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.ToolParamDescriptions;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.*;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class HopDongTools implements McpToolGroup {

    private static final String KIEU_HOP_DONG = "Kiểu hợp đồng cần lọc: ID, mã hoặc tên (mơ hồ sẽ báo lỗi kèm ứng viên)";

    private final HopDongService hopDongService;

    @Tool(name = "hopdong_tong_quan", description = """
        Tổng quan hợp đồng theo trạng thái thi công, loại, kiểu hợp đồng
        Dùng khi: bao nhiêu hợp đồng chậm tiến độ; bao nhiêu hợp đồng có vướng mắc; tiến độ trung bình các hợp đồng; bao nhiêu hợp đồng đang thi công; bao nhiêu đối tượng hoàn thành trong kỳ
        Trả về: số hợp đồng, số đối tượng, tỷ lệ hoàn thành, tiến độ theo trạng thái thi công, số hợp đồng chậm tiến độ, số vướng mắc đang mở (soHopDongVuongMac), số lượng theo kiểu và loại hợp đồng.
        Lưu ý: truyền fromDate/toDate để có thêm doiTuongHoanThanhTrongKy (số đối tượng hoàn thành trong kỳ). Khớp màn Tiến độ hợp đồng: "Tổng hợp đồng", "Tổng đối tượng", "Tiến độ trung bình", "Chậm tiến độ", "Có vướng mắc" (= soHopDongVuongMac), số HĐ HT/Đang/Chưa (= tienDoTheoTrangThai).
        Không dùng khi: chi tiết từng vướng mắc -> vuongmac_tim_kiem.
        """)
    public HopDongTongQuanToolResponse hopDongTongQuan(
        @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
        @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
        @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
        @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
        @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
        @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
        @ToolParam(description = KIEU_HOP_DONG, required = false) String kieuHopDong,
        @ToolParam(description = "Ngưỡng % tỷ lệ hoàn thành của hợp đồng, dưới mức này là chậm tiến độ (soHopDongChamTienDo), mặc định 70", required = false) Double nguongChamTienDo,
        @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
        @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate) {
    return hopDongService.tongQuan(new HopDongFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, kieuHopDong),
        nguongChamTienDo, fromDate, toDate);
    }

    @Tool(name = "hopdong_thong_ke_theo_loai", description = """
        Tổng quan hợp đồng theo từng loại hợp đồng (lĩnh vực)
        Dùng khi: mỗi loại hợp đồng có bao nhiêu hợp đồng; loại hợp đồng nào chậm nhất; so sánh các lĩnh vực trên Dashboard; lĩnh vực kiểm định có bao nhiêu đối tượng
        Trả về: mỗi loại: số hợp đồng, số đối tượng, tiến độ trung bình, số hợp đồng chậm tiến độ, số vướng mắc đang mở, số hợp đồng theo trạng thái thi công.
        Lưu ý: tên loại hợp đồng chính là "lĩnh vực" trên Dashboard. Thẻ lĩnh vực (Kiểm định, Tư vấn thiết kế, Gia cố củng cố, Xây mới): số HĐ = soHopDong, số đối tượng = soDoiTuongHoatDong (chi tiết "858 Tuyến, 39 Trạm" = soDoiTuongTheoKieu), sản lượng = tongSanLuong, đang vướng = soVuongMacDangVuong (chỉ có khi không lọc). Số liệu mỗi loại khớp màn Tiến độ hợp đồng khi lọc loại đó.
        Không dùng khi: hỏi 1 loại cụ thể -> hopdong_tong_quan với loaiHopDong.
        """)
    public HopDongTheoLoaiResponse hopDongThongKeTheoLoai(
        @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
        @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
        @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
        @ToolParam(description = KIEU_HOP_DONG, required = false) String kieuHopDong,
        @ToolParam(description = "Ngưỡng % tỷ lệ hoàn thành, dưới mức này là chậm tiến độ, mặc định 70", required = false) Double nguongChamTienDo) {
    return hopDongService.thongKeTheoLoai(new HopDongFilter(null, null, nhaThau, khuVuc, tinhThanh, null, kieuHopDong),
        nguongChamTienDo);
    }

    @Tool(name = "hopdong_tim_kiem", description = """
        Danh sách hợp đồng theo nhà thầu, tỉnh, loại, kiểu, ngày bắt đầu hoặc hạn hợp đồng
        Dùng khi: tìm hợp đồng theo tên; liệt kê hợp đồng của nhà thầu A; hợp đồng nào hết hạn tháng này; hợp đồng đang thi công ở tỉnh X
        Trả về: mã, tên, giá trị, trạng thái thi công, loại, kiểu; có phân trang.
        Lưu ý: lọc ngày theo ngày bắt đầu thực hiện (mặc định) hoặc hạn hợp đồng (loaiNgay=han_hop_dong).
        """)
    public HopDongTimKiemResponse hopDongTimKiem(
        @ToolParam(description = "Mã hoặc tên (một phần) hợp đồng cần tìm; khớp nhiều hợp đồng thì trả tất cả", required = false) String hopDong,
        @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
        @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
        @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
        @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
        @ToolParam(description = KIEU_HOP_DONG, required = false) String kieuHopDong,
        @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
        @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
        @ToolParam(description = "Cột ngày dùng cho fromDate/toDate: ngay_thuc_hien (mặc định, ngày bắt đầu thực hiện) hoặc han_hop_dong (hạn hoàn thành hợp đồng)", required = false) String loaiNgay,
        @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
        @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
    return hopDongService.timKiem(new HopDongFilter(doiTuong, null, nhaThau, null, tinhThanh, loaiHopDong, kieuHopDong),
        hopDong, fromDate, toDate, loaiNgay, page, pageSize);
    }

    @Tool(name = "hopdong_thong_ke_doi_tuong", description = """
        Tổng quan số đối tượng (trạm/tuyến) theo trạng thái, khu vực, tỉnh, nhà thầu, hợp đồng
        Dùng khi: khu vực X có bao nhiêu trạm, xong bao nhiêu; nhà thầu A làm bao nhiêu trạm; bao nhiêu trạm đang thi công
        Trả về: tổng, hoàn thành, đang thi công, có vướng mắc mở.
        """)
    public HopDongTrangThaiKhuVucResponse hopDongThongKeDoiTuong(
        @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
        @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
        @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
        @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
        @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
        @ToolParam(description = KIEU_HOP_DONG, required = false) String kieuHopDong) {
    return hopDongService.thongKeThuHep(new HopDongFilter(null, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, kieuHopDong));
    }

    @Tool(name = "hopdong_xep_hang", description = """
        Xếp hạng hợp đồng theo khu vực, hoặc tỉnh trong một hợp đồng
        Dùng khi: khu vực nào nhiều hợp đồng nhất; trong hợp đồng X tỉnh nào hoàn thành cao nhất; xếp hạng tỉnh theo số đối tượng của hợp đồng
        Trả về: theo=khu_vuc: khu vực theo số hợp đồng (kèm số đối tượng, số vướng mắc); theo=tinh: tỉnh trong 1 hợp đồng theo số đối tượng (kèm số hoàn thành, tỷ lệ hoàn thành, số vướng mắc).
        Lưu ý: mỗi lần gọi trả 1 bảng; theo=tinh cần truyền hopDong. Chiều trùng cấp bộ lọc hoặc lọc 1 đối tượng cụ thể: trả ghiChu kèm gợi ý chiều khác.
        """)
    public HopDongXepHangResponse hopDongXepHang(
        @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
        @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
        @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
        @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
        @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
        @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
        @ToolParam(description = KIEU_HOP_DONG, required = false) String kieuHopDong,
        @ToolParam(description = "Chiều xếp hạng (bắt buộc, 1 giá trị): khu_vuc (khu vực), tinh (tỉnh/thành trong 1 hợp đồng, cần hopDong)") String theo,
        @ToolParam(description = "Số dòng của bảng xếp hạng, mặc định và tối đa 20", required = false) Integer top) {
    return hopDongService.xepHang(new HopDongFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, kieuHopDong), theo, top);
    }

    @Tool(name = "hopdong_theo_buoc", description = """
        Chi tiết hợp đồng theo bước triển khai và lịch sử chuyển bước
        Dùng khi: hợp đồng X đang ở bước nào; mỗi bước có bao nhiêu đối tượng; ai chuyển bước hợp đồng X lúc nào
        Trả về: số đối tượng ở từng bước, lịch sử chuyển bước (ngày, hành động, người thực hiện); có phân trang.
        """)
    public HopDongTheoBuocResponse hopDongTheoBuoc(
        @ToolParam(description = ToolParamDescriptions.HOP_DONG) String hopDong,
        @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
        @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
    return hopDongService.theoBuoc(hopDong, page, pageSize);
    }

        @Tool(name = "hopdong_canh_bao_tien_do", description = """
            Danh sách hợp đồng cảnh báo tiến độ khối lượng mức xanh, vàng, đỏ
            Dùng khi: hợp đồng nào có nguy cơ trễ tiến độ; bao nhiêu hợp đồng đỏ vàng xanh; hợp đồng nào báo đỏ về khối lượng
            Trả về: số hợp đồng Xanh / Vàng / Đỏ, danh sách hợp đồng Đỏ kèm % hoàn thành.
            """)
        public HopDongCanhBaoTienDoResponse hopDongCanhBaoTienDo(
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
            @ToolParam(description = KIEU_HOP_DONG, required = false) String kieuHopDong,
            @ToolParam(description = "Ngưỡng % tiến độ khối lượng từ mức này trở lên là cảnh báo Xanh, mặc định 90", required = false) Double nguongXanh,
            @ToolParam(description = "Ngưỡng % tiến độ khối lượng từ mức này trở lên (và dưới nguongXanh) là cảnh báo Vàng, mặc định 70", required = false) Double nguongVang) {
        return hopDongService.canhBaoTienDo(new HopDongFilter(null, hopDong, nhaThau, null, tinhThanh, loaiHopDong, kieuHopDong),
            nguongXanh, nguongVang);
    }
}