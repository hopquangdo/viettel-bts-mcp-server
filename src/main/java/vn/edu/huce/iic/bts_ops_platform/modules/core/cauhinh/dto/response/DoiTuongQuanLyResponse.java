        package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response;

        import lombok.Data;
        import java.util.UUID;
import java.time.Instant;

        @Data
        public class DoiTuongQuanLyResponse {
            private UUID id;
    private String ma;
    private String ten;
    private String bieuTuong;
    private String moTa;
    private Boolean hoatDong;
    private Boolean hienThiTrenGiaoDien;
    private Instant ngayTao;
    private Instant ngayCapNhat;
        }
