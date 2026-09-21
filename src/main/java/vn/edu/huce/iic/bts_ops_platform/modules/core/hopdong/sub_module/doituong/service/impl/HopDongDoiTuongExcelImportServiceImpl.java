package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.impl;

import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventContext;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.PreparedRow;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.ResolvedColumn;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.RowResolution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.config.AppExcelImportProperties;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ExcelMappingCotResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ExcelMappingResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ExcelMappingImportService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ExcelMappingService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LienKetBangGiaTriNormalizer;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LienKetBangImportResolver;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongGiaTriTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongImportBatchRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongXoaTheoLocRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongNhomUuTienResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongNhomUuTien;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.DoiTuongImportFields;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.mapper.HopDongMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuongGiaTri;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongGiaTriRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongNhomUuTienRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongExcelImportService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongGiaTriService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongLookupService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HopDongDoiTuongExcelImportServiceImpl implements HopDongDoiTuongExcelImportService {

    private static final String DEFAULT_GROUP_COLOR = "#6b7280";
    private static final int RESPONSE_ENRICH_LIMIT = 100;

    private final ExcelMappingImportService excelMappingImportService;
    private final ExcelMappingService excelMappingService;
    private final HopDongLookupService hopDongLookupService;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final HopDongDoiTuongGiaTriService hopDongDoiTuongGiaTriService;
    private final AppEventContext appEventContext;
    private final HopDongNhomUuTienRepository hopDongNhomUuTienRepository;
    private final HopDongDoiTuongRepository hopDongDoiTuongRepository;
    private final HopDongDoiTuongGiaTriRepository hopDongDoiTuongGiaTriRepository;
    private final HopDongMapper hopDongMapper;
    private final AppExcelImportProperties excelImportProperties;
    private final LienKetBangImportResolver lienKetBangImportResolver;
    private final ThuocTinhService thuocTinhService;
    private final LienKetBangGiaTriNormalizer lienKetBangGiaTriNormalizer;

    @Override
    @Transactional
    public Map<String, Object> importFromExcel(
            UUID hopDongId,
            MultipartFile file,
            UUID excelMappingId,
            String tenSheet,
            Boolean importAsPending,
            Boolean replacePending) {
        hopDongLookupService.getById(hopDongId);
        ExcelMappingResponse mapping = excelMappingService.getById(excelMappingId);
        lienKetBangImportResolver.clearCache();

        UUID doiTuongQuanLyId = mapping.getDoiTuongQuanLyId();
        boolean pendingImport = Boolean.TRUE.equals(importAsPending);
        List<ExcelMappingCotResponse> cots = mapping.getCotDanhSach() != null
                ? mapping.getCotDanhSach()
                : List.of();
        List<ResolvedColumn> resolvedColumns = resolveColumns(cots);
        preloadLinkColumns(resolvedColumns);
        ResolvedColumn primaryKeyColumn = resolvedColumns.stream()
                .filter(ResolvedColumn::primaryKey)
                .findFirst()
                .orElse(null);

        List<Map<String, Object>> rows = excelMappingImportService.parsePreviewRows(
                file,
                excelMappingId,
                tenSheet,
                excelImportProperties.maxImportRows());

        int dataStartRow = mapping.getDongBatDauDoc() != null && mapping.getDongBatDauDoc() > 0
                ? mapping.getDongBatDauDoc()
                : 2;
        boolean supportsNhomUuTien = DoiTuongImportFields.supportsNhomUuTien(doiTuongQuanLyId)
                && mapping.getCotNhomUuTien() != null
                && !mapping.getCotNhomUuTien().isBlank();

        int skipped = 0;
        List<String> errors = new ArrayList<>();
        if (primaryKeyColumn == null) {
            errors.add("Không xác định được cột khóa chính trong mapping. Vui lòng cấu hình thuộc tính khóa chính trước khi import.");
            return buildResponse(hopDongId, rows.size(), List.of(), rows.size(), errors, supportsNhomUuTien, true);
        }
        Map<String, UUID> existingIdByPrimaryKey = loadExistingPrimaryKeyIds(hopDongId, primaryKeyColumn.thuocTinhId());
        Set<String> importPrimaryKeys = new LinkedHashSet<>();
        List<PreparedUpdateRow> updateRows = new ArrayList<>();
        List<PreparedRow> preparedRows = prepareRows(
                rows,
                dataStartRow,
                0,
                resolvedColumns,
                primaryKeyColumn,
                existingIdByPrimaryKey,
                importPrimaryKeys,
                updateRows,
                errors);
        skipped = rows.size() - preparedRows.size() - updateRows.size();

        if (preparedRows.isEmpty() && updateRows.isEmpty()) {
            return buildResponse(hopDongId, rows.size(), List.of(), skipped, errors, supportsNhomUuTien, true);
        }

        if (!errors.isEmpty()) {
            return buildResponse(hopDongId, rows.size(), List.of(), skipped, errors, supportsNhomUuTien, true);
        }

        maybeReplacePending(hopDongId, doiTuongQuanLyId, pendingImport, Boolean.TRUE.equals(replacePending), 0);
        hopDongDoiTuongService.assertImportAllowed(hopDongId, doiTuongQuanLyId);

        Map<String, UUID> nhomUuTienByTen = supportsNhomUuTien
                ? ensureNhomUuTienBatch(hopDongId, preparedRows)
                : Map.of();

        List<HopDongDoiTuongTaoRequest> doiTuongRequests = buildDoiTuongRequests(
                hopDongId, doiTuongQuanLyId, preparedRows, supportsNhomUuTien, nhomUuTienByTen, pendingImport);

        int batchSize = excelImportProperties.batchSize();
        List<UUID> createdIds = saveDoiTuongInBatches(doiTuongRequests, batchSize);

        List<HopDongDoiTuongGiaTriTaoRequest> giaTriRequests = new ArrayList<>();
        for (int i = 0; i < preparedRows.size(); i++) {
            appendGiaTriRequests(giaTriRequests, createdIds.get(i), preparedRows.get(i).row(), resolvedColumns);
        }
        saveGiaTriInBatches(giaTriRequests, batchSize);

        List<UUID> updatedIds = applyUpdateRows(updateRows, resolvedColumns, batchSize);

        if (supportsNhomUuTien) {
            refreshNhomUuTienStats(hopDongId);
        }

        List<UUID> allIds = new ArrayList<>(createdIds);
        allIds.addAll(updatedIds);
        return buildResponse(hopDongId, rows.size(), allIds, skipped, errors, supportsNhomUuTien, true);
    }

    @Override
    @Transactional
    public Map<String, Object> importRowBatch(UUID hopDongId, HopDongDoiTuongImportBatchRequest request) {
        hopDongLookupService.getById(hopDongId);
        ExcelMappingResponse mapping = excelMappingService.getById(request.getExcelMappingId());
        lienKetBangImportResolver.clearCache();

        UUID doiTuongQuanLyId = mapping.getDoiTuongQuanLyId();
        boolean pendingImport = Boolean.TRUE.equals(request.getImportAsPending());
        List<ExcelMappingCotResponse> cots = mapping.getCotDanhSach() != null
                ? mapping.getCotDanhSach()
                : List.of();
        List<ResolvedColumn> resolvedColumns = resolveColumns(cots);
        appendUnmappedThuocTinhColumns(doiTuongQuanLyId, resolvedColumns);
        ResolvedColumn primaryKeyColumn = resolvedColumns.stream()
                .filter(ResolvedColumn::primaryKey)
                .findFirst()
                .orElse(null);

        List<Map<String, Object>> rows = request.getRows() != null ? request.getRows() : List.of();
        int dataStartRow = mapping.getDongBatDauDoc() != null && mapping.getDongBatDauDoc() > 0
                ? mapping.getDongBatDauDoc()
                : 2;
        boolean supportsNhomUuTien = DoiTuongImportFields.supportsNhomUuTien(doiTuongQuanLyId)
                && mapping.getCotNhomUuTien() != null
                && !mapping.getCotNhomUuTien().isBlank();

        int skipped = 0;
        List<String> errors = new ArrayList<>();
        if (primaryKeyColumn == null) {
            errors.add("Không xác định được cột khóa chính trong mapping. Vui lòng cấu hình thuộc tính khóa chính trước khi import.");
            return buildResponse(hopDongId, rows.size(), List.of(), rows.size(), errors, supportsNhomUuTien, request.isFinalBatch());
        }

        preloadLinkColumns(resolvedColumns);
        Map<String, UUID> existingIdByPrimaryKey = loadExistingPrimaryKeyIds(hopDongId, primaryKeyColumn.thuocTinhId());
        Set<String> importPrimaryKeys = new LinkedHashSet<>();

        // UPSERT: dòng trùng khóa chính với dữ liệu đã có → CẬP NHẬT giá trị thay vì bỏ qua.
        List<PreparedUpdateRow> updateRows = new ArrayList<>();
        List<PreparedRow> preparedRows = prepareRows(
                rows,
                dataStartRow,
                request.getRowOffset(),
                resolvedColumns,
                primaryKeyColumn,
                existingIdByPrimaryKey,
                importPrimaryKeys,
                updateRows,
                errors);

        skipped = rows.size() - preparedRows.size() - updateRows.size();

        if (preparedRows.isEmpty() && updateRows.isEmpty()) {
            return buildResponse(hopDongId, rows.size(), List.of(), skipped, errors, supportsNhomUuTien, request.isFinalBatch());
        }

        if (!errors.isEmpty() || request.isValidateOnly()) {
            return buildResponse(hopDongId, rows.size(), List.of(), skipped, errors, supportsNhomUuTien, request.isFinalBatch());
        }

        maybeReplacePending(
                hopDongId,
                doiTuongQuanLyId,
                pendingImport,
                Boolean.TRUE.equals(request.getReplacePending()),
                request.getRowOffset());
        hopDongDoiTuongService.assertImportAllowed(hopDongId, doiTuongQuanLyId);

        Map<String, UUID> nhomUuTienByTen = supportsNhomUuTien
                ? ensureNhomUuTienBatch(hopDongId, preparedRows)
                : Map.of();

        List<HopDongDoiTuongTaoRequest> doiTuongRequests = buildDoiTuongRequests(
                hopDongId, doiTuongQuanLyId, preparedRows, supportsNhomUuTien, nhomUuTienByTen, pendingImport);

        int batchSize = excelImportProperties.batchSize();
        List<UUID> createdIds = saveDoiTuongInBatches(doiTuongRequests, batchSize);

        List<HopDongDoiTuongGiaTriTaoRequest> giaTriRequests = new ArrayList<>();
        for (int i = 0; i < preparedRows.size(); i++) {
            appendGiaTriRequests(giaTriRequests, createdIds.get(i), preparedRows.get(i).row(), resolvedColumns);
        }
        saveGiaTriInBatches(giaTriRequests, batchSize);

        if (request.isFinalBatch() && supportsNhomUuTien) {
            refreshNhomUuTienStats(hopDongId);
        }

        List<UUID> updatedIds = applyUpdateRows(updateRows, resolvedColumns, batchSize);

        List<UUID> allIds = new ArrayList<>(createdIds);
        allIds.addAll(updatedIds);
        return buildResponse(hopDongId, rows.size(), allIds, skipped, errors, supportsNhomUuTien, request.isFinalBatch());
    }

    /** Dòng trùng khóa chính — cập nhật giá trị vào đối tượng sẵn có (ô trống trong file giữ nguyên giá trị cũ). */
    private record PreparedUpdateRow(Map<String, Object> row, int excelRowNumber, UUID doiTuongId) {
    }

    private Map<String, UUID> loadExistingPrimaryKeyIds(UUID hopDongId, UUID primaryThuocTinhId) {
        Map<String, UUID> map = new LinkedHashMap<>();
        for (Object[] pair : hopDongDoiTuongGiaTriRepository
                .findPrimaryKeyValueIdPairsByHopDongAndThuocTinh(hopDongId, primaryThuocTinhId)) {
            String value = pair[0] != null ? pair[0].toString().trim() : "";
            if (!value.isEmpty() && pair[1] instanceof UUID id) {
                map.putIfAbsent(value, id);
            }
        }
        return map;
    }

    private List<UUID> applyUpdateRows(
            List<PreparedUpdateRow> updateRows,
            List<ResolvedColumn> resolvedColumns,
            int batchSize) {
        if (updateRows.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = updateRows.stream().map(PreparedUpdateRow::doiTuongId).distinct().toList();
        Map<UUID, Map<UUID, HopDongDoiTuongGiaTri>> existingByDoiTuong = new HashMap<>();
        for (HopDongDoiTuongGiaTri entity
                : hopDongDoiTuongGiaTriService.findActiveEntitiesByHopDongDoiTuongIds(ids)) {
            existingByDoiTuong
                    .computeIfAbsent(entity.getHopDongDoiTuongId(), ignored -> new HashMap<>())
                    .put(entity.getThuocTinhId(), entity);
        }
        List<HopDongDoiTuongGiaTriTaoRequest> creates = new ArrayList<>();
        for (PreparedUpdateRow prepared : updateRows) {
            Map<UUID, HopDongDoiTuongGiaTri> byThuocTinh =
                    existingByDoiTuong.getOrDefault(prepared.doiTuongId(), Map.of());
            for (ResolvedColumn column : resolvedColumns) {
                Object raw = prepared.row().get(column.fieldKey());
                String value = raw != null ? String.valueOf(raw).trim() : "";
                if (value.isEmpty()) {
                    continue; // ô trống trong file: giữ giá trị cũ, không xóa
                }
                HopDongDoiTuongGiaTri entity = byThuocTinh.get(column.thuocTinhId());
                if (entity != null) {
                    if (!value.equals(entity.getGiaTri())) {
                        entity.setGiaTri(value);
                        hopDongDoiTuongGiaTriService.saveEntity(entity);
                    }
                } else {
                    HopDongDoiTuongGiaTriTaoRequest create = new HopDongDoiTuongGiaTriTaoRequest();
                    create.setHopDongDoiTuongId(prepared.doiTuongId());
                    create.setThuocTinhId(column.thuocTinhId());
                    create.setGiaTri(value);
                    creates.add(create);
                }
            }
        }
        saveGiaTriInBatches(creates, batchSize);
        return ids;
    }

    private List<PreparedRow> prepareRows(
            List<Map<String, Object>> rows,
            int dataStartRow,
            int rowOffset,
            List<ResolvedColumn> resolvedColumns,
            ResolvedColumn primaryKeyColumn,
            Map<String, UUID> existingIdByPrimaryKey,
            Set<String> importPrimaryKeys,
            List<PreparedUpdateRow> updateRows,
            List<String> errors) {
        List<PreparedRow> preparedRows = new ArrayList<>();

        for (int index = 0; index < rows.size(); index++) {
            Map<String, Object> row = rows.get(index);
            int excelRowNumber = dataStartRow + rowOffset + index;
            String validationError = validateRequired(row, resolvedColumns);
            if (validationError != null) {
                errors.add("Dòng " + excelRowNumber + ": " + validationError);
                continue;
            }
            String rawPrimaryValue = row.get(primaryKeyColumn.fieldKey()) != null
                    ? String.valueOf(row.get(primaryKeyColumn.fieldKey()))
                    : "";
            String primaryValue = normalizePrimaryValue(rawPrimaryValue);
            if (primaryValue.isEmpty()) {
                errors.add("Dòng " + excelRowNumber + ": Thiếu giá trị khóa chính " + primaryKeyColumn.label());
                continue;
            }
            if (!importPrimaryKeys.add(primaryValue)) {
                errors.add("Dòng " + excelRowNumber + ": Trùng khóa chính trong file import (" + rawPrimaryValue.trim() + ")");
                continue;
            }
            RowResolution resolution = resolveRowLinks(row, resolvedColumns);
            if (!resolution.errors().isEmpty()) {
                for (String error : resolution.errors()) {
                    errors.add("Dòng " + excelRowNumber + ": " + error);
                }
                continue;
            }
            UUID existingId = existingIdByPrimaryKey.get(primaryValue);
            if (existingId != null) {
                // Trùng với trạm đã có → CẬP NHẬT giá trị theo file (upsert), không bỏ qua nữa
                updateRows.add(new PreparedUpdateRow(resolution.row(), excelRowNumber, existingId));
                continue;
            }
            preparedRows.add(new PreparedRow(resolution.row(), excelRowNumber));
        }
        return preparedRows;
    }

    private List<HopDongDoiTuongTaoRequest> buildDoiTuongRequests(
            UUID hopDongId,
            UUID doiTuongQuanLyId,
            List<PreparedRow> preparedRows,
            boolean supportsNhomUuTien,
            Map<String, UUID> nhomUuTienByTen,
            boolean importAsPending) {
        List<HopDongDoiTuongTaoRequest> doiTuongRequests = new ArrayList<>(preparedRows.size());
        for (PreparedRow prepared : preparedRows) {
            UUID nhomUuTienId = null;
            if (supportsNhomUuTien) {
                Object rawNhom = prepared.row().get(DoiTuongImportFields.NHOM_UU_TIEN);
                if (rawNhom != null) {
                    String ten = String.valueOf(rawNhom).trim();
                    if (!ten.isEmpty()) {
                        nhomUuTienId = nhomUuTienByTen.get(ten.toLowerCase(Locale.ROOT));
                    }
                }
            }

            HopDongDoiTuongTaoRequest createRequest = new HopDongDoiTuongTaoRequest();
            createRequest.setHopDongId(hopDongId);
            createRequest.setDoiTuongQuanLyId(doiTuongQuanLyId);
            createRequest.setTrangThaiHopDongId(null);
            createRequest.setHopDongNhomUuTienId(nhomUuTienId);
            createRequest.setHoatDong(!importAsPending);
            doiTuongRequests.add(createRequest);
        }
        return doiTuongRequests;
    }

    private void maybeReplacePending(
            UUID hopDongId,
            UUID doiTuongQuanLyId,
            boolean importAsPending,
            boolean replacePending,
            int rowOffset) {
        if (!importAsPending || !replacePending || rowOffset > 0 || doiTuongQuanLyId == null) {
            return;
        }
        HopDongDoiTuongXoaTheoLocRequest deleteRequest = new HopDongDoiTuongXoaTheoLocRequest();
        deleteRequest.setHopDongId(hopDongId);
        deleteRequest.setDoiTuongQuanLyId(doiTuongQuanLyId);
        deleteRequest.setActiveOnly(false);
        hopDongDoiTuongService.deleteByFilter(deleteRequest);
    }

    private void preloadLinkColumns(List<ResolvedColumn> resolvedColumns) {
        for (ResolvedColumn column : resolvedColumns) {
            if (column.linkColumn()) {
                lienKetBangImportResolver.preload(column.thuocTinh());
            }
        }
    }

    private List<UUID> saveDoiTuongInBatches(List<HopDongDoiTuongTaoRequest> requests, int batchSize) {
        List<UUID> createdIds = new ArrayList<>(requests.size());
        for (int offset = 0; offset < requests.size(); offset += batchSize) {
            List<HopDongDoiTuongTaoRequest> chunk = requests.subList(
                    offset, Math.min(offset + batchSize, requests.size()));
            createdIds.addAll(hopDongDoiTuongService.saveBatchForImport(chunk));
        }
        return createdIds;
    }

    private void saveGiaTriInBatches(List<HopDongDoiTuongGiaTriTaoRequest> requests, int batchSize) {
        for (int offset = 0; offset < requests.size(); offset += batchSize) {
            List<HopDongDoiTuongGiaTriTaoRequest> chunk = requests.subList(
                    offset, Math.min(offset + batchSize, requests.size()));
            hopDongDoiTuongGiaTriService.createBatchForImportResolved(chunk);
        }
    }

    private Map<String, UUID> ensureNhomUuTienBatch(UUID hopDongId, List<PreparedRow> preparedRows) {
        Map<String, UUID> cache = new LinkedHashMap<>();
        for (HopDongNhomUuTien existing : hopDongNhomUuTienRepository.findByHopDongIdAndNgayXoaIsNull(hopDongId)) {
            if (existing.getTen() != null) {
                cache.put(existing.getTen().toLowerCase(Locale.ROOT), existing.getId());
            }
        }

        Set<String> missingNames = new LinkedHashSet<>();
        for (PreparedRow prepared : preparedRows) {
            Object rawNhom = prepared.row().get(DoiTuongImportFields.NHOM_UU_TIEN);
            if (rawNhom == null) {
                continue;
            }
            String ten = String.valueOf(rawNhom).trim();
            if (ten.isEmpty()) {
                continue;
            }
            String cacheKey = ten.toLowerCase(Locale.ROOT);
            if (!cache.containsKey(cacheKey)) {
                missingNames.add(ten);
            }
        }

        if (missingNames.isEmpty()) {
            return cache;
        }

        short nextOrder = (short) (hopDongNhomUuTienRepository.findByHopDongIdAndNgayXoaIsNull(hopDongId).stream()
                .map(HopDongNhomUuTien::getThuTu)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse((short) 0) + 1);

        List<HopDongNhomUuTien> toCreate = new ArrayList<>(missingNames.size());
        for (String ten : missingNames) {
            String cacheKey = ten.toLowerCase(Locale.ROOT);
            if (cache.containsKey(cacheKey)) {
                continue;
            }
            HopDongNhomUuTien entity = new HopDongNhomUuTien();
            entity.setHopDongId(hopDongId);
            entity.setTen(ten);
            entity.setMa(generateGroupMa(ten));
            entity.setMauSac(DEFAULT_GROUP_COLOR);
            entity.setKeHoach(0);
            entity.setHoanThanh(0);
            entity.setConLai(0);
            entity.setPhanTram(BigDecimal.ZERO);
            entity.setSoNhan(0);
            entity.setThuTu(nextOrder++);
            toCreate.add(entity);
        }

        for (HopDongNhomUuTien created : hopDongNhomUuTienRepository.saveAll(toCreate)) {
            cache.put(created.getTen().toLowerCase(Locale.ROOT), created.getId());
        }
        return cache;
    }

    private void refreshNhomUuTienStats(UUID hopDongId) {
        Map<UUID, Integer> counts = new HashMap<>();
        for (Object[] row : hopDongDoiTuongRepository.countGroupByNhomUuTienForHopDong(hopDongId)) {
            counts.put((UUID) row[0], ((Number) row[1]).intValue());
        }

        List<HopDongNhomUuTien> groups = hopDongNhomUuTienRepository.findByHopDongIdAndNgayXoaIsNull(hopDongId);
        for (HopDongNhomUuTien group : groups) {
            int count = counts.getOrDefault(group.getId(), 0);
            group.setKeHoach(count);
            group.setConLai(Math.max(0, count - (group.getHoanThanh() != null ? group.getHoanThanh() : 0)));
        }
        hopDongNhomUuTienRepository.saveAll(groups);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> resolvePreviewRows(
            UUID excelMappingId,
            List<Map<String, Object>> rawRows) {
        lienKetBangImportResolver.clearCache();
        ExcelMappingResponse mapping = excelMappingService.getById(excelMappingId);
        int dataStartRow = mapping.getDongBatDauDoc() != null && mapping.getDongBatDauDoc() > 0
                ? mapping.getDongBatDauDoc()
                : 2;
        List<ResolvedColumn> resolvedColumns = resolveColumns(
                mapping.getCotDanhSach() != null ? mapping.getCotDanhSach() : List.of());

        List<Map<String, Object>> previewRows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int index = 0; index < rawRows.size(); index++) {
            int excelRowNumber = dataStartRow + index;
            Map<String, Object> row = rawRows.get(index);
            String validationError = validateRequired(row, resolvedColumns);
            if (validationError != null) {
                errors.add("Dòng " + excelRowNumber + ": " + validationError);
                continue;
            }
            RowResolution resolution = resolveRowLinks(row, resolvedColumns, true);
            if (!resolution.errors().isEmpty()) {
                for (String error : resolution.errors()) {
                    errors.add("Dòng " + excelRowNumber + ": " + error);
                }
                continue;
            }
            previewRows.add(resolution.row());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("duLieuXemTruoc", previewRows);
        response.put("soDongXemTruoc", previewRows.size());
        response.put("loi", errors);
        return response;
    }

    private RowResolution resolveRowLinks(Map<String, Object> row, List<ResolvedColumn> columns) {
        return resolveRowLinks(row, columns, false);
    }

    private RowResolution resolveRowLinks(
            Map<String, Object> row,
            List<ResolvedColumn> columns,
            boolean previewMode) {
        Map<String, Object> resolvedRow = new LinkedHashMap<>(row);
        List<String> errors = new ArrayList<>();

        for (ResolvedColumn column : columns) {
            if (!column.linkColumn() || column.thuocTinh() == null) {
                continue;
            }
            Object raw = row.get(column.fieldKey());
            if (raw == null || String.valueOf(raw).trim().isEmpty()) {
                continue;
            }
            String rawText = String.valueOf(raw).trim();
            if (previewMode) {
                String previewValue = lienKetBangGiaTriNormalizer
                        .resolveDoiTuongPreviewValue(column.thuocTinh(), rawText)
                        .orElse(rawText);
                resolvedRow.put(column.fieldKey(), previewValue);
                continue;
            }
            Optional<String> normalized = lienKetBangGiaTriNormalizer.normalizeDoiTuongGiaTriForImport(
                    column.thuocTinh(),
                    rawText);
            if (normalized.isPresent()) {
                resolvedRow.put(column.fieldKey(), normalized.get());
            } else if (column.required()) {
                errors.add("Không tìm thấy " + column.label() + " khớp với \"" + rawText + "\"");
            } else {
                resolvedRow.remove(column.fieldKey());
            }
        }

        return new RowResolution(resolvedRow, errors);
    }

    private void appendGiaTriRequests(
            List<HopDongDoiTuongGiaTriTaoRequest> target,
            UUID hopDongDoiTuongId,
            Map<String, Object> row,
            List<ResolvedColumn> columns) {
        for (ResolvedColumn column : columns) {
            Object raw = row.get(column.fieldKey());
            if (raw == null) {
                continue;
            }
            String giaTri = String.valueOf(raw).trim();
            if (giaTri.isEmpty()) {
                continue;
            }

            HopDongDoiTuongGiaTriTaoRequest request = new HopDongDoiTuongGiaTriTaoRequest();
            request.setHopDongDoiTuongId(hopDongDoiTuongId);
            request.setThuocTinhId(column.thuocTinhId());
            request.setGiaTri(giaTri);
            request.setHoatDong(true);
            target.add(request);
        }
    }

    private Map<String, Object> buildResponse(
            UUID hopDongId,
            int totalRows,
            List<UUID> createdIds,
            int skipped,
            List<String> errors,
            boolean supportsNhomUuTien) {
        return buildResponse(hopDongId, totalRows, createdIds, skipped, errors, supportsNhomUuTien, true);
    }

    private Map<String, Object> buildResponse(
            UUID hopDongId,
            int totalRows,
            List<UUID> createdIds,
            int skipped,
            List<String> errors,
            boolean supportsNhomUuTien,
            boolean includeNhomStats) {
        int created = createdIds.size();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("soDongXemTruoc", totalRows);
        response.put("daNhap", created);
        response.put("daTao", created);
        response.put("boQua", skipped);
        response.put("loi", errors);
        response.put("thanhCong", created > 0 && errors.isEmpty());
        if (!createdIds.isEmpty() && createdIds.size() <= RESPONSE_ENRICH_LIMIT) {
            List<HopDongDoiTuongResponse> doiTuongDaTao = hopDongDoiTuongService.getByIds(createdIds);
            response.put("doiTuongDaTao", doiTuongDaTao);
        }
        if (includeNhomStats && supportsNhomUuTien) {
            List<HopDongNhomUuTienResponse> nhomUuTienDanhSach = hopDongNhomUuTienRepository
                    .findByHopDongIdAndNgayXoaIsNull(hopDongId)
                    .stream()
                    .sorted(Comparator.comparing(HopDongNhomUuTien::getThuTu, Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(hopDongMapper::toHopDongNhomUuTienResponse)
                    .toList();
            response.put("nhomUuTienDanhSach", nhomUuTienDanhSach);
        }
        response.put(
                "ghiChu",
                !errors.isEmpty()
                        ? "Import thất bại — còn " + errors.size() + " lỗi, không ghi nhận dữ liệu."
                        : (created > 0
                                ? "Đã import " + created + " bản ghi đối tượng"
                                : (totalRows == 0
                                        ? "Chưa parse được dòng nào. Kiểm tra mapping và sheet."
                                        : "Không import được bản ghi nào. Kiểm tra dữ liệu và cột bắt buộc.")));
        if (totalRows > 0) {
            // 1 dòng audit tổng kết mỗi lượt import (không ghi theo từng dòng dữ liệu)
            appEventContext.audit("IMPORT_DOI_TUONG",
                    "Import Excel đối tượng",
                    "Tạo mới " + created + ", cập nhật/bỏ qua " + skipped + " / tổng " + totalRows + " dòng",
                    java.util.Map.of("hopDongId", hopDongId));
        }
        return response;
    }

    private String generateGroupMa(String ten) {
        String normalized = EntityFilter.normalizeCode(ten.replace(' ', '_'));
        if (normalized == null || normalized.isBlank()) {
            return "NHOM_" + System.currentTimeMillis();
        }
        return normalized.length() > 50 ? normalized.substring(0, 50) : normalized;
    }

    private String validateRequired(Map<String, Object> row, List<ResolvedColumn> columns) {
        for (ResolvedColumn column : columns) {
            if (!column.required()) {
                continue;
            }
            Object raw = row.get(column.fieldKey());
            if (raw == null || String.valueOf(raw).trim().isEmpty()) {
                return "Thiếu giá trị bắt buộc: " + column.label();
            }
        }
        return null;
    }

    private String normalizePrimaryValue(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }


    /**
     * Lưới nhập trên FE hiển thị ĐỦ mọi thuộc tính của đối tượng quản lý (không chỉ các cột
     * có trong mapping) để người dùng dán trực tiếp từ Excel theo từng cột. Bổ sung các thuộc
     * tính chưa nằm trong mapping làm cột tùy chọn (không bắt buộc, không khóa chính) để giá
     * trị dán tay ở các cột đó vẫn được ghi nhận; các lô import chỉ chứa cột mapping (đọc từ
     * file) không bị ảnh hưởng vì cột thiếu giá trị sẽ được bỏ qua.
     */
    private void appendUnmappedThuocTinhColumns(UUID doiTuongQuanLyId, List<ResolvedColumn> resolvedColumns) {
        if (doiTuongQuanLyId == null) {
            return;
        }
        Set<UUID> mappedThuocTinhIds = new LinkedHashSet<>();
        Set<String> usedFieldKeys = new LinkedHashSet<>();
        for (ResolvedColumn column : resolvedColumns) {
            mappedThuocTinhIds.add(column.thuocTinhId());
            usedFieldKeys.add(column.fieldKey());
        }
        for (ThuocTinhResponse thuocTinh : thuocTinhService.list(null, true, false, doiTuongQuanLyId, null)) {
            if (thuocTinh.getId() == null || mappedThuocTinhIds.contains(thuocTinh.getId())) {
                continue;
            }
            String fieldKey = thuocTinh.getTen() != null && !thuocTinh.getTen().isBlank()
                    ? thuocTinh.getTen().trim()
                    : thuocTinh.getId().toString();
            if (!usedFieldKeys.add(fieldKey)) {
                continue;
            }
            resolvedColumns.add(new ResolvedColumn(
                    thuocTinh.getId(),
                    fieldKey,
                    fieldKey,
                    false,
                    false,
                    thuocTinh,
                    lienKetBangImportResolver.resolveLinkTable(thuocTinh)));
        }
    }

    private List<ResolvedColumn> resolveColumns(List<ExcelMappingCotResponse> cots) {
        List<ResolvedColumn> resolved = new ArrayList<>();
        for (ExcelMappingCotResponse cot : cots) {
            ThuocTinhResponse thuocTinh = cot.getThuocTinh();
            if (thuocTinh == null || cot.getThuocTinhId() == null) {
                continue;
            }
            String fieldKey = resolveFieldKey(thuocTinh, cot);
            String label = thuocTinh.getTen() != null && !thuocTinh.getTen().isBlank()
                    ? thuocTinh.getTen().trim()
                    : fieldKey;
            resolved.add(new ResolvedColumn(
                    cot.getThuocTinhId(),
                    fieldKey,
                    label,
                    isRequiredColumn(cot, thuocTinh),
                    Boolean.TRUE.equals(thuocTinh.getLaKhoaChinh()),
                    thuocTinh,
                    lienKetBangImportResolver.resolveLinkTable(thuocTinh)));
        }
        return resolved;
    }

    private boolean isRequiredColumn(ExcelMappingCotResponse cot, ThuocTinhResponse thuocTinh) {
        return Boolean.TRUE.equals(cot.getBatBuoc())
                || Boolean.TRUE.equals(thuocTinh.getBatBuoc())
                || Boolean.TRUE.equals(thuocTinh.getLaKhoaChinh());
    }

    private String resolveFieldKey(ThuocTinhResponse thuocTinh, ExcelMappingCotResponse cot) {
        if (thuocTinh.getTen() != null && !thuocTinh.getTen().isBlank()) {
            return thuocTinh.getTen().trim();
        }
        if (cot.getCotExcel() != null && !cot.getCotExcel().isBlank()) {
            return cot.getCotExcel().trim();
        }
        return Objects.requireNonNull(cot.getThuocTinhId()).toString();
    }

}
