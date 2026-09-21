package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.TrangThaiHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.TrangThaiHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.TrangThaiHopDongResponse;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TrangThaiHopDongService {
    List<TrangThaiHopDongResponse> list(String search, Boolean activeOnly, boolean includeDeleted);
    TrangThaiHopDongResponse getById(UUID id);
    List<TrangThaiHopDongResponse> getByIds(Collection<UUID> ids);
    TrangThaiHopDongResponse create(TrangThaiHopDongTaoRequest request);
    TrangThaiHopDongResponse update(UUID id, TrangThaiHopDongCapNhatRequest request);
    void delete(UUID id);
}
