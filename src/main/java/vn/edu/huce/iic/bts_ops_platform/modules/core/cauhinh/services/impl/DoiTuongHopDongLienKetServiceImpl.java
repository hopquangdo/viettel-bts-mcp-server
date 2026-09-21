package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DoiTuongHopDongLienKetDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DoiTuongHopDongLienKetResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.DoiTuongHopDongLienKet;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.DoiTuongQuanLy;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.DoiTuongHopDongLienKetRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.DoiTuongQuanLyRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongLuongTrangThaiRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LoaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LuongTrangThaiRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.DoiTuongHopDongLienKetService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongTrangThaiService;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DoiTuongHopDongLienKetServiceImpl implements DoiTuongHopDongLienKetService {

    private final DoiTuongHopDongLienKetRepository lienKetRepository;
    private final DoiTuongQuanLyRepository doiTuongQuanLyRepository;
    private final LoaiHopDongRepository loaiHopDongRepository;
    private final KieuHopDongRepository kieuHopDongRepository;
    private final KieuHopDongLuongTrangThaiRepository kieuHopDongLuongTrangThaiRepository;
    private final LuongTrangThaiRepository luongTrangThaiRepository;
    private final HopDongDoiTuongTrangThaiService hopDongDoiTuongTrangThaiService;

    public DoiTuongHopDongLienKetServiceImpl(
            DoiTuongHopDongLienKetRepository lienKetRepository,
            DoiTuongQuanLyRepository doiTuongQuanLyRepository,
            LoaiHopDongRepository loaiHopDongRepository,
            KieuHopDongRepository kieuHopDongRepository,
            KieuHopDongLuongTrangThaiRepository kieuHopDongLuongTrangThaiRepository,
            LuongTrangThaiRepository luongTrangThaiRepository,
            @Lazy HopDongDoiTuongTrangThaiService hopDongDoiTuongTrangThaiService) {
        this.lienKetRepository = lienKetRepository;
        this.doiTuongQuanLyRepository = doiTuongQuanLyRepository;
        this.loaiHopDongRepository = loaiHopDongRepository;
        this.kieuHopDongRepository = kieuHopDongRepository;
        this.kieuHopDongLuongTrangThaiRepository = kieuHopDongLuongTrangThaiRepository;
        this.luongTrangThaiRepository = luongTrangThaiRepository;
        this.hopDongDoiTuongTrangThaiService = hopDongDoiTuongTrangThaiService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoiTuongHopDongLienKetResponse> list(
            UUID loaiHopDongId,
            UUID kieuHopDongId,
            Boolean activeOnly,
            boolean includeDeleted) {
        List<DoiTuongHopDongLienKet> source = resolveSource(loaiHopDongId, kieuHopDongId, activeOnly, includeDeleted);
        Map<UUID, DoiTuongQuanLy> doiTuongMap = loadDoiTuongMap(source);
        return source.stream()
                .map(item -> toResponse(item, doiTuongMap.get(item.getDoiTuongQuanLyId())))
                .toList();
    }

    @Override
    @Transactional
    public List<DoiTuongHopDongLienKetResponse> sync(DoiTuongHopDongLienKetDongBoRequest request) {
        UUID loaiHopDongId = request.getLoaiHopDongId();
        UUID kieuHopDongId = request.getKieuHopDongId();
        validateScope(loaiHopDongId, kieuHopDongId);

        List<UUID> desiredIds = request.getDoiTuongQuanLyIds() == null
                ? List.of()
                : request.getDoiTuongQuanLyIds().stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
        for (UUID doiTuongId : desiredIds) {
            ensureDoiTuongExists(doiTuongId);
        }

        List<DoiTuongHopDongLienKet> current = kieuHopDongId != null
                ? lienKetRepository.findByKieuHopDongIdAndNgayXoaIsNull(kieuHopDongId)
                : lienKetRepository.findByLoaiHopDongIdAndKieuHopDongIdIsNullAndNgayXoaIsNull(loaiHopDongId);

        Set<UUID> desiredSet = new HashSet<>(desiredIds);
        short order = 0;
        for (DoiTuongHopDongLienKet link : current) {
            if (!desiredSet.contains(link.getDoiTuongQuanLyId())) {
                link.setNgayXoa(Instant.now());
                link.setHoatDong(false);
                lienKetRepository.save(link);
            }
        }

        Map<UUID, DoiTuongHopDongLienKet> activeByDoiTuong = current.stream()
                .filter(link -> link.getNgayXoa() == null)
                .collect(Collectors.toMap(DoiTuongHopDongLienKet::getDoiTuongQuanLyId, Function.identity(), (a, b) -> a));

        for (UUID doiTuongId : desiredIds) {
            DoiTuongHopDongLienKet existing = activeByDoiTuong.get(doiTuongId);
            if (existing != null) {
                existing.setThuTu(order++);
                existing.setHoatDong(true);
                lienKetRepository.save(existing);
                continue;
            }

            DoiTuongHopDongLienKet created = new DoiTuongHopDongLienKet();
            created.setDoiTuongQuanLyId(doiTuongId);
            created.setLoaiHopDongId(loaiHopDongId);
            created.setKieuHopDongId(kieuHopDongId);
            created.setThuTu(order++);
            created.setHoatDong(true);
            lienKetRepository.save(created);
        }

        if (kieuHopDongId != null) {
            UUID resolvedLoaiId = loaiHopDongId;
            if (resolvedLoaiId == null) {
                resolvedLoaiId = kieuHopDongRepository.findByIdAndNgayXoaIsNull(kieuHopDongId)
                        .map(KieuHopDong::getLoaiHopDongId)
                        .orElse(null);
            }
            final UUID syncLoaiId = resolvedLoaiId;
            if (syncLoaiId != null) {
                kieuHopDongLuongTrangThaiRepository
                        .findById_KieuHopDongIdAndId_LoaiHopDongIdAndNgayXoaIsNull(kieuHopDongId, syncLoaiId)
                        .ifPresent(link -> {
                            UUID luongId = link.getLuongTrangThaiId();
                            if (luongId == null) {
                                return;
                            }
                            luongTrangThaiRepository.findByIdAndNgayXoaIsNull(luongId).ifPresent(luong ->
                                    hopDongDoiTuongTrangThaiService.syncFromLuong(
                                            kieuHopDongId, syncLoaiId, luong.getId()));
                        });
            }
        }

        return list(loaiHopDongId, kieuHopDongId, true, false);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        DoiTuongHopDongLienKet entity = lienKetRepository.findById(id)
                .filter(item -> item.getNgayXoa() == null)
                .orElseThrow(() -> new AppException(
                        CauHinhErrorCode.DOI_TUONG_HOP_DONG_LIEN_KET_NOT_FOUND,
                        "Không tìm thấy liên kết"));
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        lienKetRepository.save(entity);
    }

    private List<DoiTuongHopDongLienKet> resolveSource(
            UUID loaiHopDongId,
            UUID kieuHopDongId,
            Boolean activeOnly,
            boolean includeDeleted) {
        if (kieuHopDongId != null) {
            KieuHopDong kieu = kieuHopDongRepository.findByIdAndNgayXoaIsNull(kieuHopDongId)
                    .orElseThrow(() -> new AppException(CauHinhErrorCode.KIEU_HOP_DONG_NOT_FOUND, "Không tìm thấy kiểu HĐ"));
            UUID resolvedLoaiId = loaiHopDongId != null ? loaiHopDongId : kieu.getLoaiHopDongId();
            if (resolvedLoaiId == null) {
                throw new AppException(CauHinhErrorCode.LOAI_HOP_DONG_NOT_FOUND, "Không xác định được loại HĐ");
            }
            return lienKetRepository.search(
                    includeDeleted, activeOnly, resolvedLoaiId, kieuHopDongId, true, false, false);
        }
        if (loaiHopDongId != null) {
            return lienKetRepository.search(
                    includeDeleted, activeOnly, loaiHopDongId, null, false, true, false);
        }
        return lienKetRepository.search(includeDeleted, activeOnly, null, null, false, false, true);
    }

    private Map<UUID, DoiTuongQuanLy> loadDoiTuongMap(List<DoiTuongHopDongLienKet> source) {
        Set<UUID> ids = source.stream().map(DoiTuongHopDongLienKet::getDoiTuongQuanLyId).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return doiTuongQuanLyRepository.findActiveByIdIn(ids).stream()
                .collect(Collectors.toMap(DoiTuongQuanLy::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    private DoiTuongHopDongLienKetResponse toResponse(DoiTuongHopDongLienKet entity, DoiTuongQuanLy doiTuong) {
        DoiTuongHopDongLienKetResponse response = new DoiTuongHopDongLienKetResponse();
        response.setId(entity.getId());
        response.setDoiTuongQuanLyId(entity.getDoiTuongQuanLyId());
        response.setLoaiHopDongId(entity.getLoaiHopDongId());
        response.setKieuHopDongId(entity.getKieuHopDongId());
        response.setKeThuaTuLoai(entity.getKieuHopDongId() == null && entity.getLoaiHopDongId() != null);
        response.setThuTu(entity.getThuTu());
        response.setHoatDong(entity.getHoatDong());
        response.setNgayTao(entity.getNgayTao());
        response.setNgayCapNhat(entity.getNgayCapNhat());
        if (doiTuong != null) {
            response.setDoiTuongMa(doiTuong.getMa());
            response.setDoiTuongTen(doiTuong.getTen());
            response.setDoiTuongBieuTuong(doiTuong.getBieuTuong());
        }
        return response;
    }

    private void validateScope(UUID loaiHopDongId, UUID kieuHopDongId) {
        if (loaiHopDongId == null && kieuHopDongId == null) {
            throw new AppException(CauHinhErrorCode.DOI_TUONG_HOP_DONG_LIEN_KET_INVALID, "Chọn loại hoặc kiểu HĐ");
        }
        if (kieuHopDongId != null) {
            KieuHopDong kieu = kieuHopDongRepository.findByIdAndNgayXoaIsNull(kieuHopDongId)
                    .orElseThrow(() -> new AppException(CauHinhErrorCode.KIEU_HOP_DONG_NOT_FOUND, "Không tìm thấy kiểu HĐ"));
            if (loaiHopDongId != null && !loaiHopDongId.equals(kieu.getLoaiHopDongId())) {
                throw new AppException(CauHinhErrorCode.DOI_TUONG_HOP_DONG_LIEN_KET_INVALID, "Kiểu HĐ không thuộc loại HĐ đã chọn");
            }
            return;
        }
        loaiHopDongRepository.findByIdAndNgayXoaIsNull(loaiHopDongId)
                .orElseThrow(() -> new AppException(CauHinhErrorCode.LOAI_HOP_DONG_NOT_FOUND, "Không tìm thấy loại HĐ"));
    }

    private void ensureDoiTuongExists(UUID doiTuongQuanLyId) {
        doiTuongQuanLyRepository.findByIdAndNgayXoaIsNull(doiTuongQuanLyId)
                .orElseThrow(() -> new AppException(CauHinhErrorCode.DOI_TUONG_QUAN_LY_NOT_FOUND, "Không tìm thấy đối tượng quản lý"));
    }
}
