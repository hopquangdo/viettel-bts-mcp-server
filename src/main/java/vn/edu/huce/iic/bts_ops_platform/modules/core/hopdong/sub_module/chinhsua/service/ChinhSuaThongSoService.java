package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.service;

import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.ChinhSuaThongSoDanhSachItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.ChinhSuaThongSoDeXuatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.ChinhSuaThongSoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.LanChinhSuaGanNhatResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.dto.SoLanSuaThongSoResponse;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface ChinhSuaThongSoService {

    String TRANG_THAI_DA_AP_DUNG = "da_ap_dung";
    String TRANG_THAI_CHO_DUYET = "cho_duyet";
    String TRANG_THAI_TU_CHOI = "tu_choi";

    /** Số lần đã áp dụng thành công cho từng thuộc tính của 1 trạm. */
    SoLanSuaThongSoResponse demSoLanDaApDung(UUID hopDongDoiTuongId);

    /** Batch: doiTuongId -> (thuocTinhId -> soLan). */
    Map<UUID, Map<UUID, Integer>> demSoLanDaApDungBatch(Collection<UUID> hopDongDoiTuongIds);

    /** Batch response dạng list cho FE. */
    java.util.List<SoLanSuaThongSoResponse> demSoLanDaApDungBatchList(Collection<UUID> hopDongDoiTuongIds);

    /** Batch: thời điểm chỉnh sửa thông số gần nhất theo từng trạm. */
    java.util.List<LanChinhSuaGanNhatResponse> lanChinhSuaGanNhatBatch(Collection<UUID> hopDongDoiTuongIds);

    /** Kiểm tra trước khi gọi update trực tiếp — ném lỗi nếu cần đề xuất + phê duyệt. */
    void requireDirectEditAllowed(UUID hopDongDoiTuongId, UUID thuocTinhId);

    /** Ghi nhận lần sửa đã áp dụng trực tiếp (lần 1–2). */
    void ghiApDungTrucTiep(
            UUID hopDongId,
            UUID hopDongDoiTuongId,
            UUID thuocTinhId,
            String tenThuocTinh,
            String giaTriCu,
            String giaTriMoi);

    /** Đề xuất chỉnh sửa — tự áp dụng nếu lần 1–2, chờ duyệt nếu lần 3+. */
    java.util.List<ChinhSuaThongSoResponse> deXuat(ChinhSuaThongSoDeXuatRequest request);

    java.util.List<ChinhSuaThongSoResponse> lichSu(UUID hopDongDoiTuongId);

    PageResponse<ChinhSuaThongSoDanhSachItemResponse> danhSachChoDuyet(
            UUID hopDongId, Integer page, Integer size);

    ChinhSuaThongSoResponse pheDuyet(UUID id);

    ChinhSuaThongSoResponse tuChoi(UUID id, String lyDo);
}
