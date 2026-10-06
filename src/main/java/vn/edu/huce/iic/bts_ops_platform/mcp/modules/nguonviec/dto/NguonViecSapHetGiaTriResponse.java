package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * nguonviec_sap_het_gia_tri: hợp đồng nguồn việc còn ít giá trị được giao (tỷ lệ còn lại dưới ngưỡng), tối đa 10 dòng,
 * ít giá trị còn lại nhất lên đầu.
 *
 * @param kyBaoCao     "01/01/2026 – 31/12/2026"
 * @param nguongConLai "10%"
 * @param tongSo       số hợp đồng trả về
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NguonViecSapHetGiaTriResponse(
        String kyBaoCao,
        String nguongConLai,
        int tongSo,
        List<NguonViecSapHetGiaTriItem> danhSach,
        String ghiChu) {
}
