package vn.edu.huce.iic.bts_ops_platform.mcp.dto.tramton;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong.HopDongInfo;

import java.math.BigDecimal;
import java.util.UUID;

/** 1 dòng trạm tồn trả về cho AI tool (tramton_search) — bản rút gọn của TramTonItemResponse. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramTonToolItemDto {
    private UUID id;
    private HopDongInfo hopDong;
    private String maTram;
    private String khuVuc;
    private String nhaThau;
    private BigDecimal giaTri;
    private long soNgayTon;
    private String trangThai;
    private String lyDoTon;
}
