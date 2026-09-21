package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class DoiTuongHopDongLienKetResponse {
    private UUID id;
    private UUID doiTuongQuanLyId;
    private UUID loaiHopDongId;
    private UUID kieuHopDongId;
    private String doiTuongMa;
    private String doiTuongTen;
    private String doiTuongBieuTuong;
    private Boolean keThuaTuLoai;
    private Short thuTu;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
}
