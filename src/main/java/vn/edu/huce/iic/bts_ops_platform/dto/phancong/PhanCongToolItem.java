package vn.edu.huce.iic.bts_ops_platform.dto.phancong;

import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.common.dto.GeoRefResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongInfo;

import java.time.Instant;
import java.util.UUID;

/** 1 dòng phân công trả về cho MCP tool (search_business_data / find_business_issues entity=PHAN_CONG). */
@Data
public class PhanCongToolItem {
    private UUID id;
    private HopDongInfo hopDong;
    private UUID hopDongDoiTuongId;
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
