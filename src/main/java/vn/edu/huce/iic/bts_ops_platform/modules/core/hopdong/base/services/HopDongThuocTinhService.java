package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongThuocTinhCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongThuocTinhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongThuocTinh;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface HopDongThuocTinhService {

    /** For same-domain callers (e.g. HopDongService) that need entities, not response DTOs. */
    List<HopDongThuocTinh> findActiveEntitiesByHopDongId(UUID hopDongId);

    /** Low-level save for same-domain callers (e.g. HopDongService) doing internal cascades. */
    HopDongThuocTinh saveEntity(HopDongThuocTinh entity);

    /** Mirrors the repository's bulk-fetch query, for same-domain callers (e.g. HopDongService's enrichment). */
    List<Object[]> findActiveWithTenByHopDongIds(Collection<UUID> hopDongIds);

    List<HopDongThuocTinhResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hopDongId);
    HopDongThuocTinhResponse getById(UUID id);
    HopDongThuocTinhResponse create(HopDongThuocTinhTaoRequest request);
    HopDongThuocTinhResponse update(UUID id, HopDongThuocTinhCapNhatRequest request);
    void delete(UUID id);
}
