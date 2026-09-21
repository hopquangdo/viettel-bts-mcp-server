package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongNhomUuTienCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongNhomUuTienTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongNhomUuTienResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongNhomUuTien;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface HopDongNhomUuTienService {

    /** For same-domain callers (e.g. HopDongDoiTuongService) that need entities, not response DTOs. */
    List<HopDongNhomUuTien> findActiveEntitiesByIds(Collection<UUID> ids);

    /** For same-domain callers (e.g. HopDongService) that need entities, not response DTOs. */
    List<HopDongNhomUuTien> findActiveEntitiesByHopDongId(UUID hopDongId);

    /** Low-level save for same-domain callers (e.g. HopDongService) doing internal cascades. */
    HopDongNhomUuTien saveEntity(HopDongNhomUuTien entity);

    List<HopDongNhomUuTienResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hopDongId);
    HopDongNhomUuTienResponse getById(UUID id);
    HopDongNhomUuTienResponse create(HopDongNhomUuTienTaoRequest request);
    HopDongNhomUuTienResponse update(UUID id, HopDongNhomUuTienCapNhatRequest request);
    void delete(UUID id);
    List<HopDongNhomUuTienResponse> assignDoiTuong(UUID id, List<UUID> doiTuongIds);
    void syncStats(UUID id);
}
