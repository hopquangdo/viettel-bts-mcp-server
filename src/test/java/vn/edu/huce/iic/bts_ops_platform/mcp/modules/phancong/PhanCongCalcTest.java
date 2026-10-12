package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong;

import org.junit.jupiter.api.Test;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.query.PhanCongDanhSachResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.query.PhanCongRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.request.PhanCongFilter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhanCongCalcTest {

    @Test
    void toQueryNormalizesKeywordAndPagination() {
        PhanCongFilter filter = new PhanCongFilter(
                "  Giai_đoạn 50%\\A  ", null, null, null, null, null, null, null, null, null, null);

        var query = PhanCongCalc.toQuery(filter, -2, 500);

        assertEquals("%giai\\_đoạn 50\\%\\\\a%", query.keywordPattern());
        assertEquals(0, query.page());
        assertEquals(PhanCongCalc.MAX_PAGE_SIZE, query.pageSize());
    }

    @Test
    void toQueryUsesNullForBlankKeywordAndDefaultPageSize() {
        var query = PhanCongCalc.toQuery(PhanCongFilter.empty(), null, null);

        assertNull(query.keywordPattern());
        assertEquals(0, query.page());
        assertEquals(PhanCongCalc.DEFAULT_PAGE_SIZE, query.pageSize());
    }

    @Test
    void toResponseCalculatesPageBoundariesAndCopiesRows() {
        UUID assignmentId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-02T03:04:05Z");
        var firstPage = PhanCongCalc.toResponse(new PhanCongDanhSachResult(List.of(
            new PhanCongRow(assignmentId, null, null, "Vendor", "Survey", null,
                "NORTH", null, true, createdAt)), 7), 0, 3);
        var lastPage = PhanCongCalc.toResponse(new PhanCongDanhSachResult(List.of(), 7), 2, 3);

        assertEquals(assignmentId, firstPage.items().get(0).id());
        assertEquals(createdAt, firstPage.items().get(0).ngayTao());
        assertEquals(3, firstPage.totalPages());
        assertTrue(firstPage.hasNext());
        assertFalse(firstPage.hasPrevious());
        assertFalse(lastPage.hasNext());
        assertTrue(lastPage.hasPrevious());
    }
}