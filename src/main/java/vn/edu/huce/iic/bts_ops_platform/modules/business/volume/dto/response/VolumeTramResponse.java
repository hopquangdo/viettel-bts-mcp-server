package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/** Thông tin chung của 1 tỉnh trong 1 HĐ — KHÔNG kèm danh sách trạm nữa, xem VolumeController.danhSachTram (phân trang riêng). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeTramResponse {

    private UUID hopDongId;
    private String province;
    private String region;
    /** Tên đối tượng quản lý của HĐ (Trạm, Tuyến, ...) để hiển thị linh hoạt. */
    private String doiTuongTen;
    private BigDecimal giaTriBinhQuan;
    private BigDecimal heSoNguong;
    private BigDecimal nguongCanhBao;
}
