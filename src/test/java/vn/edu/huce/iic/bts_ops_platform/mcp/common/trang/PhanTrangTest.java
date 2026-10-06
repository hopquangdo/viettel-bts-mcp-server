package vn.edu.huce.iic.bts_ops_platform.mcp.common.trang;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhanTrangTest {

    @Test
    void resolvePage_keepsZeroBasedIndexAndDefaultsInvalidValuesToZero() {
        assertThat(PhanTrang.resolvePage(null)).isZero();
        assertThat(PhanTrang.resolvePage(-1)).isZero();
        assertThat(PhanTrang.resolvePage(0)).isZero();
        assertThat(PhanTrang.resolvePage(4)).isEqualTo(4);
    }

    @Test
    void resolvePageSize_usesMcpDefaultsAndClampsToCurrentLimits() {
        assertThat(PhanTrang.resolvePageSize(null)).isEqualTo(3);
        assertThat(PhanTrang.resolvePageSize(0)).isEqualTo(1);
        assertThat(PhanTrang.resolvePageSize(-5)).isEqualTo(1);
        assertThat(PhanTrang.resolvePageSize(25)).isEqualTo(25);
        assertThat(PhanTrang.resolvePageSize(150)).isEqualTo(100);
    }

    @Test
    void toPageRequest_usesNormalizedZeroBasedPageAndSize() {
        var request = PhanTrang.toPageRequest(4, 150);
        var defaultRequest = PhanTrang.toPageRequest(null, null);

        assertThat(request.getPageNumber()).isEqualTo(4);
        assertThat(request.getPageSize()).isEqualTo(100);
        assertThat(defaultRequest.getPageNumber()).isZero();
        assertThat(defaultRequest.getPageSize()).isEqualTo(3);
    }

    @Test
    void trangOf_calculatesTotalPagesAndCopiesItems() {
        var source = new ArrayList<>(List.of("a", "b"));

        var result = Trang.of(source, 1, 3, 7);
        source.add("c");

        assertThat(result.items()).containsExactly("a", "b");
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(3);
        assertThat(result.totalItems()).isEqualTo(7);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThatThrownBy(() -> result.items().add("d"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void trangOf_hasNoPagesWhenTotalIsZero() {
        assertThat(Trang.of(List.of(), 0, 3, 0).totalPages()).isZero();
    }
}