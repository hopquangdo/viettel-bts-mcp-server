package vn.edu.huce.iic.bts_ops_platform.dto.hopdong;

import lombok.Data;

/** 1 dòng xếp hạng khu vực theo số hợp đồng/đối tượng — trả về cho hopdong_tool.xepHangKhuVuc. */
@Data
public class HopDongXepHangKhuVucToolItem {
    private String khuVuc;
    private long soHopDong;
    private long soDoiTuong;
    private long soVuongMac;
}
