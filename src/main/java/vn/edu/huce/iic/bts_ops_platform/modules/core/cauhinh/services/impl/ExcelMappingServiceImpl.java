package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ExcelMappingCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ExcelMappingDongBoItemRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ExcelMappingDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ExcelMappingTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ExcelMappingCotResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ExcelMappingResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.DoiTuongQuanLy;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ExcelMapping;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ExcelMappingCot;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ThuocTinh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.mapper.ExcelMappingMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.DoiTuongQuanLyRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.ExcelMappingCotRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.ExcelMappingRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuDuLieuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.ThuocTinhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ExcelMappingService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.LienKetBangSupport;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelMappingServiceImpl implements ExcelMappingService {

    private final ExcelMappingRepository excelMappingRepository;
    private final ExcelMappingCotRepository excelMappingCotRepository;
    private final DoiTuongQuanLyRepository doiTuongQuanLyRepository;
    private final ThuocTinhRepository thuocTinhRepository;
    private final KieuDuLieuRepository kieuDuLieuRepository;
    private final ExcelMappingMapper excelMappingMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ExcelMappingResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID doiTuongQuanLyId) {
        String keyword = EntityFilter.normalizeSearch(search);
        List<ExcelMapping> filtered = excelMappingRepository.search(includeDeleted, activeOnly, doiTuongQuanLyId, keyword);
        Map<UUID, DoiTuongQuanLy> doiTuongMap = loadDoiTuongMap(filtered);
        return filtered.stream()
                .map(entity -> toResponse(entity, doiTuongMap))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExcelMappingResponse getById(UUID id) {
        ExcelMapping entity = findById(id);
        Map<UUID, DoiTuongQuanLy> doiTuongMap = loadDoiTuongMap(List.of(entity));
        return toResponse(entity, doiTuongMap);
    }

    @Override
    @Transactional
    public ExcelMappingResponse create(ExcelMappingTaoRequest request) {
        validateDoiTuongQuanLy(request.getDoiTuongQuanLyId());
        ExcelMapping entity = excelMappingMapper.fromTaoRequest(request);
        normalize(entity);
        normalizeParseRows(entity);
        entity.setNguoiCapNhat(currentUserId());
        if (entity.getHoatDong() == null) {
            entity.setHoatDong(true);
        }
        return toResponse(excelMappingRepository.save(entity));
    }

    @Override
    @Transactional
    public ExcelMappingResponse update(UUID id, ExcelMappingCapNhatRequest request) {
        ExcelMapping entity = findById(id);
        validateDoiTuongQuanLy(request.getDoiTuongQuanLyId());
        excelMappingMapper.updateFromCapNhatRequest(request, entity);
        if (request.getDongBatDauDoc() != null && request.getDongBatDauDoc() > 0) {
            entity.setDongBatDauDoc(request.getDongBatDauDoc());
        }
        normalize(entity);
        normalizeParseRows(entity);
        entity.setNguoiCapNhat(currentUserId());
        if (request.getHoatDong() == null) {
            entity.setHoatDong(Boolean.TRUE.equals(entity.getHoatDong()));
        }
        return toResponse(excelMappingRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ExcelMapping entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        entity.setNguoiCapNhat(currentUserId());
        excelMappingRepository.save(entity);
        excelMappingCotRepository.findByExcelMappingIdAndNgayXoaIsNullOrderByThuTuAsc(id).forEach(cot -> {
            cot.setNgayXoa(Instant.now());
            excelMappingCotRepository.save(cot);
        });
    }

    @Override
    @Transactional
    public List<ExcelMappingCotResponse> dongBo(UUID id, ExcelMappingDongBoRequest request) {
        findById(id);
        List<ExcelMappingDongBoItemRequest> desired = request.getCotDanhSach() == null
                ? List.of()
                : request.getCotDanhSach();

        Map<UUID, ThuocTinh> thuocTinhMap = loadThuocTinhMap(desired.stream()
                .map(ExcelMappingDongBoItemRequest::getThuocTinhId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        Map<UUID, ExcelMappingCot> currentByThuocTinh = excelMappingCotRepository
                .findByExcelMappingIdAndNgayXoaIsNullOrderByThuTuAsc(id)
                .stream()
                .filter(item -> item.getThuocTinhId() != null)
                .collect(Collectors.toMap(ExcelMappingCot::getThuocTinhId, item -> item, (a, b) -> a, LinkedHashMap::new));

        excelMappingCotRepository.findByExcelMappingIdAndNgayXoaIsNullOrderByThuTuAsc(id).stream()
                .filter(item -> item.getThuocTinhId() == null)
                .forEach(item -> {
                    item.setNgayXoa(Instant.now());
                    excelMappingCotRepository.save(item);
                });

        Set<UUID> desiredIds = desired.stream()
                .map(ExcelMappingDongBoItemRequest::getThuocTinhId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        currentByThuocTinh.values().stream()
                .filter(item -> !desiredIds.contains(item.getThuocTinhId()))
                .forEach(item -> {
                    item.setNgayXoa(Instant.now());
                    excelMappingCotRepository.save(item);
                });

        short order = 0;
        for (ExcelMappingDongBoItemRequest itemRequest : desired) {
            if (itemRequest.getThuocTinhId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu thuộc tính mapping");
            }
            validateThuocTinhExists(itemRequest.getThuocTinhId(), thuocTinhMap);
            ExcelMappingCot entity = currentByThuocTinh.get(itemRequest.getThuocTinhId());
            if (entity == null) {
                entity = new ExcelMappingCot();
                entity.setExcelMappingId(id);
                entity.setThuocTinhId(itemRequest.getThuocTinhId());
            }
            entity.setCotExcel(EntityFilter.trimToNull(itemRequest.getCotExcel()));
            entity.setBatBuoc(itemRequest.getBatBuoc() != null && itemRequest.getBatBuoc());
            entity.setThuTu(itemRequest.getThuTu() != null ? itemRequest.getThuTu() : order);
            excelMappingCotRepository.save(entity);
            order++;
        }

        return loadCotResponses(id);
    }

    private ExcelMappingResponse toResponse(ExcelMapping entity, Map<UUID, DoiTuongQuanLy> doiTuongMap) {
        ExcelMappingResponse response = excelMappingMapper.toResponse(entity);
        response.setCotDanhSach(loadCotResponses(entity.getId()));
        DoiTuongQuanLy doiTuong = doiTuongMap.get(entity.getDoiTuongQuanLyId());
        if (doiTuong != null) {
            response.setTenDoiTuongQuanLy(doiTuong.getTen());
            response.setMaDoiTuongQuanLy(doiTuong.getMa());
        }
        return response;
    }

    private ExcelMappingResponse toResponse(ExcelMapping entity) {
        return toResponse(entity, loadDoiTuongMap(List.of(entity)));
    }

    private Map<UUID, DoiTuongQuanLy> loadDoiTuongMap(List<ExcelMapping> entities) {
        Set<UUID> ids = entities.stream()
                .map(ExcelMapping::getDoiTuongQuanLyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return doiTuongQuanLyRepository.findAllById(ids).stream()
                .filter(item -> item.getNgayXoa() == null)
                .collect(Collectors.toMap(DoiTuongQuanLy::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private List<ExcelMappingCotResponse> loadCotResponses(UUID excelMappingId) {
        List<ExcelMappingCot> source = excelMappingCotRepository.findByExcelMappingIdAndNgayXoaIsNullOrderByThuTuAsc(excelMappingId);
        Map<UUID, ThuocTinhResponse> thuocTinhMap = loadThuocTinhResponses(source.stream()
                .map(ExcelMappingCot::getThuocTinhId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        return source.stream()
                .map(item -> {
                    ExcelMappingCotResponse response = excelMappingMapper.toCotResponse(item);
                    if (item.getThuocTinhId() != null) {
                        response.setThuocTinh(thuocTinhMap.get(item.getThuocTinhId()));
                    }
                    return response;
                })
                .toList();
    }

    private Map<UUID, ThuocTinhResponse> loadThuocTinhResponses(Set<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return thuocTinhRepository.findAllById(ids).stream()
                .filter(item -> item.getNgayXoa() == null)
                .collect(Collectors.toMap(ThuocTinh::getId, this::toThuocTinhResponse, (a, b) -> a, LinkedHashMap::new));
    }

    private Map<UUID, ThuocTinh> loadThuocTinhMap(Set<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return thuocTinhRepository.findAllById(ids).stream()
                .filter(item -> item.getNgayXoa() == null)
                .collect(Collectors.toMap(ThuocTinh::getId, item -> item, (a, b) -> a, LinkedHashMap::new));
    }

    private ThuocTinhResponse toThuocTinhResponse(ThuocTinh entity) {
        ThuocTinhResponse response = new ThuocTinhResponse();
        response.setId(entity.getId());
        response.setDoiTuongQuanLyId(entity.getDoiTuongQuanLyId());
        response.setTen(entity.getTen());
        response.setKieuDuLieuId(entity.getKieuDuLieuId());
        response.setLienKetBang(
                LienKetBangSupport.resolveLienKetBang(entity.getKieuDuLieuId(), kieuDuLieuRepository));
        response.setLaKhoaChinh(entity.getLaKhoaChinh());
        response.setBatBuoc(entity.getBatBuoc());
        response.setDonVi(entity.getDonVi());
        response.setHoatDong(entity.getHoatDong());
        response.setNgayTao(entity.getNgayTao());
        response.setNgayCapNhat(entity.getNgayCapNhat());
        return response;
    }

    private void validateThuocTinhExists(UUID thuocTinhId, Map<UUID, ThuocTinh> map) {
        if (!map.containsKey(thuocTinhId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy thuộc tính: " + thuocTinhId);
        }
    }

    private void validateDoiTuongQuanLy(UUID doiTuongQuanLyId) {
        if (doiTuongQuanLyId == null) {
            return;
        }
        doiTuongQuanLyRepository.findByIdAndNgayXoaIsNull(doiTuongQuanLyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy đối tượng quản lý"));
    }

    private ExcelMapping findById(UUID id) {
        return excelMappingRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi"));
    }

    private void normalize(ExcelMapping entity) {
        entity.setTen(EntityFilter.trimToNull(entity.getTen()));
        entity.setMa(EntityFilter.trimToNull(entity.getMa()));
        entity.setMoTa(EntityFilter.trimToNull(entity.getMoTa()));
        entity.setCotNhomUuTien(normalizeExcelColumn(entity.getCotNhomUuTien()));
    }

    private void normalizeParseRows(ExcelMapping entity) {
        short dataStartRow = entity.getDongBatDauDoc() == null || entity.getDongBatDauDoc() < 1
                ? 2
                : entity.getDongBatDauDoc();
        entity.setDongBatDauDoc(dataStartRow);
        entity.setDongTieuDe((short) 1);
    }

    private String normalizeExcelColumn(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toUpperCase();
    }

    private UUID currentUserId() {
        JwtUserPrincipal principal = SecurityContextHelper.currentUserOrNull();
        return principal != null ? principal.id() : null;
    }
}
