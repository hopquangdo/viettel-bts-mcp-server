package vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VolumeCalcTest {

    @Test
    void ratioHandlesNullAndNonPositiveDenominators() {
        assertEquals(0, VolumeCalc.ratio(null, BigDecimal.TEN).compareTo(BigDecimal.ZERO));
        assertEquals(BigDecimal.ZERO, VolumeCalc.ratio(BigDecimal.TEN, null));
        assertEquals(BigDecimal.ZERO, VolumeCalc.ratio(BigDecimal.TEN, BigDecimal.ZERO));
        assertEquals(BigDecimal.ZERO, VolumeCalc.ratio(BigDecimal.TEN, BigDecimal.valueOf(-1)));
        assertEquals(new BigDecimal("33.33"), VolumeCalc.ratio(BigDecimal.ONE, BigDecimal.valueOf(3)));
        assertEquals(BigDecimal.ZERO, VolumeCalc.nz(null));
    }

    @Test
    void resolvesStatusAtThresholdsAndForInvalidPlanValues() {
        assertEquals("binh_thuong", VolumeCalc.resolveStatus(0, BigDecimal.valueOf(100)));
        assertEquals("binh_thuong", VolumeCalc.resolveStatus(-1, BigDecimal.valueOf(100)));
        assertEquals("binh_thuong", VolumeCalc.resolveStatus(100, new BigDecimal("89.99")));
        assertEquals("canh_bao", VolumeCalc.resolveStatus(100, BigDecimal.valueOf(90)));
        assertEquals("canh_bao", VolumeCalc.resolveStatus(100, BigDecimal.valueOf(80), BigDecimal.valueOf(80)));
        assertEquals("vuot_nguong", VolumeCalc.resolveStatus(100, new BigDecimal("100.01")));
    }

    @Test
    void resolvesProvinceVariantsInPriorityOrder() {
        assertEquals("surplus", VolumeCalc.resolveProvinceVariant(2, 1, BigDecimal.ONE, BigDecimal.ZERO));
        assertEquals("shortage", VolumeCalc.resolveProvinceVariant(1, 0, BigDecimal.ONE, BigDecimal.ZERO));
        assertEquals("surplus", VolumeCalc.resolveProvinceVariant(0, 0, BigDecimal.valueOf(-1), BigDecimal.ZERO));
        assertEquals("warning", VolumeCalc.resolveProvinceVariant(0, 0, BigDecimal.ONE, BigDecimal.valueOf(90)));
        assertEquals("warning", VolumeCalc.resolveProvinceVariant(0, 0, BigDecimal.ONE,
                BigDecimal.valueOf(80), BigDecimal.valueOf(80)));
        assertEquals("normal", VolumeCalc.resolveProvinceVariant(0, 0, BigDecimal.ONE, BigDecimal.valueOf(89)));
    }

    @Test
    void computesFormulaReferencesAndPercentages() {
        UUID groupId = UUID.randomUUID();
        UUID firstItemId = UUID.randomUUID();
        UUID secondItemId = UUID.randomUUID();
        var result = VolumeCalc.compute(
                List.of(new VolumeCalc.NhomRow(groupId, "G", "Group")),
                List.of(
                        new VolumeCalc.ChiTietRow(firstItemId, groupId, "A", "First", BigDecimal.TEN,
                                BigDecimal.valueOf(2), null, null, null),
                        new VolumeCalc.ChiTietRow(secondItemId, groupId, "B", "Second", null,
                                null, null, null, "@A.TT * 25%")),
                List.of(), Map.of(), Map.of());

        assertEquals(0, result.tongThanhTien().compareTo(BigDecimal.valueOf(25)));
        assertEquals(new BigDecimal("20"), result.nhom().get(0).hangMuc().get(0).thanhTien());
        assertEquals(0, result.nhom().get(0).hangMuc().get(1).thanhTien().compareTo(BigDecimal.valueOf(5)));
    }
}