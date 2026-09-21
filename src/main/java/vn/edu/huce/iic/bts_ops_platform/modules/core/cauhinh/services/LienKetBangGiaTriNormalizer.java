package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;

import java.util.Optional;
import java.util.UUID;

public interface LienKetBangGiaTriNormalizer {

    String normalizeDoiTuongGiaTri(UUID thuocTinhId, String rawValue);

    String normalizeHopDongGiaTri(UUID thuocTinhHopDongId, String rawValue);

    String normalizeDoiTuongGiaTri(ThuocTinhResponse thuocTinh, String rawValue);

    String normalizeHopDongGiaTri(ThuocTinhHopDongResponse thuocTinh, String rawValue);

    /**
     * Chuẩn hóa giá trị import: trả về empty nếu không bắt buộc và không resolve được.
     */
    Optional<String> normalizeDoiTuongGiaTriForImport(ThuocTinhResponse thuocTinh, String rawValue);

    Optional<String> resolveDoiTuongPreviewValue(ThuocTinhResponse thuocTinh, String rawValue);
}
