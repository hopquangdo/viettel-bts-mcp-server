        package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response;

        import lombok.Data;

import java.time.Instant;
import java.util.UUID;

        @Data
        public class KieuDuLieuResponse {
            private UUID id;
    private String ten;
    private String lienKetBang;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
        }
