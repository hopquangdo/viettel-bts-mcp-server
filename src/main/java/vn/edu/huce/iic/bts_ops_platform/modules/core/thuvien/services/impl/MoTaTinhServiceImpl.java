package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.MoTaTinhRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.MoTaTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.MoTaTinh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.TinhThanh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.exception.ThuVienErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.MoTaTinhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.TinhThanhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.services.MoTaTinhService;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MoTaTinhServiceImpl implements MoTaTinhService {

    private final TinhThanhRepository tinhThanhRepository;
    private final MoTaTinhRepository moTaTinhRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MoTaTinhResponse> list() {
        Map<UUID, MoTaTinh> moTaByTinhThanhId = moTaTinhRepository.findByNgayXoaIsNull().stream()
                .collect(java.util.stream.Collectors.toMap(MoTaTinh::getTinhThanhId, m -> m, (a, b) -> a));

        // Nhóm theo tinhThanhId (nhóm ổn định) — ưu tiên bản ghi tên hiện tại (laTinhCu=false) làm đại diện.
        Map<UUID, TinhThanh> representative = new LinkedHashMap<>();
        tinhThanhRepository.findByNgayXoaIsNullOrderByMaAsc().stream()
                .filter(TinhThanh::getHoatDong)
                .forEach(tinh -> representative.merge(tinh.getTinhThanhId(), tinh,
                        (existing, candidate) -> Boolean.FALSE.equals(existing.getLaTinhCu()) ? existing : candidate));

        return representative.values().stream()
                .sorted(Comparator.comparing(TinhThanh::getMa, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(tinh -> {
                    MoTaTinh moTa = moTaByTinhThanhId.get(tinh.getTinhThanhId());
                    return MoTaTinhResponse.builder()
                            .tinhThanhId(tinh.getTinhThanhId())
                            .maTinh(tinh.getMa())
                            .tenTinh(tinh.getTen())
                            .viTriDiaLy(moTa != null ? moTa.getViTriDiaLy() : null)
                            .dacDiemDiaHinh(moTa != null ? moTa.getDacDiemDiaHinh() : null)
                            .khiHau(moTa != null ? moTa.getKhiHau() : null)
                            .dieuKienKinhTe(moTa != null ? moTa.getDieuKienKinhTe() : null)
                            .coMoTa(moTa != null)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public MoTaTinhResponse upsert(UUID tinhThanhId, MoTaTinhRequest request) {
        TinhThanh tinh = tinhThanhRepository.findByNgayXoaIsNullOrderByMaAsc().stream()
                .filter(t -> tinhThanhId.equals(t.getTinhThanhId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ThuVienErrorCode.TINH_THANH_NOT_FOUND, "Không tìm thấy tỉnh/thành"));

        MoTaTinh entity = moTaTinhRepository.findByTinhThanhIdAndNgayXoaIsNull(tinhThanhId)
                .orElseGet(() -> {
                    MoTaTinh created = new MoTaTinh();
                    created.setTinhThanhId(tinhThanhId);
                    return created;
                });
        entity.setViTriDiaLy(blankToNull(request.getViTriDiaLy()));
        entity.setDacDiemDiaHinh(blankToNull(request.getDacDiemDiaHinh()));
        entity.setKhiHau(blankToNull(request.getKhiHau()));
        entity.setDieuKienKinhTe(blankToNull(request.getDieuKienKinhTe()));
        MoTaTinh saved = moTaTinhRepository.save(entity);

        return MoTaTinhResponse.builder()
                .tinhThanhId(tinh.getTinhThanhId())
                .maTinh(tinh.getMa())
                .tenTinh(tinh.getTen())
                .viTriDiaLy(saved.getViTriDiaLy())
                .dacDiemDiaHinh(saved.getDacDiemDiaHinh())
                .khiHau(saved.getKhiHau())
                .dieuKienKinhTe(saved.getDieuKienKinhTe())
                .coMoTa(true)
                .build();
    }

    private static String blankToNull(String value) {
        return value != null && !value.isBlank() ? value.trim() : null;
    }
}
