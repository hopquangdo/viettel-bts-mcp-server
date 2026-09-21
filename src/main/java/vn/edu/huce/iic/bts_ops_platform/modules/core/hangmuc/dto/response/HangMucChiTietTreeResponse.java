package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class HangMucChiTietTreeResponse extends HangMucChiTietResponse {

    private List<HangMucCongViecResponse> congViec = new ArrayList<>();
}
