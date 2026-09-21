package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ChinhSuaThongSoDeXuatRequest {

    @NotNull
    private UUID hopDongDoiTuongId;

    @NotNull
    private List<ChinhSuaThongSoMucRequest> mucThayDoi;

    /** Bắt buộc khi sửa từ lần 3 trở đi. */
    private String giaiTrinh;
}
