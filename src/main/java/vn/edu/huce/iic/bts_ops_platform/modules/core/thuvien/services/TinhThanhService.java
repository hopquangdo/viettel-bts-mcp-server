package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.TinhThanhCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.TinhThanhNhomCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.TinhThanhNhomTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.TinhThanhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.TinhThanhNhomResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.TinhThanhResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TinhThanhService {

    /**
     * Bulk lookup mã tỉnh/thành theo id — khóa bằng cả PK ({@code id}) lẫn
     * {@code tinhThanhId} nghiệp vụ, để các module khác hiển thị mã tỉnh mà
     * không phải tiêm TinhThanhRepository trực tiếp.
     */
    Map<UUID, String> buildProvinceCodeLookup();

    List<TinhThanhResponse> list(String search, UUID khuVucId, Boolean activeOnly);

    TinhThanhResponse getById(UUID id);

    TinhThanhResponse create(TinhThanhTaoRequest request);

    TinhThanhResponse update(UUID id, TinhThanhCapNhatRequest request);

    TinhThanhNhomResponse createGroup(TinhThanhNhomTaoRequest request);

    TinhThanhNhomResponse updateGroup(UUID tinhThanhId, TinhThanhNhomCapNhatRequest request);

    void deleteGroup(UUID tinhThanhId);

    void delete(UUID id);
}
