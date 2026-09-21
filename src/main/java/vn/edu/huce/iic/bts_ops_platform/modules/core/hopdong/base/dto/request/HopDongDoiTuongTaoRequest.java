    package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

    import jakarta.validation.constraints.NotNull;
    import lombok.Data;
    import java.util.UUID;

    @Data
    public class HopDongDoiTuongTaoRequest {
        @NotNull
private UUID hopDongId;
@NotNull
private UUID doiTuongQuanLyId;
private UUID trangThaiHopDongId;
private UUID hopDongNhomUuTienId;
private UUID nhaThauId;
private UUID khuVucId;
private UUID tinhThanhId;
private Boolean hoatDong = true;
    }
