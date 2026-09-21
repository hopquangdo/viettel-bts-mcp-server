package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class HopDongCapNhatRequest {
    @Size(max = 100)
    private String maHopDong;
    @Size(max = 500)
    private String ten;
    @Size(max = 500)
    private String benKyA;
    private Long giaTriHd;
    private LocalDate ngayThucHien;
    private Integer soNgayThucHien;
    private UUID loaiHopDongId;
    private UUID kieuHopDongId;
    private Short trangThaiPhapLy;
    private Boolean hoatDong;
    @Valid
    private List<HopDongThuocTinhGiaTriItem> thuocTinhGiaTri = new ArrayList<>();
}
