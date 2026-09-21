package vn.edu.huce.iic.bts_ops_platform.common.util;

/**
 * Giá trị mặc định page/size dùng chung cho các list API "danh sách chính" (khác với các API
 * dạng "hiện gần hết" như audit log — những chỗ đó vẫn giữ hằng số riêng vì giá trị cố tình
 * khác). Gộp lại để tránh 8 service lặp lại cùng 1 cặp DEFAULT_SIZE=5/MAX_SIZE=100.
 */
public final class PaginationDefaults {

    public static final int DEFAULT_PAGE_SIZE = 5;
    public static final int MAX_PAGE_SIZE = 100;

    /** AuditLogServiceImpl — cố tình lớn hơn mức chung để giữ UX "hiện gần hết" hiện có. */
    public static final int AUDIT_LOG_DEFAULT_SIZE = 200;
    public static final int AUDIT_LOG_MAX_SIZE = 500;

    /**
     * Số lớn thay cho "không giới hạn" khi gọi thẳng repository (bỏ qua clamp MAX_PAGE_SIZE của
     * list()) để lấy hết trong 1 lần thay vì loop nhiều trang — chỉ an toàn khi caller đã tự
     * scope theo 1 hợp đồng/đối tượng (hàng trăm dòng), không dùng cho quét toàn bảng.
     */
    public static final int LIST_ALL_SIZE = 20_000;

    private PaginationDefaults() {
    }
}
