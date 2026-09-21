package vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "phan_anh_nguoi_dung")
public class PhanAnhNguoiDung extends AuditableEntity {

    @Column(name = "nguoi_dung_id", nullable = false)
    private UUID nguoiDungId;

    @Column(name = "loai", nullable = false, length = 50)
    private String loai;

    @Column(name = "tieu_de", nullable = false)
    private String tieuDe;

    @Column(name = "noi_dung", columnDefinition = "text")
    private String noiDung;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "moi";
}
