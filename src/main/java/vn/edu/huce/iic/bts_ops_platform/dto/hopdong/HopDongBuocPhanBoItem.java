package vn.edu.huce.iic.bts_ops_platform.dto.hopdong;

import lombok.Data;

/** 1 bước trong luồng trạng thái của 1 hợp đồng, kèm số đối tượng đang ở bước đó. */
@Data
public class HopDongBuocPhanBoItem {
    private String tenBuoc;
    private Short thuTu;
    private long soDoiTuong;
}
