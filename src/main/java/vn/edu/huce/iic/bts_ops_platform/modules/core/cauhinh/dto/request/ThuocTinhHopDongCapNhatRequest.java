package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ThuocTinhHopDongCapNhatRequest {
    @NotBlank
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

    @AssertTrue(message = "Chọn kiểu dữ liệu hoặc bảng liên kết")
    public boolean isDataTypeSpecified() {
        return (kieuDuLieuId != null && !kieuDuLieuId.isBlank())
                || (lienKetBang != null && !lienKetBang.isBlank());
    }
}
