package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuDuLieu;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuDuLieuRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class LienKetBangSupport {

    public static final Set<String> ALLOWED_LINK_TABLES = Set.of(
            "tinh_thanh",
            "chu_dau_tu",
            "khu_vuc",
            "loai_hop_dong",
            "kieu_hop_dong",
            "trang_thai_hop_dong",
            "nguoi_dung",
            "hop_dong");

    private LienKetBangSupport() {
    }

    public static boolean isKnownLinkTable(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return ALLOWED_LINK_TABLES.contains(value.trim());
    }

    public static String resolveLienKetBang(String kieuDuLieuId, KieuDuLieuRepository kieuDuLieuRepository) {
        if (kieuDuLieuId == null || kieuDuLieuId.isBlank()) {
            return null;
        }
        String normalized = kieuDuLieuId.trim();
        if (isKnownLinkTable(normalized)) {
            return normalized;
        }
        return kieuDuLieuRepository.findByTenAndNgayXoaIsNull(normalized)
                .map(KieuDuLieu::getLienKetBang)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .filter(LienKetBangSupport::isKnownLinkTable)
                .orElse(null);
    }

    /**
     * Như {@link #resolveLienKetBang(String, KieuDuLieuRepository)} nhưng tra trong map đã nạp sẵn.
     * <p>
     * Bản dùng repository bắn một query cho MỖI thuộc tính; khi map cả một danh sách thì thành
     * N+1 xuống {@code kieu_du_lieu} — bảng chỉ có 7 dòng mà mỗi query mất ~50ms. Caller nạp map
     * một lần bằng {@link #buildLienKetBangByTen(Collection)} rồi truyền vào đây.
     * <p>
     * Chuỗi xử lý giữ nguyên: trim → nếu đã là bảng hợp lệ thì trả luôn → tra theo {@code ten} →
     * bỏ giá trị rỗng → trim → chỉ nhận bảng nằm trong {@link #ALLOWED_LINK_TABLES}.
     */
    public static String resolveLienKetBang(String kieuDuLieuId, Map<String, String> lienKetBangByTen) {
        if (kieuDuLieuId == null || kieuDuLieuId.isBlank()) {
            return null;
        }
        String normalized = kieuDuLieuId.trim();
        if (isKnownLinkTable(normalized)) {
            return normalized;
        }
        if (lienKetBangByTen == null) {
            return null;
        }
        String lienKetBang = lienKetBangByTen.get(normalized);
        if (lienKetBang == null || lienKetBang.isBlank()) {
            return null;
        }
        String trimmed = lienKetBang.trim();
        return isKnownLinkTable(trimmed) ? trimmed : null;
    }

    /**
     * Dựng map {@code ten -> lienKetBang} từ các bản ghi kieu_du_lieu chưa xoá mềm.
     * Trùng {@code ten} thì giữ bản gặp trước — khớp hành vi findByTenAndNgayXoaIsNull (trả
     * Optional, tức là dữ liệu vốn không có trùng ten).
     */
    public static Map<String, String> buildLienKetBangByTen(Collection<KieuDuLieu> kieuDuLieus) {
        if (kieuDuLieus == null || kieuDuLieus.isEmpty()) {
            return Map.of();
        }
        Map<String, String> result = new HashMap<>();
        for (KieuDuLieu kieuDuLieu : kieuDuLieus) {
            if (kieuDuLieu == null || kieuDuLieu.getTen() == null) {
                continue;
            }
            result.putIfAbsent(kieuDuLieu.getTen(), kieuDuLieu.getLienKetBang());
        }
        return result;
    }
}
