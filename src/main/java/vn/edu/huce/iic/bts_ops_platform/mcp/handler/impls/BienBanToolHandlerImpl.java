package vn.edu.huce.iic.bts_ops_platform.mcp.handler.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.bienban.BienBanQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.bienban.BienBanTheoTrangThaiToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.bienban.BienBanThieuKhaoSatToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.bienban.BienBanThieuTheoTienDoToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.bienban.BienBanTongQuanToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.bienban.BienBanTheoTrangThaiRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.bienban.DemNhomRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.khuvuc.KhuVucInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhathau.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.handler.BienBanToolHandler;
import vn.edu.huce.iic.bts_ops_platform.mcp.repository.BienBanToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.DoiTuongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.KhuVucComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.TinhComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PagedResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Impl thật cho tool AI module Biên bản — 1 method duy nhất, chỉ gọi BienBanToolRepository. */
@Component
@RequiredArgsConstructor
public class BienBanToolHandlerImpl implements BienBanToolHandler {

    private final BienBanToolRepository bienBanToolRepository;
    private final HopDongComponent hopDongComponent;
    private final KhuVucComponent khuVucComponent;
    private final DoiTuongComponent doiTuongComponent;
    private final NhaThauComponent nhaThauComponent;
    private final TinhComponent tinhComponent;
    private final McpParallel parallel;

    @Override
    public BienBanQueryResponse query(String khuVuc, String tinhThanh, String hopDong, String nhaThau, String trangThai,
                                      String doiTuong, LocalDate fromDate, LocalDate toDate,
                                      Integer page, Integer pageSize) {
        var f_resolvedNhaThau = parallel.async(() -> nhaThauComponent.resolve(nhaThau));
        var f_resolvedHopDong = parallel.async(() -> hopDongComponent.resolve(hopDong));
        var f_tinhThanhId = parallel.async(() -> tinhComponent.resolveId(tinhThanh));
        var f_resolvedDoiTuong = parallel.async(() -> doiTuongComponent.resolve(doiTuong));
        KhuVucInfo resolvedKhuVuc = khuVucComponent.resolveInScope(khuVuc);
        // các tra cứu id chạy song song (mỗi tra cứu 1-2 lần khứ hồi tới DB nếu chưa có trong bộ nhớ đệm)
        NhaThauInfo resolvedNhaThau = McpParallel.get(f_resolvedNhaThau);
        HopDongInfo resolvedHopDong = McpParallel.get(f_resolvedHopDong);
        UUID tinhThanhId = McpParallel.get(f_tinhThanhId);
        UUID doiTuongId = McpParallel.get(f_resolvedDoiTuong).id();
        final LocalDate from = fromDate != null && toDate != null && fromDate.isAfter(toDate) ? toDate : fromDate;
        final LocalDate to = fromDate != null && toDate != null && fromDate.isAfter(toDate) ? fromDate : toDate;

        UUID khuVucId = resolvedKhuVuc.id();
        UUID hopDongId = resolvedHopDong.id();
        UUID nhaThauId = resolvedNhaThau.id();
        var pageable = PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE);

        // 4 khối độc lập chạy song song: độ trễ = khối chậm nhất thay vì tổng
        var fTongQuan = parallel.async(() -> computeTongQuan(khuVucId, tinhThanhId, hopDongId, nhaThauId, doiTuongId, from, to));
        var fThieuKhaoSat = parallel.async(() -> {
            Page<Object[]> thieuKhaoSatPage = bienBanToolRepository.findTramThieuKhaoSat(null, khuVucId, tinhThanhId, nhaThauId, pageable);
            return PagedResult.of(thieuKhaoSatPage.getContent().stream()
                    .map(BienBanToolHandlerImpl::mapThieuKhaoSat)
                    .toList(), thieuKhaoSatPage.getNumber(), thieuKhaoSatPage.getSize(), thieuKhaoSatPage.getTotalElements());
        });
        var fThieuHoSo = parallel.async(() -> computeThieuHoSo(hopDongId, nhaThauId, page, pageSize));
        var fTheoTrangThai = parallel.async(() -> computeTheoTrangThai(
                normalizeTrangThai(trangThai), khuVucId, tinhThanhId, hopDongId, nhaThauId, doiTuongId, from, to, page, pageSize));

        BienBanTongQuanToolItem tongQuan = McpParallel.get(fTongQuan);
        PagedResult<BienBanThieuKhaoSatToolItem> thieuKhaoSat = McpParallel.get(fThieuKhaoSat);
        PagedResult<BienBanThieuTheoTienDoToolItem> thieuHoSo = McpParallel.get(fThieuHoSo);
        PagedResult<BienBanTheoTrangThaiToolItem> theoTrangThai = McpParallel.get(fTheoTrangThai);

        return BienBanQueryResponse.builder()
                .tongQuan(tongQuan)
                .thieuKhaoSat(thieuKhaoSat)
                .thieuHoSo(thieuHoSo)
                .theoTrangThai(theoTrangThai)
                .build();
    }

    private static BienBanThieuKhaoSatToolItem mapThieuKhaoSat(Object[] row) {
        BienBanThieuKhaoSatToolItem item = new BienBanThieuKhaoSatToolItem();
        item.setMaTram((String) row[0]);
        item.setKhuVuc((String) row[1]);
        item.setMaHopDong((String) row[2]);
        return item;
    }

    /** Chuẩn hóa các cách LLM hay truyền về đúng giá trị cột trang_thai. */
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
        // 2 câu cho cả trang thay vì 2 câu / hợp đồng
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
