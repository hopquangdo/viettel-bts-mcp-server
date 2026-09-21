package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuDuLieu;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ThuocTinh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.mapper.CauHinhMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.DoiTuongQuanLyRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuDuLieuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.ThuocTinhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.LienKetBangSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.TuyChonSupport;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ThuocTinhServiceImpl implements ThuocTinhService {

    private final ThuocTinhRepository thuocTinhRepository;
    private final DoiTuongQuanLyRepository doiTuongQuanLyRepository;
    private final KieuDuLieuRepository kieuDuLieuRepository;
    private final CauHinhMapper cauHinhMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ThuocTinhResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID doiTuongQuanLyId, Boolean selectorOnly) {
        String keyword = EntityFilter.normalizeSearch(search);
        Map<String, String> lienKetBangByTen = loadLienKetBangByTen();
        return thuocTinhRepository.search(includeDeleted, activeOnly, doiTuongQuanLyId, selectorOnly, keyword)
                .stream()
                .map(entity -> toThuocTinhResponse(entity, lienKetBangByTen))
                .toList();
    }

    /**
     * Giống {@link #list} nhưng bỏ bước phân giải lienKetBang — tránh N+1 xuống kieu_du_lieu.
     * Cùng query, cùng mapper; chỉ khác là {@code lienKetBang} để null.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ThuocTinhResponse> listLite(String search, Boolean activeOnly, boolean includeDeleted, UUID doiTuongQuanLyId, Boolean selectorOnly) {
        String keyword = EntityFilter.normalizeSearch(search);
        return thuocTinhRepository.search(includeDeleted, activeOnly, doiTuongQuanLyId, selectorOnly, keyword)
                .stream()
                .map(cauHinhMapper::toThuocTinhResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, List<ThuocTinhResponse>> listGroupedByDoiTuongQuanLyIds(Collection<UUID> doiTuongQuanLyIds) {
        if (doiTuongQuanLyIds == null || doiTuongQuanLyIds.isEmpty()) {
            return Map.of();
        }
        List<UUID> ids = doiTuongQuanLyIds.stream().filter(id -> id != null).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<UUID, List<ThuocTinhResponse>> grouped = new LinkedHashMap<>();
        for (UUID id : ids) {
            grouped.put(id, new java.util.ArrayList<>());
        }
        Map<String, String> lienKetBangByTen = loadLienKetBangByTen();
        thuocTinhRepository.findByDoiTuongQuanLyIdInAndNgayXoaIsNullOrderByTenAsc(ids)
                .forEach(entity -> grouped.computeIfAbsent(entity.getDoiTuongQuanLyId(), key -> new java.util.ArrayList<>())
                        .add(toThuocTinhResponse(entity, lienKetBangByTen)));
        return grouped;
    }

    @Override
    @Transactional(readOnly = true)
    public ThuocTinhResponse getById(UUID id) {
        return toThuocTinhResponse(findById(id));
    }

    @Override
    @Transactional
    public ThuocTinhResponse create(ThuocTinhTaoRequest request) {
        ensureDoiTuongQuanLyExists(request.getDoiTuongQuanLyId());
        ThuocTinh entity = cauHinhMapper.fromThuocTinhTaoRequest(request);
        if (request.getTen() != null) entity.setTen(request.getTen().trim());
        entity.setKieuDuLieuId(resolveKieuDuLieuId(request.getKieuDuLieuId(), request.getLienKetBang()));
        if (request.getDonVi() != null) entity.setDonVi(request.getDonVi().trim());
        entity.setTuyChon(TuyChonSupport.normalizeForType(entity.getKieuDuLieuId(), request.getTuyChon()));
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        return toThuocTinhResponse(thuocTinhRepository.save(entity));
    }

    @Override
    @Transactional
    public ThuocTinhResponse update(UUID id, ThuocTinhCapNhatRequest request) {
        ThuocTinh entity = findById(id);
        if (request.getDoiTuongQuanLyId() != null) {
            ensureDoiTuongQuanLyExists(request.getDoiTuongQuanLyId());
        }
        cauHinhMapper.updateFromThuocTinhCapNhatRequest(request, entity);
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

        return toThuocTinhResponse(thuocTinhRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ThuocTinh entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        thuocTinhRepository.save(entity);
    }

    private ThuocTinh findById(UUID id) {
        return thuocTinhRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(CauHinhErrorCode.THUOC_TINH_NOT_FOUND, "Không tìm thấy bản ghi"));
    }

    private void ensureDoiTuongQuanLyExists(UUID doiTuongQuanLyId) {
        if (doiTuongQuanLyId == null) {
            throw new AppException(CauHinhErrorCode.DOI_TUONG_QUAN_LY_NOT_FOUND, "Không tìm thấy đối tượng quản lý");
        }
        doiTuongQuanLyRepository.findByIdAndNgayXoaIsNull(doiTuongQuanLyId)
                .orElseThrow(() -> new AppException(CauHinhErrorCode.DOI_TUONG_QUAN_LY_NOT_FOUND, "Không tìm thấy đối tượng quản lý"));
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

    /** Dùng khi map 1 entity lẻ (getById/create/update) — 1 entity thì 1 query, không phải N+1. */
    private ThuocTinhResponse toThuocTinhResponse(ThuocTinh entity) {
        ThuocTinhResponse response = cauHinhMapper.toThuocTinhResponse(entity);
        response.setLienKetBang(
                LienKetBangSupport.resolveLienKetBang(entity.getKieuDuLieuId(), kieuDuLieuRepository));
        return response;
    }

    /**
     * Bản dùng khi map cả danh sách: tra lienKetBang trong map đã nạp sẵn thay vì query từng entity.
     * Kết quả giống hệt bản trên — xem javadoc LienKetBangSupport.resolveLienKetBang(String, Map).
     */
    private ThuocTinhResponse toThuocTinhResponse(ThuocTinh entity, Map<String, String> lienKetBangByTen) {
        ThuocTinhResponse response = cauHinhMapper.toThuocTinhResponse(entity);
        response.setLienKetBang(
                LienKetBangSupport.resolveLienKetBang(entity.getKieuDuLieuId(), lienKetBangByTen));
        return response;
    }

    /** kieu_du_lieu là bảng cấu hình 7 dòng — nạp trọn một lần cho mỗi lượt map danh sách. */
    private Map<String, String> loadLienKetBangByTen() {
        return LienKetBangSupport.buildLienKetBangByTen(kieuDuLieuRepository.findByNgayXoaIsNull());
    }
}
