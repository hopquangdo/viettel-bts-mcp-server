package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class VuongMacTaoRequest {
    @Size(max = 50)
    private String ma;
    private UUID duLieuDoiTuongId;
    private UUID hopDongId;
    @NotBlank
    @Size(max = 100)
    private String giaiDoan;
    @NotBlank
    @Size(max = 50)
    private String kieuVuongMac;
    /** User chọn khi ghi nhận — có được tiếp tục bổ sung sản lượng khi VM còn mở. */
    private Boolean coTheBoSungSanLuong;
    @NotBlank
    private String moTa;
    private String moTaDayDu;
    private UUID nguoiBaoCaoId;
    @Size(max = 255)
    private String tenNguoiBaoCao;
    @Size(max = 20)
    private String trangThai = "pending";
    private UUID nguoiXuLyId;
    private String ghiChuGiaiQuyet;
    private String lyDoTuChoi;
    private UUID tepDinhKemId;
    private Boolean hoatDong = true;
}
