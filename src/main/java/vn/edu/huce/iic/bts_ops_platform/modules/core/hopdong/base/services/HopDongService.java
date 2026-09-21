package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services;

import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongImportBatchRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongTaiLieuDownload;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongTaiLieuDownload;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongTaiLieuItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThongKeTatCaResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.response.TepDinhKemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface HopDongService {

    /** For same-domain callers (other hopdong services) that need the entity itself, not the response DTO. */
    Optional<HopDong> findActiveEntityById(UUID id);

    /** For same-domain callers (other hopdong services) that need the entity itself, not the response DTO. */
    List<HopDong> findAllActiveEntities();
    PageResponse<HopDongResponse> list(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID loaiHopDongId,
            UUID kieuHopDongId,
            Integer page,
            Integer size);

    List<HopDongResponse> listAll(String search, Boolean activeOnly, boolean includeDeleted, UUID loaiHopDongId, UUID kieuHopDongId);

    HopDongResponse getById(UUID id);
    List<HopDongResponse> getByIds(Collection<UUID> ids);
    /** Số hợp đồng đang hoạt động nhóm theo loại hợp đồng (dùng dashboard). */
    Map<UUID, Long> demActiveNhomTheoLoaiHopDong();
    Map<UUID, String> mapContractLabelsByIds(Collection<UUID> ids);
    HopDongResponse create(HopDongTaoRequest request);

    TepDinhKemResponse uploadTaiLieu(UUID hopDongId, MultipartFile file, String loaiTaiLieu);

    List<HopDongTaiLieuItemResponse> listTaiLieu(UUID hopDongId);

    void deleteTaiLieu(UUID hopDongId, UUID linkId);

    HopDongTaiLieuDownload downloadTaiLieu(UUID hopDongId, UUID linkId);

    HopDongResponse update(UUID id, HopDongCapNhatRequest request);
    void delete(UUID id);
    HopDongResponse cancel(UUID id);
    Map<String, Object> thongKe(UUID loaiHopDongId, UUID kieuHopDongId);

    /**
     * Cùng bộ số liệu như {@link #thongKe(UUID, UUID)} nhưng trả về cho TẤT CẢ loại hợp đồng
     * trong 1 lần gọi, với số query cố định — thay cho việc client gọi lặp lại một lần cho mỗi
     * loaiHopDongId. Không nhận kieuHopDongId và không lọc theo nhà thầu, giống thongKe().
     */
    HopDongThongKeTatCaResponse thongKeTatCa();

    /**
     * Phân loại từng hợp đồng theo tỷ lệ hoàn thành vào 3 cấp cảnh báo: xanh (≥90%), vàng
     * (70-90%), đỏ (<70%) — dùng cho câu hỏi "hợp đồng nào đang ở mức rủi ro nào theo tiến độ".
     * Tái dùng số liệu tyLeHoanThanh đã tính trong thongKe(), không thêm truy vấn nặng.
     */
    Map<String, Object> canhBaoTienDo(String loaiHopDong);

    long countActive();
    Map<String, Object> previewImportExcel(UUID hopDongId, MultipartFile file, UUID excelMappingId, String tenSheet);
    Map<String, Object> importExcel(
            UUID hopDongId,
            MultipartFile file,
            UUID excelMappingId,
            String tenSheet,
            Boolean importAsPending,
            Boolean replacePending);

    Map<String, Object> importDoiTuongBatch(UUID hopDongId, HopDongDoiTuongImportBatchRequest request);
}
