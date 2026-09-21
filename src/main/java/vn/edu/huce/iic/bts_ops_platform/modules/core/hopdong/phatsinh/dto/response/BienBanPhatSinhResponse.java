package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.response;

import lombok.Builder;
import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.BienBanPhatSinhChiTietItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BienBanPhatSinhResponse {
    private UUID id;
    private UUID hopDongId;
    private UUID hopDongDoiTuongId;
    private String maPhatSinh;
    private String tenDauViec;
    private String moTaLyDo;
    private BigDecimal khoiLuongPhatSinh;
    private String donVi;
    private LocalDate ngayLap;
    private UUID nguoiLapId;
    private String nguoiLapTen;
    private String trangThai;
    private String lyDoTuChoi;
    private UUID nguoiPheDuyetId;
    private String nguoiPheDuyetTen;
    private Instant ngayPheDuyet;
    private String loai;
    private List<BienBanPhatSinhChiTietItem> chiTiet;
    /** Ngày ký phụ lục hợp đồng phần phát sinh — chỉ có sau khi đã duyệt. */
    private LocalDate ngayKyPhuLuc;
    private List<BienBanPhatSinhTepDinhKemResponse> tepDinhKem;
}
