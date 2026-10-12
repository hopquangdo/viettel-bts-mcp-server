package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang;

import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpCodeEnum;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpCodeEnums;

import java.util.List;

/** Chiều xếp hạng của tool vuongmac_xep_hang (tham số {@code theo}) — nguồn duy nhất cho mã, nhãn, alias. */
public enum VuongMacXepHangChieu implements McpCodeEnum {
    HOP_DONG("hop_dong", "Hợp đồng", List.of("hopdong", "contract")),
    KHU_VUC("khu_vuc", "Khu vực", List.of("khuvuc", "ttkv"));

    public static final String THAM_SO = "theo";

    private final String code;
    private final String label;
    private final List<String> aliases;

    VuongMacXepHangChieu(String code, String label, List<String> aliases) {
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

    public static VuongMacXepHangChieu of(String value) {
        return McpCodeEnums.of(VuongMacXepHangChieu.class, THAM_SO, value);
    }

    public static String codes() {
        return McpCodeEnums.codes(VuongMacXepHangChieu.class);
    }
}
