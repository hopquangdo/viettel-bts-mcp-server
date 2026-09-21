package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucNhomCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucNhomTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucChiTietTreeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucCongViecResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucHopDongTreeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucKhoiLuongSanLuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucNhomHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucNhomResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucNhomSaoChepResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucNhomTreeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucChiTiet;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucCongViec;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucNhom;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.exception.HangMucErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.mapper.HangMucMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucChiTietRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucCongViecRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucNhomRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucCongViecService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucNhomService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.util.HangMucThanhTienCalculator;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.repository.SanLuongRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HangMucNhomServiceImpl implements HangMucNhomService {

    private final HangMucNhomRepository hangMucNhomRepository;
    private final HangMucChiTietRepository hangMucChiTietRepository;
    private final HangMucCongViecRepository hangMucCongViecRepository;
    private final HangMucMapper hangMucMapper;
    private final HangMucCongViecService hangMucCongViecService;
    private final SanLuongRepository sanLuongRepository;
    private final HopDongRepository hopDongRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HangMucNhomResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hopDongId) {
        String keyword = EntityFilter.normalizeSearch(search);
        List<HangMucNhom> source = includeDeleted ? hangMucNhomRepository.findAll() : hangMucNhomRepository.findByNgayXoaIsNull();
        return source.stream()
                .filter(entity -> EntityFilter.isActive(entity, HangMucNhom::getHoatDong, activeOnly))
                .filter(entity -> hopDongId == null || hopDongId.equals(entity.getHopDongId()))
                .filter(entity -> EntityFilter.matchesKeyword(keyword, EntityFilter.nullToEmpty(entity.getMa()), EntityFilter.nullToEmpty(entity.getTen()), EntityFilter.nullToEmpty(entity.getMoTa())))
                .sorted(Comparator.comparing(HangMucNhom::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(hangMucMapper::toHangMucNhomResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HangMucNhomResponse getById(UUID id) {
        return hangMucMapper.toHangMucNhomResponse(findById(id));
    }

    @Override
    @Transactional
    public HangMucNhomResponse create(HangMucNhomTaoRequest request) {
        HangMucNhom entity = hangMucMapper.fromHangMucNhomTaoRequest(request);
        entity.setMa(EntityFilter.normalizeCode(request.getMa()));
        if (request.getTen() != null) entity.setTen(request.getTen().trim());
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());
        if (entity.getThuTu() == null) {
            // Cột thu_tu NOT NULL — mapper ghi đè default của entity bằng null khi request bỏ trống
            entity.setThuTu((short) 0);
        }

        String normalizedCode = EntityFilter.normalizeCode(request.getMa());
        UUID hopDongId = request.getHopDongId();
        if (hopDongId == null) {
            throw new AppException(HangMucErrorCode.HANG_MUC_NHOM_NOT_FOUND, "Hợp đồng không hợp lệ");
        }
        if (hangMucNhomRepository.existsByMaIgnoreCaseAndHopDongIdAndNgayXoaIsNull(normalizedCode, hopDongId)) {
            throw new AppException(HangMucErrorCode.HANG_MUC_NHOM_CODE_EXISTS, "Mã đã tồn tại trong hợp đồng");
        }
        entity.setMa(normalizedCode);
        return hangMucMapper.toHangMucNhomResponse(hangMucNhomRepository.save(entity));
    }

    @Override
    @Transactional
    public HangMucNhomResponse update(UUID id, HangMucNhomCapNhatRequest request) {
        HangMucNhom entity = findById(id);
        hangMucMapper.updateFromHangMucNhomCapNhatRequest(request, entity);

        if (request.getMa() != null) {
            String normalizedCode = EntityFilter.normalizeCode(request.getMa());
            UUID hopDongId = entity.getHopDongId();
            if (hopDongId != null
                    && hangMucNhomRepository.existsByMaIgnoreCaseAndHopDongIdAndIdNotAndNgayXoaIsNull(normalizedCode, hopDongId, id)) {
                throw new AppException(HangMucErrorCode.HANG_MUC_NHOM_CODE_EXISTS, "Mã đã tồn tại trong hợp đồng");
            }
            entity.setMa(normalizedCode);
        }
        HangMucNhomResponse response = hangMucMapper.toHangMucNhomResponse(hangMucNhomRepository.save(entity));
        // Đổi mã nhóm có thể đổi kết quả công thức tham chiếu (@MÃ...) — denormalize lại tổng.
        recomputeTongThanhTienThiCong(entity.getHopDongId());
        return response;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        HangMucNhom entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        hangMucNhomRepository.save(entity);
        // Nhóm rời cây hạng mục — tổng thành tiền thi công của hợp đồng đổi theo.
        recomputeTongThanhTienThiCong(entity.getHopDongId());
    }

    @Override
    @Transactional
    public int deleteAllByHopDongId(UUID hopDongId) {
        int deleted = hangMucCongViecService.deleteAllHangMucByHopDongId(hopDongId);
        recomputeTongThanhTienThiCong(hopDongId);
        return deleted;
    }

    @Override
    @Transactional(readOnly = true)
    public HangMucKhoiLuongSanLuongResponse getKhoiLuongSanLuong(UUID hopDongId) {
        HangMucKhoiLuongSanLuongResponse response = new HangMucKhoiLuongSanLuongResponse();
        if (hopDongId == null) {
            return response;
        }
        for (Object[] row : sanLuongRepository.sumKhoiLuongTheoCongViecByHopDong(hopDongId)) {
            response.getTheoCongViec().put((UUID) row[0], (BigDecimal) row[1]);
        }
        for (Object[] row : sanLuongRepository.sumKhoiLuongTheoChiTietByHopDong(hopDongId)) {
            response.getTheoChiTiet().put((UUID) row[0], (BigDecimal) row[1]);
        }
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public HangMucNhomHopDongResponse listByHopDongWithKhoiLuong(UUID hopDongId, Boolean activeOnly) {
        HangMucNhomHopDongResponse response = new HangMucNhomHopDongResponse();
        if (hopDongId == null) {
            return response;
        }
        response.setKhoiLuongSanLuong(getKhoiLuongSanLuong(hopDongId));
        response.setNhom(list(null, activeOnly, false, hopDongId));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public HangMucHopDongTreeResponse getTreeByHopDongId(UUID hopDongId, Boolean activeOnly) {
        HangMucHopDongTreeResponse tree = new HangMucHopDongTreeResponse();
        if (hopDongId == null) {
            return tree;
        }
        Map<UUID, HangMucHopDongTreeResponse> trees = getTreesByHopDongIds(List.of(hopDongId), activeOnly);
        return trees.getOrDefault(hopDongId, tree);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, HangMucHopDongTreeResponse> getTreesByHopDongIds(
            Collection<UUID> hopDongIds, Boolean activeOnly) {
        return getTreesByHopDongIds(hopDongIds, activeOnly, true);
    }

    @Override
    @Transactional
    public void recomputeTongThanhTienThiCong(UUID hopDongId) {
        if (hopDongId == null) return;
        HangMucHopDongTreeResponse tree = getTreeByHopDongId(hopDongId, true);
        BigDecimal tongThanhTien = HangMucThanhTienCalculator.compute(tree).getTongThanhTien();
        hopDongRepository.updateTongThanhTienThiCong(hopDongId, tongThanhTien);
    }

    @Override
    @Transactional
    public HangMucNhomSaoChepResponse copyFromHopDong(UUID hopDongNguonId, UUID hopDongDichId) {
        if (hopDongNguonId == null || hopDongDichId == null) {
            throw new AppException(HangMucErrorCode.HANG_MUC_SAO_CHEP_INVALID, "Thiếu hợp đồng nguồn/đích");
        }
        if (hopDongNguonId.equals(hopDongDichId)) {
            throw new AppException(HangMucErrorCode.HANG_MUC_SAO_CHEP_INVALID, "Hợp đồng nguồn và đích phải khác nhau");
        }
        if (!hopDongRepository.existsById(hopDongNguonId) || !hopDongRepository.existsById(hopDongDichId)) {
            throw new AppException(HangMucErrorCode.HANG_MUC_NHOM_NOT_FOUND, "Không tìm thấy hợp đồng nguồn/đích");
        }

        List<HangMucNhom> sourceNhoms = hangMucNhomRepository.findByHopDongIdAndNgayXoaIsNull(hopDongNguonId);
        if (sourceNhoms.isEmpty()) {
            throw new AppException(HangMucErrorCode.HANG_MUC_SAO_CHEP_NGUON_RONG, "Hợp đồng nguồn chưa có hạng mục thi công");
        }

        Set<String> existingTargetMa = hangMucNhomRepository.findByHopDongIdAndNgayXoaIsNull(hopDongDichId).stream()
                .map(n -> n.getMa().toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(HashSet::new));

        List<UUID> sourceNhomIds = sourceNhoms.stream().map(HangMucNhom::getId).toList();
        Map<UUID, List<HangMucChiTiet>> chiTietByNhomId = hangMucChiTietRepository
                .findByHangMucNhomIdInAndNgayXoaIsNull(sourceNhomIds).stream()
                .collect(Collectors.groupingBy(HangMucChiTiet::getHangMucNhomId));

        List<UUID> sourceChiTietIds = chiTietByNhomId.values().stream()
                .flatMap(List::stream).map(HangMucChiTiet::getId).toList();
        Map<UUID, List<HangMucCongViec>> congViecByChiTietId = sourceChiTietIds.isEmpty()
                ? Map.of()
                : hangMucCongViecRepository.findByHangMucChiTietIdInAndNgayXoaIsNull(sourceChiTietIds).stream()
                        .collect(Collectors.groupingBy(HangMucCongViec::getHangMucChiTietId));

        // 1) Sao chép nhóm — giữ nguyên thứ tự source để khớp lại savedNhoms theo index.
        Map<UUID, HangMucNhom> newNhomBySourceId = new LinkedHashMap<>();
        for (HangMucNhom source : sourceNhoms) {
            HangMucNhom copy = new HangMucNhom();
            copy.setHopDongId(hopDongDichId);
            copy.setMa(resolveUniqueMa(source.getMa(), existingTargetMa));
            copy.setTen(source.getTen());
            copy.setMoTa(source.getMoTa());
            copy.setThuTu(source.getThuTu());
            copy.setHoatDong(true);
            newNhomBySourceId.put(source.getId(), copy);
        }
        hangMucNhomRepository.saveAll(newNhomBySourceId.values());

        // 2) Sao chép chi tiết (thuộc nhóm vừa tạo).
        Map<UUID, HangMucChiTiet> newChiTietBySourceId = new LinkedHashMap<>();
        for (HangMucNhom source : sourceNhoms) {
            UUID newNhomId = newNhomBySourceId.get(source.getId()).getId();
            for (HangMucChiTiet sourceItem : chiTietByNhomId.getOrDefault(source.getId(), List.of())) {
                HangMucChiTiet copy = new HangMucChiTiet();
                copy.setHangMucNhomId(newNhomId);
                copy.setMa(sourceItem.getMa());
                copy.setTen(sourceItem.getTen());
                copy.setDonVi(sourceItem.getDonVi());
                copy.setDonGia(sourceItem.getDonGia());
                copy.setKhoiLuong(sourceItem.getKhoiLuong());
                copy.setCongThucKhoiLuong(sourceItem.getCongThucKhoiLuong());
                copy.setCongThucDonGia(sourceItem.getCongThucDonGia());
                copy.setCongThucThanhTien(sourceItem.getCongThucThanhTien());
                copy.setViTriThiCong(sourceItem.getViTriThiCong());
                copy.setTrangThai(sourceItem.getTrangThai());
                copy.setHoatDong(true);
                newChiTietBySourceId.put(sourceItem.getId(), copy);
            }
        }
        hangMucChiTietRepository.saveAll(newChiTietBySourceId.values());

        // 3) Sao chép công việc (thuộc chi tiết vừa tạo).
        List<HangMucCongViec> newCongViecs = new ArrayList<>();
        for (Map.Entry<UUID, HangMucChiTiet> entry : newChiTietBySourceId.entrySet()) {
            UUID newChiTietId = entry.getValue().getId();
            for (HangMucCongViec sourceTask : congViecByChiTietId.getOrDefault(entry.getKey(), List.of())) {
                HangMucCongViec copy = new HangMucCongViec();
                copy.setHangMucChiTietId(newChiTietId);
                copy.setMa(sourceTask.getMa());
                copy.setTen(sourceTask.getTen());
                copy.setDonVi(sourceTask.getDonVi());
                copy.setDonGia(sourceTask.getDonGia());
                copy.setKhoiLuong(sourceTask.getKhoiLuong());
                copy.setCongThucKhoiLuong(sourceTask.getCongThucKhoiLuong());
                copy.setCongThucDonGia(sourceTask.getCongThucDonGia());
                copy.setCongThucThanhTien(sourceTask.getCongThucThanhTien());
                copy.setViTriThiCong(sourceTask.getViTriThiCong());
                copy.setThuTu(sourceTask.getThuTu());
                copy.setGhiChu(sourceTask.getGhiChu());
                copy.setTrangThai(sourceTask.getTrangThai());
                newCongViecs.add(copy);
            }
        }
        hangMucCongViecRepository.saveAll(newCongViecs);

        recomputeTongThanhTienThiCong(hopDongDichId);

        return new HangMucNhomSaoChepResponse(
                newNhomBySourceId.size(), newChiTietBySourceId.size(), newCongViecs.size());
    }

    /** Mã hạng mục nhóm chỉ unique theo từng hợp đồng — nếu trùng ở đích thì thêm hậu tố -2, -3... */
    private static String resolveUniqueMa(String sourceMa, Set<String> existingTargetMaLower) {
        String base = sourceMa == null ? "" : sourceMa.trim();
        String candidate = base;
        int suffix = 2;
        while (existingTargetMaLower.contains(candidate.toLowerCase(Locale.ROOT))) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        existingTargetMaLower.add(candidate.toLowerCase(Locale.ROOT));
        return candidate;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, HangMucHopDongTreeResponse> getTreesByHopDongIds(
            Collection<UUID> hopDongIds, Boolean activeOnly, boolean includeKhoiLuongSanLuong) {
        Map<UUID, HangMucHopDongTreeResponse> result = new LinkedHashMap<>();
        if (hopDongIds == null || hopDongIds.isEmpty()) {
            return result;
        }
        List<UUID> ids = hopDongIds.stream().filter(id -> id != null).distinct().toList();
        if (ids.isEmpty()) {
            return result;
        }
        for (UUID hopDongId : ids) {
            result.put(hopDongId, new HangMucHopDongTreeResponse());
        }

        List<HangMucNhom> allGroups = hangMucNhomRepository.findByHopDongIdInAndNgayXoaIsNull(ids).stream()
                .filter(group -> EntityFilter.isActive(group, HangMucNhom::getHoatDong, activeOnly))
                .sorted(Comparator
                        .comparing(HangMucNhom::getThuTu, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(group -> EntityFilter.nullToEmpty(group.getMa()), String.CASE_INSENSITIVE_ORDER))
                .toList();
        if (allGroups.isEmpty()) {
            if (includeKhoiLuongSanLuong) {
                for (UUID hopDongId : ids) {
                    result.get(hopDongId).setKhoiLuongSanLuong(getKhoiLuongSanLuong(hopDongId));
                }
            }
            return result;
        }

        List<UUID> groupIds = allGroups.stream().map(HangMucNhom::getId).toList();
        List<HangMucChiTiet> chiTiets = hangMucChiTietRepository.findByHangMucNhomIdInAndNgayXoaIsNull(groupIds).stream()
                .filter(item -> EntityFilter.isActive(item, HangMucChiTiet::getHoatDong, activeOnly))
                .sorted(Comparator.comparing(
                        item -> EntityFilter.nullToEmpty(item.getMa()),
                        String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<UUID> chiTietIds = chiTiets.stream().map(HangMucChiTiet::getId).toList();
        Map<UUID, List<HangMucCongViecResponse>> congViecByChiTietId = chiTietIds.isEmpty()
                ? Map.of()
                : hangMucCongViecRepository.findByHangMucChiTietIdInAndNgayXoaIsNull(chiTietIds).stream()
                        .sorted(Comparator
                                .comparing(HangMucCongViec::getThuTu, Comparator.nullsLast(Comparator.naturalOrder()))
                                .thenComparing(
                                        task -> EntityFilter.nullToEmpty(task.getMa()),
                                        String.CASE_INSENSITIVE_ORDER))
                        .map(hangMucMapper::toHangMucCongViecResponse)
                        .collect(Collectors.groupingBy(HangMucCongViecResponse::getHangMucChiTietId));

        Map<UUID, List<HangMucChiTietTreeResponse>> chiTietByGroupId = chiTiets.stream()
                .map(item -> {
                    HangMucChiTietTreeResponse node = hangMucMapper.toHangMucChiTietTreeResponse(item);
                    node.setCongViec(new ArrayList<>(congViecByChiTietId.getOrDefault(item.getId(), List.of())));
                    return node;
                })
                .collect(Collectors.groupingBy(HangMucChiTietTreeResponse::getHangMucNhomId));

        Map<UUID, List<HangMucNhom>> groupsByHopDong = allGroups.stream()
                .collect(Collectors.groupingBy(HangMucNhom::getHopDongId, LinkedHashMap::new, Collectors.toList()));

        for (UUID hopDongId : ids) {
            HangMucHopDongTreeResponse tree = result.get(hopDongId);
            if (includeKhoiLuongSanLuong) {
                tree.setKhoiLuongSanLuong(getKhoiLuongSanLuong(hopDongId));
            }
            List<HangMucNhomTreeResponse> nhomNodes = groupsByHopDong.getOrDefault(hopDongId, List.of()).stream()
                    .map(group -> {
                        HangMucNhomTreeResponse node = hangMucMapper.toHangMucNhomTreeResponse(group);
                        node.setChiTiet(new ArrayList<>(chiTietByGroupId.getOrDefault(group.getId(), List.of())));
                        return node;
                    })
                    .toList();
            tree.setNhom(nhomNodes);
        }
        return result;
    }

    private HangMucNhom findById(UUID id) {
        return hangMucNhomRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(HangMucErrorCode.HANG_MUC_NHOM_NOT_FOUND, "Không tìm thấy bản ghi"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HangMucNhom> findActiveEntitiesByHopDongId(UUID hopDongId) {
        return hangMucNhomRepository.findByHopDongIdAndNgayXoaIsNull(hopDongId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HangMucNhom> findActiveEntitiesByHopDongIds(Collection<UUID> hopDongIds) {
        return hangMucNhomRepository.findByHopDongIdInAndNgayXoaIsNull(hopDongIds);
    }
}
