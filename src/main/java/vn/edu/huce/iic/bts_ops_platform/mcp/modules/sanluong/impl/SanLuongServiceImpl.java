package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.impl;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.OutputSummaryDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PeriodInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PeriodTrendDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.ProgressSummaryDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankedEntityDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankingOverviewDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongPeriodTrendDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongNhaThauItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongDoiTuongChuaCoItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongNghiemThuDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.DoiTuongListProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.HangMucDoiTuongProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongHangMucDoiTuongDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongHangMucItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongDoiTuongItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.DoiTuongProgressProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.ProgressGroupProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.NhaThauChuaBaoProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.NhaThauDaBaoProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.TrendInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituong.dto.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.KhuVucInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.query.SanLuongProgressGroupQuery;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.response.SanLuongProgressSummaryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongXepHangContext;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongXepHangRegistry;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.SanLuongToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.NumberUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PeriodComparisonHelper;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.RankingBuilder;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.TrendCalculator;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.util.VietnamDateUtils;

import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Impl tool AI module Sản lượng. Mỗi method public (1 tool sanluong_*) tự quy đổi bộ lọc ({@link #resolve}) rồi
 * chạy đúng các truy vấn của phần nó trả về ({@code xxxAsync}). {@link #query} (đủ mọi phần, cho tool gộp ngoài
 * module) dùng lại các phần đó, chung 1 lần resolve và 1 lần progressBundle.
 */
@Component
@RequiredArgsConstructor
public class SanLuongServiceImpl implements SanLuongService {

    private final SanLuongToolRepository sanLuongToolRepository;
    private final DoiTuongComponent doiTuongComponent;
    private final HopDongComponent hopDongComponent;
    private final NhaThauComponent nhaThauComponent;
    private final KhuVucComponent khuVucComponent;
    private final TinhComponent tinhComponent;
    private final DanhMucComponent danhMucComponent;
    private final McpParallel parallel;
    private final SanLuongXepHangRegistry xepHangRegistry;
        private final SanLuongCalc calc = new SanLuongCalc();

        public SanLuongProgressSummaryResponse tienDo(
            vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.request.SanLuongFilter filter,
            Double nguongHoanThanhThap) {
        SanLuongFilter legacyFilter = new SanLuongFilter(filter.doiTuong(), filter.hopDong(), filter.nhaThau(),
            filter.khuVuc(), filter.tinhThanh(), filter.loaiHopDong(), filter.fromDate(), filter.toDate());
        List<SanLuongProgressGroupQuery> groups = groupsOf(McpParallel.get(
                bundleAsync(resolve(legacyFilter), null, defaultPage()))).stream()
            .map(group -> new SanLuongProgressGroupQuery(group.getN(), group.getTotalHangMuc(), group.getDoneHangMuc()))
            .toList();
        return calc.tienDo(groups, nguongHoanThanhThap != null ? nguongHoanThanhThap : DEFAULT_NGUONG_HOAN_THANH_THAP);
        }

    private static final long MOC_SOM_NHAT_TTL_MS = 10 * 60 * 1000L;
    private volatile LocalDate mocSomNhat;
    private volatile boolean mocSomNhatDaCo;
    private volatile long mocSomNhatLuc;

    /** Ngày sản lượng sớm nhất hiếm khi đổi: nhớ 10 phút để khỏi tốn 1 lần khứ hồi DB ở đầu mỗi lần gọi. */
    private LocalDate ngayThucHienSomNhat() {
        long now = System.currentTimeMillis();
        if (!mocSomNhatDaCo || now - mocSomNhatLuc > MOC_SOM_NHAT_TTL_MS) {
            mocSomNhat = sanLuongToolRepository.findNgayThucHienSomNhat();
            mocSomNhatLuc = now;
            mocSomNhatDaCo = true;
        }
        return mocSomNhat;
    }

    /**
     * Bộ lọc đã quy đổi ra id + khoảng ngày thực dùng. fromDateIn/toDateIn giữ giá trị người gọi truyền (null = không lọc)
     * cho các truy vấn nghiệm thu, vốn không lấy mặc định ngày như phần còn lại.
     */
    private record Scope(LocalDate fromDateIn, LocalDate toDateIn, LocalDate fromDate, LocalDate toDate, LocalDate today,
                         UUID nhaThauId, UUID hopDongId, String doiTuongIds, UUID khuVucId, UUID tinhThanhId, UUID loaiHopDongId) {

        PeriodInfo period() {
            return new PeriodInfo(fromDate.toString(), toDate.toString(), fromDate + " → " + toDate);
        }

        Instant fromInstant() {
            return fromDate.atStartOfDay(VietnamDateUtils.ZONE).toInstant();
        }

        Instant toInstantExclusive() {
            return toDate.plusDays(1).atStartOfDay(VietnamDateUtils.ZONE).toInstant();
        }
    }

    // ------------------------------------------------------------------ API theo từng tool

    public SanLuongTongQuanToolResponse tongQuan(SanLuongFilter filter, Double nguongHoanThanhThap) {
        Scope s = resolve(filter);
        double nguong = nguongHoanThanhThap != null ? nguongHoanThanhThap : DEFAULT_NGUONG_HOAN_THANH_THAP;
        return SanLuongToolMapper.tongQuan(McpParallel.get(tongQuanAsync(s, bundleAsync(s, null, defaultPage()), nguong)), nguong);
    }

    public SanLuongXepHangResponse xepHang(SanLuongFilter filter, String theo, String xepHangTheo, Boolean tangDan) {
        SanLuongXepHangChieu chieu = SanLuongXepHangChieu.of(theo); // sai mã thì báo lỗi trước khi chạm DB
        Scope s = resolve(filter);
        SanLuongXepHangStrategy strategy = xepHangRegistry.get(chieu);
        String khoaXepHang = khoaXepHang(xepHangTheo);
        boolean tang = Boolean.TRUE.equals(tangDan);
        return McpParallel.get(xepHangContextAsync(s, bundleAsync(s, null, defaultPage()), hangMucAsync(s), khoaXepHang, tang)
                .thenApply(ctx -> {
                    if (ctx == null) {
                        return SanLuongToolMapper.xepHang(chieu, s.period(), khoaXepHang, tang, null,
                                "Đang lọc 1 đối tượng cụ thể nên không có bảng xếp hạng. Bỏ tham số doiTuong để xếp hạng,"
                                        + " hoặc dùng sanluong_hang_muc_doi_tuong để xem chi tiết đối tượng đó.");
                    }
                    String lyDo = strategy.lyDoBoQua(ctx);
                    if (lyDo != null) {
                        return SanLuongToolMapper.xepHang(chieu, s.period(), khoaXepHang, tang, null,
                                lyDo + " Có thể thử " + goiYChieuKhac(chieu, ctx) + ".");
                    }
                    return SanLuongToolMapper.xepHang(chieu, s.period(), khoaXepHang, tang, strategy.rank(ctx), null);
                }));
    }

    public SanLuongXuHuongResponse xuHuong(SanLuongFilter filter) {
        Scope s = resolve(filter);
        return McpParallel.get(xuHuongAsync(s, bundleAsync(s, null, defaultPage())));
    }

    public SanLuongNhaThauBaoCaoResponse nhaThauBaoCao(SanLuongFilter filter, String trangThai, Integer page, Integer pageSize) {
        SanLuongTrangThaiBaoCao tt = SanLuongTrangThaiBaoCao.of(trangThai); // sai mã thì báo lỗi trước khi chạm DB
        Scope s = resolve(filter);
        return SanLuongToolMapper.nhaThauBaoCao(tt, s.period(), McpParallel.get(nhaThauBaoCaoAsync(s, tt, pageOf(page, pageSize))));
    }

    public SanLuongNghiemThuResponse nghiemThu(SanLuongFilter filter, Integer page, Integer pageSize) {
        return McpParallel.get(nghiemThuAsync(resolve(filter), pageOf(page, pageSize)));
    }

    public SanLuongDoiTuongChuaCoResponse doiTuongChuaCo(SanLuongFilter filter, Integer page, Integer pageSize) {
        Scope s = resolve(filter);
        PageRequest pageNt = pageOf(page, pageSize);
        return McpParallel.get(doiTuongChuaCoAsync(s, bundleAsync(s, null, pageNt), pageNt));
    }

    public SanLuongDanhSachDoiTuongResponse danhSachDoiTuong(SanLuongFilter filter, Boolean includeWithoutOutput, String sapXep,
                                                            Integer page, Integer pageSize) {
        return McpParallel.get(danhSachDoiTuongAsync(resolve(filter), includeWithoutOutput, sapXep, pageOf(page, pageSize)));
    }

    public SanLuongHangMucDoiTuongResponse hangMucDoiTuong(String doiTuong) {
        String doiTuongIds = resolveDoiTuongIds(doiTuong);
        List<SanLuongHangMucDoiTuongDto> hangMuc = doiTuongIds == null ? List.of()
                : gomHangMuc(sanLuongToolRepository.hangMucCuaDoiTuong(doiTuongIds));
        return new SanLuongHangMucDoiTuongResponse(hangMuc);
    }

    /** Đủ mọi phần trong 1 lần (system_overview, hồ sơ đối tượng): resolve 1 lần, progressBundle 1 lần, các phần chạy song song. */
    public SanLuongQueryResponse query(String doiTuong, String hopDong, String nhaThau,
                                       String khuVuc, String tinhThanh, LocalDate fromDate, LocalDate toDate,
                                       Integer page, Integer pageSize, Double nguongHoanThanhThap, Boolean includeWithoutOutput,
                                       String sapXep, String loaiHopDong, String xepHangTheo, Boolean tangDan) {
        Scope s = resolve(new SanLuongFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, fromDate, toDate));
        PageRequest pageNt = pageOf(page, pageSize);
        Boolean chiCoSanLuong = Boolean.FALSE.equals(includeWithoutOutput) ? Boolean.TRUE : null;
        var bundle = bundleAsync(s, chiCoSanLuong, pageNt);
        var hangMuc = hangMucAsync(s);

        var tongQuan = tongQuanAsync(s, bundle, nguongHoanThanhThap);
        var xepHang = xepHangTongHopAsync(s, bundle, hangMuc, xepHangTheo, tangDan);
        var xuHuong = xuHuongAsync(s, bundle);
        var nhaThauDaBao = nhaThauBaoCaoAsync(s, SanLuongTrangThaiBaoCao.DA_BAO, pageNt);
        var nhaThauChuaBao = nhaThauBaoCaoAsync(s, SanLuongTrangThaiBaoCao.CHUA_BAO, pageNt);
        var nghiemThu = nghiemThuAsync(s, pageNt);
        var doiTuongChuaCo = doiTuongChuaCoAsync(s, bundle, pageNt);
        var danhSach = danhSachDoiTuongAsync(s, includeWithoutOutput, sapXep, pageNt);

        SanLuongTongQuanResponse tq = McpParallel.get(tongQuan);
        SanLuongXepHangTongHopDto xh = McpParallel.get(xepHang);
        PagedResult<SanLuongNhaThauItem> daBao = McpParallel.get(nhaThauDaBao);
        PagedResult<SanLuongNhaThauItem> chuaBao = McpParallel.get(nhaThauChuaBao);
        return SanLuongQueryResponse.builder()
                .period(s.period())
                .summary(tq.summary())
                .progress(tq.progress())
                .trend(tq.trend())
                .nhaThau(xh.nhaThau())
                .khuVuc(xh.khuVuc())
                .tinh(xh.tinh())
                .hopDong(xh.hopDong())
                .loaiHopDong(xh.loaiHopDong())
                .doiTuong(xh.doiTuong())
                .periodTrend(McpParallel.get(xuHuong).periodTrend())
                .nhaThauDaBaoTrongKy(daBao)
                .nhaThauChuaBaoTrongKy(chuaBao)
                .nghiemThu(McpParallel.get(nghiemThu).nghiemThu())
                .doiTuongChuaCoSanLuong(McpParallel.get(doiTuongChuaCo).doiTuong())
                .danhSachDoiTuong(McpParallel.get(danhSach).doiTuong())
                .hangMucDoiTuong(McpParallel.get(hangMuc))
                .build();
    }

    // ------------------------------------------------------------------ quy đổi bộ lọc

    private Scope resolve(SanLuongFilter f) {
        LocalDate fromDateIn = f.fromDate();
        LocalDate toDateIn = f.toDate();
        LocalDate toDate = toDateIn != null ? toDateIn : LocalDate.now();
        // Không truyền khoảng ngày nào: lấy TOÀN BỘ thời gian (khớp trang Sản lượng của FE, REST tong-hop không giới hạn ngày);
        // chỉ truyền 1 đầu thì đầu còn lại giữ cách cũ (đến hôm nay / lùi 30 ngày).
        LocalDate fromDate;
        if (fromDateIn != null) {
            fromDate = fromDateIn;
        } else if (toDateIn == null) {
            LocalDate somNhat = ngayThucHienSomNhat();
            fromDate = somNhat != null && !somNhat.isAfter(toDate) ? somNhat : toDate.minusDays(30);
        } else {
            fromDate = toDate.minusDays(30);
        }

        // các tra cứu id chạy song song (mỗi tra cứu 1-2 lần khứ hồi tới DB nếu chưa có trong bộ nhớ đệm)
        var fNhaThau = parallel.async(() -> nhaThauComponent.resolve(f.nhaThau()));
        var fHopDong = parallel.async(() -> hopDongComponent.resolve(f.hopDong()));
        var fTinhThanhId = parallel.async(() -> tinhComponent.resolveId(f.tinhThanh()));
        var fLoaiHopDongId = parallel.async(() -> danhMucComponent.resolveLoaiHopDong(f.loaiHopDong()));
        String doiTuongIds = resolveDoiTuongIds(f.doiTuong());
        KhuVucInfo khuVuc = khuVucComponent.resolveInScope(f.khuVuc());

        return new Scope(fromDateIn, toDateIn, fromDate, toDate, LocalDate.now(),
                McpParallel.get(fNhaThau).id(), McpParallel.get(fHopDong).id(), doiTuongIds, khuVuc.id(),
                McpParallel.get(fTinhThanhId), McpParallel.get(fLoaiHopDongId));
    }

    /** doiTuong nhận 1 hoặc nhiều giá trị ngăn cách bằng dấu phẩy/chấm phẩy; mỗi giá trị tra cứu song song. null = không lọc. */
    private String resolveDoiTuongIds(String doiTuong) {
        var futures = tachDanhSach(doiTuong).stream().map(v -> parallel.async(() -> doiTuongComponent.resolve(v))).toList();
        if (futures.isEmpty()) {
            return null;
        }
        return futures.stream().map(McpParallel::get).map(DoiTuongInfo::id).distinct().map(UUID::toString)
                .collect(Collectors.joining(","));
    }

    private static PageRequest pageOf(Integer page, Integer pageSize) {
        return PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE);
    }

    private static PageRequest defaultPage() {
        return pageOf(null, null);
    }

    // ------------------------------------------------------------------ truy vấn dùng chung

    /** Nhóm đối tượng (G) + top đối tượng (T) + trang đối tượng chưa có sản lượng (C) trong 1 câu: bảng đối tượng chỉ tính 1 lần. */
    private CompletableFuture<List<ProgressBundleRow>> bundleAsync(Scope s, Boolean chiCoSanLuong, PageRequest pageNt) {
        return parallel.async(() -> sanLuongToolRepository.progressBundle(chiCoSanLuong, s.fromDate(), s.toDate(),
                s.nhaThauId(), s.hopDongId(), s.doiTuongIds(), s.khuVucId(), s.tinhThanhId(), s.loaiHopDongId(),
                pageNt.getPageSize(), pageNt.getOffset()));
    }

    private static List<ProgressGroupProjection> groupsOf(List<ProgressBundleRow> bundle) {
        return bundle.stream().filter(r -> "G".equals(r.getKind())).map(r -> (ProgressGroupProjection) r).toList();
    }

    /** Hạng mục của đối tượng cụ thể; null khi không lọc đối tượng. Loại đối tượng (không phải mã) cho danh sách rỗng. */
    private CompletableFuture<List<SanLuongHangMucDoiTuongDto>> hangMucAsync(Scope s) {
        if (s.doiTuongIds() == null) {
            return CompletableFuture.completedFuture(null);
        }
        return parallel.async(() -> gomHangMuc(sanLuongToolRepository.hangMucCuaDoiTuong(s.doiTuongIds())));
    }

    // ------------------------------------------------------------------ từng phần (ghép future, không chặn chờ trong pool)

    private CompletableFuture<SanLuongTongQuanResponse> tongQuanAsync(Scope s, CompletableFuture<List<ProgressBundleRow>> bundle,
                                                                      Double nguongHoanThanhThap) {
        var previousPeriod = PeriodComparisonHelper.previousPeriod(s.fromDate(), s.toDate());
        var fAggregate = parallel.async(() -> sanLuongToolRepository.aggregateTongHop(s.fromDate(), s.toDate(),
                previousPeriod.fromDate(), previousPeriod.toDate(), s.today(),
                s.nhaThauId(), s.hopDongId(), s.doiTuongIds(), s.khuVucId(), s.tinhThanhId(), s.loaiHopDongId()));
        var fHoanThanh = parallel.async(() -> sanLuongToolRepository.thanhTienHoanThanh(s.fromDate(), s.toDate(),
                previousPeriod.fromDate(), previousPeriod.toDate(),
                s.nhaThauId(), s.hopDongId(), s.doiTuongIds(), s.khuVucId(), s.tinhThanhId(), s.loaiHopDongId()));
        var fIssue = parallel.async(() -> sanLuongToolRepository.demVuongMacMoTrongKy(s.fromInstant(), s.toInstantExclusive(),
                s.nhaThauId(), s.hopDongId(), s.doiTuongIds(), s.khuVucId(), s.tinhThanhId(), s.loaiHopDongId()));

        return CompletableFuture.allOf(fAggregate, fHoanThanh, fIssue, bundle).thenApply(ignored -> {
            var aggregate = fAggregate.join().orElse(null);
            int totalObjects = aggregate != null ? NumberUtil.nz(aggregate.getTotalDisplayed()) : 0;
            int objectsWithOutput = aggregate != null ? NumberUtil.nz(aggregate.getWithOutput()) : 0;
            BigDecimal periodValue = aggregate != null ? NumberUtil.nz(aggregate.getPeriodTotal()) : BigDecimal.ZERO;
            BigDecimal todayValue = aggregate != null ? NumberUtil.nz(aggregate.getTodayTotal()) : BigDecimal.ZERO;
            BigDecimal previousValue = aggregate != null ? NumberUtil.nz(aggregate.getPreviousTotal()) : BigDecimal.ZERO;

            OutputSummaryDto summary = OutputSummaryDto.builder()
                    .totalObjects(totalObjects)
                    .objectsWithOutput(objectsWithOutput)
                    .objectsWithoutOutput(Math.max(totalObjects - objectsWithOutput, 0))
                    .periodValue(periodValue)
                    .todayValue(todayValue)
                    .averageValuePerObject(totalObjects > 0
                            ? periodValue.divide(BigDecimal.valueOf(totalObjects), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO)
                    .unit("VNĐ")
                    .openIssueCount((int) (long) fIssue.join())
                    .build();
            ProgressSummaryDto progress = computeProgress(groupsOf(bundle.join()),
                    nguongHoanThanhThap != null ? nguongHoanThanhThap : DEFAULT_NGUONG_HOAN_THANH_THAP);
            TrendInfo trend = TrendCalculator.compare(periodValue, previousValue, previousPeriod.label());
            Object[] hoanThanh = fHoanThanh.join().isEmpty() ? new Object[]{null, null} : fHoanThanh.join().get(0);
            return new SanLuongTongQuanResponse(s.period(), summary, progress, trend,
                    NumberUtil.toBigDecimal(hoanThanh[0]), NumberUtil.toBigDecimal(hoanThanh[1]));
        });
    }

    /**
     * Dữ liệu chung cho xếp hạng: nhóm đối tượng, top đối tượng, các chiều đang bị lọc. null khi lọc 1 đối tượng cụ thể
     * (không có bảng xếp hạng nào có nghĩa).
     */
    private CompletableFuture<SanLuongXepHangContext> xepHangContextAsync(Scope s, CompletableFuture<List<ProgressBundleRow>> bundle,
                                                                         CompletableFuture<List<SanLuongHangMucDoiTuongDto>> hangMuc,
                                                                         String khoaXepHang, boolean tang) {
        return bundle.thenCombine(hangMuc, (rows, hangMucDt) -> {
            if (hangMucDt != null && !hangMucDt.isEmpty()) {
                return null;
            }
            List<ProgressGroupProjection> groups = groupsOf(rows);
            List<DoiTuongProgressProjection> topDoiTuong = rows.stream().filter(r -> "T".equals(r.getKind()))
                    .map(r -> (DoiTuongProgressProjection) r).toList();
            long soDoiTuong = groups.stream().mapToLong(ProgressGroupProjection::getN).sum();
            return new SanLuongXepHangContext(groups, topDoiTuong, soDoiTuong, khoaXepHang, tang, capDaLoc(s));
        });
    }

    /** Chiều nào đang lọc đúng 1 giá trị thì xếp hạng theo chiều đó vô nghĩa. */
    private static Set<SanLuongXepHangChieu> capDaLoc(Scope s) {
        EnumSet<SanLuongXepHangChieu> cap = EnumSet.noneOf(SanLuongXepHangChieu.class);
        if (s.nhaThauId() != null) {
            cap.add(SanLuongXepHangChieu.NHA_THAU);
        }
        if (s.khuVucId() != null) {
            cap.add(SanLuongXepHangChieu.KHU_VUC);
        }
        if (s.tinhThanhId() != null) {
            cap.add(SanLuongXepHangChieu.TINH);
        }
        if (s.hopDongId() != null) {
            cap.add(SanLuongXepHangChieu.HOP_DONG);
        }
        if (s.loaiHopDongId() != null) {
            cap.add(SanLuongXepHangChieu.LOAI_HOP_DONG);
        }
        return cap;
    }

    /** "theo=tinh, theo=hop_dong" — các chiều còn xếp hạng được trong phạm vi hiện tại. */
    private String goiYChieuKhac(SanLuongXepHangChieu hienTai, SanLuongXepHangContext ctx) {
        return xepHangRegistry.all().values().stream()
                .filter(st -> st.chieu() != hienTai && st.lyDoBoQua(ctx) == null)
                .map(st -> "theo=" + st.chieu().code() + " (" + st.chieu().label() + ")")
                .collect(Collectors.joining(", "));
    }

    private static String khoaXepHang(String xepHangTheo) {
        return xepHangTheo == null || xepHangTheo.isBlank() ? "giaTri" : xepHangTheo.trim();
    }

    /** Mọi chiều trong 1 lần cho đường tổng quan: duyệt registry, chiều bị bỏ qua để null (JSON giữ nguyên như trước). */
    private CompletableFuture<SanLuongXepHangTongHopDto> xepHangTongHopAsync(Scope s, CompletableFuture<List<ProgressBundleRow>> bundle,
                                                                             CompletableFuture<List<SanLuongHangMucDoiTuongDto>> hangMuc,
                                                                             String xepHangTheo, Boolean tangDan) {
        return xepHangContextAsync(s, bundle, hangMuc, khoaXepHang(xepHangTheo), Boolean.TRUE.equals(tangDan)).thenApply(ctx -> {
            Map<SanLuongXepHangChieu, RankingOverviewDto> bang = new EnumMap<>(SanLuongXepHangChieu.class);
            if (ctx != null) {
                xepHangRegistry.all().forEach((chieu, st) -> {
                    if (st.lyDoBoQua(ctx) == null) {
                        bang.put(chieu, st.rank(ctx));
                    }
                });
            }
            return new SanLuongXepHangTongHopDto(s.period(),
                    bang.get(SanLuongXepHangChieu.NHA_THAU),
                    bang.get(SanLuongXepHangChieu.KHU_VUC),
                    bang.get(SanLuongXepHangChieu.TINH),
                    bang.get(SanLuongXepHangChieu.HOP_DONG),
                    bang.get(SanLuongXepHangChieu.LOAI_HOP_DONG),
                    bang.get(SanLuongXepHangChieu.DOI_TUONG));
        });
    }

    private CompletableFuture<SanLuongXuHuongResponse> xuHuongAsync(Scope s, CompletableFuture<List<ProgressBundleRow>> bundle) {
        List<LocalDate[]> buckets = periodBuckets(s.fromDate(), s.toDate());
        String bucketFrom = buckets.stream().map(x -> x[0].toString()).collect(Collectors.joining(","));
        String bucketTo = buckets.stream().map(x -> x[1].toString()).collect(Collectors.joining(","));
        var fCells = parallel.async(() -> sanLuongToolRepository.sanLuongTheoKy(bucketFrom, bucketTo,
                s.nhaThauId(), s.hopDongId(), s.doiTuongIds(), s.khuVucId(), s.tinhThanhId(), s.loaiHopDongId()));
        return bundle.thenCombine(fCells, (rows, cells) -> new SanLuongXuHuongResponse(s.period(),
                computePeriodTrend(s.fromDate(), s.toDate(), groupsOf(rows), buckets, cells)));
    }

    /** 1 trang nhà thầu theo trạng thái báo sản lượng: 2 trạng thái chỉ khác câu truy vấn và cách map dòng. */
    private CompletableFuture<PagedResult<SanLuongNhaThauItem>> nhaThauBaoCaoAsync(Scope s, SanLuongTrangThaiBaoCao trangThai,
                                                                                   PageRequest pageNt) {
        return switch (trangThai) {
            case DA_BAO -> nhaThauTrangAsync(pageNt,
                    () -> sanLuongToolRepository.findNhaThauDaBaoTrongKy(s.fromDate(), s.toDate(),
                            s.nhaThauId(), s.hopDongId(), s.doiTuongIds(), s.khuVucId(), s.tinhThanhId(), s.loaiHopDongId(), pageNt),
                    NhaThauDaBaoProjection::getTong,
                    () -> sanLuongToolRepository.demNhaThauDaBaoTrongKy(s.fromDate(), s.toDate(), s.nhaThauId(),
                            s.hopDongId(), s.doiTuongIds(), s.khuVucId(), s.tinhThanhId(), s.loaiHopDongId()),
                    SanLuongServiceImpl::mapNhaThauDaBao);
            case CHUA_BAO -> nhaThauTrangAsync(pageNt,
                    () -> sanLuongToolRepository.findNhaThauChuaBaoTrongKy(s.fromDate(), s.toDate(),
                            s.nhaThauId(), s.hopDongId(), s.doiTuongIds(), s.khuVucId(), s.tinhThanhId(), s.loaiHopDongId(), pageNt),
                    NhaThauChuaBaoProjection::getTong,
                    () -> sanLuongToolRepository.demNhaThauChuaBaoTrongKy(s.fromDate(), s.toDate(), s.nhaThauId(),
                            s.hopDongId(), s.doiTuongIds(), s.khuVucId(), s.tinhThanhId(), s.loaiHopDongId()),
                    SanLuongServiceImpl::mapNhaThauChuaBao);
        };
    }

    private <P> CompletableFuture<PagedResult<SanLuongNhaThauItem>> nhaThauTrangAsync(
            PageRequest pageNt, Supplier<List<P>> timTrang, Function<P, Long> tongTuDong, LongSupplier demRieng,
            Function<P, SanLuongNhaThauItem> map) {
        return parallel.async(() -> {
            List<P> rows = timTrang.get();
            return PagedResult.of(rows.stream().map(map).toList(), pageNt.getPageNumber(), pageNt.getPageSize(),
                    tongTheoTrang(rows.isEmpty() ? null : tongTuDong.apply(rows.get(0)), pageNt.getPageNumber(), demRieng));
        });
    }

    /** Lọc ngày theo ngày nghiệm thu, chỉ khi người gọi truyền ngày (không lấy mặc định toàn thời gian như phần khác). */
    private CompletableFuture<SanLuongNghiemThuResponse> nghiemThuAsync(Scope s, PageRequest pageNt) {
        Instant ntFrom = s.fromDateIn() != null ? s.fromDateIn().atStartOfDay(VietnamDateUtils.ZONE).toInstant() : null;
        Instant ntTo = s.toDateIn() != null ? s.toDateIn().plusDays(1).atStartOfDay(VietnamDateUtils.ZONE).toInstant() : null;
        var fKhongDat = parallel.async(() -> sanLuongToolRepository.findNghiemThuKhongDat(s.nhaThauId(), s.hopDongId(),
                s.doiTuongIds(), s.khuVucId(), s.tinhThanhId(), s.loaiHopDongId(), ntFrom, ntTo, pageNt));
        var fThongKe = parallel.async(() -> sanLuongToolRepository.thongKeNghiemThu(s.nhaThauId(), s.hopDongId(),
                s.doiTuongIds(), s.khuVucId(), s.tinhThanhId(), s.loaiHopDongId(), ntFrom, ntTo));
        var fCho = parallel.async(() -> sanLuongToolRepository.thongKeChoNghiemThu(s.nhaThauId(), s.hopDongId(),
                s.doiTuongIds(), s.khuVucId(), s.tinhThanhId(), s.loaiHopDongId(), s.fromDateIn(), s.toDateIn()));

        return CompletableFuture.allOf(fKhongDat, fThongKe, fCho).thenApply(ignored -> {
            long soDat = 0;
            BigDecimal giaTriDat = BigDecimal.ZERO;
            long soKhongDat = 0;
            BigDecimal giaTriKhongDat = BigDecimal.ZERO;
            for (Object[] r : fThongKe.join()) {
                if ("dat".equals(r[0])) {
                    soDat = ((Number) r[1]).longValue();
                    giaTriDat = NumberUtil.toBigDecimal(r[2]);
                } else {
                    soKhongDat = ((Number) r[1]).longValue();
                    giaTriKhongDat = NumberUtil.toBigDecimal(r[2]);
                }
            }
            Object[] cho = fCho.join().get(0);
            var khongDat = fKhongDat.join();
            return new SanLuongNghiemThuResponse(s.period(), SanLuongNghiemThuDto.builder()
                    .soDat(soDat)
                    .giaTriDat(giaTriDat)
                    .soKhongDat(soKhongDat)
                    .giaTriKhongDat(giaTriKhongDat)
                    .soChoNghiemThu(((Number) cho[0]).longValue())
                    .giaTriChoNghiemThu(NumberUtil.toBigDecimal(cho[1]))
                    .khongDat(PagedResult.of(khongDat.getContent(), khongDat.getNumber(), khongDat.getSize(), khongDat.getTotalElements()))
                    .build());
        });
    }

    private CompletableFuture<SanLuongDoiTuongChuaCoResponse> doiTuongChuaCoAsync(Scope s, CompletableFuture<List<ProgressBundleRow>> bundle,
                                                                                  PageRequest pageNt) {
        return bundle.thenApply(rows -> {
            var chuaCo = rows.stream().filter(r -> "C".equals(r.getKind())).toList();
            return new SanLuongDoiTuongChuaCoResponse(s.period(), PagedResult.of(chuaCo.stream()
                            .map(r -> SanLuongDoiTuongChuaCoItem.builder()
                                    .maDoiTuong(r.getMaDoiTuong())
                                    .loaiDoiTuong(r.getDoiTuongTen())
                                    .hopDong(r.getHopDongTen())
                                    .nhaThau(r.getNhaThauTen())
                                    .khuVuc(r.getKhuVucTen())
                                    .soHangMuc(r.getTotalHangMuc() != null ? r.getTotalHangMuc() : 0)
                                    .coVuongMacMo(Boolean.TRUE.equals(r.getHasOpenVuongMac()))
                                    .build()).toList(),
                    pageNt.getPageNumber(), pageNt.getPageSize(),
                    tongTheoTrang(chuaCo.isEmpty() ? null : chuaCo.get(0).getTong(), pageNt.getPageNumber(),
                            () -> sanLuongToolRepository.demDoiTuongChuaCoSanLuong(s.fromDate(), s.toDate(), s.nhaThauId(),
                                    s.hopDongId(), s.doiTuongIds(), s.khuVucId(), s.tinhThanhId(), s.loaiHopDongId()))));
        });
    }

    /** Danh sách từng đối tượng (đọc thêm lũy kế sản lượng). */
    private CompletableFuture<SanLuongDanhSachDoiTuongResponse> danhSachDoiTuongAsync(Scope s, Boolean includeWithoutOutput,
                                                                                      String sapXep, PageRequest pageNt) {
        return parallel.async(() -> sanLuongToolRepository.danhSachDoiTuong(
                        Boolean.TRUE.equals(includeWithoutOutput) ? Boolean.TRUE : null, chuanHoaSapXep(sapXep), s.today(),
                        s.fromDate(), s.toDate(), s.nhaThauId(), s.hopDongId(), s.doiTuongIds(), s.khuVucId(), s.tinhThanhId(),
                        s.loaiHopDongId(), pageNt.getPageSize(), pageNt.getOffset()))
                .thenApply(rows -> new SanLuongDanhSachDoiTuongResponse(s.period(),
                        danhSachTrang(rows, pageNt.getPageNumber(), pageNt.getPageSize(), () -> tongDanhSach(includeWithoutOutput,
                                sapXep, s.today(), s.fromDate(), s.toDate(), s.nhaThauId(), s.hopDongId(), s.doiTuongIds(),
                                s.khuVucId(), s.tinhThanhId(), s.loaiHopDongId()))));
    }

    private static List<SanLuongHangMucDoiTuongDto> gomHangMuc(List<HangMucDoiTuongProjection> rows) {
        Map<UUID, List<HangMucDoiTuongProjection>> byObj = new LinkedHashMap<>();
        for (var r : rows) {
            byObj.computeIfAbsent(r.getDoiTuongId(), k -> new ArrayList<>()).add(r);
        }
        List<SanLuongHangMucDoiTuongDto> out = new ArrayList<>();
        for (var list : byObj.values()) {
            List<SanLuongHangMucItem> items = list.stream().map(r -> SanLuongHangMucItem.builder()
                    .nhom(r.getNhom()).ma(r.getMaHangMuc()).ten(r.getTenHangMuc())
                    .trangThai(r.getTrangThai() != null ? r.getTrangThai() : "chua_co_ban_ghi")
                    .daLam("done".equalsIgnoreCase(r.getTrangThai()))
                    .donGia(r.getDonGia())
                    .khoiLuong(r.getKhoiLuong())
                    .giaTri(r.getTrangThai() != null ? r.getGiaTri() : null)
                    .ngayThucHien(r.getNgayThucHien() != null ? r.getNgayThucHien().toString() : null)
                    .ketQuaNghiemThu(r.getKetQuaNghiemThu())
                    .lyDoKhongDat(r.getLyDoKhongDat())
                    .build()).toList();
            int daLam = (int) items.stream().filter(SanLuongHangMucItem::isDaLam).count();
            out.add(SanLuongHangMucDoiTuongDto.builder().maDoiTuong(list.get(0).getMaDoiTuong())
                    .tongHangMuc(items.size()).daLam(daLam).chuaLam(items.size() - daLam).hangMuc(items).build());
        }
        return out;
    }

    private static String chuanHoaSapXep(String sapXep) {
        if (sapXep == null) {
            return "latest";
        }
        return switch (sapXep.trim()) {
            case "periodValue", "totalValue", "todayValue" -> sapXep.trim();
            default -> "latest";
        };
    }

    /** Trang rỗng ở trang sau (xin quá trang cuối) không mang được tổng: lấy tổng từ trang đầu. */
    private long tongDanhSach(Boolean includeWithoutOutput, String sapXep, LocalDate today, LocalDate fromDate, LocalDate toDate,
                              UUID nhaThauId, UUID hopDongId, String doiTuongIds, UUID khuVucId, UUID tinhThanhId, UUID loaiHopDongId) {
        var dau = sanLuongToolRepository.danhSachDoiTuong(Boolean.TRUE.equals(includeWithoutOutput) ? Boolean.TRUE : null,
                chuanHoaSapXep(sapXep), today, fromDate, toDate, nhaThauId, hopDongId, doiTuongIds, khuVucId, tinhThanhId, loaiHopDongId, 1, 0L);
        return dau.isEmpty() || dau.get(0).getTong() == null ? 0L : dau.get(0).getTong();
    }

    private static PagedResult<SanLuongDoiTuongItem> danhSachTrang(List<DoiTuongListProjection> rows, int page, int size,
                                                                    java.util.function.LongSupplier demRieng) {
        List<SanLuongDoiTuongItem> items = rows.stream().map(r -> SanLuongDoiTuongItem.builder()
                .maDoiTuong(r.getMaDoiTuong() != null ? r.getMaDoiTuong() : r.getDoiTuongTen())
                .loaiDoiTuong(r.getDoiTuongTen())
                .hopDong(r.getHopDongTen())
                .nhaThau(r.getNhaThauTen())
                .khuVuc(r.getKhuVucTen())
                .tinh(r.getTinhMa())
                .tenTinh(r.getTinhTen())
                .laTinhCu(Boolean.TRUE.equals(r.getLaTinhCu()))
                .giaTriTrongKy(NumberUtil.nz(r.getPeriodValue()))
                .slTong(NumberUtil.nz(r.getTotalValue()))
                .slHomNay(NumberUtil.nz(r.getTodayValue()))
                .thiCongGanNhat(r.getThiCongGanNhat() != null ? r.getThiCongGanNhat().toString() : null)
                .soHangMucHoanThanh(NumberUtil.nz(r.getDoneHangMuc()))
                .tongHangMuc(NumberUtil.nz(r.getTotalHangMuc()))
                .coVuongMacMo(Boolean.TRUE.equals(r.getHasOpenVuongMac()))
                .build()).toList();
        return PagedResult.of(items, page, size, tongTheoTrang(rows.isEmpty() ? null : rows.get(0).getTong(), page, demRieng));
    }

    /** Ngưỡng (%) dưới mức này thì đối tượng đang thi công dở được tính là hoàn thành thấp (lowCompletionCount). */
    private static final double DEFAULT_NGUONG_HOAN_THANH_THAP = 50.0;

    private ProgressSummaryDto computeProgress(List<ProgressGroupProjection> groups,
                                               double nguongHoanThanhThap) {
        SanLuongProgressSummaryResponse result = calc.tienDo(groups.stream()
                .map(group -> new SanLuongProgressGroupQuery(group.getN(), group.getTotalHangMuc(), group.getDoneHangMuc()))
                .toList(), nguongHoanThanhThap);
        return ProgressSummaryDto.builder()
                .completionRate(result.tyLeHoanThanh())
                .completedCount((int) result.soDoiTuongHoanThanh())
                .inProgressCount((int) result.soDoiTuongDangThiCong())
                .notStartedCount((int) result.soDoiTuongChuaBatDau())
                .lowCompletionCount((int) result.soDoiTuongHoanThanhThap())
                .build();
    }



    /**
     * Các kỳ con của xu hướng: theo ngày (dưới 7 ngày), tuần (đến 31 ngày), tháng (đến 365 ngày và trải tối đa 3 tháng lịch),
     * quý (đến 365 ngày nhưng trải trên 3 tháng lịch), còn lại theo năm.
     */
    private static String granularityOf(LocalDate fromDate, LocalDate toDate) {
        long days = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        if (days < 7) {
            return "day";
        }
        if (days <= 31) {
            return "week";
        }
        if (days > 365) {
            return "year";
        }
        long thang = ChronoUnit.MONTHS.between(fromDate.withDayOfMonth(1), toDate.withDayOfMonth(1)) + 1;
        return thang > 3 ? "quarter" : "month";
    }

    private static List<LocalDate[]> periodBuckets(LocalDate fromDate, LocalDate toDate) {
        String granularity = granularityOf(fromDate, toDate);
        List<LocalDate[]> buckets = new ArrayList<>();
        LocalDate bucketStart = fromDate;
        while (!bucketStart.isAfter(toDate)) {
            LocalDate bucketEnd = switch (granularity) {
                case "day" -> bucketStart;
                case "week" -> bucketStart.plusDays(6);
                case "month" -> bucketStart.with(TemporalAdjusters.lastDayOfMonth());
                case "quarter" -> LocalDate.of(bucketStart.getYear(), ((bucketStart.getMonthValue() - 1) / 3 + 1) * 3, 1)
                        .with(TemporalAdjusters.lastDayOfMonth());
                default -> bucketStart.with(TemporalAdjusters.lastDayOfYear());
            };
            bucketEnd = bucketEnd.isAfter(toDate) ? toDate : bucketEnd;
            buckets.add(new LocalDate[]{bucketStart, bucketEnd});
            bucketStart = bucketEnd.plusDays(1);
        }
        return buckets;
    }

    /**
     * Xu hướng theo kỳ con. {@code cellRows} là kết quả 1 truy vấn thưa ({@code sanLuongTheoKy}) cho mọi kỳ (đã chạy song song ở
     * luồng gọi). Số đối tượng, số đối tượng có hạng mục và số đối tượng đang vướng mắc là hằng số theo kỳ nên lấy từ {@code groups};
     * đối tượng không có sản lượng trong kỳ được tính là 0 hạng mục hoàn thành.
     */
    /**
     * Tổng số bản ghi: lấy từ cột window của trang; chỉ khi trang rỗng mà không phải trang đầu (xin quá trang cuối) mới phải đếm riêng.
     */
    private static long tongTheoTrang(Long tongTuTrang, int trang, java.util.function.LongSupplier demRieng) {
        if (tongTuTrang != null) {
            return tongTuTrang;
        }
        return trang == 0 ? 0L : demRieng.getAsLong();
    }

    private PeriodTrendDto computePeriodTrend(
            LocalDate fromDate, LocalDate toDate, List<ProgressGroupProjection> groups,
            List<LocalDate[]> buckets, List<Object[]> bucketRows) {
        long soDoiTuong = 0;
        long dangVuongMac = 0;
        for (var g : groups) {
            soDoiTuong += g.getN();
            if (NumberUtil.isTrue(g.getHasOpenVuongMac())) {
                dangVuongMac += g.getN();
            }
        }

        Map<Integer, BigDecimal> valueByBucket = new HashMap<>();
        for (Object[] row : bucketRows) {
            valueByBucket.put(((Number) row[0]).intValue(), NumberUtil.toBigDecimal(row[1]));
        }

        List<SanLuongPeriodTrendDto> values = new ArrayList<>();
        for (int i = 0; i < buckets.size(); i++) {
            values.add(SanLuongPeriodTrendDto.builder()
                    .period(buckets.get(i)[0] + " → " + buckets.get(i)[1])
                    .totalValue(valueByBucket.getOrDefault(i + 1, BigDecimal.ZERO))
                    .totalObjects((int) soDoiTuong)
                    .openIssueCount((int) dangVuongMac)
                    .build());
        }

        return PeriodTrendDto.<SanLuongPeriodTrendDto>builder()
                .fromDate(fromDate.toString())
                .toDate(toDate.toString())
                .granularity(granularityOf(fromDate, toDate))
                .values(values)
                .build();
    }

    private static SanLuongNhaThauItem mapNhaThauChuaBao(NhaThauChuaBaoProjection row) {
        SanLuongNhaThauItem item = new SanLuongNhaThauItem();
        item.setNhaThau(NhaThauInfo.of(row.getNhaThauId(), row.getTenNhaThau()));
        item.setSoDoiTuongPhuTrach(NumberUtil.nz(row.getSoDoiTuongPhuTrach()));
        return item;
    }

    private static SanLuongNhaThauItem mapNhaThauDaBao(NhaThauDaBaoProjection row) {
        SanLuongNhaThauItem item = new SanLuongNhaThauItem();
        item.setNhaThau(NhaThauInfo.of(row.getNhaThauId(), row.getTenNhaThau()));
        item.setSoDoiTuongPhuTrach(NumberUtil.nz(row.getSoDoiTuongPhuTrach()));
        item.setGiaTriDaBao(NumberUtil.nz(row.getGiaTriDaBao()));
        return item;
    }

    /** Tách danh sách đối tượng theo dấu phẩy/chấm phẩy/gạch đứng; bỏ giá trị rỗng và trùng. */
    private static List<String> tachDanhSach(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split("[,;|]")).map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
    }
}
