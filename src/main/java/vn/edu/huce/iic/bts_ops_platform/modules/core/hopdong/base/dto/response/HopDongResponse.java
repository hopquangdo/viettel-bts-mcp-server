package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DoiTuongHopDongLienKetResponse;

@Data
public class HopDongResponse {
    private UUID id;
    private UUID loaiHopDongId;
    private UUID kieuHopDongId;
    private Short trangThaiPhapLy;
    private String trangThaiThiCong;
    private Boolean hoatDong;
    private String maHopDong;
    private String ma;
    private String ten;
    private String benKyA;
    private Long giaTriHd;
    private LocalDate ngayThucHien;
    private Integer soNgayThucHien;
    private LocalDate hanHopDong;
    private BigDecimal hesoNguong;
    /** Denormalize từ cây hạng mục — xem HopDong.tongThanhTienThiCong. */
    private BigDecimal tongThanhTienThiCong;
    private List<HopDongThuocTinhResponse> thuocTinhGiaTri = new ArrayList<>();
    /** Đối tượng quản lý đã gắn loại/kiểu HĐ (dùng tab danh sách trạm). */
    private List<DoiTuongHopDongLienKetResponse> doiTuongLienKet = new ArrayList<>();
    /** true = kiểu HĐ chưa gắn đối tượng quản lý (trừ HM thi công). */
    private Boolean thieuCauHinhDoiTuongQuanLy;
    private String cauHinhDoiTuongQuanLyMessage;
    private Instant ngayTao;
    private Instant ngayCapNhat;
    /** active | archived */
    private String trangThaiLuuTru;
    private Boolean archived;
}
