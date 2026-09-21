package vn.edu.huce.iic.bts_ops_platform.dto.tramton;

import lombok.Data;

import java.math.BigDecimal;

/** 1 dòng xếp hạng khu vực theo số đối tượng tồn chờ quyết toán — trả về cho doituongton_tool.xepHangKhuVuc. */
@Data
public class TramTonXepHangKhuVucToolItem {
    private String khuVuc;
    private long soDoiTuong;
    private BigDecimal giaTriTon;
}
