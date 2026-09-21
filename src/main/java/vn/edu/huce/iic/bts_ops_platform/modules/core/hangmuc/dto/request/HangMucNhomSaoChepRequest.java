package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class HangMucNhomSaoChepRequest {
    private UUID hopDongNguonId;
    private UUID hopDongDichId;
}
