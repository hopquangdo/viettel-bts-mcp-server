package vn.edu.huce.iic.bts_ops_platform.handler.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongBuocLichSuItem;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongBuocPhanBoItem;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongCanhBaoItem;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongCanhBaoTienDoResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongSoLuongTheoKieuItem;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongTheoBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongTimKiemItem;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongTimKiemResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongTrangThaiKhuVucResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongXepHangKhuVucToolItem;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongXepHangTinhToolItem;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.LichSuBuocProjection;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.PhanBoBuocProjection;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.XepHangKhuVucProjection;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.XepHangTinhProjection;
import vn.edu.huce.iic.bts_ops_platform.dto.doituong.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.dto.khuvuc.KhuVucInfo;
import vn.edu.huce.iic.bts_ops_platform.dto.nhathau.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.dto.common.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.handler.HopDongToolHandler;
import vn.edu.huce.iic.bts_ops_platform.repository.HopDongToolRepository;
import vn.edu.huce.iic.bts_ops_platform.components.DoiTuongComponent;
import vn.edu.huce.iic.bts_ops_platform.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.components.KhuVucComponent;
import vn.edu.huce.iic.bts_ops_platform.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.components.TinhComponent;
import vn.edu.huce.iic.bts_ops_platform.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.support.ResolveSupport;
import vn.edu.huce.iic.bts_ops_platform.support.StatusLabels;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Impl thật cho tool AI module Hợp đồng — 1 method duy nhất, chỉ gọi HopDongToolRepository. */
@Component
@RequiredArgsConstructor
public class HopDongToolHandlerImpl implements HopDongToolHandler {

    private final HopDongToolRepository hopDongToolRepository;
    private final DoiTuongComponent doiTuongComponent;
    private final HopDongComponent hopDongComponent;
    private final KhuVucComponent khuVucComponent;
    private final NhaThauComponent nhaThauComponent;
    private final TinhComponent tinhComponent;
    private final vn.edu.huce.iic.bts_ops_platform.components.DanhMucComponent danhMucComponent;
    private final McpParallel parallel;

    /** Ngưỡng "chậm tiến độ" mặc định (%) - khớp HopDongThongKeTatCaHelper.CHAM_TIEN_DO_NGUONG_PHAN_TRAM bên REST. */
    private static final double DEFAULT_NGUONG_CHAM_TIEN_DO = 70.0;
    /** Ngưỡng mặc định (%) phân loại cảnh báo tiến độ khối lượng: >= xanh là Xanh, >= vàng là Vàng, còn lại Đỏ. */
    private static final double DEFAULT_NGUONG_XANH = 90.0;
    private static final double DEFAULT_NGUONG_VANG = 70.0;

    @Override
    public HopDongQueryResponse query(String doiTuong, String hopDong, String nhaThau,
                                      String khuVuc, String tinhThanh, String loaiHopDong, String kieuHopDong, String query, Integer page, Integer pageSize,
                                      Double nguongChamTienDo, Double nguongXanh, Double nguongVang) {
        double resolvedNguongCham = nguongChamTienDo != null ? nguongChamTienDo : DEFAULT_NGUONG_CHAM_TIEN_DO;
        double resolvedNguongXanh = nguongXanh != null ? nguongXanh : DEFAULT_NGUONG_XANH;
        double resolvedNguongVang = nguongVang != null ? nguongVang : DEFAULT_NGUONG_VANG;
        var f_resolvedNhaThau = parallel.async(() -> nhaThauComponent.resolve(nhaThau));
        var f_resolvedHopDong = parallel.async(() -> hopDongComponent.resolve(hopDong));
        var f_resolvedDoiTuong = parallel.async(() -> doiTuongComponent.resolve(doiTuong));
        var f_tinhThanhId = parallel.async(() -> tinhComponent.resolveId(tinhThanh));
        var f_loaiId = parallel.async(() -> danhMucComponent.resolveLoaiHopDong(loaiHopDong));
        var f_kieuId = parallel.async(() -> danhMucComponent.resolveKieuHopDong(kieuHopDong));
        KhuVucInfo resolvedKhuVuc = khuVucComponent.resolveInScope(khuVuc);
        // các tra cứu id chạy song song (mỗi tra cứu 1-2 lần khứ hồi tới DB nếu chưa có trong bộ nhớ đệm)
        NhaThauInfo resolvedNhaThau = McpParallel.get(f_resolvedNhaThau);
        HopDongInfo resolvedHopDong = McpParallel.get(f_resolvedHopDong);
        DoiTuongInfo resolvedDoiTuong = McpParallel.get(f_resolvedDoiTuong);
        UUID tinhThanhId = McpParallel.get(f_tinhThanhId);
        UUID loaiId = McpParallel.get(f_loaiId);
        UUID kieuId = McpParallel.get(f_kieuId);

        UUID khuVucId = resolvedKhuVuc.id();
        UUID nhaThauId = resolvedNhaThau.id();
        UUID hopDongId = resolvedHopDong.id();
        UUID doiTuongId = resolvedDoiTuong.id();
        boolean doiTuongCuThe = doiTuongComponent.laCuThe(resolvedDoiTuong);

        // Các khối độc lập chạy song song: độ trễ = khối chậm nhất thay vì tổng. Truy vấn của tổng quan phát riêng từng câu.
        var fTrangThai = parallel.async(() -> hopDongToolRepository.thongKeTrangThaiDoiTuongTheoHopDong(khuVucId, tinhThanhId, nhaThauId, hopDongId, doiTuongId, loaiId, kieuId));
        var fTheoHopDong = parallel.async(() -> hopDongToolRepository.thongKeTheoHopDong(khuVucId, tinhThanhId, nhaThauId, hopDongId, doiTuongId, loaiId, kieuId));
        var fNhom = parallel.async(() -> hopDongToolRepository.countTheoNhomFiltered(khuVucId, tinhThanhId, nhaThauId, hopDongId, doiTuongId, loaiId, kieuId));
        var fDanhSach = (query != null || nhaThauId != null || doiTuongId != null || tinhThanhId != null || loaiId != null || kieuId != null)
                ? parallel.async(() -> computeDanhSach(query, nhaThauId, doiTuongId, tinhThanhId, loaiId, kieuId, page, pageSize)) : null;
        var fThuHep = (khuVucId != null || tinhThanhId != null || nhaThauId != null || hopDongId != null || loaiId != null || kieuId != null)
                ? parallel.async(() -> computeThongKeThuHep(khuVucId, tinhThanhId, nhaThauId, hopDongId, loaiId, kieuId)) : null;
        var fTienDo = parallel.async(() -> computeCanhBaoTienDo(hopDongId, nhaThauId, tinhThanhId, loaiId, kieuId, resolvedNguongXanh, resolvedNguongVang));
        var fKhuVuc = (khuVucId == null && !doiTuongCuThe) ? parallel.async(() -> computeXepHangKhuVuc(hopDongId, tinhThanhId, nhaThauId, doiTuongId, loaiId, kieuId)) : null;
        var fTinh = (hopDongId != null && !doiTuongCuThe) ? parallel.async(() -> computeXepHangTinh(hopDongId)) : null;
        var fBuoc = hopDongId != null ? parallel.async(() -> computeTheoBuoc(hopDongId, page, pageSize)) : null;

        HopDongTongQuanResponse tongQuan = computeTongQuan(McpParallel.get(fTrangThai), McpParallel.get(fTheoHopDong),
                McpParallel.get(fNhom), resolvedNguongCham);
        HopDongTimKiemResponse danhSach = fDanhSach != null ? McpParallel.get(fDanhSach) : null;
        HopDongTrangThaiKhuVucResponse thongKeThuHep = fThuHep != null ? McpParallel.get(fThuHep) : null;
        HopDongCanhBaoTienDoResponse canhBaoTienDo = McpParallel.get(fTienDo);
        List<HopDongXepHangKhuVucToolItem> xepHangKhuVuc = fKhuVuc != null ? McpParallel.get(fKhuVuc) : null;
        List<HopDongXepHangTinhToolItem> xepHangTinh = fTinh != null ? McpParallel.get(fTinh) : null;
        HopDongTheoBuocResponse theoBuoc = fBuoc != null ? McpParallel.get(fBuoc) : null;

        return HopDongQueryResponse.builder()
                .tongQuan(tongQuan)
                .danhSach(danhSach)
                .thongKeThuHep(thongKeThuHep)
                .canhBaoTienDo(canhBaoTienDo)
                .xepHangKhuVuc(xepHangKhuVuc)
                .xepHangTinh(xepHangTinh)
                .theoBuoc(theoBuoc)
                .build();
    }

    private List<HopDongXepHangKhuVucToolItem> computeXepHangKhuVuc(UUID hopDongId, UUID tinhThanhId,
                                                                    UUID nhaThauId, UUID doiTuongId,
                                                                    UUID loaiHopDongId, UUID kieuHopDongId) {
        return hopDongToolRepository.rankKhuVuc(20, hopDongId, tinhThanhId, nhaThauId, doiTuongId, loaiHopDongId, kieuHopDongId).stream()
                .map(HopDongToolHandlerImpl::mapXepHangKhuVuc)
                .toList();
    }

    private static HopDongXepHangKhuVucToolItem mapXepHangKhuVuc(XepHangKhuVucProjection row) {
        HopDongXepHangKhuVucToolItem item = new HopDongXepHangKhuVucToolItem();
        item.setKhuVuc(row.getKhuVuc());
        item.setSoHopDong(row.getSoHopDong() != null ? row.getSoHopDong() : 0);
        item.setSoDoiTuong(row.getSoDoiTuong() != null ? row.getSoDoiTuong() : 0);
        item.setSoVuongMac(row.getSoVuongMac() != null ? row.getSoVuongMac() : 0);
        return item;
    }

    private List<HopDongXepHangTinhToolItem> computeXepHangTinh(UUID hopDongId) {
        return hopDongToolRepository.rankTinhTheoHopDong(hopDongId).stream()
                .map(HopDongToolHandlerImpl::mapXepHangTinh)
                .toList();
    }

    private static HopDongXepHangTinhToolItem mapXepHangTinh(XepHangTinhProjection row) {
        HopDongXepHangTinhToolItem item = new HopDongXepHangTinhToolItem();
        item.setTinh(row.getTinh());
        item.setSoDoiTuong(row.getSoDoiTuong() != null ? row.getSoDoiTuong() : 0);
        item.setSoHoanThanh(row.getSoHoanThanh() != null ? row.getSoHoanThanh() : 0);
        item.setSoVuongMac(row.getSoVuongMac() != null ? row.getSoVuongMac() : 0);
        return item;
    }

    private HopDongTheoBuocResponse computeTheoBuoc(UUID hopDongId, Integer page, Integer pageSize) {
        List<HopDongBuocPhanBoItem> phanBo = hopDongToolRepository.phanBoBuoc(hopDongId).stream()
                .map(HopDongToolHandlerImpl::mapBuocPhanBo)
                .toList();

        PagedResult<HopDongBuocLichSuItem> lichSu;
        if (hopDongId == null) {
            int resolvedPage = PagingUtil.resolvePage(page);
            int resolvedPageSize = PagingUtil.resolvePageSize(pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE);
            lichSu = PagedResult.of(List.of(), resolvedPage, resolvedPageSize, 0);
        } else {
                Page<LichSuBuocProjection> lichSuPage =
                    hopDongToolRepository.lichSuBuoc(hopDongId, PagingUtil.toPageRequest(page, pageSize,
                        PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE));
            List<HopDongBuocLichSuItem> items = lichSuPage.getContent().stream()
                    .map(HopDongToolHandlerImpl::mapBuocLichSu)
                    .toList();
            lichSu = PagedResult.of(items, lichSuPage.getNumber(), lichSuPage.getSize(), lichSuPage.getTotalElements());
        }

        return HopDongTheoBuocResponse.builder()
                .phanBo(phanBo)
                .lichSu(lichSu)
                .build();
    }

    private static HopDongBuocPhanBoItem mapBuocPhanBo(PhanBoBuocProjection row) {
        HopDongBuocPhanBoItem item = new HopDongBuocPhanBoItem();
        item.setTenBuoc(row.getTenBuoc());
        item.setThuTu(row.getThuTu());
        item.setSoDoiTuong(row.getSoDoiTuong() != null ? row.getSoDoiTuong() : 0);
        return item;
    }

    private static HopDongBuocLichSuItem mapBuocLichSu(LichSuBuocProjection row) {
        HopDongBuocLichSuItem item = new HopDongBuocLichSuItem();
        item.setNgay(row.getNgay());
        item.setHanhDong(row.getHanhDong());
        item.setMoTa(row.getMoTa());
        item.setNguoiThucHien(row.getNguoiThucHien());
        return item;
    }

    /**
     * Tổng quan hợp đồng - bám HopDongServiceImpl.thongKeTatCa().tongHop (số FE hiển thị):
     * tongDoiTuong = tổng đối tượng hoạt động của các hợp đồng (bỏ hạng mục/BOQ), tyLeHoanThanh = % đối tượng
     * ở trạng thái hoàn thành trên tổng đối tượng có trạng thái, soHopDongChamTienDo = số hợp đồng (có đối tượng)
     * có tỷ lệ hoàn thành nhỏ hơn nguongChamTienDo, soHopDongVuongMac = tổng số vướng mắc đang mở của các hợp đồng đó.
     */
    private HopDongTongQuanResponse computeTongQuan(List<Object[]> trangThaiRows, List<Object[]> theoHopDongRows,
                                                    List<Object[]> nhomRows, double nguongChamTienDo) {
        Map<UUID, Map<String, Long>> trangThaiTheoHopDong = new HashMap<>();
        for (Object[] row : trangThaiRows) {
            trangThaiTheoHopDong.computeIfAbsent((UUID) row[0], ignored -> new HashMap<>())
                    .merge((String) row[1], ((Number) row[2]).longValue(), Long::sum);
        }
        long tongHopDong = 0;
        long tongDoiTuong = 0;
        long soHopDongChamTienDo = 0;
        long soHopDongVuongMac = 0;
        Map<String, Long> trangThaiGop = new HashMap<>();
        for (Object[] row : theoHopDongRows) {
            Map<String, Long> trangThai = trangThaiTheoHopDong.getOrDefault((UUID) row[0], Map.of());
            long soDoiTuong = ((Number) row[2]).longValue();
            tongHopDong++;
            tongDoiTuong += soDoiTuong;
            trangThai.forEach((ma, so) -> trangThaiGop.merge(ma, so, Long::sum));
            if (soDoiTuong <= 0) {
                continue;
            }
            if (tinhTyLeHoanThanh(trangThai) < nguongChamTienDo) {
                soHopDongChamTienDo++;
            }
            soHopDongVuongMac += ((Number) row[3]).longValue();
        }
        long tongHoatDong = tongHopDong;
        double tyLeHoanThanh = tongDoiTuong > 0 ? tinhTyLeHoanThanh(trangThaiGop) : 0.0;

        Map<String, Long> tienDoTheoTrangThai = new LinkedHashMap<>();
        for (String status : List.of("CHUA_TC", "DANG_TC", "HOAN_THANH", "HUY")) {
            tienDoTheoTrangThai.put(StatusLabels.trangThaiThiCong(status), 0L);
        }
        List<Object[]> kieuRows = new ArrayList<>();
        Map<String, Long> soLuongTheoLoai = new HashMap<>();
        for (Object[] row : nhomRows) {
            String kind = (String) row[0];
            long soLuong = ((Number) row[5]).longValue();
            switch (kind) {
                case "T" -> tienDoTheoTrangThai.put(StatusLabels.trangThaiThiCong((String) row[1]), soLuong);
                case "K" -> kieuRows.add(row);
                default -> soLuongTheoLoai.put((String) row[1], soLuong);
            }
        }
        // kiểu hợp đồng theo số lượng giảm dần; đồng số lượng thì theo mã (trước đây thứ tự đồng hạng không xác định)
        kieuRows.sort((x, y) -> {
            int byCount = Long.compare(((Number) y[5]).longValue(), ((Number) x[5]).longValue());
            return byCount != 0 ? byCount : String.valueOf(x[2]).compareTo(String.valueOf(y[2]));
        });

        List<HopDongSoLuongTheoKieuItem> soLuongTheoKieu = new ArrayList<>();
        for (Object[] row : kieuRows) {
            soLuongTheoKieu.add(HopDongSoLuongTheoKieuItem.builder()
                    .kieuHopDongId(UUID.fromString((String) row[1]))
                    .ma((String) row[2])
                    .ten((String) row[3])
                    .nhom(row[4] != null ? row[4].toString() : null)
                    .soLuong(((Number) row[5]).longValue())
                    .build());
        }

        return HopDongTongQuanResponse.builder()
                .tongHopDong(tongHopDong)
                .tongHoatDong(tongHoatDong)
                .tongDoiTuong(tongDoiTuong)
                .tienDoTheoTrangThai(tienDoTheoTrangThai)
                .tyLeHoanThanh(tyLeHoanThanh)
                .soLuongTheoKieu(soLuongTheoKieu)
                .soLuongTheoLoai(soLuongTheoLoai)
                .soHopDongChamTienDo(soHopDongChamTienDo)
                .soHopDongVuongMac(soHopDongVuongMac)
                .build();
    }

    private HopDongTimKiemResponse computeDanhSach(String query, UUID nhaThauId, UUID doiTuongId, UUID tinhThanhId, UUID loaiId, UUID kieuId, Integer page, Integer pageSize) {
        String keyword = ResolveSupport.likePattern(query);
        Page<HopDong> pageResult = hopDongToolRepository.search(keyword, loaiId, kieuId, nhaThauId, doiTuongId, tinhThanhId,
            PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE));
        // Bảng danh mục loại/kiểu hợp đồng rất nhỏ — lấy hết 1 lần rồi map trong bộ nhớ thay vì
        // join thêm vào native query "search" (đã DISTINCT h.* khá phức tạp, xem HopDongToolRepository).
        Map<UUID, String> loaiHopDongTenById = new HashMap<>();
        for (Object[] row : hopDongToolRepository.findAllLoaiHopDongIdTen()) {
            loaiHopDongTenById.put((UUID) row[0], (String) row[1]);
        }
        Map<UUID, String> kieuHopDongTenById = new HashMap<>();
        for (Object[] row : hopDongToolRepository.findAllKieuHopDongIdTen()) {
            kieuHopDongTenById.put((UUID) row[0], (String) row[1]);
        }
        List<HopDongTimKiemItem> items = pageResult.getContent().stream()
                .map(h -> HopDongTimKiemItem.builder()
                        .id(h.getId())
                        .maHopDong(h.getMaHopDong())
                        .ten(h.getTen())
                        .giaTriHd(h.getGiaTriHd())
                        .trangThaiThiCong(StatusLabels.trangThaiThiCong(h.getTrangThaiThiCong()))
                        .loaiHopDongId(h.getLoaiHopDongId())
                        .loaiHopDongTen(loaiHopDongTenById.get(h.getLoaiHopDongId()))
                        .kieuHopDongId(h.getKieuHopDongId())
                        .kieuHopDongTen(kieuHopDongTenById.get(h.getKieuHopDongId()))
                        .build())
                .toList();
        return HopDongTimKiemResponse.builder()
            .danhSach(PagedResult.of(items, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements()))
                .build();
    }

    private HopDongTrangThaiKhuVucResponse computeThongKeThuHep(UUID khuVucId, UUID tinhThanhId, UUID nhaThauId, UUID hopDongId, UUID loaiId, UUID kieuId) {
        var stat = hopDongToolRepository.thongKeTheoKhuVuc(khuVucId, tinhThanhId, loaiId, kieuId, nhaThauId, hopDongId);
        int tong = stat.getTong() != null ? stat.getTong() : 0;
        int hoanThanh = stat.getHoanThanh() != null ? stat.getHoanThanh() : 0;
        int vuongMac = stat.getCoVuongMacMo() != null ? stat.getCoVuongMacMo() : 0;
        return HopDongTrangThaiKhuVucResponse.builder()
                .tong(tong)
                .hoanThanh(hoanThanh)
                .dangThiCong(Math.max(tong - hoanThanh, 0))
                .vuongMac(vuongMac)
                .build();
    }

    private HopDongCanhBaoTienDoResponse computeCanhBaoTienDo(UUID hopDongId, UUID nhaThauId, UUID tinhThanhId, UUID loaiId, UUID kieuId,
                                                               double nguongXanh, double nguongVang) {
        TienDoTongHop tienDoTongHop = tinhTienDoTatCa(hopDongId, nhaThauId, tinhThanhId, loaiId, kieuId, nguongXanh, nguongVang);
        return HopDongCanhBaoTienDoResponse.builder()
                .soHopDongXanh(tienDoTongHop.soXanh())
                .soHopDongVang(tienDoTongHop.soVang())
                .soHopDongDo(tienDoTongHop.soDo())
                .hopDongDo(tienDoTongHop.hopDongDo())
                .build();
    }

    private record TienDoTongHop(long soXanh, long soVang, long soDo, double tyLeHoanThanhTrungBinh, List<HopDongCanhBaoItem> hopDongDo) {
    }

    private TienDoTongHop tinhTienDoTatCa(UUID hopDongId, UUID nhaThauId, UUID tinhThanhId, UUID loaiId, UUID kieuId, double nguongXanh, double nguongVang) {
        long xanh = 0;
        long vang = 0;
        long do_ = 0;
        BigDecimal tongTyLe = BigDecimal.ZERO;
        int soLuong = 0;
        List<HopDongCanhBaoItem> hopDongDo = new ArrayList<>();

        for (Object[] row : hopDongToolRepository.tienDoTatCaHopDong(loaiId, kieuId, hopDongId, nhaThauId, tinhThanhId)) {
            UUID hopDongIdRow = (UUID) row[0];
            String maHd = (String) row[1];
            String ten = (String) row[2];
            BigDecimal tongKhoiLuong = (BigDecimal) row[3];
            BigDecimal sanLuongDone = row[4] instanceof BigDecimal bd ? bd : BigDecimal.valueOf(((Number) row[4]).doubleValue());

            if (tongKhoiLuong == null || tongKhoiLuong.signum() <= 0) {
                continue;
            }
            double tyLe = sanLuongDone.divide(tongKhoiLuong, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            tongTyLe = tongTyLe.add(BigDecimal.valueOf(tyLe));
            soLuong++;

            if (tyLe >= nguongXanh) {
                xanh++;
            } else if (tyLe >= nguongVang) {
                vang++;
            } else {
                do_++;
                hopDongDo.add(HopDongCanhBaoItem.builder()
                        .hopDong(HopDongInfo.of(hopDongIdRow, maHd, ten))
                        .tyLeHoanThanh(tyLe)
                        .build());
            }
        }

        double trungBinh = soLuong > 0
                ? tongTyLe.divide(BigDecimal.valueOf(soLuong), 2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;
        return new TienDoTongHop(xanh, vang, do_, trungBinh, hopDongDo);
    }

    /** Trạng thái đối tượng coi là đã hoàn thành - khớp HopDongDanhSachTienDoHelper.isCompletedStatus. */
    private static boolean laTrangThaiHoanThanh(String ma) {
        if (ma == null) {
            return false;
        }
        String n = ma.trim().toUpperCase(java.util.Locale.ROOT);
        return n.equals("HT") || n.equals("QT") || n.equals("HOAN_THANH")
                || n.contains("QUYET TOAN") || n.contains("QUYẾT TOÁN")
                || n.contains("HOAN THANH") || n.contains("HOÀN THÀNH");
    }

    /** % đối tượng hoàn thành trên tổng đối tượng có trạng thái, làm tròn 2 số - khớp computeTyLeHoanThanh bên REST. */
    private static double tinhTyLeHoanThanh(Map<String, Long> trangThai) {
        if (trangThai == null || trangThai.isEmpty()) {
            return 0.0;
        }
        long tong = trangThai.values().stream().mapToLong(Long::longValue).sum();
        if (tong <= 0) {
            return 0.0;
        }
        long done = trangThai.entrySet().stream()
                .filter(e -> laTrangThaiHoanThanh(e.getKey()))
                .mapToLong(Map.Entry::getValue)
                .sum();
        return Math.round((done * 10000.0) / tong) / 100.0;
    }
}
