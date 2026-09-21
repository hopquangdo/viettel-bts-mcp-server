package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MoTaTinhRequest {

    @Size(max = 4000)
    private String viTriDiaLy;

    @Size(max = 4000)
    private String dacDiemDiaHinh;

    @Size(max = 4000)
    private String khiHau;

    @Size(max = 4000)
    private String dieuKienKinhTe;
}
