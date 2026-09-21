        package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response;

        import lombok.Data;
        import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

        @Data
        public class HangMucCongViecResponse {
            private UUID id;
    private UUID hangMucChiTietId;
    private String ma;
    private String ten;
    private String donVi;
    private BigDecimal donGia;
    private BigDecimal khoiLuong;
    private String congThucKhoiLuong;
    private String congThucDonGia;
    private String congThucThanhTien;
    private String viTriThiCong;
    private Short thuTu;
    private String ghiChu;
    private String trangThai;
    private Instant ngayTao;
    private Instant ngayCapNhat;
        }
