    package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

    import jakarta.validation.constraints.Size;
    import lombok.Data;
    import java.util.UUID;

    @Data
    public class HopDongTepDinhKemCapNhatRequest {
        private UUID hopDongId;
private UUID tepDinhKemId;
@Size(max = 50)
private String loaiTaiLieu;
private String ghiChu;
private Boolean hoatDong;
    }
