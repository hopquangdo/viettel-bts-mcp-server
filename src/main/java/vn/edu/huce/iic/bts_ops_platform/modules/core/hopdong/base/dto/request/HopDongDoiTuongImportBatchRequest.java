package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class HopDongDoiTuongImportBatchRequest {

    @NotNull
    private UUID excelMappingId;

    @NotEmpty
    private List<Map<String, Object>> rows;

    /** Chỉ số dòng bắt đầu trong file (0-based) — dùng khi import theo lô để báo lỗi đúng số dòng Excel. */
    private int rowOffset = 0;

    /** Gọi sau lô cuối để cập nhật thống kê nhóm ưu tiên. */
    private boolean finalBatch = false;

    /** Import vào danh sách đợi thêm (hoatDong = false) thay vì danh sách chính. */
    private Boolean importAsPending;

    /** Xóa toàn bộ đối tượng đợi thêm trước khi import — chỉ áp dụng lô đầu (rowOffset = 0). */
    private Boolean replacePending;

    /** Chỉ validate, không ghi DB — dùng kiểm tra toàn bộ lô trước khi commit. */
    private boolean validateOnly = false;
}
