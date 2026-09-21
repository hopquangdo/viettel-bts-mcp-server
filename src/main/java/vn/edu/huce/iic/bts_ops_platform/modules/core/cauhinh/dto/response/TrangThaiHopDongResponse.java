        package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response;

        import lombok.Data;

import java.time.Instant;
import java.util.UUID;

        @Data
        public class TrangThaiHopDongResponse {
            private UUID id;
    private String ten;
    private String ma;
    private String mauSac;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
        }
