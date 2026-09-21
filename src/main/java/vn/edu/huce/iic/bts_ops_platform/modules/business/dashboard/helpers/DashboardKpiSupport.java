package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.util.UuidUtils;
import vn.edu.huce.iic.bts_ops_platform.config.AppCacheProperties;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.DashboardKpiSnapshot;
import vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto.ThuocTinhLookup;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucHopDongTreeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucNhomService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongGiaTriResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongGiaTriService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.HangMucThanhTienResult;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.util.HangMucThanhTienCalculator;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KPI dashboard = tổng thành tiền hạng mục HĐ (định mức / đối tượng), cùng quy tắc module Volume.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardKpiSupport {

    private final HopDongService hopDongService;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final HopDongDoiTuongGiaTriService hopDongDoiTuongGiaTriService;
    private final HangMucNhomService hangMucNhomService;
    private final ThuocTinhService thuocTinhService;
    private final AppCacheProperties cacheProperties;

    /** Lookup thuộc tính theo loại đối tượng — ít thay đổi. */
    private final Map<UUID, ThuocTinhLookup> thuocTinhLookupCache = new ConcurrentHashMap<>();

    /** Cache KPI theo scope — tránh tính lại nhiều API / request dashboard. */
    private final ConcurrentHashMap<String, TimedSnapshot> snapshotCache = new ConcurrentHashMap<>();

    private record TimedSnapshot(DashboardKpiSnapshot snapshot, long expiresAtMs) {
    }

    @Transactional(readOnly = true)
    public DashboardKpiSnapshot buildSnapshot(UUID hopDongId, UUID loaiHopDongId) {
        String cacheKey = cacheKey(hopDongId, loaiHopDongId);
        long now = System.currentTimeMillis();
        TimedSnapshot hit = snapshotCache.get(cacheKey);
        if (hit != null && hit.expiresAtMs() > now) {
            return hit.snapshot();
        }
        return snapshotCache.compute(cacheKey, (key, existing) -> {
            if (existing != null && existing.expiresAtMs() > System.currentTimeMillis()) {
                return existing;
            }
            long ttlMs = cacheProperties.volumeBatchTtl().toMillis();
            return new TimedSnapshot(computeSnapshot(hopDongId, loaiHopDongId), System.currentTimeMillis() + ttlMs);
        }).snapshot();
    }

    private static String cacheKey(UUID hopDongId, UUID loaiHopDongId) {
        return (hopDongId != null ? hopDongId : "_") + ":" + (loaiHopDongId != null ? loaiHopDongId : "_");
    }

    private DashboardKpiSnapshot computeSnapshot(UUID hopDongId, UUID loaiHopDongId) {
        try {
            List<HopDongResponse> hopDongs = loadHopDongs(hopDongId, loaiHopDongId);
            if (hopDongs.isEmpty()) {
                return DashboardKpiSnapshot.empty();
            }

            Set<UUID> allowedHopDongIds = new HashSet<>();
            Map<UUID, UUID> loaiByHopDong = new HashMap<>();
            for (HopDongResponse hopDong : hopDongs) {
                allowedHopDongIds.add(hopDong.getId());
                if (hopDong.getLoaiHopDongId() != null) {
                    loaiByHopDong.put(hopDong.getId(), hopDong.getLoaiHopDongId());
                }
            }

            // Bản Lite: dưới đây chỉ đọc id / hopDongId / doiTuongQuanLyId. Bản đầy đủ chạy
            // enrichList — nạp EAV + trạng thái + nhóm ưu tiên + tên nhà thầu cho TOÀN BỘ ~25k
            // đối tượng, tức ~337k dòng EAV phải map sang DTO mà không dùng đến.
            // Lưu ý cho người sửa sau: enrichListLite KHÔNG set trangThaiMa / nhomUuTienTen /
            // nhaThauTen / giaTri — cần trường nào trong số đó thì phải quay lại bản đầy đủ.
            List<HopDongDoiTuongResponse> objects =
                    hopDongDoiTuongService.listActiveLiteByHopDongIds(allowedHopDongIds);
            if (objects.isEmpty()) {
                return DashboardKpiSnapshot.empty();
            }

            Set<UUID> objectHopDongIds = new HashSet<>();
            Set<UUID> hopDongDoiTuongIds = new HashSet<>();
            Set<UUID> doiTuongQuanLyIds = new HashSet<>();
            for (HopDongDoiTuongResponse object : objects) {
                if (object.getId() != null) {
                    hopDongDoiTuongIds.add(object.getId());
                }
                if (object.getHopDongId() != null) {
                    objectHopDongIds.add(object.getHopDongId());
                }
                if (object.getDoiTuongQuanLyId() != null) {
                    doiTuongQuanLyIds.add(object.getDoiTuongQuanLyId());
                }
            }

            Map<UUID, HangMucHopDongTreeResponse> trees = hangMucNhomService.getTreesByHopDongIds(
                    objectHopDongIds, true, false);
            Map<UUID, BigDecimal> planPerHopDong = new HashMap<>();
            for (UUID scopedHopDongId : objectHopDongIds) {
                HangMucHopDongTreeResponse tree =
                        trees.getOrDefault(scopedHopDongId, new HangMucHopDongTreeResponse());
                planPerHopDong.put(scopedHopDongId, safePlanAmount(tree));
            }

            GeoMaps geo = loadGeoMaps(hopDongDoiTuongIds, doiTuongQuanLyIds);

            DashboardKpiSnapshot.Builder builder = new DashboardKpiSnapshot.Builder();
            for (HopDongDoiTuongResponse object : objects) {
                if (object.getId() == null || object.getHopDongId() == null) {
                    continue;
                }
                BigDecimal planShare = planPerHopDong.getOrDefault(object.getHopDongId(), BigDecimal.ZERO);
                if (planShare.signum() == 0) {
                    continue;
                }
                UUID khuVucId = resolveKhuVucId(object, geo);
                UUID tinhId = resolveTinhThanhId(object, geo);
                UUID loaiId = loaiByHopDong.get(object.getHopDongId());
                builder.add(khuVucId, tinhId, loaiId, planShare);
            }
            return builder.build();
        } catch (Exception ex) {
            log.warn("Không tính được KPI dashboard (hopDongId={}, loaiHopDongId={}): {}",
                    hopDongId, loaiHopDongId, ex.toString());
            return DashboardKpiSnapshot.empty();
        }
    }

    private static BigDecimal safePlanAmount(HangMucHopDongTreeResponse tree) {
        try {
            HangMucThanhTienResult computed = HangMucThanhTienCalculator.compute(tree);
            return computed.getTongThanhTien() != null ? computed.getTongThanhTien() : BigDecimal.ZERO;
        } catch (RuntimeException ex) {
            return BigDecimal.ZERO;
        }
    }

    private List<HopDongResponse> loadHopDongs(UUID hopDongId, UUID loaiHopDongId) {
        if (hopDongId != null) {
            HopDongResponse hopDong = hopDongService.getById(hopDongId);
            boolean keep = Boolean.TRUE.equals(hopDong.getHoatDong())
                    && (loaiHopDongId == null || loaiHopDongId.equals(hopDong.getLoaiHopDongId()));
            return keep ? List.of(hopDong) : List.of();
        }
        return hopDongService.listAll(null, Boolean.TRUE, false, loaiHopDongId, null);
    }

    private UUID resolveKhuVucId(HopDongDoiTuongResponse entity, GeoMaps geo) {
        if (entity == null || entity.getDoiTuongQuanLyId() == null) {
            return null;
        }
        ThuocTinhLookup lookup = geo.lookupByDoiTuong().get(entity.getDoiTuongQuanLyId());
        if (lookup == null || lookup.khuVucAttrId() == null) {
            return null;
        }
        Map<UUID, String> giaTri = geo.giaTriByDoiTuong().getOrDefault(entity.getId(), Map.of());
        return UuidUtils.parseUuid(giaTri.get(lookup.khuVucAttrId()));
    }

    private UUID resolveTinhThanhId(HopDongDoiTuongResponse entity, GeoMaps geo) {
        if (entity == null || entity.getDoiTuongQuanLyId() == null) {
            return null;
        }
        ThuocTinhLookup lookup = geo.lookupByDoiTuong().get(entity.getDoiTuongQuanLyId());
        if (lookup == null || lookup.tinhThanhAttrId() == null) {
            return null;
        }
        Map<UUID, String> giaTri = geo.giaTriByDoiTuong().getOrDefault(entity.getId(), Map.of());
        return UuidUtils.parseUuid(giaTri.get(lookup.tinhThanhAttrId()));
    }

    /**
     * {@code doiTuongQuanLyIds} lấy từ chính danh sách đối tượng đã nạp ở computeSnapshot.
     * <p>
     * Trước đây hàm này gọi thêm {@code getByIds(...)} — một lượt enrichList đầy đủ trên TOÀN BỘ
     * ~25k đối tượng — chỉ để thu tập id đó và dựng map {@code doiTuongById} mà không hàm nào đọc
     * ({@link #resolveKhuVucId} / {@link #resolveTinhThanhId} chỉ dùng lookupByDoiTuong và
     * giaTriByDoiTuong).
     */
    private GeoMaps loadGeoMaps(Set<UUID> hopDongDoiTuongIds, Set<UUID> doiTuongQuanLyIds) {
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
        return new GeoMaps(lookupByDoiTuong, giaTriByDoiTuong);
    }

    private ThuocTinhLookup buildThuocTinhLookup(UUID doiTuongQuanLyId) {
        return thuocTinhLookupCache.computeIfAbsent(doiTuongQuanLyId, id -> {
            UUID primaryAttrId = null;
            UUID khuVucAttrId = null;
            UUID tinhThanhAttrId = null;
            // listLite: chỉ cần ten/kieuDuLieuId/laKhoaChinh, không đọc lienKetBang => tránh N+1 kieu_du_lieu
            List<ThuocTinhResponse> thuocTinhList = thuocTinhService
                    .listLite(null, null, false, id, null).stream()
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
        });
    }

    private record GeoMaps(
            Map<UUID, ThuocTinhLookup> lookupByDoiTuong,
            Map<UUID, Map<UUID, String>> giaTriByDoiTuong) {
    }
}
