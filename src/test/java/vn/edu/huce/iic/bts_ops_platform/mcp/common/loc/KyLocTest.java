package vn.edu.huce.iic.bts_ops_platform.mcp.common.loc;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class KyLocTest {

    @Test
    void exposesOptionalLocalDateBoundsAndStringGranularity() {
        var ky = new KyLoc(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), "thang");

        assertThat(ky.fromDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(ky.toDate()).isEqualTo(LocalDate.of(2026, 3, 31));
        assertThat(ky.chiaTheo()).isEqualTo("thang");
    }

    @Test
    void allowsEachPeriodFilterToBeOmitted() {
        var ky = new KyLoc(null, null, null);

        assertThat(ky.fromDate()).isNull();
        assertThat(ky.toDate()).isNull();
        assertThat(ky.chiaTheo()).isNull();
    }
}