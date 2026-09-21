package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tổng hợp KPI (thành tiền hạng mục HĐ / đối tượng) theo các chiều dashboard. */
public final class DashboardKpiSnapshot {

    private final BigDecimal total;
    private final Map<UUID, BigDecimal> byKhuVuc;
    private final Map<UUID, Map<UUID, BigDecimal>> byKhuVucTinh;
    private final Map<UUID, Map<UUID, BigDecimal>> byKhuVucLoai;
    private final Map<UUID, BigDecimal> unmappedTinhByKhuVuc;
    private final Map<UUID, BigDecimal> unmappedLoaiByKhuVuc;
    private final BigDecimal orphan;

    public DashboardKpiSnapshot(
            BigDecimal total,
            Map<UUID, BigDecimal> byKhuVuc,
            Map<UUID, Map<UUID, BigDecimal>> byKhuVucTinh,
            Map<UUID, Map<UUID, BigDecimal>> byKhuVucLoai,
            Map<UUID, BigDecimal> unmappedTinhByKhuVuc,
            Map<UUID, BigDecimal> unmappedLoaiByKhuVuc,
            BigDecimal orphan) {
        this.total = total != null ? total : BigDecimal.ZERO;
        this.byKhuVuc = byKhuVuc != null ? byKhuVuc : Map.of();
        this.byKhuVucTinh = byKhuVucTinh != null ? byKhuVucTinh : Map.of();
        this.byKhuVucLoai = byKhuVucLoai != null ? byKhuVucLoai : Map.of();
        this.unmappedTinhByKhuVuc = unmappedTinhByKhuVuc != null ? unmappedTinhByKhuVuc : Map.of();
        this.unmappedLoaiByKhuVuc = unmappedLoaiByKhuVuc != null ? unmappedLoaiByKhuVuc : Map.of();
        this.orphan = orphan != null ? orphan : BigDecimal.ZERO;
    }

    public static DashboardKpiSnapshot empty() {
        return new DashboardKpiSnapshot(
                BigDecimal.ZERO, Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), BigDecimal.ZERO);
    }

    public BigDecimal total() {
        return total;
    }

    public BigDecimal forKhuVuc(UUID khuVucId) {
        if (khuVucId == null) {
            return orphan;
        }
        return byKhuVuc.getOrDefault(khuVucId, BigDecimal.ZERO);
    }

    public BigDecimal forKhuVucTinh(UUID khuVucId, UUID tinhThanhId) {
        if (khuVucId == null) {
            return BigDecimal.ZERO;
        }
        Map<UUID, BigDecimal> byTinh = byKhuVucTinh.getOrDefault(khuVucId, Map.of());
        if (tinhThanhId == null) {
            return unmappedTinhByKhuVuc.getOrDefault(khuVucId, BigDecimal.ZERO);
        }
        return byTinh.getOrDefault(tinhThanhId, BigDecimal.ZERO);
    }

    public BigDecimal forKhuVucLoai(UUID khuVucId, UUID loaiHopDongId) {
        if (khuVucId == null) {
            return BigDecimal.ZERO;
        }
        if (loaiHopDongId == null) {
            return unmappedLoaiByKhuVuc.getOrDefault(khuVucId, BigDecimal.ZERO);
        }
        Map<UUID, BigDecimal> byLoai = byKhuVucLoai.getOrDefault(khuVucId, Map.of());
        return byLoai.getOrDefault(loaiHopDongId, BigDecimal.ZERO);
    }

    public BigDecimal orphanLoaiForKhuVuc(UUID khuVucId) {
        if (khuVucId == null) {
            return BigDecimal.ZERO;
        }
        return unmappedLoaiByKhuVuc.getOrDefault(khuVucId, BigDecimal.ZERO);
    }

    public BigDecimal orphan() {
        return orphan;
    }

    public static class Builder {
        private BigDecimal total = BigDecimal.ZERO;
        private final Map<UUID, BigDecimal> byKhuVuc = new HashMap<>();
        private final Map<UUID, Map<UUID, BigDecimal>> byKhuVucTinh = new HashMap<>();
        private final Map<UUID, Map<UUID, BigDecimal>> byKhuVucLoai = new HashMap<>();
        private final Map<UUID, BigDecimal> unmappedTinhByKhuVuc = new HashMap<>();
        private final Map<UUID, BigDecimal> unmappedLoaiByKhuVuc = new HashMap<>();
        private BigDecimal orphan = BigDecimal.ZERO;

        public void add(UUID khuVucId, UUID tinhThanhId, UUID loaiHopDongId, BigDecimal planShare) {
            if (planShare == null || planShare.signum() == 0) {
                return;
            }
            total = total.add(planShare);
            if (khuVucId == null) {
                orphan = orphan.add(planShare);
                return;
            }
            byKhuVuc.merge(khuVucId, planShare, BigDecimal::add);
            if (tinhThanhId == null) {
                unmappedTinhByKhuVuc.merge(khuVucId, planShare, BigDecimal::add);
            } else {
                byKhuVucTinh
                        .computeIfAbsent(khuVucId, ignored -> new HashMap<>())
                        .merge(tinhThanhId, planShare, BigDecimal::add);
            }
            if (loaiHopDongId != null) {
                byKhuVucLoai
                        .computeIfAbsent(khuVucId, ignored -> new HashMap<>())
                        .merge(loaiHopDongId, planShare, BigDecimal::add);
            } else {
                unmappedLoaiByKhuVuc.merge(khuVucId, planShare, BigDecimal::add);
            }
        }

        public DashboardKpiSnapshot build() {
            return new DashboardKpiSnapshot(
                    total, byKhuVuc, byKhuVucTinh, byKhuVucLoai, unmappedTinhByKhuVuc, unmappedLoaiByKhuVuc, orphan);
        }
    }
}
