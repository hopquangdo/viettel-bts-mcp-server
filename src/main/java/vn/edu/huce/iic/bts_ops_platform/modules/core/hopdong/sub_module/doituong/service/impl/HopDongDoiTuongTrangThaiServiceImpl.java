package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DoiTuongHopDongLienKetResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.TrangThaiHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.DoiTuongHopDongLienKetService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.KieuHopDongLuongTrangThaiService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LuongTrangThaiService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.TrangThaiHopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTrangThaiCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTrangThaiTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongTrangThaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuongTrangThai;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.HopDongDanhSachTienDoHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.mapper.HopDongMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongTrangThaiRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongTrangThaiService;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HopDongDoiTuongTrangThaiServiceImpl implements HopDongDoiTuongTrangThaiService {

    private final HopDongDoiTuongTrangThaiRepository hopDongDoiTuongTrangThaiRepository;
    private final TrangThaiHopDongService trangThaiHopDongService;
    private final LuongTrangThaiService luongTrangThaiService;
    private final DoiTuongHopDongLienKetService doiTuongHopDongLienKetService;
    private final KieuHopDongLuongTrangThaiService kieuHopDongLuongTrangThaiService;
    private final HopDongMapper hopDongMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongTrangThaiResponse> list(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID doiTuongId,
            UUID kieuHopDongId,
            UUID loaiHopDongId) {
        String keyword = EntityFilter.normalizeSearch(search);
        List<HopDongDoiTuongTrangThai> source = includeDeleted
                ? hopDongDoiTuongTrangThaiRepository.findAll()
                : hopDongDoiTuongTrangThaiRepository.findByNgayXoaIsNull();

        Map<UUID, TrangThaiHopDongResponse> statusMap = loadStatusMap(source);

        return source.stream()
                .filter(entity -> EntityFilter.isActive(entity, HopDongDoiTuongTrangThai::getHoatDong, activeOnly))
                .filter(entity -> doiTuongId == null || doiTuongId.equals(entity.getDoiTuongId()))
                .filter(entity -> kieuHopDongId == null || kieuHopDongId.equals(entity.getKieuHopDongId()))
                .filter(entity -> loaiHopDongId == null || loaiHopDongId.equals(entity.getLoaiHopDongId()))
                .filter(entity -> EntityFilter.matchesKeyword(
                        keyword,
                        EntityFilter.nullToEmpty(entity.getNhanHienThi()),
                        EntityFilter.nullToEmpty(entity.getMauSac()),
                        EntityFilter.nullToEmpty(entity.getMauNen()),
                        EntityFilter.nullToEmpty(entity.getGiaTriLoc())))
                .sorted(Comparator
                        .comparing(HopDongDoiTuongTrangThai::getThuTu, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(HopDongDoiTuongTrangThai::getNgayTao, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(entity -> toEnrichedResponse(entity, statusMap.get(entity.getTrangThaiId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HopDongDoiTuongTrangThaiResponse getById(UUID id) {
        HopDongDoiTuongTrangThai entity = findById(id);
        return toEnrichedResponse(entity, loadStatus(entity.getTrangThaiId()));
    }

    @Override
    @Transactional
    public HopDongDoiTuongTrangThaiResponse create(HopDongDoiTuongTrangThaiTaoRequest request) {
        HopDongDoiTuongTrangThai entity = hopDongMapper.fromHopDongDoiTuongTrangThaiTaoRequest(request);
        if (request.getNhanHienThi() != null) {
            entity.setNhanHienThi(request.getNhanHienThi().trim());
        }
        if (request.getMauSac() != null) {
            entity.setMauSac(request.getMauSac().trim());
        }
        if (request.getMauNen() != null) {
            entity.setMauNen(request.getMauNen().trim());
        }
        if (request.getGiaTriLoc() != null) {
            entity.setGiaTriLoc(request.getGiaTriLoc().trim());
        }
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        HopDongDoiTuongTrangThai saved = hopDongDoiTuongTrangThaiRepository.save(entity);
        return toEnrichedResponse(saved, loadStatus(saved.getTrangThaiId()));
    }

    @Override
    @Transactional
    public HopDongDoiTuongTrangThaiResponse update(UUID id, HopDongDoiTuongTrangThaiCapNhatRequest request) {
        HopDongDoiTuongTrangThai entity = findById(id);
        hopDongMapper.updateFromHopDongDoiTuongTrangThaiCapNhatRequest(request, entity);

        HopDongDoiTuongTrangThai saved = hopDongDoiTuongTrangThaiRepository.save(entity);
        return toEnrichedResponse(saved, loadStatus(saved.getTrangThaiId()));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        HopDongDoiTuongTrangThai entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        hopDongDoiTuongTrangThaiRepository.save(entity);
    }

    @Override
    @Transactional
    public void syncFromLuong(UUID kieuHopDongId, UUID loaiHopDongId, UUID luongTrangThaiId) {
        if (kieuHopDongId == null || loaiHopDongId == null || luongTrangThaiId == null) {
            return;
        }

        List<LuongTrangThaiBuocResponse> buocList = luongTrangThaiService.listBuocByLuongId(luongTrangThaiId);
        List<DoiTuongHopDongLienKetResponse> doiTuongLinks = doiTuongHopDongLienKetService.list(
                loaiHopDongId, kieuHopDongId, true, false);

        Map<UUID, TrangThaiHopDongResponse> statusMap = loadStatusMapFromBuoc(buocList);
        List<HopDongDoiTuongTrangThai> existing = hopDongDoiTuongTrangThaiRepository
                .findByKieuHopDongIdAndLoaiHopDongIdAndNgayXoaIsNull(kieuHopDongId, loaiHopDongId);

        Map<String, HopDongDoiTuongTrangThai> existingByKey = existing.stream()
                .collect(Collectors.toMap(
                        row -> compositeKey(row.getDoiTuongId(), row.getTrangThaiId()),
                        Function.identity(),
                        (left, right) -> left));

        Set<String> activeKeys = new HashSet<>();
        Set<UUID> seenDoiTuong = new HashSet<>();

        for (DoiTuongHopDongLienKetResponse link : doiTuongLinks) {
            UUID doiTuongQuanLyId = link.getDoiTuongQuanLyId();
            if (doiTuongQuanLyId == null || !seenDoiTuong.add(doiTuongQuanLyId)) {
                continue;
            }
            for (LuongTrangThaiBuocResponse buoc : buocList) {
                UUID trangThaiId = buoc.getTrangThaiHopDongId();
                String key = compositeKey(doiTuongQuanLyId, trangThaiId);
                activeKeys.add(key);

                HopDongDoiTuongTrangThai row = existingByKey.get(key);
                if (row == null) {
                    row = new HopDongDoiTuongTrangThai();
                    row.setKieuHopDongId(kieuHopDongId);
                    row.setLoaiHopDongId(loaiHopDongId);
                    row.setLuongTrangThaiId(luongTrangThaiId);
                    row.setDoiTuongId(doiTuongQuanLyId);
                    row.setTrangThaiId(trangThaiId);
                    row.setGhiDe(false);
                    row.setHoatDong(true);
                }
                row.setLuongTrangThaiId(luongTrangThaiId);
                row.setThuTu(buoc.getThuTu());
                row.setNgayXoa(null);
                row.setHoatDong(true);
                applyStatusDisplay(row, statusMap.get(trangThaiId));
                hopDongDoiTuongTrangThaiRepository.save(row);
            }
        }

        for (HopDongDoiTuongTrangThai row : existing) {
            String key = compositeKey(row.getDoiTuongId(), row.getTrangThaiId());
            if (!activeKeys.contains(key)) {
                row.setNgayXoa(Instant.now());
                row.setHoatDong(false);
                hopDongDoiTuongTrangThaiRepository.save(row);
            }
        }
    }

    @Override
    @Transactional
    public void syncAllKieuUsingLuong(UUID luongTrangThaiId) {
        kieuHopDongLuongTrangThaiService.listByLuongTrangThaiId(luongTrangThaiId)
                .forEach(link -> syncFromLuong(
                        link.getKieuHopDongId(),
                        link.getLoaiHopDongId(),
                        luongTrangThaiId));
    }

    @Override
    @Transactional
    public void removeForKieu(UUID kieuHopDongId, UUID loaiHopDongId) {
        hopDongDoiTuongTrangThaiRepository
                .findByKieuHopDongIdAndLoaiHopDongIdAndNgayXoaIsNull(kieuHopDongId, loaiHopDongId)
                .forEach(row -> {
                    row.setNgayXoa(Instant.now());
                    row.setHoatDong(false);
                    hopDongDoiTuongTrangThaiRepository.save(row);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public UUID resolveDefaultTrangThaiId(UUID kieuHopDongId, UUID loaiHopDongId, UUID doiTuongQuanLyId) {
        return hopDongDoiTuongTrangThaiRepository
                .findByKieuHopDongIdAndLoaiHopDongIdAndDoiTuongIdAndNgayXoaIsNullOrderByThuTuAsc(
                        kieuHopDongId, loaiHopDongId, doiTuongQuanLyId)
                .stream()
                .findFirst()
                .map(HopDongDoiTuongTrangThai::getTrangThaiId)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAllowedTrangThai(
            UUID kieuHopDongId,
            UUID loaiHopDongId,
            UUID doiTuongQuanLyId,
            UUID trangThaiHopDongId) {
        if (trangThaiHopDongId == null) {
            return true;
        }
        if (hopDongDoiTuongTrangThaiRepository
                .findByKieuHopDongIdAndLoaiHopDongIdAndDoiTuongIdAndTrangThaiIdAndNgayXoaIsNull(
                        kieuHopDongId, loaiHopDongId, doiTuongQuanLyId, trangThaiHopDongId)
                .isPresent()) {
            return true;
        }
        TrangThaiHopDongResponse status = loadStatus(trangThaiHopDongId);
        if (status == null || status.getMa() == null) {
            return false;
        }
        String ma = HopDongDanhSachTienDoHelper.normalizeStatusMa(status.getMa());
        return ma.equals(HopDongDanhSachTienDoHelper.MA_CHO_XAC_NHAN_HUY)
                || ma.equals(HopDongDanhSachTienDoHelper.MA_CHO_XAC_NHAN_HT);
    }

    private Map<UUID, TrangThaiHopDongResponse> loadStatusMap(List<HopDongDoiTuongTrangThai> source) {
        List<UUID> ids = source.stream()
                .map(HopDongDoiTuongTrangThai::getTrangThaiId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return trangThaiHopDongService.getByIds(ids).stream()
                .collect(Collectors.toMap(TrangThaiHopDongResponse::getId, Function.identity(), (a, b) -> a));
    }

    private Map<UUID, TrangThaiHopDongResponse> loadStatusMapFromBuoc(List<LuongTrangThaiBuocResponse> buocList) {
        if (buocList.isEmpty()) {
            return Map.of();
        }
        List<UUID> ids = buocList.stream()
                .map(LuongTrangThaiBuocResponse::getTrangThaiHopDongId)
                .distinct()
                .toList();
        return trangThaiHopDongService.getByIds(ids).stream()
                .collect(Collectors.toMap(TrangThaiHopDongResponse::getId, Function.identity(), (a, b) -> a));
    }

    private TrangThaiHopDongResponse loadStatus(UUID trangThaiId) {
        if (trangThaiId == null) {
            return null;
        }
        try {
            return trangThaiHopDongService.getById(trangThaiId);
        } catch (AppException ignored) {
            return null;
        }
    }

    private HopDongDoiTuongTrangThaiResponse toEnrichedResponse(
            HopDongDoiTuongTrangThai entity,
            TrangThaiHopDongResponse status) {
        HopDongDoiTuongTrangThaiResponse response = hopDongMapper.toHopDongDoiTuongTrangThaiResponse(entity);
        if (status != null) {
            response.setTrangThaiMa(status.getMa());
            response.setTrangThaiTen(status.getTen());
        }
        return response;
    }

    private void applyStatusDisplay(HopDongDoiTuongTrangThai row, TrangThaiHopDongResponse status) {
        if (status == null) {
            if (row.getNhanHienThi() == null || row.getNhanHienThi().isBlank()) {
                row.setNhanHienThi("—");
            }
            return;
        }
        if (!Boolean.TRUE.equals(row.getGhiDe()) || row.getNhanHienThi() == null || row.getNhanHienThi().isBlank()) {
            row.setNhanHienThi(Objects.requireNonNullElse(status.getTen(), status.getMa()));
        }
        if (!Boolean.TRUE.equals(row.getGhiDe()) || row.getMauSac() == null) {
            row.setMauSac(status.getMauSac());
        }
        if (row.getGiaTriLoc() == null || row.getGiaTriLoc().isBlank()) {
            row.setGiaTriLoc(status.getMa());
        }
    }

    private String compositeKey(UUID doiTuongId, UUID trangThaiId) {
        return doiTuongId + ":" + trangThaiId;
    }

    private HopDongDoiTuongTrangThai findById(UUID id) {
        return hopDongDoiTuongTrangThaiRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_TRANG_THAI_NOT_FOUND, "Không tìm thấy bản ghi"));
    }
}
