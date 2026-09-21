package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class HopDongChecklistResponse {
    private UUID hopDongId;
    private String ghiChuHopDong;
    private List<ChecklistMucResponse> mucDanhSach;
}
