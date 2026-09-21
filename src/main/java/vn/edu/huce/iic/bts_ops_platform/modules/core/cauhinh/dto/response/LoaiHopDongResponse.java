        package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class LoaiHopDongResponse {
    private UUID id;
    private String ma;
    private String ten;
    private String moTa;
    private BigDecimal hesoNguong;
    private String heNghiepVu;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
}
