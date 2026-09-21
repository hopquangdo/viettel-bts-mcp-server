package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class HangMucKhoiLuongSanLuongResponse {

    /** Tổng khoiLuongHoanThanh theo hang_muc_cong_viec_id. */
    private Map<UUID, BigDecimal> theoCongViec = new LinkedHashMap<>();

    /** Tổng khoiLuongHoanThanh theo hang_muc_chi_tiet_id (không gắn công việc). */
    private Map<UUID, BigDecimal> theoChiTiet = new LinkedHashMap<>();
}
