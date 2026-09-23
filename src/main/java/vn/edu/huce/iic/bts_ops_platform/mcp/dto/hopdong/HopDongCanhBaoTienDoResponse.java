package vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Kết quả hopdong_canh_bao_tien_do. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HopDongCanhBaoTienDoResponse {
    private long soHopDongXanh;
    private long soHopDongVang;
    private long soHopDongDo;
    private List<HopDongCanhBaoItem> hopDongDo;
}
