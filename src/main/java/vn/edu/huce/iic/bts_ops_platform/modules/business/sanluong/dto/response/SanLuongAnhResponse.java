package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response;

import lombok.Data;

import java.time.Instant;

@Data
public class SanLuongAnhResponse {
    private String id;
    private String filename;
    private String label;
    private Boolean done;
    private String url;
    private Instant uploadedAt;
    private String loaiAnh;
    private String hangMucCongViecId;
    private String hangMucChiTietId;
}
