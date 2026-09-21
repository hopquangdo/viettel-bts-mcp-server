package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class DoiTuongTienDoCapNhatRequest {

    private LocalDate ngayBanGiaoMatBang;
    private String trangThaiVcontractBgm;
    private LocalDate ngayYeuCauVatTuB;
    private LocalDate ngayHoanThanhVatTuB;
    private String trangThaiVatTuA;
    private String trangThaiVatTuB;
    private Boolean coPhatSinh;
    private String trangThaiNghiemThu;
    private LocalDate ngayNghiemThu;
    private Boolean daChotTham;
    private BigDecimal giaTriChotTham;
    private Boolean daGiaoThauPhu;
    private Boolean daKyHdThauPhu;
}
