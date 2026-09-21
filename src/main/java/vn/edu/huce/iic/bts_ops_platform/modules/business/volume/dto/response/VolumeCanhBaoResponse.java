package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** Cảnh báo volume theo hợp đồng (trạm bất thường / thiếu / thừa sản lượng). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeCanhBaoResponse {
    private String id;
    private UUID hopDongId;
    private String maHopDong;
    private String ten;
    private String type;
    private long stationCount;
    private long tramThieu;
    private long tramThua;
    private String alertLevel;
    private String alertText;
    private String trangThaiVolume;
}
