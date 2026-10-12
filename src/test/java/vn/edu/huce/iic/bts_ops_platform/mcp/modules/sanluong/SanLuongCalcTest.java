package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong;

import org.junit.jupiter.api.Test;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.query.SanLuongProgressGroupQuery;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.response.SanLuongProgressSummaryResponse;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SanLuongCalcTest {

    private final SanLuongCalc calc = new SanLuongCalc();

    @Test
    void calculatesObjectWeightedProgressAndLowCompletionCount() {
        SanLuongProgressSummaryResponse result = calc.tienDo(List.of(
                new SanLuongProgressGroupQuery(2L, 4, 1),
                new SanLuongProgressGroupQuery(1L, 4, 3),
                new SanLuongProgressGroupQuery(1L, 4, 4),
                new SanLuongProgressGroupQuery(1L, 4, 0)), 50.0);

        assertEquals(new BigDecimal("45.0"), result.tyLeHoanThanh());
        assertEquals(1L, result.soDoiTuongHoanThanh());
        assertEquals(3L, result.soDoiTuongDangThiCong());
        assertEquals(1L, result.soDoiTuongChuaBatDau());
        assertEquals(2L, result.soDoiTuongHoanThanhThap());
    }

    @Test
    void thresholdIsExclusiveAndGroupsWithoutObjectsOrWorkItemsAreIgnored() {
        SanLuongProgressSummaryResponse result = calc.tienDo(List.of(
                new SanLuongProgressGroupQuery(1L, 2, 1),
                new SanLuongProgressGroupQuery(0L, 2, 0),
                new SanLuongProgressGroupQuery(4L, 0, 0),
                new SanLuongProgressGroupQuery(null, 2, 1),
                new SanLuongProgressGroupQuery(2L, null, 1)), 50.0);

        assertEquals(new BigDecimal("50.0"), result.tyLeHoanThanh());
        assertEquals(0L, result.soDoiTuongHoanThanhThap());
        assertEquals(1L, result.soDoiTuongDangThiCong());
    }

    @Test
    void emptyGroupsReturnZeroSummary() {
        SanLuongProgressSummaryResponse result = calc.tienDo(List.of(), 50.0);

        assertEquals(BigDecimal.ZERO, result.tyLeHoanThanh());
        assertEquals(0L, result.soDoiTuongHoanThanh());
        assertEquals(0L, result.soDoiTuongDangThiCong());
        assertEquals(0L, result.soDoiTuongChuaBatDau());
        assertEquals(0L, result.soDoiTuongHoanThanhThap());
    }
}