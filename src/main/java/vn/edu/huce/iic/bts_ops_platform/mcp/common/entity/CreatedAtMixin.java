package vn.edu.huce.iic.bts_ops_platform.mcp.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public abstract class CreatedAtMixin {

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private Instant ngayTao;

    @PrePersist
    protected void onCreateTimestamp() {
        ngayTao = Instant.now();
    }
}
