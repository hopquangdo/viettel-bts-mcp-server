package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhKieuHopDongDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhKieuHopDongResponse;

import java.util.List;
import java.util.UUID;

public interface ThuocTinhKieuHopDongService {
    List<ThuocTinhKieuHopDongResponse> list(UUID kieuHopDongId, Boolean activeOnly, boolean includeDeleted);
    List<ThuocTinhKieuHopDongResponse> sync(ThuocTinhKieuHopDongDongBoRequest request);
    void delete(UUID id);
}
