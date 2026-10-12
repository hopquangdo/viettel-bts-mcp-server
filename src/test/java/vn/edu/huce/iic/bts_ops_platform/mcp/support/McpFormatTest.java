package vn.edu.huce.iic.bts_ops_platform.mcp.support;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class McpFormatTest {

    @Test
    void tien_doiDonViTyTrieuNghin() {
        assertThat(McpFormat.tien(new BigDecimal("1250000000"))).isEqualTo("1,25 tỷ đồng");
        assertThat(McpFormat.tien(new BigDecimal("12345678901"))).isEqualTo("12,35 tỷ đồng");
        assertThat(McpFormat.tien(new BigDecimal("820000000"))).isEqualTo("820 triệu đồng");
        assertThat(McpFormat.tien(new BigDecimal("1500000"))).isEqualTo("1,5 triệu đồng");
        assertThat(McpFormat.tien(new BigDecimal("45000"))).isEqualTo("45 nghìn đồng");
        assertThat(McpFormat.tien(new BigDecimal("999"))).isEqualTo("999 đồng");
        assertThat(McpFormat.tien(BigDecimal.ZERO)).isEqualTo("0 đồng");
        assertThat(McpFormat.tien(new BigDecimal("-2500000000"))).isEqualTo("-2,5 tỷ đồng");
        assertThat(McpFormat.tien(new BigDecimal("1234000000000"))).isEqualTo("1.234 tỷ đồng");
        assertThat(McpFormat.tien(null)).isNull();
    }

    @Test
    void tien_moiKieuSo() {
        assertThat(McpFormat.tien(1_250_000_000L)).isEqualTo("1,25 tỷ đồng");
        assertThat(McpFormat.tien(Integer.valueOf(45000))).isEqualTo("45 nghìn đồng");
        assertThat(McpFormat.tien(Double.valueOf(1.5e6))).isEqualTo("1,5 triệu đồng");
        assertThat(McpFormat.tien((Number) null)).isNull();
    }

    @Test
    void tienAnnotation_ghiChuoiKhiRaJson() throws Exception {
        record Dong(@Tien java.math.BigDecimal giaTri, @Tien Long giaTriHd, long soLuong) {
        }
        String json = new com.fasterxml.jackson.databind.ObjectMapper()
                .writeValueAsString(new Dong(new BigDecimal("4100000000"), null, 3));
        assertThat(json).isEqualTo("{\"giaTri\":\"4,1 tỷ đồng\",\"giaTriHd\":null,\"soLuong\":3}");
    }

    @Test
    void tienTuTy_doiTuDonViTy() {
        assertThat(McpFormat.tienTuTy(1.25)).isEqualTo("1,25 tỷ đồng");
        assertThat(McpFormat.tienTuTy(0.82)).isEqualTo("820 triệu đồng");
    }

    @Test
    void phanTram_dauPhayThapPhan() {
        assertThat(McpFormat.phanTram(new BigDecimal("82.5"))).isEqualTo("82,5%");
        assertThat(McpFormat.phanTram(new BigDecimal("82.54"))).isEqualTo("82,5%");
        assertThat(McpFormat.phanTram(new BigDecimal("100.0"))).isEqualTo("100%");
        assertThat(McpFormat.phanTram(6.66)).isEqualTo("6,7%");
        assertThat(McpFormat.phanTram((BigDecimal) null)).isNull();
    }

    @Test
    void so_phanCachNghinBangDauCham() {
        assertThat(McpFormat.so(1234567)).isEqualTo("1.234.567");
        assertThat(McpFormat.so(new BigDecimal("1234.5"), 2)).isEqualTo("1.234,5");
    }

    @Test
    void ngayVaKy() {
        assertThat(McpFormat.ngay(LocalDate.of(2026, 9, 30))).isEqualTo("30/09/2026");
        assertThat(McpFormat.ngay("2026-01-01")).isEqualTo("01/01/2026");
        assertThat(McpFormat.ngay("khong-phai-ngay")).isEqualTo("khong-phai-ngay");
        assertThat(McpFormat.ky(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 30))).isEqualTo("01/01/2026 – 30/09/2026");
        assertThat(McpFormat.ky("2026-01-01", "2026-09-30")).isEqualTo("01/01/2026 – 30/09/2026");
        assertThat(McpFormat.ky((LocalDate) null, null)).isNull();
    }
}
