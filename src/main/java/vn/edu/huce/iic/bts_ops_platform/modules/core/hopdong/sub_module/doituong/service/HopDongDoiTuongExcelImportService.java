package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongImportBatchRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface HopDongDoiTuongExcelImportService {

    Map<String, Object> importFromExcel(
            UUID hopDongId,
            MultipartFile file,
            UUID excelMappingId,
            String tenSheet,
            Boolean importAsPending,
            Boolean replacePending);

    Map<String, Object> importRowBatch(UUID hopDongId, HopDongDoiTuongImportBatchRequest request);

    Map<String, Object> resolvePreviewRows(UUID excelMappingId, List<Map<String, Object>> rawRows);
}
