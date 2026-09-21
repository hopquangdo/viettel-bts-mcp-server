package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DoiTuongHopDongLienKetDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DoiTuongHopDongLienKetResponse;

import java.util.List;
import java.util.UUID;

public interface DoiTuongHopDongLienKetService {
    List<DoiTuongHopDongLienKetResponse> list(UUID loaiHopDongId, UUID kieuHopDongId, Boolean activeOnly, boolean includeDeleted);

    List<DoiTuongHopDongLienKetResponse> sync(DoiTuongHopDongLienKetDongBoRequest request);

    void delete(UUID id);
}
