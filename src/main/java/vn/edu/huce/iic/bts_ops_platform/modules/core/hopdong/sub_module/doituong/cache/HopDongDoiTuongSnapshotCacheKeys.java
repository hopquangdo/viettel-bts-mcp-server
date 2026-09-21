package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.cache;

import java.util.UUID;

public final class HopDongDoiTuongSnapshotCacheKeys {

    public static String key(UUID hopDongDoiTuongId) {
        return hopDongDoiTuongId.toString();
    }

    private HopDongDoiTuongSnapshotCacheKeys() {
    }
}
