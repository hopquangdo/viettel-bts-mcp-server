package vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhathau;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 1 nhà thầu trong danh sách: định danh và quy mô phụ trách (trong phạm vi bộ lọc). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NhaThauItem {
    private NhaThauInfo nhaThau;
    private String tenDangNhap;
    private long soHopDong;
    private long soDoiTuong;
}
