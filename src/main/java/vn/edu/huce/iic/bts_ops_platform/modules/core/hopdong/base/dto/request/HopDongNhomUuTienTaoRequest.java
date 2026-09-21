        package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

        import jakarta.validation.constraints.NotBlank;
        import jakarta.validation.constraints.NotNull;
        import jakarta.validation.constraints.Size;
        import lombok.Data;
        import java.util.UUID;
import java.math.BigDecimal;

        @Data
        public class HopDongNhomUuTienTaoRequest {
            @NotNull
    private UUID hopDongId;
    @NotBlank
    @Size(max = 50)
    private String ma;
    @NotBlank
    @Size(max = 255)
    private String ten;
    @Size(max = 20)
    private String mauSac;
    private Integer keHoach;
    private Integer hoanThanh;
    private Integer conLai;
    private BigDecimal phanTram;
    private Integer soNhan;
    private Short thuTu;
        }
