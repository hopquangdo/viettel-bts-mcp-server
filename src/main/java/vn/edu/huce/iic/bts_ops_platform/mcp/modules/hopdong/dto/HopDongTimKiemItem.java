package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.support.Tien;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 1 bản ghi hợp đồng trong kết quả hopdong_search. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HopDongTimKiemItem {
    private UUID id;
    private String maHopDong;
    private String ten;
    @Tien
    private Long giaTriHd;
    private String trangThaiThiCong;
    private UUID loaiHopDongId;
    private String loaiHopDongTen;
    private UUID kieuHopDongId;
    private String kieuHopDongTen;
}
