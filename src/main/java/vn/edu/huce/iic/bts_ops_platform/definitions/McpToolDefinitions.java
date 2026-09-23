package vn.edu.huce.iic.bts_ops_platform.definitions;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.dto.bienban.BienBanQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.hosodoituong.HoSoDoiTuongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.nganho.NganHoQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.nguonviec.NguonViecQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.nhathau.NhaThauQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.phancong.PhanCongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.sanluong.SanLuongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.common.SystemOverviewResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.tramton.TramTonQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.vuongmac.VuongMacQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.handler.BienBanToolHandler;
import vn.edu.huce.iic.bts_ops_platform.handler.HoSoDoiTuongToolHandler;
import vn.edu.huce.iic.bts_ops_platform.handler.HopDongToolHandler;
import vn.edu.huce.iic.bts_ops_platform.handler.NganHoToolHandler;
import vn.edu.huce.iic.bts_ops_platform.handler.NguonViecToolHandler;
import vn.edu.huce.iic.bts_ops_platform.handler.NhaThauToolHandler;
import vn.edu.huce.iic.bts_ops_platform.handler.PhanCongToolHandler;
import vn.edu.huce.iic.bts_ops_platform.handler.SanLuongToolHandler;
import vn.edu.huce.iic.bts_ops_platform.handler.TramTonToolHandler;
import vn.edu.huce.iic.bts_ops_platform.handler.VuongMacToolHandler;

import java.time.LocalDate;

/**
 * Định nghĩa TẤT CẢ MCP tool nghiệp vụ trong 1 chỗ — mỗi method chỉ là stub khai báo @Tool +
 * @ToolParam rồi forward sang handler tương ứng. Logic thật nằm ở
 * {@code common/tools/handler/impls/*}. Gộp 1 file để mọi mô tả tool cùng chỗ, dễ giữ nhất quán,
 * và {@code McpToolConfig} chỉ cần 1 tham số.
 */
@Service
@RequiredArgsConstructor
public class McpToolDefinitions {

    private final SanLuongToolHandler sanLuongToolHandler;
    private final HopDongToolHandler hopDongToolHandler;
    private final TramTonToolHandler doiTuongTonToolHandler;
    private final VuongMacToolHandler vuongMacToolHandler;
    private final PhanCongToolHandler phanCongToolHandler;
    private final BienBanToolHandler bienBanToolHandler;
    private final NguonViecToolHandler nguonViecToolHandler;
    private final NhaThauToolHandler nhaThauToolHandler;
    private final vn.edu.huce.iic.bts_ops_platform.handler.NhatKyToolHandler nhatKyToolHandler;
    private final NganHoToolHandler nganHoToolHandler;
    private final HoSoDoiTuongToolHandler hoSoDoiTuongToolHandler;


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
            @ToolParam(description = "Đối tượng cần lọc: ID, mã hoặc tên; nhận NHIỀU đối tượng ngăn cách bằng dấu phẩy (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên)", required = false) String doiTuong,
            @ToolParam(description = "Hợp đồng cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp đồng, ví dụ TVTK023241, 31032026VTK2025", required = false) String hopDong,
            @ToolParam(description = "Nhà thầu cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: Royal, Anh Tu; truyền đúng tên người dùng nói, hệ thống tự tra gần đúng, không cần hỏi lại", required = false) String nhaThau,
            @ToolParam(description = "Khu vực cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp lệ: TTKV1, TTKV2, TTKV3. Người dùng viết \"kv1\", \"khu vực 1\", \"TTKV 1\" thì truyền TTKV1, không hỏi lại", required = false) String khuVuc,
            @ToolParam(description = "Tỉnh/thành cần lọc: ID, mã hoặc tên. Ví dụ: BNH (Bắc Ninh), NAN (Nghệ An), THA (Thanh Hóa)", required = false) String tinhThanh,
            @ToolParam(description = "Ngày bắt đầu của khoảng thời gian tính sản lượng", required = false) LocalDate fromDate,
            @ToolParam(description = "Ngày kết thúc của khoảng thời gian tính sản lượng", required = false) LocalDate toDate,
            @ToolParam(description = "Trang kết quả (0-based) cho nhaThauChuaBaoTrongKy/nhaThauDaBaoTrongKy, mặc định 0", required = false) Integer page,
            @ToolParam(description = "Số dòng mỗi trang, mặc định 3, tối đa 100", required = false) Integer pageSize,
            @ToolParam(description = "Ngưỡng % hoàn thành hạng mục: đối tượng đang thi công dưới mức này tính vào lowCompletionCount, mặc định 50", required = false) Double nguongHoanThanhThap,
            @ToolParam(description = "false: chỉ liệt kê đối tượng đã có sản lượng trong kỳ ở topDoiTuong; bỏ trống hoặc true: gồm cả đối tượng chưa có sản lượng", required = false) Boolean includeWithoutOutput,
            @ToolParam(description = "Sắp xếp danhSachDoiTuong (bảng từng đối tượng: mã, tỉnh, khu vực, hợp đồng, nhà thầu, SL tổng, SL hôm nay, thi công gần nhất, hạng mục, vướng mắc; chỉ gồm đối tượng có bản ghi trong kỳ, trừ khi includeWithoutOutput=true; phân trang theo page/pageSize) giảm dần theo: latest (thi công gần nhất, mặc định), periodValue (giá trị trong kỳ), totalValue (SL tổng), todayValue (SL hôm nay)", required = false) String sapXep,
            @ToolParam(description = "Loại hợp đồng (lĩnh vực) cần lọc: ID, mã hoặc tên", required = false) String loaiHopDong,
            @ToolParam(description = "Sắp xếp các bảng xếp hạng (nhaThau, khuVuc, tinh, hopDong, loaiHopDong) theo: giaTri (mặc định), hoanThanh (% hoàn thành), soDoiTuong, vuongMac. Lọc theo cấp nào thì bảng cấp đó được bỏ (vd lọc khuVuc thì không có khuVuc, chỉ có tinh, hopDong, nhaThau… trong khu vực đó); lọc 1 đối tượng cụ thể thì không có bảng xếp hạng nào, chỉ có hangMucDoiTuong", required = false) String xepHangTheo,
            @ToolParam(description = "true: xếp hạng tăng dần (mặc định giảm dần)", required = false) Boolean tangDan) {
        return sanLuongToolHandler.query(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, fromDate, toDate, page, pageSize, nguongHoanThanhThap, includeWithoutOutput, sapXep, loaiHopDong, xepHangTheo, tangDan);
    }

    // ------------------------------------------------------------------ hopdong_tool

    @Tool(
            name = "hopdong_tool",
            description = """
                    Mục đích: Báo cáo hợp đồng theo trạng thái, tiến độ, khu vực, nhà thầu hoặc từ khóa tìm kiếm.
                    Dùng khi: cần thống kê tổng quan, tìm hợp đồng, xem tiến độ, cảnh báo chậm hoặc drill-down theo khu vực/tỉnh.
                    Trả về: tongQuan, danhSach, thongKeThuHep, canhBaoTienDo, xepHangKhuVuc, xepHangTinh và theoBuoc.
                    Quy tắc xếp hạng: hopDong -> xepHangHopDong = null; khuVuc -> xepHangKhuVuc = null; các bộ lọc khác không cùng cấp vẫn trả [] nếu không có dữ liệu; lọc 1 đối tượng cụ thể bỏ tất cả bảng xếp hạng. Xếp hạng chỉ tính vướng mắc đang mở và áp dụng nhaThau, tinhThanh, loaiHopDong, giaiDoan, kieuVuongMac; trangThai, dangMoOnly, query và sinceDate chỉ áp cho các khối tương ứng.
                    """
    )
    public HopDongQueryResponse hopDong(
            @ToolParam(description = "Đối tượng cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã trạm/tuyến, ví dụ HNI1335, THA1948, NBH0306-11", required = false) String doiTuong,
            @ToolParam(description = "Hợp đồng cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp đồng, ví dụ TVTK023241, 31032026VTK2025", required = false) String hopDong,
            @ToolParam(description = "Nhà thầu cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: Royal, Anh Tu; truyền đúng tên người dùng nói, hệ thống tự tra gần đúng, không cần hỏi lại", required = false) String nhaThau,
            @ToolParam(description = "Khu vực cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp lệ: TTKV1, TTKV2, TTKV3. Người dùng viết \"kv1\", \"khu vực 1\", \"TTKV 1\" thì truyền TTKV1, không hỏi lại", required = false) String khuVuc,
            @ToolParam(description = "Tỉnh/thành cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: BNH (Bắc Ninh), NAN (Nghệ An), THA (Thanh Hóa)", required = false) String tinhThanh,
            @ToolParam(description = "Loại hợp đồng cần lọc: ID, mã hoặc tên (mơ hồ sẽ báo lỗi kèm ứng viên)", required = false) String loaiHopDong,
            @ToolParam(description = "Kiểu hợp đồng cần lọc: ID, mã hoặc tên (mơ hồ sẽ báo lỗi kèm ứng viên)", required = false) String kieuHopDong,
            @ToolParam(description = "Từ khóa tìm theo mã hoặc tên hợp đồng (trả nhiều kết quả; muốn đúng một hợp đồng hãy dùng hopDong)", required = false) String query,
            @ToolParam(description = "Trang kết quả (0-based) cho danhSach/theoBuoc.lichSu, mặc định 0", required = false) Integer page,
            @ToolParam(description = "Số dòng mỗi trang, mặc định 3, tối đa 100", required = false) Integer pageSize,
            @ToolParam(description = "Ngưỡng % tỷ lệ hoàn thành của hợp đồng, dưới mức này là chậm tiến độ (soHopDongChamTienDo), mặc định 70", required = false) Double nguongChamTienDo,
            @ToolParam(description = "Ngưỡng % tiến độ khối lượng từ mức này trở lên là cảnh báo Xanh, mặc định 90", required = false) Double nguongXanh,
            @ToolParam(description = "Ngưỡng % tiến độ khối lượng từ mức này trở lên (và dưới nguongXanh) là cảnh báo Vàng, dưới mức này là Đỏ, mặc định 70", required = false) Double nguongVang,
            @ToolParam(description = "Số dòng cho các bảng xếp hạng khu vực/tỉnh, mặc định và tối đa 20", required = false) Integer top,
            @ToolParam(description = "Từ ngày (yyyy-MM-dd): lọc danhSach hợp đồng có ngày bắt đầu thực hiện từ ngày này (hợp đồng mới ký/mới triển khai), hoặc hạn hợp đồng nếu loaiNgay=han_hop_dong; đồng thời đếm doiTuongHoanThanhTrongKy (đối tượng hoàn thành thi công trong kỳ)", required = false) LocalDate fromDate,
            @ToolParam(description = "Đến ngày (yyyy-MM-dd, gồm cả ngày này), cùng ý nghĩa với fromDate; ví dụ 'sắp hết hạn trong 30 ngày tới' thì loaiNgay=han_hop_dong, fromDate=hôm nay, toDate=hôm nay+30", required = false) LocalDate toDate,
            @ToolParam(description = "Cột ngày dùng cho fromDate/toDate của danhSach: ngay_thuc_hien (mặc định, ngày bắt đầu thực hiện) hoặc han_hop_dong (hạn hoàn thành hợp đồng)", required = false) String loaiNgay) {
        return hopDongToolHandler.query(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, kieuHopDong, query, page, pageSize, nguongChamTienDo, nguongXanh, nguongVang, top, fromDate, toDate, loaiNgay);
    }

    // ------------------------------------------------------------------ doituongton_tool

    @Tool(
            name = "doituongton_tool",
            description = """
                    Mục đích: Báo cáo đối tượng tồn theo lý do tồn, thời gian tồn, khu vực và nhà thầu.
                    Dùng khi: cần thống kê tồn đọng, tìm đối tượng chậm cập nhật, bất thường hoặc thiếu điều kiện quyết toán.
                    Trả về: tongQuan, danhSach, thieuCapNhat, sanLuongBatThuong, xepHangKhuVuc và thieuNhieuDieuKien.
                    Quy tắc xếp hạng: lọc khuVuc hoặc 1 đối tượng cụ thể (mã trạm/tuyến) -> xepHangKhuVuc = null. xepHangKhuVuc, sanLuongBatThuong, thieuCapNhat và thieuNhieuDieuKien theo nhaThau, hopDong, khuVuc, tinhThanh, loaiHopDong, doiTuong;
                    không theo tab và quaHanNgay (xếp hạng tính trên nhóm chờ quyết toán).
                    """
    )
    public TramTonQueryResponse doiTuongTon(
            @ToolParam(description = "Đối tượng cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã trạm/tuyến, ví dụ HNI1335, THA1948, NBH0306-11", required = false) String doiTuong,
            @ToolParam(description = "Hợp đồng cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp đồng, ví dụ TVTK023241, 31032026VTK2025", required = false) String hopDong,
            @ToolParam(description = "Nhà thầu cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: Royal, Anh Tu; truyền đúng tên người dùng nói, hệ thống tự tra gần đúng, không cần hỏi lại", required = false) String nhaThau,
            @ToolParam(description = "Khu vực cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp lệ: TTKV1, TTKV2, TTKV3. Người dùng viết \"kv1\", \"khu vực 1\", \"TTKV 1\" thì truyền TTKV1, không hỏi lại", required = false) String khuVuc,
            @ToolParam(description = "Tỉnh/thành cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: BNH (Bắc Ninh), NAN (Nghệ An), THA (Thanh Hóa)", required = false) String tinhThanh,
            @ToolParam(description = "Loại hợp đồng cần lọc: ID, mã hoặc tên (mơ hồ sẽ báo lỗi kèm ứng viên)", required = false) String loaiHopDong,
            @ToolParam(description = "Chọn nhóm tồn để liệt kê trong danhSach: cho_quyet_toan (chờ quyết toán), qua_han (quá hạn), chua_phap_ly (hợp đồng chưa đủ pháp lý), vuong_mac (vướng mắc quá ngưỡng). Bỏ trống: chờ quyết toán và quá hạn", required = false) String tab,
            @ToolParam(description = "Ngưỡng ngày cho thieuCapNhat, truyền ngày cách đây N ngày (mặc định 7 ngày trước)", required = false) LocalDate sinceDate,
                        @ToolParam(description = "Ngưỡng số ngày để tính quá hạn trong tongQuan, mặc định 90 ngày", required = false) Integer quaHanNgay,
                        @ToolParam(description = "Số dòng xepHangKhuVuc, mặc định 5, tối đa 20", required = false) Integer top,
                        @ToolParam(description = "Số trang, bắt đầu từ 0", required = false) Integer page,
                        @ToolParam(description = "Số dòng mỗi trang, mặc định 3, tối đa 100", required = false) Integer pageSize,
            @ToolParam(description = "Ngưỡng số ngày cho thieuCapNhat (đối tượng không cập nhật quá số ngày này), mặc định 7; nếu bỏ trống có thể dùng sinceDate", required = false) Integer soNgayThieuCapNhat,
            @ToolParam(description = "Chỉ tính đối tượng hoàn thành thi công từ ngày này (yyyy-MM-dd); áp cho nhóm chờ quyết toán/quá hạn trong tongQuan và danhSach", required = false) LocalDate fromDate,
            @ToolParam(description = "Chỉ tính đối tượng hoàn thành thi công đến ngày này (yyyy-MM-dd, gồm cả ngày này); áp cho nhóm chờ quyết toán/quá hạn trong tongQuan và danhSach", required = false) LocalDate toDate) {
                return doiTuongTonToolHandler.query(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, tab, sinceDate, quaHanNgay, top, page, pageSize, soNgayThieuCapNhat, fromDate, toDate);
    }

    // ------------------------------------------------------------------ vuongmac_tool

    @Tool(
            name = "vuongmac_tool",
            description = """
                    Mục đích: Báo cáo vướng mắc theo loại, trạng thái, nhà thầu, hợp đồng hoặc khu vực.
                    Dùng khi: cần thống kê tổng quan, tìm vướng mắc, xem quá hạn hoặc top hợp đồng/khu vực có nhiều sự cố.
                    Trả về: tongQuan, danhSach, xepHangHopDong, xepHangKhuVuc và quaHan.
                    Quy tắc xếp hạng: hopDong -> xepHangHopDong = null; khuVuc -> xepHangKhuVuc = null; các bộ lọc khác không cùng cấp vẫn trả [] nếu không có dữ liệu; lọc 1 đối tượng cụ thể bỏ tất cả bảng xếp hạng.
                    """
    )
    public VuongMacQueryResponse vuongMac(
            @ToolParam(description = "Đối tượng cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã trạm/tuyến, ví dụ HNI1335, THA1948, NBH0306-11", required = false) String doiTuong,
            @ToolParam(description = "Hợp đồng cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp đồng, ví dụ TVTK023241, 31032026VTK2025", required = false) String hopDong,
            @ToolParam(description = "Nhà thầu cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: Royal, Anh Tu; truyền đúng tên người dùng nói, hệ thống tự tra gần đúng, không cần hỏi lại", required = false) String nhaThau,
            @ToolParam(description = "Khu vực cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp lệ: TTKV1, TTKV2, TTKV3. Người dùng viết \"kv1\", \"khu vực 1\", \"TTKV 1\" thì truyền TTKV1, không hỏi lại", required = false) String khuVuc,
            @ToolParam(description = "Tỉnh/thành cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: BNH (Bắc Ninh), NAN (Nghệ An), THA (Thanh Hóa)", required = false) String tinhThanh,
            @ToolParam(description = "Loại hợp đồng cần lọc: ID, mã hoặc tên (mơ hồ sẽ báo lỗi kèm ứng viên)", required = false) String loaiHopDong,
            @ToolParam(description = "Trạng thái vướng mắc: pending (chờ xử lý), in_progress (đang xử lý), resolved (đã xử lý), rejected (từ chối)", required = false) String trangThai,
            @ToolParam(description = "Loại vướng mắc: giai_phong_mat_bang, thiet_ke, thu_tuc_phap_ly, nguon_vat_lieu, thi_cong", required = false) String kieuVuongMac,
            @ToolParam(description = "Giai đoạn phát sinh vướng mắc (khớp đúng, không phân biệt hoa thường)", required = false) String giaiDoan,
            @ToolParam(description = "true: chỉ vướng mắc đang mở (chờ xử lý hoặc đang xử lý)", required = false) Boolean dangMoOnly,
            @ToolParam(description = "Từ khóa tìm theo mã hoặc mô tả vướng mắc", required = false) String query,
            @ToolParam(description = "Số lượng phần tử top được trả về cho ranking, mặc định 5", required = false) Integer top,
                        @ToolParam(description = "Ngưỡng ngày cho quaHan, truyền ngày cách đây N ngày (mặc định 30 ngày trước)", required = false) LocalDate sinceDate,
                        @ToolParam(description = "Số trang, bắt đầu từ 0", required = false) Integer page,
                            @ToolParam(description = "Số dòng mỗi trang, mặc định 3, tối đa 100", required = false) Integer pageSize,
            @ToolParam(description = "Ngưỡng số ngày để coi vướng mắc là quá hạn cho khối quaHan, mặc định 30 (nếu bỏ trống có thể dùng sinceDate)", required = false) Integer quaHanNgay,
            @ToolParam(description = "Cán bộ xử lý vướng mắc cần lọc: ID, tên hoặc tên đăng nhập, ví dụ \"Vinh\"", required = false) String canBo,
            @ToolParam(description = "Chỉ tính vướng mắc tạo từ ngày này (yyyy-MM-dd); đồng thời là đầu kỳ của resolvedTrongKy (số vướng đã xử lý trong kỳ)", required = false) LocalDate fromDate,
            @ToolParam(description = "Chỉ tính vướng mắc tạo đến ngày này (yyyy-MM-dd, gồm cả ngày này); đồng thời là cuối kỳ của resolvedTrongKy", required = false) LocalDate toDate) {
                return vuongMacToolHandler.query(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, trangThai, kieuVuongMac, giaiDoan, dangMoOnly, query, top, sinceDate, page, pageSize, quaHanNgay, canBo, fromDate, toDate);
    }

    // ------------------------------------------------------------------ phancong_tool

    @Tool(
            name = "phancong_tool",
            description = """
                    Mục đích: Báo cáo phân công công việc theo khu vực, cán bộ, nhà thầu, vùng hoặc hợp đồng.
                    Dùng khi: cần xem tổng quan phân công, công việc của cán bộ, đối tượng chưa giao nhà thầu hoặc top nhà thầu phụ trách nhiều việc.
                    Trả về: tongQuan, danhSach, theoCanBo, chuaPhanCong và xepHangNhaThau.
                    Quy tắc xếp hạng: lọc nhaThau, canBo hoặc 1 đối tượng cụ thể (mã trạm/tuyến, không phải loại đối tượng) -> xepHangNhaThau = null; các bộ lọc khác không cùng cấp vẫn trả [] nếu không có dữ liệu;
                    bảng xếp hạng theo hopDong, khuVuc, tinhThanh, doiTuong (loại), giaiDoan và vung; không theo 'query' (query chỉ để tìm danh sách).
                    """
    )
    public PhanCongQueryResponse phanCong(
            @ToolParam(description = "Mã vùng quản lý (không phải mã đối tượng cụ thể)", required = false) String vung,
            @ToolParam(description = "Nhà thầu cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: Royal, Anh Tu; truyền đúng tên người dùng nói, hệ thống tự tra gần đúng, không cần hỏi lại", required = false) String nhaThau,
            @ToolParam(description = "Cán bộ cần lọc: ID, mã hoặc tên", required = false) String canBo,
            @ToolParam(description = "Từ khóa tìm theo giai đoạn phân công (nhà thầu, vùng và hợp đồng đã có tham số riêng)", required = false) String query,
            @ToolParam(description = "Lọc chuaPhanCong theo hợp đồng: ID, mã hoặc tên", required = false) String hopDong,
            @ToolParam(description = "Khu vực cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp lệ: TTKV1, TTKV2, TTKV3. Người dùng viết \"kv1\", \"khu vực 1\", \"TTKV 1\" thì truyền TTKV1, không hỏi lại", required = false) String khuVuc,
            @ToolParam(description = "Tỉnh/thành cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: BNH (Bắc Ninh), NAN (Nghệ An), THA (Thanh Hóa)", required = false) String tinhThanh,
            @ToolParam(description = "Đối tượng cần lọc: ID, mã hoặc tên (mã trạm, mã tuyến, tên tòa…); dùng để xem phân công của một trạm cụ thể", required = false) String doiTuong,
            @ToolParam(description = "Giai đoạn phân công cần lọc (khớp đúng, không phân biệt hoa thường)", required = false) String giaiDoan,
            @ToolParam(description = "true: danhSach gồm cả phân công đã ngừng (lịch sử phân công của đối tượng), mặc định chỉ phân công đang hoạt động", required = false) Boolean lichSu,
                        @ToolParam(description = "Số dòng xepHangNhaThau, mặc định 5, tối đa 20", required = false) Integer top,
                        @ToolParam(description = "Số trang cho danhSach/theoCanBo/chuaPhanCong, bắt đầu từ 0", required = false) Integer page,
                        @ToolParam(description = "Số dòng mỗi trang, mặc định 3, tối đa 100", required = false) Integer pageSize,
            @ToolParam(description = "Chỉ tính phân công tạo từ ngày này (yyyy-MM-dd); áp cho tongQuan, danhSach và theoCanBo", required = false) LocalDate fromDate,
            @ToolParam(description = "Chỉ tính phân công tạo đến ngày này (yyyy-MM-dd, gồm cả ngày này); áp cho tongQuan, danhSach và theoCanBo", required = false) LocalDate toDate) {
                return phanCongToolHandler.query(vung, nhaThau, canBo, query, hopDong, khuVuc, tinhThanh, doiTuong, giaiDoan, lichSu, top, page, pageSize, fromDate, toDate);
    }

    // ------------------------------------------------------------------ bienban_tool

    @Tool(
            name = "bienban_tool",
            description = """
                    Mục đích: Báo cáo biên bản và hồ sơ hợp đồng theo trạng thái, khu vực, nhà thầu hoặc hợp đồng.
                    Dùng khi: cần thống kê biên bản, xem trạng thái duyệt, tìm thiếu khảo sát hoặc thiếu hồ sơ theo checklist.
                    Trả về: tongQuan, theoTrangThai, thieuKhaoSat và thieuHoSo.
                    """
    )
    public BienBanQueryResponse bienBan(
            @ToolParam(description = "Khu vực cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp lệ: TTKV1, TTKV2, TTKV3. Người dùng viết \"kv1\", \"khu vực 1\", \"TTKV 1\" thì truyền TTKV1, không hỏi lại", required = false) String khuVuc,
            @ToolParam(description = "Tỉnh/thành cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: BNH (Bắc Ninh), NAN (Nghệ An), THA (Thanh Hóa)", required = false) String tinhThanh,
            @ToolParam(description = "Hợp đồng cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp đồng, ví dụ TVTK023241, 31032026VTK2025", required = false) String hopDong,
            @ToolParam(description = "Nhà thầu cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: Royal, Anh Tu; truyền đúng tên người dùng nói, hệ thống tự tra gần đúng, không cần hỏi lại", required = false) String nhaThau,
                        @ToolParam(description = "Lọc theo trạng thái biên bản: cho_duyet | da_duyet | tu_choi (tu_choi = bị từ chối / không đạt)", required = false) String trangThai,
                        @ToolParam(description = "Đối tượng (trạm/tuyến) cần lọc: ID, mã hoặc tên, ví dụ THA1948. Biên bản lưu theo hợp đồng nên kết quả là biên bản của các hợp đồng chứa đối tượng này", required = false) String doiTuong,
                        @ToolParam(description = "Chỉ tính biên bản có ngày lập từ ngày này (yyyy-MM-dd), áp cho tongQuan và theoTrangThai", required = false) LocalDate fromDate,
                        @ToolParam(description = "Chỉ tính biên bản có ngày lập đến ngày này (yyyy-MM-dd, gồm cả ngày này), áp cho tongQuan và theoTrangThai", required = false) LocalDate toDate,
                        @ToolParam(description = "Số trang, bắt đầu từ 0", required = false) Integer page,
                        @ToolParam(description = "Số dòng mỗi trang, mặc định 3, tối đa 100", required = false) Integer pageSize) {
                return bienBanToolHandler.query(khuVuc, tinhThanh, hopDong, nhaThau, trangThai, doiTuong, fromDate, toDate, page, pageSize);
    }

    // ------------------------------------------------------------------ nguonviec_tool

    @Tool(
            name = "nguonviec_tool",
            description = """
                    Mục đích: Báo cáo nguồn việc theo trung tâm, nhà thầu, trạng thái pháp lý và khoảng thời gian lọc.
                    Dùng khi: cần xem tổng hợp nguồn việc, rủi ro pháp lý, top hợp đồng hoặc danh sách hợp đồng theo trung tâm.
                    Trả về: summary, tabCounts, danhSach, xepHangRuiRo, xepHangTrungTam và sapHetGiaTri.
                    """
    )
    public NguonViecQueryResponse nguonViec(
            @ToolParam(description = "Khu vực cần lọc: ID, mã hoặc tên. Mã hợp lệ: TTKV1, TTKV2, TTKV3. Người dùng viết \"kv1\", \"khu vực 1\", \"TTKV 1\" thì truyền TTKV1, không hỏi lại", required = false) String khuVuc,
            @ToolParam(description = "Nhà thầu cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: Royal, Anh Tu; truyền đúng tên người dùng nói, hệ thống tự tra gần đúng, không cần hỏi lại", required = false) String nhaThau,
            @ToolParam(description = "Trạng thái pháp lý cần lọc, ví dụ vuong_phap_ly", required = false) String trangThai,
            @ToolParam(description = "Loại công việc: kstk (khảo sát thiết kế), tc (thi công), tk (thiết kế)", required = false) String loaiCv,
            @ToolParam(description = "Pháp lý hợp đồng: da_co (đã có), chua_co (chưa có), nhap (nháp)", required = false) String phapLy,
            @ToolParam(description = "Lĩnh vực hợp đồng, ví dụ tu-van, kiem-dinh, tong (khớp đúng, không phân biệt hoa thường)", required = false) String linhVuc,
            @ToolParam(description = "Tab của danhSach: all (tất cả), active (đang triển khai hoặc vướng pháp lý), nearly (gần hoàn thành); tabCounts luôn trả đủ 3 tab", required = false) String tab,
            @ToolParam(description = "Ngày bắt đầu của khoảng thời gian lọc", required = false) LocalDate fromDate,
            @ToolParam(description = "Ngày kết thúc của khoảng thời gian lọc", required = false) LocalDate toDate,
            @ToolParam(description = "Từ khóa tìm theo tên chương trình hoặc mã hợp đồng", required = false) String query,
                        @ToolParam(description = "Số hợp đồng top xếp hạng rủi ro, mặc định 5, tối đa 10", required = false) Integer top,
                        @ToolParam(description = "Số trang danhSach, bắt đầu từ 0", required = false) Integer page,
                        @ToolParam(description = "Số dòng mỗi trang, mặc định 3, tối đa 100", required = false) Integer pageSize,
            @ToolParam(description = "Ngưỡng % giá trị hợp đồng còn lại để tính là sắp hết giá trị (sapHetGiaTri), mặc định 10", required = false) Double nguongSapHet) {
                return nguonViecToolHandler.query(khuVuc, nhaThau, trangThai, loaiCv, phapLy, linhVuc, tab, fromDate, toDate, query, top, page, pageSize, nguongSapHet);
    }

    // ------------------------------------------------------------------ nhathau_tool

    @Tool(
            name = "nhathau_tool",
            description = """
                    Mục đích: Liệt kê các nhà thầu đang phụ trách đối tượng (có những nhà thầu nào, bao nhiêu nhà thầu).
                    Dùng khi: cần biết danh sách nhà thầu, tìm nhà thầu theo tên, hoặc nhà thầu nào đang làm ở một khu vực, tỉnh hoặc hợp đồng.
                    Trả về: tongSo và danhSach (mỗi nhà thầu gồm id, tên, tên đăng nhập, số hợp đồng và số đối tượng đang phụ trách).
                    Muốn xem chi tiết một nhà thầu (sản lượng, vướng mắc, tồn, phân công) hãy dùng tool của mảng đó kèm tham số nhaThau.
                    """
    )
    public NhaThauQueryResponse nhaThau(
            @ToolParam(description = "Từ khóa tìm theo tên hoặc tên đăng nhập nhà thầu", required = false) String query,
            @ToolParam(description = "Hợp đồng cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp đồng, ví dụ TVTK023241, 31032026VTK2025", required = false) String hopDong,
            @ToolParam(description = "Khu vực cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp lệ: TTKV1, TTKV2, TTKV3. Người dùng viết \"kv1\", \"khu vực 1\", \"TTKV 1\" thì truyền TTKV1, không hỏi lại", required = false) String khuVuc,
            @ToolParam(description = "Tỉnh/thành cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: BNH (Bắc Ninh), NAN (Nghệ An), THA (Thanh Hóa)", required = false) String tinhThanh,
            @ToolParam(description = "Số trang, bắt đầu từ 0", required = false) Integer page,
            @ToolParam(description = "Số dòng mỗi trang, mặc định 3, tối đa 100", required = false) Integer pageSize) {
        return nhaThauToolHandler.query(query, hopDong, khuVuc, tinhThanh, page, pageSize);
    }

    // ------------------------------------------------------------------ auditlog_tool

    @Tool(
            name = "auditlog_tool",
            description = """
                    Mục đích: Xem nhật ký thao tác (ai làm gì, lúc nào) trên hợp đồng và đối tượng: nhập, sửa, xóa, nghiệm thu sản lượng,
                    cập nhật hoặc chuyển trạng thái đối tượng, quyết toán, tạo/sửa hợp đồng.
                    Dùng khi: hỏi ai nhập sản lượng lần đầu, ai sửa sau, sửa những gì, ai đổi trạng thái đối tượng, hôm qua ai thao tác trên hợp đồng.
                    Trả về: tongSo, lanDau (thao tác sớm nhất), ganNhat (thao tác mới nhất), danhSach (thời gian, người thực hiện, hành động, mô tả
                    "trường cũ → mới", hợp đồng, mã đối tượng) và luuY khi dữ liệu cũ không có người thực hiện.
                    Muốn biết người nhập lần đầu: truyền doiTuong, hanhDong=NHAP_SAN_LUONG và đọc lanDau; người sửa: hanhDong=SUA_SAN_LUONG.
                    """
    )
    public vn.edu.huce.iic.bts_ops_platform.dto.nhatky.NhatKyQueryResponse nhatKy(
            @ToolParam(description = "Đối tượng cần xem: ID, mã hoặc tên (mã trạm, mã tuyến…) (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên)", required = false) String doiTuong,
            @ToolParam(description = "Hợp đồng cần xem: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên)", required = false) String hopDong,
            @ToolParam(description = "Hành động, nhiều giá trị ngăn cách bằng dấu phẩy: NHAP_SAN_LUONG, SUA_SAN_LUONG, XOA_SAN_LUONG, NGHIEM_THU_SAN_LUONG, CAP_NHAT_DOI_TUONG, DOI_TRANG_THAI_DOI_TUONG, THEM_DOI_TUONG, XOA_DOI_TUONG, IMPORT_DOI_TUONG, THEM_THUOC_TINH, SUA_THUOC_TINH, CAP_NHAT_QUYET_TOAN, TAO_HOP_DONG, CAP_NHAT_HOP_DONG, XOA_HOP_DONG; san_luong là cả 4 thao tác sản lượng. Bỏ trống: tất cả", required = false) String hanhDong,
            @ToolParam(description = "Tên người thực hiện (tìm chứa, không phân biệt hoa thường)", required = false) String nguoiThucHien,
            @ToolParam(description = "Từ ngày (theo giờ Việt Nam)", required = false) LocalDate fromDate,
            @ToolParam(description = "Đến ngày (theo giờ Việt Nam, gồm cả ngày này)", required = false) LocalDate toDate,
            @ToolParam(description = "Thứ tự danhSach: moi_nhat (mặc định) hoặc cu_nhat", required = false) String thuTu,
            @ToolParam(description = "Số trang, bắt đầu từ 0", required = false) Integer page,
            @ToolParam(description = "Số dòng mỗi trang, mặc định 3, tối đa 100", required = false) Integer pageSize) {
        return nhatKyToolHandler.query(doiTuong, hopDong, hanhDong, nguoiThucHien, fromDate, toDate, thuTu, page, pageSize);
    }

    // ------------------------------------------------------------------ hosodoituong_tool

    @Tool(
            name = "hosodoituong_tool",
            description = """
                    Mục đích: Hồ sơ thuộc tính khảo sát của một đối tượng hoặc một hợp đồng.
                    Dùng khi: cần kiểm tra thông tin bàn giao mặt bằng, vật tư, đối tượng chưa khảo sát hoặc đã khảo sát nhưng chưa có sản lượng.
                    Trả về: tongQuan, thuocTinh, chuaKhaoSat và khaoSatXongChuaCoSanLuong.
                    """
    )
    public HoSoDoiTuongQueryResponse hoSoDoiTuong(
            @ToolParam(description = "Đối tượng (trạm/tuyến...) cần tra thuộc tính: ID, mã hoặc tên", required = false) String doiTuong,
                        @ToolParam(description = "Hợp đồng cần tra tongQuan/chuaKhaoSat: ID, mã hoặc tên", required = false) String hopDong,
            @ToolParam(description = "Nhà thầu cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: Royal, Anh Tu; truyền đúng tên người dùng nói, hệ thống tự tra gần đúng, không cần hỏi lại", required = false) String nhaThau,
            @ToolParam(description = "Khu vực cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp lệ: TTKV1, TTKV2, TTKV3. Người dùng viết \"kv1\", \"khu vực 1\", \"TTKV 1\" thì truyền TTKV1, không hỏi lại", required = false) String khuVuc,
            @ToolParam(description = "Tỉnh/thành cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: BNH (Bắc Ninh), NAN (Nghệ An), THA (Thanh Hóa)", required = false) String tinhThanh,
            @ToolParam(description = "Bước trạng thái của đối tượng trong hợp đồng cần lọc: ID, mã hoặc tên (ví dụ Khảo sát, Thi công)", required = false) String trangThaiHopDong,
            @ToolParam(description = "true: chỉ đối tượng thuộc nhóm ưu tiên; false: chỉ đối tượng không thuộc nhóm ưu tiên; bỏ trống: tất cả", required = false) Boolean coNhomUuTien,
            @ToolParam(description = "Chỉ xét đối tượng đã khảo sát/bàn giao mặt bằng từ ngày này (yyyy-MM-dd); áp cho khaoSatXongChuaCoSanLuong", required = false) LocalDate fromDate,
            @ToolParam(description = "Chỉ xét đối tượng đã khảo sát/bàn giao mặt bằng đến ngày này (yyyy-MM-dd, gồm cả ngày này), ví dụ 'khảo sát trước 01/08' thì toDate=2026-07-31; áp cho khaoSatXongChuaCoSanLuong", required = false) LocalDate toDate,
                        @ToolParam(description = "Số trang, bắt đầu từ 0", required = false) Integer page,
                        @ToolParam(description = "Số dòng mỗi trang, mặc định 3, tối đa 100", required = false) Integer pageSize) {
                return hoSoDoiTuongToolHandler.query(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, trangThaiHopDong, coNhomUuTien, fromDate, toDate, page, pageSize);
    }

    // ------------------------------------------------------------------ volume_tool

    @Tool(
            name = "volume_tool",
            description = """
                    Mục đích: Kiểm soát ngân sách hợp đồng so với thành tiền thi công đã ghi nhận.
                    Dùng khi: cần phát hiện hợp đồng vượt, thiếu, thừa ngân sách hoặc drill-down theo chi tiết hợp đồng/tỉnh.
                    Trả về: tongQuan, chiTietHopDong và theoKhuVucTinh.
                    """
    )
    public NganHoQueryResponse nganHo(
            @ToolParam(description = "Hợp đồng cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp đồng, ví dụ TVTK023241, 31032026VTK2025", required = false) String hopDong,
            @ToolParam(description = "Từ khóa tìm theo mã hoặc tên hợp đồng cho phần tongQuan", required = false) String query,
            @ToolParam(description = "Hệ số ngưỡng GCCC dùng để tính ngân sách, ghi đè hệ số mặc định trong cấu hình (thường 2.0)", required = false) Double heSo,
            @ToolParam(description = "Ngưỡng % sử dụng ngân sách từ mức này trở lên là cảnh báo (canh_bao/warning), mặc định 90", required = false) Double nguongCanhBao,
            @ToolParam(description = "Loại hợp đồng cần lọc: ID, mã hoặc tên (mơ hồ sẽ báo lỗi kèm ứng viên); áp dụng cho tongQuan và danhSachHopDong", required = false) String loaiHopDong,
            @ToolParam(description = "Liệt kê hợp đồng trong danhSachHopDong theo trạng thái: thieu, thua, can_bang (theo sản lượng so với ngân sách), alert (cảnh báo nghiêm trọng), da_qt, dang_qt, chua_qt (quyết toán), all (tất cả)", required = false) String statusFilter,
            @ToolParam(description = "Khu vực: chỉ tính các hợp đồng có đối tượng ở khu vực này (ID, mã hoặc tên). Ngân sách vẫn tính trên cả hợp đồng", required = false) String khuVuc,
            @ToolParam(description = "Tỉnh/thành: chỉ tính các hợp đồng có đối tượng ở tỉnh này (ID, mã hoặc tên). Ngân sách vẫn tính trên cả hợp đồng", required = false) String tinhThanh,
            @ToolParam(description = "Trang danhSachHopDong (0-based), mặc định 0", required = false) Integer page,
            @ToolParam(description = "Số dòng mỗi trang danhSachHopDong, mặc định 3, tối đa 100", required = false) Integer pageSize) {
        return nganHoToolHandler.query(hopDong, query, heSo, nguongCanhBao, loaiHopDong, statusFilter, khuVuc, tinhThanh, page, pageSize);
    }

    // ------------------------------------------------------------------ system_overview_tool

    @Tool(
            name = "system_overview_tool",
            description = """
                    Mục đích: Tổng quan gộp 7 module trong một lần gọi để xem tình hình chung hệ thống.
                    Dùng khi: cần đối chiếu số liệu tổng hợp từ hợp đồng, sản lượng, phân công, trạm tồn, vướng mắc, biên bản và nguồn việc.
                    Trả về: hopDong, sanLuong, phanCong, tramTon, vuongMac, bienBan và nguonViec theo cấu trúc tương ứng của từng module.
                    """
    )
    public SystemOverviewResponse systemOverview(
            @ToolParam(description = "Từ ngày (yyyy-MM-dd), chỉ áp cho khối sanLuong", required = false) LocalDate fromDate,
            @ToolParam(description = "Đến ngày (yyyy-MM-dd), chỉ áp cho khối sanLuong", required = false) LocalDate toDate,
            @ToolParam(description = "Khu vực cần lọc cho mọi khối: ID, mã hoặc tên (ví dụ TTKV1, TTKV2, TTKV3)", required = false) String khuVuc,
            @ToolParam(description = "Số dòng cho các bảng xếp hạng trong từng khối, tối đa 20", required = false) Integer top) {
        return SystemOverviewResponse.builder()
                .hopDong(hopDongToolHandler.query(null, null, null, khuVuc, null, null, null, null, null, null, null, null, null, top, null, null, null))
                .sanLuong(sanLuongToolHandler.query(null, null, null, khuVuc, null, fromDate, toDate, null, null, null, null, null, null, null, null))
                .phanCong(phanCongToolHandler.query(null, null, null, null, null, khuVuc, null, null, null, null, top, null, null, null, null))
                .tramTon(doiTuongTonToolHandler.query(null, null, null, khuVuc, null, null, null, null, null, top, null, null, null, null, null))
                .vuongMac(vuongMacToolHandler.query(null, null, null, khuVuc, null, null, null, null, null, null, null, top, null, null, null, null, null, null, null))
                .bienBan(bienBanToolHandler.query(khuVuc, null, null, null, null, null, null, null, null, null))
                .nguonViec(nguonViecToolHandler.query(khuVuc, null, null, null, null, null, null, null, null, null, top, null, null, null))
                .build();
    }
}
