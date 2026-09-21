package vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NguoiDungThamChieuResponse {

    private UUID id;
    private String tenDangNhap;
    private String hoTen;
    private String tenCongTy;
    private String chucVu;
    private String ntNguoiPhuTrach;
    private String ntChucVuPhuTrach;
    private String ntGiamDoc;
    private String ntChucVuGiamDoc;
    private String cdtNguoiGiamSat;
    private String cdtChucVuGiamSat;
    private String cdtGiamDoc;
    private String cdtChucVuGiamDoc;
    private String cdtNguoiPhuTrach;
    private String cdtChucVuPhuTrach;
}
