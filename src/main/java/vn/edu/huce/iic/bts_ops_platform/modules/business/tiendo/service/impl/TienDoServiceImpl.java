package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.security.AuthorityPrefix;
import vn.edu.huce.iic.bts_ops_platform.common.security.FullAccessRoleCodes;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.constants.ThongBaoLoai;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.services.ThongBaoDispatchService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.constants.TienDoConstants;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.ChecklistDamBaoCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.DoiTuongChecklistDapUngCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.DoiTuongTienDoCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.HopDongChecklistSyncRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.KeHoachImportChiTietRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.KeHoachTrienKhaiTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.KeHoachTuChoiRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.KpiNguongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.VatTuTrangThaiCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.VatTuYeuCauTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.ChecklistDamBaoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.ChecklistMucResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.DoiTuongChecklistDapUngResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.DoiTuongChecklistMucTrangThaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.HopDongChecklistResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.DoiTuongTienDoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.KeHoachTrienKhaiChiTietResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.KeHoachTrienKhaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.KpiCanhBaoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.KpiNguongCauHinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.VatTuYeuCauResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity.HopDongChecklistMuc;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity.HopDongDoiTuongChecklistDapUng;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity.KeHoachTrienKhai;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity.KeHoachTrienKhaiChiTiet;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity.KpiNguongCauHinh;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity.VatTuYeuCau;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.exception.TienDoErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.repository.HopDongChecklistMucRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.repository.HopDongDoiTuongChecklistDapUngRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.repository.KeHoachTrienKhaiChiTietRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.repository.KeHoachTrienKhaiRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.repository.KpiNguongCauHinhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.repository.VatTuYeuCauRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.service.TienDoService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TienDoServiceImpl implements TienDoService {

    private final HopDongDoiTuongRepository hopDongDoiTuongRepository;
    private final HopDongRepository hopDongRepository;
    private final KeHoachTrienKhaiRepository keHoachTrienKhaiRepository;
    private final KeHoachTrienKhaiChiTietRepository keHoachTrienKhaiChiTietRepository;
    private final VatTuYeuCauRepository vatTuYeuCauRepository;
    private final KpiNguongCauHinhRepository kpiNguongCauHinhRepository;
    private final HopDongChecklistMucRepository hopDongChecklistMucRepository;
    private final HopDongDoiTuongChecklistDapUngRepository hopDongDoiTuongChecklistDapUngRepository;
    private final ThongBaoDispatchService thongBaoDispatchService;

    @Override
    @Transactional(readOnly = true)
    public List<KpiCanhBaoResponse> listKpiCanhBao() {
        Map<String, KpiNguongCauHinh> configs = kpiNguongCauHinhRepository.findByHoatDongTrueOrderByUuTienAscMaAsc()
                .stream()
                .collect(Collectors.toMap(KpiNguongCauHinh::getMa, Function.identity()));

        KpiNguongCauHinh banGiaoConfig = configs.get(TienDoConstants.KPI_BAN_GIAO_HT);
        KpiNguongCauHinh vatTuConfig = configs.get(TienDoConstants.KPI_VAT_TU_B);

        List<KpiCanhBaoResponse> alerts = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (HopDongDoiTuong entity : hopDongDoiTuongRepository.findActiveForKpiCanhBao()) {
            if (banGiaoConfig != null && entity.getNgayBanGiaoMatBang() != null) {
                LocalDate endDate = entity.getNgayHtTc() != null ? entity.getNgayHtTc() : today;
                int days = (int) ChronoUnit.DAYS.between(entity.getNgayBanGiaoMatBang(), endDate);
                if (days > banGiaoConfig.getSoNgayNguong()) {
                    alerts.add(buildKpiAlert(entity, banGiaoConfig, days));
                }
            }

            if (vatTuConfig != null
                    && entity.getNgayYeuCauVatTuB() != null
                    && !TienDoConstants.TRANG_THAI_VAT_TU_DA_NHAN.equals(entity.getTrangThaiVatTuB())) {
                LocalDate endDate = entity.getNgayHoanThanhVatTuB() != null
                        ? entity.getNgayHoanThanhVatTuB()
                        : today;
                int days = (int) ChronoUnit.DAYS.between(entity.getNgayYeuCauVatTuB(), endDate);
                if (days > vatTuConfig.getSoNgayNguong()) {
                    alerts.add(buildKpiAlert(entity, vatTuConfig, days));
                }
            }
        }

        alerts.sort(Comparator
                .comparing(KpiCanhBaoResponse::getUuTien)
                .thenComparing(KpiCanhBaoResponse::getSoNgayVuot, Comparator.reverseOrder()));
        return alerts;
    }

    @Override
    @Transactional(readOnly = true)
    public DoiTuongTienDoResponse getDoiTuongTienDo(UUID id) {
        return toDoiTuongResponse(requireDoiTuong(id));
    }

    @Override
    @Transactional
    public DoiTuongTienDoResponse capNhatDoiTuongTienDo(UUID id, DoiTuongTienDoCapNhatRequest request) {
        HopDongDoiTuong entity = requireDoiTuong(id);
        assertAdminForDateChanges(entity, request);

        applyIfPresent(request.getNgayBanGiaoMatBang(), entity::setNgayBanGiaoMatBang);
        applyIfPresent(request.getTrangThaiVcontractBgm(), entity::setTrangThaiVcontractBgm);
        applyIfPresent(request.getNgayYeuCauVatTuB(), entity::setNgayYeuCauVatTuB);
        applyIfPresent(request.getNgayHoanThanhVatTuB(), entity::setNgayHoanThanhVatTuB);
        applyIfPresent(request.getTrangThaiVatTuA(), entity::setTrangThaiVatTuA);
        applyIfPresent(request.getTrangThaiVatTuB(), entity::setTrangThaiVatTuB);
        applyIfPresent(request.getCoPhatSinh(), entity::setCoPhatSinh);
        applyIfPresent(request.getTrangThaiNghiemThu(), entity::setTrangThaiNghiemThu);
        applyIfPresent(request.getNgayNghiemThu(), entity::setNgayNghiemThu);
        applyIfPresent(request.getDaChotTham(), entity::setDaChotTham);
        applyIfPresent(request.getGiaTriChotTham(), entity::setGiaTriChotTham);
        applyIfPresent(request.getDaGiaoThauPhu(), entity::setDaGiaoThauPhu);
        applyIfPresent(request.getDaKyHdThauPhu(), entity::setDaKyHdThauPhu);

        lockDoiTuongWhenConstructionStarts(entity);
        return toDoiTuongResponse(hopDongDoiTuongRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ChecklistDamBaoResponse getChecklistDamBao(UUID hopDongId) {
        return toChecklistResponse(requireHopDong(hopDongId));
    }

    @Override
    @Transactional
    public ChecklistDamBaoResponse capNhatChecklistDamBao(UUID hopDongId, ChecklistDamBaoCapNhatRequest request) {
        HopDong hopDong = requireHopDong(hopDongId);
        applyIfPresent(request.getChecklistDoiTac(), hopDong::setChecklistDoiTac);
        applyIfPresent(request.getChecklistCcdc(), hopDong::setChecklistCcdc);
        applyIfPresent(request.getChecklistAtld(), hopDong::setChecklistAtld);
        applyIfPresent(request.getChecklistVatTuA(), hopDong::setChecklistVatTuA);
        applyIfPresent(request.getChecklistVatTuB(), hopDong::setChecklistVatTuB);
        applyIfPresent(request.getChecklistGhiChu(), hopDong::setChecklistGhiChu);
        return toChecklistResponse(hopDongRepository.save(hopDong));
    }

    @Override
    @Transactional(readOnly = true)
    public HopDongChecklistResponse getHopDongChecklist(UUID hopDongId) {
        HopDong hopDong = requireHopDong(hopDongId);
        List<ChecklistMucResponse> muc = hopDongChecklistMucRepository
                .findByHopDongIdAndNgayXoaIsNullOrderByThuTuAscTenAsc(hopDongId)
                .stream()
                .map(this::toChecklistMucResponse)
                .toList();
        return HopDongChecklistResponse.builder()
                .hopDongId(hopDongId)
                .ghiChuHopDong(hopDong.getChecklistGhiChu())
                .mucDanhSach(muc)
                .build();
    }

    @Override
    @Transactional
    public HopDongChecklistResponse syncHopDongChecklist(UUID hopDongId, HopDongChecklistSyncRequest request) {
        HopDong hopDong = requireHopDong(hopDongId);
        if (request.getGhiChuHopDong() != null) {
            hopDong.setChecklistGhiChu(request.getGhiChuHopDong());
            hopDongRepository.save(hopDong);
        }

        List<HopDongChecklistMuc> existing = hopDongChecklistMucRepository
                .findByHopDongIdAndNgayXoaIsNullOrderByThuTuAscTenAsc(hopDongId);
        Set<UUID> keepIds = request.getMucDanhSach() == null
                ? Set.of()
                : request.getMucDanhSach().stream()
                        .map(HopDongChecklistSyncRequest.MucItem::getId)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toSet());

        Instant now = Instant.now();
        for (HopDongChecklistMuc muc : existing) {
            if (!keepIds.contains(muc.getId())) {
                muc.setNgayXoa(now);
                hopDongChecklistMucRepository.save(muc);
            }
        }

        if (request.getMucDanhSach() != null) {
            int order = 0;
            for (HopDongChecklistSyncRequest.MucItem item : request.getMucDanhSach()) {
                order += 1;
                HopDongChecklistMuc entity = item.getId() == null
                        ? null
                        : hopDongChecklistMucRepository.findByIdAndNgayXoaIsNull(item.getId()).orElse(null);
                if (entity == null) {
                    entity = new HopDongChecklistMuc();
                    entity.setHopDongId(hopDongId);
                }
                entity.setTen(item.getTen().trim());
                entity.setThuTu(item.getThuTu() != null ? item.getThuTu() : order);
                entity.setGhiChu(item.getGhiChu());
                hopDongChecklistMucRepository.save(entity);
            }
        }

        return getHopDongChecklist(hopDongId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoiTuongChecklistDapUngResponse> listDoiTuongChecklistDapUng(UUID hopDongId, List<UUID> doiTuongIds) {
        requireHopDong(hopDongId);
        List<HopDongChecklistMuc> mucList = hopDongChecklistMucRepository
                .findByHopDongIdAndNgayXoaIsNullOrderByThuTuAscTenAsc(hopDongId);
        if (mucList.isEmpty() || doiTuongIds == null || doiTuongIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, Map<UUID, HopDongDoiTuongChecklistDapUng>> dapUngByDoiTuong = hopDongDoiTuongChecklistDapUngRepository
                .findByHopDongDoiTuongIdInAndNgayXoaIsNull(doiTuongIds)
                .stream()
                .collect(Collectors.groupingBy(
                        HopDongDoiTuongChecklistDapUng::getHopDongDoiTuongId,
                        Collectors.toMap(HopDongDoiTuongChecklistDapUng::getChecklistMucId, Function.identity(), (a, b) -> a)));

        List<DoiTuongChecklistDapUngResponse> result = new ArrayList<>();
        for (UUID doiTuongId : doiTuongIds) {
            Map<UUID, HopDongDoiTuongChecklistDapUng> byMuc = dapUngByDoiTuong.getOrDefault(doiTuongId, Map.of());
            List<DoiTuongChecklistMucTrangThaiResponse> items = mucList.stream()
                    .map(muc -> {
                        HopDongDoiTuongChecklistDapUng dapUng = byMuc.get(muc.getId());
                        return DoiTuongChecklistMucTrangThaiResponse.builder()
                                .checklistMucId(muc.getId())
                                .ten(muc.getTen())
                                .thuTu(muc.getThuTu())
                                .daDuyet(dapUng != null && Boolean.TRUE.equals(dapUng.getDaDuyet()))
                                .ngayDuyet(dapUng != null ? dapUng.getNgayDuyet() : null)
                                .build();
                    })
                    .toList();
            result.add(DoiTuongChecklistDapUngResponse.builder()
                    .hopDongDoiTuongId(doiTuongId)
                    .mucDanhSach(items)
                    .build());
        }
        return result;
    }

    @Override
    @Transactional
    public DoiTuongChecklistDapUngResponse capNhatDoiTuongChecklistDapUng(
            UUID doiTuongId,
            DoiTuongChecklistDapUngCapNhatRequest request) {
        HopDongDoiTuong doiTuong = requireDoiTuong(doiTuongId);
        HopDongChecklistMuc muc = hopDongChecklistMucRepository.findByIdAndNgayXoaIsNull(request.getChecklistMucId())
                .orElseThrow(() -> new AppException(TienDoErrorCode.CHECKLIST_MUC_NOT_FOUND));
        if (!doiTuong.getHopDongId().equals(muc.getHopDongId())) {
            throw new AppException(TienDoErrorCode.CHECKLIST_MUC_NOT_FOUND);
        }

        HopDongDoiTuongChecklistDapUng entity = hopDongDoiTuongChecklistDapUngRepository
                .findByHopDongDoiTuongIdAndChecklistMucIdAndNgayXoaIsNull(doiTuongId, request.getChecklistMucId())
                .orElseGet(() -> {
                    HopDongDoiTuongChecklistDapUng created = new HopDongDoiTuongChecklistDapUng();
                    created.setHopDongDoiTuongId(doiTuongId);
                    created.setChecklistMucId(request.getChecklistMucId());
                    return created;
                });

        entity.setDaDuyet(request.getDaDuyet());
        entity.setGhiChu(request.getGhiChu());
        if (Boolean.TRUE.equals(request.getDaDuyet())) {
            JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
            entity.setNgayDuyet(Instant.now());
            entity.setNguoiDuyetId(user.id());
        } else {
            entity.setNgayDuyet(null);
            entity.setNguoiDuyetId(null);
        }
        hopDongDoiTuongChecklistDapUngRepository.save(entity);

        return listDoiTuongChecklistDapUng(doiTuong.getHopDongId(), List.of(doiTuongId)).stream()
                .findFirst()
                .orElse(DoiTuongChecklistDapUngResponse.builder()
                        .hopDongDoiTuongId(doiTuongId)
                        .mucDanhSach(List.of())
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KeHoachTrienKhaiResponse> listKeHoach(UUID hopDongId) {
        List<KeHoachTrienKhai> plans = hopDongId == null
                ? keHoachTrienKhaiRepository.findByNgayXoaIsNullOrderByNgayTaoDesc()
                : keHoachTrienKhaiRepository.findByHopDongIdAndNgayXoaIsNullOrderByNgayTaoDesc(hopDongId);
        return plans.stream().map(this::toKeHoachResponse).toList();
    }

    @Override
    @Transactional
    public KeHoachTrienKhaiResponse taoKeHoach(KeHoachTrienKhaiTaoRequest request) {
        requireHopDong(request.getHopDongId());
        JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();

        KeHoachTrienKhai entity = new KeHoachTrienKhai();
        entity.setHopDongId(request.getHopDongId());
        entity.setTen(request.getTen());
        entity.setTrangThai(TienDoConstants.KE_HOACH_NHAP);
        entity.setNguoiLapId(user.id());
        return toKeHoachResponse(keHoachTrienKhaiRepository.save(entity));
    }

    @Override
    @Transactional
    public KeHoachTrienKhaiResponse guiKeHoachDuyet(UUID id) {
        KeHoachTrienKhai entity = requireKeHoach(id);
        if (!TienDoConstants.KE_HOACH_NHAP.equals(entity.getTrangThai())
                && !TienDoConstants.KE_HOACH_TU_CHOI.equals(entity.getTrangThai())) {
            throw new AppException(TienDoErrorCode.KE_HOACH_TRANG_THAI_INVALID);
        }
        entity.setTrangThai(TienDoConstants.KE_HOACH_CHO_DUYET);
        entity.setNgayGui(Instant.now());
        entity.setLyDoTuChoi(null);
        KeHoachTrienKhaiResponse saved = toKeHoachResponse(keHoachTrienKhaiRepository.save(entity));
        thongBaoDispatchService.notifyUsersWithQuyenHan(
                QuyenHanMa.QUAN_LY_HOP_DONG,
                ThongBaoLoai.WORKFLOW,
                "Kế hoạch chờ duyệt",
                "Kế hoạch \"" + entity.getTen() + "\" đang chờ duyệt",
                "/stations",
                entity.getId());
        return saved;
    }

    @Override
    @Transactional
    public KeHoachTrienKhaiResponse duyetKeHoach(UUID id) {
        KeHoachTrienKhai entity = requireKeHoach(id);
        if (!TienDoConstants.KE_HOACH_CHO_DUYET.equals(entity.getTrangThai())) {
            throw new AppException(TienDoErrorCode.KE_HOACH_TRANG_THAI_INVALID);
        }
        JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
        entity.setTrangThai(TienDoConstants.KE_HOACH_DA_DUYET);
        entity.setNguoiDuyetId(user.id());
        entity.setNgayDuyet(Instant.now());
        KeHoachTrienKhaiResponse saved = toKeHoachResponse(keHoachTrienKhaiRepository.save(entity));
        if (entity.getNguoiLapId() != null) {
            thongBaoDispatchService.notifyUser(
                    entity.getNguoiLapId(),
                    ThongBaoLoai.WORKFLOW,
                    "Kế hoạch đã duyệt",
                    "Kế hoạch \"" + entity.getTen() + "\" đã được duyệt",
                    "/stations",
                    entity.getId());
        }
        return saved;
    }

    @Override
    @Transactional
    public KeHoachTrienKhaiResponse tuChoiKeHoach(UUID id, KeHoachTuChoiRequest request) {
        KeHoachTrienKhai entity = requireKeHoach(id);
        if (!TienDoConstants.KE_HOACH_CHO_DUYET.equals(entity.getTrangThai())) {
            throw new AppException(TienDoErrorCode.KE_HOACH_TRANG_THAI_INVALID);
        }
        entity.setTrangThai(TienDoConstants.KE_HOACH_TU_CHOI);
        entity.setLyDoTuChoi(request != null ? request.getLyDoTuChoi() : null);
        KeHoachTrienKhaiResponse saved = toKeHoachResponse(keHoachTrienKhaiRepository.save(entity));
        if (entity.getNguoiLapId() != null) {
            thongBaoDispatchService.notifyUser(
                    entity.getNguoiLapId(),
                    ThongBaoLoai.WORKFLOW,
                    "Kế hoạch bị từ chối",
                    "Kế hoạch \"" + entity.getTen() + "\" bị từ chối"
                            + (entity.getLyDoTuChoi() != null ? ": " + entity.getLyDoTuChoi() : ""),
                    "/stations",
                    entity.getId());
        }
        return saved;
    }

    @Override
    @Transactional
    public KeHoachTrienKhaiResponse importKeHoachChiTiet(UUID keHoachId, KeHoachImportChiTietRequest request) {
        KeHoachTrienKhai keHoach = requireKeHoach(keHoachId);
        List<HopDongDoiTuong> doiTuongs = hopDongDoiTuongRepository.findByIdInAndNgayXoaIsNull(request.getDoiTuongIds());

        for (HopDongDoiTuong doiTuong : doiTuongs) {
            if (!keHoach.getHopDongId().equals(doiTuong.getHopDongId())) {
                continue;
            }
            if (keHoachTrienKhaiChiTietRepository
                    .findByKeHoachIdAndHopDongDoiTuongIdAndNgayXoaIsNull(keHoachId, doiTuong.getId())
                    .isPresent()) {
                continue;
            }
            KeHoachTrienKhaiChiTiet chiTiet = new KeHoachTrienKhaiChiTiet();
            chiTiet.setKeHoachId(keHoachId);
            chiTiet.setHopDongDoiTuongId(doiTuong.getId());
            chiTiet.setKhuVucId(doiTuong.getKhuVucId());
            chiTiet.setNhaThauId(doiTuong.getNhaThauId());
            keHoachTrienKhaiChiTietRepository.save(chiTiet);
        }
        return toKeHoachResponse(keHoach);
    }

    @Override
    @Transactional
    public VatTuYeuCauResponse taoVatTuYeuCau(VatTuYeuCauTaoRequest request) {
        HopDongDoiTuong doiTuong = requireDoiTuong(request.getHopDongDoiTuongId());
        LocalDate ngayYeuCau = request.getNgayYeuCau() != null ? request.getNgayYeuCau() : LocalDate.now();

        VatTuYeuCau entity = new VatTuYeuCau();
        entity.setHopDongDoiTuongId(request.getHopDongDoiTuongId());
        entity.setLoai(request.getLoai() != null ? request.getLoai() : TienDoConstants.VAT_TU_LOAI_B_CAP);
        entity.setTrangThai(TienDoConstants.VAT_TU_TRANG_THAI_TAO);
        entity.setNgayYeuCau(ngayYeuCau);
        entity.setGhiChu(request.getGhiChu());
        VatTuYeuCau saved = vatTuYeuCauRepository.save(entity);

        if (doiTuong.getNgayYeuCauVatTuB() == null) {
            doiTuong.setNgayYeuCauVatTuB(ngayYeuCau);
        }
        if (doiTuong.getTrangThaiVatTuB() == null || "CHUA_YEU_CAU".equals(doiTuong.getTrangThaiVatTuB())) {
            doiTuong.setTrangThaiVatTuB("DA_YEU_CAU");
        }
        hopDongDoiTuongRepository.save(doiTuong);

        return toVatTuResponse(saved);
    }

    @Override
    @Transactional
    public VatTuYeuCauResponse capNhatTrangThaiVatTu(UUID id, VatTuTrangThaiCapNhatRequest request) {
        VatTuYeuCau entity = vatTuYeuCauRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(TienDoErrorCode.VAT_TU_NOT_FOUND));
        entity.setTrangThai(request.getTrangThai());
        if (request.getGhiChu() != null) {
            entity.setGhiChu(request.getGhiChu());
        }
        if (request.getNgayHoanThanh() != null) {
            entity.setNgayHoanThanh(request.getNgayHoanThanh());
        } else if (TienDoConstants.TRANG_THAI_VAT_TU_DA_NHAN.equals(request.getTrangThai())) {
            entity.setNgayHoanThanh(LocalDate.now());
        }

        VatTuYeuCau saved = vatTuYeuCauRepository.save(entity);
        syncDoiTuongVatTuB(entity);
        return toVatTuResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VatTuYeuCauResponse> listVatTuByDoiTuong(UUID doiTuongId) {
        requireDoiTuong(doiTuongId);
        return vatTuYeuCauRepository.findByHopDongDoiTuongIdAndNgayXoaIsNullOrderByNgayTaoDesc(doiTuongId)
                .stream()
                .map(this::toVatTuResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiNguongCauHinhResponse> listKpiNguong() {
        return kpiNguongCauHinhRepository.findByHoatDongTrueOrderByUuTienAscMaAsc()
                .stream()
                .map(this::toKpiNguongResponse)
                .toList();
    }

    @Override
    @Transactional
    public KpiNguongCauHinhResponse capNhatKpiNguong(UUID id, KpiNguongCapNhatRequest request) {
        KpiNguongCauHinh entity = kpiNguongCauHinhRepository.findById(id)
                .orElseThrow(() -> new AppException(TienDoErrorCode.KPI_NGUONG_NOT_FOUND));
        if (request.getTen() != null) {
            entity.setTen(request.getTen());
        }
        entity.setSoNgayNguong(request.getSoNgayNguong());
        if (request.getUuTien() != null) {
            entity.setUuTien(request.getUuTien());
        }
        if (request.getHoatDong() != null) {
            entity.setHoatDong(request.getHoatDong());
        }
        return toKpiNguongResponse(kpiNguongCauHinhRepository.save(entity));
    }

    private void lockDoiTuongWhenConstructionStarts(HopDongDoiTuong entity) {
        if (entity.getNgayThiCongGanNhat() != null) {
            entity.setDanhSachKhoa(true);
        }
    }

    private void assertAdminForDateChanges(HopDongDoiTuong entity, DoiTuongTienDoCapNhatRequest request) {
        boolean needsAdmin = Boolean.TRUE.equals(entity.getDanhSachKhoa())
                || isChangingExistingDate(entity.getNgayBanGiaoMatBang(), request.getNgayBanGiaoMatBang())
                || isChangingExistingDate(entity.getNgayYeuCauVatTuB(), request.getNgayYeuCauVatTuB())
                || isChangingExistingDate(entity.getNgayHoanThanhVatTuB(), request.getNgayHoanThanhVatTuB())
                || isChangingExistingDate(entity.getNgayNghiemThu(), request.getNgayNghiemThu());

        if (needsAdmin && !isCurrentUserAdmin()) {
            throw new AppException(TienDoErrorCode.KHONG_DU_QUYEN_ADMIN,
                    "Chỉ Admin được sửa dữ liệu ngày tháng đã khóa hoặc đã nhập");
        }
    }

    private boolean isChangingExistingDate(LocalDate current, LocalDate requested) {
        return current != null && requested != null && !current.equals(requested);
    }

    private boolean isCurrentUserAdmin() {
        JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
        for (GrantedAuthority authority : user.getAuthorities()) {
            String code = authority.getAuthority();
            if (AuthorityPrefix.isFullAccessAuthority(code)
                    || FullAccessRoleCodes.isFullAccessRoleAuthority(code)) {
                return true;
            }
        }
        return false;
    }

    private HopDongDoiTuong requireDoiTuong(UUID id) {
        return hopDongDoiTuongRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(TienDoErrorCode.DOI_TUONG_NOT_FOUND));
    }

    private HopDong requireHopDong(UUID id) {
        return hopDongRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(TienDoErrorCode.HOP_DONG_NOT_FOUND));
    }

    private KeHoachTrienKhai requireKeHoach(UUID id) {
        return keHoachTrienKhaiRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(TienDoErrorCode.KE_HOACH_NOT_FOUND));
    }

    private void syncDoiTuongVatTuB(VatTuYeuCau vatTu) {
        hopDongDoiTuongRepository.findByIdAndNgayXoaIsNull(vatTu.getHopDongDoiTuongId())
                .ifPresent(doiTuong -> {
                    doiTuong.setTrangThaiVatTuB(vatTu.getTrangThai());
                    if (vatTu.getNgayHoanThanh() != null) {
                        doiTuong.setNgayHoanThanhVatTuB(vatTu.getNgayHoanThanh());
                    }
                    hopDongDoiTuongRepository.save(doiTuong);
                });
    }

    private KpiCanhBaoResponse buildKpiAlert(HopDongDoiTuong entity, KpiNguongCauHinh config, int days) {
        return KpiCanhBaoResponse.builder()
                .hopDongDoiTuongId(entity.getId())
                .hopDongId(entity.getHopDongId())
                .kpiMa(config.getMa())
                .kpiTen(config.getTen())
                .uuTien(config.getUuTien())
                .soNgayThucTe(days)
                .soNgayNguong(config.getSoNgayNguong())
                .soNgayVuot(days - config.getSoNgayNguong())
                .build();
    }

    private DoiTuongTienDoResponse toDoiTuongResponse(HopDongDoiTuong entity) {
        return DoiTuongTienDoResponse.builder()
                .id(entity.getId())
                .hopDongId(entity.getHopDongId())
                .doiTuongQuanLyId(entity.getDoiTuongQuanLyId())
                .ngayBanGiaoMatBang(entity.getNgayBanGiaoMatBang())
                .trangThaiVcontractBgm(entity.getTrangThaiVcontractBgm())
                .ngayYeuCauVatTuB(entity.getNgayYeuCauVatTuB())
                .ngayHoanThanhVatTuB(entity.getNgayHoanThanhVatTuB())
                .trangThaiVatTuA(entity.getTrangThaiVatTuA())
                .trangThaiVatTuB(entity.getTrangThaiVatTuB())
                .coPhatSinh(entity.getCoPhatSinh())
                .trangThaiNghiemThu(entity.getTrangThaiNghiemThu())
                .ngayNghiemThu(entity.getNgayNghiemThu())
                .daChotTham(entity.getDaChotTham())
                .giaTriChotTham(entity.getGiaTriChotTham())
                .daGiaoThauPhu(entity.getDaGiaoThauPhu())
                .daKyHdThauPhu(entity.getDaKyHdThauPhu())
                .danhSachKhoa(entity.getDanhSachKhoa())
                .ngayHtTc(entity.getNgayHtTc())
                .ngayThiCongGanNhat(entity.getNgayThiCongGanNhat())
                .build();
    }

    private ChecklistDamBaoResponse toChecklistResponse(HopDong hopDong) {
        return ChecklistDamBaoResponse.builder()
                .hopDongId(hopDong.getId())
                .checklistDoiTac(hopDong.getChecklistDoiTac())
                .checklistCcdc(hopDong.getChecklistCcdc())
                .checklistAtld(hopDong.getChecklistAtld())
                .checklistVatTuA(hopDong.getChecklistVatTuA())
                .checklistVatTuB(hopDong.getChecklistVatTuB())
                .checklistGhiChu(hopDong.getChecklistGhiChu())
                .build();
    }

    private KeHoachTrienKhaiResponse toKeHoachResponse(KeHoachTrienKhai entity) {
        List<KeHoachTrienKhaiChiTietResponse> chiTiet = keHoachTrienKhaiChiTietRepository
                .findByKeHoachIdAndNgayXoaIsNull(entity.getId())
                .stream()
                .map(ct -> KeHoachTrienKhaiChiTietResponse.builder()
                        .id(ct.getId())
                        .keHoachId(ct.getKeHoachId())
                        .hopDongDoiTuongId(ct.getHopDongDoiTuongId())
                        .khuVucId(ct.getKhuVucId())
                        .nhaThauId(ct.getNhaThauId())
                        .build())
                .toList();

        return KeHoachTrienKhaiResponse.builder()
                .id(entity.getId())
                .hopDongId(entity.getHopDongId())
                .ten(entity.getTen())
                .trangThai(entity.getTrangThai())
                .nguoiLapId(entity.getNguoiLapId())
                .nguoiDuyetId(entity.getNguoiDuyetId())
                .ngayGui(entity.getNgayGui())
                .ngayDuyet(entity.getNgayDuyet())
                .lyDoTuChoi(entity.getLyDoTuChoi())
                .chiTiet(chiTiet)
                .build();
    }

    private VatTuYeuCauResponse toVatTuResponse(VatTuYeuCau entity) {
        return VatTuYeuCauResponse.builder()
                .id(entity.getId())
                .hopDongDoiTuongId(entity.getHopDongDoiTuongId())
                .loai(entity.getLoai())
                .trangThai(entity.getTrangThai())
                .ngayYeuCau(entity.getNgayYeuCau())
                .ngayHoanThanh(entity.getNgayHoanThanh())
                .ghiChu(entity.getGhiChu())
                .build();
    }

    private KpiNguongCauHinhResponse toKpiNguongResponse(KpiNguongCauHinh entity) {
        return KpiNguongCauHinhResponse.builder()
                .id(entity.getId())
                .ma(entity.getMa())
                .ten(entity.getTen())
                .soNgayNguong(entity.getSoNgayNguong())
                .uuTien(entity.getUuTien())
                .hoatDong(entity.getHoatDong())
                .build();
    }

    private ChecklistMucResponse toChecklistMucResponse(HopDongChecklistMuc entity) {
        return ChecklistMucResponse.builder()
                .id(entity.getId())
                .hopDongId(entity.getHopDongId())
                .ten(entity.getTen())
                .thuTu(entity.getThuTu())
                .ghiChu(entity.getGhiChu())
                .build();
    }

    private <T> void applyIfPresent(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
