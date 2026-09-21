package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.util.ExcelParseResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ExcelMappingImportService {

    List<Map<String, Object>> listPreviewMappings(Collection<UUID> doiTuongQuanLyIds);

    List<String> listSheetNames(MultipartFile file);

    List<Map<String, Object>> parsePreviewRows(
            MultipartFile file,
            UUID excelMappingId,
            String tenSheet,
            int maxRows);

    ExcelParseResult buildPreview(
            MultipartFile file,
            UUID excelMappingId,
            String tenSheet,
            int maxRows);

    int countParsedRows(MultipartFile file, UUID excelMappingId, String tenSheet);

    Optional<String> resolveMappedAttributeFieldName(UUID excelMappingId, String... candidateNames);

    /** Mapping thuộc đối tượng Hạng mục thi công (dùng routing import). */
    boolean isHangMucThiCongMapping(UUID excelMappingId);
}
