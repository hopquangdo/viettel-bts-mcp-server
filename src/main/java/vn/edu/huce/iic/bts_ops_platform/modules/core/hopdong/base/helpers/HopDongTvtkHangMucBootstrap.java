package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LoaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.LoaiHopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucChiTietTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucNhomTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucNhomResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucNhomRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucChiTietService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucNhomService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Hợp đồng thuộc loại tư vấn thiết kế: tạo sẵn nhóm TVTK và 2 hạng mục Khảo sát / Thiết kế.
 */
@Component
@RequiredArgsConstructor
public class HopDongTvtkHangMucBootstrap {

    static final String NHOM_MA = "TVTK";
    static final String NHOM_TEN = "TVTK";
    static final String KHAO_SAT_MA = "KHAO_SAT";
    static final String KHAO_SAT_TEN = "Khảo sát";
    static final String THIET_KE_MA = "THIET_KE";
    static final String THIET_KE_TEN = "Thiết kế";

    private final LoaiHopDongRepository loaiHopDongRepository;
    private final HangMucNhomRepository hangMucNhomRepository;
    private final HangMucNhomService hangMucNhomService;
    private final HangMucChiTietService hangMucChiTietService;

    public void seedDefaultHangMucIfTuVan(HopDong hopDong) {
        if (hopDong == null || hopDong.getId() == null || hopDong.getLoaiHopDongId() == null) {
            return;
        }
        LoaiHopDong loai = loaiHopDongRepository.findByIdAndNgayXoaIsNull(hopDong.getLoaiHopDongId()).orElse(null);
        if (!HeNghiepVuResolver.isTuVanThietKe(loai)) {
            return;
        }
        List<?> existing = hangMucNhomRepository.findByHopDongIdAndNgayXoaIsNull(hopDong.getId());
        if (!existing.isEmpty()) {
            return;
        }

        HangMucNhomTaoRequest nhomRequest = new HangMucNhomTaoRequest();
        nhomRequest.setHopDongId(hopDong.getId());
        nhomRequest.setMa(NHOM_MA);
        nhomRequest.setTen(NHOM_TEN);
        nhomRequest.setThuTu((short) 0);
        nhomRequest.setHoatDong(true);
        HangMucNhomResponse nhom = hangMucNhomService.create(nhomRequest);

        createChiTiet(nhom.getId(), KHAO_SAT_MA, KHAO_SAT_TEN);
        createChiTiet(nhom.getId(), THIET_KE_MA, THIET_KE_TEN);
    }

    private void createChiTiet(UUID hangMucNhomId, String ma, String ten) {
        HangMucChiTietTaoRequest request = new HangMucChiTietTaoRequest();
        request.setHangMucNhomId(hangMucNhomId);
        request.setMa(ma);
        request.setTen(ten);
        request.setKhoiLuong(BigDecimal.ONE);
        request.setDonGia(BigDecimal.ZERO);
        request.setHoatDong(true);
        hangMucChiTietService.create(request);
    }
}
