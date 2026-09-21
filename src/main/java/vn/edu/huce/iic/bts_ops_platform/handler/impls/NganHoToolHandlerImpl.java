package vn.edu.huce.iic.bts_ops_platform.handler.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.dto.nganho.NganHoChiTietHopDongToolDto;
import vn.edu.huce.iic.bts_ops_platform.dto.nganho.NganHoHopDongToolItem;
import vn.edu.huce.iic.bts_ops_platform.dto.common.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.support.PagingUtil;
import org.springframework.data.domain.Page;
import vn.edu.huce.iic.bts_ops_platform.dto.nganho.NganHoHangMucBreakdownToolItem;
import vn.edu.huce.iic.bts_ops_platform.dto.nganho.NganHoKhuVucCardToolItem;
import vn.edu.huce.iic.bts_ops_platform.dto.nganho.NganHoKhuVucDetailToolItem;
import vn.edu.huce.iic.bts_ops_platform.dto.nganho.NganHoKhuVucToolDto;
import vn.edu.huce.iic.bts_ops_platform.dto.nganho.NganHoNhomBreakdownToolItem;
import vn.edu.huce.iic.bts_ops_platform.dto.nganho.NganHoTinhCanhBaoToolItem;
import vn.edu.huce.iic.bts_ops_platform.dto.nganho.NganHoQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.nganho.NganHoTongQuanToolDto;
import vn.edu.huce.iic.bts_ops_platform.dto.nganho.TongQuanAggregateRow;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.handler.NganHoToolHandler;
import vn.edu.huce.iic.bts_ops_platform.repository.NganHoToolRepository;
import vn.edu.huce.iic.bts_ops_platform.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.support.NganHoFormulaCalc;
import vn.edu.huce.iic.bts_ops_platform.support.StatusLabels;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;

import vn.edu.huce.iic.bts_ops_platform.support.ResolveSupport;

import vn.edu.huce.iic.bts_ops_platform.support.McpParallel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Impl thật cho tool AI module Ngân sách hợp đồng — port lại VolumeService.tongQuan/getChiTiet/
 * khuVucByHopDong thành native query + tính toán riêng của tools (xem NganHoToolRepository/
 * NganHoFormulaCalc), KHÔNG còn phụ thuộc VolumeService ở module ngoài. Không port heSoOverrides/
 * contractorScope (tool AI luôn dùng hệ số mặc định, không giới hạn phạm vi nhà thầu).
 */
@Component
@RequiredArgsConstructor
public class NganHoToolHandlerImpl implements NganHoToolHandler {

    private final NganHoToolRepository repository;
    private final HopDongComponent hopDongComponent;
    private final vn.edu.huce.iic.bts_ops_platform.components.KhuVucComponent khuVucComponent;
    private final vn.edu.huce.iic.bts_ops_platform.components.TinhComponent tinhComponent;
    private final vn.edu.huce.iic.bts_ops_platform.components.DanhMucComponent danhMucComponent;
    private final McpParallel parallel;

    @Override
    public NganHoQueryResponse query(String hopDong, String query, Double heSo, Double nguongCanhBao,
                                     String loaiHopDong, String statusFilter, String khuVuc, String tinhThanh,
                                     Integer page, Integer pageSize) {
        BigDecimal nguong = nguongCanhBao != null && nguongCanhBao > 0
                ? BigDecimal.valueOf(nguongCanhBao) : NganHoFormulaCalc.NGUONG_CANH_BAO_MAC_DINH;
        var f_hopDong = parallel.async(() -> hopDongComponent.resolve(hopDong));
        var f_loaiId = parallel.async(() -> danhMucComponent.resolveLoaiHopDong(loaiHopDong));
        var f_heSo = parallel.async(this::heSoGccc);
        var f_tinh = parallel.async(() -> tinhComponent.resolveId(tinhThanh));
        UUID khuVucId = khuVucComponent.resolveInScope(khuVuc).id();
        String status = ResolveSupport.enumValue("statusFilter", statusFilter,
                "all", "thieu", "thua", "can_bang", "alert", "da_qt", "dang_qt", "chua_qt");
        HopDongInfo resolvedHopDong = McpParallel.get(f_hopDong);
        UUID loaiId = McpParallel.get(f_loaiId);
        UUID tinhThanhId = McpParallel.get(f_tinh);
        UUID hopDongId = resolvedHopDong.id();

        BigDecimal heSoGccc = McpParallel.get(f_heSo);
        BigDecimal heSoDung = heSo != null && heSo > 0 ? BigDecimal.valueOf(heSo) : heSoGccc;
        String keyword = ResolveSupport.likePattern(query);

        // Mọi truy vấn lá độc lập nhau: bắn cùng lúc từ luồng gọi tool (không lồng async), độ trễ = nhánh chậm nhất
        var fAgg = parallel.async(() -> repository.tongQuanAggregate(keyword, NganHoToolRepository.GCCC_LOAI_HOP_DONG_ID,
                NganHoToolRepository.XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID, heSoDung, nguong, loaiId, khuVucId, tinhThanhId));
        var fTheoLoai = parallel.async(() -> repository.tongQuanTheoLoai(keyword, loaiId, khuVucId, tinhThanhId));
        var fDanhSach = (status != null || loaiId != null || khuVucId != null || tinhThanhId != null)
                ? parallel.async(() -> computeDanhSach(keyword, heSoDung, nguong, loaiId, "all".equals(status) ? null : status, khuVucId, tinhThanhId, page, pageSize)) : null;
        var fHd = hopDongId != null ? parallel.async(() -> repository.findById(hopDongId).orElse(null)) : null;
        var fNhom = hopDongId != null ? parallel.async(() -> repository.findNhomByHopDongId(hopDongId)) : null;
        var fCt = hopDongId != null ? parallel.async(() -> repository.findChiTietByHopDongId(hopDongId)) : null;
        var fCv = hopDongId != null ? parallel.async(() -> repository.findCongViecByHopDongId(hopDongId)) : null;
        var fSumCv = hopDongId != null ? parallel.async(() -> repository.sumKhoiLuongTheoCongViec(hopDongId)) : null;
        var fSumCt = hopDongId != null ? parallel.async(() -> repository.sumKhoiLuongTheoChiTiet(hopDongId)) : null;
        var fGeo = hopDongId != null ? parallel.async(() -> repository.findObjectGeoByHopDongId(hopDongId)) : null;

        NganHoTongQuanToolDto tongQuan = buildTongQuan(McpParallel.get(fAgg), McpParallel.get(fTheoLoai));
        HopDong hd = fHd != null ? McpParallel.get(fHd) : null;
        NganHoChiTietHopDongToolDto chiTietHopDong = hd == null ? null : computeChiTiet(hd, McpParallel.get(fNhom), McpParallel.get(fCt),
                McpParallel.get(fCv), McpParallel.get(fSumCv), McpParallel.get(fSumCt), nguong);
        NganHoKhuVucToolDto theoKhuVucTinh = hd == null ? null : computeKhuVuc(hd, McpParallel.get(fGeo), nguong);

        return NganHoQueryResponse.builder()
                .tongQuan(tongQuan)
                .chiTietHopDong(chiTietHopDong)
                .theoKhuVucTinh(theoKhuVucTinh)
                .danhSachHopDong(fDanhSach != null ? McpParallel.get(fDanhSach) : null)
                .build();
    }

    /** Hệ số mặc định GCCC: cấu hình DB (loai_hop_dong.heso_nguong), fallback hằng số — khớp REST VolumeServiceImpl.loadGcccHeSoFromDb(). */
    private BigDecimal heSoGccc() {
        BigDecimal v = repository.findHeSoNguongByLoaiHopDongId(NganHoToolRepository.GCCC_LOAI_HOP_DONG_ID);
        return v == null || v.compareTo(BigDecimal.ZERO) <= 0 ? NganHoToolRepository.DEFAULT_HESO_GCCC : v;
    }

    /** Danh sách hợp đồng theo trạng thái ngân sách/quyết toán, cùng tập và cách tính với tổng quan. */
    private PagedResult<NganHoHopDongToolItem> computeDanhSach(String keyword, BigDecimal heSoDung, BigDecimal nguongCanhBao, UUID loaiId,
                                                              String status, UUID khuVucId, UUID tinhThanhId, Integer page, Integer pageSize) {
        Page<Object[]> result = repository.danhSachHopDong(keyword,
                NganHoToolRepository.GCCC_LOAI_HOP_DONG_ID, NganHoToolRepository.XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID,
                heSoDung, nguongCanhBao, loaiId, status, khuVucId, tinhThanhId,
                PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE));
        List<NganHoHopDongToolItem> items = result.getContent().stream().map(r -> NganHoHopDongToolItem.builder()
                .hopDong(HopDongInfo.of((UUID) r[0], (String) r[1], (String) r[2]))
                .giaTriHopDong(NganHoFormulaCalc.nz(toBig(r[3])))
                .tongSanLuongThucTe(NganHoFormulaCalc.nz(toBig(r[4])))
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
        Map<UUID, Long> theoLoai = new LinkedHashMap<>();
        for (Object[] row : theoLoaiRows) {
            theoLoai.put((UUID) row[0], ((Number) row[1]).longValue());
        }

        long giaTriHd = agg.getGiaTriHopDong() != null ? agg.getGiaTriHopDong() : 0L;
        BigDecimal tongThiCong = NganHoFormulaCalc.nz(agg.getTongThanhTienThiCong());

        return NganHoTongQuanToolDto.builder()
                .soHopDong(agg.getSoHopDong())
                .giaTriHopDong(giaTriHd)
                .tongThanhTienThiCong(tongThiCong)
                .chenhLech(tongThiCong.subtract(BigDecimal.valueOf(giaTriHd)))
                .tyLeSuDung(NganHoFormulaCalc.ratio(tongThiCong, BigDecimal.valueOf(giaTriHd)))
                .soHopDongVuotNguong(agg.getSoVuotNguong())
                .soHopDongCanhBao(agg.getSoCanhBao())
                .soHopDongThieu(agg.getSoThieu())
                .soHopDongThua(agg.getSoThua())
                .soHopDongCanBang(agg.getSoCanBang())
                .tongQuyetToan(NganHoFormulaCalc.nz(agg.getTongQuyetToan()))
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

    /** Port VolumeService.getChiTiet — cây hạng mục qua NganHoFormulaCalc (formula engine porting). */
    private NganHoChiTietHopDongToolDto computeChiTiet(HopDong hopDong, List<Object[]> nhomData, List<Object[]> chiTietData,
                                                        List<Object[]> congViecData, List<Object[]> sumCongViec, List<Object[]> sumChiTiet,
                                                        BigDecimal nguongCanhBao) {
        List<NganHoFormulaCalc.NhomRow> nhomRows = new ArrayList<>();
        for (Object[] row : nhomData) {
            nhomRows.add(new NganHoFormulaCalc.NhomRow((UUID) row[0], (String) row[1], (String) row[2]));
        }
        List<NganHoFormulaCalc.ChiTietRow> chiTietRows = new ArrayList<>();
        for (Object[] row : chiTietData) {
            chiTietRows.add(new NganHoFormulaCalc.ChiTietRow((UUID) row[0], (UUID) row[1], (String) row[2], (String) row[3],
                    (BigDecimal) row[4], (BigDecimal) row[5], (String) row[6], (String) row[7], (String) row[8]));
        }
        List<NganHoFormulaCalc.CongViecRow> congViecRows = new ArrayList<>();
        for (Object[] row : congViecData) {
            congViecRows.add(new NganHoFormulaCalc.CongViecRow((UUID) row[0], (UUID) row[1], (String) row[2], (String) row[3],
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

        NganHoFormulaCalc.ComputeResult computed = NganHoFormulaCalc.compute(nhomRows, chiTietRows, congViecRows, theoCongViec, theoChiTiet);

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
                .chenhLech(NganHoFormulaCalc.nz(computed.tongThanhTien()).subtract(BigDecimal.valueOf(giaTriHd)))
                .tyLeSuDung(NganHoFormulaCalc.ratio(computed.tongThanhTien(), BigDecimal.valueOf(giaTriHd)))
                .trangThai(StatusLabels.volumeTrangThai(NganHoFormulaCalc.resolveStatus(giaTriHd, computed.tongThanhTien(), nguongCanhBao)))
                .nhom(nhom)
                .build();
    }

    /**
     * Port VolumeService.khuVucByHopDong — nhóm theo khu_vuc_id/tinh_thanh_id (cột FK thật trên
     * hop_dong_doi_tuong, không cần snapshot service). Không port oldProvince/provinceKey (chỉ có
     * ý nghĩa khi suy tỉnh gián tiếp từ mã trạm cũ — nằm ngoài phạm vi tool đọc dữ liệu tổng hợp).
     */
    private NganHoKhuVucToolDto computeKhuVuc(HopDong hopDong, List<Object[]> objectRows, BigDecimal nguongCanhBao) {
        if (objectRows.isEmpty()) {
            return NganHoKhuVucToolDto.builder().tongKhuVuc(0).duLieu(List.of()).canhBaoThieu(List.of())
                    .canhBaoThua(List.of()).chiTiet(List.of()).build();
        }

        int soTram = objectRows.size();
        BigDecimal planPerStation = NganHoFormulaCalc.nz(hopDong.getTongThanhTienThiCong())
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
            BigDecimal sanLuongHieuLuc = NganHoFormulaCalc.nz((BigDecimal) row[5]);
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
                BigDecimal usedPercent = NganHoFormulaCalc.ratio(bucket.thanhTienThiCong()[0], bucket.giaTriHopDong()[0]);
                String variant = NganHoFormulaCalc.resolveProvinceVariant(
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
                    .usedPercent(NganHoFormulaCalc.ratio(khuVucConstruction, khuVucContract))
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

        canhBaoThieu.sort(Comparator.comparing((NganHoTinhCanhBaoToolItem a) -> NganHoFormulaCalc.nz(a.getGiaTriSanLuong()).abs()).reversed());
        canhBaoThua.sort(Comparator.comparing((NganHoTinhCanhBaoToolItem a) -> NganHoFormulaCalc.nz(a.getGiaTriSanLuong()).abs()).reversed());

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
