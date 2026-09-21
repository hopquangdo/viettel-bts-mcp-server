package vn.edu.huce.iic.bts_ops_platform.handler.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.components.KhuVucComponent;
import vn.edu.huce.iic.bts_ops_platform.components.TinhComponent;
import vn.edu.huce.iic.bts_ops_platform.dto.common.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.dto.nhathau.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.dto.nhathau.NhaThauItem;
import vn.edu.huce.iic.bts_ops_platform.dto.nhathau.NhaThauQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.handler.NhaThauToolHandler;
import vn.edu.huce.iic.bts_ops_platform.repository.NhaThauRepository;
import vn.edu.huce.iic.bts_ops_platform.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.support.ResolveSupport;

import java.util.List;

/**
 * Impl tool Nhà thầu: chỉ trả lời "có những nhà thầu nào" (đang phụ trách ít nhất 1 đối tượng), kèm số hợp đồng và số đối tượng,
 * lọc theo từ khoá tên, hợp đồng, khu vực, tỉnh. Số liệu chi tiết của từng nhà thầu lấy ở tool của mảng tương ứng (tham số nhaThau).
 */
@Component
@RequiredArgsConstructor
public class NhaThauToolHandlerImpl implements NhaThauToolHandler {

    private final NhaThauRepository nhaThauRepository;
    private final HopDongComponent hopDongComponent;
    private final KhuVucComponent khuVucComponent;
    private final TinhComponent tinhComponent;
    private final McpParallel parallel;

    @Override
    public NhaThauQueryResponse query(String query, String hopDong, String khuVuc, String tinhThanh,
                                      Integer page, Integer pageSize) {
        var f_resolvedHopDong = parallel.async(() -> hopDongComponent.resolve(hopDong));
        var f_tinhThanhId = parallel.async(() -> tinhComponent.resolveId(tinhThanh));
        var resolvedKhuVuc = khuVucComponent.resolveInScope(khuVuc);
        // các tra cứu id chạy song song (mỗi tra cứu 1-2 lần khứ hồi tới DB nếu chưa có trong bộ nhớ đệm)
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
