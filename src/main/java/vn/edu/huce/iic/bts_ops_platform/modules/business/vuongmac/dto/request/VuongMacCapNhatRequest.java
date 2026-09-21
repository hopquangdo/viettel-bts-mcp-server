package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class VuongMacCapNhatRequest {
    @Size(max = 50)
    private String ma;
    private UUID duLieuDoiTuongId;
    private UUID hopDongId;
    @Size(max = 100)
    private String giaiDoan;
    private String kieuVuongMac;
    private Boolean coTheBoSungSanLuong;
    private String moTa;
    private String moTaDayDu;
    private UUID nguoiBaoCaoId;
    @Size(max = 255)
    private String tenNguoiBaoCao;
    @Size(max = 20)
    private String trangThai;
    private UUID nguoiXuLyId;
    private String ghiChuGiaiQuyet;
    private String lyDoTuChoi;
    private UUID tepDinhKemId;
    private Boolean hoatDong;
}
