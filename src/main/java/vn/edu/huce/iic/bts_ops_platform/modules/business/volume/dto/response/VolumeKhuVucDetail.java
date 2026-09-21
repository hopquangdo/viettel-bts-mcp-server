package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Thông tin khu vực — KHÔNG kèm danh sách tỉnh nữa, xem VolumeController.danhSachTinh (phân trang riêng). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeKhuVucDetail {
    private String id;
    private String name;
    private String color;
    private int provinceCount;
    private int alertCount;
    private boolean defaultOpen;
}
