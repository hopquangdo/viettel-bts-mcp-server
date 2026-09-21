    package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.Data;
    import java.util.UUID;

    @Data
    public class HopDongTepDinhKemTaoRequest {
        @NotNull
private UUID hopDongId;
@NotNull
private UUID tepDinhKemId;
@Size(max = 50)
private String loaiTaiLieu;
private String ghiChu;
private Boolean hoatDong = true;
    }
