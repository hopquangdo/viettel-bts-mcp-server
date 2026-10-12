package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpCodeEnum;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpCodeEnums;

import java.util.List;

/**
 * Trạng thái báo sản lượng của nhà thầu trong kỳ (tham số {@code trangThai} của sanluong_nha_thau_bao_cao).
 * Alias reported/not_reported giữ tương thích với cách gọi cũ ({@code status}).
 */
public enum SanLuongTrangThaiBaoCao implements McpCodeEnum {
    DA_BAO("da_bao", "Đã báo sản lượng", List.of("reported", "da_bao_cao", "da_bao_san_luong")),
    CHUA_BAO("chua_bao", "Chưa báo sản lượng", List.of("not_reported", "chua_bao_cao", "chua_bao_san_luong"));

    public static final String THAM_SO = "trangThai";

    private final String code;
    private final String label;
    private final List<String> aliases;

    SanLuongTrangThaiBaoCao(String code, String label, List<String> aliases) {
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

    public static SanLuongTrangThaiBaoCao of(String value) {
        return McpCodeEnums.of(SanLuongTrangThaiBaoCao.class, THAM_SO, value);
    }

    public static String codes() {
        return McpCodeEnums.codes(SanLuongTrangThaiBaoCao.class);
    }
}
