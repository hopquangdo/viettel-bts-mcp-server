package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.edu.huce.iic.bts_ops_platform.common.util.UuidUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PhanCongDashboardQueryRepository {

    private static final String BASE_FROM = """
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
            """;

    public record StatusMetrics(List<UUID> completedStatusIds, List<UUID> quyetToanStatusIds) {
        public static StatusMetrics empty() {
            return new StatusMetrics(List.of(), List.of());
        }
    }

    private final EntityManager entityManager;

    public record FilterParams(
            UUID hopDongId,
            UUID nhaThauId,
            String giaiDoan,
            UUID khuVucId,
            boolean scopeActive,
            List<UUID> scopedDoiTuongIds,
            List<UUID> scopedHopDongIds,
            List<UUID> giaiDoanStatusFilter,
            boolean giaiDoanUnassignedOnly) {
    }

    public record TongQuanAggregate(
            long tongTram,
            long daPhanNhaThau,
            long soNhaThau,
            long coGiaiDoan,
            long hoanThanh,
            long vuongMac,
            long ton) {
    }

    public record NhaThauAggregate(
            UUID nhaThauId,
            long soTram,
            long hoanThanh,
            long vuongMac,
            long ton,
            java.math.BigDecimal sanLuong,
            UUID sampleKhuVucId) {
    }

    public record GiaiDoanAggregate(UUID trangThaiId, long tong, long daPhan) {
    }

    public record MaVungAggregate(
            UUID khuVucId,
            long soTram,
            UUID nhaThauChinhId,
            UUID sampleTinhThanhId) {
    }

    public record TramRow(
            UUID id,
            UUID hopDongId,
            UUID nhaThauId,
            UUID trangThaiHopDongId,
            UUID khuVucId,
            UUID tinhThanhId,
            java.time.LocalDate ngayHtTc,
            boolean coVuongMacMo,
            java.math.BigDecimal quyetToanThuc,
            java.math.BigDecimal sanLuongHieuLuc) {
    }

    public List<Object[]> countGroupByNhaThau(FilterParams filter) {
        String sql = """
                SELECT d.nha_thau_id, COUNT(*)
                """ + BASE_FROM + """
                 AND d.nha_thau_id IS NOT NULL
                """;
        return runGroupedQuery(sql, filter, "d.nha_thau_id");
    }

    public List<Object[]> countGroupByGiaiDoan(FilterParams filter) {
        String sql = """
                SELECT d.trang_thai_hop_dong_id, COUNT(*)
                """ + BASE_FROM;
        return runGroupedQuery(sql, filter, "d.trang_thai_hop_dong_id");
    }

    public List<Object[]> countGroupByMaVung(FilterParams filter) {
        String sql = """
                SELECT d.khu_vuc_id, COUNT(*)
                """ + BASE_FROM + """
                 AND d.khu_vuc_id IS NOT NULL
                """;
        return runGroupedQuery(sql, filter, "d.khu_vuc_id");
    }

    public List<Object[]> countGroupByHopDong(FilterParams filter) {
        String sql = """
                SELECT d.hop_dong_id, COUNT(*)
                """ + BASE_FROM + """
                 AND d.hop_dong_id IS NOT NULL
                """;
        return runGroupedQuery(sql, filter, "d.hop_dong_id");
    }

    public TongQuanAggregate aggregateTongQuan(FilterParams filter, StatusMetrics metrics) {
        List<Object[]> giaiDoanGroups = countGroupByGiaiDoan(filter);
        long tongTram = giaiDoanGroups.stream().mapToLong(row -> toLong(row[1])).sum();
        long coGiaiDoan = giaiDoanGroups.stream()
                .filter(row -> row[0] != null)
                .mapToLong(row -> toLong(row[1]))
                .sum();
        List<Object[]> nhaThauGroups = countGroupByNhaThau(filter);
        long daPhanNhaThau = nhaThauGroups.stream().mapToLong(row -> toLong(row[1])).sum();
        long soNhaThau = nhaThauGroups.size();
        long[] metricCounts = countHoanThanhVuongMacTon(filter);
        return new TongQuanAggregate(
                tongTram, daPhanNhaThau, soNhaThau, coGiaiDoan, metricCounts[0], metricCounts[1], metricCounts[2]);
    }

    private long[] countHoanThanhVuongMacTon(FilterParams filter) {
        String sql = "SELECT"
                + " COUNT(*) FILTER (WHERE " + completedPredicate(null) + "),"
                + " COUNT(*) FILTER (WHERE COALESCE(d.co_vuong_mac_mo, FALSE) IS TRUE),"
                + " COUNT(*) FILTER (WHERE " + tonPredicate(null) + ")"
                + BASE_FROM;
        Query query = bindFilters(createNativeQuery(sql, filter), filter, null);
        Object[] row = (Object[]) query.getSingleResult();
        return new long[] {toLong(row[0]), toLong(row[1]), toLong(row[2])};
    }

    public List<NhaThauAggregate> aggregateTheoNhaThau(FilterParams filter, StatusMetrics metrics) {
        String sql = "SELECT d.nha_thau_id,"
                + " COUNT(*) AS so_tram,"
                + " mode() WITHIN GROUP (ORDER BY d.khu_vuc_id) AS sample_khu_vuc_id "
                + BASE_FROM
                + " AND d.nha_thau_id IS NOT NULL";
        Query query = bindFilters(createNativeQuery(sql + " GROUP BY d.nha_thau_id ORDER BY COUNT(*) DESC", filter), filter, null);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<NhaThauAggregate> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            result.add(new NhaThauAggregate(
                    toUuid(row[0]),
                    toLong(row[1]),
                    0L,
                    0L,
                    0L,
                    java.math.BigDecimal.ZERO,
                    toUuid(row[2])));
        }
        return result;
    }

    public List<GiaiDoanAggregate> aggregateTheoGiaiDoan(FilterParams filter) {
        String sql = """
                SELECT d.trang_thai_hop_dong_id,
                       COUNT(*) AS tong,
                       COUNT(d.nha_thau_id) AS da_phan
                """ + BASE_FROM;
        Query query = bindFilters(createNativeQuery(sql + " GROUP BY d.trang_thai_hop_dong_id ORDER BY tong DESC", filter), filter, null);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<GiaiDoanAggregate> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            result.add(new GiaiDoanAggregate(toUuid(row[0]), toLong(row[1]), toLong(row[2])));
        }
        return result;
    }

    public List<MaVungAggregate> aggregateTheoMaVung(FilterParams filter) {
        String sql = "SELECT d.khu_vuc_id,"
                + " COUNT(*) AS so_tram,"
                + " mode() WITHIN GROUP (ORDER BY d.nha_thau_id) AS nha_thau_chinh_id,"
                + " mode() WITHIN GROUP (ORDER BY d.tinh_thanh_id) AS sample_tinh_id "
                + BASE_FROM
                + " AND d.khu_vuc_id IS NOT NULL";
        Query query = bindFilters(createNativeQuery(sql + " GROUP BY d.khu_vuc_id ORDER BY COUNT(*) DESC", filter), filter, null);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<MaVungAggregate> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            result.add(new MaVungAggregate(
                    toUuid(row[0]),
                    toLong(row[1]),
                    toUuid(row[2]),
                    toUuid(row[3])));
        }
        return result;
    }

    public long countTram(FilterParams filter) {
        return countWhere(filter, "TRUE", null);
    }

    private long countWhere(FilterParams filter, String predicate, StatusMetrics metrics) {
        String sql = "SELECT COUNT(*) " + BASE_FROM + " AND (" + predicate + ")";
        Query query = bindFilters(createNativeQuery(sql, filter), filter, metrics);
        return toLong(query.getSingleResult());
    }

    public List<TramRow> findTramPage(FilterParams filter, int offset, int limit) {
        String sql = """
                SELECT d.id, d.hop_dong_id, d.nha_thau_id, d.trang_thai_hop_dong_id, d.khu_vuc_id,
                       d.tinh_thanh_id, d.ngay_ht_tc, d.co_vuong_mac_mo, d.quyet_toan_thuc, d.san_luong_hieu_luc
                """ + BASE_FROM;
        Query query = bindFilters(createNativeQuery(sql + " ORDER BY d.id LIMIT :pageLimit OFFSET :pageOffset", filter), filter, null);
        query.setParameter("pageLimit", limit);
        query.setParameter("pageOffset", offset);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<TramRow> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            result.add(new TramRow(
                    toUuid(row[0]),
                    toUuid(row[1]),
                    toUuid(row[2]),
                    toUuid(row[3]),
                    toUuid(row[4]),
                    toUuid(row[5]),
                    toLocalDate(row[6]),
                    toBoolean(row[7]),
                    toBigDecimal(row[8]),
                    toBigDecimal(row[9])));
        }
        return result;
    }

    private List<Object[]> runGroupedQuery(String sql, FilterParams filter, String groupBy) {
        Query query = bindFilters(createNativeQuery(sql + " GROUP BY " + groupBy + " ORDER BY COUNT(*) DESC", filter), filter, null);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows;
    }

    private static String completedPredicate(StatusMetrics metrics) {
        return "d.ngay_ht_tc IS NOT NULL";
    }

    private static String tonPredicate(StatusMetrics metrics) {
        return "d.ngay_ht_tc IS NOT NULL"
                + " AND COALESCE(d.co_vuong_mac_mo, FALSE) IS NOT TRUE"
                + " AND d.quyet_toan_thuc IS NULL";
    }

    private Query createNativeQuery(String sql, FilterParams filter) {
        if (filter.scopeActive()
                && filter.scopedDoiTuongIds().isEmpty()
                && filter.scopedHopDongIds().isEmpty()) {
            return entityManager.createNativeQuery(sql + " AND 1 = 0");
        }
        if (filter.giaiDoanStatusFilter() != null && filter.giaiDoanStatusFilter().isEmpty() && !filter.giaiDoanUnassignedOnly()) {
            return entityManager.createNativeQuery(sql + " AND 1 = 0");
        }
        return entityManager.createNativeQuery(sql + buildFilterClause(filter));
    }

    private Query bindFilters(Query query, FilterParams filter, StatusMetrics metrics) {
        if (filter.hopDongId() != null) {
            query.setParameter("hopDongId", filter.hopDongId());
        }
        if (filter.nhaThauId() != null) {
            query.setParameter("nhaThauId", filter.nhaThauId());
        }
        if (filter.khuVucId() != null) {
            query.setParameter("khuVucId", filter.khuVucId());
        }
        if (filter.giaiDoan() != null && filter.giaiDoan().matches("[0-9a-fA-F-]{36}")) {
            query.setParameter("giaiDoanStatusId", UUID.fromString(filter.giaiDoan()));
        }
        if (filter.giaiDoanStatusFilter() != null && !filter.giaiDoanStatusFilter().isEmpty()) {
            query.setParameter("giaiDoanStatusFilter", filter.giaiDoanStatusFilter());
        }
        if (filter.scopeActive()) {
            if (!filter.scopedDoiTuongIds().isEmpty()) {
                query.setParameter("scopedDoiTuongIds", filter.scopedDoiTuongIds());
            }
            if (!filter.scopedHopDongIds().isEmpty()) {
                query.setParameter("scopedHopDongIds", filter.scopedHopDongIds());
            }
        }
        if (metrics != null && !metrics.completedStatusIds().isEmpty()) {
            query.setParameter("completedStatusIds", metrics.completedStatusIds());
        }
        if (metrics != null && !metrics.quyetToanStatusIds().isEmpty()) {
            query.setParameter("quyetToanStatusIds", metrics.quyetToanStatusIds());
        }
        return query;
    }

    private String buildFilterClause(FilterParams filter) {
        StringBuilder clause = new StringBuilder();
        if (filter.hopDongId() != null) {
            clause.append(" AND d.hop_dong_id = :hopDongId");
        }
        if (filter.nhaThauId() != null) {
            clause.append(" AND d.nha_thau_id = :nhaThauId");
        }
        if (filter.khuVucId() != null) {
            clause.append(" AND d.khu_vuc_id = :khuVucId");
        }
        appendGiaiDoanClause(clause, filter);
        appendScopeClause(clause, filter);
        return clause.toString();
    }

    private void appendScopeClause(StringBuilder clause, FilterParams filter) {
        if (!filter.scopeActive()) {
            return;
        }
        if (filter.scopedDoiTuongIds().isEmpty() && filter.scopedHopDongIds().isEmpty()) {
            clause.append(" AND 1 = 0");
            return;
        }
        List<String> parts = new ArrayList<>(2);
        if (!filter.scopedDoiTuongIds().isEmpty()) {
            parts.add("d.id IN :scopedDoiTuongIds");
        }
        if (!filter.scopedHopDongIds().isEmpty()) {
            parts.add("d.hop_dong_id IN :scopedHopDongIds");
        }
        clause.append(" AND (").append(String.join(" OR ", parts)).append(")");
    }

    private void appendGiaiDoanClause(StringBuilder clause, FilterParams filter) {
        String giaiDoan = filter.giaiDoan();
        if (giaiDoan == null || giaiDoan.isBlank() || "all".equalsIgnoreCase(giaiDoan)) {
            return;
        }
        if (giaiDoan.matches("[0-9a-fA-F-]{36}")) {
            clause.append(" AND d.trang_thai_hop_dong_id = :giaiDoanStatusId");
            return;
        }
        if (filter.giaiDoanUnassignedOnly()) {
            clause.append(" AND d.trang_thai_hop_dong_id IS NULL");
            return;
        }
        if (filter.giaiDoanStatusFilter() != null && !filter.giaiDoanStatusFilter().isEmpty()) {
            clause.append(" AND d.trang_thai_hop_dong_id IN (:giaiDoanStatusFilter)");
        }
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

    private static UUID toUuid(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        return UuidUtils.parseUuid(value.toString());
    }

    private static java.math.BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return java.math.BigDecimal.ZERO;
        }
        if (value instanceof java.math.BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return java.math.BigDecimal.valueOf(number.doubleValue());
        }
        return new java.math.BigDecimal(value.toString());
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

    private static java.time.LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.time.LocalDate localDate) {
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
}
