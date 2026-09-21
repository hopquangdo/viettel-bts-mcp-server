package vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.PaginationDefaults;
import vn.edu.huce.iic.bts_ops_platform.common.security.DataScopeService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.services.SanLuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.cache.TramTonAggregateCache;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.dto.response.TramTonBucketResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.dto.response.TramTonChiTietResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.dto.response.TramTonItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.dto.response.TramTonTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.helpers.TramTonTinhToanHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.services.TramTonService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response.VuongMacResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.services.VuongMacService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.HopDongDoiTuongSnapshot;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.HopDongDoiTuongTonStatRow;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.response.HopDongDoiTuongTonItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongSnapshotService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongTonService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Tổng hợp trạm tồn từ 3 nguồn thuộc đúng module sở hữu nghiệp vụ — KHÔNG tự query nữa:
 * - "Chờ quyết toán"/"Quá hạn": HopDongDoiTuongTonService (core, module hop-dong-doi-tuong).
 * - "Đang vướng mắc": VuongMacService (module vuongmac, dangMoOnly=true).
 * 2 nguồn core (đã HTTC + có pháp lý + chưa quyết toán) có thể trùng với nguồn vướng mắc (1 đối
 * tượng vừa đủ điều kiện core vừa đang có vướng mắc mở) — nguồn vướng mắc được ưu tiên (loại
 * khỏi core) vì "đang vướng mắc" là lý do chưa đủ điều kiện, không tính là chờ quyết toán/quá
 * hạn. "Giá trị tồn" ưu tiên sản lượng báo cáo thực tế (gọi 1 lần cho toàn bộ tập đã gộp),
 * fallback chia đều giá trị HĐ (đã tính sẵn bởi core cho phần cho_qt/qua_han).
 */
@Service
@RequiredArgsConstructor
public class TramTonServiceImpl implements TramTonService {

    private final HopDongDoiTuongTonService hopDongDoiTuongTonService;
    private final VuongMacService vuongMacService;
    private final SanLuongService sanLuongService;
    private final HopDongDoiTuongSnapshotService hopDongDoiTuongSnapshotService;
    private final TramTonAggregateCache tramTonAggregateCache;
    private final DataScopeService dataScopeService;

    private record TramTonRow(
            TramTonItemResponse item,
            boolean duDieuKien,
            String reasonBucketId,
            String agingBucketId,
            UUID vuongMacEnrichId) {

        TramTonRow(TramTonItemResponse item, boolean duDieuKien, String reasonBucketId, String agingBucketId) {
            this(item, duDieuKien, reasonBucketId, agingBucketId, null);
        }
    }

    private record CachedTramTonRows(List<TramTonRow> rows) {
    }

    private record CachedTramTonLiteRows(List<TramTonRow> rows) {
    }

    @Override
    @Transactional(readOnly = true)
    public TramTonTongQuanResponse tongQuan(
            UUID loaiHopDongId,
            String trungTam,
            LocalDate dateFrom,
            LocalDate dateTo,
            Integer quaHanNgay,
            LocalDate ngayBaoCao
    ) {
        trungTam = dataScopeService.resolveTrungTamFilter(trungTam);
        final String scopedTrungTam = trungTam;
        int threshold = TramTonTinhToanHelper.normalizeQuaHanNgay(quaHanNgay);
        LocalDate reportDate = TramTonTinhToanHelper.resolveNgayBaoCao(ngayBaoCao);
        String cacheKey = tongQuanCacheKey(loaiHopDongId, scopedTrungTam, dateFrom, dateTo, threshold, reportDate);

        return tramTonAggregateCache.getOrLoad(cacheKey, TramTonTongQuanResponse.class, () ->
                computeTongQuan(loaiHopDongId, scopedTrungTam, dateFrom, dateTo, threshold, reportDate));
    }

    private TramTonTongQuanResponse computeTongQuan(
            UUID loaiHopDongId,
            String trungTam,
            LocalDate dateFrom,
            LocalDate dateTo,
            int threshold,
            LocalDate reportDate
    ) {
        // — tongQuan chỉ đếm, không hiển thị từng dòng, xem HopDongDoiTuongTonService.
        // choQuyetToanStatsAll). Vướng mắc vẫn dùng bản đầy đủ (tập thường nhỏ hơn nhiều).
        CompletableFuture<List<HopDongDoiTuongTonStatRow>> choQtFuture = supplyAsyncWithSecurityContext(() ->
                hopDongDoiTuongTonService.choQuyetToanStatsAll(loaiHopDongId, dateFrom, dateTo, threshold, reportDate));
        CompletableFuture<List<HopDongDoiTuongTonStatRow>> quaHanFuture = supplyAsyncWithSecurityContext(() ->
                hopDongDoiTuongTonService.quaHanStatsAll(loaiHopDongId, dateFrom, dateTo, threshold, reportDate));
        CompletableFuture<List<HopDongDoiTuongTonStatRow>> chuaPhapLyFuture = supplyAsyncWithSecurityContext(() ->
                hopDongDoiTuongTonService.chuaPhapLyStatsAll(loaiHopDongId, reportDate));
        CompletableFuture<List<VuongMacResponse>> vuongFuture = supplyAsyncWithSecurityContext(() ->
                vuongMacService.danhSachAll(Boolean.TRUE, loaiHopDongId, null, true, threshold, reportDate));
        CompletableFuture.allOf(choQtFuture, quaHanFuture, chuaPhapLyFuture, vuongFuture).join();
        List<HopDongDoiTuongTonStatRow> choQtStats = choQtFuture.join();
        List<HopDongDoiTuongTonStatRow> quaHanStats = quaHanFuture.join();
        List<HopDongDoiTuongTonStatRow> chuaPhapLyStats = chuaPhapLyFuture.join();
        List<VuongMacResponse> vuongList = vuongFuture.join();

        Set<UUID> chuaPhapLyIds = chuaPhapLyStats.stream()
                .map(HopDongDoiTuongTonStatRow::id)
                .collect(java.util.stream.Collectors.toSet());

        String trungTamKey = normalizeTrungTam(trungTam);
        LinkedHashSet<UUID> snapshotIds = new LinkedHashSet<>();
        choQtStats.forEach(row -> snapshotIds.add(row.id()));
        quaHanStats.forEach(row -> snapshotIds.add(row.id()));
        chuaPhapLyStats.forEach(row -> snapshotIds.add(row.id()));
        Map<UUID, HopDongDoiTuongSnapshot> snapshotById = snapshotIds.isEmpty()
                ? Map.of()
                : hopDongDoiTuongSnapshotService.getSnapshots(snapshotIds);

        List<UUID> vuongIds = vuongList.stream()
                .map(VuongMacResponse::getDuLieuDoiTuongId)
                .filter(Objects::nonNull)
                .toList();
        Map<UUID, BigDecimal> sanLuongByDoiTuong = vuongIds.isEmpty()
                ? Map.of()
                : sanLuongService.tongThanhTienTheoDoiTuongIds(vuongIds);

        Map<String, VuongMacResponse> oldestVuongMacByGroup = new LinkedHashMap<>();
        for (VuongMacResponse v : vuongList) {
            if (v.getDuLieuDoiTuongId() != null && chuaPhapLyIds.contains(v.getDuLieuDoiTuongId())) {
                continue;
            }
            if (!matchesTrungTam(v.getRegion(), trungTamKey)) {
                continue;
            }
            String key = groupKeyForVuongMac(v);
            VuongMacResponse existing = oldestVuongMacByGroup.get(key);
            if (existing == null || isOlderVuongMac(v, existing)) {
                oldestVuongMacByGroup.put(key, v);
            }
        }

        long tong = 0;
        long duDk = 0;
        long choQt = 0;
        long chuaDu = 0;
        long quaHan = 0;
        BigDecimal giaTriTon = BigDecimal.ZERO;
        UUID tramLauNhatId = null;
        long tramLauNhatSoNgay = 0;

        Map<String, long[]> aging = initAgingBuckets();
        Map<String, long[]> reasons = initReasonBuckets();
        Map<String, Long> tabCounts = initTabCounts();

        for (HopDongDoiTuongTonStatRow row : choQtStats) {
            if (!statRowMatchesTrungTam(row.id(), snapshotById, trungTamKey)) {
                continue;
            }
            tong++;
            duDk++;
            choQt++;
            giaTriTon = giaTriTon.add(TramTonTinhToanHelper.nz(row.giaTri()));
            tabCounts.merge(TramTonTinhToanHelper.TAB_ALL, 1L, Long::sum);
            tabCounts.merge(TramTonTinhToanHelper.TAB_CHO_QT, 1L, Long::sum);
            bumpBucket(aging, TramTonTinhToanHelper.agingBucketId(row.soNgayTon()), row.giaTri());
            if (row.soNgayTon() > tramLauNhatSoNgay) {
                tramLauNhatSoNgay = row.soNgayTon();
                tramLauNhatId = row.id();
            }
        }
        for (HopDongDoiTuongTonStatRow row : quaHanStats) {
            if (!statRowMatchesTrungTam(row.id(), snapshotById, trungTamKey)) {
                continue;
            }
            tong++;
            duDk++;
            quaHan++;
            giaTriTon = giaTriTon.add(TramTonTinhToanHelper.nz(row.giaTri()));
            tabCounts.merge(TramTonTinhToanHelper.TAB_ALL, 1L, Long::sum);
            tabCounts.merge(TramTonTinhToanHelper.TAB_HAN, 1L, Long::sum);
            bumpBucket(aging, TramTonTinhToanHelper.agingBucketId(row.soNgayTon()), row.giaTri());
            if (row.soNgayTon() > tramLauNhatSoNgay) {
                tramLauNhatSoNgay = row.soNgayTon();
                tramLauNhatId = row.id();
            }
        }
        for (HopDongDoiTuongTonStatRow row : chuaPhapLyStats) {
            if (!statRowMatchesTrungTam(row.id(), snapshotById, trungTamKey)) {
                continue;
            }
            tong++;
            chuaDu++;
            giaTriTon = giaTriTon.add(TramTonTinhToanHelper.nz(row.giaTri()));
            tabCounts.merge(TramTonTinhToanHelper.TAB_ALL, 1L, Long::sum);
            tabCounts.merge(TramTonTinhToanHelper.TAB_PHAP_LY, 1L, Long::sum);
            bumpBucket(reasons, "phap_ly", row.giaTri());
        }
        for (VuongMacResponse v : oldestVuongMacByGroup.values()) {
            TramTonRow row = fromVuongMac(v, reportDate, sanLuongByDoiTuong, 1, v.getId());
            TramTonItemResponse item = row.item();
            tong++;
            chuaDu++;
            giaTriTon = giaTriTon.add(TramTonTinhToanHelper.nz(item.getGiaTri()));
            bumpTabCounts(tabCounts, item);
            bumpBucket(aging, row.agingBucketId(), item.getGiaTri());
            bumpBucket(reasons, row.reasonBucketId(), item.getGiaTri());
        }

        // Mã trạm lâu nhất — chỉ 1 lookup snapshot duy nhất (thay vì resolve cho mọi dòng).
        String tramLauNhatMa = null;
        if (tramLauNhatId != null) {
            HopDongDoiTuongSnapshot snapshot = hopDongDoiTuongSnapshotService.getSnapshot(tramLauNhatId);
            if (snapshot != null && snapshot.getMaDoiTuong() != null && !snapshot.getMaDoiTuong().isBlank()) {
                tramLauNhatMa = snapshot.getMaDoiTuong();
            }
        }

        return TramTonTongQuanResponse.builder()
                .tongSoTram(tong)
                .duDieuKienDt(duDk)
                .choQuyetToan(choQt)
                .chuaDuDieuKien(chuaDu)
                .quaHan(quaHan)
                .quaHanNgay(threshold)
                .ngayBaoCao(reportDate)
                .giaTriTon(giaTriTon)
                .aging(toBuckets(aging, duDk))
                .reasons(toBuckets(reasons, chuaDu))
                .tabCounts(tabCounts)
                .tramLauNhatMa(tramLauNhatMa)
                .tramLauNhatSoNgay(tramLauNhatSoNgay)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TramTonItemResponse> danhSach(
            UUID loaiHopDongId,
            String trungTam,
            LocalDate dateFrom,
            LocalDate dateTo,
            String tab,
            String search,
            Integer page,
            Integer size,
            Integer quaHanNgay,
            LocalDate ngayBaoCao) {
        trungTam = dataScopeService.resolveTrungTamFilter(trungTam);
        final String scopedTrungTam = trungTam;
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE);
        int pageNumber = EntityFilter.normalizePage(page);
        int threshold = TramTonTinhToanHelper.normalizeQuaHanNgay(quaHanNgay);
        LocalDate reportDate = TramTonTinhToanHelper.resolveNgayBaoCao(ngayBaoCao);
        String keyword = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        String trungTamKey = normalizeTrungTam(scopedTrungTam);
        boolean useLitePath = trungTamKey == null && keyword.isEmpty();

        List<TramTonRow> source = useLitePath
                ? loadRowsLiteCached(loaiHopDongId, scopedTrungTam, dateFrom, dateTo, threshold, reportDate)
                : loadRowsCached(loaiHopDongId, scopedTrungTam, dateFrom, dateTo, threshold, reportDate);

        List<TramTonItemResponse> filtered = new ArrayList<>();
        for (TramTonRow row : source) {
            TramTonItemResponse item = row.item();
            if (!TramTonTinhToanHelper.matchesTab(tab, item.getTrangThai(), item.getThieuDieuKien())) {
                continue;
            }
            if (!keyword.isEmpty()) {
                String ma = item.getMaTram() == null ? "" : item.getMaTram().toLowerCase(Locale.ROOT);
                String nhaThau = item.getNhaThau() == null ? "" : item.getNhaThau().toLowerCase(Locale.ROOT);
                String hd = item.getMaHopDong() == null ? "" : item.getMaHopDong().toLowerCase(Locale.ROOT);
                if (!ma.contains(keyword) && !nhaThau.contains(keyword) && !hd.contains(keyword)) {
                    continue;
                }
            }
            filtered.add(item);
        }

        filtered.sort(Comparator
                .comparingLong(TramTonItemResponse::getSoNgayTon).reversed()
                .thenComparing(TramTonItemResponse::getMaTram, Comparator.nullsLast(String::compareToIgnoreCase)));

        long total = filtered.size();
        int from = Math.min(pageNumber * pageSize, filtered.size());
        int to = Math.min(from + pageSize, filtered.size());

        if (!useLitePath || from >= to) {
            return PageResponse.ofItems(filtered.subList(from, to), pageNumber, pageSize, total);
        }

        List<TramTonRow> pageRowRefs = new ArrayList<>();
        Map<UUID, TramTonRow> rowByItemId = new HashMap<>();
        for (TramTonRow row : source) {
            rowByItemId.put(row.item().getId(), row);
        }
        for (TramTonItemResponse item : filtered.subList(from, to)) {
            TramTonRow ref = rowByItemId.get(item.getId());
            if (ref != null) {
                pageRowRefs.add(ref);
            }
        }
        List<TramTonItemResponse> enriched = enrichPageItems(pageRowRefs, reportDate);
        return PageResponse.ofItems(enriched, pageNumber, pageSize, total);
    }

    @Override
    @Transactional(readOnly = true)
    public TramTonChiTietResponse chiTiet(UUID doiTuongId, Integer quaHanNgay, LocalDate ngayBaoCao) {
        return chiTiet(doiTuongId, quaHanNgay, ngayBaoCao, false);
    }

    @Override
    @Transactional(readOnly = true)
    public TramTonChiTietResponse chiTiet(
            UUID doiTuongId,
            Integer quaHanNgay,
            LocalDate ngayBaoCao,
            boolean refresh) {
        if (doiTuongId == null) {
            return TramTonChiTietResponse.builder().build();
        }
        int threshold = TramTonTinhToanHelper.normalizeQuaHanNgay(quaHanNgay);
        LocalDate reportDate = TramTonTinhToanHelper.resolveNgayBaoCao(ngayBaoCao);
        String cacheKey = chiTietCacheKey(doiTuongId, threshold, reportDate);
        if (refresh) {
            tramTonAggregateCache.evict(cacheKey);
        } else {
            Optional<TramTonChiTietResponse> cached =
                    tramTonAggregateCache.get(cacheKey, TramTonChiTietResponse.class);
            if (cached.isPresent()) {
                return cached.get();
            }
        }
        TramTonChiTietResponse loaded = computeChiTiet(doiTuongId, threshold, reportDate);
        tramTonAggregateCache.put(cacheKey, loaded, TramTonAggregateCache.CHI_TIET_TTL);
        return loaded;
    }

    private TramTonChiTietResponse computeChiTiet(UUID doiTuongId, int threshold, LocalDate reportDate) {
        CompletableFuture<HopDongDoiTuongSnapshot> snapshotFuture = supplyAsyncWithSecurityContext(
                () -> hopDongDoiTuongSnapshotService.getSnapshot(doiTuongId));
        CompletableFuture<List<VuongMacResponse>> vuongFuture = supplyAsyncWithSecurityContext(
                () -> vuongMacService.listByDoiTuongIdLite(doiTuongId, true));
        CompletableFuture.allOf(snapshotFuture, vuongFuture).join();

        HopDongDoiTuongSnapshot snapshot = snapshotFuture.join();
        List<VuongMacResponse> vuongMacList = vuongFuture.join();
        TramTonItemResponse matched = resolveChiTietItem(doiTuongId, threshold, reportDate, vuongMacList, snapshot);

        List<TramTonChiTietResponse.TramTonVuongMacItem> vmItems = vuongMacList.stream()
                .map(v -> TramTonChiTietResponse.TramTonVuongMacItem.builder()
                        .id(v.getId())
                        .giaiDoan(v.getGiaiDoan())
                        .kieuVuongMac(v.getKieuVuongMac())
                        .coTheBoSungSanLuong(v.getCoTheBoSungSanLuong())
                        .moTa(v.getMoTa())
                        .moTaDayDu(v.getMoTaDayDu())
                        .trangThai(v.getTrangThai())
                        .ngayTao(v.getNgayTao())
                        .tenNguoiXuLy(v.getTenNguoiXuLy())
                        .build())
                .toList();

        if (matched != null) {
            return TramTonChiTietResponse.builder()
                    .id(matched.getId())
                    .hopDongId(matched.getHopDongId())
                    .maTram(matched.getMaTram())
                    .maHopDong(matched.getMaHopDong())
                    .khuVuc(matched.getKhuVuc())
                    .nhaThau(matched.getNhaThau())
                    .trangThai(matched.getTrangThai())
                    .lyDoTon(matched.getLyDoTon())
                    .soNgayTon(matched.getSoNgayTon())
                    .thieuDieuKien(matched.getThieuDieuKien())
                    .vuongMacList(vmItems)
                    .build();
        }

        String maTram = snapshot != null && snapshot.getMaDoiTuong() != null ? snapshot.getMaDoiTuong() : "—";
        return TramTonChiTietResponse.builder()
                .id(doiTuongId)
                .maTram(maTram)
                .lyDoTon(vmItems.isEmpty() ? "—" : resolveLyDoVuongMac(vuongMacList.get(0), vmItems.size()))
                .vuongMacList(vmItems)
                .build();
    }

    private List<TramTonRow> loadRowsCached(
            UUID loaiHopDongId,
            String trungTam,
            LocalDate dateFrom,
            LocalDate dateTo,
            int quaHanNgay,
            LocalDate ngayBaoCao) {
        String cacheKey = rowsCacheKey(loaiHopDongId, trungTam, dateFrom, dateTo, quaHanNgay, ngayBaoCao);
        CachedTramTonRows cached = tramTonAggregateCache.getOrLoad(cacheKey, CachedTramTonRows.class, () ->
                new CachedTramTonRows(loadRows(loaiHopDongId, trungTam, dateFrom, dateTo, quaHanNgay, ngayBaoCao)));
        return cached.rows();
    }

    private List<TramTonRow> loadRowsLiteCached(
            UUID loaiHopDongId,
            String trungTam,
            LocalDate dateFrom,
            LocalDate dateTo,
            int quaHanNgay,
            LocalDate ngayBaoCao) {
        String cacheKey = "rows-lite|" + rowsCacheKey(loaiHopDongId, trungTam, dateFrom, dateTo, quaHanNgay, ngayBaoCao);
        CachedTramTonLiteRows cached = tramTonAggregateCache.getOrLoad(cacheKey, CachedTramTonLiteRows.class, () ->
                new CachedTramTonLiteRows(loadRowsLite(loaiHopDongId, trungTam, dateFrom, dateTo, quaHanNgay, ngayBaoCao)));
        return cached.rows();
    }

    private List<TramTonItemResponse> enrichPageItems(List<TramTonRow> page, LocalDate reportDate) {
        if (page.isEmpty()) {
            return List.of();
        }
        List<UUID> coreIds = new ArrayList<>();
        List<UUID> vuongMacIds = new ArrayList<>();
        for (TramTonRow row : page) {
            if (row.vuongMacEnrichId() != null) {
                vuongMacIds.add(row.vuongMacEnrichId());
            } else {
                coreIds.add(row.item().getId());
            }
        }
        Map<UUID, HopDongDoiTuongSnapshot> snapshots = coreIds.isEmpty()
                ? Map.of()
                : hopDongDoiTuongSnapshotService.getSnapshots(coreIds);
        Map<UUID, VuongMacResponse> vuongById = vuongMacIds.isEmpty()
                ? Map.of()
                : vuongMacService.enrichByIds(vuongMacIds).stream()
                        .collect(java.util.stream.Collectors.toMap(VuongMacResponse::getId, v -> v, (a, b) -> a));

        List<UUID> vuongDoiTuongIds = page.stream()
                .filter(row -> row.vuongMacEnrichId() != null)
                .map(row -> row.item().getId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, BigDecimal> sanLuongByDoiTuong = vuongDoiTuongIds.isEmpty()
                ? Map.of()
                : sanLuongService.tongThanhTienTheoDoiTuongIds(vuongDoiTuongIds);

        List<TramTonItemResponse> enriched = new ArrayList<>(page.size());
        for (TramTonRow row : page) {
            if (row.vuongMacEnrichId() != null) {
                VuongMacResponse v = vuongById.get(row.vuongMacEnrichId());
                if (v != null) {
                    int vmCount = row.item().getSoVuongMacMo() > 0 ? row.item().getSoVuongMacMo() : 1;
                    enriched.add(fromVuongMac(v, reportDate, sanLuongByDoiTuong, vmCount, null).item());
                    continue;
                }
            }
            enriched.add(withSnapshot(row.item(), snapshots.get(row.item().getId())));
        }
        return enriched;
    }

    private static TramTonItemResponse withSnapshot(TramTonItemResponse base, HopDongDoiTuongSnapshot snapshot) {
        if (snapshot == null) {
            return base;
        }
        return TramTonItemResponse.builder()
                .id(base.getId())
                .hopDongId(base.getHopDongId())
                .maTram(snapshot.getMaDoiTuong() != null && !snapshot.getMaDoiTuong().isBlank()
                        ? snapshot.getMaDoiTuong() : base.getMaTram())
                .khuVuc(snapshot.getKhuVuc() != null && !snapshot.getKhuVuc().isBlank()
                        ? snapshot.getKhuVuc() : base.getKhuVuc())
                .nhaThau(snapshot.getNhaThau() != null && !snapshot.getNhaThau().isBlank()
                        ? snapshot.getNhaThau() : base.getNhaThau())
                .maHopDong(base.getMaHopDong())
                .giaTri(base.getGiaTri())
                .ngayHtTc(base.getNgayHtTc())
                .ngayHtTcFallback(base.isNgayHtTcFallback())
                .soNgayTon(base.getSoNgayTon())
                .thieuDieuKien(base.getThieuDieuKien())
                .trangThai(base.getTrangThai())
                .nguoiPhuTrach(base.getNguoiPhuTrach())
                .lyDoTon(base.getLyDoTon())
                .soVuongMacMo(base.getSoVuongMacMo())
                .build();
    }

    private TramTonItemResponse resolveChiTietItem(
            UUID doiTuongId,
            int threshold,
            LocalDate reportDate,
            List<VuongMacResponse> vuongMacList,
            HopDongDoiTuongSnapshot snapshot) {
        Optional<HopDongDoiTuongTonItemResponse> phapLyOpt =
                hopDongDoiTuongTonService.findChuaPhapLyChiTietByDoiTuongId(doiTuongId, reportDate, snapshot);
        if (phapLyOpt.isPresent()) {
            return fromChuaPhapLy(phapLyOpt.get()).item();
        }
        if (vuongMacList != null && !vuongMacList.isEmpty()) {
            VuongMacResponse oldest = vuongMacList.get(0);
            for (VuongMacResponse candidate : vuongMacList) {
                if (isOlderVuongMac(candidate, oldest)) {
                    oldest = candidate;
                }
            }
            return fromVuongMac(oldest, reportDate, Map.of(), vuongMacList.size(), null).item();
        }
        return hopDongDoiTuongTonService.findQuaHanOrChoQtChiTietByDoiTuongId(
                        doiTuongId, null, null, threshold, reportDate, snapshot)
                .map(core -> fromCore(core).item())
                .orElse(null);
    }

    private static String chiTietCacheKey(UUID doiTuongId, int quaHanNgay, LocalDate ngayBaoCao) {
        return "chi-tiet|"
                + doiTuongId
                + "|"
                + quaHanNgay
                + "|"
                + (ngayBaoCao != null ? ngayBaoCao : "");
    }

    private static String rowsCacheKey(
            UUID loaiHopDongId,
            String trungTam,
            LocalDate dateFrom,
            LocalDate dateTo,
            int quaHanNgay,
            LocalDate ngayBaoCao) {
        return "rows|"
                + (loaiHopDongId != null ? loaiHopDongId : "")
                + "|" + (trungTam != null ? trungTam : "")
                + "|" + (dateFrom != null ? dateFrom : "")
                + "|" + (dateTo != null ? dateTo : "")
                + "|" + quaHanNgay
                + "|" + (ngayBaoCao != null ? ngayBaoCao : "");
    }

    private static String tongQuanCacheKey(
            UUID loaiHopDongId,
            String trungTam,
            LocalDate dateFrom,
            LocalDate dateTo,
            int quaHanNgay,
            LocalDate ngayBaoCao) {
        return "tong-quan|" + rowsCacheKey(loaiHopDongId, trungTam, dateFrom, dateTo, quaHanNgay, ngayBaoCao);
    }

    private List<TramTonRow> loadRowsLite(
            UUID loaiHopDongId,
            String trungTam,
            LocalDate dateFrom,
            LocalDate dateTo,
            int quaHanNgay,
            LocalDate ngayBaoCao) {
        LocalDate today = ngayBaoCao != null ? ngayBaoCao : LocalDate.now(TramTonTinhToanHelper.ZONE);

        CompletableFuture<List<HopDongDoiTuongTonItemResponse>> choQtFuture = supplyAsyncWithSecurityContext(
                () -> hopDongDoiTuongTonService.choQuyetToanLiteAll(loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao));
        CompletableFuture<List<HopDongDoiTuongTonItemResponse>> quaHanFuture = supplyAsyncWithSecurityContext(
                () -> hopDongDoiTuongTonService.quaHanLiteAll(loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao));
        CompletableFuture<List<HopDongDoiTuongTonItemResponse>> chuaPhapLyFuture = supplyAsyncWithSecurityContext(
                () -> hopDongDoiTuongTonService.chuaPhapLyLiteAll(loaiHopDongId, ngayBaoCao));
        CompletableFuture<List<VuongMacResponse>> vuongFuture = supplyAsyncWithSecurityContext(
                () -> vuongMacService.danhSachOpenLiteForTramTon(loaiHopDongId, quaHanNgay, ngayBaoCao));
        CompletableFuture.allOf(choQtFuture, quaHanFuture, chuaPhapLyFuture, vuongFuture).join();
        List<HopDongDoiTuongTonItemResponse> choQt = choQtFuture.join();
        List<HopDongDoiTuongTonItemResponse> quaHanList = quaHanFuture.join();
        List<HopDongDoiTuongTonItemResponse> chuaPhapLyList = chuaPhapLyFuture.join();
        List<VuongMacResponse> vuongList = vuongFuture.join();

        Set<UUID> chuaPhapLyIds = chuaPhapLyList.stream()
                .map(HopDongDoiTuongTonItemResponse::getId)
                .collect(java.util.stream.Collectors.toSet());

        String trungTamKey = normalizeTrungTam(trungTam);
        List<TramTonRow> rows = new ArrayList<>();

        for (HopDongDoiTuongTonItemResponse it : choQt) {
            rows.add(fromCore(it));
        }
        for (HopDongDoiTuongTonItemResponse it : quaHanList) {
            rows.add(fromCore(it));
        }
        for (HopDongDoiTuongTonItemResponse it : chuaPhapLyList) {
            rows.add(fromChuaPhapLy(it));
        }

        Map<String, Integer> vuongCountByGroupKey = new HashMap<>();
        for (VuongMacResponse v : vuongList) {
            if (v.getDuLieuDoiTuongId() != null && chuaPhapLyIds.contains(v.getDuLieuDoiTuongId())) {
                continue;
            }
            vuongCountByGroupKey.merge(groupKeyForVuongMac(v), 1, Integer::sum);
        }
        Map<String, VuongMacResponse> oldestVuongMacByDoiTuong = new LinkedHashMap<>();
        for (VuongMacResponse v : vuongList) {
            if (v.getDuLieuDoiTuongId() != null && chuaPhapLyIds.contains(v.getDuLieuDoiTuongId())) {
                continue;
            }
            String key = groupKeyForVuongMac(v);
            VuongMacResponse existing = oldestVuongMacByDoiTuong.get(key);
            if (existing == null || isOlderVuongMac(v, existing)) {
                oldestVuongMacByDoiTuong.put(key, v);
            }
        }
        for (VuongMacResponse v : oldestVuongMacByDoiTuong.values()) {
            int vmCount = vuongCountByGroupKey.getOrDefault(groupKeyForVuongMac(v), 1);
            rows.add(fromVuongMac(v, today, Map.of(), vmCount, v.getId()));
        }
        return rows;
    }

    private List<TramTonRow> loadRows(
            UUID loaiHopDongId,
            String trungTam,
            LocalDate dateFrom,
            LocalDate dateTo,
            int quaHanNgay,
            LocalDate ngayBaoCao) {
        LocalDate today = ngayBaoCao != null ? ngayBaoCao : LocalDate.now(TramTonTinhToanHelper.ZONE);

        // 3 nguồn chạy song song — mỗi API tự lo phần dữ liệu/nghiệp vụ của module mình.
        CompletableFuture<List<HopDongDoiTuongTonItemResponse>> choQtFuture = supplyAsyncWithSecurityContext(
                () -> hopDongDoiTuongTonService.choQuyetToanAll(loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao));
        CompletableFuture<List<HopDongDoiTuongTonItemResponse>> quaHanFuture = supplyAsyncWithSecurityContext(
                () -> hopDongDoiTuongTonService.quaHanAll(loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao));
        CompletableFuture<List<HopDongDoiTuongTonItemResponse>> chuaPhapLyFuture = supplyAsyncWithSecurityContext(
                () -> hopDongDoiTuongTonService.chuaPhapLyAll(loaiHopDongId, ngayBaoCao));
        CompletableFuture<List<VuongMacResponse>> vuongFuture = supplyAsyncWithSecurityContext(
                () -> vuongMacService.danhSachAll(Boolean.TRUE, loaiHopDongId, null, true, quaHanNgay, ngayBaoCao));
        CompletableFuture.allOf(choQtFuture, quaHanFuture, chuaPhapLyFuture, vuongFuture).join();
        List<HopDongDoiTuongTonItemResponse> choQt = choQtFuture.join();
        List<HopDongDoiTuongTonItemResponse> quaHanList = quaHanFuture.join();
        List<HopDongDoiTuongTonItemResponse> chuaPhapLyList = chuaPhapLyFuture.join();
        List<VuongMacResponse> vuongList = vuongFuture.join();

        Set<UUID> chuaPhapLyIds = chuaPhapLyList.stream()
                .map(HopDongDoiTuongTonItemResponse::getId)
                .collect(java.util.stream.Collectors.toSet());

        // "Đang vướng mắc" đã bị loại thẳng trong SQL (co_vuong_mac_mo = FALSE) — không cần tự
        // loại trừ ở đây nữa. Giá trị tồn của choQt/quaHanList cũng đã có sẵn (san_luong_hieu_luc
        // denormalize), chỉ còn vướng mắc (vuongList) cần tra sản lượng riêng.
        List<UUID> vuongIds = vuongList.stream()
                .map(VuongMacResponse::getDuLieuDoiTuongId)
                .filter(Objects::nonNull)
                .toList();
        Map<UUID, BigDecimal> sanLuongByDoiTuong = vuongIds.isEmpty()
                ? Map.of()
                : sanLuongService.tongThanhTienTheoDoiTuongIds(vuongIds);

        String trungTamKey = normalizeTrungTam(trungTam);
        List<TramTonRow> rows = new ArrayList<>();

        for (HopDongDoiTuongTonItemResponse it : choQt) {
            if (!matchesTrungTam(it.getKhuVuc(), trungTamKey)) {
                continue;
            }
            rows.add(fromCore(it));
        }
        for (HopDongDoiTuongTonItemResponse it : quaHanList) {
            if (!matchesTrungTam(it.getKhuVuc(), trungTamKey)) {
                continue;
            }
            rows.add(fromCore(it));
        }
        for (HopDongDoiTuongTonItemResponse it : chuaPhapLyList) {
            if (!matchesTrungTam(it.getKhuVuc(), trungTamKey)) {
                continue;
            }
            rows.add(fromChuaPhapLy(it));
        }
        Map<String, Integer> vuongCountByGroupKey = new HashMap<>();
        for (VuongMacResponse v : vuongList) {
            if (v.getDuLieuDoiTuongId() != null && chuaPhapLyIds.contains(v.getDuLieuDoiTuongId())) {
                continue;
            }
            if (!matchesTrungTam(v.getRegion(), trungTamKey)) {
                continue;
            }
            String key = groupKeyForVuongMac(v);
            vuongCountByGroupKey.merge(key, 1, Integer::sum);
        }
        // Gộp theo đối tượng — 1 trạm có thể có NHIỀU vướng mắc mở cùng lúc, nhưng "trạm tồn"
        // chỉ cần hiện 1 dòng/trạm. Giữ lại vướng mắc TỒN LÂU NHẤT (ngayTao sớm nhất) làm đại
        // diện cho dòng đó — phản ánh đúng "trạng thái tồn đọng nghiêm trọng nhất".
        // Key gộp dùng mã nghiệp vụ (mã đối tượng + mã hợp đồng) thay vì duLieuDoiTuongId kỹ
        // thuật, vì dữ liệu hop_dong_doi_tuong có thể có nhiều bản ghi trùng (id khác nhau)
        // cho cùng 1 trạm/hợp đồng thực tế, gây hiện nhiều dòng trùng lặp trên UI.
        Map<String, VuongMacResponse> oldestVuongMacByDoiTuong = new LinkedHashMap<>();
        for (VuongMacResponse v : vuongList) {
            if (v.getDuLieuDoiTuongId() != null && chuaPhapLyIds.contains(v.getDuLieuDoiTuongId())) {
                continue;
            }
            if (!matchesTrungTam(v.getRegion(), trungTamKey)) {
                continue;
            }
            String key = groupKeyForVuongMac(v);
            VuongMacResponse existing = oldestVuongMacByDoiTuong.get(key);
            if (existing == null || isOlderVuongMac(v, existing)) {
                oldestVuongMacByDoiTuong.put(key, v);
            }
        }
        for (VuongMacResponse v : oldestVuongMacByDoiTuong.values()) {
            int vmCount = vuongCountByGroupKey.getOrDefault(groupKeyForVuongMac(v), 1);
            rows.add(fromVuongMac(v, today, sanLuongByDoiTuong, vmCount, null));
        }
        return rows;
    }

    /**
     * Khóa gộp theo mã nghiệp vụ (mã đối tượng + mã hợp đồng) khi có đủ, để gộp đúng cả khi
     * duLieuDoiTuongId khác nhau giữa các bản ghi trùng lặp; fallback về duLieuDoiTuongId/id
     * kỹ thuật khi thiếu mã nghiệp vụ.
     */
    private static String groupKeyForVuongMac(VuongMacResponse v) {
        String maDoiTuong = v.getMaDoiTuong();
        String maHopDong = v.getMaHopDong();
        if (maDoiTuong != null && !maDoiTuong.isBlank() && maHopDong != null && !maHopDong.isBlank()) {
            return maDoiTuong + "|" + maHopDong;
        }
        UUID fallback = v.getDuLieuDoiTuongId() != null ? v.getDuLieuDoiTuongId() : v.getId();
        return "id:" + fallback;
    }

    /** true nếu candidate tồn lâu hơn (ngayTao sớm hơn) current — null ngayTao coi như mới nhất. */
    private static boolean isOlderVuongMac(VuongMacResponse candidate, VuongMacResponse current) {
        if (candidate.getNgayTao() == null) {
            return false;
        }
        if (current.getNgayTao() == null) {
            return true;
        }
        return candidate.getNgayTao().isBefore(current.getNgayTao());
    }

    private static TramTonRow fromCore(HopDongDoiTuongTonItemResponse it) {
        BigDecimal giaTri = resolveGiaTri(it.getSanLuongHieuLuc(), it.getGiaTri());
        boolean quaHan = "qua_han".equals(it.getTrangThai());
        String trangThai = quaHan ? TramTonTinhToanHelper.ST_QUA_HAN : TramTonTinhToanHelper.ST_CHO_QT;
        String lyDo = quaHan
                ? "Quá hạn quyết toán sau hoàn thành thi công"
                : "Đủ điều kiện DT, chờ quyết toán";
        TramTonItemResponse item = TramTonItemResponse.builder()
                .id(it.getId())
                .hopDongId(it.getHopDongId())
                .maTram(it.getMaTram())
                .khuVuc(it.getKhuVuc())
                .nhaThau(it.getNhaThau())
                .maHopDong(it.getMaHopDong())
                .giaTri(giaTri)
                .ngayHtTc(it.getNgayHtTc())
                .ngayHtTcFallback(false)
                .soNgayTon(it.getSoNgayTon())
                .thieuDieuKien(List.of())
                .trangThai(trangThai)
                .nguoiPhuTrach("—")
                .lyDoTon(lyDo)
                .soVuongMacMo(0)
                .build();
        return new TramTonRow(item, true, null, TramTonTinhToanHelper.agingBucketId(it.getSoNgayTon()));
    }

    private static TramTonRow fromChuaPhapLy(HopDongDoiTuongTonItemResponse it) {
        BigDecimal giaTri = resolveGiaTri(it.getSanLuongHieuLuc(), it.getGiaTri());
        List<String> thieu = List.of(TramTonTinhToanHelper.DK_PHAP_LY);
        TramTonItemResponse item = TramTonItemResponse.builder()
                .id(it.getId())
                .hopDongId(it.getHopDongId())
                .maTram(it.getMaTram())
                .khuVuc(it.getKhuVuc())
                .nhaThau(it.getNhaThau())
                .maHopDong(it.getMaHopDong())
                .giaTri(giaTri)
                .ngayHtTc(it.getNgayHtTc())
                .ngayHtTcFallback(false)
                .soNgayTon(it.getSoNgayTon())
                .thieuDieuKien(thieu)
                .trangThai(TramTonTinhToanHelper.ST_PHAP_LY)
                .nguoiPhuTrach("—")
                .lyDoTon("Hợp đồng chưa có pháp lý")
                .soVuongMacMo(0)
                .build();
        return new TramTonRow(item, false, TramTonTinhToanHelper.reasonBucketId(thieu),
                TramTonTinhToanHelper.agingBucketId(it.getSoNgayTon()));
    }

    private static TramTonRow fromVuongMac(
            VuongMacResponse v, LocalDate today, Map<UUID, BigDecimal> sanLuongByDoiTuong, int soVuongMacMo,
            UUID vuongMacEnrichId) {
        LocalDate ngayTao = v.getNgayTao() != null
                ? ZonedDateTime.ofInstant(v.getNgayTao(), TramTonTinhToanHelper.ZONE).toLocalDate()
                : null;
        long soNgay = TramTonTinhToanHelper.soNgayTon(ngayTao, today);
        List<String> thieu = List.of(TramTonTinhToanHelper.DK_VUONG);
        BigDecimal giaTri = resolveGiaTri(
                v.getDuLieuDoiTuongId() != null ? sanLuongByDoiTuong.get(v.getDuLieuDoiTuongId()) : null,
                BigDecimal.ZERO);
        String lyDo = resolveLyDoVuongMac(v, soVuongMacMo);

        TramTonItemResponse item = TramTonItemResponse.builder()
                .id(v.getDuLieuDoiTuongId() != null ? v.getDuLieuDoiTuongId() : v.getId())
                .hopDongId(v.getHopDongId())
                .maTram(v.getMaTram() != null && !v.getMaTram().isBlank() ? v.getMaTram() : "—")
                .khuVuc(v.getRegion() != null && !v.getRegion().isBlank() ? v.getRegion() : "—")
                .nhaThau(v.getNhaThau() != null && !v.getNhaThau().isBlank() ? v.getNhaThau() : "—")
                .maHopDong(v.getMaHopDong() != null && !v.getMaHopDong().isBlank() ? v.getMaHopDong() : "—")
                .giaTri(giaTri)
                .ngayHtTc(null)
                .ngayHtTcFallback(false)
                .soNgayTon(soNgay)
                .thieuDieuKien(thieu)
                .trangThai(TramTonTinhToanHelper.ST_VUONG)
                .nguoiPhuTrach(v.getTenNguoiXuLy() != null && !v.getTenNguoiXuLy().isBlank() ? v.getTenNguoiXuLy() : "—")
                .lyDoTon(lyDo)
                .soVuongMacMo(soVuongMacMo)
                .build();
        return new TramTonRow(item, false, TramTonTinhToanHelper.reasonBucketId(thieu),
                TramTonTinhToanHelper.agingBucketId(soNgay), vuongMacEnrichId);
    }

    private static String resolveLyDoVuongMac(VuongMacResponse v, int soVuongMacMo) {
        String moTa = firstNonBlank(v.getMoTa(), v.getMoTaDayDu());
        if (moTa != null) {
            if (soVuongMacMo > 1) {
                return moTa + " (+" + (soVuongMacMo - 1) + " vướng mắc khác)";
            }
            return moTa;
        }
        if (v.getGiaiDoan() != null && !v.getGiaiDoan().isBlank()) {
            return "Vướng mắc giai đoạn " + v.getGiaiDoan();
        }
        return "Đang vướng mắc, chưa quyết toán được";
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static BigDecimal resolveGiaTri(BigDecimal reported, BigDecimal fallback) {
        if (reported != null && reported.compareTo(BigDecimal.ZERO) > 0) {
            return reported;
        }
        return TramTonTinhToanHelper.nz(fallback);
    }

    private static boolean statRowMatchesTrungTam(
            UUID id, Map<UUID, HopDongDoiTuongSnapshot> snapshots, String trungTamKey) {
        if (trungTamKey == null) {
            return true;
        }
        HopDongDoiTuongSnapshot snapshot = snapshots.get(id);
        return matchesTrungTam(snapshot != null ? snapshot.getKhuVuc() : null, trungTamKey);
    }

    private static String normalizeTrungTam(String trungTam) {
        return trungTam == null || trungTam.isBlank() || "all".equalsIgnoreCase(trungTam)
                ? null
                : trungTam.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean matchesTrungTam(String khuVuc, String trungTamKey) {
        if (trungTamKey == null) {
            return true;
        }
        String regionKey = khuVuc == null ? "" : khuVuc.trim().toLowerCase(Locale.ROOT);
        return regionKey.contains(trungTamKey);
    }

    private static Map<String, long[]> initAgingBuckets() {
        Map<String, long[]> map = new LinkedHashMap<>();
        map.put("lt_1w", new long[]{0, 0});
        map.put("1_2w", new long[]{0, 0});
        map.put("2_4w", new long[]{0, 0});
        map.put("1_3m", new long[]{0, 0});
        map.put("gt_3m", new long[]{0, 0});
        return map;
    }

    private static Map<String, Long> initTabCounts() {
        Map<String, Long> map = new LinkedHashMap<>();
        map.put(TramTonTinhToanHelper.TAB_ALL, 0L);
        map.put(TramTonTinhToanHelper.TAB_CHO_QT, 0L);
        map.put(TramTonTinhToanHelper.TAB_PHAP_LY, 0L);
        map.put(TramTonTinhToanHelper.TAB_VUONG, 0L);
        map.put(TramTonTinhToanHelper.TAB_THIEU, 0L);
        map.put(TramTonTinhToanHelper.TAB_HAN, 0L);
        return map;
    }

    private static void bumpTabCounts(Map<String, Long> tabCounts, TramTonItemResponse item) {
        for (String tab : tabCounts.keySet()) {
            if (TramTonTinhToanHelper.matchesTab(tab, item.getTrangThai(), item.getThieuDieuKien())) {
                tabCounts.put(tab, tabCounts.get(tab) + 1);
            }
        }
    }

    private static Map<String, long[]> initReasonBuckets() {
        Map<String, long[]> map = new LinkedHashMap<>();
        map.put("httc", new long[]{0, 0});
        map.put("vuong", new long[]{0, 0});
        map.put("phap_ly", new long[]{0, 0});
        map.put("thieu_2", new long[]{0, 0});
        return map;
    }

    private static void bumpBucket(Map<String, long[]> buckets, String id, BigDecimal value) {
        if (id == null) {
            return;
        }
        long[] slot = buckets.get(id);
        if (slot == null) {
            return;
        }
        slot[0] += 1;
        slot[1] += TramTonTinhToanHelper.nz(value).longValue();
    }

    private static List<TramTonBucketResponse> toBuckets(Map<String, long[]> source, long total) {
        Map<String, String> labels = Map.of(
                "lt_1w", "< 1 Tuần",
                "1_2w", "1 - 2 Tuần",
                "2_4w", "2 - 4 Tuần",
                "1_3m", "1 - 3 Tháng",
                "gt_3m", "> 3 Tháng",
                "httc", "Chưa HT thi công",
                "vuong", "Đang vướng mắc",
                "phap_ly", "Chưa có pháp lý",
                "thieu_2", "Thiếu 2+ điều kiện"
        );
        List<TramTonBucketResponse> result = new ArrayList<>();
        for (Map.Entry<String, long[]> e : source.entrySet()) {
            long count = e.getValue()[0];
            result.add(TramTonBucketResponse.builder()
                    .id(e.getKey())
                    .label(labels.getOrDefault(e.getKey(), e.getKey()))
                    .count(count)
                    .value(BigDecimal.valueOf(e.getValue()[1]))
                    .percent(TramTonTinhToanHelper.percent(count, total))
                    .build());
        }
        return result;
    }

    /** ForkJoinPool mặc định không mang SecurityContext — cần copy JWT sang thread con. */
    private static <T> CompletableFuture<T> supplyAsyncWithSecurityContext(Supplier<T> supplier) {
        SecurityContext captured = SecurityContextHolder.getContext();
        return CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.setContext(captured);
            try {
                return supplier.get();
            } finally {
                SecurityContextHolder.clearContext();
            }
        });
    }

}
