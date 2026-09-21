    package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request;

    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.Data;


    @Data
    public class QuyenTaoRequest {
        @NotBlank
@Size(max = 100)
private String ma;
@NotBlank
@Size(max = 255)
private String ten;
private Boolean hoatDong = true;
    }
