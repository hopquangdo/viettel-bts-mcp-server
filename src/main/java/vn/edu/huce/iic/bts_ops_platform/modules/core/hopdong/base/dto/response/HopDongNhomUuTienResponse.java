        package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

        import lombok.Data;
        import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

        @Data
        public class HopDongNhomUuTienResponse {
            private UUID id;
    private UUID hopDongId;
    private String ma;
    private String ten;
    private String mauSac;
    private Integer keHoach;
    private Integer hoanThanh;
    private Integer conLai;
    private BigDecimal phanTram;
    private Integer soNhan;
    private Short thuTu;
    private Instant ngayTao;
        }
