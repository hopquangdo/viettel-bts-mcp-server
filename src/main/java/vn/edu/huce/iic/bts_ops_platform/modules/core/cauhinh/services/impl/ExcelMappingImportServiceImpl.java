package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import vn.edu.huce.iic.bts_ops_platform.config.AppExcelImportProperties;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ExcelMapping;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ExcelMappingCot;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ThuocTinh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.ExcelMappingCotRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.ExcelMappingRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.ThuocTinhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ExcelMappingImportService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.util.ExcelImportHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.util.ExcelParseResult;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers.HangMucImportFields;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelMappingImportServiceImpl implements ExcelMappingImportService {

    private final ExcelMappingRepository excelMappingRepository;
    private final ExcelMappingCotRepository excelMappingCotRepository;
    private final ThuocTinhRepository thuocTinhRepository;
    private final AppExcelImportProperties excelImportProperties;

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listPreviewMappings(Collection<UUID> doiTuongQuanLyIds) {
        if (doiTuongQuanLyIds == null || doiTuongQuanLyIds.isEmpty()) {
            return List.of();
        }
        Set<UUID> seen = new LinkedHashSet<>();
        List<Map<String, Object>> mappings = new ArrayList<>();
        for (UUID doiTuongQuanLyId : doiTuongQuanLyIds) {
            if (doiTuongQuanLyId == null) {
                continue;
            }
            for (ExcelMapping mapping : excelMappingRepository
                    .findByDoiTuongQuanLyIdAndNgayXoaIsNullOrderByNgayTaoDesc(doiTuongQuanLyId)) {
                if (!seen.add(mapping.getId())) {
                    continue;
                }
                mappings.add(toPreviewMapping(mapping));
            }
        }
        return mappings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listSheetNames(MultipartFile file) {
        try {
            return ExcelImportHelper.listSheetNames(file);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không đọc được file Excel");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> parsePreviewRows(
            MultipartFile file,
            UUID excelMappingId,
            String tenSheet,
            int maxRows) {
        return parseRows(file, excelMappingId, tenSheet, maxRows);
    }

    @Override
    @Transactional(readOnly = true)
    public ExcelParseResult buildPreview(
            MultipartFile file,
            UUID excelMappingId,
            String tenSheet,
            int maxRows) {
        if (excelMappingId == null || file == null || file.isEmpty()) {
            return new ExcelParseResult(List.of(), List.of());
        }
        ExcelMapping mapping = excelMappingRepository.findById(excelMappingId)
                .filter(item -> item.getNgayXoa() == null)
                .orElse(null);
        if (mapping == null) {
            return new ExcelParseResult(List.of(), List.of());
        }
        List<ExcelMappingCot> cots = excelMappingCotRepository
                .findByExcelMappingIdAndNgayXoaIsNullOrderByThuTuAsc(mapping.getId());
        Map<UUID, String> thuocTinhNames = loadThuocTinhNames(cots);
        int dataStartRow = resolveDataStartRow(mapping);
        String cotNhomUuTien = mapping.getCotNhomUuTien();
        try {
            return ExcelImportHelper.parseMappedWorkbook(
                    file, tenSheet, cots, thuocTinhNames, mapping.getDoiTuongQuanLyId(), dataStartRow, maxRows, cotNhomUuTien);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không parse được file Excel");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int countParsedRows(MultipartFile file, UUID excelMappingId, String tenSheet) {
        return parseRows(file, excelMappingId, tenSheet, excelImportProperties.maxImportRows()).size();
    }

    private List<Map<String, Object>> parseRows(
            MultipartFile file,
            UUID excelMappingId,
            String tenSheet,
            int maxRows) {
        if (excelMappingId == null || file == null || file.isEmpty()) {
            return List.of();
        }
        ExcelMapping mapping = excelMappingRepository.findById(excelMappingId)
                .filter(item -> item.getNgayXoa() == null)
                .orElse(null);
        if (mapping == null) {
            return List.of();
        }
        List<ExcelMappingCot> cots = excelMappingCotRepository
                .findByExcelMappingIdAndNgayXoaIsNullOrderByThuTuAsc(mapping.getId());
        Map<UUID, String> thuocTinhNames = loadThuocTinhNames(cots);
        int dataStartRow = resolveDataStartRow(mapping);
        String cotNhomUuTien = mapping.getCotNhomUuTien();
        try {
            return ExcelImportHelper.parseRows(
                    file, tenSheet, cots, thuocTinhNames, mapping.getDoiTuongQuanLyId(), dataStartRow, maxRows, cotNhomUuTien);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không parse được file Excel");
        }
    }

    private Map<UUID, String> loadThuocTinhNames(List<ExcelMappingCot> cots) {
        Set<UUID> ids = cots.stream()
                .map(ExcelMappingCot::getThuocTinhId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return thuocTinhRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(ThuocTinh::getId, ThuocTinh::getTen, (left, right) -> left));
    }

    private Map<String, Object> toPreviewMapping(ExcelMapping mapping) {
        List<ExcelMappingCot> cots = excelMappingCotRepository
                .findByExcelMappingIdAndNgayXoaIsNullOrderByThuTuAsc(mapping.getId());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("excelMappingId", mapping.getId());
        response.put("ten", mapping.getTen());
        response.put("ma", mapping.getMa());
        response.put("doiTuongQuanLyId", mapping.getDoiTuongQuanLyId());
        response.put("dongTieuDe", mapping.getDongTieuDe());
        response.put("dongBatDauDoc", mapping.getDongBatDauDoc());
        response.put("cotNhomUuTien", mapping.getCotNhomUuTien());
        Map<UUID, String> thuocTinhNames = loadThuocTinhNames(cots);
        response.put("cotDanhSach", cots.stream().map(cot -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("thuocTinhId", cot.getThuocTinhId());
            item.put("cotExcel", cot.getCotExcel());
            if (cot.getThuocTinhId() != null) {
                item.put("thuocTinhTen", thuocTinhNames.get(cot.getThuocTinhId()));
            }
            item.put("batBuoc", cot.getBatBuoc());
            item.put("thuTu", cot.getThuTu());
            return item;
        }).toList());
        return response;
    }

    private int resolveDataStartRow(ExcelMapping mapping) {
        return mapping.getDongBatDauDoc() != null && mapping.getDongBatDauDoc() > 0
                ? mapping.getDongBatDauDoc()
                : 2;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> resolveMappedAttributeFieldName(UUID excelMappingId, String... candidateNames) {
        if (excelMappingId == null || candidateNames == null || candidateNames.length == 0) {
            return Optional.empty();
        }
        List<ExcelMappingCot> cots = excelMappingCotRepository
                .findByExcelMappingIdAndNgayXoaIsNullOrderByThuTuAsc(excelMappingId);
        Map<UUID, String> thuocTinhNames = loadThuocTinhNames(cots);
        for (ExcelMappingCot cot : cots) {
            if (cot.getThuocTinhId() == null) {
                continue;
            }
            String ten = thuocTinhNames.get(cot.getThuocTinhId());
            if (ten != null && matchesCandidateName(ten, candidateNames)) {
                return Optional.of(ten);
            }
        }
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isHangMucThiCongMapping(UUID excelMappingId) {
        if (excelMappingId == null) {
            return false;
        }
        return excelMappingRepository.findById(excelMappingId)
                .filter(item -> item.getNgayXoa() == null)
                .map(ExcelMapping::getDoiTuongQuanLyId)
                .filter(HangMucImportFields.DOI_TUONG_ID::equals)
                .isPresent();
    }

    private static boolean matchesCandidateName(String ten, String... candidateNames) {
        for (String candidate : candidateNames) {
            if (HangMucImportFields.matchesName(ten, candidate)) {
                return true;
            }
        }
        return false;
    }
}
