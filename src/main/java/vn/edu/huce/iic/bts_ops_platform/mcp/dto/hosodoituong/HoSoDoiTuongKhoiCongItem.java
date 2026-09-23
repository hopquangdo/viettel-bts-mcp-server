package vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong;

import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.doituong.DoiTuongInfo;

import java.time.LocalDate;

/** 1 đối tượng khởi công (ghi nhận sản lượng done đầu tiên) trong kỳ — trả về cho doituong_tool.hoSoDoiTuong.khoiCongTrongKy. */
@Data
public class HoSoDoiTuongKhoiCongItem {
    private DoiTuongInfo doiTuong;
    private String maHopDong;
    private String khuVuc;
    private String nhaThau;
    private LocalDate ngayKhoiCong;
}
