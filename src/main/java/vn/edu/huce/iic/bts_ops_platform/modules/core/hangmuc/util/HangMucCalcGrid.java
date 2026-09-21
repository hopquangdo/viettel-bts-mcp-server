package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class HangMucCalcGrid {
    final List<HangMucCalcGridRow> rows = new ArrayList<>();
    final Map<Integer, HangMucCalcGridRow> byRowNumber = new HashMap<>();
    final Map<String, HangMucCalcGridRow> byMa = new HashMap<>();
    final Map<UUID, List<HangMucCalcGridRow>> childrenByItemId = new HashMap<>();

    void add(HangMucCalcGridRow row) {
        rows.add(row);
        byRowNumber.put(row.rowNumber, row);
        if (row.ma != null) {
            byMa.put(HangMucThanhTienCalculator.normalizeMa(row.ma), row);
        }
        if (row.parentItemId != null) {
            childrenByItemId.computeIfAbsent(row.parentItemId, ignored -> new ArrayList<>()).add(row);
        }
    }
}
