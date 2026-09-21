package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

public interface HangMucExcelImportService {

    Map<String, Object> importFromExcel(UUID hopDongId, MultipartFile file, UUID excelMappingId, String tenSheet);
}
