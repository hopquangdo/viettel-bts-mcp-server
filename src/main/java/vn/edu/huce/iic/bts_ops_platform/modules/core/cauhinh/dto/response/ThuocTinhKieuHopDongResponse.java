package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class ThuocTinhKieuHopDongResponse {
    private UUID id;
    private UUID kieuHopDongId;
    private UUID thuocTinhHopDongId;
    private Short thuTu;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
    private String ten;
    private String kieuDuLieuId;
    private String lienKetBang;
    private Boolean laKhoaChinh;
    private Boolean batBuoc;
    private String donVi;
    private List<String> tuyChon;
}
