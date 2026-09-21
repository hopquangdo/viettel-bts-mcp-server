package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.request;

import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.BienBanPhatSinhChiTietItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BienBanPhatSinhTaoRequest {
    /** Trạm liên quan — bỏ trống nếu phát sinh áp dụng chung cho cả hợp đồng. */
    private UUID hopDongDoiTuongId;
    private String tenDauViec;
    private String moTaLyDo;
    private BigDecimal khoiLuongPhatSinh;
    private String donVi;
    /** Các dòng khối lượng phát sinh — có gửi thì tenDauViec/khoiLuongPhatSinh ở trên có thể bỏ
     * trống, hệ thống lấy dòng đầu làm đại diện. */
    private List<BienBanPhatSinhChiTietItem> chiTiet;
    /** Bỏ trống thì lấy ngày hiện tại. */
    private LocalDate ngayLap;
    /** PHAT_SINH | YEU_CAU_NGHIEM_THU — mặc định PHAT_SINH */
    private String loai;
}
