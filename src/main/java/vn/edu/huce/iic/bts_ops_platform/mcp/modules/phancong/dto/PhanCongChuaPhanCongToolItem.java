package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto;

import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituong.dto.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;

/** 1 đối tượng chưa được phân công nhà thầu (hop_dong_doi_tuong.nha_thau_id IS NULL) — trả về cho
 * phancong_tool.chuaPhanCong. */
@Data
public class PhanCongChuaPhanCongToolItem {
    private DoiTuongInfo doiTuong;
    private HopDongInfo hopDong;
    private String khuVuc;
    private String tinh;
}
