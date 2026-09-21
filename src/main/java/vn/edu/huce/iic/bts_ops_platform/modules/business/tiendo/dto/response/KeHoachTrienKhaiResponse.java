package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class KeHoachTrienKhaiResponse {

    private UUID id;
    private UUID hopDongId;
    private String ten;
    private String trangThai;
    private UUID nguoiLapId;
    private UUID nguoiDuyetId;
    private Instant ngayGui;
    private Instant ngayDuyet;
    private String lyDoTuChoi;
    private List<KeHoachTrienKhaiChiTietResponse> chiTiet;
}
