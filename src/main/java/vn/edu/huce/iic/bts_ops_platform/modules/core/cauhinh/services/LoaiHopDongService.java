package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LoaiHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LoaiHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LoaiHopDongResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface LoaiHopDongService {
    List<LoaiHopDongResponse> list(String search, Boolean activeOnly, boolean includeDeleted);
    LoaiHopDongResponse getById(UUID id);

    Map<UUID, LoaiHopDongResponse> getByIds(Collection<UUID> ids);
    LoaiHopDongResponse create(LoaiHopDongTaoRequest request);
    LoaiHopDongResponse update(UUID id, LoaiHopDongCapNhatRequest request);
    void delete(UUID id);
}
