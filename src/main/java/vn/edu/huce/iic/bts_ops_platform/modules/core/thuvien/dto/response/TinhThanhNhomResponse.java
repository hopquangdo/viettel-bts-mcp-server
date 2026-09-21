package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TinhThanhNhomResponse {

    private UUID tinhThanhId;
    private TinhThanhResponse tinhHienTai;
    private List<TinhThanhResponse> tinhCu;
}
