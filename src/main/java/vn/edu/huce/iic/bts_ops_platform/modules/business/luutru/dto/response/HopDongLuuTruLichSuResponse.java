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
public class HopDongLuuTruLichSuResponse {

    private UUID hopDongId;
    private String trangThai;
    private Instant ngayArchive;
    private UUID nguoiArchiveId;
    private String nguoiArchiveTen;
    private String ghiChu;
    private Instant ngayTao;
    private Instant ngayCapNhat;
}
