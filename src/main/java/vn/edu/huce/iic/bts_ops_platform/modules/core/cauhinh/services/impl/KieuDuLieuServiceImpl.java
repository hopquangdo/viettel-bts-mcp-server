package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuDuLieuCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuDuLieuTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.KieuDuLieuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuDuLieu;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.mapper.CauHinhMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuDuLieuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.KieuDuLieuService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KieuDuLieuServiceImpl implements KieuDuLieuService {

    private final KieuDuLieuRepository kieuDuLieuRepository;
    private final CauHinhMapper cauHinhMapper;

    @Override
    @Transactional(readOnly = true)
    public List<KieuDuLieuResponse> list(String search, Boolean activeOnly, boolean includeDeleted) {
        String keyword = EntityFilter.normalizeSearch(search);
        return kieuDuLieuRepository.search(includeDeleted, activeOnly, keyword)
                .stream()
                .map(cauHinhMapper::toKieuDuLieuResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public KieuDuLieuResponse getById(UUID id) {
        return cauHinhMapper.toKieuDuLieuResponse(findById(id));
    }

    @Override
    @Transactional
    public KieuDuLieuResponse create(KieuDuLieuTaoRequest request) {
        KieuDuLieu entity = cauHinhMapper.fromKieuDuLieuTaoRequest(request);
        if (request.getTen() != null) entity.setTen(request.getTen().trim());
        if (request.getLienKetBang() != null) entity.setLienKetBang(request.getLienKetBang().trim());
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        return cauHinhMapper.toKieuDuLieuResponse(kieuDuLieuRepository.save(entity));
    }

    @Override
    @Transactional
    public KieuDuLieuResponse update(UUID id, KieuDuLieuCapNhatRequest request) {
        KieuDuLieu entity = findById(id);
        cauHinhMapper.updateFromKieuDuLieuCapNhatRequest(request, entity);

        return cauHinhMapper.toKieuDuLieuResponse(kieuDuLieuRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        KieuDuLieu entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        kieuDuLieuRepository.save(entity);
    }

    private KieuDuLieu findById(UUID id) {
        return kieuDuLieuRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(CauHinhErrorCode.KIEU_DU_LIEU_NOT_FOUND, "Không tìm thấy bản ghi"));
    }
}
