package vn.edu.huce.iic.bts_ops_platform.mcp.modules.xephang;

import org.junit.jupiter.api.Test;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongKhuVucXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongTinhXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangRegistry;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang.NguonViecRuiRoXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang.NguonViecTrungTamXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang.NguonViecXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang.NguonViecXepHangRegistry;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongDoiTuongXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongHopDongXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongKhuVucXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongLoaiHopDongXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongNhaThauXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongTinhXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongXepHangRegistry;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacHopDongXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacKhuVucXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacXepHangRegistry;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Registry phải có đúng 1 strategy cho mỗi chiều; thiếu hoặc trùng thì lỗi ngay khi khởi tạo bean. */
class XepHangRegistryTest {

    @Test
    void sanLuong_duChieu() {
        var registry = new SanLuongXepHangRegistry(List.of(new SanLuongNhaThauXepHangStrategy(), new SanLuongKhuVucXepHangStrategy(),
                new SanLuongTinhXepHangStrategy(), new SanLuongHopDongXepHangStrategy(), new SanLuongLoaiHopDongXepHangStrategy(),
                new SanLuongDoiTuongXepHangStrategy()));
        for (SanLuongXepHangChieu c : SanLuongXepHangChieu.values()) {
            assertThat(registry.get(c).chieu()).isEqualTo(c);
        }
        assertThat(registry.all().keySet()).containsExactly(SanLuongXepHangChieu.values());
    }

    @Test
    void sanLuong_thieuChieu_loiKhiKhoiDong() {
        assertThatThrownBy(() -> new SanLuongXepHangRegistry(List.of(new SanLuongNhaThauXepHangStrategy(),
                new SanLuongKhuVucXepHangStrategy())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Thiếu strategy")
                .hasMessageContaining("TINH")
                .hasMessageContaining("DOI_TUONG");
    }

    @Test
    void sanLuong_trungChieu_loiKhiKhoiDong() {
        assertThatThrownBy(() -> new SanLuongXepHangRegistry(List.of(new SanLuongNhaThauXepHangStrategy(),
                new SanLuongNhaThauXepHangStrategy())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trùng strategy")
                .hasMessageContaining("NHA_THAU");
    }

    @Test
    void hopDong() {
        var registry = new HopDongXepHangRegistry(List.of(new HopDongKhuVucXepHangStrategy(null), new HopDongTinhXepHangStrategy(null)));
        assertThat(registry.get(HopDongXepHangChieu.TINH)).isInstanceOf(HopDongTinhXepHangStrategy.class);
        assertThatThrownBy(() -> new HopDongXepHangRegistry(List.of(new HopDongKhuVucXepHangStrategy(null))))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("TINH");
    }

    @Test
    void vuongMac() {
        var registry = new VuongMacXepHangRegistry(List.of(new VuongMacHopDongXepHangStrategy(null), new VuongMacKhuVucXepHangStrategy(null)));
        assertThat(registry.get(VuongMacXepHangChieu.KHU_VUC)).isInstanceOf(VuongMacKhuVucXepHangStrategy.class);
        assertThatThrownBy(() -> new VuongMacXepHangRegistry(List.of(new VuongMacHopDongXepHangStrategy(null))))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("KHU_VUC");
    }

    @Test
    void nguonViec() {
        var registry = new NguonViecXepHangRegistry(List.of(new NguonViecRuiRoXepHangStrategy(), new NguonViecTrungTamXepHangStrategy()));
        assertThat(registry.get(NguonViecXepHangChieu.RUI_RO)).isInstanceOf(NguonViecRuiRoXepHangStrategy.class);
        assertThatThrownBy(() -> new NguonViecXepHangRegistry(List.of(new NguonViecTrungTamXepHangStrategy())))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("RUI_RO");
    }
}
