package vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.McpToolGroup;
import vn.edu.huce.iic.bts_ops_platform.mcp.definitions.ToolParamDescriptions;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.dto.*;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DoiTuongTonTools implements McpToolGroup {

    private static final String TAB = "Chọn nhóm tồn để liệt kê: cho_quyet_toan (chờ quyết toán), qua_han (quá hạn), chua_phap_ly (hợp đồng chưa đủ pháp lý), vuong_mac (vướng mắc quá ngưỡng). Bỏ trống: chờ quyết toán và quá hạn";
    private static final String QUA_HAN_NGAY = "Ngưỡng số ngày để tính quá hạn (và vướng mắc quá ngưỡng), mặc định 90 ngày";

        private final DoiTuongTonService doiTuongTonService;

    @Tool(name = "doituongton_tong_quan", description = """
            Tổng quan đối tượng tồn theo tuổi tồn, lý do tồn, điều kiện quyết toán
            Dùng khi: có bao nhiêu trạm tồn; giá trị tồn bao nhiêu; bao nhiêu trạm tồn chờ quyết toán; tồn đọng theo lý do; tình hình tồn đọng
            Trả về: tổng số, đủ điều kiện (chờ quyết toán, quá hạn), chưa đủ điều kiện (chưa pháp lý, vướng mắc), giá trị tồn, theoTuoiTon, theoLyDo.
            Không dùng khi: cần danh sách từng trạm tồn -> doituongton_danh_sach.
            """)
    public TramTonTongQuanToolResponse doiTuongTonTongQuan(
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
            @ToolParam(description = QUA_HAN_NGAY, required = false) Integer quaHanNgay,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate) {
        return doiTuongTonService.tongQuan(new TramTonFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong),
                quaHanNgay, fromDate, toDate);
    }

    @Tool(name = "doituongton_danh_sach", description = """
            Danh sách đối tượng (trạm) tồn theo nhóm tab, hợp đồng, khu vực, nhà thầu, số ngày tồn
            Dùng khi: trạm nào đang tồn; liệt kê trạm tồn chờ quyết toán; trạm tồn quá N ngày; nhà thầu A có trạm nào tồn
            Trả về: mã trạm, hợp đồng, khu vực, nhà thầu, giá trị, số ngày tồn, trạng thái, lý do tồn; có phân trang.
            Không dùng khi: chỉ cần số tổng hợp -> doituongton_tong_quan.
            """)
    public TramTonDanhSachResponse doiTuongTonDanhSach(
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
            @ToolParam(description = TAB, required = false) String tab,
            @ToolParam(description = QUA_HAN_NGAY, required = false) Integer quaHanNgay,
            @ToolParam(description = ToolParamDescriptions.FROM_DATE, required = false) LocalDate fromDate,
            @ToolParam(description = ToolParamDescriptions.TO_DATE, required = false) LocalDate toDate,
            @ToolParam(description = ToolParamDescriptions.PAGE, required = false) Integer page,
            @ToolParam(description = ToolParamDescriptions.PAGE_SIZE, required = false) Integer pageSize) {
        return doiTuongTonService.danhSach(new TramTonFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong),
                tab, quaHanNgay, fromDate, toDate, page, pageSize);
    }

    @Tool(name = "doituongton_thieu_cap_nhat", description = """
            Danh sách đối tượng (trạm) chậm cập nhật thi công quá N ngày
            Dùng khi: trạm nào lâu không cập nhật thi công; trạm nào bỏ cập nhật tiến độ; đối tượng không cập nhật quá 7 ngày
            Trả về: mã trạm, hợp đồng, khu vực, nhà thầu, ngày thi công gần nhất, số ngày không cập nhật.
            Lưu ý: mặc định N = 7 ngày.
            Không dùng khi: nhà thầu chưa báo sản lượng trong kỳ -> sanluong_nha_thau_bao_cao.
            """)
    public TramTonThieuCapNhatResponse doiTuongTonThieuCapNhat(
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
            @ToolParam(description = "Ngưỡng số ngày (đối tượng không cập nhật quá số ngày này), mặc định 7; nếu bỏ trống có thể dùng sinceDate", required = false) Integer soNgayThieuCapNhat,
            @ToolParam(description = "Thay cho soNgayThieuCapNhat: truyền ngày cách đây N ngày (mặc định 7 ngày trước)", required = false) LocalDate sinceDate) {
        return doiTuongTonService.thieuCapNhat(new TramTonFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong),
                soNgayThieuCapNhat, sinceDate);
    }

    @Tool(name = "doituongton_san_luong_bat_thuong", description = """
            Danh sách đối tượng tồn có sản lượng bất thường so với hợp đồng
            Dùng khi: trạm tồn nào sản lượng lệch hợp đồng; rà soát sai lệch sản lượng trạm tồn; sản lượng hiệu lực không khớp giá trị hợp đồng
            Trả về: giá trị hợp đồng, ngày hoàn thành, sản lượng hiệu lực của từng đối tượng.
            Không dùng khi: trạm vượt ngưỡng bình quân hợp đồng (Dashboard) -> volume_tram_vuot_nguong.
            """)
    public TramTonSanLuongBatThuongResponse doiTuongTonSanLuongBatThuong(
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong) {
        return doiTuongTonService.sanLuongBatThuong(new TramTonFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong));
    }

    @Tool(name = "doituongton_xep_hang_khu_vuc", description = """
            Xếp hạng khu vực theo đối tượng tồn chờ quyết toán và giá trị tồn
            Dùng khi: khu vực nào tồn nhiều nhất; top khu vực tồn chờ quyết toán; khu vực nào giá trị tồn cao nhất
            Trả về: mỗi khu vực: số đối tượng tồn chờ quyết toán, giá trị tồn.
            Lưu ý: lọc theo 1 đối tượng cụ thể (mã trạm/tuyến) thì không có bảng xếp hạng.
            """)
    public TramTonXepHangKhuVucResponse doiTuongTonXepHangKhuVuc(
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong,
            @ToolParam(description = "Số dòng xếp hạng, mặc định 5, tối đa 20", required = false) Integer top) {
        return doiTuongTonService.xepHangKhuVuc(new TramTonFilter(doiTuong, hopDong, nhaThau, null, tinhThanh, loaiHopDong), top);
    }

    @Tool(name = "doituongton_thieu_dieu_kien", description = """
            Danh sách đối tượng tồn thiếu nhiều điều kiện quyết toán
            Dùng khi: trạm nào khó quyết toán; đối tượng thiếu từ 2 điều kiện quyết toán; trạm vừa chưa đủ pháp lý vừa đang vướng mắc
            Trả về: tối đa 20 đối tượng thiếu từ 2 điều kiện trở lên (chưa đủ pháp lý, đang vướng mắc, chưa có sản lượng hiệu lực).
            """)
    public TramTonThieuDieuKienResponse doiTuongTonThieuDieuKien(
            @ToolParam(description = ToolParamDescriptions.DOI_TUONG, required = false) String doiTuong,
            @ToolParam(description = ToolParamDescriptions.HOP_DONG, required = false) String hopDong,
            @ToolParam(description = ToolParamDescriptions.NHA_THAU, required = false) String nhaThau,
            @ToolParam(description = ToolParamDescriptions.KHU_VUC, required = false) String khuVuc,
            @ToolParam(description = ToolParamDescriptions.TINH_THANH, required = false) String tinhThanh,
            @ToolParam(description = ToolParamDescriptions.LOAI_HOP_DONG, required = false) String loaiHopDong) {
        return doiTuongTonService.thieuDieuKien(new TramTonFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong));
    }
}