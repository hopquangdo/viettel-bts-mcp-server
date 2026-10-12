package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban;

import org.junit.jupiter.api.Test;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.loc.KyLoc;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.loc.PhamViLoc;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.request.BienBanFilter;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class BienBanCalcTest {

    @Test
    void normalizesKnownAliasesAndLeavesUnknownStatusesAvailableForExactFiltering() {
        assertEquals("cho_duyet", BienBanCalc.normalizeTrangThai(" Pending "));
        assertEquals("da_duyet", BienBanCalc.normalizeTrangThai("DA DUYET"));
        assertEquals("tu_choi", BienBanCalc.normalizeTrangThai("rejected"));
        assertEquals("custom_status", BienBanCalc.normalizeTrangThai(" Custom Status "));
        assertNull(BienBanCalc.normalizeTrangThai("  "));
    }

    @Test
    void normalizeFilterReversesDatesAndPreservesOtherInputs() {
        PhamViLoc scope = new PhamViLoc(null, "HD-01", "Nha thau A", null, "Ha Noi", null);
        KyLoc period = new KyLoc(LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 1), "thang");

        BienBanFilter normalized = BienBanCalc.normalizeFilter(new BienBanFilter(scope, "approved", period));

        assertSame(scope, normalized.phamVi());
        assertEquals("da_duyet", normalized.trangThai());
        assertEquals(new KyLoc(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), "thang"), normalized.ky());
    }
}