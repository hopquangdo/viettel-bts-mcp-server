package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.services;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request.SanLuongCapNhatDoiTuongRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request.SanLuongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request.SanLuongTaoHangLoatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request.SanLuongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.SanLuongAnhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.SanLuongBoLocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.SanLuongDoiTuongChiTietResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.SanLuongDoiTuongNgayTongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.SanLuongDoiTuongRowResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.SanLuongDoiTuongTongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.SanLuongHangMucItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.SanLuongNhatKyResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.SanLuongProgressResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.SanLuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.SanLuongTongHopResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SanLuongService {
    SanLuongResponse getById(UUID id);
    SanLuongResponse create(SanLuongTaoRequest request);
    SanLuongResponse update(UUID id, SanLuongCapNhatRequest request);
    void delete(UUID id);

    /**
     * Ghi nhận kết quả nghiệm thu (đạt/không đạt) cho 1 bản ghi sản lượng cụ thể — khác với
     * `trangThai` (tiến độ pending/done/issue). ketQua phải là "dat" hoặc "khong_dat"; bắt buộc
     * có lyDo khi "khong_dat".
     */
    SanLuongResponse nghiemThu(UUID id, String ketQua, String lyDo);
    long countActive();

    /** Tổng thành tiền sản lượng đang hoạt động trong khoảng ngày (dùng dashboard). */
    BigDecimal tongThanhTien(LocalDate dateFrom, LocalDate dateTo);

    /** Tổng thành tiền sản lượng nhóm theo loại hợp đồng. */
    Map<UUID, BigDecimal> tongThanhTienTheoLoaiHopDong();

    /** Tổng thành tiền sản lượng gộp theo đối tượng hợp đồng. */
    List<SanLuongDoiTuongTongResponse> tongThanhTienTheoDoiTuong();

    /** Tổng thành tiền sản lượng gộp theo đối tượng hợp đồng và ngày thực hiện. */
    List<SanLuongDoiTuongNgayTongResponse> tongThanhTienTheoDoiTuongVaNgay(LocalDate dateFrom, LocalDate dateTo);

    /** Tổng thành tiền sản lượng theo danh sách đối tượng hợp đồng (dùng volume). */
    Map<UUID, BigDecimal> tongThanhTienTheoDoiTuongIds(Collection<UUID> hopDongDoiTuongIds);

    // ===== Trang "sản lượng thi công" — bảng đối tượng (phân trang) + card tổng quan =====

    /** Phân trang đơn giản (offset-based): page 0-based, size = số bản ghi/trang. */
    PageResponse<SanLuongDoiTuongRowResponse> listDoiTuong(
            UUID hopDongId,
            List<UUID> doiTuongQuanLyIds,
            UUID contractorId,
            LocalDate dateFrom,
            LocalDate dateTo,
            boolean includeWithoutOutput,
            Integer page,
            Integer size);

    /**
     * Tìm đối tượng theo mã/tên/giá trị thuộc tính (EAV) — tách riêng khỏi listDoiTuong vì
     * ngữ nghĩa khác hẳn: bỏ qua dateFrom/dateTo để luôn tra được mã trạm dù sản lượng nằm
     * ngoài kỳ đang xem trên UI (xem SanLuongRepository.findPageRowsForSanLuongByKeyword).
     * Luôn trả cả đối tượng CHƯA có sản lượng nào, miễn khớp từ khóa — không nhận
     * includeWithoutOutput như listDoiTuong (yêu cầu "phải có sản lượng" không có ý nghĩa khi
     * tìm theo mã/tên).
     */
    PageResponse<SanLuongDoiTuongRowResponse> searchDoiTuong(
            String search,
            UUID hopDongId,
            List<UUID> doiTuongQuanLyIds,
            UUID contractorId,
            Integer page,
            Integer size);

    SanLuongTongHopResponse tongHop(
            String search,
            UUID hopDongId,
            List<UUID> doiTuongQuanLyIds,
            UUID contractorId,
            LocalDate dateFrom,
            LocalDate dateTo);

    PageResponse<SanLuongHangMucItemResponse> listHangMuc(
            UUID hopDongDoiTuongId,
            Integer page,
            Integer size);

    SanLuongDoiTuongChiTietResponse getDoiTuongChiTiet(UUID hopDongDoiTuongId, boolean includeWorkItems);

    SanLuongNhatKyResponse listNhatKy(UUID hopDongDoiTuongId);

    SanLuongBoLocResponse boLoc();

    SanLuongDoiTuongRowResponse taoHangLoat(SanLuongTaoHangLoatRequest request);

    SanLuongDoiTuongRowResponse capNhatDoiTuong(UUID hopDongDoiTuongId, SanLuongCapNhatDoiTuongRequest request);

    /**
     * Xác nhận đối tượng đã hoàn thành thi công (dù chưa làm hết hạng mục): gán ngày HT/TC,
     * chuyển trạng thái sang bước HT trong luồng, khóa bổ sung sản lượng; không đổi trạng thái
     * từng hạng mục sản lượng.
     */
    SanLuongDoiTuongRowResponse xacNhanHoanThanhDoiTuong(UUID hopDongDoiTuongId);

    SanLuongAnhResponse uploadAnh(
            UUID hopDongDoiTuongId,
            MultipartFile file,
            UUID hangMucCongViecId,
            UUID hangMucChiTietId,
            String loaiAnh,
            String moTa);

    void xoaDoiTuong(UUID hopDongDoiTuongId);

    /**
     * Tính lại và nạp lại cache cho 1 trạm cụ thể (eager warming sau khi dữ liệu thay đổi).
     * Sử dụng dateFrom/dateTo=null để cache cho khoảng thời gian mặc định (toàn lịch sử).
     */
    void recomputeAndCacheDoiTuongRow(UUID hopDongDoiTuongId, UUID doiTuongQuanLyId);

    /**
     * Xóa toàn bộ cache tổng hợp theo hợp đồng, sau đó tính lại cho 2 trường hợp:
     * 1. Hợp đồng cụ thể (scope = hopDongId).
     * 2. Toàn quốc (scope = null = global).
     */
    void recomputeAndCacheTongHop(UUID hopDongId);

    /**
     * Tiến độ hạng mục (đã làm/tổng/số hạng mục lỗi) + tổng thành tiền của 1 đối tượng,
     * tính trên toàn bộ lịch sử — không bỏ qua đối tượng chưa có bản ghi sản lượng nào
     * (khác với listDoiTuong, vốn ẩn các dòng rỗng). Dùng làm nguồn cho cache mô tả
     * dùng chung với module volume.
     */
    SanLuongProgressResponse getProgress(UUID hopDongDoiTuongId);

    /**
     * Tổng thành tiền sản lượng gộp theo khu vực hoặc theo nhà thầu (đọc từ snapshot đã tính
     * sẵn của core, không query lại EAV) — dùng cho breakdown "sản lượng theo khu vực/nhà thầu
     * là bao nhiêu?". nhom: "khuvuc" | "nhathau".
     */
    List<vn.edu.huce.iic.bts_ops_platform.common.dto.RankedItemResponse> breakdownTheoNhom(String nhom);

    /**
     * Tổng thành tiền sản lượng theo từng tháng trong N tháng gần nhất (mặc định 6) — dùng cho
     * câu hỏi "xu hướng sản lượng theo tháng". Mỗi phần tử: label = "YYYY-MM", value = tổng
     * thành tiền tháng đó.
     */
    List<vn.edu.huce.iic.bts_ops_platform.common.dto.RankedItemResponse> xuHuongTheoThang(int soThang);

    /** Tổng km khảo sát (trangThai=survey) của hợp đồng — dùng cảnh báo vượt khối lượng HĐ TVTK. */
    BigDecimal tongKhaoSatKmTheoHopDong(UUID hopDongId);

    /** Tổng thành tiền sản lượng của hợp đồng — so với giá trị HĐ (giaTriHd). */
    BigDecimal tongThanhTienTheoHopDong(UUID hopDongId);
}
