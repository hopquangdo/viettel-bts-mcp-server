package vn.edu.huce.iic.bts_ops_platform.mcp.common.ky;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KyBaoCaoTest {

    @Test
    void chuanHoa_daoKhoangVaDungGiaTriMacDinhChoBienThieu() {
        var khoang = KyBaoCao.chuanHoa(LocalDate.of(2026, 5, 10), null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 1));

        assertThat(khoang).isEqualTo(new KyBaoCao.KhoangNgay(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10)));
    }

    @Test
    void khoangNgay_chuyenThanhInstantTheoGioVietNamVaBaoGomNgayKetThuc() {
        var khoang = new KyBaoCao.KhoangNgay(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1));

        assertThat(khoang.tuInstant()).isEqualTo(Instant.parse("2025-12-31T17:00:00Z"));
        assertThat(khoang.denInstantKhongBaoGom()).isEqualTo(Instant.parse("2026-01-01T17:00:00Z"));
    }

    @Test
    void kyTruoc_giuDoDaiVaNhanKy() {
        var kyTruoc = KyBaoCao.kyTruoc(new KyBaoCao.KhoangNgay(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)));

        assertThat(kyTruoc.khoangNgay()).isEqualTo(new KyBaoCao.KhoangNgay(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 30)));
        assertThat(kyTruoc.nhan()).isEqualTo("Tháng trước");
    }

    @Test
    void chiaO_thangCatBienTheoKhoangYeuCau() {
        var cacKy = KyBaoCao.chiaO(new KyBaoCao.KhoangNgay(
                LocalDate.of(2026, 1, 20), LocalDate.of(2026, 3, 2)), "thang");

        assertThat(cacKy).containsExactly(
                new KyBaoCao.KhoangNgay(LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 31)),
                new KyBaoCao.KhoangNgay(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)),
                new KyBaoCao.KhoangNgay(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 2)));
    }

    @Test
    void chiaO_tuanTheoTuanThuHaiDenChuNhat() {
        var cacKy = KyBaoCao.chiaO(new KyBaoCao.KhoangNgay(
                LocalDate.of(2026, 1, 7), LocalDate.of(2026, 1, 13)), "tuan");

        assertThat(cacKy).containsExactly(
                new KyBaoCao.KhoangNgay(LocalDate.of(2026, 1, 7), LocalDate.of(2026, 1, 11)),
                new KyBaoCao.KhoangNgay(LocalDate.of(2026, 1, 12), LocalDate.of(2026, 1, 13)));
    }

    @Test
    void chiaO_tuChoiDonViKhongHoTro() {
        assertThatThrownBy(() -> KyBaoCao.chiaO(new KyBaoCao.KhoangNgay(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2)), "decade"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}