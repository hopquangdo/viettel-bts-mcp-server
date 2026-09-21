        package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response;

        import lombok.Data;
        import java.util.UUID;
import java.time.Instant;

        @Data
        public class KieuHopDongResponse {
            private UUID id;
    private UUID loaiHopDongId;
    private String ma;
    private String ten;
    private String nhom;
    private String mauSac;
    private String moTa;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
        }
