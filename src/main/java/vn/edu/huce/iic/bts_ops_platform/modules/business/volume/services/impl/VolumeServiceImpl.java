package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.DoiTuongHopDongLienKetSupport;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LoaiHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LoaiHopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucHopDongTreeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucNhomService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.HopDongObjectGeo;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.VolumeTongQuanAggregateRow;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScope;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScopeService;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.PaginationDefaults;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventContext;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuongBoSungLichSu;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongBoSungLichSuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.HopDongDoiTuongSnapshot;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongSnapshotService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.services.SanLuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.cache.VolumeCacheNames;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.constants.VolumeConstants;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.HangMucItemComputed;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.HangMucNhomComputed;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.HangMucThanhTienResult;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.ProvinceBucket;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request.VolumeBoSungRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request.VolumeCauHinhNguongRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request.VolumeQuyetToanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeCauHinhNguongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LoaiHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeCanhBaoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeChiTietResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeHangMucBreakdown;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeHopDongRowResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeKhuVucCard;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeKhuVucDetail;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeKhuVucResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeNhomBreakdown;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeProvinceAlert;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeProvinceRow;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeTramResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeTramRow;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.VolumeTinhToanHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.services.VolumeService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.util.HangMucThanhTienCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VolumeServiceImpl implements VolumeService {

    private static final List<String> REGION_COLORS = List.of(
            "#E31C23", "#2563eb", "#059669", "#d97706", "#9333ea", "#0891b2");

    private final HopDongService hopDongService;
    private final HangMucNhomService hangMucNhomService;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final HopDongDoiTuongSnapshotService hopDongDoiTuongSnapshotService;
    private final LoaiHopDongService loaiHopDongService;
    private final HopDongRepository hopDongRepository;
    private final ContractorScopeService contractorScopeService;
    private final vn.edu.huce.iic.bts_ops_platform.common.cache.CacheService cacheService;
    private final vn.edu.huce.iic.bts_ops_platform.config.AppCacheProperties cacheProperties;
    private final AppEventContext appEventContext;
    private final HopDongDoiTuongBoSungLichSuRepository boSungLichSuRepository;

    /** public để CacheService (Jackson) dùng làm Class<T> khi (de)serialize. */
    public record VolumeBatchContext(
            List<HopDongResponse> hopDongs,
            List<HopDongDoiTuongResponse> objects,
            Map<UUID, List<HopDongDoiTuongResponse>> objectsByHopDong,
            List<HopDongObjectGeo> geos,
            Map<UUID, List<HopDongObjectGeo>> geosByHopDong,
            Map<UUID, Long> objectCountByHopDong,
            // Precompute 1 lần để tránh O(N·M) khi lặp theo hợp đồng
            Map<UUID, HopDongDoiTuongResponse> objectById,
            Map<UUID, String> objectTypeByHopDong,
            Map<UUID, LoaiHopDongResponse> loaiHopDongById) {
    }

    @Transactional(readOnly = true)
    public VolumeTongQuanResponse tongQuan(UUID loaiHopDongId, String search, BigDecimal heSo) {
        return tongQuan(loaiHopDongId, search, heSo, Map.of());
    }

    @Transactional(readOnly = true)
    public VolumeTongQuanResponse tongQuan(
            UUID loaiHopDongId, String search, BigDecimal heSo, Map<UUID, BigDecimal> heSoOverrides) {
        BigDecimal defaultHeSo = resolveEffectiveDefaultHeSo(heSo);
        String keyword = EntityFilter.normalizeSearch(search);
        Map<UUID, BigDecimal> overrides = resolveEffectiveOverrides(heSoOverrides);
        UUID[] overrideIds = overrides.keySet().toArray(new UUID[0]);
        BigDecimal[] overrideHeSos = new BigDecimal[overrideIds.length];
        for (int i = 0; i < overrideIds.length; i++) {
            overrideHeSos[i] = overrides.get(overrideIds[i]);
        }

        Optional<ContractorScope> scope = contractorScopeService.currentScope();
        boolean hasScope = scope.isPresent();
        UUID[] scopeHopDongIds = hasScope ? scope.get().hopDongIds().toArray(new UUID[0]) : new UUID[0];

        VolumeTongQuanAggregateRow agg = hopDongRepository.tongQuanAggregate(
                loaiHopDongId, keyword,
                VolumeConstants.GCCC_LOAI_HOP_DONG_ID, VolumeConstants.XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID,
                defaultHeSo, overrideIds, overrideHeSos, hasScope, scopeHopDongIds);

        Map<UUID, Long> theoLoai = new LinkedHashMap<>();
        for (Object[] row : hopDongRepository.tongQuanTheoLoai(loaiHopDongId, keyword, hasScope, scopeHopDongIds)) {
            theoLoai.put((UUID) row[0], ((Number) row[1]).longValue());
        }

        long giaTriHd = agg.getGiaTriHopDong() != null ? agg.getGiaTriHopDong() : 0L;
        BigDecimal tongThiCong = VolumeTinhToanHelper.nz(agg.getTongThanhTienThiCong());

        return VolumeTongQuanResponse.builder()
                .soHopDong(agg.getSoHopDong())
                .giaTriHopDong(giaTriHd)
                .tongThanhTienThiCong(tongThiCong)
                .chenhLech(tongThiCong.subtract(BigDecimal.valueOf(giaTriHd)))
                .tyLeSuDung(VolumeTinhToanHelper.ratio(tongThiCong, BigDecimal.valueOf(giaTriHd)))
                .soHopDongVuotNguong(agg.getSoVuotNguong())
                .soHopDongCanhBao(agg.getSoCanhBao())
                .soHopDongThieu(agg.getSoThieu())
                .soHopDongThua(agg.getSoThua())
                .soHopDongCanBang(agg.getSoCanBang())
                .tongQuyetToan(VolumeTinhToanHelper.nz(agg.getTongQuyetToan()))
                .soHopDongDaQuyetToan(agg.getSoDaQuyetToan())
                .soHopDongDangQuyetToan(agg.getSoDangQuyetToan())
                .soHopDongChuaQuyetToan(agg.getSoChuaQuyetToan())
                .tongTram(agg.getTongTram())
                .soHopDongDaThiCong(agg.getSoHopDongDaThiCong())
                .tongTramDaThiCong(agg.getTongTramDaThiCong())
                .soTramBatThuong(agg.getSoTramBatThuong())
                .soHopDongThieuLon(agg.getSoHopDongThieuLon())
                .tongTramDaQuyetToan(agg.getTongTramDaQuyetToan())
                .tongTramDangQuyetToan(agg.getTongTramDangQuyetToan())
                .tongTramChuaQuyetToan(agg.getTongTramChuaQuyetToan())
                .giaTriDaQuyetToan(agg.getGiaTriDaQuyetToan() != null ? agg.getGiaTriDaQuyetToan() : 0L)
                .giaTriDangQuyetToan(agg.getGiaTriDangQuyetToan() != null ? agg.getGiaTriDangQuyetToan() : 0L)
                .giaTriChuaQuyetToan(agg.getGiaTriChuaQuyetToan() != null ? agg.getGiaTriChuaQuyetToan() : 0L)
                .soHopDongTheoLoai(theoLoai)
                .build();
    }

    @Transactional(readOnly = true)
    public List<VolumeHopDongRowResponse> listHopDong(UUID loaiHopDongId, String search, BigDecimal heSo) {
        return listHopDong(loaiHopDongId, search, heSo, Map.of());
    }

    /** Xử lý tối đa N HĐ/lượt khi cần quét hết (lọc trạng thái) — tránh load 15k+ trạm một lần. */
    private static final int HOP_DONG_VOLUME_CHUNK = 10;

    @Override
    @Transactional(readOnly = true)
    public vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse<VolumeHopDongRowResponse> danhSach(
            UUID loaiHopDongId,
            String search,
            BigDecimal heSo,
            Map<UUID, BigDecimal> heSoOverrides,
            String statusFilter,
            Integer page,
            Integer size) {
        BigDecimal defaultHeSo = resolveEffectiveDefaultHeSo(heSo);
        Map<UUID, BigDecimal> overrides = resolveEffectiveOverrides(heSoOverrides);
        String status = statusFilter == null || statusFilter.isBlank()
                ? "all" : statusFilter.trim().toLowerCase(Locale.ROOT);

        List<HopDongResponse> allHopDongs = loadHopDongs(loaiHopDongId, null, search);
        int pageSize = size == null || size <= 0 ? PaginationDefaults.DEFAULT_PAGE_SIZE : Math.min(size, PaginationDefaults.MAX_PAGE_SIZE);
        int pageNumber = page == null || page < 0 ? 0 : page;

        if ("all".equals(status)) {
            int from = Math.min(pageNumber * pageSize, allHopDongs.size());
            int to = Math.min(from + pageSize, allHopDongs.size());
            List<HopDongResponse> pageHopDongs = allHopDongs.subList(from, to);
            VolumeBatchContext ctx = computeLiteBatchForHopDongs(pageHopDongs);
            List<VolumeHopDongRowResponse> rows = buildRows(pageHopDongs, defaultHeSo, overrides, ctx);
            return vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse.ofItems(
                    rows, pageNumber, pageSize, allHopDongs.size());
        }

        List<VolumeHopDongRowResponse> allRows = buildRowsInChunks(allHopDongs, defaultHeSo, overrides);
        List<VolumeHopDongRowResponse> filtered = filterHopDongRows(allRows, status);
        int from = Math.min(pageNumber * pageSize, filtered.size());
        int to = Math.min(from + pageSize, filtered.size());
        return vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse.ofItems(
                filtered.subList(from, to), pageNumber, pageSize, filtered.size());
    }

    @Transactional(readOnly = true)
    public List<VolumeHopDongRowResponse> listHopDong(
            UUID loaiHopDongId, String search, BigDecimal heSo, Map<UUID, BigDecimal> heSoOverrides) {
        BigDecimal defaultHeSo = resolveEffectiveDefaultHeSo(heSo);
        Map<UUID, BigDecimal> overrides = resolveEffectiveOverrides(heSoOverrides);
        List<HopDongResponse> hopDongs = loadHopDongs(loaiHopDongId, null, search);
        return buildRowsInChunks(hopDongs, defaultHeSo, overrides);
    }

    private List<VolumeHopDongRowResponse> buildRowsInChunks(
            List<HopDongResponse> hopDongs,
            BigDecimal defaultHeSo,
            Map<UUID, BigDecimal> overrides) {
        if (hopDongs.isEmpty()) {
            return List.of();
        }
        List<VolumeHopDongRowResponse> rows = new ArrayList<>(hopDongs.size());
        for (int i = 0; i < hopDongs.size(); i += HOP_DONG_VOLUME_CHUNK) {
            List<HopDongResponse> chunk = hopDongs.subList(i, Math.min(i + HOP_DONG_VOLUME_CHUNK, hopDongs.size()));
            VolumeBatchContext ctx = computeLiteBatchForHopDongs(chunk);
            rows.addAll(buildRows(chunk, defaultHeSo, overrides, ctx));
        }
        return rows;
    }

    private List<VolumeHopDongRowResponse> buildRows(
            List<HopDongResponse> hopDongs,
            BigDecimal defaultHeSo,
            Map<UUID, BigDecimal> overrides,
            VolumeBatchContext ctx) {
        List<VolumeHopDongRowResponse> rows = new ArrayList<>(hopDongs.size());
        for (HopDongResponse hopDong : hopDongs) {
            BigDecimal rowHeSo = resolveRowHeSo(hopDong, defaultHeSo, overrides);
            rows.add(toRowFromBatch(hopDong, rowHeSo, ctx));
        }
        return rows;
    }

    /** Chủ đầu tư là thuộc tính động của HĐ — khớp tên y hệt cách module Tiến độ hợp đồng resolve client-side. */
    private static String resolveChuDauTu(HopDongResponse hopDong) {
        List<HopDongThuocTinhResponse> thuocTinh = hopDong.getThuocTinhGiaTri();
        if (thuocTinh == null) {
            return "—";
        }
        for (HopDongThuocTinhResponse attr : thuocTinh) {
            String ten = attr.getTenThuocTinh() != null ? attr.getTenThuocTinh().toLowerCase(Locale.ROOT) : "";
            if (ten.contains("chủ đầu tư") || ten.contains("chu dau tu")) {
                String giaTri = attr.getGiaTri();
                return giaTri != null && !giaTri.isBlank() ? giaTri : "—";
            }
        }
        return "—";
    }

    private static List<VolumeHopDongRowResponse> filterHopDongRows(
            List<VolumeHopDongRowResponse> rows, String status) {
        return switch (status) {
            case "thieu" -> rows.stream().filter(r -> "thieu".equals(r.getTrangThaiVolume())).toList();
            case "thua" -> rows.stream().filter(r -> "thua".equals(r.getTrangThaiVolume())).toList();
            case "can_bang" -> rows.stream().filter(r -> "can_bang".equals(r.getTrangThaiVolume())).toList();
            case "alert" -> rows.stream().filter(r -> "danger".equals(r.getAlertLevel())).toList();
            case "da_qt" -> rows.stream().filter(r -> "da_qt".equals(r.getTrangThaiQt())).toList();
            case "dang_qt" -> rows.stream().filter(r -> "dang_qt".equals(r.getTrangThaiQt())).toList();
            case "chua_qt" -> rows.stream().filter(r -> "chua_qt".equals(r.getTrangThaiQt())).toList();
            default -> rows;
        };
    }

    @Transactional(readOnly = true)
    public VolumeChiTietResponse getChiTiet(UUID hopDongId) {
        HopDongResponse hopDong = hopDongService.getById(hopDongId);

        HangMucHopDongTreeResponse tree = hangMucNhomService.getTreeByHopDongId(hopDongId, true);
        HangMucThanhTienResult computed = HangMucThanhTienCalculator.compute(tree);

        long giaTriHd = hopDong.getGiaTriHd() != null ? hopDong.getGiaTriHd() : 0L;
        List<VolumeNhomBreakdown> nhomRows = new ArrayList<>();
        for (HangMucNhomComputed nhom : computed.getNhom()) {
            List<VolumeHangMucBreakdown> hangMucRows = new ArrayList<>();
            for (HangMucItemComputed hangMuc : nhom.getHangMuc()) {
                hangMucRows.add(VolumeHangMucBreakdown.builder()
                        .id(hangMuc.getId())
                        .ma(hangMuc.getMa())
                        .ten(hangMuc.getTen())
                        .khoiLuong(hangMuc.getKhoiLuong())
                        .donGia(hangMuc.getDonGia())
                        .thanhTien(hangMuc.getThanhTien())
                        .build());
            }
            nhomRows.add(VolumeNhomBreakdown.builder()
                    .id(nhom.getId())
                    .ma(nhom.getMa())
                    .ten(nhom.getTen())
                    .thanhTien(nhom.getThanhTien())
                    .hangMuc(hangMucRows)
                    .build());
        }

        return VolumeChiTietResponse.builder()
                .hopDongId(hopDong.getId())
                .maHopDong(hopDong.getMaHopDong())
                .ten(hopDong.getTen())
                .giaTriHd(giaTriHd)
                .tongThanhTienThiCong(computed.getTongThanhTien())
                .chenhLech(VolumeTinhToanHelper.nz(computed.getTongThanhTien()).subtract(BigDecimal.valueOf(giaTriHd)))
                .tyLeSuDung(VolumeTinhToanHelper.ratio(computed.getTongThanhTien(), BigDecimal.valueOf(giaTriHd)))
                .trangThai(VolumeTinhToanHelper.resolveStatus(giaTriHd, computed.getTongThanhTien()))
                .nhom(nhomRows)
                .build();
    }

    @Transactional(readOnly = true)
    public List<VolumeCanhBaoResponse> canhBao(UUID loaiHopDongId, BigDecimal heSo) {
        List<VolumeHopDongRowResponse> rows = listHopDong(loaiHopDongId, null, heSo);
        List<VolumeCanhBaoResponse> alerts = new ArrayList<>();
        for (VolumeHopDongRowResponse row : rows) {
            if (row.getTramBatThuong() <= 0 && !"danger".equals(row.getAlertLevel())
                    && !"warning".equals(row.getAlertLevel())) {
                continue;
            }
            alerts.add(VolumeCanhBaoResponse.builder()
                    .id(row.getHopDongId() != null ? row.getHopDongId().toString() : null)
                    .hopDongId(row.getHopDongId())
                    .maHopDong(row.getMaHopDong())
                    .ten(row.getTen())
                    .type("abnormal")
                    .stationCount(row.getTramBatThuong())
                    .tramThieu(row.getTramThieu())
                    .tramThua(row.getTramThua())
                    .alertLevel(row.getAlertLevel())
                    .alertText(row.getAlertText())
                    .trangThaiVolume(row.getTrangThaiVolume())
                    .build());
        }
        return alerts;
    }

    @Transactional(readOnly = true)
    public VolumeKhuVucResponse khuVuc(UUID loaiHopDongId) {
        return khuVuc(loaiHopDongId, null, VolumeTinhToanHelper.DEFAULT_HESO_GCCC);
    }

    @Transactional(readOnly = true)
    public VolumeKhuVucResponse khuVucByHopDong(UUID hopDongId, BigDecimal heSo) {
        hopDongService.getById(hopDongId);
        return khuVuc(null, hopDongId, VolumeTinhToanHelper.resolveHeSo(heSo));
    }

    @Transactional(readOnly = true)
    public VolumeKhuVucResponse khuVuc(UUID loaiHopDongId, UUID hopDongId, BigDecimal heSo) {
        RegionComputation computed = computeRegions(loaiHopDongId, hopDongId, heSo, null);
        return VolumeKhuVucResponse.builder()
                .tongKhuVuc(computed.cards().size())
                .duLieu(computed.cards())
                .canhBaoThieu(computed.canhBaoThieu())
                .canhBaoThua(computed.canhBaoThua())
                .chiTiet(computed.details())
                .build();
    }

    /** public để CacheService (Jackson) — không dùng, chỉ nhóm dữ liệu tính 1 lần cho khuVuc()/danhSachTinh(). */
    private record RegionComputation(
            List<VolumeKhuVucCard> cards,
            List<VolumeKhuVucDetail> details,
            List<VolumeProvinceAlert> canhBaoThieu,
            List<VolumeProvinceAlert> canhBaoThua,
            Map<String, List<VolumeProvinceRow>> rowsByRegionId) {
    }

    /**
     * Danh sách tỉnh (phân trang) của 1 khu vực trong 1 HĐ — tách khỏi khuVucByHopDong() vì
     * VolumeKhuVucDetail không còn kèm "rows" nữa. regionId null/rỗng → khu vực đầu tiên
     * (defaultOpen).
     */
    @Transactional(readOnly = true)
    public vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse<VolumeProvinceRow> danhSachTinh(
            UUID hopDongId, String regionId, BigDecimal heSo, String variantFilter, Integer page, Integer size,
            String groupBy) {
        hopDongService.getById(hopDongId);
        List<VolumeProvinceRow> rows = tinhRowsForRegion(hopDongId, regionId, heSo, groupBy);
        List<VolumeProvinceRow> filtered = filterProvinceRows(rows, variantFilter);

        int pageSize = size == null || size <= 0 ? PaginationDefaults.DEFAULT_PAGE_SIZE : Math.min(size, PaginationDefaults.MAX_PAGE_SIZE);
        int pageNumber = page == null || page < 0 ? 0 : page;
        int from = Math.min(pageNumber * pageSize, filtered.size());
        int to = Math.min(from + pageSize, filtered.size());
        return vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse.ofItems(
                filtered.subList(from, to), pageNumber, pageSize, filtered.size());
    }

    /** Đếm theo tab lọc (all/shortage/surplus/normal) cho tab bar của VolumeProvinceTable — rẻ, không phân trang. */
    @Transactional(readOnly = true)
    public Map<String, Long> demTinh(UUID hopDongId, String regionId, BigDecimal heSo, String groupBy) {
        hopDongService.getById(hopDongId);
        List<VolumeProvinceRow> rows = tinhRowsForRegion(hopDongId, regionId, heSo, groupBy);
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("all", (long) rows.size());
        counts.put("shortage", (long) filterProvinceRows(rows, "shortage").size());
        counts.put("surplus", (long) filterProvinceRows(rows, "surplus").size());
        counts.put("normal", (long) filterProvinceRows(rows, "normal").size());
        return counts;
    }

    private List<VolumeProvinceRow> tinhRowsForRegion(UUID hopDongId, String regionId, BigDecimal heSo, String groupBy) {
        RegionComputation computed = computeRegions(null, hopDongId, heSo, groupBy);
        String targetId = regionId != null && !regionId.isBlank()
                ? regionId
                : computed.details().isEmpty() ? null : computed.details().get(0).getId();
        return targetId != null ? computed.rowsByRegionId().getOrDefault(targetId, List.of()) : List.of();
    }

    private static List<VolumeProvinceRow> filterProvinceRows(List<VolumeProvinceRow> rows, String variantFilter) {
        String filter = variantFilter == null || variantFilter.isBlank() ? "all" : variantFilter.trim().toLowerCase(Locale.ROOT);
        return switch (filter) {
            case "shortage" -> rows.stream()
                    .filter(r -> r.getShortageStations() > 0 || "shortage".equals(r.getVariant()))
                    .toList();
            case "surplus" -> rows.stream()
                    .filter(r -> r.getSurplusStations() > 0 || "surplus".equals(r.getVariant()))
                    .toList();
            case "normal" -> rows.stream()
                    .filter(r -> "normal".equals(r.getVariant()) || "warning".equals(r.getVariant()))
                    .toList();
            default -> rows;
        };
    }

    private RegionComputation computeRegions(UUID loaiHopDongId, UUID hopDongId, BigDecimal heSo, String groupBy) {
        BigDecimal effectiveHeSo = VolumeTinhToanHelper.resolveHeSo(heSo);
        VolumeBatchContext ctx = loadBatch(loaiHopDongId, hopDongId, null);
        if (ctx.hopDongs().isEmpty()) {
            return new RegionComputation(List.of(), List.of(), List.of(), List.of(), Map.of());
        }

        Map<UUID, VolumeHopDongRowResponse> hopDongById = new LinkedHashMap<>();
        for (HopDongResponse hopDong : ctx.hopDongs()) {
            VolumeHopDongRowResponse row = toRowFromBatch(hopDong, effectiveHeSo, ctx);
            hopDongById.put(row.getHopDongId(), row);
        }

        List<HopDongObjectGeo> geos = ctx.geos();
        if (geos.isEmpty()) {
            return new RegionComputation(List.of(), List.of(), List.of(), List.of(), Map.of());
        }

        Map<UUID, HopDongDoiTuongResponse> objectById = ctx.objectById();

        Map<String, Map<String, ProvinceBucket>> regionMap = new LinkedHashMap<>();
        for (HopDongObjectGeo geo : geos) {
            VolumeHopDongRowResponse hd = hopDongById.get(geo.hopDongId());
            if (hd == null) continue;

            // Định mức / 1 trạm = thành tiền cây hạng mục (giaTriBinhQuan). KHÔNG fallback sang
            // hd.getTongThanhTienThiCong() — trên VolumeHopDongRowResponse field này đã bị dùng
            // lại với nghĩa KHÁC (tổng sản lượng thực tế, xem toRowFromBatch), không phải định
            // mức catalog — fallback nhầm field từng khiến so 1 trạm với tổng SL cả hợp đồng.
            BigDecimal planShare = VolumeTinhToanHelper.nz(hd.getGiaTriBinhQuan());
            HopDongDoiTuongResponse objForShare = objectById.get(geo.hopDongDoiTuongId());
            BigDecimal actualShare = VolumeTinhToanHelper.nz(
                    objForShare != null ? objForShare.getSanLuongHieuLuc() : null);

            // Gộp theo mã tỉnh (HG, NBH…) — không dùng provinceKey UUID từng trạm
            // (resolveObjectGeos set provinceKey = id trạm khi suy tỉnh từ mã trạm).
            // groupBy=contractor: gộp theo nhà thầu (khóa NT::), dòng "tỉnh" thành dòng nhà thầu.
            boolean byContractor = "contractor".equalsIgnoreCase(groupBy == null ? "" : groupBy.trim());
            String groupKey = byContractor
                    ? contractorGroupKey(geo)
                    : VolumeTinhToanHelper.volumeProvinceGroupKey(geo);
            Map<String, ProvinceBucket> provinces =
                    regionMap.computeIfAbsent(geo.region(), ignored -> new LinkedHashMap<>());
            ProvinceBucket bucket = provinces.computeIfAbsent(
                    groupKey,
                    ignored -> byContractor
                            ? new ProvinceBucket(
                                    geo.region(), contractorDisplay(geo), groupKey, "", contractorDisplay(geo))
                            : new ProvinceBucket(
                                    geo.region(), geo.province(), groupKey, geo.oldProvince(), geo.contractor()));
            bucket.stations += 1;
            if (actualShare.compareTo(BigDecimal.ZERO) > 0) {
                bucket.stationsWithOutput += 1;
            }
            // Tính thiếu/thừa cho mọi trạm đã có định mức catalog thật (planShare > 0) — KỂ CẢ
            // trạm chưa có sản lượng nào (actualShare = 0): định mức dương mà thực tế = 0 nghĩa
            // là thiếu toàn bộ, phải tính vào "thiếu", không loại trừ khỏi thống kê nữa.
            // planShare = 0 nghĩa là chưa recompute tongThanhTienThiCong cho hợp đồng này, so với
            // 0 sẽ luôn ra kết quả vô nghĩa nên vẫn bỏ qua trường hợp này.
            if (planShare.compareTo(BigDecimal.ZERO) > 0) {
                String maTram = geo.maDoiTuong();
                BigDecimal delta = planShare.subtract(actualShare);
                if (delta.compareTo(BigDecimal.ZERO) > 0) {
                    bucket.shortageStations += 1;
                    bucket.shortageValue = bucket.shortageValue.add(delta);
                    bucket.shortageCodes.add(maTram);
                } else if (delta.compareTo(BigDecimal.ZERO) < 0) {
                    // "Thừa" khớp tiêu chí với card tổng hợp đồng (toRowFromBatch.tramThua):
                    // actualShare > planShare — KHÔNG dùng threshold (planShare × hệ số ngưỡng),
                    // vì đó là tiêu chí "bất thường" (tramBatThuong), khác khái niệm "thừa".
                    bucket.surplusStations += 1;
                    bucket.surplusValue = bucket.surplusValue.add(delta.abs());
                    bucket.surplusCodes.add(maTram);
                }
            }
            // contractValue ở đây = tổng định mức HM theo tỉnh (sum planShare), không phải giaTriHd
            bucket.contractValue = bucket.contractValue.add(planShare);
            bucket.constructionValue = bucket.constructionValue.add(actualShare);
        }

        List<VolumeKhuVucCard> cards = new ArrayList<>();
        List<VolumeKhuVucDetail> details = new ArrayList<>();
        List<VolumeProvinceAlert> canhBaoThieu = new ArrayList<>();
        List<VolumeProvinceAlert> canhBaoThua = new ArrayList<>();
        Map<String, List<VolumeProvinceRow>> rowsByRegionId = new LinkedHashMap<>();

        int regionIndex = 0;
        for (Map.Entry<String, Map<String, ProvinceBucket>> regionEntry : regionMap.entrySet()) {
            String regionName = regionEntry.getKey();
            String color = REGION_COLORS.get(regionIndex % REGION_COLORS.size());
            String regionId = "kv-" + regionIndex;

            BigDecimal regionContract = BigDecimal.ZERO;
            BigDecimal regionConstruction = BigDecimal.ZERO;
            long regionStations = 0;
            long regionShortageStations = 0;
            long regionSurplusStations = 0;
            int alertCount = 0;

            List<VolumeProvinceRow> provinceRows = new ArrayList<>();
            int order = 1;
            for (ProvinceBucket bucket : regionEntry.getValue().values()) {
                BigDecimal remaining = bucket.contractValue.subtract(bucket.constructionValue);
                BigDecimal usedPercent = VolumeTinhToanHelper.ratio(bucket.constructionValue, bucket.contractValue);
                String variant = VolumeTinhToanHelper.resolveProvinceVariant(
                        bucket.shortageStations, bucket.surplusStations, remaining, usedPercent);
                BigDecimal shortage = bucket.shortageValue;
                BigDecimal surplus = bucket.surplusValue;

                VolumeProvinceRow row = VolumeProvinceRow.builder()
                        .id(regionId + "-" + order)
                        .order(order++)
                        .province(bucket.province)
                        .provinceKey(bucket.provinceKey)
                        .legacyCode(bucket.oldProvince == null || bucket.oldProvince.isBlank() ? "—" : bucket.oldProvince)
                        .nhaThau(bucket.contractor == null || bucket.contractor.isBlank() ? "—" : bucket.contractor)
                        .contractStations(bucket.stations)
                        .constructionStations(bucket.stationsWithOutput)
                        .shortageStations(bucket.shortageStations)
                        .surplusStations(bucket.surplusStations)
                        .contractValue(bucket.contractValue)
                        .constructionValue(bucket.constructionValue)
                        .remainingValue(remaining.max(BigDecimal.ZERO))
                        .quotaStations(bucket.stations)
                        .quotaValue(bucket.contractValue)
                        .shortage(shortage)
                        .surplus(surplus)
                        .avgExceeded(usedPercent)
                        .variant(variant)
                        .build();
                provinceRows.add(row);

                regionContract = regionContract.add(bucket.contractValue);
                regionConstruction = regionConstruction.add(bucket.constructionValue);
                regionStations += bucket.stations;
                regionShortageStations += bucket.shortageStations;
                regionSurplusStations += bucket.surplusStations;
                if (bucket.shortageStations > 0 || bucket.surplusStations > 0
                        || "shortage".equals(variant) || "surplus".equals(variant) || "warning".equals(variant)) {
                    alertCount += 1;
                }

                if (bucket.shortageStations > 0) {
                    canhBaoThieu.add(VolumeProvinceAlert.builder()
                            .id(row.getId() + "-thieu")
                            .province(bucket.province)
                            .region(regionName)
                            .stationCount(bucket.shortageStations)
                            .volumeValue(shortage)
                            .avgValue(bucket.shortageStations > 0
                                    ? shortage.divide(BigDecimal.valueOf(bucket.shortageStations), VolumeTinhToanHelper.MC)
                                    : BigDecimal.ZERO)
                            .type("shortage")
                            .stationCodes(new ArrayList<>(bucket.shortageCodes))
                            .build());
                }
                if (bucket.surplusStations > 0) {
                    canhBaoThua.add(VolumeProvinceAlert.builder()
                            .id(row.getId() + "-thua")
                            .province(bucket.province)
                            .region(regionName)
                            .stationCount(bucket.surplusStations)
                            .volumeValue(surplus)
                            .avgValue(bucket.surplusStations > 0
                                    ? surplus.divide(BigDecimal.valueOf(bucket.surplusStations), VolumeTinhToanHelper.MC)
                                    : BigDecimal.ZERO)
                            .type("surplus")
                            .stationCodes(new ArrayList<>(bucket.surplusCodes))
                            .build());
                }
            }

            details.add(VolumeKhuVucDetail.builder()
                    .id(regionId)
                    .name(regionName)
                    .color(color)
                    .defaultOpen(regionIndex == 0)
                    .provinceCount(provinceRows.size())
                    .alertCount(alertCount)
                    .build());
            rowsByRegionId.put(regionId, provinceRows);

            BigDecimal remaining = regionContract.subtract(regionConstruction);
            BigDecimal usedPercent = VolumeTinhToanHelper.ratio(regionConstruction, regionContract);
            cards.add(VolumeKhuVucCard.builder()
                    .id(regionId)
                    .name(regionName)
                    .color(color)
                    .usedPercent(usedPercent)
                    .barColor(remaining.compareTo(BigDecimal.ZERO) < 0 ? "#ef4444" : "#059669")
                    .contractValue(regionContract)
                    .constructionValue(regionConstruction)
                    .remainingValue(remaining.max(BigDecimal.ZERO))
                    .chenhLech(regionConstruction.subtract(regionContract))
                    .avgPerStation(regionStations > 0
                            ? regionConstruction.divide(BigDecimal.valueOf(regionStations), VolumeTinhToanHelper.MC)
                            : BigDecimal.ZERO)
                    .quotaStations(regionStations)
                    .shortageStations(regionShortageStations)
                    .surplusStations(regionSurplusStations)
                    .build());

            regionIndex += 1;
        }

        canhBaoThieu.sort(Comparator.comparing(
                (VolumeProvinceAlert a) -> VolumeTinhToanHelper.nz(a.getVolumeValue()).abs()).reversed());
        canhBaoThua.sort(Comparator.comparing(
                (VolumeProvinceAlert a) -> VolumeTinhToanHelper.nz(a.getVolumeValue()).abs()).reversed());

        return new RegionComputation(cards, details, canhBaoThieu, canhBaoThua, rowsByRegionId);
    }

    private record TramComputation(
            String province, String region, BigDecimal hmThiCong, BigDecimal nguong, List<VolumeTramRow> rows) {
    }

    @Transactional(readOnly = true)
    public VolumeTramResponse listTramByProvince(
            UUID hopDongId, String provinceKey, BigDecimal heSo) {
        hopDongService.getById(hopDongId);
        VolumeBatchContext ctx = loadBatch(null, hopDongId, null);
        TramComputation computed = computeTram(hopDongId, provinceKey, heSo, ctx);

        return VolumeTramResponse.builder()
                .hopDongId(hopDongId)
                .province(computed.province())
                .region(computed.region())
                .doiTuongTen(ctx.objectTypeByHopDong().get(hopDongId))
                .giaTriBinhQuan(computed.hmThiCong())
                .heSoNguong(VolumeTinhToanHelper.resolveHeSo(heSo))
                .nguongCanhBao(computed.nguong())
                .build();
    }

    /** Danh sách trạm (phân trang) theo tỉnh trong 1 HĐ — tách khỏi listTramByProvince(). */
    @Transactional(readOnly = true)
    public vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse<VolumeTramRow> danhSachTram(
            UUID hopDongId, String provinceKey, BigDecimal heSo, String variantFilter, Integer page, Integer size) {
        hopDongService.getById(hopDongId);
        VolumeBatchContext ctx = loadBatch(null, hopDongId, null);
        List<VolumeTramRow> rows = computeTram(hopDongId, provinceKey, heSo, ctx).rows();
        List<VolumeTramRow> filtered = filterTramRows(rows, variantFilter);

        int pageSize = size == null || size <= 0 ? PaginationDefaults.DEFAULT_PAGE_SIZE : Math.min(size, PaginationDefaults.MAX_PAGE_SIZE);
        int pageNumber = page == null || page < 0 ? 0 : page;
        int from = Math.min(pageNumber * pageSize, filtered.size());
        int to = Math.min(from + pageSize, filtered.size());
        return vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse.ofItems(
                filtered.subList(from, to), pageNumber, pageSize, filtered.size());
    }

    /** Đếm theo tab lọc (all/alert/abnormal/shortage/ok) cho tab bar của VolumeStationTable — rẻ, không phân trang. */
    @Transactional(readOnly = true)
    public Map<String, Long> demTram(UUID hopDongId, String provinceKey, BigDecimal heSo) {
        hopDongService.getById(hopDongId);
        VolumeBatchContext ctx = loadBatch(null, hopDongId, null);
        List<VolumeTramRow> rows = computeTram(hopDongId, provinceKey, heSo, ctx).rows();
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("all", (long) rows.size());
        counts.put("alert", (long) filterTramRows(rows, "alert").size());
        counts.put("abnormal", (long) filterTramRows(rows, "abnormal").size());
        counts.put("shortage", (long) filterTramRows(rows, "shortage").size());
        counts.put("ok", (long) filterTramRows(rows, "ok").size());
        return counts;
    }

    private static List<VolumeTramRow> filterTramRows(List<VolumeTramRow> rows, String variantFilter) {
        String filter = variantFilter == null || variantFilter.isBlank() ? "all" : variantFilter.trim().toLowerCase(Locale.ROOT);
        return switch (filter) {
            case "alert", "abnormal" -> rows.stream().filter(VolumeTramRow::isBatThuong).toList();
            case "shortage" -> rows.stream().filter(r -> "shortage".equals(r.getVariant())).toList();
            case "ok" -> rows.stream().filter(r -> !r.isBatThuong()).toList();
            default -> rows;
        };
    }

    /** Nhóm gom "theo nhà thầu" — tên hiển thị (trống → nhóm chờ gán). */
    private static String contractorDisplay(HopDongObjectGeo geo) {
        String contractor = geo.contractor() == null ? "" : geo.contractor().trim();
        return contractor.isBlank() ? "Chưa gán nhà thầu" : contractor;
    }

    /** Khóa gom theo nhà thầu, tiền tố NT:: — provinceKey mang khóa này đi FE rồi quay lại
     * các endpoint trạm, computeTram tự nhận diện mode từ tiền tố, không cần param riêng. */
    private static String contractorGroupKey(HopDongObjectGeo geo) {
        return "NT::" + contractorDisplay(geo).toUpperCase(Locale.ROOT);
    }

    /** Định mức bình quân / 1 trạm = tổng thành tiền cây hạng mục ÷ số trạm (0 trạm → 0). */
    private static BigDecimal chiaBinhQuanTram(BigDecimal tong, int soTram) {
        if (soTram <= 0 || tong.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return tong.divide(BigDecimal.valueOf(soTram), VolumeTinhToanHelper.MC);
    }

    private TramComputation computeTram(UUID hopDongId, String provinceKey, BigDecimal heSo, VolumeBatchContext ctx) {
        BigDecimal effectiveHeSo = VolumeTinhToanHelper.resolveHeSo(heSo);
        // Định mức so per-trạm = tổng cây hạng mục CHIA số trạm (xem toRowFromBatch.planPerStation)
        // — dùng thẳng tổng cả hợp đồng từng khiến mỗi trạm bị so với định mức của cả HĐ.
        BigDecimal tongCayHangMuc = ctx.hopDongs().stream()
                .filter(h -> hopDongId.equals(h.getId()))
                .findFirst()
                .map(h -> VolumeTinhToanHelper.nz(h.getTongThanhTienThiCong()))
                .orElse(BigDecimal.ZERO);
        BigDecimal hmThiCong = chiaBinhQuanTram(
                tongCayHangMuc, ctx.objectsByHopDong().getOrDefault(hopDongId, List.of()).size());
        BigDecimal nguong = effectiveHeSo != null ? hmThiCong.multiply(effectiveHeSo, VolumeTinhToanHelper.MC) : null;

        Map<UUID, HopDongDoiTuongResponse> objectById = ctx.objectById();

        String normalizedKey = provinceKey == null ? "" : provinceKey.trim();

        String province = null;
        String region = null;
        List<VolumeTramRow> tramRows = new ArrayList<>();

        // Khóa NT:: = drill-in từ dòng gom theo nhà thầu (xem contractorGroupKey) — lọc theo nhà thầu.
        boolean byContractor = normalizedKey.regionMatches(true, 0, "NT::", 0, 4);
        for (HopDongObjectGeo geo : ctx.geos()) {
            String groupKey = VolumeTinhToanHelper.volumeProvinceGroupKey(geo);
            boolean match = byContractor
                    ? normalizedKey.equalsIgnoreCase(contractorGroupKey(geo))
                    : normalizedKey.isBlank()
                            || normalizedKey.equalsIgnoreCase(groupKey)
                            || normalizedKey.equalsIgnoreCase(geo.province())
                            || normalizedKey.equalsIgnoreCase(geo.provinceKey());
            if (!match) continue;

            if (province == null) {
                province = byContractor ? contractorDisplay(geo) : geo.province();
                region = geo.region();
            }

            HopDongDoiTuongResponse obj = objectById.get(geo.hopDongDoiTuongId());
            BigDecimal boSung = VolumeTinhToanHelper.nz(obj != null ? obj.getBoSungSanLuong() : null);
            BigDecimal sanLuongHieuLuc = VolumeTinhToanHelper.nz(obj != null ? obj.getSanLuongHieuLuc() : null);
            BigDecimal sanLuong = sanLuongHieuLuc.subtract(boSung);
            // Quy ước chung: âm = thiếu, dương = thừa → thực tế trừ kế hoạch (không phải ngược lại).
            BigDecimal chenhLech = sanLuongHieuLuc.subtract(hmThiCong);
            BigDecimal quyetToan = obj != null ? obj.getQuyetToanThuc() : null;
            BigDecimal chenhLechQt = quyetToan != null ? quyetToan.subtract(hmThiCong) : null;
            boolean batThuong = nguong != null && nguong.compareTo(BigDecimal.ZERO) > 0 && sanLuongHieuLuc.compareTo(nguong) > 0;
            String variant = VolumeTinhToanHelper.resolveTramVariant(sanLuongHieuLuc, hmThiCong, batThuong);

            tramRows.add(VolumeTramRow.builder()
                    .id(geo.hopDongDoiTuongId())
                    .maTram(geo.maDoiTuong())
                    .diaDiem(geo.province())
                    .diaChi(geo.diaChi())
                    .nhaThau(geo.contractor())
                    .region(geo.region())
                    .province(geo.province())
                    .sanLuong(sanLuong)
                    .boSungSanLuong(boSung)
                    .binhQuan(hmThiCong)
                    .nguong(nguong)
                    .chenhLechHd(chenhLech)
                    .quyetToanThuc(quyetToan)
                    .chenhLechQt(chenhLechQt)
                    .batThuong(batThuong)
                    .variant(variant)
                    .build());
        }

        tramRows.sort(Comparator
                .comparing(VolumeTramRow::isBatThuong).reversed()
                .thenComparing(r -> VolumeTinhToanHelper.nz(r.getSanLuong()).abs(), Comparator.reverseOrder()));

        return new TramComputation(province, region, hmThiCong, nguong, tramRows);
    }

    @Override
    @Transactional
    public VolumeTramRow updateQuyetToan(
            UUID hopDongId,
            UUID hopDongDoiTuongId,
            VolumeQuyetToanRequest request,
            BigDecimal heSo) {
        HopDongResponse hopDong = hopDongService.getById(hopDongId);

        BigDecimal qt = request != null ? request.getQuyetToanThuc() : null;
        HopDongDoiTuongResponse obj = hopDongDoiTuongService.capNhatQuyetToanThuc(hopDongId, hopDongDoiTuongId, qt);

        BigDecimal effectiveHeSo = VolumeTinhToanHelper.resolveHeSo(heSo);
        BigDecimal hmThiCong = chiaBinhQuanTram(
                VolumeTinhToanHelper.nz(hopDong.getTongThanhTienThiCong()),
                hopDongDoiTuongService.findActiveEntitiesByHopDongId(hopDongId).size());
        BigDecimal nguong = effectiveHeSo != null ? hmThiCong.multiply(effectiveHeSo, VolumeTinhToanHelper.MC) : null;

        HopDongObjectGeo geo = toGeo(hopDongDoiTuongId, hopDongId,
                hopDongDoiTuongSnapshotService.getSnapshot(hopDongDoiTuongId));

        BigDecimal boSung = VolumeTinhToanHelper.nz(obj.getBoSungSanLuong());
        BigDecimal sanLuongHieuLuc = VolumeTinhToanHelper.nz(obj.getSanLuongHieuLuc());
        BigDecimal sanLuong = sanLuongHieuLuc.subtract(boSung);
        BigDecimal chenhLech = sanLuongHieuLuc.subtract(hmThiCong);
        BigDecimal chenhLechQt = qt != null ? qt.subtract(hmThiCong) : null;
        boolean batThuong = nguong != null && nguong.compareTo(BigDecimal.ZERO) > 0 && sanLuongHieuLuc.compareTo(nguong) > 0;
        String variant = VolumeTinhToanHelper.resolveTramVariant(sanLuongHieuLuc, hmThiCong, batThuong);

        return VolumeTramRow.builder()
                .id(hopDongDoiTuongId)
                .maTram(geo != null ? geo.maDoiTuong() : "—")
                .diaDiem(geo != null ? geo.province() : (obj != null ? "—" : "—"))
                .diaChi(geo != null ? geo.diaChi() : "—")
                .nhaThau(geo != null ? geo.contractor() : "—")
                .region(geo != null ? geo.region() : null)
                .province(geo != null ? geo.province() : null)
                .sanLuong(sanLuong)
                .boSungSanLuong(boSung)
                .binhQuan(hmThiCong)
                .nguong(nguong)
                .chenhLechHd(chenhLech)
                .quyetToanThuc(qt)
                .chenhLechQt(chenhLechQt)
                .batThuong(batThuong)
                .variant(variant)
                .build();
    }

    @Override
    @Transactional
    public VolumeTramRow updateBoSungSanLuong(
            UUID hopDongId,
            UUID hopDongDoiTuongId,
            VolumeBoSungRequest request,
            BigDecimal heSo) {
        HopDongResponse hopDong = hopDongService.getById(hopDongId);

        BigDecimal soTienTrieu = request != null ? request.getSoTienTrieu() : null;
        if (soTienTrieu == null || soTienTrieu.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Số tiền điều chỉnh phải lớn hơn 0");
        }

        String lyDo = request != null ? request.getLyDo() : null;
        if (lyDo == null || lyDo.isBlank()) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Thiếu lý do điều chỉnh");
        }
        String ghiChu = request != null && request.getGhiChu() != null ? request.getGhiChu().trim() : null;

        String action = request.getAction() == null ? "add" : request.getAction().trim().toLowerCase(Locale.ROOT);
        if (!"add".equals(action) && !"remove".equals(action)) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Hành động không hợp lệ (add|remove)");
        }

        HopDongDoiTuongResponse before = hopDongDoiTuongService.getById(hopDongDoiTuongId);
        assertThuocHopDongVolume(before, hopDongId);
        assertBoSungAdjustAllowed(before);

        BigDecimal delta = soTienTrieu.multiply(BigDecimal.valueOf(1_000_000L), VolumeTinhToanHelper.MC);
        if ("remove".equals(action)) {
            delta = delta.negate();
        }

        BigDecimal boSungTruoc = VolumeTinhToanHelper.nz(before.getBoSungSanLuong());
        BigDecimal boSungSau = boSungTruoc.add(delta);
        if (boSungSau.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Không thể giảm quá số đã bổ sung (hiện có "
                            + boSungTruoc.divide(BigDecimal.valueOf(1_000_000L), 2, RoundingMode.HALF_UP)
                            + " triệu VNĐ)");
        }

        HopDongDoiTuongResponse obj = hopDongDoiTuongService.dieuChinhBoSungSanLuong(hopDongId, hopDongDoiTuongId, delta);

        persistBoSungLichSu(hopDongId, hopDongDoiTuongId, action, delta, boSungTruoc, boSungSau, lyDo.trim(), ghiChu);
        appEventContext.audit(
                "DIEU_CHINH_BO_SUNG_SL",
                "Điều chỉnh bổ sung sản lượng Volume HD",
                buildBoSungAuditDetail(action, soTienTrieu, lyDo.trim(), ghiChu, boSungTruoc, boSungSau),
                Map.of(
                        "hopDongId", hopDongId,
                        "doiTuongIds", List.of(hopDongDoiTuongId),
                        "hanhDong", action,
                        "lyDo", lyDo.trim()));

        BigDecimal effectiveHeSo = VolumeTinhToanHelper.resolveHeSo(heSo);
        BigDecimal hmThiCong = chiaBinhQuanTram(
                VolumeTinhToanHelper.nz(hopDong.getTongThanhTienThiCong()),
                hopDongDoiTuongService.findActiveEntitiesByHopDongId(hopDongId).size());
        BigDecimal nguong = effectiveHeSo != null ? hmThiCong.multiply(effectiveHeSo, VolumeTinhToanHelper.MC) : null;

        HopDongObjectGeo geo = toGeo(hopDongDoiTuongId, hopDongId,
                hopDongDoiTuongSnapshotService.getSnapshot(hopDongDoiTuongId));

        BigDecimal boSung = VolumeTinhToanHelper.nz(obj.getBoSungSanLuong());
        BigDecimal sanLuongHieuLuc = VolumeTinhToanHelper.nz(obj.getSanLuongHieuLuc());
        BigDecimal sanLuong = sanLuongHieuLuc.subtract(boSung);
        BigDecimal chenhLech = sanLuongHieuLuc.subtract(hmThiCong);
        BigDecimal qt = obj.getQuyetToanThuc();
        BigDecimal chenhLechQt = qt != null ? qt.subtract(hmThiCong) : null;
        boolean batThuong = nguong != null && nguong.compareTo(BigDecimal.ZERO) > 0 && sanLuongHieuLuc.compareTo(nguong) > 0;
        String variant = VolumeTinhToanHelper.resolveTramVariant(sanLuongHieuLuc, hmThiCong, batThuong);

        return VolumeTramRow.builder()
                .id(hopDongDoiTuongId)
                .maTram(geo != null ? geo.maDoiTuong() : "—")
                .diaDiem(geo != null ? geo.province() : "—")
                .diaChi(geo != null ? geo.diaChi() : "—")
                .nhaThau(geo != null ? geo.contractor() : "—")
                .region(geo != null ? geo.region() : null)
                .province(geo != null ? geo.province() : null)
                .sanLuong(sanLuong)
                .boSungSanLuong(boSung)
                .binhQuan(hmThiCong)
                .nguong(nguong)
                .chenhLechHd(chenhLech)
                .quyetToanThuc(qt)
                .chenhLechQt(chenhLechQt)
                .batThuong(batThuong)
                .variant(variant)
                .build();
    }

    private VolumeBatchContext loadBatch(UUID loaiHopDongId, UUID hopDongId, String search) {
        String cacheKey = (loaiHopDongId != null ? loaiHopDongId.toString() : "-") + "|"
                + (hopDongId != null ? hopDongId.toString() : "-") + "|"
                + vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter.normalizeSearch(search);
        java.util.Optional<VolumeBatchContext> cached =
                cacheService.get(VolumeCacheNames.BATCH, cacheKey, VolumeBatchContext.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        VolumeBatchContext computed = computeBatchForHopDongs(loadHopDongs(loaiHopDongId, hopDongId, search));
        cacheService.put(VolumeCacheNames.BATCH, cacheKey, computed, cacheProperties.volumeBatchTtl());
        return computed;
    }

    /**
     * Loại bỏ đối tượng placeholder "Hạng mục thi công" (neo dữ liệu hạng mục ở cấp hợp đồng,
     * không phải trạm thật — xem DoiTuongHopDongLienKetSupport.isHangMucDoiTuong()) khỏi mọi
     * tính toán volume (số trạm, thiếu/thừa, gộp theo tỉnh/khu vực). Thiếu bước lọc này từng
     * khiến volume đếm dư 1 "trạm" không tên/không tỉnh/không khu vực cho mỗi hợp đồng có cấu
     * hình hạng mục thi công.
     */
    private static List<HopDongDoiTuongResponse> excludeHangMucPlaceholder(List<HopDongDoiTuongResponse> objects) {
        return objects.stream()
                .filter(o -> !DoiTuongHopDongLienKetSupport
                        .isHangMucDoiTuong(o.getDoiTuongQuanLyId(), o.getDoiTuongTen(), o.getDoiTuongMa()))
                .toList();
    }

    private VolumeBatchContext computeLiteBatchForHopDongs(List<HopDongResponse> hopDongs) {
        if (hopDongs.isEmpty()) {
            return emptyBatchContext();
        }
        List<UUID> hopDongIds = hopDongs.stream().map(HopDongResponse::getId).toList();
        List<HopDongDoiTuongResponse> objects =
                excludeHangMucPlaceholder(hopDongDoiTuongService.listActiveLiteByHopDongIds(hopDongIds));
        ObjectMaps maps = buildObjectMaps(objects, hopDongs);
        return new VolumeBatchContext(
                hopDongs, objects, maps.objectsByHopDong(),
                List.of(), Map.of(), Map.of(),
                maps.objectById(), maps.objectTypeByHopDong(), maps.loaiHopDongById());
    }

    private VolumeBatchContext computeBatchForHopDongs(List<HopDongResponse> hopDongs) {
        if (hopDongs.isEmpty()) {
            return emptyBatchContext();
        }

        List<UUID> hopDongIds = hopDongs.stream().map(HopDongResponse::getId).toList();
        List<HopDongDoiTuongResponse> objects =
                excludeHangMucPlaceholder(hopDongDoiTuongService.listActiveLiteByHopDongIds(hopDongIds));
        ObjectMaps maps = buildObjectMaps(objects, hopDongs);

        List<UUID> objectIds = objects.stream().map(HopDongDoiTuongResponse::getId)
                .filter(Objects::nonNull).toList();
        Map<UUID, HopDongDoiTuongSnapshot> snapshots = hopDongDoiTuongSnapshotService.getSnapshots(objectIds);
        List<HopDongObjectGeo> geos = objects.stream()
                .map(o -> toGeo(o.getId(), o.getHopDongId(), snapshots.get(o.getId())))
                .filter(Objects::nonNull)
                .toList();

        Map<UUID, List<HopDongObjectGeo>> geosByHopDong = geos.stream()
                .collect(Collectors.groupingBy(HopDongObjectGeo::hopDongId));
        Map<UUID, Long> objectCountByHopDong = new HashMap<>();
        for (HopDongObjectGeo geo : geos) {
            objectCountByHopDong.merge(geo.hopDongId(), 1L, Long::sum);
        }

        return new VolumeBatchContext(
                hopDongs, objects, maps.objectsByHopDong(), geos, geosByHopDong, objectCountByHopDong,
                maps.objectById(), maps.objectTypeByHopDong(), maps.loaiHopDongById());
    }

    private static VolumeBatchContext emptyBatchContext() {
        return new VolumeBatchContext(
                List.of(), List.of(), Map.of(), List.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
    }

    private record ObjectMaps(
            Map<UUID, List<HopDongDoiTuongResponse>> objectsByHopDong,
            Map<UUID, HopDongDoiTuongResponse> objectById,
            Map<UUID, String> objectTypeByHopDong,
            Map<UUID, LoaiHopDongResponse> loaiHopDongById) {
    }

    private ObjectMaps buildObjectMaps(List<HopDongDoiTuongResponse> objects, List<HopDongResponse> hopDongs) {
        Map<UUID, List<HopDongDoiTuongResponse>> objectsByHopDong = objects.stream()
                .filter(o -> o.getHopDongId() != null)
                .collect(Collectors.groupingBy(HopDongDoiTuongResponse::getHopDongId));

        Map<UUID, HopDongDoiTuongResponse> objectById = new HashMap<>();
        for (HopDongDoiTuongResponse o : objects) {
            if (o.getId() == null) continue;
            objectById.putIfAbsent(o.getId(), o);
        }
        Map<UUID, String> objectTypeByHopDong = VolumeTinhToanHelper.buildObjectTypeByHopDong(objects);

        Set<UUID> loaiHopDongIds = hopDongs.stream()
                .map(HopDongResponse::getLoaiHopDongId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, LoaiHopDongResponse> loaiHopDongById = loaiHopDongService.getByIds(loaiHopDongIds);

        return new ObjectMaps(objectsByHopDong, objectById, objectTypeByHopDong, loaiHopDongById);
    }

    /**
     * Adapter sang HopDongObjectGeo (kiểu volume vẫn dùng nội bộ) từ snapshot dùng chung
     * HopDongDoiTuongSnapshotService — tránh volume tự resolve EAV riêng (mã trạm/tỉnh/khu
     * vực/nhà thầu), đọc lại đúng 1 nguồn tính duy nhất mà sanluong/vuongmac cũng đang dùng.
     */
    private static HopDongObjectGeo toGeo(UUID hopDongDoiTuongId, UUID hopDongId, HopDongDoiTuongSnapshot snap) {
        if (snap == null) {
            return new HopDongObjectGeo(hopDongDoiTuongId, hopDongId, "—", "—", null, null, "—", "—", "—", null, null);
        }
        return new HopDongObjectGeo(
                hopDongDoiTuongId, hopDongId,
                snap.getKhuVuc(), snap.getTinh(), snap.getProvinceKey(), snap.getOldProvince(), snap.getNhaThau(),
                snap.getMaDoiTuong(), snap.getDiaChi(), null, null);
    }

    private List<HopDongResponse> loadHopDongs(UUID loaiHopDongId, UUID hopDongId, String search) {
        List<HopDongResponse> source;
        if (hopDongId != null) {
            HopDongResponse hopDong = hopDongService.getById(hopDongId);
            boolean keep = Boolean.TRUE.equals(hopDong.getHoatDong())
                    && (loaiHopDongId == null || loaiHopDongId.equals(hopDong.getLoaiHopDongId()));
            source = keep ? List.of(hopDong) : List.of();
        } else {
            source = hopDongService.listAll(search, Boolean.TRUE, false, loaiHopDongId, null);
        }
        return source.stream()
                .sorted(Comparator.comparing(
                        HopDongResponse::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private VolumeHopDongRowResponse toRowFromBatch(
            HopDongResponse hopDong, BigDecimal heSo, VolumeBatchContext ctx) {
        long giaTriHd = hopDong.getGiaTriHd() != null ? hopDong.getGiaTriHd() : 0L;
        List<HopDongDoiTuongResponse> hopObjects = ctx.objectsByHopDong().getOrDefault(hopDong.getId(), List.of());
        long soTram = hopObjects.size();
        // tongThanhTienThiCong = thành tiền cây hạng mục CHO CẢ HỢP ĐỒNG (nhiều trạm), không
        // phải cho 1 trạm — phải CHIA cho số trạm mới ra định mức bình quân/1 trạm để so với
        // sanLuongHieuLuc (vốn là số của từng trạm riêng lẻ). Thiếu bước chia này từng khiến so
        // 1 trạm với định mức của cả hợp đồng, sai lệch hàng chục lần.
        BigDecimal planPerStation = soTram > 0
                ? VolumeTinhToanHelper.nz(hopDong.getTongThanhTienThiCong())
                        .divide(BigDecimal.valueOf(soTram), VolumeTinhToanHelper.MC)
                : BigDecimal.ZERO;

        // Phân biệt ngưỡng: GCCC dùng heSo, còn lại dùng loaiHopDong.nguong
        BigDecimal threshold;
        BigDecimal ngueongTinhToan;
        boolean isGccc = VolumeConstants.GCCC_LOAI_HOP_DONG_ID.equals(hopDong.getLoaiHopDongId());
        if (isGccc) {
            // GCCC: ngưỡng = planPerStation × heSo
            threshold = heSo != null ? planPerStation.multiply(heSo, VolumeTinhToanHelper.MC) : null;
            ngueongTinhToan = threshold;
        } else {
            // Xây mới & khác: lấy hesoNguong từ loai "Xây mới & Còn lại" (id = XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID)
            LoaiHopDongResponse xayMoiConLai = ctx.loaiHopDongById().get(VolumeConstants.XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID);
            BigDecimal hesoXayMoi = xayMoiConLai != null ? xayMoiConLai.getHesoNguong() : null;
            ngueongTinhToan = hesoXayMoi != null ? planPerStation.multiply(hesoXayMoi, VolumeTinhToanHelper.MC) : null;
            threshold = ngueongTinhToan;
        }

        long tramThieu = 0;
        long tramThua = 0;
        long tramBatThuong = 0;
        long tramDaQt = 0;
        BigDecimal tongSlThucTe = BigDecimal.ZERO;
        BigDecimal tongQuyetToan = BigDecimal.ZERO;
        for (HopDongDoiTuongResponse objForSl : hopObjects) {
            BigDecimal sl = VolumeTinhToanHelper.nz(objForSl.getSanLuongHieuLuc());
            tongSlThucTe = tongSlThucTe.add(sl);
            if (objForSl.getQuyetToanThuc() != null) {
                tramDaQt += 1;
                tongQuyetToan = tongQuyetToan.add(objForSl.getQuyetToanThuc());
            }

            // Đếm thiếu/thừa cho mọi trạm đã có định mức catalog thật (planPerStation > 0) — KỂ
            // CẢ trạm chưa có sản lượng nào (sl = 0): định mức dương mà thực tế = 0 là thiếu toàn
            // bộ, phải tính vào "thiếu". planPerStation = 0 nghĩa là chưa recompute
            // tongThanhTienThiCong cho hợp đồng này, so với 0 sẽ luôn ra "thừa" giả nên vẫn bỏ qua.
            if (planPerStation.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (sl.compareTo(planPerStation) < 0) {
                tramThieu += 1;
            } else if (sl.compareTo(planPerStation) > 0) {
                tramThua += 1;
            }
            if (threshold != null && threshold.compareTo(BigDecimal.ZERO) > 0 && sl.compareTo(threshold) > 0) {
                tramBatThuong += 1;
            }
        }

        // Quy ước chung: âm = thiếu, dương = thừa → thực tế trừ kế hoạch.
        BigDecimal remaining = tongSlThucTe.subtract(BigDecimal.valueOf(giaTriHd));

        String trangThaiVolume;
        if (tramThieu > 0) {
            trangThaiVolume = "thieu";
        } else if (tramThua > 0 || tramBatThuong > 0) {
            trangThaiVolume = "thua";
        } else {
            trangThaiVolume = "can_bang";
        }

        // Cảnh báo nguy hiểm = vượt ngưỡng (heSo × BQ/trạm); cảnh báo = còn đối tượng thiếu/thừa SL.
        // Gộp đủ loại để dòng cha phản ánh cả trường hợp vừa thiếu vừa thừa.
        long tramThuaThuong = Math.max(0, tramThua - tramBatThuong);
        List<String> alertParts = new ArrayList<>();
        if (tramBatThuong > 0) {
            alertParts.add(tramBatThuong + " đối tượng vượt ngưỡng");
        }
        if (tramThieu > 0) {
            alertParts.add(tramThieu + " đối tượng thiếu SL");
        }
        if (tramThuaThuong > 0) {
            alertParts.add(tramThuaThuong + " đối tượng thừa SL");
        }
        String alertLevel;
        if (tramBatThuong > 0) {
            alertLevel = "danger";
        } else if (tramThieu > 0 || tramThuaThuong > 0) {
            alertLevel = "warning";
        } else {
            alertLevel = "ok";
        }
        String alertText = String.join(" · ", alertParts);

        String trangThai;
        if (isGccc) {
            trangThai = VolumeTinhToanHelper.resolveStatus(giaTriHd, tongSlThucTe);
        } else {
            LoaiHopDongResponse xayMoiConLai = ctx.loaiHopDongById().get(VolumeConstants.XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID);
            BigDecimal hesoXayMoi = xayMoiConLai != null ? xayMoiConLai.getHesoNguong() : null;
            BigDecimal contractThreshold = hesoXayMoi != null
                    ? VolumeTinhToanHelper.nz(hopDong.getTongThanhTienThiCong())
                            .multiply(hesoXayMoi, VolumeTinhToanHelper.MC)
                    : null;
            trangThai = VolumeTinhToanHelper.resolveStatus(contractThreshold, tongSlThucTe);
        }

        String trangThaiQt;
        if (soTram == 0 || tramDaQt == 0) {
            trangThaiQt = "chua_qt";
        } else if (tramDaQt >= soTram) {
            trangThaiQt = "da_qt";
        } else {
            trangThaiQt = "dang_qt";
        }

        return VolumeHopDongRowResponse.builder()
                .hopDongId(hopDong.getId())
                .maHopDong(hopDong.getMaHopDong())
                .ma(hopDong.getMa())
                .ten(hopDong.getTen())
                .loaiHopDongId(hopDong.getLoaiHopDongId())
                .loaiTen(Optional.ofNullable(ctx.loaiHopDongById().get(hopDong.getLoaiHopDongId()))
                        .map(LoaiHopDongResponse::getTen)
                        .orElse("—"))
                .kieuHopDongId(hopDong.getKieuHopDongId())
                .doiTuongTen(ctx.objectTypeByHopDong().get(hopDong.getId()))
                .chuDauTu(resolveChuDauTu(hopDong))
                .giaTriHd(giaTriHd)
                .tongThanhTienThiCong(tongSlThucTe)
                .chenhLech(remaining)
                .tyLeSuDung(VolumeTinhToanHelper.ratio(tongSlThucTe, BigDecimal.valueOf(giaTriHd)))
                .trangThai(trangThai)
                .soTram(soTram)
                .giaTriBinhQuan(planPerStation)
                .tramThieu(tramThieu)
                .tramThua(tramThua)
                .tramBatThuong(tramBatThuong)
                .trangThaiVolume(trangThaiVolume)
                .alertLevel(alertLevel)
                .alertText(alertText)
                .heSoNguong(heSo)
                .tongQuyetToan(tongQuyetToan)
                .trangThaiQt(trangThaiQt)
                .build();
    }

    private void assertThuocHopDongVolume(HopDongDoiTuongResponse obj, UUID hopDongId) {
        if (obj == null || hopDongId == null || !hopDongId.equals(obj.getHopDongId())) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_NOT_FOUND,
                    "Đối tượng không thuộc hợp đồng này");
        }
    }

    private void assertBoSungAdjustAllowed(HopDongDoiTuongResponse obj) {
        if (obj.getQuyetToanThuc() != null && obj.getQuyetToanThuc().signum() > 0) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Trạm đã quyết toán, không thể điều chỉnh bổ sung sản lượng");
        }
        String statusMa = obj.getTrangThaiMa();
        if (statusMa != null && !statusMa.isBlank()) {
            String normalized = statusMa.trim().toUpperCase(Locale.ROOT);
            if ("QT".equals(normalized) || normalized.contains("QUYET")) {
                throw new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                        "Trạm đang ở trạng thái quyết toán, không thể điều chỉnh bổ sung sản lượng");
            }
        }
    }

    private void persistBoSungLichSu(
            UUID hopDongId,
            UUID hopDongDoiTuongId,
            String action,
            BigDecimal delta,
            BigDecimal boSungTruoc,
            BigDecimal boSungSau,
            String lyDo,
            String ghiChu) {
        HopDongDoiTuongBoSungLichSu log = new HopDongDoiTuongBoSungLichSu();
        log.setHopDongId(hopDongId);
        log.setHopDongDoiTuongId(hopDongDoiTuongId);
        log.setHanhDong(action);
        log.setDeltaVnd(delta);
        log.setBoSungTruoc(boSungTruoc);
        log.setBoSungSau(boSungSau);
        log.setLyDo(lyDo);
        log.setGhiChu(ghiChu);
        boSungLichSuRepository.save(log);
    }

    private String buildBoSungAuditDetail(
            String action,
            BigDecimal soTienTrieu,
            String lyDo,
            String ghiChu,
            BigDecimal boSungTruoc,
            BigDecimal boSungSau) {
        String actionLabel = "add".equals(action) ? "Bổ sung" : "Giảm";
        StringBuilder detail = new StringBuilder();
        detail.append(actionLabel)
                .append(' ')
                .append(soTienTrieu.toPlainString())
                .append(" triệu · Lý do: ")
                .append(resolveLyDoLabel(lyDo))
                .append(" · Bổ sung: ")
                .append(boSungTruoc.toPlainString())
                .append(" → ")
                .append(boSungSau.toPlainString());
        if (ghiChu != null && !ghiChu.isBlank()) {
            detail.append(" · Ghi chú: ").append(ghiChu.trim());
        }
        return detail.toString();
    }

    private static String resolveLyDoLabel(String code) {
        if (code == null || code.isBlank()) {
            return "—";
        }
        return switch (code) {
            case "design_change" -> "Thay đổi thiết kế sau KS";
            case "contract_supplement" -> "Phụ lục bổ sung HĐ";
            case "price_adjustment" -> "Điều chỉnh đơn giá";
            case "scope_change" -> "Thay đổi phạm vi công việc";
            case "error_correction" -> "Sửa lỗi nhập liệu";
            case "manager_override" -> "Quản lý HĐ điều chỉnh cân bằng";
            case "other" -> "Lý do khác";
            default -> code;
        };
    }

    @Override
    @Transactional(readOnly = true)
    public VolumeCauHinhNguongResponse getCauHinhNguong() {
        return VolumeCauHinhNguongResponse.builder()
                .gcccHeSo(loadGcccHeSoFromDb())
                .xayMoiHeSo(loadXayMoiHeSoFromDb())
                .overrides(loadOverridesFromDb())
                .build();
    }

    @Override
    @Transactional
    public VolumeCauHinhNguongResponse saveCauHinhNguong(VolumeCauHinhNguongRequest request) {
        if (request == null) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Thiếu dữ liệu cấu hình");
        }
        BigDecimal gcccHeSo = request.getGcccHeSo();
        BigDecimal xayMoiHeSo = request.getXayMoiHeSo();
        if (gcccHeSo == null || gcccHeSo.compareTo(BigDecimal.ZERO) <= 0
                || xayMoiHeSo == null || xayMoiHeSo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Hệ số ngưỡng phải lớn hơn 0");
        }

        LoaiHopDongCapNhatRequest gcccPatch = new LoaiHopDongCapNhatRequest();
        gcccPatch.setHesoNguong(gcccHeSo);
        loaiHopDongService.update(VolumeConstants.GCCC_LOAI_HOP_DONG_ID, gcccPatch);

        LoaiHopDongCapNhatRequest xayMoiPatch = new LoaiHopDongCapNhatRequest();
        xayMoiPatch.setHesoNguong(xayMoiHeSo);
        loaiHopDongService.update(VolumeConstants.XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID, xayMoiPatch);

        Map<UUID, BigDecimal> incoming = request.getOverrides() != null ? request.getOverrides() : Map.of();
        java.util.Set<UUID> hopIdsToUpdate = new java.util.HashSet<>(incoming.keySet());
        hopIdsToUpdate.addAll(hopDongRepository.findGcccHopDongIdsWithHeSoOverride(
                VolumeConstants.GCCC_LOAI_HOP_DONG_ID));

        for (UUID hopDongId : hopIdsToUpdate) {
            HopDong entity = hopDongRepository.findById(hopDongId)
                    .filter(h -> h.getNgayXoa() == null)
                    .orElseThrow(() -> new AppException(
                            HopDongErrorCode.HOP_DONG_NOT_FOUND, "Không tìm thấy hợp đồng"));
            if (!VolumeConstants.GCCC_LOAI_HOP_DONG_ID.equals(entity.getLoaiHopDongId())) {
                throw new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                        "Hệ số riêng chỉ áp dụng cho hợp đồng GCCC");
            }
            BigDecimal override = incoming.get(hopDongId);
            if (override != null && override.compareTo(BigDecimal.ZERO) > 0
                    && override.compareTo(gcccHeSo) != 0) {
                entity.setHesoNguong(override);
            } else {
                entity.setHesoNguong(null);
            }
            hopDongRepository.save(entity);
        }

        cacheService.evictAll(VolumeCacheNames.BATCH);

        appEventContext.audit(
                "CAU_HINH_NGUONG_VOLUME",
                "Lưu cấu hình ngưỡng cảnh báo Volume",
                "GCCC ×" + gcccHeSo.toPlainString()
                        + " · Xây mới ×" + xayMoiHeSo.toPlainString()
                        + " · " + incoming.size() + " HĐ override",
                Map.of(
                        "gcccHeSo", gcccHeSo,
                        "xayMoiHeSo", xayMoiHeSo,
                        "overrideCount", incoming.size()));

        return getCauHinhNguong();
    }

    private BigDecimal loadGcccHeSoFromDb() {
        LoaiHopDongResponse loai = loaiHopDongService.getById(VolumeConstants.GCCC_LOAI_HOP_DONG_ID);
        BigDecimal heSo = loai != null ? loai.getHesoNguong() : null;
        return heSo != null && heSo.compareTo(BigDecimal.ZERO) > 0
                ? heSo
                : VolumeTinhToanHelper.DEFAULT_HESO_GCCC;
    }

    private BigDecimal loadXayMoiHeSoFromDb() {
        LoaiHopDongResponse loai =
                loaiHopDongService.getById(VolumeConstants.XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID);
        return loai != null ? loai.getHesoNguong() : null;
    }

    private Map<UUID, BigDecimal> loadOverridesFromDb() {
        Map<UUID, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : hopDongRepository.findGcccVolumeHeSoOverrides(VolumeConstants.GCCC_LOAI_HOP_DONG_ID)) {
            UUID id = (UUID) row[0];
            BigDecimal heSo = (BigDecimal) row[1];
            if (id != null && heSo != null && heSo.compareTo(BigDecimal.ZERO) > 0) {
                map.put(id, heSo);
            }
        }
        return map;
    }

    private BigDecimal resolveEffectiveDefaultHeSo(BigDecimal heSoFromClient) {
        if (heSoFromClient != null && heSoFromClient.compareTo(BigDecimal.ZERO) > 0) {
            return heSoFromClient;
        }
        return loadGcccHeSoFromDb();
    }

    private Map<UUID, BigDecimal> resolveEffectiveOverrides(Map<UUID, BigDecimal> heSoOverridesFromClient) {
        Map<UUID, BigDecimal> fromDb = loadOverridesFromDb();
        if (heSoOverridesFromClient == null || heSoOverridesFromClient.isEmpty()) {
            return fromDb;
        }
        Map<UUID, BigDecimal> merged = new LinkedHashMap<>(fromDb);
        merged.putAll(heSoOverridesFromClient);
        return merged;
    }

    private static BigDecimal resolveRowHeSo(
            HopDongResponse hopDong, BigDecimal defaultHeSo, Map<UUID, BigDecimal> overrides) {
        if (overrides.containsKey(hopDong.getId())) {
            return overrides.get(hopDong.getId());
        }
        if (hopDong.getHesoNguong() != null && hopDong.getHesoNguong().compareTo(BigDecimal.ZERO) > 0) {
            return hopDong.getHesoNguong();
        }
        return defaultHeSo;
    }

}
