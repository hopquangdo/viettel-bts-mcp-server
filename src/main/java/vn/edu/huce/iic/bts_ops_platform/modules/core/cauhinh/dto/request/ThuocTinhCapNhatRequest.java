package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;


@Data
public class ThuocTinhCapNhatRequest {
    private UUID doiTuongQuanLyId;
    @Size(max = 255)
    private String ten;
    @Size(max = 20)
    private String kieuDuLieuId;
    @Size(max = 100)
    private String lienKetBang;
    private Boolean laKhoaChinh;
    private Boolean batBuoc;
    @Size(max = 50)
    private String donVi;
    private List<String> tuyChon;
    private Boolean hoatDong;
}
