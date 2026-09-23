package vn.edu.huce.iic.bts_ops_platform.mcp.support;

import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec.NguonViecRowToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec.NguonViecManualRowDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Port thuần Java từ {@code modules.business.nguonluc.helpers.NguonViecTinhToanHelper} — tools
 * không được import helper/service của module nguonluc, nên nhân bản đúng logic tại đây.
 */
public final class NguonViecCalc {

    private static final BigDecimal TY = BigDecimal.valueOf(1_000_000_000L);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private NguonViecCalc() {
    }

    public static double toTy(BigDecimal value) {
        return value == null ? 0 : value.divide(TY, 2, RoundingMode.HALF_UP).doubleValue();
    }

    public static double toTy(Long value) {
        return value == null ? 0 : toTy(BigDecimal.valueOf(value));
    }

    public static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static String mapPhapLyStage(Short trangThaiPhapLy) {
        if (trangThaiPhapLy == null) {
            return "nhap";
        }
        return switch (trangThaiPhapLy.intValue()) {
            case 1 -> "chua_co";
            case 2 -> "da_co";
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
        return instant == null ? "—" : DATE_FMT.format(instant);
    }

    /** row: [ten, giaTri] — trả về giá trị của thuộc tính đầu tiên khớp 1 trong các keyword (không phân biệt hoa/thường). */
    public static String findThuocTinh(List<String[]> attrs, String... keywords) {
        if (attrs == null || attrs.isEmpty()) {
            return "";
        }
        for (String[] attr : attrs) {
            String name = attr[0] != null ? attr[0].toLowerCase(Locale.ROOT) : "";
            for (String keyword : keywords) {
                if (name.contains(keyword.toLowerCase(Locale.ROOT))) {
                    return nullToDash(attr[1]);
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

    public static void applyDerivedMetrics(NguonViecRowToolItem row) {
        double slCon = Math.max(0, row.getGiaTriHD() - row.getSxTong());
        double dtCon = Math.max(0, row.getSxTong() - row.getDtTong());
        double chuaKt = Math.max(0, slCon - dtCon);
        row.setSlConHD(round2(slCon));
        row.setDtConSL(round2(dtCon));
        row.setChuaKhaThi(round2(chuaKt));
    }

    public static String extractTrungTam(List<String[]> attrs) {
        String value = findThuocTinh(attrs, "trung tam", "ttkv", "khu vuc");
        return value.isBlank() ? "TTKV2" : value;
    }

    /** Suy lĩnh vực từ loại hợp đồng (ưu tiên mã/tên loại), fallback tên chương trình. */
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

    /** Suy lĩnh vực từ tên chương trình / nguồn việc — khớp FE normalizeContractType. */
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

    public static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    public static int parseInt(String value) {
        if (value == null || value.isBlank() || "—".equals(value)) {
            return 0;
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public static boolean matchesFilters(NguonViecRowToolItem row, String search, String trangThai, String khuVuc,
                                         String loaiCv, String phapLy, String linhVuc) {
        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase(Locale.ROOT);
            String chuongTrinh = row.getChuongTrinh() != null ? row.getChuongTrinh().toLowerCase(Locale.ROOT) : "";
            String maHd = row.getMaHD() != null ? row.getMaHD().toLowerCase(Locale.ROOT) : "";
            if (!chuongTrinh.contains(q) && !maHd.contains(q)) {
                return false;
            }
        }
        if (trangThai != null && !trangThai.isBlank() && !"all".equalsIgnoreCase(trangThai)
                && !trangThai.equalsIgnoreCase(row.getStatus())) {
            return false;
        }
        if (loaiCv != null && !loaiCv.isBlank() && !loaiCv.equalsIgnoreCase(row.getLoaiCV())) {
            return false;
        }
        if (phapLy != null && !phapLy.isBlank() && !phapLy.equalsIgnoreCase(row.getPhapLyStage())) {
            return false;
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
        if (khuVuc != null && !khuVuc.isBlank() && !"all".equalsIgnoreCase(khuVuc)) {
            String rowTrungTam = row.getTrungTam() != null ? row.getTrungTam().trim() : "";
            if (!khuVuc.trim().equalsIgnoreCase(rowTrungTam)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Port {@code NguonViecTinhToanHelper.fromManualRow} — chuyển 1 hàng "tự thêm" (overlay thủ công
     * lưu trong nguon_viec_bang.du_lieu.manualRows) thành NguonViecRowToolItem, để gộp vào kết quả
     * tổng hợp trả cho AI thay vì bỏ qua như trước.
     */
    public static NguonViecRowToolItem fromManualRow(NguonViecManualRowDto manual, int stt, String defaultTrungTam) {
        NguonViecRowToolItem row = NguonViecRowToolItem.builder()
                .id(manual.getId())
                .stt(stt)
                .chuongTrinh(manual.getChuongTrinh() == null ? "" : manual.getChuongTrinh())
                .maHD(nullToDash(manual.getMaHD()))
                .loaiCV(nullToDash(manual.getLoaiCV()))
                .nhom(nullToDash(manual.getNhom()))
                .phapLyStage(manual.getPhapLyStage() != null ? manual.getPhapLyStage() : "nhap")
                .giaTriHD(round2(manual.getGiaTriHD() != null ? manual.getGiaTriHD() : 0))
                .sxT1(round2(manual.getSxT1() != null ? manual.getSxT1() : 0))
                .sxT2(round2(manual.getSxT2() != null ? manual.getSxT2() : 0))
                .sxTong(round2(manual.getSxTong() != null ? manual.getSxTong() : 0))
                .dtT1(round2(manual.getDtT1() != null ? manual.getDtT1() : 0))
                .dtT2(round2(manual.getDtT2() != null ? manual.getDtT2() : 0))
                .dtTong(round2(manual.getDtTong() != null ? manual.getDtTong() : 0))
                .status(manual.getStatus() != null ? manual.getStatus() : "dang_trien_khai")
                .nhaThau(nullToDash(manual.getNhaThau()))
                .ghiChuKV(manual.getGhiChuKV() == null ? "" : manual.getGhiChuKV())
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

    public static double riskScore(NguonViecRowToolItem row) {
        double score = row.getSlVuong() * 2.0;
        if (row.getGiaTriHD() > 0) {
            double chuaHoanThanh = Math.max(0, 1 - row.getSxTong() / row.getGiaTriHD());
            score += chuaHoanThanh * 10.0;
        }
        if ("vuong_phap_ly".equals(row.getStatus())) {
            score += 5.0;
        }
        return score;
    }
}
