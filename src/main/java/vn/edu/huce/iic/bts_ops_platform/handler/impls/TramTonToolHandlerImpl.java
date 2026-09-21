package vn.edu.huce.iic.bts_ops_platform.handler.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.support.NumberUtil;
import vn.edu.huce.iic.bts_ops_platform.dto.tramton.TramTonQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.doituong.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.dto.khuvuc.KhuVucInfo;
import vn.edu.huce.iic.bts_ops_platform.dto.nhathau.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.dto.tramton.TramTonToolBucketDto;
import vn.edu.huce.iic.bts_ops_platform.dto.tramton.TramTonToolItemDto;
import vn.edu.huce.iic.bts_ops_platform.dto.tramton.TramTonToolSanLuongBatThuongItemDto;
import vn.edu.huce.iic.bts_ops_platform.dto.tramton.TramTonToolThieuCapNhatItemDto;
import vn.edu.huce.iic.bts_ops_platform.dto.tramton.TramTonToolTongQuanDto;
import vn.edu.huce.iic.bts_ops_platform.dto.tramton.TramTonThieuNhieuDieuKienToolItem;
import vn.edu.huce.iic.bts_ops_platform.dto.tramton.TramTonXepHangKhuVucToolItem;
import vn.edu.huce.iic.bts_ops_platform.dto.tramton.ThieuNhieuDieuKienRow;
import vn.edu.huce.iic.bts_ops_platform.dto.tramton.XepHangKhuVucRow;
import vn.edu.huce.iic.bts_ops_platform.handler.TramTonToolHandler;
import vn.edu.huce.iic.bts_ops_platform.repository.TramTonToolRepository;
import vn.edu.huce.iic.bts_ops_platform.components.DoiTuongComponent;
import vn.edu.huce.iic.bts_ops_platform.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.components.KhuVucComponent;
import vn.edu.huce.iic.bts_ops_platform.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.components.TinhComponent;
import vn.edu.huce.iic.bts_ops_platform.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.support.StatusLabels;
import vn.edu.huce.iic.bts_ops_platform.dto.common.PagedResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Impl thật cho tool AI module Trạm tồn — 1 method duy nhất, chỉ gọi TramTonToolRepository. */
@Component
@RequiredArgsConstructor
public class TramTonToolHandlerImpl implements TramTonToolHandler {

    private final TramTonToolRepository tramTonToolRepository;
    private final DoiTuongComponent doiTuongComponent;
    private final HopDongComponent hopDongComponent;
    private final KhuVucComponent khuVucComponent;
    private final NhaThauComponent nhaThauComponent;
    private final TinhComponent tinhComponent;
    private final vn.edu.huce.iic.bts_ops_platform.components.DanhMucComponent danhMucComponent;
    private final McpParallel parallel;

    @Override
    public TramTonQueryResponse query(String doiTuong, String hopDong, String nhaThau,
                                      String khuVuc, String tinhThanh, String loaiHopDong, String tab, LocalDate sinceDate, Integer quaHanNgay, Integer topIn,
                                      Integer page, Integer pageSize, Integer soNgayThieuCapNhat) {
        var f_resolvedNhaThau = parallel.async(() -> nhaThauComponent.resolve(nhaThau));
        var f_resolvedHopDong = parallel.async(() -> hopDongComponent.resolve(hopDong));
        var f_resolvedDoiTuong = parallel.async(() -> doiTuongComponent.resolve(doiTuong));
        var f_tinhThanhId = parallel.async(() -> tinhComponent.resolveId(tinhThanh));
        var f_loaiId = parallel.async(() -> danhMucComponent.resolveLoaiHopDong(loaiHopDong));
        String tabLoc = vn.edu.huce.iic.bts_ops_platform.support.ResolveSupport.enumValue("tab", tab,
                "cho_quyet_toan", "qua_han", "chua_phap_ly", "vuong_mac");
        KhuVucInfo resolvedKhuVuc = khuVucComponent.resolveInScope(khuVuc);
        // các tra cứu id chạy song song (mỗi tra cứu 1-2 lần khứ hồi tới DB nếu chưa có trong bộ nhớ đệm)
        NhaThauInfo resolvedNhaThau = McpParallel.get(f_resolvedNhaThau);
        HopDongInfo resolvedHopDong = McpParallel.get(f_resolvedHopDong);
        DoiTuongInfo resolvedDoiTuong = McpParallel.get(f_resolvedDoiTuong);
        UUID tinhThanhId = McpParallel.get(f_tinhThanhId);
        UUID loaiId = McpParallel.get(f_loaiId);

        int resolvedQuaHanNgay = quaHanNgay != null && quaHanNgay >= 0 ? Math.min(quaHanNgay, 3650) : DEFAULT_QUA_HAN_NGAY;
        UUID khuVucId = resolvedKhuVuc.id();
        UUID nhaThauId = resolvedNhaThau.id();
        UUID hopDongId = resolvedHopDong.id();
        UUID doiTuongId = resolvedDoiTuong.id();
        // Ngưỡng thiếu cập nhật (ngày): ưu tiên soNgayThieuCapNhat, sau đó sinceDate, mặc định 7 ngày.
        int soNgay = soNgayThieuCapNhat != null && soNgayThieuCapNhat >= 0 ? soNgayThieuCapNhat
                : sinceDate != null ? (int) ChronoUnit.DAYS.between(sinceDate, LocalDate.now()) : 7;
        int top = topIn != null ? Math.min(Math.max(topIn, 1), 20) : 5;

        // Các khối độc lập chạy song song: độ trễ = khối chậm nhất thay vì tổng. Tổng quan phát riêng từng câu.
        var fChoQt = parallel.async(() -> tramTonToolRepository.thongKeChoQuyetToanVaQuaHan(resolvedQuaHanNgay, khuVucId, tinhThanhId, nhaThauId, hopDongId, doiTuongId, loaiId));
        var fChuaPhapLy = parallel.async(() -> tramTonToolRepository.thongKeChuaPhapLy(khuVucId, tinhThanhId, nhaThauId, hopDongId, doiTuongId, loaiId));
        var fVuongMac = parallel.async(() -> tramTonToolRepository.thongKeVuongMacQuaNguong(resolvedQuaHanNgay, khuVucId, tinhThanhId, nhaThauId, hopDongId, doiTuongId, loaiId));
        var fDanhSach = (nhaThauId != null || hopDongId != null || doiTuongId != null || khuVucId != null || tinhThanhId != null || loaiId != null || tabLoc != null)
                ? parallel.async(() -> computeDanhSach(nhaThauId, hopDongId, khuVucId, tinhThanhId, doiTuongId, loaiId, tabLoc, resolvedQuaHanNgay, page, pageSize)) : null;
        var fThieuCapNhat = parallel.async(() -> computeThieuCapNhat(soNgay, nhaThauId, hopDongId, khuVucId, tinhThanhId, doiTuongId, loaiId));
        var fBatThuong = parallel.async(() -> computeSanLuongBatThuong(nhaThauId, hopDongId, khuVucId, tinhThanhId, doiTuongId, loaiId));
        boolean rankDisabled = khuVucId != null || doiTuongComponent.laCuThe(resolvedDoiTuong);
        var fXepHang = (!rankDisabled) ? parallel.async(() -> computeXepHangKhuVuc(top, nhaThauId, hopDongId, doiTuongId, tinhThanhId, loaiId)) : null;
        var fThieuNhieu = parallel.async(() -> computeThieuNhieuDieuKien(nhaThauId, hopDongId, khuVucId, tinhThanhId, doiTuongId, loaiId));

        TramTonToolTongQuanDto tongQuan = computeTongQuan(McpParallel.get(fChoQt), McpParallel.get(fChuaPhapLy), McpParallel.get(fVuongMac), resolvedQuaHanNgay);
        PagedResult<TramTonToolItemDto> danhSach = fDanhSach != null ? McpParallel.get(fDanhSach) : null;
        List<TramTonToolThieuCapNhatItemDto> thieuCapNhat = McpParallel.get(fThieuCapNhat);
        List<TramTonToolSanLuongBatThuongItemDto> sanLuongBatThuong = McpParallel.get(fBatThuong);

        return TramTonQueryResponse.builder()
                .tongQuan(tongQuan)
                .danhSach(danhSach)
                .thieuCapNhat(thieuCapNhat)
                .sanLuongBatThuong(sanLuongBatThuong)
                .xepHangKhuVuc(fXepHang != null ? McpParallel.get(fXepHang) : null)
                .thieuNhieuDieuKien(McpParallel.get(fThieuNhieu))
                .build();
    }

    private List<TramTonXepHangKhuVucToolItem> computeXepHangKhuVuc(int top, UUID nhaThauId, UUID hopDongId,
                                                                    UUID doiTuongId, UUID tinhThanhId, UUID loaiHopDongId) {
        return tramTonToolRepository.rankKhuVuc(PageRequest.of(0, top), nhaThauId, hopDongId, doiTuongId, tinhThanhId, loaiHopDongId).stream()
                .map(TramTonToolHandlerImpl::mapXepHangKhuVuc)
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
                .map(TramTonToolHandlerImpl::mapThieuNhieuDieuKien)
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

    /**
     * Ngưỡng "quá hạn" mặc định, khớp {@code TramTonTinhToanHelper.QUA_HAN_NGAY} bên REST
     * (dùng khi REST không truyền quaHanNgay). Tool AI dùng làm mặc định khi tham số
     * {@code quaHanNgay} không được truyền vào.
     */
    private static final int DEFAULT_QUA_HAN_NGAY = 90;

    /** Nhãn nhóm tuổi tồn, cùng thứ tự/mốc 7-14-28-90 ngày với REST {@code TramTonTinhToanHelper.agingBucketId}. */
    private static final String[] AGING_IDS = {"lt_1w", "1_2w", "2_4w", "1_3m", "gt_3m"};
    private static final String[] AGING_LABELS = {"< 1 Tuần", "1 - 2 Tuần", "2 - 4 Tuần", "1 - 3 Tháng", "> 3 Tháng"};

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

    /**
     * Tổng quan trạm tồn — cộng 4 tập rời nhau đúng như REST {@code TramTonServiceImpl.computeTongQuan}:
     * chờ quyết toán (chưa quá hạn) + quá hạn + chưa pháp lý + vướng mắc quá ngưỡng.
     * {@code quaHanNgay} là ngưỡng dùng chung cho cả quá hạn lẫn lọc vướng mắc (như REST).
     */
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

    /** Tỉ lệ làm tròn 2 chữ số, khớp cách REST hiển thị (count / tổng * 100). */
    private static TramTonToolBucketDto bucket(String label, long count, BigDecimal value, long tong) {
        double percent = tong > 0 ? Math.round(count * 10000.0 / tong) / 100.0 : 0.0;
        return TramTonToolBucketDto.builder().label(label).count(count).value(value).percent(percent).build();
    }

    private TramTonToolBucketDto bucketReason(String label, long count, BigDecimal value, long tong) {
        double percent = tong > 0 ? count * 100.0 / tong : 0.0;
        return TramTonToolBucketDto.builder().label(label).count(count).value(value).percent(percent).build();
    }

    /**
     * Danh sách theo tab (cùng tập với 4 nhóm của tổng quan): mặc định/cho_quyet_toan/qua_han lấy từ tập "đã hoàn thành thi công, chờ quyết toán"
     * (không truyền tab thì gồm cả hai), chua_phap_ly và vuong_mac lấy từ tập riêng.
     */
    private PagedResult<TramTonToolItemDto> computeDanhSach(UUID nhaThauId, UUID hopDongId, UUID khuVucId, UUID tinhThanhId, UUID doiTuongId,
                                                            UUID loaiId, String tab, int quaHanNgay, Integer page, Integer pageSize) {
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
            result = tramTonToolRepository.search(nhaThauId, hopDongId, khuVucId, tinhThanhId, doiTuongId, loaiId, tabQt, quaHanNgay, pageable);
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
                .map(TramTonToolHandlerImpl::mapThieuCapNhatItem)
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
                .map(TramTonToolHandlerImpl::mapSanLuongBatThuongItem)
                .toList();
    }

    private static TramTonToolSanLuongBatThuongItemDto mapSanLuongBatThuongItem(Object[] row) {
        return TramTonToolSanLuongBatThuongItemDto.builder()
                .id((UUID) row[0])
                .hopDong(HopDongInfo.of((UUID) row[1], (String) row[2], (String) row[3]))
                .giaTriHd(row[4] != null ? ((Number) row[4]).longValue() : null)
                .ngayHoanThanh((LocalDate) row[5])
                .sanLuongHieuLuc(NumberUtil.toBigDecimal(row[6]))
                .build();
    }

    private static long soNgay(LocalDate resolvedNgayHt) {
        return resolvedNgayHt != null ? ChronoUnit.DAYS.between(resolvedNgayHt, LocalDate.now()) : 0;
    }
}
