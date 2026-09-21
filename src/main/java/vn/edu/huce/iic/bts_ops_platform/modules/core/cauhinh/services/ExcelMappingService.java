package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ExcelMappingCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ExcelMappingDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ExcelMappingTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ExcelMappingCotResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ExcelMappingResponse;

import java.util.List;
import java.util.UUID;

public interface ExcelMappingService {

    List<ExcelMappingResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID doiTuongQuanLyId);

    ExcelMappingResponse getById(UUID id);

    ExcelMappingResponse create(ExcelMappingTaoRequest request);

    ExcelMappingResponse update(UUID id, ExcelMappingCapNhatRequest request);

    void delete(UUID id);

    List<ExcelMappingCotResponse> dongBo(UUID id, ExcelMappingDongBoRequest request);
}
