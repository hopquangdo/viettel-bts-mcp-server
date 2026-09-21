package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto;

import lombok.Getter;
import lombok.Setter;

/** Thông tin người ký — nhập 1 lần khi xuất, dùng chung cho cả lô biên bản. */
@Getter
@Setter
public class BienBanNguoiKyRequest {

    /** Người giám sát của chủ đầu tư — ký Biên bản số 1 + Nhật ký thi công. */
    private String cdtNguoiGiamSat;
    private String cdtChucVuGiamSat;

    /** Giám đốc chủ đầu tư — ký Biên bản số 2. */
    private String cdtGiamDoc;
    private String cdtChucVuGiamDoc;

    /** Người phụ trách phía chủ đầu tư — ký Biên bản số 2. */
    private String cdtNguoiPhuTrach;
    private String cdtChucVuPhuTrach;

    /** Người phụ trách phía nhà thầu — ký cả 3 loại biên bản. */
    private String ntNguoiPhuTrach;
    private String ntChucVuPhuTrach;

    /** Giám đốc nhà thầu — ký Biên bản số 2. */
    private String ntGiamDoc;
    private String ntChucVuGiamDoc;
}
