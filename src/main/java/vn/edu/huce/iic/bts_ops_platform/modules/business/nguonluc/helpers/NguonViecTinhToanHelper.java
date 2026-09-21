package vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.helpers;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.NguonViecManualRowDto;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.response.NguonViecRowResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.response.NguonViecSummaryResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.response.NguonViecTabCountsResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class NguonViecTinhToanHelper {

    private static final BigDecimal TY = BigDecimal.valueOf(1_000_000_000L);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private NguonViecTinhToanHelper() {
    }

    public static double toTy(BigDecimal value) {
        if (value == null) {
            return 0;
        }
        return value.divide(TY, 2, RoundingMode.HALF_UP).doubleValue();
    }

    public static double toTy(Long value) {
        if (value == null) {
            return 0;
        }
        return toTy(BigDecimal.valueOf(value));
    }

    public static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static String mapPhapLyStage(Short trangThaiPhapLy) {
        if (trangThaiPhapLy == null) {
            return "nhap";
        }
        return switch (trangThaiPhapLy.intValue()) {
            case 0 -> "nhap";
            case 1 -> "chua_co";
            case 2 -> "da_co";
            default -> "nhap";
        };
    }

    /** Chuẩn hóa mã stage (kể cả legacy) để lọc/hiển thị thống nhất với HĐ. */
    public static String normalizePhapLyStage(String stage) {
        if (stage == null || stage.isBlank()) {
            return "nhap";
        }
        return switch (stage.trim().toLowerCase(Locale.ROOT)) {
            case "da_co" -> "da_co";
            case "chua_co" -> "chua_co";
            case "nhap" -> "nhap";
            case "dang_thuc_hien" -> "chua_co";
            case "dang_xuc_tien" -> "nhap";
            default -> "nhap";
        };
    }

    public static String resolveStatus(double giaTriHd, double sxTong, boolean hasOpenIssue) {
        if (hasOpenIssue) {
            return "vuong_phap_ly";
        }
        if (giaTriHd <= 0) {
            return "dang_trien_khai";
        }
        double pct = sxTong / giaTriHd;
        if (pct >= 1) {
            return "da_hoan_thanh";
        }
        if (pct >= 0.9) {
            return "gan_hoan_thanh";
        }
        return "dang_trien_khai";
    }

    public static String formatInstant(Instant instant) {
        if (instant == null) {
            return "—";
        }
        return DATE_FMT.format(instant);
    }

    public static String findThuocTinh(List<HopDongThuocTinhResponse> attrs, String... keywords) {
        if (attrs == null || attrs.isEmpty()) {
            return "";
        }
        for (HopDongThuocTinhResponse attr : attrs) {
            String name = attr.getTenThuocTinh() != null
                    ? attr.getTenThuocTinh().toLowerCase(Locale.ROOT)
                    : "";
            for (String keyword : keywords) {
                if (name.contains(keyword.toLowerCase(Locale.ROOT))) {
                    return nullToDash(attr.getGiaTri());
                }
            }
        }
        return "";
    }

    public static String resolveLoaiCv(String kieuMa) {
        if (kieuMa == null || kieuMa.isBlank()) {
            return "TK";
        }
        String normalized = kieuMa.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("KSTK") || normalized.contains("KHAO SAT")) {
            return "KSTK";
        }
        if (normalized.contains("TC") || normalized.contains("THI CONG")) {
            return "TC";
        }
        return "TK";
    }

    public static void applyDerivedMetrics(NguonViecRowResponse row) {
        double slCon = Math.max(0, row.getGiaTriHD() - row.getSxTong());
        double dtCon = Math.max(0, row.getSxTong() - row.getDtTong());
        double chuaKt = Math.max(0, slCon - dtCon);
        row.setSlConHD(round2(slCon));
        row.setDtConSL(round2(dtCon));
        row.setChuaKhaThi(round2(chuaKt));
    }

    public static NguonViecRowResponse fromManualRow(NguonViecManualRowDto manual, int stt, String defaultTrungTam) {
        NguonViecRowResponse row = NguonViecRowResponse.builder()
                .id(manual.getId())
                .stt(stt)
                .chuongTrinh(nullToEmpty(manual.getChuongTrinh()))
                .maHD(nullToDash(manual.getMaHD()))
                .loaiCV(nullToDash(manual.getLoaiCV()))
                .nhom(nullToDash(manual.getNhom()))
                .phapLyStage(normalizePhapLyStage(manual.getPhapLyStage()))
                .giaTriHD(round2(manual.getGiaTriHD() != null ? manual.getGiaTriHD() : 0))
                .sxT1(round2(manual.getSxT1() != null ? manual.getSxT1() : 0))
                .sxT2(round2(manual.getSxT2() != null ? manual.getSxT2() : 0))
                .sxTong(round2(manual.getSxTong() != null ? manual.getSxTong() : 0))
                .dtT1(round2(manual.getDtT1() != null ? manual.getDtT1() : 0))
                .dtT2(round2(manual.getDtT2() != null ? manual.getDtT2() : 0))
                .dtTong(round2(manual.getDtTong() != null ? manual.getDtTong() : 0))
                .slConHD(round2(manual.getSlConHD() != null ? manual.getSlConHD() : 0))
                .dtConSL(round2(manual.getDtConSL() != null ? manual.getDtConSL() : 0))
                .chuaKhaThi(round2(manual.getChuaKhaThi() != null ? manual.getChuaKhaThi() : 0))
                .status(manual.getStatus() != null ? manual.getStatus() : "dang_trien_khai")
                .nhaThau(nullToDash(manual.getNhaThau()))
                .ghiChuKV(nullToEmpty(manual.getGhiChuKV()))
                .trungTam(manual.getTrungTam() != null ? manual.getTrungTam() : defaultTrungTam)
                .ngayKyHD(nullToDash(manual.getNgayKyHD()))
                .ngayKetThuc(nullToDash(manual.getNgayKetThuc()))
                .ngayCapNhat(nullToDash(manual.getNgayCapNhat()))
                .soDoiKS(manual.getSoDoiKS() != null ? manual.getSoDoiKS() : 0)
                .soDoiTC(manual.getSoDoiTC() != null ? manual.getSoDoiTC() : 0)
                .slHuy(0)
                .slVuong(0)
                .manual(true)
                .build();
        applyDerivedMetrics(row);
        return row;
    }

    public static NguonViecSummaryResponse buildSummary(List<NguonViecRowResponse> rows) {
        double giaTri = 0;
        double sx = 0;
        double dt = 0;
        double dtCon = 0;
        double chuaKt = 0;
        double slCon = 0;
        for (NguonViecRowResponse row : rows) {
            giaTri += row.getGiaTriHD();
            sx += row.getSxTong();
            dt += row.getDtTong();
            dtCon += row.getDtConSL();
            chuaKt += row.getChuaKhaThi();
            slCon += row.getSlConHD();
        }
        return NguonViecSummaryResponse.builder()
                .giaTriHD(round2(giaTri))
                .sxTong(round2(sx))
                .dtTong(round2(dt))
                .dtConSL(round2(dtCon))
                .chuaKhaThi(round2(chuaKt))
                .slConHD(round2(slCon))
                .build();
    }

    public static NguonViecTabCountsResponse buildTabCounts(List<NguonViecRowResponse> rows) {
        long active = rows.stream()
                .filter(row -> "dang_trien_khai".equals(row.getStatus()) || "vuong_phap_ly".equals(row.getStatus()))
                .count();
        long nearly = rows.stream().filter(row -> "gan_hoan_thanh".equals(row.getStatus())).count();
        return NguonViecTabCountsResponse.builder()
                .all(rows.size())
                .active(active)
                .nearly(nearly)
                .build();
    }

    public static boolean matchesFilters(
            NguonViecRowResponse row,
            String search,
            String loaiCv,
            String trangThai,
            String phapLy,
            String nhanSu,
            String linhVuc,
            String trungTam,
            String tab) {
        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase(Locale.ROOT);
            String chuongTrinh = row.getChuongTrinh() != null ? row.getChuongTrinh().toLowerCase(Locale.ROOT) : "";
            String maHd = row.getMaHD() != null ? row.getMaHD().toLowerCase(Locale.ROOT) : "";
            if (!chuongTrinh.contains(q) && !maHd.contains(q)) {
                return false;
            }
        }
        if (loaiCv != null && !loaiCv.isBlank() && !"all".equalsIgnoreCase(loaiCv)
                && !loaiCv.equalsIgnoreCase(row.getLoaiCV())) {
            return false;
        }
        if (trangThai != null && !trangThai.isBlank() && !"all".equalsIgnoreCase(trangThai)
                && !trangThai.equalsIgnoreCase(row.getStatus())) {
            return false;
        }
        if (phapLy != null && !phapLy.isBlank() && !"all".equalsIgnoreCase(phapLy)) {
            String normalizedRow = normalizePhapLyStage(row.getPhapLyStage());
            String normalizedFilter = normalizePhapLyStage(phapLy);
            if (!normalizedFilter.equalsIgnoreCase(normalizedRow)) {
                return false;
            }
        }
        if (nhanSu != null && !nhanSu.isBlank() && !"all".equalsIgnoreCase(nhanSu)) {
            String rowNhaThau = row.getNhaThau() != null ? row.getNhaThau().trim() : "";
            if (!nhanSu.trim().equalsIgnoreCase(rowNhaThau)) {
                return false;
            }
        }
        if (linhVuc != null && !linhVuc.isBlank() && !"all".equalsIgnoreCase(linhVuc)) {
            String rowLinhVuc = row.getLinhVuc();
            if (rowLinhVuc == null || rowLinhVuc.isBlank()) {
                rowLinhVuc = resolveLinhVuc(row.getChuongTrinh());
            }
            if (rowLinhVuc.isBlank() || !linhVuc.trim().equalsIgnoreCase(rowLinhVuc)) {
                return false;
            }
        }
        if (trungTam != null && !trungTam.isBlank() && !"all".equalsIgnoreCase(trungTam)) {
            String rowTrungTam = row.getTrungTam() != null ? row.getTrungTam().trim() : "";
            if (!trungTam.trim().equalsIgnoreCase(rowTrungTam)) {
                return false;
            }
        }
        if ("active".equalsIgnoreCase(tab)
                && !"dang_trien_khai".equals(row.getStatus())
                && !"vuong_phap_ly".equals(row.getStatus())) {
            return false;
        }
        if ("nearly".equalsIgnoreCase(tab) && !"gan_hoan_thanh".equals(row.getStatus())) {
            return false;
        }
        return true;
    }

    public static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    public static String extractTrungTam(HopDongResponse hopDong) {
        String value = findThuocTinh(hopDong.getThuocTinhGiaTri(), "trung tam", "ttkv", "khu vuc");
        return value.isBlank() ? "TTKV2" : value;
    }

    /**
     * Suy lĩnh vực từ loại hợp đồng (ưu tiên mã/tên loại), fallback tên chương trình.
     */
    public static String resolveLinhVucFromLoai(String loaiMa, String loaiTen, String heNghiepVu) {
        if (loaiMa != null && !loaiMa.isBlank()) {
            String fromMa = resolveLinhVuc(loaiMa);
            if (!fromMa.isBlank()) {
                return fromMa;
            }
        }
        if (loaiTen != null && !loaiTen.isBlank()) {
            String fromTen = resolveLinhVuc(loaiTen);
            if (!fromTen.isBlank()) {
                return fromTen;
            }
        }
        if (heNghiepVu != null && heNghiepVu.equalsIgnoreCase("tu_van_thiet_ke")) {
            return "tu-van";
        }
        return "";
    }

    /**
     * Suy lĩnh vực từ tên chương trình / nguồn việc — khớp FE normalizeContractType.
     */
    public static String resolveLinhVuc(String chuongTrinh) {
        if (chuongTrinh == null || chuongTrinh.isBlank()) {
            return "";
        }
        String normalized = removeDiacritics(chuongTrinh).toLowerCase(Locale.ROOT).trim();
        if ("tong".equals(normalized)) {
            return "tong";
        }
        if (normalized.equals("tu van") || normalized.contains("tu van")) {
            return "tu-van";
        }
        if (normalized.contains("kiem dinh")) {
            return "kiem-dinh";
        }
        if ("gphtvt".equals(normalized) || normalized.contains("giai phap htvt")) {
            return "gphtvt";
        }
        if (normalized.contains("do luong")) {
            return "do-luong";
        }
        if ("ict".equals(normalized)) {
            return "ict";
        }
        if (normalized.contains("dan dung")) {
            return "dan-dung";
        }
        return "";
    }

    private static String removeDiacritics(String value) {
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    public static Map<String, BigDecimal> toAmountMap(List<Object[]> rows) {
        java.util.HashMap<String, BigDecimal> result = new java.util.HashMap<>();
        for (Object[] row : rows) {
            if (row[0] == null) {
                continue;
            }
            BigDecimal amount = row[1] instanceof BigDecimal bigDecimal
                    ? bigDecimal
                    : BigDecimal.valueOf(((Number) row[1]).doubleValue());
            result.put(row[0].toString(), amount);
        }
        return result;
    }
}
