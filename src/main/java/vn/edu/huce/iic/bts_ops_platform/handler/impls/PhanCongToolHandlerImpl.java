package vn.edu.huce.iic.bts_ops_platform.handler.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.dto.doituong.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.common.dto.GeoRefResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.dto.phancong.PhanCongChuaPhanCongToolItem;
import vn.edu.huce.iic.bts_ops_platform.dto.phancong.PhanCongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.dto.phancong.PhanCongToolItem;
import vn.edu.huce.iic.bts_ops_platform.dto.phancong.PhanCongToolThongKe;
import vn.edu.huce.iic.bts_ops_platform.dto.phancong.PhanCongXepHangNhaThauToolItem;
import vn.edu.huce.iic.bts_ops_platform.dto.phancong.ChuaPhanCongRow;
import vn.edu.huce.iic.bts_ops_platform.dto.phancong.TongQuanDoiTuongRow;
import vn.edu.huce.iic.bts_ops_platform.dto.phancong.XepHangNhaThauRow;
import vn.edu.huce.iic.bts_ops_platform.dto.khuvuc.KhuVucInfo;
import vn.edu.huce.iic.bts_ops_platform.dto.nhathau.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.dto.canbo.CanBoInfo;
import vn.edu.huce.iic.bts_ops_platform.handler.PhanCongToolHandler;
import vn.edu.huce.iic.bts_ops_platform.repository.PhanCongToolRepository;
import vn.edu.huce.iic.bts_ops_platform.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.components.KhuVucComponent;
import vn.edu.huce.iic.bts_ops_platform.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.components.TinhComponent;
import vn.edu.huce.iic.bts_ops_platform.components.CanBoComponent;
import vn.edu.huce.iic.bts_ops_platform.repository.PhanCongEntityToolRepository;
import vn.edu.huce.iic.bts_ops_platform.dto.common.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.support.ResolveSupport;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.entity.PhanCong;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Impl thật cho tool AI module Phân công — đọc trực tiếp qua repository (không qua service tầng
 * nghiệp vụ) để tránh phụ thuộc chéo. PhanCong.nhaThau là tên tự do (không có FK), nên khi truyền
 * nhaThauId sẽ tra tên qua repository thuộc package tools rồi lọc theo tên gần đúng.
 */
@Component
@RequiredArgsConstructor
public class PhanCongToolHandlerImpl implements PhanCongToolHandler {

    private final PhanCongEntityToolRepository phanCongRepository;
    private final PhanCongToolRepository phanCongToolRepository;
    private final HopDongComponent hopDongComponent;
    private final KhuVucComponent khuVucComponent;
    private final NhaThauComponent nhaThauComponent;
    private final TinhComponent tinhComponent;
    private final CanBoComponent canBoComponent;
    private final vn.edu.huce.iic.bts_ops_platform.components.DoiTuongComponent doiTuongComponent;
    private final McpParallel parallel;

    @Override
    public PhanCongQueryResponse query(String vung, String nhaThau, String canBo,
                                       String query, String hopDong, String khuVuc, String tinhThanh, String doiTuong, String giaiDoan,
                                       Boolean lichSu, Integer topIn,
                                       Integer page, Integer pageSize,
                                       java.time.LocalDate fromDate, java.time.LocalDate toDate) {
        var f_resolvedNhaThau = parallel.async(() -> nhaThauComponent.resolve(nhaThau));
        var f_resolvedHopDong = parallel.async(() -> hopDongComponent.resolve(hopDong));
        var f_tinhThanhId = parallel.async(() -> tinhComponent.resolveId(tinhThanh));
        var f_resolvedCanBo = parallel.async(() -> canBoComponent.resolve(canBo));
        var f_resolvedDoiTuong = parallel.async(() -> doiTuongComponent.resolve(doiTuong));
        KhuVucInfo resolvedKhuVuc = khuVucComponent.resolveInScope(khuVuc);
        // các tra cứu id chạy song song (mỗi tra cứu 1-2 lần khứ hồi tới DB nếu chưa có trong bộ nhớ đệm)
        NhaThauInfo resolvedNhaThau = McpParallel.get(f_resolvedNhaThau);
        HopDongInfo resolvedHopDong = McpParallel.get(f_resolvedHopDong);
        UUID tinhThanhId = McpParallel.get(f_tinhThanhId);
        CanBoInfo resolvedCanBo = McpParallel.get(f_resolvedCanBo);
        var resolvedDoiTuong = McpParallel.get(f_resolvedDoiTuong);
        UUID doiTuongId = resolvedDoiTuong.id();
        String giaiDoanLoc = ResolveSupport.normalize(giaiDoan);
        // fromDate/toDate lọc theo ngày tạo phân công (giờ VN, gồm cả toDate); đảo lại nếu nhập ngược.
        java.time.LocalDate tuNgay = fromDate != null && toDate != null && fromDate.isAfter(toDate) ? toDate : fromDate;
        java.time.LocalDate denNgay = fromDate != null && toDate != null && fromDate.isAfter(toDate) ? fromDate : toDate;
        java.time.ZoneId zoneVn = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
        java.time.Instant ngayTaoFrom = tuNgay != null ? tuNgay.atStartOfDay(zoneVn).toInstant() : null;
        java.time.Instant ngayTaoTo = denNgay != null ? denNgay.plusDays(1).atStartOfDay(zoneVn).toInstant() : null;
        Boolean lichSuLoc = Boolean.TRUE.equals(lichSu) ? Boolean.TRUE : null;
        String tenNhaThau = resolvedNhaThau.ten();

        UUID hopDongId = resolvedHopDong.id();
        UUID nhaThauId = resolvedNhaThau.id();
        UUID khuVucId = resolvedKhuVuc.id();
        int top = topIn != null ? Math.min(Math.max(topIn, 1), 20) : 5;
        var pageable = PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE);
        boolean coDanhSach = query != null || tenNhaThau != null || vung != null || tinhThanhId != null || doiTuongId != null || giaiDoanLoc != null
                || lichSuLoc != null || ngayTaoFrom != null || ngayTaoTo != null;

        // Giai đoạn 1: các truy vấn độc lập chạy song song (độ trễ = câu chậm nhất thay vì tổng)
        var fThongKe = parallel.async(() -> phanCongRepository.thongKe(vung, ResolveSupport.likePattern(tenNhaThau), hopDongId, khuVucId, tinhThanhId, doiTuongId, giaiDoanLoc, ngayTaoFrom, ngayTaoTo));
        var fDoiTuong = parallel.async(() -> phanCongToolRepository.tongQuanTheoDoiTuong(hopDongId, nhaThauId, khuVucId, tinhThanhId, doiTuongId));
        var fKhuVucCat = parallel.async(this::khuVucTenById);
        var fTinhCat = parallel.async(this::tinhThanhTenById);
        var fSearch = coDanhSach
                ? parallel.async(() -> phanCongRepository.search(ResolveSupport.likePattern(query), vung,
                        ResolveSupport.likePattern(tenNhaThau), tinhThanhId, hopDongId, khuVucId, doiTuongId, giaiDoanLoc, lichSuLoc, ngayTaoFrom, ngayTaoTo, pageable))
                : null;
        var fCanBo = resolvedCanBo.id() != null
                ? parallel.async(() -> phanCongRepository.findActiveForContractor(resolvedCanBo.id(), resolvedCanBo.ten(), resolvedCanBo.username(), ngayTaoFrom, ngayTaoTo))
                : null;
        var fChua = parallel.async(() -> phanCongToolRepository.findChuaPhanCong(hopDongId, khuVucId, tinhThanhId, doiTuongId, pageable));
        // canBo là tài khoản nhà thầu/cán bộ được lọc → cùng cấp với xếp hạng nhà thầu nên bỏ bảng, như khi lọc nhaThau
        boolean rankDisabled = nhaThauId != null || resolvedCanBo.id() != null || doiTuongComponent.laCuThe(resolvedDoiTuong);
        var fRank = (!rankDisabled) ? parallel.async(() -> computeXepHangNhaThau(top, hopDongId, khuVucId, tinhThanhId, doiTuongId, giaiDoanLoc, vung)) : null;

        // Giai đoạn 2: bổ sung tên hợp đồng và cờ vướng mắc cho 2 danh sách (chỉ biết id sau giai đoạn 1), cũng song song
        Page<PhanCong> searchPage = fSearch != null ? McpParallel.get(fSearch) : null;
        List<PhanCong> canBoAll = fCanBo != null ? McpParallel.get(fCanBo) : null;
        int resolvedPage = PagingUtil.resolvePage(page);
        int resolvedPageSize = PagingUtil.resolvePageSize(pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE);
        List<PhanCong> canBoSlice = null;
        if (canBoAll != null) {
            int from = Math.min(resolvedPage * resolvedPageSize, canBoAll.size());
            canBoSlice = canBoAll.subList(from, Math.min(from + resolvedPageSize, canBoAll.size()));
        }
        var eDs = searchPage != null ? enrichAsync(searchPage.getContent()) : null;
        var eCb = canBoSlice != null ? enrichAsync(canBoSlice) : null;

        PhanCongToolThongKe tongQuan = buildThongKe(McpParallel.get(fThongKe));
        applyThongKeDoiTuong(tongQuan, McpParallel.get(fDoiTuong));
        Map<UUID, String> khuVucById = McpParallel.get(fKhuVucCat);
        Map<UUID, String> tinhThanhById = McpParallel.get(fTinhCat);
        PagedResult<PhanCongToolItem> danhSach = searchPage == null ? null
                : toPagedResult(searchPage.getContent(), searchPage.getNumber(), searchPage.getSize(), searchPage.getTotalElements(),
                        eDs, khuVucById, tinhThanhById);
        PagedResult<PhanCongToolItem> theoCanBo = canBoAll == null ? null
                : toPagedResult(canBoSlice, resolvedPage, resolvedPageSize, canBoAll.size(), eCb, khuVucById, tinhThanhById);
        Page<ChuaPhanCongRow> chuaPage = McpParallel.get(fChua);
        PagedResult<PhanCongChuaPhanCongToolItem> chuaPhanCong = PagedResult.of(
                chuaPage.map(PhanCongToolHandlerImpl::mapChuaPhanCong).getContent(),
                chuaPage.getNumber(), chuaPage.getSize(), chuaPage.getTotalElements());

        return PhanCongQueryResponse.builder()
                .tongQuan(tongQuan)
                .danhSach(danhSach)
                .theoCanBo(theoCanBo)
                .chuaPhanCong(chuaPhanCong)
                .xepHangNhaThau(fRank != null ? McpParallel.get(fRank) : null)
                .build();
    }

    private List<PhanCongXepHangNhaThauToolItem> computeXepHangNhaThau(int top, UUID hopDongId, UUID khuVucId,
                                                                      UUID tinhThanhId, UUID doiTuongId, String giaiDoan, String vung) {
        return phanCongToolRepository.rankNhaThau(PageRequest.of(0, top), hopDongId, khuVucId, tinhThanhId, doiTuongId, giaiDoan, vung).stream()
                .map(PhanCongToolHandlerImpl::mapXepHangNhaThau)
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

    /**
     * Ghi đè số đếm bằng thống kê theo ĐỐI TƯỢNG (nguồn chân lý hop_dong_doi_tuong, cùng nguồn với chuaPhanCong và
     * với trang Phân công của FE) thay vì đếm bản ghi bảng phan_cong.
     */
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

    /** Phần bổ sung (tên hợp đồng, cờ vướng mắc) của 1 danh sách phân công, lấy song song. */
    private record EnrichFutures(java.util.concurrent.CompletableFuture<Map<UUID, String[]>> hopDong,
                                 java.util.concurrent.CompletableFuture<Set<UUID>> coVuongMo) {
    }

    private EnrichFutures enrichAsync(List<PhanCong> content) {
        List<UUID> hopDongIds = content.stream().map(PhanCong::getHopDongId).filter(java.util.Objects::nonNull).distinct().toList();
        List<UUID> doiTuongIds = content.stream().map(PhanCong::getHopDongDoiTuongId).filter(java.util.Objects::nonNull).distinct().toList();
        var hopDong = hopDongIds.isEmpty()
                ? java.util.concurrent.CompletableFuture.completedFuture(Map.<UUID, String[]>of())
                : parallel.async(() -> {
                    Map<UUID, String[]> result = new HashMap<>();
                    for (Object[] row : phanCongToolRepository.findHopDongIdMaTenByIds(hopDongIds)) {
                        result.put((UUID) row[0], new String[]{(String) row[1], (String) row[2]});
                    }
                    return result;
                });
        var coVuongMo = doiTuongIds.isEmpty()
                ? java.util.concurrent.CompletableFuture.completedFuture(Set.<UUID>of())
                : parallel.async(() -> Set.copyOf(phanCongToolRepository.findDoiTuongIdsCoVuongDangMo(doiTuongIds)));
        return new EnrichFutures(hopDong, coVuongMo);
    }

    private PagedResult<PhanCongToolItem> toPagedResult(List<PhanCong> content, int page, int pageSize, long total,
                                                        EnrichFutures enrich, Map<UUID, String> khuVucById,
                                                        Map<UUID, String> tinhThanhById) {
        Set<UUID> coVuongMo = McpParallel.get(enrich.coVuongMo());
        Map<UUID, String[]> hopDongById = McpParallel.get(enrich.hopDong());
        List<PhanCongToolItem> items = content.stream()
                .map(entity -> toItem(entity, coVuongMo, hopDongById, khuVucById, tinhThanhById)).toList();
        return PagedResult.of(items, page, pageSize, total);
    }

    private Map<UUID, String> khuVucTenById() {
        Map<UUID, String> result = new HashMap<>();
        for (Object[] row : phanCongToolRepository.findAllKhuVucIdTen()) {
            result.put((UUID) row[0], (String) row[1]);
        }
        return result;
    }

    private Map<UUID, String> tinhThanhTenById() {
        Map<UUID, String> result = new HashMap<>();
        for (Object[] row : phanCongToolRepository.findAllTinhThanhIdTen()) {
            result.put((UUID) row[0], (String) row[1]);
        }
        return result;
    }

    private PhanCongToolItem toItem(PhanCong entity, Set<UUID> coVuongMo, Map<UUID, String[]> hopDongById,
                                     Map<UUID, String> khuVucById, Map<UUID, String> tinhThanhById) {
        PhanCongToolItem item = new PhanCongToolItem();
        item.setId(entity.getId());
        String[] hopDong = hopDongById.get(entity.getHopDongId());
        item.setHopDong(HopDongInfo.of(entity.getHopDongId(),
                hopDong != null ? hopDong[0] : null, hopDong != null ? hopDong[1] : null));
        item.setHopDongDoiTuongId(entity.getHopDongDoiTuongId());
        item.setNhaThau(entity.getNhaThau());
        item.setGiaiDoan(entity.getGiaiDoan());
        item.setKhuVuc(GeoRefResponse.of(entity.getKhuVucId(), null, khuVucById.get(entity.getKhuVucId())));
        item.setMaVung(entity.getMaVung());
        item.setTinhThanh(GeoRefResponse.of(entity.getTinhThanhId(), null, tinhThanhById.get(entity.getTinhThanhId())));
        item.setHoatDong(entity.getHoatDong());
        item.setNgayTao(entity.getNgayTao());
        item.setNgayCapNhat(entity.getNgayCapNhat());
        item.setNgayXoa(entity.getNgayXoa());
        item.setCoVuongMacMo(entity.getHopDongDoiTuongId() != null && coVuongMo.contains(entity.getHopDongDoiTuongId()));
        return item;
    }
}
