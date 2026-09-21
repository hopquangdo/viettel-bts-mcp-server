        package vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.response;

        import lombok.Data;
        import java.util.UUID;
import java.time.Instant;
import java.util.UUID;

        @Data
        public class TepDinhKemResponse {
            private UUID id;
    private String ma;
    private String tenTep;
    private String tenTepGoc;
    private String duongDan;
    private String url;
    private String loaiTep;
    private String mimeType;
    private Long kichThuoc;
    private String checksum;
    private UUID nguoiTaoId;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
        }
