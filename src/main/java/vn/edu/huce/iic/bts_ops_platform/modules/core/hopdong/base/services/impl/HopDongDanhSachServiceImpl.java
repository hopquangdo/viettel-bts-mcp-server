package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScope;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScopeService;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.PaginationDefaults;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDongLuongTrangThai;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LoaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LuongTrangThai;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LuongTrangThaiBuoc;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.TrangThaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.exception.CauHinhErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.DoiTuongQuanLy;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.DoiTuongQuanLyRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongLuongTrangThaiRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LoaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LuongTrangThaiBuocRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LuongTrangThaiRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.TrangThaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.entity.BienBan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.repository.BienBanRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.HeNghiepVuResolver;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.service.TramThiCongGiaiDoanResolver;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.entity.BienBanPhatSinh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.repository.BienBanPhatSinhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachHopDongItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachKieuItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachLoaiItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachLuongTrangThaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachMoRongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTinhChiTietResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTinhChiTietSummaryResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTramRowResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachNhomUuTienItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongNhomUuTien;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongThuocTinh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.mapper.HopDongMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongNhomUuTienRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongThuocTinhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongDanhSachService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.HopDongDanhSachDoiTuongGroupSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.HopDongDanhSachTienDoHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.DoiTuongHopDongLienKetSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhHopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LienKetBangLabelService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.services.HopDongArchiveGuard;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.services.PhanCongService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.services.SanLuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucNhomService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucHopDongTreeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.VolumeTinhToanHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.util.HangMucThanhTienCalculator;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HopDongDanhSachServiceImpl implements HopDongDanhSachService {


    private final LoaiHopDongRepository loaiHopDongRepository;
    private final KieuHopDongRepository kieuHopDongRepository;
    private final HopDongRepository hopDongRepository;
    private final HopDongDoiTuongRepository hopDongDoiTuongRepository;
    private final HopDongThuocTinhRepository hopDongThuocTinhRepository;
    private final HopDongMapper hopDongMapper;
    private final ContractorScopeService contractorScopeService;
    private final KieuHopDongLuongTrangThaiRepository kieuHopDongLuongTrangThaiRepository;
    private final LuongTrangThaiRepository luongTrangThaiRepository;
    private final LuongTrangThaiBuocRepository luongTrangThaiBuocRepository;
    private final TrangThaiHopDongRepository trangThaiHopDongRepository;
    private final ThuocTinhHopDongService thuocTinhHopDongService;
    private final LienKetBangLabelService lienKetBangLabelService;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final HopDongNhomUuTienRepository hopDongNhomUuTienRepository;
    private final PhanCongService phanCongService;
    private final HopDongDanhSachDoiTuongGroupSupport doiTuongGroupSupport;
    private final SanLuongService sanLuongService;
    private final HangMucNhomService hangMucNhomService;
    private final DoiTuongQuanLyRepository doiTuongQuanLyRepository;
    private final BienBanRepository bienBanRepository;
    private final BienBanPhatSinhRepository bienBanPhatSinhRepository;
    private final TramThiCongGiaiDoanResolver tramThiCongGiaiDoanResolver;
    private final ObjectMapper objectMapper;
    private final HopDongArchiveGuard hopDongArchiveGuard;

    @Override
    @Transactional(readOnly = true)
    public HopDongDanhSachTongQuanResponse getTongQuan(UUID loaiHopDongId, boolean includeArchive) {
        Map<UUID, Long> countByLoai = toCountMap(hopDongRepository.countGroupByLoaiHopDongId(true));
        Map<UUID, Long> countByKieu = toCountMap(hopDongRepository.countGroupByKieuHopDongId(true, loaiHopDongId));
        if (!includeArchive) {
            Map<UUID, Long> archivedByLoai = hopDongArchiveGuard.countArchivedGroupByLoaiHopDongId();
            Map<UUID, Long> archivedByKieu = hopDongArchiveGuard.countArchivedGroupByKieuHopDongId(loaiHopDongId);
            archivedByLoai.forEach((id, count) ->
                    countByLoai.merge(id, count, (current, archived) -> Math.max(0L, current - archived)));
            archivedByKieu.forEach((id, count) ->
                    countByKieu.merge(id, count, (current, archived) -> Math.max(0L, current - archived)));
        }

        List<LoaiHopDong> loaiList = loaiHopDongRepository.search(false, true, "").stream()
                .filter(loai -> loaiHopDongId == null || loai.getId().equals(loaiHopDongId))
                .toList();

        List<KieuHopDong> kieuList = kieuHopDongRepository.search(false, true, loaiHopDongId, "");

        Map<String, UUID> luongIdByKieuLoai = loadLuongIdByKieuLoai(loaiHopDongId);
        Map<UUID, HopDongDanhSachLuongTrangThaiResponse> luongById = loadLuongResponses(luongIdByKieuLoai.values());

        Map<UUID, List<KieuHopDong>> kieuByLoai = kieuList.stream()
                .collect(Collectors.groupingBy(KieuHopDong::getLoaiHopDongId, LinkedHashMap::new, Collectors.toList()));

        List<HopDongDanhSachLoaiItemResponse> loaiResponses = new ArrayList<>();
        for (LoaiHopDong loai : loaiList) {
            HopDongDanhSachLoaiItemResponse loaiItem = new HopDongDanhSachLoaiItemResponse();
            loaiItem.setId(loai.getId());
            loaiItem.setMa(loai.getMa());
            loaiItem.setTen(loai.getTen());
            loaiItem.setMoTa(loai.getMoTa());
            loaiItem.setHoatDong(Boolean.TRUE.equals(loai.getHoatDong()));
            loaiItem.setSoLuongHopDong(countByLoai.getOrDefault(loai.getId(), 0L));

            List<HopDongDanhSachKieuItemResponse> kieuItems = (kieuByLoai.getOrDefault(loai.getId(), List.of())).stream()
                    .map(kieu -> toKieuItem(kieu, countByKieu, luongIdByKieuLoai, luongById))
                    .sorted(byHopDongCountDesc(HopDongDanhSachKieuItemResponse::getSoLuongHopDong,
                            HopDongDanhSachKieuItemResponse::getTen))
                    .toList();
            loaiItem.setKieuHopDongs(kieuItems);
            loaiResponses.add(loaiItem);
        }

        loaiResponses.sort(byHopDongCountDesc(
                HopDongDanhSachLoaiItemResponse::getSoLuongHopDong,
                HopDongDanhSachLoaiItemResponse::getTen));

        HopDongDanhSachTongQuanResponse response = new HopDongDanhSachTongQuanResponse();
        long tongHopDong = hopDongRepository.countFiltered(true, loaiHopDongId);
        if (!includeArchive) {
            long archivedTotal = hopDongArchiveGuard.countArchivedGroupByLoaiHopDongId().values().stream()
                    .mapToLong(Long::longValue)
                    .sum();
            if (loaiHopDongId != null) {
                archivedTotal = hopDongArchiveGuard.countArchivedGroupByLoaiHopDongId()
                        .getOrDefault(loaiHopDongId, 0L);
            }
            tongHopDong = Math.max(0L, tongHopDong - archivedTotal);
        }
        response.setTongHopDongHoatDong(tongHopDong);
        response.setCapNhatLuc(Instant.now());
        response.setLoaiHopDongs(loaiResponses);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HopDongDanhSachHopDongItemResponse> listHopDong(
            UUID kieuHopDongId,
            UUID loaiHopDongId,
            String search,
            Integer page,
            Integer size,
            boolean includeArchive) {
        KieuHopDong kieu = null;
        if (kieuHopDongId != null) {
            kieu = kieuHopDongRepository.findByIdAndNgayXoaIsNull(kieuHopDongId)
                    .orElseThrow(() -> new AppException(CauHinhErrorCode.KIEU_HOP_DONG_NOT_FOUND, "Không tìm thấy kiểu HĐ"));
        }

        // Chỉ lọc loai_hop_dong khi client gửi loaiHopDongId (tab loại HĐ).
        // Không fallback kieu.getLoaiHopDongId() — dữ liệu thực tế có thể lệch cấu hình kiểu/loại.
        UUID effectiveLoaiId = loaiHopDongId;
        UUID flowLoaiId = loaiHopDongId != null
                ? loaiHopDongId
                : (kieu != null ? kieu.getLoaiHopDongId() : null);
        Boolean activeOnly = Boolean.TRUE;
        boolean includeDeleted = false;
        int pageSize = EntityFilter.normalizeSize(size, PaginationDefaults.DEFAULT_PAGE_SIZE, PaginationDefaults.MAX_PAGE_SIZE);
        int pageNumber = EntityFilter.normalizePage(page);
        long offset = (long) pageNumber * pageSize;
        String keyword = EntityFilter.normalizeSearchForLike(search);

        long total = keyword.isBlank()
                ? hopDongRepository.countSearchNative(
                        includeDeleted, activeOnly, effectiveLoaiId, kieuHopDongId, includeArchive)
                : hopDongRepository.countSearchWithKeyword(
                        includeDeleted, activeOnly, effectiveLoaiId, kieuHopDongId, keyword, includeArchive);

        List<HopDong> fetched = keyword.isBlank()
                ? hopDongRepository.searchPageNative(
                        includeDeleted, activeOnly, effectiveLoaiId, kieuHopDongId, includeArchive, pageSize, offset)
                : hopDongRepository.searchPageWithKeyword(
                        includeDeleted, activeOnly, effectiveLoaiId, kieuHopDongId, keyword, includeArchive, pageSize, offset);

        List<HopDong> pageItems = applyContractorScope(fetched);
        if (pageItems.isEmpty()) {
            return PageResponse.ofItems(List.of(), pageNumber, pageSize, total);
        }

        List<UUID> hopDongIds = pageItems.stream().map(HopDong::getId).toList();
        Map<UUID, List<HopDongThuocTinhResponse>> thuocTinhByHopDong = loadThuocTinhBatch(hopDongIds);
        Map<UUID, Long> totalsByHopDong = toCountMap(hopDongDoiTuongRepository.countGroupByHopDongIdForIds(
                hopDongIds,
                DoiTuongHopDongLienKetSupport.HANG_MUC_THI_CONG_DOI_TUONG_ID));
        Map<UUID, Map<String, Long>> statusByHopDong = loadStatusCountsByHopDong(hopDongIds);
        Map<UUID, Long> chamTienDoByHopDong = toCountMap(hopDongDoiTuongRepository.countChamTienDoGroupByHopDongIdForIds(
                hopDongIds,
                DoiTuongHopDongLienKetSupport.HANG_MUC_THI_CONG_DOI_TUONG_ID,
                LocalDate.now().minusDays(30)));
        Map<UUID, long[]> bienBanCountsByHopDong = loadBienBanSo1Counts(hopDongIds, totalsByHopDong);
        Map<UUID, long[]> tuVanSanLuongByHopDong = loadTuVanSanLuongCounts(hopDongIds);
        Map<UUID, String> luuTruByHopDong = hopDongArchiveGuard.resolveTrangThaiLuuTru(hopDongIds);
        List<LuongTrangThaiBuocResponse> sharedFlowBuoc = kieuHopDongId != null
                ? loadFlowBuocForKieu(flowLoaiId, kieuHopDongId)
                : List.of();

        List<HopDongDanhSachHopDongItemResponse> items = pageItems.stream()
                .map(entity -> {
                    List<LuongTrangThaiBuocResponse> flowBuoc = kieuHopDongId != null
                            ? sharedFlowBuoc
                            : loadFlowBuocForKieu(entity.getLoaiHopDongId(), entity.getKieuHopDongId());
                    HopDongDanhSachHopDongItemResponse item = toHopDongDanhSachItem(
                            entity,
                            thuocTinhByHopDong.getOrDefault(entity.getId(), List.of()),
                            totalsByHopDong.getOrDefault(entity.getId(), 0L),
                            statusByHopDong.getOrDefault(entity.getId(), Map.of()),
                            flowBuoc,
                            chamTienDoByHopDong.getOrDefault(entity.getId(), 0L),
                            bienBanCountsByHopDong.getOrDefault(entity.getId(), new long[]{0L, 0L, 0L}),
                            tuVanSanLuongByHopDong.getOrDefault(entity.getId(), new long[]{0L, 0L, 0L}));
                    applyLuuTruFlags(item, luuTruByHopDong.get(entity.getId()));
                    return item;
                })
                .toList();

        return PageResponse.ofItems(items, pageNumber, pageSize, total);
    }

    @Override
    @Transactional(readOnly = true)
    public HopDongDanhSachMoRongResponse getMoRong(UUID hopDongId) {
        if (hopDongId == null) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Thiếu hợp đồng (hopDongId)");
        }

        HopDong hopDong = hopDongRepository.findByIdAndNgayXoaIsNull(hopDongId)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_NOT_FOUND, "Không tìm thấy hợp đồng"));

        assertHopDongHopLe(hopDong);

        List<HopDong> scoped = applyContractorScope(List.of(hopDong));
        if (scoped.isEmpty()) {
            throw new AppException(HopDongErrorCode.HOP_DONG_NOT_FOUND, "Không tìm thấy hợp đồng");
        }

        List<LuongTrangThaiBuocResponse> flowBuoc = loadFlowBuocForKieu(
                hopDong.getLoaiHopDongId(),
                hopDong.getKieuHopDongId());

        List<HopDongDoiTuongResponse> doiTuongs = hopDongDoiTuongService.listAll(
                "",
                true,
                false,
                hopDongId,
                null,
                null,
                null,
                null);

        Set<UUID> doiTuongIds = doiTuongs.stream()
                .map(HopDongDoiTuongResponse::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> contractorByDoiTuongId =
                overlayNhaThauId(phanCongService.mapActiveContractorByHopDongDoiTuongIds(doiTuongIds), doiTuongs);

        HangMucHopDongTreeResponse tree = hangMucNhomService
                .getTreesByHopDongIds(List.of(hopDongId), true)
                .getOrDefault(hopDongId, new HangMucHopDongTreeResponse());
        BigDecimal planPerStation = VolumeTinhToanHelper.nz(
                HangMucThanhTienCalculator.compute(tree).getTongThanhTien());
        Map<UUID, BigDecimal> moneyByDoiTuong = sanLuongService.tongThanhTienTheoDoiTuongIds(doiTuongIds);
        Map<UUID, BigDecimal> boSungByDoiTuong = doiTuongs.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(
                        HopDongDoiTuongResponse::getId,
                        item -> VolumeTinhToanHelper.nz(item.getBoSungSanLuong()),
                        (left, right) -> left));

        HopDongDanhSachMoRongResponse response = new HopDongDanhSachMoRongResponse();
        response.setLuongTrangThaiBuoc(flowBuoc);
        response.setNhomUuTiens(loadNhomUuTiens(hopDongId));
        response.setKhuVucs(doiTuongGroupSupport.buildKhuVucs(
                doiTuongs,
                contractorByDoiTuongId,
                flowBuoc,
                moneyByDoiTuong,
                boSungByDoiTuong,
                planPerStation));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public HopDongDanhSachTinhChiTietResponse getTinhChiTiet(
            UUID hopDongId,
            String khuVuc,
            String tinh,
            String nhaThau) {
        boolean missingTinh = tinh == null || tinh.isBlank();
        boolean missingNhaThau = nhaThau == null || nhaThau.isBlank() || "—".equals(nhaThau.trim());
        if (missingTinh && missingNhaThau) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Thiếu tỉnh hoặc nhà thầu");
        }
        return buildTinhChiTiet(hopDongId, khuVuc, tinh, nhaThau);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDanhSachTramRowResponse> getDanhSachTramHopDong(UUID hopDongId) {
        return buildTinhChiTiet(hopDongId, null, null, null).getTramRows();
    }

    /** Thân dùng chung cho getTinhChiTiet (tinh bắt buộc) và getDanhSachTramHopDong (tinh=null -> toàn bộ trạm). */
    private HopDongDanhSachTinhChiTietResponse buildTinhChiTiet(
            UUID hopDongId,
            String khuVuc,
            String tinh,
            String nhaThau) {
        if (hopDongId == null) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Thiếu hợp đồng (hopDongId)");
        }

        HopDong hopDong = hopDongRepository.findByIdAndNgayXoaIsNull(hopDongId)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_NOT_FOUND, "Không tìm thấy hợp đồng"));

        assertHopDongHopLe(hopDong);

        List<HopDong> scoped = applyContractorScope(List.of(hopDong));
        if (scoped.isEmpty()) {
            throw new AppException(HopDongErrorCode.HOP_DONG_NOT_FOUND, "Không tìm thấy hợp đồng");
        }

        List<LuongTrangThaiBuocResponse> flowBuoc = loadFlowBuocForKieu(
                hopDong.getLoaiHopDongId(),
                hopDong.getKieuHopDongId());

        List<HopDongDoiTuongResponse> doiTuongs = doiTuongGroupSupport.excludeShellDoiTuong(
                hopDongDoiTuongService.listAll("", true, false, hopDongId, null, null, null, null));

        Set<UUID> doiTuongIds = doiTuongs.stream()
                .map(HopDongDoiTuongResponse::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> contractorByDoiTuongId =
                overlayNhaThauId(phanCongService.mapActiveContractorByHopDongDoiTuongIds(doiTuongIds), doiTuongs);

        List<HopDongDoiTuongResponse> filtered = doiTuongGroupSupport.filterByDisplayGeo(
                doiTuongs, contractorByDoiTuongId, khuVuc, tinh, nhaThau);

        HangMucHopDongTreeResponse tree = hangMucNhomService
                .getTreesByHopDongIds(List.of(hopDongId), true)
                .getOrDefault(hopDongId, new HangMucHopDongTreeResponse());
        BigDecimal planPerStation = VolumeTinhToanHelper.nz(
                HangMucThanhTienCalculator.compute(tree).getTongThanhTien());
        Map<UUID, BigDecimal> moneyByDoiTuong = sanLuongService.tongThanhTienTheoDoiTuongIds(doiTuongIds);

        Map<String, Long> statusByMa = new LinkedHashMap<>();
        Map<UUID, Double> tyLeHoanThanhByDoiTuong = new LinkedHashMap<>();
        BigDecimal totalConstruction = BigDecimal.ZERO;
        BigDecimal totalContract = BigDecimal.ZERO;
        long huy = 0;
        long hoanThanh = 0;
        long daQuyetToan = 0;
        long vuongMac = 0;
        long hoanThanhDo = 0;
        long chuaThiCong = 0;

        for (HopDongDoiTuongResponse row : filtered) {
            String statusKey = HopDongDanhSachTienDoHelper.resolveStatusCountKey(
                    row.getTrangThaiHopDongId(),
                    row.getTrangThaiMa(),
                    row.getTrangThaiTen(),
                    flowBuoc);
            if (row.getNgayHtTc() != null
                    && !HopDongDanhSachTienDoHelper.isHuyStatus(row.getTrangThaiMa(), row.getTrangThaiTen())) {
                statusKey = HopDongDanhSachTienDoHelper.resolveHoanThanhTrangThaiMa(flowBuoc);
            }
            statusByMa.merge(statusKey, 1L, Long::sum);
            if (HopDongDanhSachTienDoHelper.isHuyStatus(row.getTrangThaiMa(), row.getTrangThaiTen())) {
                huy++;
            } else if (HopDongDanhSachTienDoHelper.isObjectHoanThanh(
                    row.getNgayHtTc(),
                    row.getTrangThaiMa(),
                    row.getTrangThaiTen(),
                    statusKey)) {
                hoanThanh++;
                if (statusKey.equals("QT") || statusKey.contains("QUYET")) {
                    daQuyetToan++;
                }
            } else if (HopDongDanhSachTienDoHelper.isVuongMacStatus(row.getTrangThaiMa(), row.getTrangThaiTen())) {
                vuongMac++;
            } else if (isChuaThiCongStatus(statusKey)) {
                chuaThiCong++;
            } else {
                hoanThanhDo++;
            }
            if (row.getId() != null && planPerStation.compareTo(BigDecimal.ZERO) > 0) {
                totalContract = totalContract.add(planPerStation);
                BigDecimal reported = moneyByDoiTuong.getOrDefault(row.getId(), BigDecimal.ZERO);
                BigDecimal boSung = VolumeTinhToanHelper.nz(row.getBoSungSanLuong());
                BigDecimal effective = VolumeTinhToanHelper.effectiveSanLuong(reported, boSung);
                totalConstruction = totalConstruction.add(effective);
            }
            if (row.getId() != null) {
                BigDecimal reported = moneyByDoiTuong.getOrDefault(row.getId(), BigDecimal.ZERO);
                BigDecimal boSung = VolumeTinhToanHelper.nz(row.getBoSungSanLuong());
                BigDecimal effective = VolumeTinhToanHelper.effectiveSanLuong(reported, boSung);
                tyLeHoanThanhByDoiTuong.put(row.getId(), HopDongDanhSachTienDoHelper.resolveDoiTuongTyLeHoanThanh(
                        row.getNgayHtTc(),
                        row.getTrangThaiMa(),
                        row.getTrangThaiTen(),
                        statusKey,
                        effective,
                        planPerStation,
                        flowBuoc));
            }
        }

        long chuaQuyetToan = Math.max(0, hoanThanh - daQuyetToan);
        LoaiHopDong loaiHopDong = hopDong.getLoaiHopDongId() == null
                ? null
                : loaiHopDongRepository.findByIdAndNgayXoaIsNull(hopDong.getLoaiHopDongId()).orElse(null);
        List<HopDongDanhSachTramRowResponse> tramRows =
                doiTuongGroupSupport.toTramRows(filtered, contractorByDoiTuongId, true);
        for (HopDongDanhSachTramRowResponse tramRow : tramRows) {
            tramRow.setTyLeHoanThanh(tyLeHoanThanhByDoiTuong.get(tramRow.getId()));
        }
        applyThiCongGiaiDoanTram(hopDong, loaiHopDong, tramRows);

        UUID doiTuongQuanLyId = filtered.stream()
                .map(HopDongDoiTuongResponse::getDoiTuongQuanLyId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        String doiTuongQuanLyTen = null;
        String doiTuongQuanLyMa = null;
        if (doiTuongQuanLyId != null) {
            Optional<DoiTuongQuanLy> doiTuongQuanLy = doiTuongQuanLyRepository.findByIdAndNgayXoaIsNull(doiTuongQuanLyId);
            if (doiTuongQuanLy.isPresent()) {
                doiTuongQuanLyTen = doiTuongQuanLy.get().getTen();
                doiTuongQuanLyMa = doiTuongQuanLy.get().getMa();
            }
        }
        if (doiTuongQuanLyTen == null || doiTuongQuanLyTen.isBlank()) {
            doiTuongQuanLyTen = filtered.stream()
                    .map(HopDongDoiTuongResponse::getDoiTuongTen)
                    .filter(value -> value != null && !value.isBlank())
                    .findFirst()
                    .orElse(null);
        }

        HopDongDanhSachTinhChiTietSummaryResponse summary = new HopDongDanhSachTinhChiTietSummaryResponse();
        summary.setTongTram(filtered.size());
        summary.setHuy(huy);
        summary.setHoanThanh(hoanThanh);
        summary.setDaQuyetToan(daQuyetToan);
        summary.setChuaQuyetToan(chuaQuyetToan);
        summary.setVuongMac(vuongMac);
        summary.setHoanThanhDo(hoanThanhDo);
        summary.setChuaThiCong(chuaThiCong);
        summary.setGiaTriKeHoach(totalContract);
        summary.setSanLuongThiCong(totalConstruction);
        summary.setTyLeHoanThanh(resolveTinhChiTietTyLeHoanThanh(
                filtered.size(),
                hoanThanh,
                totalConstruction,
                totalContract,
                statusByMa,
                tyLeHoanThanhByDoiTuong,
                flowBuoc));

        HopDongDanhSachTinhChiTietResponse response = new HopDongDanhSachTinhChiTietResponse();
        response.setKhuVuc(khuVuc);
        response.setTinh(tinh);
        response.setNhaThau(nhaThau);
        response.setDoiTuongQuanLyId(doiTuongQuanLyId);
        response.setDoiTuongQuanLyMa(doiTuongQuanLyMa);
        response.setDoiTuongQuanLyTen(doiTuongQuanLyTen);
        response.setThuocTinhColumns(doiTuongGroupSupport.buildThuocTinhColumns(doiTuongQuanLyId));
        response.setLuongTrangThaiBuoc(flowBuoc);
        response.setSummary(summary);
        response.setTienDoTheoTrangThai(
                HopDongDanhSachTienDoHelper.buildTienDoBuocListForDisplay(flowBuoc, statusByMa));
        response.setTramRows(tramRows);
        return response;
    }

    private static boolean isChuaThiCongStatus(String statusMa) {
        return statusMa.equals("CKS")
                || statusMa.contains("CHUA")
                || statusMa.contains("CHƯA");
    }

    private static double resolveTinhChiTietTyLeHoanThanh(
            long tongDoiTuong,
            long hoanThanh,
            BigDecimal totalConstruction,
            BigDecimal totalContract,
            Map<String, Long> statusByMa,
            Map<UUID, Double> tyLeHoanThanhByDoiTuong,
            List<LuongTrangThaiBuocResponse> flowBuoc) {
        if (tongDoiTuong > 0 && !tyLeHoanThanhByDoiTuong.isEmpty()) {
            double sum = tyLeHoanThanhByDoiTuong.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();
            return Math.round((sum * 100D) / tongDoiTuong) / 100D;
        }
        if (totalContract.compareTo(BigDecimal.ZERO) > 0) {
            return VolumeTinhToanHelper.ratio(totalConstruction, totalContract).doubleValue();
        }
        return HopDongDanhSachTienDoHelper.resolveAggregateTyLeHoanThanh(
                totalConstruction,
                totalContract,
                statusByMa,
                flowBuoc,
                tongDoiTuong);
    }

    private List<HopDongDanhSachNhomUuTienItemResponse> loadNhomUuTiens(UUID hopDongId) {
        return hopDongNhomUuTienRepository.findByHopDongIdAndNgayXoaIsNullOrderByThuTuAscNgayTaoAsc(hopDongId).stream()
                .map(this::toNhomUuTienItem)
                .toList();
    }

    private HopDongDanhSachNhomUuTienItemResponse toNhomUuTienItem(HopDongNhomUuTien entity) {
        HopDongDanhSachNhomUuTienItemResponse item = new HopDongDanhSachNhomUuTienItemResponse();
        item.setId(entity.getId());
        item.setMa(entity.getMa());
        item.setTen(entity.getTen());
        item.setMauSac(entity.getMauSac());
        item.setKeHoach(entity.getKeHoach());
        item.setHoanThanh(entity.getHoanThanh());
        item.setConLai(entity.getConLai());
        item.setPhanTram(entity.getPhanTram());
        item.setSoNhan(entity.getSoNhan());
        return item;
    }

    private HopDongDanhSachHopDongItemResponse toHopDongDanhSachItem(
            HopDong entity,
            List<HopDongThuocTinhResponse> thuocTinhGiaTri,
            long soDoiTuong,
            Map<String, Long> statusCounts,
            List<LuongTrangThaiBuocResponse> flowBuoc,
            long soChamTienDo,
            long[] bienBanCounts,
            long[] tuVanSanLuongCounts) {
        HopDongDanhSachHopDongItemResponse item = new HopDongDanhSachHopDongItemResponse();
        item.setId(entity.getId());
        item.setLoaiHopDongId(entity.getLoaiHopDongId());
        item.setKieuHopDongId(entity.getKieuHopDongId());
        item.setHoatDong(Boolean.TRUE.equals(entity.getHoatDong()));
        item.setGiaTriHd(entity.getGiaTriHd());
        item.setNgayThucHien(entity.getNgayThucHien());
        item.setSoNgayThucHien(entity.getSoNgayThucHien());
        item.setHanHopDong(entity.getHanHopDong());
        item.setNgayTao(entity.getNgayTao());
        item.setNgayCapNhat(entity.getNgayCapNhat());
        item.setThuocTinhGiaTri(thuocTinhGiaTri);
        item.setSoChamTienDo(soChamTienDo);
        item.setSoBienBanChuaLap(bienBanCounts[0]);
        item.setSoBienBanChoDuyet(bienBanCounts[1]);
        item.setSoBienBanTuChoi(bienBanCounts[2]);

        String ma = firstNonBlank(entity.getMa(), entity.getMaHopDong());
        item.setMaHopDong(ma);
        item.setMa(ma);
        item.setTen(firstNonBlank(entity.getTen(), ma));
        item.setBenKyA(entity.getBenKyA());

        item.setSoDoiTuong(soDoiTuong);
        item.setChuaLamSanLuong(tuVanSanLuongCounts[0]);
        item.setDangLamSanLuong(tuVanSanLuongCounts[1]);
        item.setHoanThanhSanLuong(tuVanSanLuongCounts.length > 2 ? tuVanSanLuongCounts[2] : 0L);
        item.setLuongTrangThaiBuoc(flowBuoc != null ? flowBuoc : List.of());
        Map<String, Long> reconciled = HopDongDanhSachTienDoHelper.reconcileUnassignedStatuses(
                statusCounts, soDoiTuong, flowBuoc);
        item.setTyLeHoanThanh(
                HopDongDanhSachTienDoHelper.computeDisplayTyLeHoanThanh(
                        reconciled, soDoiTuong, flowBuoc));
        item.setTienDoTheoTrangThai(
                HopDongDanhSachTienDoHelper.buildTienDoBuocListForDisplay(flowBuoc, reconciled));
        return item;
    }

    private static void applyLuuTruFlags(HopDongDanhSachHopDongItemResponse item, String trangThaiLuuTru) {
        String status = trangThaiLuuTru != null ? trangThaiLuuTru : "active";
        item.setTrangThaiLuuTru(status);
        item.setArchived("archived".equalsIgnoreCase(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LuongTrangThaiBuocResponse> getFlowBuocForKieu(UUID loaiHopDongId, UUID kieuHopDongId) {
        return loadFlowBuocForKieu(loaiHopDongId, kieuHopDongId);
    }

    @Override
    public Map<UUID, List<LuongTrangThaiBuocResponse>> getFlowBuocForAllKieu() {
        // Cùng bộ lọc với loadLuongIdByKieuLoai() nhưng nạp mọi loại 1 lần và key theo kieuHopDongId
        // (mỗi kiểu thuộc đúng 1 loại) — 1 query link + 3 query batch trong loadLuongResponses().
        Map<UUID, UUID> luongIdByKieu = new LinkedHashMap<>();
        for (KieuHopDongLuongTrangThai link : kieuHopDongLuongTrangThaiRepository.search(false, null, null)) {
            if (link.getNgayXoa() != null || !Boolean.TRUE.equals(link.getHoatDong())) {
                continue;
            }
            luongIdByKieu.putIfAbsent(link.getId().getKieuHopDongId(), link.getLuongTrangThaiId());
        }
        Map<UUID, HopDongDanhSachLuongTrangThaiResponse> luongById = loadLuongResponses(luongIdByKieu.values());

        Map<UUID, List<LuongTrangThaiBuocResponse>> result = new LinkedHashMap<>();
        for (Map.Entry<UUID, UUID> entry : luongIdByKieu.entrySet()) {
            HopDongDanhSachLuongTrangThaiResponse luong = luongById.get(entry.getValue());
            if (luong != null && luong.getBuoc() != null && !luong.getBuoc().isEmpty()) {
                result.put(entry.getKey(), luong.getBuoc());
            }
        }
        return result;
    }

    private List<LuongTrangThaiBuocResponse> loadFlowBuocForKieu(UUID loaiHopDongId, UUID kieuHopDongId) {
        Map<String, UUID> luongIdByKieuLoai = loadLuongIdByKieuLoai(loaiHopDongId);
        UUID luongId = luongIdByKieuLoai.get(kieuLoaiKey(loaiHopDongId, kieuHopDongId));
        if (luongId == null) {
            return List.of();
        }
        HopDongDanhSachLuongTrangThaiResponse luong = loadLuongResponses(List.of(luongId)).get(luongId);
        return luong == null || luong.getBuoc() == null ? List.of() : luong.getBuoc();
    }

    private Map<UUID, Map<String, Long>> loadStatusCountsByHopDong(Collection<UUID> hopDongIds) {
        if (hopDongIds == null || hopDongIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Map<String, Long>> result = new LinkedHashMap<>();
        for (Object[] row : hopDongDoiTuongRepository.countGroupByHopDongIdAndTrangThaiMaForIds(
                hopDongIds,
                DoiTuongHopDongLienKetSupport.HANG_MUC_THI_CONG_DOI_TUONG_ID)) {
            UUID hopDongId = (UUID) row[0];
            String ma = row[1] != null ? row[1].toString() : "UNKNOWN";
            long count = row[2] instanceof Number number ? number.longValue() : 0L;
            result.computeIfAbsent(hopDongId, ignored -> new LinkedHashMap<>()).put(ma, count);
        }
        return result;
    }

    /** row value: [chuaLamSanLuong, dangLamSanLuong, hoanThanhSanLuong] */
    private Map<UUID, long[]> loadTuVanSanLuongCounts(Collection<UUID> hopDongIds) {
        if (hopDongIds == null || hopDongIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, long[]> result = new LinkedHashMap<>();
        for (Object[] row : hopDongDoiTuongRepository.countTuVanSanLuongGroupByHopDongIdForIds(
                hopDongIds,
                DoiTuongHopDongLienKetSupport.HANG_MUC_THI_CONG_DOI_TUONG_ID)) {
            UUID hopDongId = (UUID) row[0];
            long chuaLam = row[1] instanceof Number number ? number.longValue() : 0L;
            long dangLam = row[2] instanceof Number number ? number.longValue() : 0L;
            long hoanThanh = row[3] instanceof Number number ? number.longValue() : 0L;
            result.put(hopDongId, new long[]{chuaLam, dangLam, hoanThanh});
        }
        return result;
    }

    /**
     * Với mỗi hợp đồng, tổng hợp Biên bản số 1 (BB-01) theo từng đối tượng: giữ lại đúng 1 bản ghi
     * mới nhất/đối tượng (đối tượng có thể được xuất lại nhiều lần), rồi suy ra 3 số liệu — chưa
     * lập (soDoiTuong trừ đi số đối tượng đã có BB-01), đang chờ duyệt, bị từ chối. Trả về mảng
     * [chuaLap, choDuyet, tuChoi] theo đúng hopDongId.
     */
    private Map<UUID, long[]> loadBienBanSo1Counts(Collection<UUID> hopDongIds, Map<UUID, Long> soDoiTuongByHopDong) {
        if (hopDongIds == null || hopDongIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Long> meaningfulTotals = doiTuongGroupSupport.countByHopDongExcludingShells(
                hopDongDoiTuongService.listActiveByHopDongIds(hopDongIds));
        List<BienBan> rows = bienBanRepository.findByHopDongIdInAndLoaiBienBanAndNgayXoaIsNull(hopDongIds, "BIEN_BAN_SO_1");
        Map<UUID, Map<UUID, BienBan>> latestByHopDongThenDoiTuong = new HashMap<>();
        for (BienBan row : rows) {
            List<UUID> doiTuongIds = parseDoiTuongIdsOrEmpty(row.getHopDongDoiTuongIdsJson());
            if (doiTuongIds.isEmpty()) {
                continue;
            }
            UUID doiTuongId = doiTuongIds.get(0);
            Map<UUID, BienBan> perObject = latestByHopDongThenDoiTuong.computeIfAbsent(row.getHopDongId(), ignored -> new HashMap<>());
            BienBan existing = perObject.get(doiTuongId);
            if (existing == null || isNewerBienBan(row, existing)) {
                perObject.put(doiTuongId, row);
            }
        }
        Map<UUID, long[]> result = new HashMap<>();
        for (UUID hopDongId : hopDongIds) {
            Map<UUID, BienBan> perObject = latestByHopDongThenDoiTuong.getOrDefault(hopDongId, Map.of());
            long choDuyet = perObject.values().stream().filter(b -> "cho_duyet".equals(b.getTrangThai())).count();
            long tuChoi = perObject.values().stream().filter(b -> "tu_choi".equals(b.getTrangThai())).count();
            long total = meaningfulTotals.getOrDefault(hopDongId, 0L);
            long chuaLap = Math.max(0L, total - perObject.size());
            result.put(hopDongId, new long[]{chuaLap, choDuyet, tuChoi});
        }
        return result;
    }

    private static boolean isNewerBienBan(BienBan candidate, BienBan current) {
        int cmp = candidate.getNgayLap().compareTo(current.getNgayLap());
        if (cmp != 0) {
            return cmp > 0;
        }
        return candidate.getNgayTao().compareTo(current.getNgayTao()) > 0;
    }

    private List<UUID> parseDoiTuongIdsOrEmpty(String json) {
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

    private Map<UUID, List<HopDongThuocTinhResponse>> loadThuocTinhBatch(List<UUID> hopDongIds) {
        if (hopDongIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, List<HopDongThuocTinhResponse>> grouped = new LinkedHashMap<>();
        for (Object[] row : hopDongThuocTinhRepository.findActiveWithTenByHopDongIds(hopDongIds)) {
            HopDongThuocTinh value = (HopDongThuocTinh) row[0];
            String tenThuocTinh = row[1] != null ? row[1].toString() : null;
            HopDongThuocTinhResponse thuocTinh = hopDongMapper.toHopDongThuocTinhResponse(value);
            thuocTinh.setTenThuocTinh(tenThuocTinh);
            grouped.computeIfAbsent(value.getHopDongId(), ignored -> new ArrayList<>()).add(thuocTinh);
        }
        grouped.values().forEach(values -> values.sort(
                Comparator.comparing(HopDongThuocTinhResponse::getThuTu, Comparator.nullsLast(Comparator.naturalOrder()))));

        Set<UUID> thuocTinhHopDongIds = grouped.values().stream()
                .flatMap(List::stream)
                .map(HopDongThuocTinhResponse::getThuocTinhHopDongId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, ThuocTinhHopDongResponse> definitions = thuocTinhHopDongService.mapActiveByIds(thuocTinhHopDongIds);
        List<HopDongThuocTinhResponse> allValues = grouped.values().stream().flatMap(List::stream).toList();
        lienKetBangLabelService.applyDisplayLabels(allValues, definitions);

        return grouped;
    }

    /**
     * Đè giá trị từ cột chuẩn hóa hop_dong_doi_tuong.nha_thau_id (qua nhaThauTen đã enrich sẵn
     * trên response) lên map suy từ phan_cong — nha_thau_id ưu tiên cao hơn vì là nguồn chân lý
     * mới (xem kế hoạch migrate SanLuongRepository.aggregateTongHop). Đối tượng chưa gán
     * nha_thau_id (chưa migrate/backfill) vẫn giữ nguyên giá trị suy từ phan_cong như cũ.
     */
    private Map<UUID, String> overlayNhaThauId(
            Map<UUID, String> base, List<HopDongDoiTuongResponse> doiTuongs) {
        Map<UUID, String> merged = new HashMap<>(base);
        for (HopDongDoiTuongResponse row : doiTuongs) {
            if (row.getId() != null && row.getNhaThauId() != null
                    && row.getNhaThauTen() != null && !row.getNhaThauTen().isBlank()) {
                merged.put(row.getId(), row.getNhaThauTen());
            }
        }
        return merged;
    }

    private List<HopDong> applyContractorScope(List<HopDong> source) {
        Optional<ContractorScope> scope = contractorScopeService.currentScope();
        if (scope.isEmpty()) {
            return source;
        }
        Set<UUID> allowedIds = scope.get().hopDongIds();
        return source.stream()
                .filter(item -> allowedIds.contains(item.getId()))
                .toList();
    }



    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private HopDongDanhSachKieuItemResponse toKieuItem(
            KieuHopDong kieu,
            Map<UUID, Long> countByKieu,
            Map<String, UUID> luongIdByKieuLoai,
            Map<UUID, HopDongDanhSachLuongTrangThaiResponse> luongById) {
        HopDongDanhSachKieuItemResponse item = new HopDongDanhSachKieuItemResponse();
        item.setId(kieu.getId());
        item.setLoaiHopDongId(kieu.getLoaiHopDongId());
        item.setMa(kieu.getMa());
        item.setTen(kieu.getTen());
        item.setNhom(kieu.getNhom());
        item.setMauSac(kieu.getMauSac());
        item.setMoTa(kieu.getMoTa());
        item.setHoatDong(Boolean.TRUE.equals(kieu.getHoatDong()));
        item.setSoLuongHopDong(countByKieu.getOrDefault(kieu.getId(), 0L));

        UUID luongId = luongIdByKieuLoai.get(kieuLoaiKey(kieu.getLoaiHopDongId(), kieu.getId()));
        item.setLuongTrangThai(luongId == null ? null : luongById.get(luongId));
        return item;
    }

    private Map<String, UUID> loadLuongIdByKieuLoai(UUID loaiHopDongId) {
        List<KieuHopDongLuongTrangThai> links = kieuHopDongLuongTrangThaiRepository.search(false, loaiHopDongId, null);
        Map<String, UUID> result = new HashMap<>();
        for (KieuHopDongLuongTrangThai link : links) {
            if (link.getNgayXoa() != null || !Boolean.TRUE.equals(link.getHoatDong())) {
                continue;
            }
            String key = kieuLoaiKey(link.getId().getLoaiHopDongId(), link.getId().getKieuHopDongId());
            result.putIfAbsent(key, link.getLuongTrangThaiId());
        }
        return result;
    }

    private Map<UUID, HopDongDanhSachLuongTrangThaiResponse> loadLuongResponses(Iterable<UUID> luongIds) {
        Set<UUID> ids = new java.util.HashSet<>();
        luongIds.forEach(id -> {
            if (id != null) {
                ids.add(id);
            }
        });
        if (ids.isEmpty()) {
            return Map.of();
        }

        Map<UUID, LuongTrangThai> luongEntities = luongTrangThaiRepository.findAllById(ids).stream()
                .filter(l -> l.getNgayXoa() == null && Boolean.TRUE.equals(l.getHoatDong()))
                .collect(Collectors.toMap(LuongTrangThai::getId, Function.identity(), (a, b) -> a));

        List<LuongTrangThaiBuoc> allBuoc = luongTrangThaiBuocRepository
                .findById_LuongTrangThaiIdInOrderByThuTuAsc(luongEntities.keySet());
        Map<UUID, List<LuongTrangThaiBuoc>> buocByLuong = allBuoc.stream()
                .collect(Collectors.groupingBy(b -> b.getId().getLuongTrangThaiId(), LinkedHashMap::new, Collectors.toList()));

        Map<UUID, TrangThaiHopDong> statusMap = loadStatusMap(allBuoc);

        Map<UUID, HopDongDanhSachLuongTrangThaiResponse> result = new LinkedHashMap<>();
        for (LuongTrangThai luong : luongEntities.values()) {
            HopDongDanhSachLuongTrangThaiResponse response = new HopDongDanhSachLuongTrangThaiResponse();
            response.setId(luong.getId());
            response.setTen(luong.getTen());
            List<LuongTrangThaiBuocResponse> buocResponses = buocByLuong.getOrDefault(luong.getId(), List.of()).stream()
                    .map(buoc -> toBuocResponse(buoc, statusMap.get(buoc.getId().getTrangThaiHopDongId())))
                    .toList();
            response.setBuoc(buocResponses);
            result.put(luong.getId(), response);
        }
        return result;
    }

    private Map<UUID, TrangThaiHopDong> loadStatusMap(List<LuongTrangThaiBuoc> buocList) {
        if (buocList.isEmpty()) {
            return Map.of();
        }
        List<UUID> ids = buocList.stream()
                .map(b -> b.getId().getTrangThaiHopDongId())
                .distinct()
                .toList();
        return trangThaiHopDongRepository.findAllById(ids).stream()
                .filter(s -> s.getNgayXoa() == null)
                .collect(Collectors.toMap(TrangThaiHopDong::getId, Function.identity(), (a, b) -> a));
    }

    private LuongTrangThaiBuocResponse toBuocResponse(LuongTrangThaiBuoc buoc, TrangThaiHopDong status) {
        LuongTrangThaiBuocResponse response = new LuongTrangThaiBuocResponse();
        response.setTrangThaiHopDongId(buoc.getId().getTrangThaiHopDongId());
        response.setThuTu(buoc.getThuTu());
        if (status != null) {
            response.setTen(status.getTen());
            response.setMa(status.getMa());
            response.setMauSac(status.getMauSac());
        }
        return response;
    }

    private Map<UUID, Long> toCountMap(List<Object[]> rows) {
        Map<UUID, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            if (row == null || row.length < 2 || row[0] == null) {
                continue;
            }
            UUID id = (UUID) row[0];
            long count = row[1] instanceof Number number ? number.longValue() : 0L;
            result.put(id, count);
        }
        return result;
    }

    private String kieuLoaiKey(UUID loaiHopDongId, UUID kieuHopDongId) {
        return Objects.toString(loaiHopDongId, "") + ":" + Objects.toString(kieuHopDongId, "");
    }

    /** Hợp đồng hợp lệ: chưa xóa và hoatDong = true. */
    private void assertHopDongHopLe(HopDong hopDong) {
        if (!Boolean.TRUE.equals(hopDong.getHoatDong())) {
            throw new AppException(HopDongErrorCode.HOP_DONG_NOT_FOUND, "Hợp đồng không còn hoạt động");
        }
    }

    private <T> Comparator<T> byHopDongCountDesc(
            Function<T, Long> countExtractor,
            Function<T, String> nameExtractor) {
        return Comparator
                .comparingLong(countExtractor::apply).reversed()
                .thenComparing(nameExtractor, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
    }

    private void applyThiCongGiaiDoanTram(
            HopDong hopDong,
            LoaiHopDong loaiHopDong,
            List<HopDongDanhSachTramRowResponse> tramRows) {
        if (tramRows == null || tramRows.isEmpty() || HeNghiepVuResolver.isTuVanThietKe(loaiHopDong)) {
            return;
        }
        List<BienBan> bienBanRows =
                bienBanRepository.findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDong.getId());
        List<BienBanPhatSinh> phatSinhRows =
                bienBanPhatSinhRepository.findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDong.getId());
        for (HopDongDanhSachTramRowResponse tramRow : tramRows) {
            if (tramRow.getId() == null) {
                continue;
            }
            tramRow.setTrangThaiBienBanTen(
                    tramThiCongGiaiDoanResolver.resolve(tramRow.getId(), bienBanRows, phatSinhRows));
        }
    }
}
