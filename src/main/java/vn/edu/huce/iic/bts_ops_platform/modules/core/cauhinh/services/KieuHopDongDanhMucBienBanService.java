package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuHopDongDanhMucBienBanDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.KieuHopDongDanhMucBienBanResponse;

import java.util.List;
import java.util.UUID;

public interface KieuHopDongDanhMucBienBanService {
    List<KieuHopDongDanhMucBienBanResponse> list(UUID kieuHopDongId, Boolean activeOnly, boolean includeDeleted);
    List<KieuHopDongDanhMucBienBanResponse> sync(KieuHopDongDanhMucBienBanDongBoRequest request);
    void delete(UUID id);
}
