package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.constants;

import java.util.UUID;

public final class VolumeConstants {
    // GCCC (Gia Cố Củng Cố) loại hợp đồng ID — dùng hệ số nhân với giaTriHd
    public static final UUID GCCC_LOAI_HOP_DONG_ID = UUID.fromString("cfe9839a-3b7e-43d4-bd72-0b90e67e0e00");

    // Xây mới & Còn lại loại hợp đồng ID — dùng ngưỡng từ loai_hop_dong.nguong
    public static final UUID XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID = UUID.fromString("368d1d28-737a-4d1e-b779-05f4375c3065");

    private VolumeConstants() {
    }
}
