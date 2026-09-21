package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response;

import lombok.Data;

import java.util.Map;

@Data
public class PhanCongThongKeResponse {
    private long tongPhanCong;
    private long tongHoatDong;
    private Map<String, Long> theoKhuVuc;
    private Map<String, Long> theoTinhThanh;
}
