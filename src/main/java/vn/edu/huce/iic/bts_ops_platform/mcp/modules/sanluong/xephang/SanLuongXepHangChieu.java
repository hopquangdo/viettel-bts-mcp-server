package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang;

import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpCodeEnum;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpCodeEnums;

import java.util.List;

/** Chiều xếp hạng của tool sanluong_xep_hang (tham số {@code theo}) — nguồn duy nhất cho mã, nhãn, alias. */
public enum SanLuongXepHangChieu implements McpCodeEnum {
    NHA_THAU("nha_thau", "Nhà thầu", List.of("nhathau", "contractor")),
    KHU_VUC("khu_vuc", "Khu vực", List.of("khuvuc", "ttkv")),
    TINH("tinh", "Tỉnh/thành", List.of("tinh_thanh", "tinhthanh", "province")),
    HOP_DONG("hop_dong", "Hợp đồng", List.of("hopdong", "contract")),
    LOAI_HOP_DONG("loai_hop_dong", "Loại hợp đồng", List.of("loaihopdong")),
    DOI_TUONG("doi_tuong", "Đối tượng", List.of("doituong", "tram"));

    public static final String THAM_SO = "theo";

    private final String code;
    private final String label;
    private final List<String> aliases;

    SanLuongXepHangChieu(String code, String label, List<String> aliases) {
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

    public static SanLuongXepHangChieu of(String value) {
        return McpCodeEnums.of(SanLuongXepHangChieu.class, THAM_SO, value);
    }

    public static String codes() {
        return McpCodeEnums.codes(SanLuongXepHangChieu.class);
    }
}
