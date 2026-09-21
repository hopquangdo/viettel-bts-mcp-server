package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class HopDongDoiTuongTrangThaiTaoRequest {
    private UUID kieuHopDongId;
    private UUID loaiHopDongId;
    private UUID luongTrangThaiId;
    private UUID doiTuongId;
    private UUID trangThaiId;
    @NotBlank
    @Size(max = 255)
    private String nhanHienThi;
    @Size(max = 20)
    private String mauSac;
    @Size(max = 20)
    private String mauNen;
    @Size(max = 50)
    private String giaTriLoc;
    private Short thuTu;
    private Boolean ghiDe;
    private Boolean hoatDong = true;
}
