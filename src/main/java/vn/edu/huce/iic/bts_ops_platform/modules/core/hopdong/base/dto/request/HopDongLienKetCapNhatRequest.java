package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class HopDongLienKetCapNhatRequest {

    /** HĐ liên kết; null hoặc bỏ trống = xóa liên kết hiện tại */
    private UUID hopDongLienKetId;

    @Size(max = 100)
    private String ghiChu;
}
