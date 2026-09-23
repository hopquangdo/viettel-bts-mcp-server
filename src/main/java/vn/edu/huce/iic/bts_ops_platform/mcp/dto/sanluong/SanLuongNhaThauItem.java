package vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong;

import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhathau.NhaThauInfo;

import java.math.BigDecimal;

/**
 * 1 nhà thầu đang phụ trách đối tượng hoạt động, dùng chung cho cả 2 danh sách đối xứng
 * {@code nhaThauChuaBaoTrongKy} và {@code nhaThauDaBaoTrongKy}. {@code giaTriDaBao} chỉ có giá trị
 * (khác null) ở danh sách đã báo; ở danh sách chưa báo luôn null.
 */
@Data
public class SanLuongNhaThauItem {
    private NhaThauInfo nhaThau;
    private long soDoiTuongPhuTrach;
    private BigDecimal giaTriDaBao;
}
