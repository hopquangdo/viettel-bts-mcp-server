package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac;

import org.junit.jupiter.api.Test;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.query.VuongMacTongQuanQuery;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VuongMacCalcTest {

    private final VuongMacCalc calc = new VuongMacCalc();

    @Test
    void tongQuan_tinhDangVuong_vaDayDuLoaiMacDinh() {
        var response = calc.tongQuan(new VuongMacTongQuanQuery(
                12, 3, 2, 5, 2, 1, null, 4, 6,
                Map.of("thi_cong", 2L, "loai_moi", 1L)));

        assertThat(response.dangVuong()).isEqualTo(5);
        assertThat(response.daXuLyTrongKy()).isNull();
        assertThat(response.theoLoai()).containsEntry("Thi công", 2L)
                .containsEntry("Giải phóng mặt bằng", 0L)
                .containsEntry("loai_moi", 1L);
        assertThat(response.theoLoai()).hasSize(6);
    }

    @Test
    void tongQuan_giuSoDaXuLyTrongKyKhiCoGiaTri() {
        var response = calc.tongQuan(new VuongMacTongQuanQuery(
                4, 1, 1, 1, 1, 0, 3L, 1, 2, Map.of()));

        assertThat(response.daXuLyTrongKy()).isEqualTo(3L);
        assertThat(response.dangVuong()).isEqualTo(2L);
    }
}