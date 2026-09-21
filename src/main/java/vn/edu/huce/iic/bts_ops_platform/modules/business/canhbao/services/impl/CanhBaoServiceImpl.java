package vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.constants.CanhBaoNguongLoai;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.dto.request.CanhBaoNguongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.dto.request.SanLuongDaKiemTraRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.dto.response.TyLeHuyTramItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.dto.response.TyLeHuyTramResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.entity.CanhBaoNguongCauHinh;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.entity.SanLuongCanhBaoKiemTra;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.repository.CanhBaoNguongCauHinhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.repository.SanLuongCanhBaoKiemTraRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.repository.TyLeHuyTramStatsRepository;
import vn.edu.huce.iic.bts_ops_platform.common.dto.TyLeHuyRow;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.services.CanhBaoService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.realtime.services.RealtimeEventService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.constants.ThongBaoLoai;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.services.ThongBaoDispatchService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.KpiCanhBaoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.service.TienDoService;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CanhBaoServiceImpl implements CanhBaoService {

    private static final BigDecimal DEFAULT_TY_LE_HUY_NGUONG = BigDecimal.valueOf(15);

    private final CanhBaoNguongCauHinhRepository nguongRepository;
    private final TyLeHuyTramStatsRepository tyLeHuyTramStatsRepository;
    private final SanLuongCanhBaoKiemTraRepository kiemTraRepository;
    private final ThongBaoDispatchService thongBaoDispatchService;
    private final TienDoService tienDoService;
    private final RealtimeEventService realtimeEventService;

    @Override
    @Transactional(readOnly = true)
    public TyLeHuyTramResponse tyLeHuyTram() {
        BigDecimal nguong = getNguongTyLeHuy();
        return TyLeHuyTramResponse.builder()
                .nguong(nguong)
                .theoCanBo(mapRows(tyLeHuyTramStatsRepository.thongKeTheoCanBo(), nguong))
                .theoKhuVuc(mapRows(tyLeHuyTramStatsRepository.thongKeTheoKhuVuc(), nguong))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getNguongTyLeHuy() {
        return nguongRepository.findByLoaiAndHoatDongTrue(CanhBaoNguongLoai.TY_LE_HUY_TRAM)
                .map(CanhBaoNguongCauHinh::getGiaTriNguong)
                .orElse(DEFAULT_TY_LE_HUY_NGUONG);
    }

    @Override
    @Transactional
    public BigDecimal capNhatNguongTyLeHuy(CanhBaoNguongCapNhatRequest request) {
        CanhBaoNguongCauHinh entity = nguongRepository.findByLoaiAndHoatDongTrue(CanhBaoNguongLoai.TY_LE_HUY_TRAM)
                .orElseGet(() -> {
                    CanhBaoNguongCauHinh created = new CanhBaoNguongCauHinh();
                    created.setLoai(CanhBaoNguongLoai.TY_LE_HUY_TRAM);
                    created.setHoatDong(true);
                    created.setNgayTao(Instant.now());
                    return created;
                });
        entity.setGiaTriNguong(request.getGiaTriNguong());
        entity.setNgayCapNhat(Instant.now());
        nguongRepository.save(entity);
        realtimeEventService.broadcastDashboardRefresh();
        return entity.getGiaTriNguong();
    }

    @Override
    @Transactional
    public void danhDauSanLuongDaKiemTra(UUID hopDongDoiTuongId, SanLuongDaKiemTraRequest request) {
        JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
        SanLuongCanhBaoKiemTra entity = kiemTraRepository.findByHopDongDoiTuongId(hopDongDoiTuongId)
                .orElseGet(SanLuongCanhBaoKiemTra::new);
        entity.setHopDongDoiTuongId(hopDongDoiTuongId);
        entity.setNguoiKiemTraId(user.id());
        entity.setNguoiKiemTraTen(user.hoTen());
        entity.setNgayKiemTra(Instant.now());
        if (request != null && request.getGhiChu() != null) {
            entity.setGhiChu(request.getGhiChu());
        }
        kiemTraRepository.save(entity);
        realtimeEventService.broadcastDashboardRefresh();
    }

    @Override
    @Transactional
    public void quetCanhBaoTuDong() {
        BigDecimal nguong = getNguongTyLeHuy();
        List<TyLeHuyTramItemResponse> vuotNguong = new ArrayList<>();
        vuotNguong.addAll(mapRows(tyLeHuyTramStatsRepository.thongKeTheoCanBo(), nguong));
        vuotNguong.addAll(mapRows(tyLeHuyTramStatsRepository.thongKeTheoKhuVuc(), nguong));
        for (TyLeHuyTramItemResponse item : vuotNguong) {
            if (!item.isVuotNguong()) {
                continue;
            }
            thongBaoDispatchService.notifyUsersWithQuyenHan(
                    QuyenHanMa.QUAN_LY_HOP_DONG,
                    ThongBaoLoai.CANH_BAO_TY_LE_HUY,
                    "Tỷ lệ hủy trạm vượt ngưỡng",
                    item.getGroupTen() + " — " + item.getTyLeHuy() + "% (ngưỡng " + nguong + "%)",
                    "/dashboard",
                    null);
        }

        for (KpiCanhBaoResponse alert : tienDoService.listKpiCanhBao()) {
            if (alert.getUuTien() == null || alert.getUuTien() > 2) {
                continue;
            }
            String noiDung = (alert.getKpiTen() != null ? alert.getKpiTen() : "KPI")
                    + " — vượt " + (alert.getSoNgayVuot() != null ? alert.getSoNgayVuot() : 0) + " ngày";
            thongBaoDispatchService.notifyUsersWithQuyenHan(
                    QuyenHanMa.QUAN_LY_HOP_DONG,
                    ThongBaoLoai.DEADLINE_KPI,
                    "Cảnh báo KPI tiến độ",
                    noiDung,
                    "/dashboard",
                    alert.getHopDongDoiTuongId());
        }

        realtimeEventService.broadcastDashboardRefresh();
    }

    private List<TyLeHuyTramItemResponse> mapRows(
            List<TyLeHuyRow> rows,
            BigDecimal nguong) {
        List<TyLeHuyTramItemResponse> result = new ArrayList<>();
        for (TyLeHuyRow row : rows) {
            long tong = row.getTongTram();
            long huy = row.getSoTramHuy();
            BigDecimal tyLe = tong <= 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(huy * 100.0 / tong).setScale(2, RoundingMode.HALF_UP);
            boolean vuot = tyLe.compareTo(nguong) > 0;
            result.add(TyLeHuyTramItemResponse.builder()
                    .groupKey(row.getGroupKey())
                    .groupTen(row.getGroupTen())
                    .groupLoai(row.getGroupLoai())
                    .tongTram(tong)
                    .soTramHuy(huy)
                    .tyLeHuy(tyLe)
                    .vuotNguong(vuot)
                    .nguong(nguong)
                    .build());
        }
        return result;
    }
}
