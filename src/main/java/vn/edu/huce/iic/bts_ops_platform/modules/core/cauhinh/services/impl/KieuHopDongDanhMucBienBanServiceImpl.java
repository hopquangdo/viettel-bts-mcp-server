package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuHopDongDanhMucBienBanDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.KieuHopDongDanhMucBienBanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.DanhMucBienBan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDongDanhMucBienBan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.DanhMucBienBanRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongDanhMucBienBanRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.KieuHopDongDanhMucBienBanService;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KieuHopDongDanhMucBienBanServiceImpl implements KieuHopDongDanhMucBienBanService {

    private final KieuHopDongDanhMucBienBanRepository lienKetRepository;
    private final DanhMucBienBanRepository danhMucBienBanRepository;
    private final KieuHopDongRepository kieuHopDongRepository;

    @Override
    @Transactional(readOnly = true)
    public List<KieuHopDongDanhMucBienBanResponse> list(UUID kieuHopDongId, Boolean activeOnly, boolean includeDeleted) {
        if (kieuHopDongId == null) {
            throw new AppException(CauHinhErrorCode.KIEU_HOP_DONG_NOT_FOUND, "Thiếu kiểu hợp đồng");
        }
        ensureKieuExists(kieuHopDongId);
        List<KieuHopDongDanhMucBienBan> source = lienKetRepository.search(kieuHopDongId, includeDeleted, activeOnly);
        Map<UUID, DanhMucBienBan> catalogMap = loadCatalogMap(source);
        return source.stream()
                .map(item -> toResponse(item, catalogMap.get(item.getDanhMucBienBanId())))
                .toList();
    }

    @Override
    @Transactional
    public List<KieuHopDongDanhMucBienBanResponse> sync(KieuHopDongDanhMucBienBanDongBoRequest request) {
        UUID kieuHopDongId = request.getKieuHopDongId();
        ensureKieuExists(kieuHopDongId);

        List<UUID> desiredIds = request.getDanhMucBienBanIds() == null
                ? List.of()
                : request.getDanhMucBienBanIds().stream().distinct().toList();
        for (UUID danhMucId : desiredIds) {
            ensureDanhMucExists(danhMucId);
        }

        List<KieuHopDongDanhMucBienBan> current =
                lienKetRepository.findByKieuHopDongIdAndNgayXoaIsNullOrderByThuTuAsc(kieuHopDongId);
        Set<UUID> desiredSet = new HashSet<>(desiredIds);
        int order = 0;

        for (KieuHopDongDanhMucBienBan link : current) {
            if (!desiredSet.contains(link.getDanhMucBienBanId())) {
                link.setNgayXoa(Instant.now());
                link.setHoatDong(false);
                lienKetRepository.save(link);
            }
        }

        Map<UUID, KieuHopDongDanhMucBienBan> activeByDanhMuc = current.stream()
                .filter(link -> link.getNgayXoa() == null)
                .collect(Collectors.toMap(KieuHopDongDanhMucBienBan::getDanhMucBienBanId, Function.identity(), (a, b) -> a));

        for (UUID danhMucId : desiredIds) {
            KieuHopDongDanhMucBienBan existing = activeByDanhMuc.get(danhMucId);
            if (existing != null) {
                existing.setThuTu(order++);
                existing.setHoatDong(true);
                lienKetRepository.save(existing);
                continue;
            }
            KieuHopDongDanhMucBienBan created = new KieuHopDongDanhMucBienBan();
            created.setKieuHopDongId(kieuHopDongId);
            created.setDanhMucBienBanId(danhMucId);
            created.setThuTu(order++);
            created.setHoatDong(true);
            lienKetRepository.save(created);
        }

        return list(kieuHopDongId, true, false);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        KieuHopDongDanhMucBienBan entity = lienKetRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(
                        CauHinhErrorCode.KIEU_HOP_DONG_DANH_MUC_BIEN_BAN_NOT_FOUND, "Không tìm thấy liên kết"));
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        lienKetRepository.save(entity);
    }

    private Map<UUID, DanhMucBienBan> loadCatalogMap(List<KieuHopDongDanhMucBienBan> source) {
        Set<UUID> ids = source.stream().map(KieuHopDongDanhMucBienBan::getDanhMucBienBanId).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return danhMucBienBanRepository.findByIdInAndNgayXoaIsNull(ids.stream().toList()).stream()
                .collect(Collectors.toMap(DanhMucBienBan::getId, Function.identity()));
    }

    private KieuHopDongDanhMucBienBanResponse toResponse(KieuHopDongDanhMucBienBan link, DanhMucBienBan danhMuc) {
        KieuHopDongDanhMucBienBanResponse response = new KieuHopDongDanhMucBienBanResponse();
        response.setId(link.getId());
        response.setKieuHopDongId(link.getKieuHopDongId());
        response.setDanhMucBienBanId(link.getDanhMucBienBanId());
        response.setThuTu(link.getThuTu());
        response.setHoatDong(link.getHoatDong());
        response.setNgayTao(link.getNgayTao());
        response.setNgayCapNhat(link.getNgayCapNhat());
        if (danhMuc != null) {
            response.setMa(danhMuc.getMa());
            response.setTen(danhMuc.getTen());
            response.setMoTa(danhMuc.getMoTa());
            response.setCoMauWord(danhMuc.getCoMauWord());
            response.setGiaiDoan(danhMuc.getGiaiDoan());
        }
        return response;
    }

    private void ensureKieuExists(UUID kieuHopDongId) {
        kieuHopDongRepository.findByIdAndNgayXoaIsNull(kieuHopDongId)
                .orElseThrow(() -> new AppException(CauHinhErrorCode.KIEU_HOP_DONG_NOT_FOUND, "Không tìm thấy kiểu HĐ"));
    }

    private void ensureDanhMucExists(UUID danhMucBienBanId) {
        danhMucBienBanRepository.findByIdAndNgayXoaIsNull(danhMucBienBanId)
                .orElseThrow(() -> new AppException(
                        CauHinhErrorCode.DANH_MUC_BIEN_BAN_NOT_FOUND, "Không tìm thấy danh mục biên bản"));
    }
}
