package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiBuocResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class HopDongDanhSachHopDongItemResponse {

    private UUID id;
    private UUID loaiHopDongId;
    private UUID kieuHopDongId;
    private String maHopDong;
    private String ma;
    private String ten;
    private String benKyA;
    private Long giaTriHd;
    private LocalDate ngayThucHien;
    private Integer soNgayThucHien;
    private LocalDate hanHopDong;
    private Boolean hoatDong;
    private long soDoiTuong;
    /** Tư vấn thiết kế — chưa lên sản lượng. */
    private long chuaLamSanLuong;
    /** Tư vấn thiết kế — đã lên sản lượng, chưa hoàn thành. */
    private long dangLamSanLuong;
    /** Tư vấn thiết kế — đã hoàn thành (ngày HT / HT). */
    private long hoanThanhSanLuong;
    private double tyLeHoanThanh;
    /** Số đối tượng đã khởi công (có ngày thi công gần nhất) nhưng chưa hoàn thành và không cập nhật > 30 ngày. */
    private long soChamTienDo;
    /** Số đối tượng chưa có Biên bản số 1 (BB-01) nào. */
    private long soBienBanChuaLap;
    /** Số đối tượng có Biên bản số 1 mới nhất đang ở trạng thái chờ duyệt. */
    private long soBienBanChoDuyet;
    /** Số đối tượng có Biên bản số 1 mới nhất bị từ chối. */
    private long soBienBanTuChoi;
    /** Luồng trạng thái của kiểu HĐ gắn với hợp đồng — thay cho cột trạng thái cứng. */
    private List<LuongTrangThaiBuocResponse> luongTrangThaiBuoc = new ArrayList<>();
    private List<HopDongDanhSachTienDoBuocResponse> tienDoTheoTrangThai = new ArrayList<>();
    private List<HopDongThuocTinhResponse> thuocTinhGiaTri = new ArrayList<>();
    private Instant ngayTao;
    private Instant ngayCapNhat;
    /** active | archived — FC-07 lưu trữ hợp đồng */
    private String trangThaiLuuTru;
    private Boolean archived;
}
