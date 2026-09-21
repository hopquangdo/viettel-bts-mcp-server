package vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.response;

import lombok.Builder;
import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.NguonViecColumnLayoutDto;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.NguonViecCustomColumnDto;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class NguonViecDanhSachResponse {
    private List<NguonViecRowResponse> rows;
    private NguonViecSummaryResponse summary;
    private NguonViecTabCountsResponse tabCounts;
    private List<NguonViecCustomColumnDto> customColumns;
    private Map<String, Map<String, String>> customValues;
    private NguonViecColumnLayoutDto columnLayout;
}
