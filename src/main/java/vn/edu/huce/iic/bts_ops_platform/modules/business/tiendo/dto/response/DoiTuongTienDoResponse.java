package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class DoiTuongTienDoResponse {

    private UUID id;
    private UUID hopDongId;
    private UUID doiTuongQuanLyId;
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
    private Boolean danhSachKhoa;
    private LocalDate ngayHtTc;
    private LocalDate ngayThiCongGanNhat;
}
