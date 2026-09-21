package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.PaginationDefaults;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongGiaTriCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongGiaTriTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.ChinhSuaThongSoDanhSachItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.ChinhSuaThongSoDeXuatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.ChinhSuaThongSoMucRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.ChinhSuaThongSoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.LanChinhSuaGanNhatResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.SoLanSuaThongSoItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.SoLanSuaThongSoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.entity.ChinhSuaThongSo;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.repository.ChinhSuaThongSoRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.service.ChinhSuaThongSoService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuongGiaTri;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongGiaTriService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.services.ThongBaoService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class ChinhSuaThongSoServiceImpl implements ChinhSuaThongSoService {

    private static final int NGUONG_CAN_PHE_DUYET = 2;

    private final ChinhSuaThongSoRepository repository;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final HopDongDoiTuongGiaTriService hopDongDoiTuongGiaTriService;
    private final HopDongService hopDongService;
    private final ThuocTinhService thuocTinhService;
    private final ThongBaoService thongBaoService;
    private final vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.services.HopDongArchiveGuard hopDongArchiveGuard;

    public ChinhSuaThongSoServiceImpl(
            ChinhSuaThongSoRepository repository,
            HopDongDoiTuongService hopDongDoiTuongService,
            @Lazy HopDongDoiTuongGiaTriService hopDongDoiTuongGiaTriService,
            HopDongService hopDongService,
            ThuocTinhService thuocTinhService,
            @Lazy ThongBaoService thongBaoService,
            vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.services.HopDongArchiveGuard hopDongArchiveGuard) {
        this.repository = repository;
        this.hopDongDoiTuongService = hopDongDoiTuongService;
        this.hopDongDoiTuongGiaTriService = hopDongDoiTuongGiaTriService;
        this.hopDongService = hopDongService;
        this.thuocTinhService = thuocTinhService;
        this.thongBaoService = thongBaoService;
        this.hopDongArchiveGuard = hopDongArchiveGuard;
    }

    @Override
    @Transactional(readOnly = true)
    public SoLanSuaThongSoResponse demSoLanDaApDung(UUID hopDongDoiTuongId) {
        Map<UUID, Map<UUID, Integer>> batch = demSoLanDaApDungBatch(List.of(hopDongDoiTuongId));
        Map<UUID, Integer> perAttr = batch.getOrDefault(hopDongDoiTuongId, Map.of());
        List<SoLanSuaThongSoItemResponse> items = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : perAttr.entrySet()) {
            items.add(SoLanSuaThongSoItemResponse.builder()
                    .thuocTinhId(entry.getKey())
                    .soLanDaApDung(entry.getValue())
                    .choDuyet(repository.existsByHopDongDoiTuongIdAndThuocTinhIdAndTrangThaiAndNgayXoaIsNull(
                            hopDongDoiTuongId, entry.getKey(), TRANG_THAI_CHO_DUYET))
                    .build());
        }
        return SoLanSuaThongSoResponse.builder()
                .hopDongDoiTuongId(hopDongDoiTuongId)
                .items(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SoLanSuaThongSoResponse> demSoLanDaApDungBatchList(Collection<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds == null || hopDongDoiTuongIds.isEmpty()) {
            return List.of();
        }
        Map<UUID, Map<UUID, Integer>> counts = demSoLanDaApDungBatch(hopDongDoiTuongIds);
        List<SoLanSuaThongSoResponse> results = new ArrayList<>();
        for (UUID doiTuongId : hopDongDoiTuongIds) {
            Map<UUID, Integer> perAttr = counts.getOrDefault(doiTuongId, Map.of());
            List<SoLanSuaThongSoItemResponse> items = new ArrayList<>();
            for (Map.Entry<UUID, Integer> entry : perAttr.entrySet()) {
                items.add(SoLanSuaThongSoItemResponse.builder()
                        .thuocTinhId(entry.getKey())
                        .soLanDaApDung(entry.getValue())
                        .choDuyet(repository.existsByHopDongDoiTuongIdAndThuocTinhIdAndTrangThaiAndNgayXoaIsNull(
                                doiTuongId, entry.getKey(), TRANG_THAI_CHO_DUYET))
                        .build());
            }
            results.add(SoLanSuaThongSoResponse.builder()
                    .hopDongDoiTuongId(doiTuongId)
                    .items(items)
                    .build());
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Map<UUID, Integer>> demSoLanDaApDungBatch(Collection<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds == null || hopDongDoiTuongIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Map<UUID, Integer>> result = new HashMap<>();
        for (UUID id : hopDongDoiTuongIds) {
            result.put(id, new HashMap<>());
        }
        for (Object[] row : repository.countDaApDungGroupByDoiTuongAndThuocTinh(hopDongDoiTuongIds)) {
            UUID doiTuongId = (UUID) row[0];
            UUID thuocTinhId = (UUID) row[1];
            long count = (Long) row[2];
            result.computeIfAbsent(doiTuongId, key -> new HashMap<>())
                    .put(thuocTinhId, (int) count);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LanChinhSuaGanNhatResponse> lanChinhSuaGanNhatBatch(Collection<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds == null || hopDongDoiTuongIds.isEmpty()) {
            return List.of();
        }
        Map<UUID, Instant> lastAtByDoiTuong = new HashMap<>();
        for (UUID id : hopDongDoiTuongIds) {
            lastAtByDoiTuong.put(id, null);
        }
        for (Object[] row : repository.findLastChinhSuaAtByDoiTuongIds(hopDongDoiTuongIds)) {
            UUID doiTuongId = (UUID) row[0];
            Instant lastAt = (Instant) row[1];
            lastAtByDoiTuong.put(doiTuongId, lastAt);
        }
        List<LanChinhSuaGanNhatResponse> results = new ArrayList<>();
        for (UUID doiTuongId : hopDongDoiTuongIds) {
            results.add(LanChinhSuaGanNhatResponse.builder()
                    .hopDongDoiTuongId(doiTuongId)
                    .ngayChinhSuaGanNhat(lastAtByDoiTuong.get(doiTuongId))
                    .build());
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public void requireDirectEditAllowed(UUID hopDongDoiTuongId, UUID thuocTinhId) {
        int soLan = countDaApDung(hopDongDoiTuongId, thuocTinhId);
        if (soLan >= NGUONG_CAN_PHE_DUYET) {
            throw new AppException(
                    HopDongErrorCode.CHINH_SUA_THONG_SO_CAN_PHE_DUYET,
                    "Thông số đã sửa " + soLan + " lần — cần giải trình và phê duyệt quản lý");
        }
        if (repository.existsByHopDongDoiTuongIdAndThuocTinhIdAndTrangThaiAndNgayXoaIsNull(
                hopDongDoiTuongId, thuocTinhId, TRANG_THAI_CHO_DUYET)) {
            throw new AppException(
                    HopDongErrorCode.CHINH_SUA_THONG_SO_CHO_DUYET,
                    "Đang có đề xuất chỉnh sửa chờ duyệt cho thông số này");
        }
    }

    @Override
    @Transactional
    public void ghiApDungTrucTiep(
            UUID hopDongId,
            UUID hopDongDoiTuongId,
            UUID thuocTinhId,
            String tenThuocTinh,
            String giaTriCu,
            String giaTriMoi) {
        int lan = countDaApDung(hopDongDoiTuongId, thuocTinhId) + 1;
        JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
        ChinhSuaThongSo row = newRow(hopDongId, hopDongDoiTuongId, thuocTinhId, tenThuocTinh, giaTriCu, giaTriMoi,
                null, TRANG_THAI_DA_AP_DUNG, lan, user);
        repository.save(row);
    }

    @Override
    @Transactional
    public List<ChinhSuaThongSoResponse> deXuat(ChinhSuaThongSoDeXuatRequest request) {
        HopDongDoiTuongResponse doiTuong = hopDongDoiTuongService.getById(request.getHopDongDoiTuongId());
        UUID hopDongId = doiTuong.getHopDongId();
        hopDongArchiveGuard.assertWritable(hopDongId);
        JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
        List<ChinhSuaThongSoResponse> results = new ArrayList<>();
        List<String> pendingLabels = new ArrayList<>();

        for (ChinhSuaThongSoMucRequest muc : request.getMucThayDoi()) {
            UUID thuocTinhId = muc.getThuocTinhId();
            String tenThuocTinh = resolveTenThuocTinh(thuocTinhId);
            String giaTriCu = findGiaTriHienTai(request.getHopDongDoiTuongId(), thuocTinhId);
            String giaTriMoi = normalizeGiaTri(muc.getGiaTriMoi());
            if (Objects.equals(trimToEmpty(giaTriCu), trimToEmpty(giaTriMoi))) {
                continue;
            }

            int soLan = countDaApDung(request.getHopDongDoiTuongId(), thuocTinhId);
            if (repository.existsByHopDongDoiTuongIdAndThuocTinhIdAndTrangThaiAndNgayXoaIsNull(
                    request.getHopDongDoiTuongId(), thuocTinhId, TRANG_THAI_CHO_DUYET)) {
                throw new AppException(
                        HopDongErrorCode.CHINH_SUA_THONG_SO_CHO_DUYET,
                        "Đang có đề xuất chờ duyệt cho " + tenThuocTinh);
            }

            if (soLan >= NGUONG_CAN_PHE_DUYET) {
                if (request.getGiaiTrinh() == null || request.getGiaiTrinh().isBlank()) {
                    throw new AppException(
                            HopDongErrorCode.CHINH_SUA_THONG_SO_CAN_PHE_DUYET,
                            "Cần giải trình khi sửa " + tenThuocTinh + " lần thứ " + (soLan + 1));
                }
                ChinhSuaThongSo pending = newRow(hopDongId, request.getHopDongDoiTuongId(), thuocTinhId, tenThuocTinh,
                        giaTriCu, giaTriMoi, request.getGiaiTrinh().trim(), TRANG_THAI_CHO_DUYET, soLan + 1, user);
                results.add(toResponse(repository.save(pending)));
                pendingLabels.add(tenThuocTinh);
            } else {
                applyGiaTri(request.getHopDongDoiTuongId(), thuocTinhId, giaTriMoi, giaTriCu);
                ChinhSuaThongSo applied = newRow(hopDongId, request.getHopDongDoiTuongId(), thuocTinhId, tenThuocTinh,
                        giaTriCu, giaTriMoi, null, TRANG_THAI_DA_AP_DUNG, soLan + 1, user);
                results.add(toResponse(repository.save(applied)));
            }
        }
        if (!pendingLabels.isEmpty()) {
            String summary = String.join(", ", pendingLabels);
            if (request.getGiaiTrinh() != null && !request.getGiaiTrinh().isBlank()) {
                summary = summary + " — Giải trình: " + request.getGiaiTrinh().trim();
            }
            thongBaoService.thongBaoChinhSuaThongSoChoDuyet(
                    request.getHopDongDoiTuongId(),
                    doiTuong.getDoiTuongMa(),
                    summary,
                    user.id());
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChinhSuaThongSoResponse> lichSu(UUID hopDongDoiTuongId) {
        return repository.findByHopDongDoiTuongIdAndNgayXoaIsNullOrderByNgayTaoDesc(hopDongDoiTuongId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChinhSuaThongSoDanhSachItemResponse> danhSachChoDuyet(
            UUID hopDongId, Integer page, Integer size) {
        int pageNumber = EntityFilter.normalizePage(page);
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE);
        Page<ChinhSuaThongSo> result = repository.findChoDuyet(
                TRANG_THAI_CHO_DUYET, hopDongId, PageRequest.of(pageNumber, pageSize));

        Map<UUID, HopDongDoiTuongResponse> doiTuongCache = new LinkedHashMap<>();
        Map<UUID, HopDongResponse> hopDongCache = new LinkedHashMap<>();

        List<ChinhSuaThongSoDanhSachItemResponse> items = result.getContent().stream()
                .map(row -> {
                    HopDongDoiTuongResponse doiTuong = doiTuongCache.computeIfAbsent(
                            row.getHopDongDoiTuongId(), hopDongDoiTuongService::getById);
                    HopDongResponse hopDong = hopDongCache.computeIfAbsent(
                            row.getHopDongId(), hopDongService::getById);
                    return ChinhSuaThongSoDanhSachItemResponse.builder()
                            .deXuat(toResponse(row))
                            .maTram(doiTuong.getDoiTuongMa())
                            .tenHopDong(hopDong.getTen())
                            .maHopDong(hopDong.getMa())
                            .build();
                })
                .toList();

        return PageResponse.ofItems(items, pageNumber, pageSize, result.getTotalElements());
    }

    @Override
    @Transactional
    public ChinhSuaThongSoResponse pheDuyet(UUID id) {
        ChinhSuaThongSo entity = findById(id);
        if (!TRANG_THAI_CHO_DUYET.equals(entity.getTrangThai())) {
            throw new AppException(HopDongErrorCode.CHINH_SUA_THONG_SO_INVALID, "Đề xuất không ở trạng thái chờ duyệt");
        }
        applyGiaTri(entity.getHopDongDoiTuongId(), entity.getThuocTinhId(), entity.getGiaTriMoi(), entity.getGiaTriCu());
        JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
        entity.setTrangThai(TRANG_THAI_DA_AP_DUNG);
        entity.setNguoiPheDuyetId(user.id());
        entity.setNguoiPheDuyetTen(user.hoTen());
        entity.setNgayPheDuyet(Instant.now());
        ChinhSuaThongSoResponse saved = toResponse(repository.save(entity));
        HopDongDoiTuongResponse doiTuong = hopDongDoiTuongService.getById(entity.getHopDongDoiTuongId());
        thongBaoService.thongBaoChinhSuaThongSoDaDuyet(
                entity.getNguoiDeXuatId(),
                entity.getHopDongDoiTuongId(),
                doiTuong.getDoiTuongMa(),
                entity.getTenThuocTinh());
        return saved;
    }

    @Override
    @Transactional
    public ChinhSuaThongSoResponse tuChoi(UUID id, String lyDo) {
        ChinhSuaThongSo entity = findById(id);
        if (!TRANG_THAI_CHO_DUYET.equals(entity.getTrangThai())) {
            throw new AppException(HopDongErrorCode.CHINH_SUA_THONG_SO_INVALID, "Đề xuất không ở trạng thái chờ duyệt");
        }
        JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
        entity.setTrangThai(TRANG_THAI_TU_CHOI);
        entity.setLyDoTuChoi(lyDo.trim());
        entity.setNguoiPheDuyetId(user.id());
        entity.setNguoiPheDuyetTen(user.hoTen());
        entity.setNgayPheDuyet(Instant.now());
        ChinhSuaThongSoResponse saved = toResponse(repository.save(entity));
        HopDongDoiTuongResponse doiTuong = hopDongDoiTuongService.getById(entity.getHopDongDoiTuongId());
        thongBaoService.thongBaoChinhSuaThongSoTuChoi(
                entity.getNguoiDeXuatId(),
                entity.getHopDongDoiTuongId(),
                doiTuong.getDoiTuongMa(),
                entity.getTenThuocTinh(),
                lyDo);
        return saved;
    }

    private void applyGiaTri(UUID hopDongDoiTuongId, UUID thuocTinhId, String giaTriMoi, String giaTriCu) {
        String normalized = normalizeGiaTri(giaTriMoi);
        HopDongDoiTuongGiaTri existing = hopDongDoiTuongGiaTriService.findActiveEntitiesByHopDongDoiTuongId(hopDongDoiTuongId)
                .stream()
                .filter(item -> thuocTinhId.equals(item.getThuocTinhId()))
                .findFirst()
                .orElse(null);
        if (existing != null && existing.getId() != null) {
            HopDongDoiTuongGiaTriCapNhatRequest update = new HopDongDoiTuongGiaTriCapNhatRequest();
            update.setHopDongDoiTuongId(hopDongDoiTuongId);
            update.setThuocTinhId(thuocTinhId);
            update.setGiaTri(normalized);
            update.setHoatDong(true);
            hopDongDoiTuongGiaTriService.updateSkipPolicy(existing.getId(), update);
        } else if (normalized != null && !normalized.isBlank()) {
            HopDongDoiTuongGiaTriTaoRequest create = new HopDongDoiTuongGiaTriTaoRequest();
            create.setHopDongDoiTuongId(hopDongDoiTuongId);
            create.setThuocTinhId(thuocTinhId);
            create.setGiaTri(normalized);
            create.setHoatDong(true);
            hopDongDoiTuongGiaTriService.createSkipPolicy(create);
        }
    }

    private int countDaApDung(UUID hopDongDoiTuongId, UUID thuocTinhId) {
        return (int) repository.countByHopDongDoiTuongIdAndThuocTinhIdAndTrangThaiAndNgayXoaIsNull(
                hopDongDoiTuongId, thuocTinhId, TRANG_THAI_DA_AP_DUNG);
    }

    private String findGiaTriHienTai(UUID hopDongDoiTuongId, UUID thuocTinhId) {
        return hopDongDoiTuongGiaTriService.findActiveEntitiesByHopDongDoiTuongId(hopDongDoiTuongId).stream()
                .filter(item -> thuocTinhId.equals(item.getThuocTinhId()))
                .map(HopDongDoiTuongGiaTri::getGiaTri)
                .findFirst()
                .orElse(null);
    }

    private String resolveTenThuocTinh(UUID thuocTinhId) {
        try {
            return thuocTinhService.getById(thuocTinhId).getTen();
        } catch (Exception ex) {
            return thuocTinhId.toString().substring(0, 8);
        }
    }

    private ChinhSuaThongSo findById(UUID id) {
        return repository.findById(id)
                .filter(item -> item.getNgayXoa() == null)
                .orElseThrow(() -> new AppException(HopDongErrorCode.CHINH_SUA_THONG_SO_NOT_FOUND, "Không tìm thấy đề xuất"));
    }

    private ChinhSuaThongSo newRow(
            UUID hopDongId,
            UUID hopDongDoiTuongId,
            UUID thuocTinhId,
            String tenThuocTinh,
            String giaTriCu,
            String giaTriMoi,
            String giaiTrinh,
            String trangThai,
            int lanChinhSua,
            JwtUserPrincipal user) {
        ChinhSuaThongSo row = new ChinhSuaThongSo();
        row.setHopDongId(hopDongId);
        row.setHopDongDoiTuongId(hopDongDoiTuongId);
        row.setThuocTinhId(thuocTinhId);
        row.setTenThuocTinh(tenThuocTinh);
        row.setGiaTriCu(giaTriCu);
        row.setGiaTriMoi(giaTriMoi);
        row.setGiaiTrinh(giaiTrinh);
        row.setTrangThai(trangThai);
        row.setLanChinhSua(lanChinhSua);
        row.setNguoiDeXuatId(user.id());
        row.setNguoiDeXuatTen(user.hoTen());
        return row;
    }

    private ChinhSuaThongSoResponse toResponse(ChinhSuaThongSo entity) {
        return ChinhSuaThongSoResponse.builder()
                .id(entity.getId())
                .hopDongId(entity.getHopDongId())
                .hopDongDoiTuongId(entity.getHopDongDoiTuongId())
                .thuocTinhId(entity.getThuocTinhId())
                .tenThuocTinh(entity.getTenThuocTinh())
                .giaTriCu(entity.getGiaTriCu())
                .giaTriMoi(entity.getGiaTriMoi())
                .giaiTrinh(entity.getGiaiTrinh())
                .trangThai(entity.getTrangThai())
                .lanChinhSua(entity.getLanChinhSua())
                .nguoiDeXuatId(entity.getNguoiDeXuatId())
                .nguoiDeXuatTen(entity.getNguoiDeXuatTen())
                .nguoiPheDuyetId(entity.getNguoiPheDuyetId())
                .nguoiPheDuyetTen(entity.getNguoiPheDuyetTen())
                .ngayPheDuyet(entity.getNgayPheDuyet())
                .lyDoTuChoi(entity.getLyDoTuChoi())
                .ngayTao(entity.getNgayTao())
                .build();
    }

    private static String normalizeGiaTri(String value) {
        return value == null ? null : value.trim();
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
