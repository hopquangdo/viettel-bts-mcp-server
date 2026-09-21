package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongNhomUuTienCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongNhomUuTienTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongNhomUuTienResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongNhomUuTien;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.mapper.HopDongMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongNhomUuTienRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongNhomUuTienService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HopDongNhomUuTienServiceImpl implements HopDongNhomUuTienService {

    private final HopDongNhomUuTienRepository hopDongNhomUuTienRepository;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    // HopDong is the aggregate root of HopDongNhomUuTien (parent-child, not a separate
    // domain), so reading its repository directly here — instead of going through
    // HopDongService — is the intended exception. It also avoids a circular service
    // dependency, since HopDongService itself depends on HopDongNhomUuTienService.
    private final HopDongRepository hopDongRepository;
    private final HopDongMapper hopDongMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HopDongNhomUuTien> findActiveEntitiesByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return hopDongNhomUuTienRepository.findAllById(ids).stream()
                .filter(group -> group.getNgayXoa() == null)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongNhomUuTien> findActiveEntitiesByHopDongId(UUID hopDongId) {
        return hopDongNhomUuTienRepository.findByHopDongIdAndNgayXoaIsNullOrderByThuTuAscNgayTaoAsc(hopDongId);
    }

    @Override
    @Transactional
    public HopDongNhomUuTien saveEntity(HopDongNhomUuTien entity) {
        return hopDongNhomUuTienRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongNhomUuTienResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hopDongId) {
        String keyword = EntityFilter.normalizeSearch(search);
        List<HopDongNhomUuTien> source = hopDongId != null
                ? hopDongNhomUuTienRepository.findByHopDongIdAndNgayXoaIsNullOrderByThuTuAscNgayTaoAsc(hopDongId)
                : (includeDeleted ? hopDongNhomUuTienRepository.findAll() : hopDongNhomUuTienRepository.findByNgayXoaIsNull());
        return source.stream()
                .filter(entity -> hopDongId == null || hopDongId.equals(entity.getHopDongId()))
                .filter(entity -> EntityFilter.matchesKeyword(keyword, EntityFilter.nullToEmpty(entity.getMa()), EntityFilter.nullToEmpty(entity.getTen()), EntityFilter.nullToEmpty(entity.getMauSac())))
                .sorted(Comparator.comparing(HopDongNhomUuTien::getThuTu, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(HopDongNhomUuTien::getNgayTao, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(hopDongMapper::toHopDongNhomUuTienResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HopDongNhomUuTienResponse getById(UUID id) {
        return hopDongMapper.toHopDongNhomUuTienResponse(findById(id));
    }

    @Override
    @Transactional
    public HopDongNhomUuTienResponse create(HopDongNhomUuTienTaoRequest request) {
        assertHopDongExists(request.getHopDongId());
        HopDongNhomUuTien entity = hopDongMapper.fromHopDongNhomUuTienTaoRequest(request);
        entity.setMa(EntityFilter.normalizeCode(request.getMa()));
        if (request.getTen() != null) entity.setTen(request.getTen().trim());
        if (request.getMauSac() != null) entity.setMauSac(request.getMauSac().trim());
        if (entity.getThuTu() == null) {
            entity.setThuTu(nextThuTu(request.getHopDongId()));
        }
        applyDefaultStats(entity);

        return hopDongMapper.toHopDongNhomUuTienResponse(hopDongNhomUuTienRepository.save(entity));
    }

    @Override
    @Transactional
    public HopDongNhomUuTienResponse update(UUID id, HopDongNhomUuTienCapNhatRequest request) {
        HopDongNhomUuTien entity = findById(id);
        hopDongMapper.updateFromHopDongNhomUuTienCapNhatRequest(request, entity);
        if (request.getMa() != null) {
            entity.setMa(EntityFilter.normalizeCode(request.getMa()));
        }
        if (request.getTen() != null) {
            entity.setTen(request.getTen().trim());
        }
        if (request.getMauSac() != null) {
            entity.setMauSac(request.getMauSac().trim());
        }

        return hopDongMapper.toHopDongNhomUuTienResponse(hopDongNhomUuTienRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        HopDongNhomUuTien entity = findById(id);
        hopDongDoiTuongService.clearNhomUuTien(id);
        entity.setNgayXoa(Instant.now());
        hopDongNhomUuTienRepository.save(entity);
    }

    @Override
    @Transactional
    public List<HopDongNhomUuTienResponse> assignDoiTuong(UUID id, List<UUID> doiTuongIds) {
        HopDongNhomUuTien group = findById(id);
        List<UUID> targetIds = doiTuongIds == null ? List.of() : doiTuongIds.stream().distinct().toList();
        if (targetIds.isEmpty()) {
            syncStats(id);
            return List.of(hopDongMapper.toHopDongNhomUuTienResponse(group));
        }

        List<HopDongDoiTuong> entities = hopDongDoiTuongService.findActiveEntitiesByIds(targetIds);
        for (HopDongDoiTuong entity : entities) {
            if (!group.getHopDongId().equals(entity.getHopDongId())) {
                throw new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                        "Đối tượng không thuộc cùng hợp đồng với nhóm ưu tiên");
            }
        }
        hopDongDoiTuongService.updateNhomUuTienForIds(targetIds, id);

        syncStats(id);
        return list(null, true, false, group.getHopDongId());
    }

    @Override
    @Transactional
    public void syncStats(UUID id) {
        HopDongNhomUuTien group = findById(id);
        int total = (int) hopDongDoiTuongService.countByNhomUuTien(id);
        group.setSoNhan(total);
        group.setKeHoach(total);
        group.setHoanThanh(0);
        group.setConLai(total);
        group.setPhanTram(BigDecimal.ZERO);
        hopDongNhomUuTienRepository.save(group);
    }

    private HopDongNhomUuTien findById(UUID id) {
        return hopDongNhomUuTienRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_NHOM_UU_TIEN_NOT_FOUND, "Không tìm thấy bản ghi"));
    }

    private void assertHopDongExists(UUID hopDongId) {
        hopDongRepository.findByIdAndNgayXoaIsNull(hopDongId)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_NOT_FOUND, "Không tìm thấy hợp đồng"));
    }

    private short nextThuTu(UUID hopDongId) {
        List<HopDongNhomUuTien> existing = hopDongNhomUuTienRepository.findByHopDongIdAndNgayXoaIsNullOrderByThuTuAscNgayTaoAsc(hopDongId);
        if (existing.isEmpty()) {
            return 1;
        }
        short max = existing.stream()
                .map(HopDongNhomUuTien::getThuTu)
                .filter(value -> value != null)
                .max(Short::compare)
                .orElse((short) 0);
        return (short) (max + 1);
    }

    private void applyDefaultStats(HopDongNhomUuTien entity) {
        if (entity.getKeHoach() == null) entity.setKeHoach(0);
        if (entity.getHoanThanh() == null) entity.setHoanThanh(0);
        if (entity.getConLai() == null) entity.setConLai(0);
        if (entity.getPhanTram() == null) entity.setPhanTram(BigDecimal.ZERO);
        if (entity.getSoNhan() == null) entity.setSoNhan(0);
    }
}
