package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongTepDinhKemCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongTepDinhKemTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongTepDinhKemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongTepDinhKem;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.mapper.HopDongMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongTepDinhKemRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongTepDinhKemService;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HopDongTepDinhKemServiceImpl implements HopDongTepDinhKemService {

    private final HopDongTepDinhKemRepository hopDongTepDinhKemRepository;
    private final HopDongMapper hopDongMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HopDongTepDinhKem> findActiveEntitiesByHopDongId(UUID hopDongId) {
        return hopDongTepDinhKemRepository.findByHopDongIdAndNgayXoaIsNull(hopDongId);
    }

    @Override
    @Transactional
    public HopDongTepDinhKem saveEntity(HopDongTepDinhKem entity) {
        return hopDongTepDinhKemRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongTepDinhKemResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hopDongId) {
        String keyword = EntityFilter.normalizeSearch(search);
        List<HopDongTepDinhKem> source = includeDeleted ? hopDongTepDinhKemRepository.findAll() : hopDongTepDinhKemRepository.findByNgayXoaIsNull();
        return source.stream()
                .filter(entity -> EntityFilter.isActive(entity, HopDongTepDinhKem::getHoatDong, activeOnly))
                .filter(entity -> hopDongId == null || hopDongId.equals(entity.getHopDongId()))
                .filter(entity -> EntityFilter.matchesKeyword(keyword, EntityFilter.nullToEmpty(entity.getLoaiTaiLieu()), EntityFilter.nullToEmpty(entity.getGhiChu())))
                .sorted(Comparator.comparing(HopDongTepDinhKem::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(hopDongMapper::toHopDongTepDinhKemResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HopDongTepDinhKemResponse getById(UUID id) {
        return hopDongMapper.toHopDongTepDinhKemResponse(findById(id));
    }

    @Override
    @Transactional
    public HopDongTepDinhKemResponse create(HopDongTepDinhKemTaoRequest request) {
        HopDongTepDinhKem entity = hopDongMapper.fromHopDongTepDinhKemTaoRequest(request);
        if (request.getLoaiTaiLieu() != null) entity.setLoaiTaiLieu(request.getLoaiTaiLieu().trim());
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        return hopDongMapper.toHopDongTepDinhKemResponse(hopDongTepDinhKemRepository.save(entity));
    }

    @Override
    @Transactional
    public HopDongTepDinhKemResponse update(UUID id, HopDongTepDinhKemCapNhatRequest request) {
        HopDongTepDinhKem entity = findById(id);
        hopDongMapper.updateFromHopDongTepDinhKemCapNhatRequest(request, entity);

        return hopDongMapper.toHopDongTepDinhKemResponse(hopDongTepDinhKemRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        HopDongTepDinhKem entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        hopDongTepDinhKemRepository.save(entity);
    }

    private HopDongTepDinhKem findById(UUID id) {
        return hopDongTepDinhKemRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_TEP_DINH_KEM_NOT_FOUND, "Không tìm thấy bản ghi"));
    }
}
