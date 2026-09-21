package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.config.AppExcelImportProperties;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ExcelMappingImportService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers.HangMucImportContext;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers.HangMucImportAttributeRole;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers.HangMucImportFieldResolver;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.HangMucImportMappingFields;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers.HangMucImportMaHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers.HangMucImportPersistenceHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers.HangMucImportResponseHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers.ImportCounters;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucChiTietRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucCongViecRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucNhomRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucExcelImportService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongLookupService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HangMucExcelImportServiceImpl implements HangMucExcelImportService {

    private final ExcelMappingImportService excelMappingImportService;
    private final HangMucImportFieldResolver fieldResolver;
    private final HangMucImportPersistenceHelper persistenceHelper;
    private final HangMucNhomRepository hangMucNhomRepository;
    private final HangMucChiTietRepository hangMucChiTietRepository;
    private final HangMucCongViecRepository hangMucCongViecRepository;
    private final HopDongLookupService hopDongLookupService;
    private final AppExcelImportProperties excelImportProperties;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Map<String, Object> importFromExcel(
            UUID hopDongId,
            MultipartFile file,
            UUID excelMappingId,
            String tenSheet) {
        List<Map<String, Object>> rows = excelMappingImportService.parsePreviewRows(
                file, excelMappingId, tenSheet, excelImportProperties.maxImportRows());

        HangMucImportMappingFields fields = fieldResolver.resolveMappingFields(excelMappingId);
        String contractSuffix = buildContractSuffix(hopDongId);
        HangMucImportContext ctx = HangMucImportContext.preload(
                hopDongId, contractSuffix,
                hangMucNhomRepository, hangMucChiTietRepository, hangMucCongViecRepository);
        ctx.flatGroupId = persistenceHelper.ensureDefaultImportGroup(hopDongId, ctx);

        int created = 0;
        int updated = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();
        short groupOrder = 0;

        for (Map<String, Object> row : rows) {
            String tenNhom = Optional.ofNullable(
                    fieldResolver.readString(row, fields.nhomHangMuc(), HangMucImportAttributeRole.NHOM_HANG_MUC))
                    .orElse("");
            String tenHangMuc = fieldResolver.readString(row, fields.hangMuc(), HangMucImportAttributeRole.HANG_MUC);
            ImportCounters counters = persistenceHelper.importGroupedRow(
                    hopDongId, ctx, row, fields, tenNhom, tenHangMuc, groupOrder++);
            created += counters.created;
            updated += counters.updated;
            skipped += counters.skipped;
            errors.addAll(counters.errors);
        }

        entityManager.flush();
        return HangMucImportResponseHelper.buildImportResponse(
                rows, created, updated, skipped, errors, ctx.groupByHangMucKey.size());
    }

    private String buildContractSuffix(UUID hopDongId) {
        HopDongResponse hopDong = hopDongLookupService.getById(hopDongId);
        return HangMucImportMaHelper.buildContractSuffix(hopDong, hopDongId);
    }
}
