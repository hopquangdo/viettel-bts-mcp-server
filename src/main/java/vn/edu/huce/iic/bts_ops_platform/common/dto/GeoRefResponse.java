package vn.edu.huce.iic.bts_ops_platform.common.dto;

import java.util.UUID;

/**
 * Tham chiếu id + mã + tên dùng chung cho khu vực/tỉnh thành/... — nhúng vào response thay vì
 * chỉ trả tên/mã (label) như trước, để frontend gửi lại đúng id thật khi lọc thay vì gửi text.
 */
public record GeoRefResponse(UUID id, String ma, String ten) {

    public static GeoRefResponse of(UUID id, String ma, String ten) {
        if (id == null && (ma == null || ma.isBlank()) && (ten == null || ten.isBlank())) {
            return null;
        }
        return new GeoRefResponse(id, ma, ten);
    }
}
