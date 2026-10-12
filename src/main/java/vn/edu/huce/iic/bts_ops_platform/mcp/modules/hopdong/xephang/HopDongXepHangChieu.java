package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang;

import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpCodeEnum;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpCodeEnums;

import java.util.List;

/** Chiều xếp hạng của tool hopdong_xep_hang (tham số {@code theo}) — nguồn duy nhất cho mã, nhãn, alias. */
public enum HopDongXepHangChieu implements McpCodeEnum {
    KHU_VUC("khu_vuc", "Khu vực", List.of("khuvuc", "ttkv")),
    TINH("tinh", "Tỉnh/thành", List.of("tinh_thanh", "tinhthanh", "province"));

    public static final String THAM_SO = "theo";

    private final String code;
    private final String label;
    private final List<String> aliases;

    HopDongXepHangChieu(String code, String label, List<String> aliases) {
        this.code = code;
        this.label = label;
        this.aliases = aliases;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public List<String> aliases() {
        return aliases;
    }

    public static HopDongXepHangChieu of(String value) {
        return McpCodeEnums.of(HopDongXepHangChieu.class, THAM_SO, value);
    }

    public static String codes() {
        return McpCodeEnums.codes(HopDongXepHangChieu.class);
    }
}
