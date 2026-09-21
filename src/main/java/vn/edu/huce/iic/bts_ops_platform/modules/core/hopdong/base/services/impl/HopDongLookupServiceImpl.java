package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.mapper.HopDongMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongLookupService;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HopDongLookupServiceImpl implements HopDongLookupService {

    // HopDongLookupService and HopDongService are two facades over the same HopDong
    // aggregate root (not separate domains), so reading the repository directly here
    // is the intended exception. It also avoids a circular service dependency, since
    // HopDongService transitively depends on this service (via HangMucExcelImportService).
    private final HopDongRepository hopDongRepository;
    private final HopDongMapper hopDongMapper;

    @Override
    @Transactional(readOnly = true)
    public HopDongResponse getById(UUID id) {
        return hopDongMapper.toHopDongResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongResponse> listActive() {
        return hopDongRepository.findByNgayXoaIsNull().stream()
                .filter(entity -> Boolean.TRUE.equals(entity.getHoatDong()))
                .sorted(Comparator.comparing(HopDong::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(hopDongMapper::toHopDongResponse)
                .toList();
    }

    private HopDong findById(UUID id) {
        return hopDongRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_NOT_FOUND, "Không tìm thấy bản ghi"));
    }
}
