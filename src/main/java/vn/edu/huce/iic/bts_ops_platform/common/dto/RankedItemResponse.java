package vn.edu.huce.iic.bts_ops_platform.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** 1 dòng trong danh sách xếp hạng top-N (dùng cho các tool "X nào nhiều nhất/thấp nhất"). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankedItemResponse {

    private String label;
    private Object value;
    private Map<String, Object> extra;
}
