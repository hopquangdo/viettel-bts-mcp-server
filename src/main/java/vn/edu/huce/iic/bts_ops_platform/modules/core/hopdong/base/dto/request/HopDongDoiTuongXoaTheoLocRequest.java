package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class HopDongDoiTuongXoaTheoLocRequest {

    private String search;
    private Boolean activeOnly;
    @NotNull
    private UUID hopDongId;
    private UUID doiTuongQuanLyId;
    private UUID trangThaiHopDongId;
    private Boolean withoutNhomUuTien;
    private Boolean withNhomUuTien;
    private UUID hopDongNhomUuTienId;
    private List<UUID> excludeIds;
}
