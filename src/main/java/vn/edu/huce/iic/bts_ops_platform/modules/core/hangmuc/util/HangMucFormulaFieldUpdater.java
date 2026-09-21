package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.util;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucChiTietCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucChiTietTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucCongViecCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucCongViecTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucChiTiet;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucCongViec;

import java.math.BigDecimal;

public final class HangMucFormulaFieldUpdater {

    private HangMucFormulaFieldUpdater() {
    }

    public static void apply(HangMucChiTietCapNhatRequest request, HangMucChiTiet entity) {
        applyKhoiLuong(
                request.getCongThucKhoiLuong(),
                request.getKhoiLuong(),
                entity::setCongThucKhoiLuong,
                entity::setKhoiLuong
        );
        applyDonGia(
                request.getCongThucDonGia(),
                request.getDonGia(),
                entity::setCongThucDonGia,
                entity::setDonGia
        );
        applyThanhTien(request.getCongThucThanhTien(), entity::setCongThucThanhTien);
    }

    public static void apply(HangMucChiTietTaoRequest request, HangMucChiTiet entity) {
        applyKhoiLuong(
                request.getCongThucKhoiLuong(),
                request.getKhoiLuong(),
                entity::setCongThucKhoiLuong,
                entity::setKhoiLuong
        );
        applyDonGia(
                request.getCongThucDonGia(),
                request.getDonGia(),
                entity::setCongThucDonGia,
                entity::setDonGia
        );
        applyThanhTien(request.getCongThucThanhTien(), entity::setCongThucThanhTien);
    }

    public static void apply(HangMucCongViecCapNhatRequest request, HangMucCongViec entity) {
        applyKhoiLuong(
                request.getCongThucKhoiLuong(),
                request.getKhoiLuong(),
                entity::setCongThucKhoiLuong,
                entity::setKhoiLuong
        );
        applyDonGia(
                request.getCongThucDonGia(),
                request.getDonGia(),
                entity::setCongThucDonGia,
                entity::setDonGia
        );
        applyThanhTien(request.getCongThucThanhTien(), entity::setCongThucThanhTien);
    }

    public static void apply(HangMucCongViecTaoRequest request, HangMucCongViec entity) {
        applyKhoiLuong(
                request.getCongThucKhoiLuong(),
                request.getKhoiLuong(),
                entity::setCongThucKhoiLuong,
                entity::setKhoiLuong
        );
        applyDonGia(
                request.getCongThucDonGia(),
                request.getDonGia(),
                entity::setCongThucDonGia,
                entity::setDonGia
        );
        applyThanhTien(request.getCongThucThanhTien(), entity::setCongThucThanhTien);
    }

    private static void applyKhoiLuong(
            String congThuc,
            BigDecimal khoiLuong,
            java.util.function.Consumer<String> setFormula,
            java.util.function.Consumer<BigDecimal> setValue
    ) {
        if (congThuc != null) {
            String formula = congThuc.trim();
            if (!formula.isEmpty()) {
                setFormula.accept(formula);
                setValue.accept(null);
                return;
            }
            if (khoiLuong != null) {
                setFormula.accept(null);
                setValue.accept(khoiLuong);
                return;
            }
            setFormula.accept(null);
            setValue.accept(null);
            return;
        }
        if (khoiLuong != null) {
            setValue.accept(khoiLuong);
            setFormula.accept(null);
        }
    }

    private static void applyDonGia(
            String congThuc,
            BigDecimal donGia,
            java.util.function.Consumer<String> setFormula,
            java.util.function.Consumer<BigDecimal> setValue
    ) {
        if (congThuc != null) {
            String formula = congThuc.trim();
            if (!formula.isEmpty()) {
                setFormula.accept(formula);
                setValue.accept(BigDecimal.ZERO);
                return;
            }
            if (donGia != null) {
                setFormula.accept(null);
                setValue.accept(donGia);
                return;
            }
            setFormula.accept(null);
            setValue.accept(BigDecimal.ZERO);
            return;
        }
        if (donGia != null) {
            setValue.accept(donGia);
            setFormula.accept(null);
        }
    }

    private static void applyThanhTien(String congThuc, java.util.function.Consumer<String> setFormula) {
        if (congThuc == null) {
            return;
        }
        String formula = congThuc.trim();
        setFormula.accept(formula.isEmpty() ? null : formula);
    }
}
