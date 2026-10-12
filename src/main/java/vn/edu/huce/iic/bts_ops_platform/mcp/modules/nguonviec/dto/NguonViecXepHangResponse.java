package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * nguonviec_xep_hang: 1 bảng xếp hạng theo đúng 1 chiều ({@code theo}), tính trên toàn bộ hợp đồng nguồn việc trong kỳ.
 *
 * @param kyBaoCao    "01/01/2026 – 31/12/2026"
 * @param xepHangTheo nhãn chiều, vd "Trung tâm"
 * @param tieuChi     vd "Sản lượng thực hiện, cao xuống thấp"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NguonViecXepHangResponse(
        String kyBaoCao,
        String xepHangTheo,
        String tieuChi,
        List<NguonViecXepHangItem> danhSach,
        String ghiChu) {
}
