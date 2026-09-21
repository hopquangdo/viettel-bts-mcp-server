package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorScopeService;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.constants.TramThiCongBuocConstants;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.dto.response.TramThiCongBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.entity.HoSoTramTepDinhKem;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.entity.TramThiCongBuoc;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.repository.HoSoTramTepDinhKemRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.repository.TramThiCongBuocRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.service.HoSoTramService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.service.TramThiCongBuocService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.entity.BienBanPhatSinh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.repository.BienBanPhatSinhRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TramThiCongBuocServiceImpl implements TramThiCongBuocService {

    private static final String PHAT_SINH_DA_DUYET = "da_duyet";

    private final TramThiCongBuocRepository tramThiCongBuocRepository;
    private final HoSoTramTepDinhKemRepository hoSoTramTepDinhKemRepository;
    private final BienBanPhatSinhRepository bienBanPhatSinhRepository;
    private final HoSoTramService hoSoTramService;
    private final ContractorScopeService contractorScopeService;

    @Override
    @Transactional(readOnly = true)
    public List<TramThiCongBuocResponse> list(UUID hopDongId, UUID hopDongDoiTuongId) {
        hoSoTramService.list(hopDongId, hopDongDoiTuongId);
        Map<String, TramThiCongBuoc> stored = tramThiCongBuocRepository
                .findByHopDongDoiTuongIdAndNgayXoaIsNullOrderByMaBuocAsc(hopDongDoiTuongId)
                .stream()
                .collect(Collectors.toMap(TramThiCongBuoc::getMaBuoc, Function.identity(), (a, b) -> a));

        List<HoSoTramTepDinhKem> uploads = hoSoTramTepDinhKemRepository
                .findByHopDongDoiTuongIdAndNgayXoaIsNullAndHoatDongTrueOrderByNgayTaoDesc(hopDongDoiTuongId);
        List<BienBanPhatSinh> phatSinhList = bienBanPhatSinhRepository
                .findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDongId)
                .stream()
                .filter(item -> hopDongDoiTuongId.equals(item.getHopDongDoiTuongId()))
                .toList();

        List<TramThiCongBuocResponse> result = new ArrayList<>();
        for (int i = 0; i < TramThiCongBuocConstants.STEP_ORDER.size(); i++) {
            String maBuoc = TramThiCongBuocConstants.STEP_ORDER.get(i);
            TramThiCongBuoc entity = stored.get(maBuoc);
            String trangThai = entity != null ? entity.getTrangThai() : TramThiCongBuocConstants.CHUA_CO;
            boolean previousApproved = i == 0 || isApproved(stored.get(TramThiCongBuocConstants.STEP_ORDER.get(i - 1)));
            boolean ready = isStepReady(maBuoc, uploads, phatSinhList);
            boolean editable = previousApproved
                    && !TramThiCongBuocConstants.DA_DUYET.equals(trangThai)
                    && !TramThiCongBuocConstants.CHO_DUYET.equals(trangThai);

            result.add(TramThiCongBuocResponse.builder()
                    .maBuoc(maBuoc)
                    .ten(TramThiCongBuocConstants.STEP_LABELS.get(maBuoc))
                    .thuTu(i + 1)
                    .trangThai(trangThai)
                    .lyDoTuChoi(entity != null ? entity.getLyDoTuChoi() : null)
                    .ngayDuyet(entity != null ? entity.getNgayDuyet() : null)
                    .moKhoa(previousApproved)
                    .coTheTaiLen(editable)
                    .coTheGuiDuyet(previousApproved && ready && editable)
                    .coTheDuyet(TramThiCongBuocConstants.CHO_DUYET.equals(trangThai))
                    .build());
        }
        return result;
    }

    @Override
    @Transactional
    public TramThiCongBuocResponse guiDuyet(UUID hopDongId, UUID hopDongDoiTuongId, String maBuoc) {
        validateMaBuoc(maBuoc);
        List<TramThiCongBuocResponse> current = list(hopDongId, hopDongDoiTuongId);
        TramThiCongBuocResponse step = findStep(current, maBuoc);
        if (!Boolean.TRUE.equals(step.getCoTheGuiDuyet())) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Chưa đủ điều kiện gửi duyệt bước này hoặc bước trước chưa được duyệt");
        }

        TramThiCongBuoc entity = findOrCreate(hopDongDoiTuongId, maBuoc);
        entity.setTrangThai(TramThiCongBuocConstants.CHO_DUYET);
        entity.setLyDoTuChoi(null);
        entity.setNguoiDuyetId(null);
        entity.setNgayDuyet(null);
        tramThiCongBuocRepository.save(entity);
        return list(hopDongId, hopDongDoiTuongId).stream()
                .filter(item -> maBuoc.equals(item.getMaBuoc()))
                .findFirst()
                .orElseThrow();
    }

    @Override
    @Transactional
    public TramThiCongBuocResponse duyet(UUID hopDongId, UUID hopDongDoiTuongId, String maBuoc) {
        contractorScopeService.assertNotContractorForQualityAudit();
        validateMaBuoc(maBuoc);
        TramThiCongBuoc entity = tramThiCongBuocRepository
                .findByHopDongDoiTuongIdAndMaBuocAndNgayXoaIsNull(hopDongDoiTuongId, maBuoc)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Chưa gửi duyệt bước này"));
        if (!TramThiCongBuocConstants.CHO_DUYET.equals(entity.getTrangThai())) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Bước không ở trạng thái chờ duyệt");
        }

        if (TramThiCongBuocConstants.PHAT_SINH_SAU_TC.equals(maBuoc)) {
            validatePhatSinhApproved(hopDongId, hopDongDoiTuongId);
        }

        JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
        entity.setTrangThai(TramThiCongBuocConstants.DA_DUYET);
        entity.setNguoiDuyetId(user.id());
        entity.setNgayDuyet(Instant.now());
        entity.setLyDoTuChoi(null);
        tramThiCongBuocRepository.save(entity);
        return list(hopDongId, hopDongDoiTuongId).stream()
                .filter(item -> maBuoc.equals(item.getMaBuoc()))
                .findFirst()
                .orElseThrow();
    }

    @Override
    @Transactional
    public TramThiCongBuocResponse tuChoi(UUID hopDongId, UUID hopDongDoiTuongId, String maBuoc, String lyDoTuChoi) {
        contractorScopeService.assertNotContractorForQualityAudit();
        validateMaBuoc(maBuoc);
        if (lyDoTuChoi == null || lyDoTuChoi.isBlank()) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Cần nhập lý do từ chối");
        }
        TramThiCongBuoc entity = tramThiCongBuocRepository
                .findByHopDongDoiTuongIdAndMaBuocAndNgayXoaIsNull(hopDongDoiTuongId, maBuoc)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Chưa gửi duyệt bước này"));
        if (!TramThiCongBuocConstants.CHO_DUYET.equals(entity.getTrangThai())) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Bước không ở trạng thái chờ duyệt");
        }
        entity.setTrangThai(TramThiCongBuocConstants.TU_CHOI);
        entity.setLyDoTuChoi(lyDoTuChoi.trim());
        entity.setNguoiDuyetId(null);
        entity.setNgayDuyet(null);
        tramThiCongBuocRepository.save(entity);
        return list(hopDongId, hopDongDoiTuongId).stream()
                .filter(item -> maBuoc.equals(item.getMaBuoc()))
                .findFirst()
                .orElseThrow();
    }

    private TramThiCongBuoc findOrCreate(UUID hopDongDoiTuongId, String maBuoc) {
        return tramThiCongBuocRepository
                .findByHopDongDoiTuongIdAndMaBuocAndNgayXoaIsNull(hopDongDoiTuongId, maBuoc)
                .orElseGet(() -> {
                    TramThiCongBuoc created = new TramThiCongBuoc();
                    created.setHopDongDoiTuongId(hopDongDoiTuongId);
                    created.setMaBuoc(maBuoc);
                    created.setTrangThai(TramThiCongBuocConstants.CHUA_CO);
                    return created;
                });
    }

    private boolean isApproved(TramThiCongBuoc entity) {
        return entity != null && TramThiCongBuocConstants.DA_DUYET.equals(entity.getTrangThai());
    }

    private boolean isStepReady(String maBuoc, List<HoSoTramTepDinhKem> uploads, List<BienBanPhatSinh> phatSinhList) {
        if (TramThiCongBuocConstants.PHAT_SINH_SAU_TC.equals(maBuoc)) {
            return phatSinhList.stream().anyMatch(item -> PHAT_SINH_DA_DUYET.equals(item.getTrangThai()));
        }
        List<String> danhMucList = TramThiCongBuocConstants.STEP_DANH_MUC.get(maBuoc);
        if (danhMucList == null || danhMucList.isEmpty()) {
            return false;
        }
        return danhMucList.stream().allMatch(danhMuc -> uploads.stream().anyMatch(file -> danhMuc.equals(file.getDanhMuc())));
    }

    private void validatePhatSinhApproved(UUID hopDongId, UUID hopDongDoiTuongId) {
        boolean approved = bienBanPhatSinhRepository
                .findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDongId)
                .stream()
                .filter(item -> hopDongDoiTuongId.equals(item.getHopDongDoiTuongId()))
                .anyMatch(item -> PHAT_SINH_DA_DUYET.equals(item.getTrangThai()));
        if (!approved) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Cần duyệt ít nhất một biên bản phát sinh trước khi hoàn tất bước này");
        }
    }

    private TramThiCongBuocResponse findStep(List<TramThiCongBuocResponse> steps, String maBuoc) {
        return steps.stream()
                .filter(item -> maBuoc.equals(item.getMaBuoc()))
                .findFirst()
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Bước không hợp lệ"));
    }

    private void validateMaBuoc(String maBuoc) {
        if (maBuoc == null || !TramThiCongBuocConstants.STEP_ORDER.contains(maBuoc)) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Mã bước thi công không hợp lệ");
        }
    }
}
