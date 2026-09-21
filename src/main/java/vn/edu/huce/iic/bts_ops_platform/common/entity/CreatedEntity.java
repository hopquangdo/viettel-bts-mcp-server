package vn.edu.huce.iic.bts_ops_platform.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public abstract class CreatedEntity extends BaseEntity {

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private Instant ngayTao;

    @Column(name = "ngay_xoa")
    private Instant ngayXoa;

    public boolean isDeleted() {
        return ngayXoa != null;
    }

    @PrePersist
    protected void onCreate() {
        ngayTao = Instant.now();
    }
}
