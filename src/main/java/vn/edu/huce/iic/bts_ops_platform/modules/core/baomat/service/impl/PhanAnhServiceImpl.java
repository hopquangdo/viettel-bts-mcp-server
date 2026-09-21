package vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventContext;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.exception.AuthErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.dto.request.PhanAnhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.dto.response.PhanAnhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.entity.PhanAnhNguoiDung;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.repository.PhanAnhNguoiDungRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.service.PhanAnhService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.entity.NguoiDung;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.repository.NguoiDungRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhanAnhServiceImpl implements PhanAnhService {

    private final PhanAnhNguoiDungRepository repository;
    private final NguoiDungRepository nguoiDungRepository;
    private final AppEventContext appEventContext;

    @Override
    @Transactional
    public PhanAnhResponse create(PhanAnhTaoRequest request) {
        JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
        PhanAnhNguoiDung entity = new PhanAnhNguoiDung();
        entity.setNguoiDungId(user.id());
        entity.setLoai(request.getLoai().trim().toUpperCase());
        entity.setTieuDe(request.getTieuDe().trim());
        entity.setNoiDung(EntityFilter.trimToNull(request.getNoiDung()));
        entity.setTrangThai("moi");
        PhanAnhNguoiDung saved = repository.save(entity);
        appEventContext.audit("PHAN_ANH", "Gửi phản ánh: " + saved.getTieuDe(), null,
                Map.of("loai", saved.getLoai(), "phanAnhId", saved.getId()));
        return toResponse(saved, user.hoTen());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanAnhResponse> listMine() {
        UUID userId = SecurityContextHelper.requireCurrentUser().id();
        NguoiDung user = nguoiDungRepository.findById(userId).orElse(null);
        String hoTen = user != null ? user.getHoTen() : null;
        return repository.findByNguoiDungIdAndNgayXoaIsNullOrderByNgayTaoDesc(userId).stream()
                .map(item -> toResponse(item, hoTen))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanAnhResponse> listAll() {
        Map<UUID, String> hoTenById = nguoiDungRepository.findByNgayXoaIsNull().stream()
                .collect(java.util.stream.Collectors.toMap(NguoiDung::getId, NguoiDung::getHoTen, (a, b) -> a));
        return repository.findByNgayXoaIsNullOrderByNgayTaoDesc().stream()
                .map(item -> toResponse(item, hoTenById.get(item.getNguoiDungId())))
                .toList();
    }

    @Override
    @Transactional
    public PhanAnhResponse updateTrangThai(UUID id, String trangThai) {
        PhanAnhNguoiDung entity = repository.findById(id)
                .filter(item -> item.getNgayXoa() == null)
                .orElseThrow(() -> new AppException(AuthErrorCode.YEU_CAU_KHONG_HOP_LE, "Không tìm thấy phản ánh"));
        entity.setTrangThai(trangThai.trim().toLowerCase());
        PhanAnhNguoiDung saved = repository.save(entity);
        String hoTen = nguoiDungRepository.findById(saved.getNguoiDungId())
                .map(NguoiDung::getHoTen)
                .orElse(null);
        return toResponse(saved, hoTen);
    }

    private PhanAnhResponse toResponse(PhanAnhNguoiDung entity, String hoTen) {
        return PhanAnhResponse.builder()
                .id(entity.getId())
                .nguoiDungId(entity.getNguoiDungId())
                .nguoiDungTen(hoTen)
                .loai(entity.getLoai())
                .tieuDe(entity.getTieuDe())
                .noiDung(entity.getNoiDung())
                .trangThai(entity.getTrangThai())
                .ngayTao(entity.getNgayTao())
                .build();
    }
}
