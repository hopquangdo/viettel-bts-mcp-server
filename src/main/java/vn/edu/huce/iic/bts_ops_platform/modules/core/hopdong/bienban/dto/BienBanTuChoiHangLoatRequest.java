package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BienBanTuChoiHangLoatRequest {
    private List<UUID> ids;
    private String lyDo;
}
