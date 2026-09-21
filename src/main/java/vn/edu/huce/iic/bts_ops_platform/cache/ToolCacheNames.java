package vn.edu.huce.iic.bts_ops_platform.cache;

/** Namespace cache riêng cho common/tools — tách khỏi cache tầng service để evict độc lập. */
public final class ToolCacheNames {

    public static final String HOP_DONG = "tool:hopdong";
    public static final String SAN_LUONG = "tool:sanluong";
    public static final String PHAN_CONG = "tool:phancong";
    public static final String TRAM_TON = "tool:tramton";
    public static final String VUONG_MAC = "tool:vuongmac";
    public static final String NGUON_LUC = "tool:nguonluc";
    public static final String BIEN_BAN = "tool:bienban";

    /** Namespace cho lớp resolve/exists entity core dùng chung mọi tool (mcp/services/*). */
    public static final String NHA_THAU = "tool:core:nhathau";
    public static final String KHU_VUC = "tool:core:khuvuc";
    public static final String TINH_THANH = "tool:core:tinhthanh";

    private ToolCacheNames() {
    }
}
