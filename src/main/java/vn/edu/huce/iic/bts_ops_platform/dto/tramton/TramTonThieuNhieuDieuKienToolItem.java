package vn.edu.huce.iic.bts_ops_platform.dto.tramton;

import lombok.Data;

import java.util.List;

/** 1 đối tượng thiếu từ 2 điều kiện quyết toán trở lên — trả về cho doituongton_tool.thieuNhieuDieuKien.
 * Điều kiện xét: pháp lý hợp đồng, vướng mắc mở, đã có sản lượng hiệu lực. */
@Data
public class TramTonThieuNhieuDieuKienToolItem {
    private String maDoiTuong;
    private String maHopDong;
    private String khuVuc;
    private String nhaThau;
    private int soDieuKienThieu;
    /** Danh sách điều kiện còn thiếu ở dạng người đọc được. */
    private List<String> dieuKienThieu;
}
