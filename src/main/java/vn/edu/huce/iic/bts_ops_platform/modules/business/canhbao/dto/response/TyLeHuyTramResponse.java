package vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TyLeHuyTramResponse {
    private BigDecimal nguong;
    private List<TyLeHuyTramItemResponse> theoCanBo;
    private List<TyLeHuyTramItemResponse> theoKhuVuc;
}
