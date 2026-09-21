package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class KeHoachTrienKhaiChiTietResponse {

    private UUID id;
    private UUID keHoachId;
    private UUID hopDongDoiTuongId;
    private UUID khuVucId;
    private UUID nhaThauId;
}
