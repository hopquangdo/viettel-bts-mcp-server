package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang;

import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankedItemResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecRowToolItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Xếp hạng mọi trung tâm theo sản lượng thực hiện (sxTong, tỷ đồng); extra: giaTriHD, soHopDong. Không cắt top. */
@Component
public class NguonViecTrungTamXepHangStrategy implements NguonViecXepHangStrategy {

    @Override
    public NguonViecXepHangChieu chieu() {
        return NguonViecXepHangChieu.TRUNG_TAM;
    }

    @Override
    public String tieuChi() {
        return "Sản lượng thực hiện, cao xuống thấp";
    }

    @Override
    public List<RankedItemResponse> rank(List<NguonViecRowToolItem> rows, int top) {
        record Acc(double[] sxTong, double[] giaTriHD, int[] soHopDong) {
        }
        Map<String, Acc> byTrungTam = new LinkedHashMap<>();
        for (NguonViecRowToolItem row : rows) {
            String trungTam = row.getTrungTam() != null && !row.getTrungTam().isBlank() ? row.getTrungTam() : "—";
            Acc acc = byTrungTam.computeIfAbsent(trungTam, k -> new Acc(new double[]{0}, new double[]{0}, new int[]{0}));
            acc.sxTong()[0] += row.getSxTong();
            acc.giaTriHD()[0] += row.getGiaTriHD();
            acc.soHopDong()[0]++;
        }
        return byTrungTam.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue().sxTong()[0], a.getValue().sxTong()[0]))
                .map(entry -> RankedItemResponse.builder()
                        .label(entry.getKey())
                        .value(Math.round(entry.getValue().sxTong()[0] * 100) / 100.0)
                        .extra(Map.of(
                                "giaTriHD", Math.round(entry.getValue().giaTriHD()[0] * 100) / 100.0,
                                "soHopDong", entry.getValue().soHopDong()[0]))
                        .build())
                .toList();
    }
}
