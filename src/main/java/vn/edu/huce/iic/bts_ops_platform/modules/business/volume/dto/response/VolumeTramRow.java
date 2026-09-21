package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeTramRow {
    private UUID id;
    private String maTram;
    private String diaDiem;
    /** Địa chỉ/vị trí cụ thể của đối tượng (khác diaDiem — thực chất là tỉnh). */
    private String diaChi;
    private String nhaThau;
    private String region;
    private String province;
    private BigDecimal sanLuong;
    /** Bổ sung SL tích lũy (VND) */
    private BigDecimal boSungSanLuong;
    private BigDecimal binhQuan;
    private BigDecimal nguong;
    /** CL hợp đồng = binhQuan − (sanLuong + boSungSanLuong) */
    private BigDecimal chenhLechHd;
    /** Quyết toán thực (nhập) */
    private BigDecimal quyetToanThuc;
    /** CL quyết toán = quyetToanThuc − binhQuan (null nếu chưa nhập QT) */
    private BigDecimal chenhLechQt;
    private boolean batThuong;
    /** shortage | surplus | normal */
    private String variant;
}
