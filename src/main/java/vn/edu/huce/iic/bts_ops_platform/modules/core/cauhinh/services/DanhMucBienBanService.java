package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DanhMucBienBanCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DanhMucBienBanTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DanhMucBienBanResponse;

import java.util.List;
import java.util.UUID;

public interface DanhMucBienBanService {
    List<DanhMucBienBanResponse> list(String search, Boolean activeOnly, boolean includeDeleted);
    DanhMucBienBanResponse getById(UUID id);
    DanhMucBienBanResponse create(DanhMucBienBanTaoRequest request);
    DanhMucBienBanResponse update(UUID id, DanhMucBienBanCapNhatRequest request);
    void delete(UUID id);
}
