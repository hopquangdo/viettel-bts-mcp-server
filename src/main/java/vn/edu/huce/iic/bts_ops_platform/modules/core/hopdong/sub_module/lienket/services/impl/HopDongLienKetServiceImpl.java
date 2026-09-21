package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.lienket.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LoaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LoaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongLienKetCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongLienKetResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.lienket.entity.HopDongLienKet;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.lienket.repository.HopDongLienKetRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.lienket.services.HopDongLienKetService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongService;

import java.time.Instant;
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
public class HopDongLienKetServiceImpl implements HopDongLienKetService {

    private final HopDongLienKetRepository hopDongLienKetRepository;
    private final HopDongService hopDongService;
    private final LoaiHopDongRepository loaiHopDongRepository;
    private final KieuHopDongRepository kieuHopDongRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HopDongLienKetResponse> listAll() {
        return toResponses(hopDongLienKetRepository.findByNgayXoaIsNullOrderByNgayTaoDesc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongLienKetResponse> listByHopDongId(UUID hopDongId) {
        ensureHopDongExists(hopDongId);
        return toResponses(hopDongLienKetRepository.findByHopDongIdAndNgayXoaIsNullOrderByNgayTaoAsc(hopDongId));
    }

    @Override
    @Transactional
    public List<HopDongLienKetResponse> sync(UUID hopDongId, HopDongLienKetCapNhatRequest request) {
        HopDong hopDong = hopDongService.findActiveEntityById(hopDongId)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_NOT_FOUND, "Không tìm thấy hợp đồng"));

        UUID linkedId = request == null ? null : request.getHopDongLienKetId();
        if (linkedId != null) {
            HopDong linked = hopDongService.findActiveEntityById(linkedId)
                    .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_NOT_FOUND, "Không tìm thấy HĐ liên kết"));
            if (Objects.equals(linked.getId(), hopDongId)) {
                throw new AppException(
                        CauHinhErrorCode.LIEN_KET_HOP_DONG_INVALID,
                        "Không thể liên kết hợp đồng với chính nó");
            }
        }

        List<HopDongLienKet> current = hopDongLienKetRepository.findByHopDongIdAndNgayXoaIsNullOrderByNgayTaoAsc(hopDongId);
        for (HopDongLienKet existing : current) {
            existing.setNgayXoa(Instant.now());
            hopDongLienKetRepository.save(existing);
        }

        if (linkedId != null) {
            HopDongLienKet created = new HopDongLienKet();
            created.setHopDongId(hopDongId);
            created.setHopDongLienKetId(linkedId);
            created.setGhiChu(EntityFilter.trimToNull(request.getGhiChu()));
            hopDongLienKetRepository.save(created);
        }

        return listByHopDongId(hopDong.getId());
    }

    private List<HopDongLienKetResponse> toResponses(List<HopDongLienKet> source) {
        Set<UUID> hopDongIds = new HashSet<>();
        for (HopDongLienKet item : source) {
            hopDongIds.add(item.getHopDongId());
            hopDongIds.add(item.getHopDongLienKetId());
        }

        Map<UUID, HopDong> hopDongMap = hopDongIds.stream()
                .map(hopDongService::findActiveEntityById)
                .flatMap(java.util.Optional::stream)
                .collect(Collectors.toMap(HopDong::getId, Function.identity(), (a, b) -> a));

        Set<UUID> loaiIds = hopDongMap.values().stream()
                .map(HopDong::getLoaiHopDongId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<UUID> kieuIds = hopDongMap.values().stream()
                .map(HopDong::getKieuHopDongId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, LoaiHopDong> loaiMap = loaiIds.stream()
                .map(loaiHopDongRepository::findByIdAndNgayXoaIsNull)
                .flatMap(java.util.Optional::stream)
                .collect(Collectors.toMap(LoaiHopDong::getId, Function.identity(), (a, b) -> a));

        Map<UUID, KieuHopDong> kieuMap = kieuIds.stream()
                .map(kieuHopDongRepository::findByIdAndNgayXoaIsNull)
                .flatMap(java.util.Optional::stream)
                .collect(Collectors.toMap(KieuHopDong::getId, Function.identity(), (a, b) -> a));

        return source.stream().map(item -> toResponse(item, hopDongMap, loaiMap, kieuMap)).toList();
    }

    private HopDongLienKetResponse toResponse(
            HopDongLienKet item,
            Map<UUID, HopDong> hopDongMap,
            Map<UUID, LoaiHopDong> loaiMap,
            Map<UUID, KieuHopDong> kieuMap) {
        HopDongLienKetResponse response = new HopDongLienKetResponse();
        response.setId(item.getId());
        response.setHopDongId(item.getHopDongId());
        response.setHopDongLienKetId(item.getHopDongLienKetId());
        response.setGhiChu(item.getGhiChu());
        response.setNgayTao(item.getNgayTao());

        HopDong source = hopDongMap.get(item.getHopDongId());
        HopDong target = hopDongMap.get(item.getHopDongLienKetId());
        fillHopDongSide(response, source, target, loaiMap, kieuMap);
        return response;
    }

    private void fillHopDongSide(
            HopDongLienKetResponse response,
            HopDong source,
            HopDong target,
            Map<UUID, LoaiHopDong> loaiMap,
            Map<UUID, KieuHopDong> kieuMap) {
        if (source != null) {
            response.setHopDongMa(displayMa(source));
            response.setHopDongTen(source.getTen());
            LoaiHopDong loai = source.getLoaiHopDongId() == null ? null : loaiMap.get(source.getLoaiHopDongId());
            if (loai != null) {
                response.setHopDongLoaiTen(loai.getTen());
            }
            KieuHopDong kieu = source.getKieuHopDongId() == null ? null : kieuMap.get(source.getKieuHopDongId());
            if (kieu != null) {
                response.setHopDongKieuTen(kieu.getTen());
            }
        }
        if (target != null) {
            response.setHopDongLienKetMa(displayMa(target));
            response.setHopDongLienKetTen(target.getTen());
            LoaiHopDong loai = target.getLoaiHopDongId() == null ? null : loaiMap.get(target.getLoaiHopDongId());
            if (loai != null) {
                response.setHopDongLienKetLoaiTen(loai.getTen());
            }
            KieuHopDong kieu = target.getKieuHopDongId() == null ? null : kieuMap.get(target.getKieuHopDongId());
            if (kieu != null) {
                response.setHopDongLienKetKieuTen(kieu.getTen());
            }
        }
    }

    private String displayMa(HopDong hopDong) {
        if (hopDong.getMaHopDong() != null && !hopDong.getMaHopDong().isBlank()) {
            return hopDong.getMaHopDong().trim();
        }
        return hopDong.getMa() == null ? null : hopDong.getMa().trim();
    }

    private void ensureHopDongExists(UUID hopDongId) {
        hopDongService.findActiveEntityById(hopDongId)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_NOT_FOUND, "Không tìm thấy hợp đồng"));
    }

}
