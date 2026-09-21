package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;

import java.util.Optional;

public interface LienKetBangImportResolver {

    Optional<ResolvedLinkValue> resolve(ThuocTinhResponse thuocTinh, String rawValue);

    Optional<ResolvedLinkValue> resolve(ThuocTinhHopDongResponse thuocTinh, String rawValue);

    String resolveLinkTable(ThuocTinhResponse thuocTinh);

    String resolveLinkTable(ThuocTinhHopDongResponse thuocTinh);

    void clearCache();

    /** Nạp cache tra cứu liên kết bảng trước khi xử lý hàng loạt dòng import. */
    void preload(ThuocTinhResponse thuocTinh);
}
