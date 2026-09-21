package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.TrangThaiHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.TrangThaiHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.TrangThaiHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.TrangThaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.mapper.CauHinhMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.TrangThaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.TrangThaiHopDongService;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrangThaiHopDongServiceImpl implements TrangThaiHopDongService {

    private final TrangThaiHopDongRepository trangThaiHopDongRepository;
    private final CauHinhMapper cauHinhMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TrangThaiHopDongResponse> list(String search, Boolean activeOnly, boolean includeDeleted) {
        String keyword = EntityFilter.normalizeSearch(search);
        return trangThaiHopDongRepository.search(includeDeleted, activeOnly, keyword)
                .stream()
                .map(cauHinhMapper::toTrangThaiHopDongResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TrangThaiHopDongResponse getById(UUID id) {
        return cauHinhMapper.toTrangThaiHopDongResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrangThaiHopDongResponse> getByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return trangThaiHopDongRepository.findAllById(ids).stream()
                .filter(entity -> entity.getNgayXoa() == null)
                .map(cauHinhMapper::toTrangThaiHopDongResponse)
                .toList();
    }

    @Override
    @Transactional
    public TrangThaiHopDongResponse create(TrangThaiHopDongTaoRequest request) {
        TrangThaiHopDong entity = cauHinhMapper.fromTrangThaiHopDongTaoRequest(request);
        if (request.getTen() != null) entity.setTen(request.getTen().trim());
        if (request.getMa() != null) entity.setMa(request.getMa().trim());
        if (request.getMauSac() != null) entity.setMauSac(request.getMauSac().trim());
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        return cauHinhMapper.toTrangThaiHopDongResponse(trangThaiHopDongRepository.save(entity));
    }

    @Override
    @Transactional
    public TrangThaiHopDongResponse update(UUID id, TrangThaiHopDongCapNhatRequest request) {
        TrangThaiHopDong entity = findById(id);
        cauHinhMapper.updateFromTrangThaiHopDongCapNhatRequest(request, entity);

        return cauHinhMapper.toTrangThaiHopDongResponse(trangThaiHopDongRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        TrangThaiHopDong entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        trangThaiHopDongRepository.save(entity);
    }

    private TrangThaiHopDong findById(UUID id) {
        return trangThaiHopDongRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(CauHinhErrorCode.TRANG_THAI_HOP_DONG_NOT_FOUND, "Không tìm thấy bản ghi"));
    }
}
