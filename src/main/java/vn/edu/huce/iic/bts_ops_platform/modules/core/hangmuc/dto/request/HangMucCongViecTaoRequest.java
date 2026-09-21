        package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request;

        import jakarta.validation.constraints.NotBlank;
        import jakarta.validation.constraints.NotNull;
        import jakarta.validation.constraints.Size;
        import lombok.Data;
        import java.util.UUID;
import java.math.BigDecimal;

        @Data
        public class HangMucCongViecTaoRequest {
            @NotNull
    private UUID hangMucChiTietId;
    @NotBlank
    @Size(max = 50)
    private String ma;
    @NotBlank
    @Size(max = 500)
    private String ten;
    @Size(max = 50)
    private String donVi;
    private BigDecimal donGia;
    private BigDecimal khoiLuong;
    private String congThucKhoiLuong;
    private String congThucDonGia;
    private String congThucThanhTien;
    @Size(max = 255)
    private String viTriThiCong;
    private Short thuTu;
    private String ghiChu;
    @Size(max = 50)
    private String trangThai = "pending";
        }
