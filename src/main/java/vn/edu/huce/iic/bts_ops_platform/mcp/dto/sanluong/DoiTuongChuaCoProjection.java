package vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong;

/** DoiTuongProgressProjection kèm giá trị khoá chính (mã trạm, mã tuyến…) lấy ngay trong câu SQL. */
public interface DoiTuongChuaCoProjection extends DoiTuongProgressProjection {
    String getMaDoiTuong();
    /** Tổng số đối tượng chưa có sản lượng (cùng bộ lọc, trước khi phân trang). */
    Long getTong();
}
