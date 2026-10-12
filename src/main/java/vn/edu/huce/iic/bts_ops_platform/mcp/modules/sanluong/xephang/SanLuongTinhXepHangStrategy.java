package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang;

import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.ProgressGroupProjection;

import java.util.UUID;

/** Xếp hạng sản lượng theo tỉnh/thành. */
@Component
public class SanLuongTinhXepHangStrategy extends SanLuongNhomXepHangStrategy {

    /** Đối tượng chưa gán tỉnh: gom thành 1 dòng để tổng khớp. */
    public static final UUID CHUA_GAN_TINH = new UUID(0L, 0L);

    @Override
    public SanLuongXepHangChieu chieu() {
        return SanLuongXepHangChieu.TINH;
    }

    @Override
    protected UUID khoa(ProgressGroupProjection g) {
        return g.getTinhId() != null ? g.getTinhId() : CHUA_GAN_TINH;
    }

    @Override
    protected String ten(ProgressGroupProjection g) {
        return g.getTinhTen();
    }
}
