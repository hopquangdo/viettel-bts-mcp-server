package vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramTonChiTietResponse {
    private UUID id;
    private UUID hopDongId;
    private String maTram;
    private String maHopDong;
    private String khuVuc;
    private String nhaThau;
    private String trangThai;
    private String lyDoTon;
    private long soNgayTon;

    @Builder.Default
    private List<String> thieuDieuKien = new ArrayList<>();

    @Builder.Default
    private List<TramTonVuongMacItem> vuongMacList = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TramTonVuongMacItem {
        private UUID id;
        private String giaiDoan;
        private String kieuVuongMac;
        private Boolean coTheBoSungSanLuong;
        private String moTa;
        private String moTaDayDu;
        private String trangThai;
        private Instant ngayTao;
        private String tenNguoiXuLy;
    }
}
