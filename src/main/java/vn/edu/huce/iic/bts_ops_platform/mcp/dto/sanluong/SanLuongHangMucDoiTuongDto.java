package vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Hạng mục của 1 đối tượng cụ thể (trạm, tuyến…): tổng theo hợp đồng, đã làm, chưa làm và chi tiết từng hạng mục. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanLuongHangMucDoiTuongDto {
    private String maDoiTuong;
    /** Số hạng mục lá của hợp đồng áp dụng cho đối tượng. */
    private int tongHangMuc;
    private int daLam;
    private int chuaLam;
    private List<SanLuongHangMucItem> hangMuc;
}
