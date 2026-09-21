package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDoiTuongMetaChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScopeService;
import vn.edu.huce.iic.bts_ops_platform.common.util.VietnamDateUtils;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.TrangThaiHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.TrangThaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.TrangThaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.TrangThaiHopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTrangThaiBatchRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.HopDongDanhSachTienDoHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongDanhSachService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongTrangThaiDuyetService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongTrangThaiService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HopDongDoiTuongTrangThaiDuyetServiceImpl implements HopDongDoiTuongTrangThaiDuyetService {

    private final HopDongDoiTuongRepository hopDongDoiTuongRepository;
    private final HopDongRepository hopDongRepository;
    private final TrangThaiHopDongRepository trangThaiHopDongRepository;
    private final TrangThaiHopDongService trangThaiHopDongService;
    private final HopDongDoiTuongTrangThaiService hopDongDoiTuongTrangThaiService;
    private final HopDongDanhSachService hopDongDanhSachService;
    private final ContractorScopeService contractorScopeService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public int yeuCauHuy(HopDongDoiTuongTrangThaiBatchRequest request) {
        requireLyDoHuy(request.getLyDoHuy());
        UUID pendingId = requireStatusIdByMa(HopDongDanhSachTienDoHelper.MA_CHO_XAC_NHAN_HUY);
        return applyPendingStatus(request.getHopDongDoiTuongIds(), pendingId, request.getLyDoHuy(), true, false);
    }

    @Override
    @Transactional
    public int yeuCauHoanThanh(HopDongDoiTuongTrangThaiBatchRequest request) {
        UUID pendingId = requireStatusIdByMa(HopDongDanhSachTienDoHelper.MA_CHO_XAC_NHAN_HT);
        return applyPendingStatus(request.getHopDongDoiTuongIds(), pendingId, null, false, true);
    }

    @Override
    @Transactional
    public int xacNhanHuy(HopDongDoiTuongTrangThaiBatchRequest request) {
        requireLyDoHuy(request.getLyDoHuy());
        int updated = 0;
        for (UUID id : distinctIds(request.getHopDongDoiTuongIds())) {
            HopDongDoiTuong entity = loadEntity(id);
            TrangThaiHopDongResponse current = loadStatus(entity.getTrangThaiHopDongId());
            HopDong hopDong = loadHopDong(entity.getHopDongId());
            UUID huyId = resolveHuyTrangThaiId(hopDong);
            if (huyId == null) {
                throw new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                        "Chưa cấu hình trạng thái Hủy trên luồng kiểu hợp đồng");
            }
            if (current != null && HopDongDanhSachTienDoHelper.isHuyStatus(current.getMa(), current.getTen())) {
                continue;
            }
            if (current == null
                    || !HopDongDanhSachTienDoHelper.isPendingCancelStatus(current.getMa(), current.getTen())) {
                throw new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                        "Đối tượng chưa ở trạng thái chờ xác nhận hủy");
            }
            if (current != null
                    && HopDongDanhSachTienDoHelper.isPendingCompleteStatus(current.getMa(), current.getTen())) {
                throw new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                        "Đối tượng đang chờ xác nhận hoàn thành, không thể hủy");
            }
            validateAllowedStatus(hopDong, entity.getDoiTuongQuanLyId(), huyId);
            entity.setTrangThaiHopDongId(huyId);
            entity.setLyDoHuy(request.getLyDoHuy().trim());
            entity.setTrangThaiTruocXacNhanId(null);
            hopDongDoiTuongRepository.save(entity);
            publishChanged(entity);
            updated++;
        }
        return updated;
    }

    @Override
    @Transactional
    public int xacNhanHoanThanh(HopDongDoiTuongTrangThaiBatchRequest request) {
        int updated = 0;
        for (UUID id : distinctIds(request.getHopDongDoiTuongIds())) {
            HopDongDoiTuong entity = loadEntity(id);
            TrangThaiHopDongResponse current = loadStatus(entity.getTrangThaiHopDongId());
            HopDong hopDong = loadHopDong(entity.getHopDongId());
            if (current != null && HopDongDanhSachTienDoHelper.isCompletedStatus(current.getMa())) {
                continue;
            }
            if (current != null && HopDongDanhSachTienDoHelper.isHuyStatus(current.getMa(), current.getTen())) {
                throw new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                        "Đối tượng đã hủy, không thể xác nhận hoàn thành");
            }
            if (current != null
                    && HopDongDanhSachTienDoHelper.isPendingCancelStatus(current.getMa(), current.getTen())) {
                throw new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                        "Đối tượng đang chờ xác nhận hủy, không thể xác nhận hoàn thành");
            }
            if (current == null
                    || !HopDongDanhSachTienDoHelper.isPendingCompleteStatus(current.getMa(), current.getTen())) {
                throw new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                        "Đối tượng chưa ở trạng thái chờ xác nhận hoàn thành");
            }
            UUID htId = resolveHoanThanhTrangThaiId(hopDong);
            if (htId == null) {
                throw new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                        "Chưa cấu hình trạng thái hoàn thành trên luồng kiểu hợp đồng");
            }
            validateAllowedStatus(hopDong, entity.getDoiTuongQuanLyId(), htId);
            entity.setTrangThaiHopDongId(htId);
            if (entity.getNgayHtTc() == null) {
                entity.setNgayHtTc(VietnamDateUtils.today());
            }
            entity.setTrangThaiTruocXacNhanId(null);
            hopDongDoiTuongRepository.save(entity);
            publishChanged(entity);
            updated++;
        }
        return updated;
    }

    @Override
    @Transactional
    public int tuChoiXacNhan(HopDongDoiTuongTrangThaiBatchRequest request) {
        int updated = 0;
        for (UUID id : distinctIds(request.getHopDongDoiTuongIds())) {
            HopDongDoiTuong entity = loadEntity(id);
            TrangThaiHopDongResponse current = loadStatus(entity.getTrangThaiHopDongId());
            if (current == null
                    || (!HopDongDanhSachTienDoHelper.isPendingCancelStatus(current.getMa(), current.getTen())
                            && !HopDongDanhSachTienDoHelper.isPendingCompleteStatus(
                                    current.getMa(), current.getTen()))) {
                throw new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                        "Đối tượng không ở trạng thái chờ xác nhận");
            }
            UUID restoreId = entity.getTrangThaiTruocXacNhanId();
            if (restoreId == null) {
                throw new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                        "Không xác định được trạng thái trước yêu cầu xác nhận");
            }
            HopDong hopDong = loadHopDong(entity.getHopDongId());
            validateAllowedStatus(hopDong, entity.getDoiTuongQuanLyId(), restoreId);
            entity.setTrangThaiHopDongId(restoreId);
            entity.setTrangThaiTruocXacNhanId(null);
            if (HopDongDanhSachTienDoHelper.isPendingCancelStatus(current.getMa(), current.getTen())) {
                entity.setLyDoHuy(null);
            }
            hopDongDoiTuongRepository.save(entity);
            publishChanged(entity);
            updated++;
        }
        return updated;
    }

    private int applyPendingStatus(
            List<UUID> ids,
            UUID pendingStatusId,
            String lyDoHuy,
            boolean cancelFlow,
            boolean completeFlow) {
        int updated = 0;
        for (UUID id : distinctIds(ids)) {
            HopDongDoiTuong entity = loadEntity(id);
            TrangThaiHopDongResponse current = loadStatus(entity.getTrangThaiHopDongId());
            if (current != null && HopDongDanhSachTienDoHelper.isHuyStatus(current.getMa(), current.getTen())) {
                continue;
            }
            if (current != null && HopDongDanhSachTienDoHelper.isCompletedStatus(current.getMa())) {
                continue;
            }
            if (cancelFlow) {
                if (current != null
                        && HopDongDanhSachTienDoHelper.isPendingCancelStatus(current.getMa(), current.getTen())) {
                    continue;
                }
                if (current != null
                        && HopDongDanhSachTienDoHelper.isPendingCompleteStatus(current.getMa(), current.getTen())) {
                    throw new AppException(
                            HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                            "Đối tượng đang chờ xác nhận hoàn thành, không thể gửi yêu cầu hủy");
                }
            }
            if (completeFlow) {
                if (current != null
                        && HopDongDanhSachTienDoHelper.isPendingCompleteStatus(current.getMa(), current.getTen())) {
                    continue;
                }
                if (current != null
                        && HopDongDanhSachTienDoHelper.isPendingCancelStatus(current.getMa(), current.getTen())) {
                    throw new AppException(
                            HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                            "Đối tượng đang chờ xác nhận hủy, không thể gửi yêu cầu hoàn thành");
                }
            }
            HopDong hopDong = loadHopDong(entity.getHopDongId());
            validateAllowedStatus(hopDong, entity.getDoiTuongQuanLyId(), pendingStatusId);
            if (current == null
                    || (!HopDongDanhSachTienDoHelper.isPendingCancelStatus(current.getMa(), current.getTen())
                            && !HopDongDanhSachTienDoHelper.isPendingCompleteStatus(
                                    current.getMa(), current.getTen()))) {
                entity.setTrangThaiTruocXacNhanId(entity.getTrangThaiHopDongId());
            }
            entity.setTrangThaiHopDongId(pendingStatusId);
            if (cancelFlow && StringUtils.hasText(lyDoHuy)) {
                entity.setLyDoHuy(lyDoHuy.trim());
            }
            hopDongDoiTuongRepository.save(entity);
            publishChanged(entity);
            updated++;
        }
        return updated;
    }

    private HopDongDoiTuong loadEntity(UUID id) {
        contractorScopeService.assertHopDongDoiTuongAccessible(id);
        return hopDongDoiTuongRepository.findByIdAndNgayXoaIsNull(id)
                .filter(entity -> Boolean.TRUE.equals(entity.getHoatDong()))
                .orElseThrow(() -> new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_NOT_FOUND, "Không tìm thấy đối tượng hợp đồng"));
    }

    private HopDong loadHopDong(UUID hopDongId) {
        return hopDongRepository.findByIdAndNgayXoaIsNull(hopDongId)
                .filter(h -> Boolean.TRUE.equals(h.getHoatDong()))
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_NOT_FOUND, "Không tìm thấy hợp đồng"));
    }

    private TrangThaiHopDongResponse loadStatus(UUID trangThaiId) {
        if (trangThaiId == null) {
            return null;
        }
        try {
            return trangThaiHopDongService.getById(trangThaiId);
        } catch (AppException ex) {
            return null;
        }
    }

    private UUID requireStatusIdByMa(String ma) {
        return trangThaiHopDongRepository.findFirstByMaIgnoreCaseAndNgayXoaIsNull(ma)
                .map(TrangThaiHopDong::getId)
                .orElseThrow(() -> new AppException(
                        HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                        "Chưa cấu hình trạng thái \"" + ma + "\" — chạy migration hệ thống"));
    }

    private void requireLyDoHuy(String lyDoHuy) {
        if (!StringUtils.hasText(lyDoHuy)) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Vui lòng nhập lý do hủy");
        }
    }

    private void validateAllowedStatus(HopDong hopDong, UUID doiTuongQuanLyId, UUID trangThaiId) {
        if (!hopDongDoiTuongTrangThaiService.isAllowedTrangThai(
                hopDong.getKieuHopDongId(), hopDong.getLoaiHopDongId(), doiTuongQuanLyId, trangThaiId)) {
            throw new AppException(
                    HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Trạng thái không hợp lệ với luồng trạng thái của kiểu HĐ");
        }
    }

    private UUID resolveHoanThanhTrangThaiId(HopDong hopDong) {
        if (hopDong.getKieuHopDongId() == null || hopDong.getLoaiHopDongId() == null) {
            return null;
        }
        List<LuongTrangThaiBuocResponse> flowBuoc = hopDongDanhSachService.getFlowBuocForKieu(
                hopDong.getLoaiHopDongId(), hopDong.getKieuHopDongId());
        return HopDongDanhSachTienDoHelper.resolveHoanThanhTrangThaiId(flowBuoc);
    }

    private UUID resolveHuyTrangThaiId(HopDong hopDong) {
        if (hopDong.getKieuHopDongId() == null || hopDong.getLoaiHopDongId() == null) {
            return null;
        }
        List<LuongTrangThaiBuocResponse> flowBuoc = hopDongDanhSachService.getFlowBuocForKieu(
                hopDong.getLoaiHopDongId(), hopDong.getKieuHopDongId());
        for (LuongTrangThaiBuocResponse step : flowBuoc) {
            if (HopDongDanhSachTienDoHelper.isHuyStatus(step.getMa(), step.getTen())) {
                return step.getTrangThaiHopDongId();
            }
        }
        return null;
    }

    private List<UUID> distinctIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Danh sách đối tượng trống");
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private void publishChanged(HopDongDoiTuong entity) {
        applicationEventPublisher.publishEvent(
                HopDongDoiTuongMetaChangedEvent.of(entity.getHopDongId(), entity.getId()));
    }
}
