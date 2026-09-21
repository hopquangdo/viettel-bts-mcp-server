package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.edu.huce.iic.bts_ops_platform.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.dto.NameLookupResponse;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.config.AppCacheProperties;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.cache.ThuVienCacheNames;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.TinhThanhCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.TinhThanhCuEntry;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.TinhThanhNhomCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.TinhThanhNhomTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.TinhThanhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.TinhThanhNhomResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.TinhThanhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.KhuVuc;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.TinhThanh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.TinhThanhKhuVuc;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.exception.ThuVienErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.mapper.ThuVienMapper;
import org.springframework.context.ApplicationEventPublisher;
import vn.edu.huce.iic.bts_ops_platform.common.event.GeoMetaChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.TinhThanhKhuVucRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.TinhThanhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.services.KhuVucService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.services.TinhThanhService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TinhThanhServiceImpl implements TinhThanhService {

    private static final String NAME_LOOKUP_CACHE_KEY = "all";

    private final TinhThanhRepository tinhThanhRepository;
    private final TinhThanhKhuVucRepository tinhThanhKhuVucRepository;
    private final KhuVucService khuVucService;
    private final ThuVienMapper thuVienMapper;
    private final CacheService cacheService;
    private final AppCacheProperties cacheProperties;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, String> buildProvinceCodeLookup() {
        Optional<NameLookupResponse> cached = cacheService.get(
                ThuVienCacheNames.PROVINCE_CODE_LOOKUP, NAME_LOOKUP_CACHE_KEY, NameLookupResponse.class);
        if (cached.isPresent()) {
            return cached.get().items();
        }

        Map<UUID, String> lookup = new HashMap<>();
        for (TinhThanh item : tinhThanhRepository.findByNgayXoaIsNullOrderByMaAsc()) {
            if (!Boolean.TRUE.equals(item.getHoatDong()) || item.getMa() == null || item.getMa().isBlank()) {
                continue;
            }
            lookup.putIfAbsent(item.getId(), item.getMa().trim());
            if (item.getTinhThanhId() != null) {
                lookup.putIfAbsent(item.getTinhThanhId(), item.getMa().trim());
            }
        }
        cacheService.put(
                ThuVienCacheNames.PROVINCE_CODE_LOOKUP, NAME_LOOKUP_CACHE_KEY, new NameLookupResponse(lookup),
                cacheProperties.nameLookupTtl());
        return lookup;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TinhThanhResponse> list(String search, UUID khuVucId, Boolean activeOnly) {
        String keyword = EntityFilter.normalizeSearch(search);
        Map<UUID, KhuVuc> khuVucById = khuVucService.findAllActiveEntities().stream()
                .collect(Collectors.toMap(KhuVuc::getId, Function.identity()));
        Map<UUID, TinhThanhKhuVuc> mappingByTinhThanhId = tinhThanhKhuVucRepository.findAll().stream()
                .filter(m -> m.getNgayXoa() == null)
                .collect(Collectors.toMap(TinhThanhKhuVuc::getTinhThanhId, Function.identity()));

        return tinhThanhRepository.findByNgayXoaIsNullOrderByMaAsc().stream()
                .map(entity -> toResponse(entity, mappingByTinhThanhId, khuVucById))
                .filter(row -> khuVucId == null || khuVucId.equals(row.getKhuVucId()))
                .filter(row -> activeOnly == null || activeOnly == row.isHoatDong())
                .filter(row -> EntityFilter.matchesKeyword(
                        keyword,
                        EntityFilter.nullToEmpty(row.getMa()),
                        EntityFilter.nullToEmpty(row.getTen()),
                        EntityFilter.nullToEmpty(row.getTenKhuVuc())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TinhThanhResponse getById(UUID id) {
        TinhThanh entity = tinhThanhRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(ThuVienErrorCode.TINH_THANH_NOT_FOUND, "Không tìm thấy tỉnh/thành"));
        return toResponse(entity);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        TinhThanh entity = tinhThanhRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(ThuVienErrorCode.TINH_THANH_NOT_FOUND, "Không tìm thấy tỉnh/thành"));
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        tinhThanhRepository.save(entity);

        tinhThanhKhuVucRepository.findByTinhThanhId(entity.getTinhThanhId()).ifPresent(mapping -> {
            mapping.setNgayXoa(Instant.now());
            mapping.setHoatDong(false);
            tinhThanhKhuVucRepository.save(mapping);
        });
    }

    @Override
    @Transactional
    public TinhThanhResponse create(TinhThanhTaoRequest request) {
        KhuVuc khuVuc = findRegionById(request.getKhuVucId());
        TinhThanh entity = thuVienMapper.fromTinhThanhTaoRequest(request);
        UUID tinhThanhId = request.getTinhThanhId() != null
                ? request.getTinhThanhId()
                : UUID.randomUUID();
        // Mã cũ phải gắn vào nhóm đã có; tỉnh hiện tại có thể khởi tạo nhóm bằng tinhThanhId client gửi lên.
        if (request.getTinhThanhId() != null && Boolean.TRUE.equals(request.getLaTinhCu())) {
            ensureTinhThanhGroupExists(tinhThanhId);
        }
        entity.setTinhThanhId(tinhThanhId);
        entity.setMa(EntityFilter.normalizeCode(request.getMa()));
        entity.setTen(request.getTen().trim());
        entity.setLaTinhCu(Boolean.TRUE.equals(request.getLaTinhCu()));
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        TinhThanh saved = tinhThanhRepository.save(entity);
        saveRegionMapping(tinhThanhId, khuVuc.getId());
        return toResponse(saved, khuVuc);
    }

    @Override
    @Transactional
    public TinhThanhResponse update(UUID id, TinhThanhCapNhatRequest request) {
        TinhThanh entity = tinhThanhRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(ThuVienErrorCode.TINH_THANH_NOT_FOUND, "Không tìm thấy tỉnh/thành"));
        KhuVuc khuVuc = findRegionById(request.getKhuVucId());
        thuVienMapper.updateFromTinhThanhCapNhatRequest(request, entity);
        entity.setMa(EntityFilter.normalizeCode(request.getMa()));
        entity.setTen(request.getTen().trim());
        if (request.getLaTinhCu() != null) {
            entity.setLaTinhCu(request.getLaTinhCu());
        }
        if (request.getHoatDong() != null) {
            entity.setHoatDong(request.getHoatDong());
        }

        saveRegionMapping(entity.getTinhThanhId(), khuVuc.getId());
        TinhThanhResponse response = toResponse(tinhThanhRepository.save(entity), khuVuc);
        // Tên/mã tỉnh có thể ảnh hưởng nhiều hợp đồng/đối tượng không rẻ để liệt kê ở
        // đây — phát sự kiện để HopDongConsumer tự evict toàn bộ cache snapshot.
        applicationEventPublisher.publishEvent(GeoMetaChangedEvent.now());
        return response;
    }

    @Override
    @Transactional
    public TinhThanhNhomResponse createGroup(TinhThanhNhomTaoRequest request) {
        KhuVuc khuVuc = findRegionById(request.getKhuVucId());
        UUID tinhThanhId = UUID.randomUUID();
        boolean hoatDong = request.getHoatDong() == null || request.getHoatDong();

        TinhThanh main = saveProvinceRow(
                tinhThanhId,
                EntityFilter.normalizeCode(request.getMa()),
                request.getTen().trim(),
                false,
                hoatDong);
        saveRegionMapping(tinhThanhId, khuVuc.getId());

        List<TinhThanhResponse> oldResponses = new ArrayList<>();
        if (request.getTinhCu() != null) {
            for (TinhThanhCuEntry entry : request.getTinhCu()) {
                if (!StringUtils.hasText(entry.getMa()) || !StringUtils.hasText(entry.getTen())) {
                    continue;
                }
                TinhThanh old = saveProvinceRow(
                        tinhThanhId,
                        EntityFilter.normalizeCode(entry.getMa()),
                        entry.getTen().trim(),
                        true,
                        hoatDong);
                oldResponses.add(toResponse(old, khuVuc));
            }
        }

        TinhThanhNhomResponse response = new TinhThanhNhomResponse();
        response.setTinhThanhId(tinhThanhId);
        response.setTinhHienTai(toResponse(main, khuVuc));
        response.setTinhCu(oldResponses);
        return response;
    }

    @Override
    @Transactional
    public TinhThanhNhomResponse updateGroup(UUID tinhThanhId, TinhThanhNhomCapNhatRequest request) {
        KhuVuc khuVuc = findRegionById(request.getKhuVucId());
        List<TinhThanh> inGroup = findActiveGroup(tinhThanhId);

        TinhThanh main = inGroup.stream()
                .filter(row -> !Boolean.TRUE.equals(row.getLaTinhCu()))
                .findFirst()
                .orElseThrow(() -> new AppException(ThuVienErrorCode.TINH_THANH_NOT_FOUND, "Không tìm thấy tỉnh hiện tại"));

        main.setMa(EntityFilter.normalizeCode(request.getMa()));
        main.setTen(request.getTen().trim());
        if (request.getHoatDong() != null) {
            main.setHoatDong(request.getHoatDong());
        }
        tinhThanhRepository.save(main);
        saveRegionMapping(tinhThanhId, khuVuc.getId());

        List<TinhThanh> existingOlds = inGroup.stream()
                .filter(row -> Boolean.TRUE.equals(row.getLaTinhCu()))
                .toList();
        List<UUID> formOldIds = request.getTinhCu() == null
                ? List.of()
                : request.getTinhCu().stream()
                        .map(TinhThanhCuEntry::getId)
                        .filter(id -> id != null)
                        .toList();

        for (TinhThanh old : existingOlds) {
            if (!formOldIds.contains(old.getId())) {
                softDeleteProvince(old);
            }
        }

        List<TinhThanhResponse> oldResponses = new ArrayList<>();
        boolean hoatDong = request.getHoatDong() != null ? request.getHoatDong() : main.getHoatDong();

        if (request.getTinhCu() != null) {
            for (TinhThanhCuEntry entry : request.getTinhCu()) {
                if (!StringUtils.hasText(entry.getMa()) || !StringUtils.hasText(entry.getTen())) {
                    continue;
                }
                TinhThanh savedOld;
                if (entry.getId() != null) {
                    savedOld = tinhThanhRepository.findByIdAndNgayXoaIsNull(entry.getId())
                            .orElseThrow(() -> new AppException(ThuVienErrorCode.TINH_THANH_NOT_FOUND, "Không tìm thấy tỉnh cũ"));
                    savedOld.setMa(EntityFilter.normalizeCode(entry.getMa()));
                    savedOld.setTen(entry.getTen().trim());
                    savedOld.setHoatDong(hoatDong);
                    savedOld = tinhThanhRepository.save(savedOld);
                } else {
                    savedOld = saveProvinceRow(
                            tinhThanhId,
                            EntityFilter.normalizeCode(entry.getMa()),
                            entry.getTen().trim(),
                            true,
                            hoatDong);
                }
                oldResponses.add(toResponse(savedOld, khuVuc));
            }
        }

        TinhThanhNhomResponse response = new TinhThanhNhomResponse();
        response.setTinhThanhId(tinhThanhId);
        response.setTinhHienTai(toResponse(main, khuVuc));
        response.setTinhCu(oldResponses);
        return response;
    }

    @Override
    @Transactional
    public void deleteGroup(UUID tinhThanhId) {
        List<TinhThanh> inGroup = findActiveGroup(tinhThanhId);
        if (inGroup.isEmpty()) {
            throw new AppException(ThuVienErrorCode.TINH_THANH_NOT_FOUND, "Không tìm thấy tỉnh/thành");
        }
        for (TinhThanh entity : inGroup) {
            softDeleteProvince(entity);
        }
        tinhThanhKhuVucRepository.findByTinhThanhId(tinhThanhId).ifPresent(mapping -> {
            mapping.setNgayXoa(Instant.now());
            mapping.setHoatDong(false);
            tinhThanhKhuVucRepository.save(mapping);
        });
    }

    private TinhThanh saveProvinceRow(
            UUID tinhThanhId,
            String ma,
            String ten,
            boolean laTinhCu,
            boolean hoatDong) {
        TinhThanh entity = new TinhThanh();
        entity.setTinhThanhId(tinhThanhId);
        entity.setMa(ma);
        entity.setTen(ten);
        entity.setLaTinhCu(laTinhCu);
        entity.setHoatDong(hoatDong);
        return tinhThanhRepository.save(entity);
    }

    private List<TinhThanh> findActiveGroup(UUID tinhThanhId) {
        return tinhThanhRepository.findByNgayXoaIsNullOrderByMaAsc().stream()
                .filter(row -> tinhThanhId.equals(row.getTinhThanhId()))
                .toList();
    }

    private void softDeleteProvince(TinhThanh entity) {
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        tinhThanhRepository.save(entity);
    }

    private TinhThanhResponse toResponse(TinhThanh entity) {
        TinhThanhKhuVuc mapping = tinhThanhKhuVucRepository.findByTinhThanhId(entity.getTinhThanhId())
                .filter(m -> m.getNgayXoa() == null)
                .orElse(null);
        KhuVuc khuVuc = mapping == null
                ? null
                : khuVucService.findActiveEntityById(mapping.getKhuVucId()).orElse(null);
        return toResponse(entity, mapping, khuVuc);
    }

    private TinhThanhResponse toResponse(
            TinhThanh entity,
            Map<UUID, TinhThanhKhuVuc> mappingByTinhThanhId,
            Map<UUID, KhuVuc> khuVucById) {
        TinhThanhKhuVuc mapping = mappingByTinhThanhId.get(entity.getTinhThanhId());
        KhuVuc khuVuc = mapping == null ? null : khuVucById.get(mapping.getKhuVucId());
        return toResponse(entity, mapping, khuVuc);
    }

    private TinhThanhResponse toResponse(TinhThanh entity, KhuVuc khuVuc) {
        TinhThanhKhuVuc mapping = tinhThanhKhuVucRepository.findByTinhThanhId(entity.getTinhThanhId())
                .filter(m -> m.getNgayXoa() == null)
                .orElse(null);
        return toResponse(entity, mapping, khuVuc);
    }

    private TinhThanhResponse toResponse(TinhThanh entity, TinhThanhKhuVuc mapping, KhuVuc khuVuc) {
        TinhThanhResponse response = thuVienMapper.toTinhThanhResponse(entity);
        if (mapping != null) {
            response.setKhuVucId(mapping.getKhuVucId());
        }
        if (khuVuc != null) {
            response.setTenKhuVuc(khuVuc.getTen());
        }
        return response;
    }

    private void saveRegionMapping(UUID tinhThanhId, UUID khuVucId) {
        TinhThanhKhuVuc mapping = tinhThanhKhuVucRepository.findByTinhThanhId(tinhThanhId)
                .orElseGet(() -> {
                    TinhThanhKhuVuc created = new TinhThanhKhuVuc();
                    created.setTinhThanhId(tinhThanhId);
                    return created;
                });
        mapping.setKhuVucId(khuVucId);
        mapping.setHoatDong(true);
        mapping.setNgayXoa(null);
        tinhThanhKhuVucRepository.save(mapping);
    }

    private KhuVuc findRegionById(UUID id) {
        return khuVucService.findActiveEntityById(id)
                .orElseThrow(() -> new AppException(ThuVienErrorCode.KHU_VUC_NOT_FOUND, "Không tìm thấy khu vực"));
    }

    private void ensureTinhThanhGroupExists(UUID tinhThanhId) {
        boolean exists = tinhThanhRepository.findByNgayXoaIsNullOrderByMaAsc().stream()
                .anyMatch(row -> tinhThanhId.equals(row.getTinhThanhId()));
        if (!exists) {
            throw new AppException(ThuVienErrorCode.TINH_THANH_NOT_FOUND, "Không tìm thấy nhóm tỉnh/thành");
        }
    }

}
