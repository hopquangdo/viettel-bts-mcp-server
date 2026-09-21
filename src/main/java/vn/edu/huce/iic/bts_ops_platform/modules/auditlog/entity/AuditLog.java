package vn.edu.huce.iic.bts_ops_platform.modules.auditlog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

    @Column(name = "nguoi_thuc_hien_id")
    private UUID nguoiThucHienId;

    @Column(name = "ten_nguoi_thuc_hien")
    private String tenNguoiThucHien;

    @Column(name = "hanh_dong", nullable = false, length = 100)
    private String hanhDong;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "ip", length = 100)
    private String ip;

    /** Trạm/đối tượng liên quan (hop_dong_doi_tuong.id) — null với hành động không gắn trạm. */
    @Column(name = "doi_tuong_id")
    private UUID doiTuongId;

    /** Hợp đồng liên quan — null với hành động ngoài phạm vi hợp đồng (đăng nhập, tài khoản...). */
    @Column(name = "hop_dong_id")
    private UUID hopDongId;

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private Instant ngayTao;

    @PrePersist
    protected void onCreate() {
        ngayTao = Instant.now();
    }
}
