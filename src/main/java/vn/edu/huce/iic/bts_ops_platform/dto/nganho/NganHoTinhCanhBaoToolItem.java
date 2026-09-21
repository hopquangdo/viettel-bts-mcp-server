package vn.edu.huce.iic.bts_ops_platform.dto.nganho;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Cảnh báo tỉnh thừa/thiếu ngân sách. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NganHoTinhCanhBaoToolItem {
    private String id;
    private String tinh;
    private String khuVuc;
    private long soTram;
    private BigDecimal giaTriSanLuong;
    private BigDecimal giaTriTrungBinh;
    /** "thieu" | "thua" */
    private String type;
    @Builder.Default
    private List<String> maTramList = new ArrayList<>();
}
