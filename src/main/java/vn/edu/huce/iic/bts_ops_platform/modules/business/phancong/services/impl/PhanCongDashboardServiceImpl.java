package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScope;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScopeService;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.PaginationDefaults;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.common.util.UuidUtils;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.request.PhanCongGanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response.*;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.entity.PhanCong;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.entity.PhanCongLichSu;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.helpers.PhanCongGiaiDoanHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.repository.PhanCongDashboardQueryRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.repository.PhanCongLichSuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.repository.PhanCongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.constants.ThongBaoLoai;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.services.ThongBaoDispatchService;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.services.PhanCongDashboardService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.TrangThaiHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.TrangThaiHopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.HopDongDoiTuongSnapshot;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongSnapshotService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.entity.NguoiDung;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.repository.NguoiDungRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.KhuVuc;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.TinhThanh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.KhuVucRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.TinhThanhRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
public class PhanCongDashboardServiceImpl implements PhanCongDashboardService {

    private static final String UNASSIGNED_KEY = "__unassigned__";
    private static final Duration ROW_CACHE_TTL = Duration.ofSeconds(30);

    private final ConcurrentHashMap<String, CachedAssignmentRows> filteredRowCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> filteredRowCacheLocks = new ConcurrentHashMap<>();

    private final HopDongDoiTuongRepository hopDongDoiTuongRepository;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final HopDongDoiTuongSnapshotService snapshotService;
    private final HopDongRepository hopDongRepository;
    private final TrangThaiHopDongService trangThaiHopDongService;
    private final NguoiDungRepository nguoiDungRepository;
    private final KhuVucRepository khuVucRepository;
    private final TinhThanhRepository tinhThanhRepository;
    private final PhanCongRepository phanCongRepository;
    private final PhanCongLichSuRepository phanCongLichSuRepository;
    private final PhanCongDashboardQueryRepository dashboardQueryRepository;
    private final ContractorScopeService contractorScopeService;
    private final ThongBaoDispatchService thongBaoDispatchService;

    private record CachedAssignmentRows(List<AssignmentRow> rows, Instant expiresAt) {}

    private record AssignmentRow(
            UUID id,
            UUID hopDongId,
            UUID nhaThauId,
            UUID trangThaiHopDongId,
            UUID khuVucId,
            UUID tinhThanhId,
            LocalDate ngayHtTc,
            boolean coVuongMacMo,
            BigDecimal quyetToanThuc,
            BigDecimal sanLuongHieuLuc) {

        AssignmentRow withResolvedNhaThauId(UUID resolvedId) {
            UUID effective = resolvedId != null ? resolvedId : nhaThauId;
            return new AssignmentRow(
                    id, hopDongId, effective, trangThaiHopDongId, khuVucId, tinhThanhId,
                    ngayHtTc, coVuongMacMo, quyetToanThuc, sanLuongHieuLuc);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PhanCongBoLocResponse boLoc() {
        PhanCongDashboardQueryRepository.FilterParams filter = buildFilterParams(null, null, null, null, null);
        Map<UUID, Long> contractorCounts = toCountMap(dashboardQueryRepository.countGroupByNhaThau(filter));
        Map<UUID, Long> regionCounts = toCountMap(dashboardQueryRepository.countGroupByMaVung(filter));
        Map<UUID, Long> hopDongCounts = toCountMap(dashboardQueryRepository.countGroupByHopDong(filter));

        Map<String, Long> phaseCounts = new LinkedHashMap<>();
        for (Object[] row : dashboardQueryRepository.countGroupByGiaiDoan(filter)) {
            UUID statusId = toUuid(row[0]);
            long count = toLong(row[1]);
            String phaseKey = statusId != null ? statusId.toString() : UNASSIGNED_KEY;
            phaseCounts.merge(phaseKey, count, Long::sum);
        }

        Set<UUID> statusIds = phaseCounts.keySet().stream()
                .filter(key -> !UNASSIGNED_KEY.equals(key))
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        Map<UUID, TrangThaiHopDongResponse> statusMap = statusIds.isEmpty()
                ? Map.of()
                : trangThaiHopDongService.getByIds(statusIds).stream()
                        .collect(Collectors.toMap(TrangThaiHopDongResponse::getId, Function.identity(), (a, b) -> a));

        Map<UUID, NguoiDung> contractorMap = contractorCounts.isEmpty()
                ? Map.of()
                : nguoiDungRepository.findByIdInAndNgayXoaIsNull(contractorCounts.keySet()).stream()
                        .collect(Collectors.toMap(NguoiDung::getId, Function.identity(), (a, b) -> a));
        Map<UUID, KhuVuc> khuVucMap = regionCounts.isEmpty()
                ? Map.of()
                : khuVucRepository.findByIdInAndNgayXoaIsNull(regionCounts.keySet()).stream()
                        .collect(Collectors.toMap(KhuVuc::getId, Function.identity(), (a, b) -> a));

        List<PhanCongFilterOption> nhaThau = contractorCounts.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .map(entry -> {
                    NguoiDung user = contractorMap.get(entry.getKey());
                    return PhanCongFilterOption.builder()
                            .value(entry.getKey().toString())
                            .label(formatContractorLabel(user))
                            .count(entry.getValue())
                            .build();
                })
                .toList();

        List<PhanCongFilterOption> giaiDoan = phaseCounts.entrySet().stream()
                .map(entry -> {
                    if (UNASSIGNED_KEY.equals(entry.getKey())) {
                        return PhanCongFilterOption.builder()
                                .value(UNASSIGNED_KEY)
                                .label("Chưa xác định giai đoạn")
                                .count(entry.getValue())
                                .build();
                    }
                    UUID statusId = UUID.fromString(entry.getKey());
                    TrangThaiHopDongResponse status = statusMap.get(statusId);
                    return PhanCongFilterOption.builder()
                            .value(statusId.toString())
                            .label(status != null ? status.getTen() : statusId.toString())
                            .count(entry.getValue())
                            .build();
                })
                .toList();

        List<PhanCongFilterOption> maVung = regionCounts.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .map(entry -> {
                    KhuVuc khuVuc = khuVucMap.get(entry.getKey());
                    return PhanCongFilterOption.builder()
                            .value(entry.getKey().toString())
                            .label(khuVuc != null ? khuVuc.getMa() + " — " + khuVuc.getTen() : entry.getKey().toString())
                            .count(entry.getValue())
                            .build();
                })
                .toList();

        var hopDongs = hopDongRepository.findByIdInAndNgayXoaIsNull(hopDongCounts.keySet());
        Map<UUID, String> hopDongMa = hopDongs.stream()
                .collect(Collectors.toMap(h -> h.getId(), h -> h.getMaHopDong(), (a, b) -> a));

        List<PhanCongFilterOption> hopDong = hopDongCounts.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .map(entry -> PhanCongFilterOption.builder()
                        .value(entry.getKey().toString())
                        .label(hopDongMa.getOrDefault(entry.getKey(), entry.getKey().toString()))
                        .count(entry.getValue())
                        .build())
                .toList();

        return PhanCongBoLocResponse.builder()
                .nhaThau(nhaThau)
                .giaiDoan(giaiDoan)
                .maVung(maVung)
                .hopDong(hopDong)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PhanCongTongQuanResponse tongQuan(
            UUID hopDongId, UUID nhaThauId, String giaiDoan, UUID khuVucId, String search) {
        if (EntityFilter.trimToNull(search) != null) {
            List<AssignmentRow> rows = loadFilteredRows(hopDongId, nhaThauId, giaiDoan, khuVucId, search);
            Map<UUID, TrangThaiHopDongResponse> statusMap = loadStatusMap(rows);
            long tongTram = rows.size();
            long daPhanNhaThau = rows.stream().filter(r -> r.nhaThauId() != null).count();
            long hoanThanh = rows.stream()
                    .filter(r -> isCompleted(r, statusMap.get(r.trangThaiHopDongId())))
                    .count();
            long vuongMac = rows.stream().filter(AssignmentRow::coVuongMacMo).count();
            long ton = rows.stream().filter(r -> isTon(r, statusMap.get(r.trangThaiHopDongId()))).count();
            long coGiaiDoan = rows.stream().filter(r -> r.trangThaiHopDongId() != null).count();
            Set<UUID> contractorIds = rows.stream()
                    .map(AssignmentRow::nhaThauId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            return buildTongQuanResponse(tongTram, daPhanNhaThau, contractorIds.size(), coGiaiDoan, hoanThanh, vuongMac, ton);
        }

        PhanCongDashboardQueryRepository.FilterParams filter = buildFilterParams(hopDongId, nhaThauId, giaiDoan, khuVucId, null);
        PhanCongDashboardQueryRepository.StatusMetrics metrics = PhanCongDashboardQueryRepository.StatusMetrics.empty();
        PhanCongDashboardQueryRepository.TongQuanAggregate aggregate = dashboardQueryRepository.aggregateTongQuan(filter, metrics);
        return buildTongQuanResponse(
                aggregate.tongTram(),
                aggregate.daPhanNhaThau(),
                aggregate.soNhaThau(),
                aggregate.coGiaiDoan(),
                aggregate.hoanThanh(),
                aggregate.vuongMac(),
                aggregate.ton());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanCongNhaThauItemResponse> theoNhaThau(
            UUID hopDongId, UUID nhaThauId, String giaiDoan, UUID khuVucId, String search) {
        if (EntityFilter.trimToNull(search) != null) {
            List<AssignmentRow> rows = loadFilteredRows(hopDongId, nhaThauId, giaiDoan, khuVucId, search);
            Map<UUID, TrangThaiHopDongResponse> statusMap = loadStatusMap(rows);
            Map<UUID, NguoiDung> contractorMap = loadContractorMap(rows);
            Map<UUID, KhuVuc> khuVucMap = loadKhuVucMap(rows);
            Map<UUID, List<AssignmentRow>> grouped = rows.stream()
                    .filter(r -> r.nhaThauId() != null)
                    .collect(Collectors.groupingBy(AssignmentRow::nhaThauId));
            return grouped.entrySet().stream()
                    .map(entry -> buildNhaThauItem(entry.getKey(), entry.getValue(), contractorMap, khuVucMap, statusMap))
                    .sorted(Comparator.comparing(PhanCongNhaThauItemResponse::getSoTram).reversed())
                    .toList();
        }

        PhanCongDashboardQueryRepository.FilterParams filter = buildFilterParams(hopDongId, nhaThauId, giaiDoan, khuVucId, null);
        List<Object[]> grouped = dashboardQueryRepository.countGroupByNhaThau(filter);
        Set<UUID> contractorIds = grouped.stream()
                .map(row -> toUuid(row[0]))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, NguoiDung> contractorMap = contractorIds.isEmpty()
                ? Map.of()
                : nguoiDungRepository.findByIdInAndNgayXoaIsNull(contractorIds).stream()
                        .collect(Collectors.toMap(NguoiDung::getId, Function.identity(), (a, b) -> a));

        return grouped.stream()
                .map(row -> {
                    UUID nhaThauIdValue = toUuid(row[0]);
                    if (nhaThauIdValue == null) {
                        return null;
                    }
                    long soTram = toLong(row[1]);
                    NguoiDung contractor = contractorMap.get(nhaThauIdValue);
                    String labelKey = contractor != null && contractor.getHoTen() != null
                            ? contractor.getHoTen()
                            : nhaThauIdValue.toString();
                    return PhanCongNhaThauItemResponse.builder()
                            .nhaThauId(nhaThauIdValue)
                            .ten(formatContractorLabel(contractor))
                            .tenDangNhap(contractor != null ? contractor.getTenDangNhap() : null)
                            .maVung("—")
                            .mauSac(PhanCongGiaiDoanHelper.colorForKey(labelKey))
                            .soTram(soTram)
                            .hoanThanh(0L)
                            .vuongMac(0L)
                            .ton(0L)
                            .sanLuong(BigDecimal.ZERO)
                            .tienDoPercent(0D)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanCongGiaiDoanItemResponse> theoGiaiDoan(
            UUID hopDongId, UUID nhaThauId, String giaiDoan, UUID khuVucId, String search) {
        if (EntityFilter.trimToNull(search) != null) {
            List<AssignmentRow> rows = loadFilteredRows(hopDongId, nhaThauId, giaiDoan, khuVucId, search);
            Map<UUID, TrangThaiHopDongResponse> statusMap = loadStatusMap(rows);
            Map<String, List<AssignmentRow>> grouped = rows.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.trangThaiHopDongId() != null
                                    ? r.trangThaiHopDongId().toString()
                                    : UNASSIGNED_KEY,
                            LinkedHashMap::new,
                            Collectors.toList()));
            List<PhanCongGiaiDoanItemResponse> result = new ArrayList<>();
            for (Map.Entry<String, List<AssignmentRow>> entry : grouped.entrySet()) {
                String key = entry.getKey();
                List<AssignmentRow> groupRows = entry.getValue();
                TrangThaiHopDongResponse status = UNASSIGNED_KEY.equals(key)
                        ? null
                        : statusMap.get(UUID.fromString(key));
                long daPhan = groupRows.stream().filter(r -> r.nhaThauId() != null).count();
                long tong = groupRows.size();
                result.add(buildGiaiDoanItem(key, status, tong, daPhan));
            }
            result.sort(Comparator.comparing(PhanCongGiaiDoanItemResponse::getTong).reversed());
            return result;
        }

        PhanCongDashboardQueryRepository.FilterParams filter = buildFilterParams(hopDongId, nhaThauId, giaiDoan, khuVucId, null);
        List<PhanCongDashboardQueryRepository.GiaiDoanAggregate> aggregates = dashboardQueryRepository.aggregateTheoGiaiDoan(filter);
        Set<UUID> statusIds = aggregates.stream()
                .map(PhanCongDashboardQueryRepository.GiaiDoanAggregate::trangThaiId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, TrangThaiHopDongResponse> statusMap = statusIds.isEmpty()
                ? Map.of()
                : trangThaiHopDongService.getByIds(statusIds).stream()
                        .collect(Collectors.toMap(TrangThaiHopDongResponse::getId, Function.identity(), (a, b) -> a));

        return aggregates.stream()
                .map(item -> {
                    String key = item.trangThaiId() != null ? item.trangThaiId().toString() : UNASSIGNED_KEY;
                    TrangThaiHopDongResponse status = item.trangThaiId() != null ? statusMap.get(item.trangThaiId()) : null;
                    return buildGiaiDoanItem(key, status, item.tong(), item.daPhan());
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanCongMaVungItemResponse> theoMaVung(
            UUID hopDongId, UUID nhaThauId, String giaiDoan, UUID khuVucId, String search) {
        if (EntityFilter.trimToNull(search) != null) {
            List<AssignmentRow> rows = loadFilteredRows(hopDongId, nhaThauId, giaiDoan, khuVucId, search);
            Map<UUID, KhuVuc> khuVucMap = loadKhuVucMap(rows);
            Map<UUID, TinhThanh> tinhMap = loadTinhMap(rows);
            Map<UUID, NguoiDung> contractorMap = loadContractorMap(rows);
            Map<UUID, List<AssignmentRow>> grouped = rows.stream()
                    .filter(r -> r.khuVucId() != null)
                    .collect(Collectors.groupingBy(AssignmentRow::khuVucId));
            return grouped.entrySet().stream()
                    .map(entry -> {
                        KhuVuc khuVuc = khuVucMap.get(entry.getKey());
                        UUID dominantContractor = findDominantContractor(entry.getValue());
                        NguoiDung contractor = dominantContractor != null ? contractorMap.get(dominantContractor) : null;
                        String tinhLabel = entry.getValue().stream()
                                .map(AssignmentRow::tinhThanhId)
                                .filter(Objects::nonNull)
                                .map(tinhMap::get)
                                .filter(Objects::nonNull)
                                .map(TinhThanh::getTen)
                                .findFirst()
                                .orElse("—");
                        return PhanCongMaVungItemResponse.builder()
                                .khuVucId(entry.getKey())
                                .maVung(khuVuc != null ? khuVuc.getMa() : "—")
                                .tenKhuVuc(khuVuc != null ? khuVuc.getTen() : "—")
                                .tinhThanh(tinhLabel)
                                .soTram(entry.getValue().size())
                                .nhaThauChinh(formatContractorLabel(contractor))
                                .nhaThauChinhId(dominantContractor)
                                .build();
                    })
                    .sorted(Comparator.comparing(PhanCongMaVungItemResponse::getSoTram).reversed())
                    .toList();
        }

        PhanCongDashboardQueryRepository.FilterParams filter = buildFilterParams(hopDongId, nhaThauId, giaiDoan, khuVucId, null);
        List<PhanCongDashboardQueryRepository.MaVungAggregate> aggregates =
                dashboardQueryRepository.aggregateTheoMaVung(filter);
        Set<UUID> khuVucIds = aggregates.stream()
                .map(PhanCongDashboardQueryRepository.MaVungAggregate::khuVucId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<UUID> contractorIds = aggregates.stream()
                .map(PhanCongDashboardQueryRepository.MaVungAggregate::nhaThauChinhId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<UUID> tinhIds = aggregates.stream()
                .map(PhanCongDashboardQueryRepository.MaVungAggregate::sampleTinhThanhId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, KhuVuc> khuVucMap = khuVucIds.isEmpty()
                ? Map.of()
                : khuVucRepository.findByIdInAndNgayXoaIsNull(khuVucIds).stream()
                        .collect(Collectors.toMap(KhuVuc::getId, Function.identity(), (a, b) -> a));
        Map<UUID, NguoiDung> contractorMap = contractorIds.isEmpty()
                ? Map.of()
                : nguoiDungRepository.findByIdInAndNgayXoaIsNull(contractorIds).stream()
                        .collect(Collectors.toMap(NguoiDung::getId, Function.identity(), (a, b) -> a));
        Map<UUID, TinhThanh> tinhMap = tinhIds.isEmpty()
                ? Map.of()
                : tinhThanhRepository.findAllById(tinhIds).stream()
                        .filter(t -> t.getNgayXoa() == null)
                        .collect(Collectors.toMap(TinhThanh::getId, Function.identity(), (a, b) -> a));

        return aggregates.stream()
                .map(item -> {
                    UUID khuVucIdValue = item.khuVucId();
                    if (khuVucIdValue == null) {
                        return null;
                    }
                    KhuVuc khuVuc = khuVucMap.get(khuVucIdValue);
                    NguoiDung contractor =
                            item.nhaThauChinhId() != null ? contractorMap.get(item.nhaThauChinhId()) : null;
                    TinhThanh tinh =
                            item.sampleTinhThanhId() != null ? tinhMap.get(item.sampleTinhThanhId()) : null;
                    return PhanCongMaVungItemResponse.builder()
                            .khuVucId(khuVucIdValue)
                            .maVung(khuVuc != null ? khuVuc.getMa() : "—")
                            .tenKhuVuc(khuVuc != null ? khuVuc.getTen() : "—")
                            .tinhThanh(tinh != null ? tinh.getTen() : "—")
                            .soTram(item.soTram())
                            .nhaThauChinh(formatContractorLabel(contractor))
                            .nhaThauChinhId(item.nhaThauChinhId())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PhanCongTramItemResponse> danhSachTram(
            UUID hopDongId,
            UUID nhaThauId,
            String giaiDoan,
            UUID khuVucId,
            String search,
            Integer page,
            Integer size) {
        int pageNumber = EntityFilter.normalizePage(page);
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE);

        if (EntityFilter.trimToNull(search) != null) {
            List<AssignmentRow> rows = loadFilteredRows(hopDongId, nhaThauId, giaiDoan, khuVucId, search);
            int from = Math.min(pageNumber * pageSize, rows.size());
            int to = Math.min(from + pageSize, rows.size());
            List<AssignmentRow> pageRows = rows.subList(from, to);
            return buildTramPageResponse(pageRows, rows.size(), pageNumber, pageSize);
        }

        PhanCongDashboardQueryRepository.FilterParams filter = buildFilterParams(hopDongId, nhaThauId, giaiDoan, khuVucId, null);
        long total = dashboardQueryRepository.countTram(filter);
        List<PhanCongDashboardQueryRepository.TramRow> pageRows = dashboardQueryRepository.findTramPage(
                filter, pageNumber * pageSize, pageSize);
        List<AssignmentRow> rows = pageRows.stream()
                .map(row -> new AssignmentRow(
                        row.id(), row.hopDongId(), row.nhaThauId(), row.trangThaiHopDongId(),
                        row.khuVucId(), row.tinhThanhId(), row.ngayHtTc(), row.coVuongMacMo(),
                        row.quyetToanThuc(), Optional.ofNullable(row.sanLuongHieuLuc()).orElse(BigDecimal.ZERO)))
                .toList();
        return buildTramPageResponse(rows, total, pageNumber, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PhanCongLichSuResponse> lichSu(
            UUID hopDongId, UUID hopDongDoiTuongId, UUID nhaThauId, Integer page, Integer size) {
        int pageNumber = EntityFilter.normalizePage(page);
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE);
        var pageable = org.springframework.data.domain.PageRequest.of(pageNumber, pageSize);

        var result = phanCongLichSuRepository.searchHistory(hopDongId, hopDongDoiTuongId, nhaThauId, pageable);
        Set<UUID> doiTuongIds = result.getContent().stream()
                .map(PhanCongLichSu::getHopDongDoiTuongId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, HopDongDoiTuongSnapshot> snapshots = snapshotService.getSnapshots(doiTuongIds);

        List<PhanCongLichSuResponse> items = result.getContent().stream()
                .map(item -> PhanCongLichSuResponse.builder()
                        .id(item.getId())
                        .hopDongId(item.getHopDongId())
                        .hopDongDoiTuongId(item.getHopDongDoiTuongId())
                        .maTram(Optional.ofNullable(snapshots.get(item.getHopDongDoiTuongId()))
                                .map(HopDongDoiTuongSnapshot::getMaDoiTuong)
                                .orElse("—"))
                        .nhaThauCuId(item.getNhaThauCuId())
                        .nhaThauMoiId(item.getNhaThauMoiId())
                        .nhaThauCuTen(item.getNhaThauCuTen())
                        .nhaThauMoiTen(item.getNhaThauMoiTen())
                        .noiDung(item.getNoiDung())
                        .nguoiThucHienId(item.getNguoiThucHienId())
                        .nguoiThucHienTen(item.getNguoiThucHienTen())
                        .ngayTao(item.getNgayTao())
                        .build())
                .toList();

        return PageResponse.ofItems(items, pageNumber, pageSize, result.getTotalElements());
    }

    @Override
    @Transactional
    public int ganNhaThau(PhanCongGanRequest request) {
        if (request.getHopDongDoiTuongIds() == null || request.getHopDongDoiTuongIds().isEmpty()) {
            return 0;
        }
        NguoiDung nhaThauMoi = nguoiDungRepository.findByIdAndNgayXoaIsNull(request.getNhaThauId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy nhà thầu"));
        JwtUserPrincipal actor = SecurityContextHelper.requireCurrentUser();
        List<HopDongDoiTuong> entities = hopDongDoiTuongService.findActiveEntitiesByIds(
                request.getHopDongDoiTuongIds());
        if (entities.isEmpty()) {
            return 0;
        }

        Set<UUID> oldContractorIds = entities.stream()
                .map(HopDongDoiTuong::getNhaThauId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, NguoiDung> oldContractors = oldContractorIds.isEmpty()
                ? Map.of()
                : nguoiDungRepository.findByIdInAndNgayXoaIsNull(oldContractorIds).stream()
                        .collect(Collectors.toMap(NguoiDung::getId, Function.identity(), (a, b) -> a));

        int updated = 0;
        for (HopDongDoiTuong entity : entities) {
            UUID oldId = entity.getNhaThauId();
            if (Objects.equals(oldId, request.getNhaThauId())) {
                continue;
            }
            entity.setNhaThauId(request.getNhaThauId());
            hopDongDoiTuongService.saveEntity(entity);
            syncPhanCongRecord(entity, nhaThauMoi);
            writeHistory(entity, oldId, oldContractors.get(oldId), nhaThauMoi, actor, request.getGhiChu());
            updated++;
        }
        if (updated > 0) {
            thongBaoDispatchService.notifyUser(
                    nhaThauMoi.getId(),
                    ThongBaoLoai.PHAN_CONG,
                    "Phân công mới",
                    "Bạn được phân công " + updated + " đối tượng",
                    "/stations/assignment",
                    null);
        }
        return updated;
    }

    private List<AssignmentRow> loadFilteredRows(
            UUID hopDongId, UUID nhaThauId, String giaiDoan, UUID khuVucId, String search) {
        String cacheKey = buildFilteredRowCacheKey(hopDongId, nhaThauId, giaiDoan, khuVucId, search);
        Instant now = Instant.now();
        CachedAssignmentRows cached = filteredRowCache.get(cacheKey);
        if (cached != null && cached.expiresAt().isAfter(now)) {
            return cached.rows();
        }

        Object lock = filteredRowCacheLocks.computeIfAbsent(cacheKey, ignored -> new Object());
        synchronized (lock) {
            try {
                cached = filteredRowCache.get(cacheKey);
                if (cached != null && cached.expiresAt().isAfter(now)) {
                    return cached.rows();
                }
                List<AssignmentRow> computed = computeFilteredRows(hopDongId, nhaThauId, giaiDoan, khuVucId, search);
                filteredRowCache.put(cacheKey, new CachedAssignmentRows(computed, Instant.now().plus(ROW_CACHE_TTL)));
                return computed;
            } finally {
                filteredRowCacheLocks.remove(cacheKey, lock);
            }
        }
    }

    private List<AssignmentRow> computeFilteredRows(
            UUID hopDongId, UUID nhaThauId, String giaiDoan, UUID khuVucId, String search) {
        List<AssignmentRow> all = hopDongDoiTuongRepository.findActiveRowsForPhanCong().stream()
                .map(this::mapRow)
                .toList();
        Optional<ContractorScope> scope = contractorScopeService.currentScope();
        Set<UUID> scopedDoiTuongIds = scope.map(ContractorScope::hopDongDoiTuongIds).orElse(Set.of());
        Set<UUID> scopedHopDongIds = scope.map(ContractorScope::hopDongIds).orElse(Set.of());

        Map<UUID, TrangThaiHopDongResponse> statusMap = loadStatusMap(all);
        String keyword = EntityFilter.trimToNull(search);
        Set<UUID> searchSnapshotIds = null;
        if (keyword != null) {
            searchSnapshotIds = resolveSearchMatches(all, keyword.toLowerCase(Locale.ROOT));
        }

        List<AssignmentRow> filtered = new ArrayList<>();
        for (AssignmentRow row : all) {
            if (scope.isPresent() && !matchesScope(row, scopedDoiTuongIds, scopedHopDongIds)) {
                continue;
            }
            if (hopDongId != null && !hopDongId.equals(row.hopDongId())) {
                continue;
            }
            if (nhaThauId != null && !nhaThauId.equals(row.nhaThauId())) {
                continue;
            }
            if (khuVucId != null && !khuVucId.equals(row.khuVucId())) {
                continue;
            }
            TrangThaiHopDongResponse status = statusMap.get(row.trangThaiHopDongId());
            String statusMa = status != null ? status.getMa() : null;
            if (giaiDoan != null && !giaiDoan.isBlank() && !"all".equalsIgnoreCase(giaiDoan)) {
                if (giaiDoan.matches("[0-9a-fA-F-]{36}")) {
                    UUID statusFilter = UUID.fromString(giaiDoan);
                    if (!statusFilter.equals(row.trangThaiHopDongId())) {
                        continue;
                    }
                } else if (!PhanCongGiaiDoanHelper.matchesPhaseFilter(statusMa, giaiDoan)) {
                    continue;
                }
            }
            if (keyword != null && (searchSnapshotIds == null || !searchSnapshotIds.contains(row.id()))) {
                continue;
            }
            filtered.add(row);
        }
        return filtered;
    }

    private String buildFilteredRowCacheKey(
            UUID hopDongId, UUID nhaThauId, String giaiDoan, UUID khuVucId, String search) {
        Optional<ContractorScope> scope = contractorScopeService.currentScope();
        String scopeKey = scope
                .map(item -> item.hopDongDoiTuongIds().hashCode() + ":" + item.hopDongIds().hashCode())
                .orElse("all");
        String keyword = EntityFilter.trimToNull(search);
        return String.join("|",
                scopeKey,
                Objects.toString(hopDongId, ""),
                Objects.toString(nhaThauId, ""),
                giaiDoan == null ? "" : giaiDoan.trim().toLowerCase(Locale.ROOT),
                Objects.toString(khuVucId, ""),
                keyword == null ? "" : keyword.toLowerCase(Locale.ROOT));
    }

    private boolean matchesScope(AssignmentRow row, Set<UUID> scopedDoiTuongIds, Set<UUID> scopedHopDongIds) {
        if (row.id() != null && scopedDoiTuongIds.contains(row.id())) {
            return true;
        }
        return row.hopDongId() != null && scopedHopDongIds.contains(row.hopDongId());
    }

    private Set<UUID> resolveSearchMatches(List<AssignmentRow> rows, String keyword) {
        Map<UUID, HopDongDoiTuongSnapshot> snapshots = snapshotService.getSnapshots(
                rows.stream().map(AssignmentRow::id).toList());
        Map<UUID, NguoiDung> contractors = loadContractorMap(rows);
        Set<UUID> matches = new HashSet<>();
        String lower = keyword.toLowerCase(Locale.ROOT);
        for (AssignmentRow row : rows) {
            HopDongDoiTuongSnapshot snapshot = snapshots.get(row.id());
            NguoiDung contractor = contractors.get(row.nhaThauId());
            if (containsIgnoreCase(snapshot != null ? snapshot.getMaDoiTuong() : null, lower)
                    || containsIgnoreCase(snapshot != null ? snapshot.getKhuVuc() : null, lower)
                    || containsIgnoreCase(snapshot != null ? snapshot.getNhaThau() : null, lower)
                    || containsIgnoreCase(contractor != null ? contractor.getHoTen() : null, lower)
                    || containsIgnoreCase(contractor != null ? contractor.getTenDangNhap() : null, lower)) {
                matches.add(row.id());
            }
        }
        return matches;
    }

    private static boolean containsIgnoreCase(String value, String lowerKeyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerKeyword);
    }

    private AssignmentRow mapRow(Object[] raw) {
        return new AssignmentRow(
                toUuid(raw[0]),
                toUuid(raw[1]),
                toUuid(raw[2]),
                toUuid(raw[3]),
                toUuid(raw[4]),
                toUuid(raw[5]),
                toLocalDate(raw[6]),
                toBoolean(raw[7]),
                toBigDecimal(raw[8]),
                Optional.ofNullable(toBigDecimal(raw[9])).orElse(BigDecimal.ZERO));
    }

    private static UUID toUuid(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        return UuidUtils.parseUuid(value.toString());
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(value.toString());
    }

    private static boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return Boolean.TRUE.equals(bool);
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return Boolean.parseBoolean(value.toString());
    }

    private static LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        return null;
    }

    private PhanCongNhaThauItemResponse buildNhaThauItem(
            UUID nhaThauId,
            List<AssignmentRow> rows,
            Map<UUID, NguoiDung> contractorMap,
            Map<UUID, KhuVuc> khuVucMap,
            Map<UUID, TrangThaiHopDongResponse> statusMap) {
        NguoiDung contractor = contractorMap.get(nhaThauId);
        long hoanThanh = rows.stream()
                .filter(r -> isCompleted(r, statusMap.get(r.trangThaiHopDongId())))
                .count();
        long vuongMac = rows.stream().filter(AssignmentRow::coVuongMacMo).count();
        long ton = rows.stream().filter(r -> isTon(r, statusMap.get(r.trangThaiHopDongId()))).count();
        BigDecimal sanLuong = rows.stream()
                .map(AssignmentRow::sanLuongHieuLuc)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String maVung = rows.stream()
                .map(AssignmentRow::khuVucId)
                .filter(Objects::nonNull)
                .map(khuVucMap::get)
                .filter(Objects::nonNull)
                .map(k -> {
                    String ma = k.getMa() != null ? k.getMa().trim() : "";
                    String ten = k.getTen() != null ? k.getTen().trim() : "";
                    if (!ma.isEmpty() && !ten.isEmpty()) {
                        return ma + " — " + ten;
                    }
                    return !ma.isEmpty() ? ma : (!ten.isEmpty() ? ten : "—");
                })
                .findFirst()
                .orElse("—");
        double tienDo = rows.isEmpty() ? 0D : roundPercent(hoanThanh * 100D / rows.size());
        String labelKey = contractor != null ? contractor.getHoTen() : nhaThauId.toString();
        return PhanCongNhaThauItemResponse.builder()
                .nhaThauId(nhaThauId)
                .ten(formatContractorLabel(contractor))
                .tenDangNhap(contractor != null ? contractor.getTenDangNhap() : null)
                .maVung(maVung)
                .mauSac(PhanCongGiaiDoanHelper.colorForKey(labelKey))
                .soTram(rows.size())
                .hoanThanh(hoanThanh)
                .vuongMac(vuongMac)
                .ton(ton)
                .sanLuong(sanLuong)
                .tienDoPercent(tienDo)
                .build();
    }

    private PhanCongTramItemResponse toTramItem(
            AssignmentRow row,
            HopDongDoiTuongSnapshot snapshot,
            TrangThaiHopDongResponse status,
            String maHopDong,
            NguoiDung contractor) {
        boolean completed = isCompleted(row, status);
        boolean ton = isTon(row, status);
        return PhanCongTramItemResponse.builder()
                .id(row.id())
                .hopDongId(row.hopDongId())
                .maTram(snapshot != null && snapshot.getMaDoiTuong() != null ? snapshot.getMaDoiTuong() : "—")
                .maHopDong(maHopDong != null ? maHopDong : "—")
                .nhaThauId(row.nhaThauId())
                .nhaThau(formatContractorLabel(contractor))
                .giaiDoan(status != null ? status.getTen() : "—")
                .giaiDoanMa(status != null ? status.getMa() : null)
                .maVung(snapshot != null && snapshot.getKhuVuc() != null ? snapshot.getKhuVuc() : "—")
                .tinhThanh(snapshot != null && snapshot.getTinh() != null ? snapshot.getTinh() : "—")
                .sanLuong(row.sanLuongHieuLuc())
                .tienDoPercent(completed ? 100D : (row.ngayHtTc() != null ? 80D : (row.sanLuongHieuLuc() != null
                        && row.sanLuongHieuLuc().compareTo(BigDecimal.ZERO) > 0 ? 40D : 0D)))
                .ton(ton)
                .vuongMac(row.coVuongMacMo())
                .hoanThanh(completed)
                .ngayHtTc(row.ngayHtTc())
                .quyetToanThuc(row.quyetToanThuc())
                .build();
    }

    private void syncPhanCongRecord(HopDongDoiTuong entity, NguoiDung nhaThau) {
        List<PhanCong> existing = phanCongRepository.findByHopDongDoiTuongIdInAndNgayXoaIsNull(List.of(entity.getId()));
        for (PhanCong item : existing) {
            item.setHoatDong(false);
            item.setNgayXoa(java.time.Instant.now());
            phanCongRepository.save(item);
        }
        PhanCong record = new PhanCong();
        record.setHopDongId(entity.getHopDongId());
        record.setHopDongDoiTuongId(entity.getId());
        record.setNguoiDungId(nhaThau.getId());
        record.setNhaThau(nhaThau.getHoTen());
        record.setKhuVucId(entity.getKhuVucId());
        record.setTinhThanhId(entity.getTinhThanhId());
        record.setHoatDong(true);
        if (entity.getKhuVucId() != null) {
            khuVucRepository.findByIdAndNgayXoaIsNull(entity.getKhuVucId())
                    .ifPresent(k -> record.setMaVung(k.getMa()));
        }
        phanCongRepository.save(record);
    }

    private void writeHistory(
            HopDongDoiTuong entity,
            UUID oldContractorId,
            NguoiDung oldContractor,
            NguoiDung newContractor,
            JwtUserPrincipal actor,
            String ghiChu) {
        String oldName = formatContractorLabel(oldContractor);
        String newName = formatContractorLabel(newContractor);
        String noiDung = oldContractorId == null
                ? "Phân công nhà thầu " + newName + " cho trạm"
                : "Đổi nhà thầu từ " + oldName + " sang " + newName;
        if (ghiChu != null && !ghiChu.isBlank()) {
            noiDung += ". Ghi chú: " + ghiChu.trim();
        }
        PhanCongLichSu history = new PhanCongLichSu();
        history.setHopDongId(entity.getHopDongId());
        history.setHopDongDoiTuongId(entity.getId());
        history.setNhaThauCuId(oldContractorId);
        history.setNhaThauMoiId(newContractor.getId());
        history.setNhaThauCuTen(oldContractorId != null ? oldName : null);
        history.setNhaThauMoiTen(newName);
        history.setNoiDung(noiDung);
        history.setNguoiThucHienId(actor.id());
        history.setNguoiThucHienTen(actor.hoTen());
        phanCongLichSuRepository.save(history);
    }

    private Map<UUID, TrangThaiHopDongResponse> loadStatusMap(List<AssignmentRow> rows) {
        Set<UUID> ids = rows.stream()
                .map(AssignmentRow::trangThaiHopDongId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return trangThaiHopDongService.getByIds(ids).stream()
                .collect(Collectors.toMap(TrangThaiHopDongResponse::getId, Function.identity(), (a, b) -> a));
    }

    private Map<UUID, NguoiDung> loadContractorMap(List<AssignmentRow> rows) {
        Set<UUID> ids = rows.stream()
                .map(AssignmentRow::nhaThauId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return nguoiDungRepository.findByIdInAndNgayXoaIsNull(ids).stream()
                .collect(Collectors.toMap(NguoiDung::getId, Function.identity(), (a, b) -> a));
    }

    private Map<UUID, KhuVuc> loadKhuVucMap(List<AssignmentRow> rows) {
        Set<UUID> ids = rows.stream()
                .map(AssignmentRow::khuVucId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return khuVucRepository.findByIdInAndNgayXoaIsNull(ids).stream()
                .collect(Collectors.toMap(KhuVuc::getId, Function.identity(), (a, b) -> a));
    }

    private Map<UUID, TinhThanh> loadTinhMap(List<AssignmentRow> rows) {
        Set<UUID> ids = rows.stream()
                .map(AssignmentRow::tinhThanhId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return tinhThanhRepository.findAllById(ids).stream()
                .filter(t -> t.getNgayXoa() == null)
                .collect(Collectors.toMap(TinhThanh::getId, Function.identity(), (a, b) -> a));
    }

    private static UUID findDominantContractor(List<AssignmentRow> rows) {
        return rows.stream()
                .filter(r -> r.nhaThauId() != null)
                .collect(Collectors.groupingBy(AssignmentRow::nhaThauId, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static boolean isCompleted(AssignmentRow row, TrangThaiHopDongResponse status) {
        if (row.ngayHtTc() != null) {
            return true;
        }
        return status != null && PhanCongGiaiDoanHelper.isCompletedStatusMa(status.getMa());
    }

    private static boolean isTon(AssignmentRow row, TrangThaiHopDongResponse status) {
        if (row.coVuongMacMo()) {
            return false;
        }
        if (row.quyetToanThuc() != null) {
            return false;
        }
        if (status != null && PhanCongGiaiDoanHelper.isCompletedStatusMa(status.getMa())
                && (status.getMa().equalsIgnoreCase("QT")
                || status.getMa().toUpperCase(Locale.ROOT).contains("QUYET"))) {
            return false;
        }
        return row.ngayHtTc() != null;
    }

    private static String formatContractorLabel(NguoiDung contractor) {
        if (contractor == null) {
            return "—";
        }
        if (contractor.getHoTen() != null && !contractor.getHoTen().isBlank()) {
            return contractor.getHoTen().trim();
        }
        return contractor.getTenDangNhap() != null ? contractor.getTenDangNhap().trim() : "—";
    }

    private static double roundPercent(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private PhanCongDashboardQueryRepository.FilterParams buildFilterParams(
            UUID hopDongId, UUID nhaThauId, String giaiDoan, UUID khuVucId, String search) {
        Optional<ContractorScope> scope = contractorScopeService.currentScope();
        GiaiDoanFilter giaiDoanFilter = resolveGiaiDoanFilter(giaiDoan);
        return new PhanCongDashboardQueryRepository.FilterParams(
                hopDongId,
                nhaThauId,
                giaiDoan,
                khuVucId,
                scope.isPresent(),
                scope.map(s -> new ArrayList<>(s.hopDongDoiTuongIds())).orElseGet(ArrayList::new),
                scope.map(s -> new ArrayList<>(s.hopDongIds())).orElseGet(ArrayList::new),
                giaiDoanFilter.statusIds(),
                giaiDoanFilter.unassignedOnly());
    }

    private record GiaiDoanFilter(List<UUID> statusIds, boolean unassignedOnly) {
    }

    private GiaiDoanFilter resolveGiaiDoanFilter(String giaiDoan) {
        if (giaiDoan == null || giaiDoan.isBlank() || "all".equalsIgnoreCase(giaiDoan)) {
            return new GiaiDoanFilter(null, false);
        }
        if (giaiDoan.matches("[0-9a-fA-F-]{36}")) {
            return new GiaiDoanFilter(null, false);
        }
        if ("unassigned".equalsIgnoreCase(giaiDoan.trim())) {
            return new GiaiDoanFilter(List.of(), true);
        }
        List<UUID> statusIds = trangThaiHopDongService.list(null, true, false).stream()
                .filter(status -> PhanCongGiaiDoanHelper.matchesPhaseFilter(status.getMa(), giaiDoan))
                .map(TrangThaiHopDongResponse::getId)
                .toList();
        return new GiaiDoanFilter(statusIds, false);
    }

    private static Map<UUID, Long> toCountMap(List<Object[]> rows) {
        Map<UUID, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            UUID key = toUuid(row[0]);
            if (key != null) {
                result.put(key, toLong(row[1]));
            }
        }
        return result;
    }

    private static long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private PhanCongTongQuanResponse buildTongQuanResponse(
            long tongTram,
            long daPhanNhaThau,
            long soNhaThau,
            long coGiaiDoan,
            long hoanThanh,
            long vuongMac,
            long ton) {
        double tyLeNhaThau = tongTram > 0 ? roundPercent(daPhanNhaThau * 100D / tongTram) : 0D;
        double tyLeGiaiDoan = tongTram > 0 ? roundPercent(coGiaiDoan * 100D / tongTram) : 0D;
        return PhanCongTongQuanResponse.builder()
                .tongTram(tongTram)
                .soNhaThau(soNhaThau)
                .tyLeDaPhanNhaThau(tyLeNhaThau)
                .tyLeDaPhanGiaiDoan(tyLeGiaiDoan)
                .tongDaPhanNhaThau(daPhanNhaThau)
                .tongHoanThanh(hoanThanh)
                .tongVuongMac(vuongMac)
                .tongTon(ton)
                .build();
    }

    private PhanCongGiaiDoanItemResponse buildGiaiDoanItem(
            String key, TrangThaiHopDongResponse status, long tong, long daPhan) {
        long chuaPhan = tong - daPhan;
        double tyLe = tong > 0 ? roundPercent(daPhan * 100D / tong) : 0D;
        return PhanCongGiaiDoanItemResponse.builder()
                .trangThaiId(UNASSIGNED_KEY.equals(key) ? null : UUID.fromString(key))
                .ma(status != null ? status.getMa() : null)
                .ten(status != null ? status.getTen() : "Chưa xác định")
                .mauSac(status != null ? status.getMauSac() : "#6366f1")
                .tong(tong)
                .daPhan(daPhan)
                .chuaPhan(chuaPhan)
                .tyLePhanCong(tyLe)
                .build();
    }

    private PageResponse<PhanCongTramItemResponse> buildTramPageResponse(
            List<AssignmentRow> pageRows, long total, int pageNumber, int pageSize) {
        Map<UUID, TrangThaiHopDongResponse> statusMap = loadStatusMap(pageRows);
        Map<UUID, HopDongDoiTuongSnapshot> snapshots = snapshotService.getSnapshots(
                pageRows.stream().map(AssignmentRow::id).toList());
        var hopDongs = hopDongRepository.findByIdInAndNgayXoaIsNull(
                pageRows.stream().map(AssignmentRow::hopDongId).filter(Objects::nonNull).collect(Collectors.toSet()));
        Map<UUID, String> hopDongMa = hopDongs.stream()
                .collect(Collectors.toMap(h -> h.getId(), h -> h.getMaHopDong(), (a, b) -> a));
        Map<UUID, NguoiDung> contractorMap = loadContractorMap(pageRows);
        List<PhanCongTramItemResponse> items = pageRows.stream()
                .map(row -> toTramItem(row, snapshots.get(row.id()), statusMap.get(row.trangThaiHopDongId()),
                        hopDongMa.get(row.hopDongId()), contractorMap.get(row.nhaThauId())))
                .toList();
        return PageResponse.ofItems(items, pageNumber, pageSize, total);
    }
}
