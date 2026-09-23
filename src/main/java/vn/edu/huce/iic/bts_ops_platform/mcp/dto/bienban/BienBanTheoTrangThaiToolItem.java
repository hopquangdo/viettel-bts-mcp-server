package vn.edu.huce.iic.bts_ops_platform.mcp.dto.bienban;

import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong.HopDongInfo;

import java.time.Instant;
import java.time.LocalDate;

/** 1 dòng biên bản trong danh sách theo trạng thái — trả về cho bienban_tool.theoTrangThai.
 * Kèm lý do từ chối và người/ngày phê duyệt khi có. */
@Data
public class BienBanTheoTrangThaiToolItem {
    private String maBienBan;
    private String loaiBienBan;
    private HopDongInfo hopDong;
    /** Chờ duyệt | Đã duyệt | Từ chối — text tiếng Việt để LLM đọc/map trực tiếp, không phải mã DB. */
    private String trangThai;
    private LocalDate ngayLap;
    private String nguoiLapTen;
    private String lyDoTuChoi;
    private String nguoiPheDuyetTen;
    private Instant ngayPheDuyet;
}
