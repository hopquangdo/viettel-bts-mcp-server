package vn.edu.huce.iic.bts_ops_platform.dto.nganho;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongInfo;

import java.math.BigDecimal;

/** 1 hợp đồng trong danh sách ngân sách (theo bộ lọc trạng thái ngân sách/quyết toán). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NganHoHopDongToolItem {
    private HopDongInfo hopDong;
    private BigDecimal giaTriHopDong;
    private BigDecimal tongSanLuongThucTe;
    private long soTram;
    /** Chênh lệch % so với giá trị hợp đồng: (giá trị HĐ - sản lượng thực tế) / giá trị HĐ. */
    private BigDecimal chenhLechPhanTram;
    /** "vuot_nguong" | "canh_bao" | "binh_thuong" */
    private String trangThai;
    /** "thieu" | "thua" | "can_bang" */
    private String trangThaiVolume;
    /** "danger" | "warning" | "ok" */
    private String alertLevel;
    /** "da_qt" | "dang_qt" | "chua_qt" */
    private String trangThaiQt;
}
