package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;

import java.util.List;
import java.util.UUID;

/**
 * Tra cứu hợp đồng read-only — dùng cho module khác, tránh phụ thuộc vòng với {@link HopDongService}.
 */
public interface HopDongLookupService {

    HopDongResponse getById(UUID id);

    List<HopDongResponse> listActive();
}
