    package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.Data;
    import java.util.UUID;

    @Data
    public class HopDongDoiTuongGiaTriTaoRequest {
        @NotNull
private UUID hopDongDoiTuongId;
@NotNull
private UUID thuocTinhId;
@Size(max = 255)
private String giaTri;
private Boolean hoatDong = true;
    }
