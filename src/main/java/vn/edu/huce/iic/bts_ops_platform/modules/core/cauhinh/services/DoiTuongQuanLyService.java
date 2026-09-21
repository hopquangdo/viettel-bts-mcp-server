package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DoiTuongQuanLyCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DoiTuongQuanLyTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DoiTuongQuanLyResponse;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DoiTuongQuanLyService {
    List<DoiTuongQuanLyResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID loaiHopDongId, UUID kieuHopDongId, Boolean selectorOnly);
    DoiTuongQuanLyResponse getById(UUID id);
    List<DoiTuongQuanLyResponse> getByIds(Collection<UUID> ids);
    DoiTuongQuanLyResponse create(DoiTuongQuanLyTaoRequest request);
    DoiTuongQuanLyResponse update(UUID id, DoiTuongQuanLyCapNhatRequest request);
    void delete(UUID id);
}
