package vn.edu.huce.iic.bts_ops_platform.dto.phancong;

import lombok.Data;

import java.math.BigDecimal;

/** 1 dòng xếp hạng nhà thầu theo khối lượng phụ trách — trả về cho phancong_tool.xepHangNhaThau.
 * Nguồn: hop_dong_doi_tuong.nha_thau_id (nguồn chân lý phân công). */
@Data
public class PhanCongXepHangNhaThauToolItem {
    private String tenNhaThau;
    private long soDoiTuong;
    private long soDangVuongMac;
    private BigDecimal tongSanLuongHieuLuc;
}
