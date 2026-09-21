package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.util.UUID;

/** Liên kết 1 biên bản phát sinh với N tệp đính kèm (thiết kế/dự toán điều chỉnh...) — cùng mẫu
 * với HopDongTepDinhKem, tránh gắn cứng 1 file/bản ghi như VuongMac/SanLuongAnh. */
@Getter
@Setter
@Entity
@Table(name = "bien_ban_phat_sinh_tep_dinh_kem")
public class BienBanPhatSinhTepDinhKem extends AuditableEntity {

    @Column(name = "bien_ban_phat_sinh_id", nullable = false)
    private UUID bienBanPhatSinhId;

    @Column(name = "tep_dinh_kem_id", nullable = false)
    private UUID tepDinhKemId;

    /** THIET_KE_DIEU_CHINH | DU_TOAN_DIEU_CHINH | KHAC */
    @Column(name = "loai_tai_lieu", length = 50)
    private String loaiTaiLieu;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
