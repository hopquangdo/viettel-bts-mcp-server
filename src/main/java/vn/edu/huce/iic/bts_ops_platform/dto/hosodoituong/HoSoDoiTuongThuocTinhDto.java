package vn.edu.huce.iic.bts_ops_platform.dto.hosodoituong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.dto.doituong.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongInfo;

import java.time.LocalDate;

/** Thuộc tính khảo sát THỰC CÓ trong DB của 1 đối tượng cụ thể — trả về cho hosodoituong_tool.thuocTinh. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoSoDoiTuongThuocTinhDto {
    private DoiTuongInfo doiTuong;
    private HopDongInfo hopDong;
    private LocalDate ngayBanGiaoMatBang;
    /** CHUA_DAM_BAO | DA_DAM_BAO | ... */
    private String trangThaiVatTuA;
    /** CHUA_YEU_CAU | DA_YEU_CAU | DA_NHAN | ... */
    private String trangThaiVatTuB;
    private LocalDate ngayYeuCauVatTuB;
    private LocalDate ngayHoanThanhVatTuB;
}
