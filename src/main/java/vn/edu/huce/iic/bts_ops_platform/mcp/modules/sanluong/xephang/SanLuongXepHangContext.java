package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.DoiTuongProgressProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.ProgressGroupProjection;

import java.util.List;
import java.util.Set;

/**
 * Dữ liệu đầu vào cho 1 lần xếp hạng sản lượng (đã truy vấn xong, strategy chỉ tính trên bộ nhớ).
 *
 * @param groups      nhóm đối tượng (dòng "G" của progressBundle), mỗi nhóm đại diện {@code n} đối tượng
 * @param topDoiTuong 20 đối tượng đầu (dòng "T", đã sắp ở DB)
 * @param soDoiTuong  tổng số đối tượng trong phạm vi
 * @param khoaXepHang giaTri | hoanThanh | soDoiTuong | vuongMac
 * @param tangDan     true = tăng dần
 * @param capDaLoc    các chiều đang lọc đúng 1 giá trị (vd lọc khuVuc thì có KHU_VUC): xếp hạng theo chiều đó vô nghĩa
 */
public record SanLuongXepHangContext(List<ProgressGroupProjection> groups,
                                     List<DoiTuongProgressProjection> topDoiTuong,
                                     long soDoiTuong,
                                     String khoaXepHang,
                                     boolean tangDan,
                                     Set<SanLuongXepHangChieu> capDaLoc) {
}
