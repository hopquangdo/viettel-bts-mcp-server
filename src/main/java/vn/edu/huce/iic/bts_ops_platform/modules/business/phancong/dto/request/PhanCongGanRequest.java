package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PhanCongGanRequest {

    @NotEmpty
    private List<UUID> hopDongDoiTuongIds;

    @NotNull
    private UUID nhaThauId;

    /** Ghi chú tùy chọn khi phân công hàng loạt. */
    private String ghiChu;
}
