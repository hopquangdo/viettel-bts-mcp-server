package vn.edu.huce.iic.bts_ops_platform.mcp.common.repository;

/**
 * SQL dùng chung lấy mã thật của đối tượng hợp đồng (vd "NBH0581-NBH0061"): giá trị của thuộc tính khoá chính trong
 * hop_dong_doi_tuong_gia_tri. KHÔNG dùng doi_tuong_quan_ly.ma/ten làm mã -- bảng đó là LOẠI đối tượng (Tuyến/Trạm).
 * Hằng số (không phải hàm) vì @Query cần hằng lúc biên dịch; mỗi hằng ứng với 1 alias của hop_dong_doi_tuong.
 */
public final class DoiTuongSql {


    /** Mã đối tượng khi hop_dong_doi_tuong có alias {@code d}. */
    public static final String MA_DOI_TUONG = """
            (SELECT g.gia_tri FROM hop_dong_doi_tuong_gia_tri g
               JOIN thuoc_tinh tt ON tt.id = g.thuoc_tinh_id AND tt.la_khoa_chinh = TRUE AND tt.ngay_xoa IS NULL
              WHERE g.hop_dong_doi_tuong_id = d.id AND g.ngay_xoa IS NULL LIMIT 1)""";

    /** Mã đối tượng khi đối tượng nằm trong subquery alias {@code c} (cột id = id hop_dong_doi_tuong). */
    public static final String MA_DOI_TUONG_C = """
            (SELECT g.gia_tri FROM hop_dong_doi_tuong_gia_tri g
               JOIN thuoc_tinh tt ON tt.id = g.thuoc_tinh_id AND tt.la_khoa_chinh = TRUE AND tt.ngay_xoa IS NULL
              WHERE g.hop_dong_doi_tuong_id = c.id AND g.ngay_xoa IS NULL LIMIT 1)""";

    private DoiTuongSql() {
    }
}
