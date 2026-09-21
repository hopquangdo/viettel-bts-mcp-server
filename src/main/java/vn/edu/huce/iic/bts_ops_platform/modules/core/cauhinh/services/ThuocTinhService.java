package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ThuocTinhService {
    List<ThuocTinhResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID doiTuongQuanLyId, Boolean selectorOnly);

    /**
     * Bản nhẹ của {@link #list} — KHÔNG tính {@code lienKetBang}.
     * <p>
     * {@code list()} phân giải lienKetBang cho từng thuộc tính, mỗi lần là một query xuống
     * {@code kieu_du_lieu} (bảng 7 dòng) — liệt kê N thuộc tính tốn N query, đo được ~50ms/query.
     * Dùng bản này khi caller chỉ cần {@code ten} / {@code kieuDuLieuId} / {@code laKhoaChinh}
     * và không đọc {@code lienKetBang} (trường đó sẽ là null).
     */
    List<ThuocTinhResponse> listLite(String search, Boolean activeOnly, boolean includeDeleted, UUID doiTuongQuanLyId, Boolean selectorOnly);

    Map<UUID, List<ThuocTinhResponse>> listGroupedByDoiTuongQuanLyIds(Collection<UUID> doiTuongQuanLyIds);
    ThuocTinhResponse getById(UUID id);
    ThuocTinhResponse create(ThuocTinhTaoRequest request);
    ThuocTinhResponse update(UUID id, ThuocTinhCapNhatRequest request);
    void delete(UUID id);
}
