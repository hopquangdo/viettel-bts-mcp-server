package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PhanCongMaVungItemResponse {
    private UUID khuVucId;
    /** Mã khu vực (danh mục Khu vực). */
    private String maVung;
    /** Tên khu vực. */
    private String tenKhuVuc;
    private String tinhThanh;
    private long soTram;
    private String nhaThauChinh;
    private UUID nhaThauChinhId;
}
