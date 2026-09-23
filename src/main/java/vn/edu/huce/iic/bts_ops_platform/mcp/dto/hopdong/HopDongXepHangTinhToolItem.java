package vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong;

import lombok.Data;

/** 1 dòng xếp hạng tỉnh trong phạm vi 1 hợp đồng — trả về cho hopdong_tool.xepHangTinh (chỉ khi truyền maHopDong). */
@Data
public class HopDongXepHangTinhToolItem {
    private String tinh;
    private long soDoiTuong;
    private long soHoanThanh;
    private long soVuongMac;
}
