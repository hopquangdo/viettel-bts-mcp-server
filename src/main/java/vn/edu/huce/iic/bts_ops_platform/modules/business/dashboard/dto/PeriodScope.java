package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto;

import java.time.LocalDate;
import java.util.List;

/** Khoảng thời gian và danh sách bucket cho biểu đồ xu thế sản lượng. */
public record PeriodScope(LocalDate dateFrom, LocalDate dateTo, List<String> buckets) {
}
