package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.services;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.dto.RankedItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.request.VuongMacCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.request.VuongMacTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.request.VuongMacTrungKiemTraRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response.VuongMacDoiTuongOptionResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response.VuongMacLichSuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response.VuongMacResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response.VuongMacTongQuanResponse;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface VuongMacService {

    /** Số vướng mắc đang hoạt động theo danh sách trạng thái (dùng cho dashboard). */
    long demActiveTheoTrangThai(Collection<String> trangThais);

    /** Số vướng mắc đang mở trong phạm vi loại/kiểu HĐ (null = không lọc). */
    long demActiveTheoTrangThaiVaPhamVi(
            Collection<String> trangThais, UUID loaiHopDongId, UUID kieuHopDongId);

    /** Số vướng mắc mở nhóm theo hợp đồng — dùng cho thống kê tiến độ. */
    List<Object[]> demOpenIssuesGroupByHopDong(UUID loaiHopDongId, UUID kieuHopDongId);

    /** Số vướng mắc đang hoạt động nhóm theo loại hợp đồng, lọc theo trạng thái. */
    Map<UUID, Long> demActiveTheoLoaiHopDongVaTrangThai(Collection<String> trangThais);

    /** Tập đối tượng HĐ đang có ít nhất 1 vướng mắc mở (pending/in_progress). */
    Set<UUID> findDoiTuongIdsCoVuongDangMo(Collection<UUID> hopDongDoiTuongIds);

    /**
     * Số vướng mắc đang mở (pending/in_progress) phát sinh trong giai đoạn dateFrom-dateTo,
     * lọc theo hợp đồng/đối tượng quản lý/scope nhà thầu — dùng cho SanLuongTongHopResponse.
     * Không nhận search/contractor (nghiệp vụ: search không ảnh hưởng số tổng hợp).
     */
    long demMoTrongGiaiDoan(
            UUID hopDongId,
            UUID doiTuongQuanLyId,
            boolean hasScope,
            Collection<UUID> scopeIds,
            LocalDate dateFrom,
            LocalDate dateTo);

    List<VuongMacResponse> list(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID hopDongId,
            String trangThai,
            String giaiDoan,
            String kieuVuongMac,
            UUID loaiHopDongId,
            UUID khuVucId,
            UUID tinhThanhId);

    /**
     * Bản phân trang thật ở SQL (LIMIT/OFFSET) của list() — dùng cho bảng danh sách trang Issues.
     * search: LIKE trên cột SQL có sẵn, cộng chung điều kiện với các filter khác — xem
     * VuongMacRepository.findFilteredPage. KHÔNG khớp được mã trạm (thuộc tính EAV, không có
     * cột SQL). dateFrom/dateTo: lọc theo ngày tạo (khoảng [dateFrom, dateTo], null = không lọc).
     */
    PageResponse<VuongMacResponse> listPage(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID hopDongId,
            String trangThai,
            boolean dangMoOnly,
            String giaiDoan,
            String kieuVuongMac,
            UUID loaiHopDongId,
            UUID khuVucId,
            UUID tinhThanhId,
            Integer quaHanNgay,
            LocalDate ngayBaoCao,
            LocalDate dateFrom,
            LocalDate dateTo,
            Integer page,
            Integer size);

    /**
     * Tổng quan đếm theo trạng thái + quá hạn 30 ngày — API riêng cho các StatCard đầu trang
     * Issues, tính bằng SQL aggregate (không fetch hết danh sách rồi đếm ở frontend). Cùng bộ
     * filter với listPage() (không nhận search — xem searchPage()).
     *
     * @param includeKieuCounts false để bỏ qua countsByKieu (dùng khi chỉ cần StatCard, tab đã có số global).
     */
    VuongMacTongQuanResponse tongQuan(
            Boolean activeOnly,
            boolean includeDeleted,
            UUID hopDongId,
            String trangThai,
            String giaiDoan,
            String kieuVuongMac,
            UUID loaiHopDongId,
            UUID khuVucId,
            UUID tinhThanhId,
            boolean includeKieuCounts);

    /** Bản không phân trang của filterAndEnrich() — dùng để tổng hợp KPI (business.tramton.tongQuan). */
    List<VuongMacResponse> danhSachAll(
            Boolean activeOnly,
            UUID loaiHopDongId,
            UUID khuVucId,
            boolean dangMoOnly,
            Integer quaHanNgay,
            LocalDate ngayBaoCao);

    /**
     * Vướng mắc mở cho trạm tồn — chỉ field trên entity, không enrich EAV (nhanh khi gộp danh sách).
     */
    List<VuongMacResponse> danhSachOpenLiteForTramTon(
            UUID loaiHopDongId,
            Integer quaHanNgay,
            LocalDate ngayBaoCao);

    /** Enrich đầy đủ theo batch id — dùng sau khi đã cắt trang. */
    List<VuongMacResponse> enrichByIds(Collection<UUID> ids);

    List<VuongMacDoiTuongOptionResponse> timDoiTuong(String search, UUID hopDongId, int limit);

    VuongMacResponse getById(UUID id);

    VuongMacResponse create(VuongMacTaoRequest request);

    /** Vướng mắc đang hoạt động trùng đối tượng + giai đoạn — dùng cảnh báo trước khi tạo mới. */
    List<VuongMacResponse> kiemTraTrung(VuongMacTrungKiemTraRequest request);

    VuongMacResponse update(UUID id, VuongMacCapNhatRequest request);

    VuongMacResponse uploadAnh(UUID id, MultipartFile file);

    /** Lịch sử xử lý vướng mắc — mới nhất trước. */
    List<VuongMacLichSuResponse> listLichSu(UUID id);

    void delete(UUID id);

    /**
     * Tính lại và ghi đè cache row cho 1 vướng mắc (dùng bởi consumer nghe event).
     * Trả về null nếu bản ghi không còn tồn tại/đã xóa (cache đã được evict).
     */
    VuongMacResponse refreshRow(UUID vuongMacId);

    /** Làm mới cache row của mọi vướng mắc gắn với 1 đối tượng hợp đồng (mã/tỉnh/khu vực/nhà thầu đổi). */
    void refreshRowsByHopDongDoiTuongId(UUID hopDongDoiTuongId);

    /** Xóa cache row của mọi vướng mắc thuộc 1 hợp đồng (tên/loại hợp đồng đổi). */
    void evictRowsByHopDongId(UUID hopDongId);

    /** Xóa cache tổng quan + đếm theo kiểu — gọi khi vướng mắc tạo/sửa/xóa. */
    void evictTongQuanCache();

    /** Danh sách vướng mắc theo đối tượng HĐ — dùng drill-down module trạm tồn. */
    List<VuongMacResponse> listByDoiTuongId(UUID doiTuongId, boolean dangMoOnly);

    /**
     * Bản nhẹ cho modal chi tiết trạm tồn — không enrich EAV/ảnh/hợp đồng (nhanh hơn listByDoiTuongId).
     */
    List<VuongMacResponse> listByDoiTuongIdLite(UUID doiTuongId, boolean dangMoOnly);

    /** Top N hợp đồng có nhiều vướng mắc đang mở (pending/in_progress) nhất, sắp xếp giảm dần. */
    List<RankedItemResponse> topHopDongTheoVuongMacMo(int top);
}
