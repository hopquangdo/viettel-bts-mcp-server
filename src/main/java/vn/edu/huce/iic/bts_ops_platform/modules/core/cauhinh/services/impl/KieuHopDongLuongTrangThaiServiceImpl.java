package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuHopDongLuongTrangThaiGanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.KieuHopDongLuongTrangThaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDongLuongTrangThai;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDongLuongTrangThaiId;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LuongTrangThai;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongLuongTrangThaiRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LoaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LuongTrangThaiRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.KieuHopDongLuongTrangThaiService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongTrangThaiService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class KieuHopDongLuongTrangThaiServiceImpl implements KieuHopDongLuongTrangThaiService {

    private final KieuHopDongLuongTrangThaiRepository repository;
    private final LoaiHopDongRepository loaiHopDongRepository;
    private final KieuHopDongRepository kieuHopDongRepository;
    private final LuongTrangThaiRepository luongTrangThaiRepository;
    private final HopDongDoiTuongTrangThaiService hopDongDoiTuongTrangThaiService;

    public KieuHopDongLuongTrangThaiServiceImpl(
            KieuHopDongLuongTrangThaiRepository repository,
            LoaiHopDongRepository loaiHopDongRepository,
            KieuHopDongRepository kieuHopDongRepository,
            LuongTrangThaiRepository luongTrangThaiRepository,
            @Lazy HopDongDoiTuongTrangThaiService hopDongDoiTuongTrangThaiService) {
        this.repository = repository;
        this.loaiHopDongRepository = loaiHopDongRepository;
        this.kieuHopDongRepository = kieuHopDongRepository;
        this.luongTrangThaiRepository = luongTrangThaiRepository;
        this.hopDongDoiTuongTrangThaiService = hopDongDoiTuongTrangThaiService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<KieuHopDongLuongTrangThaiResponse> list(UUID loaiHopDongId, UUID kieuHopDongId) {
        List<KieuHopDongLuongTrangThai> links = repository.search(false, loaiHopDongId, kieuHopDongId);
        Map<UUID, LuongTrangThai> luongMap = loadLuongMap(links);
        return links.stream()
                .map(link -> toResponse(link, luongMap.get(link.getLuongTrangThaiId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public KieuHopDongLuongTrangThaiResponse getByKieuAndLoai(UUID kieuHopDongId, UUID loaiHopDongId) {
        KieuHopDongLuongTrangThai link = repository
                .findById_KieuHopDongIdAndId_LoaiHopDongIdAndNgayXoaIsNull(kieuHopDongId, loaiHopDongId)
                .orElseThrow(() -> new AppException(
                        CauHinhErrorCode.KIEU_HOP_DONG_LUONG_TRANG_THAI_NOT_FOUND,
                        "Kiểu HĐ chưa gắn luồng trạng thái"));
        LuongTrangThai luong = luongTrangThaiRepository.findByIdAndNgayXoaIsNull(link.getLuongTrangThaiId())
                .orElse(null);
        return toResponse(link, luong);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KieuHopDongLuongTrangThaiResponse> listByLuongTrangThaiId(UUID luongTrangThaiId) {
        List<KieuHopDongLuongTrangThai> links = repository.findByLuongTrangThaiIdAndNgayXoaIsNull(luongTrangThaiId);
        Map<UUID, LuongTrangThai> luongMap = loadLuongMap(links);
        return links.stream().map(link -> toResponse(link, luongMap.get(link.getLuongTrangThaiId()))).toList();
    }

    @Override
    @Transactional
    public KieuHopDongLuongTrangThaiResponse gan(KieuHopDongLuongTrangThaiGanRequest request) {
        UUID loaiHopDongId = request.getLoaiHopDongId();
        UUID kieuHopDongId = request.getKieuHopDongId();
        validateKieuAndLoai(loaiHopDongId, kieuHopDongId);

        KieuHopDongLuongTrangThaiId id = buildId(kieuHopDongId, loaiHopDongId);
        KieuHopDongLuongTrangThai entity = repository.findById(id).orElseGet(KieuHopDongLuongTrangThai::new);

        if (request.getLuongTrangThaiId() == null) {
            repository.findById_KieuHopDongIdAndId_LoaiHopDongIdAndNgayXoaIsNull(kieuHopDongId, loaiHopDongId)
                    .ifPresent(existing -> {
                        existing.setNgayXoa(Instant.now());
                        existing.setHoatDong(false);
                        repository.save(existing);
                    });
            hopDongDoiTuongTrangThaiService.removeForKieu(kieuHopDongId, loaiHopDongId);
            KieuHopDongLuongTrangThaiResponse response = new KieuHopDongLuongTrangThaiResponse();
            response.setKieuHopDongId(kieuHopDongId);
            response.setLoaiHopDongId(loaiHopDongId);
            response.setHoatDong(false);
            return response;
        }

        LuongTrangThai luong = luongTrangThaiRepository.findByIdAndNgayXoaIsNull(request.getLuongTrangThaiId())
                .orElseThrow(() -> new AppException(
                        CauHinhErrorCode.LUONG_TRANG_THAI_NOT_FOUND, "Luồng trạng thái không tồn tại"));

        entity.setId(id);
        entity.setLuongTrangThaiId(luong.getId());
        entity.setNgayXoa(null);
        entity.setHoatDong(true);
        repository.save(entity);
        hopDongDoiTuongTrangThaiService.syncFromLuong(kieuHopDongId, loaiHopDongId, luong.getId());
        return toResponse(entity, luong);
    }

    @Override
    @Transactional
    public void remove(UUID kieuHopDongId, UUID loaiHopDongId) {
        KieuHopDongLuongTrangThai entity = repository
                .findById_KieuHopDongIdAndId_LoaiHopDongIdAndNgayXoaIsNull(kieuHopDongId, loaiHopDongId)
                .orElseThrow(() -> new AppException(
                        CauHinhErrorCode.KIEU_HOP_DONG_LUONG_TRANG_THAI_NOT_FOUND,
                        "Không tìm thấy liên kết luồng trạng thái"));
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        repository.save(entity);
        hopDongDoiTuongTrangThaiService.removeForKieu(kieuHopDongId, loaiHopDongId);
    }

    private void validateKieuAndLoai(UUID loaiHopDongId, UUID kieuHopDongId) {
        loaiHopDongRepository.findByIdAndNgayXoaIsNull(loaiHopDongId)
                .orElseThrow(() -> new AppException(CauHinhErrorCode.LOAI_HOP_DONG_NOT_FOUND, "Loại hợp đồng không tồn tại"));
        kieuHopDongRepository.findByIdAndNgayXoaIsNull(kieuHopDongId)
                .orElseThrow(() -> new AppException(CauHinhErrorCode.KIEU_HOP_DONG_NOT_FOUND, "Kiểu hợp đồng không tồn tại"));
    }

    private KieuHopDongLuongTrangThaiId buildId(UUID kieuHopDongId, UUID loaiHopDongId) {
        KieuHopDongLuongTrangThaiId id = new KieuHopDongLuongTrangThaiId();
        id.setKieuHopDongId(kieuHopDongId);
        id.setLoaiHopDongId(loaiHopDongId);
        return id;
    }

    private Map<UUID, LuongTrangThai> loadLuongMap(List<KieuHopDongLuongTrangThai> links) {
        List<UUID> ids = links.stream().map(KieuHopDongLuongTrangThai::getLuongTrangThaiId).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return luongTrangThaiRepository.findAllById(ids).stream()
                .filter(l -> l.getNgayXoa() == null)
                .collect(Collectors.toMap(LuongTrangThai::getId, Function.identity(), (a, b) -> a));
    }

    private KieuHopDongLuongTrangThaiResponse toResponse(KieuHopDongLuongTrangThai entity, LuongTrangThai luong) {
        KieuHopDongLuongTrangThaiResponse response = new KieuHopDongLuongTrangThaiResponse();
        response.setKieuHopDongId(entity.getId().getKieuHopDongId());
        response.setLoaiHopDongId(entity.getId().getLoaiHopDongId());
        response.setLuongTrangThaiId(entity.getLuongTrangThaiId());
        if (luong != null) {
            response.setLuongTrangThaiTen(luong.getTen());
        }
        response.setHoatDong(Boolean.TRUE.equals(entity.getHoatDong()));
        response.setNgayTao(entity.getNgayTao());
        response.setNgayCapNhat(entity.getNgayCapNhat());
        return response;
    }
}
