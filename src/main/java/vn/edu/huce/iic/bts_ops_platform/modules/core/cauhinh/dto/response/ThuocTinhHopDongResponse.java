package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class ThuocTinhHopDongResponse {
    private UUID id;
    private String ten;
    private String kieuDuLieuId;
    private String lienKetBang;
    private Boolean laKhoaChinh;
    private Boolean batBuoc;
    private String donVi;
    private List<String> tuyChon;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
}
