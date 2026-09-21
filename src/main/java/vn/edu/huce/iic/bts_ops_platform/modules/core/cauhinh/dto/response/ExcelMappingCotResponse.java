package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ExcelMappingCotResponse {
    private UUID id;
    private UUID excelMappingId;
    private UUID thuocTinhId;
    private String cotExcel;
    private Boolean batBuoc;
    private Short thuTu;
    private Instant ngayTao;
    private Instant ngayCapNhat;
    private Instant ngayXoa;
    private ThuocTinhResponse thuocTinh;
}
