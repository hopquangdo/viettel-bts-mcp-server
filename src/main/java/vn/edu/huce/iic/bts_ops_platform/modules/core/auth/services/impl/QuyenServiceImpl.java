package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response.QuyenResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.Quyen;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.exception.PhanQuyenErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.mapper.PhanQuyenMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository.QuyenRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.QuyenService;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuyenServiceImpl implements QuyenService {

    private final QuyenRepository quyenRepository;
    private final PhanQuyenMapper phanQuyenMapper;

    @Override
    @Transactional(readOnly = true)
    public List<QuyenResponse> list(String search, Boolean activeOnly, boolean includeDeleted) {
        String keyword = EntityFilter.normalizeSearch(search);
        List<Quyen> source = includeDeleted ? quyenRepository.findAll() : quyenRepository.findByNgayXoaIsNull();
        return source.stream()
                .filter(entity -> EntityFilter.isActive(entity, Quyen::getHoatDong, activeOnly))
                .filter(entity -> EntityFilter.matchesKeyword(keyword, EntityFilter.nullToEmpty(entity.getMa()), EntityFilter.nullToEmpty(entity.getTen())))
                .sorted(Comparator.comparing(Quyen::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(phanQuyenMapper::toQuyenResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuyenResponse getById(UUID id) {
        return phanQuyenMapper.toQuyenResponse(findById(id));
    }

    @Override
    @Transactional
    public QuyenResponse create(QuyenTaoRequest request) {
        Quyen entity = phanQuyenMapper.fromQuyenTaoRequest(request);
        entity.setMa(EntityFilter.normalizeCode(request.getMa()));
        if (request.getTen() != null) entity.setTen(request.getTen().trim());
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        String normalizedCode = EntityFilter.normalizeCode(request.getMa());
        if (quyenRepository.existsByMaIgnoreCase(normalizedCode)) {
            throw new AppException(PhanQuyenErrorCode.QUYEN_CODE_EXISTS, "Mã đã tồn tại");
        }
        entity.setMa(normalizedCode);
        return phanQuyenMapper.toQuyenResponse(quyenRepository.save(entity));
    }

    @Override
    @Transactional
    public QuyenResponse update(UUID id, QuyenCapNhatRequest request) {
        Quyen entity = findById(id);
        phanQuyenMapper.updateFromQuyenCapNhatRequest(request, entity);

        if (request.getMa() != null) {
            String normalizedCode = EntityFilter.normalizeCode(request.getMa());
            if (quyenRepository.existsByMaIgnoreCaseAndIdNot(normalizedCode, id)) {
                throw new AppException(PhanQuyenErrorCode.QUYEN_CODE_EXISTS, "Mã đã tồn tại");
            }
            entity.setMa(normalizedCode);
        }
        return phanQuyenMapper.toQuyenResponse(quyenRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Quyen entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        quyenRepository.save(entity);
    }

    private Quyen findById(UUID id) {
        return quyenRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(PhanQuyenErrorCode.QUYEN_NOT_FOUND, "Không tìm thấy bản ghi"));
    }
}
