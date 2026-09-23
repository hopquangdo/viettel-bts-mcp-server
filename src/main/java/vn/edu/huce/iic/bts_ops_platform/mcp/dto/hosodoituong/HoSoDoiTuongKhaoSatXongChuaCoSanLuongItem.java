package vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong;

import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.doituong.DoiTuongInfo;

import java.time.LocalDate;

/** 1 đối tượng đã bàn giao mặt bằng nhưng chưa có sản lượng hiệu lực — trả về cho doituong_tool.hoSoDoiTuong.khaoSatXongChuaCoSanLuong. */
@Data
public class HoSoDoiTuongKhaoSatXongChuaCoSanLuongItem {
    private DoiTuongInfo doiTuong;
    private String maHopDong;
    private LocalDate ngayBanGiaoMatBang;
    private long soNgayKeTuBanGiao;
}
