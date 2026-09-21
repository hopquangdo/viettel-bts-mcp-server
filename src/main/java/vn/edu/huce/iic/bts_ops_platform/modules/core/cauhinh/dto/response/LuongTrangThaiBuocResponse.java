package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LuongTrangThaiBuocResponse {

    private UUID trangThaiHopDongId;
    private String ten;
    private String ma;
    private String mauSac;
    private Short thuTu;
}
