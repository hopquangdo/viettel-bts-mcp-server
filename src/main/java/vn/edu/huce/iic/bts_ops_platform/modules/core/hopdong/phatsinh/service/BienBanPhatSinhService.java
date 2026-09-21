package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.service;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.request.BienBanPhatSinhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.response.BienBanPhatSinhResponse;

import java.util.List;
import java.util.UUID;

public interface BienBanPhatSinhService {

    /** Lập biên bản phát sinh mới — trạng thái ban đầu "cho_duyet". */
    BienBanPhatSinhResponse create(UUID hopDongId, BienBanPhatSinhTaoRequest request);

    /** Danh sách biên bản phát sinh của 1 hợp đồng — mới nhất trước. */
    List<BienBanPhatSinhResponse> list(UUID hopDongId);

    /** Đính kèm 1 file thiết kế/dự toán điều chỉnh vào 1 biên bản phát sinh đã lập. */
    BienBanPhatSinhResponse uploadTepDinhKem(
            UUID hopDongId, UUID id, MultipartFile file, String loaiTaiLieu, String ghiChu);

    BienBanPhatSinhResponse pheDuyet(UUID hopDongId, UUID id);

    BienBanPhatSinhResponse tuChoi(UUID hopDongId, UUID id, String lyDo);

    /** Ghi ngày ký phụ lục HĐ cho phần phát sinh đã được CĐT duyệt — ký thật diễn ra ngoài hệ
     * thống, hệ thống chỉ ghi mốc; file phụ lục đã ký đính qua uploadTepDinhKem
     * (loaiTaiLieu=PHU_LUC_HOP_DONG). */
    BienBanPhatSinhResponse kyPhuLuc(UUID hopDongId, UUID id, java.time.LocalDate ngayKy);
}
