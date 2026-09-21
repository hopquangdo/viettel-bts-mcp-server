package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TramThiCongBuocResponse {
    private String maBuoc;
    private String ten;
    private Integer thuTu;
    private String trangThai;
    private String lyDoTuChoi;
    private Instant ngayDuyet;
    private Boolean moKhoa;
    private Boolean coTheTaiLen;
    private Boolean coTheGuiDuyet;
    private Boolean coTheDuyet;
}
