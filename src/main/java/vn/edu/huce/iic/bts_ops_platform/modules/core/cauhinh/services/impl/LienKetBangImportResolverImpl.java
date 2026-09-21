package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.LinkCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorRoleCodes;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.entity.NguoiDung;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.repository.NguoiDungRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuDuLieuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LoaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.TrangThaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LienKetBangImportResolver;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ResolvedLinkValue;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.LienKetBangSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.repository.ChuDauTuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.Quyen;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository.QuyenRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.KhuVucRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.TinhThanhRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LienKetBangImportResolverImpl implements LienKetBangImportResolver {

    private final KieuDuLieuRepository kieuDuLieuRepository;
    private final TinhThanhRepository tinhThanhRepository;
    private final KhuVucRepository khuVucRepository;
    private final ChuDauTuRepository chuDauTuRepository;
    private final LoaiHopDongRepository loaiHopDongRepository;
    private final KieuHopDongRepository kieuHopDongRepository;
    private final TrangThaiHopDongRepository trangThaiHopDongRepository;
    private final HopDongRepository hopDongRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final QuyenRepository quyenRepository;

    private final Map<String, List<LinkCandidate>> candidateCache = new HashMap<>();

    @Override
    @Transactional(readOnly = true)
    public Optional<ResolvedLinkValue> resolve(ThuocTinhResponse thuocTinh, String rawValue) {
        return resolveInternal(
                thuocTinh != null ? thuocTinh.getLienKetBang() : null,
                thuocTinh != null ? thuocTinh.getKieuDuLieuId() : null,
                thuocTinh != null ? thuocTinh.getTen() : null,
                rawValue);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResolvedLinkValue> resolve(ThuocTinhHopDongResponse thuocTinh, String rawValue) {
        return resolveInternal(
                thuocTinh != null ? thuocTinh.getLienKetBang() : null,
                thuocTinh != null ? thuocTinh.getKieuDuLieuId() : null,
                thuocTinh != null ? thuocTinh.getTen() : null,
                rawValue);
    }

    private Optional<ResolvedLinkValue> resolveInternal(
            String lienKetBang,
            String kieuDuLieuId,
            String thuocTinhTen,
            String rawValue) {
        if (rawValue == null) {
            return Optional.empty();
        }
        String input = rawValue.trim();
        if (input.isEmpty()) {
            return Optional.empty();
        }
        String linkTable = resolveLinkTable(lienKetBang, kieuDuLieuId);
        if (linkTable == null) {
            return Optional.empty();
        }
        List<LinkCandidate> candidates = loadCandidates(linkTable, thuocTinhTen);
        return findFirstMatch(candidates, input);
    }

    @Override
    public String resolveLinkTable(ThuocTinhResponse thuocTinh) {
        if (thuocTinh == null) {
            return null;
        }
        return resolveLinkTable(thuocTinh.getLienKetBang(), thuocTinh.getKieuDuLieuId());
    }

    @Override
    public String resolveLinkTable(ThuocTinhHopDongResponse thuocTinh) {
        if (thuocTinh == null) {
            return null;
        }
        return resolveLinkTable(thuocTinh.getLienKetBang(), thuocTinh.getKieuDuLieuId());
    }

    private String resolveLinkTable(String lienKetBang, String kieuDuLieuId) {
        if (lienKetBang != null && LienKetBangSupport.isKnownLinkTable(lienKetBang)) {
            return lienKetBang.trim();
        }
        if (LienKetBangSupport.isKnownLinkTable(kieuDuLieuId)) {
            return kieuDuLieuId.trim();
        }
        return LienKetBangSupport.resolveLienKetBang(kieuDuLieuId, kieuDuLieuRepository);
    }

    @Override
    public void clearCache() {
        candidateCache.clear();
    }

    @Override
    public void preload(ThuocTinhResponse thuocTinh) {
        if (thuocTinh == null) {
            return;
        }
        String linkTable = resolveLinkTable(thuocTinh);
        if (linkTable != null) {
            loadCandidates(linkTable, thuocTinh.getTen());
        }
    }

    private List<LinkCandidate> loadCandidates(String linkTable, String thuocTinhTen) {
        String cacheKey = linkTable + "::" + normalizeNguoiDungNhom(thuocTinhTen);
        return candidateCache.computeIfAbsent(cacheKey, ignored -> buildCandidates(linkTable, thuocTinhTen));
    }

    private List<LinkCandidate> buildCandidates(String linkTable, String thuocTinhTen) {
        return switch (linkTable) {
            case "tinh_thanh" -> tinhThanhRepository.findByNgayXoaIsNullOrderByMaAsc().stream()
                    .filter(item -> Boolean.TRUE.equals(item.getHoatDong()))
                    .map(item -> new LinkCandidate(
                            item.getId(),
                            item.getMa(),
                            item.getTen(),
                            item.getTen()))
                    .toList();
            case "khu_vuc" -> khuVucRepository.findByNgayXoaIsNull().stream()
                    .filter(item -> Boolean.TRUE.equals(item.getHoatDong()))
                    .map(item -> new LinkCandidate(
                            item.getId(),
                            item.getMa(),
                            item.getTen(),
                            item.getTen()))
                    .toList();
            case "chu_dau_tu" -> chuDauTuRepository.findByNgayXoaIsNull().stream()
                    .filter(item -> Boolean.TRUE.equals(item.getHoatDong()))
                    .map(item -> new LinkCandidate(
                            item.getId(),
                            item.getMa(),
                            item.getTen(),
                            item.getTen()))
                    .toList();
            case "loai_hop_dong" -> loaiHopDongRepository.findByNgayXoaIsNull().stream()
                    .filter(item -> Boolean.TRUE.equals(item.getHoatDong()))
                    .map(item -> new LinkCandidate(
                            item.getId(),
                            item.getMa(),
                            item.getTen(),
                            item.getTen()))
                    .toList();
            case "kieu_hop_dong" -> kieuHopDongRepository.findByNgayXoaIsNull().stream()
                    .filter(item -> Boolean.TRUE.equals(item.getHoatDong()))
                    .map(item -> new LinkCandidate(
                            item.getId(),
                            item.getMa(),
                            item.getTen(),
                            item.getTen()))
                    .toList();
            case "trang_thai_hop_dong" -> trangThaiHopDongRepository.findByNgayXoaIsNull().stream()
                    .filter(item -> Boolean.TRUE.equals(item.getHoatDong()))
                    .map(item -> new LinkCandidate(
                            item.getId(),
                            item.getMa(),
                            item.getTen(),
                            item.getTen()))
                    .toList();
            case "nguoi_dung" -> buildNguoiDungCandidates(thuocTinhTen);
            case "hop_dong" -> hopDongRepository.findByNgayXoaIsNull().stream()
                    .filter(item -> Boolean.TRUE.equals(item.getHoatDong()))
                    .map(this::toHopDongCandidate)
                    .toList();
            default -> List.of();
        };
    }

    private LinkCandidate toHopDongCandidate(HopDong item) {
        String ma = item.getMaHopDong() != null ? item.getMaHopDong().trim() : "";
        String ten = item.getTen() != null ? item.getTen().trim() : "";
        String label = !ten.isEmpty() ? ten : ma;
        return new LinkCandidate(item.getId(), ma, ten, label);
    }

    private List<LinkCandidate> buildNguoiDungCandidates(String thuocTinhTen) {
        UUID contractorQuyenId = quyenRepository.findByMaIgnoreCase(ContractorRoleCodes.MA)
                .map(Quyen::getId)
                .orElse(null);
        String nhom = normalizeNguoiDungNhom(thuocTinhTen);
        List<LinkCandidate> candidates = new ArrayList<>();
        for (NguoiDung user : nguoiDungRepository.findByNgayXoaIsNull()) {
            if (!Boolean.TRUE.equals(user.getHoatDong())) {
                continue;
            }
            boolean isContractor = contractorQuyenId != null && contractorQuyenId.equals(user.getQuyenId());
            if ("nha_thau".equals(nhom) && !isContractor) {
                continue;
            }
            if ("nhan_vien".equals(nhom) && isContractor) {
                continue;
            }
            String hoTen = user.getHoTen() != null ? user.getHoTen().trim() : "";
            String tenDangNhap = user.getTenDangNhap() != null ? user.getTenDangNhap().trim() : "";
            candidates.add(new LinkCandidate(user.getId(), tenDangNhap, hoTen, hoTen));
        }
        return candidates;
    }

    private Optional<ResolvedLinkValue> findFirstMatch(List<LinkCandidate> candidates, String input) {
        try {
            UUID id = UUID.fromString(input);
            for (LinkCandidate candidate : candidates) {
                if (candidate.id().equals(id)) {
                    return Optional.of(new ResolvedLinkValue(candidate.id(), candidate.displayLabel()));
                }
            }
        } catch (IllegalArgumentException ignored) {
            // not a UUID
        }

        String[] parts = input.split("\\s*[—\\-]\\s*", 2);
        if (parts.length == 2) {
            String maPart = normalize(parts[0]);
            String tenPart = normalize(parts[1]);
            for (LinkCandidate candidate : candidates) {
                if (normalize(candidate.ma()).equals(maPart) || normalize(candidate.ten()).equals(tenPart)) {
                    return Optional.of(new ResolvedLinkValue(candidate.id(), candidate.displayLabel()));
                }
            }
        }

        String normalized = normalize(input);
        for (LinkCandidate candidate : candidates) {
            if (!normalize(candidate.ma()).isEmpty() && normalize(candidate.ma()).equals(normalized)) {
                return Optional.of(new ResolvedLinkValue(candidate.id(), candidate.displayLabel()));
            }
        }
        for (LinkCandidate candidate : candidates) {
            if (!normalize(candidate.ten()).isEmpty() && normalize(candidate.ten()).equals(normalized)) {
                return Optional.of(new ResolvedLinkValue(candidate.id(), candidate.displayLabel()));
            }
        }
        for (LinkCandidate candidate : candidates) {
            if (!normalize(candidate.ten()).isEmpty() && normalize(candidate.ten()).contains(normalized)) {
                return Optional.of(new ResolvedLinkValue(candidate.id(), candidate.displayLabel()));
            }
        }
        for (LinkCandidate candidate : candidates) {
            if (!normalize(candidate.ma()).isEmpty() && normalize(candidate.ma()).contains(normalized)) {
                return Optional.of(new ResolvedLinkValue(candidate.id(), candidate.displayLabel()));
            }
        }
        return Optional.empty();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeNguoiDungNhom(String thuocTinhTen) {
        String title = thuocTinhTen == null ? "" : thuocTinhTen.trim().toLowerCase(Locale.ROOT);
        if (title.contains("nhà thầu") || title.contains("nha thau")) {
            return "nha_thau";
        }
        if (title.contains("phụ trách")
                || title.contains("phu trach")
                || title.contains("nhân viên")
                || title.contains("nhan vien")) {
            return "nhan_vien";
        }
        return "all";
    }

}
