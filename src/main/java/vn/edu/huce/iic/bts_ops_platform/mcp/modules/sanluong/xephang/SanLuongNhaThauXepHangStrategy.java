package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang;

import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.ProgressGroupProjection;

import java.util.UUID;

/** Xếp hạng sản lượng theo nhà thầu. */
@Component
public class SanLuongNhaThauXepHangStrategy extends SanLuongNhomXepHangStrategy {

    @Override
    public SanLuongXepHangChieu chieu() {
        return SanLuongXepHangChieu.NHA_THAU;
    }

    @Override
    protected UUID khoa(ProgressGroupProjection g) {
        return g.getNhaThauId();
    }

    @Override
    protected String ten(ProgressGroupProjection g) {
        return g.getNhaThauTen();
    }
}
