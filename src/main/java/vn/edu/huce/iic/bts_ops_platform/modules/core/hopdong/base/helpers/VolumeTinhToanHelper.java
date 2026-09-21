package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers;

import vn.edu.huce.iic.bts_ops_platform.common.util.TextUtils;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.HopDongObjectGeo;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongGiaTriResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Các hàm tính toán/định dạng thuần túy cho module volume (không phụ thuộc service). */
public final class VolumeTinhToanHelper {

    public static final MathContext MC = new MathContext(12, RoundingMode.HALF_UP);
    public static final BigDecimal DEFAULT_HESO_GCCC = BigDecimal.valueOf(2.0);

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal WARNING_RATIO = BigDecimal.valueOf(90);

    private VolumeTinhToanHelper() {
    }

    /** HĐ → tên đối tượng quản lý phổ biến nhất, tính 1 lần cho toàn batch (O(M)). */
    public static Map<UUID, String> buildObjectTypeByHopDong(List<HopDongDoiTuongResponse> objects) {
        Map<UUID, Map<String, Long>> freq = new HashMap<>();
        for (HopDongDoiTuongResponse o : objects) {
            if (o.getHopDongId() == null) continue;
            String ten = o.getDoiTuongTen();
            if (ten == null || ten.isBlank()) continue;
            freq.computeIfAbsent(o.getHopDongId(), k -> new HashMap<>()).merge(ten, 1L, Long::sum);
        }
        Map<UUID, String> result = new HashMap<>();
        for (Map.Entry<UUID, Map<String, Long>> e : freq.entrySet()) {
            e.getValue().entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .ifPresent(top -> result.put(e.getKey(), top.getKey()));
        }
        return result;
    }

    public static String resolveMaTram(HopDongDoiTuongResponse obj) {
        if (obj == null) return "—";
        if (obj.getGiaTri() != null) {
            for (HopDongDoiTuongGiaTriResponse item : obj.getGiaTri()) {
                String name = normalizeName(item.getTenThuocTinh());
                if (name.contains("ma tram") || name.contains("ma nha tram") || name.contains("ma doi tuong")) {
                    if (item.getGiaTri() != null && !item.getGiaTri().isBlank()) {
                        return item.getGiaTri().trim();
                    }
                }
            }
        }
        if (obj.getDoiTuongMa() != null && !obj.getDoiTuongMa().isBlank()) {
            return obj.getDoiTuongMa().trim();
        }
        return obj.getId() != null ? obj.getId().toString().substring(0, 8) : "—";
    }

    /**
     * Khóa gộp volume theo tỉnh: dùng mã/tên tỉnh (+ mã cũ), không dùng UUID trạm.
     * {@link HopDongObjectGeo#provinceKey()} có thể là id từng trạm khi suy tỉnh từ mã trạm.
     */
    public static String volumeProvinceGroupKey(HopDongObjectGeo geo) {
        String province = geo.province() == null ? "" : geo.province().trim();
        String oldProvince = geo.oldProvince() == null ? "" : geo.oldProvince().trim();
        if (province.isBlank()) {
            return "N/A::" + oldProvince;
        }
        return province + "::" + oldProvince;
    }

    private static String normalizeName(String value) {
        return TextUtils.normalizeLower(value).replace('_', ' ').replaceAll("\\s+", " ").trim();
    }

    public static BigDecimal resolveHeSo(BigDecimal heSo) {
        if (heSo == null) return DEFAULT_HESO_GCCC;
        if (heSo.compareTo(BigDecimal.ZERO) <= 0) return null;
        return heSo;
    }

    public static String resolveProvinceVariant(
            long shortageStations, long surplusStations,
            BigDecimal remaining, BigDecimal usedPercent) {
        if (surplusStations > 0) return "surplus";
        if (shortageStations > 0) return "shortage";
        if (remaining.compareTo(BigDecimal.ZERO) < 0) return "surplus";
        if (usedPercent.compareTo(WARNING_RATIO) >= 0) return "warning";
        // Không coi thiếu chỉ vì còn định mức mà chưa có SL thi công
        return "normal";
    }

    /**
     * Chỉ gắn thiếu/thừa khi trạm đã có sản lượng (SL báo + bổ sung &gt; 0).
     * Chưa thi công → normal.
     */
    public static String resolveTramVariant(
            BigDecimal sanLuongHieuLuc, BigDecimal dinhMuc, boolean batThuong) {
        if (nz(sanLuongHieuLuc).compareTo(BigDecimal.ZERO) <= 0) {
            return "normal";
        }
        if (batThuong) {
            return "surplus";
        }
        if (sanLuongHieuLuc.compareTo(nz(dinhMuc)) < 0) {
            return "shortage";
        }
        if (sanLuongHieuLuc.compareTo(nz(dinhMuc)) > 0) {
            return "surplus";
        }
        return "normal";
    }

    public static String resolveStatus(long giaTriHd, BigDecimal tongThiCong) {
        BigDecimal plan = BigDecimal.valueOf(giaTriHd);
        BigDecimal actual = nz(tongThiCong);
        // Chưa có giá trị/ngưỡng thật để so sánh (HĐ mới tạo, chưa nhập giá trị hoặc chưa cấu
        // hình hệ số ngưỡng) — không đủ căn cứ để báo vượt ngưỡng, tránh cảnh báo giả cho HĐ vừa
        // mới tạo/chưa có dữ liệu thi công.
        if (plan.compareTo(BigDecimal.ZERO) <= 0) {
            return "binh_thuong";
        }
        if (actual.compareTo(plan) > 0) return "vuot_nguong";
        BigDecimal percent = ratio(actual, plan);
        if (percent.compareTo(WARNING_RATIO) >= 0) return "canh_bao";
        return "binh_thuong";
    }

    public static String resolveStatus(BigDecimal nguong, BigDecimal tongThiCong) {
        BigDecimal plan = nz(nguong);
        BigDecimal actual = nz(tongThiCong);
        // Chưa cấu hình ngưỡng (vd. hệ số ngưỡng "Xây mới & Còn lại" chưa được xác nhận) — không
        // đủ căn cứ để báo vượt ngưỡng, tránh cảnh báo giả cho HĐ vừa mới tạo/chưa có dữ liệu thi công.
        if (plan.compareTo(BigDecimal.ZERO) <= 0) {
            return "binh_thuong";
        }
        if (actual.compareTo(plan) > 0) return "vuot_nguong";
        BigDecimal percent = ratio(actual, plan);
        if (percent.compareTo(WARNING_RATIO) >= 0) return "canh_bao";
        return "binh_thuong";
    }

    public static BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return nz(numerator).multiply(HUNDRED, MC).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /** SL hiệu lực để đối soát CL HĐ = SL báo + bổ sung điều chỉnh. */
    public static BigDecimal effectiveSanLuong(BigDecimal sanLuongBao, BigDecimal boSung) {
        return nz(sanLuongBao).add(nz(boSung));
    }
}
