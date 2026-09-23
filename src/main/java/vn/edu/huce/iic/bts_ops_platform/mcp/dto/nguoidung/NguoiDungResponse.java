package vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguoidung;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NguoiDungResponse {
    private String email;
    private String dienThoai;
}
