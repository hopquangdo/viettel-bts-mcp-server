package vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.impl;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituong.dto.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.dto.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.TramTonToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.NumberUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Business orchestration and computation for the DoiTuongTon tools. */
@Service
@RequiredArgsConstructor
public class DoiTuongTonServiceImpl implements DoiTuongTonService {

    private static final int DEFAULT_QUA_HAN_NGAY = 90;
    private static final String[] AGING_IDS = {"lt_1w", "1_2w", "2_4w", "1_3m", "gt_3m"};
    private static final String[] AGING_LABELS = {"< 1 Tuần", "1 - 2 Tuần", "2 - 4 Tuần", "1 - 3 Tháng", "> 3 Tháng"};

    private final TramTonToolRepository tramTonToolRepository;
    private final DoiTuongComponent doiTuongComponent;
    private final HopDongComponent hopDongComponent;
    private final KhuVucComponent khuVucComponent;
    private final NhaThauComponent nhaThauComponent;
    private final TinhComponent tinhComponent;
    private final DanhMucComponent danhMucComponent;
    private final McpParallel parallel;

    private record Scope(UUID nhaThauId, UUID hopDongId, UUID doiTuongId, boolean doiTuongCuThe, UUID tinhThanhId,
                         UUID khuVucId, UUID loaiId) {
        boolean coPhamVi() {
            return nhaThauId != null || hopDongId != null || doiTuongId != null || khuVucId != null || tinhThanhId != null || loaiId != null;
        }
    }

    private record Khoang(LocalDate tuNgay, LocalDate denNgay) {
        static Khoang of(LocalDate fromDate, LocalDate toDate) {
            boolean nguoc = fromDate != null && toDate != null && fromDate.isAfter(toDate);
            return new Khoang(nguoc ? toDate : fromDate, nguoc ? fromDate : toDate);
        }

        boolean coLoc() {
            return tuNgay != null || denNgay != null;
        }
    }

    public TramTonTongQuanToolResponse tongQuan(TramTonFilter filter, Integer quaHanNgay, LocalDate fromDate, LocalDate toDate) {
        return TramTonTongQuanToolResponse.of(
                McpParallel.get(tongQuanAsync(resolve(filter), quaHanNgayOf(quaHanNgay), Khoang.of(fromDate, toDate))));
    }

    public TramTonDanhSachResponse danhSach(TramTonFilter filter, String tab, Integer quaHanNgay, LocalDate fromDate, LocalDate toDate,
                                            Integer page, Integer pageSize) {
        String tabLoc = tabOf(tab);
        return new TramTonDanhSachResponse(McpParallel.get(
                danhSachAsync(resolve(filter), tabLoc, quaHanNgayOf(quaHanNgay), Khoang.of(fromDate, toDate), page, pageSize)));
    }

    public TramTonThieuCapNhatResponse thieuCapNhat(TramTonFilter filter, Integer soNgayThieuCapNhat, LocalDate sinceDate) {
        int soNgay = soNgayThieuCapNhatOf(soNgayThieuCapNhat, sinceDate);
        return new TramTonThieuCapNhatResponse(soNgay, McpParallel.get(thieuCapNhatAsync(resolve(filter), soNgay)));
    }

    public TramTonSanLuongBatThuongResponse sanLuongBatThuong(TramTonFilter filter) {
        return new TramTonSanLuongBatThuongResponse(McpParallel.get(sanLuongBatThuongAsync(resolve(filter))));
    }

    public TramTonXepHangKhuVucResponse xepHangKhuVuc(TramTonFilter filter, Integer top) {
        return new TramTonXepHangKhuVucResponse(McpParallel.get(xepHangKhuVucAsync(resolve(filter), top)));
    }

    public TramTonThieuDieuKienResponse thieuDieuKien(TramTonFilter filter) {
        return new TramTonThieuDieuKienResponse(McpParallel.get(thieuNhieuDieuKienAsync(resolve(filter))));
    }

    /** Combined legacy query used by callers outside this tool module. */
    public TramTonQueryResponse query(String doiTuong, String hopDong, String nhaThau,
                                      String khuVuc, String tinhThanh, String loaiHopDong, String tab, LocalDate sinceDate, Integer quaHanNgay, Integer topIn,
                                      Integer page, Integer pageSize, Integer soNgayThieuCapNhat,
                                      LocalDate fromDate, LocalDate toDate) {
        String tabLoc = tabOf(tab);
        Scope s = resolve(new TramTonFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong));
        int resolvedQuaHanNgay = quaHanNgayOf(quaHanNgay);
        Khoang k = Khoang.of(fromDate, toDate);

        var fTongQuan = tongQuanAsync(s, resolvedQuaHanNgay, k);
        var fDanhSach = s.coPhamVi() || tabLoc != null || k.coLoc()
                ? danhSachAsync(s, tabLoc, resolvedQuaHanNgay, k, page, pageSize)
                : CompletableFuture.<PagedResult<TramTonToolItemDto>>completedFuture(null);
        var fThieuCapNhat = thieuCapNhatAsync(s, soNgayThieuCapNhatOf(soNgayThieuCapNhat, sinceDate));
        var fBatThuong = sanLuongBatThuongAsync(s);
        var fXepHang = xepHangKhuVucAsync(s, topIn);
        var fThieuNhieu = thieuNhieuDieuKienAsync(s);

        return TramTonQueryResponse.builder()
                .tongQuan(McpParallel.get(fTongQuan))
                .danhSach(McpParallel.get(fDanhSach))
                .thieuCapNhat(McpParallel.get(fThieuCapNhat))
                .sanLuongBatThuong(McpParallel.get(fBatThuong))
                .xepHangKhuVuc(McpParallel.get(fXepHang))
                .thieuNhieuDieuKien(McpParallel.get(fThieuNhieu))
                .build();
    }

    private Scope resolve(TramTonFilter f) {
        var fNhaThau = parallel.async(() -> nhaThauComponent.resolve(f.nhaThau()));
        var fHopDong = parallel.async(() -> hopDongComponent.resolve(f.hopDong()));
        var fDoiTuong = parallel.async(() -> doiTuongComponent.resolve(f.doiTuong()));
        var fTinhThanhId = parallel.async(() -> tinhComponent.resolveId(f.tinhThanh()));
        var fLoaiId = parallel.async(() -> danhMucComponent.resolveLoaiHopDong(f.loaiHopDong()));
        UUID khuVucId = khuVucComponent.resolveInScope(f.khuVuc()).id();

        DoiTuongInfo doiTuong = McpParallel.get(fDoiTuong);
        return new Scope(
                McpParallel.get(fNhaThau).id(),
                McpParallel.get(fHopDong).id(),
                doiTuong.id(),
                doiTuongComponent.laCuThe(doiTuong),
                McpParallel.get(fTinhThanhId),
                khuVucId,
                McpParallel.get(fLoaiId));
    }

    private static String tabOf(String tab) {
        return ResolveSupport.enumValue("tab", tab, "cho_quyet_toan", "qua_han", "chua_phap_ly", "vuong_mac");
    }

    private static int quaHanNgayOf(Integer quaHanNgay) {
        return quaHanNgay != null && quaHanNgay >= 0 ? Math.min(quaHanNgay, 3650) : DEFAULT_QUA_HAN_NGAY;
    }

    private static int soNgayThieuCapNhatOf(Integer soNgayThieuCapNhat, LocalDate sinceDate) {
        return soNgayThieuCapNhat != null && soNgayThieuCapNhat >= 0 ? soNgayThieuCapNhat
                : sinceDate != null ? (int) ChronoUnit.DAYS.between(sinceDate, LocalDate.now()) : 7;
    }

    private CompletableFuture<TramTonToolTongQuanDto> tongQuanAsync(Scope s, int quaHanNgay, Khoang k) {
        var fChoQt = parallel.async(() -> tramTonToolRepository.thongKeChoQuyetToanVaQuaHan(quaHanNgay, s.khuVucId(), s.tinhThanhId(),
                s.nhaThauId(), s.hopDongId(), s.doiTuongId(), s.loaiId(), k.tuNgay(), k.denNgay()));
        var fChuaPhapLy = parallel.async(() -> tramTonToolRepository.thongKeChuaPhapLy(s.khuVucId(), s.tinhThanhId(), s.nhaThauId(),
                s.hopDongId(), s.doiTuongId(), s.loaiId()));
        var fVuongMac = parallel.async(() -> tramTonToolRepository.thongKeVuongMacQuaNguong(quaHanNgay, s.khuVucId(), s.tinhThanhId(),
                s.nhaThauId(), s.hopDongId(), s.doiTuongId(), s.loaiId()));
        return CompletableFuture.allOf(fChoQt, fChuaPhapLy, fVuongMac)
                .thenApply(v -> computeTongQuan(fChoQt.join(), fChuaPhapLy.join(), fVuongMac.join(), quaHanNgay));
    }

    private CompletableFuture<PagedResult<TramTonToolItemDto>> danhSachAsync(Scope s, String tabLoc, int quaHanNgay, Khoang k,
                                                                             Integer page, Integer pageSize) {
        return parallel.async(() -> computeDanhSach(s.nhaThauId(), s.hopDongId(), s.khuVucId(), s.tinhThanhId(), s.doiTuongId(),
                s.loaiId(), tabLoc, quaHanNgay, k.tuNgay(), k.denNgay(), page, pageSize));
    }

    private CompletableFuture<List<TramTonToolThieuCapNhatItemDto>> thieuCapNhatAsync(Scope s, int soNgay) {
        return parallel.async(() -> computeThieuCapNhat(soNgay, s.nhaThauId(), s.hopDongId(), s.khuVucId(), s.tinhThanhId(),
                s.doiTuongId(), s.loaiId()));
    }

    private CompletableFuture<List<TramTonToolSanLuongBatThuongItemDto>> sanLuongBatThuongAsync(Scope s) {
        return parallel.async(() -> computeSanLuongBatThuong(s.nhaThauId(), s.hopDongId(), s.khuVucId(), s.tinhThanhId(),
                s.doiTuongId(), s.loaiId()));
    }

    private CompletableFuture<List<TramTonXepHangKhuVucToolItem>> xepHangKhuVucAsync(Scope s, Integer topIn) {
        if (s.khuVucId() != null || s.doiTuongCuThe()) {
            return CompletableFuture.completedFuture(null);
        }
        int top = topIn != null ? Math.min(Math.max(topIn, 1), 20) : 5;
        return parallel.async(() -> computeXepHangKhuVuc(top, s.nhaThauId(), s.hopDongId(), s.doiTuongId(), s.tinhThanhId(), s.loaiId()));
    }

    private CompletableFuture<List<TramTonThieuNhieuDieuKienToolItem>> thieuNhieuDieuKienAsync(Scope s) {
        return parallel.async(() -> computeThieuNhieuDieuKien(s.nhaThauId(), s.hopDongId(), s.khuVucId(), s.tinhThanhId(),
                s.doiTuongId(), s.loaiId()));
    }

    private List<TramTonXepHangKhuVucToolItem> computeXepHangKhuVuc(int top, UUID nhaThauId, UUID hopDongId,
                                                                    UUID doiTuongId, UUID tinhThanhId, UUID loaiHopDongId) {
        return tramTonToolRepository.rankKhuVuc(PageRequest.of(0, top), nhaThauId, hopDongId, doiTuongId, tinhThanhId, loaiHopDongId).stream()
                .map(DoiTuongTonServiceImpl::mapXepHangKhuVuc)
                .toList();
    }

    private static TramTonXepHangKhuVucToolItem mapXepHangKhuVuc(XepHangKhuVucRow row) {
        TramTonXepHangKhuVucToolItem item = new TramTonXepHangKhuVucToolItem();
        item.setKhuVuc(row.getKhuVuc());
        item.setSoDoiTuong(row.getSoDoiTuong());
        item.setGiaTriTon(row.getGiaTriTon());
        return item;
    }

    private List<TramTonThieuNhieuDieuKienToolItem> computeThieuNhieuDieuKien(UUID nhaThauId, UUID hopDongId, UUID khuVucId,
                                                                            UUID tinhThanhId, UUID doiTuongId, UUID loaiHopDongId) {
        return tramTonToolRepository.findThieuNhieuDieuKien(nhaThauId, hopDongId, khuVucId, tinhThanhId, doiTuongId, loaiHopDongId).stream()
                .map(DoiTuongTonServiceImpl::mapThieuNhieuDieuKien)
                .toList();
    }

    private static TramTonThieuNhieuDieuKienToolItem mapThieuNhieuDieuKien(ThieuNhieuDieuKienRow row) {
        List<String> thieu = new ArrayList<>();
        if (row.isThieuPhapLy()) {
            thieu.add("Chưa đủ điều kiện pháp lý hợp đồng");
        }
        if (row.isDangVuongMac()) {
            thieu.add("Đang có vướng mắc mở");
        }
        if (row.isChuaCoSanLuong()) {
            thieu.add("Chưa có sản lượng hiệu lực");
        }
        TramTonThieuNhieuDieuKienToolItem item = new TramTonThieuNhieuDieuKienToolItem();
        item.setMaDoiTuong(row.getMaDoiTuong());
        item.setMaHopDong(row.getMaHopDong());
        item.setKhuVuc(row.getKhuVuc());
        item.setNhaThau(row.getNhaThau());
        item.setSoDieuKienThieu(thieu.size());
        item.setDieuKienThieu(thieu);
        return item;
    }

    private static int agingIndex(long soNgay) {
        if (soNgay < 7) {
            return 0;
        }
        if (soNgay < 14) {
            return 1;
        }
        if (soNgay < 28) {
            return 2;
        }
        return soNgay < 90 ? 3 : 4;
    }

    private static int agingIndex(String id) {
        for (int i = 0; i < AGING_IDS.length; i++) {
            if (AGING_IDS[i].equals(id)) {
                return i;
            }
        }
        return 0;
    }

    private TramTonToolTongQuanDto computeTongQuan(List<Object[]> choQtRows, List<Object[]> chuaPhapLyRows, List<Object[]> vuongMacRows, int quaHanNgay) {
        long choQuyetToan = 0;
        long quaHan = 0;
        BigDecimal giaTriTon = BigDecimal.ZERO;
        long[] agingCount = new long[AGING_IDS.length];
        BigDecimal[] agingValue = new BigDecimal[AGING_IDS.length];
        java.util.Arrays.fill(agingValue, BigDecimal.ZERO);

        for (Object[] row : choQtRows) {
            long count = ((Number) row[2]).longValue();
            BigDecimal value = NumberUtil.toBigDecimal(row[3]);
            if ("qua_han".equals(row[0])) {
                quaHan += count;
            } else {
                choQuyetToan += count;
            }
            giaTriTon = giaTriTon.add(value);
            int idx = agingIndex((String) row[1]);
            agingCount[idx] += count;
            agingValue[idx] = agingValue[idx].add(NumberUtil.toBigDecimal(row[4]));
        }

        long chuaPhapLy = 0;
        BigDecimal giaTriChuaPhapLy = BigDecimal.ZERO;
        BigDecimal giaTriChuaPhapLyNguyen = BigDecimal.ZERO;
        for (Object[] row : chuaPhapLyRows) {
            chuaPhapLy += ((Number) row[0]).longValue();
            giaTriChuaPhapLy = giaTriChuaPhapLy.add(NumberUtil.toBigDecimal(row[1]));
            giaTriChuaPhapLyNguyen = giaTriChuaPhapLyNguyen.add(NumberUtil.toBigDecimal(row[2]));
        }
        giaTriTon = giaTriTon.add(giaTriChuaPhapLy);

        long vuongMac = 0;
        BigDecimal giaTriVuongMac = BigDecimal.ZERO;
        BigDecimal giaTriVuongMacNguyen = BigDecimal.ZERO;
        for (Object[] row : vuongMacRows) {
            BigDecimal value = NumberUtil.toBigDecimal(row[1]);
            vuongMac++;
            BigDecimal valueNguyen = NumberUtil.toBigDecimal(row[2]);
            giaTriVuongMac = giaTriVuongMac.add(value);
            giaTriVuongMacNguyen = giaTriVuongMacNguyen.add(valueNguyen);
            giaTriTon = giaTriTon.add(value);
            int idx = agingIndex(((Number) row[0]).longValue());
            agingCount[idx]++;
            agingValue[idx] = agingValue[idx].add(valueNguyen);
        }

        long duDieuKienDt = choQuyetToan + quaHan;
        long chuaDuDieuKien = chuaPhapLy + vuongMac;
        long tongSoTram = duDieuKienDt + chuaDuDieuKien;

        List<TramTonToolBucketDto> aging = new ArrayList<>();
        for (int i = 0; i < AGING_IDS.length; i++) {
            aging.add(bucket(AGING_LABELS[i], agingCount[i], agingValue[i], duDieuKienDt));
        }
        List<TramTonToolBucketDto> reasons = List.of(
                bucket("Chưa HT thi công", 0, BigDecimal.ZERO, chuaDuDieuKien),
                bucket("Đang vướng mắc", vuongMac, giaTriVuongMacNguyen, chuaDuDieuKien),
                bucket("Chưa có pháp lý", chuaPhapLy, giaTriChuaPhapLyNguyen, chuaDuDieuKien),
                bucket("Thiếu 2+ điều kiện", 0, BigDecimal.ZERO, chuaDuDieuKien));

        return TramTonToolTongQuanDto.builder()
                .tongSoTram(tongSoTram)
                .duDieuKienDt(duDieuKienDt)
                .choQuyetToan(choQuyetToan)
                .chuaDuDieuKien(chuaDuDieuKien)
                .quaHan(quaHan)
                .quaHanNgay(quaHanNgay)
                .giaTriTon(giaTriTon)
                .aging(aging)
                .reasons(reasons)
                .build();
    }

    private static TramTonToolBucketDto bucket(String label, long count, BigDecimal value, long tong) {
        double percent = tong > 0 ? Math.round(count * 10000.0 / tong) / 100.0 : 0.0;
        return TramTonToolBucketDto.builder().label(label).count(count).value(value).percent(percent).build();
    }

    private TramTonToolBucketDto bucketReason(String label, long count, BigDecimal value, long tong) {
        double percent = tong > 0 ? count * 100.0 / tong : 0.0;
        return TramTonToolBucketDto.builder().label(label).count(count).value(value).percent(percent).build();
    }

    private PagedResult<TramTonToolItemDto> computeDanhSach(UUID nhaThauId, UUID hopDongId, UUID khuVucId, UUID tinhThanhId, UUID doiTuongId,
                                                            UUID loaiId, String tab, int quaHanNgay, LocalDate fromDate, LocalDate toDate,
                                                            Integer page, Integer pageSize) {
        var pageable = PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE);
        Page<Object[]> result;
        String trangThai = "cho_quyet_toan";
        String lyDo = "Chờ quyết toán";
        if ("chua_phap_ly".equals(tab)) {
            result = tramTonToolRepository.searchChuaPhapLy(nhaThauId, hopDongId, khuVucId, tinhThanhId, doiTuongId, loaiId, pageable);
            trangThai = "chua_phap_ly";
            lyDo = "Hợp đồng chưa đủ pháp lý";
        } else if ("vuong_mac".equals(tab)) {
            result = tramTonToolRepository.searchVuongMac(quaHanNgay, nhaThauId, hopDongId, khuVucId, tinhThanhId, doiTuongId, loaiId, pageable);
            trangThai = "vuong_mac";
            lyDo = "Vướng mắc đang mở quá " + quaHanNgay + " ngày";
        } else {
            String tabQt = "qua_han".equals(tab) || "cho_quyet_toan".equals(tab) ? tab : null;
            result = tramTonToolRepository.search(nhaThauId, hopDongId, khuVucId, tinhThanhId, doiTuongId, loaiId, tabQt, quaHanNgay, fromDate, toDate, pageable);
            if ("qua_han".equals(tab)) {
                trangThai = "qua_han";
                lyDo = "Quá hạn quyết toán";
            }
        }
        String tt = trangThai;
        String ld = lyDo;
        return PagedResult.of(result.getContent().stream().map(r -> mapDanhSachItem(r, tt, ld)).toList(),
            result.getNumber(), result.getSize(), result.getTotalElements());
    }

    private static TramTonToolItemDto mapDanhSachItem(Object[] row, String trangThai, String lyDoTon) {
        return TramTonToolItemDto.builder()
                .id((UUID) row[0])
                .hopDong(HopDongInfo.of((UUID) row[1], (String) row[2], (String) row[3]))
                .maTram((String) row[4])
                .khuVuc((String) row[5])
                .nhaThau((String) row[6])
                .giaTri(NumberUtil.toBigDecimal(row[7]))
                .soNgayTon(soNgay((LocalDate) row[8]))
                .trangThai(StatusLabels.tramTonTrangThai(trangThai))
                .lyDoTon(lyDoTon)
                .build();
    }

    private List<TramTonToolThieuCapNhatItemDto> computeThieuCapNhat(int soNgay, UUID nhaThauId, UUID hopDongId, UUID khuVucId,
                                                                      UUID tinhThanhId, UUID doiTuongId, UUID loaiHopDongId) {
        LocalDate nguongNgay = LocalDate.now().minusDays(soNgay);
        return tramTonToolRepository.findThieuCapNhat(nguongNgay, nhaThauId, hopDongId, khuVucId, tinhThanhId, doiTuongId, loaiHopDongId).stream()
                .map(DoiTuongTonServiceImpl::mapThieuCapNhatItem)
                .toList();
    }

    private static TramTonToolThieuCapNhatItemDto mapThieuCapNhatItem(Object[] row) {
        LocalDate ngayThiCongGanNhat = (LocalDate) row[7];
        return TramTonToolThieuCapNhatItemDto.builder()
                .id((UUID) row[0])
                .hopDong(HopDongInfo.of((UUID) row[1], (String) row[2], (String) row[3]))
                .maTram((String) row[4])
                .khuVuc((String) row[5])
                .nhaThau((String) row[6])
                .ngayThiCongGanNhat(ngayThiCongGanNhat)
                .soNgayKhongCapNhat(ngayThiCongGanNhat != null ? ChronoUnit.DAYS.between(ngayThiCongGanNhat, LocalDate.now()) : 0)
                .build();
    }

    private List<TramTonToolSanLuongBatThuongItemDto> computeSanLuongBatThuong(UUID nhaThauId, UUID hopDongId, UUID khuVucId,
                                                                              UUID tinhThanhId, UUID doiTuongId, UUID loaiHopDongId) {
        return tramTonToolRepository.findSanLuongBatThuong(nhaThauId, hopDongId, khuVucId, tinhThanhId, doiTuongId, loaiHopDongId).stream()
                .map(DoiTuongTonServiceImpl::mapSanLuongBatThuongItem)
                .toList();
    }

    private static TramTonToolSanLuongBatThuongItemDto mapSanLuongBatThuongItem(Object[] row) {
        return TramTonToolSanLuongBatThuongItemDto.builder()
                .id((UUID) row[0])
                .hopDong(HopDongInfo.of((UUID) row[1], (String) row[2], (String) row[3]))
                .giaTriHd(row[4] != null ? ((Number) row[4]).longValue() : null)
                .ngayHoanThanh((LocalDate) row[5])
                .sanLuongHieuLuc(NumberUtil.toBigDecimal(row[6]))
                .maTram((String) row[7])
                .khuVuc((String) row[8])
                .nhaThau((String) row[9])
                .build();
    }

    private static long soNgay(LocalDate resolvedNgayHt) {
        return resolvedNgayHt != null ? ChronoUnit.DAYS.between(resolvedNgayHt, LocalDate.now()) : 0;
    }
}