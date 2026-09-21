package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;
import vn.edu.huce.iic.bts_ops_platform.common.util.UuidUtils;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.repository.NguoiDungRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LoaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.TrangThaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.KieuDuLieuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LoaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.TrangThaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LienKetBangLabelService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.LienKetBangSupport;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.entity.ChuDauTu;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.repository.ChuDauTuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.KhuVuc;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.entity.NguoiDung;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.KhuVucRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.TinhThanhRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LienKetBangLabelServiceImpl implements LienKetBangLabelService {

    private final KieuDuLieuRepository kieuDuLieuRepository;
    private final TinhThanhRepository tinhThanhRepository;
    private final KhuVucRepository khuVucRepository;
    private final ChuDauTuRepository chuDauTuRepository;
    private final LoaiHopDongRepository loaiHopDongRepository;
    private final KieuHopDongRepository kieuHopDongRepository;
    private final TrangThaiHopDongRepository trangThaiHopDongRepository;
    private final NguoiDungRepository nguoiDungRepository;

    @Override
    public void applyDisplayLabels(
            List<HopDongThuocTinhResponse> values,
            Map<UUID, ThuocTinhHopDongResponse> definitions) {
        if (values == null || values.isEmpty() || definitions == null || definitions.isEmpty()) {
            return;
        }

        Map<String, Set<UUID>> idsByTable = new HashMap<>();
        for (HopDongThuocTinhResponse value : values) {
            if (value == null || value.getGiaTri() == null || value.getGiaTri().isBlank()) {
                continue;
            }
            ThuocTinhHopDongResponse definition = definitions.get(value.getThuocTinhHopDongId());
            String linkTable = resolveLinkTable(definition);
            if (linkTable == null) {
                continue;
            }
            UUID linkedId = UuidUtils.parseUuid(value.getGiaTri());
            if (linkedId == null) {
                continue;
            }
            idsByTable.computeIfAbsent(linkTable, ignored -> new HashSet<>()).add(linkedId);
        }

        Map<String, Map<UUID, String>> labelsByTable = batchResolve(idsByTable);

        for (HopDongThuocTinhResponse value : values) {
            if (value == null || value.getGiaTri() == null || value.getGiaTri().isBlank()) {
                continue;
            }
            ThuocTinhHopDongResponse definition = definitions.get(value.getThuocTinhHopDongId());
            String linkTable = resolveLinkTable(definition);
            if (linkTable == null) {
                continue;
            }
            UUID linkedId = UuidUtils.parseUuid(value.getGiaTri());
            if (linkedId == null) {
                continue;
            }
            String label = labelsByTable
                    .getOrDefault(linkTable, Map.of())
                    .get(linkedId);
            if (label != null && !label.isBlank()) {
                value.setGiaTri(label);
            }
        }
    }

    private Map<String, Map<UUID, String>> batchResolve(Map<String, Set<UUID>> idsByTable) {
        Map<String, Map<UUID, String>> result = new HashMap<>();
        for (Map.Entry<String, Set<UUID>> entry : idsByTable.entrySet()) {
            String table = entry.getKey();
            Set<UUID> ids = entry.getValue();
            if (ids == null || ids.isEmpty()) {
                continue;
            }
            result.put(table, resolveTableLabels(table, ids));
        }
        return result;
    }

    private Map<UUID, String> resolveTableLabels(String table, Collection<UUID> ids) {
        return switch (table) {
            case "tinh_thanh" -> toLabelMap(tinhThanhRepository.findAllById(ids), entity -> entity.getTen());
            case "khu_vuc" -> toLabelMap(khuVucRepository.findAllById(ids), KhuVuc::getTen);
            case "chu_dau_tu" -> chuDauTuRepository.findAllById(ids).stream()
                    .collect(Collectors.toMap(ChuDauTu::getId, item -> formatMaTen(item.getMa(), item.getTen())));
            case "loai_hop_dong" -> loaiHopDongRepository.findAllById(ids).stream()
                    .collect(Collectors.toMap(LoaiHopDong::getId, item -> formatMaTen(item.getMa(), item.getTen())));
            case "kieu_hop_dong" -> kieuHopDongRepository.findAllById(ids).stream()
                    .collect(Collectors.toMap(KieuHopDong::getId, item -> formatMaTen(item.getMa(), item.getTen())));
            case "trang_thai_hop_dong" -> toLabelMap(
                    trangThaiHopDongRepository.findAllById(ids), TrangThaiHopDong::getTen);
            case "nguoi_dung" -> nguoiDungRepository.findAllById(ids).stream()
                    .collect(Collectors.toMap(NguoiDung::getId, this::formatNguoiDung));
            default -> Map.of();
        };
    }

    private <T extends AuditableEntity> Map<UUID, String> toLabelMap(
            Iterable<T> items,
            Function<T, String> labelFn) {
        Map<UUID, String> labels = new HashMap<>();
        for (T item : items) {
            if (item == null || item.getId() == null) {
                continue;
            }
            String label = labelFn.apply(item);
            if (label != null && !label.isBlank()) {
                labels.put(item.getId(), label);
            }
        }
        return labels;
    }

    private String resolveLinkTable(ThuocTinhHopDongResponse definition) {
        if (definition == null) {
            return null;
        }
        if (definition.getLienKetBang() != null
                && LienKetBangSupport.isKnownLinkTable(definition.getLienKetBang())) {
            return definition.getLienKetBang().trim();
        }
        return LienKetBangSupport.resolveLienKetBang(definition.getKieuDuLieuId(), kieuDuLieuRepository);
    }

    private String formatMaTen(String ma, String ten) {
        String normalizedTen = ten == null || ten.isBlank() ? "—" : ten.trim();
        if (ma != null && !ma.isBlank()) {
            return ma.trim() + " — " + normalizedTen;
        }
        return normalizedTen;
    }

    private String formatNguoiDung(NguoiDung user) {
        String hoTen = user.getHoTen() == null || user.getHoTen().isBlank() ? "—" : user.getHoTen().trim();
        String tenDangNhap = user.getTenDangNhap();
        if (tenDangNhap != null && !tenDangNhap.isBlank()) {
            return hoTen + " (" + tenDangNhap.trim() + ")";
        }
        return hoTen;
    }
}
