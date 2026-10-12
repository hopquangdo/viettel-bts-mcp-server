package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.request;

import java.time.LocalDate;

/** Raw filters for assignment-list operations; null values mean no filter. */
public record PhanCongFilter(
        String query,
        String vung,
        String nhaThau,
        String hopDong,
        String khuVuc,
        String tinhThanh,
        String doiTuong,
        String giaiDoan,
        Boolean lichSu,
        LocalDate fromDate,
        LocalDate toDate) {

    public static PhanCongFilter empty() {
        return new PhanCongFilter(null, null, null, null, null, null, null, null, null, null, null);
    }
}