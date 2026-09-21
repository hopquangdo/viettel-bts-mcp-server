        package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request;

        import jakarta.validation.constraints.Size;
        import lombok.Data;
        import java.util.UUID;
import java.math.BigDecimal;

        @Data
        public class HangMucCongViecCapNhatRequest {
            private UUID hangMucChiTietId;
    @Size(max = 50)
    private String ma;
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
    private String trangThai;
        }
