 package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.impl;

import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDoiTuongCascadeDeleteEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDoiTuongMetaChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.SnapshotDataMissingEvent;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScope;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScopeService;
import vn.edu.huce.iic.bts_ops_platform.common.security.DataScopeService;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.PaginationDefaults;
import vn.edu.huce.iic.bts_ops_platform.config.AppCacheProperties;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DoiTuongHopDongLienKetResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DoiTuongQuanLyResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LoaiHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.TrangThaiHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.DoiTuongHopDongLienKetService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.DoiTuongQuanLyService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.DoiTuongHopDongLienKetSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LoaiHopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.TrangThaiHopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.HopDongObjectGeo;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongGiaTriTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongNhanBanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongXoaTheoLocRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongGiaTriResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongLoaiThongKeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongXoaTheoLocBatchResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuongGiaTri;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongNhomUuTien;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.HopDongDanhSachDoiTuongGroupSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.HopDongDanhSachTienDoHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.mapper.HopDongMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.cache.HopDongDoiTuongSnapshotCacheKeys;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.cache.HopDongDoiTuongReportCacheNames;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.cache.HopDongDoiTuongSnapshotCacheNames;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.HopDongDoiTuongSnapshot;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongGiaTriService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LoaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LoaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.common.dto.RankedItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.VolumeTinhToanHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.constants.VolumeConstants;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongBreakdownRow;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongGiaTriRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongTonCandidateRow;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.KhuVuc;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.KhuVucRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongNhomUuTienRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongSnapshotService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongTrangThaiService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.entity.NguoiDung;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.repository.NguoiDungRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HopDongDoiTuongServiceImpl implements HopDongDoiTuongService, HopDongDoiTuongSnapshotService {

    private static final int MAX_IDS_SIZE = 2000;
    private static final int DELETE_BATCH_SIZE = 5000;

    private final HopDongDoiTuongRepository hopDongDoiTuongRepository;
    private final HopDongDoiTuongGiaTriRepository hopDongDoiTuongGiaTriRepository;
    private final HopDongDoiTuongGiaTriService hopDongDoiTuongGiaTriService;
    private final HopDongNhomUuTienRepository hopDongNhomUuTienRepository;
    private final HopDongRepository hopDongRepository;
    private final DoiTuongQuanLyService doiTuongQuanLyService;
    private final ThuocTinhService thuocTinhService;
    private final TrangThaiHopDongService trangThaiHopDongService;
    private final HopDongDoiTuongTrangThaiService hopDongDoiTuongTrangThaiService;
    private final HopDongMapper hopDongMapper;
    private final DoiTuongHopDongLienKetService doiTuongHopDongLienKetService;
    private final ContractorScopeService contractorScopeService;
    private final DataScopeService dataScopeService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AppEventContext appEventContext;

    private final LoaiHopDongService loaiHopDongService;
    private final HopDongDanhSachDoiTuongGroupSupport doiTuongGroupSupport;
    private final CacheService cacheService;
    private final AppCacheProperties cacheProperties;
    private final NguoiDungRepository nguoiDungRepository;
    private final LoaiHopDongRepository loaiHopDongRepository;
    private final KhuVucRepository khuVucRepository;
    private final vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.services.HopDongArchiveGuard hopDongArchiveGuard;

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuong> findActiveEntitiesByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return hopDongDoiTuongRepository.findByIdInAndNgayXoaIsNull(new ArrayList<>(ids));
    }

    @Override
    @Transactional
    public void updateNhomUuTienForIds(Collection<UUID> doiTuongIds, UUID hopDongNhomUuTienId) {
        if (doiTuongIds == null || doiTuongIds.isEmpty()) {
            return;
        }
        List<HopDongDoiTuong> entities = hopDongDoiTuongRepository.findByIdInAndNgayXoaIsNull(new ArrayList<>(doiTuongIds));
        for (HopDongDoiTuong entity : entities) {
            entity.setHopDongNhomUuTienId(hopDongNhomUuTienId);
            hopDongDoiTuongRepository.save(entity);
        }
    }

    @Override
    @Transactional
    public void clearNhomUuTien(UUID hopDongNhomUuTienId) {
        hopDongDoiTuongRepository.findByHopDongNhomUuTienIdAndNgayXoaIsNull(hopDongNhomUuTienId).forEach(doiTuong -> {
            doiTuong.setHopDongNhomUuTienId(null);
            hopDongDoiTuongRepository.save(doiTuong);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public long countByNhomUuTien(UUID hopDongNhomUuTienId) {
        return hopDongDoiTuongRepository.countByHopDongNhomUuTienIdAndNgayXoaIsNull(hopDongNhomUuTienId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuong> findActiveEntitiesByHopDongId(UUID hopDongId) {
        return hopDongDoiTuongRepository.findByHopDongIdAndNgayXoaIsNull(hopDongId);
    }

    @Override
    @Transactional
    public HopDongDoiTuong saveEntity(HopDongDoiTuong entity) {
        return hopDongDoiTuongRepository.save(entity);
    }

    @Override
    @Transactional
    public void recalculateConstructionDate(UUID hopDongDoiTuongId) {
        hopDongDoiTuongRepository.recalculateNgayThiCongGanNhat(hopDongDoiTuongId);
    }

    @Override
    @Transactional
    public void recalculateSanLuongHieuLuc(UUID hopDongDoiTuongId) {
        hopDongDoiTuongRepository.recalculateSanLuongHieuLuc(hopDongDoiTuongId);
    }

    @Override
    @Transactional
    public int recalculateSanLuongHieuLucAll() {
        return hopDongDoiTuongRepository.recalculateSanLuongHieuLucAll();
    }

    @Override
    @Transactional
    public void updateCoVuongMacMo(UUID hopDongDoiTuongId, boolean coVuongMacMo) {
        hopDongDoiTuongRepository.updateCoVuongMacMo(hopDongDoiTuongId, coVuongMacMo);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFilteredByLoaiHopDong(Boolean activeOnly, UUID loaiHopDongId) {
        return countFilteredByLoaiHopDong(activeOnly, loaiHopDongId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFilteredByLoaiHopDong(Boolean activeOnly, UUID loaiHopDongId, UUID kieuHopDongId) {
        return hopDongDoiTuongRepository.countFilteredByLoaiHopDong(
                activeOnly,
                loaiHopDongId,
                kieuHopDongId,
                DoiTuongHopDongLienKetSupport.HANG_MUC_THI_CONG_DOI_TUONG_ID);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> countGroupByTrangThaiMa(Boolean activeOnly, UUID loaiHopDongId) {
        return hopDongDoiTuongRepository.countGroupByTrangThaiMa(activeOnly, loaiHopDongId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> countGroupByHopDongId(Boolean activeOnly, UUID loaiHopDongId, UUID kieuHopDongId) {
        return hopDongDoiTuongRepository.countGroupByHopDongId(
                activeOnly,
                loaiHopDongId,
                kieuHopDongId,
                DoiTuongHopDongLienKetSupport.HANG_MUC_THI_CONG_DOI_TUONG_ID);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> countVuongMacMoGroupByHopDongId(Boolean activeOnly, UUID loaiHopDongId, UUID kieuHopDongId) {
        return hopDongDoiTuongRepository.countVuongMacMoGroupByHopDongId(activeOnly, loaiHopDongId, kieuHopDongId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> countGroupByHopDongIdAndTrangThaiMa(Boolean activeOnly, UUID loaiHopDongId, UUID kieuHopDongId) {
        return hopDongDoiTuongRepository.countGroupByHopDongIdAndTrangThaiMa(
                activeOnly,
                loaiHopDongId,
                kieuHopDongId,
                DoiTuongHopDongLienKetSupport.HANG_MUC_THI_CONG_DOI_TUONG_ID);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HopDongDoiTuongResponse> list(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID hopDongId,
            UUID doiTuongQuanLyId,
            UUID trangThaiHopDongId,
            Boolean withoutNhomUuTien,
            Boolean withNhomUuTien,
            Integer page,
            Integer size) {
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE);
        int pageNumber = EntityFilter.normalizePage(page);
        long offset = (long) pageNumber * pageSize;
        String keyword = EntityFilter.normalizeSearch(search);

        long total = keyword.isBlank()
                ? hopDongDoiTuongRepository.countSearchNative(
                        includeDeleted,
                        activeOnly,
                        hopDongId,
                        doiTuongQuanLyId,
                        trangThaiHopDongId,
                        withoutNhomUuTien,
                        withNhomUuTien)
                : hopDongDoiTuongRepository.countSearchWithKeyword(
                        includeDeleted,
                        activeOnly,
                        hopDongId,
                        doiTuongQuanLyId,
                        trangThaiHopDongId,
                        keyword,
                        withoutNhomUuTien,
                        withNhomUuTien);

        List<HopDongDoiTuong> fetched = keyword.isBlank()
                ? hopDongDoiTuongRepository.searchPageNative(
                        includeDeleted,
                        activeOnly,
                        hopDongId,
                        doiTuongQuanLyId,
                        trangThaiHopDongId,
                        withoutNhomUuTien,
                        withNhomUuTien,
                        pageSize,
                        offset)
                : hopDongDoiTuongRepository.searchPageWithKeyword(
                        includeDeleted,
                        activeOnly,
                        hopDongId,
                        doiTuongQuanLyId,
                        trangThaiHopDongId,
                        keyword,
                        withoutNhomUuTien,
                        withNhomUuTien,
                        pageSize,
                        offset);

        List<HopDongDoiTuongResponse> items = enrichList(fetched).stream()
                .filter(this::matchesContractorScope)
                .toList();

        return PageResponse.ofItems(items, pageNumber, pageSize, total);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UUID> listIds(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID hopDongId,
            UUID doiTuongQuanLyId,
            UUID trangThaiHopDongId,
            Boolean withoutNhomUuTien,
            Boolean withNhomUuTien,
            Integer page,
            Integer size) {
        int pageSize = normalizeIdsSize(size);
        int pageNumber = EntityFilter.normalizePage(page);
        long offset = (long) pageNumber * pageSize;
        String keyword = EntityFilter.normalizeSearch(search);

        long total = keyword.isBlank()
                ? hopDongDoiTuongRepository.countSearchNative(
                        includeDeleted,
                        activeOnly,
                        hopDongId,
                        doiTuongQuanLyId,
                        trangThaiHopDongId,
                        withoutNhomUuTien,
                        withNhomUuTien)
                : hopDongDoiTuongRepository.countSearchWithKeyword(
                        includeDeleted,
                        activeOnly,
                        hopDongId,
                        doiTuongQuanLyId,
                        trangThaiHopDongId,
                        keyword,
                        withoutNhomUuTien,
                        withNhomUuTien);

        List<UUID> fetchedIds = keyword.isBlank()
                ? hopDongDoiTuongRepository.searchIdsPageNative(
                        includeDeleted,
                        activeOnly,
                        hopDongId,
                        doiTuongQuanLyId,
                        trangThaiHopDongId,
                        null,
                        withoutNhomUuTien,
                        withNhomUuTien,
                        pageSize,
                        offset)
                : hopDongDoiTuongRepository.searchIdsPageWithKeyword(
                        includeDeleted,
                        activeOnly,
                        hopDongId,
                        doiTuongQuanLyId,
                        trangThaiHopDongId,
                        null,
                        keyword,
                        withoutNhomUuTien,
                        withNhomUuTien,
                        pageSize,
                        offset);

        List<UUID> ids = fetchedIds.stream()
                .filter(this::matchesContractorScopeId)
                .toList();

        return PageResponse.ofItems(ids, pageNumber, pageSize, total);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongResponse> listAll(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID hopDongId,
            UUID doiTuongQuanLyId,
            UUID trangThaiHopDongId,
            Boolean withoutNhomUuTien,
            Boolean withNhomUuTien,
            Integer page,
            Integer size) {
        // page/size null → lấy hết trong 1 lần (an toàn vì caller nội bộ luôn scope theo
        // hopDongId). Có truyền vào → ưu tiên dùng để phân trang thật thay vì lấy hết.
        int limit = size != null
                ? EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE)
                : PaginationDefaults.LIST_ALL_SIZE;
        int offset = page != null ? EntityFilter.normalizePage(page) * limit : 0;

        String keyword = EntityFilter.normalizeSearch(search);
        List<HopDongDoiTuong> fetched = keyword.isBlank()
                ? hopDongDoiTuongRepository.searchPageNative(
                        includeDeleted, activeOnly, hopDongId, doiTuongQuanLyId, trangThaiHopDongId,
                        withoutNhomUuTien, withNhomUuTien, limit, offset)
                : hopDongDoiTuongRepository.searchPageWithKeyword(
                        includeDeleted, activeOnly, hopDongId, doiTuongQuanLyId, trangThaiHopDongId,
                        keyword, withoutNhomUuTien, withNhomUuTien, limit, offset);

        return enrichList(fetched).stream()
                .filter(this::matchesContractorScope)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongResponse> listActiveByHopDongIds(Collection<UUID> hopDongIds) {
        if (hopDongIds == null || hopDongIds.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = hopDongIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        List<HopDongDoiTuong> entities = hopDongDoiTuongRepository.findByHopDongIdInAndNgayXoaIsNull(ids).stream()
                .filter(entity -> Boolean.TRUE.equals(entity.getHoatDong()))
                .toList();
        return enrichList(entities).stream()
                .filter(this::matchesContractorScope)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongResponse> listActiveLiteByHopDongIds(Collection<UUID> hopDongIds) {
        if (hopDongIds == null || hopDongIds.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = hopDongIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        List<HopDongDoiTuong> entities = hopDongDoiTuongRepository.findByHopDongIdInAndNgayXoaIsNull(ids).stream()
                .filter(entity -> Boolean.TRUE.equals(entity.getHoatDong()))
                .toList();
        return enrichListLite(entities).stream()
                .filter(this::matchesContractorScope)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongResponse> listByNhomUuTien(UUID hopDongNhomUuTienId) {
        List<HopDongDoiTuong> entities =
                hopDongDoiTuongRepository.findByHopDongNhomUuTienIdAndNgayXoaIsNull(hopDongNhomUuTienId);
        return enrichList(entities).stream()
                .filter(this::matchesContractorScope)
                .toList();
    }


    private int normalizeIdsSize(Integer size) {
        if (size == null || size <= 0) {
            return MAX_IDS_SIZE;
        }
        return Math.min(size, MAX_IDS_SIZE);
    }


    @Override
    @Transactional(readOnly = true)
    public HopDongDoiTuongResponse getById(UUID id) {
        contractorScopeService.assertHopDongDoiTuongAccessible(id);
        HopDongDoiTuong entity = findById(id);
        HopDongDoiTuongResponse response = enrichList(List.of(entity)).get(0);
        dataScopeService.assertDoiTuongAccessible(response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongResponse> getByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<HopDongDoiTuong> entities = hopDongDoiTuongRepository.findByIdInAndNgayXoaIsNull(ids.stream().distinct().toList());
        return enrichList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongResponse> getByIdsLite(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<HopDongDoiTuong> entities = hopDongDoiTuongRepository.findByIdInAndNgayXoaIsNull(ids.stream().distinct().toList());
        return enrichListLite(entities);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<HopDongDoiTuongResponse> thieuCapNhatTienDo(Integer soNgay, int limit) {
        int nguong = soNgay == null ? 7 : Math.max(soNgay, 1);
        String cacheKey = "thieu-cap-nhat|" + nguong + "|" + limit;
        Optional<List> cached = cacheService.get(HopDongDoiTuongReportCacheNames.REPORT, cacheKey, List.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<UUID> ids = hopDongDoiTuongRepository.findIdsThieuCapNhat(nguong, Math.max(limit, 1));
        List<HopDongDoiTuongResponse> result = getByIdsLite(ids);
        cacheService.put(HopDongDoiTuongReportCacheNames.REPORT, cacheKey, result, cacheProperties.hopDongDoiTuongReportTtl());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> breakdownTheoLoaiKhuVuc(String tenLoaiHopDong, String tenKhuVuc) {
        String cacheKey = "breakdown|" + tenLoaiHopDong + "|" + tenKhuVuc;
        // KHÔNG ép kiểu thẳng Map thô về Map<String, Long> — cacheService.get(..., Map.class) mất
        // generic info (type erasure), Jackson deserialize số JSON thành Integer chứ không phải
        // Long, gây ClassCastException khi serialize lại (xem VuongMacServiceImpl.buildCountsByKieuCached
        // cho bug tương tự). Phải convert tường minh qua Number.longValue().
        Optional<Map> cachedBreakdown = cacheService.get(HopDongDoiTuongReportCacheNames.REPORT, cacheKey, Map.class);
        if (cachedBreakdown.isPresent()) {
            Map<String, Long> result = new HashMap<>();
            for (Object entry : cachedBreakdown.get().entrySet()) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) entry;
                result.put(String.valueOf(e.getKey()), e.getValue() == null ? 0L : ((Number) e.getValue()).longValue());
            }
            return result;
        }
        Map<String, Long> computed = computeBreakdownTheoLoaiKhuVuc(tenLoaiHopDong, tenKhuVuc);
        cacheService.put(HopDongDoiTuongReportCacheNames.REPORT, cacheKey, computed, cacheProperties.hopDongDoiTuongReportTtl());
        return computed;
    }

    private Map<String, Long> computeBreakdownTheoLoaiKhuVuc(String tenLoaiHopDong, String tenKhuVuc) {
        UUID loaiHopDongId = null;
        if (tenLoaiHopDong != null && !tenLoaiHopDong.isBlank()) {
            List<LoaiHopDong> matched = loaiHopDongRepository.search(false, true, tenLoaiHopDong.trim().toLowerCase(Locale.ROOT));
            loaiHopDongId = matched.isEmpty() ? null : matched.get(0).getId();
        }
        UUID khuVucId = null;
        if (tenKhuVuc != null && !tenKhuVuc.isBlank()) {
            List<KhuVuc> matched = khuVucRepository.searchByMaOrTen(tenKhuVuc.trim());
            khuVucId = matched.isEmpty() ? null : matched.get(0).getId();
        }
        HopDongDoiTuongBreakdownRow row = hopDongDoiTuongRepository.breakdownTheoLoaiKhuVuc(loaiHopDongId, khuVucId);
        Map<String, Long> result = new HashMap<>();
        result.put("tong", row.getTong() == null ? 0L : row.getTong());
        result.put("hoanThanh", row.getHoanThanh() == null ? 0L : row.getHoanThanh());
        result.put("dangThiCong", row.getDangThiCong() == null ? 0L : row.getDangThiCong());
        result.put("vuongMac", row.getVuongMac() == null ? 0L : row.getVuongMac());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongTonCandidateRow> sanLuongBatThuong(int limit) {
        // KHÔNG cache qua Redis (CacheService) — HopDongDoiTuongTonCandidateRow là JPA projection
        // INTERFACE, Jackson không thể deserialize JSON ngược lại thành interface này (không có
        // constructor/implementation cụ thể) nên đọc cache-hit sẽ vỡ. Method vốn đã giới hạn
        // limit (mặc định 20) nên chi phí không cache cũng không lớn.
        return hopDongDoiTuongRepository.findTramSanLuongBatThuong(
                VolumeConstants.GCCC_LOAI_HOP_DONG_ID,
                VolumeConstants.XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID,
                VolumeTinhToanHelper.DEFAULT_HESO_GCCC,
                Math.max(limit, 1));
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<RankedItemResponse> tocDoHoanThanh(String nhom, int soNgay) {
        String cacheKey = "toc-do-hoan-thanh|" + nhom + "|" + soNgay;
        Optional<List> cached = cacheService.get(HopDongDoiTuongReportCacheNames.REPORT, cacheKey, List.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<RankedItemResponse> result = computeTocDoHoanThanh(nhom, soNgay);
        cacheService.put(HopDongDoiTuongReportCacheNames.REPORT, cacheKey, result, cacheProperties.hopDongDoiTuongReportTtl());
        return result;
    }

    private List<RankedItemResponse> computeTocDoHoanThanh(String nhom, int soNgay) {
        int nguong = Math.max(soNgay, 1);
        LocalDate denNgay = LocalDate.now();
        LocalDate tuNgay = denNgay.minusDays(nguong - 1);
        boolean theoNhaThau = "nhathau".equalsIgnoreCase(nhom) || "canbo".equalsIgnoreCase(nhom);

        List<Object[]> rows = theoNhaThau
                ? hopDongDoiTuongRepository.countHoanThanhGroupByNhaThau(tuNgay, denNgay)
                : hopDongDoiTuongRepository.countHoanThanhGroupByKhuVuc(tuNgay, denNgay);

        Map<UUID, Long> countByGroup = new HashMap<>();
        long total = 0;
        for (Object[] row : rows) {
            if (row[0] == null) {
                continue;
            }
            long count = ((Number) row[1]).longValue();
            countByGroup.put((UUID) row[0], count);
            total += count;
        }
        if (countByGroup.isEmpty()) {
            return List.of();
        }
        double avgPerGroup = (double) total / countByGroup.size();

        Map<UUID, String> nameById = theoNhaThau
                ? nguoiDungRepository.findByIdInAndNgayXoaIsNull(countByGroup.keySet()).stream()
                        .collect(Collectors.toMap(NguoiDung::getId, NguoiDung::getHoTen, (a, b) -> a))
                : khuVucRepository.findByIdInAndNgayXoaIsNull(countByGroup.keySet()).stream()
                        .collect(Collectors.toMap(KhuVuc::getId, KhuVuc::getTen, (a, b) -> a));

        return countByGroup.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .map(entry -> {
                    double velocity = entry.getValue() / (double) nguong;
                    double diffPercent = avgPerGroup > 0
                            ? ((entry.getValue() - avgPerGroup) / avgPerGroup) * 100
                            : 0;
                    Map<String, Object> extra = new HashMap<>();
                    extra.put("soVoiTrungBinhHeThongPercent", Math.round(diffPercent * 10) / 10.0);
                    return RankedItemResponse.builder()
                            .label(nameById.getOrDefault(entry.getKey(), entry.getKey().toString()))
                            .value(Math.round(velocity * 100) / 100.0)
                            .extra(extra)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public HopDongDoiTuongResponse capNhatQuyetToanThuc(UUID hopDongId, UUID id, java.math.BigDecimal quyetToanThuc) {
        HopDongDoiTuong entity = findById(id);
        assertThuocHopDong(entity, hopDongId);
        if (quyetToanThuc != null && quyetToanThuc.signum() < 0) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Quyết toán thực không được âm");
        }
        entity.setQuyetToanThuc(quyetToanThuc);
        appEventContext.audit("CAP_NHAT_QUYET_TOAN", "Cập nhật quyết toán thực",
                quyetToanThuc == null ? "Xóa giá trị quyết toán" : "Giá trị: " + quyetToanThuc.toPlainString(),
                Map.of("hopDongId", hopDongId, "doiTuongIds", List.of(id)));
        return enrichList(List.of(hopDongDoiTuongRepository.save(entity))).get(0);
    }

    @Override
    @Transactional
    public HopDongDoiTuongResponse dieuChinhBoSungSanLuong(UUID hopDongId, UUID id, java.math.BigDecimal delta) {
        HopDongDoiTuong entity = findById(id);
        assertThuocHopDong(entity, hopDongId);
        java.math.BigDecimal current = entity.getBoSungSanLuong() != null
                ? entity.getBoSungSanLuong()
                : java.math.BigDecimal.ZERO;
        java.math.BigDecimal next = current.add(delta != null ? delta : java.math.BigDecimal.ZERO);
        if (next.signum() < 0) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Bổ sung sản lượng không được âm");
        }
        entity.setBoSungSanLuong(next);
        HopDongDoiTuong saved = hopDongDoiTuongRepository.save(entity);
        hopDongDoiTuongRepository.recalculateSanLuongHieuLuc(saved.getId());
        // recalculate... là @Modifying(clearAutomatically=true) — persistence context vừa bị evict,
        // "saved" trong Java KHÔNG mang giá trị sanLuongHieuLuc mới, phải fetch lại từ DB.
        HopDongDoiTuong refreshed = findById(saved.getId());
        return enrichList(List.of(refreshed)).get(0);
    }

    /** Audit cập nhật đối tượng — mô tả rõ chuyển trạng thái luồng (old → new) nếu có. */
    private void auditDoiTuongCapNhat(HopDongDoiTuong entity, UUID trangThaiTruoc) {
        String detail = null;
        UUID trangThaiSau = entity.getTrangThaiHopDongId();
        boolean doiTrangThai = trangThaiTruoc == null ? trangThaiSau != null : !trangThaiTruoc.equals(trangThaiSau);
        if (doiTrangThai) {
            detail = "Trạng thái: " + tenTrangThaiAudit(trangThaiTruoc) + " → " + tenTrangThaiAudit(trangThaiSau);
        }
        appEventContext.audit(
                doiTrangThai ? "DOI_TRANG_THAI_DOI_TUONG" : "CAP_NHAT_DOI_TUONG",
                doiTrangThai ? "Chuyển trạng thái đối tượng" : "Cập nhật thông tin đối tượng",
                detail,
                Map.of("hopDongId", entity.getHopDongId(), "doiTuongIds", List.of(entity.getId())));
    }

    private String tenTrangThaiAudit(UUID trangThaiId) {
        if (trangThaiId == null) {
            return "(chưa gán)";
        }
        try {
            return trangThaiHopDongService.getById(trangThaiId).getTen();
        } catch (Exception ex) {
            return trangThaiId.toString().substring(0, 8);
        }
    }

    private void assertThuocHopDong(HopDongDoiTuong entity, UUID hopDongId) {
        if (hopDongId != null && !hopDongId.equals(entity.getHopDongId())) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_NOT_FOUND,
                    "Đối tượng không thuộc hợp đồng này");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long demActive(UUID loaiHopDongId) {
        return demActive(loaiHopDongId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public long demActive(UUID loaiHopDongId, UUID khuVucId) {
        return hopDongDoiTuongRepository.countActiveScoped(loaiHopDongId, khuVucId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Long> demActiveNhomTheoLoaiHopDong() {
        Map<UUID, Long> result = new HashMap<>();
        for (Object[] row : hopDongDoiTuongRepository.countGroupByLoaiHopDongId(Boolean.TRUE)) {
            if (row[0] == null) {
                continue;
            }
            result.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Long> demActiveNhomTheoHopDong() {
        Map<UUID, Long> result = new HashMap<>();
        for (Object[] row : hopDongDoiTuongRepository.countGroupByHopDongId(
                Boolean.TRUE, null, null, DoiTuongHopDongLienKetSupport.HANG_MUC_THI_CONG_DOI_TUONG_ID)) {
            if (row[0] == null) {
                continue;
            }
            result.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongLoaiThongKeResponse> demActiveNhomTheoLoaiVaDoiTuong() {
        List<HopDongDoiTuongLoaiThongKeResponse> result = new ArrayList<>();
        for (Object[] row : hopDongDoiTuongRepository.countGroupByLoaiAndDoiTuongQuanLy(Boolean.TRUE)) {
            if (row[0] == null || row[1] == null) {
                continue;
            }
            result.add(new HopDongDoiTuongLoaiThongKeResponse(
                    (UUID) row[0], (UUID) row[1], ((Number) row[2]).longValue()));
        }
        return result;
    }

    @Override
    @Transactional
    public HopDongDoiTuongResponse create(HopDongDoiTuongTaoRequest request) {
        hopDongArchiveGuard.assertWritable(request.getHopDongId());
        HopDong hopDong = findHopDong(request.getHopDongId());
        assertDoiTuongAllowedForHopDong(hopDong, request.getDoiTuongQuanLyId());
        UUID trangThaiId = resolveTrangThaiForCreate(hopDong, request.getDoiTuongQuanLyId(), request.getTrangThaiHopDongId());

        HopDongDoiTuong entity = hopDongMapper.fromHopDongDoiTuongTaoRequest(request);
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());
        entity.setTrangThaiHopDongId(trangThaiId);
        maybeSetNgayHtTc(entity, trangThaiId);

        HopDongDoiTuongResponse created = enrichList(List.of(hopDongDoiTuongRepository.save(entity))).get(0);
        appEventContext.audit("THEM_DOI_TUONG", "Thêm đối tượng vào hợp đồng", null,
                Map.of("hopDongId", entity.getHopDongId(), "doiTuongIds", List.of(entity.getId())));
        return created;
    }

    @Override
    @Transactional
    public List<HopDongDoiTuongResponse> nhanBan(List<HopDongDoiTuongNhanBanRequest.Muc> muc) {
        if (muc == null || muc.isEmpty()) {
            return List.of();
        }

        List<UUID> sourceIds = muc.stream().map(HopDongDoiTuongNhanBanRequest.Muc::getNguonId).toList();
        List<HopDongDoiTuong> sources = findActiveEntitiesByIds(sourceIds);
        Map<UUID, HopDongDoiTuong> sourceById = sources.stream()
                .collect(Collectors.toMap(HopDongDoiTuong::getId, Function.identity()));
        if (sourceById.size() != muc.size()) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_NOT_FOUND, "Không tìm thấy đối tượng nguồn");
        }

        Map<UUID, List<HopDongDoiTuongGiaTri>> giaTriBySource = hopDongDoiTuongGiaTriService
                .findActiveEntitiesByHopDongDoiTuongIds(sourceIds)
                .stream()
                .collect(Collectors.groupingBy(HopDongDoiTuongGiaTri::getHopDongDoiTuongId));

        Set<UUID> doiTuongQuanLyIds = sources.stream()
                .map(HopDongDoiTuong::getDoiTuongQuanLyId)
                .collect(Collectors.toSet());
        Map<UUID, Map<UUID, ThuocTinhResponse>> thuocTinhByDoiTuong = thuocTinhService
                .listGroupedByDoiTuongQuanLyIds(doiTuongQuanLyIds)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .collect(Collectors.toMap(
                                        ThuocTinhResponse::getId,
                                        Function.identity(),
                                        (left, right) -> left)),
                        (left, right) -> left,
                        HashMap::new));

        Set<String> requestMaLower = new HashSet<>();
        for (HopDongDoiTuongNhanBanRequest.Muc item : muc) {
            String maMoi = item.getMaMoi() == null ? "" : item.getMaMoi().trim();
            if (maMoi.isBlank()) {
                throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Mã mới không được để trống");
            }
            String maKey = maMoi.toLowerCase(Locale.ROOT);
            if (!requestMaLower.add(maKey)) {
                throw new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_CODE_EXISTS, "Trùng mã trong danh sách nhân bản");
            }
        }

        List<HopDongDoiTuong> createdEntities = new ArrayList<>(muc.size());
        List<HopDongDoiTuongGiaTriTaoRequest> giaTriCreates = new ArrayList<>();

        for (HopDongDoiTuongNhanBanRequest.Muc item : muc) {
            HopDongDoiTuong source = sourceById.get(item.getNguonId());
            HopDong hopDong = findHopDong(source.getHopDongId());
            assertDoiTuongAllowedForHopDong(hopDong, source.getDoiTuongQuanLyId());

            Map<UUID, ThuocTinhResponse> thuocTinhMap =
                    thuocTinhByDoiTuong.getOrDefault(source.getDoiTuongQuanLyId(), Map.of());
            UUID primaryThuocTinhId = thuocTinhMap.values().stream()
                    .filter(def -> Boolean.TRUE.equals(def.getLaKhoaChinh()))
                    .map(ThuocTinhResponse::getId)
                    .findFirst()
                    .orElse(null);

            String maMoi = item.getMaMoi().trim();
            if (primaryThuocTinhId != null) {
                List<String> existing = hopDongDoiTuongGiaTriRepository.findPrimaryKeyValuesLowerByHopDongAndThuocTinh(
                        source.getHopDongId(), primaryThuocTinhId);
                if (existing.contains(maMoi.toLowerCase(Locale.ROOT))) {
                    throw new AppException(
                            HopDongErrorCode.HOP_DONG_DOI_TUONG_CODE_EXISTS,
                            "Mã \"" + maMoi + "\" đã tồn tại trong hợp đồng");
                }
            }

            UUID trangThaiId = resolveTrangThaiForCreate(hopDong, source.getDoiTuongQuanLyId(), null);
            HopDongDoiTuong copy = new HopDongDoiTuong();
            copy.setHopDongId(source.getHopDongId());
            copy.setDoiTuongQuanLyId(source.getDoiTuongQuanLyId());
            copy.setHopDongNhomUuTienId(source.getHopDongNhomUuTienId());
            copy.setNhaThauId(source.getNhaThauId());
            copy.setKhuVucId(source.getKhuVucId());
            copy.setTinhThanhId(source.getTinhThanhId());
            copy.setTrangThaiHopDongId(trangThaiId);
            copy.setHoatDong(true);
            maybeSetNgayHtTc(copy, trangThaiId);

            HopDongDoiTuong saved = hopDongDoiTuongRepository.save(copy);
            createdEntities.add(saved);

            for (HopDongDoiTuongGiaTri giaTri : giaTriBySource.getOrDefault(source.getId(), List.of())) {
                String value = giaTri.getGiaTri();
                ThuocTinhResponse definition = thuocTinhMap.get(giaTri.getThuocTinhId());
                if (definition != null && Boolean.TRUE.equals(definition.getLaKhoaChinh())) {
                    value = maMoi;
                }
                if (value == null || value.isBlank()) {
                    continue;
                }
                HopDongDoiTuongGiaTriTaoRequest createGiaTri = new HopDongDoiTuongGiaTriTaoRequest();
                createGiaTri.setHopDongDoiTuongId(saved.getId());
                createGiaTri.setThuocTinhId(giaTri.getThuocTinhId());
                createGiaTri.setGiaTri(value);
                createGiaTri.setHoatDong(true);
                giaTriCreates.add(createGiaTri);
            }
        }

        hopDongDoiTuongGiaTriService.createBatchForImport(giaTriCreates);

        List<HopDongDoiTuongResponse> responses = enrichList(createdEntities);
        UUID hopDongId = createdEntities.get(0).getHopDongId();
        appEventContext.audit(
                "THEM_DOI_TUONG",
                "Nhân bản đối tượng hợp đồng",
                null,
                Map.of(
                        "hopDongId",
                        hopDongId,
                        "doiTuongIds",
                        createdEntities.stream().map(HopDongDoiTuong::getId).toList(),
                        "nguonIds",
                        sourceIds));
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public void assertImportAllowed(UUID hopDongId, UUID doiTuongQuanLyId) {
        HopDong hopDong = findHopDong(hopDongId);
        assertDoiTuongAllowedForHopDong(hopDong, doiTuongQuanLyId);
    }

    @Override
    @Transactional
    public List<UUID> saveBatchForImport(List<HopDongDoiTuongTaoRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        UUID hopDongId = requests.get(0).getHopDongId();
        UUID doiTuongQuanLyId = requests.get(0).getDoiTuongQuanLyId();
        HopDong hopDong = findHopDong(hopDongId);

        List<HopDongDoiTuong> entities = new ArrayList<>(requests.size());
        for (HopDongDoiTuongTaoRequest request : requests) {
            HopDongDoiTuong entity = hopDongMapper.fromHopDongDoiTuongTaoRequest(request);
            entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());
            entity.setTrangThaiHopDongId(
                    resolveTrangThaiForCreate(hopDong, doiTuongQuanLyId, request.getTrangThaiHopDongId()));
            maybeSetNgayHtTc(entity, entity.getTrangThaiHopDongId());
            entities.add(entity);
        }
        return hopDongDoiTuongRepository.saveAll(entities).stream()
                .map(HopDongDoiTuong::getId)
                .toList();
    }

    @Override
    @Transactional
    public HopDongDoiTuongResponse update(UUID id, HopDongDoiTuongCapNhatRequest request) {
        HopDongDoiTuong entity = findById(id);
        hopDongArchiveGuard.assertWritable(entity.getHopDongId());
        HopDong hopDong = findHopDong(entity.getHopDongId());
        UUID trangThaiTruoc = entity.getTrangThaiHopDongId();
        boolean activatingPending = !Boolean.TRUE.equals(entity.getHoatDong())
                && Boolean.TRUE.equals(request.getHoatDong());

        hopDongMapper.updateFromHopDongDoiTuongCapNhatRequest(request, entity);
        applyHdThauPhuSigningRules(entity, request);
        validatePendingActivationRequiresNhaThau(activatingPending, entity);

        if (request.getTrangThaiHopDongId() != null
                && !Objects.equals(request.getTrangThaiHopDongId(), entity.getTrangThaiHopDongId())) {
            assertTrangThaiCoTheThayDoiThuCong(entity);
            validateTrangThai(hopDong, entity.getDoiTuongQuanLyId(), request.getTrangThaiHopDongId());
            maybeSetNgayHtTc(entity, request.getTrangThaiHopDongId());
        }
        if (request.getNgayHtTc() != null) {
            entity.setNgayHtTc(request.getNgayHtTc());
        }

        HopDongDoiTuongResponse response = enrichList(List.of(hopDongDoiTuongRepository.save(entity))).get(0);
        // Mã/tỉnh/khu vực/nhà thầu đều đọc từ giaTri của đối tượng — update() có thể đổi
        // bất kỳ field nào trong số đó, nên luôn phát event thay vì cố đoán field nào đổi.
        applicationEventPublisher.publishEvent(
                HopDongDoiTuongMetaChangedEvent.of(entity.getHopDongId(), entity.getId()));
        auditDoiTuongCapNhat(entity, trangThaiTruoc);
        return response;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        deleteBatch(List.of(id));
    }

    @Override
    @Transactional
    public int deleteBatch(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        List<UUID> allowedIds = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .filter(this::matchesContractorScopeId)
                .toList();
        if (allowedIds.isEmpty()) {
            return 0;
        }

        List<HopDongDoiTuong> entities = hopDongDoiTuongRepository.findAllById(allowedIds);
        entities.forEach(entity -> hopDongArchiveGuard.assertWritable(entity.getHopDongId()));
        assertKhongXoaDoiTuongCoDuLieuNghiemThu(entities);

        Instant now = Instant.now();
        hopDongDoiTuongGiaTriService.softDeleteByHopDongDoiTuongIds(allowedIds, now);
        int deleted = hopDongDoiTuongRepository.softDeleteByIds(allowedIds, now);
        // Cascade sang sản lượng/vướng mắc do business tự xử lý (xem SanLuongConsumer/
        // VuongMacConsumer) — core chỉ phát sự kiện, không gọi thẳng repository module khác.
        applicationEventPublisher.publishEvent(HopDongDoiTuongCascadeDeleteEvent.of(allowedIds));
        appEventContext.audit("XOA_DOI_TUONG", "Xóa đối tượng khỏi hợp đồng"
                + (deleted > 1 ? " (xóa hàng loạt " + deleted + " đối tượng)" : ""), null,
                Map.of("doiTuongIds", allowedIds));
        return deleted;
    }

    @Override
    @Transactional
    public int deleteByFilter(HopDongDoiTuongXoaTheoLocRequest request) {
        String keyword = EntityFilter.normalizeSearch(request.getSearch());
        List<UUID> excludeIds = request.getExcludeIds() == null
                ? List.of()
                : request.getExcludeIds().stream().filter(Objects::nonNull).distinct().toList();
        return executeBulkDeleteByFilter(
                Instant.now(),
                request.getActiveOnly(),
                request.getHopDongId(),
                request.getDoiTuongQuanLyId(),
                request.getTrangThaiHopDongId(),
                request.getHopDongNhomUuTienId(),
                request.getWithoutNhomUuTien(),
                request.getWithNhomUuTien(),
                keyword,
                excludeIds);
    }

    @Override
    @Transactional
    public HopDongDoiTuongXoaTheoLocBatchResponse deleteByFilterBatch(
            HopDongDoiTuongXoaTheoLocRequest request,
            int page) {
        if (page > 0) {
            return new HopDongDoiTuongXoaTheoLocBatchResponse(0, 0, false);
        }
        int deletedCount = deleteByFilter(request);
        long total = deletedCount;
        return new HopDongDoiTuongXoaTheoLocBatchResponse(deletedCount, total, false);
    }

    private int executeBulkDeleteByFilter(
            Instant now,
            Boolean activeOnly,
            UUID hopDongId,
            UUID doiTuongQuanLyId,
            UUID trangThaiHopDongId,
            UUID hopDongNhomUuTienId,
            Boolean withoutNhomUuTien,
            Boolean withNhomUuTien,
            String keyword,
            List<UUID> excludeIds) {
        boolean hasExclude = !excludeIds.isEmpty();
        List<UUID> excludeParam = hasExclude
                ? excludeIds
                : List.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        Optional<ContractorScope> scopeOpt = contractorScopeService.currentScope();
        boolean hasScope = scopeOpt.isPresent() && !scopeOpt.get().hopDongDoiTuongIds().isEmpty();
        List<UUID> scopeParam = hasScope
                ? new ArrayList<>(scopeOpt.get().hopDongDoiTuongIds())
                : List.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        if (keyword.isBlank()) {
            // Resolve ids trước khi update — dùng để phát HopDongDoiTuongCascadeDeleteEvent
            // cho business tự cascade, thay vì core gọi thẳng repository module khác.
            List<UUID> affectedIds = hopDongDoiTuongRepository.findIdsByFilter(
                    activeOnly,
                    hopDongId,
                    doiTuongQuanLyId,
                    trangThaiHopDongId,
                    hopDongNhomUuTienId,
                    withoutNhomUuTien,
                    withNhomUuTien,
                    hasExclude,
                    excludeParam,
                    hasScope,
                    scopeParam);
            assertKhongXoaDoiTuongCoDuLieuNghiemThu(
                    hopDongDoiTuongRepository.findByIdInAndNgayXoaIsNull(affectedIds));
            hopDongDoiTuongGiaTriService.softDeleteByFilter(
                    now,
                    activeOnly,
                    hopDongId,
                    doiTuongQuanLyId,
                    trangThaiHopDongId,
                    hopDongNhomUuTienId,
                    withoutNhomUuTien,
                    withNhomUuTien,
                    hasExclude,
                    excludeParam,
                    hasScope,
                    scopeParam);
            int deleted = hopDongDoiTuongRepository.softDeleteByFilter(
                    now,
                    activeOnly,
                    hopDongId,
                    doiTuongQuanLyId,
                    trangThaiHopDongId,
                    hopDongNhomUuTienId,
                    withoutNhomUuTien,
                    withNhomUuTien,
                    hasExclude,
                    excludeParam,
                    hasScope,
                    scopeParam);
            if (!affectedIds.isEmpty()) {
                applicationEventPublisher.publishEvent(HopDongDoiTuongCascadeDeleteEvent.of(affectedIds));
            }
            return deleted;
        }

        List<UUID> affectedIds = hopDongDoiTuongRepository.findIdsByFilterWithKeyword(
                activeOnly,
                hopDongId,
                doiTuongQuanLyId,
                trangThaiHopDongId,
                hopDongNhomUuTienId,
                keyword,
                withoutNhomUuTien,
                withNhomUuTien,
                hasExclude,
                excludeParam,
                hasScope,
                scopeParam);
        assertKhongXoaDoiTuongCoDuLieuNghiemThu(
                hopDongDoiTuongRepository.findByIdInAndNgayXoaIsNull(affectedIds));
        hopDongDoiTuongGiaTriService.softDeleteByFilterWithKeyword(
                now,
                activeOnly,
                hopDongId,
                doiTuongQuanLyId,
                trangThaiHopDongId,
                hopDongNhomUuTienId,
                keyword,
                withoutNhomUuTien,
                withNhomUuTien,
                hasExclude,
                excludeParam,
                hasScope,
                scopeParam);
        int deleted = hopDongDoiTuongRepository.softDeleteByFilterWithKeyword(
                now,
                activeOnly,
                hopDongId,
                doiTuongQuanLyId,
                trangThaiHopDongId,
                hopDongNhomUuTienId,
                keyword,
                withoutNhomUuTien,
                withNhomUuTien,
                hasExclude,
                excludeParam,
                hasScope,
                scopeParam);
        if (!affectedIds.isEmpty()) {
            applicationEventPublisher.publishEvent(HopDongDoiTuongCascadeDeleteEvent.of(affectedIds));
        }
        return deleted;
    }

    @Override
    @Transactional(readOnly = true)
    public long countActive() {
        return hopDongDoiTuongRepository.findByNgayXoaIsNull().size();
    }

    private UUID resolveTrangThaiForCreate(HopDong hopDong, UUID doiTuongQuanLyId, UUID requestedTrangThaiId) {
        if (requestedTrangThaiId == null) {
            return null;
        }
        validateTrangThai(hopDong, doiTuongQuanLyId, requestedTrangThaiId);
        return requestedTrangThaiId;
    }

    private void assertDoiTuongAllowedForHopDong(HopDong hopDong, UUID doiTuongQuanLyId) {
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
        if (!DoiTuongHopDongLienKetSupport.isAllowedStationDoiTuong(links, doiTuongQuanLyId)) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Đối tượng quản lý không thuộc cấu hình kiểu hợp đồng");
        }
    }

    private void applyHdThauPhuSigningRules(HopDongDoiTuong entity, HopDongDoiTuongCapNhatRequest request) {
        if (request.getDaKyHdThauPhu() == null && request.getNgayKyHdThauPhu() == null) {
            return;
        }
        if (Boolean.FALSE.equals(entity.getDaKyHdThauPhu())) {
            entity.setNgayKyHdThauPhu(null);
            return;
        }
        if (Boolean.TRUE.equals(entity.getDaKyHdThauPhu()) && entity.getNgayKyHdThauPhu() == null) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Vui lòng nhập ngày ký hợp đồng con");
        }
    }

    /** Phê duyệt đối tượng từ danh sách đợi (hoatDong: false → true) bắt buộc đã phân công nhà thầu. */
    private void validatePendingActivationRequiresNhaThau(boolean activatingPending, HopDongDoiTuong entity) {
        if (!activatingPending) {
            return;
        }
        if (entity.getNhaThauId() != null) {
            return;
        }
        throw new AppException(
                HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                "Phải phân công nhà thầu trước khi phê duyệt đối tượng vào hợp đồng");
    }

    private void maybeSetNgayHtTc(HopDongDoiTuong entity, UUID trangThaiHopDongId) {
        if (entity.getNgayHtTc() != null || trangThaiHopDongId == null) {
            return;
        }
        TrangThaiHopDongResponse status = trangThaiHopDongService.getById(trangThaiHopDongId);
        if (status == null || status.getMa() == null) {
            return;
        }
        String ma = status.getMa().trim().toUpperCase(Locale.ROOT);
        if ("QT".equals(ma)) {
            return;
        }
        if (HopDongDanhSachTienDoHelper.isCompletedStatus(ma)) {
            entity.setNgayHtTc(LocalDate.now());
        }
    }

    private void assertKhongXoaDoiTuongCoDuLieuNghiemThu(List<HopDongDoiTuong> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        int blocked = 0;
        String sampleReason = null;
        for (HopDongDoiTuong entity : entities) {
            TrangThaiHopDongResponse status = resolveTrangThaiForEntity(entity);
            String ma = status != null ? status.getMa() : null;
            String ten = status != null ? status.getTen() : null;
            String lyDo = HopDongDanhSachTienDoHelper.resolveDoiTuongXoaBiKhoaLyDo(
                    entity.getHoatDong(),
                    entity.getNgayHtTc(),
                    ma,
                    ten,
                    entity.getQuyetToanThuc(),
                    entity.getSanLuongHieuLuc());
            if (lyDo != null) {
                blocked++;
                if (sampleReason == null) {
                    sampleReason = lyDo;
                }
            }
        }
        if (blocked <= 0) {
            return;
        }
        String detail = blocked == 1
                ? "Đối tượng " + sampleReason + " — không thể xóa."
                : "Có " + blocked + " đối tượng "
                        + (sampleReason != null ? sampleReason : "đã có dữ liệu nghiệm thu")
                        + " — không thể xóa.";
        throw new AppException(
                HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                detail
                        + " Xóa sẽ gỡ toàn bộ sản lượng thi công và vướng mắc liên quan; "
                        + "liên hệ quản trị nếu cần xử lý ngoại lệ.");
    }

    private TrangThaiHopDongResponse resolveTrangThaiForEntity(HopDongDoiTuong entity) {
        if (entity.getTrangThaiHopDongId() == null) {
            return null;
        }
        try {
            return trangThaiHopDongService.getById(entity.getTrangThaiHopDongId());
        } catch (AppException ignored) {
            return null;
        }
    }

    private void assertTrangThaiCoTheThayDoiThuCong(HopDongDoiTuong entity) {
        TrangThaiHopDongResponse current = null;
        if (entity.getTrangThaiHopDongId() != null) {
            try {
                current = trangThaiHopDongService.getById(entity.getTrangThaiHopDongId());
            } catch (AppException ignored) {
                // trạng thái đã xóa — vẫn khóa nếu có ngayHtTc / hoatDong
            }
        }
        String ma = current != null ? current.getMa() : null;
        String ten = current != null ? current.getTen() : null;
        if (HopDongDanhSachTienDoHelper.isTrangThaiThayDoiThuCongBiKhoa(
                entity.getNgayHtTc(), entity.getHoatDong(), ma, ten)) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Đối tượng đang hoàn thành, hủy hoặc chờ xác nhận — không thể đổi trạng thái");
        }
    }

    private void validateTrangThai(HopDong hopDong, UUID doiTuongQuanLyId, UUID trangThaiHopDongId) {
        if (hopDong.getKieuHopDongId() == null || hopDong.getLoaiHopDongId() == null) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Hợp đồng chưa có loại/kiểu HĐ để gán trạng thái");
        }
        if (!hopDongDoiTuongTrangThaiService.isAllowedTrangThai(
                hopDong.getKieuHopDongId(),
                hopDong.getLoaiHopDongId(),
                doiTuongQuanLyId,
                trangThaiHopDongId)) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Trạng thái không hợp lệ với luồng trạng thái của kiểu HĐ");
        }
    }

    private boolean matchesKeyword(String keyword, HopDongDoiTuongResponse response) {
        if (keyword == null) {
            return true;
        }
        if (EntityFilter.matchesKeyword(keyword, EntityFilter.nullToEmpty(response.getDoiTuongTen()))) {
            return true;
        }
        if (EntityFilter.matchesKeyword(keyword, EntityFilter.nullToEmpty(response.getDoiTuongMa()))) {
            return true;
        }
        if (EntityFilter.matchesKeyword(keyword, EntityFilter.nullToEmpty(response.getTrangThaiMa()))) {
            return true;
        }
        if (EntityFilter.matchesKeyword(keyword, EntityFilter.nullToEmpty(response.getTrangThaiTen()))) {
            return true;
        }
        return response.getGiaTri().stream()
                .anyMatch(value -> EntityFilter.matchesKeyword(keyword, EntityFilter.nullToEmpty(value.getGiaTri())));
    }

    private List<HopDongDoiTuongResponse> enrichList(List<HopDongDoiTuong> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }

        Set<UUID> entityIds = entities.stream()
                .map(HopDongDoiTuong::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<UUID> doiTuongIds = entities.stream()
                .map(HopDongDoiTuong::getDoiTuongQuanLyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<UUID> trangThaiIds = entities.stream()
                .map(HopDongDoiTuong::getTrangThaiHopDongId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, DoiTuongQuanLyResponse> doiTuongMap = doiTuongQuanLyService.getByIds(doiTuongIds).stream()
                .collect(Collectors.toMap(DoiTuongQuanLyResponse::getId, Function.identity()));
        Map<UUID, TrangThaiHopDongResponse> trangThaiMap = trangThaiHopDongService.getByIds(trangThaiIds).stream()
                .collect(Collectors.toMap(TrangThaiHopDongResponse::getId, Function.identity()));

        Map<UUID, Map<UUID, ThuocTinhResponse>> thuocTinhByDoiTuong = thuocTinhService
                .listGroupedByDoiTuongQuanLyIds(doiTuongIds)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .collect(Collectors.toMap(
                                        ThuocTinhResponse::getId,
                                        Function.identity(),
                                        (left, right) -> left)),
                        (left, right) -> left,
                        HashMap::new));

        Map<UUID, List<HopDongDoiTuongGiaTri>> giaTriByEntity = hopDongDoiTuongGiaTriService
                .findActiveEntitiesByHopDongDoiTuongIds(entityIds)
                .stream()
                .collect(Collectors.groupingBy(HopDongDoiTuongGiaTri::getHopDongDoiTuongId));

        Set<UUID> nhomUuTienIds = entities.stream()
                .map(HopDongDoiTuong::getHopDongNhomUuTienId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, HopDongNhomUuTien> nhomUuTienMap = nhomUuTienIds.isEmpty()
                ? Map.of()
                : hopDongNhomUuTienRepository.findAllById(nhomUuTienIds).stream()
                        .filter(group -> group.getNgayXoa() == null)
                        .collect(Collectors.toMap(HopDongNhomUuTien::getId, Function.identity()));

        Set<UUID> nhaThauIds = entities.stream()
                .map(HopDongDoiTuong::getNhaThauId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> nhaThauTenMap = nhaThauIds.isEmpty()
                ? Map.of()
                : nguoiDungRepository.findAllById(nhaThauIds).stream()
                        .filter(nd -> nd.getNgayXoa() == null)
                        .collect(Collectors.toMap(NguoiDung::getId, NguoiDung::getHoTen));

        return entities.stream()
                .map(entity -> toEnrichedResponse(
                        entity,
                        doiTuongMap,
                        trangThaiMap,
                        thuocTinhByDoiTuong,
                        giaTriByEntity.getOrDefault(entity.getId(), List.of()),
                        nhomUuTienMap,
                        nhaThauTenMap))
                .toList();
    }

    /**
     * Bản nhẹ của {@link #enrichList(List)} — chỉ resolve {@code doiTuongMa}/{@code doiTuongTen}
     * (1 lookup {@code doiTuongQuanLyService.getByIds}), bỏ hẳn trạng thái/nhóm ưu tiên/giá trị
     * EAV. Dùng cho volume — xem javadoc {@link HopDongDoiTuongService#listActiveLiteByHopDongIds}.
     */
    private List<HopDongDoiTuongResponse> enrichListLite(List<HopDongDoiTuong> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }
        Set<UUID> doiTuongIds = entities.stream()
                .map(HopDongDoiTuong::getDoiTuongQuanLyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, DoiTuongQuanLyResponse> doiTuongMap = doiTuongQuanLyService.getByIds(doiTuongIds).stream()
                .collect(Collectors.toMap(DoiTuongQuanLyResponse::getId, Function.identity()));

        return entities.stream()
                .map(entity -> {
                    HopDongDoiTuongResponse response = hopDongMapper.toHopDongDoiTuongResponse(entity);
                    DoiTuongQuanLyResponse doiTuong = doiTuongMap.get(entity.getDoiTuongQuanLyId());
                    if (doiTuong != null) {
                        response.setDoiTuongMa(doiTuong.getMa());
                        response.setDoiTuongTen(doiTuong.getTen());
                    }
                    return response;
                })
                .toList();
    }

    private HopDongDoiTuongResponse toEnrichedResponse(
            HopDongDoiTuong entity,
            Map<UUID, DoiTuongQuanLyResponse> doiTuongMap,
            Map<UUID, TrangThaiHopDongResponse> trangThaiMap,
            Map<UUID, Map<UUID, ThuocTinhResponse>> thuocTinhByDoiTuong,
            List<HopDongDoiTuongGiaTri> entityGiaTri,
            Map<UUID, HopDongNhomUuTien> nhomUuTienMap,
            Map<UUID, String> nhaThauTenMap
    ) {
        HopDongDoiTuongResponse response = hopDongMapper.toHopDongDoiTuongResponse(entity);
        if (entity.getNhaThauId() != null) {
            response.setNhaThauTen(nhaThauTenMap.get(entity.getNhaThauId()));
        }

        DoiTuongQuanLyResponse doiTuong = doiTuongMap.get(entity.getDoiTuongQuanLyId());
        if (doiTuong != null) {
            response.setDoiTuongMa(doiTuong.getMa());
            response.setDoiTuongTen(doiTuong.getTen());
        }

        TrangThaiHopDongResponse status = trangThaiMap.get(entity.getTrangThaiHopDongId());
        if (status != null) {
            response.setTrangThaiMa(status.getMa());
            response.setTrangThaiTen(status.getTen());
            response.setTrangThaiMauSac(status.getMauSac());
        }

        HopDongNhomUuTien nhomUuTien = entity.getHopDongNhomUuTienId() != null
                ? nhomUuTienMap.get(entity.getHopDongNhomUuTienId())
                : null;
        if (nhomUuTien != null) {
            response.setNhomUuTienTen(nhomUuTien.getTen());
            response.setNhomUuTienMauSac(nhomUuTien.getMauSac());
        }

        Map<UUID, ThuocTinhResponse> thuocTinhMap = entity.getDoiTuongQuanLyId() != null
                ? thuocTinhByDoiTuong.getOrDefault(entity.getDoiTuongQuanLyId(), Map.of())
                : Map.of();
        List<HopDongDoiTuongGiaTriResponse> values = entityGiaTri.stream()
                .sorted(Comparator.comparing(HopDongDoiTuongGiaTri::getNgayTao, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(value -> {
                    HopDongDoiTuongGiaTriResponse item = hopDongMapper.toHopDongDoiTuongGiaTriResponse(value);
                    ThuocTinhResponse definition = thuocTinhMap.get(value.getThuocTinhId());
                    if (definition != null) {
                        item.setTenThuocTinh(definition.getTen());
                    }
                    return item;
                })
                .toList();
        response.setGiaTri(values);
        return response;
    }

    private boolean matchesContractorScope(HopDongDoiTuongResponse response) {
        Optional<ContractorScope> scope = contractorScopeService.currentScope();
        if (scope.isEmpty()) {
            return true;
        }
        return scope.get().hopDongDoiTuongIds().contains(response.getId());
    }

    private boolean matchesContractorScopeId(UUID id) {
        Optional<ContractorScope> scope = contractorScopeService.currentScope();
        if (scope.isEmpty()) {
            return true;
        }
        return scope.get().hopDongDoiTuongIds().contains(id);
    }

    private HopDongDoiTuong findById(UUID id) {
        return hopDongDoiTuongRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_NOT_FOUND, "Không tìm thấy bản ghi"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findActiveIdsWithActiveHopDong(UUID hopDongId) {
        return hopDongDoiTuongRepository.findActiveIdsWithActiveHopDong(hopDongId);
    }

    private HopDong findHopDong(UUID hopDongId) {
        return hopDongRepository.findByIdAndNgayXoaIsNull(hopDongId)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_NOT_FOUND, "Không tìm thấy hợp đồng"));
    }

    // =====================================================================================
    // HopDongDoiTuongSnapshotService — cache tổng hợp mã/tỉnh/khu vực/nhà thầu/tên+loại hợp
    // đồng (nhóm tĩnh, core tự tính) + sản lượng/vướng mắc (nhóm động, business.sanluong/
    // business.vuongmac chủ động patch vào). Gộp chung impl với HopDongDoiTuongService vì 2
    // interface luôn được các module khác cần cùng lúc; interface vẫn tách riêng để nơi chỉ
    // cần đọc/patch snapshot (business.sanluong, business.vuongmac) không phải phụ thuộc vào
    // toàn bộ CRUD surface của HopDongDoiTuongService.
    // =====================================================================================

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, HopDongDoiTuongSnapshot> getSnapshots(Collection<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds == null || hopDongDoiTuongIds.isEmpty()) {
            return Map.of();
        }
        List<UUID> ids = hopDongDoiTuongIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<String> cacheKeys = ids.stream().map(HopDongDoiTuongSnapshotCacheKeys::key).toList();
        List<HopDongDoiTuongSnapshot> cachedValues = cacheService.multiGet(
                HopDongDoiTuongSnapshotCacheNames.SNAPSHOT, cacheKeys, HopDongDoiTuongSnapshot.class);

        Map<UUID, HopDongDoiTuongSnapshot> result = new HashMap<>();
        List<UUID> missingIds = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            HopDongDoiTuongSnapshot cached = cachedValues.get(i);
            if (cached != null) {
                result.put(ids.get(i), cached);
            } else {
                missingIds.add(ids.get(i));
            }
        }
        if (!missingIds.isEmpty()) {
            final int batchSize = 300;
            for (int i = 0; i < missingIds.size(); i += batchSize) {
                List<UUID> batch = missingIds.subList(i, Math.min(i + batchSize, missingIds.size()));
                result.putAll(buildAndCacheStaticSnapshots(batch));
            }
        }
        return result;
    }

    @Override
    @Transactional
    public HopDongDoiTuongSnapshot refreshStaticFields(UUID hopDongDoiTuongId) {
        boolean hadDynamicData = cacheService
                .get(HopDongDoiTuongSnapshotCacheNames.SNAPSHOT, HopDongDoiTuongSnapshotCacheKeys.key(hopDongDoiTuongId),
                        HopDongDoiTuongSnapshot.class)
                .isPresent();
        return buildAndCacheStaticSnapshot(hopDongDoiTuongId, !hadDynamicData);
    }

    @Override
    @Transactional
    public void evictSnapshots(UUID hopDongId) {
        if (hopDongId == null) {
            cacheService.evictAll(HopDongDoiTuongSnapshotCacheNames.SNAPSHOT);
            return;
        }
        List<HopDongDoiTuongResponse> rows = listAll(null, null, false, hopDongId, null, null, null, null);
        for (HopDongDoiTuongResponse row : rows) {
            cacheService.evict(
                    HopDongDoiTuongSnapshotCacheNames.SNAPSHOT, HopDongDoiTuongSnapshotCacheKeys.key(row.getId()));
        }
    }

    @Override
    @Transactional
    public void applySanLuongFields(
            UUID hopDongDoiTuongId,
            BigDecimal tongThanhTien,
            BigDecimal tongThanhTienSanLuong,
            int hangMucDaLam,
            int tongHangMuc,
            LocalDate constructionDate) {
        HopDongDoiTuongSnapshot base = loadOrBuildStaticSnapshot(hopDongDoiTuongId);
        if (base == null) {
            return;
        }
        HopDongDoiTuongSnapshot updated = base.toBuilder()
                .tongThanhTien(tongThanhTien != null ? tongThanhTien : BigDecimal.ZERO)
                .tongThanhTienSanLuong(tongThanhTienSanLuong != null ? tongThanhTienSanLuong : BigDecimal.ZERO)
                .hangMucDaLam(hangMucDaLam)
                .tongHangMuc(tongHangMuc)
                .constructionDate(constructionDate)
                .build();
        cacheService.put(
                HopDongDoiTuongSnapshotCacheNames.SNAPSHOT,
                HopDongDoiTuongSnapshotCacheKeys.key(hopDongDoiTuongId),
                updated,
                cacheProperties.hopDongDoiTuongSnapshotTtl());
    }

    @Override
    @Transactional
    public void applyVuongMacFields(UUID hopDongDoiTuongId, int soVuongMac) {
        HopDongDoiTuongSnapshot base = loadOrBuildStaticSnapshot(hopDongDoiTuongId);
        if (base == null) {
            return;
        }
        HopDongDoiTuongSnapshot updated = base.toBuilder()
                .soVuongMac(soVuongMac)
                .build();
        cacheService.put(
                HopDongDoiTuongSnapshotCacheNames.SNAPSHOT,
                HopDongDoiTuongSnapshotCacheKeys.key(hopDongDoiTuongId),
                updated,
                cacheProperties.hopDongDoiTuongSnapshotTtl());
    }

    /** Cache hiện có (giữ nguyên nhóm động) hoặc build mới chỉ nhóm tĩnh nếu chưa có gì. */
    private HopDongDoiTuongSnapshot loadOrBuildStaticSnapshot(UUID hopDongDoiTuongId) {
        return cacheService
                .get(HopDongDoiTuongSnapshotCacheNames.SNAPSHOT, HopDongDoiTuongSnapshotCacheKeys.key(hopDongDoiTuongId),
                        HopDongDoiTuongSnapshot.class)
                .orElseGet(() -> computeStaticSnapshot(hopDongDoiTuongId));
    }

    /**
     * Build nhóm tĩnh, giữ nguyên nhóm động đang cache (nếu có), ghi đè cache. Nếu
     * publishMissingEventIfEmpty = true và trước đó chưa có gì trong cache, phát
     * SnapshotDataMissingEvent để business.sanluong/business.vuongmac tự điền nhóm động.
     */
    private HopDongDoiTuongSnapshot buildAndCacheStaticSnapshot(UUID hopDongDoiTuongId, boolean publishMissingEventIfEmpty) {
        HopDongDoiTuongSnapshot existing = cacheService
                .get(HopDongDoiTuongSnapshotCacheNames.SNAPSHOT, HopDongDoiTuongSnapshotCacheKeys.key(hopDongDoiTuongId),
                        HopDongDoiTuongSnapshot.class)
                .orElse(null);
        HopDongDoiTuongSnapshot staticPart = computeStaticSnapshot(hopDongDoiTuongId);
        if (staticPart == null) {
            cacheService.evict(HopDongDoiTuongSnapshotCacheNames.SNAPSHOT, HopDongDoiTuongSnapshotCacheKeys.key(hopDongDoiTuongId));
            return null;
        }
        HopDongDoiTuongSnapshot merged = existing != null
                ? staticPart.toBuilder()
                        .tongThanhTien(existing.getTongThanhTien())
                        .tongThanhTienSanLuong(existing.getTongThanhTienSanLuong())
                        .hangMucDaLam(existing.getHangMucDaLam())
                        .tongHangMuc(existing.getTongHangMuc())
                        .soVuongMac(existing.getSoVuongMac())
                        .constructionDate(existing.getConstructionDate())
                        .build()
                : staticPart;
        cacheService.put(
                HopDongDoiTuongSnapshotCacheNames.SNAPSHOT,
                HopDongDoiTuongSnapshotCacheKeys.key(hopDongDoiTuongId),
                merged,
                cacheProperties.hopDongDoiTuongSnapshotTtl());
        if (existing == null && publishMissingEventIfEmpty && cacheProperties.enabled()) {
            applicationEventPublisher.publishEvent(SnapshotDataMissingEvent.of(hopDongDoiTuongId));
        }
        return merged;
    }

    /**
     * Bản batch của buildAndCacheStaticSnapshot — dùng khi getSnapshots có nhiều id cache-miss
     * cùng lúc (VD tram-ton/tong-quan chạy trên hàng trăm ứng viên khi cache nguội). Gộp mọi
     * lượt đọc DB (getByIds/buildLabelLookups/resolveObjectGeos/hopDong/loaiHopDong) thành 1
     * round-trip mỗi loại thay vì N round-trip riêng lẻ như computeStaticSnapshot(1 id) trước
     * đây — tránh treo request khi N lớn.
     */
    private Map<UUID, HopDongDoiTuongSnapshot> buildAndCacheStaticSnapshots(List<UUID> hopDongDoiTuongIds) {
        Map<UUID, HopDongDoiTuongSnapshot> staticParts = computeStaticSnapshots(hopDongDoiTuongIds);
        Map<UUID, HopDongDoiTuongSnapshot> result = new HashMap<>();
        for (UUID id : hopDongDoiTuongIds) {
            HopDongDoiTuongSnapshot staticPart = staticParts.get(id);
            if (staticPart == null) {
                cacheService.evict(HopDongDoiTuongSnapshotCacheNames.SNAPSHOT, HopDongDoiTuongSnapshotCacheKeys.key(id));
                continue;
            }
            HopDongDoiTuongSnapshot existing = cacheService
                    .get(HopDongDoiTuongSnapshotCacheNames.SNAPSHOT, HopDongDoiTuongSnapshotCacheKeys.key(id),
                            HopDongDoiTuongSnapshot.class)
                    .orElse(null);
            HopDongDoiTuongSnapshot merged = existing != null
                    ? staticPart.toBuilder()
                            .tongThanhTien(existing.getTongThanhTien())
                            .tongThanhTienSanLuong(existing.getTongThanhTienSanLuong())
                            .hangMucDaLam(existing.getHangMucDaLam())
                            .tongHangMuc(existing.getTongHangMuc())
                            .soVuongMac(existing.getSoVuongMac())
                            .constructionDate(existing.getConstructionDate())
                            .build()
                    : staticPart;
            cacheService.put(
                    HopDongDoiTuongSnapshotCacheNames.SNAPSHOT,
                    HopDongDoiTuongSnapshotCacheKeys.key(id),
                    merged,
                    cacheProperties.hopDongDoiTuongSnapshotTtl());
            result.put(id, merged);
            if (existing == null && cacheProperties.enabled()) {
                applicationEventPublisher.publishEvent(SnapshotDataMissingEvent.of(id));
            }
        }
        return result;
    }

    /**
     * Map hopDongDoiTuongId -> tên nhà thầu, nguồn từ cột chuẩn hóa nha_thau_id (qua nhaThauTen
     * đã enrich sẵn trên response) — nguồn chân lý mới cho HopDongDoiTuongSnapshot.nhaThau,
     * thay cho việc trước đây resolveObjectGeos() chỉ có EAV fallback (map rỗng). Đối tượng
     * chưa có nha_thau_id vẫn rơi về fallback EAV như cũ trong resolveObjectGeo().
     */
    private Map<UUID, String> nhaThauOverrideMap(List<HopDongDoiTuongResponse> sources) {
        Map<UUID, String> map = new HashMap<>();
        for (HopDongDoiTuongResponse source : sources) {
            if (source.getId() != null && source.getNhaThauId() != null
                    && source.getNhaThauTen() != null && !source.getNhaThauTen().isBlank()) {
                map.put(source.getId(), source.getNhaThauTen());
            }
        }
        return map;
    }

    /** Bản batch của computeStaticSnapshot — xem javadoc buildAndCacheStaticSnapshots. */
    private Map<UUID, HopDongDoiTuongSnapshot> computeStaticSnapshots(List<UUID> hopDongDoiTuongIds) {
        List<HopDongDoiTuongResponse> sources = getByIds(hopDongDoiTuongIds);
        if (sources.isEmpty()) {
            return Map.of();
        }

        var lookups = doiTuongGroupSupport.buildLabelLookups(sources);
        Map<UUID, HopDongObjectGeo> geoById = doiTuongGroupSupport
                .resolveObjectGeos(sources, nhaThauOverrideMap(sources))
                .stream()
                .collect(Collectors.toMap(HopDongObjectGeo::hopDongDoiTuongId, geo -> geo, (a, b) -> a));

        Set<UUID> hopDongIds = sources.stream()
                .map(HopDongDoiTuongResponse::getHopDongId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, HopDong> hopDongById = hopDongRepository.findAllById(hopDongIds).stream()
                .collect(Collectors.toMap(HopDong::getId, hd -> hd, (a, b) -> a));

        Set<UUID> loaiHopDongIds = hopDongById.values().stream()
                .map(HopDong::getLoaiHopDongId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, LoaiHopDongResponse> loaiHopDongById = loaiHopDongService.getByIds(loaiHopDongIds);

        Map<UUID, HopDongDoiTuongSnapshot> result = new HashMap<>();
        for (HopDongDoiTuongResponse base : sources) {
            String maDoiTuong = doiTuongGroupSupport.resolveMaTram(base, lookups);
            HopDongObjectGeo geo = geoById.get(base.getId());

            String tenHopDong = null;
            String loaiHopDong = null;
            HopDong hopDong = base.getHopDongId() != null ? hopDongById.get(base.getHopDongId()) : null;
            if (hopDong != null) {
                tenHopDong = hopDong.getTen() != null && !hopDong.getTen().isBlank()
                        ? hopDong.getTen()
                        : hopDong.getMaHopDong();
                if (hopDong.getLoaiHopDongId() != null) {
                    LoaiHopDongResponse loaiHopDongResponse = loaiHopDongById.get(hopDong.getLoaiHopDongId());
                    loaiHopDong = loaiHopDongResponse != null ? loaiHopDongResponse.getTen() : null;
                }
            }

            result.put(base.getId(), HopDongDoiTuongSnapshot.builder()
                    .hopDongDoiTuongId(base.getId())
                    .hopDongId(base.getHopDongId())
                    .maDoiTuong(maDoiTuong)
                    .tinh(geo != null ? geo.province() : "—")
                    .oldProvince(geo != null ? geo.oldProvince() : null)
                    .provinceKey(geo != null ? geo.provinceKey() : null)
                    .khuVuc(geo != null ? geo.region() : "—")
                    .nhaThau(geo != null ? geo.contractor() : "—")
                    .diaChi(geo != null ? geo.diaChi() : "—")
                    .tenHopDong(tenHopDong)
                    .loaiHopDong(loaiHopDong)
                    .tongThanhTien(BigDecimal.ZERO)
                    .tongThanhTienSanLuong(BigDecimal.ZERO)
                    .hangMucDaLam(0)
                    .tongHangMuc(0)
                    .soVuongMac(0)
                    .constructionDate(base.getConstructionDate())
                    .build());
        }
        return result;
    }

    /** @return null nếu đối tượng không còn tồn tại/đã xóa. Chỉ tính nhóm tĩnh (core-only). */
    private HopDongDoiTuongSnapshot computeStaticSnapshot(UUID hopDongDoiTuongId) {
        List<HopDongDoiTuongResponse> source = getByIds(List.of(hopDongDoiTuongId));
        if (source.isEmpty()) {
            return null;
        }
        HopDongDoiTuongResponse base = source.get(0);

        var lookups = doiTuongGroupSupport.buildLabelLookups(List.of(base));
        String maDoiTuong = doiTuongGroupSupport.resolveMaTram(base, lookups);
        HopDongObjectGeo geo = doiTuongGroupSupport
                .resolveObjectGeos(List.of(base), nhaThauOverrideMap(List.of(base)))
                .stream()
                .findFirst()
                .orElse(null);

        String tenHopDong = null;
        String loaiHopDong = null;
        if (base.getHopDongId() != null) {
            // Đọc thẳng repository (không qua HopDongService) — tránh vòng lặp bean:
            // HopDongService -> HopDongDoiTuongExcelImportService -> HopDongDoiTuongService.
            HopDong hopDong = hopDongRepository.findByIdAndNgayXoaIsNull(base.getHopDongId()).orElse(null);
            if (hopDong != null) {
                tenHopDong = hopDong.getTen() != null && !hopDong.getTen().isBlank()
                        ? hopDong.getTen()
                        : hopDong.getMaHopDong();
                if (hopDong.getLoaiHopDongId() != null) {
                    LoaiHopDongResponse loaiHopDongResponse = loaiHopDongService.getById(hopDong.getLoaiHopDongId());
                    loaiHopDong = loaiHopDongResponse != null ? loaiHopDongResponse.getTen() : null;
                }
            }
        }

        return HopDongDoiTuongSnapshot.builder()
                .hopDongDoiTuongId(hopDongDoiTuongId)
                .hopDongId(base.getHopDongId())
                .maDoiTuong(maDoiTuong)
                .tinh(geo != null ? geo.province() : "—")
                .oldProvince(geo != null ? geo.oldProvince() : null)
                .provinceKey(geo != null ? geo.provinceKey() : null)
                .khuVuc(geo != null ? geo.region() : "—")
                .nhaThau(geo != null ? geo.contractor() : "—")
                .diaChi(geo != null ? geo.diaChi() : "—")
                .tenHopDong(tenHopDong)
                .loaiHopDong(loaiHopDong)
                .tongThanhTien(BigDecimal.ZERO)
                .tongThanhTienSanLuong(BigDecimal.ZERO)
                .hangMucDaLam(0)
                .tongHangMuc(0)
                .soVuongMac(0)
                .constructionDate(base.getConstructionDate())
                .build();
    }
}
