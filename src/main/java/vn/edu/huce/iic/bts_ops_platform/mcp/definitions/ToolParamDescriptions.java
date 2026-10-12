package vn.edu.huce.iic.bts_ops_platform.mcp.definitions;

/**
 * Mô tả @ToolParam cho các đối tượng lọc dùng chung giữa nhiều tool trong {@link McpToolDefinitions}
 * (khu vực, tỉnh thành, nhà thầu, hợp đồng...), gom 1 chỗ để sửa đồng bộ thay vì lặp lại chuỗi mô tả
 * ở từng method. Các tham số thuộc nghiệp vụ riêng của từng tool vẫn khai báo trực tiếp tại chỗ.
 */
public final class ToolParamDescriptions {

    private ToolParamDescriptions() {
    }

    public static final String NHA_THAU = "Nhà thầu cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: Royal, Anh Tu; truyền đúng tên người dùng nói, hệ thống tự tra gần đúng, không cần hỏi lại";

    public static final String KHU_VUC = "Khu vực cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp lệ: TTKV1, TTKV2, TTKV3, KV1, KV2, KV3,... Tự suy ra mã không hỏi lại";

    public static final String TINH_THANH = "Tỉnh/thành cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Ví dụ: BNH (Bắc Ninh), NAN (Nghệ An), THA (Thanh Hóa)";

    public static final String HOP_DONG = "Hợp đồng cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên). Mã hợp đồng, ví dụ TVTK023241, 31032026VTK2025";

    public static final String LOAI_HOP_DONG = "Loại hợp đồng cần lọc: ID, mã hoặc tên (mơ hồ sẽ báo lỗi kèm ứng viên)."
            + " Chỉ truyền khi người dùng nêu đích danh 1 loại hợp đồng; cụm \"sản lượng thi công\", \"thi công\" là cách gọi"
            + " chung toàn bộ sản lượng, KHÔNG phải loại hợp đồng → bỏ trống";

    public static final String PAGE = "Số trang, bắt đầu từ 0";

    public static final String PAGE_SIZE = "Số dòng mỗi trang, mặc định 3, tối đa 100";

    public static final String DOI_TUONG = "Đối tượng cần lọc: ID, mã hoặc tên (ưu tiên truyền mã hoặc ID; tên mơ hồ sẽ báo lỗi kèm danh sách ứng viên)";

    public static final String FROM_DATE = "Ngày bắt đầu khoảng lọc, YYYY-MM-DD, theo ngày hiện tại: hôm nay/hôm qua = ngày đó;"
            + " tuần này = thứ Hai tuần này; tuần trước = thứ Hai tuần trước; tháng này/trước = ngày 1 của tháng; năm nay = 01-01;"
            + " N ngày qua = hôm nay - (N-1). Người dùng không nói thời gian thì bỏ trống";

    public static final String TO_DATE = "Ngày kết thúc khoảng lọc (gồm cả ngày này), YYYY-MM-DD: kỳ đang diễn ra = hôm nay;"
            + " tuần trước = Chủ nhật tuần trước; tháng trước = ngày cuối tháng trước. Không nói thời gian thì bỏ trống";
}
