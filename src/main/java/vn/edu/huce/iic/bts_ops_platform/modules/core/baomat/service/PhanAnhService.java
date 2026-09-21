package vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.service;

import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.dto.request.PhanAnhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.dto.response.PhanAnhResponse;

import java.util.List;
import java.util.UUID;

public interface PhanAnhService {

    PhanAnhResponse create(PhanAnhTaoRequest request);

    List<PhanAnhResponse> listMine();

    List<PhanAnhResponse> listAll();

    PhanAnhResponse updateTrangThai(UUID id, String trangThai);
}
