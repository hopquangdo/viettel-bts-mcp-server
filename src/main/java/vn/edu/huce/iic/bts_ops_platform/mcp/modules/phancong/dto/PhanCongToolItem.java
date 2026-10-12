package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto;

import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.GeoRefResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;

import java.time.Instant;
import java.util.UUID;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituong.dto.DoiTuongInfo;

/** 1 dòng phân công trả về cho MCP tool (search_business_data / find_business_issues entity=PHAN_CONG). */
@Data
public class PhanCongToolItem {
    private UUID id;
    private HopDongInfo hopDong;
    /** Đối tượng được phân công: ma = mã thật (thuộc tính khoá chính), ten = loại (Tuyến/Trạm). */
    private DoiTuongInfo doiTuong;
    private String nhaThau;
    private String giaiDoan;
    private GeoRefResponse khuVuc;
    private String maVung;
    private GeoRefResponse tinhThanh;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
    private Instant ngayXoa;
    /** Chỉ có ý nghĩa khi trả về từ find_business_issues entity=PHAN_CONG — true nếu trạm/hạng mục đang có vướng mắc mở. */
    private Boolean coVuongMacMo;
}
