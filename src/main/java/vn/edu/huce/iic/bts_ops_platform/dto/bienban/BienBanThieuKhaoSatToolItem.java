package vn.edu.huce.iic.bts_ops_platform.dto.bienban;

import lombok.Data;

/** 1 dòng trạm chưa có biên bản khảo sát đã duyệt, trả về cho MCP tool bienban_thieu_khao_sat. */
@Data
public class BienBanThieuKhaoSatToolItem {
    private String maTram;
    private String khuVuc;
    private String maHopDong;
}
