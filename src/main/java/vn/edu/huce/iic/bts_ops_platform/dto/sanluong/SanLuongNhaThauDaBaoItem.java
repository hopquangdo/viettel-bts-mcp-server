package vn.edu.huce.iic.bts_ops_platform.dto.sanluong;

import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.dto.nhathau.NhaThauInfo;

import java.math.BigDecimal;

/** 1 nhà thầu đang phụ trách đối tượng hoạt động và đã có ít nhất 1 bản ghi sản lượng 'done' trong kỳ. */
@Data
public class SanLuongNhaThauDaBaoItem {
    private NhaThauInfo nhaThau;
    private long soDoiTuongPhuTrach;
    private BigDecimal giaTriDaBao;
}
