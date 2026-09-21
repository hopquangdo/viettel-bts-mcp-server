package vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramTonItemResponse {
    private UUID id;
    private UUID hopDongId;
    private String maTram;
    private String khuVuc;
    private String nhaThau;
    private String maHopDong;
    private BigDecimal giaTri;
    private LocalDate ngayHtTc;
    private boolean ngayHtTcFallback;
    private long soNgayTon;
    @Builder.Default
    private List<String> thieuDieuKien = new ArrayList<>();
    private String trangThai;
    private String nguoiPhuTrach;
    /** Lý do chính giải thích vì sao đối tượng đang tồn / chưa quyết toán. */
    private String lyDoTon;
    /** Số vướng mắc đang mở trên đối tượng (nếu có). */
    private int soVuongMacMo;
}
