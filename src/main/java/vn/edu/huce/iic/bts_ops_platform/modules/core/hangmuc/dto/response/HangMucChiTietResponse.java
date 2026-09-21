        package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response;

        import lombok.Data;
        import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

        @Data
        public class HangMucChiTietResponse {
            private UUID id;
    private UUID hangMucNhomId;
    private String ma;
    private String ten;
    private String donVi;
    private BigDecimal donGia;
    private BigDecimal khoiLuong;
    private String congThucKhoiLuong;
    private String congThucDonGia;
    private String congThucThanhTien;
    private String viTriThiCong;
    private String trangThai;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
        }
