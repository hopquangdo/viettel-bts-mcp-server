package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request;

import lombok.Data;

@Data
public class ChecklistDamBaoCapNhatRequest {

    private Boolean checklistDoiTac;
    private Boolean checklistCcdc;
    private Boolean checklistAtld;
    private Boolean checklistVatTuA;
    private Boolean checklistVatTuB;
    private String checklistGhiChu;
}
