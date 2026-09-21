package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "luong_trang_thai_buoc")
public class LuongTrangThaiBuoc {

    @EmbeddedId
    private LuongTrangThaiBuocId id;

    @Column(name = "thu_tu", nullable = false)
    private Short thuTu = 0;

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private Instant ngayTao;

    @Column(name = "ngay_cap_nhat", nullable = false)
    private Instant ngayCapNhat;

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
