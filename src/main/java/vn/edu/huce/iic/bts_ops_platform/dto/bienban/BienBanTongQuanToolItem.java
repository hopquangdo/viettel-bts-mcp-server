package vn.edu.huce.iic.bts_ops_platform.dto.bienban;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** Tổng quan số lượng biên bản theo trạng thái duyệt + theo loại biên bản — trả về cho bienban_tool. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BienBanTongQuanToolItem {
    private long total;
    private long choDuyet;
    private long daDuyet;
    private long tuChoi;
    /** Đếm theo loai_bien_ban: BIEN_BAN_SO_1 | NHAT_KY_THI_CONG | BIEN_BAN_SO_2 | BAO_CAO_KHAO_SAT ... */
    private Map<String, Long> countsByLoai;
}
