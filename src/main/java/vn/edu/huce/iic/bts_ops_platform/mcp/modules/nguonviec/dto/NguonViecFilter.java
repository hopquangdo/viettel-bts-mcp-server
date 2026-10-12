package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto;

import java.time.LocalDate;

/**
 * Bộ lọc chung của các tool nguồn việc (áp lên summary, tabCounts, danhSach). khuVuc: khớp trung tâm của hợp đồng;
 * nhaThau: ID, mã hoặc tên (tự tra cứu). loaiCv: kstk | tc | tk. phapLy: da_co | chua_co | nhap.
 * query: từ khóa theo tên chương trình hoặc mã hợp đồng. fromDate/toDate: kỳ tính sản lượng (mặc định năm hiện tại).
 */
public record NguonViecFilter(
        String khuVuc,
        String nhaThau,
        String trangThai,
        String loaiCv,
        String phapLy,
        String linhVuc,
        String query,
        LocalDate fromDate,
        LocalDate toDate) {
}
