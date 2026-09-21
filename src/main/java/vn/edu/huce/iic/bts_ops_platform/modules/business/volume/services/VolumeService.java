package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.services;

import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request.VolumeBoSungRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request.VolumeCauHinhNguongRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request.VolumeQuyetToanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeCauHinhNguongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeCanhBaoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeChiTietResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeHopDongRowResponse;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeKhuVucResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeProvinceRow;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeTramResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeTramRow;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface VolumeService {

    VolumeTongQuanResponse tongQuan(UUID loaiHopDongId, String search, BigDecimal heSo);

    VolumeTongQuanResponse tongQuan(
            UUID loaiHopDongId, String search, BigDecimal heSo, Map<UUID, BigDecimal> heSoOverrides);

    List<VolumeHopDongRowResponse> listHopDong(UUID loaiHopDongId, String search, BigDecimal heSo);

    List<VolumeHopDongRowResponse> listHopDong(
            UUID loaiHopDongId, String search, BigDecimal heSo, Map<UUID, BigDecimal> heSoOverrides);

    /** Bản phân trang thật của listHopDong() — dùng cho bảng danh sách hợp đồng trên trang Volume. */
    vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse<VolumeHopDongRowResponse> danhSach(
            UUID loaiHopDongId,
            String search,
            BigDecimal heSo,
            Map<UUID, BigDecimal> heSoOverrides,
            String statusFilter,
            Integer page,
            Integer size);

    VolumeChiTietResponse getChiTiet(UUID hopDongId);

    List<VolumeCanhBaoResponse> canhBao(UUID loaiHopDongId, BigDecimal heSo);

    VolumeKhuVucResponse khuVuc(UUID loaiHopDongId);

    VolumeKhuVucResponse khuVucByHopDong(UUID hopDongId, BigDecimal heSo);

    VolumeKhuVucResponse khuVuc(UUID loaiHopDongId, UUID hopDongId, BigDecimal heSo);

    /** Danh sách tỉnh (phân trang) của 1 khu vực trong 1 HĐ — tách khỏi khuVucByHopDong(). */
    PageResponse<VolumeProvinceRow> danhSachTinh(
            UUID hopDongId, String regionId, BigDecimal heSo, String variantFilter, Integer page, Integer size, String groupBy);

    /** Đếm theo tab lọc (all/shortage/surplus/normal) — dùng cho tab bar bảng chênh lệch theo tỉnh. */
    Map<String, Long> demTinh(UUID hopDongId, String regionId, BigDecimal heSo, String groupBy);

    VolumeTramResponse listTramByProvince(UUID hopDongId, String provinceKey, BigDecimal heSo);

    /** Danh sách trạm (phân trang) theo tỉnh trong 1 HĐ — tách khỏi listTramByProvince(). */
    PageResponse<VolumeTramRow> danhSachTram(
            UUID hopDongId, String provinceKey, BigDecimal heSo, String variantFilter, Integer page, Integer size);

    /** Đếm trạm theo tab lọc (all/alert/abnormal/shortage/ok) — dùng cho tab bar bảng chênh lệch theo trạm. */
    Map<String, Long> demTram(UUID hopDongId, String provinceKey, BigDecimal heSo);

    VolumeTramRow updateQuyetToan(UUID hopDongId, UUID hopDongDoiTuongId, VolumeQuyetToanRequest request, BigDecimal heSo);

    VolumeTramRow updateBoSungSanLuong(UUID hopDongId, UUID hopDongDoiTuongId, VolumeBoSungRequest request, BigDecimal heSo);

    VolumeCauHinhNguongResponse getCauHinhNguong();

    VolumeCauHinhNguongResponse saveCauHinhNguong(VolumeCauHinhNguongRequest request);
}
