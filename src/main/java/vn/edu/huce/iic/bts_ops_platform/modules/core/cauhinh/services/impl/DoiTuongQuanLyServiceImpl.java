package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DoiTuongQuanLyCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DoiTuongQuanLyTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DoiTuongQuanLyResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.DoiTuongHopDongLienKet;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.DoiTuongQuanLy;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.mapper.CauHinhMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.DoiTuongHopDongLienKetRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.DoiTuongQuanLyRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.DoiTuongQuanLyService;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoiTuongQuanLyServiceImpl implements DoiTuongQuanLyService {

    private final DoiTuongQuanLyRepository doiTuongQuanLyRepository;
    private final DoiTuongHopDongLienKetRepository doiTuongHopDongLienKetRepository;
    private final KieuHopDongRepository kieuHopDongRepository;
    private final CauHinhMapper cauHinhMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DoiTuongQuanLyResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID loaiHopDongId, UUID kieuHopDongId, Boolean selectorOnly) {
        String keyword = EntityFilter.normalizeSearch(search);
        Set<UUID> allowedIds = resolveAllowedDoiTuongIds(loaiHopDongId, kieuHopDongId);
        if (allowedIds != null && allowedIds.isEmpty()) {
            return List.of();
        }
        boolean filterByIds = allowedIds != null;
        return doiTuongQuanLyRepository.search(
                        includeDeleted,
                        activeOnly,
                        selectorOnly,
                        filterByIds,
                        filterByIds ? allowedIds : List.of(UUID.randomUUID()),
                        keyword)
                .stream()
                .map(cauHinhMapper::toDoiTuongQuanLyResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DoiTuongQuanLyResponse getById(UUID id) {
        return cauHinhMapper.toDoiTuongQuanLyResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoiTuongQuanLyResponse> getByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return doiTuongQuanLyRepository.findAllById(ids).stream()
                .filter(entity -> entity.getNgayXoa() == null)
                .map(cauHinhMapper::toDoiTuongQuanLyResponse)
                .toList();
    }

    @Override
    @Transactional
    public DoiTuongQuanLyResponse create(DoiTuongQuanLyTaoRequest request) {
        DoiTuongQuanLy entity = cauHinhMapper.fromDoiTuongQuanLyTaoRequest(request);
        entity.setMa(EntityFilter.normalizeCode(request.getMa()));
        if (request.getTen() != null) entity.setTen(request.getTen().trim());
        if (request.getBieuTuong() != null) entity.setBieuTuong(request.getBieuTuong().trim());
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());
        entity.setHienThiTrenGiaoDien(request.getHienThiTrenGiaoDien() == null || request.getHienThiTrenGiaoDien());

        String normalizedCode = EntityFilter.normalizeCode(request.getMa());
        if (doiTuongQuanLyRepository.existsByMaIgnoreCase(normalizedCode)) {
            throw new AppException(CauHinhErrorCode.DOI_TUONG_QUAN_LY_CODE_EXISTS, "Mã đã tồn tại");
        }
        entity.setMa(normalizedCode);
        return cauHinhMapper.toDoiTuongQuanLyResponse(doiTuongQuanLyRepository.save(entity));
    }

    @Override
    @Transactional
    public DoiTuongQuanLyResponse update(UUID id, DoiTuongQuanLyCapNhatRequest request) {
        DoiTuongQuanLy entity = findById(id);
        cauHinhMapper.updateFromDoiTuongQuanLyCapNhatRequest(request, entity);
        if (request.getHienThiTrenGiaoDien() != null) {
            entity.setHienThiTrenGiaoDien(request.getHienThiTrenGiaoDien());
        }

        if (request.getMa() != null) {
            String normalizedCode = EntityFilter.normalizeCode(request.getMa());
            if (doiTuongQuanLyRepository.existsByMaIgnoreCaseAndIdNot(normalizedCode, id)) {
                throw new AppException(CauHinhErrorCode.DOI_TUONG_QUAN_LY_CODE_EXISTS, "Mã đã tồn tại");
            }
            entity.setMa(normalizedCode);
        }
        return cauHinhMapper.toDoiTuongQuanLyResponse(doiTuongQuanLyRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        DoiTuongQuanLy entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        doiTuongQuanLyRepository.save(entity);
    }

    private DoiTuongQuanLy findById(UUID id) {
        return doiTuongQuanLyRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(CauHinhErrorCode.DOI_TUONG_QUAN_LY_NOT_FOUND, "Không tìm thấy bản ghi"));
    }

    private Set<UUID> resolveAllowedDoiTuongIds(UUID loaiHopDongId, UUID kieuHopDongId) {
        if (loaiHopDongId == null && kieuHopDongId == null) {
            return null;
        }
        Set<UUID> ids = new HashSet<>();
        if (kieuHopDongId != null) {
            UUID resolvedLoaiId = loaiHopDongId;
            if (resolvedLoaiId == null) {
                resolvedLoaiId = kieuHopDongRepository.findByIdAndNgayXoaIsNull(kieuHopDongId)
                        .map(KieuHopDong::getLoaiHopDongId)
                        .orElse(null);
            }
            if (resolvedLoaiId != null) {
                for (DoiTuongHopDongLienKet link : doiTuongHopDongLienKetRepository.findEffectiveForKieu(resolvedLoaiId, kieuHopDongId)) {
                    ids.add(link.getDoiTuongQuanLyId());
                }
            }
            return ids;
        }
        for (DoiTuongHopDongLienKet link : doiTuongHopDongLienKetRepository.findByLoaiHopDongIdAndKieuHopDongIdIsNullAndNgayXoaIsNull(loaiHopDongId)) {
            ids.add(link.getDoiTuongQuanLyId());
        }
        for (KieuHopDong kieu : kieuHopDongRepository.findByLoaiHopDongIdAndNgayXoaIsNull(loaiHopDongId)) {
            for (DoiTuongHopDongLienKet link : doiTuongHopDongLienKetRepository.findByKieuHopDongIdAndNgayXoaIsNull(kieu.getId())) {
                ids.add(link.getDoiTuongQuanLyId());
            }
        }
        return ids;
    }
}
