package vn.edu.huce.iic.bts_ops_platform.mcp.common.loc;

import java.time.LocalDate;

/** Bộ lọc kỳ tùy chọn; chiaTheo xác định cách chia khoảng ngày khi cần. */
public record KyLoc(
        LocalDate fromDate,
        LocalDate toDate,
        String chiaTheo) {
}