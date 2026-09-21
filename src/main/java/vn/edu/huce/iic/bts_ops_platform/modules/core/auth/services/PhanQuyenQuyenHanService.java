package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.PhanQuyenQuyenHanDongBoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response.PhanQuyenQuyenHanResponse;

import java.util.List;

public interface PhanQuyenQuyenHanService {

    /** Các quyền hạn đang bật của 1 vai trò trong ma trận. */
    List<PhanQuyenQuyenHanResponse> list(String quyenMa);

    /** Đồng bộ thay-cả-bộ quyền hạn cho 1 vai trò — xong tự xóa cache authority của vai trò đó. */
    List<PhanQuyenQuyenHanResponse> sync(PhanQuyenQuyenHanDongBoRequest request);

    /** Danh sách mã quyền hạn của CHÍNH người đang đăng nhập (đã tính vai trò toàn quyền) — cho
     * frontend ẩn/hiện menu, nút bấm. */
    List<String> cuaToi();
}
