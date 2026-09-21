package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.response.TepDinhKemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.services.TepDinhKemService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.dto.response.HoSoTramTepDinhKemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.entity.HoSoTramTepDinhKem;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.repository.HoSoTramTepDinhKemRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.service.HoSoTramService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.DanhMucBienBanRepository;
import vn.edu.huce.iic.bts_ops_platform.common.util.UuidUtils;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.repository.HopDongChecklistMucRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HoSoTramServiceImpl implements HoSoTramService {

    private static final String CATEGORY = "ho-so-tram";

    private final HoSoTramTepDinhKemRepository repository;
    private final TepDinhKemService tepDinhKemService;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final DanhMucBienBanRepository danhMucBienBanRepository;
    private final HopDongChecklistMucRepository hopDongChecklistMucRepository;

    private static final String CHECKLIST_DANH_MUC_PREFIX = "CL:";

    @Override
    @Transactional(readOnly = true)
    public List<HoSoTramTepDinhKemResponse> list(UUID hopDongId, UUID hopDongDoiTuongId) {
        requireDoiTuong(hopDongId, hopDongDoiTuongId);
        return repository
                .findByHopDongDoiTuongIdAndNgayXoaIsNullAndHoatDongTrueOrderByNgayTaoDesc(hopDongDoiTuongId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public HoSoTramTepDinhKemResponse upload(
            UUID hopDongId, UUID hopDongDoiTuongId, String danhMuc, MultipartFile file, String ghiChu) {
        requireDoiTuong(hopDongId, hopDongDoiTuongId);
        if (danhMuc == null || !isDanhMucChoPhep(danhMuc, hopDongId)) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Danh mục hồ sơ không hợp lệ: " + danhMuc);
        }

        TepDinhKemResponse tepDinhKem = tepDinhKemService.upload(file, CATEGORY);

        HoSoTramTepDinhKem entity = new HoSoTramTepDinhKem();
        entity.setHopDongDoiTuongId(hopDongDoiTuongId);
        entity.setDanhMuc(danhMuc);
        entity.setTepDinhKemId(tepDinhKem.getId());
        entity.setGhiChu(ghiChu);
        entity.setHoatDong(true);
        repository.save(entity);

        return toResponse(entity, tepDinhKem);
    }

    @Override
    @Transactional
    public void xoa(UUID hopDongId, UUID hopDongDoiTuongId, UUID id) {
        requireDoiTuong(hopDongId, hopDongDoiTuongId);
        HoSoTramTepDinhKem entity = repository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_NOT_FOUND, "Không tìm thấy file"));
        if (!hopDongDoiTuongId.equals(entity.getHopDongDoiTuongId())) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_NOT_FOUND, "Không tìm thấy file");
        }
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        repository.save(entity);
    }

    /** Chỉ cho tải lên thủ công với danh mục còn hoạt động và chưa có mẫu Word tự sinh — loại đã có
     * mẫu (BÁO_CAO_KHAO_SAT, BIEN_BAN_SO_1, NHAT_KY_THI_CONG, BIEN_BAN_SO_2...) đi theo luồng sinh
     * file tự động riêng, không upload thủ công qua đây. */
    private boolean isDanhMucChoPhep(String danhMuc, UUID hopDongId) {
        if (danhMuc.startsWith(CHECKLIST_DANH_MUC_PREFIX)) {
            UUID mucId = UuidUtils.parseUuid(danhMuc.substring(CHECKLIST_DANH_MUC_PREFIX.length()));
            if (mucId == null) {
                return false;
            }
            return hopDongChecklistMucRepository.findByIdAndNgayXoaIsNull(mucId)
                    .filter(muc -> hopDongId.equals(muc.getHopDongId()))
                    .isPresent();
        }
        return danhMucBienBanRepository.findByMaAndNgayXoaIsNull(danhMuc)
                .filter(dm -> Boolean.TRUE.equals(dm.getHoatDong()))
                .filter(dm -> !Boolean.TRUE.equals(dm.getCoMauWord()))
                .isPresent();
    }

    private void requireDoiTuong(UUID hopDongId, UUID hopDongDoiTuongId) {
        HopDongDoiTuongResponse doiTuong = hopDongDoiTuongService.getById(hopDongDoiTuongId);
        if (doiTuong == null || !hopDongId.equals(doiTuong.getHopDongId())) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_NOT_FOUND, "Không tìm thấy đối tượng thuộc hợp đồng này");
        }
    }

    private HoSoTramTepDinhKemResponse toResponse(HoSoTramTepDinhKem entity) {
        TepDinhKemResponse tep = tepDinhKemService.getById(entity.getTepDinhKemId());
        return toResponse(entity, tep);
    }

    private HoSoTramTepDinhKemResponse toResponse(HoSoTramTepDinhKem entity, TepDinhKemResponse tep) {
        return HoSoTramTepDinhKemResponse.builder()
                .id(entity.getId())
                .hopDongDoiTuongId(entity.getHopDongDoiTuongId())
                .danhMuc(entity.getDanhMuc())
                .tenTep(tep.getTenTepGoc() != null ? tep.getTenTepGoc() : tep.getTenTep())
                .url(tep.getUrl())
                .kichThuoc(tep.getKichThuoc())
                .ghiChu(entity.getGhiChu())
                .ngayTao(entity.getNgayTao())
                .build();
    }
}
