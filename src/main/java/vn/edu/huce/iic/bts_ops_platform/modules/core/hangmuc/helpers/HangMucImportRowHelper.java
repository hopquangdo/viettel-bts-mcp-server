package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucChiTiet;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucCongViec;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.HangMucImportMappingFields;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.LeafRow;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.RowQuantities;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HangMucImportRowHelper {

    private final HangMucImportFieldResolver fieldResolver;

    /** Chỉ Hạng mục → 2 cấp; có Công tác → 3 cấp. */
    public LeafRow resolveLeafRow(
            Map<String, Object> row,
            HangMucImportMappingFields fields,
            String tenHangMuc) {
        String tenCongTac = fieldResolver.readString(row, fields.congTac(), HangMucImportAttributeRole.CONG_TAC);
        boolean hasCongTac = !HangMucImportTextHelper.isBlank(tenCongTac);
        boolean hasHangMuc = !HangMucImportTextHelper.isBlank(tenHangMuc);
        if (hasCongTac && hasHangMuc) {
            return new LeafRow(tenCongTac, tenHangMuc, false);
        }
        if (hasHangMuc) {
            return new LeafRow(tenHangMuc, null, true);
        }
        if (hasCongTac) {
            return new LeafRow(tenCongTac, null, false);
        }
        return new LeafRow(null, null, false);
    }

    public RowQuantities readRowQuantities(Map<String, Object> row, HangMucImportMappingFields fields) {
        BigDecimal donGia = fieldResolver.readDecimal(row, fields.donGia(), HangMucImportAttributeRole.DON_GIA);
        BigDecimal khoiLuong = fieldResolver.readDecimal(row, fields.khoiLuong(), HangMucImportAttributeRole.KHOI_LUONG);
        return new RowQuantities(
                donGia != null ? donGia : BigDecimal.ZERO,
                khoiLuong != null ? khoiLuong : BigDecimal.ONE,
                fieldResolver.readString(row, fields.donVi(), HangMucImportAttributeRole.DON_VI),
                fieldResolver.readString(row, fields.viTriThiCong(), HangMucImportAttributeRole.VI_TRI_THI_CONG),
                fieldResolver.readShort(row, fields.stt(), HangMucImportAttributeRole.STT));
    }

    public String readMappedMa(Map<String, Object> row, HangMucImportMappingFields fields) {
        String ma = fieldResolver.readString(row, fields.maCongTac(), HangMucImportAttributeRole.MA_CONG_TAC);
        if (HangMucImportTextHelper.isBlank(ma)) {
            ma = fieldResolver.readString(row, fields.maImport(), HangMucImportAttributeRole.MA_IMPORT);
        }
        if (HangMucImportTextHelper.isBlank(ma)) {
            return null;
        }
        return HangMucImportMaHelper.normalizeMappedMa(ma);
    }

    /** Mã từ Excel nếu có mapping; không thì gen HM{n}/CV{n} và đảm bảo không trùng. */
    public String resolveImportMa(
            HangMucImportContext ctx,
            Map<String, Object> row,
            HangMucImportMappingFields fields,
            ImportMaPrefix prefix,
            boolean useMappedMa) {
        String mapped = useMappedMa ? readMappedMa(row, fields) : null;
        if (mapped != null) {
            return EntityFilter.normalizeCode(mapped);
        }
        String base = ctx.nextAutoMa(prefix);
        return prefix == ImportMaPrefix.HM
                ? ctx.allocateUniqueChiTietMa(base)
                : ctx.allocateUniqueCongViecMa(base);
    }

    public HangMucChiTiet newChiTiet(
            UUID groupId,
            String ma,
            String ten,
            BigDecimal donGia,
            BigDecimal khoiLuong) {
        HangMucChiTiet entity = new HangMucChiTiet();
        entity.setHangMucNhomId(groupId);
        entity.setMa(ma);
        entity.setTen(ten);
        entity.setDonGia(donGia != null ? donGia : BigDecimal.ZERO);
        // KL trống để NULL (không ép 0): calculator fallback KL theo map sản lượng khi hạng mục
        // chưa nhập — ép 0 từng làm tong_thanh_tien_thi_cong = 0 dù đã import sản lượng.
        entity.setKhoiLuong(khoiLuong);
        entity.setTrangThai(HangMucImportFields.TRANG_THAI_CHI_TIET_ACTIVE);
        entity.setHoatDong(true);
        return entity;
    }

    public void applyChiTietFields(
            HangMucChiTiet entity,
            UUID groupId,
            String tenHangMuc,
            Map<String, Object> row,
            HangMucImportMappingFields fields) {
        if (!groupId.equals(entity.getHangMucNhomId())) {
            entity.setHangMucNhomId(groupId);
        }
        entity.setTen(tenHangMuc.trim());
        String donVi = fieldResolver.readString(row, fields.donVi(), HangMucImportAttributeRole.DON_VI);
        BigDecimal khoiLuong = fieldResolver.readDecimal(row, fields.khoiLuong(), HangMucImportAttributeRole.KHOI_LUONG);
        BigDecimal donGia = fieldResolver.readDecimal(row, fields.donGia(), HangMucImportAttributeRole.DON_GIA);
        if (donVi != null) {
            entity.setDonVi(donVi);
        }
        // Ô trống = giữ nguyên giá trị cũ (upsert), không ép 0 đè lên số đã có.
        if (khoiLuong != null) {
            entity.setKhoiLuong(khoiLuong);
        }
        if (donGia != null) {
            entity.setDonGia(donGia);
        }
        String viTriThiCong = fieldResolver.readString(row, fields.viTriThiCong(), HangMucImportAttributeRole.VI_TRI_THI_CONG);
        if (viTriThiCong != null) {
            entity.setViTriThiCong(viTriThiCong);
        }
    }

    public void applyCongViecFields(
            HangMucCongViec entity,
            UUID chiTietId,
            String tenCongViec,
            RowQuantities qty) {
        entity.setHangMucChiTietId(chiTietId);
        entity.setTen(tenCongViec);
        entity.setDonVi(qty.donVi());
        entity.setKhoiLuong(qty.khoiLuong());
        entity.setDonGia(qty.donGia());
        entity.setViTriThiCong(qty.viTri());
        if (qty.thuTu() != null) {
            entity.setThuTu(qty.thuTu());
        } else if (entity.getThuTu() == null) {
            entity.setThuTu((short) 0);
        }
    }
}
