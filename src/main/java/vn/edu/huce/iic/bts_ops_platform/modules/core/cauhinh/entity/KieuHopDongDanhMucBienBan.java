package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.util.UUID;

/** Gán 1 danh mục biên bản vào 1 kiểu hợp đồng — tập hợp các bản ghi active của 1 kieuHopDongId
 * chính là checklist hồ sơ hiển thị cho hợp đồng thuộc kiểu đó (thay cho mảng REQUIRED_LOAI
 * hardcode dùng chung cho mọi kiểu HĐ trước đây). */
@Getter
@Setter
@Entity
@Table(name = "kieu_hop_dong_danh_muc_bien_ban")
public class KieuHopDongDanhMucBienBan extends AuditableEntity {

    @Column(name = "kieu_hop_dong_id", nullable = false)
    private UUID kieuHopDongId;

    @Column(name = "danh_muc_bien_ban_id", nullable = false)
    private UUID danhMucBienBanId;

    /** Thứ tự hiển thị trong checklist của riêng kiểu HĐ này — null thì dùng thuTuMacDinh của
     * danh mục. */
    @Column(name = "thu_tu")
    private Integer thuTu;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
