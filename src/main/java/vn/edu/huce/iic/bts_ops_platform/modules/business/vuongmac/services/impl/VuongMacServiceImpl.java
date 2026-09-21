package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.dto.GeoRefResponse;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.dto.RankedItemResponse;
import vn.edu.huce.iic.bts_ops_platform.common.event.VuongMacChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.PaginationDefaults;
import vn.edu.huce.iic.bts_ops_platform.common.security.DataScopeService;
import vn.edu.huce.iic.bts_ops_platform.config.AppCacheProperties;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.cache.VuongMacCacheNames;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.entity.NguoiDung;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.repository.NguoiDungRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LoaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LoaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.response.TepDinhKemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.services.TepDinhKemService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.HopDongObjectGeo;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.HopDongDoiTuongSnapshot;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongSnapshotService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.HopDongDanhSachDoiTuongGroupSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.request.VuongMacCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.request.VuongMacTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.request.VuongMacTrungKiemTraRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response.VuongMacDoiTuongOptionResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response.VuongMacLichSuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response.VuongMacResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response.VuongMacTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.helpers.VuongMacKieuHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.entity.VuongMac;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.entity.VuongMacLichSu;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.exception.VuongMacErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.mapper.VuongMacMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.repository.VuongMacLichSuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.repository.VuongMacRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.services.VuongMacService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VuongMacServiceImpl implements VuongMacService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "pending", "in_progress", "resolved", "rejected");

    /** Vướng mắc còn mở — dùng cho trạm tồn / dashboard. Bỏ qua resolved & rejected. */
    public static final Set<String> TRANG_THAI_DANG_MO = Set.of("pending", "in_progress");

    /** Đã kết thúc — không còn tính là đang vướng. */
    public static final Set<String> TRANG_THAI_DA_KET_THUC = Set.of("resolved", "rejected");
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter MA_DATE = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZONE);
    private static final AtomicInteger MA_SEQ = new AtomicInteger(0);

    private final VuongMacRepository vuongMacRepository;
    private final VuongMacLichSuRepository vuongMacLichSuRepository;
    private final VuongMacMapper vuongMacMapper;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final HopDongDoiTuongSnapshotService hopDongDoiTuongSnapshotService;
    private final HopDongDanhSachDoiTuongGroupSupport doiTuongGroupSupport;
    private final HopDongRepository hopDongRepository;
    private final LoaiHopDongRepository loaiHopDongRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final TepDinhKemService tepDinhKemService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CacheService cacheService;
    private final AppCacheProperties cacheProperties;
    private final DataScopeService dataScopeService;

    @Override
    @Transactional(readOnly = true)
    public long demActiveTheoTrangThai(Collection<String> trangThais) {
        if (trangThais == null || trangThais.isEmpty()) {
            return 0L;
        }
        if (dataScopeService.isFullAccess()) {
            return vuongMacRepository.countActiveByTrangThaiIn(trangThais);
        }
        UUID scopeKhuVuc = dataScopeService.resolveKhuVucFilter(null);
        return filterAndEnrich(
                null, Boolean.TRUE, false, null, null, false, null, null,
                null, scopeKhuVuc, null, null, null).stream()
                .filter(item -> trangThais.contains(item.getTrangThai()))
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public long demActiveTheoTrangThaiVaPhamVi(
            Collection<String> trangThais, UUID loaiHopDongId, UUID kieuHopDongId) {
        if (trangThais == null || trangThais.isEmpty()) {
            return 0L;
        }
        return vuongMacRepository.countActiveByTrangThaiInAndScope(trangThais, loaiHopDongId, kieuHopDongId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> demOpenIssuesGroupByHopDong(UUID loaiHopDongId, UUID kieuHopDongId) {
        return vuongMacRepository.countOpenIssuesGroupByHopDongScoped(loaiHopDongId, kieuHopDongId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Long> demActiveTheoLoaiHopDongVaTrangThai(Collection<String> trangThais) {
        if (trangThais == null || trangThais.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Long> result = new HashMap<>();
        for (Object[] row : vuongMacRepository.countActiveByLoaiHopDongAndTrangThaiIn(trangThais)) {
            if (row[0] == null) {
                continue;
            }
            result.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<RankedItemResponse> topHopDongTheoVuongMacMo(int top) {
        Optional<List> cached = cacheService.get(VuongMacCacheNames.STATS, "top-hopdong|" + top, List.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<RankedItemResponse> result = computeTopHopDongTheoVuongMacMo(top);
        cacheService.put(VuongMacCacheNames.STATS, "top-hopdong|" + top, result, cacheProperties.vuongMacRowTtl());
        return result;
    }

    private List<RankedItemResponse> computeTopHopDongTheoVuongMacMo(int top) {
        List<Object[]> rows = vuongMacRepository.countOpenIssuesGroupByHopDong();
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Map.Entry<UUID, Long>> sorted = rows.stream()
                .map(row -> Map.entry((UUID) row[0], ((Number) row[1]).longValue()))
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(Math.max(1, top))
                .toList();
        Map<UUID, HopDong> hopDongById = hopDongRepository
                .findByIdInAndNgayXoaIsNull(sorted.stream().map(Map.Entry::getKey).toList())
                .stream()
                .collect(Collectors.toMap(HopDong::getId, hopDong -> hopDong));
        return sorted.stream()
                .map(entry -> {
                    HopDong hopDong = hopDongById.get(entry.getKey());
                    String label = hopDong == null
                            ? entry.getKey().toString()
                            : (hopDong.getMaHopDong() != null ? hopDong.getMaHopDong() : hopDong.getMa())
                            + (hopDong.getTen() != null ? " - " + hopDong.getTen() : "");
                    return RankedItemResponse.builder()
                            .label(label)
                            .value(entry.getValue())
                            .build();
                })
                .toList();
    }

    /**
     * Đối tượng đang có vướng mắc mở ({@code pending}/{@code in_progress}).
     * Bỏ qua {@code resolved} (đã giải quyết) và {@code rejected} (từ chối giải quyết).
     */
    @Override
    @Transactional(readOnly = true)
    public Set<UUID> findDoiTuongIdsCoVuongDangMo(Collection<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds == null || hopDongDoiTuongIds.isEmpty()) {
            return Set.of();
        }
        List<UUID> ids = hopDongDoiTuongIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(vuongMacRepository.findDoiTuongIdsCoVuongDangMo(ids));
    }

    @Override
    @Transactional(readOnly = true)
    public long demMoTrongGiaiDoan(
            UUID hopDongId,
            UUID doiTuongQuanLyId,
            boolean hasScope,
            Collection<UUID> scopeIds,
            LocalDate dateFrom,
            LocalDate dateTo) {
        if (hasScope && (scopeIds == null || scopeIds.isEmpty())) {
            return 0L;
        }
        Instant tuFromInstant = dateFrom != null ? dateFrom.atStartOfDay(ZONE).toInstant() : null;
        Instant denToInstant = dateTo != null ? dateTo.plusDays(1).atStartOfDay(ZONE).toInstant() : null;
        return vuongMacRepository.demMoTrongGiaiDoan(
                hopDongId, doiTuongQuanLyId, hasScope, scopeIds, tuFromInstant, denToInstant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VuongMacResponse> list(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID hopDongId,
            String trangThai,
            String giaiDoan,
            String kieuVuongMac,
            UUID loaiHopDongId,
            UUID khuVucId,
            UUID tinhThanhId
    ) {
        khuVucId = dataScopeService.resolveKhuVucFilter(khuVucId);
        return filterAndEnrich(
                search, activeOnly, includeDeleted, hopDongId, trangThai, false, giaiDoan, kieuVuongMac,
                loaiHopDongId, khuVucId, tinhThanhId, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VuongMacResponse> listPage(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID hopDongId,
            String trangThai,
            boolean dangMoOnly,
            String giaiDoan,
            String kieuVuongMac,
            UUID loaiHopDongId,
            UUID khuVucId,
            UUID tinhThanhId,
            Integer quaHanNgay,
            LocalDate ngayBaoCao,
            LocalDate dateFrom,
            LocalDate dateTo,
            Integer page,
            Integer size
    ) {
        khuVucId = dataScopeService.resolveKhuVucFilter(khuVucId);
        String statusFilter = normalizeOptional(trangThai);
        String phaseFilter = normalizeOptional(giaiDoan);
        String kieuFilter = normalizeOptional(kieuVuongMac);
        boolean activeOnlyFlag = Boolean.TRUE.equals(activeOnly);
        Instant nguongNgay = resolveNguongNgay(quaHanNgay, ngayBaoCao);
        String keyword = EntityFilter.normalizeSearch(search);
        String likeKeyword = (keyword == null || keyword.isBlank()) ? null : "%" + keyword + "%";
        Instant tuNgay = dateFrom != null ? dateFrom.atStartOfDay(ZONE).toInstant() : null;
        Instant denNgay = dateTo != null ? dateTo.plusDays(1).atStartOfDay(ZONE).toInstant() : null;

        int pageNumber = EntityFilter.normalizePage(page);
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE);

        Page<VuongMac> result = vuongMacRepository.findFilteredPage(
                includeDeleted, activeOnlyFlag, hopDongId, statusFilter, dangMoOnly, phaseFilter, kieuFilter,
                loaiHopDongId, khuVucId, tinhThanhId, nguongNgay, tuNgay, denNgay, likeKeyword,
                PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "ngayTao")));

        List<VuongMacResponse> items = enrichAll(result.getContent());
        return PageResponse.ofItems(items, pageNumber, pageSize, result.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public VuongMacTongQuanResponse tongQuan(
            Boolean activeOnly,
            boolean includeDeleted,
            UUID hopDongId,
            String trangThai,
            String giaiDoan,
            String kieuVuongMac,
            UUID loaiHopDongId,
            UUID khuVucId,
            UUID tinhThanhId,
            boolean includeKieuCounts) {
        khuVucId = dataScopeService.resolveKhuVucFilter(khuVucId);
        String cacheKey = tongQuanCacheKey(
                activeOnly, includeDeleted, hopDongId, trangThai, giaiDoan, kieuVuongMac,
                loaiHopDongId, khuVucId, tinhThanhId, includeKieuCounts);
        Optional<VuongMacTongQuanResponse> cached = cacheService.get(
                VuongMacCacheNames.STATS, cacheKey, VuongMacTongQuanResponse.class);
        if (cached.isPresent()) {
            return cached.get();
        }

        String statusFilter = normalizeOptional(trangThai);
        String phaseFilter = normalizeOptional(giaiDoan);
        String kieuFilter = normalizeOptional(kieuVuongMac);
        boolean activeOnlyFlag = Boolean.TRUE.equals(activeOnly);

        List<Object[]> rows = vuongMacRepository.countTongQuan(
                includeDeleted, activeOnlyFlag, hopDongId, statusFilter, phaseFilter, kieuFilter, loaiHopDongId,
                khuVucId, tinhThanhId);
        Object[] row = rows.isEmpty() ? new Object[6] : rows.get(0);

        VuongMacTongQuanResponse response = VuongMacTongQuanResponse.builder()
                .total(toLong(row[0]))
                .pending(toLong(row[1]))
                .inProgress(toLong(row[2]))
                .resolved(toLong(row[3]))
                .rejected(toLong(row[4]))
                .overdue30Days(toLong(row[5]))
                .countsByKieu(includeKieuCounts ? buildCountsByKieuCached() : null)
                .build();
        cacheService.put(VuongMacCacheNames.STATS, cacheKey, response, cacheProperties.vuongMacRowTtl());
        return response;
    }

    private Map<String, Long> buildCountsByKieuCached() {
        // KHÔNG ép kiểu thẳng Map thô về Map<String, Long> — cacheService.get(..., Map.class) mất
        // generic info (type erasure), Jackson deserialize số JSON thành Integer chứ không phải
        // Long, gây ClassCastException ở lần đọc cache thứ 2 trở đi (lần đầu luôn compute tươi
        // nên không lộ bug). Phải convert tường minh qua Number.longValue().
        Optional<Map> raw = cacheService.get(VuongMacCacheNames.STATS, "kieu-counts", Map.class);
        if (raw.isPresent()) {
            Map<String, Long> counts = new LinkedHashMap<>();
            for (Object entry : raw.get().entrySet()) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) entry;
                counts.put(String.valueOf(e.getKey()), e.getValue() == null ? 0L : ((Number) e.getValue()).longValue());
            }
            return counts;
        }
        Map<String, Long> counts = buildCountsByKieu();
        cacheService.put(VuongMacCacheNames.STATS, "kieu-counts", counts, cacheProperties.vuongMacRowTtl());
        return counts;
    }

    private static String tongQuanCacheKey(
            Boolean activeOnly,
            boolean includeDeleted,
            UUID hopDongId,
            String trangThai,
            String giaiDoan,
            String kieuVuongMac,
            UUID loaiHopDongId,
            UUID khuVucId,
            UUID tinhThanhId,
            boolean includeKieuCounts) {
        return "tq|"
                + Boolean.TRUE.equals(activeOnly)
                + "|" + includeDeleted
                + "|" + (hopDongId != null ? hopDongId : "")
                + "|" + (trangThai != null ? trangThai : "")
                + "|" + (giaiDoan != null ? giaiDoan : "")
                + "|" + (kieuVuongMac != null ? kieuVuongMac : "")
                + "|" + (loaiHopDongId != null ? loaiHopDongId : "")
                + "|" + (khuVucId != null ? khuVucId : "")
                + "|" + (tinhThanhId != null ? tinhThanhId : "")
                + "|" + includeKieuCounts;
    }

    private Map<String, Long> buildCountsByKieu() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String kieu : VuongMacKieuHelper.BLOCKING_KIEUS) {
            counts.put(kieu, 0L);
        }
        for (String kieu : VuongMacKieuHelper.ALLOWING_KIEUS) {
            counts.put(kieu, 0L);
        }
        for (Object[] row : vuongMacRepository.countGroupByKieu()) {
            if (row[0] == null) {
                continue;
            }
            String kieu = VuongMacKieuHelper.normalize(row[0].toString());
            counts.put(kieu, toLong(row[1]));
        }
        return counts;
    }

    private static long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VuongMacResponse> danhSachAll(
            Boolean activeOnly, UUID loaiHopDongId, UUID khuVucId, boolean dangMoOnly,
            Integer quaHanNgay, LocalDate ngayBaoCao) {
        khuVucId = dataScopeService.resolveKhuVucFilter(khuVucId);
        return filterAndEnrich(
                null, activeOnly, false, null, null, dangMoOnly, null, null, loaiHopDongId, khuVucId, null,
                quaHanNgay, ngayBaoCao);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<VuongMacResponse> danhSachOpenLiteForTramTon(
            UUID loaiHopDongId, Integer quaHanNgay, LocalDate ngayBaoCao) {
        UUID khuVucId = dataScopeService.resolveKhuVucFilter(null);
        String cacheKey = "qua-han|" + loaiHopDongId + "|" + khuVucId + "|" + quaHanNgay + "|" + ngayBaoCao;
        Optional<List> cached = cacheService.get(VuongMacCacheNames.STATS, cacheKey, List.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        Instant nguongNgay = resolveNguongNgay(quaHanNgay, ngayBaoCao);
        List<VuongMac> filtered = vuongMacRepository.findFiltered(
                false, true, null, null, true, null, null,
                loaiHopDongId, khuVucId, null, nguongNgay);
        List<VuongMacResponse> result = filtered.stream().map(this::mapEntityLite).toList();
        cacheService.put(VuongMacCacheNames.STATS, cacheKey, result, cacheProperties.vuongMacRowTtl());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VuongMacResponse> enrichByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<VuongMac> entities = vuongMacRepository.findAllById(ids).stream()
                .filter(entity -> entity.getNgayXoa() == null)
                .toList();
        if (entities.isEmpty()) {
            return List.of();
        }
        return enrichAll(entities);
    }

    private VuongMacResponse mapEntityLite(VuongMac entity) {
        VuongMacResponse response = new VuongMacResponse();
        response.setId(entity.getId());
        response.setMa(entity.getMa());
        response.setDuLieuDoiTuongId(entity.getDuLieuDoiTuongId());
        response.setHopDongId(entity.getHopDongId());
        response.setGiaiDoan(entity.getGiaiDoan());
        response.setKieuVuongMac(entity.getKieuVuongMac());
        response.setCoTheBoSungSanLuong(entity.getCoTheBoSungSanLuong());
        response.setMoTa(entity.getMoTa());
        response.setMoTaDayDu(entity.getMoTaDayDu());
        response.setTrangThai(entity.getTrangThai());
        response.setTenNguoiBaoCao(entity.getTenNguoiBaoCao());
        response.setNgayTao(entity.getNgayTao());
        response.setHoatDong(entity.getHoatDong());
        return response;
    }

    /**
     * Lọc + enrich dùng chung cho list()/danhSach(). activeOnly/hopDongId/trangThai/giaiDoan/
     * loaiHopDongId đẩy xuống SQL (VuongMacRepository.findFiltered) để thu hẹp tập trước khi
     * enrich — chỉ region/province/keyword trên trường đã enrich (mã trạm, tên HĐ,...) mới lọc
     * tiếp trong Java vì là thuộc tính động (EAV), không có cột cố định để đưa vào SQL.
     * quaHanNgay/ngayBaoCao (cả 2 null = không lọc): chỉ giữ vướng mắc tạo TRƯỚC mốc
     * "ngayBaoCao - quaHanNgay" (strict) — dùng cho tab "Đang vướng mắc" của trạm tồn.
     */
    private List<VuongMacResponse> filterAndEnrich(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID hopDongId,
            String trangThai,
            boolean dangMoOnly,
            String giaiDoan,
            String kieuVuongMac,
            UUID loaiHopDongId,
            UUID khuVucId,
            UUID tinhThanhId,
            Integer quaHanNgay,
            LocalDate ngayBaoCao
    ) {
        String keyword = EntityFilter.normalizeSearch(search);
        String statusFilter = normalizeOptional(trangThai);
        String phaseFilter = normalizeOptional(giaiDoan);
        String kieuFilter = normalizeOptional(kieuVuongMac);
        boolean activeOnlyFlag = Boolean.TRUE.equals(activeOnly);
        Instant nguongNgay = resolveNguongNgay(quaHanNgay, ngayBaoCao);

        List<VuongMac> filtered = vuongMacRepository.findFiltered(
                includeDeleted, activeOnlyFlag, hopDongId, statusFilter, dangMoOnly, phaseFilter, kieuFilter,
                loaiHopDongId, khuVucId, tinhThanhId, nguongNgay
        );

        List<VuongMacResponse> responses = enrichAll(filtered.stream()
                .sorted(Comparator.comparing(VuongMac::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList());
        return responses.stream()
                .filter(row -> loaiHopDongId == null || loaiHopDongId.equals(row.getLoaiHopDongId()))
                .filter(row -> keyword == null || keyword.isBlank()
                        || EntityFilter.matchesKeyword(
                        keyword,
                        EntityFilter.nullToEmpty(row.getMa()),
                        EntityFilter.nullToEmpty(row.getMaTram()),
                        EntityFilter.nullToEmpty(row.getRegion()),
                        EntityFilter.nullToEmpty(row.getProvince()),
                        EntityFilter.nullToEmpty(row.getMaHopDong()),
                        EntityFilter.nullToEmpty(row.getTenHopDong()),
                        EntityFilter.nullToEmpty(row.getTenLoaiHopDong()),
                        EntityFilter.nullToEmpty(row.getTenNguoiXuLy()),
                        EntityFilter.nullToEmpty(row.getMoTa()),
                        EntityFilter.nullToEmpty(row.getTenNguoiBaoCao())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VuongMacResponse getById(UUID id) {
        return enrichOne(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VuongMacDoiTuongOptionResponse> timDoiTuong(String search, UUID hopDongId, int limit) {
        String keyword = EntityFilter.normalizeSearch(search);
        int max = Math.max(1, Math.min(limit, 50));

        List<HopDongDoiTuongResponse> doiTuongs = hopDongDoiTuongService.listAll(
                keyword, Boolean.TRUE, false, hopDongId, null, null, null, null);

        Map<UUID, HopDongObjectGeo> geoMap = doiTuongGroupSupport.resolveDisplayObjectGeos(doiTuongs).stream()
                .filter(geo -> geo.hopDongDoiTuongId() != null)
                .collect(Collectors.toMap(HopDongObjectGeo::hopDongDoiTuongId, geo -> geo, (a, b) -> a));

        Set<UUID> hopDongIds = doiTuongs.stream()
                .map(HopDongDoiTuongResponse::getHopDongId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, HopDong> hopDongMap = hopDongIds.isEmpty()
                ? Map.of()
                : hopDongRepository.findByIdInAndNgayXoaIsNull(hopDongIds).stream()
                        .collect(Collectors.toMap(HopDong::getId, hd -> hd, (a, b) -> a));

        return doiTuongs.stream()
                .limit(max)
                .map(doiTuong -> {
                    HopDongObjectGeo geo = geoMap.get(doiTuong.getId());
                    HopDong hopDong = doiTuong.getHopDongId() != null
                            ? hopDongMap.get(doiTuong.getHopDongId())
                            : null;
                    return VuongMacDoiTuongOptionResponse.builder()
                            .id(doiTuong.getId())
                            .hopDongId(doiTuong.getHopDongId())
                            .maTram(doiTuongGroupSupport.resolveMaTram(doiTuong))
                            .tenDoiTuong(doiTuong.getDoiTuongTen())
                            .maHopDong(hopDong != null ? hopDong.getMaHopDong() : null)
                            .tenHopDong(hopDong != null
                                    ? (hopDong.getTen() != null && !hopDong.getTen().isBlank()
                                            ? hopDong.getTen()
                                            : hopDong.getMaHopDong())
                                    : null)
                            .region(geo != null ? geo.region() : null)
                            .province(geo != null ? geo.province() : null)
                            .nhaThau(geo != null ? geo.contractor() : null)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public VuongMacResponse create(VuongMacTaoRequest request) {
        validateStatus(request.getTrangThai());
        validateRejectReason(request.getTrangThai(), request.getLyDoTuChoi());

        VuongMac entity = vuongMacMapper.fromVuongMacTaoRequest(request);
        String ma = request.getMa() == null || request.getMa().isBlank()
                ? generateMa()
                : EntityFilter.normalizeCode(request.getMa());
        entity.setMa(ma);
        if (request.getGiaiDoan() != null) {
            entity.setGiaiDoan(request.getGiaiDoan().trim());
        }
        entity.setKieuVuongMac(VuongMacKieuHelper.normalize(request.getKieuVuongMac()));
        entity.setCoTheBoSungSanLuong(Boolean.TRUE.equals(request.getCoTheBoSungSanLuong()));
        if (request.getMoTa() != null) {
            entity.setMoTa(request.getMoTa().trim());
        }
        if (request.getMoTaDayDu() != null) {
            entity.setMoTaDayDu(request.getMoTaDayDu().trim());
        }
        if (request.getTrangThai() != null && !request.getTrangThai().isBlank()) {
            entity.setTrangThai(request.getTrangThai().trim());
        } else {
            entity.setTrangThai("pending");
        }
        if (request.getGhiChuGiaiQuyet() != null) {
            entity.setGhiChuGiaiQuyet(request.getGhiChuGiaiQuyet().trim());
        }
        if (request.getLyDoTuChoi() != null) {
            entity.setLyDoTuChoi(request.getLyDoTuChoi().trim());
        }
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());
        fillReporterIfMissing(entity, request.getNguoiBaoCaoId(), request.getTenNguoiBaoCao());
        syncOverdue(entity);

        VuongMac saved = vuongMacRepository.save(entity);
        appendLichSu(saved, null, saved.getTrangThai(), saved.getGhiChuGiaiQuyet(), saved.getLyDoTuChoi(),
                saved.getNguoiXuLyId(), resolveNguoiXuLyTen(saved.getNguoiXuLyId()));
        applicationEventPublisher.publishEvent(
                VuongMacChangedEvent.of(saved.getId(), saved.getHopDongId(), saved.getDuLieuDoiTuongId()));
        return enrichOne(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VuongMacResponse> kiemTraTrung(VuongMacTrungKiemTraRequest request) {
        if (request.getDuLieuDoiTuongIds() == null || request.getDuLieuDoiTuongIds().isEmpty()) {
            return List.of();
        }
        if (request.getGiaiDoan() == null || request.getGiaiDoan().isBlank()) {
            return List.of();
        }
        List<VuongMac> found = vuongMacRepository.findActiveByDuLieuDoiTuongIdsAndGiaiDoan(
                request.getDuLieuDoiTuongIds(),
                request.getGiaiDoan().trim());
        return found.stream().map(this::enrichOne).toList();
    }

    @Override
    @Transactional
    public VuongMacResponse update(UUID id, VuongMacCapNhatRequest request) {
        VuongMac entity = findById(id);
        String trangThaiCu = entity.getTrangThai();
        String ghiChuCu = entity.getGhiChuGiaiQuyet();
        String lyDoCu = entity.getLyDoTuChoi();
        UUID nguoiXuLyCu = entity.getNguoiXuLyId();
        if (request.getTrangThai() != null) {
            validateStatus(request.getTrangThai());
            validateRejectReason(request.getTrangThai(), request.getLyDoTuChoi() != null
                    ? request.getLyDoTuChoi()
                    : entity.getLyDoTuChoi());
        }

        vuongMacMapper.updateFromVuongMacCapNhatRequest(request, entity);
        if (request.getMa() != null && !request.getMa().isBlank()) {
            entity.setMa(EntityFilter.normalizeCode(request.getMa()));
        }
        if (request.getGiaiDoan() != null) {
            entity.setGiaiDoan(request.getGiaiDoan().trim());
        }
        if (request.getKieuVuongMac() != null && !request.getKieuVuongMac().isBlank()) {
            entity.setKieuVuongMac(VuongMacKieuHelper.normalize(request.getKieuVuongMac()));
        }
        if (request.getCoTheBoSungSanLuong() != null) {
            entity.setCoTheBoSungSanLuong(request.getCoTheBoSungSanLuong());
        }
        if (request.getMoTa() != null) {
            entity.setMoTa(request.getMoTa().trim());
        }
        if (request.getMoTaDayDu() != null) {
            entity.setMoTaDayDu(request.getMoTaDayDu().trim());
        }
        if (request.getTenNguoiBaoCao() != null) {
            entity.setTenNguoiBaoCao(request.getTenNguoiBaoCao().trim());
        }
        if (request.getTrangThai() != null) {
            entity.setTrangThai(request.getTrangThai().trim());
        }
        if (request.getGhiChuGiaiQuyet() != null) {
            entity.setGhiChuGiaiQuyet(request.getGhiChuGiaiQuyet().trim());
        }
        if (request.getLyDoTuChoi() != null) {
            entity.setLyDoTuChoi(request.getLyDoTuChoi().trim());
        }
        syncOverdue(entity);

        VuongMac saved = vuongMacRepository.save(entity);
        boolean statusChanged = request.getTrangThai() != null && !Objects.equals(trangThaiCu, saved.getTrangThai());
        boolean noteChanged = request.getGhiChuGiaiQuyet() != null
                && !Objects.equals(trimToNull(ghiChuCu), trimToNull(saved.getGhiChuGiaiQuyet()));
        boolean rejectChanged = request.getLyDoTuChoi() != null
                && !Objects.equals(trimToNull(lyDoCu), trimToNull(saved.getLyDoTuChoi()));
        boolean resolverChanged = request.getNguoiXuLyId() != null
                && !Objects.equals(nguoiXuLyCu, saved.getNguoiXuLyId());
        if (statusChanged || noteChanged || rejectChanged || resolverChanged) {
            appendLichSu(saved, trangThaiCu, saved.getTrangThai(), saved.getGhiChuGiaiQuyet(), saved.getLyDoTuChoi(),
                    saved.getNguoiXuLyId(), resolveNguoiXuLyTen(saved.getNguoiXuLyId()));
        }
        applicationEventPublisher.publishEvent(
                VuongMacChangedEvent.of(saved.getId(), saved.getHopDongId(), saved.getDuLieuDoiTuongId()));
        return enrichOne(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VuongMacLichSuResponse> listLichSu(UUID id) {
        findById(id);
        return vuongMacLichSuRepository.findByVuongMacIdOrderByNgayTaoDesc(id).stream()
                .map(this::toLichSuResponse)
                .toList();
    }

    @Override
    @Transactional
    public VuongMacResponse uploadAnh(UUID id, MultipartFile file) {
        VuongMac entity = findById(id);
        TepDinhKemResponse tep = tepDinhKemService.upload(file, "vuong-mac");
        entity.setTepDinhKemId(tep.getId());
        return enrichOne(vuongMacRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        VuongMac entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        VuongMac saved = vuongMacRepository.save(entity);
        applicationEventPublisher.publishEvent(
                VuongMacChangedEvent.of(saved.getId(), saved.getHopDongId(), saved.getDuLieuDoiTuongId()));
    }

    private VuongMac findById(UUID id) {
        return vuongMacRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(VuongMacErrorCode.VUONG_MAC_NOT_FOUND, "Không tìm thấy bản ghi"));
    }

    private VuongMacResponse enrichOne(VuongMac entity) {
        return enrichAll(List.of(entity)).get(0);
    }

    /**
     * Cache-aside theo id: multiGet trước, chỉ tính bù (và cache lại) phần miss.
     * Nhờ vậy list() không filter theo tham số nào cũng luôn hit cache nóng sau lần đầu.
     */
    private List<VuongMacResponse> enrichAll(List<VuongMac> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        List<String> cacheKeys = entities.stream().map(e -> e.getId().toString()).toList();
        List<VuongMacResponse> cachedValues = cacheService.multiGet(
                VuongMacCacheNames.ROW, cacheKeys, VuongMacResponse.class);

        Map<UUID, VuongMacResponse> result = new HashMap<>();
        List<VuongMac> missing = new ArrayList<>();
        for (int i = 0; i < entities.size(); i++) {
            VuongMacResponse cached = cachedValues.get(i);
            if (cached != null) {
                result.put(entities.get(i).getId(), cached);
            } else {
                missing.add(entities.get(i));
            }
        }

        if (!missing.isEmpty()) {
            List<VuongMacResponse> computed = computeEnrichAll(missing);
            Map<String, Object> cacheUpdates = new HashMap<>();
            for (VuongMacResponse response : computed) {
                result.put(response.getId(), response);
                cacheUpdates.put(response.getId().toString(), response);
            }
            cacheService.multiPut(VuongMacCacheNames.ROW, cacheUpdates, cacheProperties.vuongMacRowTtl());
        }

        return entities.stream().map(e -> result.get(e.getId())).filter(Objects::nonNull).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VuongMacResponse refreshRow(UUID vuongMacId) {
        Optional<VuongMac> entity = vuongMacRepository.findByIdAndNgayXoaIsNull(vuongMacId);
        if (entity.isEmpty()) {
            cacheService.evict(VuongMacCacheNames.ROW, vuongMacId.toString());
            return null;
        }
        VuongMacResponse response = computeEnrichAll(List.of(entity.get())).get(0);
        cacheService.put(VuongMacCacheNames.ROW, vuongMacId.toString(), response, cacheProperties.vuongMacRowTtl());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public void refreshRowsByHopDongDoiTuongId(UUID hopDongDoiTuongId) {
        if (hopDongDoiTuongId == null) {
            return;
        }
        for (VuongMac entity : vuongMacRepository.findByDuLieuDoiTuongIdAndNgayXoaIsNull(hopDongDoiTuongId)) {
            refreshRow(entity.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VuongMacResponse> listByDoiTuongId(UUID doiTuongId, boolean dangMoOnly) {
        if (doiTuongId == null) {
            return List.of();
        }
        List<VuongMac> entities = vuongMacRepository.findByDuLieuDoiTuongIdAndNgayXoaIsNull(doiTuongId);
        if (entities.isEmpty()) {
            return List.of();
        }
        List<VuongMac> filtered = dangMoOnly
                ? entities.stream()
                        .filter(v -> v.getTrangThai() != null && TRANG_THAI_DANG_MO.contains(v.getTrangThai()))
                        .toList()
                : entities;
        if (filtered.isEmpty()) {
            return List.of();
        }
        return enrichAll(filtered);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VuongMacResponse> listByDoiTuongIdLite(UUID doiTuongId, boolean dangMoOnly) {
        if (doiTuongId == null) {
            return List.of();
        }
        List<VuongMac> entities = vuongMacRepository.findByDuLieuDoiTuongIdAndNgayXoaIsNull(doiTuongId);
        if (entities.isEmpty()) {
            return List.of();
        }
        List<VuongMac> filtered = dangMoOnly
                ? entities.stream()
                        .filter(v -> v.getTrangThai() != null && TRANG_THAI_DANG_MO.contains(v.getTrangThai()))
                        .toList()
                : entities;
        if (filtered.isEmpty()) {
            return List.of();
        }
        Set<UUID> nguoiXuLyIds = filtered.stream()
                .map(VuongMac::getNguoiXuLyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> nguoiXuLyTenMap = nguoiXuLyIds.isEmpty()
                ? Map.of()
                : nguoiDungRepository.findByIdInAndNgayXoaIsNull(nguoiXuLyIds).stream()
                        .collect(Collectors.toMap(NguoiDung::getId, NguoiDung::getHoTen, (a, b) -> a));
        return filtered.stream().map(entity -> {
            VuongMacResponse response = mapEntityLite(entity);
            if (entity.getNguoiXuLyId() != null) {
                response.setTenNguoiXuLy(nguoiXuLyTenMap.get(entity.getNguoiXuLyId()));
            }
            return response;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public void evictRowsByHopDongId(UUID hopDongId) {
        if (hopDongId == null) {
            return;
        }
        for (VuongMac entity : vuongMacRepository.findByHopDongIdAndNgayXoaIsNull(hopDongId)) {
            cacheService.evict(VuongMacCacheNames.ROW, entity.getId().toString());
        }
    }

    @Override
    public void evictTongQuanCache() {
        cacheService.evictAll(VuongMacCacheNames.STATS);
    }

    private List<VuongMacResponse> computeEnrichAll(List<VuongMac> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        Set<UUID> doiTuongIds = entities.stream()
                .map(VuongMac::getDuLieuDoiTuongId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<UUID> hopDongIds = entities.stream()
                .map(VuongMac::getHopDongId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        Set<UUID> nguoiXuLyIds = entities.stream()
                .map(VuongMac::getNguoiXuLyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<UUID> tepIds = entities.stream()
                .map(VuongMac::getTepDinhKemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, HopDongDoiTuongSnapshot> snapshotByDoiTuongId = doiTuongIds.isEmpty()
                ? Map.of()
                : hopDongDoiTuongSnapshotService.getSnapshots(doiTuongIds);

        for (HopDongDoiTuongSnapshot snapshot : snapshotByDoiTuongId.values()) {
            if (snapshot.getHopDongId() != null) {
                hopDongIds.add(snapshot.getHopDongId());
            }
        }

        Map<UUID, HopDong> hopDongMap = hopDongIds.isEmpty()
                ? Map.of()
                : hopDongRepository.findByIdInAndNgayXoaIsNull(hopDongIds).stream()
                        .collect(Collectors.toMap(HopDong::getId, hd -> hd, (a, b) -> a));

        Set<UUID> loaiIds = hopDongMap.values().stream()
                .map(HopDong::getLoaiHopDongId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> loaiTenMap = loaiIds.isEmpty()
                ? Map.of()
                : loaiHopDongRepository.findAllById(loaiIds).stream()
                        .collect(Collectors.toMap(LoaiHopDong::getId, LoaiHopDong::getTen, (a, b) -> a));

        Map<UUID, String> nguoiXuLyTenMap = nguoiXuLyIds.isEmpty()
                ? Map.of()
                : nguoiDungRepository.findByIdInAndNgayXoaIsNull(nguoiXuLyIds).stream()
                        .collect(Collectors.toMap(NguoiDung::getId, NguoiDung::getHoTen, (a, b) -> a));

        Map<UUID, String> anhUrlMap = new HashMap<>();
        if (!tepIds.isEmpty()) {
            for (Map.Entry<UUID, TepDinhKemResponse> entry : tepDinhKemService.getByIds(tepIds).entrySet()) {
                TepDinhKemResponse tep = entry.getValue();
                String url = tep.getUrl() != null && !tep.getUrl().isBlank() ? tep.getUrl() : tep.getDuongDan();
                if (url != null && !url.isBlank()) {
                    anhUrlMap.put(entry.getKey(), url);
                }
            }
        }

        return entities.stream().map(entity -> {
            VuongMacResponse response = vuongMacMapper.toVuongMacResponse(entity);
            boolean overdue = computeOverdue(entity);
            response.setQuaHan30Ngay(overdue);
            String kieu = VuongMacKieuHelper.normalize(entity.getKieuVuongMac());
            response.setKieuVuongMac(kieu);
            response.setCoTheBoSungSanLuong(Boolean.TRUE.equals(entity.getCoTheBoSungSanLuong()));

            HopDongDoiTuongSnapshot snapshot = entity.getDuLieuDoiTuongId() != null
                    ? snapshotByDoiTuongId.get(entity.getDuLieuDoiTuongId())
                    : null;
            if (snapshot != null) {
                String maDoiTuong = snapshot.getMaDoiTuong();
                response.setMaTram(maDoiTuong != null && !maDoiTuong.isBlank() ? maDoiTuong : "—");
                response.setMaDoiTuong(maDoiTuong);
                response.setRegion(snapshot.getKhuVuc() != null && !snapshot.getKhuVuc().isBlank()
                        ? snapshot.getKhuVuc()
                        : "—");
                response.setProvince(snapshot.getTinh() != null && !snapshot.getTinh().isBlank()
                        ? snapshot.getTinh()
                        : "—");
                response.setKhuVuc(GeoRefResponse.of(null, null, snapshot.getKhuVuc()));
                response.setTinh(GeoRefResponse.of(null, null, snapshot.getTinh()));
                response.setNhaThau(snapshot.getNhaThau() != null && !snapshot.getNhaThau().isBlank()
                        ? snapshot.getNhaThau()
                        : "—");
                if (snapshot.getTenHopDong() != null && !snapshot.getTenHopDong().isBlank()) {
                    response.setTenHopDong(snapshot.getTenHopDong());
                }
                if (snapshot.getLoaiHopDong() != null && !snapshot.getLoaiHopDong().isBlank()) {
                    response.setTenLoaiHopDong(snapshot.getLoaiHopDong());
                }
                if (response.getHopDongId() == null && snapshot.getHopDongId() != null) {
                    response.setHopDongId(snapshot.getHopDongId());
                }
            } else {
                response.setMaTram("—");
            }

            UUID hopDongId = response.getHopDongId();
            HopDong hopDong = hopDongId != null ? hopDongMap.get(hopDongId) : null;
            if (hopDong != null) {
                response.setMaHopDong(hopDong.getMaHopDong());
                if (response.getTenHopDong() == null || response.getTenHopDong().isBlank()) {
                    response.setTenHopDong(hopDong.getTen() != null && !hopDong.getTen().isBlank()
                            ? hopDong.getTen()
                            : hopDong.getMaHopDong());
                }
                response.setLoaiHopDongId(hopDong.getLoaiHopDongId());
                if (response.getTenLoaiHopDong() == null || response.getTenLoaiHopDong().isBlank()) {
                    if (hopDong.getLoaiHopDongId() != null) {
                        response.setTenLoaiHopDong(loaiTenMap.get(hopDong.getLoaiHopDongId()));
                    }
                }
            }

            if (entity.getNguoiXuLyId() != null) {
                response.setTenNguoiXuLy(nguoiXuLyTenMap.get(entity.getNguoiXuLyId()));
            }
            if (entity.getTepDinhKemId() != null) {
                response.setAnhUrl(anhUrlMap.get(entity.getTepDinhKemId()));
            }
            return response;
        }).toList();
    }

    private void fillReporterIfMissing(VuongMac entity, UUID requestReporterId, String requestReporterName) {
        if (entity.getNguoiBaoCaoId() == null && requestReporterId != null) {
            entity.setNguoiBaoCaoId(requestReporterId);
        }
        if (entity.getTenNguoiBaoCao() == null || entity.getTenNguoiBaoCao().isBlank()) {
            if (requestReporterName != null && !requestReporterName.isBlank()) {
                entity.setTenNguoiBaoCao(requestReporterName.trim());
            }
        }
        if (entity.getNguoiBaoCaoId() != null
                && (entity.getTenNguoiBaoCao() == null || entity.getTenNguoiBaoCao().isBlank())) {
            nguoiDungRepository.findById(entity.getNguoiBaoCaoId()).ifPresent(user ->
                    entity.setTenNguoiBaoCao(user.getHoTen()));
        }
        if (entity.getNguoiBaoCaoId() == null
                || entity.getTenNguoiBaoCao() == null
                || entity.getTenNguoiBaoCao().isBlank()) {
            JwtUserPrincipal current = SecurityContextHelper.currentUserOrNull();
            if (current != null) {
                if (entity.getNguoiBaoCaoId() == null) {
                    entity.setNguoiBaoCaoId(current.id());
                }
                if (entity.getTenNguoiBaoCao() == null || entity.getTenNguoiBaoCao().isBlank()) {
                    entity.setTenNguoiBaoCao(current.hoTen());
                }
            }
        }
    }

    private void validateStatus(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) {
            return;
        }
        if (!ALLOWED_STATUSES.contains(trangThai.trim())) {
            throw new AppException(
                    VuongMacErrorCode.VUONG_MAC_INVALID,
                    "Trạng thái không hợp lệ (pending|in_progress|resolved|rejected)");
        }
    }

    private void validateRejectReason(String trangThai, String lyDoTuChoi) {
        if ("rejected".equals(trangThai) && (lyDoTuChoi == null || lyDoTuChoi.isBlank())) {
            throw new AppException(VuongMacErrorCode.VUONG_MAC_INVALID, "Từ chối bắt buộc nhập lý do");
        }
    }

    private void syncOverdue(VuongMac entity) {
        entity.setQuaHan30Ngay(computeOverdue(entity));
    }

    private boolean computeOverdue(VuongMac entity) {
        String status = entity.getTrangThai();
        if (status == null || !TRANG_THAI_DANG_MO.contains(status)) {
            return false;
        }
        Instant ngayTao = entity.getNgayTao();
        if (ngayTao == null) {
            return Boolean.TRUE.equals(entity.getQuaHan30Ngay());
        }
        return ngayTao.isBefore(Instant.now().minus(30, ChronoUnit.DAYS));
    }

    private String generateMa() {
        int seq = MA_SEQ.incrementAndGet() % 1000;
        return "VM-" + MA_DATE.format(Instant.now()) + "-" + String.format("%03d", seq)
                + "-" + (System.currentTimeMillis() % 100000);
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank() || "all".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.trim();
    }

    /** null nếu quaHanNgay không truyền (không lọc) — ngược lại "ngayBaoCao - quaHanNgay" (đầu ngày). */
    /** Mốc rất xa tương lai (còn trong giới hạn timestamp của Postgres, khác Instant.MAX tràn số) — dùng khi không lọc. */
    private static final Instant NO_NGUONG_NGAY = LocalDate.of(9999, 1, 1).atStartOfDay(ZONE).toInstant();

    /**
     * LUÔN trả về non-null — mốc rất xa tương lai khi không lọc (mọi v.ngayTao đều nhỏ hơn nên
     * không loại dòng nào) để tránh Hibernate suy sai kiểu tham số null (xem javadoc
     * VuongMacRepository.findFiltered).
     */
    private static Instant resolveNguongNgay(Integer quaHanNgay, LocalDate ngayBaoCao) {
        if (quaHanNgay == null || quaHanNgay < 0) {
            return NO_NGUONG_NGAY;
        }
        LocalDate reportDate = ngayBaoCao != null ? ngayBaoCao : LocalDate.now(ZONE);
        return reportDate.minusDays(quaHanNgay).atStartOfDay(ZONE).toInstant();
    }

    private void appendLichSu(
            VuongMac entity,
            String trangThaiCu,
            String trangThaiMoi,
            String ghiChu,
            String lyDoTuChoi,
            UUID nguoiXuLyId,
            String nguoiXuLyTen) {
        JwtUserPrincipal current = SecurityContextHelper.currentUserOrNull();
        VuongMacLichSu row = new VuongMacLichSu();
        row.setVuongMacId(entity.getId());
        row.setTrangThaiCu(trangThaiCu);
        row.setTrangThaiMoi(trangThaiMoi);
        row.setGhiChu(trimToNull(ghiChu));
        row.setLyDoTuChoi(trimToNull(lyDoTuChoi));
        row.setNguoiXuLyId(nguoiXuLyId);
        row.setNguoiXuLyTen(nguoiXuLyTen);
        if (current != null) {
            row.setNguoiThucHienId(current.id());
            row.setNguoiThucHienTen(current.hoTen());
        }
        vuongMacLichSuRepository.save(row);
    }

    private VuongMacLichSuResponse toLichSuResponse(VuongMacLichSu entity) {
        return VuongMacLichSuResponse.builder()
                .id(entity.getId())
                .vuongMacId(entity.getVuongMacId())
                .trangThaiCu(entity.getTrangThaiCu())
                .trangThaiMoi(entity.getTrangThaiMoi())
                .ghiChu(entity.getGhiChu())
                .lyDoTuChoi(entity.getLyDoTuChoi())
                .nguoiXuLyId(entity.getNguoiXuLyId())
                .nguoiXuLyTen(entity.getNguoiXuLyTen())
                .nguoiThucHienId(entity.getNguoiThucHienId())
                .nguoiThucHienTen(entity.getNguoiThucHienTen())
                .ngayTao(entity.getNgayTao())
                .build();
    }

    private String resolveNguoiXuLyTen(UUID nguoiXuLyId) {
        if (nguoiXuLyId == null) {
            return null;
        }
        return nguoiDungRepository.findById(nguoiXuLyId)
                .map(NguoiDung::getHoTen)
                .orElse(null);
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
