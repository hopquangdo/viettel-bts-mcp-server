package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class LanChinhSuaGanNhatResponse {

    private UUID hopDongDoiTuongId;
    /** Thời điểm đề xuất/áp dụng chỉnh sửa gần nhất (ngayTao), null nếu chưa có lịch sử. */
    private Instant ngayChinhSuaGanNhat;
}
