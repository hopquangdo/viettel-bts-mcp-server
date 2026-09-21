package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class ExcelMappingCapNhatRequest {
    private UUID doiTuongQuanLyId;
    private String ten;
    private String ma;
    private String moTa;
    private Boolean hoatDong;
    private Short dongTieuDe;
    private Short dongBatDauDoc;
    private String cotNhomUuTien;
}
