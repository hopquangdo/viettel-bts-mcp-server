package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response;

import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.common.dto.GeoRefResponse;

import java.time.Instant;
import java.util.UUID;

@Data
public class VuongMacResponse {
    private UUID id;
    private String ma;
    private UUID duLieuDoiTuongId;
    private UUID hopDongId;
    private String giaiDoan;
    private String kieuVuongMac;
    /** User chọn khi ghi nhận — có được tiếp tục bổ sung sản lượng khi VM còn mở. */
    private Boolean coTheBoSungSanLuong;
    private String moTa;
    private String moTaDayDu;
    private UUID nguoiBaoCaoId;
    private String tenNguoiBaoCao;
    private String trangThai;
    private UUID nguoiXuLyId;
    private String tenNguoiXuLy;
    private String ghiChuGiaiQuyet;
    private String lyDoTuChoi;
    private Boolean quaHan30Ngay;
    private UUID tepDinhKemId;
    private String anhUrl;
    private String maTram;
    private String maHopDong;
    private String tenHopDong;
    private UUID loaiHopDongId;
    private String tenLoaiHopDong;
    private String region;
    private String province;
    /** Id thật kèm mã/tên — dùng để lọc/gửi lại thay vì gửi text region/province ở trên. */
    private GeoRefResponse khuVuc;
    private GeoRefResponse tinh;
    private String tenDoiTuong;
    private String maDoiTuong;
    private String nhaThau;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
}
