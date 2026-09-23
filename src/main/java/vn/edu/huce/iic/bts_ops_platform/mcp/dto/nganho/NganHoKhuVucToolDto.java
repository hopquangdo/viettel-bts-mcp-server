package vn.edu.huce.iic.bts_ops_platform.mcp.dto.nganho;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** Phân bổ ngân sách theo khu vực/tỉnh của 1 hợp đồng — mirror VolumeKhuVucResponse (module Volume). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NganHoKhuVucToolDto {

    private int tongKhuVuc;
    @Builder.Default
    private List<NganHoKhuVucCardToolItem> duLieu = new ArrayList<>();
    @Builder.Default
    private List<NganHoTinhCanhBaoToolItem> canhBaoThieu = new ArrayList<>();
    @Builder.Default
    private List<NganHoTinhCanhBaoToolItem> canhBaoThua = new ArrayList<>();
    @Builder.Default
    private List<NganHoKhuVucDetailToolItem> chiTiet = new ArrayList<>();
}
