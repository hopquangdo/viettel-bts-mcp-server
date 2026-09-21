package vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ThongBaoResponse {
    private UUID id;
    private String loai;
    private String tieuDe;
    private String noiDung;
    private String lienKet;
    private UUID thamChieuId;
    private boolean daDoc;
    private Instant ngayTao;
    private Instant ngayDoc;
}
