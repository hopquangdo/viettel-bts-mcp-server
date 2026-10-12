package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.RankedItemDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.VuongMacToolRepository;

import java.util.List;

/** Top hợp đồng có nhiều vướng mắc đang mở nhất. */
@Component
@RequiredArgsConstructor
public class VuongMacHopDongXepHangStrategy implements VuongMacXepHangStrategy {

    private final VuongMacToolRepository vuongMacToolRepository;

    @Override
    public VuongMacXepHangChieu chieu() {
        return VuongMacXepHangChieu.HOP_DONG;
    }

    @Override
    public String lyDoBoQua(VuongMacXepHangContext ctx) {
        return ctx.hopDongId() != null
                ? "Đang lọc theo 1 hợp đồng cụ thể nên bảng xếp hạng theo hợp đồng chỉ có 1 dòng, đã bỏ qua."
                : null;
    }

    @Override
    public List<RankedItemDto> rank(VuongMacXepHangContext ctx) {
        return vuongMacToolRepository.rankHopDongByOpenCount(PageRequest.of(0, ctx.top()), ctx.hopDongId(), ctx.nhaThauId(),
                        ctx.tinhThanhId(), ctx.doiTuongId(), ctx.khuVucId(), ctx.giaiDoanLoc(), ctx.kieuLoc(), ctx.loaiId(),
                        ctx.canBoId(), ctx.ngayTaoFrom(), ctx.ngayTaoTo()).stream()
                .map(row -> RankedItemDto.builder().label(row.getLabel()).name(row.getName()).value(row.getValue()).build())
                .toList();
    }
}
