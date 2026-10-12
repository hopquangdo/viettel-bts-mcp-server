package vn.edu.huce.iic.bts_ops_platform.mcp.common.loc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhamViLocTest {

    @Test
    void exposesTheSharedScopeFieldsInPlanOrder() {
        var phamVi = new PhamViLoc("doi-tuong", "hop-dong", "nha-thau", "khu-vuc", "tinh-thanh", "loai-hop-dong");

        assertThat(phamVi.doiTuong()).isEqualTo("doi-tuong");
        assertThat(phamVi.hopDong()).isEqualTo("hop-dong");
        assertThat(phamVi.nhaThau()).isEqualTo("nha-thau");
        assertThat(phamVi.khuVuc()).isEqualTo("khu-vuc");
        assertThat(phamVi.tinhThanh()).isEqualTo("tinh-thanh");
        assertThat(phamVi.loaiHopDong()).isEqualTo("loai-hop-dong");
    }

    @Test
    void supportsNullForUnfilteredDimensionsAndRecordValueEquality() {
        var phamVi = new PhamViLoc(null, "HD-01", null, null, "HN", null);

        assertThat(phamVi).isEqualTo(new PhamViLoc(null, "HD-01", null, null, "HN", null));
        assertThat(phamVi.doiTuong()).isNull();
        assertThat(phamVi.nhaThau()).isNull();
    }
}