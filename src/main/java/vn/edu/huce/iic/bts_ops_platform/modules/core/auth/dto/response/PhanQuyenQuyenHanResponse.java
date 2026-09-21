package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/** 1 ô đang bật trong ma trận phân quyền — enrich sẵn mã/tên quyền hạn. */
@Data
@Builder
public class PhanQuyenQuyenHanResponse {
    private String quyenMa;
    private UUID quyenHanId;
    private String ma;
    private String ten;
    private Boolean hoatDong;
}
