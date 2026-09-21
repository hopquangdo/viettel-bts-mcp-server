package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.util.UUID;

/** File hồ sơ trạm tải lên thủ công theo danh mục — dùng cho các loại hồ sơ chưa có mẫu Word tự
 * động (Bàn giao mặt bằng, Giao nhiệm vụ giám sát/thi công, Nghiệm thu đầu vào vật liệu) và các
 * danh mục hoàn toàn tự do (Hồ sơ thiết kế, Thi công) — phục vụ "Xuất hồ sơ đầy đủ theo trạm".
 * Cùng mẫu tái sử dụng TepDinhKem như BienBanPhatSinhTepDinhKem, không gắn cứng 1 file/bản ghi. */
@Getter
@Setter
@Entity
@Table(name = "ho_so_tram_tep_dinh_kem")
public class HoSoTramTepDinhKem extends AuditableEntity {

    @Column(name = "hop_dong_doi_tuong_id", nullable = false)
    private UUID hopDongDoiTuongId;

    /** BAN_GIAO_MAT_BANG | GIAO_NHIEM_VU_GSTC | NGHIEM_THU_VAT_LIEU | HO_SO_THIET_KE | THI_CONG */
    @Column(name = "danh_muc", nullable = false, length = 50)
    private String danhMuc;

    @Column(name = "tep_dinh_kem_id", nullable = false)
    private UUID tepDinhKemId;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
