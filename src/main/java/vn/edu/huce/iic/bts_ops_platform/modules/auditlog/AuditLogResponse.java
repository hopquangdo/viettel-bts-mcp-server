package vn.edu.huce.iic.bts_ops_platform.modules.auditlog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private UUID id;
    private UUID nguoiThucHienId;
    private String tenNguoiThucHien;
    private String hanhDong;
    private String moTa;
    private String ip;
    private UUID doiTuongId;
    private UUID hopDongId;
    private Instant ngayTao;
}
