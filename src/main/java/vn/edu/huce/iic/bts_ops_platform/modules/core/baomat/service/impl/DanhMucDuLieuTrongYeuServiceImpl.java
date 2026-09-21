package vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.dto.response.DanhMucDuLieuTrongYeuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.entity.DanhMucDuLieuTrongYeu;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.repository.DanhMucDuLieuTrongYeuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.service.DanhMucDuLieuTrongYeuService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DanhMucDuLieuTrongYeuServiceImpl implements DanhMucDuLieuTrongYeuService {

    private final DanhMucDuLieuTrongYeuRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<DanhMucDuLieuTrongYeuResponse> list(Boolean activeOnly) {
        return repository.findByNgayXoaIsNullOrderByMaAsc().stream()
                .filter(item -> EntityFilter.isActive(item, DanhMucDuLieuTrongYeu::getHoatDong, activeOnly))
                .map(this::toResponse)
                .toList();
    }

    private DanhMucDuLieuTrongYeuResponse toResponse(DanhMucDuLieuTrongYeu entity) {
        return DanhMucDuLieuTrongYeuResponse.builder()
                .id(entity.getId())
                .ma(entity.getMa())
                .ten(entity.getTen())
                .bangDuLieu(entity.getBangDuLieu())
                .cotDuLieu(entity.getCotDuLieu())
                .mucDo(entity.getMucDo())
                .chuSoHuu(entity.getChuSoHuu())
                .moTa(entity.getMoTa())
                .hoatDong(Boolean.TRUE.equals(entity.getHoatDong()))
                .build();
    }
}
