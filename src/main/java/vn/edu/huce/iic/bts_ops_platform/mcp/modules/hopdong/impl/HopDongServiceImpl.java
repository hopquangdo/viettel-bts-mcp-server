package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.impl;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongBuocLichSuItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongBuocPhanBoItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongCanhBaoItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongCanhBaoTienDoResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTheoLoaiItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTheoLoaiResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTongQuanToolResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongXepHangResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongSoLuongTheoKieuItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTheoBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTimKiemItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTimKiemResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTrangThaiKhuVucResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.LichSuBuocProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.PhanBoBuocProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituong.dto.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.KhuVucInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongToolMapper;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangContext;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangDong;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangRegistry;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.HopDongToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.NumberUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.hopdong.HopDong;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Impl tool AI module Hợp đồng. Mỗi method public (1 tool hopdong_*) tự quy đổi bộ lọc ({@link #resolve}) rồi chỉ chạy
 * truy vấn của khối nó trả về. {@link #query} (đủ mọi khối, cho tool gộp ngoài module) resolve 1 lần, các khối chạy song song.
 */
@Service
@RequiredArgsConstructor
public class HopDongServiceImpl implements HopDongService {

    private final HopDongToolRepository hopDongToolRepository;
    private final DoiTuongComponent doiTuongComponent;
    private final HopDongComponent hopDongComponent;
    private final KhuVucComponent khuVucComponent;
    private final NhaThauComponent nhaThauComponent;
    private final TinhComponent tinhComponent;
    private final DanhMucComponent danhMucComponent;
    private final McpParallel parallel;
    private final HopDongXepHangRegistry xepHangRegistry;

    /** Ngưỡng "chậm tiến độ" mặc định (%) - khớp HopDongThongKeTatCaHelper.CHAM_TIEN_DO_NGUONG_PHAN_TRAM bên REST. */
    private static final double DEFAULT_NGUONG_CHAM_TIEN_DO = 70.0;
    /** Ngưỡng mặc định (%) phân loại cảnh báo tiến độ khối lượng: >= xanh là Xanh, >= vàng là Vàng, còn lại Đỏ. */
    private static final double DEFAULT_NGUONG_XANH = 90.0;
    private static final double DEFAULT_NGUONG_VANG = 70.0;

    /** Bộ lọc đã quy đổi ra id. doiTuongCuThe: doiTuong là mã/id 1 đối tượng (không phải loại) -> bỏ bảng xếp hạng. */
    private record Scope(UUID khuVucId, UUID tinhThanhId, UUID nhaThauId, UUID hopDongId, UUID doiTuongId,
                         boolean doiTuongCuThe, UUID loaiId, UUID kieuId) {

        boolean coBoLocThuHep() {
            return khuVucId != null || tinhThanhId != null || nhaThauId != null || hopDongId != null || loaiId != null || kieuId != null;
        }
    }

    // ------------------------------------------------------------------ API theo từng tool

    public HopDongTongQuanToolResponse tongQuan(HopDongFilter filter, Double nguongChamTienDo, LocalDate fromDate, LocalDate toDate) {
        Scope s = resolve(filter);
        LocalDate[] ky = khoangNgay(fromDate, toDate);
        var fHoanThanhKy = ky[0] != null || ky[1] != null ? parallel.async(() -> hoanThanhTrongKy(s, ky[0], ky[1])) : null;
        HopDongTongQuanResponse tongQuan = tongQuanOf(s, nguongChamTienDo);
        return HopDongToolMapper.tongQuan(tongQuan, nguongChamTienDo != null ? nguongChamTienDo : DEFAULT_NGUONG_CHAM_TIEN_DO,
                fHoanThanhKy != null ? McpParallel.get(fHoanThanhKy) : null);
    }

    public HopDongTheoLoaiResponse thongKeTheoLoai(HopDongFilter filter, Double nguongChamTienDoIn) {
        double nguong = nguongChamTienDoIn != null ? nguongChamTienDoIn : DEFAULT_NGUONG_CHAM_TIEN_DO;
        Scope s = resolve(filter);
        // Số thẻ lĩnh vực của Dashboard không có bộ lọc -> chỉ gắn khi không lọc gì (lọc thì null, tránh lệch phạm vi)
        boolean khongLoc = filter.nhaThau() == null && filter.khuVuc() == null && filter.tinhThanh() == null && filter.kieuHopDong() == null;
        // Mọi loại đang hoạt động (cả loại chưa có hợp đồng); loại có hợp đồng thì tính lại tổng quan như hopdong_tong_quan lọc loại đó.
        // Chạy tuần tự (vài loại): tongQuanOf đã tự song song bên trong, lồng async vào pool sẽ dễ cạn luồng.
        Map<String, Map<String, Long>> theoKieu = new HashMap<>();
        if (khongLoc) {
            for (Object[] r : hopDongToolRepository.soDoiTuongTheoLoaiVaKieu()) {
                theoKieu.computeIfAbsent((String) r[0], k -> new LinkedHashMap<>()).merge((String) r[1], ((Number) r[2]).longValue(), Long::sum);
            }
        }
        List<HopDongTheoLoaiItem> items = new ArrayList<>();
        for (Object[] lv : hopDongToolRepository.thongKeTheoLinhVuc()) {
            String ten = (String) lv[0];
            Scope sl = new Scope(s.khuVucId(), s.tinhThanhId(), s.nhaThauId(), s.hopDongId(), s.doiTuongId(), s.doiTuongCuThe(),
                    danhMucComponent.resolveLoaiHopDong(ten), s.kieuId());
            HopDongTongQuanToolResponse tq = HopDongToolMapper.tongQuan(tongQuanOf(sl, nguong), nguong, null);
            items.add(new HopDongTheoLoaiItem(ten, tq.tongHopDong(), tq.tongDoiTuong(), tq.tyLeHoanThanh(), tq.soHopDongChamTienDo(),
                    tq.soHopDongVuongMac(), tq.tienDoTheoTrangThai(),
                    khongLoc ? ((Number) lv[2]).longValue() : null,
                    khongLoc ? theoKieu.getOrDefault(ten, Map.of()) : null,
                    khongLoc ? NumberUtil.toBigDecimal(lv[3]) : null,
                    khongLoc ? ((Number) lv[4]).longValue() : null));
        }
        items.sort((a, b) -> Long.compare(b.soHopDong(), a.soHopDong()));
        return new HopDongTheoLoaiResponse(vn.edu.huce.iic.bts_ops_platform.mcp.support.McpFormat.phanTram(nguong), items);
    }

    public HopDongTimKiemResponse timKiem(HopDongFilter filter, String query, LocalDate fromDate, LocalDate toDate, String loaiNgay,
                                          Integer page, Integer pageSize) {
        Scope s = resolve(filter);
        LocalDate[] ky = khoangNgay(fromDate, toDate);
        return computeDanhSach(query, s.nhaThauId(), s.doiTuongId(), s.tinhThanhId(), s.loaiId(), s.kieuId(), ky[0], ky[1],
                loaiNgayLoc(loaiNgay), page, pageSize);
    }

    public HopDongTrangThaiKhuVucResponse thongKeThuHep(HopDongFilter filter) {
        Scope s = resolve(filter);
        return computeThongKeThuHep(s.khuVucId(), s.tinhThanhId(), s.nhaThauId(), s.hopDongId(), s.loaiId(), s.kieuId());
    }

    public HopDongCanhBaoTienDoResponse canhBaoTienDo(HopDongFilter filter, Double nguongXanh, Double nguongVang) {
        Scope s = resolve(filter);
        return computeCanhBaoTienDo(s.hopDongId(), s.nhaThauId(), s.tinhThanhId(), s.loaiId(), s.kieuId(),
                nguongXanh != null ? nguongXanh : DEFAULT_NGUONG_XANH, nguongVang != null ? nguongVang : DEFAULT_NGUONG_VANG);
    }

    public HopDongXepHangResponse xepHang(HopDongFilter filter, String theo, Integer top) {
        HopDongXepHangChieu chieu = HopDongXepHangChieu.of(theo); // sai mã thì báo lỗi trước khi chạm DB
        Scope s = resolve(filter);
        HopDongXepHangStrategy strategy = xepHangRegistry.get(chieu);
        if (s.doiTuongCuThe()) {
            return HopDongToolMapper.xepHang(chieu, strategy.tieuChi(), null,
                    "Đang lọc 1 đối tượng cụ thể nên không có bảng xếp hạng. Bỏ tham số doiTuong để xếp hạng.");
        }
        HopDongXepHangContext ctx = xepHangContext(s, topOf(top));
        String lyDo = strategy.lyDoBoQua(ctx);
        if (lyDo != null) {
            String goiY = xepHangRegistry.all().values().stream()
                    .filter(st -> st.chieu() != chieu && st.lyDoBoQua(ctx) == null)
                    .map(st -> "theo=" + st.chieu().code() + " (" + st.chieu().label() + ")")
                    .collect(Collectors.joining(", "));
            return HopDongToolMapper.xepHang(chieu, strategy.tieuChi(), null,
                    goiY.isEmpty() ? lyDo : lyDo + " Có thể thử " + goiY + ".");
        }
        return HopDongToolMapper.xepHang(chieu, strategy.tieuChi(), strategy.rank(ctx), null);
    }

    public HopDongTheoBuocResponse theoBuoc(String hopDong, Integer page, Integer pageSize) {
        return computeTheoBuoc(hopDongComponent.resolve(hopDong).id(), page, pageSize);
    }

    /** Đủ mọi khối trong 1 lần (system_overview, hồ sơ đối tượng): resolve 1 lần, các khối chạy song song. */
    public HopDongQueryResponse query(String doiTuong, String hopDong, String nhaThau,
                                      String khuVuc, String tinhThanh, String loaiHopDong, String kieuHopDong, String query, Integer page, Integer pageSize,
                                      Double nguongChamTienDo, Double nguongXanh, Double nguongVang,
                                      Integer top, LocalDate fromDate, LocalDate toDate, String loaiNgay) {
        Scope s = resolve(new HopDongFilter(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, kieuHopDong));
        LocalDate[] ky = khoangNgay(fromDate, toDate);
        LocalDate tuNgay = ky[0];
        LocalDate denNgay = ky[1];
        String loaiNgayLoc = loaiNgayLoc(loaiNgay);
        int topN = topOf(top);
        double xanh = nguongXanh != null ? nguongXanh : DEFAULT_NGUONG_XANH;
        double vang = nguongVang != null ? nguongVang : DEFAULT_NGUONG_VANG;

        // Danh sách chỉ khi có điều kiện tìm (không thì toàn bộ hợp đồng, vô nghĩa cho tổng quan gộp).
        boolean coTimKiem = query != null || s.nhaThauId() != null || s.doiTuongId() != null || s.tinhThanhId() != null
                || s.loaiId() != null || s.kieuId() != null || tuNgay != null || denNgay != null;
        var fDanhSach = coTimKiem ? parallel.async(() -> computeDanhSach(query, s.nhaThauId(), s.doiTuongId(), s.tinhThanhId(),
                s.loaiId(), s.kieuId(), tuNgay, denNgay, loaiNgayLoc, page, pageSize)) : null;
        var fThuHep = s.coBoLocThuHep() ? parallel.async(() -> computeThongKeThuHep(s.khuVucId(), s.tinhThanhId(), s.nhaThauId(),
                s.hopDongId(), s.loaiId(), s.kieuId())) : null;
        var fTienDo = parallel.async(() -> computeCanhBaoTienDo(s.hopDongId(), s.nhaThauId(), s.tinhThanhId(), s.loaiId(), s.kieuId(), xanh, vang));
        var fKhuVuc = xepHangAsync(s, HopDongXepHangChieu.KHU_VUC, topN);
        var fTinh = xepHangAsync(s, HopDongXepHangChieu.TINH, topN);
        var fHoanThanhKy = tuNgay != null || denNgay != null ? parallel.async(() -> hoanThanhTrongKy(s, tuNgay, denNgay)) : null;
        var fBuoc = s.hopDongId() != null ? parallel.async(() -> computeTheoBuoc(s.hopDongId(), page, pageSize)) : null;

        // Tổng quan chạy trên thread gọi (bản thân nó phát 3 truy vấn song song) trong lúc các khối trên chạy.
        HopDongTongQuanResponse tongQuan = tongQuanOf(s, nguongChamTienDo);

        return HopDongQueryResponse.builder()
                .tongQuan(tongQuan)
                .danhSach(fDanhSach != null ? McpParallel.get(fDanhSach) : null)
                .thongKeThuHep(fThuHep != null ? McpParallel.get(fThuHep) : null)
                .canhBaoTienDo(McpParallel.get(fTienDo))
                .xepHangKhuVuc(fKhuVuc != null ? McpParallel.get(fKhuVuc).stream().map(HopDongToolMapper::khuVucItem).toList() : null)
                .xepHangTinh(fTinh != null ? McpParallel.get(fTinh).stream().map(HopDongToolMapper::tinhItem).toList() : null)
                .theoBuoc(fBuoc != null ? McpParallel.get(fBuoc) : null)
                .doiTuongHoanThanhTrongKy(fHoanThanhKy != null ? McpParallel.get(fHoanThanhKy) : null)
                .build();
    }

    // ------------------------------------------------------------------ quy đổi bộ lọc

    private Scope resolve(HopDongFilter f) {
        // các tra cứu id chạy song song (mỗi tra cứu 1-2 lần khứ hồi tới DB nếu chưa có trong bộ nhớ đệm)
        var fNhaThau = parallel.async(() -> nhaThauComponent.resolve(f.nhaThau()));
        var fHopDong = parallel.async(() -> hopDongComponent.resolve(f.hopDong()));
        var fDoiTuong = parallel.async(() -> doiTuongComponent.resolve(f.doiTuong()));
        var fTinhThanhId = parallel.async(() -> tinhComponent.resolveId(f.tinhThanh()));
        var fLoaiId = parallel.async(() -> danhMucComponent.resolveLoaiHopDong(f.loaiHopDong()));
        var fKieuId = parallel.async(() -> danhMucComponent.resolveKieuHopDong(f.kieuHopDong()));
        KhuVucInfo khuVuc = khuVucComponent.resolveInScope(f.khuVuc());
        DoiTuongInfo doiTuong = McpParallel.get(fDoiTuong);
        return new Scope(khuVuc.id(), McpParallel.get(fTinhThanhId), McpParallel.get(fNhaThau).id(), McpParallel.get(fHopDong).id(),
                doiTuong.id(), doiTuongComponent.laCuThe(doiTuong), McpParallel.get(fLoaiId), McpParallel.get(fKieuId));
    }

    /** [tuNgay, denNgay], đảo lại nếu nhập ngược. */
    private static LocalDate[] khoangNgay(LocalDate fromDate, LocalDate toDate) {
        boolean nguoc = fromDate != null && toDate != null && fromDate.isAfter(toDate);
        return new LocalDate[] {nguoc ? toDate : fromDate, nguoc ? fromDate : toDate};
    }

    /** Lọc danhSach theo ngày bắt đầu thực hiện (mặc định) hoặc hạn hợp đồng. */
    private static String loaiNgayLoc(String loaiNgay) {
        return loaiNgay != null && loaiNgay.trim().equalsIgnoreCase("han_hop_dong") ? "han_hop_dong" : "ngay_thuc_hien";
    }

    private static int topOf(Integer top) {
        return top != null ? Math.min(Math.max(top, 1), 20) : 20;
    }

    // ------------------------------------------------------------------ từng khối

    /** 3 truy vấn của tổng quan phát song song. Chỉ gọi từ thread request, không gọi bên trong parallel.async. */
    private HopDongTongQuanResponse tongQuanOf(Scope s, Double nguongChamTienDo) {
        var fTrangThai = parallel.async(() -> hopDongToolRepository.thongKeTrangThaiDoiTuongTheoHopDong(
                s.khuVucId(), s.tinhThanhId(), s.nhaThauId(), s.hopDongId(), s.doiTuongId(), s.loaiId(), s.kieuId()));
        var fTheoHopDong = parallel.async(() -> hopDongToolRepository.thongKeTheoHopDong(
                s.khuVucId(), s.tinhThanhId(), s.nhaThauId(), s.hopDongId(), s.doiTuongId(), s.loaiId(), s.kieuId()));
        var fNhom = parallel.async(() -> hopDongToolRepository.countTheoNhomFiltered(
                s.khuVucId(), s.tinhThanhId(), s.nhaThauId(), s.hopDongId(), s.doiTuongId(), s.loaiId(), s.kieuId()));
        return computeTongQuan(McpParallel.get(fTrangThai), McpParallel.get(fTheoHopDong), McpParallel.get(fNhom),
                nguongChamTienDo != null ? nguongChamTienDo : DEFAULT_NGUONG_CHAM_TIEN_DO);
    }

    private Long hoanThanhTrongKy(Scope s, LocalDate tuNgay, LocalDate denNgay) {
        return hopDongToolRepository.countDoiTuongHoanThanhTrongKy(tuNgay, denNgay, s.khuVucId(), s.tinhThanhId(),
                s.nhaThauId(), s.hopDongId(), s.loaiId(), s.kieuId(), s.doiTuongId());
    }

    private HopDongXepHangContext xepHangContext(Scope s, int topN) {
        return new HopDongXepHangContext(s.khuVucId(), s.tinhThanhId(), s.nhaThauId(), s.hopDongId(), s.doiTuongId(),
                s.loaiId(), s.kieuId(), topN);
    }

    /** 1 chiều cho đường tổng hợp (query): null khi lọc 1 đối tượng cụ thể hoặc chiều bị bỏ qua trong phạm vi này. */
    private CompletableFuture<List<HopDongXepHangDong>> xepHangAsync(Scope s, HopDongXepHangChieu chieu, int topN) {
        HopDongXepHangStrategy strategy = xepHangRegistry.get(chieu);
        HopDongXepHangContext ctx = xepHangContext(s, topN);
        if (s.doiTuongCuThe() || strategy.lyDoBoQua(ctx) != null) {
            return null;
        }
        return parallel.async(() -> strategy.rank(ctx));
    }

    private HopDongTheoBuocResponse computeTheoBuoc(UUID hopDongId, Integer page, Integer pageSize) {
        List<HopDongBuocPhanBoItem> phanBo = hopDongToolRepository.phanBoBuoc(hopDongId).stream()
            .map(HopDongServiceImpl::mapBuocPhanBo)
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
                    .map(HopDongServiceImpl::mapBuocLichSu)
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
        // Trạng thái thi công từng hợp đồng suy từ tiến độ đối tượng (khớp FE classifyHopDongProgressStatus),
        // KHÔNG đọc cột hop_dong.trang_thai_thi_cong vì cột này không được cập nhật theo tiến độ thực tế.
        long soChuaTc = 0;
        long soDangTc = 0;
        long soHoanThanh = 0;
        for (Object[] row : theoHopDongRows) {
            Map<String, Long> trangThai = trangThaiTheoHopDong.getOrDefault((UUID) row[0], Map.of());
            long soDoiTuong = ((Number) row[2]).longValue();
            tongHopDong++;
            tongDoiTuong += soDoiTuong;
            trangThai.forEach((ma, so) -> trangThaiGop.merge(ma, so, Long::sum));
            if (soDoiTuong <= 0) {
                soChuaTc++;
                continue;
            }
            double tyLe = tinhTyLeHoanThanh(trangThai);
            if (tyLe >= 100) {
                soHoanThanh++;
            } else if (tyLe > 0) {
                soDangTc++;
            } else {
                soChuaTc++;
            }
            if (tyLe < nguongChamTienDo) {
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
        // HUY giữ theo cột trang_thai_thi_cong (hủy là thao tác thủ công); 3 trạng thái còn lại theo tiến độ đối tượng
        long soHuy = tienDoTheoTrangThai.getOrDefault(StatusLabels.trangThaiThiCong("HUY"), 0L);
        tienDoTheoTrangThai.put(StatusLabels.trangThaiThiCong("CHUA_TC"), Math.max(0, soChuaTc - soHuy));
        tienDoTheoTrangThai.put(StatusLabels.trangThaiThiCong("DANG_TC"), soDangTc);
        tienDoTheoTrangThai.put(StatusLabels.trangThaiThiCong("HOAN_THANH"), soHoanThanh);
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

    private HopDongTimKiemResponse computeDanhSach(String query, UUID nhaThauId, UUID doiTuongId, UUID tinhThanhId, UUID loaiId, UUID kieuId,
                                                        java.time.LocalDate fromDate, java.time.LocalDate toDate, String loaiNgay,
                                                        Integer page, Integer pageSize) {
        String keyword = ResolveSupport.likePattern(query);
        Page<HopDong> pageResult = hopDongToolRepository.search(keyword, loaiId, kieuId, nhaThauId, doiTuongId, tinhThanhId,
            fromDate, toDate, loaiNgay, PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE));
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
