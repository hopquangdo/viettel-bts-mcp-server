package vn.edu.huce.iic.bts_ops_platform.mcp.dto.tramton;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong.HopDongInfo;

import java.time.LocalDate;
import java.util.UUID;

/** 1 dòng trạm thiếu cập nhật tiến độ cho AI tool (tramton_thieu_capnhat). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramTonToolThieuCapNhatItemDto {
    private UUID id;
    private HopDongInfo hopDong;
    private String maTram;
    private String khuVuc;
    private String nhaThau;
    private LocalDate ngayThiCongGanNhat;
    private long soNgayKhongCapNhat;
}
