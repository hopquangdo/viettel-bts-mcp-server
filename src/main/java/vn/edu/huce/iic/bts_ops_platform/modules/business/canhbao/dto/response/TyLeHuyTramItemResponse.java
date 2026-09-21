package vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TyLeHuyTramItemResponse {
    private String groupKey;
    private String groupTen;
    /** CAN_BO | KHU_VUC */
    private String groupLoai;
    private long tongTram;
    private long soTramHuy;
    private BigDecimal tyLeHuy;
    private boolean vuotNguong;
    private BigDecimal nguong;
}
