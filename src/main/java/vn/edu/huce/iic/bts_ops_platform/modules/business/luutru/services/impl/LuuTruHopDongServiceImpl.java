package vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScope;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScopeService;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.dto.response.HopDongLuuTruLichSuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.dto.response.HopDongLuuTruResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.entity.HopDongLuuTru;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.entity.HopDongLuuTruLichSu;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.exception.LuuTruErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.mapper.LuuTruMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.repository.HopDongLuuTruLichSuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.repository.HopDongLuuTruRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.services.LuuTruHopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.HopDongDanhSachTienDoHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventContext;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;

import java.time.Instant;
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
public class LuuTruHopDongServiceImpl implements LuuTruHopDongService {

    private static final double TY_LE_ARCHIVE_TOI_THIEU = 100D;

    private final HopDongRepository hopDongRepository;
    private final HopDongLuuTruRepository hopDongLuuTruRepository;
    private final HopDongLuuTruLichSuRepository hopDongLuuTruLichSuRepository;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final LuuTruMapper luuTruMapper;
    private final ContractorScopeService contractorScopeService;
    private final AppEventContext appEventContext;

    @Override
    @Transactional(readOnly = true)
    public List<HopDongLuuTruResponse> list(String search, Boolean activeOnly, boolean includeDeleted) {
        String keyword = EntityFilter.normalizeSearch(search);
        Optional<ContractorScope> scope = contractorScopeService.currentScope();
        Set<UUID> scopeHopDongIds = scope.map(ContractorScope::hopDongIds).orElse(Set.of());
        boolean hasScope = scope.isPresent();

        List<HopDong> hopDongs = hopDongRepository.findAll().stream()
                .filter(hd -> includeDeleted || hd.getNgayXoa() == null)
                .filter(hd -> activeOnly == null || activeOnly == Boolean.FALSE || Boolean.TRUE.equals(hd.getHoatDong()))
                .filter(hd -> !hasScope || scopeHopDongIds.contains(hd.getId()))
                .filter(hd -> matchesSearch(hd, keyword))
                .sorted(Comparator.comparing(HopDong::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        if (hopDongs.isEmpty()) {
            return List.of();
        }

        List<UUID> hopDongIds = hopDongs.stream().map(HopDong::getId).toList();
        Map<UUID, HopDongLuuTru> luuTruByHopDong = hopDongLuuTruRepository
                .findByHopDongIdInAndNgayXoaIsNull(hopDongIds).stream()
                .collect(Collectors.toMap(HopDongLuuTru::getHopDongId, item -> item, (a, b) -> a));

        Map<UUID, Long> soTramByHopDong = toCountMap(
                hopDongDoiTuongService.countGroupByHopDongId(Boolean.TRUE, null, null));
        Map<UUID, Map<String, Long>> statusCountsByHopDong = toStatusCountMap(
                hopDongDoiTuongService.countGroupByHopDongIdAndTrangThaiMa(Boolean.TRUE, null, null));
        Map<UUID, String> khuVucByHopDong = loadKhuVucByHopDong(hopDongIds);

        List<HopDongLuuTruResponse> result = new ArrayList<>();
        for (HopDong hopDong : hopDongs) {
            UUID hopDongId = hopDong.getId();
            long soTram = soTramByHopDong.getOrDefault(hopDongId, 0L);
            double tyLe = HopDongDanhSachTienDoHelper.computeTyLeHoanThanh(
                    statusCountsByHopDong.getOrDefault(hopDongId, Map.of()));
            result.add(luuTruMapper.toResponse(
                    hopDong,
                    luuTruByHopDong.get(hopDongId),
                    khuVucByHopDong.getOrDefault(hopDongId, "—"),
                    soTram,
                    tyLe));
        }
        return result;
    }

    @Override
    @Transactional
    public HopDongLuuTruResponse archive(UUID hopDongId) {
        HopDong hopDong = requireHopDong(hopDongId);
        double tyLe = computeTyLeHoanThanh(hopDongId);
        if (tyLe < TY_LE_ARCHIVE_TOI_THIEU) {
            throw new AppException(
                    LuuTruErrorCode.CHUA_DU_DIEU_KIEN_ARCHIVE,
                    "Hợp đồng chưa đạt 100% hoàn thành, không thể Archive");
        }

        HopDongLuuTru entity = hopDongLuuTruRepository.findByHopDongIdAndNgayXoaIsNull(hopDongId)
                .orElseGet(() -> {
                    HopDongLuuTru created = new HopDongLuuTru();
                    created.setHopDongId(hopDongId);
                    return created;
                });

        if ("archived".equalsIgnoreCase(entity.getTrangThai())) {
            throw new AppException(LuuTruErrorCode.DA_ARCHIVE, "Hợp đồng đã được Archive");
        }

        JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
        Instant now = Instant.now();
        entity.setTrangThai("archived");
        entity.setNgayArchive(now);
        entity.setNguoiArchiveId(user.id());
        entity.setNguoiArchiveTen(user.hoTen());
        entity.setGhiChu("Archive hợp đồng");
        HopDongLuuTru saved = hopDongLuuTruRepository.save(entity);
        appendLichSu(saved);
        appEventContext.audit(
                "ARCHIVE_HOP_DONG",
                "Archive hợp đồng " + hopDong.getMaHopDong(),
                null,
                Map.of("hopDongId", hopDongId));

        return buildDetailResponse(hopDong, saved);
    }

    @Override
    @Transactional
    public HopDongLuuTruResponse khoiPhuc(UUID hopDongId) {
        HopDong hopDong = requireHopDong(hopDongId);
        HopDongLuuTru entity = hopDongLuuTruRepository.findByHopDongIdAndNgayXoaIsNull(hopDongId)
                .orElseThrow(() -> new AppException(LuuTruErrorCode.LUU_TRU_NOT_FOUND, "Hợp đồng chưa được Archive"));

        if (!"archived".equalsIgnoreCase(entity.getTrangThai())) {
            throw new AppException(LuuTruErrorCode.CHUA_ARCHIVE, "Hợp đồng chưa ở trạng thái Archive");
        }

        JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
        entity.setTrangThai("active");
        entity.setNgayArchive(null);
        entity.setNguoiArchiveId(user.id());
        entity.setNguoiArchiveTen(user.hoTen());
        entity.setGhiChu("Khôi phục hợp đồng");
        HopDongLuuTru saved = hopDongLuuTruRepository.save(entity);
        appendLichSu(saved);
        appEventContext.audit(
                "KHOI_PHUC_HOP_DONG",
                "Khôi phục hợp đồng " + hopDong.getMaHopDong(),
                null,
                Map.of("hopDongId", hopDongId));

        return buildDetailResponse(hopDong, saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongLuuTruLichSuResponse> lichSu(UUID hopDongId) {
        requireHopDong(hopDongId);
        return hopDongLuuTruLichSuRepository.findByHopDongIdOrderByNgayTaoDesc(hopDongId).stream()
                .map(luuTruMapper::toLichSuResponse)
                .toList();
    }

    private HopDong requireHopDong(UUID hopDongId) {
        return hopDongRepository.findById(hopDongId)
                .filter(hd -> hd.getNgayXoa() == null)
                .orElseThrow(() -> new AppException(LuuTruErrorCode.HOP_DONG_NOT_FOUND, "Không tìm thấy hợp đồng"));
    }

    private double computeTyLeHoanThanh(UUID hopDongId) {
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (Object[] row : hopDongDoiTuongService.countGroupByHopDongIdAndTrangThaiMa(Boolean.TRUE, null, null)) {
            if (row.length >= 3 && hopDongId.equals(row[0])) {
                String statusMa = row[1] != null ? row[1].toString() : "UNKNOWN";
                long count = row[2] instanceof Number number ? number.longValue() : 0L;
                statusCounts.merge(statusMa, count, Long::sum);
            }
        }
        return HopDongDanhSachTienDoHelper.computeTyLeHoanThanh(statusCounts);
    }

    private HopDongLuuTruResponse buildDetailResponse(HopDong hopDong, HopDongLuuTru saved) {
        long soTram = toCountMap(hopDongDoiTuongService.countGroupByHopDongId(Boolean.TRUE, null, null))
                .getOrDefault(hopDong.getId(), 0L);
        double tyLe = computeTyLeHoanThanh(hopDong.getId());
        Map<UUID, String> khuVucByHopDong = loadKhuVucByHopDong(List.of(hopDong.getId()));
        return luuTruMapper.toResponse(
                hopDong,
                saved,
                khuVucByHopDong.getOrDefault(hopDong.getId(), "—"),
                soTram,
                tyLe);
    }

    private void appendLichSu(HopDongLuuTru entity) {
        HopDongLuuTruLichSu lichSu = new HopDongLuuTruLichSu();
        lichSu.setHopDongId(entity.getHopDongId());
        lichSu.setTrangThai(entity.getTrangThai());
        lichSu.setNgayArchive(entity.getNgayArchive());
        lichSu.setNguoiArchiveId(entity.getNguoiArchiveId());
        lichSu.setNguoiArchiveTen(entity.getNguoiArchiveTen());
        lichSu.setGhiChu(entity.getGhiChu());
        hopDongLuuTruLichSuRepository.save(lichSu);
    }

    private Map<UUID, String> loadKhuVucByHopDong(List<UUID> hopDongIds) {
        if (hopDongIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, String> result = new HashMap<>();
        for (Object[] row : hopDongLuuTruRepository.findKhuVucMaByHopDongIds(hopDongIds.toArray(new UUID[0]))) {
            if (row.length >= 2 && row[0] instanceof UUID hopDongId) {
                result.put(hopDongId, Objects.toString(row[1], "—"));
            }
        }
        return result;
    }

    private static Map<UUID, Long> toCountMap(List<Object[]> rows) {
        Map<UUID, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            if (row.length >= 2 && row[0] instanceof UUID hopDongId && row[1] instanceof Number count) {
                result.put(hopDongId, count.longValue());
            }
        }
        return result;
    }

    private static Map<UUID, Map<String, Long>> toStatusCountMap(List<Object[]> rows) {
        Map<UUID, Map<String, Long>> result = new HashMap<>();
        for (Object[] row : rows) {
            if (row.length < 3 || !(row[0] instanceof UUID hopDongId)) {
                continue;
            }
            String statusMa = row[1] != null ? row[1].toString() : "UNKNOWN";
            long count = row[2] instanceof Number number ? number.longValue() : 0L;
            result.computeIfAbsent(hopDongId, ignored -> new LinkedHashMap<>())
                    .merge(statusMa, count, Long::sum);
        }
        return result;
    }

    private static boolean matchesSearch(HopDong hopDong, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String q = keyword.toLowerCase(Locale.ROOT);
        return contains(hopDong.getMaHopDong(), q)
                || contains(hopDong.getMa(), q)
                || contains(hopDong.getTen(), q);
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
