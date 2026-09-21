package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.KhuVucCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.KhuVucTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.KhuVucResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.KhuVuc;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface KhuVucService {

    List<KhuVucResponse> list(String search, Boolean activeOnly);

    /** For same-domain callers (e.g. TinhThanhService) that need the entity itself, not the response DTO. */
    List<KhuVuc> findAllActiveEntities();

    /**
     * Bulk lookup tên khu vực theo id, để các module khác hiển thị tên khu vực
     * mà không phải tiêm KhuVucRepository trực tiếp.
     */
    Map<UUID, String> buildRegionNameLookup();

    /** For same-domain callers (e.g. TinhThanhService) that need the entity itself, not the response DTO. */
    Optional<KhuVuc> findActiveEntityById(UUID id);

    /** Số khu vực đang hoạt động (dùng cho dashboard). */
    long demActive();

    /** Lấy các khu vực theo danh sách id (bỏ bản ghi đã xóa). */
    List<KhuVucResponse> getByIds(Collection<UUID> ids);

    KhuVucResponse getById(UUID id);

    KhuVucResponse create(KhuVucTaoRequest request);

    KhuVucResponse update(UUID id, KhuVucCapNhatRequest request);

    void delete(UUID id);
}
