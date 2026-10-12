package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong;

import org.junit.jupiter.api.Test;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.query.HopDongTienDoQuery;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.response.HopDongCanhBaoTienDoResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HopDongCalcTest {

    @Test
    void classifiesThresholdBoundariesAndReturnsOnlyRedContracts() {
        HopDongCanhBaoTienDoResponse response = HopDongCalc.canhBaoTienDo(List.of(
                hopDong("green", "90", "100"),
                hopDong("yellow", "70", "100"),
                hopDong("red", "6999", "10000")), 90, 70);

        assertEquals(1, response.soHopDongXanh());
        assertEquals(1, response.soHopDongVang());
        assertEquals(1, response.soHopDongDo());
        assertEquals("red", response.hopDongDo().get(0).maHopDong());
        assertEquals(69.99, response.hopDongDo().get(0).tyLeHoanThanh());
    }

    @Test
    void skipsContractsWithoutPositiveContractValueAndTreatsNullProgressAsZero() {
        HopDongCanhBaoTienDoResponse response = HopDongCalc.canhBaoTienDo(List.of(
                hopDong("null-total", "1", null),
                hopDong("zero-total", "1", "0"),
                hopDong("negative-total", "1", "-1"),
                hopDong("null-progress", null, "100")), 90, 70);

        assertEquals(0, response.soHopDongXanh());
        assertEquals(0, response.soHopDongVang());
        assertEquals(1, response.soHopDongDo());
        assertEquals("null-progress", response.hopDongDo().get(0).maHopDong());
        assertEquals(0.0, response.hopDongDo().get(0).tyLeHoanThanh());
    }

    private static HopDongTienDoQuery hopDong(String ma, String sanLuong, String tongKhoiLuong) {
        return new HopDongTienDoQuery(
                UUID.randomUUID(), ma, ma,
                tongKhoiLuong != null ? new BigDecimal(tongKhoiLuong) : null,
                sanLuong != null ? new BigDecimal(sanLuong) : null);
    }
}