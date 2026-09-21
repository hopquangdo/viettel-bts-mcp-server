package vn.edu.huce.iic.bts_ops_platform.dto.hosodoituong;

import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.dto.doituong.DoiTuongInfo;

import java.time.LocalDate;

/** 1 đối tượng đã bàn giao mặt bằng nhưng chưa có sản lượng hiệu lực — trả về cho hosodoituong_tool.khaoSatXongChuaCoSanLuong. */
@Data
public class HoSoDoiTuongKhaoSatXongChuaCoSanLuongItem {
    private DoiTuongInfo doiTuong;
    private String maHopDong;
    private LocalDate ngayBanGiaoMatBang;
    private long soNgayKeTuBanGiao;
}
