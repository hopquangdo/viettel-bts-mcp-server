package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service;

import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongNhanBanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongXoaTheoLocRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongLoaiThongKeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongXoaTheoLocBatchResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuong;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface HopDongDoiTuongService {

    /** For same-domain callers (e.g. HopDongNhomUuTienService) that need entities, not response DTOs. */
    List<HopDongDoiTuong> findActiveEntitiesByIds(Collection<UUID> ids);

    /** Đối tượng đã khởi công nhưng quá N ngày không có cập nhật sản lượng mới — dùng cho tool AI. */
    List<HopDongDoiTuongResponse> thieuCapNhatTienDo(Integer soNgay, int limit);

    /**
     * Breakdown số trạm theo loại hợp đồng (tên/mã, LIKE, để trống = không lọc) + khu vực (tên/mã,
     * LIKE, để trống = không lọc) — dùng cho tool AI. Trả về map: tong, hoanThanh, dangThiCong, vuongMac.
     */
    Map<String, Long> breakdownTheoLoaiKhuVuc(String tenLoaiHopDong, String tenKhuVuc);

    /** Trạm có sản lượng vượt ngưỡng hệ số hợp đồng — xem HopDongDoiTuongRepository.findTramSanLuongBatThuong. */
    List<vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongTonCandidateRow>
            sanLuongBatThuong(int limit);

    /**
     * Tốc độ hoàn thành thực tế (trạm/ngày) trong N ngày gần nhất, breakdown theo "khuvuc" hoặc
     * "nhathau", so sánh với tốc độ trung bình toàn hệ thống (nhanh/chậm hơn bao nhiêu %). LƯU Ý:
     * đây là so sánh với trung bình hệ thống, KHÔNG PHẢI so với hạn hợp đồng (hệ thống chưa có field
     * hạn hợp đồng). Trả về danh sách {label, value=tốc độ trạm/ngày, extra={soVoiTrungBinhPercent}}.
     */
    List<vn.edu.huce.iic.bts_ops_platform.common.dto.RankedItemResponse> tocDoHoanThanh(String nhom, int soNgay);

    /** Assigns the given đối tượng ids to a nhóm ưu tiên (or clears it if hopDongNhomUuTienId is null). */
    void updateNhomUuTienForIds(Collection<UUID> doiTuongIds, UUID hopDongNhomUuTienId);

    /** Detaches every đối tượng currently in a nhóm ưu tiên (used when the nhóm is deleted). */
    void clearNhomUuTien(UUID hopDongNhomUuTienId);

    /** Số đối tượng đang thuộc một nhóm ưu tiên. */
    long countByNhomUuTien(UUID hopDongNhomUuTienId);

    /** For same-domain callers (e.g. HopDongService) that need entities, not response DTOs. */
    List<HopDongDoiTuong> findActiveEntitiesByHopDongId(UUID hopDongId);

    /** Low-level save for same-domain callers (e.g. HopDongService) doing internal seeding/cascades. */
    HopDongDoiTuong saveEntity(HopDongDoiTuong entity);

    /**
     * Cập nhật cột denormalize {@code ngayThiCongGanNhat} — PHẢI gọi ngay sau mỗi lần
     * tạo/sửa bản ghi {@code SanLuong} của đối tượng này để sort danh sách sản lượng
     * luôn đưa trạm/tuyến thi công gần nhất lên đầu.
     */
    void recalculateConstructionDate(UUID hopDongDoiTuongId);

    /** Cập nhật cột denormalize {@code sanLuongHieuLuc} — gọi mỗi khi sanluong-changed. */
    void recalculateSanLuongHieuLuc(UUID hopDongDoiTuongId);

    /** Vá lại san_luong_hieu_luc cho TOÀN BỘ đối tượng (1 câu UPDATE) — dùng bởi tool vá số liệu. */
    int recalculateSanLuongHieuLucAll();

    /** Cập nhật cột denormalize {@code coVuongMacMo} — gọi mỗi khi VuongMacChangedEvent bắn tới. */
    void updateCoVuongMacMo(UUID hopDongDoiTuongId, boolean coVuongMacMo);

    /** Mirrors the repository's aggregate query, for same-domain callers (e.g. HopDongService's thongKe). */
    long countFilteredByLoaiHopDong(Boolean activeOnly, UUID loaiHopDongId);

    long countFilteredByLoaiHopDong(Boolean activeOnly, UUID loaiHopDongId, UUID kieuHopDongId);

    /** Mirrors the repository's aggregate query, for same-domain callers. */
    List<Object[]> countGroupByTrangThaiMa(Boolean activeOnly, UUID loaiHopDongId);

    /** Mirrors the repository's aggregate query, for same-domain callers. */
    List<Object[]> countGroupByHopDongId(Boolean activeOnly, UUID loaiHopDongId, UUID kieuHopDongId);

    /** Mirrors the repository's aggregate query, for same-domain callers. */
    List<Object[]> countVuongMacMoGroupByHopDongId(Boolean activeOnly, UUID loaiHopDongId, UUID kieuHopDongId);

    /** Mirrors the repository's aggregate query, for same-domain callers. */
    List<Object[]> countGroupByHopDongIdAndTrangThaiMa(Boolean activeOnly, UUID loaiHopDongId, UUID kieuHopDongId);
    PageResponse<HopDongDoiTuongResponse> list(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID hopDongId,
            UUID doiTuongQuanLyId,
            UUID trangThaiHopDongId,
            Boolean withoutNhomUuTien,
            Boolean withNhomUuTien,
            Integer page,
            Integer size);

    /** Lấy hết (không phân trang) — tương đương listAll(..., page=null, size=null). */
    default List<HopDongDoiTuongResponse> listAll(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID hopDongId,
            UUID doiTuongQuanLyId,
            UUID trangThaiHopDongId,
            Boolean withoutNhomUuTien,
            Boolean withNhomUuTien) {
        return listAll(search, activeOnly, includeDeleted, hopDongId, doiTuongQuanLyId,
                trangThaiHopDongId, withoutNhomUuTien, withNhomUuTien, null, null);
    }

    /**
     * page/size null → lấy hết trong 1 lần (LIST_ALL_SIZE, scoped theo hopDongId — an toàn vì
     * caller nội bộ luôn giới hạn phạm vi). Truyền page/size thật → ưu tiên dùng để phân trang
     * đúng nghĩa (ví dụ nếu sau này expose ra controller).
     */
    List<HopDongDoiTuongResponse> listAll(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID hopDongId,
            UUID doiTuongQuanLyId,
            UUID trangThaiHopDongId,
            Boolean withoutNhomUuTien,
            Boolean withNhomUuTien,
            Integer page,
            Integer size);

    /** Bulk load for volume: one query + one enrich (không phân trang từng HĐ). */
    List<HopDongDoiTuongResponse> listActiveByHopDongIds(Collection<UUID> hopDongIds);

    /**
     * Bản nhẹ của {@link #listActiveByHopDongIds(Collection)} — chỉ enrich {@code doiTuongTen}
     * (tên loại đối tượng, vd "Trạm"/"Tuyến"), bỏ hẳn giá trị EAV/trạng thái/nhóm ưu tiên.
     * Dùng cho volume: mã trạm/tỉnh/khu vực/nhà thầu giờ đọc qua HopDongDoiTuongSnapshotService,
     * không cần {@code giaTri} nữa; các field còn lại đọc thẳng từ entity là đủ.
     */
    List<HopDongDoiTuongResponse> listActiveLiteByHopDongIds(Collection<UUID> hopDongIds);

    /**
     * Id các đối tượng đang hoạt động (và hợp đồng của nó cũng đang hoạt động), lọc theo
     * hopDongId nếu có — cho same-domain lẫn cross-module callers (vd sanluong) cần chỉ
     * id, không cần enrich response.
     */
    List<UUID> findActiveIdsWithActiveHopDong(UUID hopDongId);

    List<HopDongDoiTuongResponse> listByNhomUuTien(UUID hopDongNhomUuTienId);

    PageResponse<UUID> listIds(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID hopDongId,
            UUID doiTuongQuanLyId,
            UUID trangThaiHopDongId,
            Boolean withoutNhomUuTien,
            Boolean withNhomUuTien,
            Integer page,
            Integer size);

    HopDongDoiTuongResponse getById(UUID id);
    List<HopDongDoiTuongResponse> getByIds(Collection<UUID> ids);

    /** Bản nhẹ của getByIds — chỉ entity + mã/tên loại đối tượng, không load EAV giá trị. */
    List<HopDongDoiTuongResponse> getByIdsLite(Collection<UUID> ids);

    /** Cập nhật quyết toán thực cho đối tượng thuộc hợp đồng (dùng volume). */
    HopDongDoiTuongResponse capNhatQuyetToanThuc(UUID hopDongId, UUID id, java.math.BigDecimal quyetToanThuc);

    /** Cộng thêm (hoặc trừ) giá trị bổ sung sản lượng cho đối tượng thuộc hợp đồng (dùng volume). */
    HopDongDoiTuongResponse dieuChinhBoSungSanLuong(UUID hopDongId, UUID id, java.math.BigDecimal delta);
    HopDongDoiTuongResponse create(HopDongDoiTuongTaoRequest request);

    /** Nhân bản đối tượng — sao thuộc tính, reset trạng thái/tiến độ, đổi mã khóa chính. */
    List<HopDongDoiTuongResponse> nhanBan(List<HopDongDoiTuongNhanBanRequest.Muc> muc);
    /** Import Excel: validate HĐ một lần, insert batch qua saveAll (không enrich từng dòng). */
    void assertImportAllowed(UUID hopDongId, UUID doiTuongQuanLyId);
    List<UUID> saveBatchForImport(List<HopDongDoiTuongTaoRequest> requests);
    HopDongDoiTuongResponse update(UUID id, HopDongDoiTuongCapNhatRequest request);
    void delete(UUID id);
    int deleteBatch(List<UUID> ids);
    int deleteByFilter(HopDongDoiTuongXoaTheoLocRequest request);
    HopDongDoiTuongXoaTheoLocBatchResponse deleteByFilterBatch(HopDongDoiTuongXoaTheoLocRequest request, int page);
    long countActive();

    /** Số đối tượng đang hoạt động theo loại hợp đồng (null = tất cả). Dùng dashboard. */
    long demActive(UUID loaiHopDongId);

    /** Đếm đối tượng active, lọc thêm theo khu vực (null = không lọc). */
    long demActive(UUID loaiHopDongId, UUID khuVucId);

    /** Số đối tượng đang hoạt động nhóm theo loại hợp đồng. */
    Map<UUID, Long> demActiveNhomTheoLoaiHopDong();

    /** Số đối tượng đang hoạt động nhóm theo hợp đồng. */
    Map<UUID, Long> demActiveNhomTheoHopDong();

    /** Số đối tượng đang hoạt động nhóm theo loại hợp đồng + đối tượng quản lý. */
    List<HopDongDoiTuongLoaiThongKeResponse> demActiveNhomTheoLoaiVaDoiTuong();

    /**
     * Lưu ý: getById/getByIds phải luôn là bản rẻ, không kèm sản lượng/vướng mắc — bản tổng
     * hợp (mã/tỉnh/khu vực/nhà thầu + sản lượng + vướng mắc) sống ở
     * HopDongDoiTuongSnapshotService, không phải ở đây.
     */
}
