package vn.edu.huce.iic.bts_ops_platform.modules.core.file.services;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.request.TepDinhKemCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.request.TepDinhKemTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.response.TepDinhKemResponse;

import java.util.List;
import java.util.UUID;

public interface TepDinhKemService {
    TepDinhKemResponse getById(UUID id);

    /** Lấy hàng loạt theo id — tránh N+1 khi caller cần map nhiều tệp cùng lúc (vd enrich list). */
    java.util.Map<UUID, TepDinhKemResponse> getByIds(java.util.Collection<UUID> ids);
    TepDinhKemResponse create(TepDinhKemTaoRequest request);
    TepDinhKemResponse upload(MultipartFile file, String category);
    TepDinhKemResponse update(UUID id, TepDinhKemCapNhatRequest request);
    void delete(UUID id);
}
