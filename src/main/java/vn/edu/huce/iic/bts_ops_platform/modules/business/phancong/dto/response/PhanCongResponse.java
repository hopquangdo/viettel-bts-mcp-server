package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class PhanCongResponse {
    private UUID id;
    private UUID hopDongId;
    private UUID hopDongDoiTuongId;
    private String nhaThau;
    private String giaiDoan;
    private UUID khuVucId;
    private String maVung;
    private UUID tinhThanhId;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
    private Instant ngayXoa;
}
