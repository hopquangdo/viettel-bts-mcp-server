package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang;

import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpStrategyRegistries;

import java.util.List;
import java.util.Map;

/** Tra strategy theo chiều; khởi động lỗi ngay nếu thiếu hoặc trùng strategy cho 1 chiều. */
@Component
public class SanLuongXepHangRegistry {

    private final Map<SanLuongXepHangChieu, SanLuongXepHangStrategy> theoChieu;

    public SanLuongXepHangRegistry(List<SanLuongXepHangStrategy> strategies) {
        this.theoChieu = McpStrategyRegistries.gom(SanLuongXepHangChieu.class, strategies, SanLuongXepHangStrategy::chieu);
    }

    public SanLuongXepHangStrategy get(SanLuongXepHangChieu chieu) {
        return theoChieu.get(chieu);
    }

    /** Mọi strategy theo thứ tự khai báo của enum (cho đường tổng quan). */
    public Map<SanLuongXepHangChieu, SanLuongXepHangStrategy> all() {
        return theoChieu;
    }
}
