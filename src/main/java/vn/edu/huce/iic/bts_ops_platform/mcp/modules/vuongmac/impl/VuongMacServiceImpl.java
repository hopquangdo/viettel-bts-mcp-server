package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.impl;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituong.dto.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.RankedItemDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.VuongMacItemDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.VuongMacToolMapper;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.VuongMacTongQuanDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacQuaHanResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacTimKiemResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacXepHangResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.request.VuongMacFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.vuongmac.VuongMac;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacXepHangContext;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacXepHangRegistry;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.VuongMacToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/** Owns filter resolution, repository orchestration, and result calculation for Vướng mắc tools. */
@Service
@RequiredArgsConstructor
public class VuongMacServiceImpl implements VuongMacService {

    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");

    private final VuongMacToolRepository vuongMacToolRepository;
    private final HopDongComponent hopDongComponent;
    private final DoiTuongComponent doiTuongComponent;
    private final NhaThauComponent nhaThauComponent;
    private final TinhComponent tinhComponent;
    private final KhuVucComponent khuVucComponent;
    private final CanBoComponent canBoComponent;
    private final DanhMucComponent danhMucComponent;
    private final McpParallel parallel;
    private final VuongMacXepHangRegistry xepHangRegistry;

    private record Scope(UUID nhaThauId, UUID hopDongId, UUID doiTuongId, boolean doiTuongCuThe, UUID tinhThanhId,
                         UUID khuVucId, UUID loaiId, UUID canBoId, String trangThaiLoc, String kieuLoc, String giaiDoanLoc,
                         Boolean moOnly, Instant ngayTaoFrom, Instant ngayTaoTo, String thieuNhanLuc) {

        boolean coPhamVi() {
            return hopDongId != null || nhaThauId != null || tinhThanhId != null || doiTuongId != null || khuVucId != null
                    || loaiId != null || giaiDoanLoc != null || canBoId != null || ngayTaoFrom != null || ngayTaoTo != null;
        }

        boolean coDieuKienTim() {
            return coPhamVi() || trangThaiLoc != null || kieuLoc != null || moOnly != null || thieuNhanLuc != null;
        }
    }

    public VuongMacTongQuanResponse tongQuan(VuongMacFilter filter, LocalDate sinceDate) {
        return toModuleTongQuan(McpParallel.get(tongQuanAsync(resolve(filter), sinceDate)));
    }

    public VuongMacTimKiemResponse timKiem(VuongMacFilter filter, String query, Integer page, Integer pageSize) {
        return new VuongMacTimKiemResponse(McpParallel.get(danhSachAsync(resolve(filter), query, pageOf(page, pageSize))));
    }

    public VuongMacXepHangResponse xepHang(VuongMacFilter filter, String theo, Integer top) {
        VuongMacXepHangChieu chieu = VuongMacXepHangChieu.of(theo);
        Scope s = resolve(filter);
        VuongMacXepHangStrategy strategy = xepHangRegistry.get(chieu);
        if (s.doiTuongCuThe()) {
            return new VuongMacXepHangResponse(chieu.label(), VuongMacToolMapper.TIEU_CHI_XEP_HANG, null,
                "Đang lọc 1 đối tượng cụ thể nên không có bảng xếp hạng. Bỏ tham số doiTuong để xếp hạng,"
                    + " hoặc dùng vuongmac_tim_kiem để xem vướng mắc của đối tượng đó.");
        }
        VuongMacXepHangContext ctx = xepHangContext(s, top);
        String lyDo = strategy.lyDoBoQua(ctx);
        if (lyDo != null) {
            String goiY = xepHangRegistry.all().values().stream()
                    .filter(st -> st.chieu() != chieu && st.lyDoBoQua(ctx) == null)
                    .map(st -> "theo=" + st.chieu().code() + " (" + st.chieu().label() + ")")
                    .collect(Collectors.joining(", "));
            return new VuongMacXepHangResponse(chieu.label(), VuongMacToolMapper.TIEU_CHI_XEP_HANG, null,
                goiY.isEmpty() ? lyDo : lyDo + " Có thể thử " + goiY + ".");
        }
        var rows = McpParallel.get(parallel.async(() -> strategy.rank(ctx)));
        var root = VuongMacToolMapper.xepHang(chieu, rows, null);
        return new VuongMacXepHangResponse(root.xepHangTheo(), root.tieuChi(), root.danhSach(), root.ghiChu());
    }

    public VuongMacQuaHanResponse quaHan(Integer quaHanNgay, LocalDate sinceDate, Integer page, Integer pageSize) {
        int soNgay = soNgayQuaHan(quaHanNgay, sinceDate);
        return new VuongMacQuaHanResponse(soNgay, McpParallel.get(quaHanAsync(soNgay, pageOf(page, pageSize))));
    }

    public VuongMacQueryResponse query(String doiTuong, String hopDong, String nhaThau, String khuVuc, String tinhThanh, String loaiHopDong,
                                       String trangThai, String kieuVuongMac, String giaiDoan, Boolean dangMoOnly,
                                       String query, Integer top, LocalDate sinceDate,
                                       Integer page, Integer pageSize, Integer quaHanNgay,
                                       String canBo, LocalDate fromDate, LocalDate toDate) {
        Scope s = resolve(new VuongMacFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, trangThai, kieuVuongMac,
                giaiDoan, dangMoOnly, canBo, fromDate, toDate));
        var pageable = pageOf(page, pageSize);
        var tongQuan = tongQuanAsync(s, sinceDate).thenApply(VuongMacServiceImpl::toModuleTongQuan);
        var danhSach = query != null || s.coDieuKienTim()
                ? danhSachAsync(s, query, pageable)
                : CompletableFuture.<PagedResult<VuongMacItemDto>>completedFuture(null);
        var xepHangHd = xepHangAsync(s, VuongMacXepHangChieu.HOP_DONG, top);
        var xepHangKv = xepHangAsync(s, VuongMacXepHangChieu.KHU_VUC, top);
        var quaHan = quaHanAsync(soNgayQuaHan(quaHanNgay, sinceDate), pageable);

        return VuongMacQueryResponse.builder()
                .tongQuan(McpParallel.get(tongQuan))
                .danhSach(McpParallel.get(danhSach))
                .xepHangHopDong(McpParallel.get(xepHangHd))
                .xepHangKhuVuc(McpParallel.get(xepHangKv))
                .quaHan(McpParallel.get(quaHan))
                .build();
    }

    private Scope resolve(VuongMacFilter f) {
        var fNhaThau = parallel.async(() -> nhaThauComponent.resolve(f.phamVi().nhaThau()));
        var fHopDong = parallel.async(() -> hopDongComponent.resolve(f.phamVi().hopDong()));
        var fDoiTuong = parallel.async(() -> doiTuongComponent.resolve(f.phamVi().doiTuong()));
        var fTinhThanhId = parallel.async(() -> tinhComponent.resolveId(f.phamVi().tinhThanh()));
        var fCanBo = parallel.async(() -> canBoComponent.resolve(f.canBo()));
        UUID khuVucId = khuVucComponent.resolveInScope(f.phamVi().khuVuc()).id();
        UUID loaiId = danhMucComponent.resolveLoaiHopDong(f.phamVi().loaiHopDong());

        LocalDate fromDate = f.ky() != null ? f.ky().fromDate() : null;
        LocalDate toDate = f.ky() != null ? f.ky().toDate() : null;
        boolean nguoc = fromDate != null && toDate != null && fromDate.isAfter(toDate);
        LocalDate tuNgay = nguoc ? toDate : fromDate;
        LocalDate denNgay = nguoc ? fromDate : toDate;

        DoiTuongInfo doiTuong = McpParallel.get(fDoiTuong);
        return new Scope(
                McpParallel.get(fNhaThau).id(),
                McpParallel.get(fHopDong).id(),
                doiTuong.id(),
                doiTuongComponent.laCuThe(doiTuong),
                McpParallel.get(fTinhThanhId),
                khuVucId,
                loaiId,
                McpParallel.get(fCanBo).id(),
                ResolveSupport.enumValue("trangThai", f.trangThai(), "pending", "in_progress", "resolved", "rejected"),
                chuanHoa(f.kieuVuongMac()),
                chuanHoa(f.giaiDoan()),
                Boolean.TRUE.equals(f.dangMoOnly()) ? Boolean.TRUE : null,
                tuNgay != null ? tuNgay.atStartOfDay(ZONE_VN).toInstant() : null,
                denNgay != null ? denNgay.plusDays(1).atStartOfDay(ZONE_VN).toInstant() : null,
                ResolveSupport.enumValue("thieuNhanLuc", f.thieuNhanLuc(), "nguoi_xu_ly", "nha_thau", "bat_ky"));
    }

    private static VuongMacTongQuanResponse toModuleTongQuan(VuongMacTongQuanDto dto) {
        return new VuongMacTongQuanResponse(
                dto.getTotal(),
                dto.getPending(),
                dto.getInProgress(),
                dto.getResolved(),
                dto.getRejected(),
                dto.getPending() + dto.getInProgress(),
                dto.getOverdue30Days(),
                dto.getResolvedTrongKy(),
                dto.getCountsByKieu(),
                dto.getSoHopDongDangMo(),
                dto.getSoDoiTuongDangMo());
    }

    private static PageRequest pageOf(Integer page, Integer pageSize) {
        return PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE);
    }

    private static int soNgayQuaHan(Integer quaHanNgay, LocalDate sinceDate) {
        if (quaHanNgay != null && quaHanNgay >= 0) {
            return quaHanNgay;
        }
        return sinceDate != null ? (int) java.time.temporal.ChronoUnit.DAYS.between(sinceDate, LocalDate.now()) : 30;
    }

    private CompletableFuture<VuongMacTongQuanDto> tongQuanAsync(Scope s, LocalDate sinceDateIn) {
        Instant sinceDate = s.ngayTaoFrom() != null ? s.ngayTaoFrom()
                : sinceDateIn != null ? sinceDateIn.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant denKy = s.ngayTaoTo() != null ? s.ngayTaoTo() : Instant.now();
        var fCounts = parallel.async(() -> vuongMacToolRepository.thongKeTongQuan(sinceDate, denKy, s.hopDongId(), s.nhaThauId(),
                s.tinhThanhId(), s.doiTuongId(), s.khuVucId(), s.giaiDoanLoc(), s.trangThaiLoc(), s.kieuLoc(), s.moOnly(), s.loaiId(),
                s.canBoId(), s.ngayTaoFrom(), s.ngayTaoTo()));
        var fKieu = parallel.async(() -> s.coPhamVi()
                ? vuongMacToolRepository.countGroupByKieuFiltered(s.hopDongId(), s.nhaThauId(), s.tinhThanhId(), s.doiTuongId(),
                        s.khuVucId(), s.giaiDoanLoc(), s.loaiId(), s.canBoId(), s.ngayTaoFrom(), s.ngayTaoTo())
                : vuongMacToolRepository.countGroupByKieu());
        return fCounts.thenCombine(fKieu, (counts, kieu) -> computeTongQuan(sinceDate, counts, kieu));
    }

    private CompletableFuture<PagedResult<VuongMacItemDto>> danhSachAsync(Scope s, String query, PageRequest pageable) {
        return parallel.async(() -> vuongMacToolRepository.search(ResolveSupport.likePattern(query), s.nhaThauId(), s.hopDongId(),
                        s.doiTuongId(), s.tinhThanhId(), s.khuVucId(), s.giaiDoanLoc(), s.trangThaiLoc(), s.kieuLoc(), s.moOnly(),
                        s.loaiId(), s.canBoId(), s.ngayTaoFrom(), s.ngayTaoTo(), s.thieuNhanLuc(), pageable))
                .thenCompose(this::toPagedDtos);
    }

    private static VuongMacXepHangContext xepHangContext(Scope s, Integer topIn) {
        int top = topIn != null ? Math.min(Math.max(topIn, 1), 10) : 5;
        return new VuongMacXepHangContext(s.hopDongId(), s.nhaThauId(), s.tinhThanhId(), s.doiTuongId(), s.khuVucId(),
                s.giaiDoanLoc(), s.kieuLoc(), s.loaiId(), s.canBoId(), s.ngayTaoFrom(), s.ngayTaoTo(), top);
    }

    private CompletableFuture<List<RankedItemDto>> xepHangAsync(Scope s, VuongMacXepHangChieu chieu, Integer topIn) {
        if (s.hopDongId() != null || s.khuVucId() != null || s.doiTuongCuThe()) {
            return CompletableFuture.completedFuture(null);
        }
        VuongMacXepHangStrategy strategy = xepHangRegistry.get(chieu);
        VuongMacXepHangContext ctx = xepHangContext(s, topIn);
        return parallel.async(() -> strategy.rank(ctx));
    }

    private CompletableFuture<PagedResult<VuongMacItemDto>> quaHanAsync(int soNgay, PageRequest pageable) {
        Instant nguongNgay = Instant.now().minus(Duration.ofDays(soNgay));
        return parallel.async(() -> vuongMacToolRepository.findQuaHan(nguongNgay, pageable)).thenCompose(this::toPagedDtos);
    }

    private CompletableFuture<PagedResult<VuongMacItemDto>> toPagedDtos(Page<VuongMac> page) {
        return enrichAsync(page.getContent()).thenApply(enrich -> PagedResult.of(computeDtos(page.getContent(), enrich),
                page.getNumber(), page.getSize(), page.getTotalElements()));
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
        long soHopDongDangMo = ((Number) counts[7]).longValue();
        long soDoiTuongDangMo = ((Number) counts[8]).longValue();

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
                .soHopDongDangMo(soHopDongDangMo)
                .soDoiTuongDangMo(soDoiTuongDangMo)
                .countsByKieu(countsByKieu)
                .build();
    }

    private CompletableFuture<List<Object[]>> enrichAsync(List<VuongMac> entities) {
        if (entities.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
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

    private static String chuanHoa(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}