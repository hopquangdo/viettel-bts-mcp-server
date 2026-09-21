        package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response;

        import lombok.Data;

import java.time.Instant;
import java.util.UUID;

        @Data
        public class QuyenHanResponse {
            private UUID id;
    private String ma;
    private String ten;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
        }
