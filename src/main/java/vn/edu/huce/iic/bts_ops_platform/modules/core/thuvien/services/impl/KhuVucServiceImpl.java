package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.dto.NameLookupResponse;
import vn.edu.huce.iic.bts_ops_platform.common.event.GeoMetaChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.config.AppCacheProperties;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.cache.ThuVienCacheNames;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.KhuVucCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.KhuVucTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.KhuVucResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.KhuVuc;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.exception.ThuVienErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.mapper.ThuVienMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.KhuVucRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.TinhThanhKhuVucRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.services.KhuVucService;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KhuVucServiceImpl implements KhuVucService {

    private static final String NAME_LOOKUP_CACHE_KEY = "all";

    private final KhuVucRepository khuVucRepository;
    private final TinhThanhKhuVucRepository tinhThanhKhuVucRepository;
    private final ThuVienMapper thuVienMapper;
    private final CacheService cacheService;
    private final AppCacheProperties cacheProperties;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<KhuVucResponse> list(String search, Boolean activeOnly) {
        String keyword = EntityFilter.normalizeSearch(search);
        return khuVucRepository.findByNgayXoaIsNull().stream()
                .filter(k -> EntityFilter.isActive(k, KhuVuc::getHoatDong, activeOnly))
                .filter(k -> EntityFilter.matchesKeyword(
                        keyword,
                        EntityFilter.nullToEmpty(k.getMa()),
                        EntityFilter.nullToEmpty(k.getTen()),
                        EntityFilter.nullToEmpty(k.getMoTa())))
                .sorted(Comparator.comparing(KhuVuc::getMa))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KhuVuc> findAllActiveEntities() {
        return khuVucRepository.findByNgayXoaIsNull();
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<KhuVuc> findActiveEntityById(UUID id) {
        return khuVucRepository.findByIdAndNgayXoaIsNull(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, String> buildRegionNameLookup() {
        Optional<NameLookupResponse> cached = cacheService.get(
                ThuVienCacheNames.REGION_NAME_LOOKUP, NAME_LOOKUP_CACHE_KEY, NameLookupResponse.class);
        if (cached.isPresent()) {
            return cached.get().items();
        }

        Map<UUID, String> lookup = new HashMap<>();
        for (KhuVuc item : khuVucRepository.findByNgayXoaIsNull()) {
            if (!Boolean.TRUE.equals(item.getHoatDong())) {
                continue;
            }
            String label = item.getTen() != null && !item.getTen().isBlank() ? item.getTen()
                    : item.getMa() != null && !item.getMa().isBlank() ? item.getMa() : null;
            if (label == null) {
                continue;
            }
            lookup.put(item.getId(), label);
        }
        cacheService.put(
                ThuVienCacheNames.REGION_NAME_LOOKUP, NAME_LOOKUP_CACHE_KEY, new NameLookupResponse(lookup),
                cacheProperties.nameLookupTtl());
        return lookup;
    }

    @Override
    @Transactional(readOnly = true)
    public long demActive() {
        return khuVucRepository.countFiltered(Boolean.TRUE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KhuVucResponse> getByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return khuVucRepository.findAllById(ids).stream()
                .filter(k -> k.getNgayXoa() == null)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public KhuVucResponse getById(UUID id) {
        return toResponse(findRegionById(id));
    }

    @Override
    @Transactional
    public KhuVucResponse create(KhuVucTaoRequest request) {
        String ma = EntityFilter.normalizeCode(request.getMa());
        if (khuVucRepository.existsByMaIgnoreCase(ma)) {
            throw new AppException(ThuVienErrorCode.KHU_VUC_CODE_EXISTS, "Mã khu vực đã tồn tại");
        }

        KhuVuc entity = thuVienMapper.fromKhuVucTaoRequest(request);
        entity.setMa(ma);
        entity.setTen(request.getTen().trim());
        entity.setMoTa(EntityFilter.trimToNull(request.getMoTa()));
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        return toResponse(khuVucRepository.save(entity));
    }

    @Override
    @Transactional
    public KhuVucResponse update(UUID id, KhuVucCapNhatRequest request) {
        KhuVuc entity = findRegionById(id);
        String ma = EntityFilter.normalizeCode(request.getMa());

        if (khuVucRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) {
            throw new AppException(ThuVienErrorCode.KHU_VUC_CODE_EXISTS, "Mã khu vực đã tồn tại");
        }

        thuVienMapper.updateFromKhuVucCapNhatRequest(request, entity);
        entity.setMa(ma);
        entity.setTen(request.getTen().trim());
        if (request.getMoTa() != null) {
            entity.setMoTa(EntityFilter.trimToNull(request.getMoTa()));
        }

        KhuVucResponse response = toResponse(khuVucRepository.save(entity));
        // Tên/mã khu vực ảnh hưởng nhiều hợp đồng/đối tượng — phát sự kiện để
        // HopDongConsumer tự evict toàn bộ cache snapshot (giống TinhThanhServiceImpl).
        applicationEventPublisher.publishEvent(GeoMetaChangedEvent.now());
        return response;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        KhuVuc entity = findRegionById(id);
        long soTinhThanh = tinhThanhKhuVucRepository.countByKhuVucId(id);
        if (soTinhThanh > 0) {
            throw new AppException(
                    ThuVienErrorCode.KHU_VUC_HAS_PROVINCES,
                    "Khu vực đang có " + soTinhThanh + " tỉnh, không thể xóa");
        }
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        khuVucRepository.save(entity);
    }

    private KhuVuc findRegionById(UUID id) {
        return khuVucRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(ThuVienErrorCode.KHU_VUC_NOT_FOUND, "Không tìm thấy khu vực"));
    }

    private KhuVucResponse toResponse(KhuVuc entity) {
        long soTinhThanh = tinhThanhKhuVucRepository.countByKhuVucId(entity.getId());
        return thuVienMapper.toKhuVucResponse(entity, soTinhThanh);
    }
}
