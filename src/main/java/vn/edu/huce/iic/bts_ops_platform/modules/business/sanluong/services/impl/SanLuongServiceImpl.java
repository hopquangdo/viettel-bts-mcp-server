package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.dto.RankedItemResponse;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScope;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScopeService;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.PaginationDefaults;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.common.util.VietnamDateUtils;
import vn.edu.huce.iic.bts_ops_platform.config.AppCacheProperties;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.common.event.SanLuongChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.cache.SanLuongCacheKeys;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.cache.SanLuongCacheNames;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.ContractCatalog;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.ContractWorkItem;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.ProgressMetrics;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request.SanLuongCapNhatDoiTuongRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request.SanLuongCapNhatHangMucItem;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request.SanLuongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request.SanLuongHangMucTaoItem;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request.SanLuongTaoHangLoatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request.SanLuongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.*;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.entity.SanLuong;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.entity.SanLuongAnh;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.exception.SanLuongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.mapper.SanLuongMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.repository.SanLuongAnhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.repository.SanLuongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.repository.SanLuongTongHopAggregate;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.services.SanLuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.support.RouteOutputVolumeValidator;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.response.TepDinhKemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.services.TepDinhKemService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucChiTiet;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucCongViec;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucNhom;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucChiTietService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucCongViecService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucNhomService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongGiaTriResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.HopDongDanhSachTienDoHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongGiaTriCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongGiaTriTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongGiaTriService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongDanhSachService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.HopDongDoiTuongSnapshot;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongSnapshotService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.response.NguoiDungThamChieuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.services.NguoiDungService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.services.VuongMacService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@lombok.extern.slf4j.Slf4j
@RequiredArgsConstructor
public class SanLuongServiceImpl implements SanLuongService {

    private static final String STATUS_PENDING = "pending";
    private static final int DOI_TUONG_BATCH_SIZE = 500;
    private static final int DEFAULT_HANG_MUC_PAGE_SIZE = 5;
    private static final int MAX_HANG_MUC_PAGE_SIZE = 200;
    private static final UUID UNUSED_FILTER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final LocalDate UNUSED_FILTER_DATE = LocalDate.EPOCH;

    private final SanLuongRepository sanLuongRepository;
    private final SanLuongMapper sanLuongMapper;
    private final SanLuongAnhRepository sanLuongAnhRepository;
    private final TepDinhKemService tepDinhKemService;
    private final HangMucCongViecService hangMucCongViecService;
    private final HangMucChiTietService hangMucChiTietService;
    private final HangMucNhomService hangMucNhomService;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final HopDongDoiTuongGiaTriService hopDongDoiTuongGiaTriService;
    private final ThuocTinhService thuocTinhService;
    private final HopDongDoiTuongSnapshotService hopDongDoiTuongSnapshotService;
    private final HopDongService hopDongService;
    @Autowired
    @Lazy
    private HopDongDanhSachService hopDongDanhSachService;
    private final ContractorScopeService contractorScopeService;
    private final NguoiDungService nguoiDungService;
    private final VuongMacService vuongMacService;
    private final CacheService cacheService;
    private final AppCacheProperties cacheProperties;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventContext appEventContext;

    // ===== CRUD thô trên SanLuong entity (giữ nguyên từ trước) =====

    @Override
    @Transactional(readOnly = true)
    public SanLuongResponse getById(UUID id) {
        return sanLuongMapper.toSanLuongResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal tongThanhTien(LocalDate dateFrom, LocalDate dateTo) {
        BigDecimal value = sanLuongRepository.sumThanhTien(Boolean.TRUE, dateFrom, dateTo);
        return value != null ? value : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, BigDecimal> tongThanhTienTheoLoaiHopDong() {
        Map<UUID, BigDecimal> result = new HashMap<>();
        for (Object[] row : sanLuongRepository.sumThanhTienGroupByLoaiHopDong(Boolean.TRUE)) {
            if (row[0] == null) {
                continue;
            }
            result.put((UUID) row[0], toBigDecimal(row[1]));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SanLuongDoiTuongTongResponse> tongThanhTienTheoDoiTuong() {
        List<SanLuongDoiTuongTongResponse> result = new ArrayList<>();
        for (Object[] row : sanLuongRepository.sumThanhTienGroupByHopDongDoiTuong(Boolean.TRUE)) {
            result.add(new SanLuongDoiTuongTongResponse(
                    (UUID) row[0], (UUID) row[1], toBigDecimal(row[2])));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SanLuongDoiTuongNgayTongResponse> tongThanhTienTheoDoiTuongVaNgay(LocalDate dateFrom, LocalDate dateTo) {
        List<SanLuongDoiTuongNgayTongResponse> result = new ArrayList<>();
        for (Object[] row : sanLuongRepository.sumThanhTienGroupByHopDongDoiTuongAndNgay(Boolean.TRUE, dateFrom, dateTo)) {
            LocalDate ngay = row[1] instanceof LocalDate localDate
                    ? localDate
                    : LocalDate.parse(row[1].toString());
            result.add(new SanLuongDoiTuongNgayTongResponse(
                    (UUID) row[0], ngay, toBigDecimal(row[2])));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, BigDecimal> tongThanhTienTheoDoiTuongIds(Collection<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds == null || hopDongDoiTuongIds.isEmpty()) {
            return Map.of();
        }
        List<UUID> ids = hopDongDoiTuongIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<UUID, BigDecimal> result = new HashMap<>();
        for (Object[] row : sanLuongRepository.sumTienTheoDoiTuongIds(ids)) {
            result.put((UUID) row[0], toBigDecimal(row[1]));
        }
        return result;
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }

    @Override
    @Transactional
    public SanLuongResponse create(SanLuongTaoRequest request) {
        SanLuong entity = sanLuongMapper.fromSanLuongTaoRequest(request);
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        SanLuong saved = sanLuongRepository.save(entity);
        ghiAuditSanLuong("NHAP_SAN_LUONG", "Nhập sản lượng", List.of(moTaBanGhi(saved)), saved.getHopDongId(), saved.getHopDongDoiTuongId());
        return sanLuongMapper.toSanLuongResponse(saved);
    }

    @Override
    @Transactional
    public SanLuongResponse update(UUID id, SanLuongCapNhatRequest request) {
        SanLuong entity = findById(id);
        assertSanLuongNotLockedAfterNghiemThuDat(entity);
        GiaTriSl truoc = GiaTriSl.of(entity);
        sanLuongMapper.updateFromSanLuongCapNhatRequest(request, entity);

        SanLuong saved = sanLuongRepository.save(entity);
        String thayDoi = truoc.diff(GiaTriSl.of(saved));
        if (thayDoi != null) {
            ghiAuditSanLuong("SUA_SAN_LUONG", "Sửa sản lượng", List.of(tenHangMuc(null, saved) + ": " + thayDoi), saved.getHopDongId(), saved.getHopDongDoiTuongId());
        }
        return sanLuongMapper.toSanLuongResponse(saved);
    }

    @Override
    @Transactional
    public SanLuongResponse nghiemThu(UUID id, String ketQua, String lyDo) {
        contractorScopeService.assertNotContractorForQualityAudit();
        if (!"dat".equals(ketQua) && !"khong_dat".equals(ketQua)) {
            throw new AppException(SanLuongErrorCode.SAN_LUONG_NGHIEM_THU_INVALID, "Kết quả nghiệm thu không hợp lệ");
        }
        if ("khong_dat".equals(ketQua) && (lyDo == null || lyDo.isBlank())) {
            throw new AppException(SanLuongErrorCode.SAN_LUONG_NGHIEM_THU_INVALID, "Cần nhập lý do khi kết quả không đạt");
        }
        SanLuong entity = findById(id);
        JwtUserPrincipal currentUser = SecurityContextHelper.currentUserOrNull();
        Instant now = Instant.now();
        if ("khong_dat".equals(ketQua)) {
            invalidateCatalogSanLuongAfterKhongDat(entity, lyDo.trim(), currentUser, now);
            applicationEventPublisher.publishEvent(
                    SanLuongChangedEvent.of(entity.getHopDongId(), entity.getHopDongDoiTuongId()));
            ghiAuditSanLuong("NGHIEM_THU_SAN_LUONG", "Nghiệm thu sản lượng: không đạt",
                    List.of(moTaBanGhi(entity) + "; lý do: " + lyDo.trim()), entity.getHopDongId(), entity.getHopDongDoiTuongId());
            return sanLuongMapper.toSanLuongResponse(entity);
        }
        entity.setKetQuaNghiemThu(ketQua);
        entity.setLyDoKhongDat(null);
        entity.setNguoiNghiemThuId(currentUser != null ? currentUser.id() : null);
        entity.setNguoiNghiemThuTen(currentUser != null ? currentUser.hoTen() : null);
        entity.setNgayNghiemThu(now);
        SanLuong saved = sanLuongRepository.save(entity);
        applicationEventPublisher.publishEvent(
                SanLuongChangedEvent.of(saved.getHopDongId(), saved.getHopDongDoiTuongId()));
        ghiAuditSanLuong("NGHIEM_THU_SAN_LUONG", "Nghiệm thu sản lượng: đạt", List.of(moTaBanGhi(saved)),
                saved.getHopDongId(), saved.getHopDongDoiTuongId());
        return sanLuongMapper.toSanLuongResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        SanLuong entity = findById(id);
        assertSanLuongNotLockedAfterNghiemThuDat(entity);
        String truoc = moTaBanGhi(entity);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        sanLuongRepository.save(entity);
        ghiAuditSanLuong("XOA_SAN_LUONG", "Xóa sản lượng", List.of(truoc), entity.getHopDongId(), entity.getHopDongDoiTuongId());
    }

    @Override
    @Transactional(readOnly = true)
    public long countActive() {
        return sanLuongRepository.findByNgayXoaIsNull().size();
    }

    private SanLuong findById(UUID id) {
        return sanLuongRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(SanLuongErrorCode.SAN_LUONG_NOT_FOUND, "Không tìm thấy bản ghi"));
    }

    // ===== Trang "sản lượng thi công" — bảng đối tượng (phân trang) + card tổng quan + workflow =====

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SanLuongDoiTuongRowResponse> listDoiTuong(
            UUID hopDongId,
            List<UUID> doiTuongQuanLyIds,
            UUID contractorId,
            LocalDate dateFrom,
            LocalDate dateTo,
            boolean includeWithoutOutput,
            Integer page,
            Integer size) {
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE);
        int pageNumber = EntityFilter.normalizePage(page);
        Set<UUID> scopeIds = resolveContractorScopeIds();
        if (scopeIds != null && scopeIds.isEmpty()) {
            return PageResponse.ofItems(List.of(), pageNumber, pageSize, 0);
        }
        boolean hasScope = scopeIds != null;
        Collection<UUID> scopeIdList = hasScope ? scopeIds : List.of();

        boolean hasDateFrom = dateFrom != null;
        boolean hasDateTo = dateTo != null;
        DoiTuongQuanLyFilter doiTuongFilter = DoiTuongQuanLyFilter.of(doiTuongQuanLyIds);

        Page<UUID> pageResult = sanLuongRepository.findPageRowsForSanLuong(
                hopDongId != null,
                uuidOrUnused(hopDongId),
                doiTuongFilter.enabled(),
                doiTuongFilter.ids(),
                hasScope,
                scopeIdList,
                contractorId != null,
                uuidOrUnused(contractorId),
                hasDateFrom,
                dateOrUnused(dateFrom),
                hasDateTo,
                dateOrUnused(dateTo),
                PageRequest.of(pageNumber, pageSize));

        return loadDoiTuongRowsForPage(
                pageResult, null, dateFrom, dateTo, doiTuongQuanLyIds, includeWithoutOutput, pageNumber, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SanLuongDoiTuongRowResponse> searchDoiTuong(
            String search,
            UUID hopDongId,
            List<UUID> doiTuongQuanLyIds,
            UUID contractorId,
            Integer page,
            Integer size) {
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE);
        int pageNumber = EntityFilter.normalizePage(page);
        Set<UUID> scopeIds = resolveContractorScopeIds();
        if (scopeIds != null && scopeIds.isEmpty()) {
            return PageResponse.ofItems(List.of(), pageNumber, pageSize, 0);
        }
        boolean hasScope = scopeIds != null;
        Collection<UUID> scopeIdList = hasScope ? scopeIds : List.of();

        String keyword = EntityFilter.normalizeSearch(search);
        if (!StringUtils.hasText(keyword)) {
            return PageResponse.ofItems(List.of(), pageNumber, pageSize, 0);
        }

        // Search luôn trả cả đối tượng CHƯA có sản lượng nào, miễn khớp từ khóa — yêu cầu "phải
        // có sản lượng" (requireOutput) chỉ có ý nghĩa cho listDoiTuong (báo cáo theo kỳ), không
        // áp dụng khi tìm theo mã/tên. Cũng bỏ lọc thời gian để tra được đối tượng dù SL nằm
        // ngoài kỳ đang xem.
        DoiTuongQuanLyFilter doiTuongFilter = DoiTuongQuanLyFilter.of(doiTuongQuanLyIds);
        Page<UUID> pageResult = sanLuongRepository.findPageRowsForSanLuongByKeyword(
                hopDongId != null,
                uuidOrUnused(hopDongId),
                doiTuongFilter.enabled(),
                doiTuongFilter.ids(),
                hasScope,
                scopeIdList,
                contractorId != null,
                uuidOrUnused(contractorId),
                false,
                keyword,
                PageRequest.of(pageNumber, pageSize));

        return loadDoiTuongRowsForPage(
                pageResult, keyword, null, null, doiTuongQuanLyIds, true, pageNumber, pageSize);
    }

    /** Hợp nhất phần cache-aside dùng chung cho listDoiTuong/searchDoiTuong — contractorId đã lọc ở SQL. */
    private PageResponse<SanLuongDoiTuongRowResponse> loadDoiTuongRowsForPage(
            Page<UUID> pageResult,
            String keyword,
            LocalDate rowDateFrom,
            LocalDate rowDateTo,
            List<UUID> doiTuongQuanLyIds,
            boolean includeWithoutOutput,
            int pageNumber,
            int pageSize) {
        List<UUID> pageIds = pageResult.getContent();
        long total = pageResult.getTotalElements();

        if (pageIds.isEmpty()) {
            return PageResponse.ofItems(List.of(), pageNumber, pageSize, total);
        }

        Function<UUID, String> keyGen = id -> id + ":" + keyword + ":" + rowDateFrom + ":" + rowDateTo;
        List<String> cacheKeys = pageIds.stream().map(keyGen).toList();
        List<SanLuongDoiTuongRowResponse> cachedRows = cacheService.multiGet(
                SanLuongCacheNames.DOI_TUONG_ROW, cacheKeys, SanLuongDoiTuongRowResponse.class);

        List<SanLuongDoiTuongRowResponse> allRows = new ArrayList<>(pageIds.size());
        LinkedHashSet<UUID> missedIds = new LinkedHashSet<>();

        for (int i = 0; i < pageIds.size(); i++) {
            SanLuongDoiTuongRowResponse cached = cachedRows.get(i);
            if (cached != null) {
                allRows.add(cached);
            } else {
                missedIds.add(pageIds.get(i));
            }
        }

        if (!missedIds.isEmpty()) {
            List<SanLuongDoiTuongRowResponse> dbRows = buildDoiTuongRowsForIds(
                    missedIds, doiTuongQuanLyIds, rowDateFrom, rowDateTo, includeWithoutOutput);

            Map<String, Object> cacheUpdates = new HashMap<>();
            for (SanLuongDoiTuongRowResponse row : dbRows) {
                cacheUpdates.put(keyGen.apply(row.getId()), row);
                allRows.add(row);
            }
            cacheService.multiPut(SanLuongCacheNames.DOI_TUONG_ROW, cacheUpdates, cacheProperties.sanluongDoiTuongRowTtl());
        }

        Map<UUID, SanLuongDoiTuongRowResponse> byId = allRows.stream()
                .collect(Collectors.toMap(SanLuongDoiTuongRowResponse::getId, r -> r, (a, b) -> a));

        List<SanLuongDoiTuongRowResponse> ordered = pageIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();

        return PageResponse.ofItems(ordered, pageNumber, pageSize, total);
    }

    @Override
    @Transactional(readOnly = true)
    public SanLuongTongHopResponse tongHop(
            String search,
            UUID hopDongId,
            List<UUID> doiTuongQuanLyIds,
            UUID contractorId,
            LocalDate dateFrom,
            LocalDate dateTo) {
        String cacheKey = buildTongHopCacheKey(search, hopDongId, doiTuongQuanLyIds, contractorId, dateFrom, dateTo);
        Optional<SanLuongTongHopResponse> cached =
                cacheService.get(SanLuongCacheNames.TONG_HOP, cacheKey, SanLuongTongHopResponse.class);
        if (cached.isPresent()) {
            return cached.get();
        }

        String keyword = EntityFilter.normalizeSearch(search);
        boolean hasKeyword = StringUtils.hasText(keyword);
        LocalDate statsDateFrom = hasKeyword ? null : dateFrom;
        LocalDate statsDateTo = hasKeyword ? null : dateTo;
        SanLuongTongHopResponse stats = computeTongHop(hopDongId, doiTuongQuanLyIds, contractorId, statsDateFrom, statsDateTo);
        cacheService.put(SanLuongCacheNames.TONG_HOP, cacheKey, stats, cacheProperties.sanluongTongHopTtl());
        return stats;
    }

    private String buildTongHopCacheKey(
            String search,
            UUID hopDongId,
            List<UUID> doiTuongQuanLyIds,
            UUID contractorId,
            LocalDate dateFrom,
            LocalDate dateTo) {
        JwtUserPrincipal user = SecurityContextHelper.currentUserOrNull();
        String userPart = user != null ? user.id().toString() : null;
        return SanLuongCacheKeys.tongHop(
                hopDongId, doiTuongQuanLyIds, userPart, search, contractorId, dateFrom, dateTo);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SanLuongHangMucItemResponse> listHangMuc(
            UUID hopDongDoiTuongId,
            Integer page,
            Integer size) {
        contractorScopeService.assertHopDongDoiTuongAccessible(hopDongDoiTuongId);
        HopDongDoiTuongResponse doiTuong = hopDongDoiTuongService.getById(hopDongDoiTuongId);
        HopDong hopDong = requireHopDong(doiTuong.getHopDongId());

        List<ContractWorkItem> contractWorkItems = loadContractWorkCatalog(hopDong.getId()).items();
        List<SanLuong> records = sanLuongRepository.findByHopDongDoiTuongIdAndNgayXoaIsNull(hopDongDoiTuongId);

        int pageSize = EntityFilter.normalizeSize(size, DEFAULT_HANG_MUC_PAGE_SIZE, MAX_HANG_MUC_PAGE_SIZE);
        int pageNumber = EntityFilter.normalizePage(page);
        int total = contractWorkItems.size();
        int startIndex = Math.min(pageNumber * pageSize, total);
        int endIndex = Math.min(startIndex + pageSize, total);

        List<ContractWorkItem> pageCatalog = contractWorkItems.subList(startIndex, endIndex);
        List<SanLuongHangMucItemResponse> items = toWorkItemsFromCatalogSlice(
                pageCatalog, filterActiveSanLuong(records), records, startIndex + 1);

        return PageResponse.ofItems(items, pageNumber, pageSize, total);
    }

    @Override
    @Transactional(readOnly = true)
    public SanLuongDoiTuongChiTietResponse getDoiTuongChiTiet(UUID hopDongDoiTuongId, boolean includeWorkItems) {
        contractorScopeService.assertHopDongDoiTuongAccessible(hopDongDoiTuongId);
        HopDongDoiTuongResponse doiTuong = hopDongDoiTuongService.getById(hopDongDoiTuongId);
        HopDong hopDong = requireHopDong(doiTuong.getHopDongId());

        List<ContractWorkItem> contractWorkItems = loadContractWorkCatalog(hopDong.getId()).items();
        List<SanLuong> allRecords = sanLuongRepository.findByHopDongDoiTuongIdAndNgayXoaIsNull(hopDongDoiTuongId);
        List<SanLuong> activeRecords = filterActiveSanLuong(allRecords);

        HopDongDoiTuongSnapshot descriptor = hopDongDoiTuongSnapshotService.getSnapshot(hopDongDoiTuongId);

        SanLuongDoiTuongChiTietResponse detail = new SanLuongDoiTuongChiTietResponse();
        detail.setHopDongDoiTuongId(hopDongDoiTuongId);
        detail.setHopDongId(doiTuong.getHopDongId());
        detail.setDoiTuongQuanLyId(doiTuong.getDoiTuongQuanLyId());
        detail.setObjectCode(descriptor != null ? descriptor.getMaDoiTuong() : "—");
        detail.setContract(descriptor != null ? descriptor.getTenHopDong() : resolveContractLabel(hopDong));
        detail.setContractor(descriptor != null ? descriptor.getNhaThau() : "—");
        detail.setRegion(descriptor != null ? descriptor.getKhuVuc() : "—");
        detail.setRouteLengthKm(resolveRouteLengthKm(doiTuong));
        detail.setStartStation(firstNonBlank(findGiaTri(doiTuong.getGiaTri(), "trạm đầu", "tram dau"), "—"));
        detail.setEndStation(firstNonBlank(findGiaTri(doiTuong.getGiaTri(), "trạm cuối", "tram cuoi"), "—"));
        detail.setSurveyDate(earliestDate(activeRecords, "survey"));
        detail.setTotalOutput(sumProductiveMoney(activeRecords));
        detail.setWorkItemsTotal(contractWorkItems.size());
        if (includeWorkItems) {
            detail.setWorkItems(toWorkItemsFromRecords(contractWorkItems, activeRecords, allRecords));
        }
        List<SanLuongAnh> photos = sanLuongAnhRepository.findByHopDongDoiTuongIdAndNgayXoaIsNullOrderByNgayTaoDesc(
                hopDongDoiTuongId);
        detail.setPhotoSections(buildPhotoSections(activeRecords, contractWorkItems, photos));
        detail.setOutputLocked(isDoiTuongOutputLocked(doiTuong));
        detail.setCoVuongMacMo(Boolean.TRUE.equals(doiTuong.getCoVuongMacMo()));
        return detail;
    }

    @Override
    @Transactional(readOnly = true)
    public SanLuongNhatKyResponse listNhatKy(UUID hopDongDoiTuongId) {
        contractorScopeService.assertHopDongDoiTuongAccessible(hopDongDoiTuongId);
        HopDongDoiTuongResponse doiTuong = hopDongDoiTuongService.getById(hopDongDoiTuongId);
        HopDong hopDong = requireHopDong(doiTuong.getHopDongId());

        List<ContractWorkItem> contractWorkItems = loadContractWorkCatalog(hopDong.getId()).items();
        Map<UUID, ContractWorkItem> workItemByKey = contractWorkItems.stream()
                .collect(Collectors.toMap(ContractWorkItem::catalogKey, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        SanLuongNhatKyResponse response = new SanLuongNhatKyResponse();
        for (ContractWorkItem workItem : contractWorkItems) {
            SanLuongNhatKyColumnResponse column = new SanLuongNhatKyColumnResponse();
            column.setId(workItem.catalogKey().toString());
            column.setName(workItem.name());
            column.setShortLabel(resolveHangMucShortLabel(workItem.name()));
            response.getColumns().add(column);
        }

        List<SanLuong> records = sanLuongRepository.findByHopDongDoiTuongIdAndNgayXoaIsNull(hopDongDoiTuongId);
        Map<String, List<SanLuong>> groupedByBatch = records.stream()
                .collect(Collectors.groupingBy(this::resolveBatchKey, LinkedHashMap::new, Collectors.toList()));

        List<SanLuongNhatKyEntryResponse> entries = groupedByBatch.values().stream()
                .map(batch -> toNhatKyEntry(batch, workItemByKey))
                .sorted(Comparator.comparing(
                        SanLuongNhatKyEntryResponse::getUpdatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        response.getEntries().addAll(entries);
        return response;
    }

    private SanLuongTongHopResponse computeTongHop(
            UUID hopDongId,
            List<UUID> doiTuongQuanLyIds,
            UUID contractorId,
            LocalDate dateFrom,
            LocalDate dateTo) {
        Set<UUID> scopeIds = resolveContractorScopeIds();
        if (scopeIds != null && scopeIds.isEmpty()) {
            return emptyTongHopResponse();
        }

        boolean hasScope = scopeIds != null;
        List<UUID> scopeIdList = hasScope ? new ArrayList<>(scopeIds) : List.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        LocalDate today = VietnamDateUtils.today();
        DoiTuongQuanLyFilter doiTuongFilter = DoiTuongQuanLyFilter.of(doiTuongQuanLyIds);

        Optional<SanLuongTongHopAggregate> aggregateResult = sanLuongRepository.aggregateTongHop(
                hopDongId != null,
                uuidOrUnused(hopDongId),
                doiTuongFilter.enabled(),
                doiTuongFilter.ids(),
                hasScope,
                scopeIdList,
                dateFrom != null,
                dateOrUnused(dateFrom),
                dateTo != null,
                dateOrUnused(dateTo),
                today,
                contractorId != null,
                uuidOrUnused(contractorId));

        SanLuongTongHopAggregate aggregate = aggregateResult.orElse(null);

        if (aggregate == null || aggregate.getTotalDisplayed() == null || aggregate.getTotalDisplayed() == 0) {
            return emptyTongHopResponse();
        }

        BigDecimal periodTotal = aggregate.getPeriodTotal() != null ? aggregate.getPeriodTotal() : BigDecimal.ZERO;
        int totalDisplayed = aggregate.getTotalDisplayed();

        SanLuongTongHopResponse stats = new SanLuongTongHopResponse();
        stats.setTotalDisplayed(totalDisplayed);
        stats.setWithOutput(aggregate.getWithOutput() != null ? aggregate.getWithOutput() : 0);
        stats.setPeriodTotal(periodTotal);
        stats.setTodayTotal(aggregate.getTodayTotal() != null ? aggregate.getTodayTotal() : BigDecimal.ZERO);
        long issueCount = vuongMacService.demMoTrongGiaiDoan(
                hopDongId,
                doiTuongQuanLyIds != null && doiTuongQuanLyIds.size() == 1 ? doiTuongQuanLyIds.get(0) : null,
                hasScope,
                scopeIdList,
                dateFrom,
                dateTo);
        stats.setIssueCount((int) issueCount);
        stats.setAveragePerDoiTuong(periodTotal.divide(
                BigDecimal.valueOf(totalDisplayed), 2, RoundingMode.HALF_UP));
        stats.setPeriodGrowthPercent(BigDecimal.ZERO);
        return stats;
    }

    private SanLuongTongHopResponse emptyTongHopResponse() {
        SanLuongTongHopResponse stats = new SanLuongTongHopResponse();
        stats.setTotalDisplayed(0);
        stats.setWithOutput(0);
        stats.setPeriodTotal(BigDecimal.ZERO);
        stats.setTodayTotal(BigDecimal.ZERO);
        stats.setIssueCount(0);
        stats.setAveragePerDoiTuong(BigDecimal.ZERO);
        stats.setPeriodGrowthPercent(BigDecimal.ZERO);
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public SanLuongBoLocResponse boLoc() {
        SanLuongBoLocResponse response = new SanLuongBoLocResponse();
        response.getPartners().add(option("all", "Tất cả nhà thầu"));
        response.getContracts().add(option("all", "Tất cả hợp đồng"));

        Optional<ContractorScope> contractorScope = contractorScopeService.currentScope();

        hopDongService.findAllActiveEntities().stream()
                .filter(hopDong -> Boolean.TRUE.equals(hopDong.getHoatDong()))
                .filter(hopDong -> contractorScope.isEmpty()
                        || contractorScope.get().hopDongIds().contains(hopDong.getId()))
                .sorted(Comparator.comparing(this::resolveContractLabel, String.CASE_INSENSITIVE_ORDER))
                .forEach(hopDong -> response.getContracts().add(
                        option(hopDong.getId().toString(), resolveContractLabel(hopDong))));

        List<NguoiDungThamChieuResponse> contractors =
                nguoiDungService.listThamChieu("nha_thau", null, true);
        if (contractorScope.isPresent()) {
            JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
            contractors = contractors.stream()
                    .filter(contractor -> contractor.getId().equals(user.id())
                            || user.hoTen().equalsIgnoreCase(contractor.getHoTen()))
                    .toList();
        }
        contractors.forEach(contractor ->
                response.getPartners().add(option(contractor.getId().toString(), contractor.getHoTen())));

        return response;
    }

    @Override
    @Transactional
    public SanLuongDoiTuongRowResponse taoHangLoat(SanLuongTaoHangLoatRequest request) {
        if (request.getHopDongDoiTuongId() == null) {
            throw new AppException(SanLuongErrorCode.SAN_LUONG_NOT_FOUND, "Thiếu đối tượng hợp đồng");
        }
        if (request.getEntries() == null || request.getEntries().isEmpty()) {
            throw new IllegalArgumentException("Phải có ít nhất một hạng mục sản lượng");
        }
        List<SanLuongHangMucTaoItem> productiveEntries = request.getEntries().stream()
                .filter(entry -> entry.getAmount() != null && entry.getAmount().signum() > 0)
                .toList();
        if (productiveEntries.isEmpty()) {
            throw new IllegalArgumentException("Khối lượng phải lớn hơn 0");
        }
        assertDoiTuongActiveForOutput(request.getHopDongDoiTuongId());
        HopDongDoiTuongResponse doiTuong = hopDongDoiTuongService.getById(request.getHopDongDoiTuongId());
        UUID hopDongId = request.getHopDongId() != null ? request.getHopDongId() : doiTuong.getHopDongId();
        requireHopDong(hopDongId);
        ContractCatalog catalog = loadContractWorkCatalog(hopDongId);
        LocalDate ngay = request.getNgayThucHien() != null ? request.getNgayThucHien() : VietnamDateUtils.today();
        UUID batchId = UUID.randomUUID();

        List<SanLuong> existingRecords =
                sanLuongRepository.findByHopDongDoiTuongIdAndNgayXoaIsNull(request.getHopDongDoiTuongId());
        for (SanLuongHangMucTaoItem entry : productiveEntries) {
            validateCatalogEntry(entry, catalog);
            UUID catalogKey = resolveCatalogKeyFromEntry(entry);
            if (catalogKey != null) {
                assertCatalogKeyNotNghiemThuDat(catalogKey, existingRecords, catalog.items());
            }
        }
        assertRouteOutputVolumesAfterPlannedAdds(
                catalog,
                sanLuongRepository.findByHopDongDoiTuongIdAndNgayXoaIsNull(request.getHopDongDoiTuongId()),
                productiveEntries);

        List<String> nhapChiTiet = new ArrayList<>();
        for (SanLuongHangMucTaoItem entry : productiveEntries) {
            SanLuong entity = new SanLuong();
            entity.setHopDongId(hopDongId);
            entity.setHopDongDoiTuongId(request.getHopDongDoiTuongId());
            entity.setHoatDong(true);
            if (entry.getHangMucCongViecId() != null) {
                entity.setHangMucCongViecId(entry.getHangMucCongViecId());
                entity.setHangMucChiTietId(entry.getHangMucChiTietId());
            } else {
                entity.setHangMucChiTietId(entry.getHangMucChiTietId());
            }
            entity.setNgayThucHien(ngay);
            entity.setLanCapNhatId(batchId);
            entity.setDonGia(entry.getDonGia());
            entity.setKhoiLuongHoanThanh(entry.getAmount());
            entity.setTrangThai(firstNonBlank(entry.getTrangThai(), "done"));
            entity.setGhiChu(firstNonBlank(entry.getGhiChu(), request.getGhiChu()));
            sanLuongRepository.save(entity);
            nhapChiTiet.add(tenHangMuc(catalog, entity) + ": " + moTaGiaTri(entity));
        }

        maybeGanChieuDaiTuyenTuKhaoSat(doiTuong, productiveEntries);
        ghiAuditSanLuong("NHAP_SAN_LUONG", "Nhập sản lượng", nhapChiTiet, hopDongId, request.getHopDongDoiTuongId());

        applicationEventPublisher.publishEvent(
                SanLuongChangedEvent.of(hopDongId, request.getHopDongDoiTuongId()));

        return requireDoiTuongRow(
                buildDoiTuongRows(null, hopDongId, null, null, null, null, Set.of(request.getHopDongDoiTuongId()), true),
                request.getHopDongDoiTuongId(),
                "Không tạo được sản lượng");
    }

    @Override
    @Transactional
    public SanLuongDoiTuongRowResponse capNhatDoiTuong(UUID hopDongDoiTuongId, SanLuongCapNhatDoiTuongRequest request) {
        assertDoiTuongActiveForOutput(hopDongDoiTuongId);
        HopDongDoiTuongResponse doiTuong = hopDongDoiTuongService.getById(hopDongDoiTuongId);
        UUID hopDongId = doiTuong.getHopDongId();
        requireHopDong(hopDongId);
        ContractCatalog catalog = loadContractWorkCatalog(hopDongId);

        List<SanLuong> existing = sanLuongRepository.findByHopDongDoiTuongIdAndNgayXoaIsNull(hopDongDoiTuongId);
        Map<UUID, SanLuong> byId = existing.stream()
                .collect(Collectors.toMap(SanLuong::getId, Function.identity()));
        Map<UUID, SanLuong> byCatalogKey = indexLatestSanLuongByCatalogKey(
                filterActiveSanLuong(existing), catalog.items());

        Set<UUID> keepIds = new HashSet<>();
        UUID batchId = UUID.randomUUID();
        List<String> nhapChiTiet = new ArrayList<>();
        List<String> suaChiTiet = new ArrayList<>();
        List<String> xoaChiTiet = new ArrayList<>();
        for (SanLuongCapNhatHangMucItem item : request.getWorkItems()) {
            SanLuong entity = resolveExistingRecord(item, byId, byCatalogKey);
            if (entity != null && !Boolean.TRUE.equals(entity.getHoatDong())) {
                entity = null;
            }
            if (entity != null) {
                assertSanLuongNotLockedAfterNghiemThuDat(entity);
            } else {
                UUID newKey = item.getHangMucCongViecId() != null
                        ? item.getHangMucCongViecId()
                        : item.getHangMucChiTietId();
                if (newKey != null) {
                    assertCatalogKeyNotNghiemThuDat(newKey, existing, catalog.items());
                }
            }
            if (entity == null) {
                if (item.getHangMucCongViecId() != null) {
                    requireAllowedCongViec(item.getHangMucCongViecId(), catalog.congViecIds());
                    entity = new SanLuong();
                    entity.setHopDongDoiTuongId(hopDongDoiTuongId);
                    entity.setHopDongId(hopDongId);
                    entity.setHangMucCongViecId(item.getHangMucCongViecId());
                    entity.setHoatDong(true);
                } else if (item.getHangMucChiTietId() != null
                        && catalog.chiTietLeafIds().contains(item.getHangMucChiTietId())) {
                    entity = new SanLuong();
                    entity.setHopDongDoiTuongId(hopDongDoiTuongId);
                    entity.setHopDongId(hopDongId);
                    entity.setHangMucChiTietId(item.getHangMucChiTietId());
                    entity.setHoatDong(true);
                } else {
                    throw new AppException(SanLuongErrorCode.SAN_LUONG_NOT_FOUND, "Hạng mục không thuộc hợp đồng");
                }
            } else {
                keepIds.add(entity.getId());
                if (item.getHangMucCongViecId() != null) {
                    requireAllowedCongViec(item.getHangMucCongViecId(), catalog.congViecIds());
                    entity.setHangMucCongViecId(item.getHangMucCongViecId());
                } else if (item.getHangMucChiTietId() != null) {
                    if (!catalog.chiTietLeafIds().contains(item.getHangMucChiTietId())) {
                        throw new AppException(SanLuongErrorCode.SAN_LUONG_NOT_FOUND, "Hạng mục không thuộc hợp đồng");
                    }
                    entity.setHangMucCongViecId(null);
                    entity.setHangMucChiTietId(item.getHangMucChiTietId());
                }
            }
            String status = firstNonBlank(item.getStatus(), STATUS_PENDING);
            LocalDate executionDate = item.getDate();
            if (executionDate == null && entity.getNgayThucHien() != null) {
                executionDate = entity.getNgayThucHien();
            }
            if (executionDate == null) {
                executionDate = VietnamDateUtils.today();
            }
            boolean laBanGhiMoi = entity.getId() == null;
            GiaTriSl giaTriTruoc = laBanGhiMoi ? null : GiaTriSl.of(entity);
            entity.setNgayThucHien(executionDate);
            entity.setLanCapNhatId(batchId);
            entity.setDonGia(item.getUnitPrice());
            entity.setKhoiLuongHoanThanh(item.getAmount());
            entity.setTrangThai(status);
            sanLuongRepository.save(entity);
            keepIds.add(entity.getId());
            if (laBanGhiMoi) {
                nhapChiTiet.add(tenHangMuc(catalog, entity) + ": " + moTaGiaTri(entity));
            } else {
                String thayDoi = giaTriTruoc.diff(GiaTriSl.of(entity));
                if (thayDoi != null) {
                    suaChiTiet.add(tenHangMuc(catalog, entity) + ": " + thayDoi);
                }
            }
        }

        for (SanLuong entity : existing) {
            if (!keepIds.contains(entity.getId())) {
                if (!Boolean.TRUE.equals(entity.getHoatDong())) {
                    continue;
                }
                assertSanLuongNotLockedAfterNghiemThuDat(entity);
                xoaChiTiet.add(tenHangMuc(catalog, entity) + ": " + moTaGiaTri(entity));
                entity.setNgayXoa(Instant.now());
                entity.setHoatDong(false);
                sanLuongRepository.save(entity);
            }
        }

        assertRouteOutputVolumesFromPersisted(catalog, hopDongDoiTuongId);

        applicationEventPublisher.publishEvent(SanLuongChangedEvent.of(hopDongId, hopDongDoiTuongId));
        ghiAuditSanLuong("NHAP_SAN_LUONG", "Nhập sản lượng", nhapChiTiet, hopDongId, hopDongDoiTuongId);
        ghiAuditSanLuong("SUA_SAN_LUONG", "Sửa sản lượng", suaChiTiet, hopDongId, hopDongDoiTuongId);
        ghiAuditSanLuong("XOA_SAN_LUONG", "Xóa sản lượng", xoaChiTiet, hopDongId, hopDongDoiTuongId);

        return requireDoiTuongRow(
                buildDoiTuongRows(null, null, null, null, null, null, Set.of(hopDongDoiTuongId), true),
                hopDongDoiTuongId,
                "Không tìm thấy đối tượng hợp đồng");
    }

    @Override
    @Transactional
    public SanLuongDoiTuongRowResponse xacNhanHoanThanhDoiTuong(UUID hopDongDoiTuongId) {
        contractorScopeService.assertNotContractorForQualityAudit();
        contractorScopeService.assertHopDongDoiTuongAccessible(hopDongDoiTuongId);
        HopDongDoiTuongResponse doiTuong = hopDongDoiTuongService.getById(hopDongDoiTuongId);
        HopDong hopDong = requireHopDong(doiTuong.getHopDongId());

        if (isDoiTuongOutputLocked(doiTuong)) {
            throw new AppException(
                    SanLuongErrorCode.SAN_LUONG_DOI_TUONG_COMPLETED,
                    "Đối tượng đã hoàn thành, không thể xác nhận lại");
        }
        if (Boolean.TRUE.equals(doiTuong.getCoVuongMacMo())) {
            throw new AppException(
                    SanLuongErrorCode.SAN_LUONG_DOI_TUONG_CO_VUONG_MAC,
                    "Đối tượng đang có vướng mắc chưa xử lý/đang xử lý, không thể xác nhận hoàn thành");
        }

        HopDongDoiTuongCapNhatRequest updateRequest = new HopDongDoiTuongCapNhatRequest();
        updateRequest.setNgayHtTc(VietnamDateUtils.today());
        UUID hoanThanhTrangThaiId = resolveHoanThanhTrangThaiId(hopDong);
        if (hoanThanhTrangThaiId != null
                && !HopDongDanhSachTienDoHelper.isCompletedStatus(doiTuong.getTrangThaiMa())) {
            updateRequest.setTrangThaiHopDongId(hoanThanhTrangThaiId);
        }
        hopDongDoiTuongService.update(hopDongDoiTuongId, updateRequest);

        recomputeAndCacheDoiTuongRow(hopDongDoiTuongId, doiTuong.getDoiTuongQuanLyId());
        recomputeAndCacheTongHop(hopDong.getId());

        return requireDoiTuongRow(
                buildDoiTuongRows(null, null, null, null, null, null, Set.of(hopDongDoiTuongId), true),
                hopDongDoiTuongId,
                "Không tìm thấy đối tượng hợp đồng");
    }

    private UUID resolveHoanThanhTrangThaiId(HopDong hopDong) {
        if (hopDong.getKieuHopDongId() == null || hopDong.getLoaiHopDongId() == null) {
            return null;
        }
        List<LuongTrangThaiBuocResponse> flowBuoc = hopDongDanhSachService.getFlowBuocForKieu(
                hopDong.getLoaiHopDongId(),
                hopDong.getKieuHopDongId());
        return HopDongDanhSachTienDoHelper.resolveHoanThanhTrangThaiId(flowBuoc);
    }

    @Override
    @Transactional
    public SanLuongAnhResponse uploadAnh(
            UUID hopDongDoiTuongId,
            MultipartFile file,
            UUID hangMucCongViecId,
            UUID hangMucChiTietId,
            String loaiAnh,
            String moTa) {
        contractorScopeService.assertHopDongDoiTuongAccessible(hopDongDoiTuongId);
        assertDoiTuongActiveForOutput(hopDongDoiTuongId);
        HopDongDoiTuongResponse doiTuong = hopDongDoiTuongService.getById(hopDongDoiTuongId);
        HopDong hopDong = requireHopDong(doiTuong.getHopDongId());
        if (file == null || file.isEmpty()) {
            throw new AppException(SanLuongErrorCode.SAN_LUONG_NOT_FOUND, "Thiếu file ảnh");
        }
        ResolvedHangMucUpload resolved = resolveUploadHangMucIds(
                hopDong.getId(), hangMucCongViecId, hangMucChiTietId);
        TepDinhKemResponse tep = tepDinhKemService.upload(file, "san-luong");
        SanLuongAnh entity = new SanLuongAnh();
        entity.setHopDongDoiTuongId(hopDongDoiTuongId);
        entity.setHangMucCongViecId(resolved.congViecId());
        entity.setHangMucChiTietId(resolved.chiTietId());
        entity.setTepDinhKemId(tep.getId());
        entity.setLoaiAnh(firstNonBlank(loaiAnh, "construction"));
        entity.setMoTa(moTa);
        entity.setDaHoanThanh("construction".equalsIgnoreCase(entity.getLoaiAnh()));
        entity.setHoatDong(true);
        return toAnhResponse(sanLuongAnhRepository.save(entity), tep);
    }

    @Override
    @Transactional
    public void xoaDoiTuong(UUID hopDongDoiTuongId) {
        contractorScopeService.assertHopDongDoiTuongAccessible(hopDongDoiTuongId);
        hopDongDoiTuongService.delete(hopDongDoiTuongId);
    }

    @Override
    @Transactional(readOnly = true)
    public void recomputeAndCacheDoiTuongRow(UUID hopDongDoiTuongId, UUID doiTuongQuanLyId) {
        try {
            List<SanLuongDoiTuongRowResponse> rows = buildDoiTuongRowsForIds(
                    Set.of(hopDongDoiTuongId),
                    doiTuongQuanLyId != null ? List.of(doiTuongQuanLyId) : List.of(),
                    null, null, // no date filter — cache full history row
                    false);
            if (!rows.isEmpty()) {
                String key = hopDongDoiTuongId.toString() + ":null:null";
                cacheService.put(SanLuongCacheNames.DOI_TUONG_ROW, key, rows.get(0),
                        cacheProperties.sanluongDoiTuongRowTtl());
            }
        } catch (Exception ex) {
//            logger.warn("Failed to warm doituong-row cache for hopDongDoiTuongId={}", hopDongDoiTuongId, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void recomputeAndCacheTongHop(UUID hopDongId) {
        try {
            // 1. Evict tất cả cache tổng hợp có prefix của hợp đồng này
            if (hopDongId != null) {
                cacheService.evictByPrefix(SanLuongCacheNames.TONG_HOP,
                        SanLuongCacheKeys.tongHopPrefixForHopDong(hopDongId));
            }
            // Evict global cache (key đầu tiên là empty — không tâm hopDongId)
            cacheService.evictByPrefix(SanLuongCacheNames.TONG_HOP,
                    SanLuongCacheKeys.tongHopPrefixForHopDong(null));

            // 2. Warm lại: global (no filter) và per-hợp-đồng
            JwtUserPrincipal currentUser = SecurityContextHelper.currentUserOrNull();
            String userPart = currentUser != null ? currentUser.id().toString() : null;

            SanLuongTongHopResponse globalStats = computeTongHop(null, null, null, null, null);
            String globalKey = SanLuongCacheKeys.tongHop(null, List.of(), userPart, null, null, null, null);
            cacheService.put(SanLuongCacheNames.TONG_HOP, globalKey, globalStats,
                    cacheProperties.sanluongTongHopTtl());

            if (hopDongId != null) {
                SanLuongTongHopResponse hopDongStats = computeTongHop(hopDongId, null, null, null, null);
                String hopDongKey = SanLuongCacheKeys.tongHop(hopDongId, List.of(), userPart, null, null, null, null);
                cacheService.put(SanLuongCacheNames.TONG_HOP, hopDongKey, hopDongStats,
                        cacheProperties.sanluongTongHopTtl());
            }
        } catch (Exception ex) {
//            log.warn("Failed to warm tonghop cache for hopDongId={}", hopDongId, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SanLuongProgressResponse getProgress(UUID hopDongDoiTuongId) {
        if (hopDongDoiTuongId == null) {
            return new SanLuongProgressResponse(0, 0, 0, BigDecimal.ZERO);
        }
        List<HopDongDoiTuongResponse> doiTuongSource = hopDongDoiTuongService.getByIds(List.of(hopDongDoiTuongId));
        if (doiTuongSource.isEmpty()) {
            return new SanLuongProgressResponse(0, 0, 0, BigDecimal.ZERO);
        }
        HopDongDoiTuongResponse doiTuong = doiTuongSource.get(0);

        List<SanLuong> records = sanLuongRepository
                .findByHopDongDoiTuongIdInAndNgayXoaIsNull(List.of(hopDongDoiTuongId))
                .stream()
                .filter(item -> Boolean.TRUE.equals(item.getHoatDong()))
                .toList();

        List<ContractWorkItem> contractWorkItems = doiTuong.getHopDongId() != null
                ? loadContractWorkCatalogByHopDongIds(Set.of(doiTuong.getHopDongId()))
                        .getOrDefault(doiTuong.getHopDongId(), List.of())
                : List.of();

        Map<UUID, SanLuong> sanLuongByCatalogKey = indexLatestSanLuongByCatalogKey(records, contractWorkItems);
        ProgressMetrics metrics = computeProgress(
                contractWorkItems,
                sanLuongByCatalogKey,
                records,
                records,
                VietnamDateUtils.today());

        // issueCount thật lấy từ coVuongMacMo denormalize — xem ghi chú ở buildDoiTuongRows().
        int issueCount = Boolean.TRUE.equals(doiTuong.getCoVuongMacMo()) ? 1 : 0;
        return new SanLuongProgressResponse(
                metrics.itemsDone(), metrics.itemsTotal(), issueCount, metrics.totalOutput());
    }

    private List<SanLuongDoiTuongRowResponse> buildDoiTuongRows(
            String search,
            UUID hopDongId,
            List<UUID> doiTuongQuanLyIds,
            String contractor,
            LocalDate dateFrom,
            LocalDate dateTo,
            Set<UUID> scopedHopDongDoiTuongIds,
            boolean includeWithoutOutput) {
        if (scopedHopDongDoiTuongIds != null && scopedHopDongDoiTuongIds.isEmpty()) {
            return List.of();
        }

        List<HopDongDoiTuongResponse> doiTuongSource;
        if (scopedHopDongDoiTuongIds != null && !scopedHopDongDoiTuongIds.isEmpty()) {
            doiTuongSource = hopDongDoiTuongService.getByIds(scopedHopDongDoiTuongIds);
        } else {
            doiTuongSource = loadActiveDoiTuongResponses(hopDongId);
        }
        LinkedHashSet<UUID> ids = doiTuongSource.stream()
                .map(HopDongDoiTuongResponse::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return buildDoiTuongRowsForIds(ids, doiTuongQuanLyIds, dateFrom, dateTo, includeWithoutOutput);
    }

    private List<SanLuongDoiTuongRowResponse> buildDoiTuongRowsForIds(
            Set<UUID> doiTuongIds,
            List<UUID> doiTuongQuanLyIds,
            LocalDate dateFrom,
            LocalDate dateTo,
            boolean includeWithoutOutput) {
        if (doiTuongIds == null || doiTuongIds.isEmpty()) {
            return List.of();
        }

        LocalDate today = VietnamDateUtils.today();

        List<HopDongDoiTuongResponse> doiTuongSource = hopDongDoiTuongService.getByIdsLite(doiTuongIds);
        if (doiTuongSource.isEmpty()) {
            return List.of();
        }

        // Mã/tỉnh/khu vực/nhà thầu/tên hợp đồng đều đọc từ đây — cache mô tả dùng chung
        // với module volume, thay vì tự resolve/tính lại (xem HopDongDoiTuongService).
        Map<UUID, HopDongDoiTuongSnapshot> descriptorsById = hopDongDoiTuongSnapshotService.getSnapshots(doiTuongIds);

        Map<UUID, List<SanLuong>> sanLuongGrouped = sanLuongRepository
                .findActiveByHopDongDoiTuongIdInAndDateRange(
                        doiTuongIds,
                        dateFrom != null,
                        dateOrUnused(dateFrom),
                        dateTo != null,
                        dateOrUnused(dateTo))
                .stream()
                .filter(item -> item.getHopDongDoiTuongId() != null)
                .collect(Collectors.groupingBy(SanLuong::getHopDongDoiTuongId));

        Map<UUID, List<SanLuong>> sanLuongAllGrouped = sanLuongRepository
                .findByHopDongDoiTuongIdInAndNgayXoaIsNull(doiTuongIds)
                .stream()
                .filter(item -> Boolean.TRUE.equals(item.getHoatDong()))
                .filter(item -> item.getHopDongDoiTuongId() != null)
                .collect(Collectors.groupingBy(SanLuong::getHopDongDoiTuongId));

        Set<UUID> hopDongIds = doiTuongSource.stream()
                .map(HopDongDoiTuongResponse::getHopDongId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, List<ContractWorkItem>> workCatalogByHopDong = loadContractWorkCatalogByHopDongIds(hopDongIds);
        List<HopDongResponse> hopDongList = hopDongService.getByIds(hopDongIds);
        Map<UUID, String> contractLabelByHopDongId = hopDongList.stream()
                .collect(Collectors.toMap(
                        HopDongResponse::getId,
                        hd -> firstNonBlank(hd.getTen(), hd.getMaHopDong(), hd.getId().toString()),
                        (a, b) -> a));
        Map<UUID, String> contractMaByHopDongId = hopDongList.stream()
                .filter(hd -> hd.getMaHopDong() != null && !hd.getMaHopDong().isBlank())
                .collect(Collectors.toMap(HopDongResponse::getId, HopDongResponse::getMaHopDong, (a, b) -> a));

        List<SanLuongDoiTuongRowResponse> rows = new ArrayList<>();
        Set<UUID> doiTuongQuanLyFilter = doiTuongQuanLyIds == null || doiTuongQuanLyIds.isEmpty()
                ? Set.of()
                : new HashSet<>(doiTuongQuanLyIds);
        for (HopDongDoiTuongResponse doiTuong : doiTuongSource) {
            if (!doiTuongQuanLyFilter.isEmpty()
                    && !doiTuongQuanLyFilter.contains(doiTuong.getDoiTuongQuanLyId())) {
                continue;
            }
            if (doiTuong.getHopDongId() == null) {
                continue;
            }
            HopDongDoiTuongSnapshot descriptor = descriptorsById.get(doiTuong.getId());
            if (descriptor == null && !includeWithoutOutput) {
                continue;
            }

            List<SanLuong> periodRecords = sanLuongGrouped.getOrDefault(doiTuong.getId(), List.of());
            List<SanLuong> allRecords = sanLuongAllGrouped.getOrDefault(doiTuong.getId(), List.of());
            if (periodRecords.isEmpty() && allRecords.isEmpty() && !includeWithoutOutput) {
                continue;
            }

            List<ContractWorkItem> contractWorkItems = workCatalogByHopDong.getOrDefault(doiTuong.getHopDongId(), List.of());
            ProgressMetrics metrics;
            if (allRecords.isEmpty()) {
                metrics = new ProgressMetrics(
                        0,
                        contractWorkItems.size(),
                        0,
                        0,
                        sumProductiveMoney(periodRecords),
                        sumMoneyByDate(periodRecords, today),
                        null);
            } else {
                Map<UUID, SanLuong> sanLuongByCatalogKey = indexLatestSanLuongByCatalogKey(allRecords, contractWorkItems);
                // SL tổng = lũy kế mọi bản ghi; SL hôm nay = chỉ ngayThucHien = today (không dùng
                // periodRecords — khi lọc dateFrom/dateTo = hôm nay sẽ khiến SL tổng = SL hôm nay).
                metrics = computeProgress(
                        contractWorkItems,
                        sanLuongByCatalogKey,
                        allRecords,
                        allRecords,
                        today);
            }

            SanLuongDoiTuongRowResponse row = new SanLuongDoiTuongRowResponse();
            row.setId(doiTuong.getId());
            row.setHopDongId(doiTuong.getHopDongId());
            row.setHopDongDoiTuongId(doiTuong.getId());
            row.setDoiTuongQuanLyId(doiTuong.getDoiTuongQuanLyId());
            row.setObjectCode(descriptor != null && StringUtils.hasText(descriptor.getMaDoiTuong())
                    ? descriptor.getMaDoiTuong()
                    : firstNonBlank(doiTuong.getDoiTuongMa(), doiTuong.getId().toString()));
            row.setContract(descriptor != null && StringUtils.hasText(descriptor.getTenHopDong())
                    ? descriptor.getTenHopDong()
                    : contractLabelByHopDongId.getOrDefault(doiTuong.getHopDongId(), "—"));
            row.setContractCode(contractMaByHopDongId.get(doiTuong.getHopDongId()));
            row.setContractor(descriptor != null && StringUtils.hasText(descriptor.getNhaThau())
                    ? descriptor.getNhaThau()
                    : firstNonBlank(doiTuong.getNhaThauTen(), "—"));
            row.setContractorId(doiTuong.getNhaThauId());
            row.setProvinceCode(descriptor != null && StringUtils.hasText(descriptor.getTinh())
                    ? descriptor.getTinh()
                    : "—");
            row.setProvinceCodeOld(descriptor != null && StringUtils.hasText(descriptor.getOldProvince())
                    ? descriptor.getOldProvince()
                    : null);
            row.setRegion(descriptor != null && StringUtils.hasText(descriptor.getKhuVuc())
                    ? descriptor.getKhuVuc()
                    : "—");
            row.setRouteLengthKm(null);
            row.setStartStation(null);
            row.setEndStation(null);
            row.setSurveyDate(earliestDate(allRecords, "survey"));
            row.setConstructionDate(latestDate(allRecords, "done"));
            row.setTotalOutput(metrics.totalOutput());
            row.setTodayOutput(metrics.todayOutput());
            row.setItemsDone(metrics.itemsDone());
            row.setItemsTotal(metrics.itemsTotal());
            // KHÔNG dùng metrics.issueCount() (đếm SanLuong.trangThai='issue' — giá trị không
            // ai từng set) — issueCount thật lấy từ coVuongMacMo denormalize, đồng bộ từ module
            // Vướng mắc (xem HopDongDoiTuongVuongMacConsumer), khớp với card tổng quan.
            int issueCount = Boolean.TRUE.equals(doiTuong.getCoVuongMacMo()) ? 1 : 0;
            // Tuyến (có bản ghi khảo sát/thiết kế) tính %% theo km thay vì đếm hạng mục done
            Integer tuyenPercent = computeTuyenKmPercent(doiTuong, allRecords);
            int completionPercent = tuyenPercent != null ? tuyenPercent : metrics.completionPercent();
            if (isDoiTuongOutputLocked(doiTuong)) {
                completionPercent = 100;
            }
            row.setCompletionPercent(completionPercent);
            row.setIssueCount(issueCount);
            row.setLastUpdated(metrics.lastUpdated());
            row.setRowStatus(resolveRowStatus(completionPercent, issueCount, metrics.lastUpdated()));
            row.setOutputLocked(isDoiTuongOutputLocked(doiTuong));
            row.setCoVuongMacMo(Boolean.TRUE.equals(doiTuong.getCoVuongMacMo()));
            rows.add(row);
        }

        // Thứ tự trả về do DB quyết định (ORDER BY ngay_cap_nhat_san_luong DESC) —
        // Java sort theo lastUpdated đã bị remove vì listDoiTuong() ngay sau đó
        // reorder theo pageIds từ DB, làm cho Java sort vô nghĩa.
        return rows;
    }

    private List<SanLuongHangMucItemResponse> toWorkItemsFromCatalogSlice(
            List<ContractWorkItem> catalogSlice,
            List<SanLuong> activeRecords,
            List<SanLuong> allRecordsForAudit,
            int startOrder) {
        Map<UUID, BigDecimal> sumByCatalogKey = sumAmountByCatalogKey(activeRecords);
        Map<UUID, BigDecimal> todayByCatalogKey = sumAmountByCatalogKeyForDate(activeRecords, VietnamDateUtils.today());
        Map<UUID, SanLuong> latestByCatalogKey = indexLatestSanLuongByCatalogKey(activeRecords, catalogSlice);

        List<SanLuongHangMucItemResponse> items = new ArrayList<>();
        int order = startOrder;
        for (ContractWorkItem workItem : catalogSlice) {
            UUID catalogKey = workItem.catalogKey();
            SanLuong latest = latestByCatalogKey.get(catalogKey);
            SanLuongHangMucItemResponse item = new SanLuongHangMucItemResponse();
            item.setOrder(order++);
            item.setHangMucCongViecId(workItem.hangMucCongViecId());
            item.setHangMucChiTietId(workItem.hangMucChiTietId());
            item.setName(workItem.name());
            item.setCode(workItem.code());
            item.setAmount(sumByCatalogKey.getOrDefault(catalogKey, BigDecimal.ZERO));
            item.setTodayAmount(todayByCatalogKey.getOrDefault(catalogKey, BigDecimal.ZERO));
            if (latest != null) {
                item.setId(latest.getId().toString());
                item.setDate(latest.getNgayThucHien());
                item.setUnitPrice(latest.getDonGia() != null ? latest.getDonGia() : workItem.unitPrice());
                item.setStatus(firstNonBlank(latest.getTrangThai(), STATUS_PENDING));
                item.setKetQuaNghiemThu(latest.getKetQuaNghiemThu());
                item.setLyDoKhongDat(latest.getLyDoKhongDat());
                item.setNguoiNghiemThuId(latest.getNguoiNghiemThuId());
                item.setNguoiNghiemThuTen(latest.getNguoiNghiemThuTen());
                item.setNgayNghiemThu(latest.getNgayNghiemThu());
            } else {
                item.setUnitPrice(workItem.unitPrice() != null ? workItem.unitPrice() : BigDecimal.ZERO);
                item.setStatus(STATUS_PENDING);
                item.setLyDoKhongDatGanNhat(resolveLyDoKhongDatGanNhat(allRecordsForAudit, catalogKey));
            }
            items.add(item);
        }
        return items;
    }

    private SanLuongDoiTuongRowResponse requireDoiTuongRow(
            List<SanLuongDoiTuongRowResponse> rows,
            UUID hopDongDoiTuongId,
            String message) {
        return rows.stream()
                .filter(row -> hopDongDoiTuongId.equals(row.getHopDongDoiTuongId()))
                .findFirst()
                .orElseThrow(() -> new AppException(SanLuongErrorCode.SAN_LUONG_NOT_FOUND, message));
    }

    private List<SanLuongHangMucItemResponse> toWorkItemsFromRecords(
            List<ContractWorkItem> contractWorkItems,
            List<SanLuong> activeRecords,
            List<SanLuong> allRecordsForAudit) {
        Map<UUID, BigDecimal> sumByCatalogKey = sumAmountByCatalogKey(activeRecords);
        Map<UUID, BigDecimal> todayByCatalogKey = sumAmountByCatalogKeyForDate(activeRecords, VietnamDateUtils.today());
        Map<UUID, SanLuong> latestByCatalogKey = indexLatestSanLuongByCatalogKey(activeRecords, contractWorkItems);

        List<SanLuongHangMucItemResponse> items = new ArrayList<>();
        int order = 1;
        for (ContractWorkItem workItem : contractWorkItems) {
            UUID catalogKey = workItem.catalogKey();
            SanLuong latest = latestByCatalogKey.get(catalogKey);
            SanLuongHangMucItemResponse item = new SanLuongHangMucItemResponse();
            item.setOrder(order++);
            item.setHangMucCongViecId(workItem.hangMucCongViecId());
            item.setHangMucChiTietId(workItem.hangMucChiTietId());
            item.setName(workItem.name());
            item.setCode(workItem.code());
            item.setAmount(sumByCatalogKey.getOrDefault(catalogKey, BigDecimal.ZERO));
            item.setTodayAmount(todayByCatalogKey.getOrDefault(catalogKey, BigDecimal.ZERO));
            if (latest != null) {
                item.setId(latest.getId().toString());
                item.setDate(latest.getNgayThucHien());
                item.setUnitPrice(latest.getDonGia() != null ? latest.getDonGia() : workItem.unitPrice());
                item.setStatus(firstNonBlank(latest.getTrangThai(), STATUS_PENDING));
                item.setKetQuaNghiemThu(latest.getKetQuaNghiemThu());
                item.setLyDoKhongDat(latest.getLyDoKhongDat());
                item.setNguoiNghiemThuId(latest.getNguoiNghiemThuId());
                item.setNguoiNghiemThuTen(latest.getNguoiNghiemThuTen());
                item.setNgayNghiemThu(latest.getNgayNghiemThu());
            } else {
                item.setUnitPrice(workItem.unitPrice() != null ? workItem.unitPrice() : BigDecimal.ZERO);
                item.setStatus(STATUS_PENDING);
                item.setLyDoKhongDatGanNhat(resolveLyDoKhongDatGanNhat(allRecordsForAudit, catalogKey));
            }
            items.add(item);
        }
        return items;
    }

    private ProgressMetrics computeProgress(
            List<ContractWorkItem> contractWorkItems,
            Map<UUID, SanLuong> sanLuongByCatalogKey,
            List<SanLuong> progressRecords,
            List<SanLuong> moneyRecords,
            LocalDate today) {
        boolean useInScope = !sanLuongByCatalogKey.isEmpty();
        int itemsTotal = useInScope ? sanLuongByCatalogKey.size() : contractWorkItems.size();
        int itemsDone = 0;
        int issueCount = 0;
        if (useInScope) {
            for (SanLuong record : sanLuongByCatalogKey.values()) {
                if ("done".equalsIgnoreCase(record.getTrangThai())) {
                    itemsDone++;
                }
                if ("issue".equalsIgnoreCase(record.getTrangThai())) {
                    issueCount++;
                }
            }
        } else {
            for (ContractWorkItem workItem : contractWorkItems) {
                SanLuong record = sanLuongByCatalogKey.get(workItem.catalogKey());
                if (record == null) {
                    continue;
                }
                if ("done".equalsIgnoreCase(record.getTrangThai())) {
                    itemsDone++;
                }
                if ("issue".equalsIgnoreCase(record.getTrangThai())) {
                    issueCount++;
                }
            }
        }
        int completionPercent = itemsTotal == 0 ? 0 : Math.round((itemsDone * 100f) / itemsTotal);
        Instant lastUpdated = progressRecords.stream()
                .map(SanLuong::getNgayThucHien)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .map(date -> date.atStartOfDay(VietnamDateUtils.ZONE).toInstant())
                .orElse(null);
        return new ProgressMetrics(
                itemsDone,
                itemsTotal,
                completionPercent,
                issueCount,
                sumProductiveMoney(moneyRecords),
                sumMoneyByDate(moneyRecords, today),
                lastUpdated);
    }

    private List<HopDongDoiTuongResponse> loadActiveDoiTuongResponses(UUID hopDongId) {
        List<UUID> ids = hopDongDoiTuongService.findActiveIdsWithActiveHopDong(hopDongId);
        if (ids.isEmpty()) {
            return List.of();
        }
        List<HopDongDoiTuongResponse> responses = new ArrayList<>();
        for (int index = 0; index < ids.size(); index += DOI_TUONG_BATCH_SIZE) {
            List<UUID> batch = ids.subList(index, Math.min(index + DOI_TUONG_BATCH_SIZE, ids.size()));
            responses.addAll(hopDongDoiTuongService.getByIds(batch));
        }
        return responses;
    }

    private Map<UUID, List<ContractWorkItem>> loadContractWorkCatalogByHopDongIds(Set<UUID> hopDongIds) {
        if (hopDongIds == null || hopDongIds.isEmpty()) {
            return Map.of();
        }
        List<UUID> hopDongIdList = hopDongIds.stream().filter(Objects::nonNull).distinct().toList();
        if (hopDongIdList.isEmpty()) {
            return Map.of();
        }

        List<HangMucNhom> allGroups = hangMucNhomService.findActiveEntitiesByHopDongIds(hopDongIdList).stream()
                .filter(group -> Boolean.TRUE.equals(group.getHoatDong()))
                .toList();
        if (allGroups.isEmpty()) {
            return hopDongIdList.stream().collect(Collectors.toMap(Function.identity(), id -> List.of()));
        }

        Map<UUID, List<HangMucNhom>> groupsByHopDong = allGroups.stream()
                .collect(Collectors.groupingBy(
                        HangMucNhom::getHopDongId,
                        Collectors.collectingAndThen(Collectors.toList(), groups -> groups.stream()
                                .sorted(Comparator.comparing(
                                        HangMucNhom::getThuTu,
                                        Comparator.nullsLast(Comparator.naturalOrder())))
                                .toList())));

        List<UUID> allGroupIds = allGroups.stream().map(HangMucNhom::getId).toList();
        List<HangMucChiTiet> allChiTiets = hangMucChiTietService
                .findActiveEntitiesByNhomIds(allGroupIds)
                .stream()
                .filter(item -> Boolean.TRUE.equals(item.getHoatDong()))
                .toList();

        Map<UUID, List<HangMucChiTiet>> chiTietsByGroup = allChiTiets.stream()
                .collect(Collectors.groupingBy(
                        HangMucChiTiet::getHangMucNhomId,
                        Collectors.collectingAndThen(Collectors.toList(), items -> items.stream()
                                .sorted(Comparator.comparing(
                                        HangMucChiTiet::getMa,
                                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                                .toList())));

        List<UUID> allChiTietIds = allChiTiets.stream().map(HangMucChiTiet::getId).toList();
        Map<UUID, List<HangMucCongViec>> congViecByChiTiet = allChiTietIds.isEmpty()
                ? Map.of()
                : hangMucCongViecService.findActiveEntitiesByChiTietIds(allChiTietIds)
                .stream()
                .sorted(Comparator.comparing(
                        HangMucCongViec::getThuTu,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.groupingBy(HangMucCongViec::getHangMucChiTietId));

        Map<UUID, List<ContractWorkItem>> result = new HashMap<>();
        for (UUID hopDongId : hopDongIdList) {
            List<HangMucNhom> groups = groupsByHopDong.getOrDefault(hopDongId, List.of());
            List<HangMucChiTiet> chiTiets = groups.stream()
                    .flatMap(group -> chiTietsByGroup.getOrDefault(group.getId(), List.of()).stream())
                    .toList();
            result.put(hopDongId, buildContractWorkItemsFromData(chiTiets, congViecByChiTiet));
        }
        return result;
    }

    private List<ContractWorkItem> buildContractWorkItemsFromData(
            List<HangMucChiTiet> chiTiets,
            Map<UUID, List<HangMucCongViec>> congViecByChiTiet) {
        if (chiTiets.isEmpty()) {
            return List.of();
        }
        List<ContractWorkItem> items = new ArrayList<>();
        for (HangMucChiTiet chiTiet : chiTiets) {
            List<HangMucCongViec> tasks = congViecByChiTiet.getOrDefault(chiTiet.getId(), List.of());
            if (tasks.isEmpty()) {
                items.add(new ContractWorkItem(
                        chiTiet.getId(),
                        null,
                        chiTiet.getId(),
                        resolveHangMucDisplayName(chiTiet.getTen(), chiTiet.getMa()),
                        normalizeHangMucCode(chiTiet.getMa()),
                        chiTiet.getDonGia(),
                        true));
                continue;
            }
            for (HangMucCongViec task : tasks) {
                items.add(new ContractWorkItem(
                        task.getId(),
                        task.getId(),
                        task.getHangMucChiTietId(),
                        resolveHangMucDisplayName(task.getTen(), task.getMa()),
                        normalizeHangMucCode(task.getMa()),
                        task.getDonGia(),
                        false));
            }
        }
        return items;
    }

    private ContractCatalog buildContractWorkItems(List<HangMucNhom> groups) {
        if (groups.isEmpty()) {
            return new ContractCatalog(List.of(), Set.of(), Set.of());
        }

        List<UUID> groupIds = groups.stream().map(HangMucNhom::getId).toList();
        List<HangMucChiTiet> chiTiets = hangMucChiTietService.findActiveEntitiesByNhomIds(groupIds).stream()
                .filter(item -> Boolean.TRUE.equals(item.getHoatDong()))
                .sorted(Comparator.comparing(HangMucChiTiet::getMa, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
        if (chiTiets.isEmpty()) {
            return new ContractCatalog(List.of(), Set.of(), Set.of());
        }

        List<UUID> chiTietIds = chiTiets.stream().map(HangMucChiTiet::getId).toList();
        Map<UUID, List<HangMucCongViec>> congViecByChiTiet = hangMucCongViecService
                .findActiveEntitiesByChiTietIds(chiTietIds)
                .stream()
                .sorted(Comparator.comparing(HangMucCongViec::getThuTu, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.groupingBy(HangMucCongViec::getHangMucChiTietId));

        return toContractCatalog(chiTiets, congViecByChiTiet);
    }

    private ContractCatalog toContractCatalog(
            List<HangMucChiTiet> chiTiets,
            Map<UUID, List<HangMucCongViec>> congViecByChiTiet) {
        List<ContractWorkItem> items = new ArrayList<>();
        Set<UUID> congViecIds = new HashSet<>();
        Set<UUID> chiTietLeafIds = new HashSet<>();
        for (HangMucChiTiet chiTiet : chiTiets) {
            List<HangMucCongViec> tasks = congViecByChiTiet.getOrDefault(chiTiet.getId(), List.of());
            if (tasks.isEmpty()) {
                chiTietLeafIds.add(chiTiet.getId());
                items.add(new ContractWorkItem(
                        chiTiet.getId(),
                        null,
                        chiTiet.getId(),
                        resolveHangMucDisplayName(chiTiet.getTen(), chiTiet.getMa()),
                        normalizeHangMucCode(chiTiet.getMa()),
                        chiTiet.getDonGia(),
                        true));
                continue;
            }
            for (HangMucCongViec task : tasks) {
                congViecIds.add(task.getId());
                items.add(new ContractWorkItem(
                        task.getId(),
                        task.getId(),
                        task.getHangMucChiTietId(),
                        resolveHangMucDisplayName(task.getTen(), task.getMa()),
                        normalizeHangMucCode(task.getMa()),
                        task.getDonGia(),
                        false));
            }
        }
        return new ContractCatalog(items, congViecIds, chiTietLeafIds);
    }

    private ContractCatalog loadContractWorkCatalog(UUID hopDongId) {
        if (hopDongId == null) {
            return new ContractCatalog(List.of(), Set.of(), Set.of());
        }

        List<HangMucNhom> groups = hangMucNhomService.findActiveEntitiesByHopDongId(hopDongId).stream()
                .filter(group -> Boolean.TRUE.equals(group.getHoatDong()))
                .sorted(Comparator.comparing(HangMucNhom::getThuTu, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        return buildContractWorkItems(groups);
    }

    private void validateCatalogEntry(SanLuongHangMucTaoItem entry, ContractCatalog catalog) {
        UUID congViecId = entry.getHangMucCongViecId();
        UUID chiTietId = entry.getHangMucChiTietId();
        if (congViecId != null && catalog.congViecIds().contains(congViecId)) {
            return;
        }
        if (chiTietId != null && catalog.chiTietLeafIds().contains(chiTietId)) {
            return;
        }
        // FE đôi khi gửi nhầm id hạng mục lá vào hangMucCongViecId
        if (congViecId != null && catalog.chiTietLeafIds().contains(congViecId)) {
            entry.setHangMucChiTietId(congViecId);
            entry.setHangMucCongViecId(null);
            return;
        }
        throw new AppException(SanLuongErrorCode.SAN_LUONG_NOT_FOUND, "Hạng mục không thuộc hợp đồng");
    }

    private record ResolvedHangMucUpload(UUID congViecId, UUID chiTietId) {}

    /**
     * Upload ảnh chỉ gắn 1 khóa danh mục — ưu tiên công việc, không lưu kèm chi tiết cha.
     * Cho phép cả hai null (ảnh chung).
     */
    private ResolvedHangMucUpload resolveUploadHangMucIds(
            UUID hopDongId, UUID hangMucCongViecId, UUID hangMucChiTietId) {
        if (hangMucCongViecId == null && hangMucChiTietId == null) {
            return new ResolvedHangMucUpload(null, null);
        }
        ContractCatalog catalog = loadContractWorkCatalog(hopDongId);
        if (hangMucCongViecId != null && catalog.congViecIds().contains(hangMucCongViecId)) {
            return new ResolvedHangMucUpload(hangMucCongViecId, null);
        }
        if (hangMucChiTietId != null && catalog.chiTietLeafIds().contains(hangMucChiTietId)) {
            return new ResolvedHangMucUpload(null, hangMucChiTietId);
        }
        if (hangMucCongViecId != null && catalog.chiTietLeafIds().contains(hangMucCongViecId)) {
            return new ResolvedHangMucUpload(null, hangMucCongViecId);
        }
        if (hangMucChiTietId != null && catalog.congViecIds().contains(hangMucChiTietId)) {
            return new ResolvedHangMucUpload(hangMucChiTietId, null);
        }
        throw new AppException(SanLuongErrorCode.SAN_LUONG_NOT_FOUND, "Hạng mục không thuộc hợp đồng");
    }

    private UUID requireAllowedCongViec(UUID hangMucCongViecId, Set<UUID> allowedCongViecIds) {
        if (hangMucCongViecId == null) {
            throw new AppException(SanLuongErrorCode.SAN_LUONG_NOT_FOUND, "Thiếu hạng mục công việc của hợp đồng");
        }
        if (!allowedCongViecIds.contains(hangMucCongViecId)) {
            throw new AppException(SanLuongErrorCode.SAN_LUONG_NOT_FOUND, "Hạng mục công việc không thuộc hợp đồng");
        }
        return hangMucCongViecId;
    }

    private SanLuong resolveExistingRecord(
            SanLuongCapNhatHangMucItem item,
            Map<UUID, SanLuong> byId,
            Map<UUID, SanLuong> byCatalogKey) {
        if (item.getId() != null && byId.containsKey(item.getId())) {
            return byId.get(item.getId());
        }
        if (item.getHangMucCongViecId() != null) {
            return byCatalogKey.get(item.getHangMucCongViecId());
        }
        if (item.getHangMucChiTietId() != null) {
            return byCatalogKey.get(item.getHangMucChiTietId());
        }
        return null;
    }

    private Map<UUID, SanLuong> indexLatestSanLuongByCatalogKey(
            List<SanLuong> records,
            List<ContractWorkItem> catalog) {
        Set<UUID> catalogKeys = catalog.stream().map(ContractWorkItem::catalogKey).collect(Collectors.toSet());
        Map<UUID, SanLuong> direct = new HashMap<>();
        for (SanLuong record : records) {
            UUID key = record.getHangMucCongViecId() != null
                    ? record.getHangMucCongViecId()
                    : record.getHangMucChiTietId();
            if (key == null) {
                continue;
            }
            SanLuong current = direct.get(key);
            if (current == null || isNewer(record, current)) {
                direct.put(key, record);
            }
        }

        Map<UUID, SanLuong> result = new LinkedHashMap<>();
        for (ContractWorkItem workItem : catalog) {
            UUID catalogKey = workItem.catalogKey();
            SanLuong record = direct.get(catalogKey);
            if (record == null && workItem.hangMucCongViecId() != null) {
                record = direct.get(workItem.hangMucChiTietId());
            }
            if (record == null || !catalogKeys.contains(catalogKey)) {
                continue;
            }
            SanLuong current = result.get(catalogKey);
            if (current == null || isNewer(record, current)) {
                result.put(catalogKey, record);
            }
        }
        return result;
    }

    private boolean isNewer(SanLuong left, SanLuong right) {
        Instant leftUpdated = left.getNgayCapNhat();
        Instant rightUpdated = right.getNgayCapNhat();
        if (leftUpdated == null) {
            return false;
        }
        if (rightUpdated == null) {
            return true;
        }
        return leftUpdated.isAfter(rightUpdated);
    }

    private HopDong requireHopDong(UUID hopDongId) {
        if (hopDongId == null) {
            throw new AppException(SanLuongErrorCode.SAN_LUONG_NOT_FOUND, "Không tìm thấy hợp đồng");
        }
        HopDong hopDong = hopDongService.findActiveEntityById(hopDongId)
                .orElseThrow(() -> new AppException(SanLuongErrorCode.SAN_LUONG_NOT_FOUND, "Không tìm thấy hợp đồng"));
        if (!Boolean.TRUE.equals(hopDong.getHoatDong())) {
            throw new AppException(
                    SanLuongErrorCode.SAN_LUONG_HOP_DONG_CANCELLED,
                    "Hợp đồng đã bị hủy, không thể cập nhật sản lượng");
        }
        return hopDong;
    }

    private void assertDoiTuongActiveForOutput(UUID hopDongDoiTuongId) {
        HopDongDoiTuongResponse doiTuong = hopDongDoiTuongService.getById(hopDongDoiTuongId);
        if (isDoiTuongOutputLocked(doiTuong)) {
            if (!Boolean.TRUE.equals(doiTuong.getHoatDong())
                    || HopDongDanhSachTienDoHelper.isHuyStatus(
                            doiTuong.getTrangThaiMa(), doiTuong.getTrangThaiTen())) {
                throw new AppException(
                        SanLuongErrorCode.SAN_LUONG_HOP_DONG_CANCELLED,
                        "Đối tượng đã bị hủy, không thể cập nhật sản lượng");
            }
            throw new AppException(
                    SanLuongErrorCode.SAN_LUONG_DOI_TUONG_COMPLETED,
                    "Đối tượng đã hoàn thành, không thể bổ sung sản lượng");
        }
        if (Boolean.TRUE.equals(doiTuong.getCoVuongMacMo())) {
            throw new AppException(
                    SanLuongErrorCode.SAN_LUONG_DOI_TUONG_CO_VUONG_MAC,
                    "Đối tượng đang có vướng mắc chưa xử lý/đang xử lý, không thể cập nhật sản lượng");
        }
        requireHopDong(doiTuong.getHopDongId());
    }

    private boolean isDoiTuongOutputLocked(HopDongDoiTuongResponse doiTuong) {
        if (doiTuong == null) {
            return true;
        }
        if (!Boolean.TRUE.equals(doiTuong.getHoatDong())) {
            return true;
        }
        if (doiTuong.getNgayHtTc() != null) {
            return true;
        }
        if (HopDongDanhSachTienDoHelper.isHuyStatus(doiTuong.getTrangThaiMa(), doiTuong.getTrangThaiTen())) {
            return true;
        }
        return HopDongDanhSachTienDoHelper.isCompletedStatus(doiTuong.getTrangThaiMa());
    }

    private Set<UUID> resolveContractorScopeIds() {
        Optional<ContractorScope> scope = contractorScopeService.currentScope();
        if (scope.isEmpty()) {
            return null;
        }
        Set<UUID> ids = scope.get().hopDongDoiTuongIds();
        return ids == null ? Set.of() : ids;
    }

    /**
     * %% hoàn thành theo KM cho đối tượng tuyến (luồng TVTK): đã sang thiết kế → km thiết kế
     * / km khảo sát; mới khảo sát → km khảo sát / chiều dài tuyến (import sẵn hoặc được tự
     * gán từ km khảo sát). Trả null nếu không có bản ghi khảo sát (không phải tuyến TVTK).
     */
    private Integer computeTuyenKmPercent(HopDongDoiTuongResponse doiTuong, List<SanLuong> records) {
        BigDecimal surveyKm = sumKmByTrangThai(records, "survey");
        if (surveyKm.signum() <= 0) {
            return null;
        }
        BigDecimal designKm = sumKmByTrangThai(records, "design");
        if (designKm.signum() > 0) {
            return clampPercent(designKm.multiply(BigDecimal.valueOf(100))
                    .divide(surveyKm, 0, RoundingMode.HALF_UP));
        }
        BigDecimal length = resolveRouteLengthKm(doiTuong);
        if (length == null || length.signum() <= 0) {
            return 100; // chưa có chiều dài tuyến → km khảo sát chính là chiều dài
        }
        return clampPercent(surveyKm.multiply(BigDecimal.valueOf(100))
                .divide(length, 0, RoundingMode.HALF_UP));
    }

    private BigDecimal sumKmByTrangThai(List<SanLuong> records, String trangThai) {
        return records.stream()
                .filter(record -> trangThai.equalsIgnoreCase(record.getTrangThai()))
                .map(record -> record.getKhoiLuongHoanThanh() != null
                        ? record.getKhoiLuongHoanThanh()
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal tongKhaoSatKmTheoHopDong(UUID hopDongId) {
        if (hopDongId == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = sanLuongRepository.sumSurveyKmByHopDong(hopDongId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal tongThanhTienTheoHopDong(UUID hopDongId) {
        if (hopDongId == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = sanLuongRepository.sumThanhTienByHopDong(hopDongId);
        return total != null ? total : BigDecimal.ZERO;
    }

    private int clampPercent(BigDecimal value) {
        return Math.max(0, Math.min(100, value.intValue()));
    }

    /** Ưu tiên TÊN hợp đồng (dễ nhận biết trên UI sản lượng), fallback mã rồi id. */
    private String resolveContractLabel(HopDong hopDong) {
        return firstNonBlank(hopDong.getTen(), hopDong.getMaHopDong(), hopDong.getId().toString());
    }

    private String findGiaTri(List<HopDongDoiTuongGiaTriResponse> giaTri, String... keywords) {
        if (giaTri == null) {
            return null;
        }
        for (String keyword : keywords) {
            for (HopDongDoiTuongGiaTriResponse item : giaTri) {
                String label = item.getTenThuocTinh();
                if (label != null && label.toLowerCase().contains(keyword.toLowerCase())) {
                    return item.getGiaTri();
                }
            }
        }
        return null;
    }

    private BigDecimal resolveRouteLengthKm(HopDongDoiTuongResponse doiTuong) {
        String raw = findGiaTri(
                doiTuong.getGiaTri(),
                "chiều dài tuyến",
                "chieu dai tuyen",
                "chiều dài",
                "chieu dai");
        return parseDecimal(raw);
    }

    /**
     * Tuyến chưa import sẵn KM ("Chiều dài tuyến" trống/0) thì KM khảo sát vừa nhập được coi
     * là KM của tuyến luôn (theo luồng nghiệp vụ): gán tổng km khảo sát lũy kế vào thuộc tính
     * chiều dài để các phép tính %% khảo sát/thi công có mẫu số. Best-effort — lỗi ở bước phụ
     * này không được làm hỏng việc lưu sản lượng.
     */
    private void maybeGanChieuDaiTuyenTuKhaoSat(
            HopDongDoiTuongResponse doiTuong, List<SanLuongHangMucTaoItem> entries) {
        try {
            BigDecimal surveyKmBatch = entries.stream()
                    .filter(entry -> "survey".equalsIgnoreCase(entry.getTrangThai()))
                    .map(SanLuongHangMucTaoItem::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (surveyKmBatch.signum() <= 0) {
                return;
            }
            BigDecimal existing = resolveRouteLengthKm(doiTuong);
            if (existing != null && existing.signum() > 0) {
                return; // đã có KM import sẵn — chỉ so %%, không ghi đè
            }

            // Chiều dài = tổng km khảo sát lũy kế (gồm cả lô vừa lưu ở transaction này)
            BigDecimal totalSurveyKm = sanLuongRepository
                    .findByHopDongDoiTuongIdAndNgayXoaIsNull(doiTuong.getId()).stream()
                    .filter(record -> "survey".equalsIgnoreCase(record.getTrangThai()))
                    .map(record -> record.getKhoiLuongHoanThanh() != null
                            ? record.getKhoiLuongHoanThanh()
                            : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal length = totalSurveyKm.signum() > 0 ? totalSurveyKm : surveyKmBatch;
            String lengthText = length.stripTrailingZeros().toPlainString();

            HopDongDoiTuongGiaTriResponse existingRow = findChieuDaiTuyenRow(doiTuong.getGiaTri());
            if (existingRow != null) {
                HopDongDoiTuongGiaTriCapNhatRequest update = new HopDongDoiTuongGiaTriCapNhatRequest();
                update.setGiaTri(lengthText);
                hopDongDoiTuongGiaTriService.update(existingRow.getId(), update);
                return;
            }
            ThuocTinhResponse chieuDaiAttr = thuocTinhService
                    .list(null, true, false, doiTuong.getDoiTuongQuanLyId(), null).stream()
                    .filter(attr -> attr.getTen() != null && isChieuDaiLabel(attr.getTen()))
                    .findFirst()
                    .orElse(null);
            if (chieuDaiAttr == null) {
                return; // đối tượng không có thuộc tính chiều dài — bỏ qua
            }
            HopDongDoiTuongGiaTriTaoRequest create = new HopDongDoiTuongGiaTriTaoRequest();
            create.setHopDongDoiTuongId(doiTuong.getId());
            create.setThuocTinhId(chieuDaiAttr.getId());
            create.setGiaTri(lengthText);
            hopDongDoiTuongGiaTriService.create(create);
        } catch (RuntimeException ignored) {
            // best-effort: không chặn lưu sản lượng vì lỗi gán chiều dài
        }
    }

    private HopDongDoiTuongGiaTriResponse findChieuDaiTuyenRow(List<HopDongDoiTuongGiaTriResponse> giaTri) {
        if (giaTri == null) {
            return null;
        }
        return giaTri.stream()
                .filter(item -> item.getTenThuocTinh() != null && isChieuDaiLabel(item.getTenThuocTinh()))
                .findFirst()
                .orElse(null);
    }

    private boolean isChieuDaiLabel(String label) {
        String lower = label.toLowerCase(Locale.ROOT);
        return lower.contains("chiều dài") || lower.contains("chieu dai");
    }

    private BigDecimal parseDecimal(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().replace(",", ".");
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void assertRouteOutputVolumesAfterPlannedAdds(
            ContractCatalog catalog,
            List<SanLuong> existingRecords,
            List<SanLuongHangMucTaoItem> additions) {
        Map<UUID, BigDecimal> sums = new HashMap<>(sumAmountByCatalogKey(existingRecords));
        for (SanLuongHangMucTaoItem entry : additions) {
            UUID key = resolveCatalogKeyFromEntry(entry);
            if (key == null || entry.getAmount() == null) {
                continue;
            }
            sums.merge(key, entry.getAmount(), BigDecimal::add);
        }
        RouteOutputVolumeValidator.assertValid(catalog.items(), sums);
    }

    private void assertRouteOutputVolumesFromPersisted(ContractCatalog catalog, UUID hopDongDoiTuongId) {
        List<SanLuong> records = sanLuongRepository.findByHopDongDoiTuongIdAndNgayXoaIsNull(hopDongDoiTuongId);
        RouteOutputVolumeValidator.assertValid(catalog.items(), sumAmountByCatalogKey(filterActiveSanLuong(records)));
    }

    private UUID resolveCatalogKeyFromEntry(SanLuongHangMucTaoItem entry) {
        if (entry.getHangMucCongViecId() != null) {
            return entry.getHangMucCongViecId();
        }
        return entry.getHangMucChiTietId();
    }

    private Map<UUID, BigDecimal> sumAmountByCatalogKey(List<SanLuong> records) {
        Map<UUID, BigDecimal> result = new HashMap<>();
        for (SanLuong record : records) {
            if (!Boolean.TRUE.equals(record.getHoatDong())) {
                continue;
            }
            UUID key = record.getHangMucCongViecId() != null
                    ? record.getHangMucCongViecId()
                    : record.getHangMucChiTietId();
            if (key == null) {
                continue;
            }
            BigDecimal amount = record.getKhoiLuongHoanThanh() != null
                    ? record.getKhoiLuongHoanThanh()
                    : BigDecimal.ZERO;
            result.merge(key, amount, BigDecimal::add);
        }
        return result;
    }

    private Map<UUID, BigDecimal> sumAmountByCatalogKeyForDate(List<SanLuong> records, LocalDate date) {
        Map<UUID, BigDecimal> result = new HashMap<>();
        for (SanLuong record : records) {
            if (!Boolean.TRUE.equals(record.getHoatDong())) {
                continue;
            }
            if (!isBillableStatus(record.getTrangThai()) || !contributesOnDate(record, date)) {
                continue;
            }
            UUID key = record.getHangMucCongViecId() != null
                    ? record.getHangMucCongViecId()
                    : record.getHangMucChiTietId();
            if (key == null) {
                continue;
            }
            result.merge(key, recordMoney(record), BigDecimal::add);
        }
        return result;
    }

    private LocalDate resolveRecordDate(SanLuong record) {
        if (record.getNgayThucHien() != null) {
            return record.getNgayThucHien();
        }
        LocalDate fromUpdate = VietnamDateUtils.toLocalDate(record.getNgayCapNhat());
        if (fromUpdate != null) {
            return fromUpdate;
        }
        return VietnamDateUtils.toLocalDate(record.getNgayTao());
    }

    private String resolveBatchKey(SanLuong record) {
        if (record.getLanCapNhatId() != null) {
            return "batch:" + record.getLanCapNhatId();
        }
        Instant created = record.getNgayTao();
        if (created != null) {
            return "ts:" + created.truncatedTo(ChronoUnit.SECONDS);
        }
        return "id:" + record.getId();
    }

    private SanLuongNhatKyEntryResponse toNhatKyEntry(
            List<SanLuong> records,
            Map<UUID, ContractWorkItem> workItemByKey) {
        SanLuongNhatKyEntryResponse entry = new SanLuongNhatKyEntryResponse();
        if (records.isEmpty()) {
            return entry;
        }

        SanLuong anchor = records.get(0);
        UUID batchId = records.stream()
                .map(SanLuong::getLanCapNhatId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(anchor.getId());
        entry.setId(batchId.toString());

        Instant updatedAt = records.stream()
                .map(record -> record.getNgayTao() != null ? record.getNgayTao() : record.getNgayCapNhat())
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(null);
        entry.setUpdatedAt(updatedAt);

        LocalDate executionDate = records.stream()
                .map(SanLuong::getNgayThucHien)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(resolveRecordDate(anchor));
        entry.setDate(executionDate);

        Map<String, BigDecimal> volumes = new LinkedHashMap<>();
        boolean hasIssue = false;
        List<String> notes = new ArrayList<>();

        for (SanLuong record : records) {
            UUID key = record.getHangMucCongViecId() != null
                    ? record.getHangMucCongViecId()
                    : record.getHangMucChiTietId();
            if (key == null) {
                continue;
            }
            BigDecimal amount = record.getKhoiLuongHoanThanh() != null
                    ? record.getKhoiLuongHoanThanh()
                    : BigDecimal.ZERO;
            volumes.merge(key.toString(), amount, BigDecimal::add);
            if ("issue".equalsIgnoreCase(record.getTrangThai())) {
                hasIssue = true;
            }
            if ("khong_dat".equals(record.getKetQuaNghiemThu())) {
                hasIssue = true;
                if (record.getLyDoKhongDat() != null && !record.getLyDoKhongDat().isBlank()) {
                    ContractWorkItem workItem = workItemByKey.get(key);
                    String prefix = workItem != null ? workItem.name() + " — không đạt: " : "Không đạt: ";
                    notes.add(prefix + record.getLyDoKhongDat().trim());
                }
            }
            if (record.getGhiChu() != null && !record.getGhiChu().isBlank()) {
                ContractWorkItem workItem = workItemByKey.get(key);
                String prefix = workItem != null ? workItem.name() + ": " : "";
                notes.add(prefix + record.getGhiChu().trim());
            }
        }

        entry.setVolumes(volumes);
        entry.setHasIssue(hasIssue);
        entry.setGhiChu(notes.isEmpty() ? null : String.join(" · ", notes));
        return entry;
    }

    private String resolveHangMucDisplayName(String ten, String ma) {
        if (ten != null && !ten.isBlank()) {
            return ten.trim();
        }
        if (ma != null && !ma.isBlank()) {
            return ma.trim();
        }
        return "";
    }

    private String normalizeHangMucCode(String ma) {
        return ma == null ? "" : ma.trim();
    }

    private String resolveHangMucShortLabel(String name) {
        if (name == null || name.isBlank()) {
            return "HM";
        }
        String normalized = name.toLowerCase(Locale.ROOT)
                .replace('đ', 'd');
        if (normalized.contains("khao sat")) {
            return "KS";
        }
        if (normalized.contains("thiet ke")) {
            return "TK";
        }
        String[] words = name.trim().split("\\s+");
        if (words.length >= 2) {
            return (String.valueOf(words[0].charAt(0)) + words[1].charAt(0)).toUpperCase(Locale.ROOT);
        }
        return name.length() <= 3 ? name.toUpperCase(Locale.ROOT) : name.substring(0, 3).toUpperCase(Locale.ROOT);
    }

    private static final String NGHIEM_THU_KHONG_DAT_TAG = "[NGHIEM_THU:KHONG_DAT]";

    private List<SanLuong> filterActiveSanLuong(List<SanLuong> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        return records.stream().filter(item -> Boolean.TRUE.equals(item.getHoatDong())).toList();
    }

    private UUID catalogKeyOf(SanLuong record) {
        if (record == null) {
            return null;
        }
        return record.getHangMucCongViecId() != null
                ? record.getHangMucCongViecId()
                : record.getHangMucChiTietId();
    }

    private void invalidateCatalogSanLuongAfterKhongDat(
            SanLuong anchor,
            String lyDo,
            JwtUserPrincipal currentUser,
            Instant now) {
        UUID catalogKey = catalogKeyOf(anchor);
        UUID doiTuongId = anchor.getHopDongDoiTuongId();
        if (catalogKey == null || doiTuongId == null) {
            markSanLuongKhongDat(anchor, lyDo, currentUser, now);
            sanLuongRepository.save(anchor);
            return;
        }
        List<SanLuong> siblings = sanLuongRepository.findByHopDongDoiTuongIdAndNgayXoaIsNull(doiTuongId);
        for (SanLuong record : siblings) {
            if (!catalogKey.equals(catalogKeyOf(record))) {
                continue;
            }
            if (!Boolean.TRUE.equals(record.getHoatDong())) {
                continue;
            }
            markSanLuongKhongDat(record, lyDo, currentUser, now);
            sanLuongRepository.save(record);
        }
    }

    private void markSanLuongKhongDat(
            SanLuong record,
            String lyDo,
            JwtUserPrincipal currentUser,
            Instant now) {
        record.setKetQuaNghiemThu("khong_dat");
        record.setLyDoKhongDat(lyDo);
        record.setHoatDong(false);
        record.setNguoiNghiemThuId(currentUser != null ? currentUser.id() : null);
        record.setNguoiNghiemThuTen(currentUser != null ? currentUser.hoTen() : null);
        record.setNgayNghiemThu(now);
        record.setGhiChu(appendNghiemThuKhongDatGhiChu(record.getGhiChu(), lyDo));
    }

    private String appendNghiemThuKhongDatGhiChu(String existing, String lyDo) {
        String tagLine = NGHIEM_THU_KHONG_DAT_TAG + " " + lyDo;
        if (existing == null || existing.isBlank()) {
            return tagLine;
        }
        if (existing.contains(NGHIEM_THU_KHONG_DAT_TAG)) {
            return existing;
        }
        return existing.trim() + " · " + tagLine;
    }

    private String resolveLyDoKhongDatGanNhat(List<SanLuong> allRecords, UUID catalogKey) {
        if (allRecords == null || catalogKey == null) {
            return null;
        }
        return allRecords.stream()
                .filter(record -> catalogKey.equals(catalogKeyOf(record)))
                .filter(record -> "khong_dat".equals(record.getKetQuaNghiemThu()))
                .max(Comparator.comparing(
                        SanLuong::getNgayNghiemThu,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(SanLuong::getLyDoKhongDat)
                .filter(reason -> reason != null && !reason.isBlank())
                .orElse(null);
    }

    private void assertSanLuongNotLockedAfterNghiemThuDat(SanLuong entity) {
        if (entity != null && "dat".equals(entity.getKetQuaNghiemThu())) {
            throw new AppException(
                    SanLuongErrorCode.SAN_LUONG_NGHIEM_THU_LOCKED,
                    "Hạng mục đã nghiệm thu đạt, không thể sửa hoặc xóa sản lượng");
        }
    }

    private void assertCatalogKeyNotNghiemThuDat(
            UUID catalogKey,
            List<SanLuong> records,
            List<ContractWorkItem> catalogItems) {
        if (catalogKey == null) {
            return;
        }
        Map<UUID, SanLuong> latestByKey = indexLatestSanLuongByCatalogKey(
                filterActiveSanLuong(records), catalogItems);
        SanLuong latest = latestByKey.get(catalogKey);
        assertSanLuongNotLockedAfterNghiemThuDat(latest);
    }

    private BigDecimal recordMoney(SanLuong record) {
        BigDecimal quantity = record.getKhoiLuongHoanThanh() != null
                ? record.getKhoiLuongHoanThanh()
                : BigDecimal.ZERO;
        BigDecimal unitPrice = record.getDonGia() != null ? record.getDonGia() : BigDecimal.ZERO;
        return quantity.multiply(unitPrice);
    }

    /**
     * Tổng SL lũy kế tính tiền — tính mọi bản ghi có khối lượng × đơn giá (done/survey/design),
     * xem isBillableStatus().
     */
    private BigDecimal sumProductiveMoney(List<SanLuong> records) {
        return records.stream()
                .filter(r -> Boolean.TRUE.equals(r.getHoatDong()))
                .filter(r -> isBillableStatus(r.getTrangThai()))
                .map(this::recordMoney)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumMoneyByDate(List<SanLuong> records, LocalDate date) {
        return records.stream()
                .filter(item -> Boolean.TRUE.equals(item.getHoatDong()))
                .filter(item -> isBillableStatus(item.getTrangThai()))
                .filter(item -> contributesOnDate(item, date))
                .map(this::recordMoney)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isProductiveStatus(String status) {
        if (status == null) {
            return false;
        }
        return "done".equalsIgnoreCase(status)
                || "survey".equalsIgnoreCase(status)
                || "design".equalsIgnoreCase(status);
    }

    /**
     * Sản lượng tính tiền: done/survey/design đều tính (khối lượng × đơn giá). Với hợp đồng
     * tư vấn thiết kế (tuyến TVTK), khảo sát/thiết kế CHÍNH LÀ sản lượng — trước đây chỉ
     * tính done khiến SL tổng/SL hôm nay của tuyến luôn bằng 0 dù đã bổ sung sản lượng.
     * Bản ghi chưa có đơn giá vẫn ra 0 đồng nên không gây tính thừa.
     */
    private boolean isBillableStatus(String status) {
        return isProductiveStatus(status);
    }

    /** SL hôm nay = bản ghi có {@code ngayThucHien} đúng ngày (không dùng ngayCapNhat — tránh coi cả lũy kế là SL trong ngày). */
    private boolean contributesOnDate(SanLuong record, LocalDate date) {
        LocalDate executionDate = record.getNgayThucHien();
        return executionDate != null && date.equals(executionDate);
    }

    private List<SanLuongAnhNhomResponse> buildPhotoSections(
            List<SanLuong> records,
            List<ContractWorkItem> contractWorkItems,
            List<SanLuongAnh> photos) {
        if (photos.isEmpty()) {
            return List.of();
        }

        Map<UUID, TepDinhKemResponse> tepCache = new HashMap<>();
        Map<UUID, SanLuong> latestByCatalogKey = indexLatestSanLuongByCatalogKey(records, contractWorkItems);

        Map<String, SanLuongAnhNhomResponse> sections = new LinkedHashMap<>();
        for (ContractWorkItem workItem : contractWorkItems) {
            SanLuongAnhNhomResponse section = new SanLuongAnhNhomResponse();
            section.setId(workItem.catalogKey().toString());
            section.setTitle(workItem.name());
            SanLuong latest = latestByCatalogKey.get(workItem.catalogKey());
            if (latest != null) {
                section.setStatus(firstNonBlank(latest.getTrangThai(), STATUS_PENDING));
            }
            sections.put(workItem.catalogKey().toString(), section);
        }

        SanLuongAnhNhomResponse generalSection = new SanLuongAnhNhomResponse();
        generalSection.setId("general");
        generalSection.setTitle("Ảnh chung");

        for (SanLuongAnh photo : photos) {
            TepDinhKemResponse tep = resolveTepDinhKem(photo.getTepDinhKemId(), tepCache);
            SanLuongAnhResponse response = toAnhResponse(photo, tep);
            UUID catalogKey = photo.getHangMucCongViecId() != null
                    ? photo.getHangMucCongViecId()
                    : photo.getHangMucChiTietId();
            if (catalogKey != null && sections.containsKey(catalogKey.toString())) {
                sections.get(catalogKey.toString()).getPhotos().add(response);
            } else {
                generalSection.getPhotos().add(response);
            }
        }

        List<SanLuongAnhNhomResponse> result = sections.values().stream()
                .filter(section -> !section.getPhotos().isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
        if (!generalSection.getPhotos().isEmpty()) {
            result.add(generalSection);
        }
        return result;
    }

    private TepDinhKemResponse resolveTepDinhKem(UUID tepId, Map<UUID, TepDinhKemResponse> cache) {
        if (tepId == null) {
            return null;
        }
        if (cache.containsKey(tepId)) {
            return cache.get(tepId);
        }
        try {
            TepDinhKemResponse tep = tepDinhKemService.getById(tepId);
            cache.put(tepId, tep);
            return tep;
        } catch (Exception ex) {
            cache.put(tepId, null);
            return null;
        }
    }

    private SanLuongAnhResponse toAnhResponse(SanLuongAnh entity, TepDinhKemResponse tep) {
        SanLuongAnhResponse response = new SanLuongAnhResponse();
        response.setId(entity.getId().toString());
        response.setUploadedAt(entity.getNgayTao());
        response.setLoaiAnh(entity.getLoaiAnh());
        response.setDone(Boolean.TRUE.equals(entity.getDaHoanThanh()));
        response.setLabel(firstNonBlank(entity.getMoTa(), "Ảnh thi công"));
        if (entity.getHangMucCongViecId() != null) {
            response.setHangMucCongViecId(entity.getHangMucCongViecId().toString());
        }
        if (entity.getHangMucChiTietId() != null) {
            response.setHangMucChiTietId(entity.getHangMucChiTietId().toString());
        }
        if (tep != null) {
            response.setFilename(firstNonBlank(tep.getTenTepGoc(), tep.getTenTep(), "image.jpg"));
            response.setUrl(firstNonBlank(tep.getUrl(), tep.getDuongDan()));
        } else {
            response.setFilename("image.jpg");
        }
        return response;
    }

    private LocalDate earliestDate(List<SanLuong> records, String status) {
        return records.stream()
                .filter(item -> status.equalsIgnoreCase(item.getTrangThai()))
                .map(SanLuong::getNgayThucHien)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(records.stream().map(SanLuong::getNgayThucHien).filter(Objects::nonNull).min(LocalDate::compareTo).orElse(null));
    }

    private LocalDate latestDate(List<SanLuong> records, String status) {
        return records.stream()
                .filter(item -> status.equalsIgnoreCase(item.getTrangThai()))
                .map(SanLuong::getNgayThucHien)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    private String resolveRowStatus(int completionPercent, int issueCount, Instant lastUpdated) {
        if (issueCount > 0) {
            return "issue";
        }
        if (completionPercent >= 100) {
            return "done";
        }
        if (lastUpdated != null && lastUpdated.isBefore(Instant.now().minusSeconds(30L * 24 * 3600))) {
            return "stale";
        }
        return "normal";
    }

    private SanLuongBoLocOptionResponse option(String value, String label) {
        SanLuongBoLocOptionResponse option = new SanLuongBoLocOptionResponse();
        option.setValue(value);
        option.setLabel(label);
        return option;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static UUID uuidOrUnused(UUID value) {
        return value != null ? value : UNUSED_FILTER_UUID;
    }

    private static LocalDate dateOrUnused(LocalDate value) {
        return value != null ? value : UNUSED_FILTER_DATE;
    }

    private record DoiTuongQuanLyFilter(boolean enabled, List<UUID> ids) {
        static DoiTuongQuanLyFilter of(List<UUID> doiTuongQuanLyIds) {
            if (doiTuongQuanLyIds == null || doiTuongQuanLyIds.isEmpty()) {
                return new DoiTuongQuanLyFilter(false, List.of(UNUSED_FILTER_UUID));
            }
            List<UUID> normalized = doiTuongQuanLyIds.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (normalized.isEmpty()) {
                return new DoiTuongQuanLyFilter(false, List.of(UNUSED_FILTER_UUID));
            }
            return new DoiTuongQuanLyFilter(true, normalized);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<RankedItemResponse> breakdownTheoNhom(String nhom) {
        boolean theoNhaThau = "nhathau".equalsIgnoreCase(nhom);
        String cacheKey = "breakdown|" + (theoNhaThau ? "nhathau" : "khuvuc");
        Optional<List> cached = cacheService.get(SanLuongCacheNames.TONG_HOP, cacheKey, List.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<UUID> ids = hopDongDoiTuongService.findActiveIdsWithActiveHopDong(null);
        Map<UUID, HopDongDoiTuongSnapshot> snapshots = hopDongDoiTuongSnapshotService.getSnapshots(ids);
        Map<String, BigDecimal> sums = new LinkedHashMap<>();
        for (HopDongDoiTuongSnapshot snapshot : snapshots.values()) {
            String key = theoNhaThau ? snapshot.getNhaThau() : snapshot.getKhuVuc();
            if (key == null || key.isBlank()) {
                key = "—";
            }
            BigDecimal amount = snapshot.getTongThanhTien() != null ? snapshot.getTongThanhTien() : BigDecimal.ZERO;
            sums.merge(key, amount, BigDecimal::add);
        }
        List<RankedItemResponse> result = sums.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(entry -> RankedItemResponse.builder().label(entry.getKey()).value(entry.getValue()).build())
                .toList();
        cacheService.put(SanLuongCacheNames.TONG_HOP, cacheKey, result, cacheProperties.sanluongTongHopTtl());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<RankedItemResponse> xuHuongTheoThang(int soThang) {
        int thang = Math.min(Math.max(soThang, 1), 24);
        String cacheKey = "xuhuong|" + thang;
        Optional<List> cached = cacheService.get(SanLuongCacheNames.TONG_HOP, cacheKey, List.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        java.time.YearMonth current = java.time.YearMonth.now();
        java.time.YearMonth oldest = current.minusMonths(thang - 1);

        // 1 query group-by-tháng thay vì lặp gọi tongThanhTien N lần (N round-trip DB riêng biệt).
        Map<java.time.YearMonth, BigDecimal> totalsByMonth = new HashMap<>();
        for (Object[] row : sanLuongRepository.sumThanhTienGroupByThang(Boolean.TRUE, oldest.atDay(1), current.atEndOfMonth())) {
            java.time.YearMonth ym = java.time.YearMonth.from(((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate());
            BigDecimal total = row[1] instanceof BigDecimal bd ? bd : BigDecimal.valueOf(((Number) row[1]).doubleValue());
            totalsByMonth.put(ym, total);
        }

        List<RankedItemResponse> result = new ArrayList<>();
        for (int i = thang - 1; i >= 0; i--) {
            java.time.YearMonth ym = current.minusMonths(i);
            BigDecimal total = totalsByMonth.getOrDefault(ym, BigDecimal.ZERO);
            result.add(RankedItemResponse.builder().label(ym.toString()).value(total).build());
        }
        cacheService.put(SanLuongCacheNames.TONG_HOP, cacheKey, result, cacheProperties.sanluongTongHopTtl());
        return result;
    }

    private static final int AUDIT_TOI_DA_DONG = 30;

    /**
     * Ghi nhật ký thao tác sản lượng vào audit_log, MỖI hạng mục 1 dòng (cột mo_ta chỉ 255 ký tự nên không gộp nhiều hạng mục vào một dòng).
     * Người thực hiện và IP lấy từ ngữ cảnh đăng nhập. Chạy bất đồng bộ, lỗi thì bỏ qua: ghi nhật ký không được làm hỏng nghiệp vụ.
     */
    private void ghiAuditSanLuong(String action, String label, List<String> chiTiet, UUID hopDongId, UUID hopDongDoiTuongId) {
        if (chiTiet == null || chiTiet.isEmpty()) {
            return;
        }
        try {
            Map<String, Object> attrs = new HashMap<>();
            if (hopDongId != null) {
                attrs.put("hopDongId", hopDongId);
            }
            if (hopDongDoiTuongId != null) {
                attrs.put("doiTuongIds", List.of(hopDongDoiTuongId));
            }
            for (String dong : chiTiet.size() > AUDIT_TOI_DA_DONG ? chiTiet.subList(0, AUDIT_TOI_DA_DONG) : chiTiet) {
                appEventContext.audit(action, label, dong, attrs);
            }
            if (chiTiet.size() > AUDIT_TOI_DA_DONG) {
                appEventContext.audit(action, label, "… và " + (chiTiet.size() - AUDIT_TOI_DA_DONG) + " hạng mục khác", attrs);
            }
        } catch (Exception ex) {
            log.warn("ghi audit sản lượng thất bại action={}", action, ex);
        }
    }

    /** Ảnh chụp giá trị của 1 bản ghi sản lượng trước khi sửa, để ghi nhật ký "cũ → mới" chỉ với các trường thay đổi. */
    private record GiaTriSl(BigDecimal khoiLuong, BigDecimal donGia, String trangThai, LocalDate ngay) {
        static GiaTriSl of(SanLuong s) {
            return new GiaTriSl(s.getKhoiLuongHoanThanh(), s.getDonGia(), s.getTrangThai(), s.getNgayThucHien());
        }

        /** Chuỗi các trường thay đổi (vd "KL 5 → 8; ngày 2026-09-18 → 2026-09-19"); null nếu không đổi. */
        String diff(GiaTriSl sau) {
            List<String> ds = new ArrayList<>();
            them(ds, "KL", khoiLuong, sau.khoiLuong);
            them(ds, "đơn giá", donGia, sau.donGia);
            them(ds, "trạng thái", trangThai, sau.trangThai);
            them(ds, "ngày", ngay, sau.ngay);
            return ds.isEmpty() ? null : String.join("; ", ds);
        }

        private static void them(List<String> ds, String ten, Object truoc, Object sau) {
            String a = hienThi(truoc);
            String b = hienThi(sau);
            if (!a.equals(b)) {
                ds.add(ten + " " + a + " → " + b);
            }
        }

        private static String hienThi(Object v) {
            if (v == null) {
                return "—";
            }
            return v instanceof BigDecimal bd ? bd.stripTrailingZeros().toPlainString() : v.toString();
        }
    }

    /** Giá trị của 1 bản ghi sản lượng, dùng cho nhật ký: khối lượng, đơn giá, trạng thái, ngày thực hiện. */
    private static String moTaGiaTri(SanLuong s) {
        return "KL=" + (s.getKhoiLuongHoanThanh() != null ? s.getKhoiLuongHoanThanh().stripTrailingZeros().toPlainString() : "—")
                + ", đơn giá=" + (s.getDonGia() != null ? s.getDonGia().stripTrailingZeros().toPlainString() : "—")
                + ", trạng thái=" + (s.getTrangThai() != null ? s.getTrangThai() : "—")
                + ", ngày=" + (s.getNgayThucHien() != null ? s.getNgayThucHien() : "—");
    }

    private static String moTaBanGhi(SanLuong s) {
        return "hạng mục " + (s.getHangMucCongViecId() != null ? s.getHangMucCongViecId() : s.getHangMucChiTietId()) + ": " + moTaGiaTri(s);
    }

    /** Tên hạng mục (mã - tên) theo danh mục của hợp đồng; không tìm thấy thì dùng id. */
    private static String tenHangMuc(ContractCatalog catalog, SanLuong s) {
        UUID cv = s.getHangMucCongViecId();
        UUID ct = s.getHangMucChiTietId();
        if (catalog != null) {
            for (ContractWorkItem item : catalog.items()) {
                if ((cv != null && cv.equals(item.hangMucCongViecId())) || (cv == null && ct != null && ct.equals(item.hangMucChiTietId()))) {
                    return (item.code() != null ? item.code() + " - " : "") + item.name();
                }
            }
        }
        return "hạng mục " + (cv != null ? cv : ct);
    }
}
