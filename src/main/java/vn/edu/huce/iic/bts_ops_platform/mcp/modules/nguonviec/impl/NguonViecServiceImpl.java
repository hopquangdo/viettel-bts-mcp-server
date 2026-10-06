package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.impl;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankedItemResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecDanhSachResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecManualRowDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecRowToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecSapHetGiaTriResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecSummaryToolDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecTabCountsToolDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecToolMapper;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecXepHangResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.hopdong.HopDong;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.nguonluc.NguonViecBang;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang.NguonViecXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang.NguonViecXepHangRegistry;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang.NguonViecXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.NguonViecOverlayToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.NguonViecToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.NguonViecCalc;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class NguonViecServiceImpl implements NguonViecService {

    private static final double DEFAULT_NGUONG_SAP_HET = 10.0;

    private final NguonViecToolRepository repository;
    private final NguonViecOverlayToolRepository overlayRepository;
    private final NhaThauComponent nhaThauComponent;
    private final McpParallel parallel;
    private final NguonViecXepHangRegistry xepHangRegistry;

    private record Scope(String khuVuc, String tenNhaThau, String trangThai, String loaiCvLoc, String phapLyLoc,
                         String linhVuc, String query, LocalDate fromDate, LocalDate toDate) {
    }

    public NguonViecTongQuanResponse tongQuan(NguonViecFilter filter) {
        Scope scope = resolve(filter);
        List<NguonViecRowToolItem> filteredRows = filterRows(
                McpParallel.get(rowsAsync(scope.fromDate(), scope.toDate())), scope);
        LocalDate[] ky = kyTinh(scope.fromDate(), scope.toDate());
        return NguonViecToolMapper.tongQuan(ky[0], ky[1], buildSummary(filteredRows), buildTabCounts(filteredRows));
    }

    public NguonViecDanhSachResponse danhSach(NguonViecFilter filter, String tab, Integer page, Integer pageSize) {
        Scope scope = resolve(filter);
        String tabLoc = resolveTab(tab);
        List<NguonViecRowToolItem> filteredRows = filterRows(
                McpParallel.get(rowsAsync(scope.fromDate(), scope.toDate())), scope);
        return new NguonViecDanhSachResponse(buildDanhSach(filteredRows, scope, tabLoc, page, pageSize));
    }

    public NguonViecXepHangResponse xepHang(String theo, LocalDate fromDate, LocalDate toDate, Integer top) {
        NguonViecXepHangChieu chieu = NguonViecXepHangChieu.of(theo);
        NguonViecXepHangStrategy strategy = xepHangRegistry.get(chieu);
        LocalDate[] ky = kyTinh(fromDate, toDate);
        List<RankedItemResponse> rows = strategy.rank(McpParallel.get(rowsAsync(fromDate, toDate)), topOf(top));
        return NguonViecToolMapper.xepHang(chieu, strategy.tieuChi(), ky[0], ky[1], rows);
    }

    public NguonViecSapHetGiaTriResponse sapHetGiaTri(LocalDate fromDate, LocalDate toDate, Double nguongSapHet) {
        double nguong = nguongSapHet != null ? nguongSapHet : DEFAULT_NGUONG_SAP_HET;
        LocalDate[] ky = kyTinh(fromDate, toDate);
        return NguonViecToolMapper.sapHetGiaTri(ky[0], ky[1], nguong,
                sapHetGiaTriRows(McpParallel.get(rowsAsync(fromDate, toDate)), nguong));
    }

    public NguonViecQueryResponse query(String khuVuc, String nhaThau, String trangThai, String loaiCv, String phapLy,
                                        String linhVuc, String tab,
                                        LocalDate fromDate, LocalDate toDate, String query, Integer topIn,
                                        Integer page, Integer pageSize, Double nguongSapHet) {
        Scope scope = resolve(new NguonViecFilter(khuVuc, nhaThau, trangThai, loaiCv, phapLy, linhVuc, query,
                fromDate, toDate));
        String tabLoc = resolveTab(tab);
        List<NguonViecRowToolItem> allRows = McpParallel.get(rowsAsync(scope.fromDate(), scope.toDate()));
        List<NguonViecRowToolItem> filteredRows = filterRows(allRows, scope);
        int top = topOf(topIn);

        return NguonViecQueryResponse.builder()
                .summary(buildSummary(filteredRows))
                .tabCounts(buildTabCounts(filteredRows))
                .danhSach(buildDanhSach(filteredRows, scope, tabLoc, page, pageSize))
                .xepHangRuiRo(xepHangRegistry.get(NguonViecXepHangChieu.RUI_RO).rank(allRows, top))
                .xepHangTrungTam(xepHangRegistry.get(NguonViecXepHangChieu.TRUNG_TAM).rank(allRows, top))
                .sapHetGiaTri(sapHetGiaTriRows(allRows,
                        nguongSapHet != null ? nguongSapHet : DEFAULT_NGUONG_SAP_HET).stream()
                        .map(NguonViecServiceImpl::sapHetItem).toList())
                .build();
    }

    private Scope resolve(NguonViecFilter filter) {
        String tenNhaThau = nhaThauComponent.resolve(filter.nhaThau()).ten();
        return new Scope(
                filter.khuVuc(),
                tenNhaThau,
                filter.trangThai(),
                ResolveSupport.enumValue("loaiCv", filter.loaiCv(), "kstk", "tc", "tk"),
                ResolveSupport.enumValue("phapLy", filter.phapLy(), "da_co", "chua_co", "nhap"),
                filter.linhVuc(),
                filter.query(),
                filter.fromDate(),
                filter.toDate());
    }

    private static String resolveTab(String tab) {
        return ResolveSupport.enumValue("tab", tab, "all", "active", "nearly");
    }

    private static List<NguonViecRowToolItem> filterRows(List<NguonViecRowToolItem> allRows, Scope scope) {
        return allRows.stream()
                .filter(row -> NguonViecCalc.matchesFilters(row, scope.query(), scope.trangThai(), scope.khuVuc(),
                        scope.loaiCvLoc(), scope.phapLyLoc(), scope.linhVuc()))
                .toList();
    }

    private static PagedResult<NguonViecRowToolItem> buildDanhSach(List<NguonViecRowToolItem> filteredRows,
                                                                    Scope scope, String tabLoc,
                                                                    Integer page, Integer pageSize) {
        List<NguonViecRowToolItem> rows = filteredRows;
        if (scope.tenNhaThau() != null) {
            String needle = scope.tenNhaThau().toLowerCase();
            rows = rows.stream()
                    .filter(row -> row.getNhaThau() != null && row.getNhaThau().toLowerCase().contains(needle))
                    .toList();
        }

        if ("active".equals(tabLoc)) {
            rows = rows.stream().filter(row -> "dang_trien_khai".equals(row.getStatus())
                    || "vuong_phap_ly".equals(row.getStatus())).toList();
        } else if ("nearly".equals(tabLoc)) {
            rows = rows.stream().filter(row -> "gan_hoan_thanh".equals(row.getStatus())).toList();
        }

        int resolvedPage = PagingUtil.resolvePage(page);
        int resolvedPageSize = PagingUtil.resolvePageSize(pageSize, PagingUtil.DEFAULT_PAGE_SIZE,
                PagingUtil.MAX_PAGE_SIZE);
        int from = Math.min(resolvedPage * resolvedPageSize, rows.size());
        int to = Math.min(from + resolvedPageSize, rows.size());
        return PagedResult.of(rows.subList(from, to).stream().map(NguonViecServiceImpl::toDisplayItem).toList(),
                resolvedPage, resolvedPageSize, rows.size());
    }

    private static int topOf(Integer topIn) {
        return topIn != null ? Math.min(Math.max(topIn, 1), 10) : 5;
    }

    private static LocalDate[] kyTinh(LocalDate fromDateIn, LocalDate toDateIn) {
        return new LocalDate[] {
                fromDateIn != null ? fromDateIn : LocalDate.of(Year.now().getValue(), Month.JANUARY, 1),
                toDateIn != null ? toDateIn : LocalDate.of(Year.now().getValue(), Month.DECEMBER, 31)};
    }

    private CompletableFuture<List<NguonViecRowToolItem>> rowsAsync(LocalDate fromDateIn, LocalDate toDateIn) {
        LocalDate[] ky = kyTinh(fromDateIn, toDateIn);
        LocalDate dateFrom = ky[0];
        LocalDate dateTo = ky[1];
        LocalDate t1From = clampStart(LocalDate.of(dateFrom.getYear(), Month.JANUARY, 1), dateFrom);
        LocalDate t1To = clampEnd(LocalDate.of(dateFrom.getYear(), Month.JUNE, 30), dateTo);
        LocalDate t2From = clampStart(LocalDate.of(dateFrom.getYear(), Month.JULY, 1), dateFrom);
        LocalDate t2To = clampEnd(LocalDate.of(dateFrom.getYear(), Month.DECEMBER, 31), dateTo);

        var fTien = parallel.async(() -> repository.sumThanhTienTheoKy(dateFrom, dateTo, t1From, t1To, t2From, t2To));
        var fIssue = parallel.async(repository::countOpenIssuesGroupByHopDong);
        var fHuy = parallel.async(() -> repository.countGroupByHopDongIdAndTrangThaiMa(
                NguonViecToolRepository.HANG_MUC_THI_CONG_DOI_TUONG_ID));
        var fKieu = parallel.async(repository::findAllKieuHopDongIdMa);
        var fLoai = parallel.async(repository::findAllLoaiHopDong);
        var fHopDong = parallel.async(repository::findActiveHopDong);
        var fThuocTinh = parallel.async(repository::findThuocTinhHopDongActive);
        var fOverlay = parallel.async(overlayRepository::findByNgayXoaIsNull);

        return CompletableFuture.allOf(fTien, fIssue, fHuy, fKieu, fLoai, fHopDong, fThuocTinh, fOverlay)
                .thenApply(ignored -> buildRows(fTien.join(), fIssue.join(), fHuy.join(), fKieu.join(), fLoai.join(),
                        fHopDong.join(), fThuocTinh.join(), fOverlay.join()));
    }

    private List<NguonViecRowToolItem> buildRows(List<Object[]> tienRows, List<Object[]> issueRows,
                                                  List<Object[]> huyRows, List<Object[]> kieuRows,
                                                  List<Object[]> loaiRows, List<HopDong> hopDongs,
                                                  List<Object[]> thuocTinhRows, List<NguonViecBang> overlays) {
        Map<UUID, BigDecimal> sxT1Map = new HashMap<>();
        Map<UUID, BigDecimal> sxT2Map = new HashMap<>();
        Map<UUID, BigDecimal> sxTotalMap = new HashMap<>();
        Map<UUID, BigDecimal> dtT1Map = new HashMap<>();
        Map<UUID, BigDecimal> dtT2Map = new HashMap<>();
        Map<UUID, BigDecimal> dtTotalMap = new HashMap<>();
        for (Object[] row : tienRows) {
            UUID id = (UUID) row[0];
            sxT1Map.put(id, amount(row[1]));
            sxT2Map.put(id, amount(row[2]));
            sxTotalMap.put(id, amount(row[3]));
            dtT1Map.put(id, amount(row[4]));
            dtT2Map.put(id, amount(row[5]));
            dtTotalMap.put(id, amount(row[6]));
        }
        Map<UUID, Long> openIssueMap = toCountMap(issueRows);
        Map<UUID, Long> huyCountMap = buildHuyCountMap(huyRows);

        Map<UUID, String> kieuHopDongMaMap = new HashMap<>();
        for (Object[] row : kieuRows) {
            kieuHopDongMaMap.put((UUID) row[0], row[1] != null ? (String) row[1] : "");
        }
        Map<UUID, Object[]> loaiHopDongMap = new HashMap<>();
        for (Object[] row : loaiRows) {
            loaiHopDongMap.put((UUID) row[0], row);
        }

        Map<UUID, List<String[]>> thuocTinhByHopDong = new HashMap<>();
        for (Object[] row : thuocTinhRows) {
            UUID hopDongId = (UUID) row[0];
            thuocTinhByHopDong.computeIfAbsent(hopDongId, ignored -> new ArrayList<>())
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
            Object[] loaiHopDong = hopDong.getLoaiHopDongId() != null
                    ? loaiHopDongMap.get(hopDong.getLoaiHopDongId()) : null;
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
        applyOverlay(rows, stt, overlays);
        return rows;
    }

    private void applyOverlay(List<NguonViecRowToolItem> rows, int nextStt, List<NguonViecBang> overlays) {
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
        return value instanceof BigDecimal decimal ? decimal : BigDecimal.valueOf(((Number) value).doubleValue());
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
                .slHuy(rows.stream().mapToDouble(NguonViecRowToolItem::getSlHuy).sum())
                .slVuong(rows.stream().mapToDouble(NguonViecRowToolItem::getSlVuong).sum())
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

    private static List<NguonViecRowToolItem> sapHetGiaTriRows(List<NguonViecRowToolItem> rows,
                                                                double nguongSapHetPhanTram) {
        return rows.stream()
                .filter(row -> row.getGiaTriHD() > 0)
                .filter(row -> row.getSlConHD() / row.getGiaTriHD() * 100.0 <= nguongSapHetPhanTram)
                .sorted(Comparator.comparingDouble(NguonViecRowToolItem::getSlConHD))
                .limit(10)
                .toList();
    }

    private static RankedItemResponse sapHetItem(NguonViecRowToolItem row) {
        return RankedItemResponse.builder()
                .label((row.getMaHD() != null ? row.getMaHD() : "—")
                        + (row.getChuongTrinh() != null ? " - " + row.getChuongTrinh() : ""))
                .value(Math.round(row.getSlConHD() * 100) / 100.0)
                .extra(Map.of(
                        "giaTriHD", row.getGiaTriHD(),
                        "trungTam", row.getTrungTam() != null ? row.getTrungTam() : "—"))
                .build();
    }

    private static NguonViecRowToolItem toDisplayItem(NguonViecRowToolItem row) {
        return NguonViecRowToolItem.builder()
                .id(row.getId())
                .stt(row.getStt())
                .chuongTrinh(row.getChuongTrinh())
                .maHD(row.getMaHD())
                .linhVuc(StatusLabels.nguonViecLinhVuc(row.getLinhVuc()))
                .loaiCV(StatusLabels.nguonViecLoaiCv(row.getLoaiCV()))
                .nhom(row.getNhom())
                .phapLyStage(StatusLabels.nguonViecPhapLyStage(row.getPhapLyStage()))
                .giaTriHD(row.getGiaTriHD())
                .sxT1(row.getSxT1())
                .sxT2(row.getSxT2())
                .sxTong(row.getSxTong())
                .dtT1(row.getDtT1())
                .dtT2(row.getDtT2())
                .dtTong(row.getDtTong())
                .slConHD(row.getSlConHD())
                .dtConSL(row.getDtConSL())
                .chuaKhaThi(row.getChuaKhaThi())
                .slHuy(row.getSlHuy())
                .slVuong(row.getSlVuong())
                .status(StatusLabels.nguonViecStatus(row.getStatus()))
                .nhaThau(row.getNhaThau())
                .ghiChuKV(row.getGhiChuKV())
                .trungTam(row.getTrungTam())
                .ngayKyHD(row.getNgayKyHD())
                .ngayKetThuc(row.getNgayKetThuc())
                .ngayCapNhat(row.getNgayCapNhat())
                .soDoiKS(row.getSoDoiKS())
                .soDoiTC(row.getSoDoiTC())
                .manual(row.isManual())
                .build();
    }
}