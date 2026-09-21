package vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.dto.CommonErrorCode;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.AuthorityPrefix;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.PaginationDefaults;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.constants.ThongBaoLoai;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.dto.response.ThongBaoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.dto.response.ThongBaoUnreadCountResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.entity.ThongBao;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.repository.ThongBaoRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.services.ThongBaoService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.exception.AuthErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.PhanQuyenResolverService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.entity.NguoiDung;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.repository.NguoiDungRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ThongBaoServiceImpl implements ThongBaoService {

    private final ThongBaoRepository thongBaoRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final PhanQuyenResolverService phanQuyenResolverService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ThongBaoResponse> inbox(Integer page, Integer size) {
        JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
        int pageNumber = EntityFilter.normalizePage(page);
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE);
        Page<ThongBao> result = thongBaoRepository.findByNguoiNhanIdOrderByNgayTaoDesc(
                user.id(), PageRequest.of(pageNumber, pageSize));
        return PageResponse.ofItems(
                result.getContent().stream().map(this::toResponse).toList(),
                pageNumber,
                pageSize,
                result.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThongBaoResponse> listMine(int limit) {
        UUID userId = SecurityContextHelper.requireCurrentUser().id();
        int size = limit > 0 && limit <= 200 ? limit : 50;
        return thongBaoRepository.findByNguoiNhanIdOrderByNgayTaoDesc(userId, PageRequest.of(0, size))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long demChuaDoc() {
        UUID userId = SecurityContextHelper.requireCurrentUser().id();
        return thongBaoRepository.countByNguoiNhanIdAndDaDocFalse(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public ThongBaoUnreadCountResponse unreadCount() {
        return ThongBaoUnreadCountResponse.builder()
                .chuaDoc(demChuaDoc())
                .build();
    }

    @Override
    @Transactional
    public ThongBaoResponse danhDauDaDoc(UUID id) {
        JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
        ThongBao entity = thongBaoRepository.findById(id)
                .orElseThrow(() -> new AppException(CommonErrorCode.BAD_REQUEST, "Không tìm thấy thông báo"));
        if (!user.id().equals(entity.getNguoiNhanId())) {
            throw new AppException(CommonErrorCode.FORBIDDEN, "Không có quyền truy cập thông báo này");
        }
        if (!Boolean.TRUE.equals(entity.getDaDoc())) {
            entity.setDaDoc(true);
            entity.setNgayDoc(Instant.now());
            thongBaoRepository.save(entity);
        }
        return toResponse(entity);
    }

    @Override
    @Transactional
    public void markRead(UUID id) {
        UUID userId = SecurityContextHelper.requireCurrentUser().id();
        int updated = thongBaoRepository.markRead(id, userId, Instant.now());
        if (updated == 0) {
            throw new AppException(AuthErrorCode.KHONG_DU_QUYEN, "Không tìm thấy thông báo");
        }
    }

    @Override
    @Transactional
    public void markAllRead() {
        UUID userId = SecurityContextHelper.requireCurrentUser().id();
        thongBaoRepository.markAllRead(userId, Instant.now());
    }

    @Override
    @Transactional
    public void thongBaoChinhSuaThongSoChoDuyet(
            UUID hopDongDoiTuongId,
            String maTram,
            String noiDungTomTat,
            UUID nguoiDeXuatId) {
        String tieuDe = "Trạm " + (maTram == null || maTram.isBlank() ? hopDongDoiTuongId.toString().substring(0, 8) : maTram.trim())
                + " cần phê duyệt sửa thông số";
        for (UUID approverId : resolveApproverUserIds(nguoiDeXuatId)) {
            if (thongBaoRepository.existsByNguoiNhanIdAndLoaiAndThamChieuIdAndDaDocFalse(
                    approverId, ThongBaoLoai.CHINH_SUA_THONG_SO_CHO_DUYET, hopDongDoiTuongId)) {
                continue;
            }
            ThongBao row = new ThongBao();
            row.setNguoiNhanId(approverId);
            row.setLoai(ThongBaoLoai.CHINH_SUA_THONG_SO_CHO_DUYET);
            row.setTieuDe(tieuDe);
            row.setNoiDung(noiDungTomTat);
            row.setThamChieuId(hopDongDoiTuongId);
            row.setDaDoc(false);
            row.setNgayTao(Instant.now());
            thongBaoRepository.save(row);
        }
    }

    @Override
    @Transactional
    public void thongBaoChinhSuaThongSoDaDuyet(
            UUID nguoiNhanId,
            UUID hopDongDoiTuongId,
            String maTram,
            String tenThuocTinh) {
        if (nguoiNhanId == null) {
            return;
        }
        ThongBao row = new ThongBao();
        row.setNguoiNhanId(nguoiNhanId);
        row.setLoai(ThongBaoLoai.CHINH_SUA_THONG_SO_DA_DUYET);
        row.setTieuDe("Đã phê duyệt sửa " + safeLabel(tenThuocTinh) + " — trạm " + safeLabel(maTram));
        row.setNoiDung("Thay đổi thông số đã được quản lý phê duyệt và áp dụng.");
        row.setThamChieuId(hopDongDoiTuongId);
        row.setDaDoc(false);
        row.setNgayTao(Instant.now());
        thongBaoRepository.save(row);
    }

    @Override
    @Transactional
    public void thongBaoChinhSuaThongSoTuChoi(
            UUID nguoiNhanId,
            UUID hopDongDoiTuongId,
            String maTram,
            String tenThuocTinh,
            String lyDo) {
        if (nguoiNhanId == null) {
            return;
        }
        ThongBao row = new ThongBao();
        row.setNguoiNhanId(nguoiNhanId);
        row.setLoai(ThongBaoLoai.CHINH_SUA_THONG_SO_TU_CHOI);
        row.setTieuDe("Từ chối sửa " + safeLabel(tenThuocTinh) + " — trạm " + safeLabel(maTram));
        row.setNoiDung(lyDo == null || lyDo.isBlank() ? "Quản lý đã từ chối đề xuất chỉnh sửa thông số." : lyDo.trim());
        row.setThamChieuId(hopDongDoiTuongId);
        row.setDaDoc(false);
        row.setNgayTao(Instant.now());
        thongBaoRepository.save(row);
    }

    private List<UUID> resolveApproverUserIds(UUID excludeUserId) {
        Set<UUID> approverIds = new LinkedHashSet<>();
        for (NguoiDung user : nguoiDungRepository.findByNgayXoaIsNull()) {
            if (!Boolean.TRUE.equals(user.getHoatDong())) {
                continue;
            }
            if (canApproveChinhSuaThongSo(user.getQuyenId())) {
                approverIds.add(user.getId());
            }
        }

        if (excludeUserId != null) {
            approverIds.remove(excludeUserId);
            if (approverIds.isEmpty()) {
                nguoiDungRepository.findByIdAndNgayXoaIsNull(excludeUserId)
                        .filter(user -> Boolean.TRUE.equals(user.getHoatDong()))
                        .filter(user -> canApproveChinhSuaThongSo(user.getQuyenId()))
                        .ifPresent(user -> approverIds.add(user.getId()));
            }
        }

        return new ArrayList<>(approverIds);
    }

    private boolean canApproveChinhSuaThongSo(UUID quyenId) {
        if (quyenId == null) {
            return false;
        }
        String approvalAuthority = AuthorityPrefix.quyenHan(QuyenHanMa.SUA_SO_LIEU_DA_DUYET);
        for (GrantedAuthority authority : phanQuyenResolverService.resolveAuthorities(quyenId)) {
            String value = authority.getAuthority();
            if (AuthorityPrefix.isFullAccessAuthority(value)) {
                return true;
            }
            if (approvalAuthority.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private ThongBaoResponse toResponse(ThongBao entity) {
        return ThongBaoResponse.builder()
                .id(entity.getId())
                .loai(entity.getLoai())
                .tieuDe(entity.getTieuDe())
                .noiDung(entity.getNoiDung())
                .lienKet(entity.getLienKet())
                .thamChieuId(entity.getThamChieuId())
                .daDoc(Boolean.TRUE.equals(entity.getDaDoc()))
                .ngayTao(entity.getNgayTao())
                .ngayDoc(entity.getNgayDoc())
                .build();
    }

    private static String safeLabel(String value) {
        return value == null || value.isBlank() ? "—" : value.trim();
    }
}
