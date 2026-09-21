package vn.edu.huce.iic.bts_ops_platform.dto.tramton;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Số liệu tổng hợp trạm tồn cho AI tool (tramton_tongquan). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramTonToolTongQuanDto {
    private long tongSoTram;
    private long duDieuKienDt;
    private long choQuyetToan;
    private long chuaDuDieuKien;
    private long quaHan;
    /** Ngưỡng số ngày đã dùng để tính quá hạn/vướng mắc (truyền vào hoặc mặc định 90). */
    private int quaHanNgay;
    private BigDecimal giaTriTon;

    @Builder.Default
    private List<TramTonToolBucketDto> aging = new ArrayList<>();

    @Builder.Default
    private List<TramTonToolBucketDto> reasons = new ArrayList<>();
}
