package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.RankedItemDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.VuongMacToolRepository;

import java.util.List;

/** Top khu vực có nhiều vướng mắc đang mở nhất. */
@Component
@RequiredArgsConstructor
public class VuongMacKhuVucXepHangStrategy implements VuongMacXepHangStrategy {

    private final VuongMacToolRepository vuongMacToolRepository;

    @Override
    public VuongMacXepHangChieu chieu() {
        return VuongMacXepHangChieu.KHU_VUC;
    }

    @Override
    public String lyDoBoQua(VuongMacXepHangContext ctx) {
        return ctx.khuVucId() != null
                ? "Đang lọc theo 1 khu vực cụ thể nên bảng xếp hạng theo khu vực chỉ có 1 dòng, đã bỏ qua."
                : null;
    }

    @Override
    public List<RankedItemDto> rank(VuongMacXepHangContext ctx) {
        return vuongMacToolRepository.rankKhuVucByOpenCount(PageRequest.of(0, ctx.top()), ctx.hopDongId(), ctx.nhaThauId(),
                        ctx.tinhThanhId(), ctx.doiTuongId(), ctx.giaiDoanLoc(), ctx.kieuLoc(), ctx.loaiId(), ctx.canBoId(),
                        ctx.ngayTaoFrom(), ctx.ngayTaoTo()).stream()
                .map(row -> RankedItemDto.builder().label(row.getLabel()).value(row.getValue()).build())
                .toList();
    }
}
