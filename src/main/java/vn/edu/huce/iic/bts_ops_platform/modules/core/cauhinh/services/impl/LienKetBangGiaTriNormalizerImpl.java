package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LienKetBangGiaTriNormalizer;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LienKetBangImportResolver;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ResolvedLinkValue;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhHopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LienKetBangGiaTriNormalizerImpl implements LienKetBangGiaTriNormalizer {

    private final ThuocTinhService thuocTinhService;
    private final ThuocTinhHopDongService thuocTinhHopDongService;
    private final LienKetBangImportResolver lienKetBangImportResolver;

    @Override
    public String normalizeDoiTuongGiaTri(UUID thuocTinhId, String rawValue) {
        return normalizeDoiTuongGiaTri(thuocTinhService.getById(thuocTinhId), rawValue);
    }

    @Override
    public String normalizeHopDongGiaTri(UUID thuocTinhHopDongId, String rawValue) {
        return normalizeHopDongGiaTri(thuocTinhHopDongService.getById(thuocTinhHopDongId), rawValue);
    }

    @Override
    public String normalizeDoiTuongGiaTri(ThuocTinhResponse thuocTinh, String rawValue) {
        return normalizeForStorage(
                thuocTinh,
                rawValue,
                lienKetBangImportResolver.resolveLinkTable(thuocTinh),
                (definition, value) -> lienKetBangImportResolver.resolve(definition, value),
                HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID);
    }

    @Override
    public String normalizeHopDongGiaTri(ThuocTinhHopDongResponse thuocTinh, String rawValue) {
        return normalizeForStorage(
                thuocTinh,
                rawValue,
                lienKetBangImportResolver.resolveLinkTable(thuocTinh),
                (definition, value) -> lienKetBangImportResolver.resolve(definition, value),
                HopDongErrorCode.HOP_DONG_THUOC_TINH_INVALID);
    }

    @Override
    public Optional<String> normalizeDoiTuongGiaTriForImport(ThuocTinhResponse thuocTinh, String rawValue) {
        if (rawValue == null) {
            return Optional.empty();
        }
        String trimmed = rawValue.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        if (lienKetBangImportResolver.resolveLinkTable(thuocTinh) == null) {
            return Optional.of(trimmed);
        }
        Optional<ResolvedLinkValue> resolved = lienKetBangImportResolver.resolve(thuocTinh, trimmed);
        if (resolved.isPresent()) {
            return Optional.of(resolved.get().id().toString());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> resolveDoiTuongPreviewValue(ThuocTinhResponse thuocTinh, String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return Optional.empty();
        }
        if (lienKetBangImportResolver.resolveLinkTable(thuocTinh) == null) {
            return Optional.of(rawValue.trim());
        }
        return lienKetBangImportResolver.resolve(thuocTinh, rawValue.trim())
                .map(ResolvedLinkValue::displayLabel);
    }

    private <T> String normalizeForStorage(
            T definition,
            String rawValue,
            String linkTable,
            LinkResolver<T> resolver,
            HopDongErrorCode errorCode) {
        if (rawValue == null) {
            return null;
        }
        String trimmed = rawValue.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        if (linkTable == null) {
            return trimmed;
        }
        return resolver.resolve(definition, trimmed)
                .map(value -> value.id().toString())
                .orElseThrow(() -> unresolvedLinkError(labelOf(definition), trimmed, errorCode));
    }

    private String labelOf(Object definition) {
        if (definition instanceof ThuocTinhResponse thuocTinh) {
            return thuocTinh.getTen();
        }
        if (definition instanceof ThuocTinhHopDongResponse thuocTinhHopDong) {
            return thuocTinhHopDong.getTen();
        }
        return "thuộc tính";
    }

    private AppException unresolvedLinkError(String label, String rawValue, HopDongErrorCode errorCode) {
        String safeLabel = label != null && !label.isBlank() ? label.trim() : "thuộc tính";
        return new AppException(
                errorCode,
                "Không tìm thấy " + safeLabel + " khớp với \"" + rawValue.trim() + "\"");
    }

    @FunctionalInterface
    private interface LinkResolver<T> {
        Optional<ResolvedLinkValue> resolve(T definition, String rawValue);
    }
}
