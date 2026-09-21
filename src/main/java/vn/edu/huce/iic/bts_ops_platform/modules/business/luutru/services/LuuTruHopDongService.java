package vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.services;

import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.dto.response.HopDongLuuTruLichSuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.dto.response.HopDongLuuTruResponse;

import java.util.List;
import java.util.UUID;

public interface LuuTruHopDongService {

    List<HopDongLuuTruResponse> list(String search, Boolean activeOnly, boolean includeDeleted);

    HopDongLuuTruResponse archive(UUID hopDongId);

    HopDongLuuTruResponse khoiPhuc(UUID hopDongId);

    List<HopDongLuuTruLichSuResponse> lichSu(UUID hopDongId);
}
