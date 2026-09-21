package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiBuocResponse;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class HopDongDanhSachLuongTrangThaiResponse {

    private UUID id;
    private String ten;
    private List<LuongTrangThaiBuocResponse> buoc;
}
