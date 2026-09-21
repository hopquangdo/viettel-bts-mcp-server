package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class ExcelMappingTaoRequest {
    private UUID doiTuongQuanLyId;

    @NotBlank
    private String ten;

    @NotBlank
    private String ma;

    private String moTa;

    private Boolean hoatDong;

    /** Dòng tiêu đề cột (mặc định 1). */
    private Short dongTieuDe;

    /** Dòng bắt đầu đọc dữ liệu (mặc định 2). */
    private Short dongBatDauDoc;
}
