package vn.edu.huce.iic.bts_ops_platform.dto.hosodoituong;

import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.dto.doituong.DoiTuongInfo;

/**
 * 1 đối tượng CHƯA có ngày bàn giao mặt bằng trong 1 hợp đồng — proxy gần nhất cho "chưa khảo
 * sát" (hệ thống chưa có trường ngày khảo sát riêng). Trả về cho hosodoituong_tool.chuaKhaoSat.
 */
@Data
public class HoSoDoiTuongChuaKhaoSatItem {
    private DoiTuongInfo doiTuong;
    private String khuVuc;
    private String nhaThau;
}
