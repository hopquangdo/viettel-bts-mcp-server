package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucCongViecCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucCongViecTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucCongViecResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucCongViec;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface HangMucCongViecService {
    List<HangMucCongViecResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hangMucChiTietId);
    HangMucCongViecResponse getById(UUID id);

    Map<UUID, HangMucCongViecResponse> getByIds(Collection<UUID> ids);
    HangMucCongViecResponse create(HangMucCongViecTaoRequest request);
    HangMucCongViecResponse update(UUID id, HangMucCongViecCapNhatRequest request);
    void delete(UUID id);
    int deleteAllByHopDongId(UUID hopDongId);
    int deleteAllHangMucByHopDongId(UUID hopDongId);

    /** For cross-module callers (vd sanluong) cần entity, không cần response DTO. */
    List<HangMucCongViec> findActiveEntitiesByChiTietIds(Collection<UUID> hangMucChiTietIds);
}
