package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.util.UuidUtils;
import vn.edu.huce.iic.bts_ops_platform.common.security.DataScopeService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DoiTuongQuanLyResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LoaiHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.DoiTuongQuanLyService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LoaiHopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.DashboardKpiSnapshot;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.PeriodScope;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.ThuocTinhLookup;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardChiTietTinhItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardChiTietTrungTamRegionResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardChiTietTrungTamResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardSanLuongBatThuongItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardTheoDoiTuongItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardTheoLinhVucItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardTheoLinhVucRegionResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardTheoLinhVucResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardTheoLoaiItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardXuTheSanLuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.response.DashboardXuTheSeriesResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.constants.VolumeConstants;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.VolumeTinhToanHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.repository.SanLuongCanhBaoKiemTraRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.entity.SanLuongCanhBaoKiemTra;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.helpers.DashboardKpiSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.helpers.DashboardTinhToanHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.services.DashboardService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongGiaTriResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongLoaiThongKeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongGiaTriService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.SanLuongDoiTuongNgayTongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.SanLuongDoiTuongTongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.services.SanLuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.KhuVucResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.TinhThanhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.services.KhuVucService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.services.TinhThanhService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.services.VuongMacService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final List<String> TRANG_THAI_DANG_VUONG = List.of("pending", "in_progress");
    private static final String TRANG_THAI_QUYET_TOAN = "QT";
    private static final int DEFAULT_LIMIT_BAT_THUONG = 50;
    private static final Set<String> PERIOD_MODES = Set.of("day", "week", "month", "quarter", "year");

    private final HopDongService hopDongService;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final HopDongDoiTuongGiaTriService hopDongDoiTuongGiaTriService;
    private final SanLuongService sanLuongService;
    private final KhuVucService khuVucService;
    private final TinhThanhService tinhThanhService;
    private final VuongMacService vuongMacService;
    private final LoaiHopDongService loaiHopDongService;
    private final DoiTuongQuanLyService doiTuongQuanLyService;
    private final ThuocTinhService thuocTinhService;
    private final DashboardKpiSupport dashboardKpiSupport;
    private final SanLuongCanhBaoKiemTraRepository sanLuongCanhBaoKiemTraRepository;
    private final DataScopeService dataScopeService;

    @Override
    @Transactional(readOnly = true)
    public DashboardTongQuanResponse tongQuan(Integer nam, Integer thang) {
        LocalDate today = LocalDate.now();
        int year = nam != null ? nam : today.getYear();
        int month = thang != null ? thang : today.getMonthValue();

        LocalDate yearFrom = LocalDate.of(year, 1, 1);
        LocalDate yearTo = LocalDate.of(year, 12, 31);
        LocalDate prevYearFrom = LocalDate.of(year - 1, 1, 1);
        LocalDate prevYearTo = LocalDate.of(year - 1, 12, 31);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate monthFrom = yearMonth.atDay(1);
        LocalDate monthTo = yearMonth.atEndOfMonth();
        YearMonth prevYearMonth = yearMonth.minusMonths(1);
        LocalDate prevMonthFrom = prevYearMonth.atDay(1);
        LocalDate prevMonthTo = prevYearMonth.atEndOfMonth();

        BigDecimal sanLuongNam = sanLuongService.tongThanhTien(yearFrom, yearTo);
        BigDecimal sanLuongNamTruoc = sanLuongService.tongThanhTien(prevYearFrom, prevYearTo);
        BigDecimal sanLuongThang = sanLuongService.tongThanhTien(monthFrom, monthTo);
        BigDecimal sanLuongThangTruoc = sanLuongService.tongThanhTien(prevMonthFrom, prevMonthTo);
        DashboardKpiSnapshot kpiSnapshot = dashboardKpiSupport.buildSnapshot(null, null);
        BigDecimal tongKpi = kpiSnapshot.total();

        UUID scopeKhuVuc = dataScopeService.isFullAccess() ? null : dataScopeService.resolveKhuVucFilter(null);

        return DashboardTongQuanResponse.builder()
                .capNhatLuc(Instant.now())
                .tongDoiTuong(hopDongDoiTuongService.demActive(null, scopeKhuVuc))
                .soHopDong(hopDongService.countActive())
                .soVuongMacDangVuong(vuongMacService.demActiveTheoTrangThai(TRANG_THAI_DANG_VUONG))
                .soKhuVuc(dataScopeService.scopedKhuVucCount(khuVucService.demActive()))
                .sanLuongNam(DashboardTinhToanHelper.metric(
                        "TỔNG SẢN LƯỢNG NĂM",
                        sanLuongNam,
                        sanLuongNamTruoc,
                        tongKpi))
                .sanLuongThang(DashboardTinhToanHelper.metric(
                        "SẢN LƯỢNG THÁNG",
                        sanLuongThang,
                        sanLuongThangTruoc,
                        tongKpi))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardTheoLoaiItemResponse> theoLoai() {
        List<LoaiHopDongResponse> loaiList = loaiHopDongService.list(null, true, false);
        Map<UUID, Long> hopDongByLoai = hopDongService.demActiveNhomTheoLoaiHopDong();
        Map<UUID, Long> doiTuongByLoai = hopDongDoiTuongService.demActiveNhomTheoLoaiHopDong();
        Map<UUID, BigDecimal> sanLuongByLoai = sanLuongService.tongThanhTienTheoLoaiHopDong();
        Map<UUID, Long> vuongMacByLoai = vuongMacService.demActiveTheoLoaiHopDongVaTrangThai(TRANG_THAI_DANG_VUONG);
        Map<UUID, List<DashboardTheoDoiTuongItemResponse>> theoDoiTuongByLoai = buildTheoDoiTuongByLoai();

        List<DashboardTheoLoaiItemResponse> items = new ArrayList<>();
        for (LoaiHopDongResponse loai : loaiList) {
            items.add(DashboardTheoLoaiItemResponse.builder()
                    .loaiHopDongId(loai.getId())
                    .ma(loai.getMa())
                    .ten(loai.getTen())
                    .soHopDong(hopDongByLoai.getOrDefault(loai.getId(), 0L))
                    .soDoiTuong(doiTuongByLoai.getOrDefault(loai.getId(), 0L))
                    .tongSanLuong(sanLuongByLoai.getOrDefault(loai.getId(), BigDecimal.ZERO))
                    .soVuongMacDangVuong(vuongMacByLoai.getOrDefault(loai.getId(), 0L))
                    .theoDoiTuong(theoDoiTuongByLoai.getOrDefault(loai.getId(), List.of()))
                    .build());
        }
        return items;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardSanLuongBatThuongItemResponse> sanLuongBatThuong(Integer limit) {
        int max = limit != null && limit > 0 ? limit : DEFAULT_LIMIT_BAT_THUONG;

        Map<UUID, Long> soTramByHopDong = hopDongDoiTuongService.demActiveNhomTheoHopDong();
        List<SanLuongDoiTuongTongResponse> sanLuongRows = sanLuongService.tongThanhTienTheoDoiTuong();
        if (sanLuongRows.isEmpty()) {
            return List.of();
        }

        Set<UUID> hopDongIds = new HashSet<>();
        for (SanLuongDoiTuongTongResponse row : sanLuongRows) {
            if (row.hopDongId() != null) {
                hopDongIds.add(row.hopDongId());
            }
        }
        Map<UUID, HopDongResponse> hopDongById = new HashMap<>();
        Map<UUID, LoaiHopDongResponse> loaiHopDongById = new HashMap<>();
        for (HopDongResponse hopDong : hopDongService.getByIds(hopDongIds)) {
            hopDongById.put(hopDong.getId(), hopDong);
            if (hopDong.getLoaiHopDongId() != null) {
                loaiHopDongById.computeIfAbsent(hopDong.getLoaiHopDongId(),
                        id -> loaiHopDongService.getById(id));
            }
        }
        LoaiHopDongResponse xayMoiConLai = loaiHopDongById.get(VolumeConstants.XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID);
        if (xayMoiConLai == null) {
            try {
                xayMoiConLai = loaiHopDongService.getById(VolumeConstants.XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID);
            } catch (Exception ignored) {
                xayMoiConLai = null;
            }
        }

        List<DashboardSanLuongBatThuongItemResponse> candidates = new ArrayList<>();
        for (SanLuongDoiTuongTongResponse row : sanLuongRows) {
            UUID hopDongDoiTuongId = row.hopDongDoiTuongId();
            UUID hopDongId = row.hopDongId();
            if (hopDongDoiTuongId == null || hopDongId == null) {
                continue;
            }
            HopDongResponse hopDong = hopDongById.get(hopDongId);
            if (hopDong == null || hopDong.getGiaTriHd() == null || hopDong.getGiaTriHd() <= 0) {
                continue;
            }
            long soTram = soTramByHopDong.getOrDefault(hopDongId, 0L);
            if (soTram <= 0) {
                continue;
            }
            BigDecimal heSo = resolveHeSoNguong(hopDong, xayMoiConLai);
            if (heSo == null) {
                continue;
            }
            BigDecimal sanLuong = row.tongThanhTien() != null ? row.tongThanhTien() : BigDecimal.ZERO;
            BigDecimal binhQuan = BigDecimal.valueOf(hopDong.getGiaTriHd())
                    .divide(BigDecimal.valueOf(soTram), 2, RoundingMode.HALF_UP);
            BigDecimal nguong = binhQuan.multiply(heSo);
            if (sanLuong.compareTo(nguong) <= 0) {
                continue;
            }

            candidates.add(DashboardSanLuongBatThuongItemResponse.builder()
                    .hopDongDoiTuongId(hopDongDoiTuongId)
                    .hopDongId(hopDongId)
                    .sanLuong(sanLuong)
                    .giaTriBinhQuan(binhQuan)
                    .heSo(heSo)
                    .nguongCanhBao(nguong)
                    .tyLeSoVoiBinhQuan(binhQuan.signum() == 0
                            ? BigDecimal.ZERO
                            : sanLuong.divide(binhQuan, 4, RoundingMode.HALF_UP))
                    .build());
        }

        if (candidates.isEmpty()) {
            return List.of();
        }
        candidates.sort(Comparator
                .comparing(DashboardSanLuongBatThuongItemResponse::getTyLeSoVoiBinhQuan).reversed());
        if (candidates.size() > max) {
            candidates = new ArrayList<>(candidates.subList(0, max));
        }

        return enrichBatThuong(candidates);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardXuTheSanLuongResponse xuTheSanLuong(
            String timeMode,
            String granularity,
            Integer nam,
            Integer thang,
            Integer tuan,
            Integer quy,
            String ngay,
            String hopDongId,
            String loaiHopDongId
    ) {
        LocalDate today = LocalDate.now();
        String bucketMode = granularity == null || granularity.isBlank()
                ? "week"
                : granularity.trim().toLowerCase(Locale.ROOT);
        if (!PERIOD_MODES.contains(bucketMode)) {
            bucketMode = "week";
        }

        String scopeMode = timeMode == null || timeMode.isBlank()
                ? null
                : timeMode.trim().toLowerCase(Locale.ROOT);
        if (scopeMode != null && !PERIOD_MODES.contains(scopeMode)) {
            scopeMode = null;
        }

        int year = nam != null ? nam : today.getYear();
        int month = thang != null ? thang : today.getMonthValue();
        LocalDate ngayDate = null;
        if (ngay != null && !ngay.isBlank()) {
            try {
                ngayDate = LocalDate.parse(ngay.trim());
            } catch (Exception ignored) {
                ngayDate = today;
            }
        }

        PeriodScope scope;
        if (scopeMode != null) {
            scope = DashboardTinhToanHelper.resolveScopeFromTimeMode(
                    scopeMode, bucketMode, year, thang, tuan, quy, ngayDate);
        } else {
            scope = DashboardTinhToanHelper.resolveXuTheScope(bucketMode, year, month);
            scopeMode = bucketMode;
        }

        List<String> buckets = scope.buckets();

        List<SanLuongDoiTuongNgayTongResponse> rows = filterRowsByHopDongScope(
                sanLuongService.tongThanhTienTheoDoiTuongVaNgay(scope.dateFrom(), scope.dateTo()),
                UuidUtils.parseUuid(hopDongId),
                UuidUtils.parseUuid(loaiHopDongId));

        Map<UUID, UUID> khuVucByDoiTuong = Map.of();
        if (!rows.isEmpty()) {
            Set<UUID> hopDongDoiTuongIds = new HashSet<>();
            for (SanLuongDoiTuongNgayTongResponse row : rows) {
                if (row.hopDongDoiTuongId() != null) {
                    hopDongDoiTuongIds.add(row.hopDongDoiTuongId());
                }
            }
            khuVucByDoiTuong = resolveGeoByHopDongDoiTuong(hopDongDoiTuongIds).khuVucByDoiTuong();
        }

        Map<UUID, Map<String, BigDecimal>> amountByKhuVuc = new HashMap<>();
        Map<String, BigDecimal> unmappedByBucket = new HashMap<>();
        for (SanLuongDoiTuongNgayTongResponse row : rows) {
            UUID hopDongDoiTuongId = row.hopDongDoiTuongId();
            LocalDate rowNgay = row.ngay();
            BigDecimal amount = row.tongThanhTien() != null ? row.tongThanhTien() : BigDecimal.ZERO;
            String bucket = DashboardTinhToanHelper.toBucketKeyInRange(
                    bucketMode, rowNgay, scope.dateFrom(), scope.dateTo());
            if (bucket == null || !buckets.contains(bucket)) {
                continue;
            }
            UUID khuVucId = khuVucByDoiTuong.get(hopDongDoiTuongId);
            if (khuVucId == null) {
                unmappedByBucket.merge(bucket, amount, BigDecimal::add);
                continue;
            }
            amountByKhuVuc
                    .computeIfAbsent(khuVucId, ignored -> new HashMap<>())
                    .merge(bucket, amount, BigDecimal::add);
        }

        List<KhuVucResponse> activeKhuVucs = listScopedKhuVucs();

        UUID hopDongUuid = UuidUtils.parseUuid(hopDongId);
        UUID loaiHopDongUuid = UuidUtils.parseUuid(loaiHopDongId);
        DashboardKpiSnapshot kpiSnapshot = dashboardKpiSupport.buildSnapshot(hopDongUuid, loaiHopDongUuid);

        List<DashboardXuTheSeriesResponse> series = new ArrayList<>();
        for (KhuVucResponse khuVuc : activeKhuVucs) {
            series.add(DashboardTinhToanHelper.buildXuTheSeries(
                    khuVuc.getId(),
                    khuVuc.getMa(),
                    khuVuc.getTen(),
                    buckets,
                    amountByKhuVuc.getOrDefault(khuVuc.getId(), Map.of()),
                    kpiSnapshot.forKhuVuc(khuVuc.getId())));
        }

        series.add(DashboardTinhToanHelper.buildXuTheSeries(
                null,
                "OTHER",
                "Khác",
                buckets,
                unmappedByBucket,
                kpiSnapshot.orphan()));

        return DashboardXuTheSanLuongResponse.builder()
                .timeMode(scopeMode)
                .granularity(bucketMode)
                .nam(year)
                .thang(thang != null ? thang : month)
                .tuan(tuan)
                .quy(quy)
                .ngay(ngayDate != null ? ngayDate.toString() : ngay)
                .buckets(buckets)
                .series(series)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardChiTietTrungTamResponse chiTietTrungTam(
            String timeMode,
            Integer nam,
            Integer thang,
            Integer tuan,
            Integer quy,
            String ngay,
            String hopDongId,
            String loaiHopDongId) {
        LocalDate today = LocalDate.now();
        String scopeMode = timeMode == null || timeMode.isBlank()
                ? "month"
                : timeMode.trim().toLowerCase(Locale.ROOT);
        if (!PERIOD_MODES.contains(scopeMode)) {
            scopeMode = "month";
        }

        int year = nam != null ? nam : today.getYear();
        int month = thang != null ? thang : today.getMonthValue();
        LocalDate ngayDate = parseNgay(ngay, today);

        PeriodScope scope = DashboardTinhToanHelper.resolveScopeFromTimeMode(
                scopeMode, scopeMode, year, thang, tuan, quy, ngayDate);

        List<SanLuongDoiTuongNgayTongResponse> rows = filterRowsByHopDongScope(
                sanLuongService.tongThanhTienTheoDoiTuongVaNgay(scope.dateFrom(), scope.dateTo()),
                UuidUtils.parseUuid(hopDongId),
                UuidUtils.parseUuid(loaiHopDongId));

        Map<UUID, UUID> khuVucByDoiTuong = Map.of();
        Map<UUID, UUID> tinhByDoiTuong = Map.of();
        if (!rows.isEmpty()) {
            Set<UUID> hopDongDoiTuongIds = new HashSet<>();
            for (SanLuongDoiTuongNgayTongResponse row : rows) {
                if (row.hopDongDoiTuongId() != null) {
                    hopDongDoiTuongIds.add(row.hopDongDoiTuongId());
                }
            }
            GeoByDoiTuong geo = resolveGeoByHopDongDoiTuong(hopDongDoiTuongIds);
            khuVucByDoiTuong = geo.khuVucByDoiTuong();
            tinhByDoiTuong = geo.tinhThanhByDoiTuong();
        }

        // amountByKhuVucId -> (tinhThanhId -> amount); null tinh key = "OTHER"
        Map<UUID, Map<UUID, BigDecimal>> amountByKhuVucTinh = new HashMap<>();
        Map<UUID, BigDecimal> unmappedByKhuVuc = new HashMap<>();
        BigDecimal orphanAmount = BigDecimal.ZERO;

        for (SanLuongDoiTuongNgayTongResponse row : rows) {
            BigDecimal amount = row.tongThanhTien() != null ? row.tongThanhTien() : BigDecimal.ZERO;
            UUID hopDongDoiTuongId = row.hopDongDoiTuongId();
            UUID khuVucId = khuVucByDoiTuong.get(hopDongDoiTuongId);
            UUID tinhId = tinhByDoiTuong.get(hopDongDoiTuongId);
            if (khuVucId == null) {
                orphanAmount = orphanAmount.add(amount);
                continue;
            }
            if (tinhId == null) {
                unmappedByKhuVuc.merge(khuVucId, amount, BigDecimal::add);
                continue;
            }
            amountByKhuVucTinh
                    .computeIfAbsent(khuVucId, ignored -> new HashMap<>())
                    .merge(tinhId, amount, BigDecimal::add);
        }

        List<KhuVucResponse> activeKhuVucs = listScopedKhuVucs();

        UUID hopDongUuid = UuidUtils.parseUuid(hopDongId);
        UUID loaiHopDongUuid = UuidUtils.parseUuid(loaiHopDongId);
        DashboardKpiSnapshot kpiSnapshot = dashboardKpiSupport.buildSnapshot(hopDongUuid, loaiHopDongUuid);

        List<DashboardChiTietTrungTamRegionResponse> regions = new ArrayList<>();
        for (KhuVucResponse khuVuc : activeKhuVucs) {
            Map<UUID, BigDecimal> byTinh = amountByKhuVucTinh.getOrDefault(khuVuc.getId(), Map.of());
            List<TinhThanhResponse> provinces = tinhThanhService.list(null, khuVuc.getId(), true).stream()
                    .filter(t -> !Boolean.TRUE.equals(t.getLaTinhCu()))
                    .sorted(Comparator.comparing(TinhThanhResponse::getMa, String.CASE_INSENSITIVE_ORDER))
                    .toList();

            List<DashboardChiTietTinhItemResponse> items = new ArrayList<>();
            for (TinhThanhResponse tinh : provinces) {
                // giaTri có thể lưu id dòng tỉnh hoặc tinhThanhId nhóm — thử cả hai
                BigDecimal amount = byTinh.getOrDefault(tinh.getId(), BigDecimal.ZERO);
                BigDecimal kpiAmount = kpiSnapshot.forKhuVucTinh(khuVuc.getId(), tinh.getId());
                if (amount.signum() == 0 && tinh.getTinhThanhId() != null) {
                    amount = byTinh.getOrDefault(tinh.getTinhThanhId(), BigDecimal.ZERO);
                    if (kpiAmount.signum() == 0) {
                        kpiAmount = kpiSnapshot.forKhuVucTinh(khuVuc.getId(), tinh.getTinhThanhId());
                    }
                }
                items.add(DashboardChiTietTinhItemResponse.builder()
                        .tinhThanhId(tinh.getId())
                        .ma(tinh.getMa())
                        .ten(tinh.getTen())
                        .amount(amount)
                        .kpiAmount(kpiAmount)
                        .build());
            }

            BigDecimal other = unmappedByKhuVuc.getOrDefault(khuVuc.getId(), BigDecimal.ZERO);
            BigDecimal otherKpi = kpiSnapshot.forKhuVucTinh(khuVuc.getId(), null);
            if (other.signum() > 0 || otherKpi.signum() > 0) {
                items.add(DashboardChiTietTinhItemResponse.builder()
                        .tinhThanhId(null)
                        .ma("OTHER")
                        .ten("Khác")
                        .amount(other)
                        .kpiAmount(otherKpi)
                        .build());
            }

            regions.add(DashboardChiTietTrungTamRegionResponse.builder()
                    .khuVucId(khuVuc.getId())
                    .ma(khuVuc.getMa())
                    .ten(khuVuc.getTen())
                    .provinces(items)
                    .build());
        }

        if (orphanAmount.signum() > 0 || kpiSnapshot.orphan().signum() > 0) {
            regions.add(DashboardChiTietTrungTamRegionResponse.builder()
                    .khuVucId(null)
                    .ma("OTHER")
                    .ten("Khác")
                    .provinces(List.of(DashboardChiTietTinhItemResponse.builder()
                            .tinhThanhId(null)
                            .ma("OTHER")
                            .ten("Chưa gán khu vực")
                            .amount(orphanAmount)
                            .kpiAmount(kpiSnapshot.orphan())
                            .build()))
                    .build());
        }

        return DashboardChiTietTrungTamResponse.builder()
                .timeMode(scopeMode)
                .nam(year)
                .thang(thang != null ? thang : month)
                .tuan(tuan)
                .quy(quy)
                .ngay(ngayDate != null ? ngayDate.toString() : ngay)
                .regions(regions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardTheoLinhVucResponse theoLinhVuc(
            String timeMode,
            Integer nam,
            Integer thang,
            Integer tuan,
            Integer quy,
            String ngay,
            String hopDongId) {
        LocalDate today = LocalDate.now();
        String scopeMode = timeMode == null || timeMode.isBlank()
                ? "month"
                : timeMode.trim().toLowerCase(Locale.ROOT);
        if (!PERIOD_MODES.contains(scopeMode)) {
            scopeMode = "month";
        }

        int year = nam != null ? nam : today.getYear();
        int month = thang != null ? thang : today.getMonthValue();
        LocalDate ngayDate = parseNgay(ngay, today);

        PeriodScope scope = DashboardTinhToanHelper.resolveScopeFromTimeMode(
                scopeMode, scopeMode, year, thang, tuan, quy, ngayDate);

        // Chỉ lọc theo HĐ; lĩnh vực (loại HĐ) để FE highlight, không slice tại đây
        List<SanLuongDoiTuongNgayTongResponse> rows = filterRowsByHopDongScope(
                sanLuongService.tongThanhTienTheoDoiTuongVaNgay(scope.dateFrom(), scope.dateTo()),
                UuidUtils.parseUuid(hopDongId),
                null);

        Map<UUID, UUID> khuVucByDoiTuong = Map.of();
        Map<UUID, UUID> loaiByDoiTuong = Map.of();
        if (!rows.isEmpty()) {
            Set<UUID> hopDongDoiTuongIds = new HashSet<>();
            for (SanLuongDoiTuongNgayTongResponse row : rows) {
                if (row.hopDongDoiTuongId() != null) {
                    hopDongDoiTuongIds.add(row.hopDongDoiTuongId());
                }
            }
            KhuVucLoaiByDoiTuong khuVucLoai = resolveKhuVucAndLoaiByHopDongDoiTuong(hopDongDoiTuongIds);
            khuVucByDoiTuong = khuVucLoai.khuVucByDoiTuong();
            loaiByDoiTuong = khuVucLoai.loaiByDoiTuong();
        }

        Map<UUID, Map<UUID, BigDecimal>> amountByKhuVucLoai = new HashMap<>();
        Map<UUID, BigDecimal> unmappedByKhuVuc = new HashMap<>();
        BigDecimal orphanAmount = BigDecimal.ZERO;

        for (SanLuongDoiTuongNgayTongResponse row : rows) {
            BigDecimal amount = row.tongThanhTien() != null ? row.tongThanhTien() : BigDecimal.ZERO;
            UUID hopDongDoiTuongId = row.hopDongDoiTuongId();
            UUID khuVucId = khuVucByDoiTuong.get(hopDongDoiTuongId);
            UUID loaiId = loaiByDoiTuong.get(hopDongDoiTuongId);
            if (khuVucId == null) {
                orphanAmount = orphanAmount.add(amount);
                continue;
            }
            if (loaiId == null) {
                unmappedByKhuVuc.merge(khuVucId, amount, BigDecimal::add);
                continue;
            }
            amountByKhuVucLoai
                    .computeIfAbsent(khuVucId, ignored -> new HashMap<>())
                    .merge(loaiId, amount, BigDecimal::add);
        }

        List<LoaiHopDongResponse> loaiList = loaiHopDongService.list(null, true, false).stream()
                .sorted(Comparator.comparing(LoaiHopDongResponse::getMa, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<KhuVucResponse> activeKhuVucs = listScopedKhuVucs();

        UUID hopDongUuid = UuidUtils.parseUuid(hopDongId);
        DashboardKpiSnapshot kpiSnapshot = dashboardKpiSupport.buildSnapshot(hopDongUuid, null);

        List<DashboardTheoLinhVucRegionResponse> regions = new ArrayList<>();
        for (KhuVucResponse khuVuc : activeKhuVucs) {
            Map<UUID, BigDecimal> byLoai = amountByKhuVucLoai.getOrDefault(khuVuc.getId(), Map.of());
            List<DashboardTheoLinhVucItemResponse> domains = new ArrayList<>();
            for (LoaiHopDongResponse loai : loaiList) {
                domains.add(DashboardTheoLinhVucItemResponse.builder()
                        .loaiHopDongId(loai.getId())
                        .ma(loai.getMa())
                        .ten(loai.getTen())
                        .amount(byLoai.getOrDefault(loai.getId(), BigDecimal.ZERO))
                        .kpiAmount(kpiSnapshot.forKhuVucLoai(khuVuc.getId(), loai.getId()))
                        .build());
            }
            BigDecimal other = unmappedByKhuVuc.getOrDefault(khuVuc.getId(), BigDecimal.ZERO);
            BigDecimal otherKpi = kpiSnapshot.orphanLoaiForKhuVuc(khuVuc.getId());
            if (other.signum() > 0 || otherKpi.signum() > 0) {
                domains.add(DashboardTheoLinhVucItemResponse.builder()
                        .loaiHopDongId(null)
                        .ma("OTHER")
                        .ten("Khác")
                        .amount(other)
                        .kpiAmount(otherKpi)
                        .build());
            }
            regions.add(DashboardTheoLinhVucRegionResponse.builder()
                    .khuVucId(khuVuc.getId())
                    .ma(khuVuc.getMa())
                    .ten(khuVuc.getTen())
                    .domains(domains)
                    .build());
        }

        if (orphanAmount.signum() > 0 || kpiSnapshot.orphan().signum() > 0) {
            regions.add(DashboardTheoLinhVucRegionResponse.builder()
                    .khuVucId(null)
                    .ma("OTHER")
                    .ten("Khác")
                    .domains(List.of(DashboardTheoLinhVucItemResponse.builder()
                            .loaiHopDongId(null)
                            .ma("OTHER")
                            .ten("Chưa gán khu vực")
                            .amount(orphanAmount)
                            .kpiAmount(kpiSnapshot.orphan())
                            .build()))
                    .build());
        }

        return DashboardTheoLinhVucResponse.builder()
                .timeMode(scopeMode)
                .nam(year)
                .thang(thang != null ? thang : month)
                .tuan(tuan)
                .quy(quy)
                .ngay(ngayDate != null ? ngayDate.toString() : ngay)
                .regions(regions)
                .build();
    }

    /**
     * Lọc dòng sản lượng theo hợp đồng / loại HĐ (AND khi cả hai có).
     * null = không lọc chiều đó.
     */
    private List<SanLuongDoiTuongNgayTongResponse> filterRowsByHopDongScope(
            List<SanLuongDoiTuongNgayTongResponse> rows,
            UUID hopDongId,
            UUID loaiHopDongId) {
        if (rows.isEmpty() || (hopDongId == null && loaiHopDongId == null)) {
            return rows;
        }

        Set<UUID> hopDongDoiTuongIds = new HashSet<>();
        for (SanLuongDoiTuongNgayTongResponse row : rows) {
            if (row.hopDongDoiTuongId() != null) {
                hopDongDoiTuongIds.add(row.hopDongDoiTuongId());
            }
        }
        if (hopDongDoiTuongIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, HopDongDoiTuongResponse> doiTuongById = new HashMap<>();
        Set<UUID> hopDongIds = new HashSet<>();
        for (HopDongDoiTuongResponse entity : hopDongDoiTuongService.getByIds(hopDongDoiTuongIds)) {
            doiTuongById.put(entity.getId(), entity);
            if (entity.getHopDongId() != null) {
                hopDongIds.add(entity.getHopDongId());
            }
        }

        Map<UUID, UUID> loaiByHopDong = new HashMap<>();
        if (loaiHopDongId != null && !hopDongIds.isEmpty()) {
            for (HopDongResponse hopDong : hopDongService.getByIds(hopDongIds)) {
                if (hopDong.getLoaiHopDongId() != null) {
                    loaiByHopDong.put(hopDong.getId(), hopDong.getLoaiHopDongId());
                }
            }
        }

        Set<UUID> allowed = new HashSet<>();
        for (UUID hopDongDoiTuongId : hopDongDoiTuongIds) {
            HopDongDoiTuongResponse entity = doiTuongById.get(hopDongDoiTuongId);
            if (entity == null || entity.getHopDongId() == null) {
                continue;
            }
            if (hopDongId != null && !hopDongId.equals(entity.getHopDongId())) {
                continue;
            }
            if (loaiHopDongId != null) {
                UUID loaiId = loaiByHopDong.get(entity.getHopDongId());
                if (!loaiHopDongId.equals(loaiId)) {
                    continue;
                }
            }
            allowed.add(hopDongDoiTuongId);
        }

        if (!dataScopeService.isFullAccess()) {
            allowed.removeIf(id -> {
                HopDongDoiTuongResponse entity = doiTuongById.get(id);
                return entity == null || !dataScopeService.matchesKhuVuc(entity.getKhuVucId());
            });
        }

        if (allowed.isEmpty()) {
            return List.of();
        }
        return rows.stream()
                .filter(row -> row.hopDongDoiTuongId() != null && allowed.contains(row.hopDongDoiTuongId()))
                .toList();
    }

    private Map<UUID, UUID> resolveLoaiHopDongIdByHopDongDoiTuong(Set<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, HopDongDoiTuongResponse> doiTuongById = new HashMap<>();
        Set<UUID> hopDongIds = new HashSet<>();
        for (HopDongDoiTuongResponse entity : hopDongDoiTuongService.getByIds(hopDongDoiTuongIds)) {
            doiTuongById.put(entity.getId(), entity);
            if (entity.getHopDongId() != null) {
                hopDongIds.add(entity.getHopDongId());
            }
        }
        Map<UUID, UUID> loaiByHopDong = new HashMap<>();
        if (!hopDongIds.isEmpty()) {
            for (HopDongResponse hopDong : hopDongService.getByIds(hopDongIds)) {
                if (hopDong.getLoaiHopDongId() != null) {
                    loaiByHopDong.put(hopDong.getId(), hopDong.getLoaiHopDongId());
                }
            }
        }
        Map<UUID, UUID> result = new HashMap<>();
        for (UUID hopDongDoiTuongId : hopDongDoiTuongIds) {
            HopDongDoiTuongResponse entity = doiTuongById.get(hopDongDoiTuongId);
            if (entity == null || entity.getHopDongId() == null) {
                continue;
            }
            UUID loaiId = loaiByHopDong.get(entity.getHopDongId());
            if (loaiId != null) {
                result.put(hopDongDoiTuongId, loaiId);
            }
        }
        return result;
    }

    private LocalDate parseNgay(String ngay, LocalDate fallback) {
        if (ngay == null || ngay.isBlank()) {
            return fallback;
        }
        try {
            return LocalDate.parse(ngay.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private Map<UUID, UUID> resolveTinhThanhIdByHopDongDoiTuong(Set<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, HopDongDoiTuongResponse> doiTuongById = new HashMap<>();
        Set<UUID> doiTuongQuanLyIds = new HashSet<>();
        for (HopDongDoiTuongResponse entity : hopDongDoiTuongService.getByIds(hopDongDoiTuongIds)) {
            doiTuongById.put(entity.getId(), entity);
            if (entity.getDoiTuongQuanLyId() != null) {
                doiTuongQuanLyIds.add(entity.getDoiTuongQuanLyId());
            }
        }

        Map<UUID, ThuocTinhLookup> lookupByDoiTuong = new HashMap<>();
        for (UUID doiTuongQuanLyId : doiTuongQuanLyIds) {
            lookupByDoiTuong.put(doiTuongQuanLyId, buildThuocTinhLookup(doiTuongQuanLyId));
        }

        Map<UUID, Map<UUID, String>> giaTriByDoiTuong = new HashMap<>();
        for (HopDongDoiTuongGiaTriResponse giaTri : hopDongDoiTuongGiaTriService
                .getByHopDongDoiTuongIds(hopDongDoiTuongIds)) {
            if (giaTri.getThuocTinhId() == null || giaTri.getGiaTri() == null) {
                continue;
            }
            giaTriByDoiTuong
                    .computeIfAbsent(giaTri.getHopDongDoiTuongId(), ignored -> new HashMap<>())
                    .put(giaTri.getThuocTinhId(), giaTri.getGiaTri().trim());
        }

        Map<UUID, UUID> result = new HashMap<>();
        for (UUID hopDongDoiTuongId : hopDongDoiTuongIds) {
            HopDongDoiTuongResponse entity = doiTuongById.get(hopDongDoiTuongId);
            if (entity == null) {
                continue;
            }
            ThuocTinhLookup lookup = lookupByDoiTuong.get(entity.getDoiTuongQuanLyId());
            if (lookup == null || lookup.tinhThanhAttrId() == null) {
                continue;
            }
            Map<UUID, String> giaTri = giaTriByDoiTuong.getOrDefault(hopDongDoiTuongId, Map.of());
            UUID tinhId = UuidUtils.parseUuid(giaTri.get(lookup.tinhThanhAttrId()));
            if (tinhId != null) {
                result.put(hopDongDoiTuongId, tinhId);
            }
        }
        return result;
    }

    private Map<UUID, UUID> resolveKhuVucIdByHopDongDoiTuong(Set<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, HopDongDoiTuongResponse> doiTuongById = new HashMap<>();
        Set<UUID> doiTuongQuanLyIds = new HashSet<>();
        for (HopDongDoiTuongResponse entity : hopDongDoiTuongService.getByIds(hopDongDoiTuongIds)) {
            doiTuongById.put(entity.getId(), entity);
            if (entity.getDoiTuongQuanLyId() != null) {
                doiTuongQuanLyIds.add(entity.getDoiTuongQuanLyId());
            }
        }

        Map<UUID, ThuocTinhLookup> lookupByDoiTuong = new HashMap<>();
        for (UUID doiTuongQuanLyId : doiTuongQuanLyIds) {
            lookupByDoiTuong.put(doiTuongQuanLyId, buildThuocTinhLookup(doiTuongQuanLyId));
        }

        Map<UUID, Map<UUID, String>> giaTriByDoiTuong = new HashMap<>();
        for (HopDongDoiTuongGiaTriResponse giaTri : hopDongDoiTuongGiaTriService
                .getByHopDongDoiTuongIds(hopDongDoiTuongIds)) {
            if (giaTri.getThuocTinhId() == null || giaTri.getGiaTri() == null) {
                continue;
            }
            giaTriByDoiTuong
                    .computeIfAbsent(giaTri.getHopDongDoiTuongId(), ignored -> new HashMap<>())
                    .put(giaTri.getThuocTinhId(), giaTri.getGiaTri().trim());
        }

        Map<UUID, UUID> result = new HashMap<>();
        for (UUID hopDongDoiTuongId : hopDongDoiTuongIds) {
            HopDongDoiTuongResponse entity = doiTuongById.get(hopDongDoiTuongId);
            if (entity == null) {
                continue;
            }
            ThuocTinhLookup lookup = lookupByDoiTuong.get(entity.getDoiTuongQuanLyId());
            if (lookup == null || lookup.khuVucAttrId() == null) {
                continue;
            }
            Map<UUID, String> giaTri = giaTriByDoiTuong.getOrDefault(hopDongDoiTuongId, Map.of());
            UUID khuVucId = UuidUtils.parseUuid(giaTri.get(lookup.khuVucAttrId()));
            if (khuVucId != null) {
                result.put(hopDongDoiTuongId, khuVucId);
            }
        }
        return result;
    }

    /**
     * Nạp một lần toàn bộ nguyên liệu để phân giải khu vực / tỉnh / loại HĐ cho một tập đối tượng.
     * <p>
     * Trước đây mỗi {@code resolve*ByHopDongDoiTuong} tự nạp lại: getByIds (enrichList nạp EAV ngầm)
     * + getByHopDongDoiTuongIds (nạp EAV lần nữa) => 2 lần/hàm. Ba endpoint dashboard gọi 2-3 hàm
     * như vậy nối tiếp trên cùng tập id nên tốn tới 4-6 lần nạp EAV mỗi request (scope năm ~109k
     * dòng mỗi lần). Ở đây nạp đúng 1 lần rồi cho các hàm rút ra map cần dùng.
     * <p>
     * Dùng getByIdsLite vì chỉ cần id / doiTuongQuanLyId / hopDongId — không cần EAV, trạng thái,
     * nhóm ưu tiên hay tên nhà thầu mà enrichList kéo theo.
     */
    private GeoSource loadGeoSource(Set<UUID> hopDongDoiTuongIds) {
        Map<UUID, UUID> doiTuongQuanLyByEntity = new HashMap<>();
        Map<UUID, UUID> hopDongByEntity = new HashMap<>();
        Set<UUID> doiTuongQuanLyIds = new HashSet<>();
        for (HopDongDoiTuongResponse entity : hopDongDoiTuongService.getByIdsLite(hopDongDoiTuongIds)) {
            if (entity.getDoiTuongQuanLyId() != null) {
                doiTuongQuanLyByEntity.put(entity.getId(), entity.getDoiTuongQuanLyId());
                doiTuongQuanLyIds.add(entity.getDoiTuongQuanLyId());
            }
            if (entity.getHopDongId() != null) {
                hopDongByEntity.put(entity.getId(), entity.getHopDongId());
            }
        }

        Map<UUID, ThuocTinhLookup> lookupByDoiTuongQuanLy = new HashMap<>();
        for (UUID doiTuongQuanLyId : doiTuongQuanLyIds) {
            lookupByDoiTuongQuanLy.put(doiTuongQuanLyId, buildThuocTinhLookup(doiTuongQuanLyId));
        }

        Map<UUID, Map<UUID, String>> giaTriByEntity = new HashMap<>();
        for (HopDongDoiTuongGiaTriResponse giaTri : hopDongDoiTuongGiaTriService
                .getByHopDongDoiTuongIds(hopDongDoiTuongIds)) {
            if (giaTri.getThuocTinhId() == null || giaTri.getGiaTri() == null) {
                continue;
            }
            giaTriByEntity
                    .computeIfAbsent(giaTri.getHopDongDoiTuongId(), ignored -> new HashMap<>())
                    .put(giaTri.getThuocTinhId(), giaTri.getGiaTri().trim());
        }
        return new GeoSource(doiTuongQuanLyByEntity, hopDongByEntity, lookupByDoiTuongQuanLy, giaTriByEntity);
    }

    private record GeoSource(
            Map<UUID, UUID> doiTuongQuanLyByEntity,
            Map<UUID, UUID> hopDongByEntity,
            Map<UUID, ThuocTinhLookup> lookupByDoiTuongQuanLy,
            Map<UUID, Map<UUID, String>> giaTriByEntity) {
    }

    private static ThuocTinhLookup lookupOf(GeoSource source, UUID hopDongDoiTuongId) {
        UUID doiTuongQuanLyId = source.doiTuongQuanLyByEntity().get(hopDongDoiTuongId);
        return doiTuongQuanLyId == null ? null : source.lookupByDoiTuongQuanLy().get(doiTuongQuanLyId);
    }

    /** Đọc 1 thuộc tính động của đối tượng và parse sang UUID (null nếu thiếu / không phải UUID). */
    private static UUID readAttrUuid(GeoSource source, UUID hopDongDoiTuongId, UUID thuocTinhId) {
        if (thuocTinhId == null) {
            return null;
        }
        return UuidUtils.parseUuid(
                source.giaTriByEntity().getOrDefault(hopDongDoiTuongId, Map.of()).get(thuocTinhId));
    }

    /** Khu vực + tỉnh — dùng cho chi-tiet-trung-tam (cần cả hai) và xu-the-san-luong (chỉ khu vực). */
    private GeoByDoiTuong resolveGeoByHopDongDoiTuong(Set<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds.isEmpty()) {
            return new GeoByDoiTuong(Map.of(), Map.of());
        }
        GeoSource source = loadGeoSource(hopDongDoiTuongIds);

        Map<UUID, UUID> khuVucByDoiTuong = new HashMap<>();
        Map<UUID, UUID> tinhThanhByDoiTuong = new HashMap<>();
        for (UUID hopDongDoiTuongId : hopDongDoiTuongIds) {
            ThuocTinhLookup lookup = lookupOf(source, hopDongDoiTuongId);
            if (lookup == null) {
                continue;
            }
            UUID khuVucId = readAttrUuid(source, hopDongDoiTuongId, lookup.khuVucAttrId());
            if (khuVucId != null) {
                khuVucByDoiTuong.put(hopDongDoiTuongId, khuVucId);
            }
            UUID tinhId = readAttrUuid(source, hopDongDoiTuongId, lookup.tinhThanhAttrId());
            if (tinhId != null) {
                tinhThanhByDoiTuong.put(hopDongDoiTuongId, tinhId);
            }
        }
        return new GeoByDoiTuong(khuVucByDoiTuong, tinhThanhByDoiTuong);
    }

    private record GeoByDoiTuong(
            Map<UUID, UUID> khuVucByDoiTuong,
            Map<UUID, UUID> tinhThanhByDoiTuong) {
    }

    /**
     * Khu vực + loại HĐ — dùng cho theo-linh-vuc. Khu vực lấy từ thuộc tính động, loại HĐ đi qua
     * hopDongId của đối tượng (không phải EAV) nên cần thêm 1 lượt tra hợp đồng.
     */
    private KhuVucLoaiByDoiTuong resolveKhuVucAndLoaiByHopDongDoiTuong(Set<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds.isEmpty()) {
            return new KhuVucLoaiByDoiTuong(Map.of(), Map.of());
        }
        GeoSource source = loadGeoSource(hopDongDoiTuongIds);

        Set<UUID> hopDongIds = new HashSet<>(source.hopDongByEntity().values());
        Map<UUID, UUID> loaiByHopDong = new HashMap<>();
        if (!hopDongIds.isEmpty()) {
            for (HopDongResponse hopDong : hopDongService.getByIds(hopDongIds)) {
                if (hopDong.getLoaiHopDongId() != null) {
                    loaiByHopDong.put(hopDong.getId(), hopDong.getLoaiHopDongId());
                }
            }
        }

        Map<UUID, UUID> khuVucByDoiTuong = new HashMap<>();
        Map<UUID, UUID> loaiByDoiTuong = new HashMap<>();
        for (UUID hopDongDoiTuongId : hopDongDoiTuongIds) {
            ThuocTinhLookup lookup = lookupOf(source, hopDongDoiTuongId);
            if (lookup != null) {
                UUID khuVucId = readAttrUuid(source, hopDongDoiTuongId, lookup.khuVucAttrId());
                if (khuVucId != null) {
                    khuVucByDoiTuong.put(hopDongDoiTuongId, khuVucId);
                }
            }
            UUID hopDongIdCuaDoiTuong = source.hopDongByEntity().get(hopDongDoiTuongId);
            if (hopDongIdCuaDoiTuong != null) {
                UUID loaiId = loaiByHopDong.get(hopDongIdCuaDoiTuong);
                if (loaiId != null) {
                    loaiByDoiTuong.put(hopDongDoiTuongId, loaiId);
                }
            }
        }
        return new KhuVucLoaiByDoiTuong(khuVucByDoiTuong, loaiByDoiTuong);
    }

    private record KhuVucLoaiByDoiTuong(
            Map<UUID, UUID> khuVucByDoiTuong,
            Map<UUID, UUID> loaiByDoiTuong) {
    }

    private List<DashboardSanLuongBatThuongItemResponse> enrichBatThuong(
            List<DashboardSanLuongBatThuongItemResponse> candidates) {
        Set<UUID> hopDongDoiTuongIds = new HashSet<>();
        for (DashboardSanLuongBatThuongItemResponse item : candidates) {
            hopDongDoiTuongIds.add(item.getHopDongDoiTuongId());
        }

        Map<UUID, HopDongDoiTuongResponse> doiTuongById = new HashMap<>();
        Set<UUID> doiTuongQuanLyIds = new HashSet<>();
        for (HopDongDoiTuongResponse entity : hopDongDoiTuongService.getByIds(hopDongDoiTuongIds)) {
            doiTuongById.put(entity.getId(), entity);
            if (entity.getDoiTuongQuanLyId() != null) {
                doiTuongQuanLyIds.add(entity.getDoiTuongQuanLyId());
            }
        }

        Map<UUID, ThuocTinhLookup> lookupByDoiTuong = new HashMap<>();
        for (UUID doiTuongQuanLyId : doiTuongQuanLyIds) {
            lookupByDoiTuong.put(doiTuongQuanLyId, buildThuocTinhLookup(doiTuongQuanLyId));
        }

        Map<UUID, Map<UUID, String>> giaTriByDoiTuong = new HashMap<>();
        Set<UUID> khuVucIds = new HashSet<>();
        for (HopDongDoiTuongGiaTriResponse giaTri : hopDongDoiTuongGiaTriService
                .getByHopDongDoiTuongIds(hopDongDoiTuongIds)) {
            if (giaTri.getThuocTinhId() == null || giaTri.getGiaTri() == null) {
                continue;
            }
            giaTriByDoiTuong
                    .computeIfAbsent(giaTri.getHopDongDoiTuongId(), ignored -> new HashMap<>())
                    .put(giaTri.getThuocTinhId(), giaTri.getGiaTri().trim());
        }

        for (DashboardSanLuongBatThuongItemResponse item : candidates) {
            HopDongDoiTuongResponse entity = doiTuongById.get(item.getHopDongDoiTuongId());
            if (entity == null) {
                continue;
            }
            ThuocTinhLookup lookup = lookupByDoiTuong.get(entity.getDoiTuongQuanLyId());
            Map<UUID, String> giaTri = giaTriByDoiTuong.getOrDefault(item.getHopDongDoiTuongId(), Map.of());
            if (lookup != null && lookup.khuVucAttrId() != null) {
                UUID linked = UuidUtils.parseUuid(giaTri.get(lookup.khuVucAttrId()));
                if (linked != null) {
                    khuVucIds.add(linked);
                }
            }
        }

        Map<UUID, String> khuVucById = new HashMap<>();
        if (!khuVucIds.isEmpty()) {
            for (KhuVucResponse khuVuc : khuVucService.getByIds(khuVucIds)) {
                khuVucById.put(khuVuc.getId(), khuVuc.getTen());
            }
        }

        Set<UUID> daKiemTraIds = new HashSet<>();
        List<SanLuongCanhBaoKiemTra> kiemTraRows =
                sanLuongCanhBaoKiemTraRepository.findByHopDongDoiTuongIdIn(hopDongDoiTuongIds);
        for (SanLuongCanhBaoKiemTra row : kiemTraRows) {
            daKiemTraIds.add(row.getHopDongDoiTuongId());
        }

        List<DashboardSanLuongBatThuongItemResponse> enriched = new ArrayList<>(candidates.size());
        for (DashboardSanLuongBatThuongItemResponse item : candidates) {
            HopDongDoiTuongResponse entity = doiTuongById.get(item.getHopDongDoiTuongId());
            String maTram = "—";
            String khuVuc = "—";
            boolean daQuyetToan = false;
            if (entity != null) {
                ThuocTinhLookup lookup = lookupByDoiTuong.get(entity.getDoiTuongQuanLyId());
                Map<UUID, String> giaTri = giaTriByDoiTuong.getOrDefault(item.getHopDongDoiTuongId(), Map.of());
                maTram = DashboardTinhToanHelper.resolveMaTram(lookup, giaTri);
                khuVuc = DashboardTinhToanHelper.resolveKhuVuc(lookup, giaTri, khuVucById);
                String trangThaiMa = entity.getTrangThaiMa();
                daQuyetToan = trangThaiMa != null
                        && TRANG_THAI_QUYET_TOAN.equalsIgnoreCase(trangThaiMa.trim());
            }
            enriched.add(item.toBuilder()
                    .maTram(maTram)
                    .khuVuc(khuVuc)
                    .daQuyetToan(daQuyetToan)
                    .daKiemTra(daKiemTraIds.contains(item.getHopDongDoiTuongId()))
                    .build());
        }
        return enriched;
    }

    private BigDecimal resolveHeSoNguong(HopDongResponse hopDong, LoaiHopDongResponse xayMoiConLai) {
        if (VolumeConstants.GCCC_LOAI_HOP_DONG_ID.equals(hopDong.getLoaiHopDongId())) {
            return VolumeTinhToanHelper.resolveHeSo(hopDong.getHesoNguong());
        }
        BigDecimal fromHopDong = VolumeTinhToanHelper.resolveHeSo(hopDong.getHesoNguong());
        if (fromHopDong != null) {
            return fromHopDong;
        }
        if (hopDong.getLoaiHopDongId() != null) {
            try {
                LoaiHopDongResponse loai = loaiHopDongService.getById(hopDong.getLoaiHopDongId());
                BigDecimal fromLoai = VolumeTinhToanHelper.resolveHeSo(loai.getHesoNguong());
                if (fromLoai != null) {
                    return fromLoai;
                }
            } catch (Exception ignored) {
                // fallback below
            }
        }
        if (xayMoiConLai != null) {
            return VolumeTinhToanHelper.resolveHeSo(xayMoiConLai.getHesoNguong());
        }
        return VolumeTinhToanHelper.DEFAULT_HESO_GCCC;
    }

    private ThuocTinhLookup buildThuocTinhLookup(UUID doiTuongQuanLyId) {
        UUID primaryAttrId = null;
        UUID khuVucAttrId = null;
        UUID tinhThanhAttrId = null;
        // listLite: chỉ cần ten/kieuDuLieuId/laKhoaChinh, không đọc lienKetBang => tránh N+1 kieu_du_lieu
        List<ThuocTinhResponse> thuocTinhList = thuocTinhService
                .listLite(null, null, false, doiTuongQuanLyId, null).stream()
                .sorted(Comparator.comparing(
                        ThuocTinhResponse::getTen,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
        for (ThuocTinhResponse thuocTinh : thuocTinhList) {
            if (primaryAttrId == null && Boolean.TRUE.equals(thuocTinh.getLaKhoaChinh())) {
                primaryAttrId = thuocTinh.getId();
            }
            if (khuVucAttrId == null && DashboardTinhToanHelper.isKhuVucThuocTinh(thuocTinh)) {
                khuVucAttrId = thuocTinh.getId();
            }
            if (tinhThanhAttrId == null && DashboardTinhToanHelper.isTinhThanhThuocTinh(thuocTinh)) {
                tinhThanhAttrId = thuocTinh.getId();
            }
        }
        return new ThuocTinhLookup(primaryAttrId, khuVucAttrId, tinhThanhAttrId);
    }

    private Map<UUID, List<DashboardTheoDoiTuongItemResponse>> buildTheoDoiTuongByLoai() {
        List<HopDongDoiTuongLoaiThongKeResponse> rows = hopDongDoiTuongService.demActiveNhomTheoLoaiVaDoiTuong();
        Map<UUID, Map<UUID, Long>> countsByLoai = new HashMap<>();
        Set<UUID> doiTuongIds = new HashSet<>();
        for (HopDongDoiTuongLoaiThongKeResponse row : rows) {
            if (row.loaiHopDongId() == null || row.doiTuongQuanLyId() == null) {
                continue;
            }
            countsByLoai
                    .computeIfAbsent(row.loaiHopDongId(), ignored -> new HashMap<>())
                    .put(row.doiTuongQuanLyId(), row.soLuong());
            doiTuongIds.add(row.doiTuongQuanLyId());
        }

        Map<UUID, DoiTuongQuanLyResponse> doiTuongById = new HashMap<>();
        if (!doiTuongIds.isEmpty()) {
            for (DoiTuongQuanLyResponse dt : doiTuongQuanLyService.getByIds(doiTuongIds)) {
                doiTuongById.put(dt.getId(), dt);
            }
        }

        Map<UUID, List<DashboardTheoDoiTuongItemResponse>> result = new HashMap<>();
        for (Map.Entry<UUID, Map<UUID, Long>> entry : countsByLoai.entrySet()) {
            List<DashboardTheoDoiTuongItemResponse> details = new ArrayList<>();
            for (Map.Entry<UUID, Long> dtEntry : entry.getValue().entrySet()) {
                DoiTuongQuanLyResponse dt = doiTuongById.get(dtEntry.getKey());
                details.add(DashboardTheoDoiTuongItemResponse.builder()
                        .doiTuongQuanLyId(dtEntry.getKey())
                        .ma(dt != null ? dt.getMa() : null)
                        .ten(dt != null && dt.getTen() != null && !dt.getTen().isBlank()
                                ? dt.getTen()
                                : "Đối tượng")
                        .soLuong(dtEntry.getValue())
                        .build());
            }
            details.sort(Comparator
                    .comparingLong(DashboardTheoDoiTuongItemResponse::getSoLuong).reversed()
                    .thenComparing(DashboardTheoDoiTuongItemResponse::getTen, String.CASE_INSENSITIVE_ORDER));
            result.put(entry.getKey(), details);
        }
        return result;
    }

    private List<KhuVucResponse> listScopedKhuVucs() {
        return dataScopeService.filterByKhuVuc(khuVucService.list(null, true), KhuVucResponse::getId).stream()
                .sorted(Comparator.comparing(KhuVucResponse::getTen, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
