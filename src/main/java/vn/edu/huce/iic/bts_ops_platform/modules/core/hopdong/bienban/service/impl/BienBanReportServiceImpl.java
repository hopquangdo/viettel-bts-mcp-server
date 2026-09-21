package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.config.AppCacheProperties;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.DanhMucBienBan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LoaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.DanhMucBienBanRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LoaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.cache.BienBanReportCacheNames;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.entity.BienBan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.repository.BienBanRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.service.BienBanReportService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.HopDongDoiTuongSnapshot;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongSnapshotService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BienBanReportServiceImpl implements BienBanReportService {

    private static final String LOAI_BAO_CAO_KHAO_SAT = "BAO_CAO_KHAO_SAT";
    private static final String TRANG_THAI_DA_DUYET = "da_duyet";
    private static final int MAX_KET_QUA = 30;
    /** Danh mục biên bản cố định — mọi kiểu HĐ dùng chung (khớp frontend bienBanChecklist.ts). */
    private static final List<String> STANDARD_CHECKLIST_LOAI = List.of(
            "BIEN_BAN_SO_1",
            "NHAT_KY_THI_CONG",
            "BIEN_BAN_SO_2",
            "BAO_CAO_KHAO_SAT");

    private final BienBanRepository bienBanRepository;
    private final HopDongRepository hopDongRepository;
    private final LoaiHopDongRepository loaiHopDongRepository;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final HopDongDoiTuongSnapshotService hopDongDoiTuongSnapshotService;
    private final DanhMucBienBanRepository danhMucBienBanRepository;
    private final CacheService cacheService;
    private final AppCacheProperties cacheProperties;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> thieuKhaoSat(String loaiHopDong, String khuVuc) {
        String cacheKey = "thieu-khao-sat|" + normalize(loaiHopDong) + "|" + normalize(khuVuc);
        Optional<List> cached = cacheService.get(BienBanReportCacheNames.REPORT, cacheKey, List.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<Map<String, Object>> result = computeThieuKhaoSat(loaiHopDong, khuVuc);
        cacheService.put(BienBanReportCacheNames.REPORT, cacheKey, result, cacheProperties.hopDongDoiTuongReportTtl());
        return result;
    }

    private List<Map<String, Object>> computeThieuKhaoSat(String loaiHopDong, String khuVuc) {
        UUID loaiHopDongId = resolveLoaiHopDongId(loaiHopDong);
        List<HopDong> hopDongs = (loaiHopDongId != null
                ? hopDongRepository.findByLoaiHopDongIdAndNgayXoaIsNull(loaiHopDongId)
                : hopDongRepository.findByNgayXoaIsNull()).stream()
                .filter(hd -> Boolean.TRUE.equals(hd.getHoatDong()))
                .toList();
        if (hopDongs.isEmpty()) {
            return List.of();
        }
        List<UUID> hopDongIds = hopDongs.stream().map(HopDong::getId).toList();

        // 1. Trạm đã có biên bản khảo sát đã duyệt — union từ JSON snapshot của mỗi BienBan.
        Set<UUID> coBienBan = new HashSet<>();
        for (BienBan bienBan : bienBanRepository.findByHopDongIdInAndLoaiBienBanAndNgayXoaIsNull(hopDongIds, LOAI_BAO_CAO_KHAO_SAT)) {
            if (!TRANG_THAI_DA_DUYET.equals(bienBan.getTrangThai())) {
                continue;
            }
            coBienBan.addAll(fromJsonIdsOrEmpty(bienBan.getHopDongDoiTuongIdsJson()));
        }

        // 2. Toàn bộ trạm active trong phạm vi lọc.
        List<UUID> tramIds = new ArrayList<>();
        for (UUID hopDongId : hopDongIds) {
            tramIds.addAll(hopDongDoiTuongService.findActiveIdsWithActiveHopDong(hopDongId));
        }

        List<UUID> thieuIds = tramIds.stream().filter(id -> !coBienBan.contains(id)).toList();
        if (thieuIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, HopDongDoiTuongSnapshot> snapshots = hopDongDoiTuongSnapshotService.getSnapshots(thieuIds);
        String khuVucKeyword = khuVuc == null ? null : khuVuc.trim().toLowerCase(Locale.ROOT);

        List<Map<String, Object>> result = new ArrayList<>();
        for (UUID tramId : thieuIds) {
            HopDongDoiTuongSnapshot snapshot = snapshots.get(tramId);
            if (snapshot == null) {
                continue;
            }
            if (khuVucKeyword != null && !khuVucKeyword.isBlank()) {
                String sKhuVuc = snapshot.getKhuVuc() == null ? "" : snapshot.getKhuVuc().toLowerCase(Locale.ROOT);
                if (!sKhuVuc.contains(khuVucKeyword)) {
                    continue;
                }
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("hopDongDoiTuongId", tramId);
            row.put("maTram", snapshot.getMaDoiTuong());
            row.put("khuVuc", snapshot.getKhuVuc());
            row.put("maHopDong", snapshot.getTenHopDong());
            result.add(row);
            if (result.size() >= MAX_KET_QUA) {
                break;
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> thieuTheoTienDo(String maHopDong) {
        String cacheKey = "thieu-theo-tien-do|" + normalize(maHopDong);
        Optional<List> cached = cacheService.get(BienBanReportCacheNames.REPORT, cacheKey, List.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<Map<String, Object>> result = computeThieuTheoTienDo(maHopDong);
        cacheService.put(BienBanReportCacheNames.REPORT, cacheKey, result, cacheProperties.hopDongDoiTuongReportTtl());
        return result;
    }

    private List<Map<String, Object>> computeThieuTheoTienDo(String maHopDong) {
        String keyword = maHopDong == null ? null : maHopDong.trim().toLowerCase(Locale.ROOT);
        List<HopDong> hopDongs = hopDongRepository.findByNgayXoaIsNull().stream()
                .filter(hd -> Boolean.TRUE.equals(hd.getHoatDong()))
                .filter(hd -> keyword == null || keyword.isBlank()
                        || (hd.getMaHopDong() != null && hd.getMaHopDong().toLowerCase(Locale.ROOT).contains(keyword))
                        || (hd.getMa() != null && hd.getMa().toLowerCase(Locale.ROOT).contains(keyword)))
                .filter(hd -> hd.getKieuHopDongId() != null)
                .toList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (HopDong hopDong : hopDongs) {
            List<BienBan> bienBansCuaHopDong = bienBanRepository
                    .findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDong.getId());

            List<String> thieu = new ArrayList<>();
            for (String loai : STANDARD_CHECKLIST_LOAI) {
                boolean daCo = bienBansCuaHopDong.stream()
                        .anyMatch(bb -> TRANG_THAI_DA_DUYET.equals(bb.getTrangThai()) && loai.equals(bb.getLoaiBienBan()));
                if (!daCo) {
                    thieu.add(resolveTenDanhMuc(loai));
                }
            }

            if (!thieu.isEmpty()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("hopDongId", hopDong.getId());
                row.put("maHopDong", hopDong.getMa() != null ? hopDong.getMa() : hopDong.getMaHopDong());
                row.put("ten", hopDong.getTen());
                row.put("thieuBienBan", thieu);
                result.add(row);
                if (result.size() >= MAX_KET_QUA) {
                    break;
                }
            }
        }
        return result;
    }

    private String resolveTenDanhMuc(String ma) {
        return danhMucBienBanRepository.findByMaAndNgayXoaIsNull(ma)
                .filter(d -> Boolean.TRUE.equals(d.getHoatDong()))
                .map(DanhMucBienBan::getTen)
                .orElse(ma);
    }

    private UUID resolveLoaiHopDongId(String loaiHopDong) {
        if (loaiHopDong == null || loaiHopDong.isBlank()) {
            return null;
        }
        List<LoaiHopDong> matched = loaiHopDongRepository.search(false, true, loaiHopDong.trim().toLowerCase(Locale.ROOT));
        return matched.isEmpty() ? null : matched.get(0).getId();
    }

    private List<UUID> fromJsonIdsOrEmpty(String json) {
        if (json == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<UUID>>() {
            });
        } catch (IOException e) {
            return List.of();
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
