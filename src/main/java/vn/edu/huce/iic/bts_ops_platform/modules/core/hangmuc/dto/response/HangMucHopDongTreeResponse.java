package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HangMucHopDongTreeResponse {

    private List<HangMucNhomTreeResponse> nhom = new ArrayList<>();

    private HangMucKhoiLuongSanLuongResponse khoiLuongSanLuong = new HangMucKhoiLuongSanLuongResponse();
}
