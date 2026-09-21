    package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request;

    import jakarta.validation.constraints.Size;
    import lombok.Data;


    @Data
    public class QuyenHanCapNhatRequest {
        @Size(max = 100)
private String ma;
@Size(max = 255)
private String ten;
private Boolean hoatDong;
    }
