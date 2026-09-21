package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuDuLieuCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuDuLieuTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.KieuDuLieuResponse;

import java.util.List;
import java.util.UUID;

public interface KieuDuLieuService {
    List<KieuDuLieuResponse> list(String search, Boolean activeOnly, boolean includeDeleted);
    KieuDuLieuResponse getById(UUID id);
    KieuDuLieuResponse create(KieuDuLieuTaoRequest request);
    KieuDuLieuResponse update(UUID id, KieuDuLieuCapNhatRequest request);
    void delete(UUID id);
}
