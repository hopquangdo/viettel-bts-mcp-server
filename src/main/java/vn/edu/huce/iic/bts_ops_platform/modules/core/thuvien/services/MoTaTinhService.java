package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.MoTaTinhRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.MoTaTinhResponse;

import java.util.List;
import java.util.UUID;

public interface MoTaTinhService {

    /** Danh sách tất cả tỉnh/thành đang hoạt động, kèm mô tả nếu đã có người nhập. */
    List<MoTaTinhResponse> list();

    MoTaTinhResponse upsert(UUID tinhThanhId, MoTaTinhRequest request);
}
