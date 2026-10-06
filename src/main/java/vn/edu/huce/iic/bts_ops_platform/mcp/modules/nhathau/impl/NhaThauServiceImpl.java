package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.impl;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.KhuVucComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.TinhComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;

import java.util.List;

/** Orchestrates nhà thầu search and assembles the paged tool response. */
@Service
@RequiredArgsConstructor
public class NhaThauServiceImpl implements NhaThauService {

    private final NhaThauRepository nhaThauRepository;
    private final HopDongComponent hopDongComponent;
    private final KhuVucComponent khuVucComponent;
    private final TinhComponent tinhComponent;
    private final McpParallel parallel;

    public NhaThauQueryResponse query(String query, String hopDong, String khuVuc, String tinhThanh,
                                     Integer page, Integer pageSize) {
        var f_resolvedHopDong = parallel.async(() -> hopDongComponent.resolve(hopDong));
        var f_tinhThanhId = parallel.async(() -> tinhComponent.resolveId(tinhThanh));
        var resolvedKhuVuc = khuVucComponent.resolveInScope(khuVuc);
        // Các tra cứu id chạy song song (mỗi tra cứu 1-2 lần khứ hồi tới DB nếu chưa có trong bộ nhớ đệm).
        var resolvedHopDong = McpParallel.get(f_resolvedHopDong);
        var tinhThanhId = McpParallel.get(f_tinhThanhId);

        Page<NhaThauRepository.NhaThauRow> result = nhaThauRepository.danhSachNhaThau(
                ResolveSupport.likePattern(query), resolvedHopDong.id(), resolvedKhuVuc.id(), tinhThanhId,
                PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE));

        List<NhaThauItem> items = result.getContent().stream()
                .map(row -> NhaThauItem.builder()
                        .nhaThau(NhaThauInfo.of(row.getId(), row.getHoTen()))
                        .tenDangNhap(row.getTenDangNhap())
                        .soHopDong(row.getSoHopDong() != null ? row.getSoHopDong() : 0)
                        .soDoiTuong(row.getSoDoiTuong() != null ? row.getSoDoiTuong() : 0)
                        .build())
                .toList();
        return NhaThauQueryResponse.builder()
                .tongSo(result.getTotalElements())
                .danhSach(PagedResult.of(items, result.getNumber(), result.getSize(), result.getTotalElements()))
                .build();
    }
}