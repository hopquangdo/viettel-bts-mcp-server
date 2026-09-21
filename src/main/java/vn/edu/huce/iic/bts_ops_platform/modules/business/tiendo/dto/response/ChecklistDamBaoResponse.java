package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ChecklistDamBaoResponse {

    private UUID hopDongId;
    private Boolean checklistDoiTac;
    private Boolean checklistCcdc;
    private Boolean checklistAtld;
    private Boolean checklistVatTuA;
    private Boolean checklistVatTuB;
    private String checklistGhiChu;
}
