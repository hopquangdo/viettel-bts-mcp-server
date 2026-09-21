package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuDuLieu;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ThuocTinhHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.mapper.CauHinhMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuDuLieuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.ThuocTinhHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhHopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.LienKetBangSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.TuyChonSupport;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThuocTinhHopDongServiceImpl implements ThuocTinhHopDongService {

    private final ThuocTinhHopDongRepository thuocTinhHopDongRepository;
    private final KieuDuLieuRepository kieuDuLieuRepository;
    private final CauHinhMapper cauHinhMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ThuocTinhHopDongResponse> list(String search, Boolean activeOnly, boolean includeDeleted) {
        String keyword = EntityFilter.normalizeSearch(search);
        return thuocTinhHopDongRepository.search(includeDeleted, activeOnly, keyword)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ThuocTinhHopDongResponse getById(java.util.UUID id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, ThuocTinhHopDongResponse> mapActiveByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        List<UUID> idList = ids.stream().filter(id -> id != null).distinct().toList();
        if (idList.isEmpty()) {
            return Map.of();
        }
        return thuocTinhHopDongRepository.findByIdInAndNgayXoaIsNull(idList).stream()
                .filter(entity -> Boolean.TRUE.equals(entity.getHoatDong()))
                .map(this::toResponse)
                .collect(Collectors.toMap(
                        ThuocTinhHopDongResponse::getId,
                        Function.identity(),
                        (left, right) -> left));
    }

    @Override
    @Transactional
    public ThuocTinhHopDongResponse create(ThuocTinhHopDongTaoRequest request) {
        ThuocTinhHopDong entity = cauHinhMapper.fromThuocTinhHopDongTaoRequest(request);
        entity.setTen(request.getTen().trim());
        entity.setKieuDuLieuId(resolveKieuDuLieuId(request.getKieuDuLieuId(), request.getLienKetBang()));
        if (request.getDonVi() != null) {
            entity.setDonVi(request.getDonVi().trim());
        }
        entity.setTuyChon(TuyChonSupport.normalizeForType(entity.getKieuDuLieuId(), request.getTuyChon()));
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());
        return toResponse(thuocTinhHopDongRepository.save(entity));
    }

    @Override
    @Transactional
    public ThuocTinhHopDongResponse update(java.util.UUID id, ThuocTinhHopDongCapNhatRequest request) {
        ThuocTinhHopDong entity = findById(id);
        cauHinhMapper.updateFromThuocTinhHopDongCapNhatRequest(request, entity);
        if (request.getLienKetBang() != null && !request.getLienKetBang().isBlank()) {
            entity.setKieuDuLieuId(resolveKieuDuLieuId(null, request.getLienKetBang()));
        } else if (request.getKieuDuLieuId() != null && !request.getKieuDuLieuId().isBlank()) {
            entity.setKieuDuLieuId(request.getKieuDuLieuId().trim());
        }
        if (request.getTuyChon() != null) {
            entity.setTuyChon(TuyChonSupport.normalizeForType(entity.getKieuDuLieuId(), request.getTuyChon()));
        } else if (!TuyChonSupport.requiresOptions(entity.getKieuDuLieuId())) {
            entity.setTuyChon(List.of());
        } else {
            TuyChonSupport.normalizeForType(entity.getKieuDuLieuId(), entity.getTuyChon());
        }
        return toResponse(thuocTinhHopDongRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(java.util.UUID id) {
        ThuocTinhHopDong entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        thuocTinhHopDongRepository.save(entity);
    }

    private ThuocTinhHopDong findById(java.util.UUID id) {
        return thuocTinhHopDongRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(
                        CauHinhErrorCode.THUOC_TINH_HOP_DONG_NOT_FOUND, "Không tìm thấy thuộc tính HĐ"));
    }

    private String resolveKieuDuLieuId(String kieuDuLieuId, String lienKetBang) {
        if (lienKetBang != null && !lienKetBang.isBlank()) {
            String table = lienKetBang.trim();
            if (table.length() > 20) {
                throw new AppException(CauHinhErrorCode.KIEU_DU_LIEU_NOT_FOUND, "Tên bảng liên kết quá dài");
            }
            if (!LienKetBangSupport.ALLOWED_LINK_TABLES.contains(table)) {
                throw new AppException(CauHinhErrorCode.KIEU_DU_LIEU_NOT_FOUND, "Bảng liên kết không hợp lệ: " + table);
            }
            ensureKieuDuLieuForTable(table);
            return table;
        }
        if (kieuDuLieuId == null || kieuDuLieuId.isBlank()) {
            throw new AppException(CauHinhErrorCode.KIEU_DU_LIEU_NOT_FOUND, "Chọn kiểu dữ liệu hoặc bảng liên kết");
        }
        return kieuDuLieuId.trim();
    }

    private void ensureKieuDuLieuForTable(String table) {
        kieuDuLieuRepository.findByLienKetBangAndNgayXoaIsNull(table)
                .orElseGet(() -> {
                    KieuDuLieu entity = new KieuDuLieu();
                    entity.setTen(table);
                    entity.setLienKetBang(table);
                    entity.setHoatDong(true);
                    return kieuDuLieuRepository.save(entity);
                });
    }

    private ThuocTinhHopDongResponse toResponse(ThuocTinhHopDong entity) {
        ThuocTinhHopDongResponse response = cauHinhMapper.toThuocTinhHopDongResponse(entity);
        response.setLienKetBang(
                LienKetBangSupport.resolveLienKetBang(entity.getKieuDuLieuId(), kieuDuLieuRepository));
        return response;
    }
}
