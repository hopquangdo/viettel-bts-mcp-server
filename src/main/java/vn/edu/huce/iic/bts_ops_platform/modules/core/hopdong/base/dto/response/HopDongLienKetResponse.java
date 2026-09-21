package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class HopDongLienKetResponse {
    private UUID id;
    private UUID hopDongId;
    private String hopDongMa;
    private String hopDongTen;
    private String hopDongLoaiTen;
    private String hopDongKieuTen;
    private UUID hopDongLienKetId;
    private String hopDongLienKetMa;
    private String hopDongLienKetTen;
    private String hopDongLienKetLoaiTen;
    private String hopDongLienKetKieuTen;
    private String ghiChu;
    private Instant ngayTao;
}
