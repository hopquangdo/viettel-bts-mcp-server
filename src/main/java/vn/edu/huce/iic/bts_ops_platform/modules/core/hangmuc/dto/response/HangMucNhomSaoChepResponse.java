package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HangMucNhomSaoChepResponse {
    private int soNhom;
    private int soChiTiet;
    private int soCongViec;
}
