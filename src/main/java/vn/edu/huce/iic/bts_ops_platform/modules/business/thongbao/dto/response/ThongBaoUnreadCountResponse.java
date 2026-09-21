package vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThongBaoUnreadCountResponse {
    private long chuaDoc;
}
