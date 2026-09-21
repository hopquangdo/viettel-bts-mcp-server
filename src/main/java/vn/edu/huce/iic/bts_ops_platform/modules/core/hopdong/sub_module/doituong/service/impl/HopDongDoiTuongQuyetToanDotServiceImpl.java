package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventContext;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request.VolumeQuyetToanDotRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeQuyetToanDotResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeQuyetToanTongHopResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.TrangThaiHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.TrangThaiHopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers.HopDongDanhSachTienDoHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuongQuyetToanDot;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongQuyetToanDotRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongQuyetToanDotService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HopDongDoiTuongQuyetToanDotServiceImpl implements HopDongDoiTuongQuyetToanDotService {

    private final HopDongDoiTuongQuyetToanDotRepository quyetToanDotRepository;
    private final HopDongDoiTuongRepository hopDongDoiTuongRepository;
    private final TrangThaiHopDongService trangThaiHopDongService;
    private final AppEventContext appEventContext;

    @Override
    @Transactional(readOnly = true)
    public VolumeQuyetToanTongHopResponse list(UUID hopDongId, UUID hopDongDoiTuongId) {
        HopDongDoiTuong obj = findObject(hopDongId, hopDongDoiTuongId);
        return toTongHop(obj);
    }

    @Override
    @Transactional
    public VolumeQuyetToanTongHopResponse create(UUID hopDongId, UUID hopDongDoiTuongId, VolumeQuyetToanDotRequest request) {
        HopDongDoiTuong obj = findObject(hopDongId, hopDongDoiTuongId);
        assertDotCreateAllowed(obj);
        validate(request);

        HopDongDoiTuongQuyetToanDot dot = new HopDongDoiTuongQuyetToanDot();
        dot.setHopDongDoiTuongId(hopDongDoiTuongId);
        dot.setSoTien(request.getSoTien());
        dot.setNgayQuyetToan(request.getNgayQuyetToan());
        dot.setGhiChu(request.getGhiChu());
        dot.setHoatDong(true);
        quyetToanDotRepository.save(dot);

        hopDongDoiTuongRepository.recalculateQuyetToanThuc(hopDongDoiTuongId);
        HopDongDoiTuong refreshed = findObject(hopDongId, hopDongDoiTuongId);

        auditDot(
                "THEM_DOT_QUYET_TOAN",
                "Thêm đợt quyết toán Volume",
                buildDotAuditDetail("Thêm", request.getSoTien(), request.getGhiChu()),
                hopDongId,
                hopDongDoiTuongId,
                dot.getId());

        return toTongHop(refreshed);
    }

    @Override
    @Transactional
    public VolumeQuyetToanTongHopResponse update(
            UUID hopDongId, UUID hopDongDoiTuongId, UUID dotId, VolumeQuyetToanDotRequest request) {
        HopDongDoiTuong obj = findObject(hopDongId, hopDongDoiTuongId);
        assertDotEditAllowed(obj);
        validate(request);

        HopDongDoiTuongQuyetToanDot dot = findDot(hopDongDoiTuongId, dotId);
        BigDecimal soTienTruoc = dot.getSoTien();
        dot.setSoTien(request.getSoTien());
        dot.setNgayQuyetToan(request.getNgayQuyetToan());
        dot.setGhiChu(request.getGhiChu());
        quyetToanDotRepository.save(dot);

        hopDongDoiTuongRepository.recalculateQuyetToanThuc(hopDongDoiTuongId);
        HopDongDoiTuong refreshed = findObject(hopDongId, hopDongDoiTuongId);

        auditDot(
                "SUA_DOT_QUYET_TOAN",
                "Sửa đợt quyết toán Volume",
                buildDotAuditDetail(
                        "Sửa",
                        request.getSoTien(),
                        request.getGhiChu(),
                        soTienTruoc),
                hopDongId,
                hopDongDoiTuongId,
                dotId);

        return toTongHop(refreshed);
    }

    @Override
    @Transactional
    public VolumeQuyetToanTongHopResponse delete(UUID hopDongId, UUID hopDongDoiTuongId, UUID dotId) {
        HopDongDoiTuong obj = findObject(hopDongId, hopDongDoiTuongId);
        assertDotEditAllowed(obj);
        HopDongDoiTuongQuyetToanDot dot = findDot(hopDongDoiTuongId, dotId);
        BigDecimal soTienTruoc = dot.getSoTien();
        String ghiChuTruoc = dot.getGhiChu();
        dot.setNgayXoa(Instant.now());
        dot.setHoatDong(false);
        quyetToanDotRepository.save(dot);

        hopDongDoiTuongRepository.recalculateQuyetToanThuc(hopDongDoiTuongId);
        HopDongDoiTuong refreshed = findObject(hopDongId, hopDongDoiTuongId);

        auditDot(
                "XOA_DOT_QUYET_TOAN",
                "Xóa đợt quyết toán Volume",
                buildDotAuditDetail("Xóa", soTienTruoc, ghiChuTruoc),
                hopDongId,
                hopDongDoiTuongId,
                dotId);

        return toTongHop(refreshed);
    }

    private void validate(VolumeQuyetToanDotRequest request) {
        if (request == null || request.getSoTien() == null || request.getSoTien().signum() <= 0) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Số tiền đợt quyết toán phải lớn hơn 0");
        }
    }

    private HopDongDoiTuong findObject(UUID hopDongId, UUID hopDongDoiTuongId) {
        HopDongDoiTuong obj = hopDongDoiTuongRepository.findByIdAndNgayXoaIsNull(hopDongDoiTuongId)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Không tìm thấy đối tượng"));
        if (!hopDongId.equals(obj.getHopDongId())) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Đối tượng không thuộc hợp đồng này");
        }
        return obj;
    }

    private HopDongDoiTuongQuyetToanDot findDot(UUID hopDongDoiTuongId, UUID dotId) {
        HopDongDoiTuongQuyetToanDot dot = quyetToanDotRepository.findByIdAndNgayXoaIsNull(dotId)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Không tìm thấy đợt quyết toán"));
        if (!hopDongDoiTuongId.equals(dot.getHopDongDoiTuongId())) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Đợt quyết toán không thuộc đối tượng này");
        }
        return dot;
    }

    private TrangThaiHopDongResponse resolveTrangThai(HopDongDoiTuong obj) {
        if (obj.getTrangThaiHopDongId() == null) {
            return null;
        }
        try {
            return trangThaiHopDongService.getById(obj.getTrangThaiHopDongId());
        } catch (Exception ignored) {
            return null;
        }
    }

    private void assertDotCreateAllowed(HopDongDoiTuong obj) {
        TrangThaiHopDongResponse st = resolveTrangThai(obj);
        String ma = st != null ? st.getMa() : null;
        String ten = st != null ? st.getTen() : null;
        String msg = HopDongDanhSachTienDoHelper.resolveQuyetToanVolumeDotLockMessage(
                obj.getQuyetToanThuc(), ma, ten, true);
        if (msg != null) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, msg);
        }
    }

    private void assertDotEditAllowed(HopDongDoiTuong obj) {
        TrangThaiHopDongResponse st = resolveTrangThai(obj);
        String ma = st != null ? st.getMa() : null;
        String ten = st != null ? st.getTen() : null;
        String msg = HopDongDanhSachTienDoHelper.resolveQuyetToanVolumeDotLockMessage(
                obj.getQuyetToanThuc(), ma, ten, false);
        if (msg != null) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, msg);
        }
    }

    private VolumeQuyetToanTongHopResponse toTongHop(HopDongDoiTuong obj) {
        List<VolumeQuyetToanDotResponse> dots = quyetToanDotRepository
                .findByHopDongDoiTuongIdAndNgayXoaIsNullAndHoatDongTrueOrderByNgayQuyetToanDescNgayTaoDesc(obj.getId())
                .stream()
                .map(d -> VolumeQuyetToanDotResponse.builder()
                        .id(d.getId())
                        .soTien(d.getSoTien())
                        .ngayQuyetToan(d.getNgayQuyetToan())
                        .ghiChu(d.getGhiChu())
                        .build())
                .toList();
        BigDecimal tong = obj.getQuyetToanThuc();
        TrangThaiHopDongResponse st = resolveTrangThai(obj);
        String ma = st != null ? st.getMa() : null;
        String ten = st != null ? st.getTen() : null;
        boolean fullLock = HopDongDanhSachTienDoHelper.isQuyetToanVolumeDotFullLock(ma, ten);
        boolean editLock = HopDongDanhSachTienDoHelper.isQuyetToanVolumeDotEditLock(tong, ma, ten);
        String lockMessage = fullLock
                ? HopDongDanhSachTienDoHelper.resolveQuyetToanVolumeDotLockMessage(tong, ma, ten, true)
                : editLock
                        ? HopDongDanhSachTienDoHelper.resolveQuyetToanVolumeDotLockMessage(tong, ma, ten, false)
                        : null;
        return VolumeQuyetToanTongHopResponse.builder()
                .tongQuyetToan(tong)
                .dots(dots)
                .quyetToanDotFullLock(fullLock)
                .quyetToanDotEditLock(editLock)
                .quyetToanDotLockMessage(lockMessage)
                .build();
    }

    private void auditDot(
            String action,
            String title,
            String detail,
            UUID hopDongId,
            UUID hopDongDoiTuongId,
            UUID dotId) {
        appEventContext.audit(
                action,
                title,
                detail,
                Map.of(
                        "hopDongId", hopDongId,
                        "doiTuongIds", List.of(hopDongDoiTuongId),
                        "dotId", dotId));
    }

    private static String buildDotAuditDetail(String verb, BigDecimal soTien, String ghiChu) {
        return buildDotAuditDetail(verb, soTien, ghiChu, null);
    }

    private static String buildDotAuditDetail(
            String verb, BigDecimal soTien, String ghiChu, BigDecimal soTienTruoc) {
        StringBuilder sb = new StringBuilder();
        sb.append(verb).append(' ');
        if (soTienTruoc != null) {
            sb.append(soTienTruoc.toPlainString()).append(" → ");
        }
        sb.append(soTien != null ? soTien.toPlainString() : "—");
        if (ghiChu != null && !ghiChu.isBlank()) {
            sb.append(" · Ghi chú: ").append(ghiChu.trim());
        }
        return sb.toString();
    }
}
