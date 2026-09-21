package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenHanCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenHanTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response.QuyenHanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.QuyenHan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.exception.PhanQuyenErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.mapper.PhanQuyenMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository.QuyenHanRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.QuyenHanService;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuyenHanServiceImpl implements QuyenHanService {

    private final QuyenHanRepository quyenHanRepository;
    private final PhanQuyenMapper phanQuyenMapper;

    @Override
    @Transactional(readOnly = true)
    public List<QuyenHanResponse> list(String search, Boolean activeOnly, boolean includeDeleted) {
        String keyword = EntityFilter.normalizeSearch(search);
        List<QuyenHan> source = includeDeleted ? quyenHanRepository.findAll() : quyenHanRepository.findByNgayXoaIsNull();
        return source.stream()
                .filter(entity -> EntityFilter.isActive(entity, QuyenHan::getHoatDong, activeOnly))
                .filter(entity -> EntityFilter.matchesKeyword(keyword, EntityFilter.nullToEmpty(entity.getMa()), EntityFilter.nullToEmpty(entity.getTen())))
                .sorted(Comparator.comparing(QuyenHan::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(phanQuyenMapper::toQuyenHanResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuyenHanResponse getById(UUID id) {
        return phanQuyenMapper.toQuyenHanResponse(findById(id));
    }

    @Override
    @Transactional
    public QuyenHanResponse create(QuyenHanTaoRequest request) {
        QuyenHan entity = phanQuyenMapper.fromQuyenHanTaoRequest(request);
        entity.setMa(EntityFilter.normalizeCode(request.getMa()));
        if (request.getTen() != null) entity.setTen(request.getTen().trim());
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        String normalizedCode = EntityFilter.normalizeCode(request.getMa());
        if (quyenHanRepository.existsByMaIgnoreCase(normalizedCode)) {
            throw new AppException(PhanQuyenErrorCode.QUYEN_HAN_CODE_EXISTS, "Mã đã tồn tại");
        }
        entity.setMa(normalizedCode);
        return phanQuyenMapper.toQuyenHanResponse(quyenHanRepository.save(entity));
    }

    @Override
    @Transactional
    public QuyenHanResponse update(UUID id, QuyenHanCapNhatRequest request) {
        QuyenHan entity = findById(id);
        phanQuyenMapper.updateFromQuyenHanCapNhatRequest(request, entity);

        if (request.getMa() != null) {
            String normalizedCode = EntityFilter.normalizeCode(request.getMa());
            if (quyenHanRepository.existsByMaIgnoreCaseAndIdNot(normalizedCode, id)) {
                throw new AppException(PhanQuyenErrorCode.QUYEN_HAN_CODE_EXISTS, "Mã đã tồn tại");
            }
            entity.setMa(normalizedCode);
        }
        return phanQuyenMapper.toQuyenHanResponse(quyenHanRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        QuyenHan entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        quyenHanRepository.save(entity);
    }

    private QuyenHan findById(UUID id) {
        return quyenHanRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(PhanQuyenErrorCode.QUYEN_HAN_NOT_FOUND, "Không tìm thấy bản ghi"));
    }
}
