package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LuongTrangThaiResponse {

    private UUID id;
    private String ten;
    private String moTa;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
    private List<LuongTrangThaiBuocResponse> buoc;
}
