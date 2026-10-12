package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang;

import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpStrategyRegistries;

import java.util.List;
import java.util.Map;

/** Tra strategy theo chiều; khởi động lỗi ngay nếu thiếu hoặc trùng strategy cho 1 chiều. */
@Component
public class NguonViecXepHangRegistry {

    private final Map<NguonViecXepHangChieu, NguonViecXepHangStrategy> theoChieu;

    public NguonViecXepHangRegistry(List<NguonViecXepHangStrategy> strategies) {
        this.theoChieu = McpStrategyRegistries.gom(NguonViecXepHangChieu.class, strategies, NguonViecXepHangStrategy::chieu);
    }

    public NguonViecXepHangStrategy get(NguonViecXepHangChieu chieu) {
        return theoChieu.get(chieu);
    }

    public Map<NguonViecXepHangChieu, NguonViecXepHangStrategy> all() {
        return theoChieu;
    }
}
