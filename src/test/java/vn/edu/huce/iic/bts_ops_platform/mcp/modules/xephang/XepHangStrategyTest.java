package vn.edu.huce.iic.bts_ops_platform.mcp.modules.xephang;

import org.junit.jupiter.api.Test;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankingOverviewDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.XepHangKhuVucProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecRowToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.ProgressGroupProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.HopDongOpenCountProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongKhuVucXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongTinhXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangContext;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang.NguonViecRuiRoXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang.NguonViecTrungTamXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongKhuVucXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongTinhXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongXepHangContext;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacHopDongXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacXepHangContext;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.HopDongToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.VuongMacToolRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class XepHangStrategyTest {

    private static final UUID KV1 = UUID.randomUUID();
    private static final UUID KV2 = UUID.randomUUID();

    private static ProgressGroupProjection nhom(UUID khuVucId, String khuVucTen, long n, String giaTri, int tong, int xong, boolean vuong) {
        ProgressGroupProjection g = mock(ProgressGroupProjection.class);
        when(g.getKhuVucId()).thenReturn(khuVucId);
        when(g.getKhuVucTen()).thenReturn(khuVucTen);
        when(g.getN()).thenReturn(n);
        when(g.getPeriodValue()).thenReturn(new BigDecimal(giaTri));
        when(g.getTotalHangMuc()).thenReturn(tong);
        when(g.getDoneHangMuc()).thenReturn(xong);
        when(g.getHasOpenVuongMac()).thenReturn(vuong);
        return g;
    }

    // ------------------------------------------------------------------ sản lượng

    @Test
    void sanLuong_khuVuc_gomTheoNhomVaSapTheoGiaTri() {
        var groups = List.of(
                nhom(KV1, "TTKV1", 2, "100000000", 10, 5, false),  // 2 đối tượng x 100tr = 200tr, 50%
                nhom(KV2, "TTKV2", 1, "500000000", 10, 10, true),  // 500tr, 100%
                nhom(KV1, "TTKV1", 1, "50000000", 10, 0, true));   // +50tr, 0%
        var ctx = new SanLuongXepHangContext(groups, List.of(), 4, "giaTri", false, Set.of());

        RankingOverviewDto r = new SanLuongKhuVucXepHangStrategy().rank(ctx);

        assertThat(r.getTotalEntities()).isEqualTo(2);
        assertThat(r.getRankings()).extracting(x -> x.name()).containsExactly("TTKV2", "TTKV1");
        var kv1 = r.getRankings().get(1);
        assertThat(kv1.rank()).isEqualTo(2);
        assertThat(kv1.value()).isEqualByComparingTo("250000000");
        assertThat(kv1.objectCount()).isEqualTo(3);
        assertThat(kv1.issueCount()).isEqualTo(1);
        assertThat(kv1.completionRate()).isEqualByComparingTo("33.3"); // (50*2 + 0*1) / 3
        assertThat(r.getTopByCompletion().name()).isEqualTo("TTKV2");
        assertThat(r.getBottomByCompletion().name()).isEqualTo("TTKV1");
    }

    @Test
    void sanLuong_tangDanTheoHoanThanh() {
        var groups = List.of(nhom(KV1, "TTKV1", 1, "1", 10, 9, false), nhom(KV2, "TTKV2", 1, "2", 10, 1, false));
        var ctx = new SanLuongXepHangContext(groups, List.of(), 2, "hoanThanh", true, Set.of());
        assertThat(new SanLuongKhuVucXepHangStrategy().rank(ctx).getRankings()).extracting(x -> x.name())
                .containsExactly("TTKV2", "TTKV1");
    }

    @Test
    void sanLuong_lyDoBoQua_khiLocCungCap() {
        var strategy = new SanLuongKhuVucXepHangStrategy();
        var ctxLoc = new SanLuongXepHangContext(List.of(), List.of(), 0, "giaTri", false, Set.of(SanLuongXepHangChieu.KHU_VUC));
        var ctxKhac = new SanLuongXepHangContext(List.of(), List.of(), 0, "giaTri", false, Set.of(SanLuongXepHangChieu.TINH));
        assertThat(strategy.lyDoBoQua(ctxLoc)).contains("khu vực");
        assertThat(strategy.lyDoBoQua(ctxKhac)).isNull();
    }

    @Test
    void sanLuong_tinh_doiTuongChuaGanTinhGomMotDong() {
        ProgressGroupProjection g = mock(ProgressGroupProjection.class);
        when(g.getTinhId()).thenReturn(null);
        when(g.getN()).thenReturn(3L);
        when(g.getPeriodValue()).thenReturn(BigDecimal.TEN);
        when(g.getTotalHangMuc()).thenReturn(0);
        var ctx = new SanLuongXepHangContext(List.of(g), List.of(), 3, "giaTri", false, Set.of());
        var r = new SanLuongTinhXepHangStrategy().rank(ctx);
        assertThat(r.getRankings()).hasSize(1);
        assertThat(r.getRankings().get(0).id()).isEqualTo(SanLuongTinhXepHangStrategy.CHUA_GAN_TINH.toString());
        assertThat(r.getRankings().get(0).objectCount()).isEqualTo(3);
    }

    // ------------------------------------------------------------------ hợp đồng

    @Test
    void hopDong_khuVuc_goiRepositoryVaMapDong() {
        HopDongToolRepository repo = mock(HopDongToolRepository.class);
        XepHangKhuVucProjection row = mock(XepHangKhuVucProjection.class);
        when(row.getKhuVuc()).thenReturn("TTKV1");
        when(row.getSoHopDong()).thenReturn(5L);
        when(row.getSoDoiTuong()).thenReturn(null);
        when(row.getSoVuongMac()).thenReturn(2L);
        when(repo.rankKhuVuc(eq(20), isNull(), isNull(), isNull(), isNull(), isNull(), isNull())).thenReturn(List.of(row));

        var ctx = new HopDongXepHangContext(null, null, null, null, null, null, null, 20);
        var rows = new HopDongKhuVucXepHangStrategy(repo).rank(ctx);

        assertThat(rows).singleElement().satisfies(r -> {
            assertThat(r.ten()).isEqualTo("TTKV1");
            assertThat(r.soHopDong()).isEqualTo(5L);
            assertThat(r.soDoiTuong()).isZero();
            assertThat(r.soHoanThanh()).isNull();
            assertThat(r.soVuongMac()).isEqualTo(2L);
        });
    }

    @Test
    void hopDong_tinh_canLocHopDong() {
        HopDongToolRepository repo = mock(HopDongToolRepository.class);
        var strategy = new HopDongTinhXepHangStrategy(repo);
        var khongHopDong = new HopDongXepHangContext(null, null, null, null, null, null, null, 20);
        assertThat(strategy.lyDoBoQua(khongHopDong)).contains("hopDong");
        assertThat(strategy.lyDoBoQua(new HopDongXepHangContext(null, null, null, UUID.randomUUID(), null, null, null, 20))).isNull();
        verifyNoInteractions(repo);
    }

    // ------------------------------------------------------------------ vướng mắc

    @Test
    void vuongMac_hopDong_giuMaVaTen() {
        VuongMacToolRepository repo = mock(VuongMacToolRepository.class);
        HopDongOpenCountProjection row = mock(HopDongOpenCountProjection.class);
        when(row.getLabel()).thenReturn("TVTK023241");
        when(row.getName()).thenReturn("Hợp đồng A");
        when(row.getValue()).thenReturn(7L);
        when(repo.rankHopDongByOpenCount(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(row));
        var ctx = new VuongMacXepHangContext(null, null, null, null, null, null, null, null, null, null, null, 5);

        var rows = new VuongMacHopDongXepHangStrategy(repo).rank(ctx);

        assertThat(rows).singleElement().satisfies(r -> {
            assertThat(r.getLabel()).isEqualTo("TVTK023241");
            assertThat(r.getName()).isEqualTo("Hợp đồng A");
            assertThat(r.getValue()).isEqualTo(7L);
        });
        assertThat(new VuongMacHopDongXepHangStrategy(repo).lyDoBoQua(
                new VuongMacXepHangContext(UUID.randomUUID(), null, null, null, null, null, null, null, null, null, null, 5))).isNotNull();
    }

    // ------------------------------------------------------------------ nguồn việc

    private static NguonViecRowToolItem hd(String ma, String trungTam, double giaTri, double sx, double vuong, String status) {
        return NguonViecRowToolItem.builder().maHD(ma).chuongTrinh("CT " + ma).trungTam(trungTam)
                .giaTriHD(giaTri).sxTong(sx).slVuong(vuong).status(status).build();
    }

    @Test
    void nguonViec_trungTam_gomVaSapTheoSanLuong() {
        var rows = List.of(hd("A", "TT1", 10, 2, 0, null), hd("B", "TT2", 5, 4, 0, null), hd("C", "TT1", 3, 1.5, 0, null));
        var out = new NguonViecTrungTamXepHangStrategy().rank(rows, 1);
        assertThat(out).extracting(r -> r.getLabel()).containsExactly("TT2", "TT1"); // không cắt top
        assertThat(out.get(1).getValue()).isEqualTo(3.5);
        assertThat(out.get(1).getExtra()).containsEntry("soHopDong", 2).containsEntry("giaTriHD", 13.0);
    }

    @Test
    void nguonViec_ruiRo_catTop() {
        var rows = List.of(hd("A", "TT1", 10, 10, 0, null), hd("B", "TT1", 10, 0, 1, "vuong_phap_ly"), hd("C", "TT1", 10, 5, 0, null));
        var out = new NguonViecRuiRoXepHangStrategy().rank(rows, 2);
        assertThat(out).extracting(r -> r.getLabel()).containsExactly("B - CT B", "C - CT C");
        assertThat(out.get(0).getValue()).isEqualTo(17.0); // 1*2 + 10 + 5
    }
}
