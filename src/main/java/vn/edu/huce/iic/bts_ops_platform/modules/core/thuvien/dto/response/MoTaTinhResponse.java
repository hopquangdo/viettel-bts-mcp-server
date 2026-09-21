package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MoTaTinhResponse {

    private UUID tinhThanhId;
    private String maTinh;
    private String tenTinh;
    private String viTriDiaLy;
    private String dacDiemDiaHinh;
    private String khiHau;
    private String dieuKienKinhTe;
    /** true nếu đã có người nhập mô tả cho tỉnh này; false nghĩa là các trường trên đều rỗng mặc định. */
    private boolean coMoTa;
}
