package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response.QuyenResponse;

import java.util.List;
import java.util.UUID;

public interface QuyenService {
    List<QuyenResponse> list(String search, Boolean activeOnly, boolean includeDeleted);
    QuyenResponse getById(UUID id);
    QuyenResponse create(QuyenTaoRequest request);
    QuyenResponse update(UUID id, QuyenCapNhatRequest request);
    void delete(UUID id);
}
