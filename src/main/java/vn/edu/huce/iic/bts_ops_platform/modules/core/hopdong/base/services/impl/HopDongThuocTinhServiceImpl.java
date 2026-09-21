package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LienKetBangGiaTriNormalizer;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongThuocTinhCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongThuocTinhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongThuocTinh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.mapper.HopDongMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongThuocTinhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongThuocTinhService;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HopDongThuocTinhServiceImpl implements HopDongThuocTinhService {

    private final HopDongThuocTinhRepository hopDongThuocTinhRepository;
    private final HopDongMapper hopDongMapper;
    private final LienKetBangGiaTriNormalizer lienKetBangGiaTriNormalizer;

    @Override
    @Transactional(readOnly = true)
    public List<HopDongThuocTinh> findActiveEntitiesByHopDongId(UUID hopDongId) {
        return hopDongThuocTinhRepository.findByHopDongIdAndNgayXoaIsNull(hopDongId);
    }

    @Override
    @Transactional
    public HopDongThuocTinh saveEntity(HopDongThuocTinh entity) {
        return hopDongThuocTinhRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> findActiveWithTenByHopDongIds(Collection<UUID> hopDongIds) {
        return hopDongThuocTinhRepository.findActiveWithTenByHopDongIds(hopDongIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongThuocTinhResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hopDongId) {
        String keyword = EntityFilter.normalizeSearch(search);
        List<HopDongThuocTinh> source = includeDeleted ? hopDongThuocTinhRepository.findAll() : hopDongThuocTinhRepository.findByNgayXoaIsNull();
        return source.stream()
                .filter(entity -> EntityFilter.isActive(entity, HopDongThuocTinh::getHoatDong, activeOnly))
                .filter(entity -> hopDongId == null || hopDongId.equals(entity.getHopDongId()))
                .filter(entity -> EntityFilter.matchesKeyword(keyword, EntityFilter.nullToEmpty(entity.getGiaTri())))
                .sorted(Comparator.comparing(HopDongThuocTinh::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(hopDongMapper::toHopDongThuocTinhResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HopDongThuocTinhResponse getById(UUID id) {
        return hopDongMapper.toHopDongThuocTinhResponse(findById(id));
    }

    @Override
    @Transactional
    public HopDongThuocTinhResponse create(HopDongThuocTinhTaoRequest request) {
        HopDongThuocTinh entity = hopDongMapper.fromHopDongThuocTinhTaoRequest(request);
        if (request.getGiaTri() != null) {
            entity.setGiaTri(lienKetBangGiaTriNormalizer.normalizeHopDongGiaTri(
                    request.getThuocTinhHopDongId(),
                    request.getGiaTri()));
        }
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        return hopDongMapper.toHopDongThuocTinhResponse(hopDongThuocTinhRepository.save(entity));
    }

    @Override
    @Transactional
    public HopDongThuocTinhResponse update(UUID id, HopDongThuocTinhCapNhatRequest request) {
        HopDongThuocTinh entity = findById(id);
        hopDongMapper.updateFromHopDongThuocTinhCapNhatRequest(request, entity);
        if (request.getGiaTri() != null) {
            UUID thuocTinhHopDongId = request.getThuocTinhHopDongId() != null
                    ? request.getThuocTinhHopDongId()
                    : entity.getThuocTinhHopDongId();
            entity.setGiaTri(lienKetBangGiaTriNormalizer.normalizeHopDongGiaTri(
                    thuocTinhHopDongId,
                    request.getGiaTri()));
        }

        return hopDongMapper.toHopDongThuocTinhResponse(hopDongThuocTinhRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        HopDongThuocTinh entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        hopDongThuocTinhRepository.save(entity);
    }

    private HopDongThuocTinh findById(UUID id) {
        return hopDongThuocTinhRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_THUOC_TINH_NOT_FOUND, "Không tìm thấy bản ghi"));
    }
}
