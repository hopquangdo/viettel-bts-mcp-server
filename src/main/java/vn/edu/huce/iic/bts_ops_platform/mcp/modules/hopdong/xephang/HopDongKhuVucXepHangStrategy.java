package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.HopDongToolRepository;

import java.util.List;

/** Xếp hạng khu vực theo số hợp đồng (kèm số đối tượng, số vướng mắc). */
@Component
@RequiredArgsConstructor
public class HopDongKhuVucXepHangStrategy implements HopDongXepHangStrategy {

    private final HopDongToolRepository hopDongToolRepository;

    @Override
    public HopDongXepHangChieu chieu() {
        return HopDongXepHangChieu.KHU_VUC;
    }

    @Override
    public String tieuChi() {
        return "Số hợp đồng, nhiều xuống ít";
    }

    @Override
    public String lyDoBoQua(HopDongXepHangContext ctx) {
        return ctx.khuVucId() != null
                ? "Đang lọc theo 1 khu vực cụ thể nên bảng xếp hạng theo khu vực chỉ có 1 dòng, đã bỏ qua."
                : null;
    }

    @Override
    public List<HopDongXepHangDong> rank(HopDongXepHangContext ctx) {
        return hopDongToolRepository.rankKhuVuc(ctx.top(), ctx.hopDongId(), ctx.tinhThanhId(), ctx.nhaThauId(), ctx.doiTuongId(),
                        ctx.loaiId(), ctx.kieuId()).stream()
                .map(r -> new HopDongXepHangDong(r.getKhuVuc(), nz(r.getSoHopDong()), nz(r.getSoDoiTuong()), null, nz(r.getSoVuongMac())))
                .toList();
    }

    private static long nz(Long v) {
        return v != null ? v : 0L;
    }
}
