package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucChiTietCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucChiTietTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucChiTietResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucChiTiet;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface HangMucChiTietService {
    List<HangMucChiTietResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hangMucNhomId);
    HangMucChiTietResponse getById(UUID id);
    HangMucChiTietResponse create(HangMucChiTietTaoRequest request);
    HangMucChiTietResponse update(UUID id, HangMucChiTietCapNhatRequest request);
    void delete(UUID id);

    /** For cross-module callers (vd sanluong) cần entity, không cần response DTO. */
    List<HangMucChiTiet> findActiveEntitiesByNhomIds(Collection<UUID> hangMucNhomIds);
}
