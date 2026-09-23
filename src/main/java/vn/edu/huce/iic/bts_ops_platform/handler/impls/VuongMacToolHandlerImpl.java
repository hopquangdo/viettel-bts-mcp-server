package vn.edu.huce.iic.bts_ops_platform.handler.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.dto.doituong.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.dto.vuongmac.RankedItemDto;
import vn.edu.huce.iic.bts_ops_platform.dto.vuongmac.VuongMacItemDto;
import vn.edu.huce.iic.bts_ops_platform.dto.vuongmac.VuongMacQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.vuongmac.VuongMacTongQuanDto;
import vn.edu.huce.iic.bts_ops_platform.dto.nhathau.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.handler.VuongMacToolHandler;
import vn.edu.huce.iic.bts_ops_platform.repository.VuongMacToolRepository;
import vn.edu.huce.iic.bts_ops_platform.components.DoiTuongComponent;
import vn.edu.huce.iic.bts_ops_platform.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.components.TinhComponent;
import vn.edu.huce.iic.bts_ops_platform.support.StatusLabels;
import vn.edu.huce.iic.bts_ops_platform.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.support.ResolveSupport;
import vn.edu.huce.iic.bts_ops_platform.dto.common.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.entity.vuongmac.VuongMac;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Impl thật cho tool AI module Vướng mắc — 1 method duy nhất, chỉ gọi VuongMacToolRepository. */
@Component
@RequiredArgsConstructor
public class VuongMacToolHandlerImpl implements VuongMacToolHandler {

    private static final List<String> OPEN_STATUSES = List.of("pending", "in_progress");

    private final VuongMacToolRepository vuongMacToolRepository;
    private final HopDongComponent hopDongComponent;
    private final DoiTuongComponent doiTuongComponent;
    private final NhaThauComponent nhaThauComponent;
    private final TinhComponent tinhComponent;
    private final vn.edu.huce.iic.bts_ops_platform.components.KhuVucComponent khuVucComponent;
    private final vn.edu.huce.iic.bts_ops_platform.components.CanBoComponent canBoComponent;
    private final vn.edu.huce.iic.bts_ops_platform.components.DanhMucComponent danhMucComponent;
    private final McpParallel parallel;

    @Override
    public VuongMacQueryResponse query(String doiTuong, String hopDong, String nhaThau, String khuVuc, String tinhThanh, String loaiHopDong,
                                       String trangThai, String kieuVuongMac, String giaiDoan, Boolean dangMoOnly,
                                       String query, Integer topIn, LocalDate sinceDateIn,
                                       Integer page, Integer pageSize, Integer quaHanNgay,
                                       String canBo, LocalDate fromDate, LocalDate toDate) {
        var f_resolvedNhaThau = parallel.async(() -> nhaThauComponent.resolve(nhaThau));
        var f_resolvedHopDong = parallel.async(() -> hopDongComponent.resolve(hopDong));
        var f_resolvedDoiTuong = parallel.async(() -> doiTuongComponent.resolve(doiTuong));
        var f_tinhThanhId = parallel.async(() -> tinhComponent.resolveId(tinhThanh));
        var f_canBo = parallel.async(() -> canBoComponent.resolve(canBo));
        // các tra cứu id chạy song song (mỗi tra cứu 1-2 lần khứ hồi tới DB nếu chưa có trong bộ nhớ đệm)
        NhaThauInfo resolvedNhaThau = McpParallel.get(f_resolvedNhaThau);
        HopDongInfo resolvedHopDong = McpParallel.get(f_resolvedHopDong);
        DoiTuongInfo resolvedDoiTuong = McpParallel.get(f_resolvedDoiTuong);
        UUID tinhThanhId = McpParallel.get(f_tinhThanhId);
        UUID canBoId = McpParallel.get(f_canBo).id();
        // fromDate/toDate lọc theo ngày tạo vướng mắc (giờ VN); toDate gồm cả ngày đó. Đảo lại nếu nhập ngược.
        LocalDate tuNgay = fromDate != null && toDate != null && fromDate.isAfter(toDate) ? toDate : fromDate;
        LocalDate denNgay = fromDate != null && toDate != null && fromDate.isAfter(toDate) ? fromDate : toDate;
        ZoneId zoneVn = ZoneId.of("Asia/Ho_Chi_Minh");
        Instant ngayTaoFrom = tuNgay != null ? tuNgay.atStartOfDay(zoneVn).toInstant() : null;
        Instant ngayTaoTo = denNgay != null ? denNgay.plusDays(1).atStartOfDay(zoneVn).toInstant() : null;

        UUID khuVucId = khuVucComponent.resolveInScope(khuVuc).id();
        UUID loaiId = danhMucComponent.resolveLoaiHopDong(loaiHopDong);
        String trangThaiLoc = vn.edu.huce.iic.bts_ops_platform.support.ResolveSupport.enumValue("trangThai", trangThai,
                "pending", "in_progress", "resolved", "rejected");
        String kieuLoc = chuanHoa(kieuVuongMac);
        String giaiDoanLoc = chuanHoa(giaiDoan);
        Boolean moOnly = Boolean.TRUE.equals(dangMoOnly) ? Boolean.TRUE : null;
        // Kỳ tính "đã xử lý trong kỳ": ưu tiên [fromDate, toDate], nếu không thì [sinceDate, nay).
        Instant sinceDate = ngayTaoFrom != null ? ngayTaoFrom
                : sinceDateIn != null ? sinceDateIn.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant denKy = ngayTaoTo != null ? ngayTaoTo : Instant.now();
        UUID hopDongId = resolvedHopDong.id();
        UUID nhaThauId = resolvedNhaThau.id();
        boolean coDanhSach = query != null || nhaThauId != null || hopDongId != null || resolvedDoiTuong.id() != null || tinhThanhId != null
                || khuVucId != null || loaiId != null || trangThaiLoc != null || kieuLoc != null || giaiDoanLoc != null || moOnly != null
                || canBoId != null || ngayTaoFrom != null || ngayTaoTo != null;
        int top = topIn != null ? Math.min(Math.max(topIn, 1), 10) : 5;
        // Ngưỡng quá hạn (ngày): ưu tiên tham số quaHanNgay, sau đó sinceDate, mặc định 30 ngày (khớp cột quaHan30Ngay của REST).
        int soNgay = quaHanNgay != null && quaHanNgay >= 0 ? quaHanNgay
                : sinceDateIn != null ? (int) java.time.temporal.ChronoUnit.DAYS.between(sinceDateIn, LocalDate.now()) : 30;
        Instant nguongNgay = Instant.now().minus(Duration.ofDays(soNgay));
        var pageable = PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE);
        boolean coFilter = hopDongId != null || nhaThauId != null || tinhThanhId != null || resolvedDoiTuong.id() != null
                || khuVucId != null || loaiId != null || giaiDoanLoc != null
                || canBoId != null || ngayTaoFrom != null || ngayTaoTo != null;

        // Giai đoạn 1: các truy vấn độc lập chạy song song
        var fCounts = parallel.async(() -> vuongMacToolRepository.thongKeTongQuan(sinceDate, denKy, hopDongId, nhaThauId, tinhThanhId, resolvedDoiTuong.id(),
                khuVucId, giaiDoanLoc, trangThaiLoc, kieuLoc, moOnly, loaiId, canBoId, ngayTaoFrom, ngayTaoTo));
        var fKieu = parallel.async(() -> coFilter
                ? vuongMacToolRepository.countGroupByKieuFiltered(hopDongId, nhaThauId, tinhThanhId, resolvedDoiTuong.id(), khuVucId, giaiDoanLoc, loaiId, canBoId, ngayTaoFrom, ngayTaoTo)
                : vuongMacToolRepository.countGroupByKieu());
        var fSearch = coDanhSach
                ? parallel.async(() -> vuongMacToolRepository.search(ResolveSupport.likePattern(query), nhaThauId, hopDongId,
                        resolvedDoiTuong.id(), tinhThanhId, khuVucId, giaiDoanLoc, trangThaiLoc, kieuLoc, moOnly, loaiId, canBoId, ngayTaoFrom, ngayTaoTo, pageable))
                : null;
        var fRankHd = hopDongId == null
                ? parallel.async(() -> vuongMacToolRepository.rankHopDongByOpenCount(PageRequest.of(0, top), hopDongId, nhaThauId, tinhThanhId,
                        resolvedDoiTuong.id(), khuVucId, giaiDoanLoc, kieuLoc, loaiId, canBoId, ngayTaoFrom, ngayTaoTo).stream()
                        .map(row -> RankedItemDto.builder().label(row.getLabel()).name(row.getName()).value(row.getValue()).build())
                        .toList())
                : null;
        var fRankKv = khuVucId == null
                ? parallel.async(() -> vuongMacToolRepository.rankKhuVucByOpenCount(PageRequest.of(0, top), hopDongId, nhaThauId, tinhThanhId,
                        resolvedDoiTuong.id(), giaiDoanLoc, kieuLoc, loaiId, canBoId, ngayTaoFrom, ngayTaoTo).stream()
                        .map(row -> RankedItemDto.builder().label(row.getLabel()).value(row.getValue()).build())
                        .toList())
                : null;
        var fQuaHan = parallel.async(() -> vuongMacToolRepository.findQuaHan(nguongNgay, pageable));

        // Giai đoạn 2: bổ sung tên cho 2 danh sách (chỉ biết id sau giai đoạn 1), cũng song song
        Page<VuongMac> searchPage = fSearch != null ? McpParallel.get(fSearch) : null;
        Page<VuongMac> quaHanPage = McpParallel.get(fQuaHan);
        var fEnrichDs = searchPage != null ? enrichAsync(searchPage.getContent()) : null;
        var fEnrichQh = enrichAsync(quaHanPage.getContent());

        VuongMacTongQuanDto tongQuan = computeTongQuan(sinceDate, McpParallel.get(fCounts), McpParallel.get(fKieu));
        PagedResult<VuongMacItemDto> danhSach = searchPage == null ? null
                : PagedResult.of(computeDtos(searchPage.getContent(), McpParallel.get(fEnrichDs)),
                        searchPage.getNumber(), searchPage.getSize(), searchPage.getTotalElements());
        PagedResult<VuongMacItemDto> quaHan = PagedResult.of(computeDtos(quaHanPage.getContent(), McpParallel.get(fEnrichQh)),
                quaHanPage.getNumber(), quaHanPage.getSize(), quaHanPage.getTotalElements());
        boolean doiTuongCuThe = doiTuongComponent.laCuThe(resolvedDoiTuong);
        boolean rankDisabled = hopDongId != null || khuVucId != null || doiTuongCuThe;
        List<RankedItemDto> xepHang = fRankHd == null || rankDisabled ? null : McpParallel.get(fRankHd);
        List<RankedItemDto> xepHangKhuVuc = fRankKv == null || rankDisabled ? null : McpParallel.get(fRankKv);

        return VuongMacQueryResponse.builder()
                .tongQuan(tongQuan)
                .danhSach(danhSach)
                .xepHangHopDong(xepHang)
                .xepHangKhuVuc(xepHangKhuVuc)
                .quaHan(quaHan)
                .build();
    }

    private VuongMacTongQuanDto computeTongQuan(Instant sinceDate, List<Object[]> countRows, List<Object[]> kieuRows) {
        Object[] counts = countRows.get(0);
        long total = ((Number) counts[0]).longValue();
        long pending = ((Number) counts[1]).longValue();
        long inProgress = ((Number) counts[2]).longValue();
        long resolved = ((Number) counts[3]).longValue();
        long rejected = ((Number) counts[4]).longValue();
        long overdue30Days = ((Number) counts[5]).longValue();
        Long resolvedTrongKy = sinceDate == null ? null : ((Number) counts[6]).longValue();

        Map<String, Long> countsByKieu = new LinkedHashMap<>();
        for (String type : List.of("giai_phong_mat_bang", "thiet_ke", "thu_tuc_phap_ly", "nguon_vat_lieu", "thi_cong")) {
            countsByKieu.put(StatusLabels.kieuVuongMac(type), 0L);
        }
        for (Object[] row : kieuRows) {
            countsByKieu.put(StatusLabels.kieuVuongMac((String) row[0]), ((Number) row[1]).longValue());
        }

        return VuongMacTongQuanDto.builder()
                .total(total)
                .pending(pending)
                .inProgress(inProgress)
                .resolved(resolved)
                .rejected(rejected)
                .overdue30Days(overdue30Days)
                .resolvedTrongKy(resolvedTrongKy)
                .countsByKieu(countsByKieu)
                .build();
    }

    /** Lấy thông tin bổ sung (tên hợp đồng, khu vực, nhà thầu…) cho danh sách vướng mắc; chạy song song. */
    private java.util.concurrent.CompletableFuture<List<Object[]>> enrichAsync(List<VuongMac> entities) {
        if (entities.isEmpty()) {
            return java.util.concurrent.CompletableFuture.completedFuture(List.of());
        }
        List<UUID> ids = entities.stream().map(VuongMac::getId).toList();
        return parallel.async(() -> vuongMacToolRepository.enrichForIds(ids));
    }

    private List<VuongMacItemDto> computeDtos(List<VuongMac> entities, List<Object[]> enrichRows) {
        if (entities.isEmpty()) {
            return List.of();
        }
        Map<UUID, Object[]> enrichById = new HashMap<>();
        for (Object[] row : enrichRows) {
            enrichById.put((UUID) row[0], row);
        }

        return entities.stream().map(entity -> {
            Object[] enrich = enrichById.get(entity.getId());
            VuongMacItemDto dto = new VuongMacItemDto();
            dto.setId(entity.getId());
            dto.setMa(entity.getMa());
            dto.setGiaiDoan(entity.getGiaiDoan());
            dto.setKieuVuongMac(StatusLabels.kieuVuongMac(entity.getKieuVuongMac()));
            dto.setCoTheBoSungSanLuong(entity.getCoTheBoSungSanLuong());
            dto.setMoTa(entity.getMoTa());
            dto.setMoTaDayDu(entity.getMoTaDayDu());
            dto.setTenNguoiBaoCao(entity.getTenNguoiBaoCao());
            dto.setTrangThai(StatusLabels.vuongMac(entity.getTrangThai()));
            dto.setQuaHan30Ngay(entity.getQuaHan30Ngay());
            dto.setHoatDong(entity.getHoatDong());
            dto.setNgayTao(entity.getNgayTao());
            if (enrich != null) {
                dto.setHopDong(HopDongInfo.of(entity.getHopDongId(), (String) enrich[1], (String) enrich[2]));
                dto.setTenLoaiHopDong((String) enrich[3]);
                dto.setKhuVuc((String) enrich[4]);
                dto.setTinh((String) enrich[5]);
                dto.setNhaThau((String) enrich[6]);
                dto.setTenNguoiXuLy((String) enrich[7]);
            }
            return dto;
        }).toList();
    }

    /** Chuẩn hoá tham số chuỗi lọc: cắt khoảng trắng, chữ thường; rỗng thì bỏ lọc. */
    private static String chuanHoa(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
