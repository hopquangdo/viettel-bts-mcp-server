package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.constants;

import java.util.List;
import java.util.Map;

public final class TramThiCongBuocConstants {

    private TramThiCongBuocConstants() {}

    public static final String CHUA_CO = "chua_co";
    public static final String CHO_DUYET = "cho_duyet";
    public static final String DA_DUYET = "da_duyet";
    public static final String TU_CHOI = "tu_choi";

    public static final String BAN_GIAO_MAT_BANG = "BAN_GIAO_MAT_BANG";
    public static final String VAT_TU = "VAT_TU";
    public static final String PHAT_SINH_SAU_TC = "PHAT_SINH_SAU_TC";
    public static final String YEU_CAU_NGHIEM_THU = "YEU_CAU_NGHIEM_THU";

    public static final List<String> STEP_ORDER = List.of(
            BAN_GIAO_MAT_BANG, VAT_TU, PHAT_SINH_SAU_TC, YEU_CAU_NGHIEM_THU);

    public static final Map<String, String> STEP_LABELS = Map.of(
            BAN_GIAO_MAT_BANG, "Biên bản bàn giao mặt bằng",
            VAT_TU, "Biên bản yêu cầu cung cấp / nhận vật tư",
            PHAT_SINH_SAU_TC, "Biên bản phát sinh sau thi công",
            YEU_CAU_NGHIEM_THU, "Biên bản yêu cầu nghiệm thu");

    public static final Map<String, List<String>> STEP_DANH_MUC = Map.of(
            BAN_GIAO_MAT_BANG, List.of("BAN_GIAO_MAT_BANG"),
            VAT_TU, List.of("YEU_CAU_VAT_TU", "NHAN_VAT_TU"),
            YEU_CAU_NGHIEM_THU, List.of("YEU_CAU_NGHIEM_THU"));
}
