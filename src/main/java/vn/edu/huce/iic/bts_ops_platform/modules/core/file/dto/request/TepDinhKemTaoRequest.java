    package vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.request;

    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.Data;
    import java.util.UUID;

    @Data
    public class TepDinhKemTaoRequest {
        @Size(max = 100)
private String ma;
@NotBlank
@Size(max = 255)
private String tenTep;
@Size(max = 255)
private String tenTepGoc;
@NotBlank
@Size(max = 1000)
private String duongDan;
@Size(max = 2000)
private String url;
@Size(max = 50)
private String loaiTep;
@Size(max = 100)
private String mimeType;
private Long kichThuoc;
@Size(max = 255)
private String checksum;
private UUID nguoiTaoId;
private Boolean hoatDong = true;
    }
