package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang;

import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.ProgressGroupProjection;

import java.util.UUID;

/** Xếp hạng sản lượng theo hợp đồng (tối đa 20 dòng). */
@Component
public class SanLuongHopDongXepHangStrategy extends SanLuongNhomXepHangStrategy {

    @Override
    public SanLuongXepHangChieu chieu() {
        return SanLuongXepHangChieu.HOP_DONG;
    }

    @Override
    protected UUID khoa(ProgressGroupProjection g) {
        return g.getHopDongId();
    }

    @Override
    protected String ten(ProgressGroupProjection g) {
        return g.getHopDongTen();
    }

    @Override
    protected int topLimit() {
        return 20;
    }
}
