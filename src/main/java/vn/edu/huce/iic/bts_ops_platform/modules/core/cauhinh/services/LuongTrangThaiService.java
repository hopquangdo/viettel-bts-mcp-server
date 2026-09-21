package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LuongTrangThaiCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LuongTrangThaiDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LuongTrangThaiTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiResponse;

import java.util.List;
import java.util.UUID;

public interface LuongTrangThaiService {

    List<LuongTrangThaiResponse> list(String search, Boolean activeOnly, boolean includeDeleted);

    LuongTrangThaiResponse getById(UUID id);

    List<LuongTrangThaiBuocResponse> listBuocByLuongId(UUID luongTrangThaiId);

    LuongTrangThaiResponse create(LuongTrangThaiTaoRequest request);

    LuongTrangThaiResponse update(UUID id, LuongTrangThaiCapNhatRequest request);

    void delete(UUID id);

    LuongTrangThaiResponse syncBuoc(UUID id, LuongTrangThaiDongBoRequest request);
}
