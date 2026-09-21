package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class VolumeKhuVucResponse {

    private int tongKhuVuc;
    @Builder.Default
    private List<VolumeKhuVucCard> duLieu = new ArrayList<>();
    @Builder.Default
    private List<VolumeProvinceAlert> canhBaoThieu = new ArrayList<>();
    @Builder.Default
    private List<VolumeProvinceAlert> canhBaoThua = new ArrayList<>();
    @Builder.Default
    private List<VolumeKhuVucDetail> chiTiet = new ArrayList<>();
}
