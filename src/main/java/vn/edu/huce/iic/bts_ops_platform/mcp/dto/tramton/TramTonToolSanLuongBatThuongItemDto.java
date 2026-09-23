package vn.edu.huce.iic.bts_ops_platform.mcp.dto.tramton;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong.HopDongInfo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** 1 dòng trạm sản lượng bất thường cho AI tool (tramton_sanluong_bat_thuong). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramTonToolSanLuongBatThuongItemDto {
    private UUID id;
    private HopDongInfo hopDong;
    private Long giaTriHd;
    private LocalDate ngayHoanThanh;
    private BigDecimal sanLuongHieuLuc;
}
