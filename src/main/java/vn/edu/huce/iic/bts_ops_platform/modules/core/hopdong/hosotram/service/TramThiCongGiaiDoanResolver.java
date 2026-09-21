package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.entity.BienBan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.constants.TramThiCongGiaiDoanConstants;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.entity.BienBanPhatSinh;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.constants.TramThiCongGiaiDoanConstants.*;

/**
 * Suy ra giai đoạn trạm từ biên bản thi công + phát sinh.
 * Quy tắc: Chưa = chưa xuất (chỉ bước đầu) | Đang = đã xuất/lập, chưa duyệt | Đã = đã duyệt.
 */
@Component
@RequiredArgsConstructor
public class TramThiCongGiaiDoanResolver {

    private static final String LOAI_BGM = "BAN_GIAO_MAT_BANG";
    private static final String LOAI_YCVT = "YEU_CAU_VAT_TU";
    private static final String LOAI_NVT = "NHAN_VAT_TU";
    private static final String LOAI_PS = "PHAT_SINH";
    private static final String LOAI_NT = "YEU_CAU_NGHIEM_THU";

    private final ObjectMapper objectMapper;

    public String resolve(UUID doiTuongId, List<BienBan> bienBanRows, List<BienBanPhatSinh> phatSinhRows) {
        BienBan bgm = latestBienBan(doiTuongId, bienBanRows, LOAI_BGM);
        BienBan ycvt = latestBienBan(doiTuongId, bienBanRows, LOAI_YCVT);
        BienBan nvt = latestBienBan(doiTuongId, bienBanRows, LOAI_NVT);

        if (nvt != null && "da_duyet".equals(nvt.getTrangThai())) {
            return resolveSauVatTu(doiTuongId, phatSinhRows);
        }

        if (isPending(nvt) || isPending(ycvt) || hasVatTuInProgress(ycvt, nvt)) {
            return DANG_CUNG_CAP_VAT_TU;
        }

        if (bgm != null && "da_duyet".equals(bgm.getTrangThai())) {
            return DA_BAN_GIAO_MB;
        }
        if (isPending(bgm)) {
            return DANG_BAN_GIAO_MB;
        }

        return CHUA_BAN_GIAO_MB;
    }

    /** Chỉ xét phát sinh / nghiệm thu sau khi đã duyệt xong biên bản nhận vật tư. */
    private String resolveSauVatTu(UUID doiTuongId, List<BienBanPhatSinh> phatSinhRows) {
        BienBanPhatSinh nghiemThu = latestPhatSinh(doiTuongId, phatSinhRows, LOAI_NT);
        if (nghiemThu != null) {
            if ("da_duyet".equals(nghiemThu.getTrangThai())) {
                return DA_YEU_CAU_NGHIEM_THU;
            }
            if ("cho_duyet".equals(nghiemThu.getTrangThai()) || "tu_choi".equals(nghiemThu.getTrangThai())) {
                return DANG_YEU_CAU_NGHIEM_THU;
            }
        }

        BienBanPhatSinh phatSinh = latestPhatSinh(doiTuongId, phatSinhRows, LOAI_PS);
        if (phatSinh != null) {
            if ("da_duyet".equals(phatSinh.getTrangThai())) {
                return DA_DUYET_PHAT_SINH;
            }
            if ("cho_duyet".equals(phatSinh.getTrangThai()) || "tu_choi".equals(phatSinh.getTrangThai())) {
                return DANG_LAP_PHAT_SINH;
            }
        }

        return DA_CUNG_CAP_VAT_TU;
    }

    private static boolean isPending(BienBan record) {
        return record != null
                && ("cho_duyet".equals(record.getTrangThai()) || "tu_choi".equals(record.getTrangThai()));
    }

    private static boolean hasVatTuInProgress(BienBan ycvt, BienBan nvt) {
        if (ycvt != null && "da_duyet".equals(ycvt.getTrangThai()) && (nvt == null || !"da_duyet".equals(nvt.getTrangThai()))) {
            return true;
        }
        return false;
    }

    private BienBan latestBienBan(UUID doiTuongId, List<BienBan> rows, String loai) {
        return rows.stream()
                .filter(row -> loai.equals(row.getLoaiBienBan()))
                .filter(row -> containsDoiTuong(row.getHopDongDoiTuongIdsJson(), doiTuongId))
                .max(Comparator.comparing(BienBan::getNgayTao, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    private BienBanPhatSinh latestPhatSinh(UUID doiTuongId, List<BienBanPhatSinh> rows, String loai) {
        return rows.stream()
                .filter(row -> loai.equals(normalizePhatSinhLoai(row.getLoai())))
                .filter(row -> doiTuongId == null
                        ? row.getHopDongDoiTuongId() == null
                        : doiTuongId.equals(row.getHopDongDoiTuongId()))
                .max(Comparator.comparing(BienBanPhatSinh::getNgayTao, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    private static String normalizePhatSinhLoai(String loai) {
        return loai == null || loai.isBlank() ? LOAI_PS : loai;
    }

    private boolean containsDoiTuong(String json, UUID doiTuongId) {
        if (doiTuongId == null || json == null || json.isBlank()) {
            return false;
        }
        try {
            List<UUID> ids = objectMapper.readValue(json, new TypeReference<List<UUID>>() {
            });
            return ids.contains(doiTuongId);
        } catch (Exception e) {
            return false;
        }
    }
}
