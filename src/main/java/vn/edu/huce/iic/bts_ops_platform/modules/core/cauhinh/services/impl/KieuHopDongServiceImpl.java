package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.KieuHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.mapper.CauHinhMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.KieuHopDongService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KieuHopDongServiceImpl implements KieuHopDongService {

    private final KieuHopDongRepository kieuHopDongRepository;
    private final CauHinhMapper cauHinhMapper;

    @Override
    @Transactional(readOnly = true)
    public List<KieuHopDongResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID loaiHopDongId) {
        String keyword = EntityFilter.normalizeSearch(search);
        return kieuHopDongRepository.search(includeDeleted, activeOnly, loaiHopDongId, keyword)
                .stream()
                .map(cauHinhMapper::toKieuHopDongResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public KieuHopDongResponse getById(UUID id) {
        return cauHinhMapper.toKieuHopDongResponse(findById(id));
    }

    @Override
    @Transactional
    public KieuHopDongResponse create(KieuHopDongTaoRequest request) {
        KieuHopDong entity = cauHinhMapper.fromKieuHopDongTaoRequest(request);
        entity.setMa(EntityFilter.normalizeCode(request.getMa()));
        if (request.getTen() != null) entity.setTen(request.getTen().trim());
        if (request.getNhom() != null) entity.setNhom(request.getNhom().trim());
        if (request.getMauSac() != null) entity.setMauSac(request.getMauSac().trim());
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        String normalizedCode = EntityFilter.normalizeCode(request.getMa());
        if (kieuHopDongRepository.existsByMaIgnoreCase(normalizedCode)) {
            throw new AppException(CauHinhErrorCode.KIEU_HOP_DONG_CODE_EXISTS, "Mã đã tồn tại");
        }
        entity.setMa(normalizedCode);
        return cauHinhMapper.toKieuHopDongResponse(kieuHopDongRepository.save(entity));
    }

    @Override
    @Transactional
    public KieuHopDongResponse update(UUID id, KieuHopDongCapNhatRequest request) {
        KieuHopDong entity = findById(id);
        cauHinhMapper.updateFromKieuHopDongCapNhatRequest(request, entity);

        if (request.getMa() != null) {
            String normalizedCode = EntityFilter.normalizeCode(request.getMa());
            if (kieuHopDongRepository.existsByMaIgnoreCaseAndIdNot(normalizedCode, id)) {
                throw new AppException(CauHinhErrorCode.KIEU_HOP_DONG_CODE_EXISTS, "Mã đã tồn tại");
            }
            entity.setMa(normalizedCode);
        }
        return cauHinhMapper.toKieuHopDongResponse(kieuHopDongRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        KieuHopDong entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        kieuHopDongRepository.save(entity);
    }

    private KieuHopDong findById(UUID id) {
        return kieuHopDongRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(CauHinhErrorCode.KIEU_HOP_DONG_NOT_FOUND, "Không tìm thấy bản ghi"));
    }
}
