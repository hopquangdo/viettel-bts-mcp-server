package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PhanCongBoLocResponse {
    private List<PhanCongFilterOption> nhaThau;
    private List<PhanCongFilterOption> giaiDoan;
    private List<PhanCongFilterOption> maVung;
    private List<PhanCongFilterOption> hopDong;
}
