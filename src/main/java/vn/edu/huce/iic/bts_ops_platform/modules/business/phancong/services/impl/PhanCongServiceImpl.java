package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScope;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScopeService;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.PaginationDefaults;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.cache.PhanCongAggregateCache;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.entity.NguoiDung;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.repository.NguoiDungRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.request.PhanCongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.request.PhanCongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response.PhanCongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response.PhanCongThongKeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.entity.PhanCong;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.mapper.PhanCongMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.repository.PhanCongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.services.PhanCongService;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhanCongServiceImpl implements PhanCongService {

    private final PhanCongRepository phanCongRepository;
    private final PhanCongMapper phanCongMapper;
    private final ContractorScopeService contractorScopeService;
    private final NguoiDungRepository nguoiDungRepository;
    private final PhanCongAggregateCache phanCongAggregateCache;

    @Override
    @Transactional(readOnly = true)
    public List<PhanCongResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hopDongId) {
        return filteredEntities(search, activeOnly, includeDeleted, hopDongId).stream()
                .map(phanCongMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PhanCongResponse> listPage(
            String search, Boolean activeOnly, boolean includeDeleted, UUID hopDongId, Integer page, Integer size) {
        List<PhanCong> filtered = filteredEntities(search, activeOnly, includeDeleted, hopDongId);
        int pageNumber = EntityFilter.normalizePage(page);
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE);
        int fromIndex = Math.min(pageNumber * pageSize, filtered.size());
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        List<PhanCongResponse> items = filtered.subList(fromIndex, toIndex).stream()
                .map(phanCongMapper::toResponse)
                .toList();
        return PageResponse.ofItems(items, pageNumber, pageSize, filtered.size());
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<PhanCongResponse> theoCanBo(String tenCanBo) {
        if (tenCanBo == null || tenCanBo.isBlank()) {
            return List.of();
        }
        String cacheKey = "theo-canbo|" + tenCanBo.trim().toLowerCase();
        return phanCongAggregateCache.getOrLoad(cacheKey, List.class, () -> computeTheoCanBo(tenCanBo));
    }

    private List<PhanCongResponse> computeTheoCanBo(String tenCanBo) {
        List<NguoiDung> matched = nguoiDungRepository.findByHoTenContainingIgnoreCaseAndNgayXoaIsNull(tenCanBo.trim());
        if (matched.isEmpty()) {
            return List.of();
        }
        NguoiDung nguoiDung = matched.get(0);
        return phanCongRepository.findActiveForContractor(nguoiDung.getId(), nguoiDung.getHoTen(), nguoiDung.getTenDangNhap())
                .stream()
                .map(phanCongMapper::toResponse)
                .toList();
    }

    private List<PhanCong> filteredEntities(String search, Boolean activeOnly, boolean includeDeleted, UUID hopDongId) {
        String keyword = EntityFilter.normalizeSearch(search);
        List<PhanCong> source = includeDeleted ? phanCongRepository.findAll() : phanCongRepository.findByNgayXoaIsNull();
        return source.stream()
                .filter(entity -> EntityFilter.isActive(entity, PhanCong::getHoatDong, activeOnly))
                .filter(entity -> hopDongId == null || hopDongId.equals(entity.getHopDongId()))
                .filter(this::matchesContractorScope)
                .filter(entity -> EntityFilter.matchesKeyword(keyword,
                        EntityFilter.nullToEmpty(entity.getNhaThau()),
                        EntityFilter.nullToEmpty(entity.getGiaiDoan()),
                        EntityFilter.nullToEmpty(entity.getMaVung())))
                .sorted(Comparator.comparing(PhanCong::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PhanCongResponse getById(UUID id) {
        PhanCong entity = findById(id);
        if (!matchesContractorScope(entity)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi");
        }
        return phanCongMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public PhanCongResponse create(PhanCongTaoRequest request) {
        PhanCong entity = phanCongMapper.fromTaoRequest(request);
        normalize(entity);
        if (entity.getHoatDong() == null) {
            entity.setHoatDong(true);
        }
        PhanCongResponse response = phanCongMapper.toResponse(phanCongRepository.save(entity));
        phanCongAggregateCache.evictAll();
        return response;
    }

    @Override
    @Transactional
    public PhanCongResponse update(UUID id, PhanCongCapNhatRequest request) {
        PhanCong entity = findById(id);
        phanCongMapper.updateFromCapNhatRequest(request, entity);
        normalize(entity);
        PhanCongResponse response = phanCongMapper.toResponse(phanCongRepository.save(entity));
        phanCongAggregateCache.evictAll();
        return response;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        PhanCong entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        phanCongRepository.save(entity);
        phanCongAggregateCache.evictAll();
    }

    @Override
    @Transactional(readOnly = true)
    public PhanCongThongKeResponse thongKe() {
        return phanCongAggregateCache.getOrLoad("thong-ke", PhanCongThongKeResponse.class, this::computeThongKe);
    }

    private PhanCongThongKeResponse computeThongKe() {
        List<PhanCong> active = phanCongRepository.findByNgayXoaIsNull();
        PhanCongThongKeResponse response = new PhanCongThongKeResponse();
        response.setTongPhanCong(active.size());
        response.setTongHoatDong(active.stream().filter(item -> Boolean.TRUE.equals(item.getHoatDong())).count());
        response.setTheoKhuVuc(groupCount(active, PhanCong::getKhuVucId));
        response.setTheoTinhThanh(groupCount(active, PhanCong::getTinhThanhId));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, String> mapActiveContractorByHopDongDoiTuongId() {
        return mapContractors(phanCongRepository.findByNgayXoaIsNull());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listDistinctActiveContractors() {
        return listDistinctContractorLabels(phanCongRepository.findByNgayXoaIsNull());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, String> mapActiveContractorByHopDongDoiTuongIds(Collection<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds == null || hopDongDoiTuongIds.isEmpty()) {
            return Map.of();
        }
        return mapContractors(phanCongRepository.findByHopDongDoiTuongIdInAndNgayXoaIsNull(
                hopDongDoiTuongIds.stream().distinct().toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listDistinctActiveContractors(Collection<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds == null || hopDongDoiTuongIds.isEmpty()) {
            return List.of();
        }
        return listDistinctContractorLabels(phanCongRepository.findByHopDongDoiTuongIdInAndNgayXoaIsNull(
                hopDongDoiTuongIds.stream().distinct().toList()));
    }

    private Map<UUID, String> mapContractors(List<PhanCong> assignments) {
        Map<UUID, String> nguoiDungNames = loadNguoiDungNames(assignments);
        return assignments.stream()
                .filter(item -> item.getHopDongDoiTuongId() != null)
                .collect(Collectors.toMap(
                        PhanCong::getHopDongDoiTuongId,
                        item -> resolveContractorLabel(item, nguoiDungNames),
                        (left, right) -> left));
    }

    private List<String> listDistinctContractorLabels(List<PhanCong> assignments) {
        Map<UUID, String> nguoiDungNames = loadNguoiDungNames(assignments);
        Set<String> contractors = new TreeSet<>();
        assignments.stream()
                .map(item -> resolveContractorLabel(item, nguoiDungNames))
                .filter(label -> label != null && !label.isBlank() && !"—".equals(label))
                .forEach(contractors::add);
        return List.copyOf(contractors);
    }

    private Map<UUID, String> loadNguoiDungNames(List<PhanCong> assignments) {
        Set<UUID> nguoiDungIds = assignments.stream()
                .map(PhanCong::getNguoiDungId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (nguoiDungIds.isEmpty()) {
            return Map.of();
        }
        return nguoiDungRepository.findByIdInAndNgayXoaIsNull(nguoiDungIds).stream()
                .collect(Collectors.toMap(NguoiDung::getId, NguoiDung::getHoTen, (left, right) -> left));
    }

    private String resolveContractorLabel(PhanCong item, Map<UUID, String> nguoiDungNames) {
        if (item.getNguoiDungId() != null) {
            String hoTen = nguoiDungNames.get(item.getNguoiDungId());
            if (hoTen != null && !hoTen.isBlank()) {
                return hoTen.trim();
            }
        }
        return firstNonBlank(item.getNhaThau(), "—");
    }

    private Map<String, Long> groupCount(List<PhanCong> source, java.util.function.Function<PhanCong, UUID> extractor) {
        return source.stream()
                .map(extractor)
                .map(value -> value == null ? "null" : value.toString())
                .collect(Collectors.groupingBy(value -> value, Collectors.counting()));
    }

    private PhanCong findById(UUID id) {
        return phanCongRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi"));
    }

    private boolean matchesContractorScope(PhanCong entity) {
        Optional<ContractorScope> scope = contractorScopeService.currentScope();
        if (scope.isEmpty()) {
            return true;
        }
        ContractorScope contractorScope = scope.get();
        if (entity.getHopDongDoiTuongId() != null
                && contractorScope.hopDongDoiTuongIds().contains(entity.getHopDongDoiTuongId())) {
            return true;
        }
        return entity.getHopDongId() != null && contractorScope.hopDongIds().contains(entity.getHopDongId());
    }

    private void normalize(PhanCong entity) {
        entity.setNhaThau(EntityFilter.trimToNull(entity.getNhaThau()));
        entity.setGiaiDoan(EntityFilter.trimToNull(entity.getGiaiDoan()));
        entity.setMaVung(EntityFilter.trimToNull(entity.getMaVung()));
    }

    private static String firstNonBlank(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
