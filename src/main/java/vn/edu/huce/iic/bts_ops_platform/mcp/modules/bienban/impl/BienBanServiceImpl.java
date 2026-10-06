package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.impl;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.DoiTuongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.KhuVucComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.TinhComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.KhuVucInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.BienBanToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Owns Biên bản tool orchestration, filter resolution, and response mapping. */
@Service
@RequiredArgsConstructor
public class BienBanServiceImpl implements BienBanService {

        private final BienBanToolRepository bienBanToolRepository;
        private final HopDongComponent hopDongComponent;
        private final KhuVucComponent khuVucComponent;
        private final DoiTuongComponent doiTuongComponent;
        private final NhaThauComponent nhaThauComponent;
        private final TinhComponent tinhComponent;
        private final McpParallel parallel;

        /** Bộ lọc đã quy đổi. from/to đã đảo nếu nhập ngược. */
        private record Scope(UUID khuVucId, UUID tinhThanhId, UUID hopDongId, UUID nhaThauId, UUID doiTuongId,
                                                 LocalDate from, LocalDate to) {
        }

        public BienBanTongQuanToolResponse tongQuan(BienBanFilter filter) {
                return BienBanTongQuanToolResponse.of(McpParallel.get(tongQuanAsync(resolve(filter))));
        }

        public BienBanThieuKhaoSatResponse thieuKhaoSat(BienBanFilter filter, Integer page, Integer pageSize) {
                return new BienBanThieuKhaoSatResponse(McpParallel.get(thieuKhaoSatAsync(resolve(filter), pageOf(page, pageSize))));
        }

        public BienBanThieuHoSoResponse thieuHoSo(BienBanFilter filter, Integer page, Integer pageSize) {
                return new BienBanThieuHoSoResponse(McpParallel.get(thieuHoSoAsync(resolve(filter), page, pageSize)));
        }

        public BienBanTheoTrangThaiResponse theoTrangThai(BienBanFilter filter, String trangThai, Integer page, Integer pageSize) {
                return new BienBanTheoTrangThaiResponse(McpParallel.get(theoTrangThaiAsync(resolve(filter), trangThai, page, pageSize)));
        }

        /** Full grouped response retained for downstream legacy callers. */
        public BienBanQueryResponse query(String khuVuc, String tinhThanh, String hopDong, String nhaThau, String trangThai,
                                                                          String doiTuong, LocalDate fromDate, LocalDate toDate,
                                                                          Integer page, Integer pageSize) {
                Scope s = resolve(new BienBanFilter(khuVuc, tinhThanh, hopDong, nhaThau, doiTuong, fromDate, toDate));
                var fTongQuan = tongQuanAsync(s);
                var fThieuKhaoSat = thieuKhaoSatAsync(s, pageOf(page, pageSize));
                var fThieuHoSo = thieuHoSoAsync(s, page, pageSize);
                var fTheoTrangThai = theoTrangThaiAsync(s, trangThai, page, pageSize);

                return BienBanQueryResponse.builder()
                                .tongQuan(McpParallel.get(fTongQuan))
                                .thieuKhaoSat(McpParallel.get(fThieuKhaoSat))
                                .thieuHoSo(McpParallel.get(fThieuHoSo))
                                .theoTrangThai(McpParallel.get(fTheoTrangThai))
                                .build();
        }

        private Scope resolve(BienBanFilter f) {
                var fNhaThau = parallel.async(() -> nhaThauComponent.resolve(f.nhaThau()));
                var fHopDong = parallel.async(() -> hopDongComponent.resolve(f.hopDong()));
                var fTinhThanhId = parallel.async(() -> tinhComponent.resolveId(f.tinhThanh()));
                var fDoiTuong = parallel.async(() -> doiTuongComponent.resolve(f.doiTuong()));
                KhuVucInfo khuVuc = khuVucComponent.resolveInScope(f.khuVuc());
                NhaThauInfo nhaThau = McpParallel.get(fNhaThau);
                HopDongInfo hopDong = McpParallel.get(fHopDong);

                LocalDate fromDate = f.fromDate();
                LocalDate toDate = f.toDate();
                boolean nguoc = fromDate != null && toDate != null && fromDate.isAfter(toDate);
                return new Scope(khuVuc.id(), McpParallel.get(fTinhThanhId), hopDong.id(), nhaThau.id(),
                                McpParallel.get(fDoiTuong).id(), nguoc ? toDate : fromDate, nguoc ? fromDate : toDate);
        }

        private static PageRequest pageOf(Integer page, Integer pageSize) {
                return PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE);
        }

        private CompletableFuture<BienBanTongQuanToolItem> tongQuanAsync(Scope s) {
                return parallel.async(() -> computeTongQuan(s.khuVucId(), s.tinhThanhId(), s.hopDongId(), s.nhaThauId(), s.doiTuongId(),
                                s.from(), s.to()));
        }

        private CompletableFuture<PagedResult<BienBanThieuKhaoSatToolItem>> thieuKhaoSatAsync(Scope s, PageRequest pageable) {
                return parallel.async(() -> {
                        Page<Object[]> thieuKhaoSatPage = bienBanToolRepository.findTramThieuKhaoSat(null, s.khuVucId(), s.tinhThanhId(),
                                        s.nhaThauId(), pageable);
                        return PagedResult.of(thieuKhaoSatPage.getContent().stream()
                                        .map(BienBanServiceImpl::mapThieuKhaoSat)
                                        .toList(), thieuKhaoSatPage.getNumber(), thieuKhaoSatPage.getSize(), thieuKhaoSatPage.getTotalElements());
                });
        }

        private CompletableFuture<PagedResult<BienBanThieuTheoTienDoToolItem>> thieuHoSoAsync(Scope s, Integer page, Integer pageSize) {
                return parallel.async(() -> computeThieuHoSo(s.hopDongId(), s.nhaThauId(), page, pageSize));
        }

        private CompletableFuture<PagedResult<BienBanTheoTrangThaiToolItem>> theoTrangThaiAsync(Scope s, String trangThai,
                                                                                                                                                                                        Integer page, Integer pageSize) {
                String trangThaiLoc = normalizeTrangThai(trangThai);
                return parallel.async(() -> computeTheoTrangThai(trangThaiLoc, s.khuVucId(), s.tinhThanhId(), s.hopDongId(), s.nhaThauId(),
                                s.doiTuongId(), s.from(), s.to(), page, pageSize));
        }

        private static BienBanThieuKhaoSatToolItem mapThieuKhaoSat(Object[] row) {
                BienBanThieuKhaoSatToolItem item = new BienBanThieuKhaoSatToolItem();
                item.setMaTram((String) row[0]);
                item.setKhuVuc((String) row[1]);
                item.setMaHopDong((String) row[2]);
                return item;
        }

        private String normalizeTrangThai(String raw) {
                if (raw == null || raw.isBlank()) {
                        return null;
                }
                String s = raw.trim().toLowerCase().replace(' ', '_');
                return switch (s) {
                        case "cho_duyet", "choduyet", "pending", "cho_phe_duyet" -> "cho_duyet";
                        case "da_duyet", "daduyet", "approved", "da_phe_duyet" -> "da_duyet";
                        case "tu_choi", "tuchoi", "rejected", "bi_tu_choi" -> "tu_choi";
                        default -> s;
                };
        }

        private BienBanTongQuanToolItem computeTongQuan(UUID khuVucId, UUID tinhThanhId, UUID hopDongId, UUID nhaThauId,
                                                                                                         UUID doiTuongId, LocalDate from, LocalDate to) {
                long choDuyet = 0;
                long daDuyet = 0;
                long tuChoi = 0;
                long total = 0;
                for (DemNhomRow row : bienBanToolRepository.countGroupByTrangThai(khuVucId, tinhThanhId, hopDongId, nhaThauId, doiTuongId, from, to)) {
                        long count = row.getSoLuong();
                        total += count;
                        switch (row.getNhom() == null ? "" : row.getNhom()) {
                                case "cho_duyet" -> choDuyet = count;
                                case "da_duyet" -> daDuyet = count;
                                case "tu_choi" -> tuChoi = count;
                                default -> { }
                        }
                }

                Map<String, Long> countsByLoai = new LinkedHashMap<>();
                for (String type : List.of("BIEN_BAN_SO_1", "NHAT_KY_THI_CONG", "BIEN_BAN_SO_2",
                                "BAO_CAO_KHAO_SAT", "BAN_GIAO_MAT_BANG", "YEU_CAU_VAT_TU", "NHAN_VAT_TU")) {
                        countsByLoai.put(StatusLabels.loaiBienBan(type), 0L);
                }
                for (DemNhomRow row : bienBanToolRepository.countGroupByLoai(khuVucId, tinhThanhId, hopDongId, nhaThauId, doiTuongId, from, to)) {
                        countsByLoai.put(StatusLabels.loaiBienBan(row.getNhom()), row.getSoLuong());
                }

                return BienBanTongQuanToolItem.builder()
                                .total(total)
                                .choDuyet(choDuyet)
                                .daDuyet(daDuyet)
                                .tuChoi(tuChoi)
                                .countsByLoai(countsByLoai)
                                .build();
        }

        private PagedResult<BienBanTheoTrangThaiToolItem> computeTheoTrangThai(String trangThai, UUID khuVucId, UUID tinhThanhId,
                                                                                                                                                        UUID hopDongId, UUID nhaThauId,
                                                                                                                                                        UUID doiTuongId, LocalDate from, LocalDate to,
                                                                                                                                                        Integer page, Integer pageSize) {
                Page<BienBanTheoTrangThaiRow> resultPage = bienBanToolRepository.findTheoTrangThai(trangThai, khuVucId, tinhThanhId, hopDongId,
                                nhaThauId, doiTuongId, from, to, PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE));
                List<BienBanTheoTrangThaiToolItem> result = new ArrayList<>();
                for (BienBanTheoTrangThaiRow row : resultPage) {
                        BienBanTheoTrangThaiToolItem item = new BienBanTheoTrangThaiToolItem();
                        item.setMaBienBan(row.getMaBienBan());
                        item.setLoaiBienBan(StatusLabels.loaiBienBan(row.getLoaiBienBan()));
                        item.setHopDong(HopDongInfo.of(null, row.getMaHopDong(), row.getTenHopDong()));
                        item.setTrangThai(StatusLabels.bienBan(row.getTrangThai()));
                        item.setNgayLap(row.getNgayLap());
                        item.setNguoiLapTen(row.getNguoiLapTen());
                        item.setLyDoTuChoi(row.getLyDoTuChoi());
                        item.setNguoiPheDuyetTen(row.getNguoiPheDuyetTen());
                        item.setNgayPheDuyet(row.getNgayPheDuyet());
                        result.add(item);
                }
                return PagedResult.of(result, resultPage.getNumber(), resultPage.getSize(), resultPage.getTotalElements());
        }

        private PagedResult<BienBanThieuTheoTienDoToolItem> computeThieuHoSo(UUID hopDongId, UUID nhaThauId,
                                                                                                                                                   Integer page, Integer pageSize) {
                List<BienBanThieuTheoTienDoToolItem> result = new ArrayList<>();
                Page<Object[]> hopDongPage = bienBanToolRepository.findHopDongForChecklist(hopDongId, nhaThauId,
                                PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE));
                List<UUID> hopDongIds = new ArrayList<>();
                for (Object[] hopDongRow : hopDongPage) {
                        hopDongIds.add((UUID) hopDongRow[0]);
                }
                Map<UUID, List<Object[]>> checklistTheoHopDong = new HashMap<>();
                Map<UUID, Set<String>> daDuyetTheoHopDong = new HashMap<>();
                if (!hopDongIds.isEmpty()) {
                        for (Object[] row : bienBanToolRepository.checklistForHopDongs(hopDongIds)) {
                                checklistTheoHopDong.computeIfAbsent((UUID) row[0], k -> new ArrayList<>()).add(row);
                        }
                        for (Object[] row : bienBanToolRepository.loaiBienBanDaDuyetTheoHopDong(hopDongIds)) {
                                daDuyetTheoHopDong.computeIfAbsent((UUID) row[0], k -> new HashSet<>()).add((String) row[1]);
                        }
                }
                for (Object[] hopDongRow : hopDongPage) {
                        UUID hopDongRowId = (UUID) hopDongRow[0];
                        String ma = (String) hopDongRow[1];
                        String ten = (String) hopDongRow[2];

                        List<Object[]> checklist = checklistTheoHopDong.getOrDefault(hopDongRowId, List.of());
                        if (checklist.isEmpty()) {
                                continue;
                        }
                        Set<String> daDuyet = daDuyetTheoHopDong.getOrDefault(hopDongRowId, Set.of());

                        List<String> thieu = checklist.stream()
                                        .filter(row -> !daDuyet.contains((String) row[1]))
                                        .map(row -> (String) row[2])
                                        .toList();

                        if (!thieu.isEmpty()) {
                                BienBanThieuTheoTienDoToolItem item = new BienBanThieuTheoTienDoToolItem();
                                item.setMaHopDong(ma);
                                item.setTen(ten);
                                item.setThieuBienBan(thieu);
                                result.add(item);
                        }
                }
                return PagedResult.of(result, hopDongPage.getNumber(), hopDongPage.getSize(), hopDongPage.getTotalElements());
        }
}