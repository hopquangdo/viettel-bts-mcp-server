package vn.edu.huce.iic.bts_ops_platform.dto.hopdong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 1 hợp đồng cấp cảnh báo Đỏ trong hopdong_canh_bao_tien_do. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HopDongCanhBaoItem {
    private HopDongInfo hopDong;
    private double tyLeHoanThanh;
}
