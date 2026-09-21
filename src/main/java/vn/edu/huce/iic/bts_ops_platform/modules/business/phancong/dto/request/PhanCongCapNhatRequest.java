package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class PhanCongCapNhatRequest {
    private UUID hopDongId;
    private UUID hopDongDoiTuongId;
    private String nhaThau;
    private String giaiDoan;
    private UUID khuVucId;
    private String maVung;
    private UUID tinhThanhId;
    private Boolean hoatDong;
}
