package vn.edu.huce.iic.bts_ops_platform.mcp.handler.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankedItemResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec.NguonViecQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec.NguonViecRowToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec.NguonViecSummaryToolDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec.NguonViecTabCountsToolDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhathau.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.handler.NguonViecToolHandler;
import vn.edu.huce.iic.bts_ops_platform.mcp.repository.NguonViecOverlayToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.repository.NguonViecToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.NguonViecCalc;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec.NguonViecManualRowDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.nguonluc.NguonViecBang;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.hopdong.HopDong;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Impl thật cho tool AI module Nguồn việc — port lại logic tổng hợp của NguonLucService.danhSach/
 * topHopDong thành native query riêng của tools (xem NguonViecToolRepository/NguonViecCalc), KHÔNG
 * còn phụ thuộc NguonLucService/NguoiDungRepository ở module ngoài. Đã port phần overlay thủ công
 * (nguon_viec_bang: hàng tự thêm + ghi chú khu vực từ UI WorkSourceManagement) qua
 * {@link NguonViecOverlayToolRepository} — đọc thẳng entity/DTO JSON (không qua NguonLucService),
 * gộp overlay của TẤT CẢ trung tâm (AI tool không có khái niệm "1 trung tâm đang xem" như UI).
 */
@Component
@RequiredArgsConstructor
public class NguonViecToolHandlerImpl implements NguonViecToolHandler {

    private final NguonViecToolRepository repository;
    private final NguonViecOverlayToolRepository overlayRepository;
    private final NhaThauComponent nhaThauComponent;
    private final McpParallel parallel;

    @Override
    public NguonViecQueryResponse query(String khuVuc, String nhaThau, String trangThai, String loaiCv, String phapLy,
                                        String linhVuc, String tab,
                                        LocalDate fromDate, LocalDate toDate, String query, Integer topIn,
                                        Integer page, Integer pageSize, Double nguongSapHet) {
        NhaThauInfo resolvedNhaThau = nhaThauComponent.resolve(nhaThau);
        String loaiCvLoc = ResolveSupport.enumValue("loaiCv", loaiCv, "kstk", "tc", "tk");
        String phapLyLoc = ResolveSupport.enumValue("phapLy", phapLy, "da_co", "chua_co", "nhap");
        String tabLoc = ResolveSupport.enumValue("tab", tab, "all", "active", "nearly");
        List<NguonViecRowToolItem> allRows = buildRows(fromDate, toDate);
        List<NguonViecRowToolItem> filteredRows = allRows.stream()
            .filter(row -> NguonViecCalc.matchesFilters(row, query, trangThai, khuVuc, loaiCvLoc, phapLyLoc, linhVuc))
                .toList();

        String tenNhaThau = resolvedNhaThau.ten();
        List<NguonViecRowToolItem> rows = filteredRows;
        if (tenNhaThau != null) {
            String needle = tenNhaThau.toLowerCase();
            rows = rows.stream()
                    .filter(row -> row.getNhaThau() != null && row.getNhaThau().toLowerCase().contains(needle))
                    .toList();
        }

        if ("active".equals(tabLoc)) {
            rows = rows.stream().filter(r -> "dang_trien_khai".equals(r.getStatus()) || "vuong_phap_ly".equals(r.getStatus())).toList();
        } else if ("nearly".equals(tabLoc)) {
            rows = rows.stream().filter(r -> "gan_hoan_thanh".equals(r.getStatus())).toList();
        }

        int resolvedPage = PagingUtil.resolvePage(page);
        int resolvedPageSize = PagingUtil.resolvePageSize(pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE);
        int from = Math.min(resolvedPage * resolvedPageSize, rows.size());
        int to = Math.min(from + resolvedPageSize, rows.size());

        int top = topIn != null ? Math.min(Math.max(topIn, 1), 10) : 5;
        List<RankedItemResponse> xepHangRuiRo = computeXepHangRuiRo(allRows, top);

        // Toàn bộ hợp đồng nguồn việc, KHÔNG lọc theo trungTam — dùng để xếp hạng trung tâm và tìm
        // hợp đồng sắp hết giá trị trên toàn hệ thống (không giới hạn trong 1 trung tâm cụ thể).
        // allRows chính là tập này (chỉ filteredRows/rows mới bị lọc) nên dùng lại, không dựng lại lần 2.
        List<NguonViecRowToolItem> rankingRows = allRows;

        return NguonViecQueryResponse.builder()
                .summary(buildSummary(filteredRows))
                .tabCounts(buildTabCounts(filteredRows))
                .danhSach(PagedResult.of(rows.subList(from, to).stream().map(NguonViecToolHandlerImpl::toDisplayItem).toList(),
                        resolvedPage, resolvedPageSize, rows.size()))
                .xepHangRuiRo(xepHangRuiRo)
                .xepHangTrungTam(computeXepHangTrungTam(rankingRows))
                .sapHetGiaTri(computeSapHetGiaTri(rankingRows, nguongSapHet != null ? nguongSapHet : DEFAULT_NGUONG_SAP_HET))
                .build();
    }

    /** Port NguonLucServiceImpl.buildAllRows — mỗi hợp đồng active 1 dòng, không áp overlay thủ công. */
    private List<NguonViecRowToolItem> buildRows(LocalDate fromDateIn, LocalDate toDateIn) {
        LocalDate dateFrom = fromDateIn != null ? fromDateIn : LocalDate.of(Year.now().getValue(), Month.JANUARY, 1);
        LocalDate dateTo = toDateIn != null ? toDateIn : LocalDate.of(Year.now().getValue(), Month.DECEMBER, 31);
        LocalDate t1From = clampStart(LocalDate.of(dateFrom.getYear(), Month.JANUARY, 1), dateFrom);
        LocalDate t1To = clampEnd(LocalDate.of(dateFrom.getYear(), Month.JUNE, 30), dateTo);
        LocalDate t2From = clampStart(LocalDate.of(dateFrom.getYear(), Month.JULY, 1), dateFrom);
        LocalDate t2To = clampEnd(LocalDate.of(dateFrom.getYear(), Month.DECEMBER, 31), dateTo);

        // 7 truy vấn độc lập: chạy song song, độ trễ = câu chậm nhất thay vì tổng 7 câu
        var fTien = parallel.async(() -> repository.sumThanhTienTheoKy(dateFrom, dateTo, t1From, t1To, t2From, t2To));
        var fIssue = parallel.async(repository::countOpenIssuesGroupByHopDong);
        var fHuy = parallel.async(() -> repository.countGroupByHopDongIdAndTrangThaiMa(NguonViecToolRepository.HANG_MUC_THI_CONG_DOI_TUONG_ID));
        var fKieu = parallel.async(repository::findAllKieuHopDongIdMa);
        var fLoai = parallel.async(repository::findAllLoaiHopDong);
        var fHopDong = parallel.async(repository::findActiveHopDong);
        var fThuocTinh = parallel.async(repository::findThuocTinhHopDongActive);

        // 6 tổng tiền (sản lượng và "đã thực hiện", mỗi loại theo kỳ 1, kỳ 2, cả khoảng) từ 1 câu
        Map<UUID, BigDecimal> sxT1Map = new HashMap<>();
        Map<UUID, BigDecimal> sxT2Map = new HashMap<>();
        Map<UUID, BigDecimal> sxTotalMap = new HashMap<>();
        Map<UUID, BigDecimal> dtT1Map = new HashMap<>();
        Map<UUID, BigDecimal> dtT2Map = new HashMap<>();
        Map<UUID, BigDecimal> dtTotalMap = new HashMap<>();
        for (Object[] row : McpParallel.get(fTien)) {
            UUID id = (UUID) row[0];
            sxT1Map.put(id, amount(row[1]));
            sxT2Map.put(id, amount(row[2]));
            sxTotalMap.put(id, amount(row[3]));
            dtT1Map.put(id, amount(row[4]));
            dtT2Map.put(id, amount(row[5]));
            dtTotalMap.put(id, amount(row[6]));
        }
        Map<UUID, Long> openIssueMap = toCountMap(McpParallel.get(fIssue));
        Map<UUID, Long> huyCountMap = buildHuyCountMap(McpParallel.get(fHuy));

        Map<UUID, String> kieuHopDongMaMap = new HashMap<>();
        for (Object[] row : McpParallel.get(fKieu)) {
            kieuHopDongMaMap.put((UUID) row[0], row[1] != null ? (String) row[1] : "");
        }
        Map<UUID, Object[]> loaiHopDongMap = new HashMap<>();
        for (Object[] row : McpParallel.get(fLoai)) {
            loaiHopDongMap.put((UUID) row[0], row);
        }

        List<HopDong> hopDongs = McpParallel.get(fHopDong);
        Map<UUID, List<String[]>> thuocTinhByHopDong = new HashMap<>();
        for (Object[] row : McpParallel.get(fThuocTinh)) {
            UUID hopDongId = (UUID) row[0];
            thuocTinhByHopDong.computeIfAbsent(hopDongId, k -> new ArrayList<>())
                    .add(new String[]{(String) row[1], (String) row[2]});
        }

        List<NguonViecRowToolItem> rows = new ArrayList<>();
        int stt = 1;
        for (HopDong hopDong : hopDongs) {
            UUID hopDongId = hopDong.getId();
            List<String[]> attrs = thuocTinhByHopDong.getOrDefault(hopDongId, List.of());
            double giaTriTy = NguonViecCalc.toTy(hopDong.getGiaTriHd());
            double sxT1 = NguonViecCalc.toTy(sxT1Map.get(hopDongId));
            double sxT2 = NguonViecCalc.toTy(sxT2Map.get(hopDongId));
            double sxTong = NguonViecCalc.toTy(sxTotalMap.get(hopDongId));
            double dtT1 = NguonViecCalc.toTy(dtT1Map.get(hopDongId));
            double dtT2 = NguonViecCalc.toTy(dtT2Map.get(hopDongId));
            double dtTong = NguonViecCalc.toTy(dtTotalMap.get(hopDongId));
            boolean hasOpenIssue = openIssueMap.getOrDefault(hopDongId, 0L) > 0;
            String loaiCv = NguonViecCalc.resolveLoaiCv(kieuHopDongMaMap.get(hopDong.getKieuHopDongId()));
            Object[] loaiHopDong = hopDong.getLoaiHopDongId() != null ? loaiHopDongMap.get(hopDong.getLoaiHopDongId()) : null;
            String linhVucValue = NguonViecCalc.resolveLinhVucFromLoai(
                    loaiHopDong != null ? (String) loaiHopDong[1] : null,
                    loaiHopDong != null ? (String) loaiHopDong[2] : null,
                    loaiHopDong != null ? (String) loaiHopDong[3] : null);
            String nhaThau = NguonViecCalc.findThuocTinh(attrs, "nha thau", "nhà thầu");
            if (nhaThau.isBlank()) {
                nhaThau = "—";
            }

            NguonViecRowToolItem row = NguonViecRowToolItem.builder()
                    .id(hopDongId.toString())
                    .stt(stt++)
                    .chuongTrinh(hopDong.getTen() != null ? hopDong.getTen() : hopDong.getMaHopDong())
                    .maHD(hopDong.getMaHopDong() != null ? hopDong.getMaHopDong() : "—")
                    .linhVuc(linhVucValue)
                    .loaiCV(loaiCv)
                    .nhom("—")
                    .phapLyStage(NguonViecCalc.mapPhapLyStage(hopDong.getTrangThaiPhapLy()))
                    .giaTriHD(giaTriTy)
                    .sxT1(sxT1)
                    .sxT2(sxT2)
                    .sxTong(sxTong)
                    .dtT1(dtT1)
                    .dtT2(dtT2)
                    .dtTong(dtTong)
                    .status(NguonViecCalc.resolveStatus(giaTriTy, sxTong, hasOpenIssue))
                    .nhaThau(nhaThau)
                    .ghiChuKV("")
                    .trungTam(NguonViecCalc.extractTrungTam(attrs))
                    .ngayKyHD(NguonViecCalc.findThuocTinh(attrs, "ngay ky", "ngày ký"))
                    .ngayKetThuc(NguonViecCalc.findThuocTinh(attrs, "ngay ket thuc", "ngày kết thúc"))
                    .ngayCapNhat(NguonViecCalc.formatInstant(hopDong.getNgayCapNhat()))
                    .soDoiKS(NguonViecCalc.parseInt(NguonViecCalc.findThuocTinh(attrs, "doi ks", "đội ks", "khao sat")))
                    .soDoiTC(NguonViecCalc.parseInt(NguonViecCalc.findThuocTinh(attrs, "doi tc", "đội tc", "thi cong")))
                    .slHuy(huyCountMap.getOrDefault(hopDongId, 0L).doubleValue())
                    .slVuong(openIssueMap.getOrDefault(hopDongId, 0L).doubleValue())
                    .manual(false)
                    .build();
            NguonViecCalc.applyDerivedMetrics(row);
            rows.add(row);
        }
        applyOverlay(rows, stt);
        return rows;
    }

    /**
     * Áp overlay thủ công (nguon_viec_bang: ghi chú khu vực theo hopDongId + hàng tự thêm) — gộp
     * TẤT CẢ trung tâm (AI tool không có khái niệm "1 trung tâm đang xem" như UI WorkSourceManagement).
     * Trước đây phần này bị bỏ qua hoàn toàn (xem Javadoc lớp cũ) — nay đã port lại để số liệu AI trả
     * lời không thiếu các dòng/ghi chú người dùng tự thêm trên UI.
     */
    private void applyOverlay(List<NguonViecRowToolItem> rows, int nextStt) {
        List<NguonViecBang> overlays = overlayRepository.findByNgayXoaIsNull();
        if (overlays.isEmpty()) {
            return;
        }
        Map<String, String> ghiChuById = new HashMap<>();
        List<NguonViecManualRowDto> manualRows = new ArrayList<>();
        for (NguonViecBang overlay : overlays) {
            if (overlay.getDuLieu() == null) {
                continue;
            }
            if (overlay.getDuLieu().getGhiChuKv() != null) {
                ghiChuById.putAll(overlay.getDuLieu().getGhiChuKv());
            }
            if (overlay.getDuLieu().getManualRows() != null) {
                manualRows.addAll(overlay.getDuLieu().getManualRows());
            }
        }
        if (!ghiChuById.isEmpty()) {
            for (NguonViecRowToolItem row : rows) {
                String ghiChu = ghiChuById.get(row.getId());
                if (ghiChu != null) {
                    row.setGhiChuKV(ghiChu);
                }
            }
        }
        int stt = nextStt;
        for (NguonViecManualRowDto manual : manualRows) {
            rows.add(NguonViecCalc.fromManualRow(manual, stt++, "TTKV2"));
        }
    }

    private Map<UUID, Long> buildHuyCountMap(List<Object[]> rowsTheoTrangThai) {
        Map<UUID, Long> result = new HashMap<>();
        for (Object[] row : rowsTheoTrangThai) {
            if (row[0] == null || row[1] == null) {
                continue;
            }
            String ma = row[1].toString();
            if (!ma.contains("HUY")) {
                continue;
            }
            UUID hopDongId = (UUID) row[0];
            long count = ((Number) row[2]).longValue();
            result.merge(hopDongId, count, Long::sum);
        }
        return result;
    }

    private static LocalDate clampStart(LocalDate preferred, LocalDate min) {
        return preferred.isBefore(min) ? min : preferred;
    }

    private static LocalDate clampEnd(LocalDate preferred, LocalDate max) {
        return preferred.isAfter(max) ? max : preferred;
    }

    private static BigDecimal amount(Object value) {
        return value instanceof BigDecimal bd ? bd : BigDecimal.valueOf(((Number) value).doubleValue());
    }

    private static Map<UUID, BigDecimal> toAmountMap(List<Object[]> rows) {
        Map<UUID, BigDecimal> result = new HashMap<>();
        for (Object[] row : rows) {
            if (row[0] == null || row[1] == null) {
                continue;
            }
            BigDecimal amount = row[1] instanceof BigDecimal bigDecimal
                    ? bigDecimal : BigDecimal.valueOf(((Number) row[1]).doubleValue());
            result.put((UUID) row[0], amount);
        }
        return result;
    }

    private static Map<UUID, Long> toCountMap(List<Object[]> rows) {
        Map<UUID, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            if (row[0] == null || row[1] == null) {
                continue;
            }
            result.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }

    private static NguonViecSummaryToolDto buildSummary(List<NguonViecRowToolItem> rows) {
        double giaTri = 0;
        double sx = 0;
        double dt = 0;
        double dtCon = 0;
        double chuaKt = 0;
        double slCon = 0;
        for (NguonViecRowToolItem row : rows) {
            giaTri += row.getGiaTriHD();
            sx += row.getSxTong();
            dt += row.getDtTong();
            dtCon += row.getDtConSL();
            chuaKt += row.getChuaKhaThi();
            slCon += row.getSlConHD();
        }
        return NguonViecSummaryToolDto.builder()
                .giaTriHD(NguonViecCalc.round2(giaTri))
                .sxTong(NguonViecCalc.round2(sx))
                .dtTong(NguonViecCalc.round2(dt))
                .dtConSL(NguonViecCalc.round2(dtCon))
                .chuaKhaThi(NguonViecCalc.round2(chuaKt))
                .slConHD(NguonViecCalc.round2(slCon))
                .build();
    }

    private static NguonViecTabCountsToolDto buildTabCounts(List<NguonViecRowToolItem> rows) {
        long active = rows.stream()
                .filter(row -> "dang_trien_khai".equals(row.getStatus()) || "vuong_phap_ly".equals(row.getStatus()))
                .count();
        long nearly = rows.stream().filter(row -> "gan_hoan_thanh".equals(row.getStatus())).count();
        return NguonViecTabCountsToolDto.builder()
                .all(rows.size())
                .active(active)
                .nearly(nearly)
                .build();
    }

    private List<RankedItemResponse> computeXepHangRuiRo(List<NguonViecRowToolItem> rows, int top) {
        return rows.stream()
                .sorted(Comparator.comparingDouble(NguonViecCalc::riskScore).reversed())
                .limit(top)
                .map(row -> RankedItemResponse.builder()
                        .label((row.getMaHD() != null ? row.getMaHD() : "—")
                                + (row.getChuongTrinh() != null ? " - " + row.getChuongTrinh() : ""))
                        .value(Math.round(NguonViecCalc.riskScore(row) * 10) / 10.0)
                        .build())
                .toList();
    }

    private List<RankedItemResponse> computeXepHangTrungTam(List<NguonViecRowToolItem> rows) {
        record Acc(double[] sxTong, double[] giaTriHD, int[] soHopDong) {
        }
        Map<String, Acc> byTrungTam = new LinkedHashMap<>();
        for (NguonViecRowToolItem row : rows) {
            String trungTam = row.getTrungTam() != null && !row.getTrungTam().isBlank() ? row.getTrungTam() : "—";
            Acc acc = byTrungTam.computeIfAbsent(trungTam, k -> new Acc(new double[]{0}, new double[]{0}, new int[]{0}));
            acc.sxTong()[0] += row.getSxTong();
            acc.giaTriHD()[0] += row.getGiaTriHD();
            acc.soHopDong()[0]++;
        }
        return byTrungTam.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue().sxTong()[0], a.getValue().sxTong()[0]))
                .map(entry -> RankedItemResponse.builder()
                        .label(entry.getKey())
                        .value(Math.round(entry.getValue().sxTong()[0] * 100) / 100.0)
                        .extra(Map.of(
                                "giaTriHD", Math.round(entry.getValue().giaTriHD()[0] * 100) / 100.0,
                                "soHopDong", entry.getValue().soHopDong()[0]))
                        .build())
                .toList();
    }

    /** Hợp đồng còn <= nguongSapHetPhanTram % (mặc định 10) giá trị được giao chưa dùng đến (slConHD/giaTriHD), giaTriHD > 0 — sắp hết trước. */
    private static final double DEFAULT_NGUONG_SAP_HET = 10.0;

    private List<RankedItemResponse> computeSapHetGiaTri(List<NguonViecRowToolItem> rows, double nguongSapHetPhanTram) {
        return rows.stream()
                .filter(row -> row.getGiaTriHD() > 0)
                .filter(row -> row.getSlConHD() / row.getGiaTriHD() * 100.0 <= nguongSapHetPhanTram)
                .sorted(Comparator.comparingDouble(NguonViecRowToolItem::getSlConHD))
                .limit(10)
                .map(row -> RankedItemResponse.builder()
                        .label((row.getMaHD() != null ? row.getMaHD() : "—")
                                + (row.getChuongTrinh() != null ? " - " + row.getChuongTrinh() : ""))
                        .value(Math.round(row.getSlConHD() * 100) / 100.0)
                        .extra(Map.of(
                                "giaTriHD", row.getGiaTriHD(),
                                "trungTam", row.getTrungTam() != null ? row.getTrungTam() : "—"))
                        .build())
                .toList();
    }

    /** Áp nhãn hiển thị (StatusLabels) trước khi trả ra ngoài — dữ liệu nội bộ vẫn dùng mã gốc để lọc/so sánh/xếp hạng. */
    private static NguonViecRowToolItem toDisplayItem(NguonViecRowToolItem r) {
        return NguonViecRowToolItem.builder()
                .id(r.getId())
                .stt(r.getStt())
                .chuongTrinh(r.getChuongTrinh())
                .maHD(r.getMaHD())
                .linhVuc(StatusLabels.nguonViecLinhVuc(r.getLinhVuc()))
                .loaiCV(StatusLabels.nguonViecLoaiCv(r.getLoaiCV()))
                .nhom(r.getNhom())
                .phapLyStage(StatusLabels.nguonViecPhapLyStage(r.getPhapLyStage()))
                .giaTriHD(r.getGiaTriHD())
                .sxT1(r.getSxT1())
                .sxT2(r.getSxT2())
                .sxTong(r.getSxTong())
                .dtT1(r.getDtT1())
                .dtT2(r.getDtT2())
                .dtTong(r.getDtTong())
                .slConHD(r.getSlConHD())
                .dtConSL(r.getDtConSL())
                .chuaKhaThi(r.getChuaKhaThi())
                .slHuy(r.getSlHuy())
                .slVuong(r.getSlVuong())
                .status(StatusLabels.nguonViecStatus(r.getStatus()))
                .nhaThau(r.getNhaThau())
                .ghiChuKV(r.getGhiChuKV())
                .trungTam(r.getTrungTam())
                .ngayKyHD(r.getNgayKyHD())
                .ngayKetThuc(r.getNgayKetThuc())
                .ngayCapNhat(r.getNgayCapNhat())
                .soDoiKS(r.getSoDoiKS())
                .soDoiTC(r.getSoDoiTC())
                .manual(r.isManual())
                .build();
    }
}
