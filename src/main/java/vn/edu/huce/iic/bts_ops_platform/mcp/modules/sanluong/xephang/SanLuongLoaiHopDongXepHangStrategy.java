package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang;

import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.ProgressGroupProjection;

import java.util.UUID;

/** Xếp hạng sản lượng theo loại hợp đồng. */
@Component
public class SanLuongLoaiHopDongXepHangStrategy extends SanLuongNhomXepHangStrategy {

    @Override
    public SanLuongXepHangChieu chieu() {
        return SanLuongXepHangChieu.LOAI_HOP_DONG;
    }

    @Override
    protected UUID khoa(ProgressGroupProjection g) {
        return g.getLoaiHopDongId();
    }

    @Override
    protected String ten(ProgressGroupProjection g) {
        return g.getLoaiHopDongTen();
    }
}
