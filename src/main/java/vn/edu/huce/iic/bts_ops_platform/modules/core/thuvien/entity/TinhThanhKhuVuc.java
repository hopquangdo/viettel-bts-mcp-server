package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tinh_thanh_khu_vuc")
public class TinhThanhKhuVuc {

    @Id
    @Column(name = "tinh_thanh_id", nullable = false)
    private UUID tinhThanhId;

    @Column(name = "khu_vuc_id", nullable = false)
    private UUID khuVucId;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private Instant ngayTao;

    @Column(name = "ngay_cap_nhat", nullable = false)
    private Instant ngayCapNhat;

    @Column(name = "ngay_xoa")
    private Instant ngayXoa;

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
