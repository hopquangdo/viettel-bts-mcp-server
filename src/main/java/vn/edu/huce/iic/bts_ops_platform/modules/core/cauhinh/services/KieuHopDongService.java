package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.KieuHopDongResponse;

import java.util.List;
import java.util.UUID;

public interface KieuHopDongService {
    List<KieuHopDongResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID loaiHopDongId);
    KieuHopDongResponse getById(UUID id);
    KieuHopDongResponse create(KieuHopDongTaoRequest request);
    KieuHopDongResponse update(UUID id, KieuHopDongCapNhatRequest request);
    void delete(UUID id);
}
