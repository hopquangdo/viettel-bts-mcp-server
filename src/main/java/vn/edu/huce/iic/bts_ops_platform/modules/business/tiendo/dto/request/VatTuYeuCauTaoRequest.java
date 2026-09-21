package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class VatTuYeuCauTaoRequest {

    @NotNull
    private UUID hopDongDoiTuongId;

    private String loai;
    private LocalDate ngayYeuCau;
    private String ghiChu;
}
