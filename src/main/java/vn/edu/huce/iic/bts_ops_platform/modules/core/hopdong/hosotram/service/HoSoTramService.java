package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.service;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.dto.response.HoSoTramTepDinhKemResponse;

import java.util.List;
import java.util.UUID;

public interface HoSoTramService {

    /** Danh sách file đã tải lên của 1 trạm, mới nhất trước — gồm mọi danh mục thủ công. */
    List<HoSoTramTepDinhKemResponse> list(UUID hopDongId, UUID hopDongDoiTuongId);

    /** Tải lên 1 file cho 1 danh mục hồ sơ của 1 trạm — danhMuc phải thuộc tập cho phép. */
    HoSoTramTepDinhKemResponse upload(
            UUID hopDongId, UUID hopDongDoiTuongId, String danhMuc, MultipartFile file, String ghiChu);

    /** Gỡ 1 file đã tải nhầm — xoá mềm, không ảnh hưởng file vật lý đã lưu. */
    void xoa(UUID hopDongId, UUID hopDongDoiTuongId, UUID id);
}
