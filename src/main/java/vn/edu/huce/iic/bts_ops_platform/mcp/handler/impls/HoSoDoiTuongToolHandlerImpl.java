package vn.edu.huce.iic.bts_ops_platform.mcp.handler.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.doituong.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong.HoSoDoiTuongChuaKhaoSatItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong.HoSoDoiTuongKhaoSatXongChuaCoSanLuongItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong.HoSoDoiTuongKhoiCongItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong.HoSoDoiTuongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong.HoSoDoiTuongThuocTinhDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong.HoSoDoiTuongTongQuanDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong.ChuaKhaoSatProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong.KhaoSatXongChuaCoSanLuongProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong.KhoiCongTrongKyProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.handler.HoSoDoiTuongToolHandler;
import vn.edu.huce.iic.bts_ops_platform.mcp.repository.HoSoDoiTuongToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PagedResult;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/** Impl khối hoSoDoiTuong của tool AI doituong_tool (trước là hosodoituong_tool) — 1 method duy nhất, chỉ gọi HoSoDoiTuongToolRepository. */
@Component
@RequiredArgsConstructor
public class HoSoDoiTuongToolHandlerImpl implements HoSoDoiTuongToolHandler {

    private final HoSoDoiTuongToolRepository repository;
    private final DoiTuongComponent doiTuongComponent;
    private final HopDongComponent hopDongComponent;
    private final NhaThauComponent nhaThauComponent;
    private final KhuVucComponent khuVucComponent;
    private final TinhComponent tinhComponent;
    private final DanhMucComponent danhMucComponent;
    private final McpParallel parallel;

    @Override
    public HoSoDoiTuongQueryResponse query(String doiTuong, String hopDong, String nhaThau, String khuVuc, String tinhThanh,
                                       String trangThaiHopDong, Boolean coNhomUuTien,
                                       LocalDate fromDate, LocalDate toDate, Integer page, Integer pageSize) {
        var f_resolvedHopDong = parallel.async(() -> hopDongComponent.resolve(hopDong));
        var f_resolvedDoiTuong = parallel.async(() -> doiTuongComponent.resolve(doiTuong));
        var f_nhaThau = parallel.async(() -> nhaThauComponent.resolve(nhaThau));
        var f_tinh = parallel.async(() -> tinhComponent.resolveId(tinhThanh));
        var f_trangThai = parallel.async(() -> danhMucComponent.resolveTrangThaiHopDongIds(trangThaiHopDong));
        UUID khuVucId = khuVucComponent.resolveInScope(khuVuc).id();
        // các tra cứu id chạy song song (mỗi tra cứu 1-2 lần khứ hồi tới DB nếu chưa có trong bộ nhớ đệm)
        HopDongInfo resolvedHopDong = McpParallel.get(f_resolvedHopDong);
        DoiTuongInfo resolvedDoiTuong = McpParallel.get(f_resolvedDoiTuong);
        UUID hopDongId = resolvedHopDong.id();
        UUID doiTuongId = resolvedDoiTuong.id();
        Loc loc = new Loc(McpParallel.get(f_nhaThau).id(), khuVucId, McpParallel.get(f_tinh), McpParallel.get(f_trangThai), coNhomUuTien);

        // Đảo lại nếu nhập ngược khoảng ngày
        LocalDate tuNgay = fromDate != null && toDate != null && fromDate.isAfter(toDate) ? toDate : fromDate;
        LocalDate denNgay = fromDate != null && toDate != null && fromDate.isAfter(toDate) ? fromDate : toDate;

        // 5 khối độc lập chạy song song
        boolean coLoc = hopDongId != null || loc.coGiaTri();
        var fTongQuan = coLoc ? parallel.async(() -> computeTongQuan(hopDongId, loc)) : null;
        var fThuocTinh = doiTuongId != null ? parallel.async(() -> computeThuocTinh(doiTuongId, hopDongId)) : null;
        var fChuaKhaoSat = coLoc ? parallel.async(() -> computeChuaKhaoSat(hopDongId, loc, page, pageSize)) : null;
        // Hỏi 1 đối tượng cụ thể thì đã có thuocTinh (ngày bàn giao, ngayKhoiCong) — không liệt kê danh sách toàn hệ thống
        boolean motDoiTuong = doiTuongId != null;
        var fKhaoSatXong = !motDoiTuong ? parallel.async(() -> computeKhaoSatXongChuaCoSanLuong(hopDongId, loc, tuNgay, denNgay, page, pageSize)) : null;
        var fKhoiCong = !motDoiTuong ? parallel.async(() -> computeKhoiCongTrongKy(hopDongId, loc, tuNgay, denNgay, page, pageSize)) : null;

        HoSoDoiTuongTongQuanDto tongQuan = fTongQuan != null ? McpParallel.get(fTongQuan) : null;
        HoSoDoiTuongThuocTinhDto thuocTinh = fThuocTinh != null ? McpParallel.get(fThuocTinh) : null;
        PagedResult<HoSoDoiTuongChuaKhaoSatItem> chuaKhaoSat = fChuaKhaoSat != null ? McpParallel.get(fChuaKhaoSat) : null;
        PagedResult<HoSoDoiTuongKhaoSatXongChuaCoSanLuongItem> khaoSatXongChuaCoSanLuong = fKhaoSatXong != null ? McpParallel.get(fKhaoSatXong) : null;
        PagedResult<HoSoDoiTuongKhoiCongItem> khoiCongTrongKy = fKhoiCong != null ? McpParallel.get(fKhoiCong) : null;

        return HoSoDoiTuongQueryResponse.builder()
                .tongQuan(tongQuan)
                .thuocTinh(thuocTinh)
                .chuaKhaoSat(chuaKhaoSat)
                .khaoSatXongChuaCoSanLuong(khaoSatXongChuaCoSanLuong)
                .khoiCongTrongKy(khoiCongTrongKy)
                .build();
    }

    /** Bộ lọc cấp đối tượng đã resolve thành id (null = không lọc). */
    private record Loc(UUID nhaThauId, UUID khuVucId, UUID tinhThanhId, String trangThaiIds, Boolean coNhomUuTien) {
        boolean coGiaTri() {
            return nhaThauId != null || khuVucId != null || tinhThanhId != null || trangThaiIds != null || coNhomUuTien != null;
        }
    }

    private HoSoDoiTuongTongQuanDto computeTongQuan(UUID hopDongId, Loc loc) {
        var row = repository.tongQuan(hopDongId, loc.nhaThauId(), loc.khuVucId(), loc.tinhThanhId(), loc.trangThaiIds(), loc.coNhomUuTien());
        if (row == null || row.getTongDoiTuong() == null) {
            return HoSoDoiTuongTongQuanDto.builder()
                    .tongDoiTuong(0).thieuBanGiaoMatBang(0).vatTuAChuaDamBao(0).vatTuBChuaHoanThanh(0)
                    .build();
        }
        return HoSoDoiTuongTongQuanDto.builder()
                .tongDoiTuong(row.getTongDoiTuong())
                .thieuBanGiaoMatBang(row.getThieuBanGiaoMatBang() != null ? row.getThieuBanGiaoMatBang() : 0)
                .vatTuAChuaDamBao(row.getVatTuAChuaDamBao() != null ? row.getVatTuAChuaDamBao() : 0)
                .vatTuBChuaHoanThanh(row.getVatTuBChuaHoanThanh() != null ? row.getVatTuBChuaHoanThanh() : 0)
                .build();
    }

    private HoSoDoiTuongThuocTinhDto computeThuocTinh(UUID doiTuongId, UUID hopDongId) {
        var row = repository.thuocTinh(doiTuongId, hopDongId);
        if (row == null) {
            return null;
        }
        return HoSoDoiTuongThuocTinhDto.builder()
                .doiTuong(DoiTuongInfo.of(row.getMaDoiTuong(), row.getTenDoiTuong()))
                .hopDong(HopDongInfo.of(null, row.getMaHopDong(), row.getTenHopDong()))
                .ngayBanGiaoMatBang(row.getNgayBanGiaoMatBang())
                .trangThaiVatTuA(StatusLabels.vatTuA(row.getTrangThaiVatTuA()))
                .trangThaiVatTuB(StatusLabels.vatTuB(row.getTrangThaiVatTuB()))
                .ngayYeuCauVatTuB(row.getNgayYeuCauVatTuB())
                .ngayHoanThanhVatTuB(row.getNgayHoanThanhVatTuB())
                .ngayKhoiCong(row.getNgayKhoiCong())
                .build();
    }

    private PagedResult<HoSoDoiTuongChuaKhaoSatItem> computeChuaKhaoSat(UUID hopDongId, Loc loc, Integer page, Integer pageSize) {
        Page<ChuaKhaoSatProjection> resultPage = repository.chuaKhaoSat(hopDongId,
                loc.nhaThauId(), loc.khuVucId(), loc.tinhThanhId(), loc.trangThaiIds(), loc.coNhomUuTien(),
                PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE));
        List<HoSoDoiTuongChuaKhaoSatItem> items = resultPage.map(row -> {
            HoSoDoiTuongChuaKhaoSatItem item = new HoSoDoiTuongChuaKhaoSatItem();
            item.setDoiTuong(DoiTuongInfo.of(row.getMaDoiTuong(), row.getTenDoiTuong()));
            item.setKhuVuc(row.getKhuVuc());
            item.setNhaThau(row.getNhaThau());
            return item;
        }).getContent();
        return PagedResult.of(items, resultPage.getNumber(), resultPage.getSize(), resultPage.getTotalElements());
    }

    private PagedResult<HoSoDoiTuongKhaoSatXongChuaCoSanLuongItem> computeKhaoSatXongChuaCoSanLuong(UUID hopDongId, Loc loc,
                                                                                                    LocalDate fromDate, LocalDate toDate,
                                                                                                    Integer page, Integer pageSize) {
        LocalDate today = LocalDate.now();
        Page<KhaoSatXongChuaCoSanLuongProjection> resultPage = repository.khaoSatXongChuaCoSanLuong(
                hopDongId, loc.nhaThauId(), loc.khuVucId(), loc.tinhThanhId(), loc.trangThaiIds(), loc.coNhomUuTien(), fromDate, toDate, PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE));
        List<HoSoDoiTuongKhaoSatXongChuaCoSanLuongItem> items = resultPage.map(row -> {
            HoSoDoiTuongKhaoSatXongChuaCoSanLuongItem item = new HoSoDoiTuongKhaoSatXongChuaCoSanLuongItem();
            item.setDoiTuong(DoiTuongInfo.of(row.getMaDoiTuong(), row.getTenDoiTuong()));
            item.setMaHopDong(row.getMaHopDong());
            item.setNgayBanGiaoMatBang(row.getNgayBanGiaoMatBang());
            item.setSoNgayKeTuBanGiao(row.getNgayBanGiaoMatBang() != null
                    ? ChronoUnit.DAYS.between(row.getNgayBanGiaoMatBang(), today) : 0);
            return item;
        }).getContent();
        return PagedResult.of(items, resultPage.getNumber(), resultPage.getSize(), resultPage.getTotalElements());
    }

    private PagedResult<HoSoDoiTuongKhoiCongItem> computeKhoiCongTrongKy(UUID hopDongId, Loc loc,
                                                                         LocalDate fromDate, LocalDate toDate,
                                                                         Integer page, Integer pageSize) {
        Page<KhoiCongTrongKyProjection> resultPage = repository.khoiCongTrongKy(
                hopDongId, loc.nhaThauId(), loc.khuVucId(), loc.tinhThanhId(), loc.trangThaiIds(), loc.coNhomUuTien(), fromDate, toDate,
                PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE));
        List<HoSoDoiTuongKhoiCongItem> items = resultPage.map(row -> {
            HoSoDoiTuongKhoiCongItem item = new HoSoDoiTuongKhoiCongItem();
            item.setDoiTuong(DoiTuongInfo.of(row.getMaDoiTuong(), row.getTenDoiTuong()));
            item.setMaHopDong(row.getMaHopDong());
            item.setKhuVuc(row.getKhuVuc());
            item.setNhaThau(row.getNhaThau());
            item.setNgayKhoiCong(row.getNgayKhoiCong());
            return item;
        }).getContent();
        return PagedResult.of(items, resultPage.getNumber(), resultPage.getSize(), resultPage.getTotalElements());
    }
}
