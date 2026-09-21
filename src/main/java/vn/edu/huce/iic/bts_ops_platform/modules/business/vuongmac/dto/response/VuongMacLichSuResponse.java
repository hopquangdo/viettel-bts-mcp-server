package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class VuongMacLichSuResponse {
    private UUID id;
    private UUID vuongMacId;
    private String trangThaiCu;
    private String trangThaiMoi;
    private String ghiChu;
    private String lyDoTuChoi;
    private UUID nguoiXuLyId;
    private String nguoiXuLyTen;
    private UUID nguoiThucHienId;
    private String nguoiThucHienTen;
    private Instant ngayTao;
}
