package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class ExcelMappingResponse {
    private UUID id;
    private UUID doiTuongQuanLyId;
    private String tenDoiTuongQuanLy;
    private String maDoiTuongQuanLy;
    private String ten;
    private String ma;
    private String moTa;
    private Boolean hoatDong;
    private Short dongTieuDe;
    private Short dongBatDauDoc;
    private String cotNhomUuTien;
    private UUID nguoiCapNhat;
    private Instant ngayTao;
    private Instant ngayCapNhat;
    private Instant ngayXoa;
    private List<ExcelMappingCotResponse> cotDanhSach;
}
