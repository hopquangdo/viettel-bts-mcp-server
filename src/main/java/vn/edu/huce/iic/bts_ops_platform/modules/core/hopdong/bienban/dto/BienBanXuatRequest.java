package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BienBanXuatRequest {
    private List<UUID> hopDongDoiTuongIds;
    private BienBanNguoiKyRequest nguoiKy;
    /** BIEN_BAN_SO_1 | NHAT_KY_THI_CONG | BIEN_BAN_SO_2 | PHU_LUC — rỗng/null nghĩa là xuất đủ cả 4. */
    private List<String> loaiBienBan;
}
