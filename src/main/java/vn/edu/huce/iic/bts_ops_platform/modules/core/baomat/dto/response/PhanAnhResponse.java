package vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PhanAnhResponse {

    private UUID id;
    private UUID nguoiDungId;
    private String nguoiDungTen;
    private String loai;
    private String tieuDe;
    private String noiDung;
    private String trangThai;
    private Instant ngayTao;
}
