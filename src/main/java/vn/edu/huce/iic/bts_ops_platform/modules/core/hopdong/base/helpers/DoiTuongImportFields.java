package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers.HangMucImportFields;

import java.util.UUID;

public final class DoiTuongImportFields {

    public static final String NHOM_UU_TIEN = "Nhóm ưu tiên";

    private DoiTuongImportFields() {
    }

    public static boolean supportsNhomUuTien(UUID doiTuongQuanLyId) {
        return doiTuongQuanLyId != null && !HangMucImportFields.DOI_TUONG_ID.equals(doiTuongQuanLyId);
    }
}
