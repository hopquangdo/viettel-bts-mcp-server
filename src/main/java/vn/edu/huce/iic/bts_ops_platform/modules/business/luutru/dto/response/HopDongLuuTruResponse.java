package vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HopDongLuuTruResponse {

    private UUID hopDongId;
    private String maHopDong;
    private String ten;
    private UUID loaiHopDongId;
    private UUID kieuHopDongId;
    /** active | archived */
    private String trangThai;
    private Instant ngayArchive;
    private UUID nguoiArchiveId;
    private String nguoiArchiveTen;
    private String ghiChu;
    private Instant ngayTao;
    private Instant ngayCapNhat;

    /** Mã khu vực gộp (VD: KV1, KV1-KV2) — phục vụ bộ lọc FE. */
    private String khuVuc;
    private Long soTram;
    /** % hoàn thành theo trạng thái đối tượng (0–100). */
    private Double tyLeHoanThanh;
    private Long giaTriHd;
}
