package vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Kết quả hopdong_trangthai_theo_khuvuc / hopdong_theo_loai_khuvuc. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HopDongTrangThaiKhuVucResponse {
    private long tong;
    private long hoanThanh;
    private long dangThiCong;
    private long vuongMac;
}
