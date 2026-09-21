package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhHopDongResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ThuocTinhHopDongService {
    List<ThuocTinhHopDongResponse> list(String search, Boolean activeOnly, boolean includeDeleted);
    ThuocTinhHopDongResponse getById(UUID id);
    Map<UUID, ThuocTinhHopDongResponse> mapActiveByIds(Collection<UUID> ids);
    ThuocTinhHopDongResponse create(ThuocTinhHopDongTaoRequest request);
    ThuocTinhHopDongResponse update(UUID id, ThuocTinhHopDongCapNhatRequest request);
    void delete(UUID id);
}
