package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang;

import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpStrategyRegistries;

import java.util.List;
import java.util.Map;

/** Tra strategy theo chiều; khởi động lỗi ngay nếu thiếu hoặc trùng strategy cho 1 chiều. */
@Component
public class VuongMacXepHangRegistry {

    private final Map<VuongMacXepHangChieu, VuongMacXepHangStrategy> theoChieu;

    public VuongMacXepHangRegistry(List<VuongMacXepHangStrategy> strategies) {
        this.theoChieu = McpStrategyRegistries.gom(VuongMacXepHangChieu.class, strategies, VuongMacXepHangStrategy::chieu);
    }

    public VuongMacXepHangStrategy get(VuongMacXepHangChieu chieu) {
        return theoChieu.get(chieu);
    }

    public Map<VuongMacXepHangChieu, VuongMacXepHangStrategy> all() {
        return theoChieu;
    }
}
