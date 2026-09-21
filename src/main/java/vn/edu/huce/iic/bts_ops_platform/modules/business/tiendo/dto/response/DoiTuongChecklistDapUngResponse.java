package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DoiTuongChecklistDapUngResponse {
    private UUID hopDongDoiTuongId;
    private List<DoiTuongChecklistMucTrangThaiResponse> mucDanhSach;
}
