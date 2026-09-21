package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class KpiCanhBaoResponse {

    private UUID hopDongDoiTuongId;
    private UUID hopDongId;
    private String kpiMa;
    private String kpiTen;
    private Integer uuTien;
    private Integer soNgayThucTe;
    private Integer soNgayNguong;
    private Integer soNgayVuot;
}
