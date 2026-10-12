package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.impl;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.GeoRefResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.security.DataScopeService;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.CanBoComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.DoiTuongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.KhuVucComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.TinhComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.CanBoInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituong.dto.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.KhuVucInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.ChuaPhanCongRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongChuaPhanCongResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongChuaPhanCongToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongDanhSachResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongTheoCanBoResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongTongQuanToolResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongToolThongKe;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongTyLeHuyItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongTyLeHuyResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongXepHangNhaThauResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongXepHangNhaThauToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.TongQuanDoiTuongRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.XepHangNhaThauRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.phancong.PhanCong;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.query.PhanCongDanhSachQuery;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.query.PhanCongDanhSachResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpFormat;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Coordinates assignment tools and response calculations. */
@Service
@RequiredArgsConstructor
public class PhanCongServiceImpl implements PhanCongService {

    private final PhanCongEntityToolRepository phanCongRepository;
    private final PhanCongToolRepository phanCongToolRepository;
    private final HopDongComponent hopDongComponent;
    private final KhuVucComponent khuVucComponent;
    private final NhaThauComponent nhaThauComponent;
    private final TinhComponent tinhComponent;
    private final CanBoComponent canBoComponent;
    private final DoiTuongComponent doiTuongComponent;
    private final McpParallel parallel;
    private final DataScopeService dataScopeService;
    private final ObjectProvider<PhanCongRepository> typedRepository;

    private static final BigDecimal NGUONG_TY_LE_HUY_MAC_DINH = BigDecimal.valueOf(15);
    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");

    private record Scope(String vung, UUID nhaThauId, String tenNhaThau, UUID hopDongId, UUID khuVucId, UUID tinhThanhId,
                         UUID doiTuongId, boolean doiTuongCuThe, String giaiDoanLoc, Instant ngayTaoFrom, Instant ngayTaoTo) {
    }

    /** id -> [mã, tên]. */
    private record Catalog(Map<UUID, String[]> khuVucById, Map<UUID, String[]> tinhThanhById) {
    }

    private record EnrichFutures(CompletableFuture<Map<UUID, String[]>> hopDong,
                                 CompletableFuture<Map<UUID, String[]>> doiTuong,
                                 CompletableFuture<Set<UUID>> coVuongMo) {
    }

        public vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.response.PhanCongDanhSachResponse danhSach(
            vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.request.PhanCongFilter filter, Integer page, Integer pageSize) {
        PhanCongDanhSachQuery query = PhanCongCalc.toQuery(filter, page, pageSize);
        PhanCongDanhSachResult result = typedRepository.getObject().danhSach(query);
        return PhanCongCalc.toResponse(result, query.page(), query.pageSize());
    }

    public PhanCongTongQuanToolResponse tongQuan(PhanCongFilter filter) {
        return PhanCongTongQuanToolResponse.of(McpParallel.get(tongQuanAsync(resolve(filter))));
    }

    public PhanCongTyLeHuyResponse tyLeHuyTram(String theo, Boolean chiVuotNguong) {
        String chieu = ResolveSupport.enumValue("theo", theo == null ? "nha_thau" : theo, "nha_thau", "khu_vuc");
        var fRows = parallel.async(() -> "khu_vuc".equals(chieu)
                ? phanCongToolRepository.tyLeHuyTheoKhuVuc() : phanCongToolRepository.tyLeHuyTheoNhaThau());
        BigDecimal cauHinh = phanCongToolRepository.nguongTyLeHuy();
        BigDecimal nguong = cauHinh != null ? cauHinh : NGUONG_TY_LE_HUY_MAC_DINH;
        List<PhanCongTyLeHuyItem> items = new ArrayList<>();
        long soVuot = 0;
        for (Object[] r : McpParallel.get(fRows)) {
            long tong = ((Number) r[1]).longValue();
            long huy = ((Number) r[2]).longValue();
            BigDecimal tyLe = tong <= 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(huy * 100.0 / tong).setScale(2, RoundingMode.HALF_UP);
            boolean vuot = tyLe.compareTo(nguong) > 0;
            soVuot += vuot ? 1 : 0;
            if (!Boolean.TRUE.equals(chiVuotNguong) || vuot) {
                items.add(new PhanCongTyLeHuyItem((String) r[0], tong, huy, McpFormat.phanTram(tyLe), vuot));
            }
        }
        return new PhanCongTyLeHuyResponse(chieu, McpFormat.phanTram(nguong), soVuot, items);
    }

    public PhanCongDanhSachResponse danhSach(PhanCongFilter filter, String query, Boolean lichSu, Integer page, Integer pageSize) {
        var catalog = catalogAsync();
        Scope s = resolve(filter);
        return new PhanCongDanhSachResponse(McpParallel.get(danhSachAsync(s, query, lichSu, pageOf(page, pageSize), catalog)));
    }

    public PhanCongTheoCanBoResponse theoCanBo(String canBo, LocalDate fromDate, LocalDate toDate, Integer page, Integer pageSize) {
        var catalog = catalogAsync();
        CanBoInfo resolvedCanBo = canBoComponent.resolve(canBo);
        Instant[] khoang = khoangNgayTao(fromDate, toDate);
        return new PhanCongTheoCanBoResponse(resolvedCanBo,
                McpParallel.get(theoCanBoAsync(resolvedCanBo, khoang[0], khoang[1], page, pageSize, catalog)));
    }

    public PhanCongChuaPhanCongResponse chuaPhanCong(PhanCongFilter filter, Integer page, Integer pageSize) {
        return new PhanCongChuaPhanCongResponse(McpParallel.get(chuaPhanCongAsync(resolve(filter), pageOf(page, pageSize))));
    }

    public PhanCongXepHangNhaThauResponse xepHangNhaThau(PhanCongFilter filter, Integer top) {
        Scope s = resolve(filter);
        return new PhanCongXepHangNhaThauResponse(rankDisabled(s, null) ? null : McpParallel.get(xepHangAsync(s, top)));
    }

    public PhanCongQueryResponse query(String vung, String nhaThau, String canBo,
                                       String query, String hopDong, String khuVuc, String tinhThanh, String doiTuong, String giaiDoan,
                                       Boolean lichSu, Integer topIn,
                                       Integer page, Integer pageSize,
                                       LocalDate fromDate, LocalDate toDate) {
        var fCanBo = parallel.async(() -> canBoComponent.resolve(canBo));
        Scope s = resolve(new PhanCongFilter(vung, nhaThau, hopDong, khuVuc, tinhThanh, doiTuong, giaiDoan, fromDate, toDate));
        CanBoInfo resolvedCanBo = McpParallel.get(fCanBo);
        Boolean lichSuLoc = Boolean.TRUE.equals(lichSu) ? Boolean.TRUE : null;
        var pageable = pageOf(page, pageSize);
        boolean coDanhSach = query != null || s.tenNhaThau() != null || s.vung() != null || s.tinhThanhId() != null
                || s.doiTuongId() != null || s.giaiDoanLoc() != null
                || lichSuLoc != null || s.ngayTaoFrom() != null || s.ngayTaoTo() != null;

        var catalog = catalogAsync();
        var tongQuan = tongQuanAsync(s);
        var danhSach = coDanhSach
                ? danhSachAsync(s, query, lichSu, pageable, catalog)
                : CompletableFuture.<PagedResult<PhanCongToolItem>>completedFuture(null);
        var theoCanBo = theoCanBoAsync(resolvedCanBo, s.ngayTaoFrom(), s.ngayTaoTo(), page, pageSize, catalog);
        var chua = chuaPhanCongAsync(s, pageable);
        var rank = rankDisabled(s, resolvedCanBo)
                ? CompletableFuture.<List<PhanCongXepHangNhaThauToolItem>>completedFuture(null)
                : xepHangAsync(s, topIn);

        return PhanCongQueryResponse.builder()
                .tongQuan(McpParallel.get(tongQuan))
                .danhSach(McpParallel.get(danhSach))
                .theoCanBo(McpParallel.get(theoCanBo))
                .chuaPhanCong(McpParallel.get(chua))
                .xepHangNhaThau(McpParallel.get(rank))
                .build();
    }

    private Scope resolve(PhanCongFilter f) {
        var fNhaThau = parallel.async(() -> nhaThauComponent.resolve(f.nhaThau()));
        var fHopDong = parallel.async(() -> hopDongComponent.resolve(f.hopDong()));
        var fTinhThanhId = parallel.async(() -> tinhComponent.resolveId(f.tinhThanh()));
        var fDoiTuong = parallel.async(() -> doiTuongComponent.resolve(f.doiTuong()));
        KhuVucInfo khuVuc = khuVucComponent.resolveInScope(f.khuVuc());
        NhaThauInfo nhaThau = McpParallel.get(fNhaThau);
        HopDongInfo hopDong = McpParallel.get(fHopDong);
        UUID tinhThanhId = McpParallel.get(fTinhThanhId);
        DoiTuongInfo doiTuong = McpParallel.get(fDoiTuong);
        Instant[] khoang = khoangNgayTao(f.fromDate(), f.toDate());
        return new Scope(f.vung(), nhaThau.id(), nhaThau.ten(), hopDong.id(), khuVuc.id(), tinhThanhId,
                doiTuong.id(), doiTuongComponent.laCuThe(doiTuong), ResolveSupport.normalize(f.giaiDoan()), khoang[0], khoang[1]);
    }

    private static Instant[] khoangNgayTao(LocalDate fromDate, LocalDate toDate) {
        boolean nguoc = fromDate != null && toDate != null && fromDate.isAfter(toDate);
        LocalDate tuNgay = nguoc ? toDate : fromDate;
        LocalDate denNgay = nguoc ? fromDate : toDate;
        return new Instant[]{
                tuNgay != null ? tuNgay.atStartOfDay(ZONE_VN).toInstant() : null,
                denNgay != null ? denNgay.plusDays(1).atStartOfDay(ZONE_VN).toInstant() : null};
    }

    private static PageRequest pageOf(Integer page, Integer pageSize) {
        return PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE);
    }

    private static boolean rankDisabled(Scope s, CanBoInfo canBo) {
        return s.nhaThauId() != null || (canBo != null && canBo.id() != null) || s.doiTuongCuThe();
    }

    private CompletableFuture<Catalog> catalogAsync() {
        return parallel.async(() -> idToMaTen(phanCongToolRepository.findAllKhuVucIdMaTen()))
                .thenCombine(parallel.async(() -> idToMaTen(phanCongToolRepository.findAllTinhThanhIdMaTen())), Catalog::new);
    }

    private CompletableFuture<PhanCongToolThongKe> tongQuanAsync(Scope s) {
        var fThongKe = parallel.async(() -> phanCongRepository.thongKe(s.vung(), ResolveSupport.likePattern(s.tenNhaThau()), s.hopDongId(),
                s.khuVucId(), s.tinhThanhId(), s.doiTuongId(), s.giaiDoanLoc(), s.ngayTaoFrom(), s.ngayTaoTo()));
        var fDoiTuong = parallel.async(() -> phanCongToolRepository.tongQuanTheoDoiTuong(s.hopDongId(), s.nhaThauId(), s.khuVucId(),
                s.tinhThanhId(), s.doiTuongId()));
        boolean motKhuVuc = s.khuVucId() != null || dataScopeService.scopedKhuVucCount(Long.MAX_VALUE) == 1L;
        var fSoKhuVuc = motKhuVuc ? CompletableFuture.completedFuture(1L)
                : parallel.async(phanCongToolRepository::countKhuVucHoatDong);
        return fThongKe.thenCombine(fDoiTuong, (rows, row) -> {
            PhanCongToolThongKe tongQuan = buildThongKe(rows);
            applyThongKeDoiTuong(tongQuan, row);
            return tongQuan;
        }).thenCombine(fSoKhuVuc, (tongQuan, soKhuVuc) -> {
            tongQuan.setSoKhuVuc(soKhuVuc);
            return tongQuan;
        });
    }

    private CompletableFuture<PagedResult<PhanCongToolItem>> danhSachAsync(Scope s, String query, Boolean lichSu, PageRequest pageable,
                                                                          CompletableFuture<Catalog> catalog) {
        Boolean lichSuLoc = Boolean.TRUE.equals(lichSu) ? Boolean.TRUE : null;
        return parallel.async(() -> phanCongRepository.search(ResolveSupport.likePattern(query), s.vung(),
                        ResolveSupport.likePattern(s.tenNhaThau()), s.tinhThanhId(), s.hopDongId(), s.khuVucId(), s.doiTuongId(),
                        s.giaiDoanLoc(), lichSuLoc, s.ngayTaoFrom(), s.ngayTaoTo(), pageable))
                .thenCompose(p -> toPagedResult(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), catalog));
    }

    private CompletableFuture<PagedResult<PhanCongToolItem>> theoCanBoAsync(CanBoInfo canBo, Instant ngayTaoFrom, Instant ngayTaoTo,
                                                                           Integer page, Integer pageSize, CompletableFuture<Catalog> catalog) {
        if (canBo.id() == null) {
            return CompletableFuture.completedFuture(null);
        }
        int resolvedPage = PagingUtil.resolvePage(page);
        int resolvedPageSize = PagingUtil.resolvePageSize(pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE);
        return parallel.async(() -> phanCongRepository.findActiveForContractor(canBo.id(), canBo.ten(), canBo.username(), ngayTaoFrom, ngayTaoTo))
                .thenCompose(all -> {
                    int from = Math.min(resolvedPage * resolvedPageSize, all.size());
                    List<PhanCong> slice = all.subList(from, Math.min(from + resolvedPageSize, all.size()));
                    return toPagedResult(slice, resolvedPage, resolvedPageSize, all.size(), catalog);
                });
    }

    private CompletableFuture<PagedResult<PhanCongChuaPhanCongToolItem>> chuaPhanCongAsync(Scope s, PageRequest pageable) {
        return parallel.async(() -> phanCongToolRepository.findChuaPhanCong(s.hopDongId(), s.khuVucId(), s.tinhThanhId(), s.doiTuongId(), pageable))
                .thenApply(p -> PagedResult.of(p.map(PhanCongServiceImpl::mapChuaPhanCong).getContent(),
                        p.getNumber(), p.getSize(), p.getTotalElements()));
    }

    private CompletableFuture<List<PhanCongXepHangNhaThauToolItem>> xepHangAsync(Scope s, Integer topIn) {
        int top = topIn != null ? Math.min(Math.max(topIn, 1), 20) : 5;
        return parallel.async(() -> computeXepHangNhaThau(top, s.hopDongId(), s.khuVucId(), s.tinhThanhId(), s.doiTuongId(),
                s.giaiDoanLoc(), s.vung()));
    }

    private List<PhanCongXepHangNhaThauToolItem> computeXepHangNhaThau(int top, UUID hopDongId, UUID khuVucId,
                                                                      UUID tinhThanhId, UUID doiTuongId, String giaiDoan, String vung) {
        return phanCongToolRepository.rankNhaThau(PageRequest.of(0, top), hopDongId, khuVucId, tinhThanhId, doiTuongId, giaiDoan, vung).stream()
                .map(PhanCongServiceImpl::mapXepHangNhaThau)
                .toList();
    }

    private static PhanCongChuaPhanCongToolItem mapChuaPhanCong(ChuaPhanCongRow row) {
        PhanCongChuaPhanCongToolItem item = new PhanCongChuaPhanCongToolItem();
        item.setDoiTuong(DoiTuongInfo.of(row.getMaDoiTuong(), row.getTenDoiTuong()));
        item.setHopDong(HopDongInfo.of(null, row.getMaHopDong(), row.getTenHopDong()));
        item.setKhuVuc(row.getKhuVuc());
        item.setTinh(row.getTinh());
        return item;
    }

    private static PhanCongXepHangNhaThauToolItem mapXepHangNhaThau(XepHangNhaThauRow row) {
        PhanCongXepHangNhaThauToolItem item = new PhanCongXepHangNhaThauToolItem();
        item.setTenNhaThau(row.getTenNhaThau());
        item.setSoDoiTuong(row.getSoDoiTuong());
        item.setSoDangVuongMac(row.getSoDangVuongMac());
        item.setTongSanLuongHieuLuc(row.getTongSanLuongHieuLuc());
        return item;
    }

    private void applyThongKeDoiTuong(PhanCongToolThongKe tongQuan, TongQuanDoiTuongRow row) {
        long tongTram = row.getTongTram();
        long daPhan = row.getDaPhanNhaThau();
        tongQuan.setTongHoatDong(tongTram);
        tongQuan.setTongPhanCong(daPhan);
        tongQuan.setTongDaPhanNhaThau(daPhan);
        tongQuan.setSoNhaThau(row.getSoNhaThau());
        tongQuan.setTongHoanThanh(row.getHoanThanh());
        tongQuan.setTongVuongMac(row.getVuongMac());
        tongQuan.setTongTon(row.getTon());
        tongQuan.setTyLeDaPhanNhaThau(tongTram > 0 ? Math.round(daPhan * 10000.0 / tongTram) / 100.0 : 0.0);
    }

    private PhanCongToolThongKe buildThongKe(List<Object[]> rows) {
        long tong = 0;
        long hoatDong = 0;
        Map<String, Long> theoKhuVuc = new HashMap<>();
        Map<String, Long> theoTinhThanh = new HashMap<>();
        for (Object[] row : rows) {
            long so = ((Number) row[3]).longValue();
            tong += so;
            if (Boolean.TRUE.equals(row[2])) {
                hoatDong += so;
            }
            theoKhuVuc.merge(row[0] == null ? "null" : row[0].toString(), so, Long::sum);
            theoTinhThanh.merge(row[1] == null ? "null" : row[1].toString(), so, Long::sum);
        }
        PhanCongToolThongKe thongKe = new PhanCongToolThongKe();
        thongKe.setTongPhanCong(tong);
        thongKe.setTongHoatDong(hoatDong);
        thongKe.setTheoKhuVuc(theoKhuVuc);
        thongKe.setTheoTinhThanh(theoTinhThanh);
        return thongKe;
    }

    private EnrichFutures enrichAsync(List<PhanCong> content) {
        List<UUID> hopDongIds = content.stream().map(PhanCong::getHopDongId).filter(java.util.Objects::nonNull).distinct().toList();
        List<UUID> doiTuongIds = content.stream().map(PhanCong::getHopDongDoiTuongId).filter(java.util.Objects::nonNull).distinct().toList();
        var hopDong = hopDongIds.isEmpty()
                ? CompletableFuture.completedFuture(Map.<UUID, String[]>of())
                : parallel.async(() -> idToMaTen(phanCongToolRepository.findHopDongIdMaTenByIds(hopDongIds)));
        var doiTuong = doiTuongIds.isEmpty()
                ? CompletableFuture.completedFuture(Map.<UUID, String[]>of())
                : parallel.async(() -> idToMaTen(phanCongToolRepository.findDoiTuongMaLoaiByIds(doiTuongIds)));
        var coVuongMo = doiTuongIds.isEmpty()
                ? CompletableFuture.completedFuture(Set.<UUID>of())
                : parallel.async(() -> Set.copyOf(phanCongToolRepository.findDoiTuongIdsCoVuongDangMo(doiTuongIds)));
        return new EnrichFutures(hopDong, doiTuong, coVuongMo);
    }

    private CompletableFuture<PagedResult<PhanCongToolItem>> toPagedResult(List<PhanCong> content, int page, int pageSize, long total,
                                                                          CompletableFuture<Catalog> catalog) {
        EnrichFutures enrich = enrichAsync(content);
        return CompletableFuture.allOf(enrich.hopDong(), enrich.doiTuong(), enrich.coVuongMo(), catalog).thenApply(v -> {
            Set<UUID> coVuongMo = enrich.coVuongMo().join();
            Map<UUID, String[]> hopDongById = enrich.hopDong().join();
            Map<UUID, String[]> doiTuongById = enrich.doiTuong().join();
            Catalog c = catalog.join();
            List<PhanCongToolItem> items = content.stream()
                    .map(entity -> toItem(entity, coVuongMo, hopDongById, doiTuongById, c)).toList();
            return PagedResult.of(items, page, pageSize, total);
        });
    }

    /** Rows [id, mã, tên] -> id -> [mã, tên]. */
    private static Map<UUID, String[]> idToMaTen(List<Object[]> rows) {
        Map<UUID, String[]> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put((UUID) row[0], new String[]{(String) row[1], (String) row[2]});
        }
        return result;
    }

    private static String[] maTen(Map<UUID, String[]> byId, UUID id) {
        String[] value = id == null ? null : byId.get(id);
        return value != null ? value : new String[]{null, null};
    }

    private PhanCongToolItem toItem(PhanCong entity, Set<UUID> coVuongMo, Map<UUID, String[]> hopDongById,
                                     Map<UUID, String[]> doiTuongById, Catalog catalog) {
        PhanCongToolItem item = new PhanCongToolItem();
        item.setId(entity.getId());
        String[] hopDong = maTen(hopDongById, entity.getHopDongId());
        item.setHopDong(HopDongInfo.of(entity.getHopDongId(), hopDong[0], hopDong[1]));
        if (entity.getHopDongDoiTuongId() != null) {
            String[] doiTuong = maTen(doiTuongById, entity.getHopDongDoiTuongId());
            item.setDoiTuong(new DoiTuongInfo(entity.getHopDongDoiTuongId(), doiTuong[0], doiTuong[1]));
        }
        item.setNhaThau(entity.getNhaThau());
        item.setGiaiDoan(entity.getGiaiDoan());
        String[] khuVuc = maTen(catalog.khuVucById(), entity.getKhuVucId());
        item.setKhuVuc(GeoRefResponse.of(entity.getKhuVucId(), khuVuc[0], khuVuc[1]));
        item.setMaVung(entity.getMaVung());
        String[] tinhThanh = maTen(catalog.tinhThanhById(), entity.getTinhThanhId());
        item.setTinhThanh(GeoRefResponse.of(entity.getTinhThanhId(), tinhThanh[0], tinhThanh[1]));
        item.setHoatDong(entity.getHoatDong());
        item.setNgayTao(entity.getNgayTao());
        item.setNgayCapNhat(entity.getNgayCapNhat());
        item.setNgayXoa(entity.getNgayXoa());
        item.setCoVuongMacMo(entity.getHopDongDoiTuongId() != null && coVuongMo.contains(entity.getHopDongDoiTuongId()));
        return item;
    }
}