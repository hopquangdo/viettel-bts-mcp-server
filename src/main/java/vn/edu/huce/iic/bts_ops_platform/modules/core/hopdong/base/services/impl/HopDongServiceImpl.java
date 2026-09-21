package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.impl;

import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventContext;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.storage.LocalFileStorageService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.LinkedDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongCanceledEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDeletedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongMetaChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScope;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScopeService;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.PaginationDefaults;
import vn.edu.huce.iic.bts_ops_platform.config.AppExcelImportProperties;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.cache.HopDongAggregateCache;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LoaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ExcelMappingImportService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.util.ExcelParseResult;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.DoiTuongHopDongLienKetService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DoiTuongHopDongLienKetResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.DoiTuongHopDongLienKetSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.LienKetBangSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhKieuHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LienKetBangGiaTriNormalizer;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhHopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhKieuHopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucExcelImportService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongExcelImportService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.response.TepDinhKemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.services.TepDinhKemService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongImportBatchRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongThuocTinhGiaTriItem;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.HopDongDanhSachTienDoHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.HopDongThongKeTatCaHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.HopDongTvtkHangMucBootstrap;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.exception.FileErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongTaiLieuDownload;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongTaiLieuItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThongKeTatCaResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongTepDinhKem;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongThuocTinh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.mapper.HopDongMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongDanhSachService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongNhomUuTienService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTienDoBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongTepDinhKemService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongThuocTinhService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.services.VuongMacService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.services.impl.VuongMacServiceImpl;

import java.time.Instant;
import java.time.LocalDate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HopDongServiceImpl implements HopDongService {


    private final HopDongRepository hopDongRepository;
    private final HopDongMapper hopDongMapper;
    private final AppEventContext appEventContext;
    private final HopDongThuocTinhService hopDongThuocTinhService;
    private final ThuocTinhHopDongService thuocTinhHopDongService;
    private final ThuocTinhKieuHopDongService thuocTinhKieuHopDongService;
    private final DoiTuongHopDongLienKetService doiTuongHopDongLienKetService;
    private final ExcelMappingImportService excelMappingImportService;
    private final HangMucExcelImportService hangMucExcelImportService;
    private final HopDongDoiTuongExcelImportService hopDongDoiTuongExcelImportService;
    private final AppExcelImportProperties excelImportProperties;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    @Autowired
    @Lazy
    private HopDongDanhSachService hopDongDanhSachService;
    private final TepDinhKemService tepDinhKemService;
    private final LocalFileStorageService localFileStorageService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final HopDongTepDinhKemService hopDongTepDinhKemService;
    private final KieuHopDongRepository kieuHopDongRepository;
    private final LoaiHopDongRepository loaiHopDongRepository;
    private final HopDongTvtkHangMucBootstrap hopDongTvtkHangMucBootstrap;
    private final ContractorScopeService contractorScopeService;
    private final LienKetBangGiaTriNormalizer lienKetBangGiaTriNormalizer;
    private final HopDongAggregateCache hopDongAggregateCache;
    private final VuongMacService vuongMacService;
    private final vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.services.HopDongArchiveGuard hopDongArchiveGuard;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HopDongResponse> list(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID loaiHopDongId,
            UUID kieuHopDongId,
            Integer page,
            Integer size) {
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE);
        int pageNumber = EntityFilter.normalizePage(page);
        long offset = (long) pageNumber * pageSize;
        String keyword = EntityFilter.normalizeSearch(search);

        boolean includeArchive = false;
        long total = keyword.isBlank()
                ? hopDongRepository.countSearchNative(
                        includeDeleted, activeOnly, loaiHopDongId, kieuHopDongId, includeArchive)
                : hopDongRepository.countSearchWithKeyword(
                        includeDeleted, activeOnly, loaiHopDongId, kieuHopDongId, keyword, includeArchive);

        List<HopDong> fetched = keyword.isBlank()
                ? hopDongRepository.searchPageNative(
                        includeDeleted, activeOnly, loaiHopDongId, kieuHopDongId, includeArchive, pageSize, offset)
                : hopDongRepository.searchPageWithKeyword(
                        includeDeleted, activeOnly, loaiHopDongId, kieuHopDongId, keyword, includeArchive, pageSize, offset);

        List<HopDong> pageItems = applyContractorScope(fetched);
        List<HopDongResponse> items = toListResponses(pageItems);

        return PageResponse.ofItems(items, pageNumber, pageSize, total);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongResponse> listAll(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID loaiHopDongId,
            UUID kieuHopDongId) {
        List<HopDongResponse> all = new ArrayList<>();
        int pageNumber = 0;
        while (true) {
            PageResponse<HopDongResponse> page = list(
                    search, activeOnly, includeDeleted, loaiHopDongId, kieuHopDongId, pageNumber, PaginationDefaults.MAX_PAGE_SIZE);
            all.addAll(page.getItems());
            if (page.getTotalPages() == 0 || pageNumber + 1 >= page.getTotalPages()) {
                break;
            }
            pageNumber++;
        }
        return all;
    }



    @Override
    @Transactional(readOnly = true)
    public HopDongResponse getById(UUID id) {
        contractorScopeService.assertHopDongAccessible(id);
        return toEnrichedResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, String> mapContractLabelsByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return hopDongRepository.findByIdInAndNgayXoaIsNull(ids.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(
                        HopDong::getId,
                        hopDong -> {
                            String ma = hopDong.getMaHopDong();
                            if (ma != null && !ma.isBlank()) {
                                return ma.trim();
                            }
                            return hopDong.getId().toString();
                        },
                        (left, right) -> left));
    }

    @Override
    @Transactional
    public HopDongResponse create(HopDongTaoRequest request) {
        validateThuocTinhGiaTri(request.getKieuHopDongId(), request.getThuocTinhGiaTri());
        assertMaHopDongUnique(request.getMaHopDong(), null);

        HopDong entity = hopDongMapper.fromHopDongTaoRequest(request);
        entity.setMaHopDong(request.getMaHopDong().trim());
        entity.setMa(request.getMaHopDong().trim());
        entity.setTen(request.getTen().trim());
        entity.setGiaTriHd(request.getGiaTriHd());
        applyThucHienFields(entity, request.getNgayThucHien(), request.getSoNgayThucHien());
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());
        if (entity.getTrangThaiThiCong() == null || entity.getTrangThaiThiCong().isBlank()) {
            entity.setTrangThaiThiCong("CHUA_TC");
        }
        syncLoaiTheoKieu(entity);
        HopDong saved = hopDongRepository.save(entity);

        List<HopDongThuocTinhGiaTriItem> thuocTinhItems = syncMaHopDongIntoThuocTinh(
                saved.getMaHopDong(),
                saved.getKieuHopDongId(),
                request.getThuocTinhGiaTri()
        );
        saveThuocTinhGiaTri(saved.getId(), saved.getKieuHopDongId(), thuocTinhItems);
        hopDongTvtkHangMucBootstrap.seedDefaultHangMucIfTuVan(saved);
        hopDongAggregateCache.evictAll();
        appEventContext.audit("TAO_HOP_DONG", "Tạo hợp đồng " + saved.getMaHopDong(), null,
                Map.of("hopDongId", saved.getId()));
        return toEnrichedResponse(saved);
    }

    @Override
    @Transactional
    public TepDinhKemResponse uploadTaiLieu(UUID hopDongId, MultipartFile file, String loaiTaiLieu) {
        findById(hopDongId);
        TepDinhKemResponse tepDinhKem = tepDinhKemService.upload(file, "hop-dong");

        HopDongTepDinhKem link = new HopDongTepDinhKem();
        link.setHopDongId(hopDongId);
        link.setTepDinhKemId(tepDinhKem.getId());
        link.setLoaiTaiLieu(resolveLoaiTaiLieuHopDong(loaiTaiLieu));
        link.setHoatDong(true);
        hopDongTepDinhKemService.saveEntity(link);

        return tepDinhKem;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongTaiLieuItemResponse> listTaiLieu(UUID hopDongId) {
        findById(hopDongId);
        return hopDongTepDinhKemService.findActiveEntitiesByHopDongId(hopDongId).stream()
                .filter(link -> Boolean.TRUE.equals(link.getHoatDong()))
                .sorted(Comparator.comparing(HopDongTepDinhKem::getNgayTao, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toTaiLieuItemResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteTaiLieu(UUID hopDongId, UUID linkId) {
        findById(hopDongId);
        HopDongTepDinhKem link = hopDongTepDinhKemService.findActiveEntitiesByHopDongId(hopDongId).stream()
                .filter(item -> linkId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new AppException(
                        HopDongErrorCode.HOP_DONG_TEP_DINH_KEM_NOT_FOUND,
                        "Không tìm thấy tài liệu hợp đồng"));
        link.setNgayXoa(Instant.now());
        link.setHoatDong(false);
        hopDongTepDinhKemService.saveEntity(link);
    }

    @Override
    @Transactional(readOnly = true)
    public HopDongTaiLieuDownload downloadTaiLieu(UUID hopDongId, UUID linkId) {
        findById(hopDongId);
        HopDongTepDinhKem link = hopDongTepDinhKemService.findActiveEntitiesByHopDongId(hopDongId).stream()
                .filter(item -> linkId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new AppException(
                        HopDongErrorCode.HOP_DONG_TEP_DINH_KEM_NOT_FOUND,
                        "Không tìm thấy tài liệu hợp đồng"));
        TepDinhKemResponse tep = tepDinhKemService.getById(link.getTepDinhKemId());
        if (tep.getDuongDan() == null || tep.getDuongDan().isBlank()) {
            throw new AppException(FileErrorCode.TEP_DINH_KEM_NOT_FOUND, "Không tìm thấy file tài liệu");
        }
        try {
            Path path = localFileStorageService.resolveRelativePath(tep.getDuongDan());
            byte[] content = Files.readAllBytes(path);
            String fileName = firstNonBlank(tep.getTenTepGoc(), firstNonBlank(tep.getTenTep(), "tai-lieu-hop-dong"));
            String contentType = tep.getMimeType() != null && !tep.getMimeType().isBlank()
                    ? tep.getMimeType()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;
            return new HopDongTaiLieuDownload(content, fileName, contentType);
        } catch (IOException e) {
            throw new AppException(FileErrorCode.FILE_UPLOAD_FAILED, "Không đọc được file tài liệu: " + e.getMessage());
        }
    }

    private HopDongTaiLieuItemResponse toTaiLieuItemResponse(HopDongTepDinhKem link) {
        HopDongTaiLieuItemResponse item = new HopDongTaiLieuItemResponse();
        item.setId(link.getId());
        item.setTepDinhKemId(link.getTepDinhKemId());
        item.setLoaiTaiLieu(link.getLoaiTaiLieu());
        item.setGhiChu(link.getGhiChu());
        item.setNgayTao(link.getNgayTao());
        try {
            TepDinhKemResponse tep = tepDinhKemService.getById(link.getTepDinhKemId());
            item.setTenTep(tep.getTenTep());
            item.setTenTepGoc(tep.getTenTepGoc());
            item.setUrl(tep.getUrl());
        } catch (RuntimeException ignored) {
            // Bản ghi link còn nhưng file gốc đã mất — vẫn trả metadata link.
        }
        return item;
    }

    private String resolveLoaiTaiLieuHopDong(String loaiTaiLieu) {
        if (loaiTaiLieu == null || loaiTaiLieu.isBlank()) {
            return "HOP_DONG";
        }
        String normalized = loaiTaiLieu.trim().toUpperCase(Locale.ROOT);
        if ("PHU_LUC".equals(normalized) || "PHU_LUC_HOP_DONG".equals(normalized)) {
            return "PHU_LUC";
        }
        return "HOP_DONG";
    }

    @Override
    @Transactional
    public HopDongResponse update(UUID id, HopDongCapNhatRequest request) {
        HopDong entity = findById(id);
        hopDongArchiveGuard.assertWritable(id);
        if (Boolean.FALSE.equals(entity.getHoatDong())) {
            throw new AppException(HopDongErrorCode.HOP_DONG_CANCELLED, "Hợp đồng đã hủy, không thể chỉnh sửa");
        }
        UUID kieuHopDongId = request.getKieuHopDongId() != null ? request.getKieuHopDongId() : entity.getKieuHopDongId();
        validateThuocTinhGiaTri(kieuHopDongId, request.getThuocTinhGiaTri());

        hopDongMapper.updateFromHopDongCapNhatRequest(request, entity);
        if (request.getMaHopDong() != null && !request.getMaHopDong().isBlank()) {
            String trimmedMa = request.getMaHopDong().trim();
            assertMaHopDongUnique(trimmedMa, id);
            entity.setMaHopDong(trimmedMa);
            entity.setMa(trimmedMa);
        }
        if (request.getTen() != null && !request.getTen().isBlank()) {
            entity.setTen(request.getTen().trim());
        }
        if (request.getGiaTriHd() != null) {
            entity.setGiaTriHd(request.getGiaTriHd());
        }
        if (request.getNgayThucHien() != null || request.getSoNgayThucHien() != null) {
            LocalDate ngayThucHien = request.getNgayThucHien() != null
                    ? request.getNgayThucHien()
                    : entity.getNgayThucHien();
            Integer soNgayThucHien = request.getSoNgayThucHien() != null
                    ? request.getSoNgayThucHien()
                    : entity.getSoNgayThucHien();
            applyThucHienFields(entity, ngayThucHien, soNgayThucHien);
        }
        syncLoaiTheoKieu(entity);
        HopDong saved = hopDongRepository.save(entity);

        List<HopDongThuocTinhGiaTriItem> thuocTinhItems = syncMaHopDongIntoThuocTinh(
                saved.getMaHopDong(),
                kieuHopDongId,
                request.getThuocTinhGiaTri());
        replaceThuocTinhGiaTri(saved.getId(), kieuHopDongId, thuocTinhItems);
        // Tên/mã/loại hợp đồng đều có thể đổi ở trên — ảnh hưởng cache descriptor của
        // MỌI đối tượng thuộc hợp đồng này, nên evict theo hopDongId thay vì đoán field.
        applicationEventPublisher.publishEvent(HopDongMetaChangedEvent.of(saved.getId()));
        hopDongAggregateCache.evictAll();
        appEventContext.audit("CAP_NHAT_HOP_DONG", "Cập nhật hợp đồng " + saved.getMaHopDong(), null,
                Map.of("hopDongId", saved.getId()));
        return toEnrichedResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<HopDong> findActiveEntityById(UUID id) {
        return hopDongRepository.findByIdAndNgayXoaIsNull(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDong> findAllActiveEntities() {
        return hopDongRepository.findByNgayXoaIsNull();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        HopDong entity = findById(id);
        hopDongArchiveGuard.assertWritable(id);
        Instant now = Instant.now();
        entity.setNgayXoa(now);
        entity.setHoatDong(false);
        hopDongRepository.save(entity);
        // Cascade sang HopDongDoiTuong/GiaTri/ThuocTinh/NhomUuTien/TepDinhKem (và mọi module
        // khác nghe HopDongDeletedEvent) chạy qua event — xem HopDongConsumer.onHopDongDeleted.
        applicationEventPublisher.publishEvent(HopDongDeletedEvent.of(id));
        hopDongAggregateCache.evictAll();
        appEventContext.audit("XOA_HOP_DONG", "Xóa hợp đồng " + entity.getMaHopDong(), null,
                Map.of("hopDongId", id));
    }

    @Override
    @Transactional
    public HopDongResponse cancel(UUID id) {
        HopDong entity = findById(id);
        hopDongArchiveGuard.assertWritable(id);
        if (Boolean.FALSE.equals(entity.getHoatDong())) {
            throw new AppException(HopDongErrorCode.HOP_DONG_CANCELLED, "Hợp đồng đã được hủy trước đó");
        }
        entity.setHoatDong(false);
        entity.setTrangThaiThiCong("HUY");
        hopDongRepository.save(entity);

        for (HopDongDoiTuong doiTuong : hopDongDoiTuongService.findActiveEntitiesByHopDongId(id)) {
            doiTuong.setHoatDong(false);
            hopDongDoiTuongService.saveEntity(doiTuong);
        }

        // Cascade sang sản lượng do business.sanluong tự xử lý (xem SanLuongConsumer) —
        // core chỉ phát sự kiện, không gọi thẳng repository của module khác.
        applicationEventPublisher.publishEvent(HopDongCanceledEvent.of(id));
        hopDongAggregateCache.evictAll();

        return toEnrichedResponse(entity);
    }

    private void assertMaHopDongUnique(String maHopDong, UUID excludeId) {
        if (maHopDong == null || maHopDong.isBlank()) {
            throw new AppException(HopDongErrorCode.HOP_DONG_THUOC_TINH_INVALID, "Mã hợp đồng không được để trống");
        }
        String normalized = maHopDong.trim();
        boolean exists = excludeId == null
                ? hopDongRepository.existsByMaHopDongIgnoreCaseAndNgayXoaIsNull(normalized)
                : hopDongRepository.existsByMaHopDongIgnoreCaseAndNgayXoaIsNullAndIdNot(normalized, excludeId);
        if (exists) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_CODE_EXISTS,
                    "Mã hợp đồng \"" + normalized + "\" đã tồn tại");
        }
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<String, Object> thongKe(UUID loaiHopDongId, UUID kieuHopDongId) {
        String cacheKey = thongKeCacheKey(loaiHopDongId, kieuHopDongId);
        return (Map<String, Object>) hopDongAggregateCache.getOrLoad(
                cacheKey, Map.class, () -> computeThongKe(loaiHopDongId, kieuHopDongId));
    }

    private static String thongKeCacheKey(UUID loaiHopDongId, UUID kieuHopDongId) {
        return "thong-ke|" + (loaiHopDongId != null ? loaiHopDongId : "") + "|" + (kieuHopDongId != null ? kieuHopDongId : "");
    }

    private Map<String, Object> computeThongKe(UUID loaiHopDongId, UUID kieuHopDongId) {
        Boolean activeOnly = Boolean.TRUE;
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("loaiHopDongId", loaiHopDongId);
        response.put("kieuHopDongId", kieuHopDongId);
        response.put("tongHopDong", hopDongRepository.countFilteredWithKieu(activeOnly, loaiHopDongId, kieuHopDongId));
        response.put("tongHoatDong", hopDongRepository.countFilteredWithKieu(activeOnly, loaiHopDongId, kieuHopDongId));
        response.put("tongDoiTuong", hopDongDoiTuongService.countFilteredByLoaiHopDong(activeOnly, loaiHopDongId, kieuHopDongId));
        response.put("capNhatLuc", Instant.now().toString());

        Map<String, Long> tienDoTheoTrangThai = toCountMap(
                hopDongRepository.countGroupByTrangThaiThiCong(loaiHopDongId, kieuHopDongId));
        response.put("tienDoTheoTrangThai", tienDoTheoTrangThai);

        Map<String, Long> objectStatusAgg = new LinkedHashMap<>();
        for (Object[] row : hopDongDoiTuongService.countGroupByHopDongIdAndTrangThaiMa(
                activeOnly, loaiHopDongId, kieuHopDongId)) {
            String ma = row[1] != null ? row[1].toString() : "UNKNOWN";
            long count = row[2] instanceof Number number ? number.longValue() : 0L;
            objectStatusAgg.merge(ma, count, Long::sum);
        }
        long tongDoiTuong = hopDongDoiTuongService.countFilteredByLoaiHopDong(activeOnly, loaiHopDongId, kieuHopDongId);
        List<LuongTrangThaiBuocResponse> flowBuocForTyLe = List.of();
        if (kieuHopDongId != null) {
            List<LuongTrangThaiBuocResponse> flowBuoc =
                    hopDongDanhSachService.getFlowBuocForKieu(loaiHopDongId, kieuHopDongId);
            flowBuocForTyLe = flowBuoc;
            if (!flowBuoc.isEmpty()) {
                Map<String, Long> statusByMaTongHop = new LinkedHashMap<>();
                for (Object[] row : hopDongDoiTuongService.countGroupByHopDongIdAndTrangThaiMa(
                        activeOnly, loaiHopDongId, kieuHopDongId)) {
                    String ma = row[1] != null ? row[1].toString() : "UNKNOWN";
                    long count = row[2] instanceof Number number ? number.longValue() : 0L;
                    statusByMaTongHop.merge(ma, count, Long::sum);
                }
                List<HopDongDanhSachTienDoBuocResponse> tienDoTheoLuongThucTe =
                        HopDongDanhSachTienDoHelper.buildTienDoBuocListForDisplay(flowBuoc, statusByMaTongHop);
                response.put("tienDoTheoLuongThucTe", tienDoTheoLuongThucTe);
            }
        }
        response.put(
                "tyLeHoanThanh",
                HopDongDanhSachTienDoHelper.computeAggregateTyLeHoanThanh(
                        objectStatusAgg, tongDoiTuong, flowBuocForTyLe));

        List<Map<String, Object>> soLuongTheoKieu = buildSoLuongTheoKieu(activeOnly, loaiHopDongId);
        response.put("soLuongTheoKieu", soLuongTheoKieu);

        if (loaiHopDongId == null) {
            Map<String, Long> soLuongTheoLoai = new LinkedHashMap<>();
            for (Object[] row : hopDongRepository.countGroupByLoaiHopDongId(activeOnly)) {
                UUID loaiId = (UUID) row[0];
                Long count = (Long) row[1];
                if (loaiId != null) {
                    soLuongTheoLoai.put(loaiId.toString(), count);
                }
            }
            response.put("soLuongTheoLoai", soLuongTheoLoai);
        }

        List<Map<String, Object>> hopDongChiTiet = buildHopDongChiTiet(activeOnly, loaiHopDongId, kieuHopDongId);
        response.put("hopDongChiTiet", hopDongChiTiet);

        long soHopDongChamTienDo = hopDongChiTiet.stream()
                .filter(item -> item.get("tyLeHoanThanh") instanceof Number n && n.doubleValue() < CHAM_TIEN_DO_NGUONG_PHAN_TRAM)
                .count();
        response.put("soHopDongChamTienDo", soHopDongChamTienDo);

        long soHopDongVuongMac = vuongMacService.demActiveTheoTrangThaiVaPhamVi(
                VuongMacServiceImpl.TRANG_THAI_DANG_MO, loaiHopDongId, kieuHopDongId);
        response.put("soHopDongVuongMac", soHopDongVuongMac);

        return response;
    }

    private static final double CANH_BAO_XANH_NGUONG_PHAN_TRAM = 90.0;
    private static final double CANH_BAO_VANG_NGUONG_PHAN_TRAM = 70.0;

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<String, Object> canhBaoTienDo(String loaiHopDong) {
        UUID loaiHopDongId = null;
        if (loaiHopDong != null && !loaiHopDong.isBlank()) {
            List<vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LoaiHopDong> matched =
                    loaiHopDongRepository.search(false, true, loaiHopDong.trim().toLowerCase(Locale.ROOT));
            loaiHopDongId = matched.isEmpty() ? null : matched.get(0).getId();
        }
        String cacheKey = "canh-bao-tien-do|" + (loaiHopDongId != null ? loaiHopDongId : "");
        UUID finalLoaiHopDongId = loaiHopDongId;
        return (Map<String, Object>) hopDongAggregateCache.getOrLoad(
                cacheKey, Map.class, () -> computeCanhBaoTienDo(finalLoaiHopDongId));
    }

    private Map<String, Object> computeCanhBaoTienDo(UUID loaiHopDongId) {
        List<Map<String, Object>> hopDongChiTiet = buildHopDongChiTiet(Boolean.TRUE, loaiHopDongId, null);

        List<UUID> hopDongIds = hopDongChiTiet.stream()
                .map(item -> UUID.fromString((String) item.get("hopDongId")))
                .toList();
        Map<UUID, HopDong> hopDongById = hopDongRepository.findByIdInAndNgayXoaIsNull(hopDongIds).stream()
                .collect(Collectors.toMap(HopDong::getId, hd -> hd, (a, b) -> a));

        long soXanh = 0;
        long soVang = 0;
        long soDo = 0;
        List<Map<String, Object>> hopDongDo = new ArrayList<>();
        for (Map<String, Object> item : hopDongChiTiet) {
            double tyLe = item.get("tyLeHoanThanh") instanceof Number n ? n.doubleValue() : 0.0;
            if (tyLe >= CANH_BAO_XANH_NGUONG_PHAN_TRAM) {
                soXanh++;
            } else if (tyLe >= CANH_BAO_VANG_NGUONG_PHAN_TRAM) {
                soVang++;
            } else {
                soDo++;
                UUID hopDongId = UUID.fromString((String) item.get("hopDongId"));
                HopDong hopDong = hopDongById.get(hopDongId);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("hopDongId", hopDongId);
                row.put("maHopDong", hopDong != null ? firstNonBlank(hopDong.getMa(), hopDong.getMaHopDong()) : null);
                row.put("ten", hopDong != null ? hopDong.getTen() : null);
                row.put("tyLeHoanThanh", tyLe);
                hopDongDo.add(row);
            }
        }
        hopDongDo.sort(Comparator.comparingDouble(row -> ((Number) row.get("tyLeHoanThanh")).doubleValue()));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("soHopDongXanh", soXanh);
        response.put("soHopDongVang", soVang);
        response.put("soHopDongDo", soDo);
        response.put("hopDongDo", hopDongDo);
        return response;
    }

    /**
     * Bản "tất cả loại" của {@link #thongKe(UUID, UUID)}: 6 query cố định — 1 lần nạp hợp đồng,
     * 3 bảng đã group theo hợp đồng, 2 lần tra tên loại/kiểu — rồi gom theo loại trong Java,
     * thay vì chạy lại toàn bộ bộ countGroupBy*() một lần cho mỗi loaiHopDongId.
     * Số query KHÔNG tăng theo số loại hợp đồng.
     */
    @Override
    @Transactional(readOnly = true)
    public HopDongThongKeTatCaResponse thongKeTatCa() {
        Boolean activeOnly = Boolean.TRUE;

        List<Object[]> hopDongRows = hopDongRepository.findThongKeRows(activeOnly);
        List<Object[]> soDoiTuongRows = hopDongDoiTuongService.countGroupByHopDongId(activeOnly, null, null);
        List<Object[]> vuongMacRows = vuongMacService.demOpenIssuesGroupByHopDong(null, null);
        List<Object[]> trangThaiDoiTuongRows =
                hopDongDoiTuongService.countGroupByHopDongIdAndTrangThaiMa(activeOnly, null, null);

        // Danh mục ĐẦY ĐỦ, không chỉ kiểu đang có hợp đồng — nếu không sẽ mất tab của các kiểu
        // chưa phát sinh hợp đồng (thực tế 16/28 kiểu rơi vào trường hợp này).
        return HopDongThongKeTatCaHelper.build(
                Instant.now().toString(),
                hopDongRows,
                soDoiTuongRows,
                vuongMacRows,
                trangThaiDoiTuongRows,
                loaiHopDongRepository.findByNgayXoaIsNull(),
                kieuHopDongRepository.findByNgayXoaIsNull(),
                hopDongDanhSachService.getFlowBuocForAllKieu());
    }

    /** Hợp đồng có tỷ lệ hoàn thành dưới ngưỡng này được xem là "chậm tiến độ". */
    private static final double CHAM_TIEN_DO_NGUONG_PHAN_TRAM =
            HopDongThongKeTatCaHelper.CHAM_TIEN_DO_NGUONG_PHAN_TRAM;

    private List<Map<String, Object>> buildSoLuongTheoKieu(Boolean activeOnly, UUID loaiHopDongId) {
        Map<UUID, Long> counts = new LinkedHashMap<>();
        for (Object[] row : hopDongRepository.countGroupByKieuHopDongId(activeOnly, loaiHopDongId)) {
            UUID kieuId = (UUID) row[0];
            Long count = (Long) row[1];
            if (kieuId != null) {
                counts.put(kieuId, count);
            }
        }
        if (counts.isEmpty()) {
            return List.of();
        }
        Map<UUID, KieuHopDong> kieuById = kieuHopDongRepository.findAllById(counts.keySet()).stream()
                .filter(item -> item.getNgayXoa() == null)
                .collect(Collectors.toMap(KieuHopDong::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<UUID, Long> entry : counts.entrySet()) {
            KieuHopDong kieu = kieuById.get(entry.getKey());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("kieuHopDongId", entry.getKey().toString());
            item.put("soLuong", entry.getValue());
            if (kieu != null) {
                item.put("ma", kieu.getMa());
                item.put("ten", kieu.getTen());
                item.put("nhom", kieu.getNhom());
            }
            items.add(item);
        }
        return items;
    }

    private List<Map<String, Object>> buildHopDongChiTiet(Boolean activeOnly, UUID loaiHopDongId, UUID kieuHopDongId) {
        Map<UUID, Long> totalsByHopDong = new LinkedHashMap<>();
        for (Object[] row : hopDongDoiTuongService.countGroupByHopDongId(activeOnly, loaiHopDongId, kieuHopDongId)) {
            totalsByHopDong.put((UUID) row[0], (Long) row[1]);
        }

        Map<UUID, Map<String, Long>> statusByHopDong = new LinkedHashMap<>();
        for (Object[] row : hopDongDoiTuongService.countGroupByHopDongIdAndTrangThaiMa(activeOnly, loaiHopDongId, kieuHopDongId)) {
            UUID hopDongId = (UUID) row[0];
            String ma = row[1] != null ? row[1].toString() : "UNKNOWN";
            Long count = (Long) row[2];
            statusByHopDong
                    .computeIfAbsent(hopDongId, ignored -> new LinkedHashMap<>())
                    .put(ma, count);
        }

        Map<UUID, Long> vuongMacByHopDong = new LinkedHashMap<>();
        for (Object[] row : vuongMacService.demOpenIssuesGroupByHopDong(loaiHopDongId, kieuHopDongId)) {
            vuongMacByHopDong.put((UUID) row[0], (Long) row[1]);
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<UUID, Long> entry : totalsByHopDong.entrySet()) {
            Map<String, Long> statusCounts = statusByHopDong.getOrDefault(entry.getKey(), Map.of());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("hopDongId", entry.getKey().toString());
            item.put("soDoiTuong", entry.getValue());
            item.put("tienDoTheoTrangThai", statusCounts);
            item.put("tyLeHoanThanh", HopDongDanhSachTienDoHelper.computeTyLeHoanThanh(statusCounts));
            item.put("soVuongMac", vuongMacByHopDong.getOrDefault(entry.getKey(), 0L));
            items.add(item);
        }
        return items;
    }

    private Map<String, Long> toCountMap(List<Object[]> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            if (row[0] == null) {
                continue;
            }
            result.put(row[0].toString(), (Long) row[1]);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public long countActive() {
        return hopDongRepository.countFiltered(true, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongResponse> getByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return hopDongRepository.findByIdInAndNgayXoaIsNull(ids.stream().distinct().toList()).stream()
                .map(hopDongMapper::toHopDongResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Long> demActiveNhomTheoLoaiHopDong() {
        Map<UUID, Long> result = new HashMap<>();
        for (Object[] row : hopDongRepository.countGroupByLoaiHopDongId(Boolean.TRUE)) {
            if (row[0] == null) {
                continue;
            }
            result.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> previewImportExcel(UUID hopDongId, MultipartFile file, UUID excelMappingId, String tenSheet) {
        HopDong hopDong = findById(hopDongId);
        List<UUID> doiTuongQuanLyIds = hopDongDoiTuongService.listAll(null, true, false, hopDongId, null, null, null, null).stream()
                .map(item -> item.getDoiTuongQuanLyId())
                .filter(id -> id != null)
                .toList();
        List<Map<String, Object>> mappings = excelMappingImportService.listPreviewMappings(doiTuongQuanLyIds);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("hopDongId", hopDong.getId());
        response.put("maHopDong", hopDong.getMaHopDong());
        response.put("fileName", file != null ? file.getOriginalFilename() : null);
        response.put("soMapping", mappings.size());
        response.put("mappings", mappings);

        if (excelMappingId != null && file != null && !file.isEmpty()) {
            ExcelParseResult parsed = excelMappingImportService.buildPreview(
                    file, excelMappingId, tenSheet, excelImportProperties.maxPreviewRows());
            response.put("sheetDanhSach", parsed.sheetNames());

            if (isHangMucImportMapping(excelMappingId)) {
                response.put("duLieuXemTruoc", parsed.rows());
                response.put("soDongXemTruoc", parsed.rows().size());
            } else {
                Map<String, Object> resolvedPreview = hopDongDoiTuongExcelImportService.resolvePreviewRows(
                        excelMappingId, parsed.rows());
                response.put("duLieuXemTruoc", resolvedPreview.get("duLieuXemTruoc"));
                response.put("soDongXemTruoc", resolvedPreview.get("soDongXemTruoc"));
                Object previewErrors = resolvedPreview.get("loi");
                if (previewErrors instanceof List<?> errors && !errors.isEmpty()) {
                    response.put("loi", errors);
                }
            }
        } else {
            response.put("sheetDanhSach", excelMappingImportService.listSheetNames(file));
        }

        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> importExcel(
            UUID hopDongId,
            MultipartFile file,
            UUID excelMappingId,
            String tenSheet,
            Boolean importAsPending,
            Boolean replacePending) {
        HopDong hopDong = findById(hopDongId);
        Map<String, Object> response = buildImportCommitBaseResponse(hopDong, file, excelMappingId, tenSheet);

        if (excelMappingId == null || file == null || file.isEmpty()) {
            response.put("daNhap", 0);
            response.put("thanhCong", Boolean.FALSE);
            response.put("ghiChu", "Chưa parse được dòng nào. Kiểm tra mapping và sheet.");
            return response;
        }

        if (isHangMucImportMapping(excelMappingId)) {
            Map<String, Object> importResult = hangMucExcelImportService.importFromExcel(
                    hopDongId, file, excelMappingId, tenSheet);
            response.putAll(importResult);
            return response;
        }

        assertStationDoiTuongConfigured(hopDong);

        Map<String, Object> importResult = hopDongDoiTuongExcelImportService.importFromExcel(
                hopDongId, file, excelMappingId, tenSheet, importAsPending, replacePending);
        response.putAll(importResult);
        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> importDoiTuongBatch(UUID hopDongId, HopDongDoiTuongImportBatchRequest request) {
        HopDong hopDong = findById(hopDongId);
        assertStationDoiTuongConfigured(hopDong);
        Map<String, Object> importResult = hopDongDoiTuongExcelImportService.importRowBatch(hopDongId, request);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("hopDongId", hopDong.getId());
        response.put("maHopDong", hopDong.getMaHopDong());
        response.put("excelMappingId", request.getExcelMappingId());
        response.putAll(importResult);
        return response;
    }

    /** Response gọn cho commit import — không trả preview/mappings để tránh payload lớn và timeout client. */
    private Map<String, Object> buildImportCommitBaseResponse(
            HopDong hopDong, MultipartFile file, UUID excelMappingId, String tenSheet) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("hopDongId", hopDong.getId());
        response.put("maHopDong", hopDong.getMaHopDong());
        response.put("fileName", file != null ? file.getOriginalFilename() : null);
        response.put("excelMappingId", excelMappingId);
        response.put("tenSheet", tenSheet);
        return response;
    }

    /**
     * Kiểu hợp đồng thuộc đúng 1 loại — nếu hợp đồng gán kiểu nhưng loại lệch (hoặc trống),
     * tự đồng bộ loại theo kiểu. Lệch loại/kiểu làm mọi tra cứu theo cặp (loại + kiểu) trượt:
     * luồng trạng thái, cấu hình đối tượng... (trạm hiển thị "Chưa cấu hình luồng TT").
     */
    private void applyThucHienFields(HopDong entity, LocalDate ngayThucHien, Integer soNgayThucHien) {
        entity.setNgayThucHien(ngayThucHien);
        entity.setSoNgayThucHien(soNgayThucHien);
        if (ngayThucHien != null && soNgayThucHien != null && soNgayThucHien >= 0) {
            // Ngày TH là ngày 1; N ngày TH → hạn = ngày TH + (N − 1).
            long offset = soNgayThucHien > 0 ? soNgayThucHien.longValue() - 1L : 0L;
            entity.setHanHopDong(ngayThucHien.plusDays(offset));
        } else {
            entity.setHanHopDong(null);
        }
    }

    private void syncLoaiTheoKieu(HopDong entity) {
        if (entity.getKieuHopDongId() == null) {
            return;
        }
        KieuHopDong kieu = kieuHopDongRepository.findById(entity.getKieuHopDongId())
                .filter(item -> item.getNgayXoa() == null)
                .orElse(null);
        if (kieu != null && kieu.getLoaiHopDongId() != null
                && !kieu.getLoaiHopDongId().equals(entity.getLoaiHopDongId())) {
            entity.setLoaiHopDongId(kieu.getLoaiHopDongId());
        }
    }

    private boolean isHangMucImportMapping(UUID excelMappingId) {
        return excelMappingImportService.isHangMucThiCongMapping(excelMappingId);
    }


    private List<HopDongResponse> toListResponses(List<HopDong> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }

        List<UUID> hopDongIds = entities.stream().map(HopDong::getId).toList();
        Map<UUID, List<HopDongThuocTinhResponse>> thuocTinhByHopDong = loadThuocTinhBatch(hopDongIds);

        return entities.stream()
                .map(entity -> toListResponse(
                        entity,
                        thuocTinhByHopDong.getOrDefault(entity.getId(), List.of())))
                .toList();
    }

    private Map<UUID, List<HopDongThuocTinhResponse>> loadThuocTinhBatch(List<UUID> hopDongIds) {
        if (hopDongIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, List<HopDongThuocTinhResponse>> grouped = new LinkedHashMap<>();
        for (Object[] row : hopDongThuocTinhService.findActiveWithTenByHopDongIds(hopDongIds)) {
            HopDongThuocTinh value = (HopDongThuocTinh) row[0];
            String tenThuocTinh = row[1] != null ? row[1].toString() : null;
            HopDongThuocTinhResponse item = hopDongMapper.toHopDongThuocTinhResponse(value);
            item.setTenThuocTinh(tenThuocTinh);
            grouped.computeIfAbsent(value.getHopDongId(), ignored -> new ArrayList<>()).add(item);
        }
        grouped.values().forEach(values -> values.sort(
                Comparator.comparing(HopDongThuocTinhResponse::getThuTu, Comparator.nullsLast(Comparator.naturalOrder()))));
        return grouped;
    }

    private HopDongResponse toListResponse(HopDong entity, List<HopDongThuocTinhResponse> values) {
        HopDongResponse response = hopDongMapper.toHopDongResponse(entity);
        response.setThuocTinhGiaTri(values);

        String ma = firstNonBlank(entity.getMa(), entity.getMaHopDong());
        String ten = firstNonBlank(entity.getTen(), ma);
        response.setMaHopDong(ma);
        response.setMa(ma);
        response.setTen(ten);
        response.setGiaTriHd(entity.getGiaTriHd());
        return response;
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return null;
    }

    private void validateThuocTinhGiaTri(UUID kieuHopDongId, List<HopDongThuocTinhGiaTriItem> items) {
        if (kieuHopDongId == null) {
            throw new AppException(HopDongErrorCode.HOP_DONG_THUOC_TINH_INVALID, "Thiếu kiểu hợp đồng");
        }
        Map<UUID, String> valueMap = toValueMap(items);
        for (LinkedDefinition linked : loadDefinitions(kieuHopDongId)) {
            ThuocTinhHopDongResponse definition = linked.definition();
            String value = valueMap.get(definition.getId());
            if (Boolean.TRUE.equals(definition.getBatBuoc()) && (value == null || value.isBlank())) {
                throw new AppException(
                        HopDongErrorCode.HOP_DONG_THUOC_TINH_INVALID,
                        "Thiếu thuộc tính bắt buộc: " + definition.getTen());
            }
        }
    }

    private void saveThuocTinhGiaTri(UUID hopDongId, UUID kieuHopDongId, List<HopDongThuocTinhGiaTriItem> items) {
        Map<UUID, String> valueMap = toValueMap(items);
        for (LinkedDefinition linked : loadDefinitions(kieuHopDongId)) {
            ThuocTinhHopDongResponse definition = linked.definition();
            String value = valueMap.get(definition.getId());
            if (value == null || value.isBlank()) {
                continue;
            }
            HopDongThuocTinh entity = new HopDongThuocTinh();
            entity.setHopDongId(hopDongId);
            entity.setThuocTinhHopDongId(definition.getId());
            entity.setGiaTri(lienKetBangGiaTriNormalizer.normalizeHopDongGiaTri(definition, value.trim()));
            entity.setThuTu(linked.thuTu());
            entity.setHoatDong(true);
            hopDongThuocTinhService.saveEntity(entity);
        }
    }

    private void replaceThuocTinhGiaTri(UUID hopDongId, UUID kieuHopDongId, List<HopDongThuocTinhGiaTriItem> items) {
        hopDongThuocTinhService.findActiveEntitiesByHopDongId(hopDongId).forEach(existing -> {
            existing.setNgayXoa(Instant.now());
            existing.setHoatDong(false);
            hopDongThuocTinhService.saveEntity(existing);
        });
        saveThuocTinhGiaTri(hopDongId, kieuHopDongId, items);
    }

    private Map<UUID, String> toValueMap(List<HopDongThuocTinhGiaTriItem> items) {
        if (items == null) {
            return Map.of();
        }
        return items.stream()
                .filter(item -> item.getThuocTinhHopDongId() != null)
                .collect(Collectors.toMap(
                        HopDongThuocTinhGiaTriItem::getThuocTinhHopDongId,
                        item -> EntityFilter.nullToEmpty(item.getGiaTri()).trim(),
                        (left, right) -> right));
    }

    private List<LinkedDefinition> loadDefinitions(UUID kieuHopDongId) {
        if (kieuHopDongId == null) {
            return List.of();
        }
        List<ThuocTinhKieuHopDongResponse> links =
                thuocTinhKieuHopDongService.list(kieuHopDongId, true, false);
        if (links.isEmpty()) {
            return List.of();
        }
        List<UUID> attrIds = links.stream()
                .map(ThuocTinhKieuHopDongResponse::getThuocTinhHopDongId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<UUID, ThuocTinhHopDongResponse> attrMap = thuocTinhHopDongService.mapActiveByIds(attrIds);
        return links.stream()
                .map(link -> {
                    ThuocTinhHopDongResponse attr = attrMap.get(link.getThuocTinhHopDongId());
                    if (attr == null) {
                        return null;
                    }
                    return new LinkedDefinition(attr, link.getThuTu());
                })
                .filter(item -> item != null)
                .toList();
    }

    private HopDongResponse toEnrichedResponse(HopDong entity) {
        HopDongResponse response = hopDongMapper.toHopDongResponse(entity);
        List<LinkedDefinition> linkedDefinitions = entity.getKieuHopDongId() == null
                ? List.of()
                : loadDefinitions(entity.getKieuHopDongId());
        Map<UUID, ThuocTinhHopDongResponse> definitionMap = linkedDefinitions.stream()
                .collect(Collectors.toMap(item -> item.definition().getId(), LinkedDefinition::definition));

        List<HopDongThuocTinhResponse> values = hopDongThuocTinhService.findActiveEntitiesByHopDongId(entity.getId()).stream()
                .sorted(Comparator.comparing(HopDongThuocTinh::getThuTu, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(value -> {
                    HopDongThuocTinhResponse item = hopDongMapper.toHopDongThuocTinhResponse(value);
                    ThuocTinhHopDongResponse definition = definitionMap.get(value.getThuocTinhHopDongId());
                    if (definition != null) {
                        item.setTenThuocTinh(definition.getTen());
                    }
                    return item;
                })
                .toList();
        response.setThuocTinhGiaTri(values);
        applyDisplayFields(response, values, definitionMap);

        String resolvedMaHopDong = resolveMaHopDong(entity.getMaHopDong(), values, definitionMap);
        response.setMaHopDong(resolvedMaHopDong);
        if (response.getMa() == null) {
            response.setMa(resolvedMaHopDong);
        }
        if (entity.getLoaiHopDongId() != null) {
            List<DoiTuongHopDongLienKetResponse> links = doiTuongHopDongLienKetService.list(
                    entity.getLoaiHopDongId(),
                    entity.getKieuHopDongId(),
                    true,
                    false);
            response.setDoiTuongLienKet(links);
            if (entity.getKieuHopDongId() != null && !DoiTuongHopDongLienKetSupport.hasStationDoiTuong(links)) {
                response.setThieuCauHinhDoiTuongQuanLy(true);
                response.setCauHinhDoiTuongQuanLyMessage(DoiTuongHopDongLienKetSupport.THIEU_CAU_HINH_MESSAGE);
            }
        }
        String trangThaiLuuTru = hopDongArchiveGuard.resolveTrangThaiLuuTru(entity.getId());
        response.setTrangThaiLuuTru(trangThaiLuuTru);
        response.setArchived("archived".equalsIgnoreCase(trangThaiLuuTru));
        return response;
    }

    private String resolveMaHopDong(
            String columnMa,
            List<HopDongThuocTinhResponse> values,
            Map<UUID, ThuocTinhHopDongResponse> definitionMap) {
        if (columnMa != null && !columnMa.isBlank()) {
            return columnMa.trim();
        }
        for (HopDongThuocTinhResponse value : values) {
            ThuocTinhHopDongResponse definition = definitionMap.get(value.getThuocTinhHopDongId());
            if (definition == null || value.getGiaTri() == null || value.getGiaTri().isBlank()) {
                continue;
            }
            if (Boolean.TRUE.equals(definition.getLaKhoaChinh())) {
                return value.getGiaTri().trim();
            }
            String normalizedTen = definition.getTen() == null ? "" : definition.getTen().toLowerCase(Locale.ROOT);
            if (normalizedTen.contains("mã hợp đồng") || normalizedTen.contains("mã hd")) {
                return value.getGiaTri().trim();
            }
        }
        return null;
    }

    private List<HopDongThuocTinhGiaTriItem> syncMaHopDongIntoThuocTinh(
            String maHopDong,
            UUID kieuHopDongId,
            List<HopDongThuocTinhGiaTriItem> items) {
        if (maHopDong == null || maHopDong.isBlank() || kieuHopDongId == null) {
            return items == null ? List.of() : items;
        }
        Optional<ThuocTinhHopDongResponse> maDefinition = findMaHopDongDefinition(kieuHopDongId);
        if (maDefinition.isEmpty()) {
            return items == null ? List.of() : items;
        }

        List<HopDongThuocTinhGiaTriItem> synced = items == null ? new ArrayList<>() : new ArrayList<>(items);
        UUID maAttrId = maDefinition.get().getId();
        boolean updated = false;
        for (HopDongThuocTinhGiaTriItem item : synced) {
            if (maAttrId.equals(item.getThuocTinhHopDongId())) {
                item.setGiaTri(maHopDong.trim());
                updated = true;
                break;
            }
        }
        if (!updated) {
            HopDongThuocTinhGiaTriItem item = new HopDongThuocTinhGiaTriItem();
            item.setThuocTinhHopDongId(maAttrId);
            item.setGiaTri(maHopDong.trim());
            synced.add(item);
        }
        return synced;
    }

    private Optional<ThuocTinhHopDongResponse> findMaHopDongDefinition(UUID kieuHopDongId) {
        return loadDefinitions(kieuHopDongId).stream()
                .map(LinkedDefinition::definition)
                .filter(this::isPrimitiveHopDongAttribute)
                .filter(definition -> Boolean.TRUE.equals(definition.getLaKhoaChinh()))
                .findFirst()
                .or(() -> loadDefinitions(kieuHopDongId).stream()
                        .map(LinkedDefinition::definition)
                        .filter(this::isPrimitiveHopDongAttribute)
                        .filter(definition -> {
                            String ten = definition.getTen() == null ? "" : definition.getTen().toLowerCase(Locale.ROOT);
                            return ten.contains("mã hợp đồng") || ten.contains("mã hd");
                        })
                        .findFirst());
    }

    private boolean isPrimitiveHopDongAttribute(ThuocTinhHopDongResponse definition) {
        if (definition.getLienKetBang() != null
                && LienKetBangSupport.isKnownLinkTable(definition.getLienKetBang())) {
            return false;
        }
        return !LienKetBangSupport.isKnownLinkTable(definition.getKieuDuLieuId());
    }

    private void applyDisplayFields(
            HopDongResponse response,
            List<HopDongThuocTinhResponse> values,
            Map<UUID, ThuocTinhHopDongResponse> definitionMap) {
        for (HopDongThuocTinhResponse value : values) {
            ThuocTinhHopDongResponse definition = definitionMap.get(value.getThuocTinhHopDongId());
            if (definition == null || value.getGiaTri() == null || value.getGiaTri().isBlank()) {
                continue;
            }
            if (Boolean.TRUE.equals(definition.getLaKhoaChinh()) && isPrimitiveHopDongAttribute(definition)) {
                response.setMa(value.getGiaTri());
            }
            String tenNormalized = definition.getTen() == null ? "" : definition.getTen().toLowerCase(Locale.ROOT);
            if (response.getTen() == null && tenNormalized.contains("tên")) {
                response.setTen(value.getGiaTri());
            }
        }
        if (response.getTen() == null && response.getMa() != null) {
            response.setTen(response.getMa());
        }
    }

    private HopDong findById(UUID id) {
        return hopDongRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_NOT_FOUND, "Không tìm thấy bản ghi"));
    }

    private List<HopDong> applyContractorScope(List<HopDong> source) {
        Optional<ContractorScope> scope = contractorScopeService.currentScope();
        if (scope.isEmpty()) {
            return source;
        }
        Set<UUID> allowedIds = scope.get().hopDongIds();
        return source.stream()
                .filter(item -> allowedIds.contains(item.getId()))
                .toList();
    }

    private void assertStationDoiTuongConfigured(HopDong hopDong) {
        if (hopDong.getLoaiHopDongId() == null || hopDong.getKieuHopDongId() == null) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_CAU_HINH_THIEU,
                    "Hợp đồng chưa có loại/kiểu HĐ");
        }
        List<DoiTuongHopDongLienKetResponse> links = doiTuongHopDongLienKetService.list(
                hopDong.getLoaiHopDongId(),
                hopDong.getKieuHopDongId(),
                true,
                false);
        if (!DoiTuongHopDongLienKetSupport.hasStationDoiTuong(links)) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_CAU_HINH_THIEU,
                    DoiTuongHopDongLienKetSupport.THIEU_CAU_HINH_MESSAGE);
        }
    }
}
