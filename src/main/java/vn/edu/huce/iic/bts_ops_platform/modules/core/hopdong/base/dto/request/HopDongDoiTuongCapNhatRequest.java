package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class HopDongDoiTuongCapNhatRequest {
    private UUID hopDongId;
    private UUID doiTuongQuanLyId;
    private UUID trangThaiHopDongId;
    private UUID hopDongNhomUuTienId;
    private UUID nhaThauId;
    private UUID khuVucId;
    private UUID tinhThanhId;
    private LocalDate ngayHtTc;
    private Boolean hoatDong;
    private Boolean daKyHdThauPhu;
    private LocalDate ngayKyHdThauPhu;
    /** Lý do hủy — chỉ áp dụng khi trangThaiHopDongId trỏ tới bước "Hủy". */
    private String lyDoHuy;
}
