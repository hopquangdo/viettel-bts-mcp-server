package vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hop_dong_luu_tru")
public class HopDongLuuTru extends AuditableEntity {

    @Column(name = "hop_dong_id", nullable = false, unique = true)
    private UUID hopDongId;

    /** active | archived */
    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "active";

    @Column(name = "ngay_archive")
    private Instant ngayArchive;

    @Column(name = "nguoi_archive_id")
    private UUID nguoiArchiveId;

    @Column(name = "nguoi_archive_ten")
    private String nguoiArchiveTen;

    @Column(name = "ghi_chu", columnDefinition = "text")
    private String ghiChu;
}
