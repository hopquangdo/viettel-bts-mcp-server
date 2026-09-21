package vn.edu.huce.iic.bts_ops_platform.dto.nganho;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Thông tin 1 khu vực (không kèm danh sách tỉnh). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NganHoKhuVucDetailToolItem {
    private String id;
    private String name;
    private String color;
    private int soTinh;
    private int soCanhBao;
    private boolean moRongMacDinh;
}
