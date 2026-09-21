package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucCongViecCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucCongViecTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucCongViecResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucChiTiet;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucCongViec;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucNhom;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.exception.HangMucErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.mapper.HangMucMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucChiTietRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucCongViecRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucNhomRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucCongViecService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.util.HangMucFormulaFieldUpdater;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HangMucCongViecServiceImpl implements HangMucCongViecService {

    private final HangMucCongViecRepository hangMucCongViecRepository;
    private final HangMucNhomRepository hangMucNhomRepository;
    private final HangMucChiTietRepository hangMucChiTietRepository;
    private final HangMucMapper hangMucMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HangMucCongViecResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hangMucChiTietId) {
        String keyword = EntityFilter.normalizeSearch(search);
        List<HangMucCongViec> source = includeDeleted ? hangMucCongViecRepository.findAll() : hangMucCongViecRepository.findByNgayXoaIsNull();
        return source.stream()
                .filter(entity -> hangMucChiTietId == null || hangMucChiTietId.equals(entity.getHangMucChiTietId()))
                .filter(entity -> EntityFilter.matchesKeyword(keyword, EntityFilter.nullToEmpty(entity.getMa()), EntityFilter.nullToEmpty(entity.getTen()), EntityFilter.nullToEmpty(entity.getDonVi()), EntityFilter.nullToEmpty(entity.getViTriThiCong()), EntityFilter.nullToEmpty(entity.getGhiChu()), EntityFilter.nullToEmpty(entity.getTrangThai())))
                .sorted(Comparator.comparing(HangMucCongViec::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(hangMucMapper::toHangMucCongViecResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HangMucCongViecResponse getById(UUID id) {
        return hangMucMapper.toHangMucCongViecResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, HangMucCongViecResponse> getByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return hangMucCongViecRepository.findAllById(ids).stream()
                .filter(entity -> entity.getNgayXoa() == null)
                .collect(Collectors.toMap(
                        HangMucCongViec::getId,
                        hangMucMapper::toHangMucCongViecResponse,
                        (left, right) -> left));
    }

    @Override
    @Transactional
    public HangMucCongViecResponse create(HangMucCongViecTaoRequest request) {
        HangMucCongViec entity = hangMucMapper.fromHangMucCongViecTaoRequest(request);
        entity.setMa(EntityFilter.normalizeCode(request.getMa()));
        if (request.getTen() != null) entity.setTen(request.getTen().trim());
        if (request.getDonVi() != null) entity.setDonVi(request.getDonVi().trim());
        if (request.getViTriThiCong() != null) entity.setViTriThiCong(request.getViTriThiCong().trim());
        if (request.getTrangThai() != null) entity.setTrangThai(request.getTrangThai().trim());
        if (entity.getDonGia() == null) {
            entity.setDonGia(BigDecimal.ZERO);
        }
        entity.setThuTu(resolveThuTu(request.getThuTu(), request.getHangMucChiTietId()));
        HangMucFormulaFieldUpdater.apply(request, entity);

        String normalizedCode = EntityFilter.normalizeCode(request.getMa());
        UUID hopDongId = resolveHopDongIdByChiTietId(request.getHangMucChiTietId());
        if (hangMucCongViecRepository.existsByMaIgnoreCaseAndHopDongIdAndNgayXoaIsNull(normalizedCode, hopDongId)) {
            throw new AppException(HangMucErrorCode.HANG_MUC_CONG_VIEC_CODE_EXISTS, "Mã đã tồn tại trong hợp đồng");
        }
        entity.setMa(normalizedCode);
        return hangMucMapper.toHangMucCongViecResponse(hangMucCongViecRepository.save(entity));
    }

    @Override
    @Transactional
    public HangMucCongViecResponse update(UUID id, HangMucCongViecCapNhatRequest request) {
        HangMucCongViec entity = findById(id);
        hangMucMapper.updateFromHangMucCongViecCapNhatRequest(request, entity);
        HangMucFormulaFieldUpdater.apply(request, entity);

        if (request.getMa() != null) {
            String normalizedCode = EntityFilter.normalizeCode(request.getMa());
            UUID hopDongId = resolveHopDongIdByChiTietId(entity.getHangMucChiTietId());
            if (hangMucCongViecRepository.existsByMaIgnoreCaseAndHopDongIdAndIdNotAndNgayXoaIsNull(normalizedCode, hopDongId, id)) {
                throw new AppException(HangMucErrorCode.HANG_MUC_CONG_VIEC_CODE_EXISTS, "Mã đã tồn tại trong hợp đồng");
            }
            entity.setMa(normalizedCode);
        }
        return hangMucMapper.toHangMucCongViecResponse(hangMucCongViecRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        HangMucCongViec entity = findById(id);
        entity.setNgayXoa(Instant.now());
        hangMucCongViecRepository.save(entity);
    }

    @Override
    @Transactional
    public int deleteAllByHopDongId(UUID hopDongId) {
        if (hopDongId == null) {
            return 0;
        }
        Instant now = Instant.now();
        int count = 0;
        for (HangMucNhom group : hangMucNhomRepository.findByHopDongIdAndNgayXoaIsNull(hopDongId)) {
            for (HangMucChiTiet item : hangMucChiTietRepository.findByHangMucNhomIdAndNgayXoaIsNull(group.getId())) {
                for (HangMucCongViec task : hangMucCongViecRepository.findByHangMucChiTietIdAndNgayXoaIsNull(item.getId())) {
                    task.setNgayXoa(now);
                    hangMucCongViecRepository.save(task);
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    @Transactional
    public int deleteAllHangMucByHopDongId(UUID hopDongId) {
        if (hopDongId == null) {
            return 0;
        }
        Instant now = Instant.now();
        int count = 0;
        for (HangMucNhom group : hangMucNhomRepository.findByHopDongIdAndNgayXoaIsNull(hopDongId)) {
            for (HangMucChiTiet item : hangMucChiTietRepository.findByHangMucNhomIdAndNgayXoaIsNull(group.getId())) {
                for (HangMucCongViec task : hangMucCongViecRepository.findByHangMucChiTietIdAndNgayXoaIsNull(item.getId())) {
                    task.setNgayXoa(now);
                    hangMucCongViecRepository.save(task);
                    count++;
                }
                item.setNgayXoa(now);
                item.setHoatDong(false);
                hangMucChiTietRepository.save(item);
                count++;
            }
            group.setNgayXoa(now);
            group.setHoatDong(false);
            hangMucNhomRepository.save(group);
            count++;
        }
        return count;
    }

    private HangMucCongViec findById(UUID id) {
        return hangMucCongViecRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(HangMucErrorCode.HANG_MUC_CONG_VIEC_NOT_FOUND, "Không tìm thấy bản ghi"));
    }

    private UUID resolveHopDongIdByChiTietId(UUID hangMucChiTietId) {
        HangMucChiTiet chiTiet = hangMucChiTietRepository.findByIdAndNgayXoaIsNull(hangMucChiTietId)
                .orElseThrow(() -> new AppException(HangMucErrorCode.HANG_MUC_CHI_TIET_NOT_FOUND, "Không tìm thấy hạng mục"));
        return hangMucNhomRepository.findByIdAndNgayXoaIsNull(chiTiet.getHangMucNhomId())
                .map(HangMucNhom::getHopDongId)
                .filter(hopDongId -> hopDongId != null)
                .orElseThrow(() -> new AppException(HangMucErrorCode.HANG_MUC_NHOM_NOT_FOUND, "Không tìm thấy nhóm hạng mục"));
    }

    private Short resolveThuTu(Short requested, UUID hangMucChiTietId) {
        if (requested != null) {
            return requested;
        }
        int next = hangMucCongViecRepository.findByHangMucChiTietIdAndNgayXoaIsNull(hangMucChiTietId).stream()
                .map(HangMucCongViec::getThuTu)
                .filter(value -> value != null)
                .mapToInt(Short::intValue)
                .max()
                .orElse(-1) + 1;
        return (short) next;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HangMucCongViec> findActiveEntitiesByChiTietIds(Collection<UUID> hangMucChiTietIds) {
        return hangMucCongViecRepository.findByHangMucChiTietIdInAndNgayXoaIsNull(new ArrayList<>(hangMucChiTietIds));
    }
}
