package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucNhomCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucNhomTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucHopDongTreeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucKhoiLuongSanLuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucNhomHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucNhomResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucNhomSaoChepResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucNhom;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface HangMucNhomService {
    List<HangMucNhomResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hopDongId);
    HangMucNhomResponse getById(UUID id);
    HangMucNhomResponse create(HangMucNhomTaoRequest request);
    HangMucNhomResponse update(UUID id, HangMucNhomCapNhatRequest request);
    void delete(UUID id);
    int deleteAllByHopDongId(UUID hopDongId);

    HangMucKhoiLuongSanLuongResponse getKhoiLuongSanLuong(UUID hopDongId);

    HangMucNhomHopDongResponse listByHopDongWithKhoiLuong(UUID hopDongId, Boolean activeOnly);

    HangMucHopDongTreeResponse getTreeByHopDongId(UUID hopDongId, Boolean activeOnly);

    /** Bulk tree for volume list: 1 load nhóm/chi tiết/công việc cho nhiều HĐ. */
    Map<UUID, HangMucHopDongTreeResponse> getTreesByHopDongIds(Collection<UUID> hopDongIds, Boolean activeOnly);

    /**
     * @param includeKhoiLuongSanLuong false khi chỉ cần định mức HĐ (dashboard KPI) — bỏ N query san_luong.
     */
    Map<UUID, HangMucHopDongTreeResponse> getTreesByHopDongIds(
            Collection<UUID> hopDongIds, Boolean activeOnly, boolean includeKhoiLuongSanLuong);

    /** For cross-module callers (vd sanluong) cần entity, không cần response DTO. */
    List<HangMucNhom> findActiveEntitiesByHopDongId(UUID hopDongId);

    /** Bulk, cùng lý do trên. */
    List<HangMucNhom> findActiveEntitiesByHopDongIds(Collection<UUID> hopDongIds);

    /** Tính lại cây hạng mục và ghi vào HopDong.tongThanhTienThiCong (denormalize cho volume). */
    void recomputeTongThanhTienThiCong(UUID hopDongId);

    /**
     * Sao chép toàn bộ cây hạng mục (nhóm → chi tiết → công việc) từ hợp đồng nguồn sang hợp
     * đồng đích — dùng để tái sử dụng catalog hạng mục giữa các hợp đồng cùng loại. Mã hạng mục
     * nhóm bị trùng với hợp đồng đích sẽ tự thêm hậu tố để tránh xung đột (mã chỉ unique theo
     * từng hợp đồng, xem HangMucNhomServiceImpl.create()).
     */
    HangMucNhomSaoChepResponse copyFromHopDong(UUID hopDongNguonId, UUID hopDongDichId);
}
