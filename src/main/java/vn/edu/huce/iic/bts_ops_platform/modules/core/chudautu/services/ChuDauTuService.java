package vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.dto.request.ChuDauTuCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.dto.request.ChuDauTuTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.dto.response.ChuDauTuResponse;

import java.util.List;
import java.util.UUID;

public interface ChuDauTuService {
    List<ChuDauTuResponse> list(String search, Boolean activeOnly, boolean includeDeleted);
    ChuDauTuResponse getById(UUID id);
    ChuDauTuResponse create(ChuDauTuTaoRequest request);
    ChuDauTuResponse update(UUID id, ChuDauTuCapNhatRequest request);
    void delete(UUID id);
}
