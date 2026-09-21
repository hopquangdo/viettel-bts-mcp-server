package vn.edu.huce.iic.bts_ops_platform.dto.sanluong;

import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.dto.nhathau.NhaThauInfo;

/** 1 nhà thầu đang phụ trách đối tượng hoạt động nhưng chưa có bản ghi sản lượng 'done' trong kỳ. */
@Data
public class SanLuongNhaThauChuaBaoItem {
    private NhaThauInfo nhaThau;
    private long soDoiTuongPhuTrach;
}
