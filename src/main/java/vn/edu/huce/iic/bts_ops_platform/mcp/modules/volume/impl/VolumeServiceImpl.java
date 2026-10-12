package vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.impl;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.*;

import vn.edu.huce.iic.bts_ops_platform.mcp.components.DanhMucComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.KhuVucComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.TinhComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoChiTietHopDongToolDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoDanhSachHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoHangMucBreakdownToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoHopDongToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoKhuVucCardToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoKhuVucDetailToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoKhuVucToolDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoNhomBreakdownToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoTinhCanhBaoToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoTongQuanToolDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoTongQuanToolResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoTramVuotNguongItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoTramVuotNguongResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.TongQuanAggregateRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.hopdong.HopDong;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpFormat;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Coordinates volume queries, calculations, and tool response mapping. */
@Service
@RequiredArgsConstructor
public class VolumeServiceImpl implements VolumeService {

    private static final String[] STATUS_VALUES = {"all", "thieu", "thua", "can_bang", "alert", "da_qt", "dang_qt", "chua_qt"};

    private final VolumeRepository repository;
    private final HopDongComponent hopDongComponent;
    private final KhuVucComponent khuVucComponent;
    private final TinhComponent tinhComponent;
    private final DanhMucComponent danhMucComponent;
    private final McpParallel parallel;

    /** Bộ lọc đã quy đổi. heSoDung: hệ số truyền vào hoặc hệ số GCCC cấu hình (null khi khối không dùng hệ số). */
    private record Scope(UUID hopDongId, UUID loaiId, UUID khuVucId, UUID tinhThanhId,
                         BigDecimal heSoDung, BigDecimal nguong, String keyword) {
    }

    public NganHoTongQuanToolResponse tongQuan(NganHoFilter filter) {
        Scope s = resolve(filter, true);
        var fTongQuan = tongQuanAsync(s);
        BigDecimal tongKpi = s.khuVucId() == null && s.tinhThanhId() == null && s.keyword() == null ? tongKpi(s) : null;
        NganHoTongQuanToolDto dto = McpParallel.get(fTongQuan);
        dto.setTongKpi(tongKpi);
        return NganHoTongQuanToolResponse.of(dto);
    }

    /**
     * Port DashboardKpiSupport: KPI = tổng thành tiền cây hạng mục (định mức) của mọi hợp đồng đang hoạt động có ít nhất
     * 1 đối tượng đang hoạt động. Mỗi hợp đồng chạy 5 truy vấn song song; các hợp đồng tính lần lượt trên luồng gọi.
     */
    private BigDecimal tongKpi(Scope s) {
        BigDecimal tong = BigDecimal.ZERO;
        for (UUID id : repository.findHopDongIdsCoDoiTuong(s.hopDongId(), s.loaiId())) {
            var fNhom = parallel.async(() -> repository.findNhomByHopDongId(id));
            var fCt = parallel.async(() -> repository.findChiTietByHopDongId(id));
            var fCv = parallel.async(() -> repository.findCongViecByHopDongId(id));
            var fSumCv = parallel.async(() -> repository.sumKhoiLuongTheoCongViec(id));
            var fSumCt = parallel.async(() -> repository.sumKhoiLuongTheoChiTiet(id));
            try {
                tong = tong.add(VolumeCalc.nz(computeHangMuc(McpParallel.get(fNhom), McpParallel.get(fCt), McpParallel.get(fCv),
                        McpParallel.get(fSumCv), McpParallel.get(fSumCt)).tongThanhTien()));
            } catch (RuntimeException ex) {
                // giống safePlanAmount bên REST: cây lỗi công thức thì coi KPI hợp đồng đó = 0
            }
        }
        return tong;
    }

    public NganHoChiTietHopDongToolDto chiTietHopDong(String hopDong, Double nguongCanhBao) {
        Scope s = resolve(new NganHoFilter(hopDong, null, null, nguongCanhBao, null, null, null), false);
        return McpParallel.get(chiTietAsync(s, hopDongAsync(s)));
    }

    public NganHoKhuVucToolDto theoKhuVuc(String hopDong, Double nguongCanhBao) {
        Scope s = resolve(new NganHoFilter(hopDong, null, null, nguongCanhBao, null, null, null), false);
        return McpParallel.get(khuVucAsync(s, hopDongAsync(s)));
    }

    public NganHoDanhSachHopDongResponse danhSachHopDong(NganHoFilter filter, String statusFilter, Integer page, Integer pageSize) {
        Scope s = resolve(filter, true);
        String status = ResolveSupport.enumValue("statusFilter", statusFilter, STATUS_VALUES);
        return new NganHoDanhSachHopDongResponse(McpParallel.get(danhSachAsync(s, status, page, pageSize)));
    }

    public NganHoTramVuotNguongResponse tramVuotNguong(NganHoFilter filter, Integer page, Integer pageSize) {
        Scope s = resolve(filter, false);
        int p = PagingUtil.resolvePage(page);
        int size = PagingUtil.resolvePageSize(pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE);
        List<Object[]> rows = repository.findTramVuotNguongBinhQuan(VolumeRepository.GCCC_LOAI_HOP_DONG_ID,
            VolumeRepository.XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID, s.hopDongId(), s.loaiId(), s.khuVucId(), s.tinhThanhId(),
                size, p * size);
        List<NganHoTramVuotNguongItem> items = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            String ma = r[0] != null && !r[0].toString().isBlank() ? r[0].toString().trim() : "—";
            BigDecimal tyLe = toBigDecimal(r[8]);
            items.add(new NganHoTramVuotNguongItem(ma, (String) r[1], (String) r[2], r[3] != null ? (String) r[3] : "—",
                    toBigDecimal(r[4]), toBigDecimal(r[5]), toBigDecimal(r[6]), toBigDecimal(r[7]),
                    tyLe != null ? McpFormat.phanTram(tyLe.multiply(BigDecimal.valueOf(100))) : null,
                    Boolean.TRUE.equals(r[9]), Boolean.TRUE.equals(r[10])));
        }
        long tong = rows.isEmpty() ? 0 : ((Number) rows.get(0)[11]).longValue();
        return new NganHoTramVuotNguongResponse(PagedResult.of(items, p, size, tong));
    }

    /** Đủ mọi khối trong 1 lần: resolve 1 lần, các khối chạy song song (kết quả như volume_tool cũ). */
    public NganHoQueryResponse query(String hopDong, String query, Double heSo, Double nguongCanhBao,
                                     String loaiHopDong, String statusFilter, String khuVuc, String tinhThanh,
                                     Integer page, Integer pageSize) {
        Scope s = resolve(new NganHoFilter(hopDong, query, heSo, nguongCanhBao, loaiHopDong, khuVuc, tinhThanh), true);
        String status = ResolveSupport.enumValue("statusFilter", statusFilter, STATUS_VALUES);

        var fTongQuan = tongQuanAsync(s);
        var fDanhSach = (status != null || s.loaiId() != null || s.khuVucId() != null || s.tinhThanhId() != null)
                ? danhSachAsync(s, status, page, pageSize)
                : CompletableFuture.<PagedResult<NganHoHopDongToolItem>>completedFuture(null);
        var fHd = hopDongAsync(s);
        var fChiTiet = chiTietAsync(s, fHd);
        var fKhuVuc = khuVucAsync(s, fHd);

        return NganHoQueryResponse.builder()
                .tongQuan(McpParallel.get(fTongQuan))
                .chiTietHopDong(McpParallel.get(fChiTiet))
                .theoKhuVucTinh(McpParallel.get(fKhuVuc))
                .danhSachHopDong(McpParallel.get(fDanhSach))
                .build();
    }

    /** canHeSo: khối có dùng hệ số (tổng quan, danh sách) -> tra hệ số GCCC cấu hình khi không truyền heSo. */
    private Scope resolve(NganHoFilter f, boolean canHeSo) {
        BigDecimal nguong = f.nguongCanhBao() != null && f.nguongCanhBao() > 0
                ? BigDecimal.valueOf(f.nguongCanhBao()) : VolumeCalc.NGUONG_CANH_BAO_MAC_DINH;
        boolean heSoTruyen = f.heSo() != null && f.heSo() > 0;
        var fHopDong = parallel.async(() -> hopDongComponent.resolve(f.hopDong()));
        var fLoaiId = parallel.async(() -> danhMucComponent.resolveLoaiHopDong(f.loaiHopDong()));
        CompletableFuture<BigDecimal> fHeSo = canHeSo && !heSoTruyen ? parallel.async(this::heSoGccc) : null;
        var fTinh = parallel.async(() -> tinhComponent.resolveId(f.tinhThanh()));
        UUID khuVucId = khuVucComponent.resolveInScope(f.khuVuc()).id();
        HopDongInfo resolvedHopDong = McpParallel.get(fHopDong);
        UUID loaiId = McpParallel.get(fLoaiId);
        UUID tinhThanhId = McpParallel.get(fTinh);
        BigDecimal heSoDung = heSoTruyen ? BigDecimal.valueOf(f.heSo()) : fHeSo != null ? McpParallel.get(fHeSo) : null;
        return new Scope(resolvedHopDong.id(), loaiId, khuVucId, tinhThanhId, heSoDung, nguong, ResolveSupport.likePattern(f.query()));
    }

    private CompletableFuture<NganHoTongQuanToolDto> tongQuanAsync(Scope s) {
        var fAgg = parallel.async(() -> repository.tongQuanAggregate(s.keyword(), VolumeRepository.GCCC_LOAI_HOP_DONG_ID,
            VolumeRepository.XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID, s.heSoDung(), s.nguong(), s.loaiId(), s.khuVucId(), s.tinhThanhId()));
        var fTheoLoai = parallel.async(() -> repository.tongQuanTheoLoai(s.keyword(), s.loaiId(), s.khuVucId(), s.tinhThanhId()));
        return fAgg.thenCombine(fTheoLoai, this::buildTongQuan);
    }

    /** status đã chuẩn hoá; "all" = không lọc trạng thái. */
    private CompletableFuture<PagedResult<NganHoHopDongToolItem>> danhSachAsync(Scope s, String status, Integer page, Integer pageSize) {
        return parallel.async(() -> computeDanhSach(s.keyword(), s.heSoDung(), s.nguong(), s.loaiId(),
                "all".equals(status) ? null : status, s.khuVucId(), s.tinhThanhId(), page, pageSize));
    }

    private CompletableFuture<HopDong> hopDongAsync(Scope s) {
        UUID id = s.hopDongId();
        return id == null ? CompletableFuture.completedFuture(null) : parallel.async(() -> repository.findById(id).orElse(null));
    }

    private CompletableFuture<NganHoChiTietHopDongToolDto> chiTietAsync(Scope s, CompletableFuture<HopDong> fHd) {
        UUID id = s.hopDongId();
        if (id == null) {
            return CompletableFuture.completedFuture(null);
        }
        var fNhom = parallel.async(() -> repository.findNhomByHopDongId(id));
        var fCt = parallel.async(() -> repository.findChiTietByHopDongId(id));
        var fCv = parallel.async(() -> repository.findCongViecByHopDongId(id));
        var fSumCv = parallel.async(() -> repository.sumKhoiLuongTheoCongViec(id));
        var fSumCt = parallel.async(() -> repository.sumKhoiLuongTheoChiTiet(id));
        return CompletableFuture.allOf(fHd, fNhom, fCt, fCv, fSumCv, fSumCt).thenApply(v -> {
            HopDong hd = fHd.join();
            return hd == null ? null : computeChiTiet(hd, fNhom.join(), fCt.join(), fCv.join(), fSumCv.join(), fSumCt.join(), s.nguong());
        });
    }

    private CompletableFuture<NganHoKhuVucToolDto> khuVucAsync(Scope s, CompletableFuture<HopDong> fHd) {
        UUID id = s.hopDongId();
        if (id == null) {
            return CompletableFuture.completedFuture(null);
        }
        var fGeo = parallel.async(() -> repository.findObjectGeoByHopDongId(id));
        return fHd.thenCombine(fGeo, (hd, geo) -> hd == null ? null : computeKhuVuc(hd, geo, s.nguong()));
    }

    /** Hệ số mặc định GCCC: cấu hình DB (loai_hop_dong.heso_nguong), fallback hằng số. */
    private BigDecimal heSoGccc() {
        BigDecimal v = repository.findHeSoNguongByLoaiHopDongId(VolumeRepository.GCCC_LOAI_HOP_DONG_ID);
        return v == null || v.compareTo(BigDecimal.ZERO) <= 0 ? VolumeRepository.DEFAULT_HESO_GCCC : v;
    }

    private PagedResult<NganHoHopDongToolItem> computeDanhSach(String keyword, BigDecimal heSoDung, BigDecimal nguongCanhBao, UUID loaiId,
                                                              String status, UUID khuVucId, UUID tinhThanhId, Integer page, Integer pageSize) {
        Page<Object[]> result = repository.danhSachHopDong(keyword,
                VolumeRepository.GCCC_LOAI_HOP_DONG_ID, VolumeRepository.XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID,
                heSoDung, nguongCanhBao, loaiId, status, khuVucId, tinhThanhId,
                PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE));
        List<NganHoHopDongToolItem> items = result.getContent().stream().map(r -> NganHoHopDongToolItem.builder()
                .hopDong(HopDongInfo.of((UUID) r[0], (String) r[1], (String) r[2]))
                .giaTriHopDong(VolumeCalc.nz(toBig(r[3])))
                .tongSanLuongThucTe(VolumeCalc.nz(toBig(r[4])))
                .soTram(r[5] == null ? 0 : ((Number) r[5]).longValue())
                .chenhLechPhanTram(toBig(r[10]) == null ? null : toBig(r[10]).setScale(2, java.math.RoundingMode.HALF_UP))
                .trangThai((String) r[6])
                .trangThaiVolume((String) r[7])
                .alertLevel((String) r[8])
                .trangThaiQt((String) r[9])
                .build()).toList();
        return PagedResult.of(items, result.getNumber(), result.getSize(), result.getTotalElements());
    }

    private static BigDecimal toBig(Object value) {
        return value == null ? null : value instanceof BigDecimal b ? b : new BigDecimal(value.toString());
    }

    private NganHoTongQuanToolDto buildTongQuan(TongQuanAggregateRow agg, List<Object[]> theoLoaiRows) {
        Map<String, Long> theoLoai = new LinkedHashMap<>();
        for (Object[] row : theoLoaiRows) {
            theoLoai.put((String) row[0], ((Number) row[1]).longValue());
        }

        long giaTriHd = agg.getGiaTriHopDong() != null ? agg.getGiaTriHopDong() : 0L;
        BigDecimal tongThiCong = VolumeCalc.nz(agg.getTongThanhTienThiCong());

        return NganHoTongQuanToolDto.builder()
                .soHopDong(agg.getSoHopDong())
                .giaTriHopDong(giaTriHd)
                .tongThanhTienThiCong(tongThiCong)
                .chenhLech(tongThiCong.subtract(BigDecimal.valueOf(giaTriHd)))
                .tyLeSuDung(VolumeCalc.ratio(tongThiCong, BigDecimal.valueOf(giaTriHd)))
                .soHopDongVuotNguong(agg.getSoVuotNguong())
                .soHopDongCanhBao(agg.getSoCanhBao())
                .soHopDongThieu(agg.getSoThieu())
                .soHopDongThua(agg.getSoThua())
                .soHopDongCanBang(agg.getSoCanBang())
                .tongQuyetToan(VolumeCalc.nz(agg.getTongQuyetToan()))
                .soHopDongDaQuyetToan(agg.getSoDaQuyetToan())
                .soHopDongDangQuyetToan(agg.getSoDangQuyetToan())
                .soHopDongChuaQuyetToan(agg.getSoChuaQuyetToan())
                .tongTram(agg.getTongTram())
                .soHopDongDaThiCong(agg.getSoHopDongDaThiCong())
                .tongTramDaThiCong(agg.getTongTramDaThiCong())
                .soTramBatThuong(agg.getSoTramBatThuong())
                .soHopDongThieuLon(agg.getSoHopDongThieuLon())
                .tongTramDaQuyetToan(agg.getTongTramDaQuyetToan())
                .tongTramDangQuyetToan(agg.getTongTramDangQuyetToan())
                .tongTramChuaQuyetToan(agg.getTongTramChuaQuyetToan())
                .giaTriDaQuyetToan(agg.getGiaTriDaQuyetToan() != null ? agg.getGiaTriDaQuyetToan() : 0L)
                .giaTriDangQuyetToan(agg.getGiaTriDangQuyetToan() != null ? agg.getGiaTriDangQuyetToan() : 0L)
                .giaTriChuaQuyetToan(agg.getGiaTriChuaQuyetToan() != null ? agg.getGiaTriChuaQuyetToan() : 0L)
                .soHopDongTheoLoai(theoLoai)
                .build();
    }

    private NganHoChiTietHopDongToolDto computeChiTiet(HopDong hopDong, List<Object[]> nhomData, List<Object[]> chiTietData,
                                                        List<Object[]> congViecData, List<Object[]> sumCongViec, List<Object[]> sumChiTiet,
                                                        BigDecimal nguongCanhBao) {
        VolumeCalc.ComputeResult computed = computeHangMuc(nhomData, chiTietData, congViecData, sumCongViec, sumChiTiet);
        return toChiTiet(hopDong, computed, nguongCanhBao);
    }

    private static VolumeCalc.ComputeResult computeHangMuc(List<Object[]> nhomData, List<Object[]> chiTietData,
                                                                  List<Object[]> congViecData, List<Object[]> sumCongViec,
                                                                  List<Object[]> sumChiTiet) {
        List<VolumeCalc.NhomRow> nhomRows = new ArrayList<>();
        for (Object[] row : nhomData) {
            nhomRows.add(new VolumeCalc.NhomRow((UUID) row[0], (String) row[1], (String) row[2]));
        }
        List<VolumeCalc.ChiTietRow> chiTietRows = new ArrayList<>();
        for (Object[] row : chiTietData) {
            chiTietRows.add(new VolumeCalc.ChiTietRow((UUID) row[0], (UUID) row[1], (String) row[2], (String) row[3],
                    (BigDecimal) row[4], (BigDecimal) row[5], (String) row[6], (String) row[7], (String) row[8]));
        }
        List<VolumeCalc.CongViecRow> congViecRows = new ArrayList<>();
        for (Object[] row : congViecData) {
            congViecRows.add(new VolumeCalc.CongViecRow((UUID) row[0], (UUID) row[1], (String) row[2], (String) row[3],
                    (BigDecimal) row[4], (BigDecimal) row[5], (String) row[6], (String) row[7], (String) row[8]));
        }
        Map<UUID, BigDecimal> theoCongViec = new HashMap<>();
        for (Object[] row : sumCongViec) {
            theoCongViec.put((UUID) row[0], toBigDecimal(row[1]));
        }
        Map<UUID, BigDecimal> theoChiTiet = new HashMap<>();
        for (Object[] row : sumChiTiet) {
            theoChiTiet.put((UUID) row[0], toBigDecimal(row[1]));
        }

        return VolumeCalc.compute(nhomRows, chiTietRows, congViecRows, theoCongViec, theoChiTiet);
    }

    private NganHoChiTietHopDongToolDto toChiTiet(HopDong hopDong, VolumeCalc.ComputeResult computed, BigDecimal nguongCanhBao) {
        long giaTriHd = hopDong.getGiaTriHd() != null ? hopDong.getGiaTriHd() : 0L;
        List<NganHoNhomBreakdownToolItem> nhom = computed.nhom().stream().map(n -> {
            List<NganHoHangMucBreakdownToolItem> hangMuc = n.hangMuc().stream()
                    .map(h -> NganHoHangMucBreakdownToolItem.builder()
                            .id(h.id()).ma(h.ma()).ten(h.ten())
                            .khoiLuong(h.khoiLuong()).donGia(h.donGia()).thanhTien(h.thanhTien())
                            .build())
                    .toList();
            return NganHoNhomBreakdownToolItem.builder()
                    .id(n.id()).ma(n.ma()).ten(n.ten()).thanhTien(n.thanhTien()).hangMuc(hangMuc)
                    .build();
        }).toList();

        return NganHoChiTietHopDongToolDto.builder()
                .hopDongId(hopDong.getId())
                .maHopDong(hopDong.getMaHopDong())
                .ten(hopDong.getTen())
                .giaTriHd(giaTriHd)
                .tongThanhTienThiCong(computed.tongThanhTien())
                .chenhLech(VolumeCalc.nz(computed.tongThanhTien()).subtract(BigDecimal.valueOf(giaTriHd)))
                .tyLeSuDung(VolumeCalc.ratio(computed.tongThanhTien(), BigDecimal.valueOf(giaTriHd)))
                .trangThai(StatusLabels.volumeTrangThai(VolumeCalc.resolveStatus(giaTriHd, computed.tongThanhTien(), nguongCanhBao)))
                .nhom(nhom)
                .build();
    }

    private NganHoKhuVucToolDto computeKhuVuc(HopDong hopDong, List<Object[]> objectRows, BigDecimal nguongCanhBao) {
        if (objectRows.isEmpty()) {
            return NganHoKhuVucToolDto.builder().tongKhuVuc(0).duLieu(List.of()).canhBaoThieu(List.of())
                    .canhBaoThua(List.of()).chiTiet(List.of()).build();
        }

        int soTram = objectRows.size();
        BigDecimal planPerStation = VolumeCalc.nz(hopDong.getTongThanhTienThiCong())
                .divide(BigDecimal.valueOf(soTram), java.math.MathContext.DECIMAL64);

        record TinhBucket(String khuVuc, String tinh, long[] soTramTong, long[] soTramCoSanLuong,
                          long[] soTramThieu, long[] soTramThua, BigDecimal[] giaTriThieu,
                          BigDecimal[] giaTriThua, BigDecimal[] giaTriHopDong, BigDecimal[] thanhTienThiCong,
                          List<String> maTramThieu, List<String> maTramThua) {
        }

        Map<String, Map<String, TinhBucket>> khuVucMap = new LinkedHashMap<>();
        for (Object[] row : objectRows) {
            String khuVucTen = (String) row[1];
            String tinhTen = (String) row[3];
            BigDecimal sanLuongHieuLuc = VolumeCalc.nz((BigDecimal) row[5]);
            String maTram = (String) row[6];

            Map<String, TinhBucket> provinces = khuVucMap.computeIfAbsent(khuVucTen, k -> new LinkedHashMap<>());
            TinhBucket bucket = provinces.computeIfAbsent(tinhTen, k -> new TinhBucket(khuVucTen, tinhTen,
                    new long[]{0}, new long[]{0}, new long[]{0}, new long[]{0},
                    new BigDecimal[]{BigDecimal.ZERO}, new BigDecimal[]{BigDecimal.ZERO},
                    new BigDecimal[]{BigDecimal.ZERO}, new BigDecimal[]{BigDecimal.ZERO},
                    new ArrayList<>(), new ArrayList<>()));

            bucket.soTramTong()[0]++;
            if (sanLuongHieuLuc.compareTo(BigDecimal.ZERO) > 0) {
                bucket.soTramCoSanLuong()[0]++;
            }
            if (planPerStation.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal delta = planPerStation.subtract(sanLuongHieuLuc);
                if (delta.compareTo(BigDecimal.ZERO) > 0) {
                    bucket.soTramThieu()[0]++;
                    bucket.giaTriThieu()[0] = bucket.giaTriThieu()[0].add(delta);
                    bucket.maTramThieu().add(maTram);
                } else if (delta.compareTo(BigDecimal.ZERO) < 0) {
                    bucket.soTramThua()[0]++;
                    bucket.giaTriThua()[0] = bucket.giaTriThua()[0].add(delta.abs());
                    bucket.maTramThua().add(maTram);
                }
            }
            bucket.giaTriHopDong()[0] = bucket.giaTriHopDong()[0].add(planPerStation);
            bucket.thanhTienThiCong()[0] = bucket.thanhTienThiCong()[0].add(sanLuongHieuLuc);
        }

        List<NganHoKhuVucCardToolItem> cards = new ArrayList<>();
        List<NganHoKhuVucDetailToolItem> details = new ArrayList<>();
        List<NganHoTinhCanhBaoToolItem> canhBaoThieu = new ArrayList<>();
        List<NganHoTinhCanhBaoToolItem> canhBaoThua = new ArrayList<>();
        String[] colors = {"#2563eb", "#059669", "#d97706", "#dc2626", "#7c3aed", "#0891b2"};

        int khuVucIndex = 0;
        for (Map.Entry<String, Map<String, TinhBucket>> khuVucEntry : khuVucMap.entrySet()) {
            String khuVucName = khuVucEntry.getKey();
            String color = colors[khuVucIndex % colors.length];
            String khuVucId = "kv-" + khuVucIndex;

            BigDecimal khuVucContract = BigDecimal.ZERO;
            BigDecimal khuVucConstruction = BigDecimal.ZERO;
            long khuVucStations = 0;
            long khuVucShortage = 0;
            long khuVucSurplus = 0;
            int soCanhBao = 0;
            int order = 1;

            for (TinhBucket bucket : khuVucEntry.getValue().values()) {
                BigDecimal remaining = bucket.giaTriHopDong()[0].subtract(bucket.thanhTienThiCong()[0]);
                BigDecimal usedPercent = VolumeCalc.ratio(bucket.thanhTienThiCong()[0], bucket.giaTriHopDong()[0]);
                String variant = VolumeCalc.resolveProvinceVariant(
                        bucket.soTramThieu()[0], bucket.soTramThua()[0], remaining, usedPercent, nguongCanhBao);

                khuVucContract = khuVucContract.add(bucket.giaTriHopDong()[0]);
                khuVucConstruction = khuVucConstruction.add(bucket.thanhTienThiCong()[0]);
                khuVucStations += bucket.soTramTong()[0];
                khuVucShortage += bucket.soTramThieu()[0];
                khuVucSurplus += bucket.soTramThua()[0];
                if (bucket.soTramThieu()[0] > 0 || bucket.soTramThua()[0] > 0 || !"normal".equals(variant)) {
                    soCanhBao += 1;
                }

                if (bucket.soTramThieu()[0] > 0) {
                    canhBaoThieu.add(NganHoTinhCanhBaoToolItem.builder()
                            .id(khuVucId + "-" + order + "-thieu")
                            .tinh(bucket.tinh()).khuVuc(khuVucName)
                            .soTram(bucket.soTramThieu()[0])
                            .giaTriSanLuong(bucket.giaTriThieu()[0])
                            .giaTriTrungBinh(bucket.giaTriThieu()[0].divide(BigDecimal.valueOf(bucket.soTramThieu()[0]), java.math.MathContext.DECIMAL64))
                            .type(StatusLabels.volumeAlertType("shortage"))
                            .maTramList(bucket.maTramThieu())
                            .build());
                }
                if (bucket.soTramThua()[0] > 0) {
                    canhBaoThua.add(NganHoTinhCanhBaoToolItem.builder()
                            .id(khuVucId + "-" + order + "-thua")
                            .tinh(bucket.tinh()).khuVuc(khuVucName)
                            .soTram(bucket.soTramThua()[0])
                            .giaTriSanLuong(bucket.giaTriThua()[0])
                            .giaTriTrungBinh(bucket.giaTriThua()[0].divide(BigDecimal.valueOf(bucket.soTramThua()[0]), java.math.MathContext.DECIMAL64))
                            .type(StatusLabels.volumeAlertType("surplus"))
                            .maTramList(bucket.maTramThua())
                            .build());
                }
                order++;
            }

            details.add(NganHoKhuVucDetailToolItem.builder()
                    .id(khuVucId).name(khuVucName).color(color).moRongMacDinh(khuVucIndex == 0)
                    .soTinh(khuVucEntry.getValue().size()).soCanhBao(soCanhBao)
                    .build());

            BigDecimal khuVucRemaining = khuVucContract.subtract(khuVucConstruction);
            cards.add(NganHoKhuVucCardToolItem.builder()
                    .id(khuVucId).name(khuVucName).color(color)
                    .usedPercent(VolumeCalc.ratio(khuVucConstruction, khuVucContract))
                    .barColor(khuVucRemaining.compareTo(BigDecimal.ZERO) < 0 ? "#ef4444" : "#059669")
                    .giaTriHopDong(khuVucContract).thanhTienThiCong(khuVucConstruction)
                    .giaTriConLai(khuVucRemaining.max(BigDecimal.ZERO))
                    .chenhLech(khuVucConstruction.subtract(khuVucContract))
                    .trungBinhMoiTram(khuVucStations > 0
                            ? khuVucConstruction.divide(BigDecimal.valueOf(khuVucStations), java.math.MathContext.DECIMAL64)
                            : BigDecimal.ZERO)
                    .soTramCanBang(khuVucStations).soTramThieu(khuVucShortage).soTramThua(khuVucSurplus)
                    .build());
            khuVucIndex++;
        }

        canhBaoThieu.sort(Comparator.comparing((NganHoTinhCanhBaoToolItem a) -> VolumeCalc.nz(a.getGiaTriSanLuong()).abs()).reversed());
        canhBaoThua.sort(Comparator.comparing((NganHoTinhCanhBaoToolItem a) -> VolumeCalc.nz(a.getGiaTriSanLuong()).abs()).reversed());

        return NganHoKhuVucToolDto.builder()
                .tongKhuVuc(cards.size())
                .duLieu(cards)
                .canhBaoThieu(canhBaoThieu)
                .canhBaoThua(canhBaoThua)
                .chiTiet(details)
                .build();
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value instanceof BigDecimal bigDecimal ? bigDecimal : BigDecimal.valueOf(((Number) value).doubleValue());
    }
}