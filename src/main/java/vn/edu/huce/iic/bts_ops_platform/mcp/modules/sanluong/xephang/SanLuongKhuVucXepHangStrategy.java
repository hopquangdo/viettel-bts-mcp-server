package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang;

import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.ProgressGroupProjection;

import java.util.UUID;

/** Xếp hạng sản lượng theo khu vực. */
@Component
public class SanLuongKhuVucXepHangStrategy extends SanLuongNhomXepHangStrategy {

    @Override
    public SanLuongXepHangChieu chieu() {
        return SanLuongXepHangChieu.KHU_VUC;
    }

    @Override
    protected UUID khoa(ProgressGroupProjection g) {
        return g.getKhuVucId();
    }

    @Override
    protected String ten(ProgressGroupProjection g) {
        return g.getKhuVucTen();
    }
}
