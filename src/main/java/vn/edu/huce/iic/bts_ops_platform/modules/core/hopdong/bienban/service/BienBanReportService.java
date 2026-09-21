package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.service;

import java.util.List;
import java.util.Map;

public interface BienBanReportService {

    /**
     * Trạm nào chưa có biên bản khảo sát (loaiBienBan=BAO_CAO_KHAO_SAT, đã duyệt) — đối chiếu
     * danh sách trạm đã có (parse từ hopDongDoiTuongIdsJson của các BienBan liên quan) với toàn bộ
     * trạm active trong phạm vi lọc.
     */
    List<Map<String, Object>> thieuKhaoSat(String loaiHopDong, String khuVuc);

    /**
     * Với mỗi hợp đồng trong phạm vi, đối chiếu checklist biên bản/hồ sơ bắt buộc
     * (KieuHopDongDanhMucBienBan theo kieuHopDongId) với biên bản/hồ sơ đã có (BienBan đã duyệt
     * cho loại có mẫu Word, HoSoTramTepDinhKem cho loại tải lên thủ công) — chỉ trả về hợp đồng
     * còn thiếu ít nhất 1 loại.
     */
    List<Map<String, Object>> thieuTheoTienDo(String maHopDong);
}
