package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class HopDongChecklistSyncRequest {

    private String ghiChuHopDong;

    @Valid
    private List<MucItem> mucDanhSach = new ArrayList<>();

    @Data
    public static class MucItem {
        private UUID id;

        @NotBlank
        private String ten;

        private Integer thuTu;

        private String ghiChu;
    }
}
