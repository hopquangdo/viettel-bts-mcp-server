package vn.edu.huce.iic.bts_ops_platform.dto.bienban;

import lombok.Data;

import java.util.List;

/** 1 hợp đồng còn thiếu biên bản/hồ sơ bắt buộc, trả về cho MCP tool bienban_thieu_theo_tien_do. */
@Data
public class BienBanThieuTheoTienDoToolItem {
    private String maHopDong;
    private String ten;
    private List<String> thieuBienBan;
}
