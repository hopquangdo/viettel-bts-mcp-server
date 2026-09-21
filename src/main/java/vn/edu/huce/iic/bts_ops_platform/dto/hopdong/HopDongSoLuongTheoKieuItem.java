package vn.edu.huce.iic.bts_ops_platform.dto.hopdong;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Số lượng hợp đồng theo 1 kiểu hợp đồng — dùng trong hopdong_tongquan. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HopDongSoLuongTheoKieuItem {
    private UUID kieuHopDongId;
    private String ma;
    private String ten;
    private String nhom;
    private long soLuong;
}
