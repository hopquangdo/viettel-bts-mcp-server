        package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response;

        import lombok.Data;
        import java.util.UUID;
import java.time.Instant;

        @Data
        public class HangMucNhomResponse {
            private UUID id;
    private UUID hopDongId;
    private String ma;
    private String ten;
    private String moTa;
    private Short thuTu;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
        }
