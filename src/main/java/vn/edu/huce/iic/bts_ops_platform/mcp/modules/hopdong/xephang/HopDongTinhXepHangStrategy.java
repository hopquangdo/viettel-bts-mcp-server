package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.HopDongToolRepository;

import java.util.List;

/** Xếp hạng tỉnh trong phạm vi 1 hợp đồng theo số đối tượng (kèm số hoàn thành, số vướng mắc). Cần lọc 1 hợp đồng. */
@Component
@RequiredArgsConstructor
public class HopDongTinhXepHangStrategy implements HopDongXepHangStrategy {

    private final HopDongToolRepository hopDongToolRepository;

    @Override
    public HopDongXepHangChieu chieu() {
        return HopDongXepHangChieu.TINH;
    }

    @Override
    public String tieuChi() {
        return "Số đối tượng trong hợp đồng, nhiều xuống ít";
    }

    @Override
    public String lyDoBoQua(HopDongXepHangContext ctx) {
        if (ctx.hopDongId() == null) {
            return "Xếp hạng theo tỉnh/thành chỉ tính trong phạm vi 1 hợp đồng; hãy truyền thêm tham số hopDong.";
        }
        if (ctx.tinhThanhId() != null) {
            return "Đang lọc theo 1 tỉnh/thành cụ thể nên bảng xếp hạng theo tỉnh/thành chỉ có 1 dòng, đã bỏ qua.";
        }
        return null;
    }

    @Override
    public List<HopDongXepHangDong> rank(HopDongXepHangContext ctx) {
        return hopDongToolRepository.rankTinhTheoHopDong(ctx.hopDongId()).stream()
                .limit(ctx.top())
                .map(r -> new HopDongXepHangDong(r.getTinh(), null, nz(r.getSoDoiTuong()), nz(r.getSoHoanThanh()), nz(r.getSoVuongMac())))
                .toList();
    }

    private static long nz(Long v) {
        return v != null ? v : 0L;
    }
}
