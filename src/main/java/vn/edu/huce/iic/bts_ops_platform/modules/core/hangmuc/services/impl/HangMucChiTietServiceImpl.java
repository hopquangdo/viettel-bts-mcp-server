package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucChiTietCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucChiTietTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucChiTietResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucChiTiet;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucNhom;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.exception.HangMucErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.mapper.HangMucMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucChiTietRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucNhomRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucChiTietService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucNhomService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.util.HangMucFormulaFieldUpdater;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HangMucChiTietServiceImpl implements HangMucChiTietService {

    private final HangMucChiTietRepository hangMucChiTietRepository;
    private final HangMucNhomRepository hangMucNhomRepository;
    private final HangMucMapper hangMucMapper;
    // Không tạo vòng bean: HangMucNhomServiceImpl không phụ thuộc HangMucChiTietService.
    private final HangMucNhomService hangMucNhomService;

    @Override
    @Transactional(readOnly = true)
    public List<HangMucChiTietResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hangMucNhomId) {
        String keyword = EntityFilter.normalizeSearch(search);
        List<HangMucChiTiet> source = includeDeleted ? hangMucChiTietRepository.findAll() : hangMucChiTietRepository.findByNgayXoaIsNull();
        return source.stream()
                .filter(entity -> EntityFilter.isActive(entity, HangMucChiTiet::getHoatDong, activeOnly))
                .filter(entity -> hangMucNhomId == null || hangMucNhomId.equals(entity.getHangMucNhomId()))
                .filter(entity -> EntityFilter.matchesKeyword(keyword, EntityFilter.nullToEmpty(entity.getMa()), EntityFilter.nullToEmpty(entity.getTen()), EntityFilter.nullToEmpty(entity.getDonVi()), EntityFilter.nullToEmpty(entity.getViTriThiCong()), EntityFilter.nullToEmpty(entity.getTrangThai())))
                .sorted(Comparator.comparing(HangMucChiTiet::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(hangMucMapper::toHangMucChiTietResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HangMucChiTietResponse getById(UUID id) {
        return hangMucMapper.toHangMucChiTietResponse(findById(id));
    }

    @Override
    @Transactional
    public HangMucChiTietResponse create(HangMucChiTietTaoRequest request) {
        HangMucChiTiet entity = hangMucMapper.fromHangMucChiTietTaoRequest(request);
        entity.setMa(EntityFilter.normalizeCode(request.getMa()));
        if (request.getTen() != null) entity.setTen(request.getTen().trim());
        if (request.getDonVi() != null) entity.setDonVi(request.getDonVi().trim());
        if (request.getViTriThiCong() != null) entity.setViTriThiCong(request.getViTriThiCong().trim());
        if (request.getTrangThai() != null) entity.setTrangThai(request.getTrangThai().trim());
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());
        HangMucFormulaFieldUpdater.apply(request, entity);
        ensureDonGiaDefault(entity);

        String normalizedCode = EntityFilter.normalizeCode(request.getMa());
        UUID hopDongId = resolveHopDongIdByNhomId(request.getHangMucNhomId());
        if (hangMucChiTietRepository.existsByMaIgnoreCaseAndHopDongIdAndNgayXoaIsNull(normalizedCode, hopDongId)) {
            throw new AppException(HangMucErrorCode.HANG_MUC_CHI_TIET_CODE_EXISTS, "Mã đã tồn tại trong hợp đồng");
        }
        entity.setMa(normalizedCode);
        HangMucChiTietResponse response = hangMucMapper.toHangMucChiTietResponse(hangMucChiTietRepository.save(entity));
        // KL/DG/công thức của chi tiết đổi tổng thành tiền thi công — denormalize ngay trong TX,
        // không đợi SanLuongChangedEvent (hạng mục đổi tay không phát event sản lượng).
        hangMucNhomService.recomputeTongThanhTienThiCong(hopDongId);
        return response;
    }

    @Override
    @Transactional
    public HangMucChiTietResponse update(UUID id, HangMucChiTietCapNhatRequest request) {
        HangMucChiTiet entity = findById(id);
        hangMucMapper.updateFromHangMucChiTietCapNhatRequest(request, entity);
        HangMucFormulaFieldUpdater.apply(request, entity);
        ensureDonGiaDefault(entity);
        if (request.getViTriThiCong() != null) {
            entity.setViTriThiCong(request.getViTriThiCong().trim());
        }

        if (request.getMa() != null) {
            String normalizedCode = EntityFilter.normalizeCode(request.getMa());
            UUID hopDongId = resolveHopDongIdByNhomId(entity.getHangMucNhomId());
            if (hangMucChiTietRepository.existsByMaIgnoreCaseAndHopDongIdAndIdNotAndNgayXoaIsNull(normalizedCode, hopDongId, id)) {
                throw new AppException(HangMucErrorCode.HANG_MUC_CHI_TIET_CODE_EXISTS, "Mã đã tồn tại trong hợp đồng");
            }
            entity.setMa(normalizedCode);
        }
        HangMucChiTietResponse response = hangMucMapper.toHangMucChiTietResponse(hangMucChiTietRepository.save(entity));
        hangMucNhomService.recomputeTongThanhTienThiCong(findHopDongIdByNhomId(entity.getHangMucNhomId()));
        return response;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        HangMucChiTiet entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        hangMucChiTietRepository.save(entity);
        hangMucNhomService.recomputeTongThanhTienThiCong(findHopDongIdByNhomId(entity.getHangMucNhomId()));
    }

    /** Như resolveHopDongIdByNhomId nhưng trả null thay vì ném lỗi (nhóm đã xóa mềm) —
     * recomputeTongThanhTienThiCong tự no-op với null, không được làm hỏng thao tác gốc. */
    private UUID findHopDongIdByNhomId(UUID hangMucNhomId) {
        if (hangMucNhomId == null) return null;
        return hangMucNhomRepository.findById(hangMucNhomId).map(HangMucNhom::getHopDongId).orElse(null);
    }

    private HangMucChiTiet findById(UUID id) {
        return hangMucChiTietRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(HangMucErrorCode.HANG_MUC_CHI_TIET_NOT_FOUND, "Không tìm thấy bản ghi"));
    }

    private UUID resolveHopDongIdByNhomId(UUID hangMucNhomId) {
        return hangMucNhomRepository.findByIdAndNgayXoaIsNull(hangMucNhomId)
                .map(HangMucNhom::getHopDongId)
                .filter(hopDongId -> hopDongId != null)
                .orElseThrow(() -> new AppException(HangMucErrorCode.HANG_MUC_NHOM_NOT_FOUND, "Không tìm thấy nhóm hạng mục"));
    }

    /** Cột don_gia NOT NULL — đồng bộ với HangMucCongViecServiceImpl khi client không gửi đơn giá. */
    private static void ensureDonGiaDefault(HangMucChiTiet entity) {
        if (entity.getDonGia() == null) {
            entity.setDonGia(BigDecimal.ZERO);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HangMucChiTiet> findActiveEntitiesByNhomIds(Collection<UUID> hangMucNhomIds) {
        return hangMucChiTietRepository.findByHangMucNhomIdInAndNgayXoaIsNull(new ArrayList<>(hangMucNhomIds));
    }
}
