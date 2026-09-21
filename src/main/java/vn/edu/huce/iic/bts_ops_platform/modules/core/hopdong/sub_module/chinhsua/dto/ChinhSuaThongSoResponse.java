package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ChinhSuaThongSoResponse {

    private UUID id;
    private UUID hopDongId;
    private UUID hopDongDoiTuongId;
    private UUID thuocTinhId;
    private String tenThuocTinh;
    private String giaTriCu;
    private String giaTriMoi;
    private String giaiTrinh;
    private String trangThai;
    private Integer lanChinhSua;
    private UUID nguoiDeXuatId;
    private String nguoiDeXuatTen;
    private UUID nguoiPheDuyetId;
    private String nguoiPheDuyetTen;
    private Instant ngayPheDuyet;
    private String lyDoTuChoi;
    private Instant ngayTao;
}
