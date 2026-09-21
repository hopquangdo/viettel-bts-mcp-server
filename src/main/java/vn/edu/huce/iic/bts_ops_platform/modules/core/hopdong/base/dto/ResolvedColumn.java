package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;

import java.util.UUID;

public record ResolvedColumn(
        UUID thuocTinhId,
        String fieldKey,
        String label,
        boolean required,
        boolean primaryKey,
        ThuocTinhResponse thuocTinh,
        String lienKetBang) {

    public boolean linkColumn() {
        return lienKetBang != null && !lienKetBang.isBlank();
    }
}
