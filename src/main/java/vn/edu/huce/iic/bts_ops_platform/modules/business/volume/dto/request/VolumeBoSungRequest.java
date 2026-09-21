package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VolumeBoSungRequest {
    /** add | remove */
    private String action;
    /** Số tiền điều chỉnh (triệu VNĐ) — FE gửi theo đơn vị triệu như form UI */
    private BigDecimal soTienTrieu;
    private String lyDo;
    private String ghiChu;
}
