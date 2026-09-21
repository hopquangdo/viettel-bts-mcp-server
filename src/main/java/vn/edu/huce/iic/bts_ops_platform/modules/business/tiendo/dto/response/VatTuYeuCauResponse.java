package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class VatTuYeuCauResponse {

    private UUID id;
    private UUID hopDongDoiTuongId;
    private String loai;
    private String trangThai;
    private LocalDate ngayYeuCau;
    private LocalDate ngayHoanThanh;
    private String ghiChu;
}
