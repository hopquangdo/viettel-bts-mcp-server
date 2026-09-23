package vn.edu.huce.iic.bts_ops_platform.mcp.handler.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhathau.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.OutputSummaryDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PeriodInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PeriodTrendDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.ProgressSummaryDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.RankedEntityDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.RankingOverviewDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.SanLuongPeriodTrendDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.SanLuongNhaThauItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.SanLuongDoiTuongChuaCoItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.SanLuongNghiemThuDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.SanLuongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.DoiTuongListProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.HangMucDoiTuongProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.SanLuongHangMucDoiTuongDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.SanLuongHangMucItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.SanLuongDoiTuongItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.DoiTuongProgressProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.ProgressGroupProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.NhaThauChuaBaoProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.NhaThauDaBaoProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.TrendInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.doituong.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.khuvuc.KhuVucInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.handler.SanLuongToolHandler;
import vn.edu.huce.iic.bts_ops_platform.mcp.repository.SanLuongToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.NumberUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PeriodComparisonHelper;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.RankingBuilder;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.TrendCalculator;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.util.VietnamDateUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Impl thật cho tool AI module Sản lượng — chủ yếu gọi SanLuongToolRepository; đếm vướng mắc
 * mở trong kỳ tái dùng VuongMacService.demMoTrongGiaiDoan (scope theo doiTuongId đã lọc từ
 * progressNhom) để không lặp lại logic đếm vướng mắc.
 */
@Component
@RequiredArgsConstructor
public class SanLuongToolHandlerImpl implements SanLuongToolHandler {

    private final SanLuongToolRepository sanLuongToolRepository;
    private final DoiTuongComponent doiTuongComponent;
    private final HopDongComponent hopDongComponent;
    private final NhaThauComponent nhaThauComponent;
    private final KhuVucComponent khuVucComponent;
    private final TinhComponent tinhComponent;
    private final DanhMucComponent danhMucComponent;
    private final McpParallel parallel;

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

    @Override
    public SanLuongQueryResponse query(String doiTuong, String hopDong, String nhaThau,
                                       String khuVuc, String tinhThanh, LocalDate fromDateIn, LocalDate toDateIn,
                                       Integer page, Integer pageSize, Double nguongHoanThanhThap, Boolean includeWithoutOutput,
                                       String sapXep, String loaiHopDong, String xepHangTheo, Boolean tangDan) {
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
        LocalDate today = LocalDate.now();

        var f_resolvedNhaThau = parallel.async(() -> nhaThauComponent.resolve(nhaThau));
        var f_resolvedHopDong = parallel.async(() -> hopDongComponent.resolve(hopDong));
        // doiTuong nhận 1 hoặc nhiều giá trị ngăn cách bằng dấu phẩy/chấm phẩy; mỗi giá trị tra cứu song song
        var f_resolvedDoiTuongs = tachDanhSach(doiTuong).stream().map(v -> parallel.async(() -> doiTuongComponent.resolve(v))).toList();
        var f_resolvedTinhThanhId = parallel.async(() -> tinhComponent.resolveId(tinhThanh));
        var f_loaiHopDongId = parallel.async(() -> danhMucComponent.resolveLoaiHopDong(loaiHopDong));
        KhuVucInfo resolvedKhuVuc = khuVucComponent.resolveInScope(khuVuc);
        // các tra cứu id chạy song song (mỗi tra cứu 1-2 lần khứ hồi tới DB nếu chưa có trong bộ nhớ đệm)
        NhaThauInfo resolvedNhaThau = McpParallel.get(f_resolvedNhaThau);
        HopDongInfo resolvedHopDong = McpParallel.get(f_resolvedHopDong);
        String doiTuongIds = f_resolvedDoiTuongs.isEmpty() ? null : f_resolvedDoiTuongs.stream().map(McpParallel::get)
                .map(DoiTuongInfo::id).distinct().map(UUID::toString).collect(java.util.stream.Collectors.joining(","));
        Boolean chiCoSanLuong = Boolean.FALSE.equals(includeWithoutOutput) ? Boolean.TRUE : null;
        UUID resolvedTinhThanhId = McpParallel.get(f_resolvedTinhThanhId);
        UUID loaiHopDongId = McpParallel.get(f_loaiHopDongId);

        UUID nhaThauId = resolvedNhaThau.id();
        UUID hopDongId = resolvedHopDong.id();
        UUID khuVucId = resolvedKhuVuc.id();
        var previousPeriod = PeriodComparisonHelper.previousPeriod(fromDate, toDate);
        List<LocalDate[]> buckets = periodBuckets(fromDate, toDate);
        String bucketFrom = buckets.stream().map(x -> x[0].toString()).collect(java.util.stream.Collectors.joining(","));
        String bucketTo = buckets.stream().map(x -> x[1].toString()).collect(java.util.stream.Collectors.joining(","));

        // 7 truy vấn độc lập: chạy song song, độ trễ = câu chậm nhất thay vì tổng các câu
        var fAggregate = parallel.async(() -> sanLuongToolRepository.aggregateTongHop(fromDate, toDate,
                previousPeriod.fromDate(), previousPeriod.toDate(), today,
                nhaThauId, hopDongId, doiTuongIds, khuVucId, resolvedTinhThanhId, loaiHopDongId));
        var pageNt = PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE);
        // nhóm đối tượng + top đối tượng + trang đối tượng chưa có sản lượng: 1 câu, bảng đối tượng chỉ tính 1 lần
        var fBundle = parallel.async(() -> sanLuongToolRepository.progressBundle(chiCoSanLuong, fromDate, toDate,
                nhaThauId, hopDongId, doiTuongIds, khuVucId, resolvedTinhThanhId, loaiHopDongId, pageNt.getPageSize(), pageNt.getOffset()));
        var fIssue = parallel.async(() -> sanLuongToolRepository.demVuongMacMoTrongKy(
                fromDate.atStartOfDay(VietnamDateUtils.ZONE).toInstant(),
                toDate.plusDays(1).atStartOfDay(VietnamDateUtils.ZONE).toInstant(),
                nhaThauId, hopDongId, doiTuongIds, khuVucId, resolvedTinhThanhId, loaiHopDongId));
        var fCells = parallel.async(() -> sanLuongToolRepository.sanLuongTheoKy(bucketFrom, bucketTo,
                nhaThauId, hopDongId, doiTuongIds, khuVucId, resolvedTinhThanhId, loaiHopDongId));
        java.time.Instant ntFrom = fromDateIn != null ? fromDateIn.atStartOfDay(VietnamDateUtils.ZONE).toInstant() : null;
        java.time.Instant ntTo = toDateIn != null ? toDateIn.plusDays(1).atStartOfDay(VietnamDateUtils.ZONE).toInstant() : null;
        var fNtKhongDat = parallel.async(() -> sanLuongToolRepository.findNghiemThuKhongDat(nhaThauId, hopDongId, doiTuongIds, khuVucId,
                resolvedTinhThanhId, loaiHopDongId, ntFrom, ntTo, pageNt));
        var fNtThongKe = parallel.async(() -> sanLuongToolRepository.thongKeNghiemThu(nhaThauId, hopDongId, doiTuongIds, khuVucId,
                resolvedTinhThanhId, loaiHopDongId, ntFrom, ntTo));
        var fNtCho = parallel.async(() -> sanLuongToolRepository.thongKeChoNghiemThu(nhaThauId, hopDongId, doiTuongIds, khuVucId,
                resolvedTinhThanhId, loaiHopDongId, fromDateIn, toDateIn));
        var fChuaBao = parallel.async(() -> sanLuongToolRepository.findNhaThauChuaBaoTrongKy(fromDate, toDate,
                nhaThauId, hopDongId, doiTuongIds, khuVucId, resolvedTinhThanhId, loaiHopDongId, pageNt));
        var fDaBao = parallel.async(() -> sanLuongToolRepository.findNhaThauDaBaoTrongKy(fromDate, toDate,
                nhaThauId, hopDongId, doiTuongIds, khuVucId, resolvedTinhThanhId, loaiHopDongId, pageNt));

        // danh sách từng đối tượng (đọc thêm lũy kế sản lượng): chạy song song với các truy vấn khác
        var fDanhSach = parallel.async(() -> sanLuongToolRepository.danhSachDoiTuong(Boolean.TRUE.equals(includeWithoutOutput) ? Boolean.TRUE : null,
                        chuanHoaSapXep(sapXep), today, fromDate, toDate, nhaThauId, hopDongId, doiTuongIds, khuVucId, resolvedTinhThanhId, loaiHopDongId,
                        pageNt.getPageSize(), pageNt.getOffset()));

        // hạng mục của đối tượng: chỉ khi truyền doiTuong (mã/id); nếu là loại đối tượng thì không khớp đối tượng cụ thể nào nên rỗng
        var fHangMuc = doiTuongIds != null
                ? parallel.async(() -> sanLuongToolRepository.hangMucCuaDoiTuong(doiTuongIds))
                : null;

        var aggregate = McpParallel.get(fAggregate).orElse(null);
        int totalObjects = aggregate != null ? NumberUtil.nz(aggregate.getTotalDisplayed()) : 0;
        int objectsWithOutput = aggregate != null ? NumberUtil.nz(aggregate.getWithOutput()) : 0;
        BigDecimal periodValue = aggregate != null ? NumberUtil.nz(aggregate.getPeriodTotal()) : BigDecimal.ZERO;
        BigDecimal todayValue = aggregate != null ? NumberUtil.nz(aggregate.getTodayTotal()) : BigDecimal.ZERO;

        var bundle = McpParallel.get(fBundle);
        List<ProgressGroupProjection> groups = bundle.stream().filter(r -> "G".equals(r.getKind())).map(r -> (ProgressGroupProjection) r).toList();
        List<DoiTuongProgressProjection> topDoiTuong = bundle.stream().filter(r -> "T".equals(r.getKind())).map(r -> (DoiTuongProgressProjection) r).toList();
        long soDoiTuong = groups.stream().mapToLong(ProgressGroupProjection::getN).sum();
        int issueCount = (int) (long) McpParallel.get(fIssue);

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
                .openIssueCount(issueCount)
                .build();

        ProgressSummaryDto progress = computeProgress(groups,
                nguongHoanThanhThap != null ? nguongHoanThanhThap : DEFAULT_NGUONG_HOAN_THANH_THAP);
        String khoaXepHang = xepHangTheo == null ? "giaTri" : xepHangTheo.trim();
        boolean tang = Boolean.TRUE.equals(tangDan);
        RankingOverviewDto nhaThauRanking = computeRanking(groups,
                ProgressGroupProjection::getNhaThauId,
                ProgressGroupProjection::getNhaThauTen, Integer.MAX_VALUE, khoaXepHang, tang);
        RankingOverviewDto khuVucRanking = computeRanking(groups,
                ProgressGroupProjection::getKhuVucId,
                ProgressGroupProjection::getKhuVucTen, Integer.MAX_VALUE, khoaXepHang, tang);
        RankingOverviewDto tinhRanking = computeRanking(groups,
                g -> g.getTinhId() != null ? g.getTinhId() : new UUID(0L, 0L), // đối tượng chưa gán tỉnh: gom thành 1 dòng "—" để tổng khớp
                ProgressGroupProjection::getTinhTen, Integer.MAX_VALUE, khoaXepHang, tang);
        RankingOverviewDto loaiHopDongRanking = computeRanking(groups,
                ProgressGroupProjection::getLoaiHopDongId,
                ProgressGroupProjection::getLoaiHopDongTen, Integer.MAX_VALUE, khoaXepHang, tang);
        RankingOverviewDto hopDongRanking = computeRanking(groups,
                ProgressGroupProjection::getHopDongId,
                ProgressGroupProjection::getHopDongTen, 20, khoaXepHang, tang);
        RankingOverviewDto doiTuongRanking = computeDoiTuongRanking(topDoiTuong, soDoiTuong);

        BigDecimal previousValue = aggregate != null ? NumberUtil.nz(aggregate.getPreviousTotal()) : BigDecimal.ZERO;
        TrendInfo trend = TrendCalculator.compare(periodValue, previousValue, previousPeriod.label());

        PeriodInfo period = new PeriodInfo(fromDate.toString(), toDate.toString(), fromDate + " → " + toDate);

        PeriodTrendDto periodTrend = computePeriodTrend(fromDate, toDate, groups, buckets, McpParallel.get(fCells));

        var nhaThauChuaBaoRows = McpParallel.get(fChuaBao);
        List<SanLuongNhaThauItem> nhaThauChuaBaoTrongKy = nhaThauChuaBaoRows.stream()
                .map(SanLuongToolHandlerImpl::mapNhaThauChuaBao)
                .toList();
        PagedResult<SanLuongNhaThauItem> nhaThauChuaBaoTrongKyPaged = PagedResult.of(
                nhaThauChuaBaoTrongKy, pageNt.getPageNumber(), pageNt.getPageSize(), tongTheoTrang(nhaThauChuaBaoRows.isEmpty() ? null : nhaThauChuaBaoRows.get(0).getTong(),
                        pageNt.getPageNumber(), () -> sanLuongToolRepository.demNhaThauChuaBaoTrongKy(fromDate, toDate,
                                nhaThauId, hopDongId, doiTuongIds, khuVucId, resolvedTinhThanhId, loaiHopDongId)));

        var nhaThauDaBaoRows = McpParallel.get(fDaBao);
        List<SanLuongNhaThauItem> nhaThauDaBaoTrongKy = nhaThauDaBaoRows.stream()
                .map(SanLuongToolHandlerImpl::mapNhaThauDaBao)
                .toList();
        PagedResult<SanLuongNhaThauItem> nhaThauDaBaoTrongKyPaged = PagedResult.of(
                nhaThauDaBaoTrongKy, pageNt.getPageNumber(), pageNt.getPageSize(), tongTheoTrang(nhaThauDaBaoRows.isEmpty() ? null : nhaThauDaBaoRows.get(0).getTong(),
                        pageNt.getPageNumber(), () -> sanLuongToolRepository.demNhaThauDaBaoTrongKy(fromDate, toDate,
                                nhaThauId, hopDongId, doiTuongIds, khuVucId, resolvedTinhThanhId, loaiHopDongId)));

        var ntKhongDatPage = McpParallel.get(fNtKhongDat);
        long soDat = 0;
        BigDecimal giaTriDat = BigDecimal.ZERO;
        long soKhongDat = 0;
        BigDecimal giaTriKhongDat = BigDecimal.ZERO;
        for (Object[] r : McpParallel.get(fNtThongKe)) {
            if ("dat".equals(r[0])) {
                soDat = ((Number) r[1]).longValue();
                giaTriDat = NumberUtil.toBigDecimal(r[2]);
            } else {
                soKhongDat = ((Number) r[1]).longValue();
                giaTriKhongDat = NumberUtil.toBigDecimal(r[2]);
            }
        }
        Object[] cho = McpParallel.get(fNtCho).get(0);
        SanLuongNghiemThuDto nghiemThu = SanLuongNghiemThuDto.builder()
                .soDat(soDat)
                .giaTriDat(giaTriDat)
                .soKhongDat(soKhongDat)
                .giaTriKhongDat(giaTriKhongDat)
                .soChoNghiemThu(((Number) cho[0]).longValue())
                .giaTriChoNghiemThu(NumberUtil.toBigDecimal(cho[1]))
                .khongDat(PagedResult.of(ntKhongDatPage.getContent(), ntKhongDatPage.getNumber(), ntKhongDatPage.getSize(),
                        ntKhongDatPage.getTotalElements()))
                .build();

        var doiTuongChuaCoRows = bundle.stream().filter(r -> "C".equals(r.getKind())).toList();
        PagedResult<SanLuongDoiTuongChuaCoItem> doiTuongChuaCo = PagedResult.of(doiTuongChuaCoRows.stream()
                .map(r -> SanLuongDoiTuongChuaCoItem.builder()
                        .maDoiTuong(r.getMaDoiTuong())
                        .loaiDoiTuong(r.getDoiTuongTen())
                        .hopDong(r.getHopDongTen())
                        .nhaThau(r.getNhaThauTen())
                        .khuVuc(r.getKhuVucTen())
                        .soHangMuc(r.getTotalHangMuc() != null ? r.getTotalHangMuc() : 0)
                        .coVuongMacMo(Boolean.TRUE.equals(r.getHasOpenVuongMac()))
                        .build()).toList(),
                pageNt.getPageNumber(), pageNt.getPageSize(), tongTheoTrang(doiTuongChuaCoRows.isEmpty() ? null : doiTuongChuaCoRows.get(0).getTong(),
                        pageNt.getPageNumber(), () -> sanLuongToolRepository.demDoiTuongChuaCoSanLuong(fromDate, toDate, nhaThauId,
                                hopDongId, doiTuongIds, khuVucId, resolvedTinhThanhId, loaiHopDongId)));

        List<SanLuongHangMucDoiTuongDto> hangMucDt = fHangMuc != null ? gomHangMuc(McpParallel.get(fHangMuc)) : null;
        boolean doiTuongCuThe = hangMucDt != null && !hangMucDt.isEmpty();
        return SanLuongQueryResponse.builder()
                .period(period)
                .summary(summary)
                .progress(progress)
                .trend(trend)
                .nhaThau(doiTuongCuThe || nhaThauId != null ? null : nhaThauRanking)
                .khuVuc(doiTuongCuThe || khuVucId != null ? null : khuVucRanking)
                .tinh(doiTuongCuThe || resolvedTinhThanhId != null ? null : tinhRanking)
                .loaiHopDong(doiTuongCuThe || loaiHopDongId != null ? null : loaiHopDongRanking)
                .hopDong(doiTuongCuThe || hopDongId != null ? null : hopDongRanking)
                .doiTuong(doiTuongCuThe ? null : doiTuongRanking)
                .periodTrend(periodTrend)
                .nhaThauChuaBaoTrongKy(nhaThauChuaBaoTrongKyPaged)
                .nhaThauDaBaoTrongKy(nhaThauDaBaoTrongKyPaged)
                .nghiemThu(nghiemThu)
                .doiTuongChuaCoSanLuong(doiTuongChuaCo)
                .danhSachDoiTuong(danhSachTrang(McpParallel.get(fDanhSach), pageNt.getPageNumber(), pageNt.getPageSize(), () -> tongDanhSach(
                        includeWithoutOutput, sapXep, today, fromDate, toDate, nhaThauId, hopDongId, doiTuongIds, khuVucId, resolvedTinhThanhId, loaiHopDongId)))
                .hangMucDoiTuong(hangMucDt)
                .build();
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
        long completed = 0;
        long inProgress = 0;
        long notStarted = 0;
        long lowCompletion = 0;
        BigDecimal rateSum = BigDecimal.ZERO;
        long counted = 0;

        for (var g : groups) {
            int total = NumberUtil.nz(g.getTotalHangMuc());
            if (total <= 0) {
                continue;
            }
            long n = g.getN();
            int done = NumberUtil.nz(g.getDoneHangMuc());
            BigDecimal rate = NumberUtil.percent(done, total, 4);
            rateSum = rateSum.add(rate.multiply(BigDecimal.valueOf(n)));
            counted += n;

            if (done >= total) {
                completed += n;
            } else if (done == 0) {
                notStarted += n;
            } else {
                inProgress += n;
                if (rate.compareTo(BigDecimal.valueOf(nguongHoanThanhThap)) < 0) {
                    lowCompletion += n;
                }
            }
        }

        BigDecimal completionRate = counted > 0
                ? rateSum.divide(BigDecimal.valueOf(counted), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ProgressSummaryDto.builder()
                .completionRate(completionRate)
                .completedCount((int) completed)
                .inProgressCount((int) inProgress)
                .notStartedCount((int) notStarted)
                .lowCompletionCount((int) lowCompletion)
                .build();
    }

    /** Xếp hạng theo khoá (nhà thầu/khu vực/hợp đồng) từ các nhóm đối tượng; mỗi nhóm tính {@code n} lần. */
    private RankingOverviewDto computeRanking(
            List<ProgressGroupProjection> groups,
            java.util.function.Function<ProgressGroupProjection, UUID> keyFn,
            java.util.function.Function<ProgressGroupProjection, String> nameFn,
            int topLimit, String khoaXepHang, boolean tang) {
        record Acc(String name, BigDecimal[] output, long[] objectCount, long[] issueCount, BigDecimal[] rateSum,
                   long[] rateCounted) {
        }
        Map<UUID, Acc> byKey = new LinkedHashMap<>();
        for (var g : groups) {
            UUID key = keyFn.apply(g);
            if (key == null) {
                continue;
            }
            long n = g.getN();
            BigDecimal weight = BigDecimal.valueOf(n);
            Acc acc = byKey.computeIfAbsent(key, id -> new Acc(
                    nameFn.apply(g), new BigDecimal[]{BigDecimal.ZERO}, new long[]{0}, new long[]{0}, new BigDecimal[]{BigDecimal.ZERO}, new long[]{0}));
            acc.output()[0] = acc.output()[0].add(NumberUtil.nz(g.getPeriodValue()).multiply(weight));
            acc.objectCount()[0] += n;
            if (NumberUtil.isTrue(g.getHasOpenVuongMac())) {
                acc.issueCount()[0] += n;
            }
            int total = NumberUtil.nz(g.getTotalHangMuc());
            if (total > 0) {
                int done = NumberUtil.nz(g.getDoneHangMuc());
                acc.rateSum()[0] = acc.rateSum()[0].add(NumberUtil.percent(done, total, 4).multiply(weight));
                acc.rateCounted()[0] += n;
            }
        }

        List<RankedEntityDto> unranked = byKey.entrySet().stream()
                .map(entry -> {
                    Acc acc = entry.getValue();
                    BigDecimal completionRate = acc.rateCounted()[0] > 0
                            ? acc.rateSum()[0].divide(BigDecimal.valueOf(acc.rateCounted()[0]), 1, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return new RankedEntityDto(entry.getKey().toString(), acc.name(), acc.output()[0], completionRate,
                            (int) acc.objectCount()[0], (int) acc.issueCount()[0], null);
                })
                .toList();

        return RankingBuilder.build(unranked, topLimit, khoaXepHang, tang);
    }

    /** Xếp hạng đối tượng: chỉ cần 20 đối tượng đầu (đã sắp ở DB); {@code totalEntities} là tổng số đối tượng. */
    private RankingOverviewDto computeDoiTuongRanking(List<DoiTuongProgressProjection> top, long totalEntities) {
        List<RankedEntityDto> unranked = top.stream()
                .map(row -> {
                    int total = NumberUtil.nz(row.getTotalHangMuc());
                    BigDecimal completionRate = BigDecimal.ZERO;
                    if (total > 0) {
                        completionRate = NumberUtil.percent(NumberUtil.nz(row.getDoneHangMuc()), total, 4)
                                .divide(BigDecimal.ONE, 1, RoundingMode.HALF_UP);
                    }
                    return new RankedEntityDto(row.getDoiTuongId().toString(), row.getDoiTuongTen(),
                            NumberUtil.nz(row.getPeriodValue()), completionRate, 1,
                            NumberUtil.isTrue(row.getHasOpenVuongMac()) ? 1 : 0, null);
                })
                .toList();
        return RankingBuilder.build(unranked, 20, totalEntities);
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
