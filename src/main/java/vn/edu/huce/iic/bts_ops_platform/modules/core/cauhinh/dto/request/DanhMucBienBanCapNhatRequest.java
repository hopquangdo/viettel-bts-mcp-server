package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DanhMucBienBanCapNhatRequest {
    @Size(max = 50)
    private String ma;
    @Size(max = 255)
    private String ten;
    @Size(max = 2000)
    private String moTa;
    private Boolean coMauWord;
    /** truoc_thi_cong | thi_cong | nghiem_thu | quyet_toan */
    @Size(max = 30)
    private String giaiDoan;
    private Integer thuTuMacDinh;
    private Boolean hoatDong;
}
