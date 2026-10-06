package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang;

import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpCodeEnum;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpCodeEnums;

import java.util.List;

/** Chiều xếp hạng của tool nguonviec_xep_hang (tham số {@code theo}) — nguồn duy nhất cho mã, nhãn, alias. */
public enum NguonViecXepHangChieu implements McpCodeEnum {
    RUI_RO("rui_ro", "Mức rủi ro hợp đồng", List.of("ruiro", "rui_ro_hop_dong", "hop_dong_rui_ro", "risk")),
    TRUNG_TAM("trung_tam", "Trung tâm", List.of("trungtam", "trung_tam_khu_vuc", "khu_vuc"));

    public static final String THAM_SO = "theo";

    private final String code;
    private final String label;
    private final List<String> aliases;

    NguonViecXepHangChieu(String code, String label, List<String> aliases) {
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

    public static NguonViecXepHangChieu of(String value) {
        return McpCodeEnums.of(NguonViecXepHangChieu.class, THAM_SO, value);
    }

    public static String codes() {
        return McpCodeEnums.codes(NguonViecXepHangChieu.class);
    }
}
