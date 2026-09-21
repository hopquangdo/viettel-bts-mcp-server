package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhanCongFilterOption {
    private String value;
    private String label;
    private Long count;
}
