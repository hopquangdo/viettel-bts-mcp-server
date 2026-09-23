package vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Kết quả hopdong_tongquan. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HopDongTongQuanResponse {
    private long tongHopDong;
    private long tongHoatDong;
    private long tongDoiTuong;
    private Map<String, Long> tienDoTheoTrangThai;
    private double tyLeHoanThanh;
    private List<HopDongSoLuongTheoKieuItem> soLuongTheoKieu;
    private Map<String, Long> soLuongTheoLoai;
    private long soHopDongChamTienDo;
    private long soHopDongVuongMac;
}
