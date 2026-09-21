package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenHanCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenHanTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response.QuyenHanResponse;

import java.util.List;
import java.util.UUID;

public interface QuyenHanService {
    List<QuyenHanResponse> list(String search, Boolean activeOnly, boolean includeDeleted);
    QuyenHanResponse getById(UUID id);
    QuyenHanResponse create(QuyenHanTaoRequest request);
    QuyenHanResponse update(UUID id, QuyenHanCapNhatRequest request);
    void delete(UUID id);
}
