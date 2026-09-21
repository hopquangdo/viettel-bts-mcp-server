package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class HopDongTaoRequest {
    @NotBlank
    @Size(max = 100)
    private String maHopDong;
    @NotBlank
    @Size(max = 500)
    private String ten;
    @Size(max = 500)
    private String benKyA;
    /** Bắt buộc nhập khi tạo hợp đồng — không cho tạo nếu thiếu/=0 (căn cứ tính toán Volume/ngưỡng cảnh báo). */
    @NotNull
    @Positive
    private Long giaTriHd;
    private LocalDate ngayThucHien;
    private Integer soNgayThucHien;
    private UUID loaiHopDongId;
    private UUID kieuHopDongId;
    private Short trangThaiPhapLy;
    private Boolean hoatDong = true;
    @Valid
    private List<HopDongThuocTinhGiaTriItem> thuocTinhGiaTri = new ArrayList<>();
}
