package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LoaiHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LoaiHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LoaiHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LoaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.mapper.CauHinhMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LoaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LoaiHopDongService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoaiHopDongServiceImpl implements LoaiHopDongService {

    private final LoaiHopDongRepository loaiHopDongRepository;
    private final CauHinhMapper cauHinhMapper;

    @Override
    @Transactional(readOnly = true)
    public List<LoaiHopDongResponse> list(String search, Boolean activeOnly, boolean includeDeleted) {
        String keyword = EntityFilter.normalizeSearch(search);
        return loaiHopDongRepository.search(includeDeleted, activeOnly, keyword)
                .stream()
                .map(cauHinhMapper::toLoaiHopDongResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoaiHopDongResponse getById(UUID id) {
        return cauHinhMapper.toLoaiHopDongResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<UUID, LoaiHopDongResponse> getByIds(java.util.Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Map.of();
        }
        return loaiHopDongRepository.findByIdInAndNgayXoaIsNull(ids).stream()
                .collect(java.util.stream.Collectors.toMap(
                        LoaiHopDong::getId, cauHinhMapper::toLoaiHopDongResponse));
    }

    @Override
    @Transactional
    public LoaiHopDongResponse create(LoaiHopDongTaoRequest request) {
        LoaiHopDong entity = cauHinhMapper.fromLoaiHopDongTaoRequest(request);
        entity.setMa(EntityFilter.normalizeCode(request.getMa()));
        if (request.getTen() != null) entity.setTen(request.getTen().trim());
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        String normalizedCode = EntityFilter.normalizeCode(request.getMa());
        if (loaiHopDongRepository.existsByMaIgnoreCase(normalizedCode)) {
            throw new AppException(CauHinhErrorCode.LOAI_HOP_DONG_CODE_EXISTS, "Mã đã tồn tại");
        }
        entity.setMa(normalizedCode);
        return cauHinhMapper.toLoaiHopDongResponse(loaiHopDongRepository.save(entity));
    }

    @Override
    @Transactional
    public LoaiHopDongResponse update(UUID id, LoaiHopDongCapNhatRequest request) {
        LoaiHopDong entity = findById(id);
        cauHinhMapper.updateFromLoaiHopDongCapNhatRequest(request, entity);

        if (request.getMa() != null) {
            String normalizedCode = EntityFilter.normalizeCode(request.getMa());
            if (loaiHopDongRepository.existsByMaIgnoreCaseAndIdNot(normalizedCode, id)) {
                throw new AppException(CauHinhErrorCode.LOAI_HOP_DONG_CODE_EXISTS, "Mã đã tồn tại");
            }
            entity.setMa(normalizedCode);
        }
        return cauHinhMapper.toLoaiHopDongResponse(loaiHopDongRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        LoaiHopDong entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        loaiHopDongRepository.save(entity);
    }

    private LoaiHopDong findById(UUID id) {
        return loaiHopDongRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(CauHinhErrorCode.LOAI_HOP_DONG_NOT_FOUND, "Không tìm thấy bản ghi"));
    }
}
