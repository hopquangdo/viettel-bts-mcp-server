package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.PaginationDefaults;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.HopDongDoiTuongSnapshot;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.HopDongDoiTuongTonStatRow;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.response.HopDongDoiTuongTonItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongTonCandidateRow;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongSnapshotService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongTonService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Core-only: chỉ dùng cột thật của hop_dong_doi_tuong/hop_dong (HTTC/pháp lý/quyết toán/ngưỡng
 * ngày/co_vuong_mac_mo/san_luong_hieu_luc — đều đã denormalize). Mã trạm/khu vực/nhà thầu đọc qua
 * HopDongDoiTuongSnapshotService (cache-aside per-object đã có sẵn, invalidate qua
 * HopDongDoiTuongMetaChangedEvent) thay vì tự resolve lại mỗi lần. "Đang vướng mắc" đã bị loại
 * thẳng trong SQL (d.co_vuong_mac_mo = FALSE) — tầng gộp business.tramton không cần tự loại trừ
 * nữa. "giaTri" chỉ là fallback chia đều giá trị HĐ, dùng khi sanLuongHieuLuc <= 0.
 */
@Service
@RequiredArgsConstructor
public class HopDongDoiTuongTonServiceImpl implements HopDongDoiTuongTonService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int DEFAULT_QUA_HAN_NGAY = 90;

    private final HopDongDoiTuongRepository hopDongDoiTuongRepository;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final HopDongDoiTuongSnapshotService hopDongDoiTuongSnapshotService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HopDongDoiTuongTonItemResponse> choQuyetToan(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay,
            LocalDate ngayBaoCao, Integer page, Integer size) {
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE);
        int pageNumber = EntityFilter.normalizePage(page);
        return build(loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao,
                PageRequest.of(pageNumber, pageSize), "cho_qt", true);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HopDongDoiTuongTonItemResponse> quaHan(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay,
            LocalDate ngayBaoCao, Integer page, Integer size) {
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE);
        int pageNumber = EntityFilter.normalizePage(page);
        return build(loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao,
                PageRequest.of(pageNumber, pageSize), "qua_han", false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongTonItemResponse> choQuyetToanAll(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay, LocalDate ngayBaoCao) {
        return build(loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao, Pageable.unpaged(), "cho_qt", true)
                .getItems();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongTonItemResponse> quaHanAll(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay, LocalDate ngayBaoCao) {
        return build(loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao, Pageable.unpaged(), "qua_han", false)
                .getItems();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongTonStatRow> choQuyetToanStatsAll(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay, LocalDate ngayBaoCao) {
        return buildStats(loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongTonStatRow> quaHanStatsAll(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay, LocalDate ngayBaoCao) {
        return buildStats(loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao, false);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HopDongDoiTuongTonItemResponse> chuaPhapLy(
            UUID loaiHopDongId, LocalDate ngayBaoCao, Integer page, Integer size) {
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE);
        int pageNumber = EntityFilter.normalizePage(page);
        return buildChuaPhapLy(loaiHopDongId, ngayBaoCao, PageRequest.of(pageNumber, pageSize));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongTonItemResponse> chuaPhapLyAll(UUID loaiHopDongId, LocalDate ngayBaoCao) {
        return buildChuaPhapLy(loaiHopDongId, ngayBaoCao, Pageable.unpaged()).getItems();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongTonItemResponse> choQuyetToanLiteAll(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay, LocalDate ngayBaoCao) {
        return buildLite(loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao, Pageable.unpaged(), "cho_qt", true)
                .getItems();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongTonItemResponse> quaHanLiteAll(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay, LocalDate ngayBaoCao) {
        return buildLite(loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao, Pageable.unpaged(), "qua_han", false)
                .getItems();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongTonItemResponse> chuaPhapLyLiteAll(UUID loaiHopDongId, LocalDate ngayBaoCao) {
        return buildChuaPhapLyLite(loaiHopDongId, ngayBaoCao, Pageable.unpaged()).getItems();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongTonStatRow> chuaPhapLyStatsAll(UUID loaiHopDongId, LocalDate ngayBaoCao) {
        return buildChuaPhapLyStats(loaiHopDongId, ngayBaoCao);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<HopDongDoiTuongTonItemResponse> findTonCoreItemByDoiTuongId(
            UUID doiTuongId,
            LocalDate dateFrom,
            LocalDate dateTo,
            Integer quaHanNgay,
            LocalDate ngayBaoCao) {
        if (doiTuongId == null) {
            return java.util.Optional.empty();
        }
        LocalDate reportDate = ngayBaoCao != null ? ngayBaoCao : LocalDate.now(ZONE);
        LocalDate nguongNgay = reportDate.minusDays(normalizeQuaHanNgay(quaHanNgay));

        java.util.Optional<HopDongDoiTuongTonCandidateRow> phapLy =
                hopDongDoiTuongRepository.findChuaPhapLyByDoiTuongId(doiTuongId);
        if (phapLy.isPresent()) {
            return java.util.Optional.of(buildSingleItem(phapLy.get(), reportDate, "thieu_phap_ly"));
        }

        java.util.Optional<HopDongDoiTuongTonCandidateRow> quaHan =
                hopDongDoiTuongRepository.findQuaHanByDoiTuongId(doiTuongId, dateFrom, dateTo, nguongNgay);
        if (quaHan.isPresent()) {
            return java.util.Optional.of(buildSingleItem(quaHan.get(), reportDate, "qua_han"));
        }

        java.util.Optional<HopDongDoiTuongTonCandidateRow> choQt =
                hopDongDoiTuongRepository.findChoQuyetToanByDoiTuongId(doiTuongId, dateFrom, dateTo, nguongNgay);
        return choQt.map(row -> buildSingleItem(row, reportDate, "cho_qt"));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<HopDongDoiTuongTonItemResponse> findChuaPhapLyChiTietByDoiTuongId(
            UUID doiTuongId,
            LocalDate ngayBaoCao,
            HopDongDoiTuongSnapshot snapshot) {
        if (doiTuongId == null) {
            return java.util.Optional.empty();
        }
        LocalDate reportDate = ngayBaoCao != null ? ngayBaoCao : LocalDate.now(ZONE);
        return hopDongDoiTuongRepository.findChuaPhapLyByDoiTuongId(doiTuongId)
                .map(row -> buildChiTietItem(row, snapshot, reportDate, "thieu_phap_ly"));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<HopDongDoiTuongTonItemResponse> findQuaHanOrChoQtChiTietByDoiTuongId(
            UUID doiTuongId,
            LocalDate dateFrom,
            LocalDate dateTo,
            Integer quaHanNgay,
            LocalDate ngayBaoCao,
            HopDongDoiTuongSnapshot snapshot) {
        if (doiTuongId == null) {
            return java.util.Optional.empty();
        }
        LocalDate reportDate = ngayBaoCao != null ? ngayBaoCao : LocalDate.now(ZONE);
        LocalDate nguongNgay = reportDate.minusDays(normalizeQuaHanNgay(quaHanNgay));

        java.util.Optional<HopDongDoiTuongTonCandidateRow> quaHan =
                hopDongDoiTuongRepository.findQuaHanByDoiTuongId(doiTuongId, dateFrom, dateTo, nguongNgay);
        if (quaHan.isPresent()) {
            return java.util.Optional.of(buildChiTietItem(quaHan.get(), snapshot, reportDate, "qua_han"));
        }

        return hopDongDoiTuongRepository.findChoQuyetToanByDoiTuongId(doiTuongId, dateFrom, dateTo, nguongNgay)
                .map(row -> buildChiTietItem(row, snapshot, reportDate, "cho_qt"));
    }

    private HopDongDoiTuongTonItemResponse buildChiTietItem(
            HopDongDoiTuongTonCandidateRow row,
            HopDongDoiTuongSnapshot snapshot,
            LocalDate reportDate,
            String trangThai) {
        long soNgay = row.getResolvedNgayHt() != null
                ? Math.max(0L, ChronoUnit.DAYS.between(row.getResolvedNgayHt(), reportDate))
                : 0L;
        return HopDongDoiTuongTonItemResponse.builder()
                .id(row.getId())
                .hopDongId(row.getHopDongId())
                .maTram(snapshot != null && snapshot.getMaDoiTuong() != null && !snapshot.getMaDoiTuong().isBlank()
                        ? snapshot.getMaDoiTuong() : "—")
                .khuVuc(snapshot != null && snapshot.getKhuVuc() != null && !snapshot.getKhuVuc().isBlank()
                        ? snapshot.getKhuVuc() : "—")
                .nhaThau(snapshot != null && snapshot.getNhaThau() != null && !snapshot.getNhaThau().isBlank()
                        ? snapshot.getNhaThau() : "—")
                .maHopDong(row.getMaHopDong())
                .giaTri(BigDecimal.ZERO)
                .sanLuongHieuLuc(row.getSanLuongHieuLuc())
                .ngayHtTc(row.getResolvedNgayHt())
                .soNgayTon(soNgay)
                .trangThai(trangThai)
                .build();
    }

    private HopDongDoiTuongTonItemResponse buildSingleItem(
            HopDongDoiTuongTonCandidateRow row, LocalDate reportDate, String trangThai) {
        HopDongDoiTuongSnapshot snapshot = hopDongDoiTuongSnapshotService.getSnapshot(row.getId());
        long soNgay = row.getResolvedNgayHt() != null
                ? Math.max(0L, ChronoUnit.DAYS.between(row.getResolvedNgayHt(), reportDate))
                : 0L;
        Map<UUID, Long> objectCountByHopDong = new HashMap<>();
        if (row.getHopDongId() != null) {
            objectCountByHopDong.put(
                    row.getHopDongId(),
                    hopDongDoiTuongRepository.countActiveObjectsByHopDongId(row.getHopDongId()));
        }
        return HopDongDoiTuongTonItemResponse.builder()
                .id(row.getId())
                .hopDongId(row.getHopDongId())
                .maTram(snapshot != null && snapshot.getMaDoiTuong() != null && !snapshot.getMaDoiTuong().isBlank()
                        ? snapshot.getMaDoiTuong() : "—")
                .khuVuc(snapshot != null && snapshot.getKhuVuc() != null && !snapshot.getKhuVuc().isBlank()
                        ? snapshot.getKhuVuc() : "—")
                .nhaThau(snapshot != null && snapshot.getNhaThau() != null && !snapshot.getNhaThau().isBlank()
                        ? snapshot.getNhaThau() : "—")
                .maHopDong(row.getMaHopDong())
                .giaTri(chiaDeuGiaTriHd(row, objectCountByHopDong))
                .sanLuongHieuLuc(row.getSanLuongHieuLuc())
                .ngayHtTc(row.getResolvedNgayHt())
                .soNgayTon(soNgay)
                .trangThai(trangThai)
                .build();
    }

    private PageResponse<HopDongDoiTuongTonItemResponse> buildChuaPhapLy(
            UUID loaiHopDongId, LocalDate ngayBaoCao, Pageable pageable) {
        int pageNumber = pageable.isPaged() ? pageable.getPageNumber() : 0;
        int pageSize = pageable.isPaged() ? pageable.getPageSize() : 0;
        LocalDate reportDate = ngayBaoCao != null ? ngayBaoCao : LocalDate.now(ZONE);

        Page<HopDongDoiTuongTonCandidateRow> pageResult =
                hopDongDoiTuongRepository.findChuaPhapLy(loaiHopDongId, pageable);
        List<HopDongDoiTuongTonCandidateRow> rows = pageResult.getContent();
        if (rows.isEmpty()) {
            return PageResponse.ofItems(List.of(), pageNumber, pageSize, pageResult.getTotalElements());
        }

        Map<UUID, Long> objectCountByHopDong = new HashMap<>();
        for (Object[] row : hopDongDoiTuongService.countGroupByHopDongId(Boolean.TRUE, loaiHopDongId, null)) {
            if (row[0] != null) {
                objectCountByHopDong.put((UUID) row[0], ((Number) row[1]).longValue());
            }
        }

        List<UUID> ids = rows.stream().map(HopDongDoiTuongTonCandidateRow::getId).toList();
        Map<UUID, HopDongDoiTuongSnapshot> snapshotById = hopDongDoiTuongSnapshotService.getSnapshots(ids);

        List<HopDongDoiTuongTonItemResponse> items = new ArrayList<>();
        for (HopDongDoiTuongTonCandidateRow row : rows) {
            HopDongDoiTuongSnapshot snapshot = snapshotById.get(row.getId());
            LocalDate mocNgay = row.getResolvedNgayHt();
            long soNgay = mocNgay != null
                    ? Math.max(0L, ChronoUnit.DAYS.between(mocNgay, reportDate))
                    : 0L;
            items.add(HopDongDoiTuongTonItemResponse.builder()
                    .id(row.getId())
                    .hopDongId(row.getHopDongId())
                    .maTram(snapshot != null && snapshot.getMaDoiTuong() != null && !snapshot.getMaDoiTuong().isBlank()
                            ? snapshot.getMaDoiTuong() : "—")
                    .khuVuc(snapshot != null && snapshot.getKhuVuc() != null && !snapshot.getKhuVuc().isBlank()
                            ? snapshot.getKhuVuc() : "—")
                    .nhaThau(snapshot != null && snapshot.getNhaThau() != null && !snapshot.getNhaThau().isBlank()
                            ? snapshot.getNhaThau() : "—")
                    .maHopDong(row.getMaHopDong())
                    .giaTri(chiaDeuGiaTriHd(row, objectCountByHopDong))
                    .sanLuongHieuLuc(row.getSanLuongHieuLuc())
                    .ngayHtTc(mocNgay)
                    .soNgayTon(soNgay)
                    .trangThai("thieu_phap_ly")
                    .build());
        }
        return PageResponse.ofItems(items, pageNumber, pageSize, pageResult.getTotalElements());
    }

    private List<HopDongDoiTuongTonStatRow> buildChuaPhapLyStats(UUID loaiHopDongId, LocalDate ngayBaoCao) {
        LocalDate reportDate = ngayBaoCao != null ? ngayBaoCao : LocalDate.now(ZONE);
        List<HopDongDoiTuongTonCandidateRow> rows =
                hopDongDoiTuongRepository.findChuaPhapLy(loaiHopDongId, Pageable.unpaged()).getContent();
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<UUID, Long> objectCountByHopDong = new HashMap<>();
        for (Object[] row : hopDongDoiTuongService.countGroupByHopDongId(Boolean.TRUE, loaiHopDongId, null)) {
            if (row[0] != null) {
                objectCountByHopDong.put((UUID) row[0], ((Number) row[1]).longValue());
            }
        }
        List<HopDongDoiTuongTonStatRow> stats = new ArrayList<>();
        for (HopDongDoiTuongTonCandidateRow row : rows) {
            LocalDate mocNgay = row.getResolvedNgayHt();
            long soNgay = mocNgay != null
                    ? Math.max(0L, ChronoUnit.DAYS.between(mocNgay, reportDate))
                    : 0L;
            BigDecimal sanLuongHieuLuc = row.getSanLuongHieuLuc();
            BigDecimal giaTri = sanLuongHieuLuc != null && sanLuongHieuLuc.compareTo(BigDecimal.ZERO) > 0
                    ? sanLuongHieuLuc
                    : chiaDeuGiaTriHd(row, objectCountByHopDong);
            stats.add(new HopDongDoiTuongTonStatRow(row.getId(), soNgay, giaTri));
        }
        return stats;
    }

    private record Candidates(
            List<HopDongDoiTuongTonCandidateRow> rows, LocalDate reportDate, Map<UUID, Long> objectCountByHopDong) {
    }

    /** Fetch candidate rows + đếm nhóm theo hợp đồng (chia đều giá trị) — dùng chung cho build()/buildStats(). */
    private Candidates loadCandidates(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay,
            LocalDate ngayBaoCao, Pageable pageable, boolean choQuyetToan, long[] totalElementsOut) {
        int threshold = normalizeQuaHanNgay(quaHanNgay);
        LocalDate reportDate = ngayBaoCao != null ? ngayBaoCao : LocalDate.now(ZONE);
        LocalDate nguongNgay = reportDate.minusDays(threshold);

        Page<HopDongDoiTuongTonCandidateRow> pageResult = choQuyetToan
                ? hopDongDoiTuongRepository.findChoQuyetToan(loaiHopDongId, dateFrom, dateTo, nguongNgay, pageable)
                : hopDongDoiTuongRepository.findQuaHan(loaiHopDongId, dateFrom, dateTo, nguongNgay, pageable);
        totalElementsOut[0] = pageResult.getTotalElements();
        List<HopDongDoiTuongTonCandidateRow> rows = pageResult.getContent();
        if (rows.isEmpty()) {
            return new Candidates(rows, reportDate, Map.of());
        }

        // Chia đều giá trị HĐ — fallback thuần core, dùng khi tầng gộp chưa có sản lượng báo cáo.
        Map<UUID, Long> objectCountByHopDong = new HashMap<>();
        for (Object[] row : hopDongDoiTuongService.countGroupByHopDongId(Boolean.TRUE, loaiHopDongId, null)) {
            if (row[0] != null) {
                objectCountByHopDong.put((UUID) row[0], ((Number) row[1]).longValue());
            }
        }
        return new Candidates(rows, reportDate, objectCountByHopDong);
    }

    private PageResponse<HopDongDoiTuongTonItemResponse> build(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay,
            LocalDate ngayBaoCao, Pageable pageable, String trangThai, boolean choQuyetToan) {
        int pageNumber = pageable.isPaged() ? pageable.getPageNumber() : 0;
        int pageSize = pageable.isPaged() ? pageable.getPageSize() : 0;

        long[] totalElementsOut = new long[1];
        Candidates candidates = loadCandidates(
                loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao, pageable, choQuyetToan, totalElementsOut);
        List<HopDongDoiTuongTonCandidateRow> rows = candidates.rows();
        if (rows.isEmpty()) {
            return PageResponse.ofItems(List.of(), pageNumber, pageSize, totalElementsOut[0]);
        }
        LocalDate reportDate = candidates.reportDate();
        Map<UUID, Long> objectCountByHopDong = candidates.objectCountByHopDong();

        // Mã trạm/khu vực/nhà thầu — đọc qua snapshot cache-aside sẵn có của core (batched,
        // invalidate qua HopDongDoiTuongMetaChangedEvent) thay vì tự resolve lại mỗi lần.
        List<UUID> ids = rows.stream().map(HopDongDoiTuongTonCandidateRow::getId).toList();
        Map<UUID, HopDongDoiTuongSnapshot> snapshotById = hopDongDoiTuongSnapshotService.getSnapshots(ids);

        List<HopDongDoiTuongTonItemResponse> items = new ArrayList<>();
        for (HopDongDoiTuongTonCandidateRow row : rows) {
            HopDongDoiTuongSnapshot snapshot = snapshotById.get(row.getId());
            long soNgay = Math.max(0L, ChronoUnit.DAYS.between(row.getResolvedNgayHt(), reportDate));

            items.add(HopDongDoiTuongTonItemResponse.builder()
                    .id(row.getId())
                    .hopDongId(row.getHopDongId())
                    .maTram(snapshot != null && snapshot.getMaDoiTuong() != null && !snapshot.getMaDoiTuong().isBlank()
                            ? snapshot.getMaDoiTuong() : "—")
                    .khuVuc(snapshot != null && snapshot.getKhuVuc() != null && !snapshot.getKhuVuc().isBlank()
                            ? snapshot.getKhuVuc() : "—")
                    .nhaThau(snapshot != null && snapshot.getNhaThau() != null && !snapshot.getNhaThau().isBlank()
                            ? snapshot.getNhaThau() : "—")
                    .maHopDong(row.getMaHopDong())
                    .giaTri(chiaDeuGiaTriHd(row, objectCountByHopDong))
                    .sanLuongHieuLuc(row.getSanLuongHieuLuc())
                    .ngayHtTc(row.getResolvedNgayHt())
                    .soNgayTon(soNgay)
                    .trangThai(trangThai)
                    .build());
        }

        return PageResponse.ofItems(items, pageNumber, pageSize, totalElementsOut[0]);
    }

    /** Bản "stats-only" của build() — bỏ hẳn resolve snapshot (mã trạm/khu vực/nhà thầu), chỉ trả soNgayTon + giaTri. */
    private List<HopDongDoiTuongTonStatRow> buildStats(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay,
            LocalDate ngayBaoCao, boolean choQuyetToan) {
        long[] totalElementsOut = new long[1];
        Candidates candidates = loadCandidates(
                loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao, Pageable.unpaged(), choQuyetToan,
                totalElementsOut);
        if (candidates.rows().isEmpty()) {
            return List.of();
        }
        LocalDate reportDate = candidates.reportDate();
        Map<UUID, Long> objectCountByHopDong = candidates.objectCountByHopDong();

        List<HopDongDoiTuongTonStatRow> stats = new ArrayList<>();
        for (HopDongDoiTuongTonCandidateRow row : candidates.rows()) {
            long soNgay = Math.max(0L, ChronoUnit.DAYS.between(row.getResolvedNgayHt(), reportDate));
            // Ưu tiên sản lượng báo cáo thực tế (san_luong_hieu_luc) — giống hệt cách
            // TramTonServiceImpl.resolveGiaTri() ưu tiên khi build() trả full item, chỉ khác
            // là ở đây resolve luôn tại nguồn vì stats row không còn giữ 2 field riêng.
            BigDecimal sanLuongHieuLuc = row.getSanLuongHieuLuc();
            BigDecimal giaTri = sanLuongHieuLuc != null && sanLuongHieuLuc.compareTo(BigDecimal.ZERO) > 0
                    ? sanLuongHieuLuc
                    : chiaDeuGiaTriHd(row, objectCountByHopDong);
            stats.add(new HopDongDoiTuongTonStatRow(row.getId(), soNgay, giaTri));
        }
        return stats;
    }

    /** Giống build() nhưng bỏ resolve snapshot — enrich sau khi đã cắt trang. */
    private PageResponse<HopDongDoiTuongTonItemResponse> buildLite(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay,
            LocalDate ngayBaoCao, Pageable pageable, String trangThai, boolean choQuyetToan) {
        int pageNumber = pageable.isPaged() ? pageable.getPageNumber() : 0;
        int pageSize = pageable.isPaged() ? pageable.getPageSize() : 0;

        long[] totalElementsOut = new long[1];
        Candidates candidates = loadCandidates(
                loaiHopDongId, dateFrom, dateTo, quaHanNgay, ngayBaoCao, pageable, choQuyetToan, totalElementsOut);
        List<HopDongDoiTuongTonCandidateRow> rows = candidates.rows();
        if (rows.isEmpty()) {
            return PageResponse.ofItems(List.of(), pageNumber, pageSize, totalElementsOut[0]);
        }
        LocalDate reportDate = candidates.reportDate();
        Map<UUID, Long> objectCountByHopDong = candidates.objectCountByHopDong();

        List<HopDongDoiTuongTonItemResponse> items = new ArrayList<>();
        for (HopDongDoiTuongTonCandidateRow row : rows) {
            long soNgay = Math.max(0L, ChronoUnit.DAYS.between(row.getResolvedNgayHt(), reportDate));
            items.add(HopDongDoiTuongTonItemResponse.builder()
                    .id(row.getId())
                    .hopDongId(row.getHopDongId())
                    .maTram("—")
                    .khuVuc("—")
                    .nhaThau("—")
                    .maHopDong(row.getMaHopDong())
                    .giaTri(chiaDeuGiaTriHd(row, objectCountByHopDong))
                    .sanLuongHieuLuc(row.getSanLuongHieuLuc())
                    .ngayHtTc(row.getResolvedNgayHt())
                    .soNgayTon(soNgay)
                    .trangThai(trangThai)
                    .build());
        }
        return PageResponse.ofItems(items, pageNumber, pageSize, totalElementsOut[0]);
    }

    private PageResponse<HopDongDoiTuongTonItemResponse> buildChuaPhapLyLite(
            UUID loaiHopDongId, LocalDate ngayBaoCao, Pageable pageable) {
        int pageNumber = pageable.isPaged() ? pageable.getPageNumber() : 0;
        int pageSize = pageable.isPaged() ? pageable.getPageSize() : 0;
        LocalDate reportDate = ngayBaoCao != null ? ngayBaoCao : LocalDate.now(ZONE);

        Page<HopDongDoiTuongTonCandidateRow> pageResult =
                hopDongDoiTuongRepository.findChuaPhapLy(loaiHopDongId, pageable);
        List<HopDongDoiTuongTonCandidateRow> rows = pageResult.getContent();
        if (rows.isEmpty()) {
            return PageResponse.ofItems(List.of(), pageNumber, pageSize, pageResult.getTotalElements());
        }

        Map<UUID, Long> objectCountByHopDong = new HashMap<>();
        for (Object[] row : hopDongDoiTuongService.countGroupByHopDongId(Boolean.TRUE, loaiHopDongId, null)) {
            if (row[0] != null) {
                objectCountByHopDong.put((UUID) row[0], ((Number) row[1]).longValue());
            }
        }

        List<HopDongDoiTuongTonItemResponse> items = new ArrayList<>();
        for (HopDongDoiTuongTonCandidateRow row : rows) {
            LocalDate mocNgay = row.getResolvedNgayHt();
            long soNgay = mocNgay != null
                    ? Math.max(0L, ChronoUnit.DAYS.between(mocNgay, reportDate))
                    : 0L;
            items.add(HopDongDoiTuongTonItemResponse.builder()
                    .id(row.getId())
                    .hopDongId(row.getHopDongId())
                    .maTram("—")
                    .khuVuc("—")
                    .nhaThau("—")
                    .maHopDong(row.getMaHopDong())
                    .giaTri(chiaDeuGiaTriHd(row, objectCountByHopDong))
                    .sanLuongHieuLuc(row.getSanLuongHieuLuc())
                    .ngayHtTc(mocNgay)
                    .soNgayTon(soNgay)
                    .trangThai("thieu_phap_ly")
                    .build());
        }
        return PageResponse.ofItems(items, pageNumber, pageSize, pageResult.getTotalElements());
    }

    private static BigDecimal chiaDeuGiaTriHd(HopDongDoiTuongTonCandidateRow row, Map<UUID, Long> objectCountByHopDong) {
        long giaTriHd = row.getGiaTriHd() != null ? row.getGiaTriHd() : 0L;
        long soTram = objectCountByHopDong.getOrDefault(row.getHopDongId(), 0L);
        if (giaTriHd <= 0 || soTram <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(giaTriHd).divide(BigDecimal.valueOf(soTram), 2, RoundingMode.HALF_UP);
    }

    private static int normalizeQuaHanNgay(Integer quaHanNgay) {
        if (quaHanNgay == null || quaHanNgay < 0) {
            return DEFAULT_QUA_HAN_NGAY;
        }
        return Math.min(quaHanNgay, 3650);
    }
}
