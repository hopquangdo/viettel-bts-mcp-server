package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class HoSoTramTepDinhKemResponse {
    private UUID id;
    private UUID hopDongDoiTuongId;
    private String danhMuc;
    private String tenTep;
    private String url;
    private Long kichThuoc;
    private String ghiChu;
    private Instant ngayTao;
}
