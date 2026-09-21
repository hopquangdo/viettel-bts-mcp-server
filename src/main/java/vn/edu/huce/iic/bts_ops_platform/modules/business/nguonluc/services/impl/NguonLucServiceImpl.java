package vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LoaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.DoiTuongHopDongLienKetSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LoaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.common.dto.RankedItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.NguonViecBangDuLieu;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.NguonViecManualRowDto;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.request.NguonViecLuuBangRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.response.NguonLucDangTrienKhaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.response.NguonViecDanhSachResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.response.NguonViecRowResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.response.NguonViecTabCountsResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.entity.NguonViecBang;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.helpers.NguonViecTinhToanHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.repository.NguonViecBangRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.services.NguonLucService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.repository.SanLuongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.repository.VuongMacRepository;

import vn.edu.huce.iic.bts_ops_platform.common.util.UuidUtils;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.cache.NguonLucAggregateCache;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.cache.NguonLucCacheKeys;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.cache.NguonLucCacheNames;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NguonLucServiceImpl implements NguonLucService {

    private final HopDongService hopDongService;
    private final HopDongRepository hopDongRepository;
    private final SanLuongRepository sanLuongRepository;
    private final VuongMacRepository vuongMacRepository;
    private final KieuHopDongRepository kieuHopDongRepository;
    private final LoaiHopDongRepository loaiHopDongRepository;
    private final HopDongDoiTuongRepository hopDongDoiTuongRepository;
    private final NguonViecBangRepository nguonViecBangRepository;
    private final NguonLucAggregateCache nguonLucAggregateCache;

    @Override
    @Transactional(readOnly = true)
    public NguonLucDangTrienKhaiResponse dangTrienKhai() {
        return nguonLucAggregateCache.getOrLoad(
                NguonLucCacheNames.DANG_TRIEN_KHAI, NguonLucDangTrienKhaiResponse.class, this::computeDangTrienKhai);
    }

    private NguonLucDangTrienKhaiResponse computeDangTrienKhai() {
        NguonLucDangTrienKhaiResponse response = new NguonLucDangTrienKhaiResponse();
        response.setTongHopDong(hopDongRepository.countFiltered(Boolean.TRUE, null));
        response.setTongSanLuong(sanLuongRepository.countByNgayXoaIsNull());
        response.setTongHopDongDoiTuong(hopDongDoiTuongRepository.countByNgayXoaIsNull());
        response.setTongDangTrienKhai(response.getTongHopDong() + response.getTongSanLuong() + response.getTongHopDongDoiTuong());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public NguonViecDanhSachResponse danhSach(
            String trungTam,
            String loaiCv,
            String trangThai,
            String phapLy,
            String nhanSu,
            String linhVuc,
            String search,
            String tab,
            LocalDate tuNgay,
            LocalDate denNgay) {
        String resolvedTrungTam = normalizeTrungTam(trungTam);
        NguonViecBangDuLieu overlay = loadOverlay(resolvedTrungTam);
        List<NguonViecRowResponse> allRows = buildAllRows(overlay, resolvedTrungTam, tuNgay, denNgay);

        List<NguonViecRowResponse> filteredWithoutTab = allRows.stream()
                .filter(row -> NguonViecTinhToanHelper.matchesFilters(
                        row, search, loaiCv, trangThai, phapLy, nhanSu, linhVuc, trungTam, null))
                .toList();
        NguonViecTabCountsResponse tabCounts = NguonViecTinhToanHelper.buildTabCounts(filteredWithoutTab);

        List<NguonViecRowResponse> filtered = filteredWithoutTab.stream()
                .filter(row -> NguonViecTinhToanHelper.matchesFilters(
                        row, search, loaiCv, trangThai, phapLy, nhanSu, linhVuc, trungTam, tab))
                .toList();

        return NguonViecDanhSachResponse.builder()
                .rows(filtered)
                .summary(NguonViecTinhToanHelper.buildSummary(filtered))
                .tabCounts(tabCounts)
                .customColumns(overlay.getCustomColumns())
                .customValues(overlay.getCustomValues())
                .columnLayout(overlay.getColumnLayout())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<RankedItemResponse> topHopDong(String metric, int top) {
        String normalizedMetric = metric == null ? "vuong" : metric.trim().toLowerCase(Locale.ROOT);
        String cacheKey = NguonLucCacheKeys.topHopDong(normalizedMetric, top);
        return nguonLucAggregateCache.getOrLoad(cacheKey, List.class, () -> computeTopHopDong(normalizedMetric, top));
    }

    private List<RankedItemResponse> computeTopHopDong(String normalizedMetric, int top) {
        List<NguonViecRowResponse> rows = buildAllRows(new NguonViecBangDuLieu(), null, null, null);

        List<NguonViecRowResponse> candidates;
        Comparator<NguonViecRowResponse> comparator;
        java.util.function.Function<NguonViecRowResponse, Object> valueOf;

        switch (normalizedMetric) {
            case "huy" -> {
                candidates = rows;
                comparator = Comparator.comparingDouble(NguonViecRowResponse::getSlHuy).reversed();
                valueOf = NguonViecRowResponse::getSlHuy;
            }
            case "sanluong_thap" -> {
                candidates = rows.stream().filter(row -> row.getGiaTriHD() > 0).toList();
                comparator = Comparator.comparingDouble(row -> row.getSxTong() / row.getGiaTriHD());
                valueOf = row -> Math.round(row.getSxTong() / row.getGiaTriHD() * 1000) / 10.0;
            }
            case "at_risk" -> {
                candidates = rows.stream().filter(row -> "vuong_phap_ly".equals(row.getStatus())).toList();
                comparator = Comparator.comparingDouble(NguonViecRowResponse::getSlVuong).reversed();
                valueOf = NguonViecRowResponse::getSlVuong;
            }
            // Heuristic ghép từ tín hiệu sẵn có (vướng mắc mở + tỷ lệ chưa hoàn thành sản lượng +
            // đang vướng pháp lý) — KHÔNG PHẢI dự báo KPI thực (hệ thống chưa có dữ liệu hạn hợp
            // đồng/KPI cam kết để so sánh, cùng giới hạn đã ghi ở nguonluc_toc_do_hoan_thanh).
            case "rui_ro" -> {
                candidates = rows;
                comparator = Comparator.comparingDouble(NguonLucServiceImpl::riskScore).reversed();
                valueOf = row -> Math.round(riskScore(row) * 10) / 10.0;
            }
            default -> {
                candidates = rows;
                comparator = Comparator.comparingDouble(NguonViecRowResponse::getSlVuong).reversed();
                valueOf = NguonViecRowResponse::getSlVuong;
            }
        }

        return candidates.stream()
                .sorted(comparator)
                .limit(Math.max(1, top))
                .map(row -> RankedItemResponse.builder()
                        .label((row.getMaHD() != null ? row.getMaHD() : "—")
                                + (row.getChuongTrinh() != null ? " - " + row.getChuongTrinh() : ""))
                        .value(valueOf.apply(row))
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public NguonViecDanhSachResponse luuBang(NguonViecLuuBangRequest request) {
        String trungTam = normalizeTrungTam(request.getTrungTam());
        NguonViecBang entity = nguonViecBangRepository.findFirstByTrungTamIgnoreCaseAndNgayXoaIsNull(trungTam)
                .orElseGet(() -> {
                    NguonViecBang created = new NguonViecBang();
                    created.setTrungTam(trungTam);
                    created.setDuLieu(new NguonViecBangDuLieu());
                    return created;
                });

        NguonViecBangDuLieu duLieu = entity.getDuLieu() != null ? entity.getDuLieu() : new NguonViecBangDuLieu();
        duLieu.setCustomColumns(request.getCustomColumns() != null ? request.getCustomColumns() : new ArrayList<>());
        duLieu.setCustomValues(request.getCustomValues() != null ? request.getCustomValues() : new HashMap<>());
        duLieu.setManualRows(request.getManualRows() != null ? request.getManualRows() : new ArrayList<>());
        duLieu.setGhiChuKv(request.getGhiChuKv() != null ? request.getGhiChuKv() : new HashMap<>());
        duLieu.setColumnLayout(request.getColumnLayout());
        entity.setDuLieu(duLieu);
        nguonViecBangRepository.save(entity);

        return danhSach(trungTam, null, null, null, null, null, null, "all", null, null);
    }

    /**
     * Điểm rủi ro heuristic: 2 điểm/vướng mắc mở + tối đa 10 điểm theo tỷ lệ sản lượng CHƯA thực
     * hiện so với giá trị hợp đồng + 5 điểm nếu đang vướng pháp lý. Trọng số chọn thủ công để ưu
     * tiên "đang có vướng mắc thật" hơn "tiến độ sản lượng chậm nhưng chưa vướng gì cụ thể".
     */
    private static double riskScore(NguonViecRowResponse row) {
        double score = row.getSlVuong() * 2.0;
        if (row.getGiaTriHD() > 0) {
            double chuaHoanThanh = Math.max(0, 1 - row.getSxTong() / row.getGiaTriHD());
            score += chuaHoanThanh * 10.0;
        }
        if ("vuong_phap_ly".equals(row.getStatus())) {
            score += 5.0;
        }
        return score;
    }

    private List<NguonViecRowResponse> buildAllRows(
            NguonViecBangDuLieu overlay,
            String defaultTrungTam,
            LocalDate tuNgay,
            LocalDate denNgay) {
        LocalDate dateFrom = tuNgay != null ? tuNgay : LocalDate.of(Year.now().getValue(), Month.JANUARY, 1);
        LocalDate dateTo = denNgay != null ? denNgay : LocalDate.of(Year.now().getValue(), Month.DECEMBER, 31);
        LocalDate t1From = LocalDate.of(dateFrom.getYear(), Month.JANUARY, 1);
        LocalDate t1To = LocalDate.of(dateFrom.getYear(), Month.JUNE, 30);
        LocalDate t2From = LocalDate.of(dateFrom.getYear(), Month.JULY, 1);
        LocalDate t2To = LocalDate.of(dateFrom.getYear(), Month.DECEMBER, 31);

        Map<UUID, BigDecimal> sxT1Map = toHopDongAmountMap(
                sanLuongRepository.sumThanhTienGroupByHopDongResolved(Boolean.TRUE, clampStart(t1From, dateFrom), clampEnd(t1To, dateTo)));
        Map<UUID, BigDecimal> sxT2Map = toHopDongAmountMap(
                sanLuongRepository.sumThanhTienGroupByHopDongResolved(Boolean.TRUE, clampStart(t2From, dateFrom), clampEnd(t2To, dateTo)));
        Map<UUID, BigDecimal> sxTotalMap = toHopDongAmountMap(
                sanLuongRepository.sumThanhTienGroupByHopDongResolved(Boolean.TRUE, dateFrom, dateTo));
        Map<UUID, BigDecimal> dtTotalMap = toHopDongAmountMap(
                sanLuongRepository.sumThanhTienDtGroupByHopDongResolved(Boolean.TRUE, dateFrom, dateTo));
        Map<UUID, BigDecimal> dtT1Map = toHopDongAmountMap(
                sanLuongRepository.sumThanhTienDtGroupByHopDongResolved(Boolean.TRUE, clampStart(t1From, dateFrom), clampEnd(t1To, dateTo)));
        Map<UUID, BigDecimal> dtT2Map = toHopDongAmountMap(
                sanLuongRepository.sumThanhTienDtGroupByHopDongResolved(Boolean.TRUE, clampStart(t2From, dateFrom), clampEnd(t2To, dateTo)));
        Map<UUID, Long> openIssueMap = toHopDongCountMap(vuongMacRepository.countOpenIssuesGroupByHopDong());
        Map<UUID, Long> huyCountMap = buildHopDongStatusCountMap("HUY");
        Map<UUID, String> kieuHopDongMaMap = kieuHopDongRepository.findByNgayXoaIsNull().stream()
                .collect(Collectors.toMap(KieuHopDong::getId, item -> item.getMa() != null ? item.getMa() : "", (a, b) -> a));
        Map<UUID, LoaiHopDong> loaiHopDongMap = loaiHopDongRepository.findByNgayXoaIsNull().stream()
                .collect(Collectors.toMap(LoaiHopDong::getId, item -> item, (a, b) -> a));

        List<HopDongResponse> hopDongs = hopDongService.listAll(null, Boolean.TRUE, false, null, null);
        List<NguonViecRowResponse> rows = new ArrayList<>();
        int stt = 1;
        for (HopDongResponse hopDong : hopDongs) {
            UUID hopDongId = hopDong.getId();
            double giaTriTy = NguonViecTinhToanHelper.toTy(hopDong.getGiaTriHd());
            double sxT1 = NguonViecTinhToanHelper.toTy(sxT1Map.get(hopDongId));
            double sxT2 = NguonViecTinhToanHelper.toTy(sxT2Map.get(hopDongId));
            double sxTong = NguonViecTinhToanHelper.toTy(sxTotalMap.get(hopDongId));
            double dtT1 = NguonViecTinhToanHelper.toTy(dtT1Map.get(hopDongId));
            double dtT2 = NguonViecTinhToanHelper.toTy(dtT2Map.get(hopDongId));
            double dtTong = NguonViecTinhToanHelper.toTy(dtTotalMap.get(hopDongId));
            boolean hasOpenIssue = openIssueMap.getOrDefault(hopDongId, 0L) > 0;
            String trungTamValue = NguonViecTinhToanHelper.extractTrungTam(hopDong);
            String loaiCv = NguonViecTinhToanHelper.resolveLoaiCv(kieuHopDongMaMap.get(hopDong.getKieuHopDongId()));
            LoaiHopDong loaiHopDong = hopDong.getLoaiHopDongId() != null
                    ? loaiHopDongMap.get(hopDong.getLoaiHopDongId())
                    : null;
            String linhVucValue = NguonViecTinhToanHelper.resolveLinhVucFromLoai(
                    loaiHopDong != null ? loaiHopDong.getMa() : null,
                    loaiHopDong != null ? loaiHopDong.getTen() : null,
                    loaiHopDong != null ? loaiHopDong.getHeNghiepVu() : null);
            String nhaThau = NguonViecTinhToanHelper.findThuocTinh(hopDong.getThuocTinhGiaTri(), "nha thau", "nhà thầu");
            if (nhaThau.isBlank()) {
                nhaThau = "—";
            }

            NguonViecRowResponse row = NguonViecRowResponse.builder()
                    .id(hopDongId.toString())
                    .stt(stt++)
                    .chuongTrinh(hopDong.getTen() != null ? hopDong.getTen() : hopDong.getMaHopDong())
                    .maHD(hopDong.getMaHopDong() != null ? hopDong.getMaHopDong() : "—")
                    .linhVuc(linhVucValue)
                    .loaiCV(loaiCv)
                    .nhom("—")
                    .phapLyStage(NguonViecTinhToanHelper.mapPhapLyStage(hopDong.getTrangThaiPhapLy()))
                    .giaTriHD(giaTriTy)
                    .sxT1(sxT1)
                    .sxT2(sxT2)
                    .sxTong(sxTong)
                    .dtT1(dtT1)
                    .dtT2(dtT2)
                    .dtTong(dtTong)
                    .status(NguonViecTinhToanHelper.resolveStatus(giaTriTy, sxTong, hasOpenIssue))
                    .nhaThau(nhaThau)
                    .ghiChuKV(overlay.getGhiChuKv().getOrDefault(hopDongId.toString(), ""))
                    .trungTam(trungTamValue)
                    .ngayKyHD(NguonViecTinhToanHelper.findThuocTinh(hopDong.getThuocTinhGiaTri(), "ngay ky", "ngày ký"))
                    .ngayKetThuc(NguonViecTinhToanHelper.findThuocTinh(hopDong.getThuocTinhGiaTri(), "ngay ket thuc", "ngày kết thúc"))
                    .ngayCapNhat(NguonViecTinhToanHelper.formatInstant(hopDong.getNgayCapNhat()))
                    .soDoiKS(parseInt(NguonViecTinhToanHelper.findThuocTinh(hopDong.getThuocTinhGiaTri(), "doi ks", "đội ks", "khao sat")))
                    .soDoiTC(parseInt(NguonViecTinhToanHelper.findThuocTinh(hopDong.getThuocTinhGiaTri(), "doi tc", "đội tc", "thi cong")))
                    .slHuy(huyCountMap.getOrDefault(hopDongId, 0L).doubleValue())
                    .slVuong(openIssueMap.getOrDefault(hopDongId, 0L).doubleValue())
                    .manual(false)
                    .build();
            NguonViecTinhToanHelper.applyDerivedMetrics(row);
            rows.add(row);
        }

        for (NguonViecManualRowDto manual : overlay.getManualRows()) {
            rows.add(NguonViecTinhToanHelper.fromManualRow(manual, stt++, defaultTrungTam));
        }

        rows.sort(Comparator.comparingInt(NguonViecRowResponse::getStt));
        for (int index = 0; index < rows.size(); index++) {
            rows.get(index).setStt(index + 1);
        }
        return rows;
    }

    private NguonViecBangDuLieu loadOverlay(String trungTam) {
        return nguonViecBangRepository.findFirstByTrungTamIgnoreCaseAndNgayXoaIsNull(trungTam)
                .map(NguonViecBang::getDuLieu)
                .orElseGet(NguonViecBangDuLieu::new);
    }

    private static String normalizeTrungTam(String trungTam) {
        if (trungTam == null || trungTam.isBlank() || "all".equalsIgnoreCase(trungTam)) {
            return "TTKV2";
        }
        return trungTam.trim();
    }

    private static LocalDate clampStart(LocalDate preferred, LocalDate min) {
        return preferred.isBefore(min) ? min : preferred;
    }

    private static LocalDate clampEnd(LocalDate preferred, LocalDate max) {
        return preferred.isAfter(max) ? max : preferred;
    }

    private Map<UUID, Long> buildHopDongStatusCountMap(String statusKeyword) {
        Map<UUID, Long> result = new HashMap<>();
        String keyword = statusKeyword.toUpperCase(Locale.ROOT);
        for (Object[] row : hopDongDoiTuongRepository.countGroupByHopDongIdAndTrangThaiMa(
                Boolean.TRUE, null, null, DoiTuongHopDongLienKetSupport.HANG_MUC_THI_CONG_DOI_TUONG_ID)) {
            if (row[0] == null || row[1] == null) {
                continue;
            }
            String ma = row[1].toString().toUpperCase(Locale.ROOT);
            if (!ma.contains(keyword)) {
                continue;
            }
            UUID hopDongId = toHopDongUuid(row[0]);
            if (hopDongId == null) {
                continue;
            }
            long count = row[2] instanceof Number number ? number.longValue() : 0L;
            result.merge(hopDongId, count, Long::sum);
        }
        return result;
    }

    private static UUID toHopDongUuid(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof UUID uuid) {
            return uuid;
        }
        return UuidUtils.parseUuid(raw.toString());
    }

    private static Map<UUID, BigDecimal> toHopDongAmountMap(List<Object[]> rows) {
        Map<UUID, BigDecimal> result = new HashMap<>();
        for (Object[] row : rows) {
            UUID hopDongId = toHopDongUuid(row[0]);
            if (hopDongId == null || row[1] == null) {
                continue;
            }
            BigDecimal amount = row[1] instanceof BigDecimal bigDecimal
                    ? bigDecimal
                    : BigDecimal.valueOf(((Number) row[1]).doubleValue());
            result.put(hopDongId, amount);
        }
        return result;
    }

    private static Map<UUID, Long> toHopDongCountMap(List<Object[]> rows) {
        Map<UUID, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            UUID hopDongId = toHopDongUuid(row[0]);
            if (hopDongId == null || row[1] == null) {
                continue;
            }
            result.put(hopDongId, ((Number) row[1]).longValue());
        }
        return result;
    }

    private static int parseInt(String value) {
        if (value == null || value.isBlank() || "—".equals(value)) {
            return 0;
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
