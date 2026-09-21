package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.impl;

import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.security.AuthorityPrefix;
import vn.edu.huce.iic.bts_ops_platform.common.security.FullAccessRoleCodes;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.PhanQuyenQuyenHanDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response.PhanQuyenQuyenHanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.PhanQuyenQuyenHan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.PhanQuyenQuyenHanId;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.Quyen;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.QuyenHan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.exception.AuthErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository.PhanQuyenQuyenHanRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository.QuyenHanRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository.QuyenRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.PhanQuyenQuyenHanService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.PhanQuyenResolverService;

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
public class PhanQuyenQuyenHanServiceImpl implements PhanQuyenQuyenHanService {

    private final PhanQuyenQuyenHanRepository lienKetRepository;
    private final QuyenRepository quyenRepository;
    private final QuyenHanRepository quyenHanRepository;
    private final PhanQuyenResolverService phanQuyenResolverService;
    private final AppEventContext appEventContext;

    @Override
    @Transactional(readOnly = true)
    public List<PhanQuyenQuyenHanResponse> list(String quyenMa) {
        Quyen quyen = requireQuyen(quyenMa);
        List<PhanQuyenQuyenHan> rows = lienKetRepository.findActiveByQuyenMa(quyen.getMa());
        Map<UUID, QuyenHan> quyenHanMap = loadQuyenHanMap(rows);
        return rows.stream()
                .map(row -> toResponse(row, quyenHanMap.get(row.getId().getQuyenHanId())))
                .filter(r -> r.getMa() != null)
                .toList();
    }

    @Override
    @Transactional
    public List<PhanQuyenQuyenHanResponse> sync(PhanQuyenQuyenHanDongBoRequest request) {
        Quyen quyen = requireQuyen(request.getQuyenMa());
        if (FullAccessRoleCodes.isFullAccess(quyen.getMa(), quyen.getTen())) {
            throw new AppException(AuthErrorCode.KHONG_DU_QUYEN,
                    "Vai trò toàn quyền không dùng ma trận — luôn có mọi quyền hạn");
        }

        List<UUID> desiredIds = request.getQuyenHanIds() == null
                ? List.of()
                : request.getQuyenHanIds().stream().distinct().toList();
        for (UUID quyenHanId : desiredIds) {
            quyenHanRepository.findByIdAndNgayXoaIsNull(quyenHanId)
                    .orElseThrow(() -> new AppException(AuthErrorCode.QUYEN_KHONG_TON_TAI, "Không tìm thấy quyền hạn"));
        }

        List<PhanQuyenQuyenHan> current = lienKetRepository.findByIdQuyenId(quyen.getMa());
        Map<UUID, PhanQuyenQuyenHan> currentByQuyenHan = current.stream()
                .collect(Collectors.toMap(row -> row.getId().getQuyenHanId(), Function.identity(), (a, b) -> a));
        Set<UUID> desiredSet = new HashSet<>(desiredIds);

        for (PhanQuyenQuyenHan row : current) {
            if (!desiredSet.contains(row.getId().getQuyenHanId()) && row.getNgayXoa() == null) {
                row.setHoatDong(false);
                row.setNgayXoa(Instant.now());
                lienKetRepository.save(row);
            }
        }
        for (UUID quyenHanId : desiredIds) {
            PhanQuyenQuyenHan existing = currentByQuyenHan.get(quyenHanId);
            if (existing != null) {
                existing.setHoatDong(true);
                existing.setNgayXoa(null);
                lienKetRepository.save(existing);
                continue;
            }
            PhanQuyenQuyenHanId id = new PhanQuyenQuyenHanId();
            id.setQuyenHanId(quyenHanId);
            id.setQuyenId(quyen.getMa());
            PhanQuyenQuyenHan created = new PhanQuyenQuyenHan();
            created.setId(id);
            created.setHoatDong(true);
            lienKetRepository.save(created);
        }

        // Authority đang được cache theo quyenId — xóa để lần request kế của user thuộc vai trò
        // này nhận ngay ma trận mới.
        phanQuyenResolverService.evictCache(quyen.getId());
        appEventContext.audit("CAP_NHAT_MA_TRAN_QUYEN",
                "Cập nhật ma trận quyền của vai trò " + quyen.getMa(), null);
        return list(quyen.getMa());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> cuaToi() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return List.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith(AuthorityPrefix.HAN))
                .map(auth -> auth.substring(AuthorityPrefix.HAN.length()))
                .sorted()
                .toList();
    }

    private Quyen requireQuyen(String quyenMa) {
        if (quyenMa == null || quyenMa.isBlank()) {
            throw new AppException(AuthErrorCode.QUYEN_KHONG_TON_TAI, "Thiếu mã vai trò");
        }
        return quyenRepository.findByMaIgnoreCase(quyenMa.trim())
                .filter(q -> q.getNgayXoa() == null)
                .orElseThrow(() -> new AppException(AuthErrorCode.QUYEN_KHONG_TON_TAI, "Không tìm thấy vai trò"));
    }

    private Map<UUID, QuyenHan> loadQuyenHanMap(List<PhanQuyenQuyenHan> rows) {
        Set<UUID> ids = rows.stream().map(row -> row.getId().getQuyenHanId()).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return quyenHanRepository.findAllById(ids).stream()
                .filter(qh -> qh.getNgayXoa() == null)
                .collect(Collectors.toMap(QuyenHan::getId, Function.identity()));
    }

    private PhanQuyenQuyenHanResponse toResponse(PhanQuyenQuyenHan row, QuyenHan quyenHan) {
        return PhanQuyenQuyenHanResponse.builder()
                .quyenMa(row.getId().getQuyenId())
                .quyenHanId(row.getId().getQuyenHanId())
                .ma(quyenHan != null ? quyenHan.getMa() : null)
                .ten(quyenHan != null ? quyenHan.getTen() : null)
                .hoatDong(row.getHoatDong())
                .build();
    }
}
