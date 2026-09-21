package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HangMucImportResponseHelper {

    private HangMucImportResponseHelper() {
    }

    public static Map<String, Object> buildImportResponse(
            List<Map<String, Object>> rows,
            int created,
            int updated,
            int skipped,
            List<String> errors,
            int soNhom) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("soDongXemTruoc", rows.size());
        response.put("daNhap", created + updated);
        response.put("daTao", created);
        response.put("daCapNhat", updated);
        response.put("boQua", skipped);
        response.put("loi", errors);
        response.put("soNhomHangMuc", soNhom);
        response.put("thanhCong", (created + updated > 0) && errors.isEmpty());
        response.put("ghiChu", !errors.isEmpty()
                ? "Import thất bại — còn " + errors.size() + " lỗi, không ghi nhận dữ liệu."
                : (created + updated > 0
                        ? "Đã import " + created + " dòng mới"
                                + (updated > 0 ? ", cập nhật " + updated : "")
                                + (soNhom > 0 ? " vào " + soNhom + " nhóm hạng mục" : "")
                                + (skipped > 0 ? ", bỏ qua " + skipped : "")
                        : "Không import được dòng nào. Kiểm tra mapping và dữ liệu file."));
        return response;
    }
}
