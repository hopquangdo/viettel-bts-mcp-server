package vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramTonTongQuanResponse {
    private long tongSoTram;
    private long duDieuKienDt;
    private long choQuyetToan;
    private long chuaDuDieuKien;
    private long quaHan;
    /** Ngưỡng số ngày tồn để đánh dấu quá hạn (mặc định 90). */
    private int quaHanNgay;
    /** Ngày chốt báo cáo trạm tồn. */
    private LocalDate ngayBaoCao;
    private BigDecimal giaTriTon;

    @Builder.Default
    private List<TramTonBucketResponse> aging = new ArrayList<>();

    @Builder.Default
    private List<TramTonBucketResponse> reasons = new ArrayList<>();

    /** Số lượng theo từng tab danh sách (all, cho_qt, phap_ly, vuong, thieu, han). */
    @Builder.Default
    private Map<String, Long> tabCounts = new LinkedHashMap<>();

    private String tramLauNhatMa;
    private long tramLauNhatSoNgay;
}
