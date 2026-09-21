package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DanhMucBienBanCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DanhMucBienBanTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DanhMucBienBanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.DanhMucBienBan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.mapper.CauHinhMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.DanhMucBienBanRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.DanhMucBienBanService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DanhMucBienBanServiceImpl implements DanhMucBienBanService {

    private final DanhMucBienBanRepository danhMucBienBanRepository;
    private final CauHinhMapper cauHinhMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DanhMucBienBanResponse> list(String search, Boolean activeOnly, boolean includeDeleted) {
        String keyword = EntityFilter.normalizeSearch(search);
        return danhMucBienBanRepository.search(includeDeleted, activeOnly, keyword)
                .stream()
                .map(cauHinhMapper::toDanhMucBienBanResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DanhMucBienBanResponse getById(UUID id) {
        return cauHinhMapper.toDanhMucBienBanResponse(findById(id));
    }

    @Override
    @Transactional
    public DanhMucBienBanResponse create(DanhMucBienBanTaoRequest request) {
        DanhMucBienBan entity = cauHinhMapper.fromDanhMucBienBanTaoRequest(request);
        entity.setTen(request.getTen().trim());
        entity.setCoMauWord(request.getCoMauWord() != null && request.getCoMauWord());
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        String normalizedCode = EntityFilter.normalizeCode(request.getMa());
        if (danhMucBienBanRepository.existsByMaIgnoreCase(normalizedCode)) {
            throw new AppException(CauHinhErrorCode.DANH_MUC_BIEN_BAN_CODE_EXISTS, "Mã đã tồn tại");
        }
        entity.setMa(normalizedCode);
        return cauHinhMapper.toDanhMucBienBanResponse(danhMucBienBanRepository.save(entity));
    }

    @Override
    @Transactional
    public DanhMucBienBanResponse update(UUID id, DanhMucBienBanCapNhatRequest request) {
        DanhMucBienBan entity = findById(id);
        cauHinhMapper.updateFromDanhMucBienBanCapNhatRequest(request, entity);

        if (request.getMa() != null) {
            String normalizedCode = EntityFilter.normalizeCode(request.getMa());
            if (danhMucBienBanRepository.existsByMaIgnoreCaseAndIdNot(normalizedCode, id)) {
                throw new AppException(CauHinhErrorCode.DANH_MUC_BIEN_BAN_CODE_EXISTS, "Mã đã tồn tại");
            }
            entity.setMa(normalizedCode);
        }
        return cauHinhMapper.toDanhMucBienBanResponse(danhMucBienBanRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        DanhMucBienBan entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        danhMucBienBanRepository.save(entity);
    }

    private DanhMucBienBan findById(UUID id) {
        return danhMucBienBanRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(
                        CauHinhErrorCode.DANH_MUC_BIEN_BAN_NOT_FOUND, "Không tìm thấy danh mục biên bản"));
    }
}
