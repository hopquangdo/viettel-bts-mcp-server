package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/** Đồng bộ (thay cả bộ) danh sách quyền hạn của 1 vai trò trong ma trận phân quyền. */
@Getter
@Setter
public class PhanQuyenQuyenHanDongBoRequest {
    @NotBlank
    private String quyenMa;
    private List<UUID> quyenHanIds;
}
