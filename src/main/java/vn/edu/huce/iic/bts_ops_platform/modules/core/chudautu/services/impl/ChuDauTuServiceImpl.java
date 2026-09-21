package vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.dto.request.ChuDauTuCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.dto.request.ChuDauTuTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.dto.response.ChuDauTuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.entity.ChuDauTu;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.exception.ChuDauTuErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.mapper.ChuDauTuMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.repository.ChuDauTuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.services.ChuDauTuService;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChuDauTuServiceImpl implements ChuDauTuService {

    private final ChuDauTuRepository chuDauTuRepository;
    private final ChuDauTuMapper chuDauTuMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ChuDauTuResponse> list(String search, Boolean activeOnly, boolean includeDeleted) {
        String keyword = EntityFilter.normalizeSearch(search);
        List<ChuDauTu> source = includeDeleted ? chuDauTuRepository.findAll() : chuDauTuRepository.findByNgayXoaIsNull();
        return source.stream()
                .filter(entity -> EntityFilter.isActive(entity, ChuDauTu::getHoatDong, activeOnly))
                .filter(entity -> EntityFilter.matchesKeyword(keyword, EntityFilter.nullToEmpty(entity.getMa()), EntityFilter.nullToEmpty(entity.getTen()), EntityFilter.nullToEmpty(entity.getMoTa())))
                .sorted(Comparator.comparing(ChuDauTu::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(chuDauTuMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ChuDauTuResponse getById(UUID id) {
        return chuDauTuMapper.toResponse(findById(id));
    }

    @Override
    @Transactional
    public ChuDauTuResponse create(ChuDauTuTaoRequest request) {
        ChuDauTu entity = chuDauTuMapper.fromTaoRequest(request);
        entity.setMa(EntityFilter.normalizeCode(request.getMa()));
        if (request.getTen() != null) entity.setTen(request.getTen().trim());
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        String normalizedCode = EntityFilter.normalizeCode(request.getMa());
        if (chuDauTuRepository.existsByMaIgnoreCase(normalizedCode)) {
            throw new AppException(ChuDauTuErrorCode.CHU_DAU_TU_CODE_EXISTS, "Mã đã tồn tại");
        }
        entity.setMa(normalizedCode);
        return chuDauTuMapper.toResponse(chuDauTuRepository.save(entity));
    }

    @Override
    @Transactional
    public ChuDauTuResponse update(UUID id, ChuDauTuCapNhatRequest request) {
        ChuDauTu entity = findById(id);
        chuDauTuMapper.updateFromCapNhatRequest(request, entity);

        if (request.getMa() != null) {
            String normalizedCode = EntityFilter.normalizeCode(request.getMa());
            if (chuDauTuRepository.existsByMaIgnoreCaseAndIdNot(normalizedCode, id)) {
                throw new AppException(ChuDauTuErrorCode.CHU_DAU_TU_CODE_EXISTS, "Mã đã tồn tại");
            }
            entity.setMa(normalizedCode);
        }
        return chuDauTuMapper.toResponse(chuDauTuRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ChuDauTu entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        chuDauTuRepository.save(entity);
    }

    private ChuDauTu findById(UUID id) {
        return chuDauTuRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(ChuDauTuErrorCode.CHU_DAU_TU_NOT_FOUND, "Không tìm thấy bản ghi"));
    }
}
