package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LuongTrangThaiCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LuongTrangThaiDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LuongTrangThaiTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LuongTrangThai;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LuongTrangThaiBuoc;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LuongTrangThaiBuocId;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.TrangThaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LuongTrangThaiBuocRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LuongTrangThaiRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.TrangThaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LuongTrangThaiService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongTrangThaiService;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LuongTrangThaiServiceImpl implements LuongTrangThaiService {

    private final LuongTrangThaiRepository luongTrangThaiRepository;
    private final LuongTrangThaiBuocRepository buocRepository;
    private final TrangThaiHopDongRepository trangThaiHopDongRepository;
    private final HopDongDoiTuongTrangThaiService hopDongDoiTuongTrangThaiService;

    public LuongTrangThaiServiceImpl(
            LuongTrangThaiRepository luongTrangThaiRepository,
            LuongTrangThaiBuocRepository buocRepository,
            TrangThaiHopDongRepository trangThaiHopDongRepository,
            @Lazy HopDongDoiTuongTrangThaiService hopDongDoiTuongTrangThaiService) {
        this.luongTrangThaiRepository = luongTrangThaiRepository;
        this.buocRepository = buocRepository;
        this.trangThaiHopDongRepository = trangThaiHopDongRepository;
        this.hopDongDoiTuongTrangThaiService = hopDongDoiTuongTrangThaiService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LuongTrangThaiResponse> list(String search, Boolean activeOnly, boolean includeDeleted) {
        String keyword = EntityFilter.normalizeSearch(search);
        return luongTrangThaiRepository.search(includeDeleted, activeOnly, keyword)
                .stream()
                .map(entity -> toResponse(entity, true))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LuongTrangThaiResponse getById(UUID id) {
        return toResponse(findById(id), true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LuongTrangThaiBuocResponse> listBuocByLuongId(UUID luongTrangThaiId) {
        findById(luongTrangThaiId);
        List<LuongTrangThaiBuoc> buocList = buocRepository
                .findById_LuongTrangThaiIdOrderByThuTuAsc(luongTrangThaiId);
        Map<UUID, TrangThaiHopDong> statusMap = loadStatusMap(buocList);
        return buocList.stream()
                .map(buoc -> toBuocResponse(buoc, statusMap.get(buoc.getId().getTrangThaiHopDongId())))
                .toList();
    }

    @Override
    @Transactional
    public LuongTrangThaiResponse create(LuongTrangThaiTaoRequest request) {
        LuongTrangThai entity = new LuongTrangThai();
        entity.setTen(request.getTen().trim());
        entity.setMoTa(EntityFilter.trimToNull(request.getMoTa()));
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());
        return toResponse(luongTrangThaiRepository.save(entity), true);
    }

    @Override
    @Transactional
    public LuongTrangThaiResponse update(UUID id, LuongTrangThaiCapNhatRequest request) {
        LuongTrangThai entity = findById(id);
        entity.setTen(request.getTen().trim());
        entity.setMoTa(EntityFilter.trimToNull(request.getMoTa()));
        if (request.getHoatDong() != null) {
            entity.setHoatDong(request.getHoatDong());
        }
        return toResponse(luongTrangThaiRepository.save(entity), true);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        LuongTrangThai entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        luongTrangThaiRepository.save(entity);
    }

    @Override
    @Transactional
    public LuongTrangThaiResponse syncBuoc(UUID id, LuongTrangThaiDongBoRequest request) {
        LuongTrangThai luong = findById(id);

        List<UUID> desiredIds = request.getTrangThaiHopDongIds() == null
                ? List.of()
                : request.getTrangThaiHopDongIds().stream().distinct().toList();

        for (UUID trangThaiId : desiredIds) {
            trangThaiHopDongRepository.findByIdAndNgayXoaIsNull(trangThaiId)
                    .orElseThrow(() -> new AppException(
                            CauHinhErrorCode.TRANG_THAI_HOP_DONG_NOT_FOUND, "Trạng thái không tồn tại"));
        }

        List<LuongTrangThaiBuoc> current = buocRepository.findById_LuongTrangThaiIdOrderByThuTuAsc(luong.getId());
        Map<UUID, LuongTrangThaiBuoc> existingByStatus = current.stream()
                .collect(Collectors.toMap(b -> b.getId().getTrangThaiHopDongId(), Function.identity(), (a, b) -> a));

        short order = 0;
        for (UUID trangThaiId : desiredIds) {
            LuongTrangThaiBuoc buoc = existingByStatus.get(trangThaiId);
            if (buoc == null) {
                LuongTrangThaiBuocId buocId = new LuongTrangThaiBuocId();
                buocId.setLuongTrangThaiId(luong.getId());
                buocId.setTrangThaiHopDongId(trangThaiId);
                buoc = new LuongTrangThaiBuoc();
                buoc.setId(buocId);
            }
            buoc.setThuTu(order++);
            buocRepository.save(buoc);
        }

        Set<UUID> desiredSet = new HashSet<>(desiredIds);
        for (LuongTrangThaiBuoc buoc : current) {
            if (!desiredSet.contains(buoc.getId().getTrangThaiHopDongId())) {
                buocRepository.delete(buoc);
            }
        }

        hopDongDoiTuongTrangThaiService.syncAllKieuUsingLuong(luong.getId());
        return toResponse(luong, true);
    }

    private LuongTrangThai findById(UUID id) {
        return luongTrangThaiRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(CauHinhErrorCode.LUONG_TRANG_THAI_NOT_FOUND, "Không tìm thấy luồng trạng thái"));
    }

    private LuongTrangThaiResponse toResponse(LuongTrangThai entity, boolean includeBuoc) {
        LuongTrangThaiResponse.LuongTrangThaiResponseBuilder builder = LuongTrangThaiResponse.builder()
                .id(entity.getId())
                .ten(entity.getTen())
                .moTa(entity.getMoTa())
                .hoatDong(Boolean.TRUE.equals(entity.getHoatDong()))
                .ngayTao(entity.getNgayTao())
                .ngayCapNhat(entity.getNgayCapNhat());

        if (includeBuoc) {
            List<LuongTrangThaiBuoc> buocList = buocRepository
                    .findById_LuongTrangThaiIdOrderByThuTuAsc(entity.getId());
            Map<UUID, TrangThaiHopDong> statusMap = loadStatusMap(buocList);
            builder.buoc(buocList.stream()
                    .map(buoc -> toBuocResponse(buoc, statusMap.get(buoc.getId().getTrangThaiHopDongId())))
                    .toList());
        }

        return builder.build();
    }

    private Map<UUID, TrangThaiHopDong> loadStatusMap(List<LuongTrangThaiBuoc> buocList) {
        if (buocList.isEmpty()) {
            return Map.of();
        }
        List<UUID> ids = buocList.stream()
                .map(b -> b.getId().getTrangThaiHopDongId())
                .distinct()
                .toList();
        return trangThaiHopDongRepository.findAllById(ids).stream()
                .filter(s -> s.getNgayXoa() == null)
                .collect(Collectors.toMap(TrangThaiHopDong::getId, Function.identity(), (a, b) -> a));
    }

    private LuongTrangThaiBuocResponse toBuocResponse(LuongTrangThaiBuoc buoc, TrangThaiHopDong status) {
        LuongTrangThaiBuocResponse.LuongTrangThaiBuocResponseBuilder builder = LuongTrangThaiBuocResponse.builder()
                .trangThaiHopDongId(buoc.getId().getTrangThaiHopDongId())
                .thuTu(buoc.getThuTu());
        if (status != null) {
            builder.ten(status.getTen())
                    .ma(status.getMa())
                    .mauSac(status.getMauSac());
        }
        return builder.build();
    }

}
