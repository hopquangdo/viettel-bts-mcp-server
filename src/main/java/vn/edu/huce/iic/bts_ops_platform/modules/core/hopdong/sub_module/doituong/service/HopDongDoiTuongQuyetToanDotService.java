package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service;

import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.request.VolumeQuyetToanDotRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeQuyetToanTongHopResponse;

import java.util.UUID;

/**
 * Quản lý các đợt quyết toán thực tế của 1 đối tượng (trạm/tuyến) — quyetToanThuc trên
 * HopDongDoiTuong là tổng denormalize, không còn nhập trực tiếp.
 */
public interface HopDongDoiTuongQuyetToanDotService {

    VolumeQuyetToanTongHopResponse list(UUID hopDongId, UUID hopDongDoiTuongId);

    VolumeQuyetToanTongHopResponse create(UUID hopDongId, UUID hopDongDoiTuongId, VolumeQuyetToanDotRequest request);

    VolumeQuyetToanTongHopResponse update(UUID hopDongId, UUID hopDongDoiTuongId, UUID dotId, VolumeQuyetToanDotRequest request);

    VolumeQuyetToanTongHopResponse delete(UUID hopDongId, UUID hopDongDoiTuongId, UUID dotId);
}
