package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang;

import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpStrategyRegistries;

import java.util.List;
import java.util.Map;

/** Tra strategy theo chiều; khởi động lỗi ngay nếu thiếu hoặc trùng strategy cho 1 chiều. */
@Component
public class HopDongXepHangRegistry {

    private final Map<HopDongXepHangChieu, HopDongXepHangStrategy> theoChieu;

    public HopDongXepHangRegistry(List<HopDongXepHangStrategy> strategies) {
        this.theoChieu = McpStrategyRegistries.gom(HopDongXepHangChieu.class, strategies, HopDongXepHangStrategy::chieu);
    }

    public HopDongXepHangStrategy get(HopDongXepHangChieu chieu) {
        return theoChieu.get(chieu);
    }

    public Map<HopDongXepHangChieu, HopDongXepHangStrategy> all() {
        return theoChieu;
    }
}
