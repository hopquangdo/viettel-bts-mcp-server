package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongTepDinhKemCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongTepDinhKemTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongTepDinhKemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongTepDinhKem;

import java.util.List;
import java.util.UUID;

public interface HopDongTepDinhKemService {

    /** For same-domain callers (e.g. HopDongService) that need entities, not response DTOs. */
    List<HopDongTepDinhKem> findActiveEntitiesByHopDongId(UUID hopDongId);

    /** Low-level save for same-domain callers (e.g. HopDongService) doing internal cascades/linking. */
    HopDongTepDinhKem saveEntity(HopDongTepDinhKem entity);

    List<HopDongTepDinhKemResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hopDongId);
    HopDongTepDinhKemResponse getById(UUID id);
    HopDongTepDinhKemResponse create(HopDongTepDinhKemTaoRequest request);
    HopDongTepDinhKemResponse update(UUID id, HopDongTepDinhKemCapNhatRequest request);
    void delete(UUID id);
}
