package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BienBanThayTheResult {
    private BienBanLichSuResponse record;
    /** Khác null với loại biên bản có mẫu Word (.zip). */
    private byte[] zipContent;
}
