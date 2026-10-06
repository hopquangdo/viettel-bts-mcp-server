package vn.edu.huce.iic.bts_ops_platform.mcp.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongTrangThaiBaoCao;
import vn.edu.huce.iic.bts_ops_platform.mcp.exception.McpInvalidParamException;
import vn.edu.huce.iic.bts_ops_platform.mcp.exception.ToolErrorCode;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang.NguonViecXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacXepHangChieu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class McpCodeEnumsTest {

    @Test
    void chuanHoa_boDauHoaThuongKhoangTrang() {
        assertThat(McpCodeEnums.chuanHoa("  Đã Báo ")).isEqualTo("da_bao");
        assertThat(McpCodeEnums.chuanHoa("khu-vực")).isEqualTo("khu_vuc");
        assertThat(McpCodeEnums.chuanHoa("LOAI_HOP_DONG")).isEqualTo("loai_hop_dong");
        assertThat(McpCodeEnums.chuanHoa(null)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"khu_vuc", "KHU_VUC", " khu vực ", "Khu-Vuc", "khuvuc"})
    void sanLuongChieu_khopMaVaAlias(String input) {
        assertThat(SanLuongXepHangChieu.of(input)).isEqualTo(SanLuongXepHangChieu.KHU_VUC);
    }

    @Test
    void sanLuongChieu_moiMaDeuParseDuoc() {
        for (SanLuongXepHangChieu c : SanLuongXepHangChieu.values()) {
            assertThat(SanLuongXepHangChieu.of(c.code())).isEqualTo(c);
            assertThat(SanLuongXepHangChieu.of(c.label())).as(c.label()).isEqualTo(c);
        }
        assertThat(SanLuongXepHangChieu.of("tỉnh thành")).isEqualTo(SanLuongXepHangChieu.TINH);
        assertThat(SanLuongXepHangChieu.of("Loại hợp đồng")).isEqualTo(SanLuongXepHangChieu.LOAI_HOP_DONG);
        assertThat(SanLuongXepHangChieu.codes()).isEqualTo("nha_thau, khu_vuc, tinh, hop_dong, loai_hop_dong, doi_tuong");
    }

    @Test
    void sanLuongChieu_saiMa_baoLoiTiengVietKemDanhSach() {
        assertThatThrownBy(() -> SanLuongXepHangChieu.of("vung"))
                .isInstanceOf(McpInvalidParamException.class)
                .hasMessageContaining("'vung'")
                .hasMessageContaining("theo")
                .hasMessageContaining("nha_thau (Nhà thầu)")
                .hasMessageContaining("doi_tuong (Đối tượng)")
                .satisfies(e -> assertThat(((McpInvalidParamException) e).getExceptionCode()).isEqualTo(ToolErrorCode.FILTER_INVALID));
    }

    @Test
    void thieuThamSo_baoLoi() {
        assertThatThrownBy(() -> SanLuongXepHangChieu.of(null))
                .isInstanceOf(McpInvalidParamException.class).hasMessageStartingWith("Thiếu tham số theo");
        assertThatThrownBy(() -> SanLuongTrangThaiBaoCao.of("  "))
                .isInstanceOf(McpInvalidParamException.class).hasMessageStartingWith("Thiếu tham số trangThai");
    }

    @ParameterizedTest
    @ValueSource(strings = {"da_bao", "reported", "REPORTED", "đã báo", "Đã báo sản lượng", "da bao cao"})
    void trangThaiBaoCao_daBao(String input) {
        assertThat(SanLuongTrangThaiBaoCao.of(input)).isEqualTo(SanLuongTrangThaiBaoCao.DA_BAO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"chua_bao", "not_reported", "chưa báo", "Chưa báo sản lượng", "not-reported"})
    void trangThaiBaoCao_chuaBao(String input) {
        assertThat(SanLuongTrangThaiBaoCao.of(input)).isEqualTo(SanLuongTrangThaiBaoCao.CHUA_BAO);
    }

    @Test
    void trangThaiBaoCao_all_khongConHopLe() {
        assertThatThrownBy(() -> SanLuongTrangThaiBaoCao.of("all"))
                .isInstanceOf(McpInvalidParamException.class)
                .hasMessageContaining("da_bao (Đã báo sản lượng), chua_bao (Chưa báo sản lượng)");
    }

    @Test
    void cacModuleKhac() {
        assertThat(HopDongXepHangChieu.of("Tỉnh")).isEqualTo(HopDongXepHangChieu.TINH);
        assertThat(VuongMacXepHangChieu.of("hợp đồng")).isEqualTo(VuongMacXepHangChieu.HOP_DONG);
        assertThat(NguonViecXepHangChieu.of("Rủi ro")).isEqualTo(NguonViecXepHangChieu.RUI_RO);
        assertThat(NguonViecXepHangChieu.of("trung tâm")).isEqualTo(NguonViecXepHangChieu.TRUNG_TAM);
        assertThatThrownBy(() -> NguonViecXepHangChieu.of("sap_het_gia_tri"))
                .isInstanceOf(McpInvalidParamException.class).hasMessageContaining("rui_ro, trung_tam".replace(", ", " (Mức rủi ro hợp đồng), "));
    }
}
