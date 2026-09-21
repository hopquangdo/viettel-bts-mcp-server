package vn.edu.huce.iic.bts_ops_platform.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private Instant ngayTao;

    @Column(name = "ngay_cap_nhat", nullable = false)
    private Instant ngayCapNhat;

    @Column(name = "ngay_xoa")
    private Instant ngayXoa;

    public boolean isDeleted() {
        return ngayXoa != null;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        ngayTao = now;
        ngayCapNhat = now;
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = Instant.now();
    }
}
