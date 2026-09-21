package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class HopDongDoiTuongNhanBanRequest {

    @NotEmpty
    @Valid
    private List<Muc> muc;

    @Data
    public static class Muc {
        @NotNull
        private UUID nguonId;

        /** Mã mới cho thuộc tính khóa chính (mã trạm/tuyến). */
        @NotBlank
        private String maMoi;
    }
}
