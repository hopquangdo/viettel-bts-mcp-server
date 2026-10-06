package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang;

import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankedItemResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecRowToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.NguonViecCalc;

import java.util.Comparator;
import java.util.List;

/** Top hợp đồng rủi ro cao nhất (điểm rủi ro {@link NguonViecCalc#riskScore}). */
@Component
public class NguonViecRuiRoXepHangStrategy implements NguonViecXepHangStrategy {

    @Override
    public NguonViecXepHangChieu chieu() {
        return NguonViecXepHangChieu.RUI_RO;
    }

    @Override
    public String tieuChi() {
        return "Điểm rủi ro hợp đồng (vướng mắc, phần chưa thực hiện, vướng pháp lý), cao xuống thấp";
    }

    @Override
    public List<RankedItemResponse> rank(List<NguonViecRowToolItem> rows, int top) {
        return rows.stream()
                .sorted(Comparator.comparingDouble(NguonViecCalc::riskScore).reversed())
                .limit(top)
                .map(row -> RankedItemResponse.builder()
                        .label((row.getMaHD() != null ? row.getMaHD() : "—")
                                + (row.getChuongTrinh() != null ? " - " + row.getChuongTrinh() : ""))
                        .value(Math.round(NguonViecCalc.riskScore(row) * 10) / 10.0)
                        .build())
                .toList();
    }
}
