package vn.edu.huce.iic.bts_ops_platform.mcp.definitions;

/**
 * Mô tả @ToolParam cho các đối tượng lọc dùng chung giữa nhiều tool trong {@link McpToolDefinitions}
 * (khu vực, tỉnh thành, nhà thầu, hợp đồng...), gom 1 chỗ để sửa đồng bộ thay vì lặp lại chuỗi mô tả
 * ở từng method. Các tham số thuộc nghiệp vụ riêng của từng tool vẫn khai báo trực tiếp tại chỗ.
 */
final class ToolParamDescriptions {

    private ToolParamDescriptions() {
    }

    static final String NHA_THAU = "Nhà thầu cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: Royal, Anh Tu; truyền đúng tên người dùng nói, hệ thống tự tra gần đúng, không cần hỏi lại";

    static final String KHU_VUC = "Khu vực cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp lệ: TTKV1, TTKV2, TTKV3. Người dùng viết \"kv1\", \"khu vực 1\", \"TTKV 1\" thì truyền TTKV1, không hỏi lại";

    static final String TINH_THANH = "Tỉnh/thành cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: BNH (Bắc Ninh), NAN (Nghệ An), THA (Thanh Hóa)";

    static final String HOP_DONG = "Hợp đồng cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp đồng, ví dụ TVTK023241, 31032026VTK2025";

    static final String LOAI_HOP_DONG = "Loại hợp đồng cần lọc: ID, mã hoặc tên (mơ hồ sẽ báo lỗi kèm ứng viên)";

    static final String PAGE = "Số trang, bắt đầu từ 0";

    static final String PAGE_SIZE = "Số dòng mỗi trang, mặc định 3, tối đa 100";

    static final String DOI_TUONG = "Đối tượng cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên)";

    static final String FROM_DATE = "Ngày bắt đầu của khoảng thời gian lọc";

    static final String TO_DATE = "Ngày kết thúc của khoảng thời gian lọc (gồm cả ngày này)";
}
