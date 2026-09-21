package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services;

import java.util.Collection;
import java.util.UUID;

public interface TuyenHangMucProvisioningService {

    void provisionForDoiTuong(UUID hopDongDoiTuongId);

    void provisionBatch(Collection<UUID> hopDongDoiTuongIds);
}
