package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PhanCongLichSuResponse {
    private UUID id;
    private UUID hopDongId;
    private UUID hopDongDoiTuongId;
    private String maTram;
    private UUID nhaThauCuId;
    private UUID nhaThauMoiId;
    private String nhaThauCuTen;
    private String nhaThauMoiTen;
    private String noiDung;
    private UUID nguoiThucHienId;
    private String nguoiThucHienTen;
    private Instant ngayTao;
}
