package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers;

import vn.edu.huce.iic.bts_ops_platform.common.util.TextUtils;
import vn.edu.huce.iic.bts_ops_platform.common.util.UuidUtils;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.HopDongObjectGeo;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.HopDongLabelLookups;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.entity.NguoiDung;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.repository.NguoiDungRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.LienKetBangSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachKhuVucMetricsResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachKhuVucResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTienDoBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTinhRowResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachThuocTinhColumnResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTramRowResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTramThuocTinhItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongGiaTriResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.DoiTuongHopDongLienKetSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.TinhThanh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.TinhThanhKhuVuc;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.helpers.TinhThanhMergerLookupSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.KhuVucRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.TinhThanhKhuVucRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.TinhThanhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.VolumeTinhToanHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HopDongDanhSachDoiTuongGroupSupport {

    private static final List<String> REGION_COLORS = List.of(
            "#E31C23", "#2563eb", "#059669", "#d97706", "#9333ea", "#0891b2");

    private static final Pattern PROVINCE_PATTERN = Pattern.compile("tinh(?!.*\\bcu\\b)|tinh thanh|province");
    private static final Pattern OLD_PROVINCE_PATTERN = Pattern.compile("tinh cu|\\bcu\\b");
    private static final Pattern REGION_PATTERN = Pattern.compile("khu vuc|region");
    private static final Pattern CONTRACTOR_PATTERN = Pattern.compile("nha thau|doi tc|do tc|contractor");
    private static final Pattern MA_TRAM_PATTERN = Pattern.compile("ma tram|ma nha tram|ma doi tuong|station");
    private static final Pattern MA_TUYEN_PATTERN = Pattern.compile("ma tuyen");
    private static final Pattern LOCATION_PATTERN = Pattern.compile("dia diem|diem|dia chi|address|location|vi tri");
    private static final Pattern HEIGHT_PATTERN = Pattern.compile("chieu cao|cao cot|height|cao");
    private static final Pattern SECTOR_PATTERN = Pattern.compile("linh vuc|sector|loai cot");

    private static final Pattern STATUS_PATTERN = Pattern.compile("trang thai|status");

    private final ThuocTinhService thuocTinhService;
    private final TinhThanhRepository tinhThanhRepository;
    private final TinhThanhKhuVucRepository tinhThanhKhuVucRepository;
    private final KhuVucRepository khuVucRepository;
    private final NguoiDungRepository nguoiDungRepository;

    public List<HopDongDanhSachKhuVucResponse> buildKhuVucs(
            List<HopDongDoiTuongResponse> rows,
            Map<UUID, String> contractorByDoiTuongId,
            List<LuongTrangThaiBuocResponse> flowBuoc) {
        return buildKhuVucs(rows, contractorByDoiTuongId, flowBuoc, Map.of(), Map.of(), BigDecimal.ZERO);
    }

    public List<HopDongDanhSachKhuVucResponse> buildKhuVucs(
            List<HopDongDoiTuongResponse> rows,
            Map<UUID, String> contractorByDoiTuongId,
            List<LuongTrangThaiBuocResponse> flowBuoc,
            Map<UUID, BigDecimal> moneyByDoiTuong,
            Map<UUID, BigDecimal> boSungByDoiTuong,
            BigDecimal planPerStation) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        HopDongLabelLookups lookups = buildLabelLookups(rows);
        Map<String, Map<String, HopDongMutableTinhRow>> regionMap = new LinkedHashMap<>();

        for (HopDongDoiTuongResponse row : rows) {
            HopDongObjectGeo geo = resolveObjectGeo(row, lookups, contractorByDoiTuongId, true);
            if (geo == null) {
                continue;
            }

            String aggregationKey = aggregationKeyForMoRong(geo);
            Map<String, HopDongMutableTinhRow> provinces = regionMap.computeIfAbsent(geo.region(), ignored -> new LinkedHashMap<>());
            HopDongMutableTinhRow existing = provinces.get(aggregationKey);
            if (existing == null) {
                existing = new HopDongMutableTinhRow(
                        geo.region() + "-" + aggregationKey,
                        geo.province(),
                        geo.contractor());
                existing.tinhMoi = resolveTinhMoiMa(geo.province(), lookups);
                provinces.put(aggregationKey, existing);
            }

            if (existing.oldProvince.isBlank() && !geo.oldProvince().isBlank()) {
                existing.oldProvince = geo.oldProvince();
            } else if (!geo.oldProvince().isBlank()
                    && !existing.oldProvince.isBlank()
                    && !containsProvinceToken(existing.oldProvince, geo.oldProvince())) {
                existing.oldProvince = existing.oldProvince + ", " + geo.oldProvince();
            }
            if (existing.tinhMoi.isBlank()) {
                existing.tinhMoi = resolveTinhMoiMa(geo.province(), lookups);
            }

            String statusKey = HopDongDanhSachTienDoHelper.resolveStatusCountKey(
                    row.getTrangThaiHopDongId(),
                    row.getTrangThaiMa(),
                    row.getTrangThaiTen(),
                    flowBuoc);
            bumpProvinceCounts(existing, row, flowBuoc);
            bumpTuVanSanLuongStatus(existing, row, flowBuoc, statusKey);
            bumpProvinceVolume(
                    existing,
                    row.getId(),
                    moneyByDoiTuong,
                    boSungByDoiTuong,
                    planPerStation);
            if (row.getId() != null) {
                BigDecimal reported = moneyByDoiTuong != null
                        ? moneyByDoiTuong.getOrDefault(row.getId(), BigDecimal.ZERO)
                        : BigDecimal.ZERO;
                BigDecimal boSung = VolumeTinhToanHelper.nz(row.getBoSungSanLuong());
                BigDecimal effective = VolumeTinhToanHelper.effectiveSanLuong(reported, boSung);
                existing.sumTyLeHoanThanh += HopDongDanhSachTienDoHelper.resolveDoiTuongTyLeHoanThanh(
                        row.getNgayHtTc(),
                        row.getTrangThaiMa(),
                        row.getTrangThaiTen(),
                        statusKey,
                        effective,
                        planPerStation,
                        flowBuoc);
            }
        }

        List<HopDongDanhSachKhuVucResponse> result = new ArrayList<>();
        int regionIndex = 0;
        for (Map.Entry<String, Map<String, HopDongMutableTinhRow>> regionEntry : regionMap.entrySet()) {
            HopDongDanhSachKhuVucResponse region = new HopDongDanhSachKhuVucResponse();
            region.setId("region-" + regionIndex);
            region.setTen(regionEntry.getKey());
            region.setMauSac(REGION_COLORS.get(regionIndex % REGION_COLORS.size()));

            List<HopDongDanhSachTinhRowResponse> tinhRows = new ArrayList<>();
            int stt = 1;
            for (HopDongMutableTinhRow mutable : regionEntry.getValue().values()) {
                tinhRows.add(finalizeProvinceRow(mutable, stt++, flowBuoc));
            }
            region.setTinhRows(tinhRows);
            region.setMetrics(buildRegionMetrics(tinhRows, flowBuoc));
            result.add(region);
            regionIndex++;
        }
        return result;
    }

    /**
     * Khóa gom dòng mo-rong: tách theo (tỉnh/khu, nhà thầu) để nhiều NThầu cùng KV không gộp
     * thành một dòng với cc = tổng toàn bộ đối tượng.
     */
    private String aggregationKeyForMoRong(HopDongObjectGeo geo) {
        if (geo == null) {
            return "";
        }
        if (geo.hopDongDoiTuongId() != null
                && geo.provinceKey().equals(geo.hopDongDoiTuongId().toString())) {
            return geo.provinceKey();
        }
        String contractorKey = geo.contractor() == null
                        || geo.contractor().isBlank()
                        || "—".equals(geo.contractor())
                ? "_none"
                : geo.contractor().trim().toLowerCase(Locale.ROOT);
        return geo.provinceKey() + "::NT::" + contractorKey;
    }

    /** Lọc đối tượng theo nhãn hiển thị KV / tỉnh / nhà thầu (dùng cho chi tiết tỉnh). */
    /**
     * Đối tượng rỗng do seed tự động khi tạo HĐ (chưa import/thêm thuộc tính) — không hiển thị
     * trong danh sách trạm/biên bản và không tính vào số "Chưa lập BB".
     */
    public boolean isShellDoiTuongWithoutImportedData(
            HopDongDoiTuongResponse row,
            HopDongLabelLookups lookups) {
        if (row == null || !Boolean.TRUE.equals(row.getHoatDong()) || isHangMucRow(row)) {
            return false;
        }
        if (row.getNhaThauId() != null || row.getTinhThanhId() != null || row.getKhuVucId() != null) {
            return false;
        }
        if (hasAnyMeaningfulGiaTri(row, lookups)) {
            return false;
        }
        if (lookups == null) {
            lookups = buildLabelLookups(List.of(row));
        }
        String maTram = resolveMaTram(row, lookups);
        return maTram == null
                || maTram.equals("—")
                || isUuidPrefixFallback(maTram, row.getId());
    }

    /** Loại bỏ đối tượng shell khỏi danh sách dùng cho trang biên bản / thống kê BB. */
    public List<HopDongDoiTuongResponse> excludeShellDoiTuong(List<HopDongDoiTuongResponse> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        HopDongLabelLookups lookups = buildLabelLookups(rows);
        return rows.stream()
                .filter(row -> !isShellDoiTuongWithoutImportedData(row, lookups))
                .toList();
    }

    /** Đếm đối tượng thật (không shell) theo hopDongId — dùng cho thống kê BB số 1. */
    public Map<UUID, Long> countByHopDongExcludingShells(List<HopDongDoiTuongResponse> rows) {
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }
        return excludeShellDoiTuong(rows).stream()
                .collect(Collectors.groupingBy(
                        HopDongDoiTuongResponse::getHopDongId,
                        Collectors.counting()));
    }

    public List<HopDongDoiTuongResponse> filterByDisplayGeo(
            List<HopDongDoiTuongResponse> rows,
            Map<UUID, String> contractorByDoiTuongId,
            String region,
            String province,
            String contractor) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        HopDongLabelLookups lookups = buildLabelLookups(rows);
        List<HopDongDoiTuongResponse> result = new ArrayList<>();
        for (HopDongDoiTuongResponse row : rows) {
            HopDongObjectGeo geo = resolveObjectGeo(row, lookups, contractorByDoiTuongId, true);
            if (geo == null) {
                continue;
            }
            if (!matchesGeoFilter(region, geo.region())) {
                continue;
            }
            if (!matchesGeoFilter(province, geo.province())) {
                continue;
            }
            if (!matchesContractorFilter(contractor, geo.contractor())) {
                continue;
            }
            result.add(row);
        }
        return result;
    }

    public HopDongDanhSachTramRowResponse toTramRow(
            HopDongDoiTuongResponse row,
            Map<UUID, String> contractorByDoiTuongId,
            HopDongLabelLookups lookups,
            List<HopDongDanhSachThuocTinhColumnResponse> thuocTinhColumns,
            boolean preferWorkflowTrangThai) {
        HopDongDanhSachTramRowResponse tram = new HopDongDanhSachTramRowResponse();
        tram.setId(row.getId());
        tram.setMaTram(resolveMaTram(row, lookups));
        tram.setDiaDiem(firstNonBlank(findGiaTriByPattern(row, LOCATION_PATTERN, lookups), "—"));
        tram.setLinhVuc(firstNonBlank(findGiaTriByPattern(row, SECTOR_PATTERN, lookups), "—"));
        tram.setCao(firstNonBlank(findGiaTriByPattern(row, HEIGHT_PATTERN, lookups), "—"));
        tram.setThuocTinhGiaTri(buildDisplayThuocTinh(row, lookups, thuocTinhColumns));

        HopDongObjectGeo geo = resolveObjectGeo(row, lookups, contractorByDoiTuongId, false);
        String contractor = geo != null ? geo.contractor() : "—";
        if ("—".equals(contractor) && row.getId() != null) {
            contractor = displayNameOnly(contractorByDoiTuongId.getOrDefault(row.getId(), ""));
        }
        tram.setNhaThau(contractor.isBlank() ? "—" : contractor);
        tram.setTrangThaiMa(row.getTrangThaiMa());
        if (!preferWorkflowTrangThai
                && row.getGiaiDoanThiCongTen() != null
                && !row.getGiaiDoanThiCongTen().isBlank()) {
            tram.setTrangThaiTen(row.getGiaiDoanThiCongTen());
        } else {
            tram.setTrangThaiTen(row.getTrangThaiTen());
        }
        tram.setTrangThaiMauSac(row.getTrangThaiMauSac());
        tram.setNhomUuTienTen(row.getNhomUuTienTen());
        tram.setNhomUuTienMauSac(row.getNhomUuTienMauSac());
        tram.setNgayBatDau(row.getConstructionDate());
        tram.setNgayHt(row.getNgayHtTc());
        tram.setNgayTao(row.getNgayTao());
        tram.setNgayCapNhat(row.getNgayCapNhat());
        return tram;
    }

    public List<HopDongDanhSachThuocTinhColumnResponse> buildThuocTinhColumns(UUID doiTuongQuanLyId) {
        if (doiTuongQuanLyId == null) {
            return List.of();
        }
        List<HopDongDanhSachThuocTinhColumnResponse> columns = new ArrayList<>();
        for (ThuocTinhResponse definition : thuocTinhService.list(null, true, false, doiTuongQuanLyId, null)) {
            if (definition == null || definition.getId() == null) {
                continue;
            }
            if (!Boolean.TRUE.equals(definition.getHoatDong())) {
                continue;
            }
            if (Boolean.TRUE.equals(definition.getLaKhoaChinh())) {
                continue;
            }
            if (isHiddenInDetailTable(definition)) {
                continue;
            }
            HopDongDanhSachThuocTinhColumnResponse column = new HopDongDanhSachThuocTinhColumnResponse();
            column.setThuocTinhId(definition.getId());
            column.setTen(definition.getTen());
            column.setThuTu(sortOrderForThuocTinh(definition));
            column.setLaKhoaChinh(definition.getLaKhoaChinh());
            columns.add(column);
        }
        columns.sort(this::compareThuocTinhColumns);
        return columns;
    }

    /** @deprecated dùng {@link #buildThuocTinhColumns(UUID)} */
    @Deprecated
    public List<HopDongDanhSachThuocTinhColumnResponse> buildThuocTinhColumns(
            UUID doiTuongQuanLyId,
            HopDongLabelLookups lookups) {
        return buildThuocTinhColumns(doiTuongQuanLyId);
    }

    public List<HopDongDanhSachTramRowResponse> toTramRows(
            List<HopDongDoiTuongResponse> rows,
            Map<UUID, String> contractorByDoiTuongId) {
        return toTramRows(rows, contractorByDoiTuongId, true);
    }

    public List<HopDongDanhSachTramRowResponse> toTramRows(
            List<HopDongDoiTuongResponse> rows,
            Map<UUID, String> contractorByDoiTuongId,
            boolean preferWorkflowTrangThai) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        HopDongLabelLookups lookups = buildLabelLookups(rows);
        UUID doiTuongQuanLyId = rows.stream()
                .map(HopDongDoiTuongResponse::getDoiTuongQuanLyId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        List<HopDongDanhSachThuocTinhColumnResponse> thuocTinhColumns = buildThuocTinhColumns(doiTuongQuanLyId);
        List<HopDongDanhSachTramRowResponse> result = new ArrayList<>();
        for (HopDongDoiTuongResponse row : rows) {
            result.add(toTramRow(row, contractorByDoiTuongId, lookups, thuocTinhColumns, preferWorkflowTrangThai));
        }
        return result;
    }

    /**
     * Phân giải từng đối tượng → (mã khu vực, mã tỉnh) để aggregate volume/sản lượng.
     */
    public List<HopDongObjectGeo> resolveObjectGeos(
            List<HopDongDoiTuongResponse> rows,
            Map<UUID, String> contractorByDoiTuongId) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        HopDongLabelLookups lookups = buildLabelLookups(rows);
        List<HopDongObjectGeo> result = new ArrayList<>();
        for (HopDongDoiTuongResponse row : rows) {
            HopDongObjectGeo geo = resolveObjectGeo(row, lookups, contractorByDoiTuongId, true);
            if (geo != null) {
                result.add(geo);
            }
        }
        return result;
    }

    /** Phân giải geo dùng nhãn hiển thị (tên KV/tỉnh) — phục vụ enrich vướng mắc. */
    public List<HopDongObjectGeo> resolveDisplayObjectGeos(List<HopDongDoiTuongResponse> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        HopDongLabelLookups lookups = buildLabelLookups(rows);
        List<HopDongObjectGeo> result = new ArrayList<>();
        for (HopDongDoiTuongResponse row : rows) {
            HopDongObjectGeo geo = resolveObjectGeo(row, lookups, Map.of(), false);
            if (geo != null) {
                result.add(geo);
            }
        }
        return result;
    }

    /** Mã hiển thị: mã tuyến/trạm → khóa chính → pattern → 8 ký tự UUID. */
    public String resolveMaTram(HopDongDoiTuongResponse row, HopDongLabelLookups lookups) {
        if (row == null) {
            return "—";
        }
        String maTuyen = findGiaTriByPattern(row, MA_TUYEN_PATTERN, lookups);
        if (isPlausibleMaTram(maTuyen) && !isDoiTuongTypeCode(maTuyen, row)) {
            return maTuyen.trim();
        }
        String primary = resolvePrimaryKeyValue(row, lookups);
        if (!primary.isBlank() && !isDoiTuongTypeCode(primary, row)) {
            return primary.trim();
        }
        String maTramRaw = findGiaTriByPattern(row, MA_TRAM_PATTERN, lookups);
        if (isPlausibleMaTram(maTramRaw) && !isDoiTuongTypeCode(maTramRaw, row)) {
            return maTramRaw.trim();
        }
        if (row.getId() != null) {
            return row.getId().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        }
        return "—";
    }

    /** @deprecated dùng {@link #resolveMaTram(HopDongDoiTuongResponse, HopDongLabelLookups)} */
    @Deprecated
    public String resolveMaTram(HopDongDoiTuongResponse row) {
        if (row == null) {
            return "—";
        }
        HopDongLabelLookups lookups = buildLabelLookups(List.of(row));
        return resolveMaTram(row, lookups);
    }

    private HopDongObjectGeo resolveObjectGeo(
            HopDongDoiTuongResponse row,
            HopDongLabelLookups lookups,
            Map<UUID, String> contractorByDoiTuongId,
            boolean useCode) {
        if (row == null || !Boolean.TRUE.equals(row.getHoatDong()) || isHangMucRow(row)) {
            return null;
        }

        String provinceRaw = findGiaTriByPattern(row, PROVINCE_PATTERN, lookups);
        String oldProvinceRaw = findGiaTriByPattern(row, OLD_PROVINCE_PATTERN, lookups);
        String regionRaw = findGiaTriByPattern(row, REGION_PATTERN, lookups);
        String contractorRaw = findGiaTriByPattern(row, CONTRACTOR_PATTERN, lookups);
        String maTramRaw = findGiaTriByPattern(row, MA_TRAM_PATTERN, lookups);

        String province = useCode
                ? resolveGeoCode(provinceRaw, lookups, true)
                : resolveLabel(provinceRaw, lookups);
        String oldProvince = useCode
                ? resolveGeoCode(oldProvinceRaw, lookups, true)
                : resolveLabel(oldProvinceRaw, lookups);
        // Fallback: nếu không có thuộc tính EAV "Tỉnh cũ" riêng (đa số đối tượng không có), suy
        // từ nhóm sáp nhập chính thức (tinh_thanh.tinhThanhId, xem buildLookups()) qua
        // tinh_thanh_id đã denormalize trên đối tượng — không phải mọi đối tượng đều có tỉnh cũ
        // (chỉ những tỉnh thực sự bị sáp nhập mới có), nên có thể vẫn rỗng sau bước này.
        if (oldProvince.isBlank() && row.getTinhThanhId() != null) {
            oldProvince = useCode
                    ? lookups.tinhCuMaByTinhId().getOrDefault(row.getTinhThanhId(), "")
                    : lookups.tinhCuTenByTinhId().getOrDefault(row.getTinhThanhId(), "");
        }
        boolean hasProvinceValue = provinceRaw != null && !provinceRaw.isBlank();
        boolean useStationRow = !hasProvinceValue && isPlausibleMaTram(maTramRaw);

        if (useStationRow) {
            String derivedProvince = resolveProvinceFromMaTram(maTramRaw, lookups);
            String letterPrefix = maTramLetterPrefix(maTramRaw);
            if (useCode) {
                province = !derivedProvince.isBlank()
                        ? derivedProvince
                        : !letterPrefix.isBlank() ? letterPrefix : "N/A";
            } else if (!derivedProvince.isBlank()) {
                province = lookups.tinhTenByMa().getOrDefault(derivedProvince, derivedProvince)
                        + " (" + maTramRaw.trim() + ")";
            } else {
                province = !letterPrefix.isBlank() ? letterPrefix + " (chưa rõ tỉnh)" : "Chưa xác định";
            }
            // Prefix là mã tỉnh cũ (khác mã hiện hành sau quy đổi) — điền luôn cột "Mã cũ" nếu trống.
            if (oldProvince.isBlank() && !derivedProvince.isBlank() && !letterPrefix.isBlank()
                    && !letterPrefix.equalsIgnoreCase(derivedProvince)
                    && lookups.tinhCanonicalMaByKey().containsKey(letterPrefix)) {
                oldProvince = letterPrefix;
            }
        } else if (province.isBlank()) {
            province = useCode ? "N/A" : "Chưa xác định";
        }

        if (useCode) {
            if (!oldProvince.isBlank()) {
                oldProvince = resolveProvinceDisplayMa(oldProvince, lookups);
            }
            if (oldProvince.isBlank() && row.getTinhThanhId() != null) {
                String cuMa = lookups.tinhCuMaByTinhId().getOrDefault(row.getTinhThanhId(), "");
                if (!cuMa.isBlank()) {
                    oldProvince = cuMa.trim().toUpperCase(Locale.ROOT);
                }
            }
        }
        String regionName = useCode
                ? resolveGeoCode(regionRaw, lookups, false)
                : resolveLabel(regionRaw, lookups);
        if (regionName.isBlank()) {
            regionName = useCode ? "N/A" : "Chưa phân khu vực";
        }

        if (useCode && useStationRow && province != null && !province.isBlank() && !"N/A".equals(province)) {
            String provinceMa = province.trim().toUpperCase(Locale.ROOT);
            if (oldProvince.isBlank()) {
                oldProvince = lookups.tinhCuMaByProvinceMa().getOrDefault(provinceMa, "");
            }
            if (regionName.isBlank() || "N/A".equals(regionName)) {
                String khuMa = lookups.khuMaByProvinceMa().getOrDefault(provinceMa, "");
                if (!khuMa.isBlank()) {
                    regionName = khuMa;
                }
            }
        }

        String contractor = "";
        if (row.getId() != null) {
            contractor = displayNameOnly(contractorByDoiTuongId.getOrDefault(row.getId(), ""));
        }
        if (contractor.isBlank()) {
            contractor = resolveContractorName(contractorRaw, lookups);
        }
        if (contractor.isBlank()) {
            contractor = "—";
        }

        String provinceKey = useStationRow && row.getId() != null
                ? row.getId().toString()
                : province + "::" + oldProvince;

        // Chỉ có id thật khi tỉnh được gán trực tiếp (không phải suy từ mã trạm).
        UUID tinhThanhId = useStationRow ? null : UuidUtils.parseUuid(provinceRaw);
        UUID khuVucId = UuidUtils.parseUuid(regionRaw);

        String diaChi = firstNonBlank(findGiaTriByPattern(row, LOCATION_PATTERN, lookups), "—");

        return new HopDongObjectGeo(
                row.getId(),
                row.getHopDongId(),
                regionName,
                province,
                provinceKey,
                oldProvince,
                contractor,
                resolveMaTram(row, lookups),
                diaChi,
                khuVucId,
                tinhThanhId);
    }

    /**
     * Nhà thầu hiệu lực của đối tượng: ưu tiên {@code nha_thau_id}, fallback thuộc tính EAV
     * (nhà thầu / đội TC) — khớp luồng gán qua modal HĐ trước khi migrate cột chuẩn hóa.
     */
    public UUID resolveEffectiveNhaThauId(HopDongDoiTuongResponse row, HopDongLabelLookups lookups) {
        if (row == null) {
            return null;
        }
        if (row.getNhaThauId() != null) {
            return row.getNhaThauId();
        }
        String contractorRaw = findGiaTriByPattern(row, CONTRACTOR_PATTERN, lookups);
        UUID parsed = UuidUtils.parseUuid(contractorRaw);
        if (parsed != null) {
            return parsed;
        }
        if (row.getGiaTri() == null) {
            return null;
        }
        for (HopDongDoiTuongGiaTriResponse item : row.getGiaTri()) {
            if (item == null || item.getGiaTri() == null || item.getGiaTri().isBlank()) {
                continue;
            }
            ThuocTinhResponse definition = item.getThuocTinhId() != null && lookups != null
                    ? lookups.thuocTinhById().get(item.getThuocTinhId())
                    : null;
            if (!belongsToDoiTuong(definition, row.getDoiTuongQuanLyId())) {
                continue;
            }
            if (!"nguoi_dung".equals(resolveLinkTable(definition))) {
                continue;
            }
            UUID linked = UuidUtils.parseUuid(item.getGiaTri());
            if (linked != null) {
                return linked;
            }
        }
        return null;
    }

    public HopDongLabelLookups buildLabelLookups(List<HopDongDoiTuongResponse> rows) {
        Set<UUID> doiTuongIds = rows.stream()
                .map(HopDongDoiTuongResponse::getDoiTuongQuanLyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, ThuocTinhResponse> thuocTinhById = new HashMap<>();
        thuocTinhService.listGroupedByDoiTuongQuanLyIds(doiTuongIds).values().forEach(definitions ->
                definitions.forEach(definition -> {
                    if (definition.getId() != null) {
                        thuocTinhById.put(definition.getId(), definition);
                    }
                }));

        Set<UUID> tinhIds = new java.util.HashSet<>();
        Set<UUID> khuIds = new java.util.HashSet<>();
        Set<UUID> nguoiDungIds = new java.util.HashSet<>();

        for (HopDongDoiTuongResponse row : rows) {
            if (row.getKhuVucId() != null) {
                khuIds.add(row.getKhuVucId());
            }
            if (row.getTinhThanhId() != null) {
                tinhIds.add(row.getTinhThanhId());
            }
            for (HopDongDoiTuongGiaTriResponse value : row.getGiaTri()) {
                if (value == null || value.getGiaTri() == null || value.getGiaTri().isBlank()) {
                    continue;
                }
                UUID linkedId = UuidUtils.parseUuid(value.getGiaTri());
                if (linkedId == null) {
                    continue;
                }
                ThuocTinhResponse definition = value.getThuocTinhId() != null
                        ? thuocTinhById.get(value.getThuocTinhId())
                        : null;
                String linkTable = resolveLinkTable(definition);
                if ("tinh_thanh".equals(linkTable)) {
                    tinhIds.add(linkedId);
                } else if ("khu_vuc".equals(linkTable)) {
                    khuIds.add(linkedId);
                } else if ("nguoi_dung".equals(linkTable)) {
                    nguoiDungIds.add(linkedId);
                }
            }
        }

        Map<UUID, String> tinhById = new HashMap<>();
        Map<UUID, String> tinhMaById = new HashMap<>();
        if (!tinhIds.isEmpty()) {
            tinhThanhRepository.findAllById(tinhIds).forEach(item -> {
                tinhById.put(item.getId(), item.getTen());
                tinhMaById.put(item.getId(), item.getMa() == null ? "" : item.getMa().trim());
            });
        }
        Map<String, String> tinhTenByMa = tinhThanhRepository.findByNgayXoaIsNullOrderByMaAsc().stream()
                .filter(item -> item.getMa() != null && !item.getMa().isBlank())
                .collect(Collectors.toMap(
                        item -> item.getMa().trim().toUpperCase(Locale.ROOT),
                        TinhThanh::getTen,
                        (left, right) -> left));
        Map<UUID, String> khuById = new HashMap<>();
        Map<UUID, String> khuMaById = new HashMap<>();
        if (!khuIds.isEmpty()) {
            khuVucRepository.findAllById(khuIds).forEach(item -> {
                khuById.put(item.getId(), item.getTen());
                khuMaById.put(item.getId(), item.getMa() == null ? "" : item.getMa().trim());
            });
        }
        Map<UUID, String> nguoiDungById = nguoiDungIds.isEmpty()
                ? Map.of()
                : nguoiDungRepository.findAllById(nguoiDungIds).stream()
                        .collect(Collectors.toMap(
                                NguoiDung::getId,
                                user -> formatNguoiDung(user),
                                (left, right) -> left));

        // Tỉnh cũ cùng nhóm sáp nhập — tinh_thanh.tinhThanhId (KHÁC id riêng của bản ghi) là khóa
        // nhóm dùng chung giữa tỉnh hiện tại (laTinhCu=false) và (các) tỉnh cũ (laTinhCu=true).
        // Phải đọc TẤT CẢ bản ghi (kể cả đã xóa mềm) vì tỉnh cũ luôn bị soft-delete sau sáp nhập.
        Map<UUID, String> tinhCuMaByTinhId = new HashMap<>();
        Map<UUID, String> tinhCuTenByTinhId = new HashMap<>();
        List<TinhThanh> allTinh = tinhThanhRepository.findAllByOrderByMaAsc();
        Map<UUID, List<TinhThanh>> tinhByGroupKey = allTinh.stream()
                .collect(Collectors.groupingBy(TinhThanh::getTinhThanhId));
        for (TinhThanh current : allTinh) {
            if (Boolean.TRUE.equals(current.getLaTinhCu())) {
                continue;
            }
            TinhThanh oldTinh = tinhByGroupKey.getOrDefault(current.getTinhThanhId(), List.of()).stream()
                    .filter(item -> Boolean.TRUE.equals(item.getLaTinhCu()))
                    .findFirst()
                    .orElse(null);
            if (oldTinh != null) {
                tinhCuMaByTinhId.put(current.getId(), oldTinh.getMa() == null ? "" : oldTinh.getMa().trim());
                tinhCuTenByTinhId.put(current.getId(), oldTinh.getTen());
            }
        }

        Map<String, String> tinhCanonicalMaByKey = new HashMap<>();
        Map<String, String> tinhDisplayMaByKey = new HashMap<>();
        Map<String, String> tinhMoiTenByMa = new HashMap<>();
        Map<String, String> tinhCuMaByProvinceMa = new HashMap<>();
        Map<String, String> khuMaByProvinceMa = new HashMap<>();
        Map<UUID, TinhThanhKhuVuc> khuVucMappingByGroupId = tinhThanhKhuVucRepository.findAll().stream()
                .filter(mapping -> mapping.getNgayXoa() == null)
                .collect(Collectors.toMap(
                        TinhThanhKhuVuc::getTinhThanhId,
                        mapping -> mapping,
                        (left, right) -> left));
        Map<UUID, String> allKhuMaById = khuVucRepository.findByNgayXoaIsNull().stream()
                .collect(Collectors.toMap(
                        item -> item.getId(),
                        item -> item.getMa() == null ? "" : item.getMa().trim(),
                        (left, right) -> left));
        TinhThanhMergerLookupSupport.MergerMaps mergerMaps = TinhThanhMergerLookupSupport.buildMergerMaps(allTinh);
        tinhCanonicalMaByKey.putAll(mergerMaps.tinhMoiMaByKey());
        tinhDisplayMaByKey.putAll(mergerMaps.displayMaByKey());
        for (List<TinhThanh> group : tinhByGroupKey.values()) {
            TinhThanh current = group.stream()
                    .filter(item -> !Boolean.TRUE.equals(item.getLaTinhCu()))
                    .findFirst()
                    .orElse(group.isEmpty() ? null : group.get(0));
            if (current == null || current.getMa() == null || current.getMa().isBlank()) {
                continue;
            }
            String currentMa = current.getMa().trim().toUpperCase(Locale.ROOT);
            String currentTen = current.getTen() == null ? "" : current.getTen().trim();
            tinhMoiTenByMa.putIfAbsent(currentMa, currentTen);
            String cuMa = current.getId() != null
                    ? tinhCuMaByTinhId.getOrDefault(current.getId(), "")
                    : "";
            if (!cuMa.isBlank()) {
                tinhCuMaByProvinceMa.put(currentMa, cuMa.trim().toUpperCase(Locale.ROOT));
            }
            TinhThanhKhuVuc regionMapping = khuVucMappingByGroupId.get(current.getTinhThanhId());
            if (regionMapping != null && regionMapping.getKhuVucId() != null) {
                String khuMa = allKhuMaById.get(regionMapping.getKhuVucId());
                if (khuMa != null && !khuMa.isBlank()) {
                    khuMaByProvinceMa.put(currentMa, khuMa.trim().toUpperCase(Locale.ROOT));
                }
            }
            for (TinhThanh member : group) {
                if (member.getId() != null && member.getMa() != null && !member.getMa().isBlank()) {
                    tinhMaById.putIfAbsent(member.getId(), member.getMa().trim());
                }
            }
        }

        return new HopDongLabelLookups(
                tinhById, tinhMaById, tinhTenByMa, khuById, khuMaById, nguoiDungById, thuocTinhById,
                tinhCuMaByTinhId, tinhCuTenByTinhId, tinhCanonicalMaByKey, tinhDisplayMaByKey, tinhMoiTenByMa,
                tinhCuMaByProvinceMa, khuMaByProvinceMa);
    }

    private static void registerProvinceLookupKey(
            Map<String, String> target,
            String rawKey,
            String canonicalMa) {
        if (rawKey == null || rawKey.isBlank() || canonicalMa == null || canonicalMa.isBlank()) {
            return;
        }
        target.put(rawKey.trim().toUpperCase(Locale.ROOT), canonicalMa);
        target.put(TextUtils.normalizeLower(rawKey), canonicalMa);
    }

    private String canonicalizeProvinceMa(String raw, HopDongLabelLookups lookups) {
        return TinhThanhMergerLookupSupport.resolveTinhMoiMa(raw, new TinhThanhMergerLookupSupport.MergerMaps(
                lookups.tinhCanonicalMaByKey(), lookups.tinhDisplayMaByKey()));
    }

    private String resolveProvinceDisplayMa(String raw, HopDongLabelLookups lookups) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        UUID id = UuidUtils.parseUuid(raw);
        if (id != null) {
            String ma = lookups.tinhMaById().get(id);
            if (ma != null && !ma.isBlank()) {
                return ma.trim().toUpperCase(Locale.ROOT);
            }
        }
        String upper = raw.trim().toUpperCase(Locale.ROOT);
        String display = lookups.tinhDisplayMaByKey().get(upper);
        if (display == null || display.isBlank()) {
            display = lookups.tinhDisplayMaByKey().get(TextUtils.normalizeLower(raw));
        }
        return display != null && !display.isBlank() ? display : upper;
    }

    private String resolveTinhMoiMa(String displayMa, HopDongLabelLookups lookups) {
        if (displayMa == null || displayMa.isBlank()) {
            return "";
        }
        return canonicalizeProvinceMa(displayMa, lookups);
    }

    private String resolveProvinceMa(String raw, HopDongLabelLookups lookups) {
        return resolveProvinceDisplayMa(raw, lookups);
    }

    private static boolean containsProvinceToken(String existing, String candidate) {
        if (existing == null || candidate == null) {
            return false;
        }
        String normalizedExisting = existing.toUpperCase(Locale.ROOT);
        String normalizedCandidate = candidate.trim().toUpperCase(Locale.ROOT);
        for (String part : normalizedExisting.split(",")) {
            if (part.trim().equals(normalizedCandidate)) {
                return true;
            }
        }
        return false;
    }

    private List<HopDongDanhSachTramThuocTinhItemResponse> buildDisplayThuocTinh(
            HopDongDoiTuongResponse row,
            HopDongLabelLookups lookups,
            List<HopDongDanhSachThuocTinhColumnResponse> thuocTinhColumns) {
        if (row == null || thuocTinhColumns == null || thuocTinhColumns.isEmpty()) {
            return List.of();
        }
        List<HopDongDanhSachTramThuocTinhItemResponse> result = new ArrayList<>();
        for (HopDongDanhSachThuocTinhColumnResponse column : thuocTinhColumns) {
            if (column == null || column.getThuocTinhId() == null) {
                continue;
            }
            String raw = findGiaTriForThuocTinh(row, column.getThuocTinhId(), column.getTen(), lookups);
            HopDongDanhSachTramThuocTinhItemResponse mapped = new HopDongDanhSachTramThuocTinhItemResponse();
            mapped.setThuocTinhId(column.getThuocTinhId());
            mapped.setTenThuocTinh(column.getTen());
            mapped.setThuTu(column.getThuTu());
            mapped.setLaKhoaChinh(column.getLaKhoaChinh());
            if (raw.isBlank()) {
                mapped.setGiaTri("—");
            } else {
                ThuocTinhResponse definition = lookups != null
                        ? lookups.thuocTinhById().get(column.getThuocTinhId())
                        : null;
                mapped.setGiaTri(resolveThuocTinhDisplayValue(raw, definition, lookups));
            }
            result.add(mapped);
        }
        return result;
    }

    private String findGiaTriForThuocTinh(
            HopDongDoiTuongResponse row,
            UUID thuocTinhId,
            String tenThuocTinh,
            HopDongLabelLookups lookups) {
        if (row.getGiaTri() == null) {
            return "";
        }
        for (HopDongDoiTuongGiaTriResponse item : row.getGiaTri()) {
            if (item == null) {
                continue;
            }
            if (thuocTinhId != null && thuocTinhId.equals(item.getThuocTinhId())) {
                return item.getGiaTri() == null ? "" : item.getGiaTri().trim();
            }
        }
        String normalizedTarget = TextUtils.normalizeLower(tenThuocTinh);
        for (HopDongDoiTuongGiaTriResponse item : row.getGiaTri()) {
            if (item == null) {
                continue;
            }
            String normalizedName = TextUtils.normalizeLower(item.getTenThuocTinh());
            if (normalizedTarget.equals(normalizedName)) {
                return item.getGiaTri() == null ? "" : item.getGiaTri().trim();
            }
        }
        return "";
    }

    private boolean isDoiTuongTypeCode(String value, HopDongDoiTuongResponse row) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String trimmed = value.trim();
        if (row.getDoiTuongMa() != null && trimmed.equalsIgnoreCase(row.getDoiTuongMa().trim())) {
            return true;
        }
        return trimmed.matches("(?i)^DTQL_.+");
    }

    /** Ẩn thuộc tính đã hiển thị ở header/cột cố định (KV, tỉnh, nhà thầu). */
    private boolean isHiddenInDetailTable(ThuocTinhResponse definition) {
        if (definition == null) {
            return true;
        }
        String linkTable = resolveLinkTable(definition);
        if ("tinh_thanh".equals(linkTable) || "khu_vuc".equals(linkTable)) {
            return true;
        }
        if ("nguoi_dung".equals(linkTable)) {
            return true;
        }
        String name = TextUtils.normalizeLower(definition.getTen());
        return PROVINCE_PATTERN.matcher(name).find()
                || OLD_PROVINCE_PATTERN.matcher(name).find()
                || REGION_PATTERN.matcher(name).find()
                || CONTRACTOR_PATTERN.matcher(name).find()
                || STATUS_PATTERN.matcher(name).find();
    }

    private int sortOrderForThuocTinh(ThuocTinhResponse definition) {
        String name = TextUtils.normalizeLower(definition.getTen());
        if (name.contains("ma tuyen") || name.contains("ma tram")) {
            return 0;
        }
        if (Boolean.TRUE.equals(definition.getLaKhoaChinh())) {
            return 1;
        }
        return 999;
    }

    private int compareThuocTinhColumns(
            HopDongDanhSachThuocTinhColumnResponse left,
            HopDongDanhSachThuocTinhColumnResponse right) {
        int byOrder = Integer.compare(
                left.getThuTu() == null ? 999 : left.getThuTu(),
                right.getThuTu() == null ? 999 : right.getThuTu());
        if (byOrder != 0) {
            return byOrder;
        }
        String leftTen = left.getTen() == null ? "" : left.getTen();
        String rightTen = right.getTen() == null ? "" : right.getTen();
        return leftTen.compareToIgnoreCase(rightTen);
    }

    private String resolvePrimaryKeyValue(HopDongDoiTuongResponse row, HopDongLabelLookups lookups) {
        if (row.getGiaTri() == null || lookups == null) {
            return "";
        }
        for (HopDongDoiTuongGiaTriResponse item : row.getGiaTri()) {
            if (item == null || item.getThuocTinhId() == null) {
                continue;
            }
            ThuocTinhResponse definition = lookups.thuocTinhById().get(item.getThuocTinhId());
            if (!belongsToDoiTuong(definition, row.getDoiTuongQuanLyId())) {
                continue;
            }
            if (!Boolean.TRUE.equals(definition.getLaKhoaChinh())) {
                continue;
            }
            if (item.getGiaTri() != null && !item.getGiaTri().isBlank()) {
                String value = item.getGiaTri().trim();
                if (!isDoiTuongTypeCode(value, row)) {
                    return value;
                }
            }
        }
        return "";
    }

    private boolean belongsToDoiTuong(ThuocTinhResponse definition, UUID doiTuongQuanLyId) {
        return definition != null
                && definition.getId() != null
                && Objects.equals(definition.getDoiTuongQuanLyId(), doiTuongQuanLyId);
    }

    private boolean isSystemThuocTinh(ThuocTinhResponse definition) {
        return isHiddenInDetailTable(definition);
    }

    private int resolveThuTu(ThuocTinhResponse definition) {
        return sortOrderForThuocTinh(definition);
    }

    private String resolveThuocTinhDisplayValue(
            String raw,
            ThuocTinhResponse definition,
            HopDongLabelLookups lookups) {
        return resolveLabel(raw, lookups);
    }

    private boolean isPlausibleMaTram(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String trimmed = value.trim();
        if (trimmed.length() < 4) {
            return false;
        }
        String normalized = TextUtils.normalizeLower(trimmed);
        if (normalized.contains("ma tram") || normalized.contains("ma nha tram") || normalized.contains("ma tuyen")) {
            return false;
        }
        if (trimmed.matches("(?i)^DTQL_.+")) {
            return false;
        }
        return trimmed.matches("^[A-Za-z]{2,3}.+");
    }

    /**
     * Trả về mã tỉnh HIỆN HÀNH (sau sáp nhập) nếu suy được từ prefix mã trạm. Prefix là mã tỉnh
     * CŨ (đã soft-delete sau sáp nhập, vd TBH) vẫn phải quy về mã mới (HYN) — nếu trả nguyên
     * prefix, trạm suy-từ-mã sẽ tách nhóm riêng khỏi các trạm cùng tỉnh có thuộc tính Tỉnh.
     */
    private String resolveProvinceFromMaTram(String maTram, HopDongLabelLookups lookups) {
        String normalized = maTram.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() < 2) {
            return "";
        }
        int maxPrefix = Math.min(3, normalized.length());
        for (int len = maxPrefix; len >= 2; len--) {
            String prefix = normalized.substring(0, len);
            String canonical = lookups.tinhCanonicalMaByKey().get(prefix);
            if (canonical != null && !canonical.isBlank()) {
                return canonical.trim().toUpperCase(Locale.ROOT);
            }
            if (lookups.tinhTenByMa().containsKey(prefix)) {
                return prefix;
            }
        }
        return "";
    }

    /** Prefix chữ cái đầu mã trạm (2-3 ký tự, vd HTY0467 → HTY) — khóa gom cho trạm lẻ không
     * suy được tỉnh, để các trạm cùng prefix vẫn gom chung 1 nhóm thay vì mỗi trạm 1 nhóm. */
    private static String maTramLetterPrefix(String maTram) {
        String normalized = maTram.trim().toUpperCase(Locale.ROOT);
        int len = 0;
        while (len < normalized.length() && len < 3 && Character.isLetter(normalized.charAt(len))) {
            len++;
        }
        return len >= 2 ? normalized.substring(0, len) : "";
    }

    private String resolveLinkTable(ThuocTinhResponse definition) {
        if (definition == null) {
            return null;
        }
        if (definition.getLienKetBang() != null
                && LienKetBangSupport.isKnownLinkTable(definition.getLienKetBang())) {
            return definition.getLienKetBang().trim();
        }
        return LienKetBangSupport.isKnownLinkTable(definition.getKieuDuLieuId())
                ? definition.getKieuDuLieuId().trim()
                : null;
    }

    private String findGiaTriByPattern(
            HopDongDoiTuongResponse row,
            Pattern pattern,
            HopDongLabelLookups lookups) {
        if (row.getGiaTri() == null) {
            return "";
        }
        for (HopDongDoiTuongGiaTriResponse item : row.getGiaTri()) {
            ThuocTinhResponse definition = item.getThuocTinhId() != null && lookups != null
                    ? lookups.thuocTinhById().get(item.getThuocTinhId())
                    : null;
            if (!belongsToDoiTuong(definition, row.getDoiTuongQuanLyId())) {
                continue;
            }
            String name = TextUtils.normalizeLower(
                    definition != null && definition.getTen() != null
                            ? definition.getTen()
                            : item.getTenThuocTinh());
            if (pattern.matcher(name).find()) {
                return item.getGiaTri() == null ? "" : item.getGiaTri().trim();
            }
        }
        return "";
    }

    private String resolveLabel(String raw, HopDongLabelLookups lookups) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
            UUID id = UuidUtils.parseUuid(raw);
        if (id != null) {
            String resolved = lookups.tinhById().get(id);
            if (resolved != null && !resolved.isBlank()) {
                return resolved;
            }
            resolved = lookups.khuById().get(id);
            if (resolved != null && !resolved.isBlank()) {
                return resolved;
            }
            resolved = lookups.nguoiDungById().get(id);
            if (resolved != null && !resolved.isBlank()) {
                return resolved;
            }
        }
        return raw.trim();
    }

    /** Nhà thầu chỉ hiện tên — bỏ mã / username nếu có. */
    private String resolveContractorName(String raw, HopDongLabelLookups lookups) {
        return displayNameOnly(resolveLabel(raw, lookups));
    }

    private String displayNameOnly(String label) {
        if (label == null || label.isBlank()) {
            return "";
        }
        String value = label.trim();
        if (value.endsWith(")")) {
            int open = value.lastIndexOf(" (");
            if (open > 0) {
                return value.substring(0, open).trim();
            }
        }
        for (String sep : List.of(" — ", " – ", " - ")) {
            int idx = value.indexOf(sep);
            if (idx >= 0) {
                String name = value.substring(idx + sep.length()).trim();
                if (!name.isBlank()) {
                    return name;
                }
            }
        }
        return value;
    }

    /** Ưu tiên mã tỉnh / mã khu vực (UUID → ma, raw tra danh mục nếu là tên). */
    private String resolveGeoCode(String raw, HopDongLabelLookups lookups, boolean province) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        if (province) {
            return resolveProvinceMa(raw, lookups);
        }
        UUID id = UuidUtils.parseUuid(raw);
        if (id != null) {
            String ma = lookups.khuMaById().get(id);
            if (ma != null && !ma.isBlank()) {
                return ma;
            }
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isHangMucRow(HopDongDoiTuongResponse row) {
        return DoiTuongHopDongLienKetSupport.isHangMucDoiTuong(
                row.getDoiTuongQuanLyId(),
                row.getDoiTuongTen(),
                row.getDoiTuongMa());
    }

    private void bumpProvinceCounts(
            HopDongMutableTinhRow row,
            HopDongDoiTuongResponse doiTuong,
            List<LuongTrangThaiBuocResponse> flowBuoc) {
        row.cc++;
        String statusKey = HopDongDanhSachTienDoHelper.resolveStatusCountKey(
                doiTuong.getTrangThaiHopDongId(),
                doiTuong.getTrangThaiMa(),
                doiTuong.getTrangThaiTen(),
                flowBuoc);
        row.statusByMa.merge(statusKey, 1L, Long::sum);
    }

    private void bumpTuVanSanLuongStatus(
            HopDongMutableTinhRow row,
            HopDongDoiTuongResponse doiTuong,
            List<LuongTrangThaiBuocResponse> flowBuoc,
            String statusKey) {
        if (HopDongDanhSachTienDoHelper.isHuyStatus(doiTuong.getTrangThaiMa(), doiTuong.getTrangThaiTen())) {
            return;
        }
        if (HopDongDanhSachTienDoHelper.isObjectHoanThanh(
                doiTuong.getNgayHtTc(),
                doiTuong.getTrangThaiMa(),
                doiTuong.getTrangThaiTen(),
                statusKey)) {
            row.hoanThanhSanLuong++;
            return;
        }
        if (flowBuoc != null && statusKey != null && !statusKey.isBlank()) {
            String hoanMa = HopDongDanhSachTienDoHelper.resolveHoanThanhTrangThaiMa(flowBuoc);
            if (hoanMa.equals(HopDongDanhSachTienDoHelper.normalizeStatusMa(statusKey))) {
                row.hoanThanhSanLuong++;
                return;
            }
        }
        if (!HopDongDanhSachTienDoHelper.countsTowardTuVanSanLuongProgress(
                doiTuong.getTrangThaiHopDongId(),
                doiTuong.getTrangThaiMa(),
                doiTuong.getTrangThaiTen(),
                doiTuong.getNgayHtTc(),
                flowBuoc)) {
            return;
        }
        BigDecimal sanLuong = VolumeTinhToanHelper.nz(doiTuong.getSanLuongHieuLuc());
        if (sanLuong.compareTo(BigDecimal.ZERO) > 0) {
            row.dangLamSanLuong++;
        } else if (HopDongDanhSachTienDoHelper.isTuVanChuaLamStep(statusKey, flowBuoc)) {
            row.chuaLamSanLuong++;
        } else {
            row.dangLamSanLuong++;
        }
    }

    private void bumpProvinceVolume(
            HopDongMutableTinhRow row,
            UUID hopDongDoiTuongId,
            Map<UUID, BigDecimal> moneyByDoiTuong,
            Map<UUID, BigDecimal> boSungByDoiTuong,
            BigDecimal planPerStation) {
        if (hopDongDoiTuongId == null) {
            return;
        }
        BigDecimal reported = moneyByDoiTuong != null
                ? moneyByDoiTuong.getOrDefault(hopDongDoiTuongId, BigDecimal.ZERO)
                : BigDecimal.ZERO;
        BigDecimal boSung = boSungByDoiTuong != null
                ? boSungByDoiTuong.getOrDefault(hopDongDoiTuongId, BigDecimal.ZERO)
                : BigDecimal.ZERO;
        row.constructionValue = row.constructionValue.add(
                VolumeTinhToanHelper.effectiveSanLuong(reported, boSung));
        if (planPerStation != null && planPerStation.compareTo(BigDecimal.ZERO) > 0) {
            row.contractValue = row.contractValue.add(planPerStation);
        }
    }

    private HopDongDanhSachTinhRowResponse finalizeProvinceRow(
            HopDongMutableTinhRow row,
            int stt,
            List<LuongTrangThaiBuocResponse> flowBuoc) {
        HopDongDanhSachTinhRowResponse response = new HopDongDanhSachTinhRowResponse();
        response.setId(row.id);
        response.setStt(stt);
        response.setTinh(row.province);
        response.setTinhMoi(row.tinhMoi);
        response.setTinhCu(row.oldProvince);
        response.setNhaThau(row.contractor);
        response.setCc(row.cc);
        response.setGiaTriKeHoach(row.contractValue);
        response.setSanLuongThiCong(row.constructionValue);

        long hoanThanhHt = sumStatus(row.statusByMa, "HT", "HOAN_THANH");
        long quyetToan = sumStatus(row.statusByMa, "QT", "DQT");
        long huy = sumStatusMatching(row.statusByMa, ma -> HopDongDanhSachTienDoHelper.isHuyStatus(ma, null));
        long chuaThiCong = sumStatusMatching(
                row.statusByMa,
                ma -> ma.equals("CKS") || ma.contains("CHUA") || ma.contains("CHƯA"));
        long hoanThanh = row.hoanThanhSanLuong > 0L ? row.hoanThanhSanLuong : hoanThanhHt;
        response.setHoanThanh(hoanThanh);
        response.setHoanThanhSanLuong(row.hoanThanhSanLuong);
        response.setQuyetToan(quyetToan);
        response.setChuaQuyetToan(Math.max(0L, hoanThanhHt - quyetToan));
        response.setHuy(huy);
        response.setChuaThiCong(chuaThiCong);
        response.setChuaLamSanLuong(row.chuaLamSanLuong);
        response.setDangLamSanLuong(row.dangLamSanLuong);

        Map<String, Long> reconciledTyLe = HopDongDanhSachTienDoHelper.reconcileUnassignedStatuses(
                row.statusByMa, row.cc, flowBuoc);
        double tyLe = row.cc > 0L
                ? HopDongDanhSachTienDoHelper.computeDisplayTyLeHoanThanh(
                        reconciledTyLe, row.cc, flowBuoc)
                : HopDongDanhSachTienDoHelper.resolveAggregateTyLeHoanThanh(
                        row.constructionValue,
                        row.contractValue,
                        row.statusByMa,
                        flowBuoc,
                        row.cc);
        response.setTyLeHoanThanh(tyLe);
        response.setTienDoTheoTrangThai(
                HopDongDanhSachTienDoHelper.buildTienDoBuocListForDisplay(flowBuoc, row.statusByMa));
        return response;
    }

    private static long sumStatus(Map<String, Long> statusByMa, String... keys) {
        long total = 0L;
        for (String key : keys) {
            total += statusByMa.getOrDefault(key, 0L);
        }
        return total;
    }

    private static long sumStatusMatching(
            Map<String, Long> statusByMa,
            java.util.function.Predicate<String> predicate) {
        long total = 0L;
        for (Map.Entry<String, Long> entry : statusByMa.entrySet()) {
            if (predicate.test(entry.getKey())) {
                total += entry.getValue();
            }
        }
        return total;
    }

    private HopDongDanhSachKhuVucMetricsResponse buildRegionMetrics(
            List<HopDongDanhSachTinhRowResponse> provinces,
            List<LuongTrangThaiBuocResponse> flowBuoc) {
        Map<String, Long> statusByMa = new LinkedHashMap<>();
        long stations = 0;
        long completed = 0;
        long hoanThanhSanLuong = 0;
        long chuaLamSanLuong = 0;
        long dangLamSanLuong = 0;
        double weightedTyLe = 0D;

        for (HopDongDanhSachTinhRowResponse province : provinces) {
            stations += province.getCc();
            completed += toLong(province.getHoanThanh());
            hoanThanhSanLuong += province.getHoanThanhSanLuong();
            chuaLamSanLuong += province.getChuaLamSanLuong();
            dangLamSanLuong += province.getDangLamSanLuong();
            weightedTyLe += province.getTyLeHoanThanh() * province.getCc();
            for (HopDongDanhSachTienDoBuocResponse step : province.getTienDoTheoTrangThai()) {
                if (step.getMa() == null || step.getMa().isBlank()) {
                    continue;
                }
                String key = step.getMa().trim().toUpperCase(Locale.ROOT);
                statusByMa.merge(key, step.getSoLuong(), Long::sum);
            }
        }

        double percent = stations > 0L
                ? Math.round((weightedTyLe / stations) * 100D) / 100D
                : 0D;
        String color = percent >= 60 ? "#059669" : percent >= 30 ? "#2563eb" : "#94a3b8";
        List<HopDongDanhSachTienDoBuocResponse> buocList =
                HopDongDanhSachTienDoHelper.buildTienDoBuocListForDisplay(flowBuoc, statusByMa);

        HopDongDanhSachKhuVucMetricsResponse metrics = new HopDongDanhSachKhuVucMetricsResponse();
        metrics.setSoDoiTuong(stations);
        metrics.setHoanThanh(hoanThanhSanLuong > 0L ? hoanThanhSanLuong : completed);
        metrics.setHoanThanhSanLuong(hoanThanhSanLuong);
        metrics.setChuaLamSanLuong(chuaLamSanLuong);
        metrics.setDangLamSanLuong(dangLamSanLuong);
        metrics.setDaQuyetToan(statusByMa.getOrDefault("QT", 0L));
        metrics.setChuaBatDau(statusByMa.getOrDefault("CKS", 0L));
        metrics.setTyLeHoanThanh(percent);
        metrics.setTyLeMauSac(color);
        metrics.setTienDoMauSac(color);
        metrics.setTienDoTheoTrangThai(statusByMa);
        metrics.setTienDoBuoc(buocList);
        return metrics;
    }

    private String formatNguoiDung(NguoiDung user) {
        if (user.getHoTen() == null || user.getHoTen().isBlank()) {
            return "—";
        }
        return user.getHoTen().trim();
    }

    private boolean matchesGeoFilter(String filter, String value) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        if (value == null || value.isBlank()) {
            return false;
        }
        return filter.trim().equalsIgnoreCase(value.trim());
    }

    private boolean matchesContractorFilter(String filter, String value) {
        if (filter == null || filter.isBlank() || "—".equals(filter.trim())) {
            return true;
        }
        if (value == null || value.isBlank() || "—".equals(value.trim())) {
            return false;
        }
        return filter.trim().equalsIgnoreCase(value.trim());
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        return fallback;
    }

    private boolean hasAnyMeaningfulGiaTri(HopDongDoiTuongResponse row, HopDongLabelLookups lookups) {
        if (row.getGiaTri() == null || row.getGiaTri().isEmpty()) {
            return false;
        }
        for (HopDongDoiTuongGiaTriResponse item : row.getGiaTri()) {
            if (item == null || item.getGiaTri() == null || item.getGiaTri().isBlank()) {
                continue;
            }
            ThuocTinhResponse definition = item.getThuocTinhId() != null && lookups != null
                    ? lookups.thuocTinhById().get(item.getThuocTinhId())
                    : null;
            if (!belongsToDoiTuong(definition, row.getDoiTuongQuanLyId())) {
                continue;
            }
            if (definition != null && isSystemThuocTinh(definition)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private static boolean isUuidPrefixFallback(String maTram, UUID id) {
        if (id == null || maTram == null) {
            return false;
        }
        String normalized = maTram.trim().toUpperCase(Locale.ROOT);
        return normalized.length() == 8 && normalized.equals(id.toString().substring(0, 8).toUpperCase(Locale.ROOT));
    }

    private static long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

}
