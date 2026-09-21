package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhKieuHopDongDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhKieuHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ThuocTinhHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ThuocTinhKieuHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuDuLieuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.ThuocTinhHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.ThuocTinhKieuHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhKieuHopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.LienKetBangSupport;

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
public class ThuocTinhKieuHopDongServiceImpl implements ThuocTinhKieuHopDongService {

    private final ThuocTinhKieuHopDongRepository lienKetRepository;
    private final ThuocTinhHopDongRepository thuocTinhHopDongRepository;
    private final KieuHopDongRepository kieuHopDongRepository;
    private final KieuDuLieuRepository kieuDuLieuRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ThuocTinhKieuHopDongResponse> list(UUID kieuHopDongId, Boolean activeOnly, boolean includeDeleted) {
        if (kieuHopDongId == null) {
            throw new AppException(CauHinhErrorCode.KIEU_HOP_DONG_NOT_FOUND, "Thiếu kiểu hợp đồng");
        }
        ensureKieuExists(kieuHopDongId);
        List<ThuocTinhKieuHopDong> source = lienKetRepository.search(kieuHopDongId, includeDeleted, activeOnly);
        Map<UUID, ThuocTinhHopDong> attrMap = loadAttrMap(source);
        return source.stream()
                .map(item -> toResponse(item, attrMap.get(item.getThuocTinhHopDongId())))
                .toList();
    }

    @Override
    @Transactional
    public List<ThuocTinhKieuHopDongResponse> sync(ThuocTinhKieuHopDongDongBoRequest request) {
        UUID kieuHopDongId = request.getKieuHopDongId();
        ensureKieuExists(kieuHopDongId);

        List<UUID> desiredIds = request.getThuocTinhHopDongIds() == null
                ? List.of()
                : request.getThuocTinhHopDongIds().stream().distinct().toList();
        for (UUID attrId : desiredIds) {
            ensureAttrExists(attrId);
        }

        List<ThuocTinhKieuHopDong> current = lienKetRepository.findByKieuHopDongIdAndNgayXoaIsNullOrderByThuTuAsc(kieuHopDongId);
        Set<UUID> desiredSet = new HashSet<>(desiredIds);
        short order = 0;

        for (ThuocTinhKieuHopDong link : current) {
            if (!desiredSet.contains(link.getThuocTinhHopDongId())) {
                link.setNgayXoa(Instant.now());
                link.setHoatDong(false);
                lienKetRepository.save(link);
            }
        }

        Map<UUID, ThuocTinhKieuHopDong> activeByAttr = current.stream()
                .filter(link -> link.getNgayXoa() == null)
                .collect(Collectors.toMap(ThuocTinhKieuHopDong::getThuocTinhHopDongId, Function.identity(), (a, b) -> a));

        for (UUID attrId : desiredIds) {
            ThuocTinhKieuHopDong existing = activeByAttr.get(attrId);
            if (existing != null) {
                existing.setThuTu(order++);
                existing.setHoatDong(true);
                lienKetRepository.save(existing);
                continue;
            }
            ThuocTinhKieuHopDong created = new ThuocTinhKieuHopDong();
            created.setKieuHopDongId(kieuHopDongId);
            created.setThuocTinhHopDongId(attrId);
            created.setThuTu(order++);
            created.setHoatDong(true);
            lienKetRepository.save(created);
        }

        return list(kieuHopDongId, true, false);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ThuocTinhKieuHopDong entity = lienKetRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(
                        CauHinhErrorCode.THUOC_TINH_KIEU_HOP_DONG_NOT_FOUND, "Không tìm thấy liên kết"));
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        lienKetRepository.save(entity);
    }

    private Map<UUID, ThuocTinhHopDong> loadAttrMap(List<ThuocTinhKieuHopDong> source) {
        Set<UUID> ids = source.stream().map(ThuocTinhKieuHopDong::getThuocTinhHopDongId).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return thuocTinhHopDongRepository.findByIdInAndNgayXoaIsNull(ids.stream().toList()).stream()
                .collect(Collectors.toMap(ThuocTinhHopDong::getId, Function.identity()));
    }

    private ThuocTinhKieuHopDongResponse toResponse(ThuocTinhKieuHopDong link, ThuocTinhHopDong attr) {
        ThuocTinhKieuHopDongResponse response = new ThuocTinhKieuHopDongResponse();
        response.setId(link.getId());
        response.setKieuHopDongId(link.getKieuHopDongId());
        response.setThuocTinhHopDongId(link.getThuocTinhHopDongId());
        response.setThuTu(link.getThuTu());
        response.setHoatDong(link.getHoatDong());
        response.setNgayTao(link.getNgayTao());
        response.setNgayCapNhat(link.getNgayCapNhat());
        if (attr != null) {
            response.setTen(attr.getTen());
            response.setKieuDuLieuId(attr.getKieuDuLieuId());
            response.setLaKhoaChinh(attr.getLaKhoaChinh());
            response.setBatBuoc(attr.getBatBuoc());
            response.setDonVi(attr.getDonVi());
            response.setTuyChon(attr.getTuyChon());
            response.setLienKetBang(
                    LienKetBangSupport.resolveLienKetBang(attr.getKieuDuLieuId(), kieuDuLieuRepository));
        }
        return response;
    }

    private void ensureKieuExists(UUID kieuHopDongId) {
        kieuHopDongRepository.findByIdAndNgayXoaIsNull(kieuHopDongId)
                .orElseThrow(() -> new AppException(CauHinhErrorCode.KIEU_HOP_DONG_NOT_FOUND, "Không tìm thấy kiểu HĐ"));
    }

    private void ensureAttrExists(UUID thuocTinhHopDongId) {
        thuocTinhHopDongRepository.findByIdAndNgayXoaIsNull(thuocTinhHopDongId)
                .orElseThrow(() -> new AppException(
                        CauHinhErrorCode.THUOC_TINH_HOP_DONG_NOT_FOUND, "Không tìm thấy thuộc tính HĐ"));
    }
}
