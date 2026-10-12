package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PeriodInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankedEntityDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.RankingBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SanLuongToolMapperTest {

    private static final PeriodInfo KY = new PeriodInfo("2026-01-01", "2026-09-30", "2026-01-01 → 2026-09-30");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void xepHang_giaTriTiengViet() {
        var ranking = RankingBuilder.build(List.of(
                new RankedEntityDto(UUID.randomUUID().toString(), "TTKV1", new BigDecimal("1250000000"), new BigDecimal("82.5"), 120, 4, null),
                new RankedEntityDto(UUID.randomUUID().toString(), "TTKV2", new BigDecimal("820000000"), new BigDecimal("64.0"), 95, 7, null)),
                Integer.MAX_VALUE, "giaTri", false);

        var res = SanLuongToolMapper.xepHang(SanLuongXepHangChieu.KHU_VUC, KY, "giaTri", false, ranking, null);
        System.out.println("sanluong_xep_hang: " + JSON.writeValueAsString(res));

        assertThat(res.kyBaoCao()).isEqualTo("01/01/2026 – 30/09/2026");
        assertThat(res.xepHangTheo()).isEqualTo("Khu vực");
        assertThat(res.tieuChi()).isEqualTo("Giá trị sản lượng, cao xuống thấp");
        assertThat(res.tongSo()).isEqualTo(2);
        assertThat(res.danhSach()).extracting(SanLuongXepHangItem::giaTri).containsExactly("1,25 tỷ đồng", "820 triệu đồng");
        assertThat(res.danhSach().get(0).tyLeHoanThanh()).isEqualTo("82,5%");
        assertThat(res.caoNhatGiaTri().ten()).isEqualTo("TTKV1");
        assertThat(res.thapNhatHoanThanh().ten()).isEqualTo("TTKV2");
        assertThat(res.ghiChu()).isNull();
    }

    @Test
    void xepHang_boQua_chiCoGhiChu() {
        var res = SanLuongToolMapper.xepHang(SanLuongXepHangChieu.KHU_VUC, KY, "hoanThanh", true, null, "Đang lọc ... theo=tinh");
        System.out.println("sanluong_xep_hang (bỏ qua): " + JSON.writeValueAsString(res));
        assertThat(res.danhSach()).isNull();
        assertThat(res.tieuChi()).isEqualTo("Tỷ lệ hoàn thành hạng mục, thấp lên cao");
        assertThat(JSON.writeValueAsString(res)).doesNotContain("danhSach").contains("ghiChu");
    }

    @Test
    void nhaThauBaoCao_daBao() {
        SanLuongNhaThauItem item = new SanLuongNhaThauItem();
        item.setNhaThau(NhaThauInfo.of(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Công ty Royal"));
        item.setSoDoiTuongPhuTrach(42);
        item.setGiaTriDaBao(new BigDecimal("820000000"));
        var res = SanLuongToolMapper.nhaThauBaoCao(SanLuongTrangThaiBaoCao.DA_BAO, KY, PagedResult.of(List.of(item), 0, 3, 7));
        System.out.println("sanluong_nha_thau_bao_cao: " + JSON.writeValueAsString(res));

        assertThat(res.trangThai()).isEqualTo("Đã báo sản lượng");
        assertThat(res.tongSo()).isEqualTo(7);
        assertThat(res.tongSoTrang()).isEqualTo(3);
        assertThat(res.danhSach()).singleElement().satisfies(i -> {
            assertThat(i.ten()).isEqualTo("Công ty Royal");
            assertThat(i.giaTriDaBao()).isEqualTo("820 triệu đồng");
        });
    }

    @Test
    void nhaThauBaoCao_chuaBao_khongCoGiaTri() {
        SanLuongNhaThauItem item = new SanLuongNhaThauItem();
        item.setNhaThau(NhaThauInfo.of(UUID.randomUUID(), "Anh Tu"));
        item.setSoDoiTuongPhuTrach(3);
        var res = SanLuongToolMapper.nhaThauBaoCao(SanLuongTrangThaiBaoCao.CHUA_BAO, KY, PagedResult.of(List.of(item), 0, 3, 1));
        assertThat(res.trangThai()).isEqualTo("Chưa báo sản lượng");
        assertThat(res.danhSach().get(0).giaTriDaBao()).isNull();
        var rong = SanLuongToolMapper.nhaThauBaoCao(SanLuongTrangThaiBaoCao.CHUA_BAO, KY, PagedResult.of(List.of(), 0, 3, 0));
        assertThat(rong.ghiChu()).contains("chưa báo sản lượng");
    }
}
