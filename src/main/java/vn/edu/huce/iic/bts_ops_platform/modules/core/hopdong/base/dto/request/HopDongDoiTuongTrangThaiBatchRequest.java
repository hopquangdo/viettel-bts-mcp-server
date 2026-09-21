package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class HopDongDoiTuongTrangThaiBatchRequest {

    @NotEmpty
    private List<UUID> hopDongDoiTuongIds;

    /** Bắt buộc khi yêu cầu / xác nhận hủy. */
    private String lyDoHuy;
}
