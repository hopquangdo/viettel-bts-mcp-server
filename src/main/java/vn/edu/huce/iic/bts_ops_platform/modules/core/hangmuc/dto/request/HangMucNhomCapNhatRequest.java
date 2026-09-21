    package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request;

    import jakarta.validation.constraints.Size;
    import lombok.Data;
    import java.util.UUID;

    @Data
    public class HangMucNhomCapNhatRequest {
        private UUID hopDongId;
@Size(max = 50)
private String ma;
@Size(max = 255)
private String ten;
@Size(max = 2000)
private String moTa;
private Short thuTu;
private Boolean hoatDong;
    }
