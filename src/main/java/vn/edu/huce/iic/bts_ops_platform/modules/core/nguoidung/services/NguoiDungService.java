package vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.request.DangKyRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.request.NguoiDungCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.request.TaoTaiKhoanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.response.NguoiDungResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.response.NguoiDungThamChieuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.entity.NguoiDung;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface NguoiDungService {

    List<NguoiDungResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID khuVucId);

    /**
     * Bulk lookup họ tên nhà thầu (nhóm "nha_thau") theo id, để các module khác
     * hiển thị tên nhà thầu mà không phải tự gọi listThamChieu + tự build map.
     */
    Map<UUID, String> buildContractorNameLookup();

    /** For same-domain callers (e.g. AuthService) that need the entity itself, not the response DTO. */
    Optional<NguoiDung> findEntityByTenDangNhap(String tenDangNhap);

    /** For same-domain callers (e.g. AuthService) that need the entity itself, not the response DTO. */
    Optional<NguoiDung> findActiveEntityById(UUID id);

    List<NguoiDungThamChieuResponse> listThamChieu(String nhom, String search, Boolean activeOnly);

    NguoiDungResponse getById(UUID id);

    NguoiDungResponse register(DangKyRequest request);

    NguoiDungResponse create(TaoTaiKhoanRequest request);

    NguoiDungResponse update(UUID id, NguoiDungCapNhatRequest request);

    void delete(UUID id);
}
