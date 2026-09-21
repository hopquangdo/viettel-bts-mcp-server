package vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.exception.LuuTruErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.repository.HopDongLuuTruRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HopDongArchiveGuard {

    private final HopDongLuuTruRepository hopDongLuuTruRepository;

    public boolean isArchived(UUID hopDongId) {
        if (hopDongId == null) {
            return false;
        }
        return hopDongLuuTruRepository.findByHopDongIdAndNgayXoaIsNull(hopDongId)
                .map(entity -> "archived".equalsIgnoreCase(entity.getTrangThai()))
                .orElse(false);
    }

    public void assertWritable(UUID hopDongId) {
        if (isArchived(hopDongId)) {
            throw new AppException(
                    LuuTruErrorCode.HOP_DONG_DANG_ARCHIVE,
                    "Hợp đồng đang Archive — chỉ được xem, không chỉnh sửa");
        }
    }

    public Set<UUID> findArchivedHopDongIds() {
        return new HashSet<>(hopDongLuuTruRepository.findArchivedHopDongIds());
    }

    public Map<UUID, String> resolveTrangThaiLuuTru(Collection<UUID> hopDongIds) {
        if (hopDongIds == null || hopDongIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, String> result = new HashMap<>();
        hopDongLuuTruRepository.findByHopDongIdInAndNgayXoaIsNull(hopDongIds).forEach(entity -> {
            if (entity.getHopDongId() != null && entity.getTrangThai() != null) {
                result.put(entity.getHopDongId(), entity.getTrangThai());
            }
        });
        return result;
    }

    public String resolveTrangThaiLuuTru(UUID hopDongId) {
        if (hopDongId == null) {
            return "active";
        }
        return hopDongLuuTruRepository.findByHopDongIdAndNgayXoaIsNull(hopDongId)
                .map(entity -> entity.getTrangThai() != null ? entity.getTrangThai() : "active")
                .orElse("active");
    }

    public Map<UUID, Long> countArchivedGroupByLoaiHopDongId() {
        return toCountMap(hopDongLuuTruRepository.countArchivedGroupByLoaiHopDongId());
    }

    public Map<UUID, Long> countArchivedGroupByKieuHopDongId(UUID loaiHopDongId) {
        return toCountMap(hopDongLuuTruRepository.countArchivedGroupByKieuHopDongId(loaiHopDongId));
    }

    private static Map<UUID, Long> toCountMap(List<Object[]> rows) {
        Map<UUID, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            if (row.length >= 2 && row[0] instanceof UUID id && row[1] instanceof Number count) {
                result.put(id, count.longValue());
            }
        }
        return result;
    }
}
