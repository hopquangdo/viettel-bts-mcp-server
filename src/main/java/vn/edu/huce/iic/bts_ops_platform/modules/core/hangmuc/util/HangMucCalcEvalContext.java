package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class HangMucCalcEvalContext {
    final HangMucCalcGrid grid;
    final Map<String, BigDecimal> cache = new HashMap<>();
    final Set<String> stack = new HashSet<>();

    HangMucCalcEvalContext(HangMucCalcGrid grid) {
        this.grid = grid;
    }
}
